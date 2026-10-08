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

import java.util.Objects;

public final class Minecraft189CpsModule
        implements Module {
    public static final String ID =
            "render.cps";
    public static final String X_SETTING_ID =
            "render.cps.x";
    public static final String Y_SETTING_ID =
            "render.cps.y";
    public static final String COMPACT_SETTING_ID =
            "render.cps.compact";
    public static final String SHOW_TOTAL_SETTING_ID =
            "render.cps.showTotal";
    public static final String RENDER_PASS_ID =
            "cps";

    private static final int PRIORITY = 117;
    private static final int TEXT_ARGB = 0xFFFFFFFF;

    private final Minecraft189ClickRateTracker clickRateTracker;
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
                    132,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> compact =
            new Setting<Boolean>(
                    COMPACT_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> showTotal =
            new Setting<Boolean>(
                    SHOW_TOTAL_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189CpsModule(
            final Minecraft189ClickRateTracker clickRateTracker,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.clickRateTracker =
                Objects.requireNonNull(
                        clickRateTracker,
                        "clickRateTracker");
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

    public Setting<Boolean> compactSetting() {
        return compact;
    }

    public Setting<Boolean> showTotalSetting() {
        return showTotal;
    }

    static String textFor(
            final int left,
            final int right,
            final boolean useCompact,
            final boolean includeTotal) {
        // CPS snapshots already contain rolling left/right counts; these
        // options change only their local text presentation.
        final String prefix = useCompact
                ? "CPS: L" + left + " R" + right
                : "CPS: L " + left + " | R " + right;
        if (!includeTotal) {
            return prefix;
        }
        final long total = (long) left + (long) right;
        return prefix + (useCompact ? " T" : " | T ") + total;
    }

    @Override
    public synchronized void onEnable() {
        if (renderRegistration != null) {
            throw new IllegalStateException(
                    "cps render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new CpsRenderPass());
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

    private final class CpsRenderPass
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

            hostCallbacks.beginUi(viewport);
            RuntimeException failure = null;
            try {
                final Minecraft189ClickRateTracker.Rates rates =
                        clickRateTracker.snapshot();
                hostCallbacks.drawText(
                        UiFonts.DEFAULT,
                        x.get().floatValue(),
                        y.get().floatValue(),
                        textFor(rates.left(), rates.right(),
                                compact.get().booleanValue(),
                                showTotal.get().booleanValue()),
                        TEXT_ARGB);
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
