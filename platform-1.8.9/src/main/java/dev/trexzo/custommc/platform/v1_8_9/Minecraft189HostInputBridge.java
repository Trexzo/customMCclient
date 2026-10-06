package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiViewportSource;

import java.util.Objects;

public final class Minecraft189HostInputBridge {
    private final Minecraft189InputHooks hooks;
    private final LegacyUiViewportSource viewportSource;

    public Minecraft189HostInputBridge(
            final Minecraft189Platform platform,
            final LegacyUiViewportSource viewportSource) {
        this(
                new Minecraft189InputHooks(
                        Objects.requireNonNull(
                                platform,
                                "platform")),
                viewportSource);
    }

    Minecraft189HostInputBridge(
            final Minecraft189InputHooks hooks,
            final LegacyUiViewportSource viewportSource) {
        this.hooks = Objects.requireNonNull(
                hooks,
                "hooks");
        this.viewportSource = Objects.requireNonNull(
                viewportSource,
                "viewportSource");
    }

    public boolean pointerButton(
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyButton,
            final boolean pressed) {
        final int width =
                viewportSource.framebufferWidth();
        final int height =
                viewportSource.framebufferHeight();
        final float scale =
                viewportSource.uiScale();

        return hooks.pointerButton(
                width,
                height,
                scale,
                pixelX,
                pixelYFromBottom,
                legacyButton,
                pressed);
    }

    public boolean scroll(
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyWheelDelta) {
        final int width =
                viewportSource.framebufferWidth();
        final int height =
                viewportSource.framebufferHeight();
        final float scale =
                viewportSource.uiScale();

        return hooks.scroll(
                width,
                height,
                scale,
                pixelX,
                pixelYFromBottom,
                legacyWheelDelta);
    }

    public boolean key(
            final int legacyKeyCode,
            final char character,
            final boolean pressed,
            final boolean repeat,
            final boolean shift,
            final boolean control,
            final boolean alt) {
        return hooks.key(
                legacyKeyCode,
                character,
                pressed,
                repeat,
                shift,
                control,
                alt);
    }
}
