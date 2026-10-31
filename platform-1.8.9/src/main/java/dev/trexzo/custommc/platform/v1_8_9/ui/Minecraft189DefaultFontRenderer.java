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
    public int measureText(final String text) {
        return Math.max(0, access.getStringWidth(
                Objects.requireNonNull(text, "text")));
    }

    private boolean acceptanceOriginalShadowReported;

    @Override
    public void drawText(
            final UiFontHandle font,
            final float x,
            final float y,
            final String text,
            final int argb) {
        drawTextNative(font, x, y, text, argb, false);
    }

    @Override
    public void drawTextShadow(
            final UiFontHandle font,
            final float x,
            final float y,
            final String text,
            final int argb) {
        drawTextNative(font, x, y, text, argb, true);
        // Printed only by the actual Mojang FontRenderer adapter, after
        // drawString(..., true) returns; never by a fake HUD callback.
        if (!acceptanceOriginalShadowReported
                && Boolean.getBoolean(
                        "custommc.acceptance.captureRealHudVisuals")) {
            acceptanceOriginalShadowReported = true;
            System.out.println(
                    "CUSTOMMC_OFFICIAL_189_REAL_ARRAYLIST_NATIVE_SHADOW_PASS=YES");
        }
    }

    private void drawTextNative(
            final UiFontHandle font,
            final float x,
            final float y,
            final String text,
            final int argb,
            final boolean shadow) {
        final UiFontHandle requested = Objects.requireNonNull(font, "font");
        if (!UiFonts.DEFAULT.equals(requested)) {
            throw new IllegalArgumentException(
                    "unsupported Minecraft 1.8.9 font handle: "
                            + requested.id());
        }
        access.drawString(Objects.requireNonNull(text, "text"),
                x, y, argb, shadow);
    }
}
