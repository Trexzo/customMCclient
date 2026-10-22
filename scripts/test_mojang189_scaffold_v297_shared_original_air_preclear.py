"""V297: consolidated genuine original vanilla /fill AIR preclear contract.

Mocked Python tests check the exact source/ordering invariants, NOT a
Mojang GUI, server, original client World, native ray or real placement.
"""
import ast
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase
from unittest.mock import patch

import RunMojang189SingleplayerSmoke as smoke


class OriginalVanillaAirPreclearUnificationTest(TestCase):
    def receipt(self, reply, command="/fill 0 99 0 0 99 0 air"):
        with TemporaryDirectory() as td:
            path = Path(td) / "original-client.log"
            path.write_bytes(b"[CHAT] No blocks filled\n")
            commands = []

            def send(*args):
                commands.append(args[3])
                with path.open("ab") as handle:
                    handle.write(reply)
            with patch.object(smoke, "send_command", side_effect=send), patch.object(
                    smoke, "require_running"):
                result = smoke.send_verified_original_air_preclear(
                    object(), path, "actual-X11", command,
                    "original source air reset", "strict original missing receipt")
            return result, commands

    def test_fresh_genuine_positive_blocks_changing_is_only_preclear_evidence(self):
        result, commands = self.receipt(b"[CHAT] 2 blocks filled\n")
        self.assertEqual("source-blocks-cleared", result)
        self.assertEqual(["/fill 0 99 0 0 99 0 air"], commands)

    def test_fresh_original_noop_is_accepted_only_for_sacrificial_air(self):
        result, commands = self.receipt(b"[CHAT] No blocks filled\n")
        self.assertEqual("source-already-air", result)
        self.assertEqual(1, len(commands))

    def test_positive_unchanged_stone_or_unrelated_chat_command_cannot_preclear(self):
        with TemporaryDirectory() as td:
            log = Path(td) / "original.log"
            log.write_bytes(b"")
            with patch.object(smoke, "send_command") as sent:
                for command in ("/fill 0 99 0 0 99 0 stone",
                                "/tp CIProbe 2 120 2",
                                "fill 0 99 0 0 99 0 air"):
                    with self.subTest(command=command):
                        with self.assertRaisesRegex(
                                ValueError, "requires actual vanilla air fill"):
                            smoke.send_verified_original_air_preclear(
                                object(), log, "X11", command, "source",
                                "missing")
                sent.assert_not_called()

    def test_missing_original_source_reply_cannot_reuse_stale_result(self):
        with TemporaryDirectory() as td:
            log = Path(td) / "original.log"
            log.write_bytes(b"[CHAT] No blocks filled\n")
            with patch.object(smoke, "send_command"), patch.object(
                    smoke, "require_running"), patch.object(
                    smoke.time, "monotonic", side_effect=[0, 9]):
                with self.assertRaisesRegex(
                        AssertionError, "strict original missing receipt"):
                    smoke.send_verified_original_air_preclear(
                        object(), log, "X11",
                        "/fill 0 99 0 0 99 0 air", "source",
                        "strict original missing receipt")

    def test_source_world_refusal_hard_fails_before_any_native_gate(self):
        for reply in (
            b"[CHAT] Cannot place blocks outside of the world\n",
            b"[CHAT] Too many blocks in the specified area\n",
            b"[CHAT] There are no blocks to fill\n",
        ):
            with self.subTest(reply=reply):
                with self.assertRaises(AssertionError):
                    self.receipt(reply)

    def test_original_preclear_contract_is_exactly_one_source_send_and_read(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        module = ast.parse(src)
        functions = [n for n in module.body if isinstance(n, ast.FunctionDef)
                     and n.name == "send_verified_original_air_preclear"]
        self.assertEqual(1, len(functions))
        body = ast.get_source_segment(src, functions[0])
        self.assertEqual(1, body.count("send_command(process, log, window, command)"))
        self.assertEqual(1, body.count("cursor = log.stat().st_size"))
        self.assertEqual(1, body.count("handle.seek(cursor)"))
        self.assertEqual(1, body.count("original_dry_chamber_preclear_reply(fresh)"))
        self.assertIn("time.monotonic() + 8", body)
        self.assertNotIn("PASS=YES", body)

    def test_all_four_original_preclears_use_one_identical_source_reader(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        module = ast.parse(src)
        scenarios = (
            ("prepare_verified_native_dry_chamber",
             ("/fill 4 79 4 12 87 12 air",)),
            ("prepare_verified_original_scaffold_raytrace_support",
             ("/fill 0 99 0 0 99 0 air",
              "/fill 0 100 0 0 102 0 air")),
            ("prepare_verified_original_scaffold_survival_edge",
             ("/fill 0 99 0 1 99 0 air",)),
            ("send_verified_original_scaffold_context_preclear",
             ("/fill 0 99 0 4 104 4 air",)),
        )
        for name, commands in scenarios:
            with self.subTest(name=name):
                targets = [n for n in module.body
                           if isinstance(n, ast.FunctionDef) and n.name == name]
                self.assertEqual(1, len(targets))
                body = ast.get_source_segment(src, targets[0])
                self.assertEqual(len(commands),
                                 body.count("send_verified_original_air_preclear("))
                for command in commands:
                    self.assertIn(command, body)
                self.assertNotIn("handle.seek(cursor)", body)
                self.assertNotIn("send_command(", body)
                self.assertNotIn("PASS=YES", body)

    def test_every_scenario_retains_different_strict_timeout_failure_wording(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        for error in (
            "bounded dry chamber deadline",
            "missing fresh vanilla V19 raytrace support preclear reply",
            "no fresh original Scaffold air-preclear vanilla reply",
            "no FRESH original vanilla V23 context air-preclear source reply",
        ):
            self.assertIn(error, src)

    def test_sacrificial_world_reset_cannot_replace_a_single_positive_fill(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        scenarios = (
            ("prepare_verified_native_dry_chamber",
             ("/fill 4 79 4 12 87 12 stone",
              "/fill 5 80 5 11 86 11 air")),
            ("prepare_verified_original_scaffold_raytrace_support",
             ("/fill 0 99 0 0 99 0 stone",)),
            ("prepare_verified_original_scaffold_survival_edge",
             ("/fill 0 99 0 1 99 0 stone",
              "/fill 1 99 0 1 99 0 air")),
            ("prepare_native_scaffold_context_chamber",
             ("/fill 0 99 0 4 104 4 stone",
              "/fill 0 100 0 4 104 4 air")),
        )
        module = ast.parse(src)
        for name, positives in scenarios:
            with self.subTest(name=name):
                n = [n for n in module.body
                     if isinstance(n, ast.FunctionDef) and n.name == name]
                self.assertEqual(1, len(n))
                body = ast.get_source_segment(src, n[0])
                self.assertEqual(len(positives), body.count("send_verified_fill("))
                for command in positives:
                    self.assertIn(command, body)

    def test_preclear_is_never_a_world_ray_or_controller_acceptance_marker(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        start = src.index("def send_verified_original_air_preclear(")
        end = src.index("def prepare_verified_native_dry_chamber(", start)
        helper = src[start:end]
        for s in ("WorldClient(", "onPlayerRightClick", "ItemStack(",
                  "CUSTOMMC_OFFICIAL_189_", "controller.", "continue_on_error"):
            self.assertNotIn(s, helper)


if __name__ == "__main__":
    import unittest
    unittest.main()
