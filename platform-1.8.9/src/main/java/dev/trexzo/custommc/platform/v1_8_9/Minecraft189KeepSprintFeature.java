package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import java.util.ArrayList;
import java.util.List;

final class Minecraft189KeepSprintFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189KeepSprintModule module = new Minecraft189KeepSprintModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189KeepSprintFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189KeepSprintFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189KeepSprintFeature feature =
                new Minecraft189KeepSprintFeature(controller);
        try {
            feature.owned.add(modules.register(feature.module));
            feature.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189KeepSprintModule.ID,
                    "Keep Sprint",
                    "Preserve mapped sprint state after synthetic player attacks.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 85)));
            feature.bind(moduleSettings, settings, settingPresentations,
                    feature.module.requireForwardSetting(), "Require Forward", 0);
            feature.bind(moduleSettings, settings, settingPresentations,
                    feature.module.requirePlayerHitSetting(), "Require Player Hit", 10);
            feature.bind(moduleSettings, settings, settingPresentations,
                    feature.module.pauseWhileSneakingSetting(), "Pause While Sneaking", 20);
            return feature;
        } catch (RuntimeException ex) {
            try { feature.close(); }
            catch (RuntimeException cleanup) { ex.addSuppressed(cleanup); }
            throw ex;
        }
    }

    private void bind(
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry presentations,
            final Setting<Boolean> setting, final String name, final int priority) {
        owned.add(settings.register(setting));
        owned.add(presentations.register(new SettingDescriptor(
                setting.id(), name, SettingValueKind.BOOLEAN, priority)));
        owned.add(moduleSettings.register(
                new ModuleSettingBinding(module.id(), setting.id(), priority)));
    }

    Minecraft189KeepSprintModule module() {
        if (closed) throw new IllegalStateException("KeepSprint feature closed");
        return module;
    }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        try {
            if (controller.stateOf(module.id()) != ModuleState.DISABLED)
                controller.disable(module.id());
        } catch (RuntimeException e) { failure = e; }
        for (int i = owned.size()-1; i >= 0; i--) {
            try { owned.get(i).close(); }
            catch (Exception e) {
                RuntimeException next = new IllegalStateException("KeepSprint close", e);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
