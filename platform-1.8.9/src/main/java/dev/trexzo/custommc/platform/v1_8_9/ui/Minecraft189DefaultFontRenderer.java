package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiFonts;

import java.util.Objects;

public final class Minecraft189DefaultFontRenderer
        implements LegacyUiTextRenderer {
    private final Minecraft189FontRendererAccess access;

    public Minecraft189DefaultFontRenderer(
            final Minecraft189FontRendererAccess access) {
        this.access =
                Objects.requireNonNull(
                        access,
                        "access");
    }

    @Override
    public void drawText(
            final UiFontHandle font,
            final float x,
            final float y,
            final String text,
            final int argb) {
        final UiFontHandle requested =
                Objects.requireNonNull(
                        font,
                        "font");
        if (!UiFonts.DEFAULT.equals(requested)) {
            throw new IllegalArgumentException(
                    "unsupported Minecraft 1.8.9 font handle: "
                            + requested.id());
        }

        access.drawString(
                Objects.requireNonNull(
                        text,
                        "text"),
                x,
                y,
                argb,
                false);
    }
}
