"""Offline fail-closed coverage for the original 1.8.9 Survival fixture.

The original graphical runner is the only authority for a REAL server fill.
These tests exercise log freshness and fail-closed rejection without implying
that the fake log has established a Minecraft in-world success.
"""
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase
from unittest.mock import patch

import RunMojang189SingleplayerSmoke as smoke


class Alive:
    def poll(self):
        return None


class DryChamberFillTest(TestCase):
    def check_reply(self, old_log, new_reply, should_fail):
        with TemporaryDirectory() as directory:
            log = Path(directory) / "game.log"
            log.write_bytes(old_log)

            def chat(_process, game_log, _window, _command):
                with game_log.open("ab") as handle:
                    handle.write(new_reply)

            with patch.object(smoke, "send_command", side_effect=chat):
                if should_fail:
                    with self.assertRaisesRegex(
                            AssertionError, "vanilla refused the dry chamber"):
                        smoke.send_verified_fill(
                            Alive(), log, "mock-window",
                            "/fill 4 79 4 12 87 12 stone")
                else:
                    smoke.send_verified_fill(
                        Alive(), log, "mock-window",
                        "/fill 4 79 4 12 87 12 stone")

    def test_does_not_reuse_old_success_when_new_fill_fails(self):
        self.check_reply(
            b"[CHAT] 729 blocks filled\\n",
            b"[CHAT] Cannot place blocks outside of the world\\n", True)

    def test_requires_real_new_success_even_after_old_failure(self):
        self.check_reply(
            b"[CHAT] Cannot place blocks outside of the world\\n",
            b"[CHAT] 343 blocks filled\\n", False)

    def test_fails_closed_on_unloaded_chunk(self):
        self.check_reply(
            b"", b"[CHAT] Cannot place blocks outside of the world\\n", True)

    def test_actual_chunk_preload_requires_new_vanilla_teleport_reply(self):
        with TemporaryDirectory() as directory:
            log = Path(directory) / "game.log"
            log.write_bytes(
                b"[CHAT] Teleported CIProbe to 8.5, 100.0, 8.5\\n")

            def next_chat(_process, game_log, _window, command):
                self.assertEqual(command, "/tp CIProbe 8 100 8")
                with game_log.open("ab") as handle:
                    handle.write(
                        b"[CHAT] Teleported CIProbe to 8.5, 100.0, 8.5\\n")

            with patch.object(smoke, "send_command", side_effect=next_chat):
                with patch.object(smoke.time, "sleep"):
                    smoke.send_verified_chunk_teleport(
                        Alive(), log, "mock-window")

    def test_native_splash_and_survival_share_prepared_original_room(self):
        source = Path(smoke.__file__).read_text(encoding="utf-8")
        # The entire original physical throw must happen AFTER the genuine
        # vanilla world accepts both fills; don't merely make a dry stage
        # that runs only AFTER the throw has already failed.
        within = source.split("            if native_rod_potion_discovery:", 1)[1]
        fixture = within.index("CUSTOMMC_189_DRY_CHAMBER_BEFORE_PHYSICAL_SPLASH_PASS")
        preparation = within.index(
            "prepare_verified_native_dry_chamber(process, log, window)")
        physical = within.index("await_marker(process, log, NATIVE_REAL_SPLASH_READY")
        real_effect = within.index("await_marker(process, log, NATIVE_REAL_SPLASH_EFFECT")
        survival = within.index('"/gamemode survival CIProbe"')
        # Both mandatory positive fills are now in a single reusable
        # original-command helper. Check its body AND verify it is called
        # before any genuine physical splash or survival confirmation.
        helper = source.split("def prepare_verified_native_dry_chamber(", 1)[1]
        helper = helper.split("def prepare_native_scaffold_context_chamber(", 1)[0]
        preclear = helper.index('"/fill 4 79 4 12 87 12 air"')
        fill_outer = helper.index('"/fill 4 79 4 12 87 12 stone"')
        fill_inner = helper.index('"/fill 5 80 5 11 86 11 air"')
        self.assertLess(preclear, fill_outer)
        self.assertLess(fill_outer, fill_inner)
        self.assertEqual(helper.count("send_verified_fill(process, log, window,"), 2)
        self.assertLess(preparation, fixture)
        self.assertLess(fixture, physical)
        self.assertLess(physical, real_effect)
        self.assertLess(real_effect, survival)
        self.assertEqual(within.count(
            "prepare_verified_native_dry_chamber(process, log, window)"), 1)

    def test_dry_chamber_is_single_chunk_and_enclosed(self):
        # 1.8.9 /fill must not depend on unloaded x=-1 or z=-1 chunks.
        # Both cuboids are entirely within block-coordinate chunk (0,0).
        stone = (4, 79, 4, 12, 87, 12)
        air = (5, 80, 5, 11, 86, 11)
        self.assertTrue(all(0 <= stone[i] < 16 for i in (0, 2, 3, 5)))
        self.assertTrue(all(0 <= air[i] < 16 for i in (0, 2, 3, 5)))
        self.assertTrue(stone[0] < air[0] <= air[3] < stone[3])
        self.assertTrue(stone[1] < air[1] <= air[4] < stone[4])
        self.assertTrue(stone[2] < air[2] <= air[5] < stone[5])
        text = Path(smoke.__file__).read_text(encoding="utf-8")
        self.assertIn('"/fill 4 79 4 12 87 12 stone"', text)
        self.assertIn('"/fill 5 80 5 11 86 11 air"', text)
        self.assertIn('"/tp CIProbe 8 80 8"', text)
        self.assertIn('"/gamemode creative CIProbe"', text)
        self.assertIn('"/tp CIProbe 8 100 8"', text)
        self.assertLess(text.index('"/tp CIProbe 8 100 8"'),
                        text.index('"/fill 4 79 4 12 87 12 stone"'))
        self.assertNotIn('"/fill -3 79 -3 3 79 3 stone"', text)


if __name__ == "__main__":
    import unittest
    unittest.main()
