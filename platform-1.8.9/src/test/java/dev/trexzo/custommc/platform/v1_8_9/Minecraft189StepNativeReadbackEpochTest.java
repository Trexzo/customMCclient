package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original player Step interface callback reentrancy; offline Java only. */
final class Minecraft189StepNativeReadbackEpochTest {
    private static final class StepPlayer implements Minecraft189PlayerStepControl {
        float height = Minecraft189StepModule.VANILLA_STEP_HEIGHT;
        int reads, writes;
        Runnable onRead;
        @Override public float customMcStepHeight() {
            reads++;
            final float old = height;
            if (onRead != null) {
                final Runnable action = onRead;
                onRead = null;
                action.run();
            }
            return old;
        }
        @Override public void customMcSetStepHeight(float next) {
            writes++;
            height = next;
        }
    }

    private static Minecraft189PlayerMovementState.Snapshot state(
            boolean ground, boolean sneak) {
        final Minecraft189PlayerMovementState m = new Minecraft189PlayerMovementState();
        m.update(ground, sneak, false);
        return m.snapshot();
    }

    @Test void nativeGetterOffOnCannotApplyOldOwnerStepHeight() {
        final Minecraft189StepModule m = new Minecraft189StepModule();
        final StepPlayer p = new StepPlayer();
        m.onEnable();
        m.heightPercentSetting().set(180);
        p.onRead = () -> { m.onDisable(); m.onEnable(); };
        m.apply(p);
        assertEquals(0, p.writes);
        assertEquals(0.6F, p.height, 0.000001F);
        m.apply(p);
        assertEquals(1.8F, p.height, 0.000001F);
        m.onDisable();
    }

    @Test void nativeGetterDisableReleasesOldBoostToVanillaImmediately() {
        final Minecraft189StepModule m = new Minecraft189StepModule();
        final StepPlayer p = new StepPlayer();
        m.onEnable();
        m.heightPercentSetting().set(180);
        m.apply(p);
        assertEquals(1.8F, p.height, 0.000001F);
        final int writes = p.writes;
        p.onRead = m::onDisable;
        m.apply(p);
        assertEquals(writes+1, p.writes);
        assertEquals(0.6F, p.height, 0.000001F);
        assertFalse(m.active());
    }

    @Test void nativeGetterEditedHeightRejectsStaleBoost() {
        final Minecraft189StepModule m = new Minecraft189StepModule();
        final StepPlayer p = new StepPlayer();
        m.onEnable();
        p.onRead = () -> m.heightPercentSetting().set(150);
        m.apply(p);
        assertEquals(0, p.writes);
        m.apply(p);
        assertEquals(1.5F, p.height, 0.000001F);
        m.onDisable();
    }

    @Test void getterNewGroundGateRevokesOldUngatedBoost() {
        final Minecraft189StepModule m = new Minecraft189StepModule();
        final StepPlayer p = new StepPlayer();
        m.onEnable();
        m.heightPercentSetting().set(180);
        p.onRead = () -> m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(p);
        assertEquals(0, p.writes);
        m.apply(p);
        assertEquals(0, p.writes); // No authority to boost without state.
        m.groundOnlySetting().set(Boolean.FALSE);
        m.apply(p);
        assertEquals(1.8F, p.height, 0.000001F);
        m.onDisable();
    }

    @Test void getterSneakPolicyEditRevokesOldBoostAndFreshVetoRestores() {
        final Minecraft189StepModule m = new Minecraft189StepModule();
        final StepPlayer p = new StepPlayer();
        m.onEnable();
        m.heightPercentSetting().set(180);
        p.onRead = () -> m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        m.apply(p, state(true,true));
        assertEquals(0, p.writes);
        m.apply(p, state(true,true));
        assertEquals(0.6F, p.height, 0.000001F);
        assertEquals(0, p.writes);
        m.apply(p, state(true,false));
        assertEquals(1.8F, p.height, 0.000001F);
        m.onDisable();
    }

    @Test void getterAirbornePercentEditRejectsOldAirborneStep() {
        final Minecraft189StepModule m = new Minecraft189StepModule();
        final StepPlayer p = new StepPlayer();
        m.onEnable();
        m.airborneOverrideSetting().set(Boolean.TRUE);
        m.airborneHeightPercentSetting().set(90);
        p.onRead = () -> m.airborneHeightPercentSetting().set(120);
        m.apply(p, state(false,false));
        assertEquals(0, p.writes);
        m.apply(p, state(false,false));
        assertEquals(1.2F, p.height, 0.000001F);
        m.onDisable();
    }

    @Test void getterDisablesWhileNativeNaNResetsVanillaInsteadOfOldBoost() {
        final Minecraft189StepModule m = new Minecraft189StepModule();
        final StepPlayer p = new StepPlayer();
        p.height = Float.NaN;
        m.onEnable();
        p.onRead = m::onDisable;
        m.apply(p);
        assertEquals(0.6F, p.height, 0.000001F);
        assertEquals(1, p.writes);
    }

    @Test void originalGuiReleasesExistingBoostAndRetainsDisabledSourceRules() {
        final Minecraft189StepModule m = new Minecraft189StepModule();
        final StepPlayer p = new StepPlayer();
        m.onEnable();
        m.heightPercentSetting().set(200);
        m.apply(p);
        assertEquals(2.0F, p.height, 0.000001F);
        m.apply(p, null, true);
        assertEquals(0.6F, p.height, 0.000001F);
        m.apply(p, null, true);
        assertEquals(2, p.writes);
        m.onDisable();
        m.apply(p);
        assertEquals(0.6F, p.height, 0.000001F);
    }

    @Test void validNativeReadWithoutReentrancyStillAppliesOriginalStep() {
        final Minecraft189StepModule m = new Minecraft189StepModule();
        final StepPlayer p = new StepPlayer();
        m.onEnable();
        m.heightPercentSetting().set(175);
        m.apply(p);
        assertEquals(1.75F, p.height, 0.000001F);
        m.apply(p);
        assertEquals(1, p.writes);
        m.onDisable();
        m.apply(p);
        assertEquals(0.6F, p.height, 0.000001F);
    }

    @Test void unmappedPlayerAndUnavailableMovementDoNotAllowGatedBoost() {
        final Minecraft189StepModule m = new Minecraft189StepModule();
        final StepPlayer p = new StepPlayer();
        m.onEnable();
        m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(null);
        m.apply(p);
        assertEquals(0, p.writes);
        m.apply(p,state(false,false));
        assertEquals(0, p.writes);
        m.apply(p,state(true,false));
        assertEquals(1.0F, p.height, 0.000001F);
        m.onDisable();
    }
}
