package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
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
                                        withInterface(interfaces,
                                                CLICK_MOUSE_CONTROL_INTERNAL_NAME),
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
                        addCrosshairHitAccessor(cv);
                        addCrosshairPlayerIndexAccessor(cv);
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
                                                interfaces,
                                                BLOCK_HIT_DELAY_CONTROL_INTERNAL_NAME),
                                        BLOCK_MINING_CONTROL_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
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
                                        PLAYER_ROTATION_CONTROL_INTERNAL_NAME));
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
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
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
                                                                        interfaces,
                                                                        WORLD_TIME_ACCESS_INTERNAL_NAME),
                                                        WORLD_WEATHER_ACCESS_INTERNAL_NAME),
                                                WORLD_ENTITY_POSITIONS_ACCESS_INTERNAL_NAME),
                                        WORLD_ENTITY_KINDS_ACCESS_INTERNAL_NAME),
                                WORLD_ENTITY_COMBAT_ACCESS_INTERNAL_NAME));
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
                                        interfaces,
                                        PLAYER_PING_ACCESS_INTERNAL_NAME));
                    }

                    @Override
                    public void visitEnd() {
                        addPingGetter(
                                cv,
                                "customMcPingMilliseconds");
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
                                        interfaces,
                                        INVENTORY_HOTBAR_CONTROL_INTERNAL_NAME));
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
                        super.visitEnd();
                    }
                },
                0);

        return writer.toByteArray();
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
