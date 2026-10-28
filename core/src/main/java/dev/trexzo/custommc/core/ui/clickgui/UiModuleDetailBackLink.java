package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A small contextual back affordance, fixed to the top right of the
 * module detail pane. Geometry is shared with pointer acceptance.
 * It never manipulates module, settings or native Minecraft state.
 */
final class UiModuleDetailBackLink {
    private static final float WIDTH = 102.0F;
    private static final float HEIGHT = 17.0F;
    private static final float TOP = 4.0F;
    private static final float RIGHT = 15.0F;

    private UiModuleDetailBackLink() {
    }

    static List<UiDrawCommand> compose(
            final UiBounds content, final UiTheme theme) {
        Objects.requireNonNull(theme, "theme");
        final UiBounds button = bounds(content);
        if (button == null) {
            return Collections.emptyList();
        }
        final List<UiDrawCommand> commands = new ArrayList<UiDrawCommand>(2);
        commands.add(new UiRoundedRectCommand(
                8, button.x(), button.y(), button.width(), button.height(),
                3.0F, theme.color(UiColorRole.SURFACE_RAISED)));
        commands.add(new UiTextCommand(
                9, button.x() + 11.0F, button.y() + 4.0F,
                UiFonts.DEFAULT, "< Modules",
                theme.color(UiColorRole.TEXT_MUTED)));
        return commands;
    }

    static boolean hit(final UiBounds content, final float x, final float y) {
        final UiBounds button = bounds(content);
        return button != null && button.contains(x, y);
    }

    private static UiBounds bounds(final UiBounds content) {
        Objects.requireNonNull(content, "content");
        if (content.width() < 210.0F || content.height() < 100.0F) {
            return null;
        }
        return new UiBounds(
                content.x() + content.width() - RIGHT - WIDTH,
                content.y() + TOP, WIDTH, HEIGHT);
    }
}
