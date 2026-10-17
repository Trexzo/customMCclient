package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189LegacySplashHealingPotionTest {
    @Test void onlyKnownVanillaSplashHealingAndRegenerationMetadataPass() {
        for (final int permitted : new int[] {
                16389, 16421, 16385, 16417, 16449
        })
            assertTrue(Minecraft189LegacySplashHealingPotion
                    .isKnownSplashHealing(permitted), "source item data " + permitted);
        for (final int rejected : new int[] {
                Integer.MIN_VALUE, -1, 0, 1, 8193, 8197, 8225, 8229,
                16388, 16420, 16396, 16428, 16386, 16390, 16392, 16453,
                32767
        })
            assertFalse(Minecraft189LegacySplashHealingPotion
                    .isKnownSplashHealing(rejected), "nonhealing data " + rejected);
    }
}
