package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189FontRendererAccess;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189MappedHostTransformationTest {
    private static final String GUI_SETTINGS_ACCESS =
            "dev/trexzo/custommc/platform/v1_8_9/ui/"
                    + "Minecraft189GuiSettingsAccess";
    private static final String FONT_RENDERER_ACCESS =
            "dev/trexzo/custommc/platform/v1_8_9/ui/"
                    + "Minecraft189FontRendererAccess";
    private static final String HOST_BINDING =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189LwjglHostBinding";

    @Test
    void transformerClaimsOnlyStableMainAndMappedHostOwners() {
        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();

        assertTrue(
                transformer.handles(
                        Minecraft189ClassTransformer
                                .TARGET_MAIN_CLASS));
        assertTrue(transformer.handles("ave"));
        assertTrue(transformer.handles("avh"));
        assertTrue(transformer.handles("avn"));

        assertFalse(transformer.handles("avo"));
        assertFalse(transformer.handles("bfk"));
        assertFalse(
                transformer.handles(
                        "net.minecraft.client.Minecraft"));
    }

    @Test
    void mappedSettingsAndFontBecomeTypedParentOwnedAccessors()
            throws Exception {
        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();

        final byte[] settingsBytes =
                transformer.transform(
                        "avh",
                        gameSettingsShape());
        final byte[] fontBytes =
                transformer.transform(
                        "avn",
                        fontRendererShape());

        assertTrue(
                hasInterface(
                        settingsBytes,
                        GUI_SETTINGS_ACCESS));
        assertTrue(
                hasInterface(
                        fontBytes,
                        FONT_RENDERER_ACCESS));

        final ByteMapClassLoader loader =
                new ByteMapClassLoader(
                        getClass().getClassLoader());
        loader.put("avh", settingsBytes);
        loader.put("avn", fontBytes);

        final Class<?> settingsClass =
                loader.loadClass("avh");
        final Object settings =
                settingsClass.getDeclaredConstructor()
                        .newInstance();
        final Field guiScale =
                settingsClass.getField("aL");
        final Field unicode =
                settingsClass.getField("aO");
        guiScale.setInt(settings, 3);
        unicode.setBoolean(settings, true);

        final Minecraft189GuiSettingsAccess settingsAccess =
                (Minecraft189GuiSettingsAccess) settings;
        assertEquals(
                3,
                settingsAccess.configuredGuiScale());
        assertTrue(settingsAccess.unicode());

        final Class<?> fontClass =
                loader.loadClass("avn");
        final Minecraft189FontRendererAccess fontAccess =
                (Minecraft189FontRendererAccess)
                        fontClass.getDeclaredConstructor()
                                .newInstance();

        assertEquals(
                91,
                fontAccess.drawString(
                        "hello",
                        1.0F,
                        2.0F,
                        0xFFFFFFFF,
                        false));
    }

    @Test
    void mappedMinecraftStartGameInstallsTheExistingHostBinding()
            throws Exception {
        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();
        final byte[] transformedMinecraft =
                transformer.transform(
                        "ave",
                        minecraftShape());

        assertEquals(
                1,
                hostInstallCalls(
                        transformedMinecraft));
        assertEquals(
                1,
                gameTickCalls(
                        transformedMinecraft));

        final ByteMapClassLoader loader =
                new ByteMapClassLoader(
                        getClass().getClassLoader());
        loader.put(
                "avh",
                transformer.transform(
                        "avh",
                        gameSettingsShape()));
        loader.put(
                "avn",
                transformer.transform(
                        "avn",
                        fontRendererShape()));
        loader.put(
                "bfk",
                emptyClass("bfk"));
        loader.put(
                "avo",
                emptyClass("avo"));
        loader.put(
                "ave",
                transformedMinecraft);

        final Minecraft189BootstrapRuntime runtime =
                Minecraft189BootstrapRuntime.create(
                        new BootstrapContext(
                                Minecraft189ClassTransformer
                                        .TARGET_MAIN_CLASS,
                                new String[0]));
        try {
            assertFalse(
                    Minecraft189RuntimeBridge
                            .hostInstalled());

            final List<Long> ticks =
                    new ArrayList<Long>();
            runtime.events()
                    .subscribe(
                            Minecraft189Hooks.TickEvent.class,
                            event -> ticks.add(
                                    event.tickIndex()));

            final Class<?> minecraftClass =
                    loader.loadClass("ave");
            final Object minecraft =
                    minecraftClass.getDeclaredConstructor()
                            .newInstance();

            minecraftClass.getField("t")
                    .set(
                            minecraft,
                            loader.loadClass("avh")
                                    .getDeclaredConstructor()
                                    .newInstance());
            minecraftClass.getField("k")
                    .set(
                            minecraft,
                            loader.loadClass("avn")
                                    .getDeclaredConstructor()
                                    .newInstance());

            final Method startGame =
                    minecraftClass.getMethod("am");
            startGame.invoke(minecraft);

            assertTrue(
                    Minecraft189RuntimeBridge
                            .hostInstalled());
            assertTrue(runtime.hostInstalled());

            final Method runTick =
                    minecraftClass.getMethod("s");
            runTick.invoke(minecraft);
            runTick.invoke(minecraft);

            assertEquals(
                    Arrays.asList(
                            0L,
                            1L),
                    ticks);
        } finally {
            runtime.close();
        }

        assertFalse(
                Minecraft189RuntimeBridge.active());
    }

    private static boolean hasInterface(
            final byte[] bytes,
            final String expected) {
        final boolean[] found =
                new boolean[]{false};
        new ClassReader(bytes)
                .accept(
                        new ClassVisitor(
                                Opcodes.ASM9) {
                            @Override
                            public void visit(
                                    final int version,
                                    final int access,
                                    final String name,
                                    final String signature,
                                    final String superName,
                                    final String[] interfaces) {
                                if (interfaces != null) {
                                    for (String current : interfaces) {
                                        if (expected.equals(current)) {
                                            found[0] = true;
                                        }
                                    }
                                }
                            }
                        },
                        ClassReader.SKIP_CODE
                                | ClassReader.SKIP_DEBUG
                                | ClassReader.SKIP_FRAMES);
        return found[0];
    }

    private static int hostInstallCalls(
            final byte[] bytes) {
        final int[] calls =
                new int[]{0};
        new ClassReader(bytes)
                .accept(
                        new ClassVisitor(
                                Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(
                                    final int access,
                                    final String name,
                                    final String descriptor,
                                    final String signature,
                                    final String[] exceptions) {
                                if (!"am".equals(name)
                                        || !"()V".equals(
                                        descriptor)) {
                                    return null;
                                }
                                return new MethodVisitor(
                                        Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            final int opcode,
                                            final String owner,
                                            final String methodName,
                                            final String methodDescriptor,
                                            final boolean isInterface) {
                                        if (opcode
                                                == Opcodes.INVOKESTATIC
                                                && HOST_BINDING.equals(
                                                owner)
                                                && "install".equals(
                                                methodName)) {
                                            calls[0]++;
                                        }
                                    }
                                };
                            }
                        },
                        0);
        return calls[0];
    }

    private static int gameTickCalls(
            final byte[] bytes) {
        final int[] calls =
                new int[]{0};
        new ClassReader(bytes)
                .accept(
                        new ClassVisitor(
                                Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(
                                    final int access,
                                    final String name,
                                    final String descriptor,
                                    final String signature,
                                    final String[] exceptions) {
                                if (!"s".equals(name)
                                        || !"()V".equals(
                                        descriptor)) {
                                    return null;
                                }
                                return new MethodVisitor(
                                        Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            final int opcode,
                                            final String owner,
                                            final String methodName,
                                            final String methodDescriptor,
                                            final boolean isInterface) {
                                        if (opcode
                                                == Opcodes.INVOKESTATIC
                                                && "dev/trexzo/custommc/platform/v1_8_9/Minecraft189RuntimeBridge"
                                                .equals(owner)
                                                && "gameTick".equals(
                                                methodName)
                                                && "()V".equals(
                                                methodDescriptor)) {
                                            calls[0]++;
                                        }
                                    }
                                };
                            }
                        },
                        0);
        return calls[0];
    }

    private static byte[] gameSettingsShape() {
        final ClassWriter writer =
                classWriter("avh");
        field(writer, "aL", "I");
        field(writer, "aO", "Z");
        endDefaultConstructor(writer, "avh");
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] fontRendererShape() {
        final ClassWriter writer =
                classWriter("avn");
        endDefaultConstructor(writer, "avn");

        final MethodVisitor draw =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "a",
                        "(Ljava/lang/String;FFIZ)I",
                        null,
                        null);
        draw.visitCode();
        draw.visitIntInsn(
                Opcodes.BIPUSH,
                91);
        draw.visitInsn(
                Opcodes.IRETURN);
        draw.visitMaxs(1, 6);
        draw.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] minecraftShape() {
        final ClassWriter writer =
                classWriter("ave");
        field(writer, "k", "Lavn;");
        field(writer, "o", "Lbfk;");
        field(writer, "q", "Lavo;");
        field(writer, "t", "Lavh;");
        endDefaultConstructor(writer, "ave");

        final MethodVisitor getMinecraft =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC
                                | Opcodes.ACC_STATIC,
                        "A",
                        "()Lave;",
                        null,
                        null);
        getMinecraft.visitCode();
        getMinecraft.visitInsn(
                Opcodes.ACONST_NULL);
        getMinecraft.visitInsn(
                Opcodes.ARETURN);
        getMinecraft.visitMaxs(1, 0);
        getMinecraft.visitEnd();

        voidMethod(writer, "am");
        voidMethod(writer, "s");

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] emptyClass(
            final String name) {
        final ClassWriter writer =
                classWriter(name);
        endDefaultConstructor(writer, name);
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static ClassWriter classWriter(
            final String name) {
        final ClassWriter writer =
                new ClassWriter(0);
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                name,
                null,
                "java/lang/Object",
                null);
        return writer;
    }

    private static void field(
            final ClassWriter writer,
            final String name,
            final String descriptor) {
        final FieldVisitor field =
                writer.visitField(
                        Opcodes.ACC_PUBLIC,
                        name,
                        descriptor,
                        null,
                        null);
        field.visitEnd();
    }

    private static void endDefaultConstructor(
            final ClassWriter writer,
            final String owner) {
        final MethodVisitor constructor =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        "()V",
                        null,
                        null);
        constructor.visitCode();
        constructor.visitVarInsn(
                Opcodes.ALOAD,
                0);
        constructor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V",
                false);
        constructor.visitInsn(
                Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
    }

    private static void voidMethod(
            final ClassWriter writer,
            final String name) {
        final MethodVisitor method =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        name,
                        "()V",
                        null,
                        null);
        method.visitCode();
        method.visitInsn(
                Opcodes.RETURN);
        method.visitMaxs(0, 1);
        method.visitEnd();
    }

    private static final class ByteMapClassLoader
            extends ClassLoader {
        private final Map<String, byte[]> classes =
                new LinkedHashMap<String, byte[]>();

        private ByteMapClassLoader(
                final ClassLoader parent) {
            super(parent);
        }

        private void put(
                final String name,
                final byte[] bytes) {
            classes.put(
                    name,
                    bytes.clone());
        }

        @Override
        protected Class<?> findClass(
                final String name)
                throws ClassNotFoundException {
            final byte[] bytes =
                    classes.get(name);
            if (bytes == null) {
                throw new ClassNotFoundException(
                        name);
            }
            return defineClass(
                    name,
                    bytes,
                    0,
                    bytes.length);
        }
    }
}
