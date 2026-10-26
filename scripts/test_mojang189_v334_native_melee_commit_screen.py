"""V334: a reentrant native GUI must revoke the final automatic melee action.

Wiring-only guard: actual attack, remote UUID, hurt and server damage
remain mandatory in genuine original Mojang 1.8.9 CI.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
PLATFORM = ROOT / "platform-1.8.9" / "src" / "main" / "java" / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"


class V334FinalNativeScreenReadTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.bridge = (PLATFORM / "Minecraft189RuntimeBridge.java").read_text(
            encoding="utf-8")
        cls.host = (PLATFORM / "Minecraft189HostRuntime.java").read_text(
            encoding="utf-8")
        cls.veto = (PLATFORM / "Minecraft189NativeGuiCombatVeto.java").read_text(
            encoding="utf-8")
        cls.harness = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py"
                       ).read_text(encoding="utf-8")
        cls.method = cls.bridge.split(
            "public static synchronized void autoClick(", 1
        )[1].split("public static synchronized void publishTick(", 1)[0]

    def test_original_screen_veto_before_each_of_three_actual_melee_calls(self):
        code = self.method
        self.assertEqual(3, code.count(
            "vetoOriginalScreenAtMeleeDispatch(host, minecraft)"))
        self.assertEqual(2, code.count("minecraft.customMcAttackCrosshairEntity();"))
        self.assertEqual(1, code.count("minecraft.customMcClickMouse();"))
        trigger = code.index("triggerDispatchUuid.set(sourceUuid);")
        aura = code.index("auraDispatchUuid.set(ownerUuid);")
        auto = code.index("minecraft.customMcClickMouse();")
        for dispatched in (trigger, aura, auto):
            prior = code.rfind(
                "if (vetoOriginalScreenAtMeleeDispatch(host, minecraft))",
                0, dispatched)
            self.assertGreater(prior, 0)
            self.assertIn("return;", code[prior:dispatched])
            self.assertLess(dispatched - prior, 270)

    def test_native_identity_and_owner_are_verified_before_final_gate(self):
        code = self.method
        trigger = code.index("triggerDispatchUuid.set(sourceUuid);")
        trigger_check = code.rfind("vetoOriginalScreenAtMeleeDispatch", 0, trigger)
        self.assertLess(code.index("host.consumeTriggerDispatchUuid(sourceUuid)"),
                        trigger_check)
        aura = code.index("auraDispatchUuid.set(ownerUuid);")
        aura_check = code.rfind("vetoOriginalScreenAtMeleeDispatch", 0, aura)
        self.assertLess(code.index("acceptanceRemotePairOwnerForNativeProof"),
                        aura_check)
        self.assertLess(aura_check, aura)
        self.assertLess(code.index("host.shouldAutoClick("), trigger_check)

    def test_no_manual_input_or_original_cooldown_bypass(self):
        helper = self.bridge.split(
            "private static boolean vetoOriginalScreenAtMeleeDispatch(", 1
        )[1].split("public static synchronized void autoClick(", 1)[0]
        self.assertIn("Minecraft189NativeGuiCombatVeto.blocks(minecraft)", helper)
        self.assertIn("host.suspendSyntheticCombatForOriginalGui();", helper)
        self.assertIn("return true;", helper)
        self.assertIn("return false;", helper)
        for forbidden in ("leftClickCounter =", "customMcClickMouse();",
                          "customMcAttackCrosshairEntity();",
                          "customMcRightClickMouse();", "xdotool",
                          "setIngameFocus"):
            self.assertNotIn(forbidden, helper)
        self.assertIn('"ave".equals(minecraft.getClass().getName())', self.veto)
        self.assertIn("screen != Minecraft189ScaffoldScreenGate.Verdict.CLEAR",
                      self.veto)

    def test_source_uuid_native_controller_hurt_and_damage_gates_unmodified(self):
        for expected in (
            "AUTO_NATIVE_UNSCOPED, deadline",
            "AUTO_NATIVE_EXACT_REMOTE, deadline",
            "AUTO_HURT if args.autoclicker_pvp",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(expected, self.harness)


if __name__ == "__main__":
    unittest.main()
