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
 * A quiet, fixed module identity when the normal detail header has scrolled
 * entirely out of sight. Source settings rows and their hitboxes never move.
 * The same fixed geometry is used by pointer hit-testing to prevent clicking
 * an obscured control behind the strip.
 */
final class UiModuleDetailStickyHeader {
    static final float SHOWN_AFTER = 102.0F; // 24px inset + 78px detail header
    private static final float HEIGHT = 26.0F;
    private static final float LEFT_INSET = 24.0F;
    private static final float BACK_RESERVED = 139.0F;

    private UiModuleDetailStickyHeader() {
    }

    static UiBounds activeBounds(final UiBounds content, final float scroll) {
        Objects.requireNonNull(content, "content");
        if (!Float.isFinite(scroll)) {
            throw new IllegalArgumentException("scroll must be finite");
        }
        if (scroll < SHOWN_AFTER
                || content.width() < 210.0F
                || content.height() < 100.0F) {
            return null;
        }
        return new UiBounds(content.x(), content.y(),
                content.width(), HEIGHT);
    }

    static boolean blocksPointer(final UiBounds content,
            final float scroll, final float x, final float y) {
        final UiBounds header = activeBounds(content, scroll);
        return header != null && header.contains(x, y);
    }

    static List<UiDrawCommand> compose(final UiBounds content,
            final float scroll, final String moduleName,
            final int settingsCount, final ModuleState state,
            final UiTheme theme) {
        Objects.requireNonNull(moduleName, "moduleName");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(theme, "theme");
        if (settingsCount < 0) {
            throw new IllegalArgumentException("settingsCount must be nonnegative");
        }
        final UiBounds header = activeBounds(content, scroll);
        if (header == null) return Collections.emptyList();

        final List<UiDrawCommand> commands = new ArrayList<UiDrawCommand>(4);
        commands.add(new UiRectCommand(7,
                header.x(), header.y(), header.width(), header.height(),
                theme.color(UiColorRole.SURFACE)));
        commands.add(new UiRectCommand(8,
                header.x() + LEFT_INSET - 7.0F, header.y() + 7.0F,
                2.0F, 12.0F,
                theme.color(stateRole(state))));
        final float textWidth = Math.max(0.0F,
                header.width() - LEFT_INSET - BACK_RESERVED);
        if (textWidth > 0.0F) {
            commands.add(new UiClipCommand(9,
                    new UiBounds(header.x() + LEFT_INSET, header.y() + 5.0F,
                            textWidth, 15.0F),
                    Collections.<UiDrawCommand>singletonList(
                            new UiTextCommand(0,
                                    header.x() + LEFT_INSET, header.y() + 8.0F,
                                    UiFonts.DEFAULT,
                                    moduleName + "  /  " + settingsCount + " settings",
                                    theme.color(UiColorRole.TEXT_PRIMARY)))));
        }
        commands.add(new UiRectCommand(8,
                header.x(), header.y() + header.height() - 1.0F,
                header.width(), 1.0F, theme.color(UiColorRole.BORDER)));
        return commands;
    }

    private static UiColorRole stateRole(final ModuleState state) {
        switch (state) {
            case ENABLED:
                return UiColorRole.ACCENT;
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
}
