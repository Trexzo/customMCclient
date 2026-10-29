package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.ArrayList;
import java.util.List;

/** Theme-aware visuals only: the existing SettingEditorController owns inputs. */
final class UiSettingDecorations {
    private UiSettingDecorations() { }

    static List<UiDrawCommand> compose(final UiBounds row,
            final Setting<?> setting,
            final SettingDescriptor descriptor, final UiTheme theme) {
        final List<UiDrawCommand> draw = new ArrayList<UiDrawCommand>();
        if (descriptor.kind() == SettingValueKind.BOOLEAN
                && row.width() >= 60.0F && row.height() >= 16.0F) {
            final boolean enabled = Boolean.TRUE.equals(setting.get());
            final float x = row.x() + row.width() - 47.0F;
            final float y = row.y() + (row.height() - 14.0F) * 0.5F;
            draw.add(new UiRoundedRectCommand(2, x, y,
                    31.0F, 14.0F, 7.0F, theme.color(
                    enabled ? UiColorRole.ACCENT : UiColorRole.BORDER)));
            draw.add(new UiRoundedRectCommand(3,
                    x + (enabled ? 18.0F : 3.0F), y + 3.0F,
                    8.0F, 8.0F, 4.0F,
                    theme.color(UiColorRole.TEXT_PRIMARY)));
        } else if (descriptor.numericSpec() != null
                && setting.get() instanceof Number) {
            final SettingNumericSpec spec = descriptor.numericSpec();
            final double span = spec.maximum() - spec.minimum();
            final double raw = ((Number) setting.get()).doubleValue();
            final double ratio = span <= 0.0D || !Double.isFinite(raw)
                    ? 0.0D : Math.max(0.0D,
                    Math.min(1.0D, (raw - spec.minimum()) / span));
            // Read-only fill level lives INSIDE the existing bounded
            // numeric value well, not beneath the setting label where it
            // resembled a draggable input. Actual editing remains left/right
            // click in SettingEditorController.
            if (row.width() < 180.0F || row.height() < 22.0F) return draw;
            final float width = 120.0F;
            final float left = row.x() + row.width() - 137.0F;
            final float top = row.y() + row.height() - 9.0F;
            draw.add(new UiRoundedRectCommand(1, left, top,
                    width, 2.0F, 1.0F, theme.color(UiColorRole.BORDER)));
            if (ratio > 0.0D) {
                draw.add(new UiRoundedRectCommand(2, left, top,
                        width * (float) ratio, 2.0F,
                        Math.min(1.0F, width * (float) ratio * 0.5F),
                        theme.color(UiColorRole.ACCENT)));
            }
        }
        return draw;
    }
}
