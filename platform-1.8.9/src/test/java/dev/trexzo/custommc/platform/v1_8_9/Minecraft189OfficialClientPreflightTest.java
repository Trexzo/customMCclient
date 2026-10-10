package dev.trexzo.custommc.platform.v1_8_9;

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

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real official (not fixture) 1.8.9 class mapping/raycast acceptance.
 * The dedicated CI job downloads this JAR to a transient private runner path.
 */
final class Minecraft189OfficialClientPreflightTest {
    private static final String OFFICIAL_SHA1 =
            "3870888a6c3d349d3771a3e9d16c9bf5e076b908";
    private static final long OFFICIAL_SIZE = 8461484L;

    @Test
    void actualMojang189RaycastClassBytesMatchAndTransform() throws Exception {
        final String pathValue = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(pathValue, "required official Mojang 1.8.9 JAR path is absent");
            assertFalse(pathValue.trim().isEmpty(), "empty official client path");
        } else {
            Assumptions.assumeTrue(pathValue != null && !pathValue.trim().isEmpty(),
                    "No real client binary in ordinary offline CI");
        }
        final Path jarPath = Paths.get(pathValue);
        assertTrue(Files.isRegularFile(jarPath), "official client jar not found");
        assertEquals(OFFICIAL_SIZE, Files.size(jarPath), "official client size");
        assertEquals(OFFICIAL_SHA1, digestFile(jarPath), "official client SHA1");

        final Map<String, byte[]> raw = new LinkedHashMap<String, byte[]>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            for (String name : Arrays.asList("bfk", "bda", "pk", "aug")) {
                final JarEntry entry = jar.getJarEntry(name + ".class");
                assertNotNull(entry, "official obfuscated class missing: " + name);
                final byte[] bytes;
                try (InputStream input = jar.getInputStream(entry)) {
                    bytes = readAll(input);
                }
                assertEquals(name, new ClassReader(bytes).getClassName(),
                        "actual client contains unexpected obfuscated class identity");
                raw.put(name, bytes);
            }
        }

        Minecraft189ClassShapeVerifier.verifyNativeRaycastCalls(raw.get("bfk"));

        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();
        final byte[] transformedRenderer =
                transformer.transform("bfk", raw.get("bfk"));
        assertEquals("bfk", new ClassReader(transformedRenderer).getClassName());
        assertFalse(Arrays.equals(raw.get("bfk"), transformedRenderer),
                "the real renderer must receive exact native raycast hooks");

        final int[] nativeCalls = new int[5];
        final int[] bridgeHooks = new int[4];
        new ClassReader(transformedRenderer).accept(
                new ClassVisitor(Opcodes.ASM9) {
                    @Override public MethodVisitor visitMethod(
                            final int access, final String name, final String desc,
                            final String signature, final String[] exceptions) {
                        if (!"a".equals(name) || !"(F)V".equals(desc)) return null;
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override public void visitMethodInsn(
                                    final int opcode, final String owner,
                                    final String method, final String descriptor,
                                    final boolean itf) {
                                if (opcode == Opcodes.INVOKEVIRTUAL) {
                                    if (owner.equals("bda") && method.equals("d")
                                            && descriptor.equals("()F")) nativeCalls[0]++;
                                    if (owner.equals("bda") && method.equals("i")
                                            && descriptor.equals("()Z")) nativeCalls[1]++;
                                    if (owner.equals("pk") && method.equals("a")
                                            && descriptor.equals("(DF)Lauh;")) nativeCalls[2]++;
                                    if (owner.equals("pk") && method.equals("ao")
                                            && descriptor.equals("()F")) nativeCalls[3]++;
                                    if (owner.equals("aug") && method.equals("b")
                                            && descriptor.equals("(DDD)Laug;")) nativeCalls[4]++;
                                }
                                if (opcode == Opcodes.INVOKESTATIC
                                        && owner.endsWith("/Minecraft189RuntimeBridge")) {
                                    if (method.equals("raycastBlockDistance")
                                            && descriptor.equals("(F)F")) bridgeHooks[0]++;
                                    if (method.equals("raycastExtendedBranch")
                                            && descriptor.equals("(Z)Z")) bridgeHooks[1]++;
                                    if (method.equals("raycastExtendedDistance")
                                            && descriptor.equals("()D")) bridgeHooks[2]++;
                                    if (method.equals("raycastHitboxBorder")
                                            && descriptor.equals("(ZF)F")) bridgeHooks[3]++;
                                }
                            }
                        };
                    }
                }, 0);
        assertTrue(nativeCalls[0] >= 1 && nativeCalls[1] >= 1
                && nativeCalls[2] >= 1, "vanilla raycast dependencies missing");
        assertEquals(1, nativeCalls[3], "vanilla collision border call");
        assertEquals(2, nativeCalls[4], "vanilla AABB expansion calls");
        assertArrayEquals(new int[]{1, 1, 1, 1}, bridgeHooks,
                "expected exact native Reach + Hitbox bridge injection counts");

        for (String name : Arrays.asList("bda", "pk", "aug")) {
            final byte[] transformed = transformer.transform(name, raw.get(name));
            assertEquals(name, new ClassReader(transformed).getClassName(),
                    "transformed class identity changed: " + name);
            System.out.println("REAL_189_TRANSFORM_PASS=" + name
                    + " source_sha256=" + sha256(raw.get(name)));
        }
        System.out.println("REAL_189_RAYCAST_HOOKS_PASS=YES");
        System.out.println("REAL_189_RENDERER_SOURCE_SHA256=" + sha256(raw.get("bfk")));
        System.out.println("REAL_189_RENDERER_PATCH_SHA256=" + sha256(transformedRenderer));
        System.out.println("CLIENT_BINARY_NOT_PERSISTED=YES");
    }

    @Test
    void allHandledMappedClassesTransformFromRealOfficialClient()
            throws Exception {
        final String value = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(value, "required official Mojang client path missing");
            assertFalse(value.trim().isEmpty(), "required official client path empty");
        } else {
            Assumptions.assumeTrue(value != null && !value.trim().isEmpty(),
                    "No Mojang jar in standard offline CI");
        }
        final Path path = Paths.get(value);
        assertTrue(Files.isRegularFile(path), "missing client JAR");
        assertEquals(OFFICIAL_SIZE, Files.size(path));
        assertEquals(OFFICIAL_SHA1, digestFile(path));
        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();
        final java.util.Set<String> handled = new java.util.TreeSet<String>();
        final java.util.List<String> missingOrFailed =
                new java.util.ArrayList<String>();
        try (JarFile jar = new JarFile(path.toFile())) {
            final java.util.Enumeration<JarEntry> iterator = jar.entries();
            while (iterator.hasMoreElements()) {
                final JarEntry entry = iterator.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
                final String internal = entry.getName().substring(
                        0, entry.getName().length() - ".class".length());
                final String binary = internal.replace('/', '.');
                if (!transformer.handles(binary)) continue;
                handled.add(binary);
                final byte[] source;
                try (InputStream input = jar.getInputStream(entry)) {
                    source = readAll(input);
                }
                assertEquals(internal, new ClassReader(source).getClassName(),
                        "official JAR path-to-owner mismatch");
                try {
                    final byte[] transformed = transformer.transform(binary, source);
                    assertNotNull(transformed, "transform returned null: " + binary);
                    assertEquals(internal, new ClassReader(transformed).getClassName(),
                            "transform changed mapped owner: " + binary);
                    System.out.println("OFFICIAL_189_TRANSFORM_OK=" + binary
                            + " sha256=" + sha256(source));
                } catch (RuntimeException invalidShape) {
                    missingOrFailed.add(binary + ": " + invalidShape.getMessage());
                }
            }
        }
        // 24 mapped runtime owners plus the Minecraft entry-point class.
        // Reject silently missing owners instead of passing a partial list.
        assertEquals(25, handled.size(),
                "not all declared 1.8.9 transform owners occur in official JAR: " + handled);
        assertTrue(missingOrFailed.isEmpty(),
                "real vanilla classes failed mapped transformation: " + missingOrFailed);
        System.out.println("OFFICIAL_189_COMPLETE_MAPPED_OWNERS_PASS=" + handled.size());
    }

    private static byte[] readAll(final InputStream input) throws Exception {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[32768];
        for (int n; (n = input.read(buffer)) != -1;) {
            stream.write(buffer, 0, n);
        }
        return stream.toByteArray();
    }
    private static String digestFile(final Path file) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("SHA-1");
        try (InputStream in = Files.newInputStream(file)) {
            final byte[] bytes = new byte[32768];
            for (int n; (n = in.read(bytes)) != -1;) md.update(bytes, 0, n);
        }
        return hex(md.digest());
    }
    private static String sha256(final byte[] bytes) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
    private static String hex(final byte[] hash) {
        final StringBuilder result = new StringBuilder();
        for (byte value : hash) {
            result.append(Character.forDigit((value >> 4) & 15, 16));
            result.append(Character.forDigit(value & 15, 16));
        }
        return result.toString();
    }
}
