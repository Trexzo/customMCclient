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

/** Lifecycle owner for conservative Combat Kill Aura mapping. */
final class Minecraft189KillAuraFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189KillAuraModule module = new Minecraft189KillAuraModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189KillAuraFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189KillAuraFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189KillAuraFeature f = new Minecraft189KillAuraFeature(controller);
        try {
            f.owned.add(modules.register(f.module));
            f.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189KillAuraModule.ID, "Kill Aura",
                    "Tracks nearest mapped player and attacks only on vanilla crosshair hits.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 70)));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.minCpsSetting(), "Minimum CPS", SettingValueKind.INTEGER,
                    0, new SettingNumericSpec(1, 20, 1));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.maxCpsSetting(), "Maximum CPS", SettingValueKind.INTEGER,
                    10, new SettingNumericSpec(1, 20, 1));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.rangeSetting(), "Target Range", SettingValueKind.DOUBLE,
                    20, new SettingNumericSpec(0.5D, 6.0D, 0.1D));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.fovSetting(), "Maximum FOV", SettingValueKind.DOUBLE,
                    30, new SettingNumericSpec(1, 180, 1));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.angularStepSetting(), "Angular Step", SettingValueKind.DOUBLE,
                    40, new SettingNumericSpec(0.5D, 180.0D, 0.5D));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.requireHoldSetting(), "Require Attack Held",
                    SettingValueKind.BOOLEAN, 50, null);
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.pauseSneakSetting(), "Pause While Sneaking",
                    SettingValueKind.BOOLEAN, 60, null);
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.pauseRightSetting(), "Pause While Right-Clicking",
                    SettingValueKind.BOOLEAN, 70, null);
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.prioritizeCrosshairSetting(), "Prioritize Crosshair",
                    SettingValueKind.BOOLEAN, 80, null);
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.switchHurtTargetsSetting(), "Switch Hurt Targets",
                    SettingValueKind.BOOLEAN, 90, null);
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.maxSwitchHurtTicksSetting(), "Max Target Hurt Ticks",
                    SettingValueKind.INTEGER, 100, new SettingNumericSpec(0, 20, 1));
            return f;
        } catch (RuntimeException ex) {
            try { f.close(); }
            catch (RuntimeException cleanup) { ex.addSuppressed(cleanup); }
            throw ex;
        }
    }

    private void bind(final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry presentations,
            final Setting<?> setting, final String name,
            final SettingValueKind kind, final int order,
            final SettingNumericSpec numeric) {
        owned.add(settings.register(setting));
        owned.add(presentations.register(numeric == null
                ? new SettingDescriptor(setting.id(), name, kind, order)
                : new SettingDescriptor(setting.id(), name, kind, order, numeric)));
        owned.add(moduleSettings.register(new ModuleSettingBinding(
                module.id(), setting.id(), order)));
    }

    Minecraft189KillAuraModule module() {
        if (closed) throw new IllegalStateException("Kill Aura feature closed");
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
            catch (Exception ex) {
                RuntimeException next = new IllegalStateException("Kill Aura cleanup", ex);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
