"""V171 source-only original Minecraft 1.8.9 Scaffold edge fixture policy.

Only pinned graphical original Mojang tests can establish native controller
placement and genuine world + inventory changes. These mocks cannot.
"""
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import RunMojang189SingleplayerSmoke as smoke


class OriginalScaffoldRealSurvivalEdgeFixtureTest(unittest.TestCase):
    @staticmethod
    def exercise(reply):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "original-Mojang.log"
            log.write_bytes(b"[CHAT] No blocks filled\\n")
            actual = []

            def vanilla_command(process, logfile, window, command):
                actual.append(("command", command))
                with logfile.open("ab") as stream:
                    stream.write(reply)

            def positive_vanilla_fill(process, logfile, window, command):
                actual.append(("positive_fill", command))

            with patch.object(smoke, "send_command",
                              side_effect=vanilla_command), patch.object(
                    smoke, "send_verified_fill",
                    side_effect=positive_vanilla_fill), patch.object(
                    smoke, "require_running"):
                smoke.prepare_verified_original_scaffold_survival_edge(
                    object(), log, "original-X11")
            return actual

    def test_noop_and_changed_source_air_preclear_still_require_two_positive_fills(self):
        for reply in (
            b"[CHAT] No blocks filled\\n",
            b"[CHAT] 2 blocks filled\\n",
        ):
            with self.subTest(reply=reply):
                self.assertEqual(self.exercise(reply), [
                    ("command", "/fill 0 99 0 1 99 0 air"),
                    ("positive_fill", "/fill 0 99 0 1 99 0 stone"),
                    ("positive_fill", "/fill 1 99 0 1 99 0 air"),
                ])

    def test_real_support_target_are_exact_adjacent_1x1_cells(self):
        # Player center x=1.05 has vanilla width .6; x_min=.75
        # overlaps the 0..1 stone support. Air target at x=1..2.
        player_min_x = 1.05 - .3
        self.assertGreater(player_min_x, 0.0)
        self.assertLess(player_min_x, 1.0)
        self.assertEqual(100-1, 99)
        commands = self.exercise(b"[CHAT] No blocks filled\\n")
        self.assertEqual(commands[1][1].split()[1:],
                         ["0", "99", "0", "1", "99", "0", "stone"])
        self.assertEqual(commands[2][1].split()[1:],
                         ["1", "99", "0", "1", "99", "0", "air"])

    def test_stale_previous_log_air_reply_never_proves_fresh_reset(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "original.log"
            log.write_bytes(b"[CHAT] No blocks filled\\n")
            with patch.object(smoke, "send_command"), patch.object(
                    smoke, "send_verified_fill") as positive, patch.object(
                    smoke, "require_running"), patch.object(
                    smoke.time, "monotonic", side_effect=[0.0, 9.0]):
                with self.assertRaisesRegex(
                        AssertionError, "no fresh original Scaffold air-preclear"):
                    smoke.prepare_verified_original_scaffold_survival_edge(
                        object(), log, "X11")
                positive.assert_not_called()

    def test_invalid_world_replies_cannot_trigger_changed_support_or_target(self):
        for reject in (
            b"[CHAT] Cannot place blocks outside of the world\\n",
            b"[CHAT] Too many blocks in the specified area\\n",
            b"[CHAT] There are no blocks to fill\\n",
        ):
            with self.subTest(reject=reject):
                with tempfile.TemporaryDirectory() as root:
                    log = Path(root) / "original.log"
                    log.write_bytes(b"")
                    def vanilla_command(_process, logfile, _window, _command):
                        with logfile.open("ab") as stream:
                            stream.write(reject)
                    with patch.object(smoke, "send_command",
                                      side_effect=vanilla_command), patch.object(
                            smoke, "send_verified_fill") as positive, patch.object(
                            smoke, "require_running"):
                        with self.assertRaisesRegex(
                                AssertionError, "rejected"):
                            smoke.prepare_verified_original_scaffold_survival_edge(
                                object(), log, "X11")
                        positive.assert_not_called()

    def test_original_real_enabled_and_disabled_place_gates_not_relaxed(self):
        text = Path(smoke.__file__).read_text(encoding="utf-8")
        start = text.index(
            "# V29: actual integrated-server vanilla survival block placement.")
        finish = text.index("            if native_scaffold_candidate:", start)
        v29 = text[start:finish]
        self.assertIn("prepare_verified_original_scaffold_survival_edge(", v29)
        self.assertIn("await_marker(process, log, module_tag", v29)
        self.assertIn("await_marker(process, log, attempt_tag", v29)
        self.assertIn("await_marker(process, log, readback_tag", v29)
        self.assertIn("await_marker(process, log, disabled_tag", v29)
        self.assertIn('b"accepted=true"', v29)
        self.assertIn("real enabled Scaffold module admitted vanilla click", v29)
        self.assertIn("source WorldClient stone target and one survival stack debit", v29)
        self.assertIn("original World air and unchanged eight-stack while disabled", v29)
        self.assertIn("if any(tag in read_tail(log)", v29)
    def test_v260_valid_native_edge_requires_no_corrective_teleport(self):
        with patch.object(smoke, "native_safe_walk_positions",
                          return_value=[(1.05, 100., .5)] * 5), patch.object(
                smoke, "send_verified_original_chat_reply") as cmd:
            smoke.await_original_v29_survival_edge_grounding(
                object(), Path("unused"), "X11")
            cmd.assert_not_called()

    def test_v260_real_drift_requires_one_fresh_vanilla_reply(self):
        class Clock:
            value = 0.
            def now(self):
                self.value += .4
                return self.value
        clock = Clock()
        drift = [(.6404239222828763, 100., .5)] * 5
        original = [(1.05, 100., .5)] * 5
        reads = [drift, original]
        with patch.object(smoke, "native_safe_walk_positions",
                          side_effect=lambda _: reads.pop(0)), patch.object(
                smoke.time, "monotonic", side_effect=clock.now), patch.object(
                smoke.time, "sleep"), patch.object(
                smoke, "require_running"), patch.object(
                smoke, "send_verified_original_chat_reply") as cmd:
            smoke.await_original_v29_survival_edge_grounding(
                object(), Path("unused"), "X11")
            cmd.assert_called_once_with(
                unittest.mock.ANY, Path("unused"), "X11",
                "/tp CIProbe 1.05 100 0.5 90 88.6",
                b"[CHAT] Teleported CIProbe to 1.05, 100.0, 0.5")

    def test_v260_native_drift_never_counts_as_position_proof(self):
        drift = [(.6404239222828763, 100., .5)] * 3
        self.assertTrue(smoke.original_v29_settled_edge_drift(drift))
        self.assertFalse(smoke.original_v29_position_accepted(drift))
        self.assertTrue(smoke.original_v29_position_accepted(
            [(1.05, 100., .5)] * 3))
        self.assertFalse(smoke.original_v29_position_accepted(
            [(1.05, 100., .5)] * 2))

    def test_v260_unrelated_and_moving_positions_cannot_reposition(self):
        for samples in (
            [(9., 100., .5)] * 3,
            [(0.5, 100., .5), (.7, 100., .5), (.9, 100., .5)],
            [(.64, 98., .5)] * 3,
            [(.64, 100., 9.)] * 3,
        ):
            self.assertFalse(smoke.original_v29_settled_edge_drift(samples))

    def test_v260_one_correction_without_native_source_still_fails(self):
        class Clock:
            value = 0.
            def now(self):
                self.value += 1.
                return self.value
        clock = Clock()
        drift = [(.6404239222828763, 100., .5)] * 5
        with patch.object(smoke, "native_safe_walk_positions",
                          return_value=drift), patch.object(
                smoke.time, "monotonic", side_effect=clock.now), patch.object(
                smoke.time, "sleep"), patch.object(
                smoke, "require_running"), patch.object(
                smoke, "send_verified_original_chat_reply") as cmd:
            with self.assertRaisesRegex(
                    AssertionError, "original Survival player not grounded"):
                smoke.await_original_v29_survival_edge_grounding(
                    object(), Path("unused"), "X11")
            cmd.assert_called_once()
