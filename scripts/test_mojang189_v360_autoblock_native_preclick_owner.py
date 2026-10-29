"""V360 AutoBlock original 1.8.9 pre-native-rightclick owner source contract.

Source-only contracts; these never replace genuine Mojang PlayerController,
real isUsingItem state, or vanilla dedicated-server authoritative damage.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
MODULE = (BASE / "Minecraft189AutoBlockModule.java").read_text(encoding="utf-8")
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class AutoBlockOriginalPreClickOwnerV360(unittest.TestCase):
    def test_exact_native_pending_token_and_original_item_generation(self):
        method = MODULE.split(
            "synchronized boolean permitsNativeUseDispatch(", 1
        )[1].split("\n    /**", 1)[0]
        for marker in (
            "token != 0L && token == pendingUseToken",
            "pendingEpoch == lifecycleEpoch && enabled",
            "!ownsBlock && swordStillHeld && !guiOpen",
            "!manualRightHeld",
            "pendingHoldTicks == holdTicks.get()",
            "pendingManualPolicy == pauseManualRight.get()",
            "pendingHeldOwnerRequired",
            "pendingHeldOwnerSerial == currentHeldOwnerSerial",
        ):
            self.assertIn(marker, method)
        self.assertNotIn("pendingUseToken = 0", method)
        self.assertNotIn("ownsBlock = true", method)

    def test_current_native_sword_read_bound_to_same_owner_generation(self):
        method = HOST.split(
            "boolean mayDispatchAutoBlockNativeUse(final long useToken)", 1
        )[1].split("\n    /**", 1)[0]
        self.assertIn("final long ownerBeforeRead = heldItemOwnerSerial;", method)
        self.assertIn("final boolean nativeSwordStillHeld = holdingVerifiedSword();",
                      method)
        self.assertIn("ownerBeforeRead == heldItemOwnerSerial", method)
        self.assertIn("featureCatalog.autoBlock().permitsNativeUseDispatch(", method)
        self.assertIn("clickGuiRuntime.coreRuntime().model().snapshot().open()",
                      method)
        self.assertIn("Minecraft189ClickRateTracker.RIGHT_BUTTON", method)
        self.assertNotIn("customMcRightClickMouse()", method)
        self.assertIn("heldItemHostLease.stillOwned()", HOST)

    def test_native_original_screen_rechecked_after_new_source_getter(self):
        segment = BRIDGE.split("host.beginAutoBlockNativeUse()", 1)[1].split(
            "host.cancelAutoBlockNativeUse(useToken)", 1
        )[0]
        self.assertEqual(1, segment.count("minecraft.customMcRightClickMouse();"))
        self.assertIn("host.mayDispatchAutoBlockNativeUse(useToken)", segment)
        before = segment.index("host.mayDispatchAutoBlockNativeUse(useToken)")
        after = segment.index("minecraft.customMcRightClickMouse();")
        self.assertLess(before, after)
        self.assertIn("vetoOriginalScreenBeforeNativeCombat(", segment[before:after])
        self.assertIn("if (swordBlock.customMcIsUsingItem())", segment[after:])
        self.assertIn("host.recordAutoBlockStart(useToken)", segment[after:])
        self.assertNotIn("customMcStopUsingItem", segment)

    def test_old_original_player_and_manual_right_protection_unchanged(self):
        self.assertIn("if (manualRightHeld)", MODULE)
        self.assertIn("return !manualRightHeld;", MODULE)
        self.assertIn("completeNativeUse(", MODULE)
        self.assertIn("recordAutoBlockStart(final long useToken)", HOST)
        self.assertIn("host.claimSyntheticCombatReblock(combatCycleToken)",
                      BRIDGE)
        self.assertIn("Minecraft189VanillaMeleeAcceptance.syntheticAuraFinished();",
                      BRIDGE)
        self.assertIn("minecraft.customMcAttackCrosshairEntity();", BRIDGE)
        self.assertIn("wait_for_real_score(", HARNESS)
        self.assertIn("REJOIN_B_RETAINED, deadline,", HARNESS)


if __name__ == "__main__":
    unittest.main()
