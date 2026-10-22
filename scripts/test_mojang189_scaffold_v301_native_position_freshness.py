"""V301 original 1.8.9 V16 post-teleport native log freshness.

File-only regressions: real Minecraft's position emitter and World/TP
remain the sole native authorities in pinned original 78-stage acceptance.
"""
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase
from unittest.mock import patch

import RunMojang189SingleplayerSmoke as smoke


def native(x, y, z):
    return ("[Client thread/INFO] CUSTOMMC_189_NATIVE_LOCAL_POS="
            "x={} y={} z={}\n".format(x, y, z)).encode("ascii")


class OriginalV16FreshPositionSourceTests(TestCase):
    def source(self, earlier, later):
        folder = TemporaryDirectory()
        self.addCleanup(folder.cleanup)
        log = Path(folder.name) / "original-mojang.log"
        log.write_bytes(earlier)
        cursor = log.stat().st_size
        with log.open("ab") as out:
            out.write(later)
        return log, cursor

    def test_real_old_ground_samples_must_not_qualify_new_world_teleport(self):
        old = native(.5, 100, .5) * 3
        log, cursor = self.source(old, native(.5, 120, .5))
        self.assertEqual(4, len(smoke.native_safe_walk_positions(log)))
        self.assertEqual([(0.5, 120.0, 0.5)],
                         smoke.native_safe_walk_positions(log, since=cursor))

    def test_three_separate_fresh_real_source_native_ticks_remain_necessary(self):
        old = native(.5, 100, .5) * 3
        fresh = native(.5, 100, .5) * 3
        log, cursor = self.source(old, fresh)
        self.assertEqual(3, len(smoke.native_safe_walk_positions(log, since=cursor)))
        self.assertEqual([(0.5, 100.0, 0.5)] * 3,
                         smoke.native_safe_walk_positions(log, since=cursor)[-3:])

    def test_last_two_fresh_native_ticks_do_not_promote_stale_third(self):
        log, cursor = self.source(
            native(.5, 100, .5) * 10, native(.5, 100, .5) * 2)
        self.assertEqual(2, len(smoke.native_safe_walk_positions(log, since=cursor)))

    def test_incomplete_last_native_sample_is_not_a_real_tick(self):
        log, cursor = self.source(
            native(.5, 100, .5), native(.5, 100, .5)
            + b"CUSTOMMC_189_NATIVE_LOCAL_POS=x=0.5 y=100 z=")
        self.assertEqual(1, len(smoke.native_safe_walk_positions(log, since=cursor)))
        with log.open("ab") as out:
            out.write(b"0.5\n")
        self.assertEqual(2, len(smoke.native_safe_walk_positions(log, since=cursor)))

    def test_fresh_text_without_any_source_position_never_counts(self):
        log, cursor = self.source(native(.5, 100, .5) * 3,
                                  b"[CHAT] Teleported CIProbe to 0.5, 100.0, 0.5\n")
        self.assertEqual([], smoke.native_safe_walk_positions(log, since=cursor))

    def test_fresh_native_x_y_z_preserve_strict_parse_and_source_order(self):
        log, cursor = self.source(
            native(.5, 100, .5),
            native(.6, 100, .5) + native(.5, 99, .5))
        self.assertEqual([(0.6, 100.0, 0.5), (0.5, 99.0, 0.5)],
                         smoke.native_safe_walk_positions(log, since=cursor))

    def test_negative_and_nonexistent_source_cursors_rejected(self):
        log, cursor = self.source(b"", native(.5, 100, .5))
        with self.assertRaisesRegex(AssertionError, "invalid original native"):
            smoke.native_safe_walk_positions(log, since=-1)
        with self.assertRaisesRegex(AssertionError, "cursor exceeds log"):
            smoke.native_safe_walk_positions(log, since=cursor + 9999)

    def test_malformed_fresh_position_line_hard_fails_not_skipped(self):
        log, cursor = self.source(b"", b"CUSTOMMC_189_NATIVE_LOCAL_POS="
                                   b"x=0.5 y=100\n")
        with self.assertRaisesRegex(AssertionError, "malformed native SafeWalk"):
            smoke.native_safe_walk_positions(log, since=cursor)

    def test_old_default_native_position_reader_remains_backwards_compatible(self):
        old = native(8.5, 120, 8.5)
        fresh = native(.5, 100, .5)
        log, cursor = self.source(old, fresh)
        self.assertEqual([(8.5, 120.0, 8.5), (0.5, 100.0, 0.5)],
                         smoke.native_safe_walk_positions(log))
        self.assertEqual([(0.5, 100.0, 0.5)],
                         smoke.native_safe_walk_positions(log, since=cursor))

    def test_v16_runner_cursor_occurs_after_tp_source_ack_and_before_world_proof(self):
        script = Path(smoke.__file__).read_text(encoding="utf-8")
        first = script.index("            if native_scaffold_world:")
        last = script.index("            if native_scaffold_tick:", first)
        body = script[first:last]
        tp = body.index('"/tp CIProbe 0 100 0 0 0"')
        cursor = body.index("v16_post_teleport_native_cursor =")
        world = body.index('"/testforblock 0 99 0 stone"')
        samples = body.index("native_safe_walk_positions(")
        witness = body.index("CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_SUPPORT_GROUNDED=YES")
        self.assertLess(tp, cursor)
        self.assertLess(cursor, world)
        self.assertLess(world, samples)
        self.assertLess(samples, witness)
        self.assertIn("since=v16_post_teleport_native_cursor", body)

    def test_v16_expected_original_testforblock_reply_matches_actual_native_format(self):
        source = Path(smoke.__file__).read_text(encoding="utf-8")
        first = source.index("            if native_scaffold_world:")
        last = source.index("            if native_scaffold_tick:", first)
        body = source[first:last]
        self.assertIn(
            'b"[CHAT] Successfully found the block at 0,99,0."', body)
        self.assertNotIn(
            'b"[CHAT] Successfully found the block at 0, 99, 0."', body)
        self.assertIn('"/testforblock 0 99 0 stone"', body)

    def test_fresh_genuine_original_coordinate_reply_is_required_not_stale_chat(self):
        with TemporaryDirectory() as td:
            log = Path(td) / "actual-original-client.log"
            log.write_bytes(
                b"[CHAT] Successfully found the block at 0,99,0.\\n")
            actual = b"[CHAT] Successfully found the block at 0,99,0."
            def actual_vanilla_source(_process, path, _window, command):
                self.assertEqual("/testforblock 0 99 0 stone", command)
                with path.open("ab") as output:
                    output.write(actual + b"\\n")
            with patch.object(smoke, "send_command",
                              side_effect=actual_vanilla_source), patch.object(
                    smoke, "require_running"):
                smoke.send_verified_original_chat_reply(
                    object(), log, "X11",
                    "/testforblock 0 99 0 stone", actual)

    def test_historical_spaced_coordinate_literal_would_reject_real_reply(self):
        with TemporaryDirectory() as td:
            log = Path(td) / "original-native.log"
            log.write_bytes(b"")
            def actual_original(_p, file, _window, _cmd):
                with file.open("ab") as out:
                    out.write(
                        b"[CHAT] Successfully found the block at 0,99,0.\\n")
            with patch.object(smoke, "send_command",
                              side_effect=actual_original), patch.object(
                    smoke, "require_running"), patch.object(
                    smoke.time, "monotonic", side_effect=[0, 9]):
                with self.assertRaisesRegex(AssertionError,
                                            "missing new source integrated-server reply"):
                    smoke.send_verified_original_chat_reply(
                        object(), log, "X11", "/testforblock 0 99 0 stone",
                        b"[CHAT] Successfully found the block at 0, 99, 0.")

    def test_v16_does_not_weaken_original_native_stability_or_time_bound(self):
        script = Path(smoke.__file__).read_text(encoding="utf-8")
        first = script.index("            if native_scaffold_world:")
        last = script.index("            if native_scaffold_tick:", first)
        body = script[first:last]
        for required in (
            "len(actual) >= 3", "abs(x - 0.5) < 0.3",
            "abs(y - 100.0) < 0.2", "abs(z - 0.5) < 0.3",
            "stable_start < 10", "SCAFFOLD_WORLD_AIR_PASS=YES",
            "SCAFFOLD_WORLD_SOLID_PASS=YES",
            "SCAFFOLD_WORLD_REVERTED_AIR_PASS=YES",
        ):
            with self.subTest(required=required):
                self.assertIn(required, body)
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_SUPPORT_GROUNDED=YES",
                         script[script.index("def native_safe_walk_positions("):
                                script.index("def original_edge_grounding_stable(")])


if __name__ == "__main__":
    import unittest
    unittest.main()
