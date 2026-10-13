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
    /**
     * V27: source-pin real vanilla PlayerControllerMP.onPlayerRightClick.
     * This checks ONLY exact Mojang method authority; it does NOT call the
     * method, send C08, mutate the world or claim server block placement.
     */
    @Test
    void originalMinecraftNativeBlockPlacementControllerMethodExists() throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "real native placement method requires Mojang binary");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "original Minecraft 1.8.9 binary unavailable in offline tests");
        }
        final Path originalPath = Paths.get(configured);
        assertTrue(Files.isRegularFile(originalPath));
        assertEquals(OFFICIAL_SIZE, Files.size(originalPath));
        assertEquals(OFFICIAL_SHA1, digestFile(originalPath));
        final byte[] source;
        try (JarFile jar = new JarFile(originalPath.toFile())) {
            final JarEntry entry = jar.getJarEntry("bda.class");
            assertNotNull(entry, "original PlayerControllerMP bda absent");
            try (InputStream input = jar.getInputStream(entry)) {
                source = readAll(input);
            }
        }
        final ClassNode vanilla = new ClassNode();
        new ClassReader(source).accept(vanilla, 0);
        assertEquals("bda", vanilla.name);
        final Minecraft189Mappings.MappedMethod nativeClick =
                Minecraft189Mappings.PLAYER_CONTROLLER_ON_PLAYER_RIGHT_CLICK;
        assertEquals("a", nativeClick.obfuscatedName());
        assertEquals("(Lbew;Lbdb;Lzx;Lcj;Lcq;Laui;)Z",
                nativeClick.descriptor());
        int sourceMatches = 0;
        for (MethodNode method : vanilla.methods) {
            if (nativeClick.obfuscatedName().equals(method.name)
                    && nativeClick.descriptor().equals(method.desc)) {
                sourceMatches++;
                assertFalse((method.access & Opcodes.ACC_ABSTRACT) != 0,
                        "native vanilla placement method must contain implementation");
                assertFalse((method.access & Opcodes.ACC_NATIVE) != 0,
                        "native Java controller must be a regular vanilla method");
                assertTrue(method.instructions != null
                        && method.instructions.size() > 10,
                        "vanilla placement method body must not be a synthetic stub");
            }
        }
        assertEquals(1, sourceMatches,
                "exact original source right-click signature must occur once");
        // V28: the generated delegate must call exactly the original
        // vanilla method with all SIX original typed Minecraft references.
        final ClassNode transformed = new ClassNode();
        new ClassReader(new Minecraft189ClassTransformer()
                .transform("bda", source)).accept(transformed, 0);
        assertTrue(transformed.interfaces.contains(
                "dev/trexzo/custommc/platform/v1_8_9/"
                        + "Minecraft189ScaffoldNativePlacementAccess"));
        MethodNode delegate = null;
        for (MethodNode method : transformed.methods) {
            if ("customMcOriginalControllerPlace".equals(method.name)) {
                assertNull(delegate, "duplicate original controller placement bridge");
                delegate = method;
            }
        }
        assertNotNull(delegate);
        int invokeCount = 0, castCount = 0;
        final String[] castOrder = {"bew", "bdb", "zx", "cj", "cq", "aui"};
        for (AbstractInsnNode instruction : delegate.instructions.toArray()) {
            assertFalse(instruction instanceof FieldInsnNode,
                    "native placement delegate must not directly write original fields");
            if (instruction instanceof org.objectweb.asm.tree.TypeInsnNode
                    && instruction.getOpcode() == Opcodes.CHECKCAST) {
                assertEquals(castOrder[castCount++],
                        ((org.objectweb.asm.tree.TypeInsnNode)instruction).desc);
            }
            if (instruction instanceof MethodInsnNode) {
                final MethodInsnNode nativeInvoke = (MethodInsnNode) instruction;
                assertEquals(Opcodes.INVOKEVIRTUAL, nativeInvoke.getOpcode());
                assertEquals("bda", nativeInvoke.owner);
                assertEquals(nativeClick.obfuscatedName(), nativeInvoke.name);
                assertEquals(nativeClick.descriptor(), nativeInvoke.desc);
                invokeCount++;
            }
        }
        assertEquals(6, castCount, "all source-native arguments must be typed");
        assertEquals(1, invokeCount, "only one vanilla controller call permitted");
    }

    /**
     * V25 source-pin the original ave.c:Lbda; and bda reach methods.
     * Generated code may only GETFIELD the original controller reference.
     */
    @Test
    void originalMinecraftControllerReachIsSourceNative() throws Exception {
        final String configured=System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "real Minecraft player controller required");
        } else {
            Assumptions.assumeTrue(configured!=null && !configured.trim().isEmpty(),
                    "official JAR not available offline");
        }
        final Path path=Paths.get(configured);
        assertTrue(Files.isRegularFile(path));
        assertEquals(OFFICIAL_SIZE, Files.size(path));
        assertEquals(OFFICIAL_SHA1, digestFile(path));
        final byte[][] classBytes=new byte[2][];
        try(JarFile jar=new JarFile(path.toFile())) {
            int idx=0;
            for(String className:Arrays.asList("ave","bda")) {
                final JarEntry entry=jar.getJarEntry(className+".class");
                assertNotNull(entry);
                try(InputStream in=jar.getInputStream(entry)) {
                    classBytes[idx++]=readAll(in);
                }
            }
        }
        final ClassNode vanillaGame=new ClassNode();
        final ClassNode vanillaController=new ClassNode();
        new ClassReader(classBytes[0]).accept(vanillaGame,0);
        new ClassReader(classBytes[1]).accept(vanillaController,0);
        int sourceControllerFields=0;
        for(FieldNode field:vanillaGame.fields)
            if("c".equals(field.name) && "Lbda;".equals(field.desc))
                sourceControllerFields++;
        assertEquals(1,sourceControllerFields,"missing original ave.c field");
        for(String methodId:Arrays.asList("d()F","i()Z")) {
            int count=0;
            for(MethodNode method:vanillaController.methods)
                if((method.name+method.desc).equals(methodId))
                    count++;
            assertEquals(1,count,"missing original bda reach method "+methodId);
        }
        final ClassNode changed=new ClassNode();
        new ClassReader(new Minecraft189ClassTransformer()
                .transform("ave",classBytes[0])).accept(changed,0);
        assertTrue(changed.interfaces.contains(
                "dev/trexzo/custommc/platform/v1_8_9/"
                        +"Minecraft189ScaffoldNativeControllerAccess"));
        MethodNode getter=null;
        for(MethodNode method:changed.methods)
            if("customMcScaffoldNativeController".equals(method.name)
                    && "()Ljava/lang/Object;".equals(method.desc)) {
                assertNull(getter,"duplicate original controller accessor");
                getter=method;
            }
        assertNotNull(getter);
        int reads=0;
        for(AbstractInsnNode node:getter.instructions.toArray()) {
            if(node instanceof MethodInsnNode)
                fail("original controller accessor must never invoke methods");
            if(!(node instanceof FieldInsnNode)) continue;
            final FieldInsnNode field=(FieldInsnNode)node;
            assertEquals(Opcodes.GETFIELD, field.getOpcode());
            assertEquals("ave",field.owner);
            assertEquals("c",field.name);
            assertEquals("Lbda;",field.desc);
            reads++;
        }
        assertEquals(1,reads,"one source original controller field read");
    }

    /**
     * V24: original joined.srg wn/aS()F, not guessed constant/field.
     * Verifies the exact original vanilla override and read-only delegate.
     */
    @Test
    void originalMinecraftPlayerEyeHeightMethodIsSourceNative() throws Exception {
        final String configured=System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "native player eye mapping requires official jar");
        } else {
            Assumptions.assumeTrue(configured!=null && !configured.trim().isEmpty(),
                    "no original Mojang 1.8.9 available in offline CI");
        }
        final Path path=Paths.get(configured);
        assertTrue(Files.isRegularFile(path));
        assertEquals(OFFICIAL_SIZE,Files.size(path));
        assertEquals(OFFICIAL_SHA1,digestFile(path));
        final byte[] vanilla;
        try(JarFile jar=new JarFile(path.toFile())) {
            final JarEntry entry=jar.getJarEntry("wn.class");
            assertNotNull(entry);
            try(InputStream in=jar.getInputStream(entry)) {
                vanilla=readAll(in);
            }
        }
        final ClassNode original=new ClassNode();
        new ClassReader(vanilla).accept(original,0);
        assertEquals("wn",original.name);
        int sourceMethods=0;
        for(MethodNode method:original.methods)
            if("aS".equals(method.name) && "()F".equals(method.desc))
                sourceMethods++;
        assertEquals(1,sourceMethods,
                "MCP 1.8.9 original EntityPlayer getEyeHeight override absent");
        final ClassNode changed=new ClassNode();
        new ClassReader(new Minecraft189ClassTransformer()
                .transform("wn",vanilla)).accept(changed,0);
        assertTrue(changed.interfaces.contains(
                "dev/trexzo/custommc/platform/v1_8_9/"
                        +"Minecraft189ScaffoldNativeEyeAccess"));
        MethodNode getter=null;
        for(MethodNode method:changed.methods)
            if("customMcOriginalEyeHeight".equals(method.name)
                    && "()F".equals(method.desc)) {
                assertNull(getter,"duplicate eye-height accessor");
                getter=method;
            }
        assertNotNull(getter);
        int calls=0;
        for(AbstractInsnNode node:getter.instructions.toArray()) {
            assertFalse(node instanceof FieldInsnNode,
                    "never estimate original player height from mutable fields");
            if(!(node instanceof MethodInsnNode)) continue;
            final MethodInsnNode method=(MethodInsnNode)node;
            calls++;
            assertEquals(Opcodes.INVOKEVIRTUAL,method.getOpcode());
            assertEquals("wn",method.owner);
            assertEquals("aS",method.name);
            assertEquals("()F",method.desc);
        }
        assertEquals(1,calls,"single original native getEyeHeight call");
    }

    /**
     * V23: the original ave.s()V must dispatch one observational callback
     * using its current `this`, with no fake singleton or captured World.
     */
    @Test
    void realMojang189RunTickPublishesItsOwnSourceContext() throws Exception {
        final String configured=System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured);
        } else {
            Assumptions.assumeTrue(configured!=null && !configured.trim().isEmpty(),
                    "official 1.8.9 absent offline");
        }
        final Path path=Paths.get(configured);
        assertTrue(Files.isRegularFile(path));
        assertEquals(OFFICIAL_SIZE,Files.size(path));
        assertEquals(OFFICIAL_SHA1,digestFile(path));
        final byte[] original;
        try (JarFile jar=new JarFile(path.toFile());
             InputStream stream=jar.getInputStream(jar.getJarEntry("ave.class"))) {
            original=readAll(stream);
        }
        final ClassNode source=new ClassNode();
        new ClassReader(original).accept(source,0);
        MethodNode nativeTick=null;
        for (MethodNode method:source.methods)
            if ("s".equals(method.name)&&"()V".equals(method.desc)) {
                assertNull(nativeTick,"duplicate original runTick");
                nativeTick=method;
            }
        assertNotNull(nativeTick,"missing source Mojang 1.8.9 runTick");
        final ClassNode changed=new ClassNode();
        new ClassReader(new Minecraft189ClassTransformer()
                .transform("ave",original)).accept(changed,0);
        MethodNode tick=null;
        for(MethodNode method:changed.methods)
            if("s".equals(method.name)&&"()V".equals(method.desc)) {
                assertNull(tick,"multiple transformed runTick methods");
                tick=method;
            }
        assertNotNull(tick);
        int callbacks=0;
        for(AbstractInsnNode node:tick.instructions.toArray()) {
            if(!(node instanceof MethodInsnNode)) continue;
            final MethodInsnNode call=(MethodInsnNode)node;
            if (!"originalScaffoldContextAtTick".equals(call.name)) continue;
            callbacks++;
            assertEquals(Opcodes.INVOKESTATIC,call.getOpcode());
            assertEquals("dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189RuntimeBridge",call.owner);
            assertEquals("(Ljava/lang/Object;)V",call.desc);
            AbstractInsnNode preceding=node.getPrevious();
            while(preceding!=null && preceding.getOpcode()<0)
                preceding=preceding.getPrevious();
            assertNotNull(preceding);
            assertEquals(Opcodes.ALOAD,preceding.getOpcode());
            assertEquals(0,((org.objectweb.asm.tree.VarInsnNode)preceding).var,
                    "runTick must pass its actual Minecraft self reference");
        }
        assertEquals(1,callbacks,"one native source callback per original runTick");
    }

    /**
     * V22: source-pin ave.h/f/s and the transformed snapshot body to
     * the genuine Mojang JAR. This does not assert a live cursor in game.
     */
    @Test
    void realMojang189MinecraftContextReadsOnlyOriginalFields() throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "V22 original binary is required");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "original Mojang JAR unavailable in offline unit CI");
        }
        final Path path = Paths.get(configured);
        assertTrue(Files.isRegularFile(path));
        assertEquals(OFFICIAL_SIZE, Files.size(path));
        assertEquals(OFFICIAL_SHA1, digestFile(path));
        final byte[] original;
        try (JarFile jar = new JarFile(path.toFile())) {
            final JarEntry entry = jar.getJarEntry("ave.class");
            assertNotNull(entry, "original Minecraft singleton class absent");
            try (InputStream in = jar.getInputStream(entry)) {
                original = readAll(in);
            }
        }
        final ClassNode vanilla = new ClassNode();
        new ClassReader(original).accept(vanilla, 0);
        assertEquals("ave", vanilla.name);
        final String[] fields = {"h:Lbew;", "f:Lbdb;", "s:Lauh;"};
        for (String required : fields) {
            int count = 0;
            for (FieldNode field : vanilla.fields)
                if ((field.name + ":" + field.desc).equals(required))
                    count++;
            assertEquals(1, count, "missing exact original ave field " + required);
        }
        final ClassNode changed = new ClassNode();
        new ClassReader(new Minecraft189ClassTransformer()
                .transform("ave", original)).accept(changed, 0);
        assertTrue(changed.interfaces.contains(
                "dev/trexzo/custommc/platform/v1_8_9/"
                        + "Minecraft189ScaffoldNativeContextAccess"),
                "snapshot interface must be on actual original ave");
        MethodNode snapshot = null;
        for (MethodNode method : changed.methods)
            if ("customMcScaffoldNativeContext".equals(method.name)
                    && "()[Ljava/lang/Object;".equals(method.desc)) {
                assertNull(snapshot, "duplicate native Minecraft snapshot");
                snapshot = method;
            }
        assertNotNull(snapshot, "original Minecraft context accessor absent");
        final List<String> reads = new ArrayList<String>();
        int writes = 0, calls = 0, allocations = 0;
        for (AbstractInsnNode instruction : snapshot.instructions.toArray()) {
            if (instruction instanceof FieldInsnNode) {
                final FieldInsnNode field = (FieldInsnNode) instruction;
                if (field.getOpcode() != Opcodes.GETFIELD) writes++;
                reads.add(field.owner + "." + field.name + ":" + field.desc);
            } else if (instruction instanceof MethodInsnNode) {
                calls++;
            } else if (instruction.getOpcode() == Opcodes.ANEWARRAY) {
                allocations++;
            }
        }
        assertEquals(Arrays.asList("ave.h:Lbew;", "ave.f:Lbdb;",
                        "ave.s:Lauh;"), reads,
                "read-only original field order/identity changed");
        assertEquals(0, writes, "native snapshot must never change game fields");
        assertEquals(0, calls, "native snapshot must not invoke or dispatch");
        assertEquals(1, allocations, "native snapshot should allocate one array");
    }

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
            // M400: the untransformed WorldClient must be owned by the same
            // game classloader as the transformed World superclass. The
            // original selective loader passed ASM preflight but failed the
            // actual Java 8 verifier with bdb not assignable to adm.
            assertFalse(transformer.handles("bdb"),
                    "the regression must cover an untransformed vanilla owner");
            final Class<?> vanillaWorldClient = Class.forName("bdb", false, loader);
            final Class<?> transformedWorld = Class.forName("adm", false, loader);
            assertSame(loader, vanillaWorldClient.getClassLoader(),
                    "untransformed Mojang WorldClient must not delegate to parent");
            assertSame(loader, transformedWorld.getClassLoader());
            assertTrue(transformedWorld.isAssignableFrom(vanillaWorldClient),
                    "untransformed WorldClient must inherit transformed World identity");
            System.out.println("OFFICIAL_189_VANILLA_WORLD_HIERARCHY_PASS=YES");
        }
    }

    /**
     * V12 Scaffold provenance only: bytecode-inspect the SHA-pinned ORIGINAL
     * Mojang client for real block placement. No fake Place() API, no extra
     * packet path and no claim of server-confirmed placement or inventory use.
     */
    @Test
    void actualMojang189ScaffoldPlacementCallchainIsNative() throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "V12 requires a pinned original client");
            assertFalse(configured.trim().isEmpty(), "empty original-client path");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "Pinned original client not present in ordinary offline CI");
        }
        final Path path = Paths.get(configured);
        assertTrue(Files.isRegularFile(path), "original Minecraft client unavailable");
        assertEquals(OFFICIAL_SIZE, Files.size(path));
        assertEquals(OFFICIAL_SHA1, digestFile(path));

        assertEquals("bda", Minecraft189Mappings.PLAYER_CONTROLLER_MP
                .obfuscatedInternalName());
        assertEquals("cj", Minecraft189Mappings.BLOCK_POS.obfuscatedInternalName());
        assertEquals("cq", Minecraft189Mappings.ENUM_FACING.obfuscatedInternalName());
        assertEquals("aui", Minecraft189Mappings.VEC3.obfuscatedInternalName());
        assertEquals("ja", Minecraft189Mappings.C08_PACKET_PLAYER_BLOCK_PLACEMENT
                .obfuscatedInternalName());

        final String placementDesc = "(Lbew;Lbdb;Lzx;Lcj;Lcq;Laui;)Z";
        final String itemUseDesc = "(Lwn;Ladm;Lcj;Lcq;FFF)Z";
        final ClassNode minecraft = new ClassNode();
        final ClassNode controller = new ClassNode();
        final ClassNode placementPacket = new ClassNode();
        try (JarFile jar = new JarFile(path.toFile())) {
            final ClassNode[] nodes = {minecraft, controller, placementPacket};
            final String[] names = {"ave", "bda", "ja"};
            for (int i = 0; i < names.length; i++) {
                final JarEntry entry = jar.getJarEntry(names[i] + ".class");
                assertNotNull(entry, "genuine Mojang owner missing: " + names[i]);
                try (InputStream stream = jar.getInputStream(entry)) {
                    new ClassReader(readAll(stream)).accept(nodes[i], 0);
                }
                assertEquals(names[i], nodes[i].name);
            }
        }
        MethodNode rightClick = null;
        for (MethodNode method : minecraft.methods) {
            if ("ax".equals(method.name) && "()V".equals(method.desc)) {
                assertNull(rightClick, "duplicate original Minecraft.rightClickMouse");
                rightClick = method;
            }
        }
        assertNotNull(rightClick, "missing original ave.ax()V right click");
        int originalCall = 0;
        for (AbstractInsnNode node : rightClick.instructions.toArray()) {
            if (!(node instanceof MethodInsnNode)) continue;
            final MethodInsnNode call = (MethodInsnNode) node;
            if ("bda".equals(call.owner) && "a".equals(call.name)
                    && placementDesc.equals(call.desc)
                    && call.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                originalCall++;
            }
        }
        assertEquals(1, originalCall,
                "one genuine right-click GUI dispatch must target original placement controller");

        MethodNode onPlayerRightClick = null;
        for (MethodNode method : controller.methods) {
            if ("a".equals(method.name) && placementDesc.equals(method.desc)) {
                assertNull(onPlayerRightClick, "duplicate original block placement method");
                onPlayerRightClick = method;
            }
        }
        assertNotNull(onPlayerRightClick, "source-mapped 1.8.9 controller is absent");
        int packetCtor = 0;
        int nativeItemUse = 0;
        int nativePacketDispatch = 0;
        int nativeStackField = 0;
        for (AbstractInsnNode node : onPlayerRightClick.instructions.toArray()) {
            if (node instanceof MethodInsnNode) {
                final MethodInsnNode call = (MethodInsnNode)node;
                if ("ja".equals(call.owner) && "<init>".equals(call.name)
                        && "(Lcj;ILzx;FFF)V".equals(call.desc)
                        && call.getOpcode() == Opcodes.INVOKESPECIAL) {
                    packetCtor++;
                }
                if ("zx".equals(call.owner) && "a".equals(call.name)
                        && itemUseDesc.equals(call.desc)
                        && call.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    nativeItemUse++;
                }
                // Source-mapped NetHandlerPlayClient.addToSendQueue(Packet):
                // bcy/a(Lff;)V. Constructor alone is not packet dispatch.
                if ("bcy".equals(call.owner) && "a".equals(call.name)
                        && "(Lff;)V".equals(call.desc)
                        && call.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    nativePacketDispatch++;
                }
            } else if (node instanceof FieldInsnNode) {
                final FieldInsnNode field = (FieldInsnNode) node;
                if ("zx".equals(field.owner) && "b".equals(field.name)
                        && "I".equals(field.desc)) nativeStackField++;
            }
        }
        assertEquals(1, packetCtor,
                "original controller must construct exactly one C08 block-placement packet");
        assertTrue(nativeItemUse >= 2,
                "original controller must invoke ItemStack.onItemUse in creative/survival branches");
        assertTrue(nativeStackField >= 2,
                "original creative path must preserve native ItemStack.stackSize");
        assertEquals(1, nativePacketDispatch,
                "original controller must dispatch exactly one C08 packet via real NetHandlerPlayClient");
        assertEquals("ja", placementPacket.name);
        System.out.println("REAL_189_SCAFFOLD_NATIVE_RIGHT_CLICK_CONTROLLER_PASS=YES");
        System.out.println("REAL_189_SCAFFOLD_NATIVE_C08_CONSTRUCT_AND_ITEM_USE_PASS=YES");
        System.out.println("REAL_189_SCAFFOLD_NATIVE_SERVER_PLACE_CONFIRMATION=NOT_TESTED");
        System.out.println("REAL_189_SCAFFOLD_REAL_INVENTORY_CONSUMPTION=NOT_TESTED");
    }

    /**
     * V13: a world-read/ItemBlock authority check on the actual original
     * Mojang JAR, not synthetic block samples or fictional placement success.
     * These APIs are prerequisites for future implementation only.
     */
    /**
     * V19: genuine original World rayTraceBlocks/block hit and face authority.
     * No simulated voxel queries, placement packets, or synthetic cast paths.
     */
    @Test
    void realMojang189ScaffoldRayTraceUsesExactNativeBlockAndFacing() throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "original binary required for V19");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "original Minecraft JAR absent in ordinary offline tests");
        }
        final Path client = Paths.get(configured);
        assertTrue(Files.isRegularFile(client));
        assertEquals(OFFICIAL_SIZE, Files.size(client));
        assertEquals(OFFICIAL_SHA1, digestFile(client));
        final byte[] vanillaWorld;
        try (JarFile jar = new JarFile(client.toFile())) {
            for (String classname : Arrays.asList("adm","aui","auh","auh$a","cq","cj")) {
                assertNotNull(jar.getJarEntry(classname + ".class"),
                        "missing original Minecraft raytrace class " + classname);
            }
            try (InputStream in = jar.getInputStream(jar.getJarEntry("adm.class"))) {
                vanillaWorld=readAll(in);
            }
        }
        final ClassNode source = new ClassNode();
        new ClassReader(vanillaWorld).accept(source,0);
        int vanillaWorldMethods = 0;
        for (MethodNode method : source.methods)
            if ("a".equals(method.name) &&
                    "(Laui;Laui;ZZZ)Lauh;".equals(method.desc))
                vanillaWorldMethods++;
        assertEquals(1,vanillaWorldMethods,
                "exact original World.rayTraceBlocks source method required");

        final ClassNode transformed = new ClassNode();
        new ClassReader(new Minecraft189ClassTransformer()
                .transform("adm", vanillaWorld)).accept(transformed,0);
        assertTrue(transformed.interfaces.contains(
                "dev/trexzo/custommc/platform/v1_8_9/"
                + "Minecraft189ScaffoldWorldRayTraceAccess"));
        MethodNode bridge=null;
        for (MethodNode m : transformed.methods)
            if ("customMcScaffoldRayTraceSupport".equals(m.name)
                    && "(DDDDDDIIII)Z".equals(m.desc)) {
                assertNull(bridge,"duplicate source World raytrace");
                bridge=m;
            }
        assertNotNull(bridge,"source World raytrace injection absent");
        final Map<String,Integer> calls=new LinkedHashMap<String,Integer>();
        final Map<String,Integer> fields=new LinkedHashMap<String,Integer>();
        int falseReturns=0;
        for (AbstractInsnNode n : bridge.instructions.toArray()) {
            if (n instanceof MethodInsnNode) {
                final MethodInsnNode m=(MethodInsnNode)n;
                final String key=m.owner+"."+m.name+m.desc;
                calls.put(key,calls.containsKey(key)?calls.get(key)+1:1);
                assertFalse("bcy".equals(m.owner) || "ja".equals(m.owner),
                        "V19 read-only raytrace must never dispatch or construct C08");
            } else if (n instanceof FieldInsnNode) {
                final FieldInsnNode f=(FieldInsnNode)n;
                final String key=f.owner+"."+f.name+":"+f.desc;
                fields.put(key,fields.containsKey(key)?fields.get(key)+1:1);
            } else if (n.getOpcode()==Opcodes.ICONST_0) falseReturns++;
        }
        final String[] expectedCalls={
            "aui.<init>(DDD)V",
            "adm.a(Laui;Laui;ZZZ)Lauh;",
            "cq.a()I",
            "cj.<init>(III)V",
            "auh.a()Lcj;",
            "cj.equals(Ljava/lang/Object;)Z"
        };
        assertEquals(expectedCalls.length,calls.size(),
                "exact native-only call set required");
        for (String key : expectedCalls)
            assertEquals(Integer.valueOf("aui.<init>(DDD)V".equals(key)?2:1),
                    calls.get(key),"source-native raytrace call " + key);
        for (String field : Arrays.asList("auh.a:Lauh$a;",
                "auh$a.b:Lauh$a;","auh.b:Lcq;"))
            assertEquals(Integer.valueOf(1),fields.get(field),
                    "exact Mojang block type/face field " + field);
        assertEquals(3,fields.size(),"no unrelated access via raytrace adapter");
        assertTrue(falseReturns>=4, "native raytrace must fail closed on null/miss/wrong face");
        System.out.println("REAL_189_SCAFFOLD_NATIVE_RAYTRACE_BLOCK_FACE_PASS=YES");
        System.out.println("REAL_189_SCAFFOLD_SOURCE_RAYTRACE_SERVER_PLACEMENT=NOT_TESTED");
    }

    /**
     * V15 original binary acceptance: source native World reads, emitted
     * bytecode semantics, and no packet/world-mutating calls in the adapter.
     */
    @Test
    void realMojang189ScaffoldWorldAdapterHasExactNativeReadOnlyCalls() throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured,"original client required for World read proof");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "No pinned Mojang client in offline unit run");
        }
        final Path original = Paths.get(configured);
        assertTrue(Files.isRegularFile(original));
        assertEquals(OFFICIAL_SIZE, Files.size(original));
        assertEquals(OFFICIAL_SHA1, digestFile(original));
        final byte[] raw;
        try (JarFile jar = new JarFile(original.toFile())) {
            final JarEntry entry = jar.getJarEntry("adm.class");
            assertNotNull(entry,"native World absent");
            try (InputStream input = jar.getInputStream(entry)) {
                raw = readAll(input);
            }
        }
        final ClassNode source = new ClassNode();
        new ClassReader(raw).accept(source, 0);
        final String[][] originals = {
            {"d","(Lcj;Z)Z"}, {"d","(Lcj;)Z"}, {"p","(Lcj;)Lalz;"}
        };
        for (String[] symbol : originals) {
            int count = 0;
            for (MethodNode n : source.methods)
                if (symbol[0].equals(n.name) && symbol[1].equals(n.desc)) count++;
            assertEquals(1,count,"exact native World symbol "+Arrays.toString(symbol));
        }
        final byte[] patched = new Minecraft189ClassTransformer()
                .transform("adm", raw);
        final ClassNode transformed = new ClassNode();
        new ClassReader(patched).accept(transformed,0);
        assertTrue(transformed.interfaces.contains(
                "dev/trexzo/custommc/platform/v1_8_9/Minecraft189ScaffoldWorldReadAccess"),
                "only source-authentic World may provide Scaffold read adapter");
        MethodNode method = null;
        for (MethodNode n : transformed.methods)
            if ("customMcScaffoldCellCode".equals(n.name)
                    && "(III)I".equals(n.desc)) {
                assertNull(method,"duplicate native Scaffold read method");
                method = n;
            }
        assertNotNull(method,"original World missing read-only native cell method");
        final Map<String,Integer> seen = new LinkedHashMap<String,Integer>();
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!(insn instanceof MethodInsnNode)) continue;
            final MethodInsnNode call = (MethodInsnNode)insn;
            final String key=call.owner+"."+call.name+call.desc;
            seen.put(key,seen.containsKey(key)?seen.get(key)+1:1);
            if ("adm.d(Lcj;Z)Z".equals(key)) {
                assertNotNull(call.getPrevious(),
                        "native loaded-chunk check must have explicit allowEmpty");
                assertEquals(Opcodes.ICONST_1,call.getPrevious().getOpcode(),
                        "allowEmpty must still require original chunkExists, not reject empty loaded chunks");
            }
            assertFalse(key.contains("func_175656_a"),
                    "Scaffold world read cannot set original block state");
            assertFalse("bcy".equals(call.owner) || "ja".equals(call.owner),
                    "Scaffold world read cannot dispatch placement packets");
        }
        final String[] expected = {
            "cj.<init>(III)V",
            "adm.N()Lamv;",
            "amv.d(II)Lamy;",
            "amy.f()Z",
            "adm.d(Lcj;)Z",
            "adm.p(Lcj;)Lalz;",
            "alz.c()Lafh;",
            "afh.o()Z"
        };
        assertEquals(expected.length,seen.size(),
                "native World cell probe must contain no unverified calls");
        for (String key : expected)
            assertEquals(Integer.valueOf(1),seen.get(key),
                    "exact native Minecraft block evidence required: "+key);
        // No empty/absent chunk may be classified AIR: provider existence
        // must precede the secondary allowEmpty loaded guard and air check.
        java.util.List<String> callOrder = new java.util.ArrayList<String>();
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!(insn instanceof MethodInsnNode)) continue;
            final MethodInsnNode call = (MethodInsnNode)insn;
            callOrder.add(call.owner + "." + call.name + call.desc);
        }
        assertTrue(callOrder.indexOf("adm.N()Lamv;") <
                        callOrder.indexOf("amv.d(II)Lamy;"),
                "native World must resolve client chunk provider before cache read");
        assertTrue(callOrder.indexOf("amv.d(II)Lamy;") <
                        callOrder.indexOf("amy.f()Z"),
                "source-native provided Chunk must be verified not an EmptyChunk");
        assertTrue(callOrder.indexOf("amy.f()Z") <
                        callOrder.indexOf("adm.d(Lcj;)Z"),
                "source-native non-EmptyChunk proof MUST precede air read");
        assertFalse(callOrder.contains("amv.a(II)Z"),
                "Minecraft 1.8.9 client chunkExists always true");
        assertFalse(callOrder.contains("adm.d(Lcj;Z)Z"),
                "redundant vanilla loaded-block test must not suppress verified air");
        System.out.println("REAL_189_SCAFFOLD_NATIVE_NONEMPTY_CHUNK_BEFORE_AIR_PASS=YES");
        System.out.println("REAL_189_SCAFFOLD_NATIVE_LOADED_AIR_FULL_CUBE_READ_PASS=YES");
        System.out.println("REAL_189_SCAFFOLD_NATIVE_READ_ONLY_WORLD_ADAPTER_PASS=YES");
        System.out.println("REAL_189_SCAFFOLD_LIVE_WORLD_COORDINATE_READ=NOT_TESTED");
    }

    /**
     * V19: the exact SHA-pinned original Mojang ray-hit owns BLOCK, BlockPos,
     * EnumFacing and Vec3. Verify native getters and the read-only generated
     * snapshot, not a fabricated raycast or a patched placement click.
     */
    @Test
    void realMojang189ScaffoldBlockFaceAndVectorSourcesAreExact() throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "genuine original ray-hit binary required");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "No original 1.8.9 binary for ordinary offline unit CI");
        }
        final Path original=Paths.get(configured);
        assertTrue(Files.isRegularFile(original));
        assertEquals(OFFICIAL_SIZE,Files.size(original));
        assertEquals(OFFICIAL_SHA1,digestFile(original));
        final Map<String,ClassNode> source = new LinkedHashMap<String,ClassNode>();
        byte[] nativeHit=null;
        try (JarFile jar=new JarFile(original.toFile())) {
            for (String name : Arrays.asList("auh","auh$a","cq","aui","cj","df")) {
                final JarEntry entry=jar.getJarEntry(name+".class");
                assertNotNull(entry,"original Minecraft missing ray source "+name);
                final byte[] bytes;
                try(InputStream stream=jar.getInputStream(entry)) { bytes=readAll(stream); }
                final ClassNode node=new ClassNode();
                new ClassReader(bytes).accept(node,0);
                assertEquals(name,node.name);
                source.put(name,node);
                if ("auh".equals(name)) nativeHit=bytes;
            }
        }
        assertEquals("df",source.get("cj").superName,
                "vanilla BlockPos must inherit source original Vec3i methods");
        final String[][] nativeFields = {
            {"auh","a","Lauh$a;"}, {"auh","b","Lcq;"},
            {"auh","c","Laui;"}, {"auh","e","Lcj;"},
            {"auh$a","b","Lauh$a;"},
            {"aui","a","D"}, {"aui","b","D"}, {"aui","c","D"}
        };
        for(String[] expected:nativeFields) {
            int count=0;
            for(FieldNode field:source.get(expected[0]).fields)
                if(expected[1].equals(field.name)&&expected[2].equals(field.desc))
                    count++;
            assertEquals(1,count,"original native ray field "+Arrays.toString(expected));
        }
        final String[][] nativeMethods = {
            {"df","n","()I"}, {"df","o","()I"}, {"df","p","()I"},
            {"cq","a","()I"}
        };
        for(String[] expected:nativeMethods) {
            int count=0;
            for(MethodNode method:source.get(expected[0]).methods)
                if(expected[1].equals(method.name)&&expected[2].equals(method.desc))
                    count++;
            assertEquals(1,count,"original native ray method "+Arrays.toString(expected));
        }
        assertNotNull(nativeHit);
        final ClassNode transformed=new ClassNode();
        new ClassReader(new Minecraft189ClassTransformer()
                .transform("auh",nativeHit)).accept(transformed,0);
        assertTrue(transformed.interfaces.contains(
                "dev/trexzo/custommc/platform/v1_8_9/"
                        +"Minecraft189ScaffoldNativeBlockHitAccess"));
        MethodNode nativeSnapshot=null;
        for(MethodNode method:transformed.methods) {
            if("customMcScaffoldNativeBlockHit".equals(method.name)
                    && "()[D".equals(method.desc)) {
                assertNull(nativeSnapshot,"duplicate native ray evidence method");
                nativeSnapshot=method;
            }
        }
        assertNotNull(nativeSnapshot,"original MovingObjectPosition not augmented");
        final java.util.List<String> reads=new java.util.ArrayList<String>();
        final java.util.List<String> calls=new java.util.ArrayList<String>();
        for(AbstractInsnNode instruction:nativeSnapshot.instructions.toArray()) {
            if(instruction instanceof FieldInsnNode) {
                final FieldInsnNode field=(FieldInsnNode)instruction;
                reads.add(field.owner+"."+field.name+field.desc);
                assertTrue(field.getOpcode()==Opcodes.GETFIELD
                                || field.getOpcode()==Opcodes.GETSTATIC,
                        "V19 ray evidence must never mutate original fields");
            } else if(instruction instanceof MethodInsnNode) {
                final MethodInsnNode call=(MethodInsnNode)instruction;
                calls.add(call.owner+"."+call.name+call.desc);
                assertFalse("ja".equals(call.owner)||"bcy".equals(call.owner),
                        "V19 ray evidence never constructs or sends placement packets");
                assertFalse(call.getOpcode()==Opcodes.INVOKESTATIC,
                        "V19 ray evidence must use source-native instance getters only");
            }
        }
        for(String expected:new String[]{
                "auh.aLauh$a;","auh.bLcq;","auh.cLaui;","auh.eLcj;",
                "auh$a.bLauh$a;","aui.aD","aui.bD","aui.cD"}) {
            assertTrue(reads.contains(expected),"missing original ray source "+expected);
        }
        for(String expected:new String[]{
                "df.n()I","df.o()I","df.p()I","cq.a()I",
                "java/lang/Object.equals(Ljava/lang/Object;)Z"})
            assertTrue(calls.contains(expected),"missing source-native ray getter "+expected);
        assertEquals(12,reads.size(),
                "native ray evidence reads exactly the verified original fields");
        assertEquals(5,calls.size(),
                "native ray evidence calls exactly three original Vec3i getters, Facing.getIndex and equals");
        System.out.println("REAL_189_SCAFFOLD_NATIVE_BLOCK_FACE_HITVECTOR_PASS=YES");
        System.out.println("REAL_189_SCAFFOLD_NATIVE_RAY_HIT_READ_ONLY_TRANSFORM_PASS=YES");
        System.out.println("REAL_189_SCAFFOLD_AUTOMATIC_PLACEMENT=NOT_TESTED");
    }

    /**
     * V14: exact original ItemStack Item field is inspected with INSTANCEOF
     * original ItemBlock (yo), never an item name or an icon/texture match.
     */
    @Test
    void realMojang189ItemStackNativeItemBlockTypeHookIsExact() throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "original 1.8.9 binary required for V14");
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "No original Minecraft JAR in ordinary offline CI");
        }
        final Path path = Paths.get(configured);
        assertTrue(Files.isRegularFile(path));
        assertEquals(OFFICIAL_SIZE, Files.size(path));
        assertEquals(OFFICIAL_SHA1, digestFile(path));
        final byte[] original;
        try (JarFile jar = new JarFile(path.toFile())) {
            final JarEntry entry = jar.getJarEntry("zx.class");
            assertNotNull(entry,"source-native ItemStack absent");
            try (InputStream stream = jar.getInputStream(entry)) {
                original = readAll(stream);
            }
        }
        final ClassNode originalNode = new ClassNode();
        new ClassReader(original).accept(originalNode,0);
        String itemField = null;
        int itemFields = 0;
        for (FieldNode field : originalNode.fields)
            if ("Lzw;".equals(field.desc)
                    && (field.access & Opcodes.ACC_STATIC) == 0) {
                itemFields++;
                itemField = field.name;
            }
        assertEquals(1,itemFields, "original ItemStack must own exactly one Item field");
        final ClassNode transformed = new ClassNode();
        new ClassReader(new Minecraft189ClassTransformer()
                .transform("zx",original)).accept(transformed,0);
        int hookCount = 0;
        for (MethodNode method : transformed.methods) {
            if (!"customMcIsBlockItem".equals(method.name)
                    || !"()Z".equals(method.desc)) continue;
            hookCount++;
            int fieldReads=0, genuineChecks=0;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof FieldInsnNode) {
                    final FieldInsnNode field=(FieldInsnNode)instruction;
                    if (field.getOpcode()==Opcodes.GETFIELD
                            && "zx".equals(field.owner) && itemField.equals(field.name)
                            && "Lzw;".equals(field.desc)) fieldReads++;
                }
                if (instruction.getOpcode()==Opcodes.INSTANCEOF
                        && instruction instanceof org.objectweb.asm.tree.TypeInsnNode
                        && "yo".equals(((org.objectweb.asm.tree.TypeInsnNode)instruction).desc))
                    genuineChecks++;
            }
            assertEquals(1,fieldReads,"getter must read the native Item field");
            assertEquals(1,genuineChecks,"getter must check exact genuine ItemBlock yo");
        }
        assertEquals(1,hookCount,"one real ItemBlock type getter on transformed ItemStack");
        System.out.println("REAL_189_SCAFFOLD_SOURCE_ITEMBLOCK_INSTANCEOF_PASS=YES");
        System.out.println("REAL_189_SCAFFOLD_SELECTED_HOTBAR_SERVER_PLACEMENT=NOT_TESTED");
    }

    @Test
    void realMojang189ScaffoldWorldSupportAndItemBlockMethodsExist() throws Exception {
        final String configured = System.getenv("CUSTOMMC_189_REAL_CLIENT_JAR");
        if ("true".equalsIgnoreCase(
                System.getenv("CUSTOMMC_189_REAL_CLIENT_REQUIRED"))) {
            assertNotNull(configured, "real client path required for V13");
            assertFalse(configured.trim().isEmpty());
        } else {
            Assumptions.assumeTrue(configured != null && !configured.trim().isEmpty(),
                    "No genuine original client for ordinary offline CI");
        }
        final Path path = Paths.get(configured);
        assertTrue(Files.isRegularFile(path));
        assertEquals(OFFICIAL_SIZE, Files.size(path));
        assertEquals(OFFICIAL_SHA1, digestFile(path));
        final Map<String,ClassNode> originals = new LinkedHashMap<String,ClassNode>();
        try (JarFile jar = new JarFile(path.toFile())) {
            for (String owner : Arrays.asList("adm", "alz", "afh", "wm", "yo", "zx")) {
                final JarEntry entry = jar.getJarEntry(owner + ".class");
                assertNotNull(entry, "original native Scaffold prerequisite missing: " + owner);
                final ClassNode node = new ClassNode();
                try (InputStream stream = jar.getInputStream(entry)) {
                    new ClassReader(readAll(stream)).accept(node, 0);
                }
                assertEquals(owner,node.name);
                originals.put(owner,node);
            }
        }
        final String[][] required = {
            {"adm","d","(Lcj;Z)Z"},         // World.isBlockLoaded
            {"adm","p","(Lcj;)Lalz;"},      // World.getBlockState
            {"adm","d","(Lcj;)Z"},         // World.isAirBlock
            {"alz","c","()Lafh;"},         // IBlockState.getBlock
            {"afh","o","()Z"},             // Block.isFullCube
            {"wm","a","(I)Lzx;"},          // InventoryPlayer.getStackInSlot
            {"yo","a","(Lzx;Lwn;Ladm;Lcj;Lcq;FFF)Z"} // ItemBlock.onItemUse
        };
        for (String[] method : required) {
            int count = 0;
            for (MethodNode nativeMethod : originals.get(method[0]).methods)
                if (method[1].equals(nativeMethod.name)
                        && method[2].equals(nativeMethod.desc)) count++;
            assertEquals(1, count, "expected exactly one genuine original API "
                    + Arrays.toString(method));
        }
        assertEquals("zw", originals.get("yo").superName,
                "source-mapped ItemBlock must subclass original Item");
        int nativeStackFields = 0;
        for (FieldNode nativeField : originals.get("zx").fields)
            if ("b".equals(nativeField.name) && "I".equals(nativeField.desc))
                nativeStackFields++;
        assertEquals(1,nativeStackFields,
                "native ItemStack.stackSize required for later server delta evidence");
        System.out.println("REAL_189_SCAFFOLD_WORLD_LOADED_STATE_AND_ITEMBLOCK_PASS=YES");
        System.out.println("REAL_189_SCAFFOLD_CANDIDATE_NATIVE_ADAPTER=NOT_YET_CONNECTED");
        System.out.println("REAL_189_SCAFFOLD_SERVER_PLACEMENT_AND_STACK_CONSUMPTION=NOT_TESTED");
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
            for (String name : Arrays.asList("bfk", "bda", "pk", "aug", "adm", "bew")) {
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

        // V3 native original-Mojang world camera proof: setupCameraTransform
        // MUST occur once inside the real renderWorldPass, and the patched
        // bridge MUST run directly after that very invocation. Never accept
        // capture from updateCameraAndRender, HUD or GUI ortho matrices.
        final int[] worldCamera = new int[3]; // original, patched, ordering
        for (int bytecode = 0; bytecode < 2; bytecode++) {
            final boolean patched = bytecode == 1;
            new ClassReader(patched ? transformedRenderer : raw.get("bfk")).accept(
                    new ClassVisitor(Opcodes.ASM9) {
                        @Override public MethodVisitor visitMethod(
                                final int access, final String method,
                                final String descriptor, final String signature,
                                final String[] exceptions) {
                            if (!"a".equals(method) || !"(IFJ)V".equals(descriptor))
                                return null;
                            return new MethodVisitor(Opcodes.ASM9) {
                                private boolean cameraSetupJustCalled;
                                @Override public void visitMethodInsn(
                                        final int opcode, final String owner,
                                        final String called, final String desc,
                                        final boolean itf) {
                                    if (opcode == Opcodes.INVOKESPECIAL && !itf
                                            && owner.equals("bfk") && called.equals("a")
                                            && desc.equals("(FI)V")) {
                                        worldCamera[0] += patched ? 0 : 1;
                                        cameraSetupJustCalled = true;
                                    } else if (patched
                                            && opcode == Opcodes.INVOKESTATIC
                                            && owner.endsWith("/Minecraft189RuntimeBridge")
                                            && called.equals("worldCameraSetupCaptured")
                                            && desc.equals("(IF)V")) {
                                        worldCamera[1]++;
                                        if (cameraSetupJustCalled) worldCamera[2]++;
                                        cameraSetupJustCalled = false;
                                    } else if (patched) {
                                        // Any intervening method call invalidates
                                        // the claimed immediate native camera seam.
                                        cameraSetupJustCalled = false;
                                    }
                                }
                            };
                        }
                    }, 0);
        }
        assertArrayEquals(new int[]{1, 1, 1}, worldCamera,
                "real 1.8.9 renderWorldPass camera seam must be native and exact");
        System.out.println("REAL_189_WORLD_CAMERA_NATIVE_RENDER_HOOK_PASS=YES");
        // Validate ORIGINAL Mojang pk/P,Q,R last-tick fields independently
        // of generated accessor bytecode. Any wrong SRG mapping is a veto.
        final int[] lastTick = new int[3];
        new ClassReader(raw.get("pk")).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public org.objectweb.asm.FieldVisitor visitField(
                    final int access, final String name, final String descriptor,
                    final String signature, final Object value) {
                if ("D".equals(descriptor)) {
                    if ("P".equals(name)) lastTick[0]++;
                    if ("Q".equals(name)) lastTick[1]++;
                    if ("R".equals(name)) lastTick[2]++;
                }
                return null;
            }
        }, 0);
        assertArrayEquals(new int[]{1, 1, 1}, lastTick,
                "original 1.8.9 pk/P,Q,R double last-tick origin fields");

        // These original Minecraft methods are *already called* throughout
        // the render pass. Count only the direct injected call chain after
        // worldCameraSetupCaptured, never unrelated vanilla mc/getView reads.
        final int[] originSequence = new int[4];
        new ClassReader(transformedRenderer).accept(
                new ClassVisitor(Opcodes.ASM9) {
                    @Override public MethodVisitor visitMethod(
                            final int access, final String name,
                            final String descriptor, final String signature,
                            final String[] exceptions) {
                        if (!"a".equals(name) || !"(IFJ)V".equals(descriptor))
                            return null;
                        return new MethodVisitor(Opcodes.ASM9) {
                            private int phase;
                            @Override public void visitFieldInsn(
                                    final int opcode, final String owner,
                                    final String name, final String descriptor) {
                                if (phase == 1 && opcode == Opcodes.GETFIELD
                                        && "bfk".equals(owner) && "h".equals(name)
                                        && "Lave;".equals(descriptor)) {
                                    originSequence[1]++;
                                    phase = 2;
                                } else if (phase != 0) {
                                    phase = 0;
                                }
                            }
                            @Override public void visitMethodInsn(
                                    final int opcode, final String owner,
                                    final String name, final String desc,
                                    final boolean itf) {
                                if (opcode == Opcodes.INVOKESTATIC
                                        && owner.endsWith("/Minecraft189RuntimeBridge")
                                        && "worldCameraSetupCaptured".equals(name)
                                        && "(IF)V".equals(desc)) {
                                    originSequence[0]++;
                                    phase = 1;
                                } else if (phase == 2
                                        && opcode == Opcodes.INVOKEVIRTUAL
                                        && "ave".equals(owner) && "ac".equals(name)
                                        && "()Lpk;".equals(desc)) {
                                    originSequence[2]++;
                                    phase = 3;
                                } else if (phase == 3
                                        && opcode == Opcodes.INVOKESTATIC
                                        && owner.endsWith("/Minecraft189RuntimeBridge")
                                        && "worldCameraViewEntityCaptured".equals(name)
                                        && "(Ljava/lang/Object;IF)V".equals(desc)) {
                                    originSequence[3]++;
                                    phase = 0;
                                } else if (phase != 0) {
                                    phase = 0;
                                }
                            }
                        };
                    }
                }, 0);
        assertArrayEquals(new int[]{1, 1, 1, 1}, originSequence,
                "world camera origin bridge follows exact source-native render-view entity");
        System.out.println("REAL_189_WORLD_CAMERA_NATIVE_ORIGIN_HOOK_PASS=YES");

        final byte[] nativeWorld = transformer.transform("adm", raw.get("adm"));
        final int[] sourceObjects = new int[3];
        new ClassReader(nativeWorld).accept(
                new ClassVisitor(Opcodes.ASM9) {
                    @Override public MethodVisitor visitMethod(
                            final int access, final String name,
                            final String descriptor, final String signature,
                            final String[] exceptions) {
                        if (!"customMcLoadedEntityObjects".equals(name)
                                || !"()[Ljava/lang/Object;".equals(descriptor)) return null;
                        sourceObjects[0]++;
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override public void visitFieldInsn(
                                    final int opcode, final String owner,
                                    final String name, final String descriptor) {
                                if (opcode == Opcodes.GETFIELD && "adm".equals(owner)
                                        && "f".equals(name)
                                        && "Ljava/util/List;".equals(descriptor))
                                    sourceObjects[1]++;
                            }
                            @Override public void visitMethodInsn(
                                    final int opcode, final String owner,
                                    final String name, final String descriptor,
                                    final boolean itf) {
                                if (opcode == Opcodes.INVOKEINTERFACE && itf
                                        && "java/util/List".equals(owner)
                                        && "toArray".equals(name)
                                        && "()[Ljava/lang/Object;".equals(descriptor))
                                    sourceObjects[2]++;
                            }
                        };
                    }
                }, 0);
        assertArrayEquals(new int[]{1, 1, 1}, sourceObjects,
                "original Mojang world.loadedEntityList must back exact live Entity references");
        final int[] sourceBridge = new int[1];
        new ClassReader(transformedRenderer).accept(
                new ClassVisitor(Opcodes.ASM9) {
                    @Override public MethodVisitor visitMethod(
                            final int access, final String name,
                            final String descriptor, final String signature,
                            final String[] exceptions) {
                        if (!"a".equals(name) || !"(IFJ)V".equals(descriptor)) return null;
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override public void visitMethodInsn(
                                    final int opcode, final String owner,
                                    final String method, final String descriptor,
                                    final boolean itf) {
                                if (opcode == Opcodes.INVOKESTATIC
                                        && owner.endsWith("/Minecraft189RuntimeBridge")
                                        && "worldCameraLoadedEntitiesCaptured".equals(method)
                                        && "(Ljava/lang/Object;IF)V".equals(descriptor))
                                    sourceBridge[0]++;
                            }
                        };
                    }
                }, 0);
        assertEquals(1, sourceBridge[0],
                "original renderer must supply native world after frame camera origin");
        System.out.println("REAL_189_WORLD_CAMERA_NATIVE_ENTITY_AABB_HOOK_PASS=YES");

        // V10: the ORIGINAL binary (not a reduced synthetic test class)
        // must contain exactly one moveEntity sneak-collision branch, and
        // the transformer must inject exactly one effective Sneak bridge.
        final byte[] nativeEntity = transformer.transform("pk", raw.get("pk"));
        final int[] originalMove = new int[2];
        final int[] mappedMove = new int[2];
        final ClassVisitor originalScan = new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(
                    final int access, final String name, final String desc,
                    final String signature, final String[] exceptions) {
                if (!"d".equals(name) || !"(DDD)V".equals(desc))
                    return null;
                originalMove[0]++;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(final int opcode,
                            final String owner, final String name,
                            final String desc, final boolean itf) {
                        if (opcode == Opcodes.INVOKEVIRTUAL && !itf
                                && "pk".equals(owner) && "av".equals(name)
                                && "()Z".equals(desc))
                            originalMove[1]++;
                    }
                };
            }
        };
        new ClassReader(raw.get("pk")).accept(originalScan, 0);
        new ClassReader(nativeEntity).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(
                    final int access, final String name, final String desc,
                    final String signature, final String[] exceptions) {
                if (!"d".equals(name) || !"(DDD)V".equals(desc))
                    return null;
                mappedMove[0]++;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(final int opcode,
                            final String owner, final String name,
                            final String desc, final boolean itf) {
                        if (opcode == Opcodes.INVOKESTATIC && !itf
                                && owner.endsWith("/Minecraft189RuntimeBridge")
                                && "nativeSafeWalkSneaking".equals(name)
                                && "(ZLjava/lang/Object;)Z".equals(desc))
                            mappedMove[1]++;
                    }
                };
            }
        }, 0);
        assertArrayEquals(new int[]{1,1}, originalMove,
                "original Mojang moveEntity must have exactly one native sneak branch");
        assertArrayEquals(new int[]{1,1}, mappedMove,
                "transformed original Mojang moveEntity must bridge exactly once");
        final byte[] localClass = transformer.transform("bew", raw.get("bew"));
        assertTrue(Arrays.asList(new ClassReader(localClass).getInterfaces())
                        .contains("dev/trexzo/custommc/platform/v1_8_9/"
                                + "Minecraft189LocalPlayerSafeWalkMarker"),
                "only original EntityPlayerSP carries local SafeWalk marker");
        System.out.println("REAL_189_NATIVE_VANILLA_SAFEWALK_HOOK_PASS=YES");

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
