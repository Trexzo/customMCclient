package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
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
 * Source-rendered module detail header, with explicit title, status,
 * description and keybind scissor regions. This class does not handle input
 * or synthesize module state.
 */
final class UiModuleDetailHeader {
    private static final float PADDING = 24.0F;
    private static final float STATUS_WIDTH = 90.0F;
    private static final float HEADER_HEIGHT = 78.0F;

    private UiModuleDetailHeader() {
    }

    static List<UiDrawCommand> compose(
            final UiBounds content,
            final float scrollOffset,
            final String name,
            final ModuleState state,
            final String description,
            final String bindText,
            final boolean capturing,
            final UiTheme theme) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(theme, "theme");
        if (!Float.isFinite(scrollOffset)) {
            throw new IllegalArgumentException("scrollOffset must be finite");
        }
        final List<UiDrawCommand> commands = new ArrayList<UiDrawCommand>();
        final float x = content.x() + PADDING;
        final float y = content.y() + PADDING - scrollOffset;
        final float available = Math.max(0.0F,
                content.width() - PADDING * 2.0F);

        addText(commands, x, y - 2.0F, available, 18.0F,
                name, UiColorRole.TEXT_PRIMARY, theme);

        addText(commands, x, y + 19.0F,
                Math.min(STATUS_WIDTH - 4.0F, available), 17.0F,
                state.name(), stateRole(state), theme);

        if (description != null && !description.isEmpty()
                && available > STATUS_WIDTH) {
            addText(commands, x + STATUS_WIDTH, y + 19.0F,
                    available - STATUS_WIDTH, 17.0F,
                    description, UiColorRole.TEXT_MUTED, theme);
        }

        if (bindText != null) {
            // Align the visual keybind text with the pre-existing native
            // 28px clickable capture rectangle at content+PADDING+38.
            addText(commands, x, y + 38.0F,
                    Math.min(280.0F, available), 28.0F,
                    bindText,
                    capturing ? UiColorRole.ACCENT
                            : UiColorRole.TEXT_MUTED,
                    theme,
                    6.0F);
        }

        if (available > 0.0F) {
            commands.add(new UiRectCommand(
                    0, x, y + HEADER_HEIGHT - 9.0F,
                    available, 1.0F, theme.color(UiColorRole.BORDER)));
        }
        return commands;
    }

    private static UiColorRole stateRole(final ModuleState state) {
        switch (state) {
            case ENABLED:
                return UiColorRole.POSITIVE;
            case FAILED:
                return UiColorRole.DANGER;
            case ENABLING:
            case DISABLING:
                return UiColorRole.WARNING;
            case DISABLED:
            default:
                return UiColorRole.TEXT_MUTED;
        }
    }

    private static void addText(
            final List<UiDrawCommand> commands,
            final float x,
            final float y,
            final float width,
            final float height,
            final String content,
            final UiColorRole color,
            final UiTheme theme) {
        addText(commands, x, y, width, height,
                content, color, theme, 2.0F);
    }

    private static void addText(
            final List<UiDrawCommand> commands,
            final float x,
            final float y,
            final float width,
            final float height,
            final String content,
            final UiColorRole color,
            final UiTheme theme,
            final float topInset) {
        if (width <= 0.0F || height <= 0.0F) {
            return;
        }
        commands.add(new UiClipCommand(
                1, new UiBounds(x, y, width, height),
                Collections.<UiDrawCommand>singletonList(
                        new UiTextCommand(0,
                                x, y + topInset,
                                UiFonts.DEFAULT, content, theme.color(color)))));
    }
}
