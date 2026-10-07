package dev.trexzo.custommc.platform.v1_8_9;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class Minecraft189PlayerPotionEffectsState {
    private boolean available;
    private List<Snapshot> effects =
            Collections.emptyList();

    public synchronized void update(
            final Minecraft189PotionEffectAccess[] nextEffects) {
        Objects.requireNonNull(
                nextEffects,
                "nextEffects");
        final List<Snapshot> next =
                new ArrayList<Snapshot>(
                        nextEffects.length);
        for (Minecraft189PotionEffectAccess effect : nextEffects) {
            final Minecraft189PotionEffectAccess source =
                    Objects.requireNonNull(
                            effect,
                            "potion effect");
            next.add(
                    new Snapshot(
                            source.customMcPotionId(),
                            source.customMcDurationTicks(),
                            source.customMcAmplifier(),
                            source.customMcEffectName()));
        }
        Collections.sort(
                next,
                new Comparator<Snapshot>() {
                    @Override
                    public int compare(
                            final Snapshot left,
                            final Snapshot right) {
                        final int name =
                                left.effectName()
                                        .compareTo(
                                                right.effectName());
                        if (name != 0) {
                            return name;
                        }
                        return Integer.compare(
                                left.potionId(),
                                right.potionId());
                    }
                });
        effects =
                Collections.unmodifiableList(
                        next);
        available = true;
    }

    public synchronized void clear() {
        available = false;
        effects = Collections.emptyList();
    }

    public synchronized StateSnapshot snapshot() {
        return new StateSnapshot(
                available,
                effects);
    }

    public static final class StateSnapshot {
        private final boolean available;
        private final List<Snapshot> effects;

        private StateSnapshot(
                final boolean available,
                final List<Snapshot> effects) {
            this.available = available;
            this.effects = effects;
        }

        public boolean available() {
            return available;
        }

        public List<Snapshot> effects() {
            return effects;
        }
    }

    public static final class Snapshot {
        private final int potionId;
        private final int durationTicks;
        private final int amplifier;
        private final String effectName;

        private Snapshot(
                final int potionId,
                final int durationTicks,
                final int amplifier,
                final String effectName) {
            if (potionId < 0) {
                throw new IllegalArgumentException(
                        "potionId must be non-negative");
            }
            if (durationTicks < 0) {
                throw new IllegalArgumentException(
                        "durationTicks must be non-negative");
            }
            if (amplifier < 0) {
                throw new IllegalArgumentException(
                        "amplifier must be non-negative");
            }
            final String name =
                    Objects.requireNonNull(
                            effectName,
                            "effectName")
                            .trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException(
                        "effectName must not be blank");
            }
            this.potionId = potionId;
            this.durationTicks = durationTicks;
            this.amplifier = amplifier;
            this.effectName = name;
        }

        public int potionId() {
            return potionId;
        }

        public int durationTicks() {
            return durationTicks;
        }

        public int amplifier() {
            return amplifier;
        }

        public String effectName() {
            return effectName;
        }
    }
}
