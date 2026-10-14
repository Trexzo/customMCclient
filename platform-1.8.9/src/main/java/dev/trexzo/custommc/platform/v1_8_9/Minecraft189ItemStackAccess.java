package dev.trexzo.custommc.platform.v1_8_9;

public interface Minecraft189ItemStackAccess {
    String customMcDisplayName();

    int customMcStackSize();

    int customMcItemDamage();

    int customMcMaxDamage();

    default boolean customMcIsSword() { return false; }

    /** Exact native ItemStack.item instanceof source-mapped ItemBlock. */
    default boolean customMcIsBlockItem() { return false; }

    /**
     * Exact source-mapped vanilla ItemStack.item reference, not a display name,
     * registry guess or a reusable ItemStack object. Original 1.8.9 server
     * inventory synchronization may replace an ItemStack while the Item
     * singleton remains unchanged. Null means identity cannot be proven.
     */
    default Object customMcOriginalItemIdentity() { return null; }

    /** Base sword damage only; NaN when not a verified ItemSword. */
    default float customMcSwordBaseDamage() { return Float.NaN; }

    default boolean customMcIsFishingRod() { return false; }

    default boolean customMcIsPotion() { return false; }
}
