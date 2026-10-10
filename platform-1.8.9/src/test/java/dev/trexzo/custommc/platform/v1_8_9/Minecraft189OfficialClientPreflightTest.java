package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

/** Preflight using authentic checksum-pinned Mojang 1.8.9 class files, not stubs. */
final class Minecraft189OfficialClientPreflightTest {
    private static final String SHA1 =
            "3870888a6c3d349d3771a3e9d16c9bf5e076b908";
    private static final long SIZE = 8461484L;

    @Test
    void realMojangClientNativeRaycastAndMappingsAcceptActualBytecode()
            throws Exception {
        final String pathname = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        final boolean required = "true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"));
        if (required) {
            assertNotNull(pathname, "Official 1.8.9 client JAR required");
            assertFalse(pathname.trim().isEmpty(), "empty client JAR path");
        } else {
            Assumptions.assumeTrue(pathname != null && !pathname.trim().isEmpty(),
                    "Vanilla 1.8.9 JAR intentionally absent in standard CI");
        }

        final Path path = Paths.get(pathname);
        assertTrue(Files.isRegularFile(path), "Official 1.8.9 client JAR missing");
        assertEquals(SIZE, Files.size(path), "Pinned official client size");
        assertEquals(SHA1, sha1(path), "Pinned official client SHA1");
        final Map<String, byte[]> bytes = new LinkedHashMap<String, byte[]>();
        try (JarFile jar = new JarFile(path.toFile())) {
            for (String name : Arrays.asList("bfk", "bda", "pk", "aug")) {
                final JarEntry entry = jar.getJarEntry(name + ".class");
                assertNotNull(entry, "Official obfuscated class missing: " + name);
                final byte[] data;
                try (InputStream stream = jar.getInputStream(entry)) {
                    data = read(stream);
                }
                assertEquals(name, new ClassReader(data).getClassName(),
                        "Real obfuscated class identity mismatch: " + name);
                bytes.put(name, data);
            }
        }

        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();
        // The exact vanilla getMouseOver must call all expected mapped methods.
        Minecraft189ClassShapeVerifier.verifyNativeRaycastCalls(bytes.get("bfk"));
        final byte[] rendered = transformer.transform("bfk", bytes.get("bfk"));
        assertTrue(rendered.length >= bytes.get("bfk").length,
                "Real renderer expected to include instrumentation");
        final int[] nativeCalls = new int[5];
        final int[] bridgeCalls = new int[4];
        new ClassReader(rendered).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(final int access,
                    final String name, final String descriptor,
                    final String signature, final String[] exceptions) {
                if (!"a".equals(name) || !"(F)V".equals(descriptor)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(final int opcode,
                            final String owner, final String method,
                            final String desc, final boolean isInterface) {
                        if (opcode == Opcodes.INVOKEVIRTUAL) {
                            if ("bda".equals(owner) && "d".equals(method)
                                    && "()F".equals(desc)) nativeCalls[0]++;
                            if ("bda".equals(owner) && "i".equals(method)
                                    && "()Z".equals(desc)) nativeCalls[1]++;
                            if ("pk".equals(owner) && "a".equals(method)
                                    && "(DF)Lauh;".equals(desc)) nativeCalls[2]++;
                            if ("pk".equals(owner) && "ao".equals(method)
                                    && "()F".equals(desc)) nativeCalls[3]++;
                            if ("aug".equals(owner) && "b".equals(method)
                                    && "(DDD)Laug;".equals(desc)) nativeCalls[4]++;
                        }
                        if (opcode == Opcodes.INVOKESTATIC
                                && owner.endsWith("/Minecraft189RuntimeBridge")) {
                            if ("raycastBlockDistance".equals(method)) bridgeCalls[0]++;
                            if ("raycastExtendedBranch".equals(method)) bridgeCalls[1]++;
                            if ("raycastExtendedDistance".equals(method)) bridgeCalls[2]++;
                            if ("raycastHitboxBorder".equals(method)) bridgeCalls[3]++;
                        }
                    }
                };
            }
        }, 0);
        assertTrue(nativeCalls[0] >= 1 && nativeCalls[1] >= 1
                && nativeCalls[2] >= 1, "Native reach/raytrace dependency missing");
        assertEquals(1, nativeCalls[3], "Native hitbox border call");
        assertEquals(2, nativeCalls[4], "Two native AABB expansion call sites");
        assertArrayEquals(new int[]{1, 1, 2, 1}, bridgeCalls,
                "Expected bounded native Reach and Hitbox hook sites");

        for (String name : Arrays.asList("bda", "pk", "aug")) {
            final byte[] modified = transformer.transform(name, bytes.get(name));
            assertEquals(name, new ClassReader(modified).getClassName());
            System.out.println("OFFICIAL_189_CLASS_PASS=" + name
                    + " original_sha256=" + sha256(bytes.get(name)));
        }
        System.out.println("OFFICIAL_189_RAYCAST_PASS=YES");
        System.out.println("OFFICIAL_189_RENDERER_ORIGINAL_SHA256=" + sha256(bytes.get("bfk")));
        System.out.println("OFFICIAL_189_RENDERER_TRANSFORMED_SHA256=" + sha256(rendered));
        System.out.println("MOJANG_BINARY_NOT_COMMITTED=YES");
    }

    private static byte[] read(final InputStream input) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final byte[] buf = new byte[32768];
        int count;
        while ((count = input.read(buf)) >= 0) {
            if (count > 0) output.write(buf, 0, count);
        }
        return output.toByteArray();
    }

    private static String sha1(final Path path) throws Exception {
        final MessageDigest sha = MessageDigest.getInstance("SHA-1");
        try (InputStream in = Files.newInputStream(path)) {
            final byte[] data = new byte[32768];
            int n;
            while ((n = in.read(data)) >= 0) {
                if (n > 0) sha.update(data, 0, n);
            }
        }
        return hex(sha.digest());
    }

    private static String sha256(final byte[] bytes) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static String hex(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte value : bytes) {
            sb.append(Character.forDigit((value >>> 4) & 15, 16))
                    .append(Character.forDigit(value & 15, 16));
        }
        return sb.toString();
    }
}
