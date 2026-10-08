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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class Minecraft189ArrayListModule
        implements Module {
    public static final String ID =
            "render.array-list";
    public static final String X_SETTING_ID =
            "render.array-list.x";
    public static final String Y_SETTING_ID =
            "render.array-list.y";
    public static final String SHOW_CATEGORIES_SETTING_ID =
            "render.array-list.showCategories";
    public static final String GROUP_CATEGORIES_SETTING_ID =
            "render.array-list.groupCategories";
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
    private final Setting<Boolean> showCategories =
            new Setting<Boolean>(SHOW_CATEGORIES_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> groupCategories =
            new Setting<Boolean>(GROUP_CATEGORIES_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
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

    public Setting<Boolean> showCategoriesSetting() { return showCategories; }
    public Setting<Boolean> groupCategoriesSetting() { return groupCategories; }

    static int categoryOrder(final ModuleDescriptor descriptor) {
        final String id = descriptor == null
                ? ModuleDescriptor.DEFAULT_CATEGORY_ID : descriptor.categoryId();
        if (Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID.equals(id)) return 0;
        if (Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID.equals(id)) return 1;
        if (Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID.equals(id)) return 2;
        if (Minecraft189FeatureCatalog.VISUALS_CATEGORY_ID.equals(id)) return 3;
        return 4;
    }

    static String moduleLabel(final String moduleId,
            final ModuleDescriptor descriptor, final boolean withCategory) {
        final String name = descriptor == null ? moduleId : descriptor.displayName();
        if (!withCategory) return name;
        final String[] categories = {"Combat", "Movement", "Player", "Visuals", "Other"};
        return "[" + categories[categoryOrder(descriptor)] + "] " + name;
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
                final List<Module> enabled = new ArrayList<Module>();
                for (Module module : modules.snapshot()) {
                    if (controller.stateOf(module.id()) == ModuleState.ENABLED) {
                        enabled.add(module);
                    }
                }
                if (groupCategories.get().booleanValue()) {
                    // Java's stable list sort preserves order within each category.
                    Collections.sort(enabled, new Comparator<Module>() {
                        @Override
                        public int compare(final Module left, final Module right) {
                            return Integer.compare(
                                    categoryOrder(presentations.find(left.id())),
                                    categoryOrder(presentations.find(right.id())));
                        }
                    });
                }
                final boolean withCategory = showCategories.get().booleanValue();
                for (Module module : enabled) {
                    final ModuleDescriptor descriptor = presentations.find(module.id());
                    hostCallbacks.drawText(UiFonts.DEFAULT, x.get().floatValue(),
                            currentY, moduleLabel(module.id(), descriptor, withCategory),
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
