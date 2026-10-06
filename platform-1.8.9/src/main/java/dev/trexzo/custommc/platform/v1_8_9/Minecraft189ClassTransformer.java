package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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

    @Override
    public boolean handles(
            final String binaryClassName) {
        return TARGET_MAIN_CLASS.equals(
                binaryClassName);
    }

    @Override
    public byte[] transform(
            final String binaryClassName,
            final byte[] originalBytes) {
        if (!handles(binaryClassName)) {
            throw new IllegalArgumentException(
                    "unsupported Minecraft 1.8.9 transform target: "
                            + binaryClassName);
        }

        final byte[] input =
                Objects.requireNonNull(
                        originalBytes,
                        "originalBytes");
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
}
