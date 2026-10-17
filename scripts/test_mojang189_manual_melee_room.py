"""Strict offline checks for the ORIGINAL Minecraft 1.8.9 manual-melee fixture.

This does NOT prove a Pig was raycast or hit in real Minecraft. The full
official graphical + dedicated-server X11 job is the only such authority.
"""
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase
from unittest.mock import patch
import unittest

import RunMojang189SingleplayerSmoke as smoke


class Alive:
    def poll(self):
        return None


class OriginalManualMeleeRoomTests(TestCase):
    def test_original_summon_reply_must_be_fresh(self):
        with TemporaryDirectory() as root:
            log = Path(root) / "latest.log"
            log.write_bytes(b"[CHAT] Object successfully summoned\n")
            def rejected(_process, game_log, _window, command):
                self.assertEqual(command,
                                 "/summon Pig 8.5 100 10.5 {NoAI:1b}")
                with game_log.open("ab") as stream:
                    stream.write(b"[CHAT] Unable to summon object\n")
            with patch.object(smoke, "send_command", side_effect=rejected):
                with self.assertRaisesRegex(
                        AssertionError, "genuine original command rejected"):
                    smoke.send_verified_original_chat_reply(
                        Alive(), log, "test",
                        "/summon Pig 8.5 100 10.5 {NoAI:1b}",
                        b"[CHAT] Object successfully summoned")

    def test_fresh_genuine_success_ignores_old_unrelated_reply(self):
        with TemporaryDirectory() as root:
            log = Path(root) / "latest.log"
            log.write_bytes(b"[CHAT] Unable to summon object\n")
            def correct(_process, game_log, _window, _command):
                with game_log.open("ab") as stream:
                    stream.write(b"[CHAT] Object successfully summoned\n")
            with patch.object(smoke, "send_command", side_effect=correct):
                smoke.send_verified_original_chat_reply(
                    Alive(), log, "test",
                    "/summon Pig 8.5 100 10.5 {NoAI:1b}",
                    b"[CHAT] Object successfully summoned")

    def test_wrong_teleport_reply_does_not_confirm_target_room(self):
        with TemporaryDirectory() as root:
            log = Path(root) / "latest.log"
            log.write_bytes(b"[CHAT] Teleported CIProbe to 8.5, 100.0, 8.5\n")
            def wrong(_process, game_log, _window, _command):
                with game_log.open("ab") as stream:
                    stream.write(b"[CHAT] Teleported CIProbe to 0.5, 70.0, 0.5\n")
            with patch.object(smoke, "send_command", side_effect=wrong):
                # Avoid an 8-second fake-log wait, not the original game deadline.
                with patch.object(smoke.time, "monotonic",
                                  side_effect=[0.0, 9.0]):
                    with self.assertRaisesRegex(
                            AssertionError, "missing new source"):
                        smoke.send_verified_original_chat_reply(
                            Alive(), log, "test",
                            "/tp CIProbe 8 100 8 0 26",
                            b"[CHAT] Teleported CIProbe to 8.5, 100.0, 8.5")

    def test_fresh_wrong_command_cannot_satisfy_other_witness(self):
        with self.assertRaises(ValueError):
            smoke.send_verified_original_chat_reply(
                Alive(), Path("unused"), "test", "/summon Pig",
                b"Object successfully summoned")

    def test_manual_world_is_loaded_and_original_hit_remains_mandatory(self):
        source = Path(smoke.__file__).read_text(encoding="utf-8")
        runner = source.split("            if controlled_combat:", 1)[1]
        manual = runner.split("                if attack:", 1)[1].split(
            "                else:", 1)[0]
        # Validates independent, real integrated-server command acceptance.
        pre = manual.index("send_verified_chunk_teleport")
        stone = manual.index('"/fill 4 99 4 12 104 13 stone"')
        air = manual.index('"/fill 5 100 5 11 103 12 air"')
        look = manual.index('"/tp CIProbe 8 100 8 0 26"')
        pig = manual.index('"/summon Pig 8.5 100 10.5 {NoAI:1b}"')
        proof = manual.index("CUSTOMMC_189_MANUAL_MELEE_FIXED_NATIVE_ROOM_PASS")
        self.assertTrue(pre < stone < air < look < pig < proof)
        # Both fill cuboids are wholly inside a loaded chunk and enclose
        # a source player x=8.5 z=8.5 and Pig x=8.5 z=10.5.
        outer = (4, 99, 4, 12, 104, 13)
        inner = (5, 100, 5, 11, 103, 12)
        for i in (0, 2, 3, 5):
            self.assertTrue(0 <= outer[i] <= 15)
            self.assertTrue(0 <= inner[i] <= 15)
        for axis in range(3):
            self.assertLess(outer[axis], inner[axis])
            self.assertLess(inner[axis+3], outer[axis+3])
        actual_hit = runner.index("await_marker(process, log, MELEE_ARMED")
        actual_hurt = runner.index("await_marker(process, log, MELEE_HURT")
        native_uuid = runner.index("await_marker(process, log, MANUAL_NATIVE_UNSCOPED")
        self.assertTrue(actual_hit < actual_hurt < native_uuid)
        self.assertNotIn("continue_on_error", runner)


if __name__ == "__main__":
    unittest.main()
