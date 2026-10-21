"""Original Mojang 1.8.9 V19 World raytrace support: source-only tests.

The read-only command fixture never asserts a fake block hit. The original
mapped World.rayTraceBlocks UP face and wrong block/face negative remain
mandatory in full binary GUI acceptance.
"""
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase
from unittest.mock import patch

import RunMojang189SingleplayerSmoke as smoke


class OriginalV19ConfirmedWorldRaytraceSupportTest(TestCase):
    def fixture(self, acknowledgement):
        with TemporaryDirectory() as root:
            log = Path(root) / "original-client.log"
            log.write_bytes(b"[CHAT] No blocks filled\n")
            calls = []

            def real_command(_process, filename, _window, command):
                calls.append(("native_command", command))
                with filename.open("ab") as stream:
                    stream.write(acknowledgement)

            def real_positive_fill(_process, _filename, _window, command):
                calls.append(("original_changed_fill", command))

            with patch.object(smoke, "send_command",
                              side_effect=real_command), patch.object(
                    smoke, "send_verified_fill",
                    side_effect=real_positive_fill), patch.object(
                    smoke, "require_running"):
                smoke.prepare_verified_original_scaffold_raytrace_support(
                    object(), log, "native-X11-window")
            return calls

    def test_native_air_noop_still_requires_fresh_positive_stone(self):
        for acknowledged in (
            b"[CHAT] No blocks filled\n",
            b"[CHAT] 1 blocks filled\n",
        ):
            with self.subTest(acknowledged=acknowledged):
                self.assertEqual(self.fixture(acknowledged), [
                    ("native_command", "/fill 0 99 0 0 99 0 air"),
                    ("original_changed_fill", "/fill 0 99 0 0 99 0 stone"),
                ])

    def test_source_air_preclear_does_not_claim_any_ray_pass(self):
        script = Path(smoke.__file__).read_text(encoding="utf8")
        start = script.index("def prepare_verified_original_scaffold_raytrace_support(")
        end = script.index("def prepare_verified_original_scaffold_survival_edge(", start)
        helper = script[start:end]
        self.assertEqual(helper.count("send_verified_fill(process, log, window,"), 1)
        self.assertIn('"/fill 0 99 0 0 99 0 air"', helper)
        self.assertIn('"/fill 0 99 0 0 99 0 stone"', helper)
        self.assertNotIn("_RAYTRACE_PASS=YES", helper)

    def test_stale_original_chat_does_not_satisfy_current_preclear(self):
        with TemporaryDirectory() as root:
            log = Path(root) / "original-client.log"
            log.write_bytes(b"[CHAT] 1 blocks filled\n")
            with patch.object(smoke, "send_command"), patch.object(
                smoke, "send_verified_fill") as proof, patch.object(
                smoke, "require_running"), patch.object(
                smoke.time, "monotonic", side_effect=[0., 9.]
            ):
                with self.assertRaisesRegex(
                    AssertionError, "missing fresh vanilla V19 raytrace"):
                    smoke.prepare_verified_original_scaffold_raytrace_support(
                        object(), log, "X11")
                proof.assert_not_called()

    def test_invalid_original_world_replies_never_commit_stone(self):
        for fail in (
            b"[CHAT] Cannot place blocks outside of the world\n",
            b"[CHAT] Too many blocks in the specified area\n",
            b"[CHAT] There are no blocks to fill\n",
        ):
            with self.subTest(fail=fail):
                with TemporaryDirectory() as root:
                    log = Path(root) / "original-client.log"
                    log.write_bytes(b"")
                    def command(_p, filename, _w, _cmd):
                        with filename.open("ab") as stream:
                            stream.write(fail)
                    with patch.object(smoke, "send_command",
                                      side_effect=command), patch.object(
                        smoke, "send_verified_fill") as proof, patch.object(
                        smoke, "require_running"):
                        with self.assertRaisesRegex(AssertionError, "rejected"):
                            smoke.prepare_verified_original_scaffold_raytrace_support(
                                object(), log, "X11")
                        proof.assert_not_called()

    def test_true_vanilla_wrong_face_and_up_face_original_gates_remain(self):
        source = Path(smoke.__file__).read_text(encoding="utf8")
        start = source.index("            if native_scaffold_raytrace:")
        stop = source.index('            require_running(process, "post world checkpoint")',start)
        v19 = source[start:stop]
        self.assertIn("prepare_verified_original_scaffold_raytrace_support(", v19)
        self.assertIn("await_marker(process, log,", v19)
        self.assertIn("SCAFFOLD_RAYTRACE_WRONG_FACE_BLOCK_VETO_PASS=YES", v19)
        self.assertIn("SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES", v19)
        self.assertIn("genuine World.rayTraceBlocks hits original stone UP face", v19)
        self.assertIn("original raytrace rejects wrong support and facing", v19)
        self.assertLess(
            v19.index("prepare_verified_original_scaffold_raytrace_support("),
            v19.index("SCAFFOLD_RAYTRACE_WRONG_FACE_BLOCK_VETO_PASS=YES"))


class OriginalV271AfterReseatStoneReadbackTest(TestCase):
    """V19 requires ACTUAL integrated-server cell evidence before raytrace."""

    def readback(self, *, prior=b"", fresh=b"", monotonic=None):
        with TemporaryDirectory() as root:
            log=Path(root)/"original-client.log"
            log.write_bytes(prior)
            commands=[]
            def native_command(_proc, filename, _window, command):
                commands.append(command)
                if fresh:
                    with filename.open("ab") as stream:
                        stream.write(fresh)
            patches=[
                patch.object(smoke,"send_command",side_effect=native_command),
                patch.object(smoke,"require_running"),
                patch.object(smoke.time,"sleep"),
            ]
            if monotonic is not None:
                patches.append(patch.object(smoke.time,"monotonic",
                                            side_effect=monotonic))
            for ctx in patches:ctx.__enter__()
            try:
                smoke.verify_original_v19_stone_after_reseat(
                    object(),log,"original-X11")
            finally:
                for ctx in reversed(patches):ctx.__exit__(None,None,None)
            return commands

    def test_actual_fresh_original_stone_after_reseat_acknowledged(self):
        self.assertEqual(self.readback(
            fresh=b"[CHAT] Successfully found the block at 0, 99, 0.\n"),
            ["/testforblock 0 99 0 stone"])

    def test_stale_previous_chat_confirmation_cannot_authorize_new_stone(self):
        with self.assertRaisesRegex(AssertionError,
                                    "no fresh original V19 post-reseat"):
            self.readback(
                prior=b"[CHAT] Successfully found the block at 0, 99, 0.\n",
                fresh=b"",monotonic=[0.,9.])

    def test_missing_success_is_never_interpreted_as_stone(self):
        with self.assertRaisesRegex(AssertionError,
                                    "no fresh original V19 post-reseat"):
            self.readback(fresh=b"[CHAT] Teleported CIProbe to 0.5, 100.0, 0.5\n",
                          monotonic=[0.,0.1,9.])

    def test_original_wrong_block_readback_fails_closed(self):
        for rejected in (
            b"[CHAT] The block at 0, 99, 0 is minecraft:air\n",
            b"[CHAT] Unknown command. Type /help for help\n",
            b"[CHAT] Cannot test blocks outside the world\n",
        ):
            with self.subTest(rejected=rejected):
                with self.assertRaisesRegex(AssertionError, "testforblock rejected"):
                    self.readback(fresh=rejected)

    def test_rejection_even_if_positive_marker_interleaves_never_passes(self):
        with self.assertRaisesRegex(AssertionError, "testforblock rejected"):
            self.readback(fresh=(
                b"[CHAT] The block at 0, 99, 0 is air\n"
                b"[CHAT] Successfully found the block at 0, 99, 0.\n"
            ))

    def test_success_marker_cannot_replace_mandatory_native_face_hit(self):
        source=Path(smoke.__file__).read_text(encoding="utf8")
        begin=source.index("            if native_scaffold_raytrace:")
        end=source.index('            require_running(process, "post world checkpoint")',
                         begin)
        step=source[begin:end]
        self.assertIn("verify_original_v19_stone_after_reseat(process, log, window)",
                      step)
        self.assertIn("SCAFFOLD_RAYTRACE_WRONG_FACE_BLOCK_VETO_PASS=YES",step)
        self.assertIn("SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES",step)
        self.assertEqual(step.count("await_marker(process, log,"),2)
        self.assertLess(
            step.index("verify_original_v19_stone_after_reseat("),
            step.index("SCAFFOLD_RAYTRACE_WRONG_FACE_BLOCK_VETO_PASS=YES"))
        self.assertLess(
            step.index("SCAFFOLD_RAYTRACE_WRONG_FACE_BLOCK_VETO_PASS=YES"),
            step.index("SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES"))

    def test_readback_helper_cannot_emit_native_raytrace_pass(self):
        source=Path(smoke.__file__).read_text(encoding="utf8")
        start=source.index("def verify_original_v19_stone_after_reseat(")
        end=source.index("def prepare_verified_original_scaffold_survival_edge(",start)
        helper=source[start:end]
        self.assertIn('"/testforblock 0 99 0 stone"',helper)
        self.assertNotIn("NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES",helper)
        self.assertNotIn("WRONG_FACE_BLOCK_VETO_PASS=YES",helper)
