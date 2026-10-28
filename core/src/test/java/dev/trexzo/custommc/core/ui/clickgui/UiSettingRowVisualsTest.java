package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
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
