package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Objects;

public final class Minecraft189RuntimeBridge {
    private static Minecraft189BootstrapRuntime activeRuntime;

    private Minecraft189RuntimeBridge() {
    }

    static synchronized Registration install(
            final Minecraft189BootstrapRuntime runtime) {
        final Minecraft189BootstrapRuntime next =
                Objects.requireNonNull(
                        runtime,
                        "runtime");
        if (activeRuntime != null) {
            throw new IllegalStateException(
                    "minecraft 1.8.9 runtime bridge is already active");
        }
        activeRuntime = next;
        return new Registration(next);
    }

    public static synchronized boolean active() {
        return activeRuntime != null
                && !activeRuntime.closed();
    }

    public static synchronized boolean hostInstalled() {
        return activeRuntime != null
                && activeRuntime.hostInstalled();
    }

    public static synchronized void targetMainEntered() {
        requireRuntime()
                .markTargetMainEntered();
    }

    public static synchronized Minecraft189HostRuntime installHost(
            final LegacyUiHostCallbacks hostCallbacks) {
        return requireRuntime()
                .installHost(
                        Objects.requireNonNull(
                                hostCallbacks,
                                "hostCallbacks"));
    }

    public static synchronized void gameTick() {
        if (activeRuntime != null) {
            activeRuntime.publishGameTick();
        }
    }

    public static synchronized void playerPosition(
            final Minecraft189PlayerPositionAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerPosition(player);
        }
    }

    public static synchronized void playerRotation(
            final Minecraft189PlayerRotationAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerRotation(player);
        }
    }

    public static synchronized void playerDimension(
            final Minecraft189PlayerDimensionAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerDimension(player);
        }
    }

    public static synchronized void playerMovementState(
            final Minecraft189PlayerMovementStateAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerMovementState(player);
        }
    }

    public static synchronized void playerSprintControl(
            final Minecraft189PlayerSprintControl player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerSprintControl(player);
        }
    }

    public static synchronized void playerJumpControl(
            final Minecraft189PlayerJumpControl player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerJumpControl(player);
        }
    }

    public static synchronized void playerHealth(
            final Minecraft189PlayerHealthAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerHealth(player);
        }
    }

    public static synchronized void playerArmor(
            final Minecraft189PlayerArmorAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerArmor(player);
        }
    }

    public static synchronized void playerHunger(
            final Minecraft189PlayerHungerAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerHunger(player);
        }
    }

    public static synchronized void playerPotionEffects(
            final Minecraft189PlayerPotionEffectsAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerPotionEffects(player);
        }
    }

    public static synchronized void playerExperience(
            final Minecraft189PlayerExperienceAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerExperience(player);
        }
    }

    public static synchronized void playerPing(
            final Minecraft189PlayerPingAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerPing(player);
        }
    }

    public static synchronized void playerHotbarSlot(
            final Minecraft189PlayerInventoryAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerHotbarSlot(player);
        }
    }

    public static synchronized void worldTime(
            final Minecraft189WorldTimeAccess world) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.worldTime(world);
        }
    }

    public static synchronized void worldWeather(
            final Minecraft189WorldWeatherAccess world) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.worldWeather(world);
        }
    }

    public static synchronized void serverAddress(
            final Minecraft189ServerDataAccess serverData) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.serverAddress(serverData);
        }
    }

    public static synchronized void playerHeldItem(
            final Minecraft189PlayerHeldItemAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerHeldItem(player);
        }
    }

    public static synchronized int rightClickDelay(
            final int currentDelay) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host == null
                ? currentDelay
                : host.rightClickDelay(
                        currentDelay);
    }

    public static synchronized int leftClickCounter(
            final int currentCounter) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host == null
                ? currentCounter
                : host.leftClickCounter(
                        currentCounter);
    }

    public static synchronized void autoClick(
            final Minecraft189ClickMouseControl minecraft) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null
                && minecraft != null
                && host.shouldAutoClick()) {
            minecraft.customMcClickMouse();
        }
    }

    public static synchronized void publishTick(
            final long tickIndex) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.publishTick(tickIndex);
        }
    }

    public static synchronized void renderFrameStarted(
            final float partialTicks) {
        if (activeRuntime != null) {
            activeRuntime.beginRenderFrame(
                    partialTicks);
        }
    }

    public static synchronized void renderHudFrame(
            final float partialTicks) {
        if (activeRuntime != null) {
            activeRuntime.renderHudFrame(
                    partialTicks);
        }
    }

    public static synchronized void renderPostProcessFrame(
            final float partialTicks) {
        if (activeRuntime != null) {
            activeRuntime.renderPostProcessFrame(
                    partialTicks);
        }
    }

    public static synchronized void renderWorld(
            final long frameIndex,
            final float partialTicks) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.renderWorld(
                    frameIndex,
                    partialTicks);
        }
    }

    public static synchronized void renderWorldOverlay(
            final long frameIndex,
            final float partialTicks) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.renderWorldOverlay(
                    frameIndex,
                    partialTicks);
        }
    }

    public static synchronized void renderHud(
            final long frameIndex,
            final float partialTicks) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.renderHud(
                    frameIndex,
                    partialTicks);
        }
    }

    public static synchronized void renderPostProcess(
            final long frameIndex,
            final float partialTicks) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.renderPostProcess(
                    frameIndex,
                    partialTicks);
        }
    }

    public static synchronized boolean pointerButton(
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyButton,
            final boolean pressed) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host != null
                && host.pointerButton(
                        pixelX,
                        pixelYFromBottom,
                        legacyButton,
                        pressed);
    }

    public static synchronized boolean scroll(
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyWheelDelta) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host != null
                && host.scroll(
                        pixelX,
                        pixelYFromBottom,
                        legacyWheelDelta);
    }

    public static synchronized boolean key(
            final int legacyKeyCode,
            final char character,
            final boolean pressed,
            final boolean repeat,
            final boolean shift,
            final boolean control,
            final boolean alt) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host != null
                && host.key(
                        legacyKeyCode,
                        character,
                        pressed,
                        repeat,
                        shift,
                        control,
                        alt);
    }

    private static Minecraft189BootstrapRuntime requireRuntime() {
        if (activeRuntime == null
                || activeRuntime.closed()) {
            throw new IllegalStateException(
                    "minecraft 1.8.9 runtime bridge is not active");
        }
        return activeRuntime;
    }

    private static Minecraft189HostRuntime activeHost() {
        if (activeRuntime == null
                || !activeRuntime.hostInstalled()) {
            return null;
        }
        return activeRuntime.requireHostRuntime();
    }

    static final class Registration
            implements AutoCloseable {
        private final Minecraft189BootstrapRuntime owner;
        private boolean closed;

        private Registration(
                final Minecraft189BootstrapRuntime owner) {
            this.owner = owner;
        }

        @Override
        public void close() {
            synchronized (Minecraft189RuntimeBridge.class) {
                if (closed) {
                    return;
                }
                if (activeRuntime == owner) {
                    activeRuntime = null;
                }
                closed = true;
            }
        }

        synchronized boolean closed() {
            return closed;
        }
    }
}
