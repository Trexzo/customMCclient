package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Genuine Java player-motion interface adapter tests, not Mojang runtime.
 * Native getter/setter callbacks may reenter settings, keyboard or lifecycle.
 */
final class Minecraft189FlightNativeAxisReadbackLeaseTest {
    private static final class Motion implements Minecraft189PlayerMotionControl {
        double x, y, z;
        int readX, readY, readZ, writeX, writeY, writeZ;
        Runnable onReadX, onReadY, onReadZ, onWriteX, onWriteY;
        @Override public double customMcMotionX() {
            readX++;
            final double result = x;
            if (onReadX != null) {
                final Runnable action = onReadX;
                onReadX = null;
                action.run();
            }
            return result;
        }
        @Override public double customMcMotionY() {
            readY++;
            final double result = y;
            if (onReadY != null) {
                final Runnable action = onReadY;
                onReadY = null;
                action.run();
            }
            return result;
        }
        @Override public double customMcMotionZ() {
            readZ++;
            final double result = z;
            if (onReadZ != null) {
                final Runnable action = onReadZ;
                onReadZ = null;
                action.run();
            }
            return result;
        }
        @Override public void customMcSetMotionX(double next) {
            writeX++;
            x = next;
            if (onWriteX != null) {
                final Runnable action = onWriteX;
                onWriteX = null;
                action.run();
            }
        }
        @Override public void customMcSetMotionY(double next) {
            writeY++;
            y = next;
            if (onWriteY != null) {
                final Runnable action = onWriteY;
                onWriteY = null;
                action.run();
            }
        }
        @Override public void customMcSetMotionZ(double next) {
            writeZ++;
            z = next;
        }
    }

    private static Minecraft189PlayerRotationState.Snapshot yawZero() {
        final Minecraft189PlayerRotationState state =
                new Minecraft189PlayerRotationState();
        state.update(0F);
        return state.snapshot();
    }

    @Test void originalYGetterOffOnCannotSpendPreviousFlightOwner() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189FlightModule flight = new Minecraft189FlightModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.SPACE, true);
        flight.onEnable();
        p.onReadY = () -> { flight.onDisable(); flight.onEnable(); };
        flight.apply(p, yawZero());
        assertEquals(0, p.writeY);
        assertEquals(0, p.readX);
        flight.apply(p, yawZero());
        assertEquals(0.30D, p.y, 0.000001D);
        flight.onDisable();
    }

    @Test void verticalNativeGetterGuiSpeedEditVetoesOldY() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189FlightModule flight =
                new Minecraft189FlightModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.SPACE, true);
        flight.onEnable();
        p.onReadY = () -> flight.verticalSpeedSetting().set(0.45D);
        flight.apply(p, yawZero());
        assertEquals(0, p.writeY);
        flight.apply(p, yawZero());
        assertEquals(1, p.writeY);
        assertEquals(0.45D, p.y, 0.000001D);
        flight.onDisable();
    }

    @Test void verticalRealSpaceReleasedDuringNativeGetterRevokesAscent() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189FlightModule flight = new Minecraft189FlightModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.SPACE, true);
        flight.onEnable();
        p.onReadY = () -> input.key(LegacyKeyboardCodes.SPACE, false);
        flight.apply(p, yawZero());
        assertEquals(0, p.writeY);
        flight.apply(p, yawZero());
        assertEquals(0, p.writeY); // Fresh hover.
        flight.onDisable();
    }

    @Test void originalXGetterRestartCannotReadZOrWriteAnyHorizontalAxis() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189FlightModule flight = new Minecraft189FlightModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W, true);
        flight.onEnable();
        p.onReadX = () -> { flight.onDisable(); flight.onEnable(); };
        flight.apply(p, yawZero());
        assertEquals(1, p.readX);
        assertEquals(0, p.readZ);
        assertEquals(0, p.writeX + p.writeZ);
        flight.apply(p, yawZero());
        assertEquals(0.30D, p.z, 0.000001D);
        flight.onDisable();
    }

    @Test void originalXGetterKeyReleaseSkipsZReadAndStaleWrite() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189FlightModule flight = new Minecraft189FlightModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W, true);
        flight.onEnable();
        p.onReadX = () -> input.key(LegacyKeyboardCodes.W, false);
        flight.apply(p, yawZero());
        assertEquals(1, p.readX);
        assertEquals(0, p.readZ);
        assertEquals(0, p.writeX + p.writeZ);
        flight.onDisable();
    }

    @Test void originalZGetterLiveSpeedEditRevokesBothWrites() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189FlightModule flight = new Minecraft189FlightModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W, true);
        input.key(LegacyKeyboardCodes.A, true);
        flight.onEnable();
        p.onReadZ = () -> flight.horizontalSpeedSetting().set(0.60D);
        flight.apply(p, yawZero());
        assertEquals(1, p.readX);
        assertEquals(1, p.readZ);
        assertEquals(0, p.writeX + p.writeZ);
        flight.apply(p, yawZero());
        assertEquals(0.60D / Math.sqrt(2D), p.x, 0.000001D);
        assertEquals(0.60D / Math.sqrt(2D), p.z, 0.000001D);
        flight.onDisable();
    }

    @Test void originalXSetterModuleDisableRevokesLaterZWrite() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189FlightModule flight = new Minecraft189FlightModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W, true);
        input.key(LegacyKeyboardCodes.A, true);
        flight.onEnable();
        p.onWriteX = flight::onDisable;
        flight.apply(p, yawZero());
        assertEquals(1, p.writeX);
        assertEquals(0, p.writeZ);
        assertEquals(0D, p.z);
        assertFalse(flight.active());
    }

    @Test void originalXSetterHorizontalPolicyChangeRevokesLaterZWrite() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189FlightModule flight = new Minecraft189FlightModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W, true);
        input.key(LegacyKeyboardCodes.A, true);
        flight.onEnable();
        p.onWriteX = () -> flight.smoothHorizontalSetting().set(Boolean.TRUE);
        flight.apply(p, yawZero());
        assertEquals(1, p.writeX);
        assertEquals(0, p.writeZ);
        flight.onDisable();
    }

    @Test void originalVerticalSetterDisableSkipsHorizontalStage() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189FlightModule flight = new Minecraft189FlightModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.SPACE, true);
        input.key(LegacyKeyboardCodes.W, true);
        flight.onEnable();
        p.onWriteY = flight::onDisable;
        flight.apply(p, yawZero());
        assertEquals(1, p.writeY);
        assertEquals(0, p.readX);
        assertEquals(0, p.writeZ);
        assertFalse(flight.active());
    }

    @Test void originalClickGuiPreventsAllAxisGetters() {
        final Minecraft189FlightModule flight =
                new Minecraft189FlightModule(new Minecraft189InputState());
        final Motion p = new Motion();
        flight.onEnable();
        flight.apply(p, yawZero(), null, true);
        assertEquals(0, p.readX + p.readY + p.readZ);
        assertEquals(0, p.writeX + p.writeY + p.writeZ);
        flight.onDisable();
    }
}
