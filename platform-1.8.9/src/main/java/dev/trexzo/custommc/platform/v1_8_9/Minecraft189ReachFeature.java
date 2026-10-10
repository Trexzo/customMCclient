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

/** Lifecycle and profile-owned settings for client-side raycast Reach. */
final class Minecraft189ReachFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189ReachModule module = new Minecraft189ReachModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189ReachFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189ReachFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189ReachFeature feature = new Minecraft189ReachFeature(controller);
        try {
            feature.owned.add(modules.register(feature.module));
            feature.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189ReachModule.ID, "Reach",
                    "Override local 1.8.9 raycast picking distance (3-6 blocks). "
                            + "Server acceptance not guaranteed.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 114)));
            feature.owned.add(settings.register(feature.module.distanceSetting()));
            feature.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189ReachModule.DISTANCE, "Raycast Reach",
                    SettingValueKind.DOUBLE, 0,
                    new SettingNumericSpec(3.0, 6.0, 0.1))));
            feature.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189ReachModule.ID, Minecraft189ReachModule.DISTANCE, 0)));
            return feature;
        } catch (RuntimeException failure) {
            try { feature.close(); } catch (RuntimeException cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    Minecraft189ReachModule module() {
        if (closed) throw new IllegalStateException("Reach feature closed");
        return module;
    }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        try {
            if (controller.stateOf(module.id()) != ModuleState.DISABLED)
                controller.disable(module.id());
        } catch (RuntimeException error) { failure = error; }
        for (int i = owned.size() - 1; i >= 0; i--) {
            try { owned.get(i).close(); } catch (Exception error) {
                final RuntimeException next =
                        new IllegalStateException("Reach cleanup", error);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
