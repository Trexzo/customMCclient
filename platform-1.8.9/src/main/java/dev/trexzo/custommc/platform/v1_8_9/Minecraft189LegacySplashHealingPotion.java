package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Conservative original Minecraft 1.8.9 legacy splash-healing metadata.
 * This supplements, never replaces, proven native ItemPotion class and
 * ItemStack membership. Custom NBT effects and server outcomes are unproven.
 */
final class Minecraft189LegacySplashHealingPotion {
    private Minecraft189LegacySplashHealingPotion() {}

    static boolean isKnownSplashHealing(final int sourceDamage) {
        switch (sourceDamage) {
            case 16389: // splash instant health I
            case 16421: // splash instant health II
            case 16385: // splash regeneration I
            case 16417: // splash regeneration II
            case 16449: // splash extended regeneration
                return true;
            default:
                return false;
        }
    }
}
