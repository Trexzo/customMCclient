package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.*;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import java.util.ArrayList;
import java.util.List;

/** V32 module-controller ownership and fail-closed registry rollback. */
final class Minecraft189ScaffoldFeature implements AutoCloseable {
    private final Minecraft189ScaffoldModule module;
    private final ModuleController controller;
    private final List<AutoCloseable> registrations;
    private boolean closed;

    private Minecraft189ScaffoldFeature(final Minecraft189ScaffoldModule module,
            final ModuleController controller,
            final List<AutoCloseable> registrations) {
        this.module=module;
        this.controller=controller;
        this.registrations=registrations;
    }

    static Minecraft189ScaffoldFeature install(final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189ScaffoldModule module=new Minecraft189ScaffoldModule();
        final List<AutoCloseable> owned=new ArrayList<AutoCloseable>();
        try {
            owned.add(modules.register(module));
            owned.add(presentations.register(new ModuleDescriptor(
                    module.id(), "Scaffold",
                    "One native placement by default; separate opt-in 2/3/4/5/6 modes.",
                    Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID, 63)));
            owned.add(settings.register(module.twoPlacementsSetting()));
            owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189ScaffoldModule.TWO_PLACEMENTS_SETTING_ID,
                    "Two Placements (Opt-in)", SettingValueKind.BOOLEAN, 0)));
            owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189ScaffoldModule.ID,
                    Minecraft189ScaffoldModule.TWO_PLACEMENTS_SETTING_ID, 0)));
            // Mutually exclusive runtime modes; the module rejects both
            // enabled rather than silently selecting a quota or one-shot.
            owned.add(settings.register(module.threePlacementsSetting()));
            owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189ScaffoldModule.THREE_PLACEMENTS_SETTING_ID,
                    "Three Placements (Opt-in)", SettingValueKind.BOOLEAN, 1)));
            owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189ScaffoldModule.ID,
                    Minecraft189ScaffoldModule.THREE_PLACEMENTS_SETTING_ID, 1)));
            owned.add(settings.register(module.fourPlacementsSetting()));
            owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189ScaffoldModule.FOUR_PLACEMENTS_SETTING_ID,
                    "Four Placements (Opt-in)", SettingValueKind.BOOLEAN, 2)));
            owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189ScaffoldModule.ID,
                    Minecraft189ScaffoldModule.FOUR_PLACEMENTS_SETTING_ID, 2)));
            // Do not renumber the existing GUI slots: V67 is additive.
            owned.add(settings.register(module.fivePlacementsSetting()));
            owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189ScaffoldModule.FIVE_PLACEMENTS_SETTING_ID,
                    "Five Placements (Opt-in)", SettingValueKind.BOOLEAN, 4)));
            owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189ScaffoldModule.ID,
                    Minecraft189ScaffoldModule.FIVE_PLACEMENTS_SETTING_ID, 4)));
            // Keep stable older UI binding orders; this is additive.
            owned.add(settings.register(module.sixPlacementsSetting()));
            owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189ScaffoldModule.SIX_PLACEMENTS_SETTING_ID,
                    "Six Placements (Opt-in)", SettingValueKind.BOOLEAN, 5)));
            owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189ScaffoldModule.ID,
                    Minecraft189ScaffoldModule.SIX_PLACEMENTS_SETTING_ID, 5)));
            owned.add(settings.register(module.adjacentEdgeTargetsSetting()));
            owned.add(settingPresentations.register(new SettingDescriptor(
                    Minecraft189ScaffoldModule.ADJACENT_EDGE_SETTING_ID,
                    "Native Adjacent Edge Targets (Opt-in)",
                    SettingValueKind.BOOLEAN, 3)));
            owned.add(moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189ScaffoldModule.ID,
                    Minecraft189ScaffoldModule.ADJACENT_EDGE_SETTING_ID, 3)));
            return new Minecraft189ScaffoldFeature(module,controller,owned);
        } catch(RuntimeException failure) {
            closeOwned(owned,failure);
            throw failure;
        }
    }

    synchronized Minecraft189ScaffoldModule module() {
        if(closed) throw new IllegalStateException("Scaffold feature closed");
        return module;
    }

    @Override public synchronized void close() {
        if(closed) return;
        closed=true;
        RuntimeException failure=null;
        try {
            if(controller.stateOf(module.id())!=ModuleState.DISABLED)
                controller.disable(module.id());
        } catch(RuntimeException error) { failure=error; }
        // Invalidate even when a broken controller prevented onDisable.
        module.onDisable();
        failure=closeOwned(registrations,failure);
        if(failure!=null) throw failure;
    }

    private static RuntimeException closeOwned(final List<AutoCloseable> owned,
            RuntimeException failure) {
        for(int i=owned.size()-1;i>=0;i--) {
            try {owned.get(i).close();}
            catch(Exception error) {
                final RuntimeException e=error instanceof RuntimeException
                        ?(RuntimeException)error:new IllegalStateException(error);
                if(failure==null)failure=e;
                else failure.addSuppressed(e);
            }
        }
        return failure;
    }
}
