package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Objects;

public final class Minecraft189ClassTransformer
        implements BootstrapClassTransformer {
    public static final String TARGET_MAIN_CLASS =
            "net.minecraft.client.main.Main";

    private static final String TARGET_MAIN_METHOD =
            "main";
    private static final String TARGET_MAIN_DESCRIPTOR =
            "([Ljava/lang/String;)V";
    private static final String RUNTIME_BRIDGE_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189RuntimeBridge";
    private static final String HOST_BINDING_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189LwjglHostBinding";
    private static final String HOST_RUNTIME_DESCRIPTOR =
            "Ldev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189HostRuntime;";
    private static final String GUI_SETTINGS_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/ui/"
                    + "Minecraft189GuiSettingsAccess";
    private static final String GUI_SETTINGS_ACCESS_DESCRIPTOR =
            "L" + GUI_SETTINGS_ACCESS_INTERNAL_NAME + ";";
    private static final String FONT_RENDERER_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/ui/"
                    + "Minecraft189FontRendererAccess";
    private static final String FONT_RENDERER_ACCESS_DESCRIPTOR =
            "L" + FONT_RENDERER_ACCESS_INTERNAL_NAME + ";";

    @Override
    public boolean handles(
            final String binaryClassName) {
        return TARGET_MAIN_CLASS.equals(
                binaryClassName)
                || Minecraft189Mappings.MINECRAFT
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.GAME_SETTINGS
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.FONT_RENDERER
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.GUI_INGAME
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.ENTITY_RENDERER
                .obfuscatedBinaryName()
                .equals(binaryClassName);
    }

    @Override
    public byte[] transform(
            final String binaryClassName,
            final byte[] originalBytes) {
        final byte[] input =
                Objects.requireNonNull(
                        originalBytes,
                        "originalBytes");

        if (TARGET_MAIN_CLASS.equals(
                binaryClassName)) {
            return transformTargetMain(input);
        }
        if (Minecraft189Mappings.MINECRAFT
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyMinecraft(input);
            return transformMinecraft(input);
        }
        if (Minecraft189Mappings.GAME_SETTINGS
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyGameSettings(input);
            return transformGameSettings(input);
        }
        if (Minecraft189Mappings.FONT_RENDERER
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyFontRenderer(input);
            return transformFontRenderer(input);
        }
        if (Minecraft189Mappings.GUI_INGAME
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyGuiIngame(input);
            return transformGuiIngame(input);
        }
        if (Minecraft189Mappings.ENTITY_RENDERER
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyEntityRenderer(input);
            return transformEntityRenderer(input);
        }

        throw new IllegalArgumentException(
                "unsupported Minecraft 1.8.9 transform target: "
                        + binaryClassName);
    }

    private static byte[] transformTargetMain(
            final byte[] input) {
        final ClassReader reader =
                new ClassReader(input);
        final ClassWriter writer =
                new ClassWriter(
                        reader,
                        0);
        final boolean[] found =
                new boolean[]{false};
        final boolean[] injected =
                new boolean[]{false};

        reader.accept(
                new ClassVisitor(
                        Opcodes.ASM9,
                        writer) {
                    @Override
                    public MethodVisitor visitMethod(
                            final int access,
                            final String name,
                            final String descriptor,
                            final String signature,
                            final String[] exceptions) {
                        final MethodVisitor delegate =
                                super.visitMethod(
                                        access,
                                        name,
                                        descriptor,
                                        signature,
                                        exceptions);

                        if (!TARGET_MAIN_METHOD.equals(name)
                                || !TARGET_MAIN_DESCRIPTOR.equals(
                                descriptor)
                                || (access & Opcodes.ACC_STATIC) == 0) {
                            return delegate;
                        }

                        if (found[0]) {
                            throw new IllegalStateException(
                                    "duplicate Minecraft target main method");
                        }
                        found[0] = true;

                        return new MethodVisitor(
                                Opcodes.ASM9,
                                delegate) {
                            @Override
                            public void visitCode() {
                                super.visitCode();
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        RUNTIME_BRIDGE_INTERNAL_NAME,
                                        "targetMainEntered",
                                        "()V",
                                        false);
                                injected[0] = true;
                            }
                        };
                    }
                },
                0);

        if (!found[0] || !injected[0]) {
            throw new IllegalStateException(
                    "Minecraft 1.8.9 target main method was not patchable");
        }

        return writer.toByteArray();
    }

    private static byte[] transformMinecraft(
            final byte[] input) {
        final ClassReader reader =
                new ClassReader(input);
        final ClassWriter writer =
                new ClassWriter(
                        reader,
                        ClassWriter.COMPUTE_MAXS);
        final boolean[] foundStartGame =
                new boolean[]{false};
        final boolean[] injectedHost =
                new boolean[]{false};
        final boolean[] foundRunTick =
                new boolean[]{false};
        final boolean[] injectedTick =
                new boolean[]{false};

        reader.accept(
                new ClassVisitor(
                        Opcodes.ASM9,
                        writer) {
                    @Override
                    public MethodVisitor visitMethod(
                            final int access,
                            final String name,
                            final String descriptor,
                            final String signature,
                            final String[] exceptions) {
                        final MethodVisitor delegate =
                                super.visitMethod(
                                        access,
                                        name,
                                        descriptor,
                                        signature,
                                        exceptions);

                        final Minecraft189Mappings.MappedMethod startGame =
                                Minecraft189Mappings.MINECRAFT_START_GAME;
                        if (startGame.obfuscatedName().equals(name)
                                && startGame.descriptor().equals(
                                descriptor)) {
                            if (foundStartGame[0]) {
                                throw new IllegalStateException(
                                        "duplicate mapped Minecraft startGame method");
                            }
                            foundStartGame[0] = true;

                            return new MethodVisitor(
                                    Opcodes.ASM9,
                                    delegate) {
                                @Override
                                public void visitInsn(
                                        final int opcode) {
                                    if (opcode == Opcodes.RETURN) {
                                        injectHostInstall(this);
                                        injectedHost[0] = true;
                                    }
                                    super.visitInsn(opcode);
                                }
                            };
                        }

                        final Minecraft189Mappings.MappedMethod runTick =
                                Minecraft189Mappings.MINECRAFT_RUN_TICK;
                        if (runTick.obfuscatedName().equals(name)
                                && runTick.descriptor().equals(
                                descriptor)) {
                            if (foundRunTick[0]) {
                                throw new IllegalStateException(
                                        "duplicate mapped Minecraft runTick method");
                            }
                            foundRunTick[0] = true;

                            return new MethodVisitor(
                                    Opcodes.ASM9,
                                    delegate) {
                                @Override
                                public void visitCode() {
                                    super.visitCode();
                                    super.visitMethodInsn(
                                            Opcodes.INVOKESTATIC,
                                            RUNTIME_BRIDGE_INTERNAL_NAME,
                                            "gameTick",
                                            "()V",
                                            false);
                                    injectedTick[0] = true;
                                }
                            };
                        }

                        return delegate;
                    }
                },
                0);

        if (!foundStartGame[0]
                || !injectedHost[0]) {
            throw new IllegalStateException(
                    "mapped Minecraft startGame method was not patchable");
        }
        if (!foundRunTick[0]
                || !injectedTick[0]) {
            throw new IllegalStateException(
                    "mapped Minecraft runTick method was not patchable");
        }
        return writer.toByteArray();
    }

    private static void injectHostInstall(
            final MethodVisitor visitor) {
        final Minecraft189Mappings.MappedClass minecraft =
                Minecraft189Mappings.MINECRAFT;
        final Minecraft189Mappings.MappedField settings =
                Minecraft189Mappings.MINECRAFT_GAME_SETTINGS;
        final Minecraft189Mappings.MappedField font =
                Minecraft189Mappings.MINECRAFT_FONT_RENDERER;

        visitor.visitVarInsn(
                Opcodes.ALOAD,
                0);
        visitor.visitFieldInsn(
                Opcodes.GETFIELD,
                minecraft.obfuscatedInternalName(),
                settings.obfuscatedName(),
                settings.descriptor());
        visitor.visitTypeInsn(
                Opcodes.CHECKCAST,
                GUI_SETTINGS_ACCESS_INTERNAL_NAME);

        visitor.visitVarInsn(
                Opcodes.ALOAD,
                0);
        visitor.visitFieldInsn(
                Opcodes.GETFIELD,
                minecraft.obfuscatedInternalName(),
                font.obfuscatedName(),
                font.descriptor());
        visitor.visitTypeInsn(
                Opcodes.CHECKCAST,
                FONT_RENDERER_ACCESS_INTERNAL_NAME);

        visitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                HOST_BINDING_INTERNAL_NAME,
                "install",
                "("
                        + GUI_SETTINGS_ACCESS_DESCRIPTOR
                        + FONT_RENDERER_ACCESS_DESCRIPTOR
                        + ")"
                        + HOST_RUNTIME_DESCRIPTOR,
                false);
        visitor.visitInsn(
                Opcodes.POP);
    }

    private static byte[] transformGameSettings(
            final byte[] input) {
        final ClassReader reader =
                new ClassReader(input);
        final ClassWriter writer =
                new ClassWriter(
                        reader,
                        ClassWriter.COMPUTE_MAXS);

        reader.accept(
                new ClassVisitor(
                        Opcodes.ASM9,
                        writer) {
                    @Override
                    public void visit(
                            final int version,
                            final int access,
                            final String name,
                            final String signature,
                            final String superName,
                            final String[] interfaces) {
                        super.visit(
                                version,
                                access,
                                name,
                                signature,
                                superName,
                                withInterface(
                                        interfaces,
                                        GUI_SETTINGS_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addIntFieldGetter(
                                cv,
                                "configuredGuiScale",
                                Minecraft189Mappings
                                        .GAME_SETTINGS_GUI_SCALE);
                        addBooleanFieldGetter(
                                cv,
                                "unicode",
                                Minecraft189Mappings
                                        .GAME_SETTINGS_FORCE_UNICODE);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    private static byte[] transformFontRenderer(
            final byte[] input) {
        final ClassReader reader =
                new ClassReader(input);
        final ClassWriter writer =
                new ClassWriter(
                        reader,
                        ClassWriter.COMPUTE_MAXS);

        reader.accept(
                new ClassVisitor(
                        Opcodes.ASM9,
                        writer) {
                    @Override
                    public void visit(
                            final int version,
                            final int access,
                            final String name,
                            final String signature,
                            final String superName,
                            final String[] interfaces) {
                        super.visit(
                                version,
                                access,
                                name,
                                signature,
                                superName,
                                withInterface(
                                        interfaces,
                                        FONT_RENDERER_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        final Minecraft189Mappings.MappedMethod drawString =
                                Minecraft189Mappings
                                        .FONT_RENDERER_DRAW_STRING;
                        final MethodVisitor method =
                                cv.visitMethod(
                                        Opcodes.ACC_PUBLIC,
                                        "drawString",
                                        drawString.descriptor(),
                                        null,
                                        null);
                        method.visitCode();
                        method.visitVarInsn(
                                Opcodes.ALOAD,
                                0);
                        method.visitVarInsn(
                                Opcodes.ALOAD,
                                1);
                        method.visitVarInsn(
                                Opcodes.FLOAD,
                                2);
                        method.visitVarInsn(
                                Opcodes.FLOAD,
                                3);
                        method.visitVarInsn(
                                Opcodes.ILOAD,
                                4);
                        method.visitVarInsn(
                                Opcodes.ILOAD,
                                5);
                        method.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL,
                                drawString.owner()
                                        .obfuscatedInternalName(),
                                drawString.obfuscatedName(),
                                drawString.descriptor(),
                                false);
                        method.visitInsn(
                                Opcodes.IRETURN);
                        method.visitMaxs(
                                0,
                                0);
                        method.visitEnd();
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    private static byte[] transformGuiIngame(
            final byte[] input) {
        final ClassReader reader =
                new ClassReader(input);
        final ClassWriter writer =
                new ClassWriter(
                        reader,
                        ClassWriter.COMPUTE_MAXS);
        final boolean[] found =
                new boolean[]{false};
        final boolean[] injected =
                new boolean[]{false};

        reader.accept(
                new ClassVisitor(
                        Opcodes.ASM9,
                        writer) {
                    @Override
                    public MethodVisitor visitMethod(
                            final int access,
                            final String name,
                            final String descriptor,
                            final String signature,
                            final String[] exceptions) {
                        final MethodVisitor delegate =
                                super.visitMethod(
                                        access,
                                        name,
                                        descriptor,
                                        signature,
                                        exceptions);
                        final Minecraft189Mappings.MappedMethod overlay =
                                Minecraft189Mappings
                                        .GUI_INGAME_RENDER_OVERLAY;
                        if (!overlay.obfuscatedName().equals(name)
                                || !overlay.descriptor().equals(
                                descriptor)) {
                            return delegate;
                        }
                        if (found[0]) {
                            throw new IllegalStateException(
                                    "duplicate mapped GuiIngame renderGameOverlay method");
                        }
                        found[0] = true;

                        return new MethodVisitor(
                                Opcodes.ASM9,
                                delegate) {
                            @Override
                            public void visitInsn(
                                    final int opcode) {
                                if (opcode == Opcodes.RETURN) {
                                    super.visitVarInsn(
                                            Opcodes.FLOAD,
                                            1);
                                    super.visitMethodInsn(
                                            Opcodes.INVOKESTATIC,
                                            RUNTIME_BRIDGE_INTERNAL_NAME,
                                            "renderHudFrame",
                                            "(F)V",
                                            false);
                                    injected[0] = true;
                                }
                                super.visitInsn(opcode);
                            }
                        };
                    }
                },
                0);

        if (!found[0] || !injected[0]) {
            throw new IllegalStateException(
                    "mapped GuiIngame renderGameOverlay method was not patchable");
        }
        return writer.toByteArray();
    }

    private static byte[] transformEntityRenderer(
            final byte[] input) {
        final ClassReader reader =
                new ClassReader(input);
        final ClassWriter writer =
                new ClassWriter(
                        reader,
                        ClassWriter.COMPUTE_MAXS);
        final boolean[] found =
                new boolean[]{false};
        final boolean[] injectedStart =
                new boolean[]{false};
        final boolean[] injectedEnd =
                new boolean[]{false};

        reader.accept(
                new ClassVisitor(
                        Opcodes.ASM9,
                        writer) {
                    @Override
                    public MethodVisitor visitMethod(
                            final int access,
                            final String name,
                            final String descriptor,
                            final String signature,
                            final String[] exceptions) {
                        final MethodVisitor delegate =
                                super.visitMethod(
                                        access,
                                        name,
                                        descriptor,
                                        signature,
                                        exceptions);
                        final Minecraft189Mappings.MappedMethod render =
                                Minecraft189Mappings
                                        .ENTITY_RENDERER_UPDATE_CAMERA_AND_RENDER;
                        if (!render.obfuscatedName().equals(name)
                                || !render.descriptor().equals(
                                descriptor)) {
                            return delegate;
                        }
                        if (found[0]) {
                            throw new IllegalStateException(
                                    "duplicate mapped EntityRenderer updateCameraAndRender method");
                        }
                        found[0] = true;

                        return new MethodVisitor(
                                Opcodes.ASM9,
                                delegate) {
                            @Override
                            public void visitCode() {
                                super.visitCode();
                                super.visitVarInsn(
                                        Opcodes.FLOAD,
                                        1);
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        RUNTIME_BRIDGE_INTERNAL_NAME,
                                        "renderFrameStarted",
                                        "(F)V",
                                        false);
                                injectedStart[0] = true;
                            }

                            @Override
                            public void visitInsn(
                                    final int opcode) {
                                if (opcode == Opcodes.RETURN) {
                                    super.visitVarInsn(
                                            Opcodes.FLOAD,
                                            1);
                                    super.visitMethodInsn(
                                            Opcodes.INVOKESTATIC,
                                            RUNTIME_BRIDGE_INTERNAL_NAME,
                                            "renderPostProcessFrame",
                                            "(F)V",
                                            false);
                                    injectedEnd[0] = true;
                                }
                                super.visitInsn(opcode);
                            }
                        };
                    }
                },
                0);

        if (!found[0]
                || !injectedStart[0]
                || !injectedEnd[0]) {
            throw new IllegalStateException(
                    "mapped EntityRenderer updateCameraAndRender method was not patchable");
        }
        return writer.toByteArray();
    }

    private static void addIntFieldGetter(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedField field) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()I",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                field.owner().obfuscatedInternalName(),
                field.obfuscatedName(),
                field.descriptor());
        method.visitInsn(
                Opcodes.IRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addBooleanFieldGetter(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedField field) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()Z",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                field.owner().obfuscatedInternalName(),
                field.obfuscatedName(),
                field.descriptor());
        method.visitInsn(
                Opcodes.IRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static String[] withInterface(
            final String[] interfaces,
            final String interfaceName) {
        final String[] existing =
                interfaces == null
                        ? new String[0]
                        : interfaces.clone();
        for (String current : existing) {
            if (interfaceName.equals(current)) {
                return existing;
            }
        }
        final String[] expanded =
                Arrays.copyOf(
                        existing,
                        existing.length + 1);
        expanded[existing.length] =
                interfaceName;
        return expanded;
    }
}
