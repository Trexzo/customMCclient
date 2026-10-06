package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleKeybindController;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.platform.PlatformContext;

import java.util.Objects;

public final class Minecraft189ModuleKeybindRuntime
        implements AutoCloseable {
    private final ModuleKeybindController controller;
    private final ServiceRegistry.Registration registration;
    private boolean closed;

    private Minecraft189ModuleKeybindRuntime(
            final ModuleKeybindController controller,
            final ServiceRegistry.Registration registration) {
        this.controller = controller;
        this.registration = registration;
    }

    public static Minecraft189ModuleKeybindRuntime install(
            final Minecraft189Platform platform,
            final ModuleKeybindRegistry keybinds) {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(keybinds, "keybinds");

        final PlatformContext context =
                platform.requireContext();
        final ServiceRegistry services =
                context.services();

        if (services.contains(
                ModuleKeybindController.class)) {
            throw new IllegalArgumentException(
                    "module keybind runtime already installed");
        }

        final ModuleKeybindController controller =
                new ModuleKeybindController(
                        keybinds,
                        context.moduleController());
        final ServiceRegistry.Registration registration =
                services.registerManaged(
                        ModuleKeybindController.class,
                        controller);

        return new Minecraft189ModuleKeybindRuntime(
                controller,
                registration);
    }

    public ModuleKeybindController controller() {
        return controller;
    }

    public synchronized boolean closed() {
        return closed;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        registration.close();
    }
}
