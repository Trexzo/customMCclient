package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.*;
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
            final ModulePresentationRegistry presentations) {
        final Minecraft189ScaffoldModule module=new Minecraft189ScaffoldModule();
        final List<AutoCloseable> owned=new ArrayList<AutoCloseable>();
        try {
            owned.add(modules.register(module));
            owned.add(presentations.register(new ModuleDescriptor(
                    module.id(), "Scaffold",
                    "One verified vanilla block attempt per enable; re-enable for another.",
                    Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID, 63)));
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
