package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiScrollEvent;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ModuleListPageContent
        implements ClickGuiPageContent {
    private static final float PADDING = 24.0F;
    private static final float TITLE_GAP = 34.0F;
    private static final float ROW_HEIGHT = 36.0F;
    private static final float ROW_GAP = 7.0F;
    private static final float ROW_RADIUS = 6.0F;

    private final ModuleRegistry modules;
    private final ModuleController controller;
    private final ClickGuiContentScrollState scroll =
            new ClickGuiContentScrollState();

    public ModuleListPageContent(
            final ModuleRegistry modules,
            final ModuleController controller) {
        this.modules =
                Objects.requireNonNull(
                        modules,
                        "modules");
        this.controller =
                Objects.requireNonNull(
                        controller,
                        "controller");
    }

    @Override
    public List<UiDrawCommand> compose(
            final ClickGuiContentContext context) {
        Objects.requireNonNull(context, "context");

        final List<UiDrawCommand> commands =
                new ArrayList<UiDrawCommand>();
        final UiBounds bounds =
                context.bounds();
        final UiTheme theme =
                context.theme();

        final List<Module> visible =
                filteredModules(
                        context.snapshot());
        final float scrollOffset =
                scroll.offset(
                        contentHeight(
                                visible.size()),
                        bounds.height());

        commands.add(
                new UiTextCommand(
                        0,
                        bounds.x() + PADDING,
                        bounds.y() + PADDING - scrollOffset,
                        UiFonts.DEFAULT,
                        context.page().title(),
                        theme.color(
                                UiColorRole.TEXT_PRIMARY)));

        float y =
                firstRowY(
                        bounds,
                        scrollOffset);

        for (Module module : visible) {
            final String id =
                    module.id();
            final ModuleState state =
                    controller.stateOf(id);
            final UiBounds row =
                    rowBounds(
                            bounds,
                            y);

            commands.add(
                    new UiRoundedRectCommand(
                            0,
                            row.x(),
                            row.y(),
                            row.width(),
                            row.height(),
                            Math.min(
                                    ROW_RADIUS,
                                    row.height() * 0.5F),
                            theme.color(
                                    state == ModuleState.ENABLED
                                            ? UiColorRole.SURFACE_RAISED
                                            : UiColorRole.BACKGROUND)));

            commands.add(
                    new UiTextCommand(
                            0,
                            row.x() + 12.0F,
                            row.y() + 10.0F,
                            UiFonts.DEFAULT,
                            id,
                            theme.color(
                                    UiColorRole.TEXT_PRIMARY)));

            commands.add(
                    new UiTextCommand(
                            0,
                            Math.max(
                                    row.x() + 12.0F,
                                    row.x()
                                            + row.width()
                                            - 72.0F),
                            row.y() + 10.0F,
                            UiFonts.DEFAULT,
                            state.name(),
                            theme.color(
                                    stateColorRole(state))));

            y += ROW_HEIGHT + ROW_GAP;
        }

        return commands;
    }

    @Override
    public boolean pointer(
            final ClickGuiContentInputContext context,
            final UiPointerEvent event) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(event, "event");

        if (event.action() != UiPointerAction.PRESS
                || event.button() != UiPointerButton.LEFT) {
            return false;
        }

        final List<Module> visible =
                filteredModules(
                        context.snapshot());
        final float scrollOffset =
                scroll.offset(
                        contentHeight(
                                visible.size()),
                        context.bounds().height());
        float y =
                firstRowY(
                        context.bounds(),
                        scrollOffset);

        for (Module module : visible) {
            final UiBounds row =
                    rowBounds(
                            context.bounds(),
                            y);

            if (row.contains(
                    event.x(),
                    event.y())) {
                return toggle(
                        module.id());
            }

            y += ROW_HEIGHT + ROW_GAP;
        }

        return false;
    }

    @Override
    public boolean scroll(
            final ClickGuiContentInputContext context,
            final UiScrollEvent event) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(event, "event");

        final int count =
                filteredModules(
                        context.snapshot())
                        .size();
        scroll.scroll(
                event.deltaY(),
                contentHeight(count),
                context.bounds().height());
        return true;
    }

    private boolean toggle(final String id) {
        final ModuleState state =
                controller.stateOf(id);
        switch (state) {
            case DISABLED:
                controller.enable(id);
                return true;
            case ENABLED:
            case FAILED:
                controller.disable(id);
                return true;
            case ENABLING:
            case DISABLING:
            default:
                return false;
        }
    }

    private List<Module> filteredModules(
            final ClickGuiSnapshot snapshot) {
        final String query =
                snapshot.searchQuery()
                        .trim()
                        .toLowerCase(Locale.ROOT);
        final List<Module> filtered =
                new ArrayList<Module>();

        for (Module module : modules.snapshot()) {
            if (query.isEmpty()
                    || module.id()
                    .toLowerCase(Locale.ROOT)
                    .contains(query)) {
                filtered.add(module);
            }
        }
        return filtered;
    }

    private static float firstRowY(
            final UiBounds bounds,
            final float scrollOffset) {
        return bounds.y()
                + PADDING
                + TITLE_GAP
                - scrollOffset;
    }

    private static float contentHeight(
            final int rowCount) {
        if (rowCount <= 0) {
            return PADDING
                    + TITLE_GAP
                    + PADDING;
        }
        return PADDING
                + TITLE_GAP
                + rowCount * ROW_HEIGHT
                + (rowCount - 1) * ROW_GAP
                + PADDING;
    }

    private static UiBounds rowBounds(
            final UiBounds bounds,
            final float y) {
        return new UiBounds(
                bounds.x() + PADDING,
                y,
                Math.max(
                        0.0F,
                        bounds.width()
                                - PADDING * 2.0F),
                ROW_HEIGHT);
    }

    private static UiColorRole stateColorRole(
            final ModuleState state) {
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
}
