package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;

import java.util.ArrayList;
import java.util.List;

/** Standalone Combat wall check ownership and GUI registration. */
final class Minecraft189WallCheckFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189WallCheckModule module =
            new Minecraft189WallCheckModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189WallCheckFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189WallCheckFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations) {
        final Minecraft189WallCheckFeature feature =
                new Minecraft189WallCheckFeature(controller);
        try {
            feature.owned.add(modules.register(feature.module));
            feature.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189WallCheckModule.ID, "Wall Check",
                    "Require verified vanilla line of sight to players "
                            + "before any automatic Combat target or attack.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 98)));
            return feature;
        } catch (RuntimeException error) {
            try { feature.close(); } catch (RuntimeException cleanup) {
                error.addSuppressed(cleanup);
            }
            throw error;
        }
    }

    Minecraft189WallCheckModule module() {
        if (closed) throw new IllegalStateException("Wall Check feature closed");
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
                        new IllegalStateException("Wall Check cleanup", ex);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
