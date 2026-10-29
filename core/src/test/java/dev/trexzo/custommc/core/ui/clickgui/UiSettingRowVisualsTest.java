package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiSettingRowVisualsTest {
    private static final UiBounds STANDARD =
            new UiBounds(100.0F, 40.0F, 400.0F, 38.0F);

    @Test
    void longLabelAndValueAreIndependentlyClippedWithoutOverlap() {
        final Setting<String> setting = new Setting<String>(
                "settings.long", "text", value -> true, SettingCodecs.STRING);
        final String label = "An extraordinarily long setting label that cannot fit";
        final String value = "A remarkably long value that must stay within the well";
        final List<UiDrawCommand> commands =
                UiSettingRowVisuals.compose(
                        STANDARD, setting,
                        new SettingDescriptor("settings.long", label,
                                SettingValueKind.TEXT, 0),
                        value, true, UiThemes.graphite());

        final UiClipCommand labelClip = clipContaining(commands, label);
        final UiClipCommand valueClip = clipContaining(commands, value);
        assertTrue(labelClip.bounds().x() + labelClip.bounds().width()
                < valueClip.bounds().x());
        assertTrue(valueClip.bounds().x() + valueClip.bounds().width()
                <= STANDARD.x() + STANDARD.width());
        assertTrue(labelClip.bounds().width() > 0);
        assertTrue(valueClip.bounds().width() > 0);
    }

    @Test
    void booleanOnlyShowsLiveSwitchNotDuplicateTextStatus() {
        final Setting<Boolean> setting = new Setting<Boolean>(
                "settings.enabled", Boolean.FALSE,
                value -> value != null, SettingCodecs.BOOLEAN);
        final SettingDescriptor descriptor = new SettingDescriptor(
                "settings.enabled", "Enabled", SettingValueKind.BOOLEAN, 0);

        for (boolean enabled : new boolean[] { false, true }) {
            setting.set(enabled);
            final List<UiDrawCommand> commands =
                    UiSettingRowVisuals.compose(STANDARD, setting,
                            descriptor, String.valueOf(enabled), false,
                            UiThemes.graphite());
            clipContaining(commands, "Enabled");
            assertFalse(hasText(commands, "false"));
            assertFalse(hasText(commands, "true"));
            int switchParts = 0;
            for (UiDrawCommand command : commands) {
                if (command instanceof UiRoundedRectCommand) {
                    switchParts++;
                }
            }
            // One backing surface plus the switch track and thumb.
            assertEquals(3, switchParts);
        }
    }

    @Test
    void nativeRowHintsDescribeRealPointerSemantics() {
        final Setting<Boolean> bool = new Setting<Boolean>(
                "render.enabled", false, v -> v != null, SettingCodecs.BOOLEAN);
        final SettingDescriptor boolDesc = new SettingDescriptor(
                "render.enabled", "Enabled", SettingValueKind.BOOLEAN, 0);
        final List<UiDrawCommand> boolCommands =
                UiSettingRowVisuals.compose(STANDARD, bool, boolDesc,
                        "false", false, UiThemes.graphite());
        assertTrue(hasText(boolCommands, "CLICK TO TOGGLE"));
        assertFalse(hasText(boolCommands, "false"));
        assertFalse(hasText(boolCommands, "true"));

        final Setting<Integer> steps = new Setting<Integer>(
                "combat.range", 3, v -> v >= 0 && v <= 8,
                SettingCodecs.INTEGER);
        final SettingDescriptor numeric = new SettingDescriptor(
                "combat.range", "Range", SettingValueKind.INTEGER, 0,
                new SettingNumericSpec(0, 8, 1));
        final List<UiDrawCommand> numericCommands =
                UiSettingRowVisuals.compose(STANDARD, steps, numeric,
                        "3", false, UiThemes.graphite());
        assertTrue(hasText(numericCommands, "LEFT +  /  RIGHT -"));
        assertTrue(hasText(numericCommands, "3"));
        assertFalse(hasText(numericCommands, "DRAG"));

        final Setting<String> text = new Setting<String>(
                "render.label", "abc", v -> true, SettingCodecs.STRING);
        final SettingDescriptor textDesc = new SettingDescriptor(
                "render.label", "Label", SettingValueKind.TEXT, 0);
        assertTrue(hasText(UiSettingRowVisuals.compose(
                STANDARD, text, textDesc, "abc", false,
                UiThemes.graphite()), "CLICK TO EDIT"));
        assertTrue(hasText(UiSettingRowVisuals.compose(
                STANDARD, text, textDesc, "abc|", true,
                UiThemes.graphite()), "ENTER SAVE  /  ESC CANCEL"));

        final SettingDescriptor themeDesc = new SettingDescriptor(
                "custommc.ui.theme", "Theme", SettingValueKind.TEXT, 0);
        assertTrue("Theme must have a distinct cycle hint",
                UiSettingRowVisuals.interactionHint(themeDesc, false)
                        .equals("CLICK TO EDIT")
                || UiSettingRowVisuals.interactionHint(themeDesc, false)
                        .equals("LEFT NEXT  /  RIGHT PREVIOUS"));
    }

    @Test
    void minimumWidthHidesExtraHintWithoutChangingControlClips() {
        final Setting<Boolean> setting = new Setting<Boolean>(
                "render.enabled", false, v -> v != null, SettingCodecs.BOOLEAN);
        final SettingDescriptor descriptor = new SettingDescriptor(
                "render.enabled", "Enabled", SettingValueKind.BOOLEAN, 0);
        final UiBounds narrow = new UiBounds(10, 20, 170, 38);
        final List<UiDrawCommand> commands =
                UiSettingRowVisuals.compose(narrow, setting, descriptor,
                        "false", false, UiThemes.graphite());
        assertTrue(hasText(commands, "Enabled"));
        assertFalse(hasText(commands, "CLICK TO TOGGLE"));
        for (UiDrawCommand command : commands) {
            if (command instanceof UiClipCommand) {
                final UiBounds bounds = ((UiClipCommand) command).bounds();
                assertTrue(bounds.x() >= narrow.x());
                assertTrue(bounds.x() + bounds.width()
                        <= narrow.x() + narrow.width());
            }
        }
    }

    @Test
    void tinyContentBoundsNeverCreateNegativeOrOffRowTextRegions() {
        final Setting<String> setting = new Setting<String>(
                "settings.scale", "value", v -> true, SettingCodecs.STRING);
        final SettingDescriptor descriptor = new SettingDescriptor(
                "settings.scale", "An extremely long label",
                SettingValueKind.TEXT, 0);

        for (float width : new float[] { 0.0F, 8.0F, 65.0F, 150.0F, 250.0F }) {
            final UiBounds row = new UiBounds(15.0F, 10.0F, width, 38.0F);
            final List<UiDrawCommand> commands = UiSettingRowVisuals.compose(
                    row, setting, descriptor, "long value", false,
                    UiThemes.graphite());
            for (UiDrawCommand command : commands) {
                if (command instanceof UiClipCommand) {
                    final UiBounds clip = ((UiClipCommand) command).bounds();
                    assertTrue(clip.width() >= 0.0F);
                    assertTrue(clip.x() >= row.x());
                    assertTrue(clip.x() + clip.width()
                            <= row.x() + row.width());
                }
            }
            if (width == 0.0F) {
                assertTrue(commands.isEmpty());
            }
        }
    }

    private static UiClipCommand clipContaining(
            final List<UiDrawCommand> commands,
            final String text) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiClipCommand
                    && hasText(((UiClipCommand) command).commands(), text)) {
                return (UiClipCommand) command;
            }
        }
        throw new AssertionError("Missing text clip for " + text);
    }

    private static boolean hasText(
            final List<UiDrawCommand> commands,
            final String expected) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand
                    && expected.equals(((UiTextCommand) command).text())) {
                return true;
            }
            if (command instanceof UiClipCommand
                    && hasText(((UiClipCommand) command).commands(), expected)) {
                return true;
            }
        }
        return false;
    }
}
