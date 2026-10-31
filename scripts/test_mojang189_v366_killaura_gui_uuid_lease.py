"""V366 original-world Aura UUID lease survives GUI pause, never source loss.

These are source-only guard assertions, not evidence of real native B
controller dispatch or authoritative server damage in the V47 acceptance.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
MODULE = (BASE / "Minecraft189KillAuraModule.java").read_text(encoding="utf-8")
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
SELECTOR = (BASE / "Minecraft189KillAuraTargetSelector.java").read_text(
    encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class OriginalAuraGuiUuidLeaseV366(unittest.TestCase):
    def test_gui_does_not_clear_only_source_verified_uuid(self):
        gui = MODULE.split("synchronized void suspendForGui() {", 1)[1].split(
            "\n    }", 1)[0]
        self.assertIn("resetSchedule();", gui)
        self.assertIn("lastEntityIndex = -1;", gui)
        self.assertNotIn("rememberSelectedTarget(null", gui)
        self.assertNotIn("lockedTargetUuid = null", gui)
        self.assertNotIn("customMc", gui)
        self.assertIn(
            'synchronized void suspend() { clear("host-suspend-original-owner-lost"); }',
            MODULE)
        self.assertIn('clear("module-disable")', MODULE)
        self.assertIn('clear("invalid-original-player-rotation")', MODULE)

    def test_all_gui_fences_suspend_scheduler_without_releasing_world_uuid(self):
        self.assertEqual(3, HOST.count(
            "featureCatalog.killAura().suspendForGui();"))
        self.assertIn("featureCatalog.killAura().suspend();", HOST)
        self.assertIn("featureCatalog.killAura().rememberSelectedTarget(\n"
                      '                null, "original-world-source-revoked");', HOST)
        self.assertIn('"original-world-identity-changed"', HOST)
        self.assertIn('"original-world-combat-inconsistent"', HOST)
        self.assertIn("featureCatalog.hitSelect().suspendForGui();", HOST)
        self.assertIn("featureCatalog.autoClicker().suspendForGui();", HOST)
        self.assertIn("vetoOriginalScreenBeforeNativeCombat(", BRIDGE)

    def test_real_world_selector_has_no_ghost_uuid_owner(self):
        for term in (
            "final UUID lockedUuid = aura.lockedTargetUuid();",
            "verifiedLockRetainable(",
            "lockedUuid.equals(identities.at(candidate.entityIndex()))",
            "identities.entityCount() != positions.entityCount()",
            "if (ownedIndex >= 0) return false;",
            "kinds.localPlayer(ownedIndex)",
            "!kinds.living(ownedIndex) || !combat.alive(ownedIndex)",
            "distanceSquared > range*range",
            "antiBot.permits(ownedIndex, combat)",
            "teamGuard.permits(ownedIndex, combat)",
            "friendGuard.permits(ownedIndex, identities)",
            "wallCheck.permits(ownedIndex, visibility)",
            "angularScore(local, rotation, candidate, fov)",
            '"selector-live-candidate-or-absent"',
        ):
            self.assertIn(term, SELECTOR)
        self.assertIn("if (aura != null) aura.rememberSelectedTarget(null,",
                      SELECTOR)
        self.assertIn("target.clear();", SELECTOR)

    def test_phase_three_original_ray_and_scheduler_diagnostics_are_read_only(self):
        owner = (BASE / "Minecraft189LockedTargetAcceptance.java").read_text(
            encoding="utf-8")
        self.assertIn('phase != 3) return;', owner)
        self.assertIn('callback != 1 && callback % 40 != 0', owner)
        self.assertIn('CUSTOMMC_189_V366_LOCK_PHASE3_NATIVE_RAY_DIAG', owner)
        self.assertIn('CUSTOMMC_189_V366_LOCK_PHASE3_SCHEDULE_DIAG', owner)
        ray = owner.split('static void observeNativeRay(', 1)[1].split(
            '    static void observeAuraSchedule(', 1)[0]
        schedule = owner.split('static void observeAuraSchedule(', 1)[1].split(
            '    static void verifyOwnedClick(', 1)[0]
        for segment in (ray, schedule):
            for forbidden in ('customMcAttackCrosshairEntity(', 'customMcClickMouse(',
                              'sendPacket(', 'rememberSelectedTarget(',
                              'Minecraft189KillAuraTargetSelector.select(',
                              'aHit=true', 'bHit=true'):
                self.assertNotIn(forbidden, segment)
        self.assertIn('Minecraft189LockedTargetAcceptance.observeNativeRay(', BRIDGE)
        self.assertIn('if (host.shouldAutoClick(playerHit, playerIndex, nativeHitbox))',
                      BRIDGE)
        self.assertIn('Minecraft189LockedTargetAcceptance.observeAuraSchedule(',
                      HOST)

    def test_phase_three_tick_and_failure_forensics_are_read_only(self):
        owner = (BASE / "Minecraft189LockedTargetAcceptance.java").read_text(
            encoding="utf-8")
        observed = owner.split("static void observe(Minecraft189HostRuntime host)", 1)[1].split(
            "static void observeNativeRay(", 1)[0]
        self.assertIn("phaseThreeTickObservations = 0;", owner)
        self.assertIn("if (phase == 3)", observed)
        self.assertIn("tick == 1 || tick % 40 == 0", observed)
        self.assertIn("CUSTOMMC_189_V366_LOCK_PHASE3_TICK_DIAG", observed)
        self.assertIn("rayCallbacks=", observed)
        self.assertIn("scheduleCallbacks=", observed)
        self.assertIn("rotationYaw=", observed)
        self.assertIn("lockedUuid=", observed)
        diagnostic = observed.split("// V366 R3:", 1)[1].split(
            "if (phase==4 &&", 1)[0]
        for forbidden in ("customMc", "attackEntity(", "sendPacket(",
                          "rememberSelectedTarget(", "shouldClick(",
                          "customMcSetRotation", "phase=4"):
            self.assertNotIn(forbidden, diagnostic)
        for witness in (
            'b"CUSTOMMC_189_V366_LOCK_PHASE3_TICK_DIAG"',
            'b"CUSTOMMC_189_V366_LOCK_PHASE3_NATIVE_RAY_DIAG"',
            'b"CUSTOMMC_189_V366_LOCK_PHASE3_SCHEDULE_DIAG"',
            "with log.open(\"rb\") as original_host:",
            "if len(phase3_first) < 12:",
            "if len(phase3_last) == 12:",
            "CUSTOMMC_189_V366_PHASE3_FORENSICS_START",
            "CUSTOMMC_189_V366_PHASE3_FORENSICS_END",
        ):
            self.assertIn(witness, HARNESS)


    def test_r4_native_combat_stage_diagnostics_cannot_change_admission(self):
        owner = (BASE / "Minecraft189LockedTargetAcceptance.java").read_text(
            encoding="utf-8")
        checkpoint = owner.split("static void observeCombatStage(", 1)[1].split(
            "static void observeNativeRay(", 1)[0]
        for stage in ("ENTER", "SCREEN_CLEAR", "GUI_VETO",
                      "POTION_RETURN", "ROD_RETURN", "BEFORE_RAY"):
            self.assertIn("case " + stage + ":", checkpoint)
            self.assertIn("CombatStage." + stage + ")", BRIDGE)
        self.assertIn("|| phase != 3 || stage == null) return;", checkpoint)
        for forbidden in ("customMc", "attackEntity(", "sendPacket(",
                          "rememberSelectedTarget(", "shouldClick(",
                          "Minecraft189KillAuraTargetSelector.select("):
            self.assertNotIn(forbidden, checkpoint)
        cycle = BRIDGE.split("public static synchronized void autoClick(", 1)[1]
        self.assertLess(cycle.index("CombatStage.ENTER)"),
                        cycle.index("reportTriggerBotRealPvp"))
        screen = cycle.index("if (vetoOriginalScreenBeforeNativeCombat(host, minecraft))")
        clear = cycle.index("CombatStage.SCREEN_CLEAR)")
        self.assertLess(screen, clear)
        self.assertLess(cycle.index("CombatStage.POTION_RETURN)"),
                        cycle.index("CombatStage.ROD_RETURN)"))
        self.assertLess(cycle.index("CombatStage.ROD_RETURN)"),
                        cycle.index("CombatStage.BEFORE_RAY)"))
        self.assertLess(cycle.index("CombatStage.BEFORE_RAY)"),
                        cycle.index("Minecraft189LockedTargetAcceptance.observeNativeRay("))
        original_screen = BRIDGE.split(
            "private static boolean vetoOriginalScreenBeforeNativeCombat(", 1)[1].split(
            "public static synchronized void autoClick(", 1)[0]
        self.assertIn("Minecraft189NativeGuiCombatVeto.blocks(minecraft)",
                      original_screen)
        self.assertIn("CombatStage.GUI_VETO)", original_screen)
        for text in ("pathEntered=", "screenClear=", "guiVeto=",
                     "potReturn=", "rodReturn=", "beforeRay=",
                     "rayCallbacks=", "scheduleCallbacks="):
            self.assertIn(text, owner)

    def test_r5_gui_veto_reports_exact_original_screen_without_admitting_click(self):
        owner = (BASE / "Minecraft189LockedTargetAcceptance.java").read_text(
            encoding="utf-8")
        method = owner.split("static void observeBlockedNativeScreen(", 1)[1].split(
            "static void observeNativeRay(", 1)[0]
        self.assertIn("phase != 3) return;", method)
        self.assertIn("n != 1 && n % 80 != 0", method)
        self.assertIn('"axu".equals(candidate.getType().getName())', method)
        self.assertIn("Minecraft189ScaffoldScreenGate.inspect(originalMinecraft)", method)
        self.assertIn("CUSTOMMC_189_V366_LOCK_PHASE3_ORIGINAL_SCREEN_DIAG", method)
        self.assertIn("originalVerdict=", method)
        self.assertIn("screenClass=", method)
        for forbidden in ("customMc", "attackEntity(", "sendPacket(",
                          "rememberSelectedTarget(", "shouldClick(",
                          "Minecraft189KillAuraTargetSelector.select(",
                          "Minecraft189ScaffoldScreenGate.Verdict.CLEAR"):
            self.assertNotIn(forbidden, method)
        veto = BRIDGE.split(
            "private static boolean vetoOriginalScreenBeforeNativeCombat(", 1)[1].split(
            "public static synchronized void autoClick(", 1)[0]
        self.assertLess(veto.index("Minecraft189NativeGuiCombatVeto.blocks(minecraft)"),
                        veto.index("observeBlockedNativeScreen("))
        self.assertIn("host.suspendSyntheticCombatForOriginalGui();", veto)
        self.assertIn("return true;", veto)

    def test_r6_original_pause_recovery_is_real_gui_and_real_x11_only(self):
        # The test harness may dismiss a source-confirmed vanilla pause,
        # never forge a CLEAR verdict, controller click, server damage or
        # loosen the original GuiScreen veto in production.
        self.assertIn(
            'if args.rejoin_target or args.rejoin_release or args.locked_target:',
            HARNESS)
        self.assertIn(
            '"custommc.acceptance.reportKillAuraLockedTarget"',
            BRIDGE.split('private static void reportAcceptanceScreen()', 1)[0])
        recovery = HARNESS.split(
            "def v348_resume_confirmed_original_pause(", 1)[1].split(
            "def restore_real_attacker_focus(", 1)[0]
        for term in (
            'v348_latest_original_gui_screen(host_log) != "axp"',
            'send_real_host_key(host, window, "Escape")',
            'b"CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)"',
            "V349 captured original host window disappeared",
            "V349 original host window title invalid",
            "CUSTOMMC_189_V348_SOURCE_PAUSE_CLOSED_BY_REAL_ESCAPE=YES",
        ):
            self.assertIn(term, recovery)
        sticky = HARNESS.split('if args.locked_target:', 1)[1].split(
            'if args.simultaneous:', 1)[0]
        preattack = sticky.split(
            'CUSTOMMC_OFFICIAL_189_LOCK_NEARER_B_STILL_ZERO_DAMAGE_PASS=YES',
            1)[1].split('send_server_command(server, server_log, "gamemode 0 CITarget")',
                     1)[0]
        self.assertIn('v348_resume_confirmed_original_pause(', preattack)
        self.assertIn(
            'v348_latest_original_gui_screen(host_log) != "(in-game/no GUI)"',
            preattack)
        self.assertIn(
            "CUSTOMMC_189_V366_LOCK_PREATTACK_ORIGINAL_GUI_CLEAR_PASS=YES",
            preattack)
        self.assertLess(sticky.index("LOCK_RETAINED, deadline,"),
                        sticky.index("v348_resume_confirmed_original_pause("))
        self.assertLess(sticky.index("CUSTOMMC_189_V366_LOCK_PREATTACK_"),
                        sticky.index('send_server_command(server, server_log, "gamemode 0 CITarget")'))
        for must_remain in (
            'wait_for(host, host_log, LOCK_HIT_A, deadline,',
            'wait_for_real_score(server, server_log, "CITarget", deadline)',
            '"scoreboard players test CITargetB taken 0 0"',
            'wait_for(host, host_log, LOCK_RELEASE, deadline,',
            'wait_for(host, host_log, LOCK_HIT_B, deadline,',
            'wait_for_real_score(server, server_log, "CITargetB", deadline)',
        ):
            self.assertIn(must_remain, sticky)
        veto = BRIDGE.split(
            "private static boolean vetoOriginalScreenBeforeNativeCombat(", 1)[1].split(
            "public static synchronized void autoClick(", 1)[0]
        self.assertIn("Minecraft189NativeGuiCombatVeto.blocks(minecraft)", veto)
        self.assertIn("return true;", veto)
        self.assertIn("host.suspendSyntheticCombatForOriginalGui();", veto)

    def test_original_v47_rejoin_server_damage_oracle_unchanged(self):
        for term in (
            "CUSTOMMC_189_REJOIN_REAL_A_DISCONNECTED_PASS",
            "CUSTOMMC_189_REJOIN_B_ONLY_STABLE_NO_DAMAGE_PASS",
            "CUSTOMMC_189_REJOIN_B_DAMAGE_A_STILL_ZERO_PASS",
            "REJOIN_B_RETAINED, deadline,",
            "wait_for_real_score(",
        ):
            self.assertIn(term, HARNESS)
        self.assertIn("CUSTOMMC_189_M419_TRANSIENT_WRONG_UUID_CLICK_VETO",
                      (BASE / "Minecraft189RejoinTargetAcceptance.java").read_text(
                          encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
