"""V318 source-only diagnosis for original 1.8.9 FOV and Wall negatives.

No fake PASS, no changing game targets, scoreboard or strict acceptance counts.
"""
import ast
from pathlib import Path
import tempfile
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    v318_original_negative_native_source as native_diagnostics,
)


PYTHON = Path(__file__).with_name("RunMojang189LocalhostTwoPlayerSmoke.py")
JAVA = (Path(__file__).parents[1] / "platform-1.8.9" / "src" / "main"
        / "java" / "dev" / "trexzo" / "custommc" / "platform"
        / "v1_8_9" / "Minecraft189RuntimeBridge.java")


class OriginalNegativeNativeDiagnosticTests(unittest.TestCase):
    def test_genuine_fov_source_lines_only(self):
        with tempfile.TemporaryDirectory() as root:
            f = Path(root) / "original-jvm.log"
            f.write_bytes(
                b"CUSTOMMC_189_FOV_ORIGINAL_SOURCE_OBSERVATION tick=160 nativePlayerYaw=0.0\n"
                b"CUSTOMMC_189_FOV_ORIGINAL_SOURCE_OBSERVATION tick=240 nativePlayerYaw=-180.0\n"
                b"CUSTOMMC_OFFICIAL_189_REAL_PLAYER_FOV_SERVER_NO_DAMAGE_PASS=YES\n"
                b"CUSTOMMC_189_WALL_LIFECYCLE_NATIVE_SOURCE_DIAG phase=1\n")
            result = native_diagnostics(f, "fov")
            self.assertEqual(len(result), 2)
            self.assertIn("tick=160", result[0])
            self.assertIn("tick=240", result[1])
            self.assertNotIn("PASS=YES", str(result))
            self.assertNotIn("WALL_LIFECYCLE", str(result))

    def test_phase_one_original_wall_source_line_not_a_pass(self):
        with tempfile.TemporaryDirectory() as root:
            f = Path(root) / "original-jvm.log"
            f.write_bytes(
                b"CUSTOMMC_189_WALL_LIFECYCLE_NORMAL_DISABLE_PASS=YES\n"
                b"CUSTOMMC_189_WALL_LIFECYCLE_NATIVE_SOURCE_DIAG "
                b"phase=1 selectedConsecutiveSourceTicks=0\n"
                b"CUSTOMMC_189_WALL_LIFECYCLE_NATIVE_SOURCE_DIAG "
                b"phase=1 selectedConsecutiveSourceTicks=3\n")
            result = native_diagnostics(f, "wall")
            self.assertEqual(len(result), 2)
            self.assertIn("selectedConsecutiveSourceTicks=3", result[-1])
            self.assertNotIn("PASS=YES", str(result))

    def test_missing_and_symlinked_original_log_provide_no_evidence(self):
        with tempfile.TemporaryDirectory() as root:
            base = Path(root)
            original = base / "native.txt"
            original.write_bytes(b"CUSTOMMC_189_FOV_ORIGINAL_SOURCE_OBSERVATION tick=240\n")
            self.assertEqual(native_diagnostics(base / "not-created", "fov"), [])
            alias = base / "alias.txt"
            alias.symlink_to(original)
            self.assertEqual(native_diagnostics(alias, "fov"), [])

    def test_old_truncated_partial_log_line_is_excluded(self):
        with tempfile.TemporaryDirectory() as root:
            p = Path(root) / "original.log"
            p.write_bytes(
                b"X" * 250 + b"\n"
                b"CUSTOMMC_189_WALL_LIFECYCLE_NATIVE_SOURCE_DIAG phase=1\n"
                b"CUSTOMMC_189_WALL_LIFECYCLE_NATIVE_SOURCE_DIAG phase=2")
            source = native_diagnostics(p, "wall", limit=128)
            self.assertEqual(len(source), 1)
            self.assertIn("phase=1", source[0])
            self.assertNotIn("phase=2", source[0])

    def test_unknown_or_oversized_read_is_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            p = Path(root) / "x.log"
            p.write_bytes(b"")
            for scene, limit in (("not-fov-or-wall", 1000),
                                 ("fov", 127), ("wall", 1048577),
                                 ("wall", True), ("fov", -4), ("wall", 0)):
                with self.subTest(scene=scene, limit=limit):
                    with self.assertRaises(ValueError):
                        native_diagnostics(p, scene, limit=limit)

    def test_output_capped_at_24_genuine_original_source_lines(self):
        with tempfile.TemporaryDirectory() as root:
            p = Path(root) / "original.log"
            p.write_bytes(b"".join(
                b"CUSTOMMC_189_WALL_LIFECYCLE_NATIVE_SOURCE_DIAG phase=1 n="
                + str(n).encode() + b"\n" for n in range(300)))
            result = native_diagnostics(p, "wall")
            self.assertEqual(len(result), 24)
            self.assertTrue(result[0].endswith("n=276"))
            self.assertTrue(result[-1].endswith("n=299"))

    def test_native_wall_phase_one_heartbeat_is_read_only(self):
        source = JAVA.read_text(encoding="utf-8")
        self.assertIn("acceptanceWallLifecyclePhase == 1", source)
        self.assertIn("acceptanceWallLifecyclePhase == 2", source)
        self.assertIn("selectedConsecutiveSourceTicks=", source)
        self.assertIn("wallCheckNativeActive=", source)
        self.assertIn("++acceptanceWallLifecycleSourceEvidenceTicks % 80 == 0",
                      source)
        self.assertIn("++acceptanceWallLifecycleDisabledSelectionTicks >= 20",
                      source)
        self.assertIn("++acceptanceWallLifecycleReenabledVetoTicks >= 30",
                      source)

    def test_original_fov_server_zero_damage_is_mandatory_and_rethrown(self):
        s = PYTHON.read_text("utf-8")
        section = s[s.index("        if args.fov_veto:"):
                    s.index("        if args.range_veto:")]
        self.assertIn("wait_for(host, host_log, FOV_VETO, deadline", section)
        self.assertIn('"scoreboard players test CITarget taken 0 0"', section)
        self.assertIn('expected=b"score 0 is in range 0 to 0"', section)
        self.assertIn('v318_original_negative_native_source(', section)
        self.assertIn('host_log, "fov"', section)
        self.assertTrue(
            any(isinstance(node, ast.Raise) and node.exc is None
                for node in ast.walk(ast.parse(section_wrap(section)))))
        self.assertLess(section.index('"scoreboard players test CITarget taken 0 0"',
                        section.index("wait_for(host, host_log, FOV_VETO")),
                        section.index("REAL_PLAYER_FOV_SERVER_NO_DAMAGE_PASS"))

    def test_original_wall_twenty_selection_and_thirty_clear_still_strict(self):
        s = PYTHON.read_text("utf-8")
        section = s[s.index("            if args.wall_lifecycle:"):
                    s.index("            if args.wall_recovery or args.wall_lifecycle:")]
        self.assertIn("WALL_LIFECYCLE_DISABLED, deadline", section)
        self.assertIn("WALL_LIFECYCLE_SELECTED, deadline", section)
        self.assertIn('host_log, "wall"', section)
        self.assertIn("WALL_LIFECYCLE_REENABLED, deadline", section)
        self.assertIn("WALL_LIFECYCLE_VETO, deadline", section)
        self.assertLess(section.index("WALL_LIFECYCLE_SELECTED, deadline"),
                        section.index("WALL_LIFECYCLE_REENABLED, deadline"))
        self.assertTrue(
            any(isinstance(node, ast.Raise) and node.exc is None
                for node in ast.walk(ast.parse(section_wrap(section)))))

    def test_diagnostic_never_supplies_original_pass_or_gameplay_mutation(self):
        s = PYTHON.read_text("utf-8")
        block = s[s.index("def v318_original_negative_native_source("):
                  s.index("def check_process(", s.index(
                      "def v318_original_negative_native_source("))]
        for forbidden in ("PASS=YES", "send_server_command(", "attackEntity",
                          "host.featureCatalog", "targetRotationState"):
            self.assertNotIn(forbidden, block)
        self.assertIn("return result[-24:]", block)


def section_wrap(block):
    """Preserve indentation for AST analysis without needing a live game."""
    return "def original_case():\n" + block


if __name__ == "__main__":
    unittest.main()
