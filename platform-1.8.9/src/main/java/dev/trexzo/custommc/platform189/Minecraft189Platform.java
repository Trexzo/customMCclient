package dev.trexzo.custommc.platform189;

import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.platform.GameEventBridge;
import dev.trexzo.custommc.platform.GamePlatform;
import dev.trexzo.custommc.platform.PlatformContext;

import java.util.Objects;

public final class Minecraft189Platform
        implements GamePlatform {
    private Minecraft189EventBridge bridge;
    private ServiceRegistry.Registration<GameEventBridge>
            bridgeRegistration;

    @Override
    public String id() {
        return "minecraft-1.8.9";
    }

    @Override
    public String gameVersion() {
        return "1.8.9";
    }

    @Override
    public synchronized void attach(
            final PlatformContext context) {
        Objects.requireNonNull(context, "context");
        if (bridge != null) {
            throw new IllegalStateException(
                    "Minecraft 1.8.9 platform already attached");
        }

        final Minecraft189EventBridge candidate =
                new Minecraft189EventBridge(
                        context.events());
        final ServiceRegistry.Registration<GameEventBridge>
                registration =
                context.services().register(
                        GameEventBridge.class,
                        candidate);

        bridge = candidate;
        bridgeRegistration = registration;
    }

    @Override
    public synchronized void detach() {
        if (bridge == null) {
            return;
        }

        bridge.deactivate();
        bridgeRegistration.close();
        bridgeRegistration = null;
        bridge = null;
    }

    public synchronized GameEventBridge bridge() {
        if (bridge == null) {
            throw new IllegalStateException(
                    "Minecraft 1.8.9 platform is detached");
        }
        return bridge;
    }
}
