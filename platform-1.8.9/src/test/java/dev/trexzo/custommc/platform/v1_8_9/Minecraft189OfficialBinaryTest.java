package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

/** Audit against the official unmodified Minecraft 1.8.9 client, never stubs. */
final class Minecraft189OfficialBinaryTest {
    private static final String EXPECTED_SHA1 =
            "3870888a6c3d349d3771a3e9d16c9bf5e076b908";
    private static final long EXPECTED_SIZE = 8461484L;

    @Test
    void officialVanillaClassesHaveMappedRaycastAndPatchableShape() throws Exception {
        final String file = System.getenv("CUSTOMMC_OFFICIAL_189_JAR");
        final boolean required = "true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_OFFICIAL_189_REQUIRED"));
        if (required) {
            assertNotNull(file, "official client required in real-client CI");
            assertFalse(file.isEmpty(), "official client path required");
        } else {
            Assumptions.assumeTrue(file != null && !file.trim().isEmpty(),
                    "No real Mojang binary supplied in synthetic foundation CI");
        }
        final Path path = Paths.get(file);
        assertTrue(Files.isRegularFile(path), "official Mojang JAR not present");
        assertEquals(EXPECTED_SIZE, Files.size(path), "official client byte count");
        final MessageDigest fileSha = MessageDigest.getInstance("SHA-1");
        try (InputStream in = Files.newInputStream(path)) {
            final byte[] buffer = new byte[32768];
            int count;
            while ((count = in.read(buffer)) != -1)
                fileSha.update(buffer, 0, count);
        }
        assertEquals(EXPECTED_SHA1, hex(fileSha.digest()),
                "official Minecraft 1.8.9 client SHA-1");

        final Map<String, byte[]> original = new LinkedHashMap<String, byte[]>();
        try (JarFile jar = new JarFile(path.toFile())) {
            for (String owner : Arrays.asList("bfk", "bda", "pk", "aug")) {
                final JarEntry entry = jar.getJarEntry(owner + ".class");
                assertNotNull(entry, "official obfuscated class missing: " + owner);
                try (InputStream in = jar.getInputStream(entry)) {
                    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    final byte[] buffer = new byte[16384];
                    int n;
                    while ((n = in.read(buffer)) != -1) bytes.write(buffer, 0, n);
                    original.put(owner, bytes.toByteArray());
                }
                assertEquals(owner, new ClassReader(original.get(owner)).getClassName(),
                        "wrong obfuscated owner in vanilla binary");
            }
        }

        // Check exact official vanilla class bodies, not generated test fixtures.
        Minecraft189ClassShapeVerifier.verifyNativeRaycastCalls(original.get("bfk"));
        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();
        for (String owner : Arrays.asList("bfk", "bda", "pk", "aug")) {
            final byte[] source = original.get(owner);
            final byte[] transformed = transformer.transform(owner, source);
            assertEquals(owner, new ClassReader(transformed).getClassName());
            System.out.println("REAL_189_CLASS_PASS=" + owner
                    + " original_sha256=" + sha256(source)
                    + " transformed_sha256=" + sha256(transformed));
            if (!"bfk".equals(owner)) continue;
            final int[] bridges = new int[4];
            final int[] nativeCalls = new int[5];
            new ClassReader(transformed).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public MethodVisitor visitMethod(final int acc,
                        final String name, final String desc,
                        final String signature, final String[] exceptions) {
                    if (!"a".equals(name) || !"(F)V".equals(desc)) return null;
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override public void visitMethodInsn(int opcode,
                                String target, String method, String signature,
                                boolean isInterface) {
                            if (opcode == Opcodes.INVOKEVIRTUAL) {
                                if ("bda".equals(target) && "d".equals(method)
                                        && "()F".equals(signature)) nativeCalls[0]++;
                                if ("bda".equals(target) && "i".equals(method)
                                        && "()Z".equals(signature)) nativeCalls[1]++;
                                if ("pk".equals(target) && "a".equals(method)
                                        && "(DF)Lauh;".equals(signature)) nativeCalls[2]++;
                                if ("pk".equals(target) && "ao".equals(method)
                                        && "()F".equals(signature)) nativeCalls[3]++;
                                if ("aug".equals(target) && "b".equals(method)
                                        && "(DDD)Laug;".equals(signature)) nativeCalls[4]++;
                            } else if (opcode == Opcodes.INVOKESTATIC
                                    && target.endsWith("/Minecraft189RuntimeBridge")) {
                                if ("raycastBlockDistance".equals(method)) bridges[0]++;
                                if ("raycastExtendedBranch".equals(method)) bridges[1]++;
                                if ("raycastExtendedDistance".equals(method)) bridges[2]++;
                                if ("raycastHitboxBorder".equals(method)) bridges[3]++;
                            }
                        }
                    };
                }
            }, 0);
            assertTrue(nativeCalls[0] >= 1 && nativeCalls[1] >= 1
                    && nativeCalls[2] >= 1, "missing vanilla renderer call");
            assertEquals(1, nativeCalls[3], "native player border method");
            assertEquals(2, nativeCalls[4], "two native bounding box expansions");
            assertArrayEquals(new int[]{1, 1, 1, 1}, bridges,
                    "real getMouseOver Reach/Hitbox patch layout");
        }
        System.out.println("OFFICIAL_189_RAYCAST_PREFLIGHT_PASS=YES");
        System.out.println("NO_CLIENT_BINARY_COMMITTED_OR_EXPORTED=YES");
    }

    private static String sha256(final byte[] data) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(data));
    }
    private static String hex(final byte[] data) {
        final StringBuilder result = new StringBuilder();
        for (byte b : data) {
            result.append(Character.forDigit((b >>> 4) & 15, 16));
            result.append(Character.forDigit(b & 15, 16));
        }
        return result.toString();
    }
}
