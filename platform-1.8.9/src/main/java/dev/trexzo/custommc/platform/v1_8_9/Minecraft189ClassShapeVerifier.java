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
                        Minecraft189Mappings.MINECRAFT_PLAYER,
                        Minecraft189Mappings.MINECRAFT_WORLD,
                        Minecraft189Mappings.MINECRAFT_FONT_RENDERER,
                        Minecraft189Mappings.MINECRAFT_ENTITY_RENDERER,
                        Minecraft189Mappings.MINECRAFT_INGAME_GUI,
                        Minecraft189Mappings.MINECRAFT_GAME_SETTINGS,
                        Minecraft189Mappings.MINECRAFT_CURRENT_SERVER_DATA
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

    public static void verifyWorld(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.WORLD,
                new Minecraft189Mappings.MappedField[0],
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.WORLD_GET_WORLD_TIME
                });
    }

    public static void verifyServerData(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.SERVER_DATA,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.SERVER_DATA_SERVER_IP
                },
                new Minecraft189Mappings.MappedMethod[0]);
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
                        Minecraft189Mappings.GAME_SETTINGS_VIEW_BOBBING,
                        Minecraft189Mappings.GAME_SETTINGS_FOV,
                        Minecraft189Mappings.GAME_SETTINGS_GAMMA,
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

    public static void verifyEntity(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.ENTITY,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.ENTITY_POS_X,
                        Minecraft189Mappings.ENTITY_POS_Y,
                        Minecraft189Mappings.ENTITY_POS_Z,
                        Minecraft189Mappings.ENTITY_ROTATION_YAW
                },
                new Minecraft189Mappings.MappedMethod[0]);
    }

    public static void verifyEntityLivingBase(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.ENTITY_LIVING_BASE,
                new Minecraft189Mappings.MappedField[0],
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.ENTITY_LIVING_BASE_GET_HEALTH,
                        Minecraft189Mappings.ENTITY_LIVING_BASE_GET_MAX_HEALTH,
                        Minecraft189Mappings.ENTITY_LIVING_BASE_GET_EQUIPMENT_IN_SLOT,
                        Minecraft189Mappings.ENTITY_LIVING_BASE_GET_ACTIVE_POTION_EFFECTS
                });
    }

    public static void verifyAbstractClientPlayer(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.ABSTRACT_CLIENT_PLAYER,
                new Minecraft189Mappings.MappedField[0],
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.ABSTRACT_CLIENT_PLAYER_GET_PLAYER_INFO
                });
    }

    public static void verifyNetworkPlayerInfo(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.NETWORK_PLAYER_INFO,
                new Minecraft189Mappings.MappedField[0],
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.NETWORK_PLAYER_INFO_GET_RESPONSE_TIME
                });
    }

    public static void verifyEntityPlayer(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.ENTITY_PLAYER,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_LEVEL,
                        Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_TOTAL,
                        Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_PROGRESS
                },
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.ENTITY_PLAYER_GET_FOOD_STATS,
                        Minecraft189Mappings.ENTITY_PLAYER_XP_BAR_CAP
                });
    }

    public static void verifyItemStack(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.ITEM_STACK,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.ITEM_STACK_SIZE
                },
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.ITEM_STACK_GET_DISPLAY_NAME,
                        Minecraft189Mappings.ITEM_STACK_GET_ITEM_DAMAGE,
                        Minecraft189Mappings.ITEM_STACK_GET_MAX_DAMAGE
                });
    }

    public static void verifyFoodStats(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.FOOD_STATS,
                new Minecraft189Mappings.MappedField[0],
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.FOOD_STATS_GET_FOOD_LEVEL,
                        Minecraft189Mappings.FOOD_STATS_GET_SATURATION_LEVEL
                });
    }

    public static void verifyPotionEffect(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.POTION_EFFECT,
                new Minecraft189Mappings.MappedField[0],
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.POTION_EFFECT_GET_POTION_ID,
                        Minecraft189Mappings.POTION_EFFECT_GET_DURATION,
                        Minecraft189Mappings.POTION_EFFECT_GET_AMPLIFIER,
                        Minecraft189Mappings.POTION_EFFECT_GET_EFFECT_NAME
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
