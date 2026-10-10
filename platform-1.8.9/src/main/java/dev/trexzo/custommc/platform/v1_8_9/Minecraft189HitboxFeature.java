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

/** Owns the optional player-only ray-hitbox border module. */
final class Minecraft189HitboxFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189HitboxModule module = new Minecraft189HitboxModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189HitboxFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189HitboxFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189HitboxFeature feature = new Minecraft189HitboxFeature(controller);
        try {
            feature.owned.add(modules.register(feature.module));
            feature.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189HitboxModule.ID, "Hitbox",
                    "Adjust only player collision borders during local ray picking. "
                            + "Does not expand server hitboxes.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 113)));
            feature.owned.add(settings.register(feature.module.extraBorderSetting()));
            feature.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189HitboxModule.EXTRA_BORDER, "Extra Hitbox Border",
                    SettingValueKind.DOUBLE, 0,
                    new SettingNumericSpec(0.0, 0.6, 0.05))));
            feature.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189HitboxModule.ID,
                    Minecraft189HitboxModule.EXTRA_BORDER, 0)));
            return feature;
        } catch (RuntimeException failure) {
            try { feature.close(); } catch (RuntimeException cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    Minecraft189HitboxModule module() {
        if (closed) throw new IllegalStateException("Hitbox feature closed");
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
                        new IllegalStateException("Hitbox cleanup", error);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
