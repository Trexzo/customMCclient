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
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiTheme;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.UiThemeSettings;
import dev.trexzo.custommc.core.ui.UiColorRole;
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
    public static final String MAX_VISIBLE_SETTING_ID =
            "render.array-list.maxVisible";
    public static final String SHOW_OVERFLOW_SETTING_ID =
            "render.array-list.showOverflow";
    public static final int DEFAULT_MAX_VISIBLE = 128;
    public static final String RIGHT_ALIGNED_SETTING_ID =
            "render.array-list.rightAligned";
    // Intrinsic-size chips replace S12's identical 184px rectangles.
    // Glyph widths are conservative approximations of the native 1.8.9 font:
    // the host currently exposes drawString but not getStringWidth.
    private static final float CHIP_HEIGHT = 11.0F;
 
    private static final int PRIORITY = 110;
    private static final int TEXT_ARGB = 0xFFFFFFFF;
    private static final float LINE_HEIGHT = 12.0F;

    private final ModuleRegistry modules;
    private final ModuleController controller;
    private final ModulePresentationRegistry presentations;
    private final RenderPipeline renderPipeline;
    private final LegacyUiHostCallbacks hostCallbacks;
    private final SettingRegistry themeSettings;
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
    private final Setting<Integer> maxVisible = new Setting<Integer>(
            MAX_VISIBLE_SETTING_ID, DEFAULT_MAX_VISIBLE,
            value -> value != null && value >= 1 && value <= 128,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> rightAligned = new Setting<Boolean>(
            RIGHT_ALIGNED_SETTING_ID, Boolean.TRUE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> showOverflow = new Setting<Boolean>(
            SHOW_OVERFLOW_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189ArrayListModule(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this(modules, controller, presentations, renderPipeline,
                hostCallbacks, null);
    }

    public Minecraft189ArrayListModule(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks,
            final SettingRegistry themeSettings) {
        this.themeSettings = themeSettings;
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
    public Setting<Integer> maxVisibleSetting() { return maxVisible; }
    public Setting<Boolean> showOverflowSetting() { return showOverflow; }
    public Setting<Boolean> rightAlignedSetting() { return rightAligned; }

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

    static float rightAlignedTextX(final float screenWidth,
            final float inset, final String label) {
        return Math.max(4.0F, screenWidth - inset
                - 8.0F - Minecraft189HudTextMetrics.width(label));
    }

    private void drawModuleCard(final UiViewport viewport,
            final float currentY, final String label) {
        final boolean right = rightAligned.get().booleanValue();
        if (!right) {
            // Existing explicitly left-aligned saved profiles remain intact.
            hostCallbacks.drawText(UiFonts.DEFAULT, x.get().floatValue(),
                    currentY, label, TEXT_ARGB);
            return;
        }
        final float edge = Math.min(viewport.logicalWidth(),
                Math.max(0.0F, viewport.logicalWidth() - x.get().floatValue()));
        final float textX = rightAlignedTextX(viewport.logicalWidth(),
                x.get().floatValue(), label);
        final float chipX = Math.max(0.0F, textX - 4.0F);
        final float chipWidth = Math.max(0.0F, edge - chipX);
        final UiTheme theme = themeSettings == null
                ? UiThemes.graphite()
                : UiThemeSettings.resolve(themeSettings, UiThemes.graphite());
        // Restraint matters: a translucent chip hugs the measured-length
        // estimate instead of filling a huge equal-width rectangle.
        if (chipWidth >= 2.0F) {
            hostCallbacks.fillRoundedRect(chipX, currentY - 1.0F,
                    chipWidth, CHIP_HEIGHT, Math.min(2.0F, chipWidth * .5F),
                    0x50000000 | (theme.color(UiColorRole.SURFACE) & 0xFFFFFF));
        }
        if (edge >= 2.0F) {
            hostCallbacks.fillRect(edge - 2.0F, currentY, 2.0F, 9.0F,
                    theme.color(UiColorRole.ACCENT));
        }
        final float clipWidth = Math.max(0.0F, edge - textX - 5.0F);
        if (clipWidth > 0.0F) {
            hostCallbacks.pushClip(textX, currentY - 1.0F,
                    clipWidth, CHIP_HEIGHT);
            try {
                hostCallbacks.drawText(UiFonts.DEFAULT,
                        textX, currentY, label,
                        theme.color(UiColorRole.TEXT_PRIMARY));
            } finally {
                hostCallbacks.popClip();
            }
        }
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
                final boolean withCategory = showCategories.get().booleanValue();
                final boolean grouped = groupCategories.get().booleanValue();
                // Stable sort: choose category first only when explicitly
                // grouped, then descending intrinsic text-width estimate.
                Collections.sort(enabled, new Comparator<Module>() {
                    @Override
                    public int compare(final Module left, final Module right) {
                        if (grouped) {
                            final int category = Integer.compare(
                                    categoryOrder(presentations.find(left.id())),
                                    categoryOrder(presentations.find(right.id())));
                            if (category != 0) return category;
                        }
                        return Integer.compare(
                                Minecraft189HudTextMetrics.width(moduleLabel(
                                        right.id(), presentations.find(right.id()), withCategory)),
                                Minecraft189HudTextMetrics.width(moduleLabel(
                                        left.id(), presentations.find(left.id()), withCategory)));
                    }
                });
                final int displayed = Math.min(enabled.size(), maxVisible.get().intValue());
                for (int index = 0; index < displayed; index++) {
                    final Module module = enabled.get(index);
                    final ModuleDescriptor descriptor = presentations.find(module.id());
                    drawModuleCard(viewport, currentY,
                            moduleLabel(module.id(), descriptor, withCategory));
                    currentY += LINE_HEIGHT;
                }
                // No fabricated modules: the overflow count is computed
                // solely from the enabled module registry snapshot.
                if (showOverflow.get().booleanValue() && enabled.size() > displayed) {
                    drawModuleCard(viewport, currentY,
                            "+ " + (enabled.size() - displayed) + " more");
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
