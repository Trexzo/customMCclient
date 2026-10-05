package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyInputTranslator;

import java.util.Objects;
import java.util.Optional;

public final class Minecraft189InputHooks {
    private final Minecraft189Platform platform;
    private final LegacyInputTranslator translator;

    public Minecraft189InputHooks(
            final Minecraft189Platform platform) {
        this(
                platform,
                new LegacyInputTranslator());
    }

    Minecraft189InputHooks(
            final Minecraft189Platform platform,
            final LegacyInputTranslator translator) {
        this.platform =
                Objects.requireNonNull(
                        platform,
                        "platform");
        this.translator =
                Objects.requireNonNull(
                        translator,
                        "translator");
    }

    public boolean pointerButton(
            final int framebufferWidth,
            final int framebufferHeight,
            final float uiScale,
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyButton,
            final boolean pressed) {
        final UiViewport viewport =
                new UiViewport(
                        framebufferWidth,
                        framebufferHeight,
                        uiScale);
        final Optional<UiPointerEvent> event =
                translator.pointerButtonEvent(
                        viewport,
                        pixelX,
                        pixelYFromBottom,
                        legacyButton,
                        pressed);

        if (!event.isPresent()) {
            return false;
        }

        return input().pointer(
                event.get(),
                viewport);
    }

    public boolean key(
            final int legacyKeyCode,
            final char character,
            final boolean pressed,
            final boolean repeat,
            final boolean shift,
            final boolean control,
            final boolean alt) {
        final UiKeyEvent event =
                translator.keyEvent(
                        legacyKeyCode,
                        character,
                        pressed,
                        repeat,
                        shift,
                        control,
                        alt);
        return input().key(event);
    }

    private ClickGuiInputController input() {
        return platform.requireContext()
                .services()
                .require(ClickGuiInputController.class);
    }
}
