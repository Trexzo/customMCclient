"""V364 original HitSelect recovery commits only at Mojang controller observation.

Offline source-only checks; no fabricated damage, vanilla clicks, UUIDs or
claims of passing dedicated-server B/A rejoin controller acceptance.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/"platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
HIT=(BASE/"Minecraft189HitSelectModule.java").read_text(encoding="utf-8")
HOST=(BASE/"Minecraft189HostRuntime.java").read_text(encoding="utf-8")
BRIDGE=(BASE/"Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
RUN=(ROOT/"scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class V364NativeHitSelectRecovery(unittest.TestCase):
    def test_scheduler_cannot_consume_real_hurt_recovery(self):
        scheduler=HOST.split("private boolean shouldAutoClickInternal(",1)[1].split(
            "    /**",1)[0]
        self.assertNotIn("hitSelect.noteScheduledOriginalClick(",scheduler)
        self.assertNotIn("hitSelect.noteOriginalNativeControllerAttack(",scheduler)
        self.assertIn("hitSelect.permitsOriginalOwner(",scheduler)
        self.assertIn("hitSelect.nativeOwnerEpoch()",scheduler)

    def test_native_controller_only_exact_original_uuid_and_owner_epoch(self):
        method=HIT.split(
            "synchronized boolean noteOriginalNativeControllerAttack(",1
        )[1].split("\n    /**",1)[0]
        for v in (
            "!enabled || !requireObservedRecovery.get()",
            "scheduledOriginalUuid.equals(actualOriginalEntityUuid)",
            "scheduledOriginalUuid.equals(observedOriginalUuid)",
            "capturedOwnerEpoch != nativeOwnerEpoch",
            "capturedMinimum != minTargetHurtTicks.get()",
            "capturedMaximum != maxTargetHurtTicks.get()",
            "recoveryByUuid.get(scheduledOriginalUuid)",
            "!= RecoveryState.READY",
            "recoveryByUuid.put(scheduledOriginalUuid, RecoveryState.WAIT_FOR_HURT)",
        ):
            self.assertIn(v,method)
        legacy=HIT.split(
            "synchronized void noteScheduledOriginalClick(",1
        )[1].split("\n    private void forgetRecovery()",1)[0]
        self.assertNotIn("recoveryByUuid.put(",legacy)

    def test_exact_three_native_controller_lanes_scoped_and_cleanup(self):
        action=BRIDGE.split("    public static synchronized void autoClick(",1)[1].split(
            "    public static synchronized void publishTick(",1)[0]
        self.assertEqual(3,action.count(
            "armHitSelectOriginalControllerIntent("))
        self.assertEqual(3,action.count(
            "hitSelectNativeControllerIntent.remove();"))
        self.assertEqual(3,action.count(
            "host.mayDispatchOriginalCriticals()"))
        self.assertEqual(3,action.count(
            "permitsHitSelectAtNativeDispatch("))
        self.assertIn("minecraft.customMcClickMouse();",action)
        self.assertEqual(2,action.count(
            "minecraft.customMcAttackCrosshairEntity();"))
        self.assertIn("triggerDispatchUuid.set(sourceUuid)",action)
        self.assertIn("auraDispatchUuid.set(ownerUuid)",action)

    def test_native_observer_is_not_scheduler_or_manual_controller(self):
        observer=BRIDGE.split(
            "public static synchronized void acceptanceControllerAttackEntity(",1
        )[1].split("\n    /**",1)[0]
        # Observational controller-entry callbacks cannot spend recovery
        # before the original UUID and stale-dispatch veto are approved.
        self.assertNotIn("observeHitSelectOriginalControllerArgument(entity);",
                         observer)
        self.assertIn("Minecraft189VanillaMeleeAcceptance.onMojangControllerAttack",
                      observer)
        final_gate=BRIDGE.split(
            "public static synchronized boolean acceptanceControllerDispatchAllowed(",1
        )[1].split("\n    public static synchronized void acceptanceControllerAttackEntity(",1)[0]
        self.assertIn("final boolean nativeAuthorized;", final_gate)
        self.assertIn("allowStaleDispatchAfterRealExit(", final_gate)
        self.assertIn("if (nativeAuthorized)", final_gate)
        self.assertIn("observeHitSelectOriginalControllerArgument(entity);",
                      final_gate)
        self.assertLess(final_gate.index("allowStaleDispatchAfterRealExit("),
                        final_gate.index("observeHitSelectOriginalControllerArgument(entity);"))
        self.assertLess(final_gate.index("if (nativeAuthorized)"),
                        final_gate.index("observeHitSelectOriginalControllerArgument(entity);"))
        guard=BRIDGE.split(
            "private static void observeHitSelectOriginalControllerArgument(",1
        )[1].split("\n    // M423",1)[0]
        for v in (
            "if (intent == null) return",
            "hitSelectNativeControllerIntent.remove()",
            "entity instanceof Minecraft189NativeEntityUuidAccess",
            ".customMcNativeEntityUuid()",
            "noteOriginalNativeControllerAttack(",
        ):
            self.assertIn(v,guard)
        self.assertNotIn("customMcAttackCrosshairEntity",guard)
        self.assertNotIn("customMcClickMouse",guard)
        self.assertIn("wait_for_real_score(",RUN)
        self.assertIn("REJOIN_B_RETAINED, deadline,",RUN)

    def test_observed_real_uuid_hurt_cycle_is_still_nonreplayable(self):
        self.assertIn("RecoveryState.WAIT_FOR_HURT",HIT)
        self.assertIn("RecoveryState.WAIT_FOR_READY",HIT)
        self.assertIn("hurt > max",HIT)
        self.assertIn("hurt >= min && hurt <= max",HIT)
        self.assertIn("selectedOriginalUuid.equals(",HIT)
        self.assertIn("suspendForSource()",HIT)
        self.assertIn("suspendForGui()",HIT)


if __name__=="__main__": unittest.main()
