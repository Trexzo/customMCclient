package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerArmorState {
    public static final int BOOTS_BIT = 1;
    public static final int LEGGINGS_BIT = 1 << 1;
    public static final int CHESTPLATE_BIT = 1 << 2;
    public static final int HELMET_BIT = 1 << 3;
    private static final int ALL_BITS =
            BOOTS_BIT
                    | LEGGINGS_BIT
                    | CHESTPLATE_BIT
                    | HELMET_BIT;

    private boolean available;
    private int mask;
    private SlotDurability bootsDurability =
            SlotDurability.unavailable();
    private SlotDurability leggingsDurability =
            SlotDurability.unavailable();
    private SlotDurability chestplateDurability =
            SlotDurability.unavailable();
    private SlotDurability helmetDurability =
            SlotDurability.unavailable();

    public synchronized void update(
            final boolean boots,
            final boolean leggings,
            final boolean chestplate,
            final boolean helmet) {
        update(
                boots,
                leggings,
                chestplate,
                helmet,
                null,
                null,
                null,
                null);
    }

    public synchronized void update(
            final boolean boots,
            final boolean leggings,
            final boolean chestplate,
            final boolean helmet,
            final Minecraft189ItemStackAccess bootsItem,
            final Minecraft189ItemStackAccess leggingsItem,
            final Minecraft189ItemStackAccess chestplateItem,
            final Minecraft189ItemStackAccess helmetItem) {
        int nextMask = 0;
        if (boots) {
            nextMask |= BOOTS_BIT;
        }
        if (leggings) {
            nextMask |= LEGGINGS_BIT;
        }
        if (chestplate) {
            nextMask |= CHESTPLATE_BIT;
        }
        if (helmet) {
            nextMask |= HELMET_BIT;
        }
        mask = nextMask;
        bootsDurability =
                SlotDurability.from(
                        bootsItem);
        leggingsDurability =
                SlotDurability.from(
                        leggingsItem);
        chestplateDurability =
                SlotDurability.from(
                        chestplateItem);
        helmetDurability =
                SlotDurability.from(
                        helmetItem);
        available = true;
    }

    public synchronized void clear() {
        available = false;
        mask = 0;
        bootsDurability =
                SlotDurability.unavailable();
        leggingsDurability =
                SlotDurability.unavailable();
        chestplateDurability =
                SlotDurability.unavailable();
        helmetDurability =
                SlotDurability.unavailable();
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                mask,
                bootsDurability,
                leggingsDurability,
                chestplateDurability,
                helmetDurability);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int mask;
        private final SlotDurability bootsDurability;
        private final SlotDurability leggingsDurability;
        private final SlotDurability chestplateDurability;
        private final SlotDurability helmetDurability;

        private Snapshot(
                final boolean available,
                final int mask,
                final SlotDurability bootsDurability,
                final SlotDurability leggingsDurability,
                final SlotDurability chestplateDurability,
                final SlotDurability helmetDurability) {
            if ((mask & ~ALL_BITS) != 0) {
                throw new IllegalArgumentException(
                        "armor mask contains unsupported bits");
            }
            this.available = available;
            this.mask = mask;
            this.bootsDurability = bootsDurability;
            this.leggingsDurability = leggingsDurability;
            this.chestplateDurability = chestplateDurability;
            this.helmetDurability = helmetDurability;
        }

        public boolean available() {
            return available;
        }

        public int mask() {
            return mask;
        }

        public boolean boots() {
            return (mask & BOOTS_BIT) != 0;
        }

        public boolean leggings() {
            return (mask & LEGGINGS_BIT) != 0;
        }

        public boolean chestplate() {
            return (mask & CHESTPLATE_BIT) != 0;
        }

        public boolean helmet() {
            return (mask & HELMET_BIT) != 0;
        }

        public int equippedCount() {
            return Integer.bitCount(mask);
        }

        public SlotDurability bootsDurability() {
            return bootsDurability;
        }

        public SlotDurability leggingsDurability() {
            return leggingsDurability;
        }

        public SlotDurability chestplateDurability() {
            return chestplateDurability;
        }

        public SlotDurability helmetDurability() {
            return helmetDurability;
        }

        public boolean hasDurabilityDetails() {
            return bootsDurability.available()
                    || leggingsDurability.available()
                    || chestplateDurability.available()
                    || helmetDurability.available();
        }
    }

    public static final class SlotDurability {
        private static final SlotDurability UNAVAILABLE =
                new SlotDurability(
                        false,
                        0,
                        0);

        private final boolean available;
        private final int itemDamage;
        private final int maxDamage;

        private SlotDurability(
                final boolean available,
                final int itemDamage,
                final int maxDamage) {
            this.available = available;
            this.itemDamage = itemDamage;
            this.maxDamage = maxDamage;
        }

        private static SlotDurability unavailable() {
            return UNAVAILABLE;
        }

        private static SlotDurability from(
                final Minecraft189ItemStackAccess item) {
            if (item == null) {
                return unavailable();
            }
            final int itemDamage =
                    item.customMcItemDamage();
            final int maxDamage =
                    item.customMcMaxDamage();
            if (itemDamage < 0) {
                throw new IllegalArgumentException(
                        "armor itemDamage must be non-negative");
            }
            if (maxDamage < 0) {
                throw new IllegalArgumentException(
                        "armor maxDamage must be non-negative");
            }
            return new SlotDurability(
                    true,
                    itemDamage,
                    maxDamage);
        }

        public boolean available() {
            return available;
        }

        public int itemDamage() {
            return itemDamage;
        }

        public int maxDamage() {
            return maxDamage;
        }

        public boolean damageable() {
            return available
                    && maxDamage > 0;
        }

        public int durabilityRemaining() {
            if (!damageable()) {
                return 0;
            }
            return Math.max(
                    0,
                    maxDamage - itemDamage);
        }
    }
}
