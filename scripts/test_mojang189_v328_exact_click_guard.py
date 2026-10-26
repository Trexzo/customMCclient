"""V328: original Minecraft 1.8.9 exact leftClickCounter read regression.

This test verifies source wiring ONLY. It never substitutes for the unchanged
real Mojang controller UUID, native hurt, or vanilla-server damage checks.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
GAME = ROOT / "platform-1.8.9" / "src" / "main" / "java" / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"


class V328ExactCounterEntryRead(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        transformer = (GAME / "Minecraft189ClassTransformer.java").read_text(encoding="utf-8")
        method_start = transformer.index(
            "final Minecraft189Mappings.MappedMethod clickMouse ="
        )
        method_end = transformer.index(
            "final Minecraft189Mappings.MappedMethod startGame =", method_start
        )
        cls.click_hook = transformer[method_start:method_end]
        cls.bridge = (GAME / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
        cls.acceptance = (GAME / "Minecraft189VanillaMeleeAcceptance.java").read_text(encoding="utf-8")
        cls.harness = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
            encoding="utf-8"
        )

    def test_genuine_click_method_and_exact_mapping_not_first_arbitrary_int(self):
        hook = self.click_hook
        self.assertIn("Minecraft189Mappings.MINECRAFT_CLICK_MOUSE", hook)
        self.assertIn("clickMouse.obfuscatedName().equals(name)", hook)
        self.assertIn("clickMouse.descriptor().equals(descriptor)", hook)
        self.assertIn("MINECRAFT_LEFT_CLICK_COUNTER", hook)
        self.assertIn("counter.obfuscatedName()", hook)
        self.assertIn("counter.descriptor()", hook)
        self.assertIn("Opcodes.GETFIELD", hook)
        self.assertNotIn("firstNativeClickCounter", hook)
        self.assertNotIn('"I".equals(fieldDescriptor)', hook)

    def test_observe_only_same_entry_no_vanilla_counter_mutation(self):
        hook = self.click_hook
        self.assertEqual(hook.count('"acceptanceVanillaClickGuard"'), 1)
        self.assertLess(hook.index('"acceptanceVanillaClick"'), hook.index('"acceptanceVanillaClickGuard"'))
        self.assertIn('"(I)V", false', hook)
        self.assertNotIn("Opcodes.PUTFIELD", hook)
        self.assertNotIn("customMcClickMouse", hook)
        self.assertIn("Minecraft189VanillaMeleeAcceptance.onNativeClickGuard(", self.bridge)

    def test_mojang_controller_and_server_damage_stay_authoritative(self):
        self.assertIn("syntheticCall && Boolean.getBoolean(", self.acceptance)
        self.assertIn('"custommc.acceptance.requireAutoClickerExactRemoteUuid"', self.acceptance)
        self.assertIn("AUTO_NATIVE_UNSCOPED, deadline", self.harness)
        self.assertIn("AUTO_NATIVE_EXACT_REMOTE, deadline", self.harness)
        self.assertIn("AUTO_HURT if args.autoclicker_pvp", self.harness)
        self.assertIn("scoreboard players test CITarget taken 1 999999", self.harness)
        self.assertNotIn("AUTOCLICKER_MOJANG_LEFT_CLICK_COUNTER_READY=YES", self.harness)


if __name__ == "__main__":
    unittest.main()
