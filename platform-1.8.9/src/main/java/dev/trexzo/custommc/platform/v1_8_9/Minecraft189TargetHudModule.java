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

public final class Minecraft189TargetHudModule implements Module {
    public static final String ID = "render.targetHud";
    public static final String X_SETTING_ID = "render.targetHud.x";
    public static final String Y_SETTING_ID = "render.targetHud.y";
    public static final String RENDER_PASS_ID = "target-hud";
    private final Minecraft189NearestPlayerTargetState nearest;
    private final Minecraft189TargetRotationState direction;
    private final RenderPipeline pipeline;
    private final LegacyUiHostCallbacks graphics;
    private final Setting<Integer> x = new Setting<Integer>(
            X_SETTING_ID, 18, v -> v != null && v >= 0 && v <= 4096, SettingCodecs.INTEGER);
    private final Setting<Integer> y = new Setting<Integer>(
            Y_SETTING_ID, 210, v -> v != null && v >= 0 && v <= 4096, SettingCodecs.INTEGER);
    private RenderPipeline.Registration registration;

    public Minecraft189TargetHudModule(
            final Minecraft189NearestPlayerTargetState nearest,
            final Minecraft189TargetRotationState direction,
            final RenderPipeline pipeline,
            final LegacyUiHostCallbacks graphics) {
        this.nearest = Objects.requireNonNull(nearest, "nearest");
        this.direction = Objects.requireNonNull(direction, "direction");
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
        this.graphics = Objects.requireNonNull(graphics, "graphics");
    }

    @Override public String id() { return ID; }
    public Setting<Integer> xSetting() { return x; }
    public Setting<Integer> ySetting() { return y; }

    @Override public synchronized void onEnable() {
        if (registration != null) {
            throw new IllegalStateException("Target HUD pass already installed");
        }
        registration = pipeline.register(new TargetHudPass());
    }

    @Override public synchronized void onDisable() {
        if (registration != null) {
            registration.close();
            registration = null;
        }
    }

    synchronized boolean renderPassInstalled() {
        return registration != null && registration.active();
    }

    static String distanceText(final Minecraft189NearestPlayerTargetState.Snapshot target) {
        return String.format(Locale.ROOT, "DIST  %.1fm", target.distance());
    }

    static String directionText(final Minecraft189TargetRotationState.Snapshot rotation) {
        // Avoid displaying -0 degrees when the exact target angles
        // carry IEEE-754 negative zero from atan2.
        return String.format(Locale.ROOT, "YAW  %d°    PITCH  %d°",
                Math.round(rotation.yaw()), Math.round(rotation.pitch()));
    }

    private final class TargetHudPass implements RenderPass {
        @Override public String id() { return RENDER_PASS_ID; }
        @Override public RenderStage stage() { return RenderStage.HUD; }
        @Override public int priority() { return 125; }

        @Override public void render(final RenderFrame frame) {
            Objects.requireNonNull(frame, "frame");
            final Minecraft189NearestPlayerTargetState.Snapshot target = nearest.snapshot();
            final Minecraft189TargetRotationState.Snapshot angle = direction.snapshot();
            if (!target.available() || !target.found() || !angle.available()) {
                return;
            }
            final UiViewport viewport = new UiViewport(
                    graphics.framebufferWidth(), graphics.framebufferHeight(), graphics.uiScale());
            final float left = x.get().floatValue();
            final float top = y.get().floatValue();
            graphics.beginUi(viewport);
            RuntimeException failure = null;
            try {
                graphics.fillRoundedRect(left, top, 164.0F, 56.0F, 6.0F, 0xE610141E);
                graphics.fillRoundedRect(left, top, 3.0F, 56.0F, 1.5F, 0xFF70C9E8);
                graphics.drawText(UiFonts.DEFAULT, left + 12, top + 7,
                        "TARGET  #" + (target.entityIndex() + 1), 0xFFFFFFFF);
                graphics.drawText(UiFonts.DEFAULT, left + 12, top + 23,
                        distanceText(target), 0xFFC4CBD5);
                graphics.drawText(UiFonts.DEFAULT, left + 12, top + 39,
                        directionText(angle), 0xFF92A8BF);
            } catch (RuntimeException e) {
                failure = e;
                throw e;
            } finally {
                try {
                    graphics.endUi();
                } catch (RuntimeException e) {
                    if (failure != null) failure.addSuppressed(e);
                    else throw e;
                }
            }
        }
    }
}
