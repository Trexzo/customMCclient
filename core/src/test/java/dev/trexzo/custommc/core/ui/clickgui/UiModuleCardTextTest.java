package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiModuleCardTextTest {
    @Test
    void actualTwoLineModuleCardIsClippedBeforeNativeSwitchAndDetails() {
        final UiBounds row = new UiBounds(232, 102, 464, 29);
        final List<UiDrawCommand> commands = UiModuleCardText.compose(
                row, "Auto Clicker",
                "Automatically clicks when attacking nearby enemies",
                true, UiThemes.graphite());
        assertEquals(1, commands.size());
        final UiClipCommand clip = (UiClipCommand) commands.get(0);
        assertEquals(245.0F, clip.bounds().x());
        assertEquals(364.0F, clip.bounds().width());
        assertEquals(2, clip.commands().size());
        final UiTextCommand title = (UiTextCommand) clip.commands().get(0);
        final UiTextCommand subtitle = (UiTextCommand) clip.commands().get(1);
        assertEquals("Auto Clicker", title.text());
        assertEquals("Automatically clicks when attacking nearby enemies",
                subtitle.text());
        assertEquals(row.y() + 4.0F, title.y());
        assertEquals(row.y() + 16.0F, subtitle.y());
        assertTrue(clip.bounds().x() + clip.bounds().width()
                <= row.x() + row.width() - 87.0F);
    }

    @Test
    void missingDescriptionRetainsOriginalCenteredSingleLine() {
        final UiBounds row = new UiBounds(232, 102, 464, 29);
        final UiClipCommand clip = (UiClipCommand) UiModuleCardText.compose(
                row, "No Hit Delay", "", true,
                UiThemes.graphite()).get(0);
        assertEquals(1, clip.commands().size());
        assertEquals(row.y() + 10.0F,
                ((UiTextCommand) clip.commands().get(0)).y());
    }

    @Test
    void narrowViewportFallsBackToSingleLineWithoutIntrudingIntoControls() {
        final UiBounds row = new UiBounds(1, 1, 160, 29);
        final UiClipCommand clip = (UiClipCommand) UiModuleCardText.compose(
                row, "Aim Assist", "Long helpful description", true,
                UiThemes.graphite()).get(0);
        assertEquals(1, clip.commands().size());
        assertEquals(60.0F, clip.bounds().width());
        assertTrue(clip.bounds().x() + clip.bounds().width()
                <= row.x() + row.width() - 87.0F);
        assertTrue(UiModuleCardText.compose(
                new UiBounds(1, 1, 40, 29), "Very Long",
                "Text", true, UiThemes.graphite()).isEmpty());
    }
}
