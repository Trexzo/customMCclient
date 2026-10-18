package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Adversarial lifetime of the *actual original World UUID getter*.
 * Uses a module policy adapter, not the genuine Mojang binary.
 */
final class Minecraft189TriggerBotNativeUuidReadLeaseTest {
    private static Minecraft189TriggerBotModule enabled() {
        final Minecraft189TriggerBotModule m = new Minecraft189TriggerBotModule();
        m.onEnable();
        return m;
    }
    @Test void disabledModuleNeverAuthorizesLoadedWorldUuidRead() {
        final Minecraft189TriggerBotModule m = new Minecraft189TriggerBotModule();
        assertEquals(0, m.beginNativeWorldUuidRead());
        assertFalse(m.completeNativeWorldUuidRead(0));
    }
    @Test void validOriginalUuidReadIsExactlyOneShot() {
        final Minecraft189TriggerBotModule m = enabled();
        final long token = m.beginNativeWorldUuidRead();
        assertNotEquals(0, token);
        assertTrue(m.completeNativeWorldUuidRead(token));
        assertFalse(m.completeNativeWorldUuidRead(token));
        m.onDisable();
    }
    @Test void offOnWhileOriginalGetterExecutesDoesNotAuthorizeNewLife() {
        final Minecraft189TriggerBotModule m = enabled();
        final long old = m.beginNativeWorldUuidRead();
        m.onDisable();m.onEnable();
        assertFalse(m.completeNativeWorldUuidRead(old));
        final long fresh = m.beginNativeWorldUuidRead();
        assertNotEquals(old,fresh);
        assertTrue(m.completeNativeWorldUuidRead(fresh));
        m.onDisable();
    }
    @Test void disableDuringOriginalGetterRevokesOldTargetCredit() {
        final Minecraft189TriggerBotModule m = enabled();
        final long old = m.beginNativeWorldUuidRead();
        m.onDisable();
        assertFalse(m.completeNativeWorldUuidRead(old));
    }
    @Test void schedulerSuspendDuringOriginalGetterRevokesOldWorld() {
        final Minecraft189TriggerBotModule m = enabled();
        final long old = m.beginNativeWorldUuidRead();
        m.suspend();
        assertFalse(m.completeNativeWorldUuidRead(old));
        assertTrue(m.completeNativeWorldUuidRead(m.beginNativeWorldUuidRead()));
        m.onDisable();
    }
    @Test void newerNativeUuidGetterCannotUseOlderOutstandingWorldToken() {
        final Minecraft189TriggerBotModule m = enabled();
        final long old = m.beginNativeWorldUuidRead();
        final long newest = m.beginNativeWorldUuidRead();
        assertNotEquals(old, newest);
        assertFalse(m.completeNativeWorldUuidRead(old));
        assertTrue(m.completeNativeWorldUuidRead(newest));
        m.onDisable();
    }
    @Test void editingConfiguredMinCpsDuringOriginalGetterRevokesRead() {
        final Minecraft189TriggerBotModule m = enabled();
        final long old = m.beginNativeWorldUuidRead();
        m.minCpsSetting().set(4);
        assertFalse(m.completeNativeWorldUuidRead(old));
        assertTrue(m.completeNativeWorldUuidRead(m.beginNativeWorldUuidRead()));
        m.onDisable();
    }
    @Test void editingConfiguredMaxCpsDuringOriginalGetterRevokesRead() {
        final Minecraft189TriggerBotModule m = enabled();
        final long old = m.beginNativeWorldUuidRead();
        m.maxCpsSetting().set(18);
        assertFalse(m.completeNativeWorldUuidRead(old));
        m.onDisable();
    }
    @Test void editingConfirmationDuringOriginalGetterRevokesRead() {
        final Minecraft189TriggerBotModule m = enabled();
        final long old = m.beginNativeWorldUuidRead();
        m.confirmTicksSetting().set(5);
        assertFalse(m.completeNativeWorldUuidRead(old));
        m.onDisable();
    }
    @Test void editingPhysicalAttackRequirementDuringGetterRevokesRead() {
        final Minecraft189TriggerBotModule m = enabled();
        final long old = m.beginNativeWorldUuidRead();
        m.requireHoldSetting().set(true);
        assertFalse(m.completeNativeWorldUuidRead(old));
        m.onDisable();
    }
    @Test void editingManualRightPauseDuringGetterRevokesRead() {
        final Minecraft189TriggerBotModule m = enabled();
        final long old = m.beginNativeWorldUuidRead();
        m.pauseRightSetting().set(false);
        assertFalse(m.completeNativeWorldUuidRead(old));
        m.onDisable();
    }
    @Test void editingNativeSneakPauseDuringGetterRevokesRead() {
        final Minecraft189TriggerBotModule m = enabled();
        final long old = m.beginNativeWorldUuidRead();
        m.pauseSneakSetting().set(false);
        assertFalse(m.completeNativeWorldUuidRead(old));
        m.onDisable();
    }
    @Test void staleUuidReadCannotGiveNewTargetOldConfirmationOrCpsCredit() {
        final Minecraft189TriggerBotModule m = enabled();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        movement.update(true,false,false);
        final UUID a = new UUID(0,100), b = new UUID(0,200);
        m.confirmTicksSetting().set(3);
        m.minCpsSetting().set(20);
        m.maxCpsSetting().set(20);
        assertFalse(m.shouldClick(true,false,false,movement.snapshot(),a));
        assertFalse(m.shouldClick(true,false,false,movement.snapshot(),a));
        final long old = m.beginNativeWorldUuidRead();
        m.onDisable();m.onEnable();
        assertFalse(m.completeNativeWorldUuidRead(old));
        assertFalse(m.shouldClick(true,false,false,movement.snapshot(),b));
        assertFalse(m.shouldClick(true,false,false,movement.snapshot(),b));
        assertTrue(m.shouldClick(true,false,false,movement.snapshot(),b));
        m.onDisable();
    }
    @Test void actualManualRightAndNativeSneakGuardsStillCancelRateCredit() {
        final Minecraft189TriggerBotModule m = enabled();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        movement.update(true,false,true);
        final UUID original = new UUID(0,500);
        m.confirmTicksSetting().set(1);
        m.minCpsSetting().set(20);
        m.maxCpsSetting().set(20);
        assertFalse(m.shouldClick(true,false,true,movement.snapshot(),original));
        movement.update(true,true,true);
        assertFalse(m.shouldClick(true,false,false,movement.snapshot(),original));
        movement.update(true,false,true);
        assertTrue(m.shouldClick(true,false,false,movement.snapshot(),original));
        m.onDisable();
    }
}
