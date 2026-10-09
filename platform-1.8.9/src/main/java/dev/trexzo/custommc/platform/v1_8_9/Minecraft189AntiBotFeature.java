package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import java.util.ArrayList;
import java.util.List;

final class Minecraft189AntiBotFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AntiBotModule module = new Minecraft189AntiBotModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189AntiBotFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189AntiBotFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AntiBotFeature feature = new Minecraft189AntiBotFeature(controller);
        try {
            feature.owned.add(modules.register(feature.module));
            feature.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189AntiBotModule.ID, "AntiBot",
                    "Optional verified network-player tab evidence filter.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 95)));
            feature.owned.add(settings.register(feature.module.allowUnknownSetting()));
            feature.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189AntiBotModule.ALLOW_UNKNOWN, "Allow Unknown Player Info",
                    SettingValueKind.BOOLEAN, 0)));
            feature.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189AntiBotModule.ID, Minecraft189AntiBotModule.ALLOW_UNKNOWN, 0)));
            return feature;
        } catch (RuntimeException error) {
            try { feature.close(); } catch (RuntimeException clean) { error.addSuppressed(clean); }
            throw error;
        }
    }

    Minecraft189AntiBotModule module() {
        if (closed) throw new IllegalStateException("AntiBot feature closed");
        return module;
    }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        try {
            if (controller.stateOf(module.id()) != ModuleState.DISABLED)
                controller.disable(module.id());
        } catch (RuntimeException ex) { failure = ex; }
        for (int i = owned.size() - 1; i >= 0; i--) {
            try { owned.get(i).close(); }
            catch (Exception ex) {
                RuntimeException next = new IllegalStateException("AntiBot cleanup", ex);
                if (failure == null) failure = next; else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
