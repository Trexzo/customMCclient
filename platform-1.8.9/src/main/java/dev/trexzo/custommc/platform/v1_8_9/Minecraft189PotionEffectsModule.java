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
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Locale;
import java.util.Objects;

public final class Minecraft189PotionEffectsModule
        implements Module {
    public static final String ID =
            "render.potionEffects";
    public static final String X_SETTING_ID =
            "render.potionEffects.x";
    public static final String Y_SETTING_ID =
            "render.potionEffects.y";
    public static final String RENDER_PASS_ID =
            "potion-effects";

    private static final int PRIORITY = 131;
    private static final int TEXT_ARGB = 0xFFFFFFFF;
    private static final float LINE_HEIGHT = 12.0F;

    private final Minecraft189PlayerPotionEffectsState potionEffectsState;
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
                    244,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189PotionEffectsModule(
            final Minecraft189PlayerPotionEffectsState potionEffectsState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.potionEffectsState =
                Objects.requireNonNull(
                        potionEffectsState,
                        "potionEffectsState");
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

    @Override
    public synchronized void onEnable() {
        if (renderRegistration != null) {
            throw new IllegalStateException(
                    "potion-effects render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new PotionEffectsRenderPass());
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

    static String textFor(
            final Minecraft189PlayerPotionEffectsState.Snapshot effect) {
        Objects.requireNonNull(
                effect,
                "effect");
        final int totalSeconds =
                effect.durationTicks() / 20;
        return String.format(
                Locale.ROOT,
                "%s Lv %d %d:%02d",
                effect.effectName(),
                effect.amplifier() + 1,
                totalSeconds / 60,
                totalSeconds % 60);
    }

    private final class PotionEffectsRenderPass
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
            final Minecraft189PlayerPotionEffectsState.StateSnapshot state =
                    potionEffectsState.snapshot();
            if (!state.available()
                    || state.effects().isEmpty()) {
                return;
            }

            final UiViewport viewport =
                    new UiViewport(
                            hostCallbacks.framebufferWidth(),
                            hostCallbacks.framebufferHeight(),
                            hostCallbacks.uiScale());

            hostCallbacks.beginUi(viewport);
            RuntimeException failure = null;
            try {
                int index = 0;
                for (Minecraft189PlayerPotionEffectsState.Snapshot effect
                        : state.effects()) {
                    hostCallbacks.drawText(
                            UiFonts.DEFAULT,
                            x.get().floatValue(),
                            y.get().floatValue()
                                    + (index * LINE_HEIGHT),
                            textFor(
                                    effect),
                            TEXT_ARGB);
                    index++;
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
