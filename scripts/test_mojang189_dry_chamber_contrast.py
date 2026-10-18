"""V166 original Mojang dry health/potion room *source policy* regressions.

These mocked source commands are NOT proof of real in-game potion use.
The full pinned GUI binary must independently confirm positive 1.8.9
integrated-server /fill replies and physical X11 splash/regen.
"""
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import RunMojang189SingleplayerSmoke as smoke


class OriginalDryRoomContrastTest(unittest.TestCase):
    def test_fresh_original_air_preclear_may_be_already_air_but_never_passes_stone(self):
        self.assertEqual(
            "source-already-air",
            smoke.original_dry_chamber_preclear_reply(
                b"[08:52:14] [Client thread/INFO]: [CHAT] No blocks filled\n"))
        self.assertEqual(
            "source-blocks-cleared",
            smoke.original_dry_chamber_preclear_reply(
                b"[08:52:14] [Client thread/INFO]: [CHAT] 729 blocks filled\n"))
        self.assertIsNone(
            smoke.original_dry_chamber_preclear_reply(
                b"[CHAT] Teleported CIProbe to 8.5, 100.0, 8.5\n"))
        self.assertIsNone(
            smoke.original_dry_chamber_preclear_reply(
                b"[CHAT] 0 blocks filled\n"))
        self.assertIsNone(
            smoke.original_dry_chamber_preclear_reply(
                b"[CHAT] Successfully filled zero blocks\n"))

    def test_world_bounds_and_empty_region_fail_closed_at_preclear(self):
        for msg in (
            b"[CHAT] Cannot place blocks outside of the world",
            b"[CHAT] Too many blocks in the specified area",
            b"[CHAT] There are no blocks to fill",
        ):
            with self.subTest(msg=msg):
                with self.assertRaisesRegex(AssertionError, "rejected"):
                    smoke.original_dry_chamber_preclear_reply(msg)

    def run_preclear(self, reply):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "original-client.log"
            log.write_bytes(b"[CHAT] No blocks filled\n")
            actions = []

            def real_original_command(process, logfile, window, command):
                actions.append(command)
                with logfile.open("ab") as f:
                    f.write(reply)

            def positive_original_fill(process, logfile, window, command):
                actions.append(command)

            with patch.object(smoke, "send_command",
                              side_effect=real_original_command), patch.object(
                    smoke, "send_verified_fill",
                    side_effect=positive_original_fill), patch.object(
                    smoke, "require_running"):
                smoke.prepare_verified_native_dry_chamber(
                    object(), log, "source-original-X11")
            return actions

    def test_both_vanilla_preclear_responses_require_two_genuine_later_fills(self):
        expected = [
            "/fill 4 79 4 12 87 12 air",
            "/fill 4 79 4 12 87 12 stone",
            "/fill 5 80 5 11 86 11 air",
        ]
        for reply in (
            b"[CHAT] No blocks filled\n",
            b"[CHAT] 81 blocks filled\n",
        ):
            with self.subTest(reply=reply):
                self.assertEqual(expected, self.run_preclear(reply))

    def test_stale_preexisting_acknowledgement_cannot_complete_preclear(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "original-client.log"
            log.write_bytes(b"[CHAT] No blocks filled\n")
            with patch.object(smoke, "send_command"), patch.object(
                    smoke, "send_verified_fill") as checked, patch.object(
                    smoke, "require_running"), patch.object(
                    smoke.time, "monotonic", side_effect=[0., 9.]):
                with self.assertRaisesRegex(
                        AssertionError, "no fresh original vanilla preclear"):
                    smoke.prepare_verified_native_dry_chamber(
                        object(), log, "X11-source")
                checked.assert_not_called()

    def test_bad_fresh_preclear_never_spends_a_stone_or_air_fill(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "original-client.log"
            log.write_bytes(b"")
            def original_command(_p, target, _w, _cmd):
                with target.open("ab") as f:
                    f.write(b"[CHAT] Cannot place blocks outside of the world\n")
            with patch.object(smoke, "send_command",
                              side_effect=original_command), patch.object(
                    smoke, "send_verified_fill") as checked, patch.object(
                    smoke, "require_running"):
                with self.assertRaisesRegex(AssertionError, "rejected"):
                    smoke.prepare_verified_native_dry_chamber(
                        object(), log, "source")
                checked.assert_not_called()

    def test_original_physical_heal_remains_mandatory_after_room_creation(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        self.assertIn("prepare_verified_native_dry_chamber(process, log, window)",
                      src)
        segment = src.split("prepare_verified_native_dry_chamber(process, log, window)",
                            2)[-1]
        self.assertIn("CUSTOMMC_189_DRY_CHAMBER_BEFORE_PHYSICAL_SPLASH_PASS", segment)
        self.assertIn("physical", segment.lower())
        self.assertIn("Regeneration", segment)
        # No replacement of positive source-world changed-block guards.
        self.assertIn("send_verified_fill(process, log, window,", src)
        self.assertIn("CUSTOMMC_OFFICIAL_189_REAL_PHYSICAL_ROD_REEL_HOOK_CLEARED_PASS", src)
