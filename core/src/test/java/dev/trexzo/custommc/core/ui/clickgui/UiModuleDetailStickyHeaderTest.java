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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiModuleDetailStickyHeaderTest {
    private static final UiBounds PANEL =
            new UiBounds(215.0F, 33.0F, 580.0F, 376.0F);

    @Test
    void nativeFirstViewDoesNotChangeAndStickyBeginsOnlyAfterFullHeader() {
        assertTrue(UiModuleDetailStickyHeader.compose(
                PANEL, 0.0F, "Velocity", 24, ModuleState.ENABLED,
                UiThemes.graphite()).isEmpty());
        assertTrue(UiModuleDetailStickyHeader.compose(
                PANEL, 101.999F, "Velocity", 24, ModuleState.ENABLED,
                UiThemes.graphite()).isEmpty());
        assertFalse(UiModuleDetailStickyHeader.blocksPointer(
                PANEL, 0.0F, 250.0F, 40.0F));

        final List<UiDrawCommand> commands = UiModuleDetailStickyHeader.compose(
                PANEL, 102.0F, "Velocity", 24, ModuleState.ENABLED,
                UiThemes.graphite());
        assertEquals(4, commands.size());
        final UiRectCommand fill = (UiRectCommand) commands.get(0);
        final UiRectCommand rail = (UiRectCommand) commands.get(1);
        final UiClipCommand clipped = (UiClipCommand) commands.get(2);
        final UiTextCommand title = (UiTextCommand) clipped.commands().get(0);
        final UiRectCommand border = (UiRectCommand) commands.get(3);

        assertEquals(PANEL.x(), fill.x(), .001F);
        assertEquals(PANEL.y(), fill.y(), .001F);
        assertEquals(PANEL.width(), fill.width(), .001F);
        assertEquals(26.0F, fill.height(), .001F);
        assertEquals(UiThemes.graphite().color(UiColorRole.SURFACE),
                fill.argb());
        assertEquals(UiThemes.graphite().color(UiColorRole.ACCENT),
                rail.argb());
        assertEquals("Velocity  /  24 settings", title.text());
        assertEquals(PANEL.x() + 24, clipped.bounds().x(), .001F);
        assertEquals(PANEL.x() + PANEL.width() - 139,
                clipped.bounds().x() + clipped.bounds().width(), .001F);
        assertEquals(PANEL.y() + 25, border.y(), .001F);
        // The original 102px fixed Back to Modules button retains a
        // completely separate top-right rectangle and input authority.
        assertFalse(UiModuleDetailBackLink.hit(PANEL, 670, 45));
        assertTrue(UiModuleDetailBackLink.hit(PANEL, 685, 46));
        assertTrue(UiModuleDetailStickyHeader.blocksPointer(
                PANEL, 230.0F, 40.0F));
        assertFalse(UiModuleDetailStickyHeader.blocksPointer(
                PANEL, 230.0F, 60.0F));
    }

    @Test
    void longSourceModuleNamesRemainClippedAndStatusRailIsHonest() {
        final String longName = "Really long module identity that should never draw underneath Back";
        final List<UiDrawCommand> failed = UiModuleDetailStickyHeader.compose(
                PANEL, 500, longName, 123, ModuleState.FAILED,
                UiThemes.graphite());
        final UiRectCommand rail = (UiRectCommand) failed.get(1);
        assertEquals(UiThemes.graphite().color(UiColorRole.DANGER),
                rail.argb());
        final UiClipCommand clipped = (UiClipCommand) failed.get(2);
        assertTrue(((UiTextCommand) clipped.commands().get(0)).text()
                .contains("123 settings"));
        assertTrue(clipped.bounds().x() + clipped.bounds().width()
                < PANEL.x() + PANEL.width() - 117.0F);
        final List<UiDrawCommand> disabled = UiModuleDetailStickyHeader.compose(
                PANEL, 102.0F, "Reach", 0, ModuleState.DISABLED,
                UiThemes.graphite());
        assertEquals(UiThemes.graphite().color(UiColorRole.TEXT_MUTED),
                ((UiRectCommand) disabled.get(1)).argb());
        assertTrue(UiModuleDetailStickyHeader.compose(
                new UiBounds(3, 3, 130, 160), 400.0F,
                "Reach", 0, ModuleState.DISABLED,
                UiThemes.graphite()).isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> UiModuleDetailStickyHeader.compose(
                        PANEL, 110, "Reach", -1, ModuleState.DISABLED,
                        UiThemes.graphite()));
        assertThrows(IllegalArgumentException.class,
                () -> UiModuleDetailStickyHeader.activeBounds(
                        PANEL, Float.NaN));
    }
}
