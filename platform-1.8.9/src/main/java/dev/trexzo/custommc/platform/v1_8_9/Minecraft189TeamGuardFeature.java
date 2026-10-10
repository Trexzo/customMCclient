package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import java.util.ArrayList;
import java.util.List;

/** Standalone, default-off Team Guard lifecycle and GUI registration. */
final class Minecraft189TeamGuardFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189TeamGuardModule module = new Minecraft189TeamGuardModule();
    private final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
    private boolean closed;

    private Minecraft189TeamGuardFeature(final ModuleController controller) {
        this.controller = controller;
    }

    static Minecraft189TeamGuardFeature install(
            final ModuleRegistry modules, final ModuleController controller,
            final ModulePresentationRegistry presentations) {
        final Minecraft189TeamGuardFeature feature =
                new Minecraft189TeamGuardFeature(controller);
        try {
            feature.owned.add(modules.register(feature.module));
            feature.owned.add(presentations.register(new ModuleDescriptor(
                    Minecraft189TeamGuardModule.ID, "Team Guard",
                    "Rejects confirmed teammates and unverified team evidence "
                    + "from synthetic Combat actions; independent of AntiBot.",
                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 96)));
            return feature;
        } catch (RuntimeException error) {
            try { feature.close(); } catch (RuntimeException clean) {
                error.addSuppressed(clean);
            }
            throw error;
        }
    }

    Minecraft189TeamGuardModule module() {
        if (closed) throw new IllegalStateException("Team Guard feature closed");
        return module;
    }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        try {
            if (controller.stateOf(module.id()) != ModuleState.DISABLED) {
                controller.disable(module.id());
            }
        } catch (RuntimeException ex) { failure = ex; }
        for (int i = owned.size() - 1; i >= 0; i--) {
            try { owned.get(i).close(); }
            catch (Exception ex) {
                final RuntimeException next =
                        new IllegalStateException("Team Guard cleanup", ex);
                if (failure == null) failure = next;
                else failure.addSuppressed(next);
            }
        }
        owned.clear();
        if (failure != null) throw failure;
    }
}
