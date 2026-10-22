"""V302 source-only regressions for original V19 post-reseat AIR corridor.

Mocks cannot establish a real Mojang original client World, block hit, native
ray, or certification. Those remain the authority of pinned original CI.
"""
import ast
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase
from unittest.mock import patch

import RunMojang189SingleplayerSmoke as smoke


class OriginalV19PostReseatAirColumnTests(TestCase):
    def test_three_exact_native_189_commands_and_no_space_responses(self):
        reads = []
        def fake_real_chat(process, log, window, command, expected):
            reads.append((command, expected))
        with patch.object(smoke, "send_verified_original_chat_reply",
                          side_effect=fake_real_chat):
            smoke.verify_original_v19_air_column_after_reseat(
                object(), object(), "original-X11")
        self.assertEqual([
            ("/testforblock 0 100 0 air",
             b"[CHAT] Successfully found the block at 0,100,0."),
            ("/testforblock 0 101 0 air",
             b"[CHAT] Successfully found the block at 0,101,0."),
            ("/testforblock 0 102 0 air",
             b"[CHAT] Successfully found the block at 0,102,0."),
        ], reads)

    def test_failing_first_original_air_receipt_aborts_every_later_source_check(self):
        sent = []
        def reject(*args):
            sent.append(args[3])
            raise AssertionError("genuine server material mismatch")
        with patch.object(smoke, "send_verified_original_chat_reply",
                          side_effect=reject):
            with self.assertRaisesRegex(AssertionError,
                                        "genuine server material mismatch"):
                smoke.verify_original_v19_air_column_after_reseat(
                    object(), object(), "X11")
        self.assertEqual(["/testforblock 0 100 0 air"], sent)

    def test_failing_second_original_air_check_never_checks_third(self):
        sent = []
        def reject_second(*args):
            sent.append(args[3])
            if len(sent) == 2:
                raise AssertionError("source AIR cell at y101 missing")
        with patch.object(smoke, "send_verified_original_chat_reply",
                          side_effect=reject_second):
            with self.assertRaisesRegex(AssertionError, "y101 missing"):
                smoke.verify_original_v19_air_column_after_reseat(
                    object(), object(), "X11")
        self.assertEqual([
            "/testforblock 0 100 0 air",
            "/testforblock 0 101 0 air",
        ], sent)

    def test_source_readback_uses_real_vanilla_chat_and_fresh_byte_offset(self):
        with TemporaryDirectory() as directory:
            log = Path(directory) / "original-189-client.log"
            log.write_bytes(
                b"[CHAT] Successfully found the block at 0,100,0.\n")
            commands = []
            def answer(_process, source_log, _window, command):
                commands.append(command)
                y = 100 + len(commands)-1
                with source_log.open("ab") as handle:
                    handle.write(
                        ("[CHAT] Successfully found the block at 0,{},0.\n"
                         .format(y)).encode("ascii"))
            with patch.object(smoke, "send_command", side_effect=answer), patch.object(
                    smoke, "require_running"):
                smoke.verify_original_v19_air_column_after_reseat(
                    object(), log, "original-X11")
            self.assertEqual([
                "/testforblock 0 100 0 air",
                "/testforblock 0 101 0 air",
                "/testforblock 0 102 0 air",
            ], commands)

    def test_stale_preexisting_original_air_reply_cannot_satisfy_new_command(self):
        with TemporaryDirectory() as directory:
            log = Path(directory) / "original-189.log"
            log.write_bytes(
                b"[CHAT] Successfully found the block at 0,100,0.\n")
            with patch.object(smoke, "send_command"), patch.object(
                    smoke, "require_running"), patch.object(
                    smoke.time, "monotonic", side_effect=[0, 9]):
                with self.assertRaisesRegex(
                        AssertionError, "missing new source integrated-server reply"):
                    smoke.verify_original_v19_air_column_after_reseat(
                        object(), log, "native-X11")

    def test_server_wrong_cell_reply_cannot_authorize_requested_v19_air(self):
        with TemporaryDirectory() as directory:
            log = Path(directory) / "native-mojang.log"
            log.write_bytes(b"")
            def wrong_reply(_process, source_log, _window, _command):
                with source_log.open("ab") as handle:
                    handle.write(
                        b"[CHAT] Successfully found the block at 1,100,0.\n")
            with patch.object(smoke, "send_command", side_effect=wrong_reply), patch.object(
                    smoke, "require_running"), patch.object(
                    smoke.time, "monotonic", side_effect=[0, 9]):
                with self.assertRaisesRegex(
                        AssertionError, "missing new source integrated-server reply"):
                    smoke.verify_original_v19_air_column_after_reseat(
                        object(), log, "X11")

    def test_original_air_reader_called_only_after_stone_and_reseat(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        a = src.index("            if native_scaffold_raytrace:",src.index("with tempfile.TemporaryDirectory"))
        b = src.index('            require_running(process, "post world checkpoint")', a)
        native = src[a:b]
        stages = [
            "prepare_verified_original_scaffold_raytrace_support(",
            '"/tp 0.5 100 0.5 0 0"',
            "verify_original_v19_stone_after_reseat(",
            "verify_original_v19_air_column_after_reseat(",
            "await_marker(process, log,",
        ]
        offsets = [native.index(stage) for stage in stages]
        self.assertEqual(offsets, sorted(offsets))

    def test_original_v19_unchanged_both_hard_real_ray_markers(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        a = src.index("            if native_scaffold_raytrace:",src.index("with tempfile.TemporaryDirectory"))
        b = src.index('            require_running(process, "post world checkpoint")', a)
        native = src[a:b]
        self.assertEqual(2, native.count("await_marker(process, log,"))
        self.assertIn("SCAFFOLD_RAYTRACE_WRONG_FACE_BLOCK_VETO_PASS=YES", native)
        self.assertIn("SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES", native)
        self.assertIn("original raytrace rejects wrong support and facing", native)
        self.assertIn("genuine World.rayTraceBlocks hits original stone UP face",native)

    def test_actual_mojang_ray_source_endpoints_are_not_written_by_air_helper(self):
        source = Path(smoke.__file__).read_text(encoding="utf-8")
        tree = ast.parse(source)
        funcs = [node for node in tree.body if isinstance(node, ast.FunctionDef)
                 and node.name == "verify_original_v19_air_column_after_reseat"]
        self.assertEqual(1,len(funcs))
        helper = ast.get_source_segment(source, funcs[0])
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_",helper)
        for unsafe in ("WorldClient(", "setBlockState(", "rayTraceBlocks(",
                       "onPlayerRightClick(", "ItemStack(", "sendPacket("):
            self.assertNotIn(unsafe,helper)

    def test_no_generic_world_material_acceptance_or_wrong_height(self):
        source = Path(smoke.__file__).read_text(encoding="utf-8")
        a=source.index("def verify_original_v19_air_column_after_reseat(")
        b=source.index("def original_v29_inventory_clear_receipt(",a)
        section=source[a:b]
        self.assertIn("for y in (100, 101, 102):",section)
        self.assertIn('"/testforblock 0 {} 0 air".format(y)',section)
        self.assertIn('"[CHAT] Successfully found the block at 0,{},0."',section)
        self.assertNotIn("if b'[CHAT] Successfully found",section)
        self.assertNotIn("if b\"[CHAT] Successfully found",section)


if __name__ == "__main__":
    import unittest
    unittest.main()
