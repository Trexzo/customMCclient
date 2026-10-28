package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Small, non-interactive footer for a clipped, scrollable ClickGUI page.
 *
 * It masks accidental partial rows in the original 1.8.9 view, explains
 * that more rows exist and is absent at the bottom so the final row stays
 * fully accessible. Pointer admission is kept in the same pure geometry.
 */
final class UiContentBottomCue {
    static final float HEIGHT = 18.0F;
    private static final float MIN_WIDTH = 124.0F;
    private static final float MIN_HEIGHT = 92.0F;

    private UiContentBottomCue() {
    }

    static List<UiDrawCommand> compose(
            final UiBounds content,
            final float contentHeight,
            final float scrollOffset,
            final UiTheme theme) {
        Objects.requireNonNull(theme, "theme");
        final UiBounds footer = activeBounds(
                content, contentHeight, scrollOffset);
        if (footer == null) {
            return Collections.emptyList();
        }

        final List<UiDrawCommand> commands = new ArrayList<UiDrawCommand>(3);
        commands.add(new UiRectCommand(8,
                footer.x(), footer.y(), footer.width(), footer.height(),
                theme.color(UiColorRole.SURFACE)));
        commands.add(new UiRectCommand(9,
                footer.x(), footer.y(), footer.width(), 1.0F,
                theme.color(UiColorRole.BORDER)));
        commands.add(new UiTextCommand(10,
                footer.x() + footer.width() - 111.0F,
                footer.y() + 4.0F,
                UiFonts.DEFAULT, "SCROLL FOR MORE",
                theme.color(UiColorRole.TEXT_MUTED)));
        return commands;
    }

    static boolean blocksPointer(
            final UiBounds content,
            final float contentHeight,
            final float scrollOffset,
            final float x,
            final float y) {
        final UiBounds footer = activeBounds(
                content, contentHeight, scrollOffset);
        return footer != null && footer.contains(x, y);
    }

    private static UiBounds activeBounds(
            final UiBounds content,
            final float contentHeight,
            final float scrollOffset) {
        Objects.requireNonNull(content, "content");
        if (!Float.isFinite(contentHeight) || contentHeight < 0.0F
                || !Float.isFinite(scrollOffset) || scrollOffset < 0.0F) {
            throw new IllegalArgumentException(
                    "content and scroll measurements must be finite and non-negative");
        }
        final float maxScroll = Math.max(
                0.0F, contentHeight - content.height());
        if (content.width() < MIN_WIDTH
                || content.height() < MIN_HEIGHT
                || maxScroll <= 0.5F
                || scrollOffset >= maxScroll - 0.5F) {
            return null;
        }
        return new UiBounds(
                content.x(), content.y() + content.height() - HEIGHT,
                content.width(), HEIGHT);
    }
}
