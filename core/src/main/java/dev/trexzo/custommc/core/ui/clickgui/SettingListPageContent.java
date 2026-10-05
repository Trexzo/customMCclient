package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
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

        commands.add(
                new UiTextCommand(
                        0,
                        bounds.x() + PADDING,
                        bounds.y() + PADDING,
                        UiFonts.DEFAULT,
                        context.page().title(),
                        theme.color(
                                UiColorRole.TEXT_PRIMARY)));

        float y =
                bounds.y()
                        + PADDING
                        + TITLE_GAP;

        for (Entry entry :
                entries(context.snapshot())) {
            final UiBounds row =
                    new UiBounds(
                            bounds.x() + PADDING,
                            y,
                            Math.max(
                                    0.0F,
                                    bounds.width()
                                            - PADDING * 2.0F),
                            ROW_HEIGHT);

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
                            String.valueOf(
                                    entry.setting.get()),
                            theme.color(
                                    UiColorRole.TEXT_MUTED)));

            y += ROW_HEIGHT + ROW_GAP;
        }

        return commands;
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
