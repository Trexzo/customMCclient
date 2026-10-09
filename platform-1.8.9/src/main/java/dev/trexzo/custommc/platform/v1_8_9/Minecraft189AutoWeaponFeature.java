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

final class Minecraft189AutoWeaponFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AutoWeaponModule module = new Minecraft189AutoWeaponModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189AutoWeaponFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189AutoWeaponFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AutoWeaponFeature f = new Minecraft189AutoWeaponFeature(controller);
        try {
            f.owned.add(modules.register(f.module));
            f.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189AutoWeaponModule.ID, "Auto Weapon (Sword)",
                    "Source-mapped highest base-damage sword from the hotbar.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 115)));
            f.owned.add(settings.register(f.module.requirePlayerHitSetting()));
            f.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189AutoWeaponModule.REQUIRE_HIT,
                    "Require Player Hit", SettingValueKind.BOOLEAN, 0)));
            f.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189AutoWeaponModule.ID,
                    Minecraft189AutoWeaponModule.REQUIRE_HIT, 0)));
            f.owned.add(settings.register(f.module.restoreAfterAttackSetting()));
            f.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189AutoWeaponModule.RESTORE,
                    "Restore After Attack", SettingValueKind.BOOLEAN, 10)));
            f.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189AutoWeaponModule.ID,
                    Minecraft189AutoWeaponModule.RESTORE, 10)));
            return f;
        } catch (RuntimeException e) {
            try { f.close(); } catch (RuntimeException cleanup) { e.addSuppressed(cleanup); }
            throw e;
        }
    }

    Minecraft189AutoWeaponModule module() {
        if (closed) throw new IllegalStateException("AutoWeapon closed");
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
                RuntimeException failure = new IllegalStateException("AutoWeapon cleanup", e);
                if (error == null) error = failure; else error.addSuppressed(failure);
            }
        }
        owned.clear();
        if (error != null) throw error;
    }
}
