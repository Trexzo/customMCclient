package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189VelocityModuleTest {
    @Test
    void velocityScalesOnlyKnockbackDeltaAndPreservesVanillaWhileDisabled() {
        final Minecraft189VelocityModule module =
                new Minecraft189VelocityModule();

        assertFalse(
                module.active());
        assertEquals(
                5.0D,
                module.adjustHorizontal(
                        2.0D,
                        5.0D),
                0.000001D);
        assertEquals(
                6.0D,
                module.adjustVertical(
                        4.0D,
                        6.0D),
                0.000001D);

        module.onEnable();
        assertTrue(
                module.active());
        assertEquals(
                2.0D,
                module.adjustHorizontal(
                        2.0D,
                        5.0D),
                0.000001D);
        assertEquals(
                4.0D,
                module.adjustVertical(
                        4.0D,
                        6.0D),
                0.000001D);

        module.horizontalPercentSetting()
                .set(50);
        module.verticalPercentSetting()
                .set(25);
        assertEquals(
                3.5D,
                module.adjustHorizontal(
                        2.0D,
                        5.0D),
                0.000001D);
        assertEquals(
                4.5D,
                module.adjustVertical(
                        4.0D,
                        6.0D),
                0.000001D);

        module.horizontalPercentSetting()
                .set(150);
        module.verticalPercentSetting()
                .set(200);
        assertEquals(
                6.5D,
                module.adjustHorizontal(
                        2.0D,
                        5.0D),
                0.000001D);
        assertEquals(
                8.0D,
                module.adjustVertical(
                        4.0D,
                        6.0D),
                0.000001D);

        assertThrows(
                IllegalArgumentException.class,
                () -> module.horizontalPercentSetting()
                        .set(-1));
        assertThrows(
                IllegalArgumentException.class,
                () -> module.verticalPercentSetting()
                        .set(201));

        module.onDisable();
        assertFalse(
                module.active());
        assertEquals(
                5.0D,
                module.adjustHorizontal(
                        2.0D,
                        5.0D),
                0.000001D);
    }
}
