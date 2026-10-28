"""V351: original controller target/epoch fence for Auto Clicker trigger mode.

This verifies source wiring; it does not substitute for original Mojang
1.8.9 binary acceptance, actual PlayerControllerMP hurt or server damage.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = (ROOT / "platform-1.8.9" / "src" / "main" / "java" /
        "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9")
read = lambda f: (BASE / f).read_text(encoding="utf-8")
BRIDGE = read("Minecraft189RuntimeBridge.java")
HOST = read("Minecraft189HostRuntime.java")
LEASE = read("Minecraft189AutoClickerControllerLease.java")
ARB = read("Minecraft189CombatCycleArbiter.java")


class AutoClickerControllerIntentV351(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.action = BRIDGE.split(
            "public static synchronized void autoClick(", 1)[1].split(
            "public static synchronized void publishTick(", 1)[0]
        cls.gate = BRIDGE.split(
            "public static synchronized boolean acceptanceControllerDispatchAllowed(", 1
        )[1].split(
            "public static synchronized void acceptanceControllerAttackEntity(", 1
        )[0]

    def test_source_scheduling_arms_one_target_and_clears_on_gui_shutdown(self):
        self.assertIn("autoClickerControllerLease.clear();", HOST)
        self.assertIn("autoClickerControllerLease.arm(", HOST)
        self.assertIn("verifiedAutoClickerUuid, verifiedAutoClickerEpoch", HOST)
        self.assertIn("hasPendingAutoClickerControllerIntent()", HOST)
        self.assertIn("consumeAutoClickerControllerIntent(", HOST)
        self.assertIn("clicker.nativeOwnerEpoch()", HOST)
        self.assertIn("clickGuiRuntime.coreRuntime().model().snapshot().open()", HOST)
        self.assertIn("autoClickerControllerLease.consume(", HOST)

    def test_native_vanilla_click_owns_one_controller_target_intent(self):
        native = self.action.split(
            "} else {\n                            // Manual mouse and Auto Clicker", 1
        )[1].split(
            "} finally {\n                        Minecraft189VanillaMeleeAcceptance.", 1
        )[0]
        self.assertIn("if (host.hasPendingAutoClickerControllerIntent())", native)
        self.assertIn("host.consumeAutoClickerControllerIntent(", native)
        self.assertIn("autoClickerControllerIntent.set(controllerIntent)", native)
        self.assertIn("autoClickerControllerIntent.remove();", native)
        self.assertEqual(1, native.count("minecraft.customMcClickMouse();"))
        self.assertLess(native.index("host.consumeAutoClickerControllerIntent("),
                        native.index("autoClickerControllerIntent.set("))
        self.assertLess(native.index("autoClickerControllerIntent.set("),
                        native.index("minecraft.customMcClickMouse();"))
        self.assertIn("if (controllerIntent == null) return;", native)
        self.assertIn("triggerModeSetting().get()", native)

    def test_real_controller_argument_matches_owner_and_original_server_gates(self):
        gate = self.gate
        self.assertIn("final Minecraft189AutoClickerControllerLease.Intent autoClickerIntent", gate)
        self.assertIn("entity instanceof Minecraft189NativeEntityUuidAccess", gate)
        self.assertIn(".customMcNativeEntityUuid()", gate)
        self.assertIn("autoClickerIntent.permits(actual,", gate)
        self.assertIn("nativeOwnerEpoch()", gate)
        self.assertIn("triggerModeSetting().get().booleanValue()", gate)
        self.assertIn("featureCatalog().killAura().active()", gate)
        self.assertIn("featureCatalog().triggerBot().active()", gate)
        self.assertIn("snapshot().open()", gate)
        self.assertIn("final UUID expected = auraDispatchUuid.get()", gate)
        self.assertIn("final UUID expectedTrigger = triggerDispatchUuid.get()", gate)
        self.assertIn("return Minecraft189RejoinTargetAcceptance.allowStaleDispatchAfterRealExit(", gate)
        self.assertIn("final UUID expectedTrigger", gate)

    def test_original_action_arbiter_and_vanilla_authority_unchanged(self):
        b = self.action
        seq = [
            "host.enterSyntheticCombatCycle()",
            "Minecraft189CombatCycleArbiter.Primary.POTION",
            "Minecraft189CombatCycleArbiter.Primary.ROD",
            "host.shouldAutoClick(playerHit, playerIndex, nativeHitbox)",
            "Minecraft189CombatCycleArbiter.Primary.MELEE",
            "host.claimSyntheticCombatReblock(combatCycleToken)",
            "host.leaveSyntheticCombatCycle(combatCycleToken)",
        ]
        indices = [b.index(x) for x in seq]
        self.assertEqual(indices, sorted(indices))
        self.assertIn("host.restoreAutoPotSlot(originalPotionSlot)", b)
        self.assertIn("host.restoreAutoRodSlot(originalRodSlot)", b)
        self.assertIn("host.restoreCombatSlotAfterSyntheticClick(originalSlot)", b)
        self.assertIn("minecraft.customMcClickMouse();", b)
        self.assertIn("minecraft.customMcAttackCrosshairEntity();", b)
        self.assertIn("phase != Phase.MELEE_ATTEMPT", ARB)
        self.assertNotIn("ATTACK_SUCCESS", ARB)

    def test_lease_never_claims_server_hit_or_changes_packets(self):
        self.assertIn("originalTarget.equals(actualControllerUuid)", LEASE)
        self.assertIn("ownerEpoch == liveEpoch", LEASE)
        self.assertIn("pending = null;", LEASE)
        self.assertNotIn("attackEntity(", LEASE)
        self.assertNotIn("sendPacket", LEASE)
        self.assertNotIn("leftClickCounter", LEASE)


if __name__ == "__main__":
    unittest.main()
