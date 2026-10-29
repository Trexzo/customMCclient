"""V363 Criticals real-player final native controller gate contracts.

Source-only; passing this is NOT original Mojang 1.8.9 client-controller or
authoritative dedicated-server damage evidence.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
CRITICALS = (BASE / "Minecraft189CriticalsModule.java").read_text(encoding="utf-8")
HITSELECT = (BASE / "Minecraft189HitSelectModule.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class OriginalCriticalsNativeDispatchV363(unittest.TestCase):
    def test_original_module_epoch_and_only_native_read_predicate(self):
        self.assertIn("synchronized long lifecycleEpoch()", CRITICALS)
        self.assertIn("++lifecycleEpoch;", CRITICALS)
        pred = CRITICALS.split(
            "synchronized boolean permitsOriginalNativeFall(", 1
        )[1].split("\n    synchronized boolean permits(", 1)[0]
        for evidence in (
            "!enabled) return false",
            "!competingMovementOwner && !originalOnGround",
            "Float.isFinite(originalFall)",
            "Double.isFinite(originalMotionY)",
            "originalFall >= minFall.get()",
            "originalMotionY < 0.0D",
        ):
            self.assertIn(evidence, pred)
        self.assertNotIn("setMotion", pred)
        self.assertNotIn("sendPacket", pred)

    def test_original_player_generation_and_all_mapped_reads_revalidated(self):
        host = HOST.split(
            "    boolean mayDispatchOriginalCriticals() {", 1
        )[1].split("\n    /**", 1)[0]
        for evidence in (
            "criticalsNativeAdmissionSerial != triggerDispatchAdmissionSerial",
            "module.active() != criticalsNativeActive",
            "module.lifecycleEpoch() != criticalsNativeLifecycleEpoch",
            "criticalsNativeSourceGeneration",
            "criticalNativeSourceStillOwned(original, generation)",
            "Minecraft189PlayerMovementStateAccess",
            "Minecraft189PlayerFallDistanceControl",
            "Minecraft189PlayerMotionControl",
            "movement.customMcOnGround()",
            "fall.customMcFallDistance()",
            "motion.customMcMotionY()",
            "ground != groundAgain",
            "distance != distanceAgain",
            "motionY != motionAgain",
            "module.permitsOriginalNativeFall(",
            "featureCatalog.noFall().active()",
            "featureCatalog.flight().active()",
            "featureCatalog.noGravity().active()",
            "featureCatalog.noClip().active()",
            "featureCatalog.freeze().active()",
        ):
            self.assertIn(evidence, host)
        self.assertEqual(2, host.count("movement.customMcOnGround()"))
        self.assertEqual(2, host.count("fall.customMcFallDistance()"))
        self.assertEqual(2, host.count("motion.customMcMotionY()"))
        self.assertNotIn("customMcSetMotionY", host)
        self.assertNotIn("customMcSetFallDistance", host)
        self.assertNotIn("customMcAttackCrosshairEntity", host)

    def test_module_disabled_default_remains_and_no_stale_on_toggle(self):
        q = HOST.split("    boolean mayDispatchOriginalCriticals() {", 1)[1]
        self.assertIn("if (!module.active()) return true;", q)
        self.assertLess(q.index("module.active() != criticalsNativeActive"),
                        q.index("if (!module.active()) return true;"))
        self.assertIn("criticalsNativeAdmissionSerial = -1L", HOST)
        self.assertIn("criticalsNativePlayer = tickHotbarPlayer;", HOST)
        self.assertIn("criticalsSourceGeneration", HOST)
        self.assertIn("tickHotbarPlayer == original", HOST)
        self.assertIn("tickHeldItemAccess == original", HOST)

    def test_all_three_native_controller_lanes_check_criticals_then_hitselect(self):
        attack = BRIDGE.split(
            "    public static synchronized void autoClick(", 1
        )[1].split("\n    public static", 1)[0]
        self.assertEqual(3, attack.count(
            "if (!host.mayDispatchOriginalCriticals()) return;"))
        self.assertEqual(3, attack.count(
            "if (!permitsHitSelectAtNativeDispatch("))
        for controller in (
            "triggerDispatchUuid.set(sourceUuid);",
            "auraDispatchUuid.set(ownerUuid);",
            "minecraft.customMcClickMouse();",
        ):
            native = attack.index(controller)
            prior = attack.rfind(
                "if (!host.mayDispatchOriginalCriticals()) return;", 0, native)
            hitselect = attack.rfind(
                "if (!permitsHitSelectAtNativeDispatch(", 0, native)
            screen = attack.rfind(
                "vetoOriginalScreenBeforeNativeCombat(", 0, native)
            self.assertGreater(prior, 0)
            self.assertLess(prior, hitselect)
            self.assertLess(hitselect, screen)
            self.assertLess(screen, native)

    def test_hitselect_uuid_recovery_and_dedicated_server_unmodified(self):
        self.assertIn("RecoveryState.WAIT_FOR_HURT", HITSELECT)
        self.assertIn("RecoveryState.WAIT_FOR_READY", HITSELECT)
        self.assertIn("selectedOriginalUuid.equals(", HITSELECT)
        self.assertIn("host.worldEntityUuidState().snapshot()", BRIDGE)
        self.assertIn("host.featureCatalog().hitSelect()", BRIDGE)
        self.assertIn("minecraft.customMcAttackCrosshairEntity();", BRIDGE)
        self.assertIn("wait_for_real_score(", HARNESS)
        self.assertIn("REJOIN_B_RETAINED, deadline,", HARNESS)


if __name__ == "__main__":
    unittest.main()
