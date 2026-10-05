package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiFocusTarget;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiFocusManagerTest {
    @Test
    void focusSwitchAndKeyRoutingAreDeterministic() {
        final UiFocusManager manager =
                new UiFocusManager();
        final List<String> events =
                new ArrayList<String>();

        manager.register(target(
                "search",
                events,
                true));
        manager.register(target(
                "module-list",
                events,
                false));

        assertTrue(manager.requestFocus("search"));
        assertEquals("search", manager.focusedId());

        assertTrue(manager.dispatchKey(
                new UiKeyEvent(
                        UiKeys.ENTER,
                        UiKeyAction.PRESS,
                        '\0',
                        false,
                        false,
                        false)));

        assertTrue(manager.requestFocus("module-list"));
        assertEquals("module-list", manager.focusedId());

        assertFalse(manager.dispatchKey(
                new UiKeyEvent(
                        UiKeys.DOWN,
                        UiKeyAction.PRESS,
                        '\0',
                        false,
                        false,
                        false)));

        assertEquals(
                java.util.Arrays.asList(
                        "search:focus:true",
                        "search:key:enter",
                        "search:focus:false",
                        "module-list:focus:true",
                        "module-list:key:down"),
                events);
    }

    @Test
    void closingFocusedRegistrationReleasesFocus() {
        final UiFocusManager manager =
                new UiFocusManager();
        final List<String> events =
                new ArrayList<String>();
        final UiFocusManager.Registration registration =
                manager.register(target(
                        "field",
                        events,
                        true));

        assertTrue(manager.requestFocus("field"));
        registration.close();
        registration.close();

        assertFalse(registration.active());
        assertNull(manager.focusedId());
        assertEquals(
                java.util.Arrays.asList(
                        "field:focus:true",
                        "field:focus:false"),
                events);
    }

    @Test
    void duplicateIdsAndUnknownFocusAreRejectedOrIgnored() {
        final UiFocusManager manager =
                new UiFocusManager();
        final List<String> events =
                new ArrayList<String>();

        manager.register(target(
                "field",
                events,
                true));

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.register(target(
                        "field",
                        events,
                        true)));
        assertFalse(manager.requestFocus("missing"));
        assertNull(manager.focusedId());
    }

    private static UiFocusTarget target(
            final String id,
            final List<String> events,
            final boolean consumesKeys) {
        return new UiFocusTarget() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public void onFocusChanged(
                    final boolean focused) {
                events.add(
                        id + ":focus:" + focused);
            }

            @Override
            public boolean onKey(
                    final UiKeyEvent event) {
                events.add(
                        id + ":key:" + event.key().id());
                return consumesKeys;
            }
        };
    }
}
