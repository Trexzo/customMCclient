"""V361 AutoRod original-source final handoff; no fake gameplay acceptance."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
ROD = (BASE / "Minecraft189AutoRodModule.java").read_text(encoding="utf-8")
VERIFY = (BASE / "Minecraft189VerifiedCombatItemSlot.java").read_text(
    encoding="utf-8")
CYCLE = (BASE / "Minecraft189CombatCycleArbiter.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class OriginalRodNativeOwnerV361(unittest.TestCase):
    def test_exact_source_generation_and_native_hotbar_lease(self):
        method = HOST.split(
            "    boolean mayExecuteAutoRodUse(final int originalSlot) {", 1
        )[1].split("\n    /**", 1)[0]
        for requirement in (
            "final Minecraft189InventoryHotbarControl inventory =",
            "final Minecraft189PlayerInventoryAccess originalPlayer =",
            "final Minecraft189PlayerHeldItemAccess originalHeld =",
            "final long originalHeldGeneration = heldItemOwnerSerial",
            "originalPlayer != (Object) originalHeld",
            "autoRodLeaseOriginal != originalSlot",
            "tickHotbarControl != inventory",
            "tickHotbarPlayer != originalPlayer",
            "tickHeldItemAccess != originalHeld",
            "heldItemOwnerSerial != originalHeldGeneration",
            "heldItemOwnerSerial == originalHeldGeneration",
            "Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(",
            "clickGuiRuntime.coreRuntime().model().snapshot().open()",
            "mayCastWithoutReelingActiveHook()",
        ):
            self.assertIn(requirement, method)
        # V132 original Mojang /give acceptance is a genuinely read-only
        # source rod discovery, with no attack-key input or fake controller.
        self.assertNotIn("Minecraft189ClickRateTracker.LEFT_BUTTON", method)
        self.assertNotIn("customMcRightClickMouse()", method)

    def test_actual_native_use_requires_current_physical_input_and_source(self):
        method = HOST.split(
            "boolean mayDispatchAutoRodNativeUse(final int originalSlot) {", 1
        )[1].split("\n    void restoreAutoRodSlot(", 1)[0]
        self.assertIn("mayExecuteAutoRodUse(originalSlot)", method)
        self.assertIn("rod.requireHoldSetting().get()", method)
        self.assertIn("Minecraft189ClickRateTracker.LEFT_BUTTON", method)
        self.assertIn("rod.pauseRightSetting().get()", method)
        self.assertIn("Minecraft189ClickRateTracker.RIGHT_BUTTON", method)
        self.assertIn("clickGuiRuntime.coreRuntime().model().snapshot().open()", method)
        self.assertNotIn("customMcRightClickMouse()", method)
        self.assertNotIn("customMcSetSelectedHotbarSlot(", method)

    def test_same_verified_original_item_and_policy(self):
        self.assertIn("module.ownsVerifiedItem(inventory, original, item)", VERIFY)
        self.assertIn("stableRodSourceItem(inventory, player, item)", VERIFY)
        self.assertIn("stableProtectedRodSourceItem(inventory, player, item,", VERIFY)
        self.assertIn("capturedLifecycleEpoch == lifecycleEpoch", ROD)
        self.assertIn("verifiedSelectedItem == item", ROD)
        self.assertIn("cooldownCommittedForLease = true", ROD)

    def test_original_screens_after_release_and_before_real_right_click(self):
        section = BRIDGE.split("if (host.shouldAutoRod(playerHit, playerIndex))", 1)[1]
        section = section.split("final double[] nativeHitbox", 1)[0]
        self.assertIn("host.claimSyntheticCombatPrimary(", section)
        self.assertIn("Minecraft189CombatCycleArbiter.Primary.ROD", section)
        self.assertIn("host.releaseAutoBlockBeforeAction()", section)
        self.assertEqual(1, section.count("host.mayExecuteAutoRodUse(originalRodSlot)"))
        self.assertEqual(1, section.count(
            "host.mayDispatchAutoRodNativeUse(originalRodSlot)"))
        self.assertEqual(2, section.count("vetoOriginalScreenBeforeNativeCombat("))
        self.assertEqual(1, section.count("minecraft.customMcRightClickMouse();"))
        a = section.index("host.releaseAutoBlockBeforeAction()")
        b = section.index("host.mayExecuteAutoRodUse(originalRodSlot)")
        c = section.index("vetoOriginalScreenBeforeNativeCombat(", b)
        d = section.index("host.mayDispatchAutoRodNativeUse(originalRodSlot)", c)
        e = section.index("vetoOriginalScreenBeforeNativeCombat(", d)
        f = section.index("minecraft.customMcRightClickMouse();", e)
        g = section.index("host.commitAutoRodUseAttempt(originalRodSlot);", f)
        for left, right in zip((a,b,c,d,e,f),(b,c,d,e,f,g)):
            self.assertLess(left, right)
        self.assertIn("host.restoreAutoRodSlot(originalRodSlot)", section)

    def test_priority_manual_input_and_server_authority_unchanged(self):
        self.assertIn("phase != Phase.EVALUATING", CYCLE)
        self.assertIn("POTION, ROD, MELEE", CYCLE)
        self.assertIn("REBLOCK_ATTEMPT", CYCLE)
        self.assertIn("Minecraft189CombatCycleArbiter.Primary.POTION", BRIDGE)
        self.assertIn("Minecraft189CombatCycleArbiter.Primary.MELEE", BRIDGE)
        self.assertIn("host.shouldKeepSprintAfterSyntheticClick(playerHit)", BRIDGE)
        self.assertIn("minecraft.customMcAttackCrosshairEntity();", BRIDGE)
        self.assertIn("wait_for_real_score(", HARNESS)
        self.assertIn("REJOIN_B_RETAINED, deadline,", HARNESS)


if __name__ == "__main__":
    unittest.main()
