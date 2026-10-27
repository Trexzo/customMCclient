"""V340 source wiring: native sword use must retain original HeldItem owner."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "platform-1.8.9" / "src" / "main" / "java" / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"
HOST = (SOURCE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
MODULE = (SOURCE / "Minecraft189AutoBlockModule.java").read_text(encoding="utf-8")
BRIDGE = (SOURCE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class V340AutoBlockOriginalHeldGeneration(unittest.TestCase):
    def test_native_admission_preserves_getter_order_and_captures_original_serial(self):
        body = HOST.split("long beginAutoBlockNativeUse() {", 1)[1].split(
            "\n    }", 1)[0]
        self.assertNotIn("holdingVerifiedSword()", body)
        self.assertIn("heldItemOwnerSerial", body)
        self.assertIn("featureCatalog.autoBlock().beginNativeUse(", body)
        # Existing exact-source sword check remains in mayStart; reservation
        # must not add a second original native HeldItem getter.
        self.assertIn("boolean mayStartAutoBlock(", HOST)
        self.assertIn("holdingVerifiedSword(),", HOST)

    def test_native_completion_cannot_use_new_sword_of_same_kind(self):
        body = HOST.split("void recordAutoBlockStart(final long useToken) {", 1)[1].split(
            "\n    }", 1)[0]
        self.assertLess(body.index("holdingVerifiedSword()"),
                        body.index("featureCatalog.autoBlock().completeNativeUse("))
        self.assertIn("heldItemOwnerSerial", body)
        self.assertIn("pendingHeldOwnerSerial == currentHeldOwnerSerial", MODULE)
        self.assertIn("hasCurrentHeldOwner", MODULE)
        self.assertIn("pendingEpoch == lifecycleEpoch", MODULE)
        self.assertIn("pendingHoldTicks == holdTicks.get()", MODULE)
        self.assertIn("pendingManualPolicy == pauseManualRight.get()", MODULE)

    def test_original_right_click_owner_and_no_manual_input_release_change(self):
        body = BRIDGE.split("public static synchronized void autoClick(", 1)[1].split(
            "public static synchronized void publishTick(", 1)[0]
        self.assertIn("final long useToken = host.beginAutoBlockNativeUse()", body)
        self.assertIn("minecraft.customMcRightClickMouse();", body)
        self.assertIn("swordBlock.customMcIsUsingItem()", body)
        self.assertIn("host.recordAutoBlockStart(useToken)", body)
        self.assertIn("host.releaseAutoBlockBeforeAction()", body)
        self.assertIn("swordBlock.customMcStopUsingItem()", body)
        self.assertNotIn("leftClickCounter =", MODULE)
        self.assertNotIn("customMcRightClickMouse()", MODULE)

    def test_strict_original_uuid_hurt_and_server_score_never_replaced(self):
        self.assertIn("wait_for(host, host_log, CONTROLLER_ATTACK,", HARNESS)
        self.assertIn("TRIGGER_HURT if args.triggerbot_pvp else AURA_HURT,", HARNESS)
        self.assertIn("wait_for(host, host_log, REJOIN_B_HIT, deadline,", HARNESS)
        self.assertIn('scoreboard players test CITarget taken 1 999999', HARNESS)


if __name__ == "__main__":
    unittest.main()
