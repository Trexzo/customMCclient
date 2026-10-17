package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Real source interface behavior: no cached names or fabricated use ticks. */
final class Minecraft189AutoPotRegenerationGuardTest {
    @Test void regenerationPotionIsVetoedOnlyWithLongEnoughOriginalEffect() {
        final Inventory inv = new Inventory();
        final Player owner = new Player(inv);
        final Item regeneration = new Item(16417);
        inv.items[3] = regeneration;
        inv.selected = 3;
        assertTrue(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner),
                "real splash regen is allowed with no active regeneration");
        owner.effects = new Minecraft189PotionEffectAccess[]{new Effect(10, 200)};
        assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner),
                "do not waste splash regen during already active regeneration");
        owner.effects = new Minecraft189PotionEffectAccess[]{new Effect(10, 39)};
        assertTrue(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner),
                "short nearly expired effect may be refreshed");
        owner.effects = new Minecraft189PotionEffectAccess[]{new Effect(1, 300)};
        assertTrue(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner),
                "speed or other potion effect must not block source regen");
        for (final int damage : new int[]{16385,16417,16449}) {
            regeneration.damage = damage;
            owner.effects = new Minecraft189PotionEffectAccess[]{new Effect(10, 40)};
            assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner));
        }
    }

    @Test void instantHealingStaysAvailableEvenWithLongActiveRegeneration() {
        final Inventory inv = new Inventory();
        final Player owner = new Player(inv);
        owner.effects = new Minecraft189PotionEffectAccess[]{new Effect(10, 500)};
        inv.selected = 3;
        final Item potion = new Item(16389);
        inv.items[3] = potion;
        assertTrue(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner));
        potion.damage = 16421;
        assertTrue(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner));
        for (final int rejected : new int[]{0,16388,16396,8197,-1}) {
            potion.damage = rejected;
            assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner));
        }
    }

    @Test void sourceMutationAndMissingNativeEvidenceFailClosed() {
        final Inventory inv = new Inventory();
        final Player owner = new Player(inv);
        inv.selected = 3;
        final Item potion = new Item(16417);
        inv.items[3] = potion;
        final Inventory foreign = new Inventory();
        assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(foreign, owner));
        owner.effects = null;
        assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner));
        owner.effects = new Minecraft189PotionEffectAccess[]{null};
        assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner));
        owner.effects = new Minecraft189PotionEffectAccess[]{new Effect(10, -1)};
        assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner));
        owner.effects = new Minecraft189PotionEffectAccess[0];
        potion.count = 0;
        assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner));
        potion.count = 1;
        inv.selected = 9;
        assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner));
        inv.selected = 3;
        owner.afterEffectsRead = () -> inv.selected = 5;
        assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner),
                "live human currentItem change during source effects getter vetoes");
        inv.selected = 3;
        potion.afterDamageRead = () -> {
            inv.items[3] = new Item(16417);
        };
        assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner),
                "replacement original ItemStack invalidates exact membership");
    }

    @Test void reentrantNativeInventoryReadStartingRegenerationVetoesFinalUse() {
        final Inventory inv = new Inventory();
        final Player owner = new Player(inv);
        inv.selected = 3;
        inv.items[3] = new Item(16417);
        inv.duringItemsRead = () ->
                owner.effects = new Minecraft189PotionEffectAccess[]{
                        new Effect(10, 200)};
        assertFalse(Minecraft189AutoPotRegenerationGuard.mayUse(inv, owner),
                "source metadata callback added regeneration before native use");
    }

    private static final class Inventory
            implements Minecraft189InventoryHotbarItemsAccess {
        int selected;
        Runnable duringItemsRead;
        final Minecraft189ItemStackAccess[] items =
                new Minecraft189ItemStackAccess[9];
        @Override public int customMcSelectedHotbarSlot() { return selected; }
        @Override public void customMcSetSelectedHotbarSlot(final int s) {
            selected = s;
        }
        @Override public Minecraft189ItemStackAccess[] customMcHotbarItems() {
            final Runnable callback = duringItemsRead;
            duringItemsRead = null;
            if (callback != null) callback.run();
            return items;
        }
    }

    private static final class Player implements
            Minecraft189PlayerInventoryAccess, Minecraft189PlayerPotionEffectsAccess {
        final Inventory inv;
        Minecraft189PotionEffectAccess[] effects = new Minecraft189PotionEffectAccess[0];
        Runnable afterEffectsRead;
        Player(final Inventory inv) { this.inv = inv; }
        @Override public Minecraft189InventoryHotbarAccess customMcInventory() {
            return inv;
        }
        @Override public Minecraft189PotionEffectAccess[] customMcPotionEffects() {
            final Runnable callback = afterEffectsRead;
            afterEffectsRead = null;
            if (callback != null) callback.run();
            return effects;
        }
    }

    private static final class Effect implements Minecraft189PotionEffectAccess {
        final int id, duration;
        Effect(final int id, final int duration) {
            this.id = id;
            this.duration = duration;
        }
        @Override public int customMcPotionId() { return id; }
        @Override public int customMcDurationTicks() { return duration; }
        @Override public int customMcAmplifier() { return 1; }
        @Override public String customMcEffectName() { return "source.effect"; }
    }

    private static final class Item implements Minecraft189ItemStackAccess {
        int damage, count = 1;
        Runnable afterDamageRead;
        Item(final int damage) { this.damage = damage; }
        @Override public boolean customMcIsPotion() { return true; }
        @Override public boolean customMcIsFishingRod() { return false; }
        @Override public int customMcItemDamage() {
            final Runnable callback = afterDamageRead;
            afterDamageRead = null;
            if (callback != null) callback.run();
            return damage;
        }
        @Override public int customMcMaxDamage() { return 0; }
        @Override public int customMcStackSize() { return count; }
        @Override public String customMcDisplayName() { return "untrusted"; }
    }
}
