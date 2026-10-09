package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPass;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Objects;

public final class Minecraft189KeystrokesModule
        implements Module {
    public static final String ID =
            "render.keystrokes";
    public static final String X_SETTING_ID =
            "render.keystrokes.x";
    public static final String Y_SETTING_ID =
            "render.keystrokes.y";
    public static final String SHOW_SPACE_SETTING_ID =
            "render.keystrokes.showSpace";
    public static final String SHOW_SHIFT_SETTING_ID =
            "render.keystrokes.showShift";
    public static final String RENDER_PASS_ID =
            "keystrokes";

    private static final int PRIORITY = 120;
    private static final int IDLE_ARGB = 0x90000000;
    private static final int PRESSED_ARGB = 0xD0FFFFFF;
    private static final int IDLE_TEXT_ARGB = 0xFFFFFFFF;
    private static final int PRESSED_TEXT_ARGB = 0xFF111111;
    private static final float KEY = 20.0F;
    private static final float GAP = 2.0F;
    private static final float MOUSE_WIDTH = 31.0F;

    private final Minecraft189InputState inputState;
    private final RenderPipeline renderPipeline;
    private final LegacyUiHostCallbacks hostCallbacks;
    private final Setting<Integer> x =
            new Setting<Integer>(
                    X_SETTING_ID,
                    8,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> y =
            new Setting<Integer>(
                    Y_SETTING_ID,
                    48,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> showSpace = new Setting<Boolean>(
            SHOW_SPACE_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> showShift = new Setting<Boolean>(
            SHOW_SHIFT_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189KeystrokesModule(
            final Minecraft189InputState inputState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.inputState =
                Objects.requireNonNull(
                        inputState,
                        "inputState");
        this.renderPipeline =
                Objects.requireNonNull(
                        renderPipeline,
                        "renderPipeline");
        this.hostCallbacks =
                Objects.requireNonNull(
                        hostCallbacks,
                        "hostCallbacks");
    }

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> xSetting() {
        return x;
    }

    public Setting<Integer> ySetting() {
        return y;
    }

    public Setting<Boolean> showSpaceSetting() {
        return showSpace;
    }

    public Setting<Boolean> showShiftSetting() {
        return showShift;
    }

    @Override
    public synchronized void onEnable() {
        if (renderRegistration != null) {
            throw new IllegalStateException(
                    "keystrokes render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new KeystrokesRenderPass());
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

    private final class KeystrokesRenderPass
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
            final float left =
                    x.get().floatValue();
            final float top =
                    y.get().floatValue();

            hostCallbacks.beginUi(viewport);
            RuntimeException failure = null;
            try {
                drawKey(
                        left + KEY + GAP,
                        top,
                        KEY,
                        KEY,
                        "W",
                        inputState.keyPressed(
                                LegacyKeyboardCodes.W));
                drawKey(
                        left,
                        top + KEY + GAP,
                        KEY,
                        KEY,
                        "A",
                        inputState.keyPressed(
                                LegacyKeyboardCodes.A));
                drawKey(
                        left + KEY + GAP,
                        top + KEY + GAP,
                        KEY,
                        KEY,
                        "S",
                        inputState.keyPressed(
                                LegacyKeyboardCodes.S));
                drawKey(
                        left + (KEY + GAP) * 2.0F,
                        top + KEY + GAP,
                        KEY,
                        KEY,
                        "D",
                        inputState.keyPressed(
                                LegacyKeyboardCodes.D));
                drawKey(
                        left,
                        top + (KEY + GAP) * 2.0F,
                        MOUSE_WIDTH,
                        KEY,
                        "LMB",
                        inputState.pointerPressed(0));
                drawKey(
                        left + MOUSE_WIDTH + GAP,
                        top + (KEY + GAP) * 2.0F,
                        MOUSE_WIDTH,
                        KEY,
                        "RMB",
                        inputState.pointerPressed(1));
                if (showSpace.get().booleanValue()) {
                    drawKey(
                            left,
                            top + (KEY + GAP) * 3.0F,
                            MOUSE_WIDTH * 2.0F + GAP,
                            KEY,
                            "SPACE",
                            inputState.keyPressed(LegacyKeyboardCodes.SPACE));
                }
                if (showShift.get().booleanValue()) {
                    drawKey(
                            left,
                            top + (KEY + GAP)
                                    * (showSpace.get().booleanValue() ? 4.0F : 3.0F),
                            MOUSE_WIDTH * 2.0F + GAP,
                            KEY,
                            "SHIFT",
                            inputState.keyPressed(LegacyKeyboardCodes.LEFT_SHIFT)
                                    || inputState.keyPressed(LegacyKeyboardCodes.RIGHT_SHIFT));
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

        private void drawKey(
                final float left,
                final float top,
                final float width,
                final float height,
                final String label,
                final boolean pressed) {
            hostCallbacks.fillRoundedRect(
                    left,
                    top,
                    width,
                    height,
                    3.0F,
                    pressed
                            ? PRESSED_ARGB
                            : IDLE_ARGB);
            hostCallbacks.drawText(
                    UiFonts.DEFAULT,
                    left + 4.0F,
                    top + 6.0F,
                    label,
                    pressed
                            ? PRESSED_TEXT_ARGB
                            : IDLE_TEXT_ARGB);
        }
    }
}
