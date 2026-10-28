package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiModuleDetailHeaderTest {
    private static final UiBounds LARGE =
            new UiBounds(100.0F, 90.0F, 500.0F, 380.0F);

    @Test
    void longNameDescriptionAndKeybindHaveIndependentClipRegions() {
        final String title = "An exceptionally long module name intended to overlap everything";
        final String description = "Very long descriptive phrase that needs independent clipping";
        final String bind = "Bind: CTRL+SHIFT+ALT+legacy-key-37";
        final List<UiDrawCommand> commands = UiModuleDetailHeader.compose(
                LARGE, 0.0F, title, ModuleState.ENABLED,
                description, bind, false, UiThemes.graphite());
        final UiClipCommand titleClip = find(commands, title);
        final UiClipCommand statusClip = find(commands, "ENABLED");
        final UiClipCommand descriptionClip = find(commands, description);
        final UiClipCommand bindClip = find(commands, bind);

        assertNotNull(titleClip);
        assertNotNull(statusClip);
        assertNotNull(descriptionClip);
        assertNotNull(bindClip);

        assertTrue(statusClip.bounds().x() + statusClip.bounds().width()
                <= descriptionClip.bounds().x());
        assertTrue(descriptionClip.bounds().x() + descriptionClip.bounds().width()
                <= LARGE.x() + LARGE.width() - 24.0F);
        assertEquals(LARGE.x() + 24.0F, titleClip.bounds().x(), 0.001F);
        assertEquals(280.0F, bindClip.bounds().width(), 0.001F);
        assertEquals(LARGE.y() + 24.0F + 38.0F, bindClip.bounds().y(), 0.001F);
        // The existing bind capture hitbox remains y+PADDING+38, 28px tall.
        assertEquals(28.0F, bindClip.bounds().height(), 0.001F);
        assertEquals(UiThemes.graphite().color(UiColorRole.POSITIVE),
                childText(statusClip).argb());
    }

    @Test
    void compactWidthClampsTextAndOmitsConflictingDescription() {
        final UiBounds narrow = new UiBounds(5.0F, 10.0F, 126.0F, 210.0F);
        final List<UiDrawCommand> commands = UiModuleDetailHeader.compose(
                narrow, 0.0F, "Long module title",
                ModuleState.FAILED, "description",
                "Bind: Unbound", false, UiThemes.graphite());
        assertNotNull(find(commands, "Long module title"));
        assertNotNull(find(commands, "FAILED"));
        assertFalse(has(commands, "description"));
        assertEquals(UiThemes.graphite().color(UiColorRole.DANGER),
                childText(find(commands, "FAILED")).argb());
        for (UiDrawCommand command : commands) {
            if (command instanceof UiClipCommand) {
                final UiBounds clip = ((UiClipCommand) command).bounds();
                assertTrue(clip.x() >= narrow.x());
                assertTrue(clip.x() + clip.width()
                        <= narrow.x() + narrow.width());
            }
        }
        assertTrue(UiModuleDetailHeader.compose(
                new UiBounds(5.0F, 10.0F, 25.0F, 210.0F), 0.0F,
                "X", ModuleState.DISABLED, null, null,
                false, UiThemes.graphite()).isEmpty());
    }

    @Test
    void scrollMovesRenderedHeaderWithoutChangingNativeModuleState() {
        final List<UiDrawCommand> first = UiModuleDetailHeader.compose(
                LARGE, 0.0F, "Velocity", ModuleState.DISABLED,
                "Controls knockback", null, false, UiThemes.graphite());
        final List<UiDrawCommand> scrolled = UiModuleDetailHeader.compose(
                LARGE, 28.0F, "Velocity", ModuleState.DISABLED,
                "Controls knockback", null, false, UiThemes.graphite());
        assertEquals(find(first, "Velocity").bounds().y() - 28.0F,
                find(scrolled, "Velocity").bounds().y(), 0.001F);
        assertEquals(UiThemes.graphite().color(UiColorRole.TEXT_MUTED),
                childText(find(scrolled, "DISABLED")).argb());
        final List<UiDrawCommand> capture = UiModuleDetailHeader.compose(
                LARGE, 0.0F, "Velocity", ModuleState.DISABLED,
                null, "Bind: Press a key...", true, UiThemes.graphite());
        assertEquals(UiThemes.graphite().color(UiColorRole.ACCENT),
                childText(find(capture, "Bind: Press a key...")).argb());
        assertTrue(first.stream().anyMatch(x -> x instanceof UiRectCommand));
    }

    private static boolean has(
            final List<UiDrawCommand> commands, final String text) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiClipCommand
                    && text.equals(childText((UiClipCommand) command).text())) {
                return true;
            }
        }
        return false;
    }

    private static UiClipCommand find(
            final List<UiDrawCommand> commands, final String text) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiClipCommand
                    && text.equals(childText((UiClipCommand) command).text())) {
                return (UiClipCommand) command;
            }
        }
        return null;
    }

    private static UiTextCommand childText(final UiClipCommand clip) {
        return (UiTextCommand) clip.commands().get(0);
    }
}
