package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleCategoryDescriptor;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiOutlineCommand;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiScrollEvent;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ModuleListPageContent
        implements ClickGuiPageContent {
    private static final float PADDING = 24.0F;
    private static final float TITLE_GAP = 34.0F;
    private static final float CATEGORY_HEADER_HEIGHT = 22.0F;
    private static final float GROUP_GAP = 10.0F;
    private static final float ROW_HEIGHT = 36.0F;
    private static final float ROW_GAP = 7.0F;
    private static final float ROW_RADIUS = 6.0F;

    private final ModuleRegistry modules;
    private final ModuleController controller;
    private final ModulePresentationRegistry presentations;
    private final ModuleCategoryRegistry categories;
    private final ModuleSelectionModel selection;
    private final ClickGuiContentScrollState scroll =
            new ClickGuiContentScrollState();

    public ModuleListPageContent(
            final ModuleRegistry modules,
            final ModuleController controller) {
        this(
                modules,
                controller,
                new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                null);
    }

    public ModuleListPageContent(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations) {
        this(
                modules,
                controller,
                presentations,
                new ModuleCategoryRegistry(),
                null);
    }

    public ModuleListPageContent(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleCategoryRegistry categories) {
        this(
                modules,
                controller,
                presentations,
                categories,
                null);
    }

    public ModuleListPageContent(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleCategoryRegistry categories,
            final ModuleSelectionModel selection) {
        this.modules =
                Objects.requireNonNull(
                        modules,
                        "modules");
        this.controller =
                Objects.requireNonNull(
                        controller,
                        "controller");
        this.presentations =
                Objects.requireNonNull(
                        presentations,
                        "presentations");
        this.categories =
                Objects.requireNonNull(
                        categories,
                        "categories");
        this.selection = selection;
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

        final List<Entry> visible =
                filteredModules(
                        context.snapshot());
        final boolean categoryHeaders =
                hasCategoryHeaders(visible);
        final float scrollOffset =
                scroll.offset(
                        contentHeight(
                                visible,
                                categoryHeaders),
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
        String currentCategory = null;

        for (Entry entry : visible) {
            if (categoryHeaders
                    && !entry.categoryId()
                    .equals(currentCategory)) {
                if (currentCategory != null) {
                    y += GROUP_GAP;
                }
                currentCategory =
                        entry.categoryId();
                commands.add(
                        new UiTextCommand(
                                0,
                                bounds.x() + PADDING,
                                y + 4.0F,
                                UiFonts.DEFAULT,
                                categoryDisplayName(
                                        currentCategory),
                                theme.color(
                                        UiColorRole.TEXT_MUTED)));
                y += CATEGORY_HEADER_HEIGHT;
            }

            final String id =
                    entry.module.id();
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

            if (selection != null
                    && selection.isSelected(id)) {
                commands.add(
                        new UiOutlineCommand(
                                1,
                                row.x(),
                                row.y(),
                                row.width(),
                                row.height(),
                                1.0F,
                                theme.color(
                                        UiColorRole.ACCENT)));
            }

            commands.add(
                    new UiTextCommand(
                            0,
                            row.x() + 12.0F,
                            row.y() + 10.0F,
                            UiFonts.DEFAULT,
                            entry.displayName(),
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

        if (event.action() != UiPointerAction.PRESS) {
            return false;
        }
        if (event.button() != UiPointerButton.LEFT
                && event.button() != UiPointerButton.RIGHT) {
            return false;
        }

        final List<Entry> visible =
                filteredModules(
                        context.snapshot());
        final boolean categoryHeaders =
                hasCategoryHeaders(visible);
        final float scrollOffset =
                scroll.offset(
                        contentHeight(
                                visible,
                                categoryHeaders),
                        context.bounds().height());
        float y =
                firstRowY(
                        context.bounds(),
                        scrollOffset);
        String currentCategory = null;

        for (Entry entry : visible) {
            if (categoryHeaders
                    && !entry.categoryId()
                    .equals(currentCategory)) {
                if (currentCategory != null) {
                    y += GROUP_GAP;
                }
                currentCategory =
                        entry.categoryId();
                y += CATEGORY_HEADER_HEIGHT;
            }

            final UiBounds row =
                    rowBounds(
                            context.bounds(),
                            y);

            if (row.contains(
                    event.x(),
                    event.y())) {
                if (event.button()
                        == UiPointerButton.LEFT) {
                    return toggle(
                            entry.module.id());
                }
                if (selection == null) {
                    return false;
                }
                selection.select(
                        entry.module.id());
                return true;
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

        final List<Entry> visible =
                filteredModules(
                        context.snapshot());
        scroll.scroll(
                event.deltaY(),
                contentHeight(
                        visible,
                        hasCategoryHeaders(visible)),
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

    private List<Entry> filteredModules(
            final ClickGuiSnapshot snapshot) {
        final String query =
                snapshot.searchQuery()
                        .trim()
                        .toLowerCase(Locale.ROOT);
        final List<Entry> filtered =
                new ArrayList<Entry>();

        int registrationIndex = 0;
        for (Module module : modules.snapshot()) {
            final ModuleDescriptor descriptor =
                    presentations.find(
                            module.id());
            if (query.isEmpty()
                    || matches(
                    module,
                    descriptor,
                    query)) {
                filtered.add(
                        new Entry(
                                module,
                                descriptor,
                                registrationIndex));
            }
            registrationIndex++;
        }

        Collections.sort(
                filtered,
                new Comparator<Entry>() {
                    @Override
                    public int compare(
                            final Entry left,
                            final Entry right) {
                        final int category =
                                compareCategories(
                                        left.categoryId(),
                                        right.categoryId());
                        if (category != 0) {
                            return category;
                        }

                        final int priority =
                                Integer.compare(
                                        left.priority(),
                                        right.priority());
                        if (priority != 0) {
                            return priority;
                        }

                        return Integer.compare(
                                left.registrationIndex,
                                right.registrationIndex);
                    }
                });
        return filtered;
    }

    private boolean matches(
            final Module module,
            final ModuleDescriptor descriptor,
            final String query) {
        if (module.id()
                .toLowerCase(Locale.ROOT)
                .contains(query)) {
            return true;
        }
        if (descriptor == null) {
            return false;
        }
        if (descriptor.displayName()
                .toLowerCase(Locale.ROOT)
                .contains(query)
                || descriptor.description()
                .toLowerCase(Locale.ROOT)
                .contains(query)
                || descriptor.categoryId()
                .toLowerCase(Locale.ROOT)
                .contains(query)) {
            return true;
        }

        final ModuleCategoryDescriptor category =
                categories.find(
                        descriptor.categoryId());
        return category != null
                && category.displayName()
                .toLowerCase(Locale.ROOT)
                .contains(query);
    }

    private int compareCategories(
            final String leftId,
            final String rightId) {
        if (leftId.equals(rightId)) {
            return 0;
        }

        final int priority =
                Integer.compare(
                        categoryPriority(leftId),
                        categoryPriority(rightId));
        if (priority != 0) {
            return priority;
        }
        return leftId.compareTo(rightId);
    }

    private int categoryPriority(
            final String categoryId) {
        final ModuleCategoryDescriptor descriptor =
                categories.find(categoryId);
        if (descriptor != null) {
            return descriptor.priority();
        }
        if (ModuleDescriptor.DEFAULT_CATEGORY_ID
                .equals(categoryId)) {
            return 0;
        }
        return Integer.MAX_VALUE;
    }

    private String categoryDisplayName(
            final String categoryId) {
        final ModuleCategoryDescriptor descriptor =
                categories.find(categoryId);
        if (descriptor != null) {
            return descriptor.displayName();
        }
        if (ModuleDescriptor.DEFAULT_CATEGORY_ID
                .equals(categoryId)) {
            return "General";
        }
        return categoryId;
    }

    private boolean hasCategoryHeaders(
            final List<Entry> entries) {
        for (Entry entry : entries) {
            if (!ModuleDescriptor.DEFAULT_CATEGORY_ID
                    .equals(entry.categoryId())
                    || categories.find(
                    entry.categoryId()) != null) {
                return true;
            }
        }
        return false;
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
            final List<Entry> entries,
            final boolean categoryHeaders) {
        float height =
                PADDING
                        + TITLE_GAP
                        + PADDING;
        if (entries.isEmpty()) {
            return height;
        }

        String currentCategory = null;
        for (Entry entry : entries) {
            if (categoryHeaders
                    && !entry.categoryId()
                    .equals(currentCategory)) {
                if (currentCategory != null) {
                    height += GROUP_GAP;
                }
                currentCategory =
                        entry.categoryId();
                height += CATEGORY_HEADER_HEIGHT;
            }
            height += ROW_HEIGHT + ROW_GAP;
        }

        return height - ROW_GAP;
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

    private static final class Entry {
        private final Module module;
        private final ModuleDescriptor descriptor;
        private final int registrationIndex;

        Entry(
                final Module module,
                final ModuleDescriptor descriptor,
                final int registrationIndex) {
            this.module = module;
            this.descriptor = descriptor;
            this.registrationIndex = registrationIndex;
        }

        String displayName() {
            return descriptor == null
                    ? module.id()
                    : descriptor.displayName();
        }

        String categoryId() {
            return descriptor == null
                    ? ModuleDescriptor.DEFAULT_CATEGORY_ID
                    : descriptor.categoryId();
        }

        int priority() {
            return descriptor == null
                    ? 0
                    : descriptor.priority();
        }
    }
}
