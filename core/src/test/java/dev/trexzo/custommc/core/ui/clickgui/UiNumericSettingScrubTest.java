package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiNumericSettingScrubTest {
    private static final UiBounds ROW = new UiBounds(100, 50, 400, 38);
    private static final UiBounds PANEL =
            new UiBounds(200, 100, 600, 400);

    @Test
    void integerThumbSnapsToSpecAndReleasesWithoutLeakingPointerCapture() {
        final Setting<Integer> value = new Setting<Integer>(
                "render.range", 32, x -> x >= 0 && x <= 100);
        final SettingDescriptor descriptor = new SettingDescriptor(
                "render.range", "Range", SettingValueKind.INTEGER, 0,
                new SettingNumericSpec(0, 100, 5));
        final UiNumericSettingScrub scrub = new UiNumericSettingScrub();
        final UiBounds well = UiNumericSettingScrub.wellBounds(ROW);
        assertEquals(357.0F, well.x(), .0001F);
        assertEquals(132.0F, well.width(), .0001F);
        assertFalse(scrub.begin(ROW, value, descriptor,
                pointer(200, 62, UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertFalse(scrub.begin(ROW, value, descriptor,
                pointer(423, 62, UiPointerButton.RIGHT, UiPointerAction.PRESS)));
        assertEquals(32, value.get().intValue());

        assertTrue(scrub.begin(ROW, value, descriptor,
                pointer(363, 62, UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertEquals(0, value.get().intValue());
        assertTrue(scrub.pointer(pointer(423, 62,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(50, value.get().intValue());
        assertTrue(scrub.pointer(pointer(452, 62,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(75, value.get().intValue());
        assertTrue(scrub.pointer(pointer(900, 600,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(100, value.get().intValue());
        assertTrue(scrub.pointer(pointer(-100, -50,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(0, value.get().intValue());
        assertTrue(scrub.pointer(pointer(900, 600,
                UiPointerButton.LEFT, UiPointerAction.RELEASE)));
        assertFalse(scrub.dragging());
        assertFalse(scrub.pointer(pointer(483, 62,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(0, value.get().intValue());
    }

    @Test
    void decimalAndUnalignedIntegerEndpointRemainReachable() {
        final UiNumericSettingScrub scrub = new UiNumericSettingScrub();
        final Setting<Double> decimal = new Setting<Double>(
                "ui.opacity", 0.5D, x -> x >= 0.5D && x <= 2.0D);
        final SettingDescriptor decimalDescriptor = new SettingDescriptor(
                "ui.opacity", "Opacity", SettingValueKind.DOUBLE, 0,
                new SettingNumericSpec(0.5D, 2.0D, 0.25D));
        assertTrue(scrub.begin(ROW, decimal, decimalDescriptor,
                pointer(423, 66, UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertEquals(1.25D, decimal.get().doubleValue(), 0.00001D);
        assertTrue(scrub.pointer(pointer(483, 66,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(2.0D, decimal.get().doubleValue(), 0.00001D);
        assertTrue(scrub.pointer(pointer(483, 66,
                UiPointerButton.LEFT, UiPointerAction.RELEASE)));

        final Setting<Integer> unaligned = new Setting<Integer>(
                "ui.steps", 0, x -> x >= 0 && x <= 9);
        final SettingDescriptor unalignedDescriptor = new SettingDescriptor(
                "ui.steps", "Steps", SettingValueKind.INTEGER, 0,
                new SettingNumericSpec(0, 9, 4));
        assertTrue(scrub.begin(ROW, unaligned, unalignedDescriptor,
                pointer(423, 62, UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertEquals(4, unaligned.get().intValue());
        assertTrue(scrub.pointer(pointer(483, 62,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(9, unaligned.get().intValue());
        assertTrue(scrub.pointer(pointer(483, 62,
                UiPointerButton.LEFT, UiPointerAction.RELEASE)));
    }

    @Test
    void validatorReadOnlyAndNarrowRowsAreStillAuthoritative() {
        final UiNumericSettingScrub scrub = new UiNumericSettingScrub();
        final Setting<Integer> locked = new Setting<Integer>(
                "ui.locked", 3, x -> x <= 3);
        final SettingDescriptor descriptor = new SettingDescriptor(
                "ui.locked", "Locked", SettingValueKind.INTEGER, 0,
                new SettingNumericSpec(0, 10, 1));
        assertTrue(scrub.begin(ROW, locked, descriptor,
                pointer(483, 62, UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertEquals(3, locked.get().intValue());
        assertTrue(scrub.pointer(pointer(483, 62,
                UiPointerButton.LEFT, UiPointerAction.RELEASE)));

        assertNull(UiNumericSettingScrub.wellBounds(
                new UiBounds(10, 20, 170, 38)));
        assertFalse(scrub.begin(new UiBounds(10, 20, 170, 38),
                locked, descriptor,
                pointer(150, 40, UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertFalse(scrub.begin(ROW, locked,
                new SettingDescriptor("ui.locked", "Locked",
                        SettingValueKind.INTEGER, 0),
                pointer(423, 62, UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertFalse(scrub.dragging());
    }

    @Test
    void globalSettingListAcceptsThumbDragAndKeepsLegacyRowSteps() {
        final SettingRegistry values = new SettingRegistry();
        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        final Setting<Integer> range = new Setting<Integer>(
                "ui.range", 3, x -> x >= 0 && x <= 100);
        values.register(range);
        presentations.register(new SettingDescriptor(
                "ui.range", "Range", SettingValueKind.INTEGER, 0,
                new SettingNumericSpec(0, 100, 1)));
        final SettingListPageContent pageContent =
                new SettingListPageContent(values, presentations);
        final ClickGuiPage page = new ClickGuiPage("settings", "Settings", 0);
        final ClickGuiSnapshot snapshot = new ClickGuiSnapshot(
                true, "settings", "", Collections.singletonList(page));
        final ClickGuiContentInputContext input =
                new ClickGuiContentInputContext(snapshot, page, PANEL);
        // Real global Settings row: x=224..776, y=158..196.
        // Its original value well is x=633..765 and track x=639..759.
        assertTrue(pageContent.pointer(input, pointer(639, 175,
                UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertEquals(0, range.get().intValue());
        assertTrue(pageContent.pointer(input, pointer(699, 175,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(50, range.get().intValue());
        assertTrue(pageContent.pointer(input, pointer(759, 175,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(100, range.get().intValue());
        assertTrue(pageContent.pointer(input, pointer(900, 600,
                UiPointerButton.LEFT, UiPointerAction.RELEASE)));
        assertFalse(pageContent.pointer(input, pointer(699, 175,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(100, range.get().intValue());
        // The old row-left step semantics are deliberately preserved.
        assertTrue(pageContent.pointer(input, pointer(240, 175,
                UiPointerButton.RIGHT, UiPointerAction.PRESS)));
        assertEquals(99, range.get().intValue());
        assertTrue(pageContent.pointer(input, pointer(240, 175,
                UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertEquals(100, range.get().intValue());

        final List<UiDrawCommand> commands = pageContent.compose(
                new ClickGuiContentContext(
                        snapshot, page, PANEL, UiThemes.graphite()));
        assertTrue(hasNumericThumb(commands));
    }

    @Test
    void moduleDetailsUsesSameRealControlAndCancelsOnSelectionChange() {
        final ModuleRegistry modules = new ModuleRegistry();
        modules.register(new Module() {
            @Override public String id() { return "ui.moduleA"; }
        });
        modules.register(new Module() {
            @Override public String id() { return "ui.moduleB"; }
        });
        final SettingRegistry values = new SettingRegistry();
        final SettingPresentationRegistry names =
                new SettingPresentationRegistry();
        final Setting<Integer> range = new Setting<Integer>(
                "ui.rangeA", 3, x -> x >= 0 && x <= 100);
        values.register(range);
        names.register(new SettingDescriptor(
                "ui.rangeA", "Range A", SettingValueKind.INTEGER, 0,
                new SettingNumericSpec(0, 100, 5)));
        final ModuleSettingRegistry bindings =
                new ModuleSettingRegistry(modules, values);
        bindings.register(new ModuleSettingBinding(
                "ui.moduleA", "ui.rangeA", 0));
        final ModuleSelectionModel selection = new ModuleSelectionModel(modules);
        selection.select("ui.moduleA");
        final ModuleDetailPageContent detail = new ModuleDetailPageContent(
                selection, new ModuleController(modules),
                new ModulePresentationRegistry(), bindings, values, names);
        final ClickGuiPage page = new ClickGuiPage(
                "module-detail", "Module Details", 0);
        final ClickGuiSnapshot snapshot = new ClickGuiSnapshot(
                true, "module-detail", "", Collections.singletonList(page));
        final ClickGuiContentInputContext input =
                new ClickGuiContentInputContext(snapshot, page, PANEL);

        // Real contextual detail row: x=224..776, y=202..240.
        assertTrue(detail.pointer(input, pointer(639, 215,
                UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertEquals(0, range.get().intValue());
        assertTrue(detail.pointer(input, pointer(699, 215,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(50, range.get().intValue());

        selection.select("ui.moduleB");
        // No real controls on B; selection change cancels A's capture.
        assertFalse(detail.pointer(input, pointer(759, 215,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(50, range.get().intValue());
        selection.select("ui.moduleA");
        assertFalse(detail.pointer(input, pointer(759, 215,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertEquals(50, range.get().intValue());
        assertTrue(detail.pointer(input, pointer(240, 215,
                UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertEquals(55, range.get().intValue());
    }

    private static boolean hasNumericThumb(final List<UiDrawCommand> commands) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiRoundedRectCommand) {
                final UiRoundedRectCommand rect = (UiRoundedRectCommand) command;
                if (rect.width() == 6.0F && rect.height() == 6.0F
                        && rect.layer() == 3) return true;
            }
        }
        return false;
    }

    private static UiPointerEvent pointer(final float x, final float y,
            final UiPointerButton button, final UiPointerAction action) {
        return new UiPointerEvent(x, y, button, action);
    }
}
