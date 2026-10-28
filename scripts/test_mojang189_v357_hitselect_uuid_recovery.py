"""V357 HitSelect original UUID-bound recovery; source and host contracts.

These are source-only checks, never evidence of original Minecraft
controller success or dedicated-server health loss.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
MODULE = (BASE / "Minecraft189HitSelectModule.java").read_text(encoding="utf-8")
FEATURE = (BASE / "Minecraft189HitSelectFeature.java").read_text(encoding="utf-8")
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
CONTROLLER = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")


class OriginalHitSelectRecoveryV357(unittest.TestCase):
    def test_new_optional_settings_keep_historical_defaults(self):
        self.assertIn('MIN_HURT_TICKS_SETTING_ID', MODULE)
        self.assertIn('REQUIRE_RECOVERY_SETTING_ID', MODULE)
        self.assertIn('MIN_HURT_TICKS_SETTING_ID, 0,', MODULE)
        self.assertIn('REQUIRE_RECOVERY_SETTING_ID, Boolean.FALSE', MODULE)
        for prop in ('minTargetHurtTicksSetting', 'requireObservedRecoverySetting'):
            self.assertIn(prop + '()', FEATURE)
        self.assertIn('"Require Observed Hurt Recovery"', FEATURE)
        self.assertIn('"Minimum Target Hurt Ticks"', FEATURE)

    def test_per_uuid_state_and_negative_owner_policy(self):
        for state in ('READY', 'WAIT_FOR_HURT', 'WAIT_FOR_READY'):
            self.assertIn(state, MODULE)
        self.assertIn('LinkedHashMap<UUID, RecoveryState>', MODULE)
        self.assertIn('MAX_TRACKED_OWNERS = 16', MODULE)
        self.assertIn('recoveryByUuid.get(originalUuid)', MODULE)
        self.assertIn('recoveryByUuid.get(selectedOriginalUuid)', MODULE)
        self.assertIn('originalOwnerValid(', MODULE)
        self.assertIn('kinds.player(index) && !kinds.localPlayer(index)', MODULE)
        self.assertIn('combat.known(index) && combat.alive(index)', MODULE)
        self.assertIn('!requireObservedRecovery.get()', MODULE)
        self.assertIn('minTargetHurtTicks.get() <= maxTargetHurtTicks.get()', MODULE)

    def test_shared_final_native_owner_and_scheduler(self):
        self.assertIn('hitSelect.observeOriginalOwner(', HOST)
        self.assertIn('hitSelect.permitsOriginalOwner(', HOST)
        self.assertIn('hitSelect.noteScheduledOriginalClick(verifiedHitSelectUuid)', HOST)
        self.assertIn('currentIds.matches(tickUuidWorld.customMcLoadedEntityUuids())', HOST)
        self.assertIn('featureCatalog.hitSelect().suspendForGui()', HOST)
        self.assertIn('featureCatalog.hitSelect().suspendForSource()', HOST)
        self.assertLess(
            HOST.index('hitSelect.noteScheduledOriginalClick(verifiedHitSelectUuid)'),
            HOST.index('clickRateTracker.recordPress(', HOST.index(
                'hitSelect.noteScheduledOriginalClick(verifiedHitSelectUuid)')))
        self.assertIn('if (admissionSerial != triggerDispatchAdmissionSerial', HOST)
        self.assertIn('minecraft.customMcAttackCrosshairEntity();', CONTROLLER)

    def test_no_synthetic_hit_from_observer_and_no_manual_interception(self):
        method = MODULE.split('synchronized void observeOriginalOwner(', 1)[1].split(
            'synchronized void noteScheduledOriginalClick(', 1)[0]
        for forbidden in ('customMcAttackCrosshairEntity(', 'sendPacket(',
                          'customMcSetRotationYaw(', 'recordPress('):
            self.assertNotIn(forbidden, method)
        self.assertNotIn('REJOIN_STICKY_B_RETAINED_PASS', MODULE)


if __name__ == '__main__':
    unittest.main()
