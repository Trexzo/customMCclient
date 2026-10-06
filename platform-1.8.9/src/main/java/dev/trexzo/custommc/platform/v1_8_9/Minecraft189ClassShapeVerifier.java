package dev.trexzo.custommc.platform.v1_8_9;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Objects;

public final class Minecraft189ClassShapeVerifier {
    private Minecraft189ClassShapeVerifier() {
    }

    public static void verifyMinecraft(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.MINECRAFT,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.MINECRAFT_FONT_RENDERER,
                        Minecraft189Mappings.MINECRAFT_ENTITY_RENDERER,
                        Minecraft189Mappings.MINECRAFT_INGAME_GUI,
                        Minecraft189Mappings.MINECRAFT_GAME_SETTINGS
                },
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.MINECRAFT_GET_MINECRAFT,
                        Minecraft189Mappings.MINECRAFT_START_GAME,
                        Minecraft189Mappings.MINECRAFT_RUN_TICK,
                        Minecraft189Mappings.MINECRAFT_CLICK_MOUSE,
                        Minecraft189Mappings.MINECRAFT_RIGHT_CLICK_MOUSE,
                        Minecraft189Mappings.MINECRAFT_MIDDLE_CLICK_MOUSE,
                        Minecraft189Mappings.MINECRAFT_DISPATCH_KEYPRESSES
                });
    }

    public static void verifyKeyBinding(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.KEY_BINDING,
                new Minecraft189Mappings.MappedField[0],
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.KEY_BINDING_SET_KEY_BIND_STATE
                });
    }

    public static void verifyGameSettings(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.GAME_SETTINGS,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.GAME_SETTINGS_GUI_SCALE,
                        Minecraft189Mappings.GAME_SETTINGS_FORCE_UNICODE
                },
                new Minecraft189Mappings.MappedMethod[0]);
    }

    public static void verifyFontRenderer(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.FONT_RENDERER,
                new Minecraft189Mappings.MappedField[0],
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.FONT_RENDERER_DRAW_STRING
                });
    }

    public static void verifyGuiIngame(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.GUI_INGAME,
                new Minecraft189Mappings.MappedField[0],
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.GUI_INGAME_RENDER_OVERLAY
                });
    }

    public static void verifyEntityRenderer(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.ENTITY_RENDERER,
                new Minecraft189Mappings.MappedField[0],
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings
                                .ENTITY_RENDERER_UPDATE_CAMERA_AND_RENDER
                });
    }

    private static void verify(
            final byte[] classBytes,
            final Minecraft189Mappings.MappedClass expectedClass,
            final Minecraft189Mappings.MappedField[] expectedFields,
            final Minecraft189Mappings.MappedMethod[] expectedMethods) {
        final byte[] bytes =
                Objects.requireNonNull(
                        classBytes,
                        "classBytes");
        final Minecraft189Mappings.MappedClass mappedClass =
                Objects.requireNonNull(
                        expectedClass,
                        "expectedClass");
        final Minecraft189Mappings.MappedField[] fields =
                expectedFields.clone();
        final Minecraft189Mappings.MappedMethod[] methods =
                expectedMethods.clone();

        final ClassReader reader =
                new ClassReader(bytes);
        final String actualName =
                reader.getClassName();
        if (!mappedClass.obfuscatedInternalName()
                .equals(actualName)) {
            throw new IllegalStateException(
                    "Minecraft 1.8.9 mapping owner mismatch: expected "
                            + mappedClass.obfuscatedInternalName()
                            + " but found "
                            + actualName);
        }

        final boolean[] fieldMatches =
                new boolean[fields.length];
        final boolean[] methodMatches =
                new boolean[methods.length];

        reader.accept(
                new ClassVisitor(
                        Opcodes.ASM9) {
                    @Override
                    public FieldVisitor visitField(
                            final int access,
                            final String name,
                            final String descriptor,
                            final String signature,
                            final Object value) {
                        for (int index = 0;
                             index < fields.length;
                             index++) {
                            final Minecraft189Mappings.MappedField field =
                                    fields[index];
                            if (field.obfuscatedName()
                                    .equals(name)
                                    && field.descriptor()
                                    .equals(descriptor)) {
                                fieldMatches[index] = true;
                            }
                        }
                        return null;
                    }

                    @Override
                    public MethodVisitor visitMethod(
                            final int access,
                            final String name,
                            final String descriptor,
                            final String signature,
                            final String[] exceptions) {
                        for (int index = 0;
                             index < methods.length;
                             index++) {
                            final Minecraft189Mappings.MappedMethod method =
                                    methods[index];
                            if (method.obfuscatedName()
                                    .equals(name)
                                    && method.descriptor()
                                    .equals(descriptor)) {
                                methodMatches[index] = true;
                            }
                        }
                        return null;
                    }
                },
                ClassReader.SKIP_CODE
                        | ClassReader.SKIP_DEBUG
                        | ClassReader.SKIP_FRAMES);

        for (int index = 0;
             index < fields.length;
             index++) {
            if (!fieldMatches[index]) {
                final Minecraft189Mappings.MappedField missing =
                        fields[index];
                throw new IllegalStateException(
                        "Minecraft 1.8.9 mapping field missing: "
                                + mappedClass.obfuscatedInternalName()
                                + "."
                                + missing.obfuscatedName()
                                + " "
                                + missing.descriptor()
                                + " ("
                                + missing.mcpName()
                                + ")");
            }
        }

        for (int index = 0;
             index < methods.length;
             index++) {
            if (!methodMatches[index]) {
                final Minecraft189Mappings.MappedMethod missing =
                        methods[index];
                throw new IllegalStateException(
                        "Minecraft 1.8.9 mapping method missing: "
                                + mappedClass.obfuscatedInternalName()
                                + "."
                                + missing.obfuscatedName()
                                + missing.descriptor()
                                + " ("
                                + missing.mcpName()
                                + ")");
            }
        }
    }
}
