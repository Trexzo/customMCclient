package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.*;
import java.util.ArrayList;
import java.util.List;

/** Module lifecycle and registry rollback for native SafeWalk. */
final class Minecraft189SafeWalkFeature implements AutoCloseable {
    private final Minecraft189SafeWalkModule module;
    private final ModuleController controller;
    private final List<AutoCloseable> owned;
    private boolean closed;

    private Minecraft189SafeWalkFeature(final Minecraft189SafeWalkModule module,
            final ModuleController controller, final List<AutoCloseable> owned) {
        this.module = module;
        this.controller = controller;
        this.owned = owned;
    }

    static Minecraft189SafeWalkFeature install(final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations) {
        final Minecraft189SafeWalkModule module = new Minecraft189SafeWalkModule();
        final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
        try {
            owned.add(modules.register(module));
            owned.add(presentations.register(new ModuleDescriptor(
                    module.id(), "SafeWalk",
                    "Vanilla collision-aware ledge protection for the local player.",
                    Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID, 62)));
            return new Minecraft189SafeWalkFeature(module, controller, owned);
        } catch (RuntimeException error) {
            closeOwned(owned, error);
            throw error;
        }
    }
    Minecraft189SafeWalkModule module() {
        if (closed) throw new IllegalStateException("SafeWalk feature closed");
        return module;
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        try {
            if (controller.stateOf(module.id()) != ModuleState.DISABLED)
                controller.disable(module.id());
        } catch (RuntimeException error) { failure = error; }
        failure = closeOwned(owned, failure);
        if (failure != null) throw failure;
    }
    private static RuntimeException closeOwned(final List<AutoCloseable> owned,
            RuntimeException failure) {
        for (int i = owned.size() - 1; i >= 0; i--) {
            try { owned.get(i).close(); }
            catch (Exception error) {
                final RuntimeException e = error instanceof RuntimeException
                        ? (RuntimeException)error : new IllegalStateException(error);
                if (failure == null) failure = e;
                else failure.addSuppressed(e);
            }
        }
        return failure;
    }
}
