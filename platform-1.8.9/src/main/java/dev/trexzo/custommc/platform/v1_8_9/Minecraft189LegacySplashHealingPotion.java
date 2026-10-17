package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Conservative original Minecraft 1.8.9 legacy splash-healing metadata.
 * This supplements, never replaces, proven native ItemPotion class and
 * ItemStack membership. Custom NBT effects and server outcomes are unproven.
 */
final class Minecraft189LegacySplashHealingPotion {
    private Minecraft189LegacySplashHealingPotion() {}

    /**
     * User opt-in combat priority: direct healing wins over delayed regen.
     * Returns zero for unknown/nonhealing variants. This is a ranking of
     * known vanilla item metadata, not a verified server healing outcome.
     */
    static int immediateHealingPriority(final int sourceDamage) {
        switch (sourceDamage) {
            case 16421: return 50; // splash instant health II
            case 16389: return 40; // splash instant health I
            case 16417: return 30; // splash regeneration II
            case 16385: return 20; // splash regeneration I
            case 16449: return 10; // splash extended regeneration
            default: return 0;
        }
    }

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
