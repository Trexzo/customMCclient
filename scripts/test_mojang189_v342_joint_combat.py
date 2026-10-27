"""V342 source-precise joint module integration and priority checks.

These assertions detect accidental dropping of WTap/HitSelect/Velocity/
AutoBlock when the large native bridge and host are composed. They do not
replace genuine Mojang 1.8.9 + server-owned damage acceptance.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SRC = (ROOT / "platform-1.8.9" / "src" / "main" / "java"
       / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9")
read = lambda name: (SRC / name).read_text(encoding="utf-8")
BRIDGE = read("Minecraft189RuntimeBridge.java")
HOST = read("Minecraft189HostRuntime.java")
TRANSFORMER = read("Minecraft189ClassTransformer.java")
WTAP = read("Minecraft189WTapModule.java")
HIT = read("Minecraft189HitSelectModule.java")
VELOCITY = read("Minecraft189VelocityModule.java")
BLOCK = read("Minecraft189AutoBlockModule.java")
ARB = read("Minecraft189CombatCycleArbiter.java")
HARNESS = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py"
           ).read_text(encoding="utf-8")


class JointCombatV342(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.action = BRIDGE.split(
            "public static synchronized void autoClick(", 1)[1].split(
            "public static synchronized void publishTick(", 1)[0]

    def test_priority_is_single_owner_and_native_leases_unmodified(self):
        body = self.action
        keys = [
            "host.enterSyntheticCombatCycle()",
            "Minecraft189CombatCycleArbiter.Primary.POTION",
            "Minecraft189CombatCycleArbiter.Primary.ROD",
            "Minecraft189CombatCycleArbiter.Primary.MELEE",
            "host.claimSyntheticCombatReblock(combatCycleToken)",
            "host.leaveSyntheticCombatCycle(combatCycleToken)",
        ]
        pos = [body.index(s) for s in keys]
        self.assertEqual(sorted(pos), pos)
        self.assertIn("Minecraft189AutoPotSourceCast.execute(", body)
        self.assertIn("host.mayExecuteAutoRodUse(", body)
        self.assertIn("host.mayDispatchSyntheticMelee(originalSlot)", body)
        self.assertIn("host.restoreCombatSlotAfterSyntheticClick(originalSlot)", body)
        self.assertIn("host.restoreAutoRodSlot(originalRodSlot)", body)
        self.assertIn("host.restoreAutoPotSlot(originalPotionSlot)", body)
        self.assertIn("phase != Phase.MELEE_ATTEMPT", ARB)
        self.assertNotIn("ATTACK_SUCCESS", ARB)

    def test_hitselect_original_uuid_is_checked_at_all_three_native_attacks(self):
        self.assertIn("hitSelectInitialUuid", self.action)
        self.assertEqual(3, self.action.count(
            "if (!permitsHitSelectAtNativeDispatch("))
        self.assertIn("selectedOriginalUuid.equals(", HIT)
        self.assertIn("currentKinds.localPlayer(verifiedCrosshairIndex)", HIT)
        self.assertIn("originalRay.customMcCrosshairPlayerIndex() != originalIndex", BRIDGE)
        self.assertEqual(2, self.action.count(
            "minecraft.customMcAttackCrosshairEntity();"))
        self.assertEqual(1, self.action.count("minecraft.customMcClickMouse();"))

    def test_auto_block_original_held_generation_survives_cycle_ownership(self):
        self.assertIn("host.claimSyntheticCombatReblock(combatCycleToken)",
                      self.action)
        self.assertIn("host.beginAutoBlockNativeUse()", self.action)
        self.assertIn("minecraft.customMcRightClickMouse();", self.action)
        self.assertIn("host.recordAutoBlockStart(useToken)", self.action)
        self.assertIn("pendingHeldOwnerSerial == currentHeldOwnerSerial", BLOCK)
        self.assertIn("heldItemOwnerSerial", HOST)
        self.assertIn("featureCatalog.autoBlock().beginNativeUse(", HOST)

    def test_wtap_and_keep_sprint_policy_remain_separate_from_action_lane(self):
        self.assertIn("resetTicksRemaining > 0", WTAP)
        self.assertIn("requireGround.get().booleanValue()", WTAP)
        self.assertIn("requireForward.get().booleanValue()", WTAP)
        self.assertIn("++nativeSprintWriteEpoch", WTAP)
        self.assertIn("featureCatalog.wTap().active()", HOST)
        self.assertIn("shouldKeepSprintAfterSyntheticClick", self.action)
        self.assertIn("host.restoreSprintAfterSyntheticClick()", self.action)
        self.assertNotIn("setSprinting(", ARB)

    def test_velocity_scales_local_entity_only_without_touching_action_owner(self):
        self.assertIn("Minecraft189Mappings.ENTITY_PLAYER_SP", BRIDGE)
        self.assertIn("velocityOriginalLocalOwner(", BRIDGE)
        self.assertIn("adjustVelocityHorizontalOriginalOwner", TRANSFORMER)
        self.assertIn("adjustVelocityVerticalOriginalOwner", TRANSFORMER)
        self.assertIn("(Ljava/lang/Object;DD)D", TRANSFORMER)
        self.assertIn("adjustHorizontal(", VELOCITY)
        self.assertNotIn("setMotion", ARB)

    def test_original_authoritative_damage_gate_is_still_strict(self):
        for marker in (
            "wait_for(host, host_log, CONTROLLER_ATTACK,",
            "TRIGGER_HURT if args.triggerbot_pvp else AURA_HURT,",
            "wait_for(host, host_log, REJOIN_B_HIT, deadline,",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(marker, HARNESS)
        self.assertNotIn("COMBAT_CYCLE_PASS", HARNESS)


if __name__ == "__main__":
    unittest.main()
