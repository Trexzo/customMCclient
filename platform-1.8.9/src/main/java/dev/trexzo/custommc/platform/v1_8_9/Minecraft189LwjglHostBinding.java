package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.ui.Lwjgl2LegacyUiHostCallbacks;
import dev.trexzo.custommc.platform.v1_8_9.ui.Lwjgl2Minecraft189ViewportSource;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189DefaultFontRenderer;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189FontRendererAccess;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;

import java.util.Objects;

public final class Minecraft189LwjglHostBinding {
    private Minecraft189LwjglHostBinding() {
    }

    public static Minecraft189HostRuntime install(
            final Minecraft189GuiSettingsAccess settings,
            final Minecraft189FontRendererAccess fontRenderer) {
        return Minecraft189RuntimeBridge.installHost(
                new Lwjgl2LegacyUiHostCallbacks(
                        new Lwjgl2Minecraft189ViewportSource(
                                Objects.requireNonNull(
                                        settings,
                                        "settings")),
                        new Minecraft189DefaultFontRenderer(
                                Objects.requireNonNull(
                                        fontRenderer,
                                        "fontRenderer"))));
    }
}
