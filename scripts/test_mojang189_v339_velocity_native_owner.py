"""V339: source original Minecraft Velocity never modifies other entities.

Only the dedicated source-local 'bew' receives the existing velocity policy.
This checks wiring, not actual native server combat or damage authority.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "platform-1.8.9" / "src" / "main" / "java" / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"
TEST = ROOT / "platform-1.8.9" / "src" / "test" / "java" / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"
TRANSFORMER = (SRC / "Minecraft189ClassTransformer.java").read_text(encoding="utf-8")
BRIDGE = (SRC / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
MAPPINGS = (SRC / "Minecraft189Mappings.java").read_text(encoding="utf-8")
MAPPED_TEST = (TEST / "Minecraft189MappedHostTransformationTest.java").read_text(encoding="utf-8")


class VelocityNativeOwnerV339(unittest.TestCase):
    def test_original_living_base_override_propagates_exact_source_receiver_on_all_axes(self):
        code = TRANSFORMER.split("private static void addVelocityKnockBackOverride(", 1)[1].split(
            "private static byte[] transformItemStack(", 1
        )[0]
        self.assertEqual(3, code.count('"(Ljava/lang/Object;DD)D"'))
        self.assertEqual(2, code.count('"adjustVelocityHorizontalOriginalOwner"'))
        self.assertEqual(1, code.count('"adjustVelocityVerticalOriginalOwner"'))
        self.assertEqual(3, code.count(
            "// The inherited pr.knockBack method also runs for remote players,"))
        self.assertNotIn('"adjustVelocityHorizontal",', code)
        self.assertNotIn('"adjustVelocityVertical",', code)
        self.assertIn("Opcodes.INVOKESPECIAL", code)
        self.assertIn("knockBack.descriptor()", code)

    def test_nonlocal_guard_is_source_exact_not_nearest_target_or_uuid(self):
        code = BRIDGE.split("static boolean velocityOriginalLocalOwner(", 1)[1].split(
            "public static synchronized double adjustVelocityHorizontal(", 1
        )[0]
        self.assertIn("ENTITY_PLAYER_SP", code)
        self.assertIn(".equals(owner.getClass().getName())", code)
        self.assertIn("return !velocityOriginalLocalOwner(originalKnockbackOwner)", code)
        self.assertIn("? after : adjustVelocityHorizontal(before, after)", code)
        self.assertIn("? after : adjustVelocityVertical(before, after)", code)
        self.assertIn('"bew",', MAPPINGS)
        for forbidden in ("attackEntity(", "customMcSetMotionX(", "leftClickCounter",
                          "getNearestPlayer", "getDisplayName"):
            self.assertNotIn(forbidden, code)

    def test_transformed_java_fixture_requires_remote_player_and_mob_vanilla(self):
        self.assertIn('new String[]{"pr", "bet"}', MAPPED_TEST)
        self.assertIn('"nonlocal " + nonlocalClass + " X is vanilla"', MAPPED_TEST)
        self.assertIn('"nonlocal " + nonlocalClass + " Y is vanilla"', MAPPED_TEST)
        self.assertIn('"nonlocal " + nonlocalClass + " Z is vanilla"', MAPPED_TEST)
        # Existing local mocked 'bew' proof is retained, not swapped out.
        self.assertIn('runtime.moduleController()', MAPPED_TEST)
        self.assertIn('knockBack.invoke(', MAPPED_TEST)
        self.assertIn('playerClass.getField("v")', MAPPED_TEST)


if __name__ == "__main__":
    unittest.main()
