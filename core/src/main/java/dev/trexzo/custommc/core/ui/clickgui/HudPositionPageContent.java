package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.UnknownSettingPolicy;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiOutlineCommand;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTheme;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Real ClickGUI click-to-place/held-pointer page backed by native TargetHUD
 * settings. MOVE is forwarded only by the 1.8.9 bridge when the GUI is open
 * and the physical left button is held. This is not a standalone HUD drag.
 */
public final class HudPositionPageContent implements ClickGuiPageContent {
    public static final String X_ID = "render.targetHud.x";
    public static final String Y_ID = "render.targetHud.y";
    private static final String COMPACT_ID = "render.targetHud.compact";
    private static final String COORDINATES_ID = "render.targetHud.showCoordinates";
    private static final int MAX_COORDINATE = 4096;
    private static final float INSET = 24.0F;
    private static final float PREVIEW_TOP = 78.0F;
    private static final float PREVIEW_BOTTOM_SPACE = 98.0F;
    private static final float RESET_WIDTH = 116.0F;
    private static final float RESET_HEIGHT = 28.0F;

    private final SettingRegistry settings;

    public HudPositionPageContent(final SettingRegistry settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    @Override
    public List<UiDrawCommand> compose(final ClickGuiContentContext context) {
        Objects.requireNonNull(context, "context");
        final List<UiDrawCommand> commands = new ArrayList<UiDrawCommand>();
        final UiBounds outer = context.bounds();
        final UiTheme theme = context.theme();
        commands.add(new UiTextCommand(0, outer.x() + INSET,
                outer.y() + INSET, UiFonts.DEFAULT,
                "HUD Layout", theme.color(UiColorRole.TEXT_PRIMARY)));

        final Position position = position();
        if (position == null) {
            commands.add(new UiTextCommand(0, outer.x() + INSET,
                    outer.y() + PREVIEW_TOP, UiFonts.DEFAULT,
                    "TargetHUD positioning is unavailable",
                    theme.color(UiColorRole.TEXT_MUTED)));
            return commands;
        }
        final UiViewport viewport = context.viewport();
        if (viewport == null) {
            return commands;
        }
        final UiBounds canvas = previewBounds(outer, viewport);
        if (canvas == null) {
            return commands;
        }
        final float scale = canvas.width() / viewport.logicalWidth();
        final float cardWidth = cardWidth();
        final float cardHeight = cardHeight();
        final float nativeX = bounded(position.x, viewport.logicalWidth() - cardWidth);
        final float nativeY = bounded(position.y, viewport.logicalHeight() - cardHeight);
        final UiBounds card = new UiBounds(
                canvas.x() + nativeX * scale,
                canvas.y() + nativeY * scale,
                cardWidth * scale, cardHeight * scale);

        commands.add(new UiTextCommand(0, outer.x() + INSET,
                outer.y() + 48.0F, UiFonts.DEFAULT,
                "Click/hold to place  |  Shift + arrows to nudge",
                theme.color(UiColorRole.TEXT_MUTED)));
        commands.add(new UiRoundedRectCommand(0,
                canvas.x(), canvas.y(), canvas.width(), canvas.height(),
                5.0F, theme.color(UiColorRole.BACKGROUND)));
        commands.add(new UiOutlineCommand(1,
                canvas.x(), canvas.y(), canvas.width(), canvas.height(),
                1.0F, theme.color(UiColorRole.BORDER)));
        commands.add(new UiRoundedRectCommand(2,
                card.x(), card.y(), card.width(), card.height(),
                Math.min(4.0F, card.height() * 0.5F),
                theme.color(UiColorRole.SURFACE_RAISED)));
        commands.add(new UiOutlineCommand(3,
                card.x(), card.y(), card.width(), card.height(),
                1.0F, theme.color(UiColorRole.ACCENT)));
        commands.add(new UiTextCommand(0, outer.x() + INSET,
                canvas.y() + canvas.height() + 22.0F,
                UiFonts.DEFAULT,
                "TargetHUD    X: " + position.x + "    Y: " + position.y,
                theme.color(UiColorRole.TEXT_PRIMARY)));
        final UiBounds reset = resetBounds(canvas);
        commands.add(new UiRoundedRectCommand(0,
                reset.x(), reset.y(), reset.width(), reset.height(),
                5.0F, theme.color(UiColorRole.SURFACE_RAISED)));
        commands.add(new UiTextCommand(1, reset.x() + 8.0F,
                reset.y() + 8.0F, UiFonts.DEFAULT,
                "Reset Position", theme.color(UiColorRole.TEXT_PRIMARY)));
        return commands;
    }

    @Override
    public boolean pointer(final ClickGuiContentInputContext context,
            final UiPointerEvent event) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(event, "event");
        if ((event.action() != UiPointerAction.PRESS
                && event.action() != UiPointerAction.MOVE)
                || event.button() != UiPointerButton.LEFT
                || position() == null || context.viewport() == null) {
            return false;
        }
        final UiViewport viewport = context.viewport();
        final UiBounds canvas = previewBounds(context.bounds(), viewport);
        if (canvas == null) {
            return false;
        }
        if (event.action() == UiPointerAction.PRESS
                && resetBounds(canvas).contains(event.x(), event.y())) {
            final Setting<?> x = settings.find(X_ID);
            final Setting<?> y = settings.find(Y_ID);
            return writePosition((Integer) x.defaultValue(),
                    (Integer) y.defaultValue());
        }
        if (!canvas.contains(event.x(), event.y())) {
            return false;
        }

        final double scale = (double) canvas.width()
                / viewport.logicalWidth();
        final double pointerX = ((double) event.x() - canvas.x()) / scale;
        final double pointerY = ((double) event.y() - canvas.y()) / scale;
        final int limitX = (int) Math.max(0.0D,
                Math.min(MAX_COORDINATE, viewport.logicalWidth() - cardWidth()));
        final int limitY = (int) Math.max(0.0D,
                Math.min(MAX_COORDINATE, viewport.logicalHeight() - cardHeight()));
        final int x = (int) Math.round(Math.max(0.0D,
                Math.min(limitX, pointerX - cardWidth() * 0.5D)));
        final int y = (int) Math.round(Math.max(0.0D,
                Math.min(limitY, pointerY - cardHeight() * 0.5D)));
        return writePosition(x, y);
    }

    /**
     * Accessible mouse-free positioning even if native vanilla currently
     * owns the grabbed cursor. Ctrl+Shift uses one logical pixel.
     */
    @Override
    public boolean key(final UiKeyEvent event) {
        Objects.requireNonNull(event, "event");
        if (event.action() != UiKeyAction.PRESS
                || !event.shift() || event.alt()) {
            return false;
        }
        final Position current = position();
        if (current == null) {
            return false;
        }
        final int step = event.control() ? 1 : 4;
        final int dx;
        final int dy;
        if (UiKeys.LEFT.equals(event.key())) {
            dx = -step;
            dy = 0;
        } else if (UiKeys.RIGHT.equals(event.key())) {
            dx = step;
            dy = 0;
        } else if (UiKeys.UP.equals(event.key())) {
            dx = 0;
            dy = -step;
        } else if (UiKeys.DOWN.equals(event.key())) {
            dx = 0;
            dy = step;
        } else {
            return false;
        }
        final int x = Math.max(0, Math.min(MAX_COORDINATE, current.x + dx));
        final int y = Math.max(0, Math.min(MAX_COORDINATE, current.y + dy));
        return writePosition(x, y);
    }

    private boolean writePosition(final int x, final int y) {
        final Position current = position();
        if (current != null && current.x == x && current.y == y) {
            return true;
        }
        final Map<String, String> patch = new LinkedHashMap<String, String>();
        patch.put(X_ID, Integer.toString(x));
        patch.put(Y_ID, Integer.toString(y));
        try {
            // Registry validates both persistent values before setting either.
            settings.applyEncoded(patch, UnknownSettingPolicy.REJECT);
            return true;
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    private Position position() {
        final Setting<?> x = settings.find(X_ID);
        final Setting<?> y = settings.find(Y_ID);
        if (x == null || y == null
                || !(x.get() instanceof Integer)
                || !(y.get() instanceof Integer)
                || !(x.defaultValue() instanceof Integer)
                || !(y.defaultValue() instanceof Integer)
                || !x.isPersistent() || !y.isPersistent()) {
            return null;
        }
        return new Position((Integer) x.get(), (Integer) y.get());
    }

    private float cardWidth() {
        final boolean compact = flag(COMPACT_ID);
        final boolean coordinates = flag(COORDINATES_ID);
        return coordinates
                ? (compact ? 208.0F : 224.0F)
                : (compact ? 142.0F : 164.0F);
    }

    private float cardHeight() {
        final boolean compact = flag(COMPACT_ID);
        final boolean coordinates = flag(COORDINATES_ID);
        return coordinates
                ? (compact ? 54.0F : 72.0F)
                : (compact ? 38.0F : 56.0F);
    }

    private boolean flag(final String id) {
        final Setting<?> setting = settings.find(id);
        return setting != null && Boolean.TRUE.equals(setting.get());
    }

    public static UiBounds previewBounds(
            final UiBounds outer, final UiViewport viewport) {
        Objects.requireNonNull(outer, "outer");
        Objects.requireNonNull(viewport, "viewport");
        final float maxWidth = Math.max(0.0F, outer.width() - 2.0F * INSET);
        final float maxHeight = Math.max(0.0F,
                outer.height() - PREVIEW_TOP - PREVIEW_BOTTOM_SPACE);
        if (maxWidth < 120.0F || maxHeight < 60.0F) {
            return null;
        }
        final double scale = Math.min(
                (double) maxWidth / viewport.logicalWidth(),
                (double) maxHeight / viewport.logicalHeight());
        if (!Double.isFinite(scale) || scale <= 0.0D) {
            return null;
        }
        final float width = (float) (viewport.logicalWidth() * scale);
        final float height = (float) (viewport.logicalHeight() * scale);
        return new UiBounds(
                outer.x() + (outer.width() - width) * 0.5F,
                outer.y() + PREVIEW_TOP, width, height);
    }

    public static UiBounds resetBounds(final UiBounds canvas) {
        Objects.requireNonNull(canvas, "canvas");
        return new UiBounds(
                canvas.x() + (canvas.width() - RESET_WIDTH) * 0.5F,
                canvas.y() + canvas.height() + 38.0F,
                RESET_WIDTH, RESET_HEIGHT);
    }

    private static float bounded(final float desired, final float max) {
        return Math.max(0.0F, Math.min(desired, Math.max(0.0F, max)));
    }

    private static final class Position {
        final int x;
        final int y;
        Position(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }
}
