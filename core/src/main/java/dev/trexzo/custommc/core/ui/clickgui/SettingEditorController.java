package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiFocusTarget;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;

import java.util.Objects;

final class SettingEditorController {
    private static final int MAX_TEXT_LENGTH = 128;

    private TextEditSession textEdit;

    boolean pointer(
            final ClickGuiContentInputContext context,
            final Setting<?> setting,
            final SettingDescriptor descriptor,
            final UiPointerEvent event) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(setting, "setting");
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(event, "event");

        if (event.action() != UiPointerAction.PRESS) {
            return false;
        }

        if (descriptor.kind() == SettingValueKind.BOOLEAN) {
            if (event.button() != UiPointerButton.LEFT) {
                return false;
            }
            return toggleBoolean(setting);
        }

        if (descriptor.kind() == SettingValueKind.INTEGER) {
            return adjustInteger(
                    setting,
                    descriptor.numericSpec(),
                    event.button());
        }

        if (descriptor.kind() == SettingValueKind.DOUBLE) {
            return adjustDouble(
                    setting,
                    descriptor.numericSpec(),
                    event.button());
        }

        if (descriptor.kind() == SettingValueKind.TEXT) {
            if (event.button() != UiPointerButton.LEFT) {
                return false;
            }
            return beginTextEdit(
                    context,
                    setting);
        }

        return false;
    }

    boolean isEditing(
            final Setting<?> setting) {
        return textEdit != null
                && textEdit.setting() == setting;
    }

    String displayValue(
            final Setting<?> setting) {
        Objects.requireNonNull(setting, "setting");
        if (isEditing(setting)) {
            return textEdit.draft() + "|";
        }
        return String.valueOf(setting.get());
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
}
