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
        self.assertEqual(11, dispatch.count("host.mayDispatchScheduledCombatLane()"))
        self.assertLess(
            dispatch.index("host.mayDispatchScheduledCombatLane()"),
            dispatch.index("host.claimSyntheticCombatPrimary("))
        self.assertIn(
            "if (!host.mayDispatchSyntheticMelee(originalSlot)\n"
            "                            || !host.mayDispatchScheduledCombatLane())",
            dispatch)
        self.assertEqual(2, dispatch.count(
            "if (!host.mayDispatchScheduledCombatLane()\n"
            "                                        || !host.mayRetainOriginalCriticalsAdmission()\n"
            "                                        || !host.mayRetainOriginalHitSelectAdmission())\n"
            "                                    return;\n"
            "                                minecraft.customMcAttackCrosshairEntity();"
        ) + dispatch.count(
            "if (!host.mayDispatchScheduledCombatLane()\n"
            "                                            || !host.mayRetainOriginalCriticalsAdmission()\n"
            "                                            || !host.mayRetainOriginalHitSelectAdmission())\n"
            "                                        return;\n"
            "                                    minecraft.customMcAttackCrosshairEntity();"
        ))
        self.assertIn(
            "if (!host.mayDispatchScheduledCombatLane()\n"
            "                                        || !host.mayRetainOriginalCriticalsAdmission()\n"
            "                                        || !host.mayRetainOriginalHitSelectAdmission())\n"
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

    def test_r4_original_criticals_policy_retained_after_hitselect_and_gui(self):
        # This is only the source contract: original Java/Mojang integration
        # and real server-positive damage require exact-head CI separately.
        proof = HOST.split("boolean mayRetainOriginalCriticalsAdmission() {", 1)[1].split(
            "\n    }", 1)[0]
        for guard in (
            "if (!mayDispatchScheduledCombatLane()",
            "criticalsNativeAdmissionSerial < 0L",
            "criticalsNativeAdmissionSerial",
            "!= triggerDispatchAdmissionSerial",
            "module.active() != criticalsNativeActive",
            "module.lifecycleEpoch() != criticalsNativeLifecycleEpoch",
            "module.minimumFallDistanceSetting().get()",
            "!= criticalsNativeMinimumFall",
            "module.requireDescendingSetting().get()",
            "!= criticalsNativeRequireDescending",
            "return !module.active()",
            "criticalNativeSourceStillOwned(",
            "criticalsNativeSourceGeneration",
        ):
            self.assertIn(guard, proof)
        # Do not inject new vanilla mapped fall getters or a source-motion
        # sample between HitSelect proof and the real controller attack.
        for forbidden in (
            "customMcOnGround()", "customMcFallDistance()",
            "customMcMotionY()", "customMcAttackCrosshairEntity()",
            "customMcClickMouse()", "new HitSelectNativeControllerIntent",
        ):
            self.assertNotIn(forbidden, proof)
        dispatch = BRIDGE.split(
            "if (host.shouldAutoClick(playerHit, playerIndex, nativeHitbox)) {",
            1)[1].split("} finally {\n                // Every early return", 1)[0]
        self.assertEqual(3, dispatch.count(
            "host.mayRetainOriginalCriticalsAdmission()"))
        self.assertEqual(3, dispatch.count(
            "if (!host.mayDispatchOriginalCriticals()) return;"))
        self.assertEqual(3, dispatch.count(
            "permitsHitSelectAtNativeDispatch("))
        # Every lane has the same admission sequence, not merely a
        # late global check which could be skipped for a native branch.
        for native_call in (
            "minecraft.customMcAttackCrosshairEntity();",
            "minecraft.customMcClickMouse();",
        ):
            self.assertIn(native_call, dispatch)
        for section in dispatch.split("host.mayDispatchOriginalCriticals()")[1:]:
            self.assertLess(section.index("permitsHitSelectAtNativeDispatch("),
                            section.index("mayRetainOriginalCriticalsAdmission()"))
            self.assertLess(section.index("mayRetainOriginalCriticalsAdmission()"),
                            min((section.find(name) for name in (
                                "minecraft.customMcAttackCrosshairEntity();",
                                "minecraft.customMcClickMouse();")
                                if section.find(name) >= 0)))

    def test_r5_hitselect_lifecycle_and_policy_fails_closed_at_controller(self):
        # Original UUID/hurt selection and the genuine controller argument
        # remain separately authoritative. This retains only the same module
        # policy after native ray, item and GUI callback reentry.
        policy = (SOURCE / "Minecraft189HitSelectPolicyAdmission.java").read_text(
            encoding="utf-8")
        for requirement in (
            "enabled == current.enabled",
            "ownerEpoch == current.ownerEpoch",
            "requireRecovery == current.requireRecovery",
            "minimum == current.minimum",
            "maximum == current.maximum",
        ):
            self.assertIn(requirement, policy)
        for forbidden in (
            "customMc", "noteOriginalNativeControllerAttack",
            "recoveryByUuid.put", "sendPacket",
        ):
            self.assertNotIn(forbidden, policy)
        snapshot = HOST.split(
            "private Minecraft189HitSelectPolicyAdmission currentHitSelectPolicy()", 1
        )[1].split("private boolean shouldAutoClickInternal(", 1)[0]
        for term in (
            "select.active(), select.nativeOwnerEpoch()",
            "select.requireObservedRecoverySetting().get()",
            "select.minTargetHurtTicksSetting().get()",
            "select.maxTargetHurtTicksSetting().get()",
        ):
            self.assertIn(term, snapshot)
        scheduler = HOST.split("private boolean shouldAutoClickInternal(", 1)[1].split(
            "boolean mayDispatchScheduledCombatLane()", 1)[0]
        self.assertIn("pendingHitSelectPolicy = null;", scheduler)
        self.assertIn("pendingHitSelectPolicy = hitSelectPolicy;", scheduler)
        self.assertLess(
            scheduler.index("hitSelect.permitsOriginalOwner("),
            scheduler.index("pendingHitSelectPolicy = hitSelectPolicy;"))
        self.assertLess(
            scheduler.index("pendingHitSelectPolicy = hitSelectPolicy;"),
            scheduler.index("pendingCombatLane = admittedLane;"))
        guard = HOST.split("boolean mayRetainOriginalHitSelectAdmission() {", 1)[1].split(
            "\n    }", 1)[0]
        self.assertIn("mayDispatchScheduledCombatLane()", guard)
        self.assertIn("pendingHitSelectPolicy != null", guard)
        self.assertIn("pendingHitSelectPolicy.matches(currentHitSelectPolicy())", guard)
        for forbidden in ("customMc", "noteOriginal", "observeOriginalOwner",
                          "shouldClick", "permitsOriginalOwner"):
            self.assertNotIn(forbidden, guard)
        dispatch = BRIDGE.split(
            "if (host.shouldAutoClick(playerHit, playerIndex, nativeHitbox)) {",
            1)[1].split("} finally {\n                // Every early return", 1)[0]
        self.assertEqual(3, dispatch.count("host.mayRetainOriginalHitSelectAdmission()"))
        self.assertEqual(3, dispatch.count("permitsHitSelectAtNativeDispatch("))
        self.assertEqual(3, dispatch.count("host.mayDispatchOriginalCriticals()"))
        self.assertEqual(3, dispatch.count("host.mayRetainOriginalCriticalsAdmission()"))
        for section in dispatch.split("host.mayDispatchOriginalCriticals()")[1:]:
            self.assertLess(section.index("permitsHitSelectAtNativeDispatch("),
                            section.index("mayRetainOriginalHitSelectAdmission()"))
            self.assertLess(section.index("mayRetainOriginalHitSelectAdmission()"),
                            min(section.index(name) for name in (
                                "minecraft.customMcAttackCrosshairEntity();",
                                "minecraft.customMcClickMouse();") if name in section))
        for shutdown in (
            HOST.split("void suspendSyntheticCombatForOriginalGui() {", 1)[1].split(
                "\n    }", 1)[0],
            HOST.split("void leaveSyntheticCombatCycle(final long token) {", 1)[1].split(
                "\n    }", 1)[0],
            HOST.split("public void close() {", 1)[1],
        ):
            self.assertIn("pendingHitSelectPolicy = null;", shutdown)
        self.assertIn(
            "host.featureCatalog().hitSelect().noteOriginalNativeControllerAttack(",
            BRIDGE)

    def test_r3_post_melee_autoblock_preserves_standalone_source_api(self):
        # Existing sword-source JUnit fixtures deliberately call the host
        # native sword/lease APIs without an automatic melee callback.
        # Only the real bridge call path may require a synthetic lane ticket.
        block = HOST.split("boolean mayStartAutoBlock(", 1)[1].split(
            "private boolean identityRequired()", 1)[0]
        self.assertNotIn("mayDispatchScheduledCombatLane()", block)
        for phrase in (
            "featureCatalog.autoBlock().mayStart(",
            "return featureCatalog.autoBlock().beginNativeUse(",
            "final boolean nativeSwordStillHeld = holdingVerifiedSword();",
            "featureCatalog.autoBlock().permitsNativeUseDispatch(",
            "featureCatalog.autoBlock().cancelPendingNativeUse(useToken);",
            "featureCatalog.autoBlock().completeNativeUse(",
        ):
            self.assertIn(phrase, block)
        native = BRIDGE.split(
            "if (swordBlock != null\n"
            "                        && host.mayDispatchScheduledCombatLane()", 1
        )[1].split(
            "host.cancelAutoBlockNativeUse(useToken);", 1)[0]
        for phrase in (
            "host.claimSyntheticCombatReblock(combatCycleToken)",
            "host.beginAutoBlockNativeUse()",
            "host.mayDispatchAutoBlockNativeUse(useToken)",
            "host.mayDispatchScheduledCombatLane()",
            "minecraft.customMcRightClickMouse();",
            "host.recordAutoBlockStart(useToken);",
        ):
            self.assertIn(phrase, native)
        self.assertEqual(5, native.count("host.mayDispatchScheduledCombatLane()"))
        self.assertLess(
            native.index("host.mayStartAutoBlock("),
            native.index("host.mayDispatchScheduledCombatLane()"))
        self.assertLess(
            native.index("host.mayStartAutoBlock("),
            native.index("host.claimSyntheticCombatReblock(combatCycleToken)"))
        self.assertLess(
            native.rindex("if (!host.mayDispatchScheduledCombatLane())"),
            native.index("minecraft.customMcRightClickMouse();"))
        self.assertLess(
            native.index("minecraft.customMcRightClickMouse();"),
            native.index("host.recordAutoBlockStart(useToken);"))
        self.assertIn("if (swordBlock.customMcIsUsingItem()) {", native)
        self.assertIn("if (host.mayDispatchScheduledCombatLane())", native)
        self.assertLess(
            native.index("if (swordBlock.customMcIsUsingItem()) {"),
            native.index("if (host.mayDispatchScheduledCombatLane())"))
        self.assertLess(
            native.index("if (host.mayDispatchScheduledCombatLane())"),
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
