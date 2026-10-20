package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * One exact original player across native NoFall read/write and the
 * subsequent physical fall-distance readback for Criticals evidence.
 */
final class Minecraft189FallHostSourceLease
        extends Minecraft189ExactSourceLease<Minecraft189PlayerFallDistanceControl> {
    Minecraft189PlayerFallDistanceControl guardedPlayer(
            final Minecraft189PlayerFallDistanceControl original,
            final BooleanSupplier actualGuiFocused) {
        if (original == null) return null;
        Objects.requireNonNull(actualGuiFocused, "actualGuiFocused");
        return new Minecraft189PlayerFallDistanceControl() {
            @Override public float customMcFallDistance() {
                return original.customMcFallDistance();
            }
            @Override public void customMcSetFallDistance(final float value) {
                // Check the exact original player and GUI focus right
                // before the real mapped setter. No write after a
                // different-source nested host callback or new GUI.
                if (stillOwned() && !actualGuiFocused.getAsBoolean()
                        && stillOwned()) {
                    original.customMcSetFallDistance(value);
                }
            }
        };
    }
}
