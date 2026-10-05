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
    private TextEditSession textEdit;

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

        final List<Entry> visible =
                entries(context.snapshot());
        for (int index = 0;
             index < visible.size();
             index++) {
            final Entry entry = visible.get(index);
            final UiBounds row =
                    rowBounds(
                            bounds,
                            index);

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
                    isEditing(entry.setting);
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
                            displayValue(
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
        for (int index = 0;
             index < visible.size();
             index++) {
            final Entry entry = visible.get(index);
            if (!rowBounds(
                    context.bounds(),
                    index)
                    .contains(
                            event.x(),
                            event.y())) {
                continue;
            }

            if (entry.descriptor.kind()
                    == SettingValueKind.BOOLEAN) {
                if (event.button()
                        != UiPointerButton.LEFT) {
                    return false;
                }
                return toggleBoolean(
                        entry.setting);
            }

            if (entry.descriptor.kind()
                    == SettingValueKind.INTEGER) {
                return adjustInteger(
                        entry.setting,
                        entry.descriptor.numericSpec(),
                        event.button());
            }

            if (entry.descriptor.kind()
                    == SettingValueKind.DOUBLE) {
                return adjustDouble(
                        entry.setting,
                        entry.descriptor.numericSpec(),
                        event.button());
            }

            if (entry.descriptor.kind()
                    == SettingValueKind.TEXT) {
                if (event.button()
                        != UiPointerButton.LEFT) {
                    return false;
                }
                return beginTextEdit(
                        context,
                        entry.setting);
            }

            return false;
        }

        return false;
    }

    private boolean beginTextEdit(
            final ClickGuiContentInputContext context,
            final Setting<?> setting) {
        final UiFocusManager focusManager =
                context.focusManager();
        if (focusManager == null) {
            return false;
        }

        final Object current = setting.get();
        if (!(current instanceof String)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final Setting<String> textSetting =
                (Setting<String>) setting;

        final TextEditSession session =
                new TextEditSession(
                        focusManager,
                        textSetting,
                        "clickgui.setting-text."
                                + context.page().id()
                                + "."
                                + setting.id(),
                        (String) current);

        final UiFocusManager.Registration registration;
        try {
            registration =
                    focusManager.register(session);
        } catch (IllegalArgumentException duplicate) {
            return false;
        }

        session.attach(registration);
        textEdit = session;

        if (!focusManager.requestFocus(
                session.id())) {
            session.dispose();
            return false;
        }
        return true;
    }

    private boolean isEditing(
            final Setting<?> setting) {
        return textEdit != null
                && textEdit.setting() == setting;
    }

    private String displayValue(
            final Setting<?> setting) {
        if (isEditing(setting)) {
            return textEdit.draft() + "|";
        }
        return String.valueOf(setting.get());
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
            final int index) {
        return new UiBounds(
                bounds.x() + PADDING,
                bounds.y()
                        + PADDING
                        + TITLE_GAP
                        + index
                        * (ROW_HEIGHT + ROW_GAP),
                Math.max(
                        0.0F,
                        bounds.width()
                                - PADDING * 2.0F),
                ROW_HEIGHT);
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

    private static boolean adjustDouble(
            final Setting<?> setting,
            final SettingNumericSpec numericSpec,
            final UiPointerButton button) {
        if (numericSpec == null) {
            return false;
        }
        final int direction;
        if (button == UiPointerButton.LEFT) {
            direction = 1;
        } else if (button == UiPointerButton.RIGHT) {
            direction = -1;
        } else {
            return false;
        }

        final Object current = setting.get();
        if (!(current instanceof Double)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final Setting<Double> doubleSetting =
                (Setting<Double>) setting;
        final double next =
                numericSpec.stepDouble(
                        (Double) current,
                        direction);

        try {
            doubleSetting.set(next);
        } catch (IllegalArgumentException rejected) {
            // The Setting validator remains authoritative.
        }
        return true;
    }

    private static boolean adjustInteger(
            final Setting<?> setting,
            final SettingNumericSpec numericSpec,
            final UiPointerButton button) {
        if (numericSpec == null) {
            return false;
        }
        final int direction;
        if (button == UiPointerButton.LEFT) {
            direction = 1;
        } else if (button == UiPointerButton.RIGHT) {
            direction = -1;
        } else {
            return false;
        }

        final Object current = setting.get();
        if (!(current instanceof Integer)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final Setting<Integer> integerSetting =
                (Setting<Integer>) setting;
        final int next =
                numericSpec.stepInteger(
                        (Integer) current,
                        direction);

        try {
            integerSetting.set(next);
        } catch (IllegalArgumentException rejected) {
            // The Setting validator remains authoritative.
        }
        return true;
    }

    private static boolean toggleBoolean(
            final Setting<?> setting) {
        final Object current = setting.get();
        if (!(current instanceof Boolean)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final Setting<Boolean> booleanSetting =
                (Setting<Boolean>) setting;

        try {
            booleanSetting.set(
                    !((Boolean) current));
        } catch (IllegalArgumentException rejected) {
            // The Setting validator remains authoritative.
        }
        return true;
    }

    private final class TextEditSession
            implements UiFocusTarget {
        private final UiFocusManager focusManager;
        private final Setting<String> setting;
        private final String id;
        private String draft;
        private UiFocusManager.Registration registration;

        TextEditSession(
                final UiFocusManager focusManager,
                final Setting<String> setting,
                final String id,
                final String draft) {
            this.focusManager =
                    Objects.requireNonNull(
                            focusManager,
                            "focusManager");
            this.setting =
                    Objects.requireNonNull(
                            setting,
                            "setting");
            this.id =
                    Objects.requireNonNull(
                            id,
                            "id");
            this.draft =
                    Objects.requireNonNull(
                            draft,
                            "draft");
        }

        void attach(
                final UiFocusManager.Registration registration) {
            this.registration =
                    Objects.requireNonNull(
                            registration,
                            "registration");
        }

        Setting<String> setting() {
            return setting;
        }

        String draft() {
            return draft;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public void onFocusChanged(
                final boolean focused) {
            if (!focused) {
                dispose();
            }
        }

        @Override
        public boolean onKey(
                final UiKeyEvent event) {
            Objects.requireNonNull(event, "event");

            if (event.action() == UiKeyAction.RELEASE) {
                return false;
            }

            if (UiKeys.ESCAPE.equals(event.key())) {
                focusManager.clearFocus();
                return true;
            }

            if (UiKeys.ENTER.equals(event.key())) {
                try {
                    setting.set(draft);
                } catch (IllegalArgumentException rejected) {
                    return true;
                }
                focusManager.clearFocus();
                return true;
            }

            if (UiKeys.BACKSPACE.equals(event.key())
                    || UiKeys.DELETE.equals(event.key())) {
                if (!draft.isEmpty()) {
                    draft =
                            draft.substring(
                                    0,
                                    draft.length() - 1);
                }
                return true;
            }

            if (event.hasCharacter()
                    && !event.control()
                    && !event.alt()
                    && !Character.isISOControl(
                            event.character())) {
                if (draft.length() < MAX_TEXT_LENGTH) {
                    draft += event.character();
                }
                return true;
            }

            return false;
        }

        void dispose() {
            if (textEdit == this) {
                textEdit = null;
            }

            final UiFocusManager.Registration current =
                    registration;
            registration = null;
            if (current != null
                    && current.active()) {
                current.close();
            }
        }
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
