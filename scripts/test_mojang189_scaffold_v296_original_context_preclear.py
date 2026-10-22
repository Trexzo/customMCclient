"""V296 independent original 1.8.9 V23 fixture reset regressions.

These use source text and a temporary log file with simulated chat replies;
they do NOT create original blocks, launch Minecraft or certify a World.
The full pinned original GUI+server run is still the only native authority.
"""
import ast
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase
from unittest.mock import patch

import RunMojang189SingleplayerSmoke as smoke


class OriginalV23ContextPreclearTests(TestCase):
    def test_actual_original_no_blocks_filled_is_allowed_only_in_preclear(self):
        self.assertEqual("source-already-air",
                         smoke.original_dry_chamber_preclear_reply(
                             b"[CHAT] No blocks filled\n"))
        self.assertFalse(
            smoke.re.search(rb"\[CHAT\] [1-9][0-9]* blocks filled",
                            b"[CHAT] No blocks filled"))

    def test_original_positive_preclear_is_also_real_source_evidence(self):
        self.assertEqual("source-blocks-cleared",
                         smoke.original_dry_chamber_preclear_reply(
                             b"[CHAT] 150 blocks filled\n"))

    def test_invalid_original_preclear_world_or_excess_is_hard_rejected(self):
        for reply in (b"[CHAT] Cannot place blocks outside of the world",
                      b"[CHAT] Too many blocks in the specified area"):
            with self.subTest(reply=reply):
                with self.assertRaises(AssertionError):
                    smoke.original_dry_chamber_preclear_reply(reply)

    def test_fresh_noop_source_after_command_is_accepted_without_fabricated_pass(self):
        with TemporaryDirectory() as td:
            log = Path(td) / "original-chat.log"
            log.write_bytes(b"[CHAT] 150 blocks filled\n")
            commands = []
            def actual_source_after_send(*args):
                commands.append(args[3])
                with log.open("ab") as out:
                    out.write(b"[CHAT] No blocks filled\n")
            with patch.object(smoke, "send_command",
                              side_effect=actual_source_after_send), patch.object(
                    smoke, "require_running"):
                observed = smoke.send_verified_original_scaffold_context_preclear(
                    object(), log, "original-x11-window")
            self.assertEqual("source-already-air", observed)
            self.assertEqual(["/fill 0 99 0 4 104 4 air"], commands)

    def test_fresh_positive_air_change_is_accepted_as_preclear_only(self):
        with TemporaryDirectory() as td:
            log = Path(td) / "original-chat.log"
            log.write_bytes(b"[CHAT] No blocks filled\n")
            def append_real_reply(*args):
                with log.open("ab") as out:
                    out.write(b"[CHAT] 74 blocks filled\n")
            with patch.object(smoke, "send_command",
                              side_effect=append_real_reply), patch.object(
                    smoke, "require_running"):
                observed = smoke.send_verified_original_scaffold_context_preclear(
                    object(), log, "original-x11-window")
            self.assertEqual("source-blocks-cleared", observed)

    def test_old_stale_success_cannot_satisfy_new_original_preclear(self):
        with TemporaryDirectory() as td:
            log = Path(td) / "original-chat.log"
            log.write_bytes(b"[CHAT] No blocks filled\n")
            with patch.object(smoke, "send_command"), patch.object(
                    smoke, "require_running"), patch.object(
                    smoke.time, "monotonic", side_effect=[100, 109]):
                with self.assertRaisesRegex(
                        AssertionError, "no FRESH original vanilla V23"):
                    smoke.send_verified_original_scaffold_context_preclear(
                        object(), log, "original-x11-window")

    def test_original_v23_chamber_requires_sacrificial_reset_before_two_real_changes(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        tree = ast.parse(src)
        names = [
            node for node in tree.body
            if isinstance(node, ast.FunctionDef)
            and node.name == "prepare_native_scaffold_context_chamber"
        ]
        self.assertEqual(1, len(names))
        section = ast.get_source_segment(src, names[0])
        tokens = [
            '"/tp CIProbe 2 120 2 0 0"',
            "send_verified_original_scaffold_context_preclear(",
            '"/fill 0 99 0 4 104 4 stone"',
            '"/fill 0 100 0 4 104 4 air"',
            '"/tp CIProbe 2 100 2 0 0"',
        ]
        offsets = [section.index(t) for t in tokens]
        self.assertEqual(offsets, sorted(offsets))
        self.assertEqual(2, section.count("send_verified_fill("))
        self.assertNotIn("send_command(", section)

    def test_preclear_only_verifies_world_reset_and_never_authorizes_original_V23(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        start = src.index("def send_verified_original_scaffold_context_preclear(")
        end = src.index("def prepare_native_scaffold_context_chamber(", start)
        section = src[start:end]
        self.assertIn('"/fill 0 99 0 4 104 4 air"', section)
        self.assertIn("send_verified_original_air_preclear(", section)
        # The sole source-freshness authority is now a shared helper, not
        # an unverified second V23-specific log reader.
        shared = src.split("def send_verified_original_air_preclear(", 1)[1]
        shared = shared.split("def prepare_verified_native_dry_chamber(", 1)[0]
        self.assertIn("original_dry_chamber_preclear_reply(fresh)", shared)
        self.assertIn("handle.seek(cursor)", shared)
        self.assertIn("cursor = log.stat().st_size", shared)
        self.assertIn("stop = time.monotonic() + 8", shared)
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_TICK_CONTEXT_PASS", section)
        self.assertNotIn("PASS=YES", section)
        for unsafe in ("controller", "ItemStack(", "WorldClient(", "no_error_continue"):
            with self.subTest(unsafe=unsafe):
                self.assertNotIn(unsafe, section)


if __name__ == "__main__":
    import unittest
    unittest.main()
