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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    public static final String SORT_BY_EXPIRY_SETTING_ID =
            "render.potionEffects.sortByExpiry";
    public static final String EXPIRY_ALERT_SETTING_ID =
            "render.potionEffects.expiryAlert";
    public static final String EXPIRY_THRESHOLD_SETTING_ID =
            "render.potionEffects.expiryThresholdSeconds";
    public static final int DEFAULT_EXPIRY_THRESHOLD_SECONDS = 10;
    public static final int MAXIMUM_EXPIRY_THRESHOLD_SECONDS = 120;
    public static final String RENDER_PASS_ID =
            "potion-effects";

    private static final int PRIORITY = 131;
    private static final int TEXT_ARGB = 0xFFFFFFFF;
    private static final int EXPIRING_ARGB = 0xFFFFB65C;
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
    private final Setting<Boolean> sortByExpiry = new Setting<Boolean>(
            SORT_BY_EXPIRY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> expiryAlert = new Setting<Boolean>(
            EXPIRY_ALERT_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> expiryThresholdSeconds = new Setting<Integer>(
            EXPIRY_THRESHOLD_SETTING_ID, DEFAULT_EXPIRY_THRESHOLD_SECONDS,
            value -> value != null && value >= 1
                    && value <= MAXIMUM_EXPIRY_THRESHOLD_SECONDS,
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

    public Setting<Boolean> sortByExpirySetting() {
        return sortByExpiry;
    }

    public Setting<Boolean> expiryAlertSetting() {
        return expiryAlert;
    }

    public Setting<Integer> expiryThresholdSecondsSetting() {
        return expiryThresholdSeconds;
    }

    static boolean expiring(
            final Minecraft189PlayerPotionEffectsState.Snapshot effect,
            final int thresholdSeconds) {
        Objects.requireNonNull(effect, "effect");
        // Compare source tick durations, not display-rounded seconds.
        // A multiplication is safe for the validated threshold 1..120.
        return effect.durationTicks() <= thresholdSeconds * 20;
    }

    static List<Minecraft189PlayerPotionEffectsState.Snapshot> displayedEffects(
            final Minecraft189PlayerPotionEffectsState.StateSnapshot state,
            final boolean orderByExpiry) {
        Objects.requireNonNull(state, "state");
        if (!orderByExpiry || state.effects().size() < 2) {
            return state.effects();
        }
        // The shared, immutable player-state snapshot remains in its
        // existing alphabetical order; sorting is local to this HUD only.
        final List<Minecraft189PlayerPotionEffectsState.Snapshot> ordered =
                new ArrayList<Minecraft189PlayerPotionEffectsState.Snapshot>(
                        state.effects());
        Collections.sort(ordered,
                new Comparator<Minecraft189PlayerPotionEffectsState.Snapshot>() {
                    @Override
                    public int compare(
                            final Minecraft189PlayerPotionEffectsState.Snapshot left,
                            final Minecraft189PlayerPotionEffectsState.Snapshot right) {
                        return Integer.compare(
                                left.durationTicks(), right.durationTicks());
                    }
                });
        // Java's stable sort preserves source name/ID ordering for ties.
        return ordered;
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
                        : displayedEffects(
                                state,
                                sortByExpiry.get().booleanValue())) {
                    hostCallbacks.drawText(
                            UiFonts.DEFAULT,
                            x.get().floatValue(),
                            y.get().floatValue()
                                    + (index * LINE_HEIGHT),
                            textFor(
                                    effect),
                            expiryAlert.get().booleanValue()
                                    && expiring(
                                            effect,
                                            expiryThresholdSeconds.get().intValue())
                                    ? EXPIRING_ARGB
                                    : TEXT_ARGB);
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
