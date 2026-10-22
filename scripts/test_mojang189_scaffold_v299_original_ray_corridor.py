"""V299 genuine original 1.8.9 ray-corridor source setup regressions.

No mocked response is considered real Minecraft World or a valid UP-face
raytrace; only full pinned Mojang GUI/server proves the unchanged V19 gate.
"""
import ast
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase
from unittest.mock import patch

import RunMojang189SingleplayerSmoke as runner


class OriginalNativeV19UnobstructedRayCorridorTests(TestCase):
    def run_fixture(self, acknowledgments):
        with TemporaryDirectory() as td:
            log = Path(td) / "genuine-original-client.log"
            log.write_bytes(b"[CHAT] 1 blocks filled\n")
            events = []
            iterator = iter(acknowledgments)

            def source_chat(_p, source_log, _w, command):
                events.append(("original-command", command))
                reply = next(iterator)
                if reply is not None:
                    with source_log.open("ab") as stream:
                        stream.write(reply)

            def strict_source_fill(_p, _log, _w, command):
                events.append(("mandatory-positive-stone", command))

            with patch.object(runner, "send_command", side_effect=source_chat), patch.object(
                    runner, "send_verified_fill",
                    side_effect=strict_source_fill), patch.object(
                    runner, "require_running"):
                runner.prepare_verified_original_scaffold_raytrace_support(
                    object(), log, "native-x11")
            return events

    def test_both_original_air_replies_precede_mandatory_changed_stone(self):
        events = self.run_fixture((
            b"[CHAT] 1 blocks filled\n",
            b"[CHAT] 3 blocks filled\n",
        ))
        self.assertEqual(events, [
            ("original-command", "/fill 0 99 0 0 99 0 air"),
            ("original-command", "/fill 0 100 0 0 102 0 air"),
            ("mandatory-positive-stone", "/fill 0 99 0 0 99 0 stone"),
        ])

    def test_real_source_corridor_can_already_be_air_without_stone_waiver(self):
        self.assertEqual(self.run_fixture((
            b"[CHAT] No blocks filled\n",
            b"[CHAT] No blocks filled\n",
        ))[-1], ("mandatory-positive-stone", "/fill 0 99 0 0 99 0 stone"))

    def test_real_source_first_air_preclear_still_must_have_fresh_reply(self):
        with TemporaryDirectory() as td:
            log = Path(td) / "original.log"
            log.write_bytes(b"[CHAT] No blocks filled\n")
            with patch.object(runner, "send_command"), patch.object(
                    runner, "send_verified_fill") as fill, patch.object(
                    runner, "require_running"), patch.object(
                    runner.time, "monotonic", side_effect=[0,9]):
                with self.assertRaisesRegex(AssertionError,
                        "missing fresh vanilla V19 raytrace support preclear reply"):
                    runner.prepare_verified_original_scaffold_raytrace_support(
                        object(), log, "X11")
                fill.assert_not_called()

    def test_second_original_air_corridor_is_fail_closed_if_reply_missing(self):
        with TemporaryDirectory() as td:
            log = Path(td) / "original.log"
            log.write_bytes(b"[CHAT] No blocks filled\n")
            calls = []
            def send(_p, source_log, _w, command):
                calls.append(command)
                if len(calls) == 1:
                    with source_log.open("ab") as stream:
                        stream.write(b"[CHAT] No blocks filled\n")
            with patch.object(runner, "send_command", side_effect=send), patch.object(
                    runner, "send_verified_fill") as fill, patch.object(
                    runner, "require_running"), patch.object(
                    runner.time, "monotonic", side_effect=[0,0,0,9]):
                with self.assertRaisesRegex(AssertionError,
                        "missing fresh vanilla V19 air-column preclear reply"):
                    runner.prepare_verified_original_scaffold_raytrace_support(
                        object(), log, "X11")
                self.assertEqual(2, len(calls))
                fill.assert_not_called()

    def test_original_second_preclear_world_error_prevents_mandatory_stone(self):
        for error in (
            b"[CHAT] Cannot place blocks outside of the world\n",
            b"[CHAT] Too many blocks in the specified area\n",
        ):
            with self.subTest(error=error):
                with patch.object(runner, "send_command") as chat:
                    with TemporaryDirectory() as td:
                        log = Path(td) / "original.log"
                        log.write_bytes(b"")
                        count = [0]
                        def send(_p, source_log, _w, _cmd):
                            count[0] += 1
                            with source_log.open("ab") as stream:
                                stream.write(b"[CHAT] No blocks filled\n"
                                             if count[0] == 1 else error)
                        chat.side_effect=send
                        with patch.object(runner, "send_verified_fill") as fill, patch.object(
                                runner, "require_running"):
                            with self.assertRaises(AssertionError):
                                runner.prepare_verified_original_scaffold_raytrace_support(
                                    object(), log, "X11")
                            fill.assert_not_called()

    def test_original_v19_corridor_covers_exact_native_ray_initial_voxels(self):
        # Existing genuine native source ray start y102, end y99.5,
        # never a created simulated block hit or a different ray.
        source = Path(runner.__file__).read_text(encoding="utf-8")
        tree = ast.parse(source)
        funcs = [n for n in tree.body if isinstance(n, ast.FunctionDef)
                 and n.name == "prepare_verified_original_scaffold_raytrace_support"]
        self.assertEqual(1, len(funcs))
        s = ast.get_source_segment(source, funcs[0])
        self.assertIn('"/fill 0 100 0 0 102 0 air"', s)
        self.assertIn('"/fill 0 99 0 0 99 0 stone"', s)
        self.assertEqual(2, s.count("send_verified_original_air_preclear("))
        self.assertEqual(1, s.count("send_verified_fill("))
        self.assertLess(s.index('"/fill 0 100 0 0 102 0 air"'),
                        s.index('"/fill 0 99 0 0 99 0 stone"'))
        bridge_token = "0.5D,102.0D,0.5D,0.5D,99.5D,0.5D"
        # Same token only inside the actual Java original source, not here.
        self.assertNotIn(bridge_token, s)
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS", s)

    def test_original_native_upface_and_two_wrong_ray_negatives_remain_mandatory(self):
        source = Path(runner.__file__).read_text(encoding="utf-8")
        first = source.index("            if native_scaffold_raytrace:")
        last = source.index('            require_running(process, "post world checkpoint")',first)
        scenario = source[first:last]
        # Only compare ACTUAL await_marker calls. The existing strict
        # pre-fixture rejection also mentions the positive ray marker in a
        # guarded negative check before either native source is created.
        setup = scenario.index("prepare_verified_original_scaffold_raytrace_support(")
        stone = scenario.index("verify_original_v19_stone_after_reseat(")
        first = scenario.index("await_marker(process, log,")
        second = scenario.index("await_marker(process, log,",first+1)
        self.assertLess(setup,stone)
        self.assertLess(stone,first)
        self.assertLess(first,second)
        self.assertIn("SCAFFOLD_RAYTRACE_WRONG_FACE_BLOCK_VETO_PASS=YES",
                      scenario[first:second])
        self.assertIn("SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES",
                      scenario[second:])
        self.assertEqual(2,scenario.count("await_marker(process, log,"))


if __name__ == "__main__":
    import unittest
    unittest.main()
