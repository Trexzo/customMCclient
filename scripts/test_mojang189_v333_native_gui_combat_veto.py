"""V333 native Mojang GUI must cancel synthetic combat; no controller bypass."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
GAME = ROOT / "platform-1.8.9" / "src" / "main" / "java" / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"


class V333NativeGuiCombatSourceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.bridge = (GAME / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
        cls.host = (GAME / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
        cls.veto = (GAME / "Minecraft189NativeGuiCombatVeto.java").read_text(encoding="utf-8")
        cls.harness = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
            encoding="utf-8"
        )

    def test_native_screen_veto_is_before_any_original_auto_clicker_arm(self):
        method = self.bridge.split(
            "public static synchronized void autoClick(", 1
        )[1].split("public static synchronized void publishTick(", 1)[0]
        guard = method.index("Minecraft189NativeGuiCombatVeto.blocks(minecraft)")
        revoke = method.index("host.suspendSyntheticCombatForOriginalGui()", guard)
        negative_control = method.index("custommc.acceptance.reportAutoClicker", revoke)
        melee = method.index("host.shouldAutoClick(", negative_control)
        self.assertLess(guard, revoke)
        self.assertLess(revoke, negative_control)
        self.assertLess(negative_control, melee)

    def test_open_native_screen_revokes_three_combat_owners_and_trigger_token(self):
        body = self.host.split(
            "void suspendSyntheticCombatForOriginalGui() {", 1
        )[1].split("\n    }", 1)[0]
        for required in (
            "requireOpen();",
            "++triggerDispatchAdmissionSerial;",
            "triggerDispatchLease.clear();",
            "featureCatalog.autoClicker().suspendForGui();",
            "featureCatalog.triggerBot().suspend();",
            "featureCatalog.killAura().suspend();",
        ):
            self.assertIn(required, body)

    def test_pinned_real_class_and_fail_shut_source_only(self):
        self.assertIn('"ave".equals(minecraft.getClass().getName())', self.veto)
        self.assertIn("Minecraft189ScaffoldScreenGate.inspect(minecraft)", self.veto)
        self.assertIn("screen != Minecraft189ScaffoldScreenGate.Verdict.CLEAR", self.veto)
        self.assertNotIn("customMcClickMouse()", self.veto)
        self.assertNotIn("leftClickCounter =", self.veto)

    def test_strict_remote_uuid_controller_hurt_server_damage_still_required(self):
        for required in (
            "AUTO_NATIVE_UNSCOPED, deadline",
            "AUTO_NATIVE_EXACT_REMOTE, deadline",
            "AUTO_HURT if args.autoclicker_pvp",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(required, self.harness)


if __name__ == "__main__":
    unittest.main()
