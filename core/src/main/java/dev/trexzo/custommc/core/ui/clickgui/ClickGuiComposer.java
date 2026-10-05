package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiCommandBuffer;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiOutlineCommand;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTheme;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ClickGuiComposer {
    private static final float ROOT_RADIUS = 12.0F;
    private static final float CONTROL_RADIUS = 6.0F;
    private static final float PAGE_HEIGHT = 30.0F;
    private static final float PAGE_GAP = 4.0F;

    private final ClickGuiLayoutEngine layoutEngine;

    public ClickGuiComposer() {
        this(new ClickGuiLayoutEngine());
    }

    public ClickGuiComposer(
            final ClickGuiLayoutEngine layoutEngine) {
        this.layoutEngine =
                Objects.requireNonNull(
                        layoutEngine,
                        "layoutEngine");
    }

    public List<UiDrawCommand> compose(
            final ClickGuiSnapshot snapshot,
            final UiViewport viewport,
            final UiTheme theme) {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(viewport, "viewport");
        Objects.requireNonNull(theme, "theme");

        if (!snapshot.open()) {
            return Collections.emptyList();
        }

        final ClickGuiLayout layout =
                layoutEngine.layout(viewport);
        final UiCommandBuffer commands =
                new UiCommandBuffer();

        final UiBounds root = layout.root();
        commands.add(
                new UiRoundedRectCommand(
                        0,
                        root.x(),
                        root.y(),
                        root.width(),
                        root.height(),
                        radiusFor(root, ROOT_RADIUS),
                        theme.color(UiColorRole.SURFACE)));

        commands.add(
                new UiOutlineCommand(
                        1,
                        root.x(),
                        root.y(),
                        root.width(),
                        root.height(),
                        1.0F,
                        theme.color(UiColorRole.BORDER)));

        final UiBounds sidebar = layout.sidebar();
        commands.add(
                new UiRectCommand(
                        2,
                        sidebar.x(),
                        sidebar.y(),
                        sidebar.width(),
                        sidebar.height(),
                        theme.color(
                                UiColorRole.BACKGROUND)));

        commands.add(
                new UiTextCommand(
                        3,
                        sidebar.x() + 18.0F,
                        sidebar.y() + 18.0F,
                        UiFonts.DEFAULT,
                        "Client",
                        theme.color(
                                UiColorRole.TEXT_PRIMARY)));

        final UiBounds search = layout.search();
        commands.add(
                new UiRoundedRectCommand(
                        4,
                        search.x(),
                        search.y(),
                        search.width(),
                        search.height(),
                        radiusFor(search, CONTROL_RADIUS),
                        theme.color(
                                UiColorRole.SURFACE_RAISED)));

        final String searchText =
                snapshot.searchQuery().isEmpty()
                        ? "Search modules"
                        : snapshot.searchQuery();
        commands.add(
                new UiTextCommand(
                        5,
                        search.x() + 10.0F,
                        search.y() + 9.0F,
                        UiFonts.DEFAULT,
                        searchText,
                        theme.color(
                                snapshot.searchQuery().isEmpty()
                                        ? UiColorRole.TEXT_MUTED
                                        : UiColorRole.TEXT_PRIMARY)));

        commands.add(
                new UiClipCommand(
                        6,
                        layout.navigation(),
                        pageCommands(
                                snapshot,
                                layout.navigation(),
                                theme)));

        final ClickGuiPage selected =
                selectedPage(snapshot);
        final UiBounds content = layout.content();

        commands.add(
                new UiTextCommand(
                        7,
                        content.x() + 24.0F,
                        content.y() + 24.0F,
                        UiFonts.DEFAULT,
                        selected == null
                                ? "No page selected"
                                : selected.title(),
                        theme.color(
                                UiColorRole.TEXT_PRIMARY)));

        return commands.seal();
    }

    private static List<UiDrawCommand> pageCommands(
            final ClickGuiSnapshot snapshot,
            final UiBounds navigation,
            final UiTheme theme) {
        final List<UiDrawCommand> commands =
                new ArrayList<UiDrawCommand>();
        float y = navigation.y();

        for (ClickGuiPage page : snapshot.pages()) {
            final boolean selected =
                    page.id().equals(
                            snapshot.selectedPageId());

            if (selected) {
                commands.add(
                        new UiRoundedRectCommand(
                                0,
                                navigation.x(),
                                y,
                                navigation.width(),
                                PAGE_HEIGHT,
                                radiusFor(
                                        new UiBounds(
                                                navigation.x(),
                                                y,
                                                navigation.width(),
                                                PAGE_HEIGHT),
                                        CONTROL_RADIUS),
                                theme.color(
                                        UiColorRole.SURFACE_RAISED)));
                commands.add(
                        new UiRectCommand(
                                0,
                                navigation.x(),
                                y + 5.0F,
                                2.0F,
                                PAGE_HEIGHT - 10.0F,
                                theme.color(
                                        UiColorRole.ACCENT)));
            }

            commands.add(
                    new UiTextCommand(
                            0,
                            navigation.x() + 10.0F,
                            y + 9.0F,
                            UiFonts.DEFAULT,
                            page.title(),
                            theme.color(
                                    selected
                                            ? UiColorRole.TEXT_PRIMARY
                                            : UiColorRole.TEXT_MUTED)));

            y += PAGE_HEIGHT + PAGE_GAP;
        }

        return commands;
    }

    private static ClickGuiPage selectedPage(
            final ClickGuiSnapshot snapshot) {
        final String selectedId =
                snapshot.selectedPageId();
        if (selectedId == null) {
            return null;
        }

        for (ClickGuiPage page : snapshot.pages()) {
            if (selectedId.equals(page.id())) {
                return page;
            }
        }
        return null;
    }

    private static float radiusFor(
            final UiBounds bounds,
            final float desired) {
        return Math.min(
                desired,
                Math.min(
                        bounds.width(),
                        bounds.height()) * 0.5F);
    }
}
