package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the whole registered transformer, not merely the Combat raycast.
 * Uses only ephemeral, checksum-verified, genuinely obfuscated Mojang classes.
 * Does not execute the graphical Minecraft client or network into servers.
 */
final class Minecraft189OfficialFullTransformPreflightTest {
    private static final String OFFICIAL_SHA1 =
            "3870888a6c3d349d3771a3e9d16c9bf5e076b908";
    private static final long OFFICIAL_SIZE = 8461484L;

    @Test
    void allRegisteredMinecraft189TargetsAcceptExactVanillaBinary() throws Exception {
        final String filename = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        final boolean required = "true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"));
        if (required) {
            assertNotNull(filename, "native preflight requires Mojang JAR");
            assertFalse(filename.trim().isEmpty(), "native preflight JAR is empty");
        } else {
            Assumptions.assumeTrue(filename != null && !filename.trim().isEmpty(),
                    "official Mojang 1.8.9 binary unavailable in ordinary CI");
        }
        final Path file = Paths.get(filename);
        assertTrue(Files.isRegularFile(file), "missing vanilla binary");
        assertEquals(OFFICIAL_SIZE, Files.size(file), "incorrect official JAR size");
        final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream input = Files.newInputStream(file)) {
            final byte[] block = new byte[32768];
            int n;
            while ((n = input.read(block)) != -1) sha1.update(block, 0, n);
        }
        final StringBuilder got = new StringBuilder();
        for (byte b : sha1.digest()) {
            got.append(Character.forDigit((b >>> 4) & 15, 16));
            got.append(Character.forDigit(b & 15, 16));
        }
        assertEquals(OFFICIAL_SHA1, got.toString(), "real binary hash mismatch");

        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();
        final List<String> failures = new ArrayList<String>();
        final Set<String> handled = new HashSet<String>();
        int unhandled = 0;
        try (JarFile jar = new JarFile(file.toFile())) {
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class"))
                    continue;
                final String binaryName = entry.getName().substring(
                        0, entry.getName().length() - 6).replace('/', '.');
                if (!transformer.handles(binaryName)) {
                    unhandled++;
                    continue;
                }
                assertTrue(handled.add(binaryName),
                        "duplicate official mapped class " + binaryName);
                final byte[] original;
                try (InputStream input = jar.getInputStream(entry)) {
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    final byte[] block = new byte[32768];
                    int n;
                    while ((n = input.read(block)) != -1) out.write(block, 0, n);
                    original = out.toByteArray();
                }
                try {
                    final ClassReader input = new ClassReader(original);
                    assertEquals(entry.getName(), input.getClassName() + ".class",
                            "vanilla class entry identity mismatch");
                    final byte[] transformed = transformer.transform(binaryName, original);
                    assertNotNull(transformed, "transformer returned null");
                    assertEquals(input.getClassName(),
                            new ClassReader(transformed).getClassName(),
                            "native class changed binary identity");
                    System.out.println("REAL_189_TRANSFORM_PASS=" + binaryName);
                } catch (Throwable problem) {
                    final String message = binaryName + ": "
                            + problem.getClass().getSimpleName() + ": "
                            + String.valueOf(problem.getMessage());
                    failures.add(message);
                    System.err.println("REAL_189_TRANSFORM_FAIL=" + message);
                }
            }
        }

        assertTrue(handled.contains("bfk"), "renderer missing");
        assertTrue(handled.contains("pk"), "Entity missing");
        assertTrue(handled.contains("bda"), "PlayerController missing");
        assertTrue(handled.contains("aug"), "AxisAlignedBB missing");
        assertTrue(handled.contains("ave"), "Minecraft main instance missing");
        System.out.println("REAL_189_MAPPED_CLASS_COUNT=" + handled.size());
        System.out.println("REAL_189_UNMODIFIED_CLASS_COUNT=" + unhandled);
        assertTrue(failures.isEmpty(),
                "Actual vanilla 1.8.9 mapped class compatibility failures ("
                        + failures.size() + "): " + failures);
        assertTrue(handled.size() >= 20,
                "suspiciously few mapped 1.8.9 classes matched official JAR");
        System.out.println("REAL_189_FULL_TRANSFORM_PREFLIGHT_PASS=YES");
    }
}
