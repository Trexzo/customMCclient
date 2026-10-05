package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.GamePlatform;
import dev.trexzo.custommc.platform.PlatformContext;

import java.util.Objects;

public final class Minecraft189Platform implements GamePlatform {
    public static final String PLATFORM_ID = "minecraft-1.8.9";
    public static final String GAME_VERSION = "1.8.9";

    private PlatformContext context;

    @Override
    public String id() {
        return PLATFORM_ID;
    }

    @Override
    public String gameVersion() {
        return GAME_VERSION;
    }

    @Override
    public synchronized void attach(final PlatformContext nextContext) {
        Objects.requireNonNull(nextContext, "context");
        if (context != null) {
            throw new IllegalStateException("platform already attached");
        }
        context = nextContext;
    }

    @Override
    public synchronized void detach() {
        if (context == null) {
            return;
        }
        context = null;
    }

    public synchronized boolean attached() {
        return context != null;
    }

    PlatformContext requireContext() {
        final PlatformContext current;
        synchronized (this) {
            current = context;
        }
        if (current == null) {
            throw new IllegalStateException("platform is not attached");
        }
        return current;
    }
}
