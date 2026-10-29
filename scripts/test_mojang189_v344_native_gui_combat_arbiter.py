"""V344: original vanilla GuiScreen coordinates with the V343 combat lanes.

Source-wire regression only. A passed test here never represents an actual
Mojang 1.8.9 controller attack, matching UUID, native hurt or server damage.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SRC = (ROOT / "platform-1.8.9" / "src" / "main" / "java"
       / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9")
read = lambda filename: (SRC / filename).read_text(encoding="utf-8")
BRIDGE = read("Minecraft189RuntimeBridge.java")
HOST = read("Minecraft189HostRuntime.java")
VETO = read("Minecraft189NativeGuiCombatVeto.java")
POT = read("Minecraft189AutoPotSourceCast.java")
WTAP = read("Minecraft189WTapModule.java")
ARB = read("Minecraft189CombatCycleArbiter.java")
MAPPED = (ROOT / "platform-1.8.9" / "src" / "test" / "java"
          / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"
          / "Minecraft189MappedHostTransformationTest.java"
          ).read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py"
           ).read_text(encoding="utf-8")


class NativeGuiAndCombatCycleV344(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.action = BRIDGE.split(
            "public static synchronized void autoClick(", 1)[1].split(
            "public static synchronized void publishTick(", 1)[0]

    def test_only_source_original_ave_and_verified_clear_admit_automation(self):
        self.assertIn('"ave".equals(minecraft.getClass().getName())', VETO)
        self.assertIn("Minecraft189ScaffoldScreenGate.inspect(minecraft)", VETO)
        self.assertIn("screen != Minecraft189ScaffoldScreenGate.Verdict.CLEAR", VETO)
        self.assertNotIn("leftClickCounter =", VETO)
        self.assertNotIn("customMcClickMouse(", VETO)

    def test_initial_veto_is_after_negative_controls_and_before_any_synthetic_action(self):
        action = self.action
        initial = action.index("if (vetoOriginalScreenBeforeNativeCombat(host, minecraft))")
        self.assertLess(action.index("custommc.acceptance.reportKillAuraLocalPvp"),
                        initial)
        self.assertLess(initial, action.index("host.shouldReleaseAutoBlock("))
        self.assertLess(initial, action.index("host.shouldAutoPot("))
        self.assertLess(initial, action.index("host.shouldAutoRod("))
        self.assertLess(initial, action.index("host.shouldAutoClick("))
        self.assertLess(action.index("host.enterSyntheticCombatCycle()"), initial)

    def test_all_three_final_melee_calls_recheck_gui_after_uuid_and_hurt(self):
        action = self.action
        self.assertEqual(3, action.count("if (!permitsHitSelectAtNativeDispatch("))
        native_calls = [
            "triggerDispatchUuid.set(sourceUuid);",
            "auraDispatchUuid.set(ownerUuid);",
            "minecraft.customMcClickMouse();",
        ]
        self.assertEqual(2, action.count("minecraft.customMcAttackCrosshairEntity();"))
        for native in native_calls:
            at = action.index(native)
            previous = action.rfind("vetoOriginalScreenBeforeNativeCombat(", 0, at)
            self.assertGreater(previous, 0)
            previous_hurt = action.rfind("if (!permitsHitSelectAtNativeDispatch(", 0, at)
            self.assertGreater(previous, previous_hurt)
        # Keep the original seven vanilla GuiScreen checks, plus V360's
        # post-sword, V361's post-rod, and V362's post-live-health-potion
        # source-getter checks. No existing screen veto may be removed.
        self.assertEqual(10, action.count("vetoOriginalScreenBeforeNativeCombat("))
        potion = action.index("if (host.shouldAutoPot(playerHit, playerIndex))")
        final_health = action.index("host.mayDispatchAutoPotNativeUse(", potion)
        final_screen = action.index("vetoOriginalScreenBeforeNativeCombat(",
                                    final_health)
        rod_start = action.index("if (host.shouldAutoRod(playerHit, playerIndex))",
                                 final_screen)
        self.assertLess(final_health, final_screen)
        self.assertLess(final_screen, rod_start)
        rod = action.index("if (host.mayExecuteAutoRodUse(originalRodSlot))")
        first_screen = action.index("vetoOriginalScreenBeforeNativeCombat(", rod)
        next_rod = action.index("if (!host.mayDispatchAutoRodNativeUse(originalRodSlot))",
                                first_screen)
        last_screen = action.index("vetoOriginalScreenBeforeNativeCombat(",
                                   next_rod)
        original_right = action.index("minecraft.customMcRightClickMouse();",
                                      last_screen)
        self.assertLess(first_screen, next_rod)
        self.assertLess(next_rod, last_screen)
        self.assertLess(last_screen, original_right)
        block = action.index("host.beginAutoBlockNativeUse()")
        new_source = action.index("host.mayDispatchAutoBlockNativeUse(useToken)", block)
        next_screen = action.index("vetoOriginalScreenBeforeNativeCombat(", new_source)
        native_reblock = action.index("minecraft.customMcRightClickMouse();",
                                     next_screen)
        self.assertLess(new_source, next_screen)
        self.assertLess(next_screen, native_reblock)

    def test_item_lanes_refuse_native_use_before_commit(self):
        action = self.action
        self.assertIn("Minecraft189AutoPotSourceCast.execute(", action)
        self.assertIn("() -> !vetoOriginalScreenBeforeNativeCombat(", action)
        self.assertLess(POT.index("if (!nativeAdmission.getAsBoolean()) return false;"),
                        POT.index("nativeRightClick.run();"))
        self.assertLess(POT.index("nativeRightClick.run();"),
                        POT.index("host.commitAutoPotUseAttempt(originalSlot);"))
        rod = action.index("if (host.mayExecuteAutoRodUse(originalRodSlot))")
        self.assertLess(rod,
                        action.index("if (vetoOriginalScreenBeforeNativeCombat(", rod))
        self.assertLess(action.index("if (vetoOriginalScreenBeforeNativeCombat(", rod),
                        action.index("host.commitAutoRodUseAttempt(originalRodSlot)"))
        block = action.index("host.beginAutoBlockNativeUse()")
        self.assertLess(block,
                        action.index("if (vetoOriginalScreenBeforeNativeCombat(", block))
        self.assertLess(action.index("if (vetoOriginalScreenBeforeNativeCombat(", block),
                        action.index("host.recordAutoBlockStart(useToken)"))

    def test_veto_revokes_existing_credit_without_stealing_manual_mouse(self):
        suspend = HOST.split("void suspendSyntheticCombatForOriginalGui()", 1)[1].split(
            "boolean shouldAutoClick()", 1)[0]
        for marker in (
            "triggerDispatchLease.clear()",
            "featureCatalog.autoClicker().suspendForGui()",
            "featureCatalog.triggerBot().suspend()",
            "featureCatalog.killAura().suspend()",
            "featureCatalog.wTap().suspendForGui(",
            "pendingKeepSprintSource = null",
        ):
            self.assertIn(marker, suspend)
        self.assertIn("nativeSprintWriteInProgress", WTAP)
        self.assertIn("host.leaveSyntheticCombatCycle(combatCycleToken)", self.action)
        self.assertIn("phase != Phase.MELEE_ATTEMPT", ARB)
        self.assertNotIn("customMcRightClickMouse()", suspend)

    def test_mapped_fixture_has_original_source_gui_and_real_negative_control(self):
        self.assertIn('loader.put("axu", emptyClass("axu"));', MAPPED)
        self.assertIn('field(writer, "m", "Laxu;");', MAPPED)
        self.assertIn('minecraftClass.getField("m").set(', MAPPED)
        self.assertIn('loader.loadClass("axu").getDeclaredConstructor()', MAPPED)

    def test_server_authoritative_original_acceptance_remains_unchanged(self):
        for marker in (
            "wait_for(host, host_log, CONTROLLER_ATTACK,",
            "TRIGGER_HURT if args.triggerbot_pvp else AURA_HURT,",
            "wait_for(host, host_log, REJOIN_B_HIT, deadline,",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(marker, HARNESS)
        self.assertNotIn("GUI_SCREEN_PASS", ARB)
        self.assertNotIn("V344_NATIVE_GUI_PASS", HARNESS)


if __name__ == "__main__":
    unittest.main()
