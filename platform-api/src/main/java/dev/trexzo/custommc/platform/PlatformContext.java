package dev.trexzo.custommc.platform;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleRegistry;

import java.util.Objects;

public final class PlatformContext {
    private final EventBus events;
    private final ModuleRegistry modules;

    public PlatformContext(
            final EventBus events,
            final ModuleRegistry modules) {
        this.events = Objects.requireNonNull(events, "events");
        this.modules = Objects.requireNonNull(modules, "modules");
    }

    public EventBus events() {
        return events;
    }

    public ModuleRegistry modules() {
        return modules;
    }
}
