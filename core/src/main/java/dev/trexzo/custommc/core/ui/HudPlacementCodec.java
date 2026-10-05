package dev.trexzo.custommc.core.ui;

import dev.trexzo.custommc.core.setting.SettingCodec;

import java.util.Objects;

public final class HudPlacementCodec
        implements SettingCodec<HudPlacement> {
    @Override
    public String encode(final HudPlacement value) {
        Objects.requireNonNull(value, "value");
        return value.anchor().name()
                + ";"
                + Float.toString(value.offsetX())
                + ";"
                + Float.toString(value.offsetY());
    }

    @Override
    public HudPlacement decode(final String encoded) {
        Objects.requireNonNull(encoded, "encoded");

        final String[] parts = encoded.split(";", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "invalid HUD placement encoding");
        }

        final UiAnchor anchor;
        final float offsetX;
        final float offsetY;

        try {
            anchor = UiAnchor.valueOf(parts[0]);
            offsetX = Float.parseFloat(parts[1]);
            offsetY = Float.parseFloat(parts[2]);
        } catch (IllegalArgumentException invalid) {
            throw new IllegalArgumentException(
                    "invalid HUD placement encoding",
                    invalid);
        }

        return new HudPlacement(
                anchor,
                offsetX,
                offsetY);
    }
}
