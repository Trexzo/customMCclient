"""V325 source-wiring regression for the real original M417 sentinel gate.

These static tests never authorize a hit or fabricate Minecraft/server
acceptance: only the full original Mojang 1.8.9 binary CI can do that.
"""
from pathlib import Path
import unittest

JAVA = (Path(__file__).resolve().parents[1] / "platform-1.8.9" / "src"
        / "main" / "java" / "dev" / "trexzo" / "custommc" / "platform"
        / "v1_8_9" / "Minecraft189RejoinTargetAcceptance.java")


class OriginalM417NovelUuidSourceWiring(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = JAVA.read_text(encoding="utf-8")
        start = cls.source.index("} else if (phase==3) {")
        finish = cls.source.index(
            "// Original A has a new real EntityPlayer instance", start)
        cls.phase = cls.source[start:finish]

    def test_positive_m417_requires_original_world_net_and_novel_uuids(self):
        self.assertIn(
            "&& m309FourSourceNativeSentinelsAdmitted(", self.phase)
        self.assertIn(
            "nativeNonPlayerBaseline, currentNativeNonplayers,", self.phase)
        self.assertIn("distinctNovelNativeUuids)) {", self.phase)
        self.assertNotIn(
            "sourceNonPlayerCount(e)>=nativeNonPlayerBaseline+4",
            self.phase)
        self.assertLess(
            self.phase.index("m309FourSourceNativeSentinelsAdmitted("),
            self.phase.index(
                "CUSTOMMC_189_REJOIN_REAL_SENTINEL_NATIVE_OBSERVED_PASS"))

    def test_both_native_witnesses_are_from_one_original_snapshot(self):
        self.assertIn(
            "currentNativeNonplayers = e == null", self.phase)
        self.assertIn("sourceNonPlayerCount(e);", self.phase)
        self.assertIn(
            "distinctNovelNativeUuids = e == null ? -1", self.phase)
        self.assertIn(
            "m273NovelNativeNonplayerCount(", self.phase)
        self.assertIn("nativeBaselineNonplayerUuids, e.ids, e.kinds",
                      self.phase)
        self.assertEqual(self.phase.count("m273NovelNativeNonplayerCount("), 1)
        self.assertEqual(self.phase.count("sourceNonPlayerCount(e)"), 1)

    def test_native_source_gate_remains_living_locked_b_and_opt_in(self):
        for witness in (
            'Boolean.getBoolean("custommc.acceptance.requireRejoinIndexReorder")',
            "!nativeSentinelObserved",
            "nativeNonPlayerBaseline >= 0",
            "e!=null && e.a==-1 && e.bOnlySelected()",
            "survivorUuid.equals(aura.lockedTargetUuid())",
        ):
            self.assertIn(witness, self.phase)

    def test_fail_closed_helper_uses_wide_delta_not_int_addition(self):
        body = self.source[
            self.source.index("static boolean m309FourSourceNativeSentinelsAdmitted("):
            self.source.index(
                "/** Read-only explanation of a genuine four-sentinel", 
                self.source.index("static boolean m309FourSourceNativeSentinelsAdmitted("))
        ]
        self.assertIn("baselineNonplayers >= 0", body)
        self.assertIn("currentNonplayers >= 0", body)
        self.assertIn("distinctNewNativeNonplayerUuids >= 4", body)
        self.assertIn(
            "((long) currentNonplayers - (long) baselineNonplayers) >= 4L",
            body)
        self.assertNotIn("baselineNonplayers+4", body)


if __name__ == "__main__":
    unittest.main()
