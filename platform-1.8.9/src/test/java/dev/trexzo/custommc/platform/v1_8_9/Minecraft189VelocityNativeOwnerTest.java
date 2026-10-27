package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V339: EntityLivingBase.knockBack runs on every living source; Velocity
 * belongs only to the original transformed Minecraft 1.8.9 client player.
 * These Java doubles cannot impersonate the native 'bew' source class.
 */
final class Minecraft189VelocityNativeOwnerTest {
    @Test
    void unknownAndRemoteKnockbackAlwaysRemainVanilla() {
        assertFalse(Minecraft189RuntimeBridge.velocityOriginalLocalOwner(null));
        assertFalse(Minecraft189RuntimeBridge.velocityOriginalLocalOwner(
                new Object()));
        assertFalse(Minecraft189RuntimeBridge.velocityOriginalLocalOwner(
                new NativeLookingRemotePlayer()));
        final Object remote = new NativeLookingRemotePlayer();
        assertEquals(5.0D,
                Minecraft189RuntimeBridge.adjustVelocityHorizontalOriginalOwner(
                        remote, 2.0D, 5.0D));
        assertEquals(6.0D,
                Minecraft189RuntimeBridge.adjustVelocityVerticalOriginalOwner(
                        remote, 4.0D, 6.0D));
        assertEquals(Double.doubleToRawLongBits(-0.0D),
                Double.doubleToRawLongBits(
                        Minecraft189RuntimeBridge
                                .adjustVelocityHorizontalOriginalOwner(
                                        remote, 1.0D, -0.0D)));
        assertTrue(Double.isNaN(
                Minecraft189RuntimeBridge.adjustVelocityVerticalOriginalOwner(
                        null, 0.0D, Double.NaN)));
    }

    private static final class NativeLookingRemotePlayer { }
}
