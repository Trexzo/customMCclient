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

    /**
     * Native original Minecraft instant health tiers: I provides 4 health
     * points, II provides 8 at full splash strength. If <=4 health points
     * are missing, the weaker source splash is enough in an ideal direct
     * hit, so prefer conserving an otherwise useful Instant Health II.
     * Never prevent use of II if I is unavailable. Other rankings remain
     * unchanged. Original source health is validated by the caller.
     */
    static int healthAwarePriority(final int sourceDamage,
            final float missingHealth, final boolean conserve) {
        if (conserve && Float.isFinite(missingHealth)
                && missingHealth > 0.0F && missingHealth <= 4.0F) {
            if (sourceDamage == 16389) return 60;
            if (sourceDamage == 16421) return 50;
        }
        return immediateHealingPriority(sourceDamage);
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
