package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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

/**
 * Real obfuscated 1.8.9 Mojang client-class acceptance gate.
 * No source stubs, no fake bytecode, no launch/credentials/network during test.
 * Only enabled by the dedicated ephemeral official-binary workflow.
 */
final class Minecraft189OfficialClientPreflightTest {
    private static final String OFFICIAL_SHA1 =
            "3870888a6c3d349d3771a3e9d16c9bf5e076b908";
    private static final long OFFICIAL_SIZE = 8461484L;

    @Test
    void officialBinaryHasExactlyMappedRaycastAndTransformsWithoutGuesswork()
            throws Exception {
        final String pathname = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        final boolean required = "true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"));
        if (required) {
            assertNotNull(pathname, "real client JAR required by native CI");
            assertFalse(pathname.trim().isEmpty(), "empty official client path");
        } else {
            Assumptions.assumeTrue(pathname != null && !pathname.trim().isEmpty(),
                    "official Mojang client jar intentionally absent in standard CI");
        }
        final Path path = Paths.get(pathname);
        assertTrue(Files.isRegularFile(path), "official 1.8.9 JAR missing");
        assertEquals(OFFICIAL_SIZE, Files.size(path), "official client byte size");
        assertEquals(OFFICIAL_SHA1, sha1(path), "official client SHA-1");
        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();

        // Names and obfuscated class bodies are from the downloaded binary,
        // never generated synthetic test stubs.
        final Map<String, byte[]> original = new LinkedHashMap<String, byte[]>();
        try (JarFile jar = new JarFile(path.toFile())) {
            for (String name : Arrays.asList("bfk", "bda", "pk", "aug")) {
                final String entry = name + ".class";
                final JarEntry member = jar.getJarEntry(entry);
                assertNotNull(member, "missing official obfuscated " + entry);
                final byte[] bytes;
                try (InputStream in = jar.getInputStream(member)) {
                    bytes = readAll(in);
                }
                assertEquals(name, new ClassReader(bytes).getClassName(),
                        "unexpected internal class " + name);
                original.put(name, bytes);
            }
        }

        Minecraft189ClassShapeVerifier.verifyNativeRaycastCalls(
                original.get("bfk"));
        final int[] observed = new int[5];
        final int[] bridge = new int[4];
        final byte[] result = transformer.transform("bfk", original.get("bfk"));
        assertTrue(result.length > original.get("bfk").length,
                "real renderer expected to contain added native bridge hooks");
        new ClassReader(result).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(
                    final int access, final String name, final String descriptor,
                    final String signature, final String[] exceptions) {
                if (!"a".equals(name) || !"(F)V".equals(descriptor)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(final int opcode,
                            final String owner, final String method,
                            final String desc, final boolean iface) {
                        if (opcode == Opcodes.INVOKEVIRTUAL) {
                            if ("bda".equals(owner) && "d".equals(method)
                                    && "()F".equals(desc)) observed[0]++;
                            if ("bda".equals(owner) && "i".equals(method)
                                    && "()Z".equals(desc)) observed[1]++;
                            if ("pk".equals(owner) && "a".equals(method)
                                    && "(DF)Lauh;".equals(desc)) observed[2]++;
                            if ("pk".equals(owner) && "ao".equals(method)
                                    && "()F".equals(desc)) observed[3]++;
                            if ("aug".equals(owner) && "b".equals(method)
                                    && "(DDD)Laug;".equals(desc)) observed[4]++;
                        } else if (opcode == Opcodes.INVOKESTATIC
                                && owner.endsWith("/Minecraft189RuntimeBridge")) {
                            if ("raycastBlockDistance".equals(method)) bridge[0]++;
                            if ("raycastExtendedBranch".equals(method)) bridge[1]++;
                            if ("raycastExtendedDistance".equals(method)) bridge[2]++;
                            if ("raycastHitboxBorder".equals(method)) bridge[3]++;
                        }
                    }
                };
            }
        }, 0);
        assertTrue(observed[0] >= 1 && observed[1] >= 1 && observed[2] >= 1,
                "real native raytrace dependencies missing");
        assertEquals(1, observed[3], "original native collision border call");
        assertEquals(1, observed[4], "original native AABB expansion call");
        assertArrayEquals(new int[]{1, 1, 2, 1}, bridge,
                "expected exactly Reach and Hitbox bridge hooks");

        for (String name : Arrays.asList("bda", "pk", "aug")) {
            final byte[] transformed = transformer.transform(name, original.get(name));
            assertEquals(name, new ClassReader(transformed).getClassName(),
                    "transformed native class identity must be unchanged: " + name);
            System.out.println("REAL_189_CLASS_SHAPE_PASS=" + name
                    + " source_sha256=" + sha256(original.get(name)));
        }
        System.out.println("REAL_189_RAYCAST_MAPPING_PASS=YES");
        System.out.println("REAL_189_RENDERER_SOURCE_SHA256=" + sha256(original.get("bfk")));
        System.out.println("REAL_189_TRANSFORMED_RENDERER_SHA256=" + sha256(result));
        System.out.println("NO_JAR_BINARY_RETAINED=YES");
    }

    private static byte[] readAll(final InputStream stream) throws IOException {
        final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        final byte[] buffer = new byte[32768];
        int n;
        while ((n = stream.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
    private static String sha1(final Path path) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream in = Files.newInputStream(path)) {
            final byte[] block = new byte[32768];
            int n;
            while ((n = in.read(block)) != -1) digest.update(block, 0, n);
        }
        return hex(digest.digest());
    }
    private static String sha256(final byte[] data) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(data));
    }
    private static String hex(final byte[] bytes) {
        final StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Character.forDigit((b >>> 4) & 0xF, 16))
                    .append(Character.forDigit(b & 0xF, 16));
        }
        return result.toString();
    }
}
