"""V338 source regression: exact original HitSelect owner at each melee call."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
NATIVE = ROOT / "platform-1.8.9" / "src" / "main" / "java" / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"
BRIDGE = (NATIVE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
HIT = (NATIVE / "Minecraft189HitSelectModule.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8"
)


class V338NativeHitSelectOwnerTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.bridge = BRIDGE.split(
            "public static synchronized void autoClick(", 1
        )[1].split("public static synchronized void publishTick(", 1)[0]

    def test_early_observation_binds_same_original_uuid_to_all_three_calls(self):
        body = self.bridge
        early = body.index("final UUID hitSelectInitialUuid")
        scheduling = body.index("host.shouldAutoClick(")
        self.assertLess(early, scheduling)
        self.assertIn("host.worldEntityUuidState().snapshot().at(playerIndex)", body)
        self.assertEqual(3, body.count(
            "permitsHitSelectAtNativeDispatch("))
        self.assertEqual(2, body.count("minecraft.customMcAttackCrosshairEntity();"))
        self.assertEqual(1, body.count("minecraft.customMcClickMouse();"))
        for call in ("triggerDispatchUuid.set(sourceUuid);",
                     "auraDispatchUuid.set(ownerUuid);",
                     "minecraft.customMcClickMouse();"):
            dispatched = body.index(call)
            check = body.rfind("if (!permitsHitSelectAtNativeDispatch(", 0, dispatched)
            self.assertGreater(check, scheduling)
            self.assertLess(dispatched - check, 260)
            self.assertIn("hitSelectInitialUuid", body[check:dispatched])
            self.assertIn("return;", body[check:dispatched])

    def test_final_gate_safeguards_actual_native_crosshair_and_original_world(self):
        helper = BRIDGE.split(
            "private static boolean permitsHitSelectAtNativeDispatch(", 1
        )[1].split("public static synchronized void autoClick(", 1)[0]
        self.assertIn("if (!hitSelect.active()) return true", helper)
        self.assertIn("originalRay.customMcCrosshairPlayerHit()", helper)
        self.assertIn("originalRay.customMcCrosshairPlayerIndex() != originalIndex",
                      helper)
        self.assertIn("host.worldEntityUuidState().snapshot()", helper)
        self.assertIn("host.worldEntityKindState().snapshot()", helper)
        self.assertIn("host.worldEntityCombatState().snapshot()", helper)
        self.assertIn("hitSelect.permitsOriginalOwner(", helper)
        self.assertNotIn("customMcClickMouse();", helper)
        self.assertNotIn("customMcAttackCrosshairEntity();", helper)
        self.assertNotIn("leftClickCounter =", helper)
        self.assertIn("selectedOriginalUuid.equals(", HIT)
        self.assertIn("currentKinds.localPlayer(verifiedCrosshairIndex)", HIT)

    def test_exact_original_controller_hurt_and_server_damage_never_weakened(self):
        for gate in ("AUTO_NATIVE_UNSCOPED, deadline",
                     "AUTO_NATIVE_EXACT_REMOTE, deadline",
                     "AUTO_HURT if args.autoclicker_pvp",
                     "scoreboard players test CITarget taken 1 999999"):
            self.assertIn(gate, HARNESS)
        self.assertNotIn("V338_HITSELECT_PASS", HARNESS)


if __name__ == "__main__":
    unittest.main()
