package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import java.util.ArrayList;
import java.util.List;

final class Minecraft189TriggerBotFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189TriggerBotModule module = new Minecraft189TriggerBotModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189TriggerBotFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189TriggerBotFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189TriggerBotFeature feature = new Minecraft189TriggerBotFeature(controller);
        try {
            feature.owned.add(modules.register(feature.module));
            feature.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189TriggerBotModule.ID, "Trigger Bot",
                    "Clicks only on confirmed vanilla crosshair hits against players.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 65)));

            feature.bind(moduleSettings, settings, settingPresentations,
                    feature.module.minCpsSetting(), "Minimum CPS",
                    SettingValueKind.INTEGER, 0, new SettingNumericSpec(1, 20, 1));
            feature.bind(moduleSettings, settings, settingPresentations,
                    feature.module.maxCpsSetting(), "Maximum CPS",
                    SettingValueKind.INTEGER, 10, new SettingNumericSpec(1, 20, 1));
            feature.bind(moduleSettings, settings, settingPresentations,
                    feature.module.confirmTicksSetting(), "Crosshair Confirm Ticks",
                    SettingValueKind.INTEGER, 20, new SettingNumericSpec(1, 10, 1));
            feature.bind(moduleSettings, settings, settingPresentations,
                    feature.module.requireHoldSetting(), "Require Attack Held",
                    SettingValueKind.BOOLEAN, 30, null);
            feature.bind(moduleSettings, settings, settingPresentations,
                    feature.module.pauseRightSetting(), "Pause While Right-Clicking",
                    SettingValueKind.BOOLEAN, 40, null);
            feature.bind(moduleSettings, settings, settingPresentations,
                    feature.module.pauseSneakSetting(), "Pause While Sneaking",
                    SettingValueKind.BOOLEAN, 50, null);
            return feature;
        } catch (RuntimeException ex) {
            try { feature.close(); }
            catch (RuntimeException cleanup) { ex.addSuppressed(cleanup); }
            throw ex;
        }
    }

    private void bind(final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Setting<?> setting, final String name,
            final SettingValueKind kind, final int priority,
            final SettingNumericSpec spec) {
        owned.add(settings.register(setting));
        owned.add(settingPresentations.register(spec == null
                ? new SettingDescriptor(setting.id(), name, kind, priority)
                : new SettingDescriptor(setting.id(), name, kind, priority, spec)));
        owned.add(moduleSettings.register(new ModuleSettingBinding(
                module.id(), setting.id(), priority)));
    }

    Minecraft189TriggerBotModule module() {
        if (closed) throw new IllegalStateException("Trigger Bot feature closed");
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
                final RuntimeException next = new IllegalStateException("Trigger Bot cleanup", e);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
