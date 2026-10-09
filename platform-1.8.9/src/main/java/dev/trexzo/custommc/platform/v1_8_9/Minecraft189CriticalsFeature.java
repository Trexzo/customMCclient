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

final class Minecraft189CriticalsFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189CriticalsModule module = new Minecraft189CriticalsModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189CriticalsFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189CriticalsFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189CriticalsFeature f = new Minecraft189CriticalsFeature(controller);
        try {
            f.owned.add(modules.register(f.module));
            f.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189CriticalsModule.ID, "Criticals",
                    "Waits for mapped natural airborne/falling state before automatic attacks.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 80)));
            f.owned.add(settings.register(f.module.minimumFallDistanceSetting()));
            f.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189CriticalsModule.MIN_FALL_DISTANCE,
                    "Minimum Fall Distance", SettingValueKind.DOUBLE, 0,
                    new SettingNumericSpec(0.0D, 3.0D, 0.05D))));
            f.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    f.module.id(), Minecraft189CriticalsModule.MIN_FALL_DISTANCE, 0)));
            f.owned.add(settings.register(f.module.requireDescendingSetting()));
            f.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189CriticalsModule.REQUIRE_DESCENDING,
                    "Require Descending", SettingValueKind.BOOLEAN, 10)));
            f.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    f.module.id(), Minecraft189CriticalsModule.REQUIRE_DESCENDING, 10)));
            return f;
        } catch (RuntimeException ex) {
            try { f.close(); } catch (RuntimeException cleanup) { ex.addSuppressed(cleanup); }
            throw ex;
        }
    }

    Minecraft189CriticalsModule module() {
        if (closed) throw new IllegalStateException("Criticals feature closed");
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
        for (int i = owned.size()-1; i >= 0; i--) {
            try { owned.get(i).close(); }
            catch (Exception e) {
                RuntimeException failure = new IllegalStateException("Criticals close", e);
                if (error == null) error = failure; else error.addSuppressed(failure);
            }
        }
        owned.clear();
        if (error != null) throw error;
    }
}
