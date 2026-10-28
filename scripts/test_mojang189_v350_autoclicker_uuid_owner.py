"""V350 source contract: UUID-bound trigger Auto Clicker joins existing combat arbiter.

This is source wiring evidence only, NOT real Minecraft PvP/server acceptance.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SRC = (ROOT / "platform-1.8.9" / "src" / "main" / "java"
       / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9")
MODULE = (SRC / "Minecraft189AutoClickerModule.java").read_text(encoding="utf-8")
HOST = (SRC / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
BRIDGE = (SRC / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
ARB = (SRC / "Minecraft189CombatCycleArbiter.java").read_text(encoding="utf-8")


class V350NativeTargetOwner(unittest.TestCase):
    def test_uuid_target_switch_revokes_confirmation_and_all_cadence(self):
        self.assertIn("final UUID verifiedCrosshairUuid", MODULE)
        self.assertIn("if (!verifiedCrosshairUuid.equals(confirmedTargetUuid))", MODULE)
        self.assertIn("confirmedTargetUuid = verifiedCrosshairUuid", MODULE)
        self.assertIn("triggerConfirmedFrames = 0;\n                resetSchedule();", MODULE)
        self.assertIn("if (!crosshairPlayerHit || verifiedCrosshairUuid == null)", MODULE)
        self.assertIn("confirmedTargetUuid = null", MODULE)
        self.assertIn("++nativeOwnerEpoch", MODULE)

    def test_original_world_uuid_read_is_bound_to_native_index(self):
        self.assertIn("return shouldAutoClickInternal(crosshairPlayerHit, crosshairPlayerIndex,", HOST)
        self.assertIn("nativeHitbox, true", HOST)
        self.assertIn("final boolean uuidBoundTrigger = requireOriginalTargetUuid", HOST)
        self.assertIn("tickUuidWorld.customMcLoadedEntityUuids()", HOST)
        self.assertIn("ids.matches(tickUuidWorld.customMcLoadedEntityUuids())", HOST)
        self.assertIn("!kinds.localPlayer(crosshairPlayerIndex)", HOST)
        self.assertIn("ownerEpoch != clicker.nativeOwnerEpoch()", HOST)
        self.assertIn("verifiedAutoClickerUuid = ids.at(crosshairPlayerIndex)", HOST)
        self.assertIn("|| admissionSerial != triggerDispatchAdmissionSerial", HOST)
        self.assertIn("verifiedAutoClickerUuid.equals(ids.at(crosshairPlayerIndex))", HOST)

    def test_shared_combat_lanes_and_native_controller_authority_remain(self):
        action = BRIDGE.split(
            "public static synchronized void autoClick(", 1)[1].split(
            "public static synchronized void publishTick(", 1)[0]
        sequence = [
            "host.enterSyntheticCombatCycle()",
            "Minecraft189CombatCycleArbiter.Primary.POTION",
            "Minecraft189CombatCycleArbiter.Primary.ROD",
            "host.shouldAutoClick(playerHit, playerIndex, nativeHitbox)",
            "Minecraft189CombatCycleArbiter.Primary.MELEE",
            "host.claimSyntheticCombatReblock(combatCycleToken)",
            "host.leaveSyntheticCombatCycle(combatCycleToken)",
        ]
        indices = [action.index(s) for s in sequence]
        self.assertEqual(indices, sorted(indices))
        self.assertIn("host.restoreAutoPotSlot(originalPotionSlot)", action)
        self.assertIn("host.restoreAutoRodSlot(originalRodSlot)", action)
        self.assertIn("host.restoreCombatSlotAfterSyntheticClick(originalSlot)", action)
        self.assertIn("minecraft.customMcClickMouse();", action)
        self.assertIn("minecraft.customMcAttackCrosshairEntity();", action)
        self.assertIn("phase != Phase.MELEE_ATTEMPT", ARB)
        self.assertNotIn("ATTACK_SUCCESS", ARB)


if __name__ == "__main__":
    unittest.main()
