package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiFocusTarget;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiScrollEvent;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class SettingListPageContent
        implements ClickGuiPageContent {
    private static final float PADDING = 24.0F;
    private static final float TITLE_GAP = 34.0F;
    private static final float ROW_HEIGHT = 38.0F;
    private static final float ROW_GAP = 7.0F;
    private static final float ROW_RADIUS = 6.0F;
    private static final int MAX_TEXT_LENGTH = 128;

    private static final Comparator<Entry> ENTRY_ORDER =
            new Comparator<Entry>() {
                @Override
                public int compare(
                        final Entry left,
                        final Entry right) {
                    final int priority =
                            Integer.compare(
                                    left.descriptor.priority(),
                                    right.descriptor.priority());
                    if (priority != 0) {
                        return priority;
                    }
                    return left.setting.id().compareTo(
                            right.setting.id());
                }
            };

    private final SettingRegistry settings;
    private final SettingPresentationRegistry presentations;
    private final ClickGuiContentScrollState scroll =
            new ClickGuiContentScrollState();
    private final SettingEditorController editor =
            new SettingEditorController();

    public SettingListPageContent(
            final SettingRegistry settings,
            final SettingPresentationRegistry presentations) {
        this.settings = Objects.requireNonNull(
                settings,
                "settings");
        this.presentations = Objects.requireNonNull(
                presentations,
                "presentations");
    }

    @Override
    public List<UiDrawCommand> compose(
            final ClickGuiContentContext context) {
        Objects.requireNonNull(context, "context");

        final List<UiDrawCommand> commands =
                new ArrayList<UiDrawCommand>();
        final UiBounds bounds = context.bounds();
        final UiTheme theme = context.theme();
        final List<Entry> visible =
                entries(context.snapshot());
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

        for (int index = 0;
             index < visible.size();
             index++) {
            final Entry entry = visible.get(index);
            final UiBounds row =
                    rowBounds(
                            bounds,
                            index,
                            scrollOffset);

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
                                    UiColorRole.BACKGROUND)));

            commands.add(
                    new UiTextCommand(
                            0,
                            row.x() + 12.0F,
                            row.y() + 10.0F,
                            UiFonts.DEFAULT,
                            entry.descriptor.label(),
                            theme.color(
                                    UiColorRole.TEXT_PRIMARY)));

            final boolean editing =
                    editor.isEditing(entry.setting);
            commands.add(
                    new UiTextCommand(
                            0,
                            Math.max(
                                    row.x() + 12.0F,
                                    row.x()
                                            + row.width()
                                            - 130.0F),
                            row.y() + 10.0F,
                            UiFonts.DEFAULT,
                            editor.displayValue(
                                    entry.setting),
                            theme.color(
                                    editing
                                            ? UiColorRole.ACCENT
                                            : UiColorRole.TEXT_MUTED)));
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

        final List<Entry> visible =
                entries(context.snapshot());
        final float scrollOffset =
                scroll.offset(
                        contentHeight(
                                visible.size()),
                        context.bounds().height());
        for (int index = 0;
             index < visible.size();
             index++) {
            final Entry entry = visible.get(index);
            if (!rowBounds(
                    context.bounds(),
                    index,
                    scrollOffset)
                    .contains(
                            event.x(),
                            event.y())) {
                continue;
            }

            return editor.pointer(
                    context,
                    entry.setting,
                    entry.descriptor,
                    event);
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
                entries(
                        context.snapshot())
                        .size();
        scroll.scroll(
                event.deltaY(),
                contentHeight(count),
                context.bounds().height());
        return true;
    }

    private List<Entry> entries(
            final ClickGuiSnapshot snapshot) {
        final String query =
                snapshot.searchQuery()
                        .trim()
                        .toLowerCase(Locale.ROOT);
        final List<Entry> entries =
                new ArrayList<Entry>();

        for (Setting<?> setting :
                settings.snapshot()) {
            final SettingDescriptor descriptor =
                    presentations.find(
                            setting.id());
            if (descriptor == null) {
                continue;
            }

            if (!query.isEmpty()
                    && !matches(
                    setting,
                    descriptor,
                    query)) {
                continue;
            }

            entries.add(
                    new Entry(
                            setting,
                            descriptor));
        }

        Collections.sort(
                entries,
                ENTRY_ORDER);
        return entries;
    }

    private static UiBounds rowBounds(
            final UiBounds bounds,
            final int index,
            final float scrollOffset) {
        return new UiBounds(
                bounds.x() + PADDING,
                bounds.y()
                        + PADDING
                        + TITLE_GAP
                        + index
                        * (ROW_HEIGHT + ROW_GAP)
                        - scrollOffset,
                Math.max(
                        0.0F,
                        bounds.width()
                                - PADDING * 2.0F),
                ROW_HEIGHT);
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

    private static boolean matches(
            final Setting<?> setting,
            final SettingDescriptor descriptor,
            final String query) {
        return setting.id()
                .toLowerCase(Locale.ROOT)
                .contains(query)
                || descriptor.label()
                .toLowerCase(Locale.ROOT)
                .contains(query);
    }

    private static final class Entry {
        private final Setting<?> setting;
        private final SettingDescriptor descriptor;

        Entry(
                final Setting<?> setting,
                final SettingDescriptor descriptor) {
            this.setting = setting;
            this.descriptor = descriptor;
        }
    }
}
