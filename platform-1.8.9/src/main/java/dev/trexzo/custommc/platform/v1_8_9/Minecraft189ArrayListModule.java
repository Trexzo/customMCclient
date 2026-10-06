package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
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

public final class Minecraft189ArrayListModule
        implements Module {
    public static final String ID =
            "render.array-list";
    public static final String X_SETTING_ID =
            "render.array-list.x";
    public static final String Y_SETTING_ID =
            "render.array-list.y";
    public static final String RENDER_PASS_ID =
            "array-list";

    private static final int PRIORITY = 110;
    private static final int TEXT_ARGB = 0xFFFFFFFF;
    private static final float LINE_HEIGHT = 12.0F;

    private final ModuleRegistry modules;
    private final ModuleController controller;
    private final ModulePresentationRegistry presentations;
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
                    24,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189ArrayListModule(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.modules = Objects.requireNonNull(
                modules,
                "modules");
        this.controller = Objects.requireNonNull(
                controller,
                "controller");
        this.presentations = Objects.requireNonNull(
                presentations,
                "presentations");
        this.renderPipeline = Objects.requireNonNull(
                renderPipeline,
                "renderPipeline");
        this.hostCallbacks = Objects.requireNonNull(
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
                    "array-list render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new ArrayListRenderPass());
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

    private final class ArrayListRenderPass
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
                float currentY =
                        y.get().floatValue();
                for (Module module :
                        modules.snapshot()) {
                    if (controller.stateOf(
                            module.id())
                            != ModuleState.ENABLED) {
                        continue;
                    }

                    final ModuleDescriptor descriptor =
                            presentations.find(
                                    module.id());
                    final String displayName =
                            descriptor == null
                                    ? module.id()
                                    : descriptor.displayName();

                    hostCallbacks.drawText(
                            UiFonts.DEFAULT,
                            x.get().floatValue(),
                            currentY,
                            displayName,
                            TEXT_ARGB);
                    currentY += LINE_HEIGHT;
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
