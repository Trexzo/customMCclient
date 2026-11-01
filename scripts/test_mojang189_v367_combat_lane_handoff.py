"""V367 source contract: scheduled combat owner cannot cross an original controller.

This static regression checks integration wiring only. Genuine Mojang client
and dedicated-server acceptance are certified separately by binary CI.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
HOST = (SOURCE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
BRIDGE = (SOURCE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
AURA = (SOURCE / "Minecraft189KillAuraModule.java").read_text(encoding="utf-8")
LANE = (SOURCE / "Minecraft189CombatLaneAdmission.java").read_text(encoding="utf-8")


class CombatLaneOriginalBridgeV367(unittest.TestCase):
    def test_admission_requires_all_three_modules_and_aba_epochs(self):
        for term in (
            "enum Owner { AURA, TRIGGER_BOT, AUTO_CLICKER }",
            "if (auraActive) return Owner.AURA;",
            "if (triggerActive) return Owner.TRIGGER_BOT;",
            "auraActive == current.auraActive",
            "triggerActive == current.triggerActive",
            "autoClickerActive == current.autoClickerActive",
            "auraEpoch == current.auraEpoch",
            "triggerEpoch == current.triggerEpoch",
            "autoClickerEpoch == current.autoClickerEpoch",
        ):
            self.assertIn(term, LANE)
        for term in (
            "synchronized long lifecycleEpoch()",
            "++lifecycleEpoch;\n        enabled = true;",
            "++lifecycleEpoch;\n        enabled = false;",
        ):
            self.assertIn(term, AURA)
        gui = AURA.split("synchronized void suspendForGui() {", 1)[1].split(
            "\n    }", 1)[0]
        self.assertNotIn("++lifecycleEpoch", gui)
        self.assertIn("resetSchedule();", gui)

    def test_scheduled_lane_is_single_owner_and_not_recomputed_from_current_flags(self):
        admission = HOST.split("private Minecraft189CombatLaneAdmission currentCombatLaneAdmission()", 1)[1].split(
            "private boolean shouldAutoClickInternal(", 1)[0]
        for term in (
            "aura.active(), trigger.active(), clicker.active()",
            "aura.lifecycleEpoch(), trigger.nativeWorldOwnerEpoch()",
            "clicker.nativeOwnerEpoch()",
        ):
            self.assertIn(term, admission)
        scheduler = HOST.split("private boolean shouldAutoClickInternal(", 1)[1].split(
            "boolean mayDispatchScheduledCombatLane()", 1)[0]
        self.assertEqual(1, scheduler.count("final Minecraft189CombatLaneAdmission.Owner selectedLane"))
        self.assertEqual(3, scheduler.count("admittedLane = currentCombatLaneAdmission();"))
        self.assertIn("if (selectedLane == Minecraft189CombatLaneAdmission.Owner.AURA)", scheduler)
        self.assertIn("Owner.TRIGGER_BOT)", scheduler)
        self.assertIn("admittedLane.matches(currentCombatLaneAdmission())", scheduler)
        self.assertIn("pendingCombatLane = admittedLane;", scheduler)
        self.assertLess(scheduler.index("clickRateTracker.recordPress("),
                        scheduler.index("pendingCombatLane = admittedLane;"))
        self.assertNotIn("pendingCombatLane = admittedLane;\n        return false", scheduler)

    def test_bridge_checks_before_claim_hotbar_and_each_real_controller(self):
        dispatch = BRIDGE.split(
            "if (host.shouldAutoClick(playerHit, playerIndex, nativeHitbox)) {",
            1)[1].split("} finally {\n                // Every early return", 1)[0]
        self.assertEqual(6, dispatch.count("host.mayDispatchScheduledCombatLane()"))
        self.assertLess(
            dispatch.index("host.mayDispatchScheduledCombatLane()"),
            dispatch.index("host.claimSyntheticCombatPrimary("))
        self.assertIn(
            "if (!host.mayDispatchSyntheticMelee(originalSlot)\n"
            "                            || !host.mayDispatchScheduledCombatLane())",
            dispatch)
        self.assertEqual(2, dispatch.count(
            "if (!host.mayDispatchScheduledCombatLane())\n"
            "                                    return;\n"
            "                                minecraft.customMcAttackCrosshairEntity();"
        ) + dispatch.count(
            "if (!host.mayDispatchScheduledCombatLane())\n"
            "                                        return;\n"
            "                                    minecraft.customMcAttackCrosshairEntity();"
        ))
        self.assertIn(
            "if (!host.mayDispatchScheduledCombatLane())\n"
            "                                    return;\n"
            "                                minecraft.customMcClickMouse();",
            dispatch)
        for requirement in (
            "vetoOriginalScreenBeforeNativeCombat(",
            "permitsHitSelectAtNativeDispatch(",
            "host.mayDispatchOriginalCriticals()",
            "host.consumeTriggerDispatchUuid(",
            "host.consumeAutoClickerControllerIntent(",
            "host.restoreCombatSlotAfterSyntheticClick(originalSlot)",
        ):
            self.assertIn(requirement, dispatch)
        self.assertNotIn("leftClickCounter =", dispatch)

    def test_r3_post_melee_autoblock_cannot_borrow_a_stale_lane(self):
        # Verified original sword getters, hotbar leases and the physical
        # rightClickMouse() still own reblock. A module epoch change cannot
        # borrow a previously accepted automatic melee callback.
        block = HOST.split("boolean mayStartAutoBlock(", 1)[1].split(
            "private boolean identityRequired()", 1)[0]
        for phrase in (
            "if (!mayDispatchScheduledCombatLane()) return false;",
            "final boolean verifiedSword = holdingVerifiedSword();",
            "return mayDispatchScheduledCombatLane()",
            "featureCatalog.autoBlock().mayStart(",
            "if (!mayDispatchScheduledCombatLane()) return 0L;",
            "return featureCatalog.autoBlock().beginNativeUse(",
            "if (useToken == 0L || !mayDispatchScheduledCombatLane())",
            "final boolean nativeSwordStillHeld = holdingVerifiedSword();",
            "featureCatalog.autoBlock().permitsNativeUseDispatch(",
            "&& mayDispatchScheduledCombatLane();",
            "featureCatalog.autoBlock().cancelPendingNativeUse(useToken);",
            "featureCatalog.autoBlock().completeNativeUse(",
        ):
            self.assertIn(phrase, block)
        native = BRIDGE.split("if (swordBlock != null && host.mayStartAutoBlock(", 1)[1].split(
            "host.cancelAutoBlockNativeUse(useToken);", 1)[0]
        for phrase in (
            "host.claimSyntheticCombatReblock(combatCycleToken)",
            "host.beginAutoBlockNativeUse()",
            "host.mayDispatchAutoBlockNativeUse(useToken)",
            "if (!host.mayDispatchScheduledCombatLane())",
            "minecraft.customMcRightClickMouse();",
            "host.recordAutoBlockStart(useToken);",
        ):
            self.assertIn(phrase, native)
        self.assertLess(
            native.rindex("if (!host.mayDispatchScheduledCombatLane())"),
            native.index("minecraft.customMcRightClickMouse();"))
        self.assertLess(
            native.index("minecraft.customMcRightClickMouse();"),
            native.index("host.recordAutoBlockStart(useToken);"))
        self.assertIn(
            "finally {\n                            // This exact uncommitted native use lease",
            BRIDGE)

    def test_reentrant_nested_callbacks_vanilla_gui_and_shutdown_revoke_ticket(self):
        scheduler = HOST.split("private boolean shouldAutoClickInternal(", 1)[1].split(
            "boolean mayDispatchScheduledCombatLane()", 1)[0]
        self.assertIn(
            "final long admissionSerial = ++triggerDispatchAdmissionSerial;\n"
            "        pendingCombatLane = null;", scheduler)
        native_guard = HOST.split("boolean mayDispatchScheduledCombatLane()", 1)[1].split(
            "\n    }", 1)[0]
        self.assertIn("pendingCombatLane != null", native_guard)
        self.assertIn("pendingCombatLane.matches(currentCombatLaneAdmission())", native_guard)
        self.assertIn("!clickGuiRuntime.coreRuntime().model().snapshot().open()", native_guard)
        gui = HOST.split("void suspendSyntheticCombatForOriginalGui() {", 1)[1].split(
            "\n    }", 1)[0]
        self.assertIn("pendingCombatLane = null;", gui)
        shutdown = HOST.split("public void close() {", 1)[1]
        self.assertIn("combatCycle.revoke();\n        pendingCombatLane = null;", shutdown)
        leave = HOST.split("void leaveSyntheticCombatCycle(final long token) {", 1)[1].split(
            "\n    }", 1)[0]
        self.assertIn("pendingCombatLane = null;", leave)
        self.assertLess(leave.index("pendingCombatLane = null;"),
                        leave.index("combatCycle.leave(token);"))


if __name__ == "__main__":
    unittest.main()
