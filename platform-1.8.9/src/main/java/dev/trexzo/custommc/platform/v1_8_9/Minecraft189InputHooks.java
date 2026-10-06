package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleKeyChord;
import dev.trexzo.custommc.core.module.ModuleKeybindController;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiScrollEvent;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
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

    public boolean scroll(
            final int framebufferWidth,
            final int framebufferHeight,
            final float uiScale,
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyWheelDelta) {
        final UiViewport viewport =
                new UiViewport(
                        framebufferWidth,
                        framebufferHeight,
                        uiScale);
        final Optional<UiScrollEvent> event =
                translator.scrollEvent(
                        viewport,
                        pixelX,
                        pixelYFromBottom,
                        legacyWheelDelta);

        if (!event.isPresent()) {
            return false;
        }

        return input().scroll(
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
        final ServiceRegistry services =
                platform.requireContext()
                        .services();

        if (services.contains(
                Minecraft189ClickGuiToggleController.class)
                && services.require(
                Minecraft189ClickGuiToggleController.class)
                .key(
                        legacyKeyCode,
                        pressed,
                        repeat)) {
            return true;
        }

        if (services.contains(
                ClickGuiInputController.class)
                && services.require(
                ClickGuiInputController.class)
                .key(event)) {
            return true;
        }

        if (services.contains(
                ClickGuiModel.class)
                && services.require(
                ClickGuiModel.class)
                .snapshot()
                .open()) {
            return false;
        }

        if (event.action() != UiKeyAction.PRESS
                || !services.contains(
                ModuleKeybindController.class)) {
            return false;
        }

        return services.require(
                ModuleKeybindController.class)
                .press(
                        new ModuleKeyChord(
                                event.key().id(),
                                event.shift(),
                                event.control(),
                                event.alt()));
    }

    private ClickGuiInputController input() {
        return platform.requireContext()
                .services()
                .require(ClickGuiInputController.class);
    }
}
