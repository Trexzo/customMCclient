package dev.trexzo.custommc.platform.v1_8_9;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.bootstrap.CustomMcBootstrapMain;
import dev.trexzo.custommc.bootstrap.TransformingTargetClassLoader;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real official (not fixture) 1.8.9 class mapping/raycast acceptance.
 * The dedicated CI job downloads this JAR to a transient private runner path.
 */
final class Minecraft189OfficialClientPreflightTest {
    private static final String OFFICIAL_SHA1 =
            "3870888a6c3d349d3771a3e9d16c9bf5e076b908";
    private static final long OFFICIAL_SIZE = 8461484L;

    private static URL[] actualMojangRuntimeClasspath(final Path jar)
            throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_LIBRARIES_FILE");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "official managed library classpath missing");
            assertFalse(configured.trim().isEmpty(), "official library manifest empty");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "No verified official Mojang library inventory offline");
        }
        final Path manifest = Paths.get(configured).toAbsolutePath().normalize();
        assertTrue(Files.isRegularFile(manifest), "library inventory is missing");
        final Path root = manifest.getParent();
        final List<String> entries = Files.readAllLines(manifest, StandardCharsets.UTF_8);
        assertTrue(entries.size() >= 12, "incomplete 1.8.9 official library classpath");
        final List<URL> urls = new ArrayList<URL>();
        urls.add(jar.toUri().toURL());
        final Set<Path> seen = new TreeSet<Path>();
        boolean jopt = false;
        for (String item : entries) {
            assertFalse(item.trim().isEmpty(), "blank official library entry");
            final Path file = Paths.get(item).toAbsolutePath().normalize();
            assertTrue(file.startsWith(root), "library escaped verified temporary directory");
            assertTrue(Files.isRegularFile(file), "official library not found: " + file);
            assertTrue(file.getFileName().toString().endsWith(".jar"),
                    "official library is not a JAR");
            assertTrue(seen.add(file), "duplicate managed Mojang library: " + file);
            jopt |= file.getFileName().toString().contains("jopt-simple");
            urls.add(file.toUri().toURL());
        }
        assertTrue(jopt, "pinned official JOpt Simple not downloaded");
        System.out.println("OFFICIAL_189_VERIFIED_MANAGED_LIBRARIES=" + entries.size());
        return urls.toArray(new URL[urls.size()]);
    }

    /**
     * M396: exercise the actual signed Mojang main bytecode with the installed
     * bootstrap runtime and the production transformer. Stop at the very first
     * injected main instruction by an explicitly armed acceptance property;
     * normal clients continue into Minecraft, while this test never needs a
     * render thread, OS window, account or native libraries.
     */
    @Test
    void officialMinecraftMainEntersLiveBootstrapBeforeGraphics() throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "required genuine Minecraft JAR path absent");
            assertFalse(configured.trim().isEmpty(), "required JAR path empty");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "No genuine Minecraft binary available offline");
        }
        final Path jar = Paths.get(configured);
        assertTrue(Files.isRegularFile(jar), "official 1.8.9 JAR missing");
        assertEquals(OFFICIAL_SIZE, Files.size(jar), "official JAR size mismatch");
        assertEquals(OFFICIAL_SHA1, digestFile(jar), "official JAR checksum mismatch");

        final String key = Minecraft189RuntimeBridge.MAIN_ENTRY_ACCEPTANCE_PROPERTY;
        final String prior = System.getProperty(key);
        assertFalse(Minecraft189RuntimeBridge.active(),
                "the official acceptance test requires an unowned runtime bridge");

        final Minecraft189BootstrapRuntime runtime =
                (Minecraft189BootstrapRuntime)
                        new Minecraft189BootstrapInitializer().initialize(
                                new BootstrapContext(
                                        Minecraft189ClassTransformer.TARGET_MAIN_CLASS,
                                        new String[0]));
        try {
            assertTrue(Minecraft189RuntimeBridge.active(),
                    "production runtime initialization did not install the bridge");
            assertFalse(runtime.targetMainEntered(),
                    "main must not be marked entered before executing Mojang main");
            try (TransformingTargetClassLoader loader =
                         new TransformingTargetClassLoader(
                                 actualMojangRuntimeClasspath(jar),
                                 getClass().getClassLoader(),
                                 new Minecraft189ClassTransformer())) {
                assertSame(loader, Class.forName("joptsimple.OptionSpec", false, loader)
                        .getClassLoader(), "JOpt Simple resolved outside pinned Mojang libraries");
                final Class<?> main = Class.forName(
                        Minecraft189ClassTransformer.TARGET_MAIN_CLASS, true, loader);
                assertSame(loader, main.getClassLoader());
                final Method entry = main.getMethod("main", String[].class);
                assertEquals(Void.TYPE, entry.getReturnType());

                System.setProperty(key, "true");
                final InvocationTargetException reached =
                        assertThrows(InvocationTargetException.class,
                                () -> entry.invoke(null, (Object) new String[0]));
                assertTrue(reached.getCause() instanceof IllegalStateException,
                        "first hooked instruction did not signal the opt-in entry probe: "
                                + reached.getCause());
                assertEquals("CUSTOMMC_OFFICIAL_189_MAIN_ENTRY_PROBE_REACHED",
                        reached.getCause().getMessage());
                assertTrue(runtime.targetMainEntered(),
                        "transformed game main did not enter the real bootstrap bridge");
                assertFalse(runtime.hostInstalled(),
                        "probe must stop before game host or graphical initialization");
                System.out.println("OFFICIAL_189_REAL_BOOTSTRAP_MAIN_HANDOFF_PASS=YES");
            }
        } finally {
            if (prior == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, prior);
            }
            runtime.close();
            assertTrue(runtime.closed(), "runtime cleanup after probe failed");
            assertFalse(Minecraft189RuntimeBridge.active(),
                    "runtime bridge leaked after official main probe");
        }
        System.out.println("OFFICIAL_189_BOOTSTRAP_CLEANUP_PASS=YES");
    }

    /**
     * M397: invoke the actual CustomMcBootstrapMain pipeline with the pinned
     * Mojang client and ALL applicable managed 1.8.9 libraries on the runtime
     * classpath. This tests session ownership, target loader construction,
     * class initialization, reflection and cleanup as one real entry flow.
     *
     * A property arms the M396 first-instruction sentinel. No Minecraft
     * graphics, assets, native libraries or network session are started.
     */
    @Test
    void officialBootstrapPipelineEntersMojangMainWithOfficialLibraries()
            throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "required official client missing");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "Official Mojang client is absent in offline CI");
        }
        final Path jar = Paths.get(configured);
        assertTrue(Files.isRegularFile(jar), "official game jar absent");
        assertEquals(OFFICIAL_SIZE, Files.size(jar));
        assertEquals(OFFICIAL_SHA1, digestFile(jar));
        final URL[] runtimeUrls = actualMojangRuntimeClasspath(jar);

        final String key = Minecraft189RuntimeBridge.MAIN_ENTRY_ACCEPTANCE_PROPERTY;
        final String priorProbe = System.getProperty(key);
        final String priorClasspath = System.getProperty("java.class.path");
        assertNotNull(priorClasspath, "JVM classpath unavailable");
        assertFalse(Minecraft189RuntimeBridge.active(),
                "runtime bridge must be free before launching the real bootstrap");
        try {
            final StringBuilder classpath = new StringBuilder(priorClasspath);
            for (URL url : runtimeUrls) {
                classpath.append(File.pathSeparator);
                classpath.append(Paths.get(url.toURI()));
            }
            System.setProperty("java.class.path", classpath.toString());
            System.setProperty(key, "true");
            final IllegalStateException reached =
                    assertThrows(IllegalStateException.class,
                            () -> CustomMcBootstrapMain.main(new String[]{
                                    "--custommc-runtime",
                                    Minecraft189BootstrapInitializer.class.getName(),
                                    Minecraft189ClassTransformer.TARGET_MAIN_CLASS,
                                    "--username", "CIProbe"
                            }));
            assertEquals("CUSTOMMC_OFFICIAL_189_MAIN_ENTRY_PROBE_REACHED",
                    reached.getMessage(),
                    "actual bootstrap did not reach the first transformed Mojang main hook");
            assertFalse(Minecraft189RuntimeBridge.active(),
                    "bootstrap did not close its owned runtime after entry probe");
            System.out.println("OFFICIAL_189_FULL_BOOTSTRAP_HANDOFF_PASS=YES");
        } finally {
            System.setProperty("java.class.path", priorClasspath);
            if (priorProbe == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, priorProbe);
            }
            assertFalse(Minecraft189RuntimeBridge.active(),
                    "bootstrap runtime leaked on cleanup");
        }
    }

    /**
     * M395: discover the entire claimed transformer owner set in the exact
     * signed official Mojang 1.8.9 JAR and require JVM definition/linkage of
     * every owner through the production TransformingTargetClassLoader.
     *
     * Class.forName(..., false, loader) does not initialize classes or launch
     * the game. This is deliberately not a graphics or multiplayer test.
     */
    @Test
    void allOfficial189MappedOwnersLinkWithProductionLoader() throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "required official JAR path is missing");
            assertFalse(configured.trim().isEmpty(), "official JAR path is empty");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "No official Mojang 1.8.9 JAR in ordinary offline CI");
        }
        final Path path = Paths.get(configured);
        assertTrue(Files.isRegularFile(path), "official JAR missing");
        assertEquals(OFFICIAL_SIZE, Files.size(path), "official size mismatch");
        assertEquals(OFFICIAL_SHA1, digestFile(path), "official SHA-1 mismatch");

        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();
        final Set<String> handled = new TreeSet<String>();
        try (JarFile jar = new JarFile(path.toFile())) {
            final java.util.Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
                final String name = entry.getName().substring(
                        0, entry.getName().length() - ".class".length())
                        .replace('/', '.');
                if (transformer.handles(name)) {
                    assertTrue(handled.add(name),
                            "duplicate claimed game owner in official JAR: " + name);
                }
            }
        }
        assertEquals(25, handled.size(),
                "official mapped-owner count differs from transformer contract");
        assertTrue(handled.contains(Minecraft189ClassTransformer.TARGET_MAIN_CLASS),
                "missing official Minecraft main entry-point from claimed owners");
        assertTrue(handled.containsAll(Arrays.asList("aug", "auh", "bda", "pk", "bfk")),
                "native raycast owner coverage regressed");

        try (TransformingTargetClassLoader loader =
                     new TransformingTargetClassLoader(
                             new URL[]{path.toUri().toURL()},
                             getClass().getClassLoader(), transformer)) {
            int linked = 0;
            for (String owner : handled) {
                final Class<?> defined;
                try {
                    defined = Class.forName(owner, false, loader);
                } catch (LinkageError failure) {
                    throw new AssertionError(
                            "genuine Mojang transformed JVM linkage failed: " + owner,
                            failure);
                } catch (ClassNotFoundException failure) {
                    throw new AssertionError(
                            "claimed official Minecraft class not loadable: " + owner,
                            failure);
                }
                assertSame(loader, defined.getClassLoader(),
                        "mapped class escaped production child loader: " + owner);
                assertEquals(owner, defined.getName());
                assertSame(defined, Class.forName(owner, false, loader),
                        "unstable mapped class identity: " + owner);
                System.out.println("OFFICIAL_189_JVM_OWNER_LINK_PASS=" + owner);
                linked++;
            }
            assertEquals(25, linked,
                    "not all exact official Mojang transformer owners were JVM-linked");
            System.out.println("OFFICIAL_189_JVM_ALL_MAPPED_OWNERS_LINKED=" + linked);
        }
    }

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
        final Map<String, ClassNode> compiledHookOwners =
                new HashMap<String, ClassNode>();
        final int[] resolvedProjectSites = new int[2];
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
                    verifyTransformedStack(internal, transformed);
                    verifyCompiledProjectHookLinkage(internal, transformed,
                            compiledHookOwners, resolvedProjectSites);
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
        assertTrue(resolvedProjectSites[0] >= 20,
                "too few CustomMC method linkage sites; verifier may have stopped inspecting injected hooks");
        System.out.println("OFFICIAL_189_COMPILED_HOOK_LINKAGE_PASS=YES methods="
                + resolvedProjectSites[0] + " fields=" + resolvedProjectSites[1]
                + " owners=" + compiledHookOwners.size());
    }


    private static final String PROJECT_PACKAGE = "dev/trexzo/custommc/";

    /**
     * Check instructions emitted into Mojang's real classes against the
     * actual compiled CustomMC class files on the test runtime classpath.
     * This catches NoSuchMethodError/NoSuchFieldError-shaped bridge drift
     * without loading a game client or invoking hooks.
     */
    private static void verifyCompiledProjectHookLinkage(
            final String minecraftOwner, final byte[] transformed,
            final Map<String, ClassNode> compiledOwners, final int[] sites)
            throws IOException {
        final ClassNode node = new ClassNode(Opcodes.ASM9);
        new ClassReader(transformed).accept(node, 0);
        assertEquals(minecraftOwner, node.name);
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst();
                    insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode) {
                    final MethodInsnNode call = (MethodInsnNode) insn;
                    if (!call.owner.startsWith(PROJECT_PACKAGE)) continue;
                    verifyCompiledProjectMethod(call, compiledOwners);
                    sites[0]++;
                } else if (insn instanceof FieldInsnNode) {
                    final FieldInsnNode field = (FieldInsnNode) insn;
                    if (!field.owner.startsWith(PROJECT_PACKAGE)) continue;
                    verifyCompiledProjectField(field, compiledOwners);
                    sites[1]++;
                }
            }
        }
    }

    private static ClassNode compiledProjectOwner(
            final String internal, final Map<String, ClassNode> cache)
            throws IOException {
        final ClassNode cached = cache.get(internal);
        if (cached != null) return cached;
        assertTrue(internal.startsWith(PROJECT_PACKAGE),
                "refusing to resolve non-project class: " + internal);
        final String resource = internal + ".class";
        final ClassLoader loader =
                Minecraft189OfficialClientPreflightTest.class.getClassLoader();
        final ClassNode resolved = new ClassNode(Opcodes.ASM9);
        try (InputStream stream = loader.getResourceAsStream(resource)) {
            assertNotNull(stream, "compiled runtime hook class missing: " + resource);
            new ClassReader(stream).accept(resolved,
                    ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        assertEquals(internal, resolved.name, "hook class internal name drift");
        assertTrue((resolved.access & Opcodes.ACC_PUBLIC) != 0,
                "real Minecraft class cannot access nonpublic hook owner: " + internal);
        cache.put(internal, resolved);
        return resolved;
    }

    private static void verifyCompiledProjectMethod(
            final MethodInsnNode call, final Map<String, ClassNode> compiledOwners)
            throws IOException {
        final ClassNode owner = compiledProjectOwner(call.owner, compiledOwners);
        assertEquals((owner.access & Opcodes.ACC_INTERFACE) != 0, call.itf,
                "interface invocation/owner mismatch: " + call.owner + "." + call.name);
        for (MethodNode method : owner.methods) {
            if (!method.name.equals(call.name) || !method.desc.equals(call.desc)) {
                continue;
            }
            assertTrue((method.access & Opcodes.ACC_PUBLIC) != 0,
                    "Minecraft cannot access nonpublic compiled hook method: "
                            + call.owner + "." + call.name + call.desc);
            assertEquals(call.getOpcode() == Opcodes.INVOKESTATIC,
                    (method.access & Opcodes.ACC_STATIC) != 0,
                    "static/instance hook invocation mismatch: "
                            + call.owner + "." + call.name + call.desc);
            return;
        }
        fail("compiled hook method missing: "
                + call.owner + "." + call.name + call.desc);
    }

    private static void verifyCompiledProjectField(
            final FieldInsnNode ref, final Map<String, ClassNode> compiledOwners)
            throws IOException {
        final ClassNode owner = compiledProjectOwner(ref.owner, compiledOwners);
        for (FieldNode field : owner.fields) {
            if (!field.name.equals(ref.name) || !field.desc.equals(ref.desc)) continue;
            assertTrue((field.access & Opcodes.ACC_PUBLIC) != 0,
                    "Minecraft cannot access nonpublic hook field: "
                            + ref.owner + "." + ref.name + ":" + ref.desc);
            final boolean expectsStatic = ref.getOpcode() == Opcodes.GETSTATIC
                    || ref.getOpcode() == Opcodes.PUTSTATIC;
            assertEquals(expectsStatic, (field.access & Opcodes.ACC_STATIC) != 0,
                    "static/instance hook field mismatch: "
                            + ref.owner + "." + ref.name + ":" + ref.desc);
            return;
        }
        fail("compiled hook field missing: "
                + ref.owner + "." + ref.name + ":" + ref.desc);
    }

    @Test
    void compiledHookLinkageRejectsMissingWrongDescriptorAndPrivateMembers()
            throws Exception {
        final Map<String, ClassNode> classes = new HashMap<String, ClassNode>();
        final String owner = "dev/trexzo/custommc/platform/v1_8_9/Minecraft189RuntimeBridge";
        verifyCompiledProjectMethod(
                new MethodInsnNode(Opcodes.INVOKESTATIC, owner,
                        "gameTick", "()V", false), classes);
        assertThrows(AssertionError.class, () -> verifyCompiledProjectMethod(
                new MethodInsnNode(Opcodes.INVOKESTATIC, owner,
                        "nonexistentGameTick", "()V", false), classes));
        assertThrows(AssertionError.class, () -> verifyCompiledProjectMethod(
                new MethodInsnNode(Opcodes.INVOKESTATIC, owner,
                        "gameTick", "(I)V", false), classes));
        assertThrows(AssertionError.class, () -> verifyCompiledProjectMethod(
                new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner,
                        "gameTick", "()V", false), classes));
        assertThrows(AssertionError.class, () -> verifyCompiledProjectField(
                new FieldInsnNode(Opcodes.GETSTATIC, owner,
                        "activeRuntime", "Ldev/trexzo/custommc/platform/v1_8_9/Minecraft189BootstrapRuntime;"),
                classes));
    }

    /**
     * Validate the operand stack and local-variable transitions in every
     * real transformed method, not just whether ClassReader parses its bytes.
     * BasicVerifier intentionally avoids external Minecraft class loading:
     * this is a structural bytecode check, NOT a full JVM linkage test.
     */
    private static void verifyTransformedStack(
            final String expectedOwner, final byte[] transformed)
            throws AnalyzerException {
        final ClassNode parsed = new ClassNode(Opcodes.ASM9);
        new ClassReader(transformed).accept(parsed, ClassReader.EXPAND_FRAMES);
        assertEquals(expectedOwner, parsed.name,
                "unexpected mapped owner in bytecode analyzer");
        int inspected = 0;
        for (MethodNode method : parsed.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
                continue;
            }
            try {
                new Analyzer<BasicValue>(new BasicVerifier())
                        .analyze(expectedOwner, method);
            } catch (AnalyzerException invalid) {
                throw new AssertionError(
                        "official 1.8.9 bytecode stack invalid: "
                                + expectedOwner + "." + method.name + method.desc,
                        invalid);
            }
            inspected++;
        }
        assertTrue(inspected > 0,
                "no real method bytecode analyzed for owner: " + expectedOwner);
        System.out.println("OFFICIAL_189_STACK_ANALYSIS_PASS=" + expectedOwner
                + " methods=" + inspected);
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
