"""Strict V60 original-controller position extraction; no fabricated game."""
import tempfile
import unittest
from pathlib import Path

from RunMojang189LocalhostTwoPlayerSmoke import (
    source_native_scaffold_position,
    latest_original_native_local_player_position,
)


PREFIX = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_KEYBOARD_WALK_ORIGINAL_SOURCE_POSITION"


class NativeKeyboardSourceTest(unittest.TestCase):
    def read(self, contents, step):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "client.log"
            log.write_bytes(contents)
            return source_native_scaffold_position(log, step)

    def test_true_native_original_positions_with_negative_spawn(self):
        contents = (PREFIX + b" step=1 x=-80.05 y=100.0 z=53.5\n" +
                    PREFIX + b" step=2 x=-79.04 y=100.0 z=53.52\n")
        self.assertEqual((-80.05, 100.0, 53.5), self.read(contents, 1))
        self.assertEqual((-79.04, 100.0, 53.52), self.read(contents, 2))

    def test_ignores_non_controller_and_other_step_sources(self):
        contents = (b"tp CIHost 77 100 0\n" +
                    PREFIX + b" step=3 x=77.0 y=100.0 z=0.5\n")
        with self.assertRaisesRegex(AssertionError, "missing/duplicate"):
            self.read(contents, 1)

    def test_duplicate_or_missing_native_marker_fails_closed(self):
        line = PREFIX + b" step=2 x=1.05 y=100.0 z=0.5\n"
        for contents in (b"", line * 2):
            with self.subTest(contents=contents):
                with self.assertRaises(AssertionError):
                    self.read(contents, 2)

    def test_nan_or_infinite_source_never_counts(self):
        for value in (b"NaN", b"Infinity", b"-Infinity"):
            with self.subTest(value=value):
                contents = PREFIX + b" step=1 x=" + value + b" y=100 z=.5\n"
                with self.assertRaises(AssertionError):
                    self.read(contents, 1)

    def test_live_native_position_is_latest_genuine_sample(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            log.write_bytes(
                b"CUSTOMMC_189_NATIVE_LOCAL_POS=x=516.05 y=100.0 z=19.5\n"
                b"CUSTOMMC_189_NATIVE_LOCAL_POS=x=516.92 y=100.0 z=19.5\n"
            )
            self.assertEqual((516.92, 100.0, 19.5),
                             latest_original_native_local_player_position(log))

    def test_live_native_position_does_not_read_teleport_commands(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            log.write_bytes(
                b"Teleported CIHost to 517.05,100,19.5\n"
                b"SERVER_SIMULATED_MOVEMENT=517.05\n"
            )
            self.assertIsNone(latest_original_native_local_player_position(log))

    def test_true_third_native_controller_position_is_distinct(self):
        contents = (PREFIX + b" step=1 x=-80.05 y=100.0 z=53.5\n"
                    + PREFIX + b" step=2 x=-79.01 y=100.0 z=53.5\n"
                    + PREFIX + b" step=3 x=-78.12 y=100.0 z=53.5\n")
        self.assertEqual((-78.12, 100.0, 53.5), self.read(contents, 3))
        with self.assertRaises(AssertionError):
            self.read(contents + PREFIX + b" step=3 x=-78.12 y=100.0 z=53.5\n", 3)

    def test_fourth_original_source_position_and_duplicates(self):
        contents = (PREFIX + b" step=1 x=13.05 y=100.0 z=8.5\n"
                    + PREFIX + b" step=2 x=14.02 y=100.0 z=8.5\n"
                    + PREFIX + b" step=3 x=15.00 y=100.0 z=8.5\n"
                    + PREFIX + b" step=4 x=16.01 y=100.0 z=8.5\n")
        self.assertEqual((16.01, 100.0, 8.5), self.read(contents, 4))
        with self.assertRaisesRegex(AssertionError, "missing/duplicate"):
            self.read(contents + PREFIX + b" step=4 x=16.01 y=100.0 z=8.5\n", 4)

    def test_step_must_be_a_genuine_four_position_mode(self):
        with self.assertRaises(ValueError):
            self.read(b"", 5)


if __name__ == "__main__":
    unittest.main()
