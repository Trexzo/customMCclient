package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiModelTest {
    @Test
    void pagesAreRetainedAndDeterministicallyOrdered() {
        final ClickGuiModel model =
                new ClickGuiModel();

        model.register(
                new ClickGuiPage(
                        "render",
                        "Render",
                        20));
        model.register(
                new ClickGuiPage(
                        "combat",
                        "Combat",
                        10));
        model.register(
                new ClickGuiPage(
                        "movement",
                        "Movement",
                        10));

        final ClickGuiSnapshot snapshot =
                model.snapshot();

        assertEquals("combat", snapshot.selectedPageId());
        assertEquals("combat", snapshot.pages().get(0).id());
        assertEquals("movement", snapshot.pages().get(1).id());
        assertEquals("render", snapshot.pages().get(2).id());

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.pages().clear());
    }

    @Test
    void explicitSelectionSurvivesLaterPageRegistration() {
        final ClickGuiModel model =
                new ClickGuiModel();

        model.register(
                new ClickGuiPage(
                        "render",
                        "Render",
                        20));
        assertTrue(model.select("render"));

        model.register(
                new ClickGuiPage(
                        "combat",
                        "Combat",
                        0));

        assertEquals(
                "render",
                model.snapshot().selectedPageId());
    }

    @Test
    void selectionOpenStateAndSearchPersistAcrossSnapshots() {
        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "combat",
                        "Combat",
                        0));
        model.register(
                new ClickGuiPage(
                        "render",
                        "Render",
                        1));

        model.open();
        model.setSearchQuery("target");
        assertTrue(model.select("render"));

        ClickGuiSnapshot snapshot =
                model.snapshot();

        assertTrue(snapshot.open());
        assertEquals("render", snapshot.selectedPageId());
        assertEquals("target", snapshot.searchQuery());

        model.toggle();
        snapshot = model.snapshot();

        assertFalse(snapshot.open());
        assertEquals("render", snapshot.selectedPageId());
        assertEquals("target", snapshot.searchQuery());
    }

    @Test
    void removingSelectedPageFallsBackDeterministically() {
        final ClickGuiModel model =
                new ClickGuiModel();

        final ClickGuiModel.Registration combat =
                model.register(
                        new ClickGuiPage(
                                "combat",
                                "Combat",
                                0));
        final ClickGuiModel.Registration render =
                model.register(
                        new ClickGuiPage(
                                "render",
                                "Render",
                                1));

        assertTrue(model.select("render"));

        render.close();
        render.close();

        assertFalse(render.active());
        assertEquals(
                "combat",
                model.snapshot().selectedPageId());

        combat.close();

        assertNull(model.snapshot().selectedPageId());
    }

    @Test
    void duplicateIdsAreRejectedAndUnknownSelectionIsIgnored() {
        final ClickGuiModel model =
                new ClickGuiModel();

        model.register(
                new ClickGuiPage(
                        "combat",
                        "Combat",
                        0));

        assertThrows(
                IllegalArgumentException.class,
                () -> model.register(
                        new ClickGuiPage(
                                "combat",
                                "Duplicate",
                                1)));

        assertFalse(model.select("missing"));
        assertEquals(
                "combat",
                model.snapshot().selectedPageId());
    }
}
