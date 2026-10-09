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

/** Owns all registered settings and lifecycle of Combat Jump Reset. */
final class Minecraft189JumpResetFeature implements AutoCloseable {
    private final Minecraft189JumpResetModule module = new Minecraft189JumpResetModule();
    private final ModuleController controller;
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189JumpResetFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189JumpResetFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189JumpResetFeature feature = new Minecraft189JumpResetFeature(controller);
        try {
            feature.owned.add(modules.register(feature.module));
            feature.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189JumpResetModule.ID, "Jump Reset",
                    "Jump on a fresh incoming hurt event when grounded.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 60)));

            feature.owned.add(settings.register(feature.module.cooldownSetting()));
            feature.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189JumpResetModule.COOLDOWN_ID, "Cooldown Ticks",
                    SettingValueKind.INTEGER, 0,
                    new SettingNumericSpec(0, Minecraft189JumpResetModule.MAX_COOLDOWN, 1))));
            feature.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189JumpResetModule.ID, Minecraft189JumpResetModule.COOLDOWN_ID, 0)));

            feature.owned.add(settings.register(feature.module.requireForwardSetting()));
            feature.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189JumpResetModule.FORWARD_ID, "Require Forward",
                    SettingValueKind.BOOLEAN, 10)));
            feature.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189JumpResetModule.ID, Minecraft189JumpResetModule.FORWARD_ID, 10)));

            feature.owned.add(settings.register(feature.module.pauseWhileSneakingSetting()));
            feature.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189JumpResetModule.SNEAK_ID, "Pause While Sneaking",
                    SettingValueKind.BOOLEAN, 20)));
            feature.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189JumpResetModule.ID, Minecraft189JumpResetModule.SNEAK_ID, 20)));
            return feature;
        } catch (RuntimeException ex) {
            try { feature.close(); }
            catch (RuntimeException cleanup) { ex.addSuppressed(cleanup); }
            throw ex;
        }
    }

    Minecraft189JumpResetModule module() {
        if (closed) throw new IllegalStateException("Jump Reset feature closed");
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
        } catch (RuntimeException e) {
            failure = e;
        }
        for (int i = owned.size() - 1; i >= 0; i--) {
            try { owned.get(i).close(); }
            catch (Exception e) {
                final RuntimeException next = new IllegalStateException("Jump Reset cleanup", e);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
