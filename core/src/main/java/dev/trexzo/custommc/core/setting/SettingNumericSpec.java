package dev.trexzo.custommc.core.setting;

public final class SettingNumericSpec {
    private final double minimum;
    private final double maximum;
    private final double step;

    public SettingNumericSpec(
            final double minimum,
            final double maximum,
            final double step) {
        requireFinite(minimum, "minimum");
        requireFinite(maximum, "maximum");
        requireFinite(step, "step");
        if (minimum > maximum) {
            throw new IllegalArgumentException(
                    "minimum must not exceed maximum");
        }
        if (step <= 0.0D) {
            throw new IllegalArgumentException(
                    "step must be positive");
        }
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = step;
    }

    public double minimum() {
        return minimum;
    }

    public double maximum() {
        return maximum;
    }

    public double step() {
        return step;
    }

    public boolean integerCompatible() {
        return isWhole(minimum)
                && isWhole(maximum)
                && isWhole(step)
                && minimum >= Integer.MIN_VALUE
                && maximum <= Integer.MAX_VALUE
                && step <= Integer.MAX_VALUE;
    }

    public int stepInteger(
            final int current,
            final int direction) {
        if (!integerCompatible()) {
            throw new IllegalStateException(
                    "numeric spec is not integer-compatible");
        }
        if (direction != -1 && direction != 1) {
            throw new IllegalArgumentException(
                    "direction must be -1 or 1");
        }

        final double candidate =
                current + step * direction;
        final double clamped =
                Math.max(
                        minimum,
                        Math.min(
                                maximum,
                                candidate));
        return (int) Math.round(clamped);
    }

    public double stepDouble(
            final double current,
            final int direction) {
        requireFinite(current, "current");
        if (direction != -1 && direction != 1) {
            throw new IllegalArgumentException(
                    "direction must be -1 or 1");
        }

        final double candidate =
                current + step * direction;
        return Math.max(
                minimum,
                Math.min(
                        maximum,
                        candidate));
    }

    private static boolean isWhole(final double value) {
        return Math.rint(value) == value;
    }

    private static void requireFinite(
            final double value,
            final String name) {
        if (Double.isNaN(value)
                || Double.isInfinite(value)) {
            throw new IllegalArgumentException(
                    name + " must be finite");
        }
    }
}
