package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Compact two-level module copy for the original Minecraft 1.8.9 renderer.
 *
 * The Vape V4 reference shows the value of describing a module without
 * opening a detail panel. Slinky's minimal HUD and Rise's quieter hierarchy
 * favor text, not another nested background/card. This helper only composes
 * clipped draw commands; authoritative module input and state stay elsewhere.
 */
final class UiModuleCardText {
    private static final float TEXT_INSET = 13.0F;
    private static final float SINGLE_LINE_Y = 10.0F;
    private static final float TITLE_Y = 4.0F;
    private static final float DESCRIPTION_Y = 16.0F;
    private static final float MINIMUM_TWO_LINE_WIDTH = 170.0F;

    private UiModuleCardText() {
    }

    static List<UiDrawCommand> compose(
            final UiBounds row,
            final String title,
            final String description,
            final boolean hasDetails,
            final UiTheme theme) {
        Objects.requireNonNull(row, "row");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(theme, "theme");

        // Match the actual right-aligned toggle and ... hitboxes.
        final float reservedRight = hasDetails ? 87.0F : 57.0F;
        final float clipWidth = Math.max(0.0F,
                row.width() - TEXT_INSET - reservedRight);
        if (clipWidth <= 0.0F || row.height() < 15.0F) {
            return Collections.emptyList();
        }

        final String detail = description == null
                ? "" : description.trim();
        final boolean twoLines = !detail.isEmpty()
                && clipWidth >= MINIMUM_TWO_LINE_WIDTH
                && row.height() >= 28.0F;
        final float textX = row.x() + TEXT_INSET;

        final List<UiDrawCommand> texts = new ArrayList<UiDrawCommand>(2);
        texts.add(new UiTextCommand(
                0, textX, row.y() + (twoLines ? TITLE_Y : SINGLE_LINE_Y),
                UiFonts.DEFAULT, title,
                theme.color(UiColorRole.TEXT_PRIMARY)));
        if (twoLines) {
            texts.add(new UiTextCommand(
                    0, textX, row.y() + DESCRIPTION_Y,
                    UiFonts.DEFAULT, detail,
                    theme.color(UiColorRole.TEXT_MUTED)));
        }
        return Collections.<UiDrawCommand>singletonList(
                new UiClipCommand(
                        2,
                        new UiBounds(textX, row.y(),
                                clipWidth, row.height()),
                        texts));
    }
}
