package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
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
    private static final String KEYBOARD_BINDING_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189LwjglKeyboardBinding";
    private static final String MOUSE_BINDING_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189LwjglMouseBinding";
    private static final String LWJGL_MOUSE_INTERNAL_NAME =
            "org/lwjgl/input/Mouse";
    private static final String LWJGL_GET_EVENT_DWHEEL =
            "getEventDWheel";
    private static final String LWJGL_GET_EVENT_DWHEEL_DESCRIPTOR =
            "()I";
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
    private static final String PLAYER_POSITION_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerPositionAccess";
    private static final String PLAYER_POSITION_ACCESS_DESCRIPTOR =
            "L" + PLAYER_POSITION_ACCESS_INTERNAL_NAME + ";";
    private static final String HITBOX_BOUNDS_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189EntityHitboxBoundsAccess";
    private static final String PLAYER_ROTATION_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerRotationAccess";
    private static final String PLAYER_ROTATION_ACCESS_DESCRIPTOR =
            "L" + PLAYER_ROTATION_ACCESS_INTERNAL_NAME + ";";
    private static final String PLAYER_ROTATION_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerRotationControl";
    private static final String PLAYER_DIMENSION_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerDimensionAccess";
    private static final String PLAYER_DIMENSION_ACCESS_DESCRIPTOR =
            "L" + PLAYER_DIMENSION_ACCESS_INTERNAL_NAME + ";";
    private static final String PLAYER_MOVEMENT_STATE_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerMovementStateAccess";
    private static final String PLAYER_MOVEMENT_STATE_ACCESS_DESCRIPTOR =
            "L" + PLAYER_MOVEMENT_STATE_ACCESS_INTERNAL_NAME + ";";
    private static final String PLAYER_SPRINT_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerSprintControl";
    private static final String PLAYER_SPRINT_CONTROL_DESCRIPTOR =
            "L" + PLAYER_SPRINT_CONTROL_INTERNAL_NAME + ";";
    private static final String PLAYER_SNEAK_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerSneakControl";
    private static final String PLAYER_SNEAK_CONTROL_DESCRIPTOR =
            "L" + PLAYER_SNEAK_CONTROL_INTERNAL_NAME + ";";
    private static final String PLAYER_JUMP_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerJumpControl";
    private static final String PLAYER_JUMP_CONTROL_DESCRIPTOR =
            "L" + PLAYER_JUMP_CONTROL_INTERNAL_NAME + ";";
    private static final String PLAYER_STEP_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerStepControl";
    private static final String PLAYER_STEP_CONTROL_DESCRIPTOR =
            "L" + PLAYER_STEP_CONTROL_INTERNAL_NAME + ";";
    private static final String PLAYER_FALL_DISTANCE_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerFallDistanceControl";
    private static final String PLAYER_FALL_DISTANCE_CONTROL_DESCRIPTOR =
            "L" + PLAYER_FALL_DISTANCE_CONTROL_INTERNAL_NAME + ";";
    private static final String PLAYER_WEB_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerWebControl";
    private static final String PLAYER_WEB_CONTROL_DESCRIPTOR =
            "L" + PLAYER_WEB_CONTROL_INTERNAL_NAME + ";";
    private static final String PLAYER_NO_CLIP_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerNoClipControl";
    private static final String PLAYER_NO_CLIP_CONTROL_DESCRIPTOR =
            "L" + PLAYER_NO_CLIP_CONTROL_INTERNAL_NAME + ";";
    private static final String PLAYER_MOTION_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerMotionControl";
    private static final String PLAYER_MOTION_CONTROL_DESCRIPTOR =
            "L" + PLAYER_MOTION_CONTROL_INTERNAL_NAME + ";";
    private static final String PLAYER_HEALTH_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerHealthAccess";
    private static final String PLAYER_HEALTH_ACCESS_DESCRIPTOR =
            "L" + PLAYER_HEALTH_ACCESS_INTERNAL_NAME + ";";
    private static final String PLAYER_HURT_TIME_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerHurtTimeAccess";
    private static final String PLAYER_HURT_TIME_ACCESS_DESCRIPTOR =
            "L" + PLAYER_HURT_TIME_ACCESS_INTERNAL_NAME + ";";
    private static final String PLAYER_ARMOR_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerArmorAccess";
    private static final String PLAYER_ARMOR_ACCESS_DESCRIPTOR =
            "L" + PLAYER_ARMOR_ACCESS_INTERNAL_NAME + ";";
    private static final String PLAYER_HUNGER_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerHungerAccess";
    private static final String PLAYER_HUNGER_ACCESS_DESCRIPTOR =
            "L" + PLAYER_HUNGER_ACCESS_INTERNAL_NAME + ";";
    private static final String POTION_EFFECT_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PotionEffectAccess";
    private static final String POTION_EFFECT_ACCESS_DESCRIPTOR =
            "L" + POTION_EFFECT_ACCESS_INTERNAL_NAME + ";";
    private static final String PLAYER_POTION_EFFECTS_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerPotionEffectsAccess";
    private static final String PLAYER_POTION_EFFECTS_ACCESS_DESCRIPTOR =
            "L" + PLAYER_POTION_EFFECTS_ACCESS_INTERNAL_NAME + ";";
    private static final String PLAYER_EXPERIENCE_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerExperienceAccess";
    private static final String PLAYER_EXPERIENCE_ACCESS_DESCRIPTOR =
            "L" + PLAYER_EXPERIENCE_ACCESS_INTERNAL_NAME + ";";
    private static final String PLAYER_PING_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerPingAccess";
    private static final String PLAYER_PING_ACCESS_DESCRIPTOR =
            "L" + PLAYER_PING_ACCESS_INTERNAL_NAME + ";";
    private static final String PLAYER_TAB_INFO_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerTabInfoAccess";
    private static final String INVENTORY_HOTBAR_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189InventoryHotbarAccess";
    private static final String INVENTORY_HOTBAR_ACCESS_DESCRIPTOR =
            "L" + INVENTORY_HOTBAR_ACCESS_INTERNAL_NAME + ";";
    private static final String INVENTORY_HOTBAR_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189InventoryHotbarControl";
    private static final String PLAYER_INVENTORY_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerInventoryAccess";
    private static final String PLAYER_INVENTORY_ACCESS_DESCRIPTOR =
            "L" + PLAYER_INVENTORY_ACCESS_INTERNAL_NAME + ";";
    private static final String WORLD_TIME_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189WorldTimeAccess";
    private static final String WORLD_TIME_ACCESS_DESCRIPTOR =
            "L" + WORLD_TIME_ACCESS_INTERNAL_NAME + ";";
    private static final String WORLD_WEATHER_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189WorldWeatherAccess";
    private static final String WORLD_WEATHER_ACCESS_DESCRIPTOR =
            "L" + WORLD_WEATHER_ACCESS_INTERNAL_NAME + ";";
    private static final String WORLD_ENTITY_POSITIONS_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189WorldEntityPositionsAccess";
    private static final String WORLD_ENTITY_POSITIONS_ACCESS_DESCRIPTOR =
            "L" + WORLD_ENTITY_POSITIONS_ACCESS_INTERNAL_NAME + ";";
    private static final String WORLD_ENTITY_KINDS_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189WorldEntityKindsAccess";
    private static final String WORLD_ENTITY_KINDS_ACCESS_DESCRIPTOR =
            "L" + WORLD_ENTITY_KINDS_ACCESS_INTERNAL_NAME + ";";
    private static final String WORLD_ENTITY_COMBAT_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189WorldEntityCombatAccess";
    private static final String WORLD_ENTITY_COMBAT_ACCESS_DESCRIPTOR =
            "L" + WORLD_ENTITY_COMBAT_ACCESS_INTERNAL_NAME + ";";
    private static final String WORLD_ENTITY_VISIBILITY_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189WorldEntityVisibilityAccess";
    private static final String WORLD_ENTITY_UUID_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189WorldEntityUuidAccess";
    private static final String SERVER_DATA_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189ServerDataAccess";
    private static final String SERVER_DATA_ACCESS_DESCRIPTOR =
            "L" + SERVER_DATA_ACCESS_INTERNAL_NAME + ";";
    private static final String ITEM_STACK_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189ItemStackAccess";
    private static final String ITEM_STACK_ACCESS_DESCRIPTOR =
            "L" + ITEM_STACK_ACCESS_INTERNAL_NAME + ";";
    private static final String INVENTORY_HOTBAR_ITEMS_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189InventoryHotbarItemsAccess";
    private static final String PLAYER_HELD_ITEM_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189PlayerHeldItemAccess";
    private static final String PLAYER_HELD_ITEM_ACCESS_DESCRIPTOR =
            "L" + PLAYER_HELD_ITEM_ACCESS_INTERNAL_NAME + ";";
    private static final String CLICK_MOUSE_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189ClickMouseControl";
    private static final String CLICK_MOUSE_CONTROL_DESCRIPTOR =
            "L" + CLICK_MOUSE_CONTROL_INTERNAL_NAME + ";";
    private static final String SWORD_BLOCK_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189SwordBlockControl";
    private static final String CROSSHAIR_HIT_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189CrosshairHitAccess";
    private static final String BLOCK_HIT_DELAY_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189BlockHitDelayControl";
    private static final String BLOCK_HIT_DELAY_CONTROL_DESCRIPTOR =
            "L" + BLOCK_HIT_DELAY_CONTROL_INTERNAL_NAME + ";";
    private static final String BLOCK_MINING_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189BlockMiningControl";
    private static final String VANILLA_REACH_ACCESS_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189VanillaReachAccess";
    private static final String BLOCK_MINING_CONTROL_DESCRIPTOR =
            "L" + BLOCK_MINING_CONTROL_INTERNAL_NAME + ";";
    private static final String TIMER_SPEED_CONTROL_INTERNAL_NAME =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189TimerSpeedControl";
    private static final String TIMER_SPEED_CONTROL_DESCRIPTOR =
            "L" + TIMER_SPEED_CONTROL_INTERNAL_NAME + ";";

    @Override
    public boolean handles(
            final String binaryClassName) {
        return TARGET_MAIN_CLASS.equals(
                binaryClassName)
                || Minecraft189Mappings.MINECRAFT
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.PLAYER_CONTROLLER_MP
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.KEY_BINDING
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
                .equals(binaryClassName)
                || Minecraft189Mappings.ENTITY
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.ENTITY_LIVING_BASE
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.AXIS_ALIGNED_BB
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.ENTITY_PLAYER_SP
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.ENTITY_PLAYER
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.INVENTORY_PLAYER
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.ABSTRACT_CLIENT_PLAYER
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.NETWORK_PLAYER_INFO
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.WORLD
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.SERVER_DATA
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.ITEM_STACK
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.FOOD_STATS
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.MOVEMENT_INPUT
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.TIMER
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.POTION_EFFECT
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.MOVING_OBJECT_POSITION
                .obfuscatedBinaryName()
                .equals(binaryClassName)
                || Minecraft189Mappings.MOVING_OBJECT_TYPE
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
        if (Minecraft189Mappings.PLAYER_CONTROLLER_MP
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyPlayerControllerMp(input);
            return transformPlayerControllerMp(input);
        }
        if (Minecraft189Mappings.KEY_BINDING
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyKeyBinding(input);
            return transformKeyBinding(input);
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
        if (Minecraft189Mappings.AXIS_ALIGNED_BB
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier.verifyAxisAlignedBB(input);
            return input;
        }
        if (Minecraft189Mappings.ENTITY
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyEntity(input);
            return transformEntity(input);
        }
        if (Minecraft189Mappings.ENTITY_LIVING_BASE
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyEntityLivingBase(input);
            return transformEntityLivingBase(input);
        }
        if (Minecraft189Mappings.ENTITY_PLAYER_SP
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyEntityPlayerSp(input);
            return transformEntityPlayerSp(input);
        }
        if (Minecraft189Mappings.ENTITY_PLAYER
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyEntityPlayer(input);
            return transformEntityPlayer(input);
        }
        if (Minecraft189Mappings.INVENTORY_PLAYER
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyInventoryPlayer(input);
            return transformInventoryPlayer(input);
        }
        if (Minecraft189Mappings.ABSTRACT_CLIENT_PLAYER
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyAbstractClientPlayer(input);
            return transformAbstractClientPlayer(input);
        }
        if (Minecraft189Mappings.NETWORK_PLAYER_INFO
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyNetworkPlayerInfo(input);
            return input;
        }
        if (Minecraft189Mappings.WORLD
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyWorld(input);
            return transformWorld(input);
        }
        if (Minecraft189Mappings.SERVER_DATA
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyServerData(input);
            return transformServerData(input);
        }
        if (Minecraft189Mappings.ITEM_STACK
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyItemStack(input);
            return transformItemStack(input);
        }
        if (Minecraft189Mappings.FOOD_STATS
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyFoodStats(input);
            return input;
        }
        if (Minecraft189Mappings.MOVEMENT_INPUT
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyMovementInput(input);
            return input;
        }
        if (Minecraft189Mappings.TIMER
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyTimer(input);
            return transformTimer(input);
        }
        if (Minecraft189Mappings.MOVING_OBJECT_TYPE
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier.verifyMovingObjectType(input);
            return input;
        }
        if (Minecraft189Mappings.MOVING_OBJECT_POSITION
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier.verifyMovingObjectPosition(input);
            // Shape-only authority: no speculative raytrace mutation.
            return input;
        }
        if (Minecraft189Mappings.POTION_EFFECT
                .obfuscatedBinaryName()
                .equals(binaryClassName)) {
            Minecraft189ClassShapeVerifier
                    .verifyPotionEffect(input);
            return transformPotionEffect(input);
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
        final boolean[] injectedPosition =
                new boolean[]{false};
        final boolean[] injectedRotation =
                new boolean[]{false};
        final boolean[] injectedDimension =
                new boolean[]{false};
        final boolean[] injectedMovementState =
                new boolean[]{false};
        final boolean[] injectedSprintControl =
                new boolean[]{false};
        final boolean[] injectedSneakControl =
                new boolean[]{false};
        final boolean[] injectedJumpControl =
                new boolean[]{false};
        final boolean[] injectedStepControl =
                new boolean[]{false};
        final boolean[] injectedFallDistanceControl =
                new boolean[]{false};
        final boolean[] injectedWebControl =
                new boolean[]{false};
        final boolean[] injectedNoClipControl =
                new boolean[]{false};
        final boolean[] injectedMotionControl =
                new boolean[]{false};
        final boolean[] injectedHealth =
                new boolean[]{false};
        final boolean[] injectedHurtTime =
                new boolean[]{false};
        final boolean[] injectedArmor =
                new boolean[]{false};
        final boolean[] injectedHunger =
                new boolean[]{false};
        final boolean[] injectedPotionEffects =
                new boolean[]{false};
        final boolean[] injectedExperience =
                new boolean[]{false};
        final boolean[] injectedPing =
                new boolean[]{false};
        final boolean[] injectedHotbarSlot =
                new boolean[]{false};
        final boolean[] injectedWorldTime =
                new boolean[]{false};
        final boolean[] injectedWeather =
                new boolean[]{false};
        final boolean[] injectedWorldEntityPositions =
                new boolean[]{false};
        final boolean[] injectedWorldEntityKinds =
                new boolean[]{false};
        final boolean[] injectedWorldEntityCombat =
                new boolean[]{false};
        final boolean[] injectedServerAddress =
                new boolean[]{false};
        final boolean[] injectedHeldItem =
                new boolean[]{false};
        final boolean[] injectedRightClickDelay =
                new boolean[]{false};
        final boolean[] injectedFastBreakControl =
                new boolean[]{false};
        final boolean[] injectedSpeedMineControl =
                new boolean[]{false};
        final boolean[] injectedTimerSpeedControl =
                new boolean[]{false};
        final boolean[] injectedLeftClickCounter =
                new boolean[]{false};
        final boolean[] injectedAutoClick =
                new boolean[]{false};
        final boolean[] foundDispatchKeypresses =
                new boolean[]{false};
        final boolean[] injectedKeyboard =
                new boolean[]{false};
        final int[] wheelReads =
                new int[]{0};

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
                                        withInterface(
                                                withInterface(interfaces,
                                                        CLICK_MOUSE_CONTROL_INTERNAL_NAME),
                                                SWORD_BLOCK_CONTROL_INTERNAL_NAME),
                                        CROSSHAIR_HIT_ACCESS_INTERNAL_NAME));
                    }

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

                                @Override
                                public void visitInsn(
                                        final int opcode) {
                                    if (opcode == Opcodes.RETURN) {
                                        final Minecraft189Mappings.MappedField player =
                                                Minecraft189Mappings
                                                        .MINECRAFT_PLAYER;
                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_POSITION_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerPosition",
                                                "("
                                                        + PLAYER_POSITION_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedPosition[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_ROTATION_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerRotation",
                                                "("
                                                        + PLAYER_ROTATION_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedRotation[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_DIMENSION_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerDimension",
                                                "("
                                                        + PLAYER_DIMENSION_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedDimension[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_MOVEMENT_STATE_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerMovementState",
                                                "("
                                                        + PLAYER_MOVEMENT_STATE_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedMovementState[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_SPRINT_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerSprintControl",
                                                "("
                                                        + PLAYER_SPRINT_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedSprintControl[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_SNEAK_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerSneakControl",
                                                "("
                                                        + PLAYER_SNEAK_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedSneakControl[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_JUMP_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerJumpControl",
                                                "("
                                                        + PLAYER_JUMP_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedJumpControl[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_STEP_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerStepControl",
                                                "("
                                                        + PLAYER_STEP_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedStepControl[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_FALL_DISTANCE_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerFallDistanceControl",
                                                "("
                                                        + PLAYER_FALL_DISTANCE_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedFallDistanceControl[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_WEB_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerWebControl",
                                                "("
                                                        + PLAYER_WEB_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedWebControl[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_NO_CLIP_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerNoClipControl",
                                                "("
                                                        + PLAYER_NO_CLIP_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedNoClipControl[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_HURT_TIME_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerHurtTime",
                                                "("
                                                        + PLAYER_HURT_TIME_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedHurtTime[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_MOTION_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerMotionControl",
                                                "("
                                                        + PLAYER_MOTION_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedMotionControl[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_HEALTH_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerHealth",
                                                "("
                                                        + PLAYER_HEALTH_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedHealth[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_ARMOR_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerArmor",
                                                "("
                                                        + PLAYER_ARMOR_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedArmor[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_HUNGER_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerHunger",
                                                "("
                                                        + PLAYER_HUNGER_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedHunger[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_POTION_EFFECTS_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerPotionEffects",
                                                "("
                                                        + PLAYER_POTION_EFFECTS_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedPotionEffects[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_EXPERIENCE_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerExperience",
                                                "("
                                                        + PLAYER_EXPERIENCE_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedExperience[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_PING_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerPing",
                                                "("
                                                        + PLAYER_PING_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedPing[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_INVENTORY_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerHotbarSlot",
                                                "("
                                                        + PLAYER_INVENTORY_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedHotbarSlot[0] = true;

                                        final Minecraft189Mappings.MappedField world =
                                                Minecraft189Mappings
                                                        .MINECRAFT_WORLD;
                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                world.obfuscatedName(),
                                                world.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                WORLD_TIME_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "worldTime",
                                                "("
                                                        + WORLD_TIME_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedWorldTime[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                world.obfuscatedName(),
                                                world.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                WORLD_WEATHER_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "worldWeather",
                                                "("
                                                        + WORLD_WEATHER_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedWeather[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                world.obfuscatedName(),
                                                world.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                WORLD_ENTITY_POSITIONS_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "worldEntityPositions",
                                                "("
                                                        + WORLD_ENTITY_POSITIONS_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedWorldEntityPositions[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                world.obfuscatedName(),
                                                world.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                WORLD_ENTITY_KINDS_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "worldEntityKinds",
                                                "("
                                                        + WORLD_ENTITY_KINDS_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedWorldEntityKinds[0] = true;

                                        super.visitVarInsn(Opcodes.ALOAD, 0);
                                        super.visitFieldInsn(Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                world.obfuscatedName(),
                                                world.descriptor());
                                        super.visitTypeInsn(Opcodes.CHECKCAST,
                                                WORLD_ENTITY_COMBAT_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "worldEntityCombat",
                                                "(" + WORLD_ENTITY_COMBAT_ACCESS_DESCRIPTOR + ")V",
                                                false);
                                        injectedWorldEntityCombat[0] = true;

                                        final Minecraft189Mappings.MappedField serverData =
                                                Minecraft189Mappings
                                                        .MINECRAFT_CURRENT_SERVER_DATA;
                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                serverData.obfuscatedName(),
                                                serverData.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                SERVER_DATA_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "serverAddress",
                                                "("
                                                        + SERVER_DATA_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedServerAddress[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                player.obfuscatedName(),
                                                player.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                PLAYER_HELD_ITEM_ACCESS_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerHeldItem",
                                                "("
                                                        + PLAYER_HELD_ITEM_ACCESS_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedHeldItem[0] = true;

                                        final Minecraft189Mappings.MappedField rightClickDelay =
                                                Minecraft189Mappings
                                                        .MINECRAFT_RIGHT_CLICK_DELAY_TIMER;
                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitInsn(
                                                Opcodes.DUP);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                rightClickDelay.obfuscatedName(),
                                                rightClickDelay.descriptor());
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "rightClickDelay",
                                                "(I)I",
                                                false);
                                        super.visitFieldInsn(
                                                Opcodes.PUTFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                rightClickDelay.obfuscatedName(),
                                                rightClickDelay.descriptor());
                                        injectedRightClickDelay[0] = true;

                                        final Minecraft189Mappings.MappedField playerController =
                                                Minecraft189Mappings
                                                        .MINECRAFT_PLAYER_CONTROLLER;
                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                playerController.obfuscatedName(),
                                                playerController.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                BLOCK_HIT_DELAY_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerControllerBreakControl",
                                                "("
                                                        + BLOCK_HIT_DELAY_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedFastBreakControl[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                playerController.obfuscatedName(),
                                                playerController.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                BLOCK_MINING_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "playerControllerMiningControl",
                                                "("
                                                        + BLOCK_MINING_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedSpeedMineControl[0] = true;

                                        final Minecraft189Mappings.MappedField timer =
                                                Minecraft189Mappings
                                                        .MINECRAFT_TIMER;
                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                timer.obfuscatedName(),
                                                timer.descriptor());
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                TIMER_SPEED_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "timerSpeedControl",
                                                "("
                                                        + TIMER_SPEED_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedTimerSpeedControl[0] = true;

                                        final Minecraft189Mappings.MappedField leftClickCounter =
                                                Minecraft189Mappings
                                                        .MINECRAFT_LEFT_CLICK_COUNTER;
                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitInsn(
                                                Opcodes.DUP);
                                        super.visitFieldInsn(
                                                Opcodes.GETFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                leftClickCounter.obfuscatedName(),
                                                leftClickCounter.descriptor());
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "leftClickCounter",
                                                "(I)I",
                                                false);
                                        super.visitFieldInsn(
                                                Opcodes.PUTFIELD,
                                                Minecraft189Mappings.MINECRAFT
                                                        .obfuscatedInternalName(),
                                                leftClickCounter.obfuscatedName(),
                                                leftClickCounter.descriptor());
                                        injectedLeftClickCounter[0] = true;

                                        super.visitVarInsn(
                                                Opcodes.ALOAD,
                                                0);
                                        super.visitTypeInsn(
                                                Opcodes.CHECKCAST,
                                                CLICK_MOUSE_CONTROL_INTERNAL_NAME);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "autoClick",
                                                "("
                                                        + CLICK_MOUSE_CONTROL_DESCRIPTOR
                                                        + ")V",
                                                false);
                                        injectedAutoClick[0] = true;
                                    }
                                    super.visitInsn(opcode);
                                }

                                @Override
                                public void visitMethodInsn(
                                        final int opcode,
                                        final String owner,
                                        final String methodName,
                                        final String methodDescriptor,
                                        final boolean isInterface) {
                                    super.visitMethodInsn(
                                            opcode,
                                            owner,
                                            methodName,
                                            methodDescriptor,
                                            isInterface);

                                    if (opcode == Opcodes.INVOKESTATIC
                                            && LWJGL_MOUSE_INTERNAL_NAME.equals(
                                            owner)
                                            && LWJGL_GET_EVENT_DWHEEL.equals(
                                            methodName)
                                            && LWJGL_GET_EVENT_DWHEEL_DESCRIPTOR
                                            .equals(methodDescriptor)) {
                                        super.visitInsn(
                                                Opcodes.DUP);
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                MOUSE_BINDING_INTERNAL_NAME,
                                                "forwardWheelDelta",
                                                "(I)V",
                                                false);
                                        wheelReads[0]++;
                                    }
                                }
                            };
                        }

                        final Minecraft189Mappings.MappedMethod dispatchKeypresses =
                                Minecraft189Mappings
                                        .MINECRAFT_DISPATCH_KEYPRESSES;
                        if (dispatchKeypresses.obfuscatedName().equals(name)
                                && dispatchKeypresses.descriptor().equals(
                                descriptor)) {
                            if (foundDispatchKeypresses[0]) {
                                throw new IllegalStateException(
                                        "duplicate mapped Minecraft dispatchKeypresses method");
                            }
                            foundDispatchKeypresses[0] = true;

                            return new MethodVisitor(
                                    Opcodes.ASM9,
                                    delegate) {
                                @Override
                                public void visitCode() {
                                    super.visitCode();
                                    super.visitMethodInsn(
                                            Opcodes.INVOKESTATIC,
                                            KEYBOARD_BINDING_INTERNAL_NAME,
                                            "forwardCurrentEvent",
                                            "()V",
                                            false);
                                    injectedKeyboard[0] = true;
                                }
                            };
                        }

                        return delegate;
                    }

                    @Override
                    public void visitEnd() {
                        addVoidMethodDelegate(
                                cv,
                                "customMcClickMouse",
                                Minecraft189Mappings
                                        .MINECRAFT_CLICK_MOUSE);
                        addVoidMethodDelegate(cv,
                                "customMcRightClickMouse",
                                Minecraft189Mappings.MINECRAFT_RIGHT_CLICK_MOUSE);
                        addMappedIsUsingItemGetter(cv);
                        addMappedStopUsingItem(cv);
                        addCrosshairHitAccessor(cv);
                        addCrosshairPlayerIndexAccessor(cv);
                        addCrosshairHitboxBoundsAccessor(cv);
                        super.visitEnd();
                    }
                },
                0);

        if (!foundStartGame[0]
                || !injectedHost[0]) {
            throw new IllegalStateException(
                    "mapped Minecraft startGame method was not patchable");
        }
        if (!foundRunTick[0]
                || !injectedTick[0]
                || !injectedPosition[0]
                || !injectedRotation[0]
                || !injectedDimension[0]
                || !injectedMovementState[0]
                || !injectedSprintControl[0]
                || !injectedSneakControl[0]
                || !injectedJumpControl[0]
                || !injectedStepControl[0]
                || !injectedFallDistanceControl[0]
                || !injectedWebControl[0]
                || !injectedNoClipControl[0]
                || !injectedMotionControl[0]
                || !injectedHealth[0]
                || !injectedHurtTime[0]
                || !injectedArmor[0]
                || !injectedHunger[0]
                || !injectedPotionEffects[0]
                || !injectedExperience[0]
                || !injectedPing[0]
                || !injectedHotbarSlot[0]
                || !injectedWorldTime[0]
                || !injectedWeather[0]
                || !injectedWorldEntityPositions[0]
                || !injectedWorldEntityKinds[0]
                || !injectedWorldEntityCombat[0]
                || !injectedServerAddress[0]
                || !injectedHeldItem[0]
                || !injectedRightClickDelay[0]
                || !injectedFastBreakControl[0]
                || !injectedSpeedMineControl[0]
                || !injectedTimerSpeedControl[0]
                || !injectedLeftClickCounter[0]
                || !injectedAutoClick[0]) {
            throw new IllegalStateException(
                    "mapped Minecraft runTick method was not patchable");
        }
        if (wheelReads[0] != 1) {
            throw new IllegalStateException(
                    "mapped Minecraft runTick must contain exactly one "
                            + "Mouse.getEventDWheel()I call, found "
                            + wheelReads[0]);
        }
        if (!foundDispatchKeypresses[0]
                || !injectedKeyboard[0]) {
            throw new IllegalStateException(
                    "mapped Minecraft dispatchKeypresses method was not patchable");
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

    private static byte[] transformKeyBinding(
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
                        final Minecraft189Mappings.MappedMethod state =
                                Minecraft189Mappings
                                        .KEY_BINDING_SET_KEY_BIND_STATE;
                        if (!state.obfuscatedName().equals(name)
                                || !state.descriptor().equals(
                                descriptor)
                                || (access & Opcodes.ACC_STATIC) == 0) {
                            return delegate;
                        }
                        if (found[0]) {
                            throw new IllegalStateException(
                                    "duplicate mapped KeyBinding setKeyBindState method");
                        }
                        found[0] = true;

                        return new MethodVisitor(
                                Opcodes.ASM9,
                                delegate) {
                            @Override
                            public void visitCode() {
                                super.visitCode();
                                super.visitVarInsn(
                                        Opcodes.ILOAD,
                                        0);
                                super.visitVarInsn(
                                        Opcodes.ILOAD,
                                        1);
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        MOUSE_BINDING_INTERNAL_NAME,
                                        "forwardKeyBindingState",
                                        "(IZ)V",
                                        false);
                                injected[0] = true;
                            }
                        };
                    }
                },
                0);

        if (!found[0] || !injected[0]) {
            throw new IllegalStateException(
                    "mapped KeyBinding setKeyBindState method was not patchable");
        }
        return writer.toByteArray();
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
                        addBooleanFieldGetter(
                                cv,
                                "viewBobbing",
                                Minecraft189Mappings
                                        .GAME_SETTINGS_VIEW_BOBBING);
                        addBooleanFieldSetter(
                                cv,
                                "viewBobbing",
                                Minecraft189Mappings
                                        .GAME_SETTINGS_VIEW_BOBBING);
                        addFloatFieldGetter(
                                cv,
                                "fovSetting",
                                Minecraft189Mappings
                                        .GAME_SETTINGS_FOV);
                        addFloatFieldSetter(
                                cv,
                                "fovSetting",
                                Minecraft189Mappings
                                        .GAME_SETTINGS_FOV);
                        addFloatFieldGetter(
                                cv,
                                "gammaSetting",
                                Minecraft189Mappings
                                        .GAME_SETTINGS_GAMMA);
                        addFloatFieldSetter(
                                cv,
                                "gammaSetting",
                                Minecraft189Mappings
                                        .GAME_SETTINGS_GAMMA);
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
        final boolean[] foundRaycast = new boolean[1];
        final int[] changedRaycastCalls = new int[2];
        final int[] changedExtendedConstants = new int[1];

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
                        final Minecraft189Mappings.MappedMethod mouseOver =
                                Minecraft189Mappings.ENTITY_RENDERER_GET_MOUSE_OVER;
                        if (mouseOver.obfuscatedName().equals(name)
                                && mouseOver.descriptor().equals(descriptor)) {
                            if (foundRaycast[0]) throw new IllegalStateException(
                                    "duplicate mapped raycast reach boundary");
                            foundRaycast[0] = true;
                            return new MethodVisitor(Opcodes.ASM9, delegate) {
                                @Override public void visitMethodInsn(
                                        final int opcode, final String owner,
                                        final String methodName, final String methodDesc,
                                        final boolean isInterface) {
                                    super.visitMethodInsn(opcode, owner,
                                            methodName, methodDesc, isInterface);
                                    if (opcode != Opcodes.INVOKEVIRTUAL || isInterface)
                                        return;
                                    final Minecraft189Mappings.MappedMethod block =
                                            Minecraft189Mappings
                                                    .PLAYER_CONTROLLER_GET_BLOCK_REACH;
                                    final Minecraft189Mappings.MappedMethod extended =
                                            Minecraft189Mappings
                                                    .PLAYER_CONTROLLER_EXTENDED_REACH;
                                    if (owner.equals(block.owner().obfuscatedInternalName())
                                            && methodName.equals(block.obfuscatedName())
                                            && methodDesc.equals(block.descriptor())) {
                                        changedRaycastCalls[0]++;
                                        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "raycastBlockDistance", "(F)F", false);
                                    }
                                    if (owner.equals(extended.owner().obfuscatedInternalName())
                                            && methodName.equals(extended.obfuscatedName())
                                            && methodDesc.equals(extended.descriptor())) {
                                        changedRaycastCalls[1]++;
                                        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "raycastExtendedBranch", "(Z)Z", false);
                                    }
                                }

                                @Override public void visitLdcInsn(final Object constant) {
                                    if (constant instanceof Double
                                            && ((Double) constant).doubleValue() == 6.0D) {
                                        changedExtendedConstants[0]++;
                                        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "raycastExtendedDistance", "()D", false);
                                    } else {
                                        super.visitLdcInsn(constant);
                                    }
                                }
                            };
                        }

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

        if (!foundRaycast[0] || changedRaycastCalls[0] != 1
                || changedRaycastCalls[1] != 1
                || changedExtendedConstants[0] != 2) {
            throw new IllegalStateException(
                    "mapped EntityRenderer reach raycast not patchable: "
                    + "blockCalls=" + changedRaycastCalls[0]
                    + " extendedCalls=" + changedRaycastCalls[1]
                    + " sixConstants=" + changedExtendedConstants[0]);
        }
        if (!found[0]
                || !injectedStart[0]
                || !injectedEnd[0]) {
            throw new IllegalStateException(
                    "mapped EntityRenderer updateCameraAndRender method was not patchable");
        }
        return writer.toByteArray();
    }

    private static byte[] transformTimer(
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
                                        TIMER_SPEED_CONTROL_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addFloatFieldGetter(
                                cv,
                                "customMcTimerSpeed",
                                Minecraft189Mappings.TIMER_SPEED);
                        addFloatFieldSetter(
                                cv,
                                "customMcSetTimerSpeed",
                                Minecraft189Mappings.TIMER_SPEED);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    private static byte[] transformPlayerControllerMp(
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
                                        withInterface(
                                        withInterface(
                                                interfaces,
                                                BLOCK_HIT_DELAY_CONTROL_INTERNAL_NAME),
                                        BLOCK_MINING_CONTROL_INTERNAL_NAME),
                                VANILLA_REACH_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addFloatMethodDelegate(cv,
                                "customMcVanillaBlockReachDistance",
                                Minecraft189Mappings.PLAYER_CONTROLLER_GET_BLOCK_REACH);
                        addBooleanMethodDelegate(cv,
                                "customMcVanillaExtendedReach",
                                Minecraft189Mappings.PLAYER_CONTROLLER_EXTENDED_REACH);
                        addIntFieldSetter(
                                cv,
                                "customMcSetBlockHitDelay",
                                Minecraft189Mappings
                                        .PLAYER_CONTROLLER_BLOCK_HIT_DELAY);
                        addBooleanFieldGetter(
                                cv,
                                "customMcIsHittingBlock",
                                Minecraft189Mappings
                                        .PLAYER_CONTROLLER_IS_HITTING_BLOCK);
                        addFloatFieldGetter(
                                cv,
                                "customMcBlockDamageProgress",
                                Minecraft189Mappings
                                        .PLAYER_CONTROLLER_CUR_BLOCK_DAMAGE);
                        addFloatFieldSetter(
                                cv,
                                "customMcSetBlockDamageProgress",
                                Minecraft189Mappings
                                        .PLAYER_CONTROLLER_CUR_BLOCK_DAMAGE);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    private static byte[] transformEntity(
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
                                        withInterface(
                                        withInterface(
                                                withInterface(
                                                        withInterface(
                                                                withInterface(
                                                                        withInterface(
                                                                                withInterface(
                                                                                        withInterface(
                                                                                                withInterface(
                                                                                                        withInterface(
                                                                                                                withInterface(
                                                                                                                        withInterface(
                                                                                                                                interfaces,
                                                                                                                                PLAYER_POSITION_ACCESS_INTERNAL_NAME),
                                                                                                                        PLAYER_ROTATION_ACCESS_INTERNAL_NAME),
                                                                                                                PLAYER_DIMENSION_ACCESS_INTERNAL_NAME),
                                                                                                        PLAYER_MOVEMENT_STATE_ACCESS_INTERNAL_NAME),
                                                                                                PLAYER_SPRINT_CONTROL_INTERNAL_NAME),
                                                                                        PLAYER_SNEAK_CONTROL_INTERNAL_NAME),
                                                                                PLAYER_STEP_CONTROL_INTERNAL_NAME),
                                                                        PLAYER_FALL_DISTANCE_CONTROL_INTERNAL_NAME),
                                                                PLAYER_WEB_CONTROL_INTERNAL_NAME),
                                                        PLAYER_NO_CLIP_CONTROL_INTERNAL_NAME),
                                                PLAYER_MOTION_CONTROL_INTERNAL_NAME),
                                        PLAYER_ROTATION_CONTROL_INTERNAL_NAME),
                                HITBOX_BOUNDS_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addDoubleFieldGetter(
                                cv,
                                "customMcPositionX",
                                Minecraft189Mappings.ENTITY_POS_X);
                        addDoubleFieldGetter(
                                cv,
                                "customMcPositionY",
                                Minecraft189Mappings.ENTITY_POS_Y);
                        addDoubleFieldGetter(
                                cv,
                                "customMcPositionZ",
                                Minecraft189Mappings.ENTITY_POS_Z);
                        addFloatFieldGetter(
                                cv,
                                "customMcRotationYaw",
                                Minecraft189Mappings.ENTITY_ROTATION_YAW);
                        addFloatFieldGetter(
                                cv,
                                "customMcRotationPitch",
                                Minecraft189Mappings.ENTITY_ROTATION_PITCH);
                        addFloatFieldSetter(
                                cv,
                                "customMcSetRotationYaw",
                                Minecraft189Mappings.ENTITY_ROTATION_YAW);
                        addFloatFieldSetter(
                                cv,
                                "customMcSetRotationPitch",
                                Minecraft189Mappings.ENTITY_ROTATION_PITCH);
                        addIntFieldGetter(
                                cv,
                                "customMcDimension",
                                Minecraft189Mappings.ENTITY_DIMENSION);
                        addBooleanFieldGetter(
                                cv,
                                "customMcOnGround",
                                Minecraft189Mappings.ENTITY_ON_GROUND);
                        addBooleanMethodDelegate(
                                cv,
                                "customMcSneaking",
                                Minecraft189Mappings.ENTITY_IS_SNEAKING);
                        addBooleanMethodDelegate(
                                cv,
                                "customMcSprinting",
                                Minecraft189Mappings.ENTITY_IS_SPRINTING);
                        addBooleanMethodSetterDelegate(
                                cv,
                                "customMcSetSprinting",
                                Minecraft189Mappings.ENTITY_SET_SPRINTING);
                        addBooleanMethodSetterDelegate(
                                cv,
                                "customMcSetSneaking",
                                Minecraft189Mappings.ENTITY_SET_SNEAKING);
                        addFloatFieldGetter(
                                cv,
                                "customMcStepHeight",
                                Minecraft189Mappings.ENTITY_STEP_HEIGHT);
                        addFloatFieldSetter(
                                cv,
                                "customMcSetStepHeight",
                                Minecraft189Mappings.ENTITY_STEP_HEIGHT);
                        addFloatFieldGetter(
                                cv,
                                "customMcFallDistance",
                                Minecraft189Mappings.ENTITY_FALL_DISTANCE);
                        addFloatFieldSetter(
                                cv,
                                "customMcSetFallDistance",
                                Minecraft189Mappings.ENTITY_FALL_DISTANCE);
                        addBooleanFieldGetter(
                                cv,
                                "customMcInWeb",
                                Minecraft189Mappings.ENTITY_IS_IN_WEB);
                        addBooleanFieldSetter(
                                cv,
                                "customMcSetInWeb",
                                Minecraft189Mappings.ENTITY_IS_IN_WEB);
                        addBooleanFieldGetter(
                                cv,
                                "customMcNoClip",
                                Minecraft189Mappings.ENTITY_NO_CLIP);
                        addBooleanFieldSetter(
                                cv,
                                "customMcSetNoClip",
                                Minecraft189Mappings.ENTITY_NO_CLIP);
                        addDoubleFieldGetter(
                                cv,
                                "customMcMotionX",
                                Minecraft189Mappings.ENTITY_MOTION_X);
                        addDoubleFieldSetter(
                                cv,
                                "customMcSetMotionX",
                                Minecraft189Mappings.ENTITY_MOTION_X);
                        addDoubleFieldGetter(
                                cv,
                                "customMcMotionY",
                                Minecraft189Mappings.ENTITY_MOTION_Y);
                        addDoubleFieldSetter(
                                cv,
                                "customMcSetMotionY",
                                Minecraft189Mappings.ENTITY_MOTION_Y);
                        addDoubleFieldGetter(
                                cv,
                                "customMcMotionZ",
                                Minecraft189Mappings.ENTITY_MOTION_Z);
                        addDoubleFieldSetter(
                                cv,
                                "customMcSetMotionZ",
                                Minecraft189Mappings.ENTITY_MOTION_Z);
                        addNativeEntityHitboxBoundsGetter(cv);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }


    /**
     * Actual vanilla Entity.getEntityBoundingBox() six-component double AABB.
     * Read-only; no hitbox mutation and no raycast extension.
     */
    private static void addNativeEntityHitboxBoundsGetter(final ClassVisitor visitor) {
        final MethodVisitor m = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcHitboxBounds", "()[D", null, null);
        final Label absent = new Label();
        final String owner = Minecraft189Mappings.AXIS_ALIGNED_BB.obfuscatedInternalName();
        m.visitCode();
        m.visitVarInsn(Opcodes.ALOAD, 0);
        m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Minecraft189Mappings.ENTITY_GET_ENTITY_BOUNDING_BOX.owner()
                        .obfuscatedInternalName(),
                Minecraft189Mappings.ENTITY_GET_ENTITY_BOUNDING_BOX.obfuscatedName(),
                Minecraft189Mappings.ENTITY_GET_ENTITY_BOUNDING_BOX.descriptor(),
                false);
        m.visitVarInsn(Opcodes.ASTORE, 1);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitJumpInsn(Opcodes.IFNULL, absent);
        m.visitIntInsn(Opcodes.BIPUSH, 6);
        m.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_DOUBLE);
        m.visitVarInsn(Opcodes.ASTORE, 2);
        final Minecraft189Mappings.MappedField[] fields =
                new Minecraft189Mappings.MappedField[]{
                        Minecraft189Mappings.AABB_MIN_X,
                        Minecraft189Mappings.AABB_MIN_Y,
                        Minecraft189Mappings.AABB_MIN_Z,
                        Minecraft189Mappings.AABB_MAX_X,
                        Minecraft189Mappings.AABB_MAX_Y,
                        Minecraft189Mappings.AABB_MAX_Z
                };
        for (int i = 0; i < fields.length; i++) {
            m.visitVarInsn(Opcodes.ALOAD, 2);
            m.visitIntInsn(Opcodes.BIPUSH, i);
            m.visitVarInsn(Opcodes.ALOAD, 1);
            m.visitFieldInsn(Opcodes.GETFIELD, owner,
                    fields[i].obfuscatedName(), fields[i].descriptor());
            m.visitInsn(Opcodes.DASTORE);
        }
        m.visitVarInsn(Opcodes.ALOAD, 2);
        m.visitInsn(Opcodes.ARETURN);
        m.visitLabel(absent);
        m.visitFrame(Opcodes.F_FULL, 2, new Object[]{
                Minecraft189Mappings.ENTITY.obfuscatedInternalName(), owner
        }, 0, new Object[0]);
        m.visitInsn(Opcodes.ACONST_NULL);
        m.visitInsn(Opcodes.ARETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
    }

    private static byte[] transformEntityLivingBase(
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
                                        withInterface(
                                                withInterface(
                                                        withInterface(
                                                                withInterface(
                                                                        withInterface(
                                                                                interfaces,
                                                                                PLAYER_HEALTH_ACCESS_INTERNAL_NAME),
                                                                        PLAYER_HURT_TIME_ACCESS_INTERNAL_NAME),
                                                                PLAYER_ARMOR_ACCESS_INTERNAL_NAME),
                                                        PLAYER_POTION_EFFECTS_ACCESS_INTERNAL_NAME),
                                                PLAYER_HELD_ITEM_ACCESS_INTERNAL_NAME),
                                        PLAYER_JUMP_CONTROL_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addFloatMethodDelegate(
                                cv,
                                "customMcHealth",
                                Minecraft189Mappings
                                        .ENTITY_LIVING_BASE_GET_HEALTH);
                        addFloatMethodDelegate(
                                cv,
                                "customMcMaxHealth",
                                Minecraft189Mappings
                                        .ENTITY_LIVING_BASE_GET_MAX_HEALTH);
                        addIntFieldGetter(
                                cv,
                                "customMcHurtTime",
                                Minecraft189Mappings
                                        .ENTITY_LIVING_BASE_HURT_TIME);
                        addEquipmentPresentGetter(
                                cv,
                                "customMcArmorBoots",
                                1);
                        addEquipmentPresentGetter(
                                cv,
                                "customMcArmorLeggings",
                                2);
                        addEquipmentPresentGetter(
                                cv,
                                "customMcArmorChestplate",
                                3);
                        addEquipmentPresentGetter(
                                cv,
                                "customMcArmorHelmet",
                                4);
                        addEquipmentItemGetter(
                                cv,
                                "customMcArmorBootsItem",
                                1);
                        addEquipmentItemGetter(
                                cv,
                                "customMcArmorLeggingsItem",
                                2);
                        addEquipmentItemGetter(
                                cv,
                                "customMcArmorChestplateItem",
                                3);
                        addEquipmentItemGetter(
                                cv,
                                "customMcArmorHelmetItem",
                                4);
                        addPotionEffectsGetter(
                                cv,
                                "customMcPotionEffects");
                        addHeldItemGetter(
                                cv,
                                "customMcHeldItem");
                        addVoidMethodDelegate(
                                cv,
                                "customMcJump",
                                Minecraft189Mappings
                                        .ENTITY_LIVING_BASE_JUMP);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    private static byte[] transformEntityPlayerSp(
            final byte[] input) {
        final ClassReader reader =
                new ClassReader(input);
        final ClassWriter writer =
                new ClassWriter(
                        reader,
                        ClassWriter.COMPUTE_MAXS);
        final int[] strafeStores =
                new int[]{0};
        final int[] forwardStores =
                new int[]{0};
        final boolean[] foundOnLivingUpdate =
                new boolean[]{false};
        final boolean[] declaredKnockBack =
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
                        final Minecraft189Mappings.MappedMethod knockBack =
                                Minecraft189Mappings
                                        .ENTITY_LIVING_BASE_KNOCK_BACK;
                        if (knockBack.obfuscatedName().equals(name)
                                && knockBack.descriptor().equals(
                                descriptor)) {
                            declaredKnockBack[0] = true;
                        }

                        final Minecraft189Mappings.MappedMethod onLivingUpdate =
                                Minecraft189Mappings
                                        .ENTITY_PLAYER_SP_ON_LIVING_UPDATE;
                        if (!onLivingUpdate.obfuscatedName().equals(name)
                                || !onLivingUpdate.descriptor().equals(
                                descriptor)) {
                            return delegate;
                        }
                        if (foundOnLivingUpdate[0]) {
                            throw new IllegalStateException(
                                    "duplicate mapped EntityPlayerSP onLivingUpdate method");
                        }
                        foundOnLivingUpdate[0] = true;

                        return new MethodVisitor(
                                Opcodes.ASM9,
                                delegate) {
                            @Override
                            public void visitFieldInsn(
                                    final int opcode,
                                    final String owner,
                                    final String fieldName,
                                    final String fieldDescriptor) {
                                if (opcode == Opcodes.PUTFIELD
                                        && Minecraft189Mappings.MOVEMENT_INPUT
                                        .obfuscatedInternalName()
                                        .equals(owner)
                                        && "F".equals(fieldDescriptor)) {
                                    final Minecraft189Mappings.MappedField strafe =
                                            Minecraft189Mappings
                                                    .MOVEMENT_INPUT_MOVE_STRAFE;
                                    final Minecraft189Mappings.MappedField forward =
                                            Minecraft189Mappings
                                                    .MOVEMENT_INPUT_MOVE_FORWARD;
                                    if (strafe.obfuscatedName().equals(
                                            fieldName)) {
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "adjustNoSlowMovement",
                                                "(F)F",
                                                false);
                                        strafeStores[0]++;
                                    } else if (forward.obfuscatedName().equals(
                                            fieldName)) {
                                        super.visitMethodInsn(
                                                Opcodes.INVOKESTATIC,
                                                RUNTIME_BRIDGE_INTERNAL_NAME,
                                                "adjustNoSlowMovement",
                                                "(F)F",
                                                false);
                                        forwardStores[0]++;
                                    }
                                }
                                super.visitFieldInsn(
                                        opcode,
                                        owner,
                                        fieldName,
                                        fieldDescriptor);
                            }
                        };
                    }

                    @Override
                    public void visitEnd() {
                        if (declaredKnockBack[0]) {
                            throw new IllegalStateException(
                                    "mapped EntityPlayerSP unexpectedly declares knockBack");
                        }
                        addVelocityKnockBackOverride(
                                cv);
                        super.visitEnd();
                    }
                },
                0);

        if (!foundOnLivingUpdate[0]
                || strafeStores[0] != 1
                || forwardStores[0] != 1) {
            throw new IllegalStateException(
                    "mapped EntityPlayerSP onLivingUpdate slowdown stores "
                            + "must be exactly one strafe and one forward, found "
                            + strafeStores[0]
                            + "/"
                            + forwardStores[0]);
        }
        return writer.toByteArray();
    }

    private static void addVelocityKnockBackOverride(
            final ClassVisitor visitor) {
        final Minecraft189Mappings.MappedMethod knockBack =
                Minecraft189Mappings.ENTITY_LIVING_BASE_KNOCK_BACK;
        final Minecraft189Mappings.MappedField motionX =
                Minecraft189Mappings.ENTITY_MOTION_X;
        final Minecraft189Mappings.MappedField motionY =
                Minecraft189Mappings.ENTITY_MOTION_Y;
        final Minecraft189Mappings.MappedField motionZ =
                Minecraft189Mappings.ENTITY_MOTION_Z;

        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        knockBack.obfuscatedName(),
                        knockBack.descriptor(),
                        null,
                        null);
        method.visitCode();

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                motionX.owner().obfuscatedInternalName(),
                motionX.obfuscatedName(),
                motionX.descriptor());
        method.visitVarInsn(Opcodes.DSTORE, 7);

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                motionY.owner().obfuscatedInternalName(),
                motionY.obfuscatedName(),
                motionY.descriptor());
        method.visitVarInsn(Opcodes.DSTORE, 9);

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                motionZ.owner().obfuscatedInternalName(),
                motionZ.obfuscatedName(),
                motionZ.descriptor());
        method.visitVarInsn(Opcodes.DSTORE, 11);

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitVarInsn(Opcodes.FLOAD, 2);
        method.visitVarInsn(Opcodes.DLOAD, 3);
        method.visitVarInsn(Opcodes.DLOAD, 5);
        method.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                knockBack.owner().obfuscatedInternalName(),
                knockBack.obfuscatedName(),
                knockBack.descriptor(),
                false);

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.DLOAD, 7);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                motionX.owner().obfuscatedInternalName(),
                motionX.obfuscatedName(),
                motionX.descriptor());
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                RUNTIME_BRIDGE_INTERNAL_NAME,
                "adjustVelocityHorizontal",
                "(DD)D",
                false);
        method.visitFieldInsn(
                Opcodes.PUTFIELD,
                motionX.owner().obfuscatedInternalName(),
                motionX.obfuscatedName(),
                motionX.descriptor());

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.DLOAD, 9);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                motionY.owner().obfuscatedInternalName(),
                motionY.obfuscatedName(),
                motionY.descriptor());
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                RUNTIME_BRIDGE_INTERNAL_NAME,
                "adjustVelocityVertical",
                "(DD)D",
                false);
        method.visitFieldInsn(
                Opcodes.PUTFIELD,
                motionY.owner().obfuscatedInternalName(),
                motionY.obfuscatedName(),
                motionY.descriptor());

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.DLOAD, 11);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                motionZ.owner().obfuscatedInternalName(),
                motionZ.obfuscatedName(),
                motionZ.descriptor());
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                RUNTIME_BRIDGE_INTERNAL_NAME,
                "adjustVelocityHorizontal",
                "(DD)D",
                false);
        method.visitFieldInsn(
                Opcodes.PUTFIELD,
                motionZ.owner().obfuscatedInternalName(),
                motionZ.obfuscatedName(),
                motionZ.descriptor());

        method.visitInsn(
                Opcodes.RETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static byte[] transformItemStack(
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
                    // The exact runtime ItemStack owns one Item-typed field.
                    // Reject ambiguous bytecode rather than assuming a field name.
                    private String sourceItemField;
                    private int itemFieldCount;

                    @Override
                    public FieldVisitor visitField(
                            final int access, final String name,
                            final String descriptor, final String signature,
                            final Object value) {
                        if ((access & Opcodes.ACC_STATIC) == 0
                                && ("L" + Minecraft189Mappings.ITEM
                                        .obfuscatedInternalName() + ";").equals(descriptor)) {
                            sourceItemField = name;
                            itemFieldCount++;
                        }
                        return super.visitField(access, name, descriptor,
                                signature, value);
                    }

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
                                        ITEM_STACK_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        if (itemFieldCount != 1) {
                            throw new IllegalStateException(
                                    "expected exactly one source-mapped ItemStack Item field"
                                            + " but found " + itemFieldCount);
                        }
                        addItemStackTypeGetter(cv, "customMcIsSword",
                                sourceItemField,
                                Minecraft189Mappings.ITEM_SWORD);
                        addSwordBaseDamageGetter(cv, sourceItemField);
                        addItemStackTypeGetter(cv, "customMcIsFishingRod",
                                sourceItemField,
                                Minecraft189Mappings.ITEM_FISHING_ROD);
                        addItemStackTypeGetter(cv, "customMcIsPotion",
                                sourceItemField,
                                Minecraft189Mappings.ITEM_POTION);
                        addStringMethodDelegate(
                                cv,
                                "customMcDisplayName",
                                Minecraft189Mappings
                                        .ITEM_STACK_GET_DISPLAY_NAME);
                        addIntFieldGetter(
                                cv,
                                "customMcStackSize",
                                Minecraft189Mappings.ITEM_STACK_SIZE);
                        addIntMethodDelegate(
                                cv,
                                "customMcItemDamage",
                                Minecraft189Mappings
                                        .ITEM_STACK_GET_ITEM_DAMAGE);
                        addIntMethodDelegate(
                                cv,
                                "customMcMaxDamage",
                                Minecraft189Mappings
                                        .ITEM_STACK_GET_MAX_DAMAGE);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    /**
     * Uses exact source ItemStack Item field and ItemSword.g()F, no names.
     * The stack-frame at the non-sword branch includes the Item reference.
     */
    private static void addSwordBaseDamageGetter(
            final ClassVisitor visitor, final String sourceItemField) {
        final String sword = Minecraft189Mappings.ITEM_SWORD.obfuscatedInternalName();
        final Minecraft189Mappings.MappedMethod damage =
                Minecraft189Mappings.ITEM_SWORD_GET_DAMAGE_VS_ENTITY;
        final MethodVisitor mv = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcSwordBaseDamage", "()F", null, null);
        final Label notSword = new Label();
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD,
                Minecraft189Mappings.ITEM_STACK.obfuscatedInternalName(),
                sourceItemField,
                "L" + Minecraft189Mappings.ITEM.obfuscatedInternalName() + ";");
        mv.visitInsn(Opcodes.DUP);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, sword);
        mv.visitJumpInsn(Opcodes.IFEQ, notSword);
        mv.visitTypeInsn(Opcodes.CHECKCAST, sword);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sword,
                damage.obfuscatedName(), damage.descriptor(), false);
        mv.visitInsn(Opcodes.FRETURN);
        mv.visitLabel(notSword);
        mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
                new Object[]{Minecraft189Mappings.ITEM.obfuscatedInternalName()});
        mv.visitInsn(Opcodes.POP);
        mv.visitLdcInsn(Float.valueOf(Float.NaN));
        mv.visitInsn(Opcodes.FRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void addItemStackTypeGetter(
            final ClassVisitor visitor,
            final String methodName,
            final String mappedFieldName,
            final Minecraft189Mappings.MappedClass itemClass) {
        final MethodVisitor method = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, methodName, "()Z", null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD,
                Minecraft189Mappings.ITEM_STACK.obfuscatedInternalName(),
                mappedFieldName,
                "L" + Minecraft189Mappings.ITEM.obfuscatedInternalName() + ";");
        method.visitTypeInsn(Opcodes.INSTANCEOF,
                itemClass.obfuscatedInternalName());
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static byte[] transformPotionEffect(
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
                                        POTION_EFFECT_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addIntMethodDelegate(
                                cv,
                                "customMcPotionId",
                                Minecraft189Mappings.POTION_EFFECT_GET_POTION_ID);
                        addIntMethodDelegate(
                                cv,
                                "customMcDurationTicks",
                                Minecraft189Mappings.POTION_EFFECT_GET_DURATION);
                        addIntMethodDelegate(
                                cv,
                                "customMcAmplifier",
                                Minecraft189Mappings.POTION_EFFECT_GET_AMPLIFIER);
                        addStringMethodDelegate(
                                cv,
                                "customMcEffectName",
                                Minecraft189Mappings.POTION_EFFECT_GET_EFFECT_NAME);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    private static byte[] transformWorld(
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
                                        withInterface(
                                        withInterface(
                                        withInterface(
                                                withInterface(
                                                        withInterface(
                                                                withInterface(
                                                                        interfaces,
                                                                        WORLD_TIME_ACCESS_INTERNAL_NAME),
                                                        WORLD_WEATHER_ACCESS_INTERNAL_NAME),
                                                WORLD_ENTITY_POSITIONS_ACCESS_INTERNAL_NAME),
                                        WORLD_ENTITY_KINDS_ACCESS_INTERNAL_NAME),
                                WORLD_ENTITY_COMBAT_ACCESS_INTERNAL_NAME),
                                WORLD_ENTITY_UUID_ACCESS_INTERNAL_NAME),
                                WORLD_ENTITY_VISIBILITY_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addLongMethodDelegate(
                                cv,
                                "customMcWorldTime",
                                Minecraft189Mappings
                                        .WORLD_GET_WORLD_TIME);
                        addBooleanMethodDelegate(
                                cv,
                                "customMcRaining",
                                Minecraft189Mappings
                                        .WORLD_IS_RAINING);
                        addBooleanMethodDelegate(
                                cv,
                                "customMcThundering",
                                Minecraft189Mappings
                                        .WORLD_IS_THUNDERING);
                        addLoadedEntityPositionsSnapshot(
                                cv);
                        addLoadedEntityKindsSnapshot(
                                cv);
                        addLoadedEntityCombatSnapshot(cv);
                        addLoadedEntityUuidSnapshot(cv);
                        addLoadedEntityVisibilitySnapshot(cv);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    private static byte[] transformServerData(
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
                                        SERVER_DATA_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addStringFieldGetter(
                                cv,
                                "customMcServerAddress",
                                Minecraft189Mappings.SERVER_DATA_SERVER_IP);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    private static byte[] transformAbstractClientPlayer(
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
                                        withInterface(
                                                interfaces,
                                                PLAYER_PING_ACCESS_INTERNAL_NAME),
                                        PLAYER_TAB_INFO_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addPingGetter(
                                cv,
                                "customMcPingMilliseconds");
                        addHasNetworkPlayerInfoGetter(cv);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    private static byte[] transformEntityPlayer(
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
                                        withInterface(
                                                withInterface(
                                                        interfaces,
                                                        PLAYER_HUNGER_ACCESS_INTERNAL_NAME),
                                                PLAYER_EXPERIENCE_ACCESS_INTERNAL_NAME),
                                        PLAYER_INVENTORY_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addFoodLevelGetter(
                                cv,
                                "customMcFoodLevel");
                        addSaturationLevelGetter(
                                cv,
                                "customMcSaturationLevel");
                        addIntFieldGetter(
                                cv,
                                "customMcExperienceLevel",
                                Minecraft189Mappings
                                        .ENTITY_PLAYER_EXPERIENCE_LEVEL);
                        addIntFieldGetter(
                                cv,
                                "customMcExperienceTotal",
                                Minecraft189Mappings
                                        .ENTITY_PLAYER_EXPERIENCE_TOTAL);
                        addFloatFieldGetter(
                                cv,
                                "customMcExperienceProgress",
                                Minecraft189Mappings
                                        .ENTITY_PLAYER_EXPERIENCE_PROGRESS);
                        addIntMethodDelegate(
                                cv,
                                "customMcExperienceBarCap",
                                Minecraft189Mappings
                                        .ENTITY_PLAYER_XP_BAR_CAP);
                        addPlayerInventoryGetter(
                                cv,
                                "customMcInventory");
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    private static byte[] transformInventoryPlayer(
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
                                        withInterface(interfaces,
                                                INVENTORY_HOTBAR_CONTROL_INTERNAL_NAME),
                                        INVENTORY_HOTBAR_ITEMS_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addIntFieldGetter(
                                cv,
                                "customMcSelectedHotbarSlot",
                                Minecraft189Mappings
                                        .INVENTORY_PLAYER_CURRENT_ITEM);
                        addIntFieldSetter(cv,
                                "customMcSetSelectedHotbarSlot",
                                Minecraft189Mappings.INVENTORY_PLAYER_CURRENT_ITEM);
                        addMappedHotbarItemsGetter(cv);
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
    }

    /** Typed view of exact InventoryPlayer.mainInventory (never mutated). */
    private static void addMappedHotbarItemsGetter(final ClassVisitor visitor) {
        final Minecraft189Mappings.MappedField main =
                Minecraft189Mappings.INVENTORY_PLAYER_MAIN_INVENTORY;
        final MethodVisitor mv = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcHotbarItems",
                "()[L" + ITEM_STACK_ACCESS_INTERNAL_NAME + ";",
                null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD,
                main.owner().obfuscatedInternalName(),
                main.obfuscatedName(), main.descriptor());
        mv.visitTypeInsn(Opcodes.CHECKCAST,
                "[L" + ITEM_STACK_ACCESS_INTERNAL_NAME + ";");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void addPlayerInventoryGetter(
            final ClassVisitor visitor,
            final String methodName) {
        final Minecraft189Mappings.MappedField inventory =
                Minecraft189Mappings.ENTITY_PLAYER_INVENTORY;
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()" + INVENTORY_HOTBAR_ACCESS_DESCRIPTOR,
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                inventory.owner().obfuscatedInternalName(),
                inventory.obfuscatedName(),
                inventory.descriptor());
        method.visitTypeInsn(
                Opcodes.CHECKCAST,
                INVENTORY_HOTBAR_ACCESS_INTERNAL_NAME);
        method.visitInsn(
                Opcodes.ARETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    /** Call the existing shape-verified AbstractClientPlayer#getPlayerInfo. */
    private static void addHasNetworkPlayerInfoGetter(
            final ClassVisitor visitor) {
        final Minecraft189Mappings.MappedMethod playerInfo =
                Minecraft189Mappings.ABSTRACT_CLIENT_PLAYER_GET_PLAYER_INFO;
        final MethodVisitor method = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcHasNetworkPlayerInfo",
                "()Z", null, null);
        final Label missing = new Label();
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                playerInfo.owner().obfuscatedInternalName(),
                playerInfo.obfuscatedName(),
                playerInfo.descriptor(), false);
        method.visitJumpInsn(Opcodes.IFNULL, missing);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitLabel(missing);
        method.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void addPingGetter(
            final ClassVisitor visitor,
            final String methodName) {
        final Minecraft189Mappings.MappedMethod playerInfo =
                Minecraft189Mappings
                        .ABSTRACT_CLIENT_PLAYER_GET_PLAYER_INFO;
        final Minecraft189Mappings.MappedMethod responseTime =
                Minecraft189Mappings
                        .NETWORK_PLAYER_INFO_GET_RESPONSE_TIME;
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()I",
                        null,
                        null);
        final Label present =
                new Label();
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                playerInfo.owner().obfuscatedInternalName(),
                playerInfo.obfuscatedName(),
                playerInfo.descriptor(),
                false);
        method.visitInsn(
                Opcodes.DUP);
        method.visitJumpInsn(
                Opcodes.IFNONNULL,
                present);
        method.visitInsn(
                Opcodes.POP);
        method.visitInsn(
                Opcodes.ICONST_M1);
        method.visitInsn(
                Opcodes.IRETURN);
        method.visitLabel(
                present);
        method.visitFrame(
                Opcodes.F_SAME1,
                0,
                null,
                1,
                new Object[]{
                        Minecraft189Mappings.NETWORK_PLAYER_INFO
                                .obfuscatedInternalName()
                });
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                responseTime.owner().obfuscatedInternalName(),
                responseTime.obfuscatedName(),
                responseTime.descriptor(),
                false);
        method.visitInsn(
                Opcodes.IRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addFoodLevelGetter(
            final ClassVisitor visitor,
            final String methodName) {
        final Minecraft189Mappings.MappedMethod foodStats =
                Minecraft189Mappings.ENTITY_PLAYER_GET_FOOD_STATS;
        final Minecraft189Mappings.MappedMethod foodLevel =
                Minecraft189Mappings.FOOD_STATS_GET_FOOD_LEVEL;
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
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                foodStats.owner().obfuscatedInternalName(),
                foodStats.obfuscatedName(),
                foodStats.descriptor(),
                false);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                foodLevel.owner().obfuscatedInternalName(),
                foodLevel.obfuscatedName(),
                foodLevel.descriptor(),
                false);
        method.visitInsn(
                Opcodes.IRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addSaturationLevelGetter(
            final ClassVisitor visitor,
            final String methodName) {
        final Minecraft189Mappings.MappedMethod foodStats =
                Minecraft189Mappings.ENTITY_PLAYER_GET_FOOD_STATS;
        final Minecraft189Mappings.MappedMethod saturation =
                Minecraft189Mappings.FOOD_STATS_GET_SATURATION_LEVEL;
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()F",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                foodStats.owner().obfuscatedInternalName(),
                foodStats.obfuscatedName(),
                foodStats.descriptor(),
                false);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                saturation.owner().obfuscatedInternalName(),
                saturation.obfuscatedName(),
                saturation.descriptor(),
                false);
        method.visitInsn(
                Opcodes.FRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addHeldItemGetter(
            final ClassVisitor visitor,
            final String methodName) {
        addEquipmentItemGetter(
                visitor,
                methodName,
                0);
    }

    private static void addEquipmentItemGetter(
            final ClassVisitor visitor,
            final String methodName,
            final int slot) {
        final Minecraft189Mappings.MappedMethod equipment =
                Minecraft189Mappings
                        .ENTITY_LIVING_BASE_GET_EQUIPMENT_IN_SLOT;
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()" + ITEM_STACK_ACCESS_DESCRIPTOR,
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitIntInsn(
                Opcodes.BIPUSH,
                slot);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                equipment.owner().obfuscatedInternalName(),
                equipment.obfuscatedName(),
                equipment.descriptor(),
                false);
        method.visitTypeInsn(
                Opcodes.CHECKCAST,
                ITEM_STACK_ACCESS_INTERNAL_NAME);
        method.visitInsn(
                Opcodes.ARETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addPotionEffectsGetter(
            final ClassVisitor visitor,
            final String methodName) {
        final Minecraft189Mappings.MappedMethod activeEffects =
                Minecraft189Mappings
                        .ENTITY_LIVING_BASE_GET_ACTIVE_POTION_EFFECTS;
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()[L" + POTION_EFFECT_ACCESS_INTERNAL_NAME + ";",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                activeEffects.owner().obfuscatedInternalName(),
                activeEffects.obfuscatedName(),
                activeEffects.descriptor(),
                false);
        method.visitInsn(
                Opcodes.ICONST_0);
        method.visitTypeInsn(
                Opcodes.ANEWARRAY,
                POTION_EFFECT_ACCESS_INTERNAL_NAME);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "java/util/Collection",
                "toArray",
                "([Ljava/lang/Object;)[Ljava/lang/Object;",
                true);
        method.visitTypeInsn(
                Opcodes.CHECKCAST,
                "[L" + POTION_EFFECT_ACCESS_INTERNAL_NAME + ";");
        method.visitInsn(
                Opcodes.ARETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addBooleanMethodSetterDelegate(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedMethod target) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "(Z)V",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitVarInsn(
                Opcodes.ILOAD,
                1);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                target.owner().obfuscatedInternalName(),
                target.obfuscatedName(),
                target.descriptor(),
                false);
        method.visitInsn(
                Opcodes.RETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addLoadedEntityPositionsSnapshot(
            final ClassVisitor visitor) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "customMcLoadedEntityPositions",
                        "()[D",
                        null,
                        null);
        final Label nonNull =
                new Label();
        final Label loopCheck =
                new Label();
        final Label loopEnd =
                new Label();

        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                Minecraft189Mappings.WORLD
                        .obfuscatedInternalName(),
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST
                        .obfuscatedName(),
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST
                        .descriptor());
        method.visitVarInsn(
                Opcodes.ASTORE,
                1);
        method.visitVarInsn(
                Opcodes.ALOAD,
                1);
        method.visitJumpInsn(
                Opcodes.IFNONNULL,
                nonNull);
        method.visitInsn(
                Opcodes.ICONST_0);
        method.visitIntInsn(
                Opcodes.NEWARRAY,
                Opcodes.T_DOUBLE);
        method.visitInsn(
                Opcodes.ARETURN);

        method.visitLabel(nonNull);
        method.visitFrame(
                Opcodes.F_FULL,
                2,
                new Object[]{
                        Minecraft189Mappings.WORLD
                                .obfuscatedInternalName(),
                        "java/util/List"
                },
                0,
                new Object[0]);
        method.visitVarInsn(
                Opcodes.ALOAD,
                1);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "java/util/List",
                "size",
                "()I",
                true);
        method.visitVarInsn(
                Opcodes.ISTORE,
                2);
        method.visitVarInsn(
                Opcodes.ILOAD,
                2);
        method.visitInsn(
                Opcodes.ICONST_3);
        method.visitInsn(
                Opcodes.IMUL);
        method.visitIntInsn(
                Opcodes.NEWARRAY,
                Opcodes.T_DOUBLE);
        method.visitVarInsn(
                Opcodes.ASTORE,
                3);
        method.visitInsn(
                Opcodes.ICONST_0);
        method.visitVarInsn(
                Opcodes.ISTORE,
                4);

        method.visitLabel(loopCheck);
        method.visitFrame(
                Opcodes.F_FULL,
                5,
                new Object[]{
                        Minecraft189Mappings.WORLD
                                .obfuscatedInternalName(),
                        "java/util/List",
                        Opcodes.INTEGER,
                        "[D",
                        Opcodes.INTEGER
                },
                0,
                new Object[0]);
        method.visitVarInsn(
                Opcodes.ILOAD,
                4);
        method.visitVarInsn(
                Opcodes.ILOAD,
                2);
        method.visitJumpInsn(
                Opcodes.IF_ICMPGE,
                loopEnd);

        method.visitVarInsn(
                Opcodes.ALOAD,
                1);
        method.visitVarInsn(
                Opcodes.ILOAD,
                4);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "java/util/List",
                "get",
                "(I)Ljava/lang/Object;",
                true);
        method.visitTypeInsn(
                Opcodes.CHECKCAST,
                PLAYER_POSITION_ACCESS_INTERNAL_NAME);
        method.visitVarInsn(
                Opcodes.ASTORE,
                5);

        method.visitVarInsn(
                Opcodes.ALOAD,
                3);
        method.visitVarInsn(
                Opcodes.ILOAD,
                4);
        method.visitInsn(
                Opcodes.ICONST_3);
        method.visitInsn(
                Opcodes.IMUL);
        method.visitVarInsn(
                Opcodes.ALOAD,
                5);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                PLAYER_POSITION_ACCESS_INTERNAL_NAME,
                "customMcPositionX",
                "()D",
                true);
        method.visitInsn(
                Opcodes.DASTORE);

        method.visitVarInsn(
                Opcodes.ALOAD,
                3);
        method.visitVarInsn(
                Opcodes.ILOAD,
                4);
        method.visitInsn(
                Opcodes.ICONST_3);
        method.visitInsn(
                Opcodes.IMUL);
        method.visitInsn(
                Opcodes.ICONST_1);
        method.visitInsn(
                Opcodes.IADD);
        method.visitVarInsn(
                Opcodes.ALOAD,
                5);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                PLAYER_POSITION_ACCESS_INTERNAL_NAME,
                "customMcPositionY",
                "()D",
                true);
        method.visitInsn(
                Opcodes.DASTORE);

        method.visitVarInsn(
                Opcodes.ALOAD,
                3);
        method.visitVarInsn(
                Opcodes.ILOAD,
                4);
        method.visitInsn(
                Opcodes.ICONST_3);
        method.visitInsn(
                Opcodes.IMUL);
        method.visitInsn(
                Opcodes.ICONST_2);
        method.visitInsn(
                Opcodes.IADD);
        method.visitVarInsn(
                Opcodes.ALOAD,
                5);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                PLAYER_POSITION_ACCESS_INTERNAL_NAME,
                "customMcPositionZ",
                "()D",
                true);
        method.visitInsn(
                Opcodes.DASTORE);

        method.visitIincInsn(
                4,
                1);
        method.visitJumpInsn(
                Opcodes.GOTO,
                loopCheck);

        method.visitLabel(loopEnd);
        method.visitFrame(
                Opcodes.F_FULL,
                5,
                new Object[]{
                        Minecraft189Mappings.WORLD
                                .obfuscatedInternalName(),
                        "java/util/List",
                        Opcodes.INTEGER,
                        "[D",
                        Opcodes.INTEGER
                },
                0,
                new Object[0]);
        method.visitVarInsn(
                Opcodes.ALOAD,
                3);
        method.visitInsn(
                Opcodes.ARETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }


    /**
     * Exact 1.8.9 pk.aK getUniqueID on each loaded Entity.
     * Returns null rather than false identity data on any unsupported entry.
     */
    private static void addLoadedEntityUuidSnapshot(final ClassVisitor visitor) {
        final MethodVisitor m = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcLoadedEntityUuids",
                "()[Ljava/util/UUID;", null, null);
        final Label haveList = new Label();
        final Label loop = new Label();
        final Label entityOk = new Label();
        final Label uuidOk = new Label();
        final Label done = new Label();
        final String world = Minecraft189Mappings.WORLD.obfuscatedInternalName();
        final String entity = Minecraft189Mappings.ENTITY.obfuscatedInternalName();
        m.visitCode();
        m.visitVarInsn(Opcodes.ALOAD, 0);
        m.visitFieldInsn(Opcodes.GETFIELD, world,
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST.obfuscatedName(),
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST.descriptor());
        m.visitVarInsn(Opcodes.ASTORE, 1);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitJumpInsn(Opcodes.IFNONNULL, haveList);
        m.visitInsn(Opcodes.ACONST_NULL);
        m.visitInsn(Opcodes.ARETURN);

        m.visitLabel(haveList);
        m.visitFrame(Opcodes.F_FULL, 2,
                new Object[]{world, "java/util/List"}, 0, new Object[0]);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List",
                "size", "()I", true);
        m.visitVarInsn(Opcodes.ISTORE, 2);
        m.visitVarInsn(Opcodes.ILOAD, 2);
        m.visitTypeInsn(Opcodes.ANEWARRAY, "java/util/UUID");
        m.visitVarInsn(Opcodes.ASTORE, 3);
        m.visitInsn(Opcodes.ICONST_0);
        m.visitVarInsn(Opcodes.ISTORE, 4);

        m.visitLabel(loop);
        final Object[] locals = new Object[]{world, "java/util/List",
                Opcodes.INTEGER, "[Ljava/util/UUID;", Opcodes.INTEGER};
        m.visitFrame(Opcodes.F_FULL, 5, locals, 0, new Object[0]);
        m.visitVarInsn(Opcodes.ILOAD, 4);
        m.visitVarInsn(Opcodes.ILOAD, 2);
        m.visitJumpInsn(Opcodes.IF_ICMPGE, done);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitVarInsn(Opcodes.ILOAD, 4);
        m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List",
                "get", "(I)Ljava/lang/Object;", true);
        m.visitInsn(Opcodes.DUP);
        m.visitTypeInsn(Opcodes.INSTANCEOF, entity);
        m.visitJumpInsn(Opcodes.IFNE, entityOk);
        m.visitInsn(Opcodes.POP);
        m.visitInsn(Opcodes.ACONST_NULL);
        m.visitInsn(Opcodes.ARETURN);

        m.visitLabel(entityOk);
        m.visitFrame(Opcodes.F_FULL, 5, locals,
                1, new Object[]{"java/lang/Object"});
        m.visitTypeInsn(Opcodes.CHECKCAST, entity);
        m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, entity,
                Minecraft189Mappings.ENTITY_GET_UNIQUE_ID.obfuscatedName(),
                Minecraft189Mappings.ENTITY_GET_UNIQUE_ID.descriptor(), false);
        m.visitInsn(Opcodes.DUP);
        m.visitJumpInsn(Opcodes.IFNONNULL, uuidOk);
        m.visitInsn(Opcodes.POP);
        m.visitInsn(Opcodes.ACONST_NULL);
        m.visitInsn(Opcodes.ARETURN);

        m.visitLabel(uuidOk);
        m.visitFrame(Opcodes.F_FULL, 5, locals,
                1, new Object[]{"java/util/UUID"});
        m.visitVarInsn(Opcodes.ALOAD, 3);
        m.visitInsn(Opcodes.SWAP);
        m.visitVarInsn(Opcodes.ILOAD, 4);
        m.visitInsn(Opcodes.SWAP);
        m.visitInsn(Opcodes.AASTORE);
        m.visitIincInsn(4, 1);
        m.visitJumpInsn(Opcodes.GOTO, loop);

        m.visitLabel(done);
        m.visitFrame(Opcodes.F_FULL, 5, locals, 0, new Object[0]);
        m.visitVarInsn(Opcodes.ALOAD, 3);
        m.visitInsn(Opcodes.ARETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
    }


    /**
     * Native line-of-sight evidence using local EntityLivingBase.canEntityBeSeen
     * (1.8.9 pr.t(Lpk;)Z) and active Minecraft World identity.
     * Returns null without an active world/local player.
     * -1 = nonplayer, 0 = occluded, 1 = visible.
     */
    private static void addLoadedEntityVisibilitySnapshot(final ClassVisitor visitor) {
        final MethodVisitor m = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcLoadedEntityVisibility",
                "()[I", null, null);
        final Label haveList = new Label();
        final Label haveMinecraft = new Label();
        final Label sameWorld = new Label();
        final Label havePlayer = new Label();
        final Label loop = new Label();
        final Label nonPlayer = new Label();
        final Label store = new Label();
        final Label done = new Label();
        final Label invalid = new Label();
        final String world = Minecraft189Mappings.WORLD.obfuscatedInternalName();
        final String minecraft = Minecraft189Mappings.MINECRAFT.obfuscatedInternalName();
        final String player = Minecraft189Mappings.ENTITY_PLAYER_SP.obfuscatedInternalName();
        final String entity = Minecraft189Mappings.ENTITY.obfuscatedInternalName();
        m.visitCode();
        m.visitVarInsn(Opcodes.ALOAD, 0);
        m.visitFieldInsn(Opcodes.GETFIELD, world,
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST.obfuscatedName(),
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST.descriptor());
        m.visitVarInsn(Opcodes.ASTORE, 1);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitJumpInsn(Opcodes.IFNONNULL, haveList);
        m.visitJumpInsn(Opcodes.GOTO, invalid);
        m.visitLabel(haveList);
        m.visitFrame(Opcodes.F_FULL, 2,
                new Object[]{world, "java/util/List"}, 0, new Object[0]);
        m.visitMethodInsn(Opcodes.INVOKESTATIC,
                Minecraft189Mappings.MINECRAFT_GET_MINECRAFT.owner().obfuscatedInternalName(),
                Minecraft189Mappings.MINECRAFT_GET_MINECRAFT.obfuscatedName(),
                Minecraft189Mappings.MINECRAFT_GET_MINECRAFT.descriptor(), false);
        m.visitVarInsn(Opcodes.ASTORE, 2);
        m.visitVarInsn(Opcodes.ALOAD, 2);
        m.visitJumpInsn(Opcodes.IFNONNULL, haveMinecraft);
        m.visitJumpInsn(Opcodes.GOTO, invalid);

        m.visitLabel(haveMinecraft);
        m.visitFrame(Opcodes.F_FULL, 3,
                new Object[]{world, "java/util/List", minecraft}, 0, new Object[0]);
        m.visitVarInsn(Opcodes.ALOAD, 2);
        m.visitFieldInsn(Opcodes.GETFIELD, minecraft,
                Minecraft189Mappings.MINECRAFT_WORLD.obfuscatedName(),
                Minecraft189Mappings.MINECRAFT_WORLD.descriptor());
        m.visitVarInsn(Opcodes.ALOAD, 0);
        m.visitJumpInsn(Opcodes.IF_ACMPEQ, sameWorld);
        m.visitJumpInsn(Opcodes.GOTO, invalid);

        m.visitLabel(sameWorld);
        m.visitFrame(Opcodes.F_FULL, 3,
                new Object[]{world, "java/util/List", minecraft}, 0, new Object[0]);
        m.visitVarInsn(Opcodes.ALOAD, 2);
        m.visitFieldInsn(Opcodes.GETFIELD, minecraft,
                Minecraft189Mappings.MINECRAFT_PLAYER.obfuscatedName(),
                Minecraft189Mappings.MINECRAFT_PLAYER.descriptor());
        m.visitVarInsn(Opcodes.ASTORE, 3);
        m.visitVarInsn(Opcodes.ALOAD, 3);
        m.visitJumpInsn(Opcodes.IFNONNULL, havePlayer);
        m.visitJumpInsn(Opcodes.GOTO, invalid);

        m.visitLabel(havePlayer);
        m.visitFrame(Opcodes.F_FULL, 4,
                new Object[]{world, "java/util/List", minecraft, player},
                0, new Object[0]);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                "java/util/List", "size", "()I", true);
        m.visitVarInsn(Opcodes.ISTORE, 4);
        m.visitVarInsn(Opcodes.ILOAD, 4);
        m.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
        m.visitVarInsn(Opcodes.ASTORE, 5);
        m.visitInsn(Opcodes.ICONST_0);
        m.visitVarInsn(Opcodes.ISTORE, 6);

        final Object[] indexed = new Object[]{
                world, "java/util/List", minecraft, player,
                Opcodes.INTEGER, "[I", Opcodes.INTEGER};
        m.visitLabel(loop);
        m.visitFrame(Opcodes.F_FULL, 7, indexed, 0, new Object[0]);
        m.visitVarInsn(Opcodes.ILOAD, 6);
        m.visitVarInsn(Opcodes.ILOAD, 4);
        m.visitJumpInsn(Opcodes.IF_ICMPGE, done);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitVarInsn(Opcodes.ILOAD, 6);
        m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                "java/util/List", "get", "(I)Ljava/lang/Object;", true);
        m.visitVarInsn(Opcodes.ASTORE, 7);
        m.visitVarInsn(Opcodes.ALOAD, 7);
        m.visitTypeInsn(Opcodes.INSTANCEOF,
                Minecraft189Mappings.ENTITY_PLAYER.obfuscatedInternalName());
        m.visitJumpInsn(Opcodes.IFEQ, nonPlayer);
        m.visitVarInsn(Opcodes.ALOAD, 3);
        m.visitVarInsn(Opcodes.ALOAD, 7);
        m.visitTypeInsn(Opcodes.CHECKCAST, entity);
        m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Minecraft189Mappings.ENTITY_LIVING_BASE_CAN_ENTITY_BE_SEEN
                        .owner().obfuscatedInternalName(),
                Minecraft189Mappings.ENTITY_LIVING_BASE_CAN_ENTITY_BE_SEEN
                        .obfuscatedName(),
                Minecraft189Mappings.ENTITY_LIVING_BASE_CAN_ENTITY_BE_SEEN
                        .descriptor(), false);
        m.visitJumpInsn(Opcodes.GOTO, store);

        final Object[] withCandidate = new Object[]{
                world, "java/util/List", minecraft, player,
                Opcodes.INTEGER, "[I", Opcodes.INTEGER, "java/lang/Object"};
        m.visitLabel(nonPlayer);
        m.visitFrame(Opcodes.F_FULL, 8, withCandidate, 0, new Object[0]);
        m.visitInsn(Opcodes.ICONST_M1);

        m.visitLabel(store);
        m.visitFrame(Opcodes.F_FULL, 8, withCandidate,
                1, new Object[]{Opcodes.INTEGER});
        m.visitVarInsn(Opcodes.ALOAD, 5);
        m.visitInsn(Opcodes.SWAP);
        m.visitVarInsn(Opcodes.ILOAD, 6);
        m.visitInsn(Opcodes.SWAP);
        m.visitInsn(Opcodes.IASTORE);
        m.visitIincInsn(6, 1);
        m.visitJumpInsn(Opcodes.GOTO, loop);

        m.visitLabel(done);
        m.visitFrame(Opcodes.F_FULL, 7, indexed, 0, new Object[0]);
        m.visitVarInsn(Opcodes.ALOAD, 5);
        m.visitInsn(Opcodes.ARETURN);

        m.visitLabel(invalid);
        m.visitFrame(Opcodes.F_FULL, 2,
                new Object[]{world, "java/util/List"}, 0, new Object[0]);
        m.visitInsn(Opcodes.ACONST_NULL);
        m.visitInsn(Opcodes.ARETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
    }

    private static void addLoadedEntityKindsSnapshot(
            final ClassVisitor visitor) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "customMcLoadedEntityKinds",
                        "()[I",
                        null,
                        null);
        final Label nonNull =
                new Label();
        final Label loopCheck =
                new Label();
        final Label loopEnd =
                new Label();

        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitFieldInsn(
                Opcodes.GETFIELD,
                Minecraft189Mappings.WORLD
                        .obfuscatedInternalName(),
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST
                        .obfuscatedName(),
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST
                        .descriptor());
        method.visitVarInsn(
                Opcodes.ASTORE,
                1);
        method.visitVarInsn(
                Opcodes.ALOAD,
                1);
        method.visitJumpInsn(
                Opcodes.IFNONNULL,
                nonNull);
        method.visitInsn(
                Opcodes.ICONST_0);
        method.visitIntInsn(
                Opcodes.NEWARRAY,
                Opcodes.T_INT);
        method.visitInsn(
                Opcodes.ARETURN);

        method.visitLabel(nonNull);
        method.visitFrame(
                Opcodes.F_FULL,
                2,
                new Object[]{
                        Minecraft189Mappings.WORLD
                                .obfuscatedInternalName(),
                        "java/util/List"
                },
                0,
                new Object[0]);
        method.visitVarInsn(
                Opcodes.ALOAD,
                1);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "java/util/List",
                "size",
                "()I",
                true);
        method.visitVarInsn(
                Opcodes.ISTORE,
                2);
        method.visitVarInsn(
                Opcodes.ILOAD,
                2);
        method.visitIntInsn(
                Opcodes.NEWARRAY,
                Opcodes.T_INT);
        method.visitVarInsn(
                Opcodes.ASTORE,
                3);
        method.visitInsn(
                Opcodes.ICONST_0);
        method.visitVarInsn(
                Opcodes.ISTORE,
                4);

        method.visitLabel(loopCheck);
        method.visitFrame(
                Opcodes.F_FULL,
                5,
                new Object[]{
                        Minecraft189Mappings.WORLD
                                .obfuscatedInternalName(),
                        "java/util/List",
                        Opcodes.INTEGER,
                        "[I",
                        Opcodes.INTEGER
                },
                0,
                new Object[0]);
        method.visitVarInsn(
                Opcodes.ILOAD,
                4);
        method.visitVarInsn(
                Opcodes.ILOAD,
                2);
        method.visitJumpInsn(
                Opcodes.IF_ICMPGE,
                loopEnd);

        method.visitVarInsn(
                Opcodes.ALOAD,
                1);
        method.visitVarInsn(
                Opcodes.ILOAD,
                4);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "java/util/List",
                "get",
                "(I)Ljava/lang/Object;",
                true);
        method.visitVarInsn(
                Opcodes.ASTORE,
                5);

        method.visitVarInsn(
                Opcodes.ALOAD,
                3);
        method.visitVarInsn(
                Opcodes.ILOAD,
                4);

        method.visitVarInsn(
                Opcodes.ALOAD,
                5);
        method.visitTypeInsn(
                Opcodes.INSTANCEOF,
                Minecraft189Mappings.ENTITY_LIVING_BASE
                        .obfuscatedInternalName());

        method.visitVarInsn(
                Opcodes.ALOAD,
                5);
        method.visitTypeInsn(
                Opcodes.INSTANCEOF,
                Minecraft189Mappings.ENTITY_PLAYER
                        .obfuscatedInternalName());
        method.visitInsn(
                Opcodes.ICONST_1);
        method.visitInsn(
                Opcodes.ISHL);
        method.visitInsn(
                Opcodes.IOR);

        method.visitVarInsn(
                Opcodes.ALOAD,
                5);
        method.visitTypeInsn(
                Opcodes.INSTANCEOF,
                Minecraft189Mappings.ENTITY_PLAYER_SP
                        .obfuscatedInternalName());
        method.visitInsn(
                Opcodes.ICONST_2);
        method.visitInsn(
                Opcodes.ISHL);
        method.visitInsn(
                Opcodes.IOR);

        method.visitInsn(
                Opcodes.IASTORE);

        method.visitIincInsn(
                4,
                1);
        method.visitJumpInsn(
                Opcodes.GOTO,
                loopCheck);

        method.visitLabel(loopEnd);
        method.visitFrame(
                Opcodes.F_FULL,
                5,
                new Object[]{
                        Minecraft189Mappings.WORLD
                                .obfuscatedInternalName(),
                        "java/util/List",
                        Opcodes.INTEGER,
                        "[I",
                        Opcodes.INTEGER
                },
                0,
                new Object[0]);
        method.visitVarInsn(
                Opcodes.ALOAD,
                3);
        method.visitInsn(
                Opcodes.ARETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    /**
     * Per-index alive/hurt evidence from the already-mapped loadedEntityList.
     * -1 = unknown or nonliving. Nonnegative = (hurtTime << 1) | alive.
     * The list index exactly matches the position/kind snapshots.
     */
    private static void addLoadedEntityCombatSnapshot(
            final ClassVisitor visitor) {
        final MethodVisitor method = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcLoadedEntityCombatStates",
                "()[I", null, null);
        final Label nonNull = new Label();
        final Label loopCheck = new Label();
        final Label loopEnd = new Label();
        final Label nonLiving = new Label();
        final Label noTabInfo = new Label();
        final Label noMinecraftForTeam = new Label();
        final Label wrongWorldForTeam = new Label();
        final Label noLocalPlayerForTeam = new Label();
        final Label teamComplete = new Label();
        final Label store = new Label();
        final String world = Minecraft189Mappings.WORLD.obfuscatedInternalName();

        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD, world,
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST.obfuscatedName(),
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST.descriptor());
        method.visitVarInsn(Opcodes.ASTORE, 1);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitJumpInsn(Opcodes.IFNONNULL, nonNull);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
        method.visitInsn(Opcodes.ARETURN);

        method.visitLabel(nonNull);
        method.visitFrame(Opcodes.F_FULL, 2,
                new Object[]{world, "java/util/List"},
                0, new Object[0]);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                "java/util/List", "size", "()I", true);
        method.visitVarInsn(Opcodes.ISTORE, 2);
        method.visitVarInsn(Opcodes.ILOAD, 2);
        method.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
        method.visitVarInsn(Opcodes.ASTORE, 3);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitVarInsn(Opcodes.ISTORE, 4);

        method.visitLabel(loopCheck);
        method.visitFrame(Opcodes.F_FULL, 5,
                new Object[]{world, "java/util/List", Opcodes.INTEGER,
                        "[I", Opcodes.INTEGER}, 0, new Object[0]);
        method.visitVarInsn(Opcodes.ILOAD, 4);
        method.visitVarInsn(Opcodes.ILOAD, 2);
        method.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitVarInsn(Opcodes.ILOAD, 4);
        method.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                "java/util/List", "get", "(I)Ljava/lang/Object;", true);
        method.visitVarInsn(Opcodes.ASTORE, 5);

        method.visitVarInsn(Opcodes.ALOAD, 5);
        method.visitTypeInsn(Opcodes.INSTANCEOF,
                PLAYER_HEALTH_ACCESS_INTERNAL_NAME);
        method.visitJumpInsn(Opcodes.IFEQ, nonLiving);

        // Math.max(0, FCMPL(health, 0)) yields exactly 1 for health > 0.
        // FCMPL is -1 for NaN, so malformed health fails closed as dead.
        method.visitVarInsn(Opcodes.ALOAD, 5);
        method.visitTypeInsn(Opcodes.CHECKCAST,
                PLAYER_HEALTH_ACCESS_INTERNAL_NAME);
        method.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                PLAYER_HEALTH_ACCESS_INTERNAL_NAME,
                "customMcHealth", "()F", true);
        method.visitInsn(Opcodes.FCONST_0);
        method.visitInsn(Opcodes.FCMPL);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitMethodInsn(Opcodes.INVOKESTATIC,
                "java/lang/Math", "max", "(II)I", false);

        method.visitVarInsn(Opcodes.ALOAD, 5);
        method.visitTypeInsn(Opcodes.CHECKCAST,
                PLAYER_HURT_TIME_ACCESS_INTERNAL_NAME);
        method.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                PLAYER_HURT_TIME_ACCESS_INTERNAL_NAME,
                "customMcHurtTime", "()I", true);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.ISHL);
        method.visitInsn(Opcodes.IOR);
        // Preserve the original low 8-bit health/hurt format. Bits 9/8
        // carry verified NetworkPlayerInfo queryability and presence.
        method.visitVarInsn(Opcodes.ALOAD, 5);
        method.visitTypeInsn(Opcodes.INSTANCEOF,
                PLAYER_TAB_INFO_ACCESS_INTERNAL_NAME);
        method.visitJumpInsn(Opcodes.IFEQ, noTabInfo);
        method.visitIntInsn(Opcodes.SIPUSH, 512);
        method.visitInsn(Opcodes.IOR);
        method.visitVarInsn(Opcodes.ALOAD, 5);
        method.visitTypeInsn(Opcodes.CHECKCAST,
                PLAYER_TAB_INFO_ACCESS_INTERNAL_NAME);
        method.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                PLAYER_TAB_INFO_ACCESS_INTERNAL_NAME,
                "customMcHasNetworkPlayerInfo", "()Z", true);
        method.visitJumpInsn(Opcodes.IFEQ, noTabInfo);
        method.visitIntInsn(Opcodes.SIPUSH, 256);
        method.visitInsn(Opcodes.IOR);
        method.visitLabel(noTabInfo);
        method.visitFrame(Opcodes.F_FULL, 6,
                new Object[]{world, "java/util/List", Opcodes.INTEGER,
                        "[I", Opcodes.INTEGER, "java/lang/Object"},
                1, new Object[]{Opcodes.INTEGER});
        // Native vanilla EntityLivingBase.isOnSameTeam(anotherLivingBase).
        // Only source-confirmed EntityPlayer candidates are eligible, and
        // team evidence is unknown if the local Minecraft player is absent.
        // This preserves all existing lower 10-bit combat evidence.
        method.visitVarInsn(Opcodes.ALOAD, 5);
        method.visitTypeInsn(Opcodes.INSTANCEOF,
                Minecraft189Mappings.ENTITY_PLAYER.obfuscatedInternalName());
        method.visitJumpInsn(Opcodes.IFEQ, teamComplete);
        method.visitMethodInsn(Opcodes.INVOKESTATIC,
                Minecraft189Mappings.MINECRAFT_GET_MINECRAFT.owner()
                        .obfuscatedInternalName(),
                Minecraft189Mappings.MINECRAFT_GET_MINECRAFT.obfuscatedName(),
                Minecraft189Mappings.MINECRAFT_GET_MINECRAFT.descriptor(), false);
        method.visitInsn(Opcodes.DUP);
        method.visitJumpInsn(Opcodes.IFNULL, noMinecraftForTeam);
        // Refuse team evidence from a world other than active Minecraft.world.
        method.visitInsn(Opcodes.DUP);
        method.visitFieldInsn(Opcodes.GETFIELD,
                Minecraft189Mappings.MINECRAFT.obfuscatedInternalName(),
                Minecraft189Mappings.MINECRAFT_WORLD.obfuscatedName(),
                Minecraft189Mappings.MINECRAFT_WORLD.descriptor());
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitJumpInsn(Opcodes.IF_ACMPNE, wrongWorldForTeam);
        method.visitFieldInsn(Opcodes.GETFIELD,
                Minecraft189Mappings.MINECRAFT.obfuscatedInternalName(),
                Minecraft189Mappings.MINECRAFT_PLAYER.obfuscatedName(),
                Minecraft189Mappings.MINECRAFT_PLAYER.descriptor());
        method.visitInsn(Opcodes.DUP);
        method.visitJumpInsn(Opcodes.IFNULL, noLocalPlayerForTeam);
        method.visitVarInsn(Opcodes.ALOAD, 5);
        method.visitTypeInsn(Opcodes.CHECKCAST,
                Minecraft189Mappings.ENTITY_LIVING_BASE.obfuscatedInternalName());
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Minecraft189Mappings.ENTITY_LIVING_BASE_IS_ON_SAME_TEAM.owner()
                        .obfuscatedInternalName(),
                Minecraft189Mappings.ENTITY_LIVING_BASE_IS_ON_SAME_TEAM
                        .obfuscatedName(),
                Minecraft189Mappings.ENTITY_LIVING_BASE_IS_ON_SAME_TEAM
                        .descriptor(), false);
        method.visitIntInsn(Opcodes.SIPUSH, 1024);
        method.visitInsn(Opcodes.IMUL);
        method.visitIntInsn(Opcodes.SIPUSH, 2048);
        method.visitInsn(Opcodes.IOR);
        method.visitInsn(Opcodes.IOR);
        method.visitJumpInsn(Opcodes.GOTO, teamComplete);

        method.visitLabel(noMinecraftForTeam);
        method.visitFrame(Opcodes.F_FULL, 6,
                new Object[]{world, "java/util/List", Opcodes.INTEGER,
                        "[I", Opcodes.INTEGER, "java/lang/Object"},
                2, new Object[]{Opcodes.INTEGER,
                        Minecraft189Mappings.MINECRAFT.obfuscatedInternalName()});
        method.visitInsn(Opcodes.POP);
        method.visitJumpInsn(Opcodes.GOTO, teamComplete);

        method.visitLabel(wrongWorldForTeam);
        method.visitFrame(Opcodes.F_FULL, 6,
                new Object[]{world, "java/util/List", Opcodes.INTEGER,
                        "[I", Opcodes.INTEGER, "java/lang/Object"},
                2, new Object[]{Opcodes.INTEGER,
                        Minecraft189Mappings.MINECRAFT.obfuscatedInternalName()});
        method.visitInsn(Opcodes.POP);
        method.visitJumpInsn(Opcodes.GOTO, teamComplete);

        method.visitLabel(noLocalPlayerForTeam);
        method.visitFrame(Opcodes.F_FULL, 6,
                new Object[]{world, "java/util/List", Opcodes.INTEGER,
                        "[I", Opcodes.INTEGER, "java/lang/Object"},
                2, new Object[]{Opcodes.INTEGER,
                        Minecraft189Mappings.ENTITY_PLAYER_SP
                                .obfuscatedInternalName()});
        method.visitInsn(Opcodes.POP);

        method.visitLabel(teamComplete);
        method.visitFrame(Opcodes.F_FULL, 6,
                new Object[]{world, "java/util/List", Opcodes.INTEGER,
                        "[I", Opcodes.INTEGER, "java/lang/Object"},
                1, new Object[]{Opcodes.INTEGER});
        method.visitVarInsn(Opcodes.ISTORE, 6);
        method.visitJumpInsn(Opcodes.GOTO, store);

        method.visitLabel(nonLiving);
        method.visitFrame(Opcodes.F_FULL, 6,
                new Object[]{world, "java/util/List", Opcodes.INTEGER,
                        "[I", Opcodes.INTEGER, "java/lang/Object"},
                0, new Object[0]);
        method.visitInsn(Opcodes.ICONST_M1);
        method.visitVarInsn(Opcodes.ISTORE, 6);

        method.visitLabel(store);
        method.visitFrame(Opcodes.F_FULL, 7,
                new Object[]{world, "java/util/List", Opcodes.INTEGER,
                        "[I", Opcodes.INTEGER, "java/lang/Object", Opcodes.INTEGER},
                0, new Object[0]);
        method.visitVarInsn(Opcodes.ALOAD, 3);
        method.visitVarInsn(Opcodes.ILOAD, 4);
        method.visitVarInsn(Opcodes.ILOAD, 6);
        method.visitInsn(Opcodes.IASTORE);
        method.visitIincInsn(4, 1);
        method.visitJumpInsn(Opcodes.GOTO, loopCheck);

        method.visitLabel(loopEnd);
        method.visitFrame(Opcodes.F_FULL, 5,
                new Object[]{world, "java/util/List", Opcodes.INTEGER,
                        "[I", Opcodes.INTEGER},
                0, new Object[0]);
        method.visitVarInsn(Opcodes.ALOAD, 3);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    /** Native EntityPlayer.isUsingItem() (wn.bS). */
    private static void addMappedIsUsingItemGetter(final ClassVisitor visitor) {
        final Minecraft189Mappings.MappedField player =
                Minecraft189Mappings.MINECRAFT_PLAYER;
        final Minecraft189Mappings.MappedMethod using =
                Minecraft189Mappings.ENTITY_PLAYER_IS_USING_ITEM;
        final MethodVisitor method = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcIsUsingItem", "()Z", null, null);
        final Label missing = new Label();
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD,
                player.owner().obfuscatedInternalName(),
                player.obfuscatedName(), player.descriptor());
        method.visitInsn(Opcodes.DUP);
        method.visitJumpInsn(Opcodes.IFNULL, missing);
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                using.owner().obfuscatedInternalName(),
                using.obfuscatedName(), using.descriptor(), false);
        method.visitInsn(Opcodes.IRETURN);
        method.visitLabel(missing);
        method.visitFrame(Opcodes.F_SAME1, 0, null, 1,
                new Object[]{Minecraft189Mappings.ENTITY_PLAYER_SP.obfuscatedInternalName()});
        method.visitInsn(Opcodes.POP);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    /** Source-verified PlayerControllerMP.onStoppedUsingItem(thePlayer). */
    private static void addMappedStopUsingItem(final ClassVisitor visitor) {
        final Minecraft189Mappings.MappedField controller =
                Minecraft189Mappings.MINECRAFT_PLAYER_CONTROLLER;
        final Minecraft189Mappings.MappedField player =
                Minecraft189Mappings.MINECRAFT_PLAYER;
        final Minecraft189Mappings.MappedMethod stop =
                Minecraft189Mappings.PLAYER_CONTROLLER_STOP_USING_ITEM;
        final MethodVisitor method = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcStopUsingItem", "()V", null, null);
        final Label done = new Label();
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD,
                controller.owner().obfuscatedInternalName(),
                controller.obfuscatedName(), controller.descriptor());
        method.visitVarInsn(Opcodes.ASTORE, 1);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD,
                player.owner().obfuscatedInternalName(),
                player.obfuscatedName(), player.descriptor());
        method.visitVarInsn(Opcodes.ASTORE, 2);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitJumpInsn(Opcodes.IFNULL, done);
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitJumpInsn(Opcodes.IFNULL, done);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                stop.owner().obfuscatedInternalName(),
                stop.obfuscatedName(), stop.descriptor(), false);
        method.visitLabel(done);
        method.visitFrame(Opcodes.F_FULL, 3,
                new Object[]{Minecraft189Mappings.MINECRAFT.obfuscatedInternalName(),
                        Minecraft189Mappings.PLAYER_CONTROLLER_MP.obfuscatedInternalName(),
                        Minecraft189Mappings.ENTITY_PLAYER_SP.obfuscatedInternalName()},
                0, new Object[0]);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void addCrosshairHitAccessor(
            final ClassVisitor visitor) {
        final Minecraft189Mappings.MappedField over =
                Minecraft189Mappings.MINECRAFT_OBJECT_MOUSE_OVER;
        final Minecraft189Mappings.MappedField kind =
                Minecraft189Mappings.MOVING_OBJECT_TYPE_OF_HIT;
        final Minecraft189Mappings.MappedField entity =
                Minecraft189Mappings.MOVING_OBJECT_ENTITY_HIT;
        final Minecraft189Mappings.MappedField enumEntity =
                Minecraft189Mappings.MOVING_OBJECT_TYPE_ENTITY;
        final Label reject = new Label();
        final MethodVisitor method = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcCrosshairPlayerHit", "()Z",
                null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD,
                over.owner().obfuscatedInternalName(),
                over.obfuscatedName(), over.descriptor());
        method.visitVarInsn(Opcodes.ASTORE, 1);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitJumpInsn(Opcodes.IFNULL, reject);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitFieldInsn(Opcodes.GETFIELD,
                kind.owner().obfuscatedInternalName(),
                kind.obfuscatedName(), kind.descriptor());
        method.visitFieldInsn(Opcodes.GETSTATIC,
                enumEntity.owner().obfuscatedInternalName(),
                enumEntity.obfuscatedName(), enumEntity.descriptor());
        method.visitJumpInsn(Opcodes.IF_ACMPNE, reject);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitFieldInsn(Opcodes.GETFIELD,
                entity.owner().obfuscatedInternalName(),
                entity.obfuscatedName(), entity.descriptor());
        method.visitTypeInsn(Opcodes.INSTANCEOF,
                Minecraft189Mappings.ENTITY_PLAYER.obfuscatedInternalName());
        method.visitInsn(Opcodes.IRETURN);
        method.visitLabel(reject);
        // Newly introduced Java 8 branch target must carry its own
        // stack-map frame; COMPUTE_MAXS alone does not generate frames.
        method.visitFrame(Opcodes.F_FULL, 2,
                new Object[]{Minecraft189Mappings.MINECRAFT.obfuscatedInternalName(),
                        Minecraft189Mappings.MOVING_OBJECT_POSITION.obfuscatedInternalName()},
                0, new Object[0]);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 2);
        method.visitEnd();
    }


    /**
     * Resolves the exact player ray-hit object to the same verified
     * loadedEntityList index used by the position/kind snapshots.
     * Only existing proven Minecraft, MovingObjectPosition and World
     * fields are touched. Unknown/stale evidence returns -1.
     */

    /** Exact native ray-hit player's mapped six-coordinate AABB; read-only. */
    private static void addCrosshairHitboxBoundsAccessor(final ClassVisitor visitor) {
        final Minecraft189Mappings.MappedField over =
                Minecraft189Mappings.MINECRAFT_OBJECT_MOUSE_OVER;
        final Minecraft189Mappings.MappedField kind =
                Minecraft189Mappings.MOVING_OBJECT_TYPE_OF_HIT;
        final Minecraft189Mappings.MappedField entity =
                Minecraft189Mappings.MOVING_OBJECT_ENTITY_HIT;
        final Minecraft189Mappings.MappedField enumEntity =
                Minecraft189Mappings.MOVING_OBJECT_TYPE_ENTITY;
        final Label reject = new Label();
        final MethodVisitor m = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcCrosshairHitboxBounds", "()[D",
                null, null);
        m.visitCode();
        m.visitVarInsn(Opcodes.ALOAD, 0);
        m.visitFieldInsn(Opcodes.GETFIELD,
                over.owner().obfuscatedInternalName(),
                over.obfuscatedName(), over.descriptor());
        m.visitVarInsn(Opcodes.ASTORE, 1);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitJumpInsn(Opcodes.IFNULL, reject);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitFieldInsn(Opcodes.GETFIELD,
                kind.owner().obfuscatedInternalName(),
                kind.obfuscatedName(), kind.descriptor());
        m.visitFieldInsn(Opcodes.GETSTATIC,
                enumEntity.owner().obfuscatedInternalName(),
                enumEntity.obfuscatedName(), enumEntity.descriptor());
        m.visitJumpInsn(Opcodes.IF_ACMPNE, reject);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitFieldInsn(Opcodes.GETFIELD,
                entity.owner().obfuscatedInternalName(),
                entity.obfuscatedName(), entity.descriptor());
        m.visitVarInsn(Opcodes.ASTORE, 2);
        m.visitVarInsn(Opcodes.ALOAD, 2);
        m.visitTypeInsn(Opcodes.INSTANCEOF,
                Minecraft189Mappings.ENTITY_PLAYER.obfuscatedInternalName());
        m.visitJumpInsn(Opcodes.IFEQ, reject);
        m.visitVarInsn(Opcodes.ALOAD, 2);
        m.visitTypeInsn(Opcodes.CHECKCAST, HITBOX_BOUNDS_ACCESS_INTERNAL_NAME);
        m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                HITBOX_BOUNDS_ACCESS_INTERNAL_NAME,
                "customMcHitboxBounds", "()[D", true);
        m.visitInsn(Opcodes.ARETURN);
        m.visitLabel(reject);
        m.visitFrame(Opcodes.F_FULL, 1,
                new Object[]{Minecraft189Mappings.MINECRAFT.obfuscatedInternalName()},
                0, new Object[0]);
        m.visitInsn(Opcodes.ACONST_NULL);
        m.visitInsn(Opcodes.ARETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
    }

    private static void addCrosshairPlayerIndexAccessor(
            final ClassVisitor visitor) {
        final Minecraft189Mappings.MappedField over =
                Minecraft189Mappings.MINECRAFT_OBJECT_MOUSE_OVER;
        final Minecraft189Mappings.MappedField kind =
                Minecraft189Mappings.MOVING_OBJECT_TYPE_OF_HIT;
        final Minecraft189Mappings.MappedField entity =
                Minecraft189Mappings.MOVING_OBJECT_ENTITY_HIT;
        final Minecraft189Mappings.MappedField enumEntity =
                Minecraft189Mappings.MOVING_OBJECT_TYPE_ENTITY;
        final Minecraft189Mappings.MappedField world =
                Minecraft189Mappings.MINECRAFT_WORLD;
        final Minecraft189Mappings.MappedField list =
                Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST;
        final Label reject = new Label();
        final MethodVisitor method = visitor.visitMethod(
                Opcodes.ACC_PUBLIC, "customMcCrosshairPlayerIndex", "()I",
                null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD,
                over.owner().obfuscatedInternalName(),
                over.obfuscatedName(), over.descriptor());
        method.visitVarInsn(Opcodes.ASTORE, 1);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitJumpInsn(Opcodes.IFNULL, reject);

        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitFieldInsn(Opcodes.GETFIELD,
                kind.owner().obfuscatedInternalName(),
                kind.obfuscatedName(), kind.descriptor());
        method.visitFieldInsn(Opcodes.GETSTATIC,
                enumEntity.owner().obfuscatedInternalName(),
                enumEntity.obfuscatedName(), enumEntity.descriptor());
        method.visitJumpInsn(Opcodes.IF_ACMPNE, reject);

        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitFieldInsn(Opcodes.GETFIELD,
                entity.owner().obfuscatedInternalName(),
                entity.obfuscatedName(), entity.descriptor());
        method.visitVarInsn(Opcodes.ASTORE, 2);
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitTypeInsn(Opcodes.INSTANCEOF,
                Minecraft189Mappings.ENTITY_PLAYER.obfuscatedInternalName());
        method.visitJumpInsn(Opcodes.IFEQ, reject);

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD,
                world.owner().obfuscatedInternalName(),
                world.obfuscatedName(), world.descriptor());
        method.visitVarInsn(Opcodes.ASTORE, 3);
        method.visitVarInsn(Opcodes.ALOAD, 3);
        method.visitJumpInsn(Opcodes.IFNULL, reject);
        method.visitVarInsn(Opcodes.ALOAD, 3);
        method.visitFieldInsn(Opcodes.GETFIELD,
                list.owner().obfuscatedInternalName(),
                list.obfuscatedName(), list.descriptor());
        method.visitVarInsn(Opcodes.ASTORE, 4);
        method.visitVarInsn(Opcodes.ALOAD, 4);
        method.visitJumpInsn(Opcodes.IFNULL, reject);

        method.visitVarInsn(Opcodes.ALOAD, 4);
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                "java/util/List", "indexOf", "(Ljava/lang/Object;)I", true);
        method.visitInsn(Opcodes.IRETURN);

        method.visitLabel(reject);
        // Merge paths where different temporary locals were populated.
        method.visitFrame(Opcodes.F_FULL, 1,
                new Object[]{Minecraft189Mappings.MINECRAFT.obfuscatedInternalName()},
                0, new Object[0]);
        method.visitInsn(Opcodes.ICONST_M1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 5);
        method.visitEnd();
    }

    private static void addVoidMethodDelegate(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedMethod target) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()V",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                target.owner().obfuscatedInternalName(),
                target.obfuscatedName(),
                target.descriptor(),
                false);
        method.visitInsn(
                Opcodes.RETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addBooleanMethodDelegate(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedMethod target) {
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
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                target.owner().obfuscatedInternalName(),
                target.obfuscatedName(),
                target.descriptor(),
                false);
        method.visitInsn(
                Opcodes.IRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addLongMethodDelegate(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedMethod target) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()J",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                target.owner().obfuscatedInternalName(),
                target.obfuscatedName(),
                target.descriptor(),
                false);
        method.visitInsn(
                Opcodes.LRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addIntMethodDelegate(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedMethod target) {
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
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                target.owner().obfuscatedInternalName(),
                target.obfuscatedName(),
                target.descriptor(),
                false);
        method.visitInsn(
                Opcodes.IRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addStringMethodDelegate(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedMethod target) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()Ljava/lang/String;",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                target.owner().obfuscatedInternalName(),
                target.obfuscatedName(),
                target.descriptor(),
                false);
        method.visitInsn(
                Opcodes.ARETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addEquipmentPresentGetter(
            final ClassVisitor visitor,
            final String methodName,
            final int slot) {
        final Minecraft189Mappings.MappedMethod equipment =
                Minecraft189Mappings
                        .ENTITY_LIVING_BASE_GET_EQUIPMENT_IN_SLOT;
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
        method.visitIntInsn(
                Opcodes.BIPUSH,
                slot);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                equipment.owner().obfuscatedInternalName(),
                equipment.obfuscatedName(),
                equipment.descriptor(),
                false);
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/util/Objects",
                "nonNull",
                "(Ljava/lang/Object;)Z",
                false);
        method.visitInsn(
                Opcodes.IRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addFloatMethodDelegate(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedMethod target) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()F",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                target.owner().obfuscatedInternalName(),
                target.obfuscatedName(),
                target.descriptor(),
                false);
        method.visitInsn(
                Opcodes.FRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addStringFieldGetter(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedField field) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()Ljava/lang/String;",
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
                Opcodes.ARETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addDoubleFieldGetter(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedField field) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()D",
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
                Opcodes.DRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addDoubleFieldSetter(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedField field) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "(D)V",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitVarInsn(
                Opcodes.DLOAD,
                1);
        method.visitFieldInsn(
                Opcodes.PUTFIELD,
                field.owner().obfuscatedInternalName(),
                field.obfuscatedName(),
                field.descriptor());
        method.visitInsn(
                Opcodes.RETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addFloatFieldGetter(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedField field) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "()F",
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
                Opcodes.FRETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addFloatFieldSetter(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedField field) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "(F)V",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitVarInsn(
                Opcodes.FLOAD,
                1);
        method.visitFieldInsn(
                Opcodes.PUTFIELD,
                field.owner().obfuscatedInternalName(),
                field.obfuscatedName(),
                field.descriptor());
        method.visitInsn(
                Opcodes.RETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
    }

    private static void addIntFieldSetter(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedField field) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "(I)V",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitVarInsn(
                Opcodes.ILOAD,
                1);
        method.visitFieldInsn(
                Opcodes.PUTFIELD,
                field.owner().obfuscatedInternalName(),
                field.obfuscatedName(),
                field.descriptor());
        method.visitInsn(
                Opcodes.RETURN);
        method.visitMaxs(
                0,
                0);
        method.visitEnd();
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

    private static void addBooleanFieldSetter(
            final ClassVisitor visitor,
            final String methodName,
            final Minecraft189Mappings.MappedField field) {
        final MethodVisitor method =
                visitor.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "(Z)V",
                        null,
                        null);
        method.visitCode();
        method.visitVarInsn(
                Opcodes.ALOAD,
                0);
        method.visitVarInsn(
                Opcodes.ILOAD,
                1);
        method.visitFieldInsn(
                Opcodes.PUTFIELD,
                field.owner().obfuscatedInternalName(),
                field.obfuscatedName(),
                field.descriptor());
        method.visitInsn(
                Opcodes.RETURN);
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
