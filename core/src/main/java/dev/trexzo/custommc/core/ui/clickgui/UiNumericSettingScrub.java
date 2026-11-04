package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;

import java.util.Objects;

/**
 * A real pointer-controlled value scrubber confined to the existing numeric
 * value well. The source Setting validator retains final authority.
 *
 * Only a LEFT press inside the numeric value well starts capture; the rest
 * of the row preserves legacy left/right single-step semantics. We retain
 * the initial well geometry while dragging, clamp beyond both endpoints,
 * and release even if the cursor has left the panel.
 */
final class UiNumericSettingScrub {
    private static final float WELL_RIGHT_INSET = 143.0F;
    private static final float WELL_WIDTH = 132.0F;
    private static final float TRACK_RIGHT_INSET = 137.0F;
    private static final float TRACK_WIDTH = 120.0F;

    private Setting<?> captured;
    private SettingNumericSpec spec;
    private SettingValueKind kind;
    private float trackLeft;

    static UiBounds wellBounds(final UiBounds row) {
        Objects.requireNonNull(row, "row");
        if (row.width() < 180.0F || row.height() < 22.0F) {
            return null;
        }
        return new UiBounds(row.x() + row.width() - WELL_RIGHT_INSET,
                row.y() + 6.0F, WELL_WIDTH, row.height() - 12.0F);
    }

    boolean begin(final UiBounds row, final Setting<?> setting,
            final SettingDescriptor descriptor, final UiPointerEvent event) {
        Objects.requireNonNull(row, "row");
        Objects.requireNonNull(setting, "setting");
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(event, "event");
        if (event.action() != UiPointerAction.PRESS
                || event.button() != UiPointerButton.LEFT
                || descriptor.numericSpec() == null) {
            return false;
        }
        final SettingValueKind type = descriptor.kind();
        if ((type != SettingValueKind.INTEGER
                    || !(setting.get() instanceof Integer))
                && (type != SettingValueKind.DOUBLE
                    || !(setting.get() instanceof Double))) {
            return false;
        }
        final UiBounds well = wellBounds(row);
        if (well == null || !well.contains(event.x(), event.y())) {
            return false;
        }
        final SettingNumericSpec numeric = descriptor.numericSpec();
        final double span = numeric.maximum() - numeric.minimum();
        if (!Double.isFinite(span) || span <= 0.0D) {
            return false;
        }
        captured = setting;
        spec = numeric;
        kind = type;
        trackLeft = row.x() + row.width() - TRACK_RIGHT_INSET;
        seek(event.x());
        return true;
    }

    boolean pointer(final UiPointerEvent event) {
        Objects.requireNonNull(event, "event");
        if (event.action() == UiPointerAction.RELEASE) {
            final boolean owned = captured != null;
            cancel();
            return owned;
        }
        if (captured != null && event.action() == UiPointerAction.MOVE) {
            seek(event.x());
            return true;
        }
        return false;
    }

    boolean dragging() {
        return captured != null;
    }

    void cancel() {
        captured = null;
        spec = null;
        kind = null;
    }

    private void seek(final float pointerX) {
        final double fraction = Math.max(0.0D, Math.min(1.0D,
                (pointerX - trackLeft) / TRACK_WIDTH));
        final double minimum = spec.minimum();
        final double maximum = spec.maximum();
        final double steps = (maximum - minimum) / spec.step();
        // Both endpoints remain reachable even when max-min is not an
        // exact multiple of step. Intermediate values snap to spec.step.
        final double nearest = fraction <= 0.0D ? minimum
                : fraction >= 1.0D ? maximum
                : Math.max(minimum, Math.min(maximum,
                        minimum + Math.floor(fraction * steps + 0.5D)
                                * spec.step()));
        try {
            if (kind == SettingValueKind.INTEGER) {
                @SuppressWarnings("unchecked")
                final Setting<Integer> integer = (Setting<Integer>) captured;
                integer.set((int) Math.round(nearest));
            } else {
                @SuppressWarnings("unchecked")
                final Setting<Double> decimal = (Setting<Double>) captured;
                decimal.set(nearest);
            }
        } catch (IllegalArgumentException rejected) {
            // A descriptor does not bypass the authoritative validator.
        }
    }
}
