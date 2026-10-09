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

final class Minecraft189AutoBlockFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AutoBlockModule module = new Minecraft189AutoBlockModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189AutoBlockFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189AutoBlockFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AutoBlockFeature f = new Minecraft189AutoBlockFeature(controller);
        try {
            f.owned.add(modules.register(f.module));
            f.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189AutoBlockModule.ID, "Auto Block (Sword)",
                    "Vanilla sword-use after synthetic attack, with verified stop.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 110)));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.holdTicksSetting(), "Block Hold Ticks",
                    SettingValueKind.INTEGER, 0, new SettingNumericSpec(1, 20, 1));
            f.bind(moduleSettings, settings, settingPresentations,
                    f.module.pauseManualRightSetting(), "Pause on Manual Right",
                    SettingValueKind.BOOLEAN, 10, null);
            return f;
        } catch (RuntimeException e) {
            try { f.close(); }
            catch (RuntimeException failure) { e.addSuppressed(failure); }
            throw e;
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

    Minecraft189AutoBlockModule module() {
        if (closed) throw new IllegalStateException("AutoBlock feature closed");
        return module;
    }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        RuntimeException error = null;
        try {
            if (controller.stateOf(module.id()) != ModuleState.DISABLED)
                controller.disable(module.id());
        } catch (RuntimeException e) { error = e; }
        for (int i=owned.size()-1; i>=0; i--) {
            try { owned.get(i).close(); }
            catch (Exception e) {
                RuntimeException failure = new IllegalStateException("AutoBlock close", e);
                if (error == null) error = failure;
                else error.addSuppressed(failure);
            }
        }
        owned.clear();
        if (error != null) throw error;
    }
}
