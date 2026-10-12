package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.*;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.setting.*;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import java.util.ArrayList;
import java.util.List;

/** Lifecycle-owned registration; failed partial installation unwinds in reverse. */
final class Minecraft189PlayerIndicatorsFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189PlayerIndicatorsModule module;
    private final List<AutoCloseable> owned;
    private boolean closed;

    private Minecraft189PlayerIndicatorsFeature(final ModuleController controller,
            final Minecraft189PlayerIndicatorsModule module,
            final List<AutoCloseable> owned) {
        this.controller = controller;
        this.module = module;
        this.owned = owned;
    }

    static Minecraft189PlayerIndicatorsFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry bindings,
            final SettingRegistry settings,
            final SettingPresentationRegistry labels,
            final Minecraft189PlayerPositionState local,
            final Minecraft189WorldEntityPositionState positions,
            final Minecraft189WorldEntityKindState kinds,
            final Minecraft189PlayerRotationState rotation,
            final RenderPipeline pipeline, final LegacyUiHostCallbacks host) {
        final Minecraft189PlayerIndicatorsModule module =
                new Minecraft189PlayerIndicatorsModule(local, positions, kinds,
                        rotation, pipeline, host);
        final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
        try {
            owned.add(modules.register(module));
            owned.add(presentations.register(new ModuleDescriptor(
                    module.id(), "Player Direction ESP",
                    "Compass-style remote-player direction indicators; not projected 3D boxes.",
                    Minecraft189FeatureCatalog.VISUALS_CATEGORY_ID, 58)));
            owned.add(settings.register(module.rangeSetting()));
            owned.add(settings.register(module.distancesSetting()));
            owned.add(labels.register(new SettingDescriptor(
                    Minecraft189PlayerIndicatorsModule.RANGE, "Range (Blocks)",
                    SettingValueKind.INTEGER, 0,
                    new SettingNumericSpec(4.0D, 128.0D, 1.0D))));
            owned.add(labels.register(new SettingDescriptor(
                    Minecraft189PlayerIndicatorsModule.DISTANCES,
                    "Show Distances", SettingValueKind.BOOLEAN, 10)));
            owned.add(bindings.register(new ModuleSettingBinding(module.id(),
                    Minecraft189PlayerIndicatorsModule.RANGE, 0)));
            owned.add(bindings.register(new ModuleSettingBinding(module.id(),
                    Minecraft189PlayerIndicatorsModule.DISTANCES, 10)));
            return new Minecraft189PlayerIndicatorsFeature(controller, module, owned);
        } catch (RuntimeException error) {
            closeOwned(owned, error);
            throw error;
        }
    }

    Minecraft189PlayerIndicatorsModule module() {
        if (closed) throw new IllegalStateException("indicators feature closed");
        return module;
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        try {
            if (controller.stateOf(module.id()) != ModuleState.DISABLED)
                controller.disable(module.id());
        } catch (RuntimeException e) { failure = e; }
        final RuntimeException result = closeOwned(owned, failure);
        if (result != null) throw result;
    }

    private static RuntimeException closeOwned(final List<AutoCloseable> registrations,
            RuntimeException failure) {
        for (int i = registrations.size() - 1; i >= 0; i--) {
            try { registrations.get(i).close(); }
            catch (Exception e) {
                final RuntimeException wrapped = e instanceof RuntimeException
                        ? (RuntimeException)e : new IllegalStateException(e);
                if (failure == null) failure = wrapped;
                else failure.addSuppressed(wrapped);
            }
        }
        return failure;
    }
}
