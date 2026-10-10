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
                        Minecraft189Mappings.MINECRAFT_PLAYER_CONTROLLER,
                        Minecraft189Mappings.MINECRAFT_FONT_RENDERER,
                        Minecraft189Mappings.MINECRAFT_ENTITY_RENDERER,
                        Minecraft189Mappings.MINECRAFT_INGAME_GUI,
                        Minecraft189Mappings.MINECRAFT_GAME_SETTINGS,
                        Minecraft189Mappings.MINECRAFT_CURRENT_SERVER_DATA,
                        Minecraft189Mappings.MINECRAFT_RIGHT_CLICK_DELAY_TIMER,
                        Minecraft189Mappings.MINECRAFT_LEFT_CLICK_COUNTER,
                        Minecraft189Mappings.MINECRAFT_TIMER,
                        Minecraft189Mappings.MINECRAFT_OBJECT_MOUSE_OVER
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

    /**
     * Fail-closed crosshair hit-result shape. All optional combat ray-hit
     * consumers must use this source-proven contract, not nearby-player
     * snapshots or guessed offsets.
     */
    public static void verifyMovingObjectPosition(
            final byte[] classBytes) {
        verify(classBytes, Minecraft189Mappings.MOVING_OBJECT_POSITION,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.MOVING_OBJECT_TYPE_OF_HIT,
                        Minecraft189Mappings.MOVING_OBJECT_ENTITY_HIT
                },
                new Minecraft189Mappings.MappedMethod[0]);
    }

    public static void verifyMovingObjectType(
            final byte[] classBytes) {
        verify(classBytes, Minecraft189Mappings.MOVING_OBJECT_TYPE,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.MOVING_OBJECT_TYPE_ENTITY
                }, new Minecraft189Mappings.MappedMethod[0]);
    }

    public static void verifyPlayerControllerMp(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.PLAYER_CONTROLLER_MP,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.PLAYER_CONTROLLER_BLOCK_HIT_DELAY,
                        Minecraft189Mappings.PLAYER_CONTROLLER_CUR_BLOCK_DAMAGE,
                        Minecraft189Mappings.PLAYER_CONTROLLER_IS_HITTING_BLOCK
                },
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.PLAYER_CONTROLLER_STOP_USING_ITEM,
                        Minecraft189Mappings.PLAYER_CONTROLLER_GET_BLOCK_REACH,
                        Minecraft189Mappings.PLAYER_CONTROLLER_EXTENDED_REACH
                });
    }

    public static void verifyTimer(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.TIMER,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.TIMER_SPEED
                },
                new Minecraft189Mappings.MappedMethod[0]);
    }

    public static void verifyWorld(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.WORLD,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST
                },
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.WORLD_GET_WORLD_TIME,
                        Minecraft189Mappings.WORLD_IS_RAINING,
                        Minecraft189Mappings.WORLD_IS_THUNDERING
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

    /** Fail-closed exact 1.8.9 AxisAlignedBB field owner and geometry shape. */
    public static void verifyAxisAlignedBB(final byte[] classBytes) {
        verify(classBytes, Minecraft189Mappings.AXIS_ALIGNED_BB,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.AABB_MIN_X,
                        Minecraft189Mappings.AABB_MIN_Y,
                        Minecraft189Mappings.AABB_MIN_Z,
                        Minecraft189Mappings.AABB_MAX_X,
                        Minecraft189Mappings.AABB_MAX_Y,
                        Minecraft189Mappings.AABB_MAX_Z
                }, new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.AABB_EXPAND
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
                        Minecraft189Mappings.ENTITY_ROTATION_YAW,
                        Minecraft189Mappings.ENTITY_ROTATION_PITCH,
                        Minecraft189Mappings.ENTITY_DIMENSION,
                        Minecraft189Mappings.ENTITY_ON_GROUND,
                        Minecraft189Mappings.ENTITY_STEP_HEIGHT,
                        Minecraft189Mappings.ENTITY_FALL_DISTANCE,
                        Minecraft189Mappings.ENTITY_IS_IN_WEB,
                        Minecraft189Mappings.ENTITY_MOTION_X,
                        Minecraft189Mappings.ENTITY_MOTION_Y,
                        Minecraft189Mappings.ENTITY_MOTION_Z,
                        Minecraft189Mappings.ENTITY_NO_CLIP
                },
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.ENTITY_IS_SNEAKING,
                        Minecraft189Mappings.ENTITY_IS_SPRINTING,
                        Minecraft189Mappings.ENTITY_SET_SPRINTING,
                        Minecraft189Mappings.ENTITY_SET_SNEAKING,
                        Minecraft189Mappings.ENTITY_GET_UNIQUE_ID,
                        Minecraft189Mappings.ENTITY_GET_ENTITY_BOUNDING_BOX,
                        Minecraft189Mappings.ENTITY_RAY_TRACE,
                        Minecraft189Mappings.ENTITY_GET_COLLISION_BORDER_SIZE
                });
    }

    public static void verifyEntityLivingBase(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.ENTITY_LIVING_BASE,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.ENTITY_LIVING_BASE_HURT_TIME
                },
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.ENTITY_LIVING_BASE_GET_HEALTH,
                        Minecraft189Mappings.ENTITY_LIVING_BASE_GET_MAX_HEALTH,
                        Minecraft189Mappings.ENTITY_LIVING_BASE_IS_ON_SAME_TEAM,
                        Minecraft189Mappings.ENTITY_LIVING_BASE_GET_EQUIPMENT_IN_SLOT,
                        Minecraft189Mappings.ENTITY_LIVING_BASE_GET_ACTIVE_POTION_EFFECTS,
                        Minecraft189Mappings.ENTITY_LIVING_BASE_JUMP,
                        Minecraft189Mappings.ENTITY_LIVING_BASE_KNOCK_BACK,
                        Minecraft189Mappings.ENTITY_LIVING_BASE_CAN_ENTITY_BE_SEEN
                });
    }

    public static void verifyEntityPlayerSp(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.ENTITY_PLAYER_SP,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.ENTITY_PLAYER_SP_MOVEMENT_INPUT
                },
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.ENTITY_PLAYER_SP_ON_LIVING_UPDATE
                });
    }

    public static void verifyMovementInput(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.MOVEMENT_INPUT,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.MOVEMENT_INPUT_MOVE_STRAFE,
                        Minecraft189Mappings.MOVEMENT_INPUT_MOVE_FORWARD
                },
                new Minecraft189Mappings.MappedMethod[0]);
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
                        Minecraft189Mappings.ENTITY_PLAYER_INVENTORY,
                        Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_LEVEL,
                        Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_TOTAL,
                        Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_PROGRESS
                },
                new Minecraft189Mappings.MappedMethod[]{
                        Minecraft189Mappings.ENTITY_PLAYER_GET_FOOD_STATS,
                        Minecraft189Mappings.ENTITY_PLAYER_XP_BAR_CAP,
                        Minecraft189Mappings.ENTITY_PLAYER_IS_USING_ITEM
                });
    }

    public static void verifyInventoryPlayer(
            final byte[] classBytes) {
        verify(
                classBytes,
                Minecraft189Mappings.INVENTORY_PLAYER,
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.INVENTORY_PLAYER_CURRENT_ITEM,
                        Minecraft189Mappings.INVENTORY_PLAYER_MAIN_INVENTORY
                },
                new Minecraft189Mappings.MappedMethod[0]);
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
                        Minecraft189Mappings.ENTITY_RENDERER_UPDATE_CAMERA_AND_RENDER,
                        Minecraft189Mappings.ENTITY_RENDERER_GET_MOUSE_OVER
                });
        verifyNativeRaycastCalls(classBytes);
    }

    /**
     * Fail-closed native 1.8.9 getMouseOver boundary. Proves the method body
     * calls all three mapped upstream vanilla operations before any later
     * reach/raycast mutation is considered. Does not rewrite these calls.
     */
    static void verifyNativeRaycastCalls(final byte[] classBytes) {
        // Official Mojang client has TWO AABB expand calls in getMouseOver:
        // broad-phase candidate query and narrow-phase collision-border ray hit.
        final int[] calls = new int[5];
        final boolean[] found = new boolean[1];
        new ClassReader(Objects.requireNonNull(classBytes, "classBytes")).accept(
                new ClassVisitor(Opcodes.ASM9) {
                    @Override public MethodVisitor visitMethod(
                            final int access, final String name,
                            final String descriptor, final String signature,
                            final String[] exceptions) {
                        final Minecraft189Mappings.MappedMethod method =
                                Minecraft189Mappings.ENTITY_RENDERER_GET_MOUSE_OVER;
                        if (!name.equals(method.obfuscatedName())
                                || !descriptor.equals(method.descriptor())) return null;
                        if (found[0]) throw new IllegalStateException(
                                "duplicate mapped getMouseOver boundary");
                        found[0] = true;
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override public void visitMethodInsn(
                                    final int opcode, final String owner,
                                    final String methodName, final String methodDesc,
                                    final boolean isInterface) {
                                final Minecraft189Mappings.MappedMethod[] required = {
                                        Minecraft189Mappings.PLAYER_CONTROLLER_GET_BLOCK_REACH,
                                        Minecraft189Mappings.PLAYER_CONTROLLER_EXTENDED_REACH,
                                        Minecraft189Mappings.ENTITY_RAY_TRACE,
                                        Minecraft189Mappings.ENTITY_GET_COLLISION_BORDER_SIZE,
                                        Minecraft189Mappings.AABB_EXPAND
                                };
                                for (int index = 0; index < required.length; index++) {
                                    final Minecraft189Mappings.MappedMethod expected =
                                            required[index];
                                    if (owner.equals(expected.owner().obfuscatedInternalName())
                                            && methodName.equals(expected.obfuscatedName())
                                            && methodDesc.equals(expected.descriptor())
                                            && opcode == Opcodes.INVOKEVIRTUAL) {
                                        calls[index]++;
                                    }
                                }
                            }
                        };
                    }
                }, 0);
        if (!found[0] || calls[0] < 1 || calls[1] < 1 || calls[2] < 1
                || calls[3] != 1 || calls[4] != 2)
            throw new IllegalStateException(
                    "Minecraft 1.8.9 native getMouseOver boundary mismatch: "
                    + "blockReach=" + calls[0] + ", extendedReach=" + calls[1]
                    + ", entityRayTrace=" + calls[2]
                    + ", collisionBorder=" + calls[3]
                    + ", boxExpand=" + calls[4]);
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
