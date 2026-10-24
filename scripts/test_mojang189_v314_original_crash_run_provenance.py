"""V314 original game crash diagnosis must never hide the actual failure."""
import ast
import os
from pathlib import Path
import tempfile
import unittest

from RunMojang189SingleplayerSmoke import v313_original_crash_report_header


def make_report(directory, text=b"java.lang.StackOverflowError\n", stamp=5000):
    root = Path(directory) / "crash-reports"
    root.mkdir(exist_ok=True)
    file = root / "crash-original-1.txt"
    file.write_bytes(text)
    os.utime(file, ns=(stamp, stamp))
    return file


class V314CurrentOriginalCrashTests(unittest.TestCase):
    def test_same_current_original_process_report_is_visible(self):
        with tempfile.TemporaryDirectory() as home:
            make_report(home, stamp=5_000_000)
            evidence = v313_original_crash_report_header(
                home, started_after_ns=4_000_000)
            self.assertIn("java.lang.StackOverflowError", evidence)
            self.assertNotIn("PASS=YES", evidence)

    def test_previous_original_game_crash_is_never_new_evidence(self):
        with tempfile.TemporaryDirectory() as home:
            make_report(home, stamp=4_000_000)
            self.assertIsNone(v313_original_crash_report_header(
                home, started_after_ns=4_000_001))

    def test_exact_start_time_report_is_accepted(self):
        with tempfile.TemporaryDirectory() as home:
            make_report(home, stamp=4_000_000)
            self.assertIsNotNone(v313_original_crash_report_header(
                home, started_after_ns=4_000_000))

    def test_newest_stale_report_cannot_override_current_report(self):
        with tempfile.TemporaryDirectory() as home:
            p = make_report(home, b"fresh-current\n", stamp=20_000)
            old = p.parent / "crash-old.txt"
            old.write_bytes(b"stale-previous\n")
            os.utime(old, ns=(10_000, 10_000))
            evidence = v313_original_crash_report_header(
                home, started_after_ns=15_000)
            self.assertIn("fresh-current", evidence)
            self.assertNotIn("stale-previous", evidence)

    def test_all_stale_reports_fail_closed_without_source(self):
        with tempfile.TemporaryDirectory() as home:
            root = Path(home) / "crash-reports"
            make_report(home, b"stale\n", stamp=100)
            p = root / "crash-stale2.txt"
            p.write_bytes(b"another stale\n")
            os.utime(p, ns=(200, 200))
            self.assertIsNone(v313_original_crash_report_header(
                home, started_after_ns=201))

    def test_symlink_crash_report_directory_cannot_escape_game_root(self):
        with tempfile.TemporaryDirectory() as home:
            game = Path(home) / "game"
            game.mkdir()
            external = Path(home) / "external"
            external.mkdir()
            make_report(external, b"external-sentinel", stamp=90_000)
            (game / "crash-reports").symlink_to(
                external / "crash-reports", target_is_directory=True)
            self.assertIsNone(v313_original_crash_report_header(
                game, started_after_ns=1))

    def test_bad_start_cursors_are_refused(self):
        with tempfile.TemporaryDirectory() as home:
            for value in (-1, False, True, "1", 1.0, [], {}):
                with self.subTest(value=value):
                    with self.assertRaises(ValueError):
                        v313_original_crash_report_header(
                            home, started_after_ns=value)

    def test_current_failure_reads_only_a_bounded_original_header(self):
        with tempfile.TemporaryDirectory() as home:
            make_report(home, b"first-native-failure\n"
                        + b"R" * 2_000_000, stamp=9999)
            evidence = v313_original_crash_report_header(
                home, max_bytes=128, started_after_ns=5000)
            self.assertIn("first-native-failure", evidence)
            self.assertLessEqual(len(evidence), 180)
            self.assertNotIn("PASS=YES", evidence)

    def test_actual_run_scope_uses_game_argument_not_undefined_cli_args(self):
        path = Path(__file__).with_name("RunMojang189SingleplayerSmoke.py")
        source = path.read_text(encoding="utf-8")
        tree = ast.parse(source)
        fn = next(node for node in tree.body
                  if isinstance(node, ast.FunctionDef) and node.name == "run")
        self.assertIn("game", [arg.arg for arg in fn.args.args])
        calls = [node for node in ast.walk(fn) if isinstance(node, ast.Call)
                 and isinstance(node.func, ast.Name)
                 and node.func.id == "v313_original_crash_report_header"]
        self.assertEqual(len(calls), 1)
        self.assertIsInstance(calls[0].args[0], ast.Name)
        self.assertEqual(calls[0].args[0].id, "game")
        self.assertEqual(len(calls[0].keywords), 1)
        self.assertEqual(calls[0].keywords[0].arg, "started_after_ns")
        self.assertIn("v314_native_process_started_ns = time.time_ns()", source)

    def test_existing_original_failure_is_rethrown_after_diagnostics(self):
        path = Path(__file__).with_name("RunMojang189SingleplayerSmoke.py")
        source = path.read_text(encoding="utf-8")
        tree = ast.parse(source)
        fn = next(node for node in tree.body
                  if isinstance(node, ast.FunctionDef) and node.name == "run")
        hooks = [node for node in ast.walk(fn)
                 if isinstance(node, ast.ExceptHandler)
                 and node.type is not None
                 and "BaseException" in (ast.get_source_segment(
                     source, node.type) or "")]
        self.assertEqual(len(hooks), 1)
        block = ast.get_source_segment(source, hooks[0])
        self.assertIn("V313_ORIGINAL_MOJANG_CRASH_HEADER_START", block)
        self.assertTrue(any(isinstance(n, ast.Raise) and n.exc is None
                            for n in ast.walk(hooks[0])))
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_LIVE_WORLD_TICKS_PASS=YES",
                         block)


if __name__ == "__main__":
    unittest.main()
