package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.ModuleKeyChord;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiFocusTarget;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;

import java.util.Objects;

final class ModuleKeybindEditorController {
    private final ModuleKeybindAssignments assignments;
    private CaptureSession capture;

    ModuleKeybindEditorController(
            final ModuleKeybindAssignments assignments) {
        this.assignments = Objects.requireNonNull(
                assignments,
                "assignments");
    }

    boolean begin(
            final ClickGuiContentInputContext context,
            final String moduleId) {
        Objects.requireNonNull(context, "context");
        final String id =
                Objects.requireNonNull(
                        moduleId,
                        "moduleId");

        final UiFocusManager focusManager =
                context.focusManager();
        if (focusManager == null) {
            return false;
        }

        if (capture != null) {
            capture.dispose();
        }

        final CaptureSession session =
                new CaptureSession(
                        focusManager,
                        id,
                        "clickgui.module-keybind."
                                + context.page().id()
                                + "."
                                + id);

        final UiFocusManager.Registration registration;
        try {
            registration =
                    focusManager.register(session);
        } catch (IllegalArgumentException duplicate) {
            return false;
        }

        session.attach(registration);
        capture = session;

        if (!focusManager.requestFocus(
                session.id())) {
            session.dispose();
            return false;
        }
        return true;
    }

    boolean isCapturing(
            final String moduleId) {
        return capture != null
                && capture.moduleId()
                .equals(
                        Objects.requireNonNull(
                                moduleId,
                                "moduleId"));
    }

    String captureLabel(
            final String moduleId) {
        if (!isCapturing(moduleId)) {
            return null;
        }
        return capture.label();
    }

    private final class CaptureSession
            implements UiFocusTarget {
        private final UiFocusManager focusManager;
        private final String moduleId;
        private final String id;
        private String label =
                "Bind: Press a key...";
        private UiFocusManager.Registration registration;

        CaptureSession(
                final UiFocusManager focusManager,
                final String moduleId,
                final String id) {
            this.focusManager =
                    Objects.requireNonNull(
                            focusManager,
                            "focusManager");
            this.moduleId =
                    Objects.requireNonNull(
                            moduleId,
                            "moduleId");
            this.id =
                    Objects.requireNonNull(
                            id,
                            "id");
        }

        void attach(
                final UiFocusManager.Registration registration) {
            this.registration =
                    Objects.requireNonNull(
                            registration,
                            "registration");
        }

        String moduleId() {
            return moduleId;
        }

        String label() {
            return label;
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

            if (event.action() == UiKeyAction.RELEASE
                    || event.action() == UiKeyAction.REPEAT) {
                return true;
            }

            if (UiKeys.ESCAPE.equals(event.key())) {
                focusManager.clearFocus();
                return true;
            }

            if (UiKeys.BACKSPACE.equals(event.key())
                    || UiKeys.DELETE.equals(event.key())) {
                try {
                    assignments.unbind(moduleId);
                } catch (IllegalArgumentException
                         | IllegalStateException rejected) {
                    label = "Bind: Cannot edit";
                    return true;
                }

                focusManager.clearFocus();
                return true;
            }

            final ModuleKeyChord chord =
                    new ModuleKeyChord(
                            event.key().id(),
                            event.shift(),
                            event.control(),
                            event.alt());
            try {
                assignments.bind(
                        moduleId,
                        chord);
            } catch (IllegalArgumentException conflict) {
                label = "Bind: Key in use";
                return true;
            } catch (IllegalStateException externalOwnership) {
                label = "Bind: Cannot edit";
                return true;
            }

            focusManager.clearFocus();
            return true;
        }

        void dispose() {
            if (capture == this) {
                capture = null;
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
