package dev.trexzo.custommc.platform;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.service.ServiceRegistry;

import java.util.Objects;

public final class PlatformContext {
    private final EventBus events;
    private final ModuleRegistry modules;
    private final ModuleController moduleController;
    private final ServiceRegistry services;

    public PlatformContext(
            final EventBus events,
            final ModuleRegistry modules,
            final ModuleController moduleController,
            final ServiceRegistry services) {
        this.events = Objects.requireNonNull(events, "events");
        this.modules = Objects.requireNonNull(modules, "modules");
        this.moduleController =
                Objects.requireNonNull(moduleController, "moduleController");
        this.services = Objects.requireNonNull(services, "services");
    }

    public EventBus events() {
        return events;
    }

    public ModuleRegistry modules() {
        return modules;
    }

    public ModuleController moduleController() {
        return moduleController;
    }

    public ServiceRegistry services() {
        return services;
    }
}
