package dev.trexzo.custommc.launcher;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.platform.PlatformContext;

public final class LauncherMain {
    private LauncherMain() {
    }

    public static void main(final String[] args) {
        final EventBus events = new EventBus();
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController moduleController =
                new ModuleController(modules);
        final ServiceRegistry services = new ServiceRegistry();

        final PlatformContext context = new PlatformContext(
                events,
                modules,
                moduleController,
                services);

        System.out.println("customMCclient foundation");
        System.out.println(
                "core.modules=" + context.modules().snapshot().size());
        System.out.println(
                "core.services=" + context.services().snapshot().size());
        System.out.println("minecraft.platform=not-attached");
    }
}
