package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Source transformed Entity.rotationPitch remains owned by the real user. */
final class Minecraft189AutoPotPitchLeaseTest {
    @Test void nativePitchLeaseAimsDownAndRestoresOriginalPitch() {
        final NativePlayer player = new NativePlayer();
        player.pitch = 16.5F;
        final Minecraft189AutoPotPitchLease lease =
                Minecraft189AutoPotPitchLease.begin(player);
        assertEquals(85F, player.pitch);
        assertEquals(1, player.sets);
        assertTrue(lease.mayThrow(player));
        assertEquals(16.5F, lease.originalPitch());
        lease.restore();
        assertEquals(16.5F, player.pitch);
        assertEquals(2, player.sets);
        assertFalse(lease.mayThrow(player));
        lease.restore();
        assertEquals(2, player.sets, "double finally must be harmless");
    }

    @Test void neverOverwriteUserOrForeignOwnerRotationChanges() {
        final NativePlayer player = new NativePlayer();
        final NativePlayer other = new NativePlayer();
        player.pitch = -13F;
        final Minecraft189AutoPotPitchLease lease =
                Minecraft189AutoPotPitchLease.begin(player);
        assertFalse(lease.mayThrow(other), "respawned source player cannot borrow pitch");
        player.pitch = 32F;
        assertFalse(lease.mayThrow(player), "manual mouse movement revokes pitch");
        lease.restore();
        assertEquals(32F, player.pitch,
                "original callback changed pitch; restore must not overwrite it");
        assertEquals(1, player.sets);
    }

    @Test void alreadyDownwardAimDoesNotWriteOrRestore() {
        final NativePlayer player = new NativePlayer();
        player.pitch = 85F;
        final Minecraft189AutoPotPitchLease lease =
                Minecraft189AutoPotPitchLease.begin(player);
        assertTrue(lease.mayThrow(player));
        assertEquals(0, player.sets);
        lease.restore();
        assertEquals(0, player.sets);
        assertEquals(85F, player.pitch);
    }

    @Test void missingInvalidIgnoredAndThrowingSourceSetterFailClosed() {
        assertFalse(Minecraft189AutoPotPitchLease.begin(null).mayThrow(null));
        final NativePlayer wrong = new NativePlayer();
        wrong.pitch = Float.NaN;
        assertFalse(Minecraft189AutoPotPitchLease.begin(wrong).mayThrow(wrong));
        assertEquals(0, wrong.sets);
        wrong.pitch = 100F;
        assertFalse(Minecraft189AutoPotPitchLease.begin(wrong).mayThrow(wrong));
        wrong.pitch = 0F;
        wrong.ignoreNextSetter = true;
        assertFalse(Minecraft189AutoPotPitchLease.begin(wrong).mayThrow(wrong),
                "ignored source setter cannot grant cast permission");
        assertEquals(0F, wrong.pitch);

        wrong.throwAfterSetter = true;
        assertFalse(Minecraft189AutoPotPitchLease.begin(wrong).mayThrow(wrong),
                "original setter mutating and throwing must fail closed");
        assertEquals(0F, wrong.pitch, "partially applied pitch must be restored");
    }

    private static final class NativePlayer
            implements Minecraft189PlayerRotationControl {
        float pitch;
        int sets;
        boolean ignoreNextSetter;
        boolean throwAfterSetter;
        @Override public float customMcRotationYaw() { return 0F; }
        @Override public float customMcRotationPitch() { return pitch; }
        @Override public void customMcSetRotationYaw(final float yaw) {}
        @Override public void customMcSetRotationPitch(final float next) {
            sets++;
            if (ignoreNextSetter) {
                ignoreNextSetter = false;
                return;
            }
            pitch = next;
            if (throwAfterSetter) {
                throwAfterSetter = false;
                throw new IllegalStateException("source pitch setter fault");
            }
        }
    }
}
