package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPass;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Objects;

public final class Minecraft189CrosshairModule
        implements Module {
    public static final String ID =
            "render.crosshair";
    public static final String LENGTH_SETTING_ID =
            "render.crosshair.length";
    public static final String GAP_SETTING_ID =
            "render.crosshair.gap";
    public static final String THICKNESS_SETTING_ID =
            "render.crosshair.thickness";
    public static final String DOT_SETTING_ID =
            "render.crosshair.dot";
    public static final String SPRINT_EXPANSION_SETTING_ID =
            "render.crosshair.sprintExpansion";
    public static final String SPRINT_GAP_BONUS_SETTING_ID =
            "render.crosshair.sprintGapBonus";
    public static final String RED_SETTING_ID = "render.crosshair.red";
    public static final String GREEN_SETTING_ID = "render.crosshair.green";
    public static final String BLUE_SETTING_ID = "render.crosshair.blue";
    public static final String OUTLINE_SETTING_ID =
            "render.crosshair.outline";
    public static final String OUTLINE_SIZE_SETTING_ID =
            "render.crosshair.outlineSize";
    public static final String RENDER_PASS_ID =
            "crosshair";

    private static final int PRIORITY = 130;
    public static final int DEFAULT_COLOR_CHANNEL = 255;
    private static final int OUTLINE_ARGB = 0xFF000000;

    private final RenderPipeline renderPipeline;
    private final LegacyUiHostCallbacks hostCallbacks;
    private final Minecraft189PlayerMovementState movementState;
    private final Setting<Integer> length =
            new Setting<Integer>(
                    LENGTH_SETTING_ID,
                    4,
                    value -> value >= 1
                            && value <= 20,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> gap =
            new Setting<Integer>(
                    GAP_SETTING_ID,
                    2,
                    value -> value >= 0
                            && value <= 12,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> thickness =
            new Setting<Integer>(
                    THICKNESS_SETTING_ID,
                    1,
                    value -> value >= 1
                            && value <= 6,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> dot =
            new Setting<Boolean>(
                    DOT_SETTING_ID,
                    Boolean.FALSE,
                    value -> true,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> sprintExpansion =
            new Setting<Boolean>(
                    SPRINT_EXPANSION_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> sprintGapBonus =
            new Setting<Integer>(
                    SPRINT_GAP_BONUS_SETTING_ID, 4,
                    value -> value != null && value >= 1 && value <= 12,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> red = new Setting<Integer>(
            RED_SETTING_ID, DEFAULT_COLOR_CHANNEL,
            Minecraft189CrosshairModule::validChannel, SettingCodecs.INTEGER);
    private final Setting<Integer> green = new Setting<Integer>(
            GREEN_SETTING_ID, DEFAULT_COLOR_CHANNEL,
            Minecraft189CrosshairModule::validChannel, SettingCodecs.INTEGER);
    private final Setting<Integer> blue = new Setting<Integer>(
            BLUE_SETTING_ID, DEFAULT_COLOR_CHANNEL,
            Minecraft189CrosshairModule::validChannel, SettingCodecs.INTEGER);
    private final Setting<Boolean> outline =
            new Setting<Boolean>(
                    OUTLINE_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> outlineSize =
            new Setting<Integer>(
                    OUTLINE_SIZE_SETTING_ID, 1,
                    value -> value != null && value >= 1 && value <= 3,
                    SettingCodecs.INTEGER);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189CrosshairModule(
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this(renderPipeline, hostCallbacks, null);
    }

    public Minecraft189CrosshairModule(
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks,
            final Minecraft189PlayerMovementState movementState) {
        this.renderPipeline =
                Objects.requireNonNull(
                        renderPipeline,
                        "renderPipeline");
        this.hostCallbacks =
                Objects.requireNonNull(
                        hostCallbacks,
                        "hostCallbacks");
        this.movementState = movementState;
    }

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> lengthSetting() {
        return length;
    }

    public Setting<Integer> gapSetting() {
        return gap;
    }

    public Setting<Integer> thicknessSetting() {
        return thickness;
    }

    public Setting<Boolean> dotSetting() {
        return dot;
    }

    public Setting<Boolean> sprintExpansionSetting() {
        return sprintExpansion;
    }

    public Setting<Integer> sprintGapBonusSetting() {
        return sprintGapBonus;
    }

    public Setting<Integer> redSetting() { return red; }
    public Setting<Integer> greenSetting() { return green; }
    public Setting<Integer> blueSetting() { return blue; }

    static boolean validChannel(final Integer v) {
        return v != null && v >= 0 && v <= 255;
    }

    static int rgbArgb(final int red, final int green, final int blue) {
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    public Setting<Boolean> outlineSetting() {
        return outline;
    }

    public Setting<Integer> outlineSizeSetting() {
        return outlineSize;
    }

    static float effectiveGap(final float baseGap,
            final boolean expansionEnabled, final int bonus,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!expansionEnabled || movement == null
                || !movement.available() || !movement.sprinting()) {
            return baseGap;
        }
        return baseGap + bonus;
    }

    @Override
    public synchronized void onEnable() {
        if (renderRegistration != null) {
            throw new IllegalStateException(
                    "crosshair render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new CrosshairRenderPass());
    }

    @Override
    public synchronized void onDisable() {
        if (renderRegistration == null) {
            return;
        }
        renderRegistration.close();
        renderRegistration = null;
    }

    synchronized boolean renderPassInstalled() {
        return renderRegistration != null
                && renderRegistration.active();
    }

    private final class CrosshairRenderPass
            implements RenderPass {
        @Override
        public String id() {
            return RENDER_PASS_ID;
        }

        @Override
        public RenderStage stage() {
            return RenderStage.HUD;
        }

        @Override
        public int priority() {
            return PRIORITY;
        }

        @Override
        public void render(
                final RenderFrame frame) {
            Objects.requireNonNull(
                    frame,
                    "frame");

            final UiViewport viewport =
                    new UiViewport(
                            hostCallbacks.framebufferWidth(),
                            hostCallbacks.framebufferHeight(),
                            hostCallbacks.uiScale());
            final float centerX =
                    viewport.logicalWidth() / 2.0F;
            final float centerY =
                    viewport.logicalHeight() / 2.0F;
            final float lineLength =
                    length.get().floatValue();
            final float lineGap =
                    effectiveGap(
                            gap.get().floatValue(),
                            sprintExpansion.get().booleanValue(),
                            sprintGapBonus.get().intValue(),
                            movementState == null ? null : movementState.snapshot());
            final float lineThickness =
                    thickness.get().floatValue();
            final float halfThickness =
                    lineThickness / 2.0F;
            final int strokeColor = rgbArgb(
                    red.get().intValue(),
                    green.get().intValue(),
                    blue.get().intValue());

            hostCallbacks.beginUi(viewport);
            RuntimeException failure = null;
            try {
                // Draw backing silhouettes first, then the exact original
                // white arm/dot geometry. With Outline OFF, the render
                // sequence and output are byte-for-byte unchanged.
                if (outline.get().booleanValue()) {
                    final float pad = outlineSize.get().floatValue();
                    final float twicePad = 2.0F * pad;
                    hostCallbacks.fillRect(
                            centerX - lineGap - lineLength - pad,
                            centerY - halfThickness - pad,
                            lineLength + twicePad,
                            lineThickness + twicePad,
                            OUTLINE_ARGB);
                    hostCallbacks.fillRect(
                            centerX + lineGap - pad,
                            centerY - halfThickness - pad,
                            lineLength + twicePad,
                            lineThickness + twicePad,
                            OUTLINE_ARGB);
                    hostCallbacks.fillRect(
                            centerX - halfThickness - pad,
                            centerY - lineGap - lineLength - pad,
                            lineThickness + twicePad,
                            lineLength + twicePad,
                            OUTLINE_ARGB);
                    hostCallbacks.fillRect(
                            centerX - halfThickness - pad,
                            centerY + lineGap - pad,
                            lineThickness + twicePad,
                            lineLength + twicePad,
                            OUTLINE_ARGB);
                    if (dot.get().booleanValue()) {
                        hostCallbacks.fillRect(
                                centerX - halfThickness - pad,
                                centerY - halfThickness - pad,
                                lineThickness + twicePad,
                                lineThickness + twicePad,
                                OUTLINE_ARGB);
                    }
                }
                hostCallbacks.fillRect(
                        centerX - lineGap - lineLength,
                        centerY - halfThickness,
                        lineLength,
                        lineThickness,
                        strokeColor);
                hostCallbacks.fillRect(
                        centerX + lineGap,
                        centerY - halfThickness,
                        lineLength,
                        lineThickness,
                        strokeColor);
                hostCallbacks.fillRect(
                        centerX - halfThickness,
                        centerY - lineGap - lineLength,
                        lineThickness,
                        lineLength,
                        strokeColor);
                hostCallbacks.fillRect(
                        centerX - halfThickness,
                        centerY + lineGap,
                        lineThickness,
                        lineLength,
                        strokeColor);

                if (dot.get().booleanValue()) {
                    hostCallbacks.fillRect(
                            centerX - halfThickness,
                            centerY - halfThickness,
                            lineThickness,
                            lineThickness,
                            strokeColor);
                }
            } catch (RuntimeException drawFailure) {
                failure = drawFailure;
                throw drawFailure;
            } finally {
                try {
                    hostCallbacks.endUi();
                } catch (RuntimeException closeFailure) {
                    if (failure != null) {
                        failure.addSuppressed(
                                closeFailure);
                    } else {
                        throw closeFailure;
                    }
                }
            }
        }
    }
}
