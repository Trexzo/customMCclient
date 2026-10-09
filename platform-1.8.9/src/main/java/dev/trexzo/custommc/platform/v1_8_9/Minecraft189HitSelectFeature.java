package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import java.util.ArrayList;
import java.util.List;

/** Separate lifecycle and settings owner for Combat Hit Select. */
final class Minecraft189HitSelectFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189HitSelectModule module =
            new Minecraft189HitSelectModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189HitSelectFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189HitSelectFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189HitSelectFeature feature =
                new Minecraft189HitSelectFeature(controller);
        try {
            feature.owned.add(modules.register(feature.module));
            feature.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189HitSelectModule.ID,
                    "Hit Select",
                    "Suppresses synthetic swings into a target's hurt-immunity window.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 75)));
            feature.owned.add(settings.register(
                    feature.module.maxTargetHurtTicksSetting()));
            feature.owned.add(settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HitSelectModule.MAX_HURT_TICKS_SETTING_ID,
                            "Maximum Target Hurt Ticks",
                            SettingValueKind.INTEGER, 0,
                            new SettingNumericSpec(
                                    0, Minecraft189HitSelectModule.MAXIMUM_HURT_TICKS, 1))));
            feature.owned.add(moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HitSelectModule.ID,
                            Minecraft189HitSelectModule.MAX_HURT_TICKS_SETTING_ID, 0)));
            return feature;
        } catch (RuntimeException ex) {
            try { feature.close(); }
            catch (RuntimeException cleanup) { ex.addSuppressed(cleanup); }
            throw ex;
        }
    }

    Minecraft189HitSelectModule module() {
        if (closed) throw new IllegalStateException("Hit Select feature closed");
        return module;
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        try {
            if (controller.stateOf(module.id()) != ModuleState.DISABLED)
                controller.disable(module.id());
        } catch (RuntimeException e) { failure = e; }
        for (int i = owned.size() - 1; i >= 0; i--) {
            try { owned.get(i).close(); }
            catch (Exception e) {
                final RuntimeException next =
                        new IllegalStateException("Hit Select cleanup", e);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
