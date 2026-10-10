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
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import java.util.ArrayList;
import java.util.List;

final class Minecraft189AutoPotFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AutoPotModule module = new Minecraft189AutoPotModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189AutoPotFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189AutoPotFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AutoPotFeature f = new Minecraft189AutoPotFeature(controller);
        try {
            f.owned.add(modules.register(f.module));
            f.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189AutoPotModule.ID, "Auto Pot (Configured Slot)",
                    "Attempts a healing item use below a mapped health threshold.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 105)));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.potSlotSetting(), "Potion Hotbar Slot",
                    SettingValueKind.INTEGER, 0, new SettingNumericSpec(1, 9, 1));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.healthThresholdSetting(), "Health Threshold (%)",
                    SettingValueKind.DOUBLE, 10, new SettingNumericSpec(1, 99, 1));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.cooldownTicksSetting(), "Cooldown Ticks",
                    SettingValueKind.INTEGER, 20, new SettingNumericSpec(1, 1200, 1));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.requireCombatTargetSetting(), "Require Combat Target",
                    SettingValueKind.BOOLEAN, 30, null);
            return f;
        } catch (RuntimeException ex) {
            try { f.close(); } catch (RuntimeException cleanup) { ex.addSuppressed(cleanup); }
            throw ex;
        }
    }

    private void bind(final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Setting<?> setting, final String name,
            final SettingValueKind kind, final int order,
            final SettingNumericSpec numeric) {
        owned.add(settings.register(setting));
        owned.add(settingPresentations.register(numeric == null
                ? new SettingDescriptor(setting.id(), name, kind, order)
                : new SettingDescriptor(setting.id(), name, kind, order, numeric)));
        owned.add(moduleSettings.register(
                new ModuleSettingBinding(module.id(), setting.id(), order)));
    }

    Minecraft189AutoPotModule module() {
        if (closed) throw new IllegalStateException("AutoPot feature closed");
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
                RuntimeException next = new IllegalStateException("AutoPot cleanup", e);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
