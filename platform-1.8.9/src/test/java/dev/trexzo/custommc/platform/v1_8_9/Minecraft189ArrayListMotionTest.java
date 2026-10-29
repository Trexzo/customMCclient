package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189ArrayListMotionTest {
    @Test
    void initialSnapshotIsFullyVisibleButLaterEnablesEaseIn() {
        final Minecraft189ArrayListMotion motion =
                new Minecraft189ArrayListMotion();
        Map<String, Float> positions = motion.advance(
                Arrays.asList("module.a", "module.b"), true);
        assertEquals(1.0F, positions.get("module.a"), .0001F);
        assertEquals(1.0F, positions.get("module.b"), .0001F);
        positions = motion.advance(
                Arrays.asList("module.a", "module.b", "module.c"), true);
        assertEquals(.22F, positions.get("module.c"), .0001F);
        assertEquals(1.0F, positions.get("module.a"), .0001F);
        for (int i = 0; i < 4; i++) {
            positions = motion.advance(
                    Arrays.asList("module.a", "module.b", "module.c"), true);
        }
        assertEquals(1.0F, positions.get("module.c"), .0001F);
        assertEquals(0.0F,
                Minecraft189ArrayListMotion.slide(positions.get("module.c")),
                .0001F);
    }

    @Test
    void disablingImmediatelyDropsEntryAndReenableReenters() {
        final Minecraft189ArrayListMotion motion =
                new Minecraft189ArrayListMotion();
        motion.advance(Collections.singletonList("a"), true);
        motion.advance(Arrays.asList("a", "b"), true);
        Map<String, Float> result =
                motion.advance(Collections.singletonList("a"), true);
        assertFalse(result.containsKey("b"));
        assertEquals(1.0F, result.get("a"), .0001F);
        result = motion.advance(Arrays.asList("a", "b"), true);
        assertEquals(.22F, result.get("b"), .0001F);
        motion.reset();
        assertEquals(1.0F,
                motion.advance(Collections.singletonList("b"), true).get("b"),
                .0001F);
    }

    @Test
    void reducedMotionIsInstantAndSnapshotsAreImmutable() {
        final Minecraft189ArrayListMotion motion =
                new Minecraft189ArrayListMotion();
        motion.advance(Collections.singletonList("a"), true);
        Map<String, Float> result =
                motion.advance(Arrays.asList("a", "b"), false);
        assertEquals(1.0F, result.get("b"), .0001F);
        assertThrows(UnsupportedOperationException.class,
                () -> result.put("c", Float.valueOf(0.5F)));
        assertThrows(IllegalArgumentException.class,
                () -> motion.advance(Arrays.asList("a", "a"), true));
        assertTrue(motion.advance(Collections.<String>emptyList(), true).isEmpty());
    }

    @Test
    void opacityPreservesRgbAndBoundsAlpha() {
        assertEquals(0x00EE8755,
                Minecraft189ArrayListMotion.withOpacity(0xFFEE8755, 0.0F));
        assertEquals(0x80EE8755,
                Minecraft189ArrayListMotion.withOpacity(0xFFEE8755, 0.5F));
        assertEquals(0xFFEE8755,
                Minecraft189ArrayListMotion.withOpacity(0xFFEE8755, 3.0F));
        assertEquals(0x50000000,
                Minecraft189ArrayListMotion.withOpacity(0x50000000, 1.0F));
        assertEquals(12.0F, Minecraft189ArrayListMotion.slide(0.0F), .0001F);
        assertTrue(Minecraft189ArrayListMotion.slide(.22F) > 0.0F);
        assertTrue(Minecraft189ArrayListMotion.slide(.22F)
                > Minecraft189ArrayListMotion.slide(.44F));
        assertTrue(Minecraft189ArrayListMotion.slide(.44F)
                > Minecraft189ArrayListMotion.slide(.66F));
        assertEquals(0.0F, Minecraft189ArrayListMotion.slide(1.0F), .0001F);
    }
}
