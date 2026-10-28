package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiOutlineCommand;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Shared source-native 1.8.9 ClickGUI settings-row presentation.
 *
 * Only projects existing setting state. Input, validation, changing values,
 * keybind focus and module lifecycle remain with their original controllers.
 */
final class UiSettingRowVisuals {
    private static final float INSET = 12.0F;
    private static final float TEXT_Y = 10.0F;

    private UiSettingRowVisuals() {
    }

    static List<UiDrawCommand> compose(
            final UiBounds row,
            final Setting<?> setting,
            final SettingDescriptor descriptor,
            final String valueText,
            final boolean editing,
            final UiTheme theme) {
        Objects.requireNonNull(row, "row");
        Objects.requireNonNull(setting, "setting");
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(valueText, "valueText");
        Objects.requireNonNull(theme, "theme");

        final List<UiDrawCommand> commands = new ArrayList<UiDrawCommand>();
        if (row.width() <= 0.0F || row.height() <= 0.0F) {
            return commands;
        }

        commands.add(new UiRoundedRectCommand(
                0, row.x(), row.y(), row.width(), row.height(),
                Math.min(6.0F,
                        Math.min(row.width(), row.height()) * 0.5F),
                theme.color(UiColorRole.SURFACE_RAISED)));
        commands.add(new UiOutlineCommand(
                1, row.x(), row.y(), row.width(), row.height(),
                1.0F, theme.color(UiColorRole.BORDER)));

        final boolean booleanControl =
                descriptor.kind() == SettingValueKind.BOOLEAN;
        final float labelX = row.x() + INSET;
        // Reserve room for either the native switch or a separate value well.
        // Do not approximate text widths: the GL scissor is authoritative.
        final float labelRight = row.x() + row.width()
                - (booleanControl ? 60.0F : 160.0F);
        final float labelWidth = Math.max(0.0F, labelRight - labelX);
        addClippedText(commands, row, labelX, labelWidth,
                descriptor.label(), theme.color(UiColorRole.TEXT_PRIMARY));

        if (!booleanControl) {
            // Text, enum and numeric rows share one bounded value area.
            // Small viewports omit the inset surface rather than drawing
            // invalid or overlapping geometry.
            if (row.width() >= 166.0F && row.height() >= 18.0F) {
                commands.add(new UiRoundedRectCommand(
                        1,
                        row.x() + row.width() - 143.0F,
                        row.y() + 6.0F,
                        132.0F, row.height() - 12.0F,
                        Math.min(5.0F, (row.height() - 12.0F) * 0.5F),
                        theme.color(UiColorRole.BACKGROUND)));
            }
        }

        commands.addAll(UiSettingDecorations.compose(
                row, setting, descriptor, theme));

        // A Boolean's authoritative state is already shown by its switch.
        // Duplicated "true/false" text clutters the compact ghost-client UI.
        if (!booleanControl) {
            final float valueX = Math.max(
                    row.x() + INSET,
                    row.x() + row.width() - 130.0F);
            final float valueRight =
                    row.x() + row.width() - 15.0F;
            addClippedText(commands, row, valueX,
                    Math.max(0.0F, valueRight - valueX),
                    valueText, theme.color(editing
                            ? UiColorRole.ACCENT : UiColorRole.TEXT_MUTED));
        }
        return commands;
    }

    private static void addClippedText(
            final List<UiDrawCommand> commands,
            final UiBounds row,
            final float x,
            final float width,
            final String value,
            final int argb) {
        if (width <= 0.0F || row.height() < TEXT_Y + 2.0F) {
            return;
        }
        commands.add(new UiClipCommand(
                2,
                new UiBounds(x, row.y(), width, row.height()),
                Collections.<UiDrawCommand>singletonList(
                        new UiTextCommand(
                                0, x, row.y() + TEXT_Y,
                                UiFonts.DEFAULT, value, argb))));
    }
}
