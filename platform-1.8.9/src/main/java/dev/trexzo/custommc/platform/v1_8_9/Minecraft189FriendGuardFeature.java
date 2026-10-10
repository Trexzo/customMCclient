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

/** Owns Combat Friend Guard and its editable, persistent UUID list. */
final class Minecraft189FriendGuardFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189FriendGuardModule module =
            new Minecraft189FriendGuardModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189FriendGuardFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189FriendGuardFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189FriendGuardFeature feature =
                new Minecraft189FriendGuardFeature(controller);
        try {
            feature.owned.add(modules.register(feature.module));
            feature.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189FriendGuardModule.ID, "Friend Guard",
                    "Avoid saved UUID friends in automatic combat. "
                            + "Enter comma-separated UUIDs; empty list disables filtering.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 97)));
            feature.owned.add(settings.register(feature.module.friendUuidsSetting()));
            feature.owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189FriendGuardModule.FRIEND_UUIDS, "Friend UUIDs",
                    SettingValueKind.TEXT, 0)));
            feature.owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189FriendGuardModule.ID,
                    Minecraft189FriendGuardModule.FRIEND_UUIDS, 0)));
            return feature;
        } catch (RuntimeException error) {
            try { feature.close(); } catch (RuntimeException cleanup) {
                error.addSuppressed(cleanup);
            }
            throw error;
        }
    }

    Minecraft189FriendGuardModule module() {
        if (closed) throw new IllegalStateException("Friend Guard feature closed");
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
                RuntimeException next =
                        new IllegalStateException("Friend Guard cleanup", ex);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
