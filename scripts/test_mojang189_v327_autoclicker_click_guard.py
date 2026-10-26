"""V327 offline source-wiring checks for observational native click guard.

These checks CANNOT certify a Mojang attack. Genuine Auto Clicker success
continues to require the exact controller UUID, native hurt and vanilla-server
damage in the independently failed/preserved V326 original workflow.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "platform-1.8.9" / "src" / "main" / "java" / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"


class V327OriginalClickGuardDiagnosticTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.melee = (JAVA / "Minecraft189VanillaMeleeAcceptance.java").read_text(encoding="utf-8")
        cls.bridge = (JAVA / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
        cls.harness = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(encoding="utf-8")

    def test_guard_is_original_counter_read_only_and_remote_opt_in(self):
        method = self.melee.split("static void onNativeClickGuard(final int nativeCounter) {", 1)[1].split(
            "static void onMojangControllerAttack(", 1)[0]
        self.assertIn("syntheticCall && Boolean.getBoolean(", method)
        self.assertIn('"custommc.acceptance.requireAutoClickerExactRemoteUuid"', method)
        self.assertIn("nativeCounter > 0", method)
        self.assertIn("nativeCounter <= 0", method)
        self.assertIn("AUTOCLICKER_MOJANG_LEFT_CLICK_COUNTER_BLOCKED=", method)
        self.assertIn("AUTOCLICKER_MOJANG_LEFT_CLICK_COUNTER_READY=YES", method)
        self.assertNotIn("nativeCounter =", method)
        self.assertNotIn("leftClickCounter =", method)
        self.assertNotIn("customMcClickMouse()", method)
        self.assertNotIn("PASS=YES", method)

    def test_diagnostics_are_bounded_and_reinitialized(self):
        for flag in ("autoclickerBlockedCounterReported", "autoclickerReadyCounterReported"):
            self.assertIn("private static boolean " + flag + ";", self.melee)
            self.assertIn(flag + " = false;", self.melee)
        method = self.melee.split("static void onNativeClickGuard(final int nativeCounter) {", 1)[1].split(
            "static void onMojangControllerAttack(", 1)[0]
        self.assertIn("!autoclickerBlockedCounterReported", method)
        self.assertIn("!autoclickerReadyCounterReported", method)
        self.assertIn("autoclickerBlockedCounterReported = true;", method)
        self.assertIn("autoclickerReadyCounterReported = true;", method)
        self.assertIn('"custommc.acceptance.reportKillAuraLocalPvp"', method)
        self.assertIn("!syntheticAura", method)

    def test_original_dispatch_and_authoritative_success_remain_required(self):
        self.assertIn("Minecraft189VanillaMeleeAcceptance.onNativeClickGuard(", self.bridge)
        self.assertIn("CUSTOMMC_189_AUTOCLICKER_NATIVE_CONTROLLER_UNSCOPED_UUID_PASS=YES", self.bridge)
        self.assertIn("CUSTOMMC_189_AUTOCLICKER_NATIVE_EXACT_REMOTE_PLAYER_UUID_PASS=YES", self.bridge)
        self.assertIn("AUTO_NATIVE_UNSCOPED, deadline", self.harness)
        self.assertIn("AUTO_NATIVE_EXACT_REMOTE, deadline", self.harness)
        self.assertIn("AUTO_HURT if args.autoclicker_pvp", self.harness)
        self.assertIn("scoreboard players test CITarget taken 1 999999", self.harness)


if __name__ == "__main__":
    unittest.main()
