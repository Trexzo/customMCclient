package dev.trexzo.custommc.platform.v1_8_9;

public interface Minecraft189ItemStackAccess {
    String customMcDisplayName();

    int customMcStackSize();

    int customMcItemDamage();

    int customMcMaxDamage();

    default boolean customMcIsSword() { return false; }

    /** Base sword damage only; NaN when not a verified ItemSword. */
    default float customMcSwordBaseDamage() { return Float.NaN; }

    default boolean customMcIsFishingRod() { return false; }

    default boolean customMcIsPotion() { return false; }
}
