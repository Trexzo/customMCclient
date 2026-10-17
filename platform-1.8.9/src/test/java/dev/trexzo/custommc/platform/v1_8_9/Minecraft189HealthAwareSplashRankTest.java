package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Regression for original 1.8.9 Instant Health I/II potion metadata. */
final class Minecraft189HealthAwareSplashRankTest {
    @Test void conservativeSourceHealTierOnlyChangesWhenStrongWouldOverheal() {
        for (float missing : new float[]{0.5F, 1F, 2F, 3.5F, 4F}) {
            assertTrue(Minecraft189LegacySplashHealingPotion
                            .healthAwarePriority(16389, missing, true)
                        > Minecraft189LegacySplashHealingPotion
                            .healthAwarePriority(16421, missing, true),
                    "original Instant Health I suffices below four missing health");
        }
        for (float missing : new float[]{4.01F, 5F, 8F, 12F, 20F}) {
            assertTrue(Minecraft189LegacySplashHealingPotion
                            .healthAwarePriority(16421, missing, true)
                        > Minecraft189LegacySplashHealingPotion
                            .healthAwarePriority(16389, missing, true),
                    "larger health deficit still selects higher-tier healing");
        }
        assertTrue(Minecraft189LegacySplashHealingPotion
                        .healthAwarePriority(16421, 2F, false)
                    > Minecraft189LegacySplashHealingPotion
                        .healthAwarePriority(16389, 2F, false),
                "default-off must preserve existing strong-heal ranking");
    }

    @Test void unknownAndDelayedEffectTypesAreNeverPromotedByHealingEconomy() {
        assertEquals(0, Minecraft189LegacySplashHealingPotion
                .healthAwarePriority(0, 2F, true));
        assertEquals(0, Minecraft189LegacySplashHealingPotion
                .healthAwarePriority(16396, 2F, true));
        for (final int damage : new int[]{16385,16417,16449}) {
            assertEquals(Minecraft189LegacySplashHealingPotion
                            .immediateHealingPriority(damage),
                    Minecraft189LegacySplashHealingPotion
                            .healthAwarePriority(damage, 2F, true),
                    "delayed native Regeneration ranks exactly as before");
        }
        assertEquals(50, Minecraft189LegacySplashHealingPotion
                .healthAwarePriority(16421, Float.NaN, true),
                "no fabricated priority from invalid health");
    }
}
