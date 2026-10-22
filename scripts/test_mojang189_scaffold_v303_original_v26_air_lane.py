"""V303: genuine original V26 EAST cursor ray-lane source-contract tests.

These are Python harness tests only: they do not fabricate a Minecraft
World, native mouse-over ray, selected ItemBlock or original success marker.
"""
import ast
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase
from unittest.mock import patch

import RunMojang189SingleplayerSmoke as smoke


class OriginalV26NativeCursorLaneTests(TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = Path(smoke.__file__).read_text(encoding="utf-8")
        tree = ast.parse(cls.source)
        cls.helper = ast.get_source_segment(cls.source, next(
            n for n in tree.body if isinstance(n, ast.FunctionDef)
            and n.name == "prepare_verified_original_v26_candidate_source_lane"))
        start=cls.source.index("            if native_scaffold_candidate:",
                               cls.source.index("with tempfile.TemporaryDirectory"))
        end=cls.source.index("            if native_scaffold_raytrace:",start)
        cls.scenario=cls.source[start:end]

    def run_mock(self):
        with TemporaryDirectory() as td:
            log = Path(td)/"original-client.txt"
            log.write_bytes(b"original\n")
            calls=[]
            def named(_p,_log,_window,command,expected):
                calls.append(("source",command,expected))
            def air(_p,_log,_window,command,*_args):
                calls.append(("air-preclear",command))
            def changed(_p,_log,_window,command):
                calls.append(("positive-fill",command))
            with patch.object(smoke,"send_verified_original_chat_reply",
                              side_effect=named), patch.object(
                    smoke,"send_verified_original_air_preclear",
                    side_effect=air), patch.object(
                    smoke,"send_verified_fill",side_effect=changed):
                cursor=smoke.prepare_verified_original_v26_candidate_source_lane(
                    object(),log,"original-X11")
            return calls,cursor,len(b"original\n")

    def test_original_V26_real_high_tp_before_any_world_change(self):
        calls,_,_=self.run_mock()
        self.assertEqual(("source","/tp CIProbe 0 120 0 0 0",
                          b"[CHAT] Teleported CIProbe to 0.5, 120.0, 0.5"),
                         calls[0])

    def test_actual_air_lane_clears_observed_original_wrong_hit_at_1_101_0(self):
        calls,_,_=self.run_mock()
        self.assertEqual(("air-preclear","/fill 0 100 0 1 102 0 air"),
                         calls[1])
        # V301 second native V26 hit was at real (1,101,0); only this
        # air column was previously unverified before the native ray.
        self.assertIn((1,101,0),[(x,y,0) for x in (0,1)
                                       for y in (100,101,102)])

    def test_original_support_stone_must_change_after_air_contrast(self):
        calls,_,_=self.run_mock()
        self.assertEqual(("air-preclear","/fill 0 99 0 0 99 0 air"),
                         calls[2])
        self.assertEqual(("positive-fill","/fill 0 99 0 0 99 0 stone"),
                         calls[3])
        self.assertEqual(1,self.helper.count("send_verified_fill("))

    def test_original_neighbor_air_before_exact_native_reseat(self):
        calls,_,_=self.run_mock()
        self.assertEqual(("air-preclear","/fill 1 99 0 1 99 0 air"),
                         calls[4])
        self.assertEqual(("source","/tp CIProbe 1.05 100 0.5 90 88.6",
                          b"[CHAT] Teleported CIProbe to 1.05, 100.0, 0.5"),
                         calls[5])

    def test_post_reseat_original_world_material_checks_are_exact_and_ordered(self):
        calls,_,_=self.run_mock()
        self.assertEqual([
            ("/testforblock 0 99 0 stone",b"[CHAT] Successfully found the block at 0,99,0."),
            ("/testforblock 1 99 0 air",b"[CHAT] Successfully found the block at 1,99,0."),
            ("/testforblock 1 100 0 air",b"[CHAT] Successfully found the block at 1,100,0."),
            ("/testforblock 1 101 0 air",b"[CHAT] Successfully found the block at 1,101,0."),
            ("/testforblock 1 102 0 air",b"[CHAT] Successfully found the block at 1,102,0."),
        ],[(a,b) for kind,a,b in calls[6:] if kind=="source"])

    def test_each_original_source_reply_is_named_and_exact_no_spaced_coordinates(self):
        calls,_,_=self.run_mock()
        for kind,command,expected in (c for c in calls if c[0]=="source"):
            with self.subTest(command=command):
                self.assertTrue(expected.startswith(b"[CHAT] "))
                # Original 1.8.9 /tp replies use comma+space, but the
                # authentic /testforblock block-coordinate reply does not.
                if command.startswith("/testforblock "):
                    self.assertNotIn(b", ",expected)
                else:
                    self.assertTrue(command.startswith("/tp CIProbe "))
                    self.assertIn(b", ",expected)
                self.assertNotIn(b"PASS=YES",expected)

    def test_native_cursor_after_real_low_tp_never_uses_previous_ground_samples(self):
        self.assertIn("v26_post_tp_cursor = prepare_verified_original_v26_candidate_source_lane(",
                      self.scenario)
        self.assertIn("since=v26_post_tp_cursor",self.scenario)
        self.assertNotIn("native_safe_walk_positions(log)[-5:]",self.scenario)
        self.assertIn("original_edge_grounding_stable(readings)",self.scenario)
        self.assertIn("time.monotonic() - stable_start < 12",self.scenario)

    def test_native_grant_and_real_original_candidate_witness_still_required(self):
        self.assertIn('"/give CIProbe stone 8"',self.scenario)
        self.assertIn("candidate_tag = b\"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_NATIVE_CANDIDATE_PASS=YES\"",
                      self.scenario)
        self.assertIn("await_marker(process, log, candidate_tag, deadline,",
                      self.scenario)
        self.assertLess(self.scenario.index("original_edge_grounding_stable(readings)"),
                        self.scenario.index('"/give CIProbe stone 8"'))

    def test_error_on_first_real_source_tp_stops_all_later_source_work(self):
        calls=[]
        def reject(_p,_log,_window,command,_expected):
            calls.append(command)
            raise AssertionError("original high TP was never confirmed")
        with TemporaryDirectory() as td:
            log=Path(td)/"original.txt"
            log.write_bytes(b"")
            with patch.object(smoke,"send_verified_original_chat_reply",
                              side_effect=reject), patch.object(
                    smoke,"send_verified_fill") as fill,patch.object(
                    smoke,"send_verified_original_air_preclear") as air:
                with self.assertRaisesRegex(AssertionError,"high TP"):
                    smoke.prepare_verified_original_v26_candidate_source_lane(
                        object(),log,"X11")
                fill.assert_not_called()
                air.assert_not_called()
        self.assertEqual(["/tp CIProbe 0 120 0 0 0"],calls)

    def test_original_second_air_preclear_rejection_cannot_create_stone(self):
        calls=[]
        def air(_p,_log,_window,command,*_args):
            calls.append(command)
            if "0 99 0 0 99 0" in command:
                raise AssertionError("original support reset refused")
        with TemporaryDirectory() as td:
            log=Path(td)/"original.txt"
            log.write_bytes(b"")
            with patch.object(smoke,"send_verified_original_chat_reply"), patch.object(
                    smoke,"send_verified_original_air_preclear",
                    side_effect=air), patch.object(
                    smoke,"send_verified_fill") as fill:
                with self.assertRaisesRegex(AssertionError,"support reset refused"):
                    smoke.prepare_verified_original_v26_candidate_source_lane(
                        object(),log,"X11")
                fill.assert_not_called()
        self.assertEqual(["/fill 0 100 0 1 102 0 air",
                          "/fill 0 99 0 0 99 0 air"],calls)

    def test_cursor_is_real_original_log_offset_not_synthetic_tick(self):
        _,cursor,size=self.run_mock()
        self.assertEqual(size,cursor)
        self.assertIn("log.stat().st_size if log.exists() else 0",self.helper)
        self.assertIn("return post_tp_source_cursor",self.helper)
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_",self.helper)

    def test_source_setup_cannot_pretend_to_be_client_ray_or_click(self):
        for banned in ("WorldClient(", "setBlockState(", "rayTraceBlocks(",
                       "onPlayerRightClick(", "ItemStack(", "sendPacket(",
                       "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_NATIVE_CANDIDATE_PASS"):
            with self.subTest(banned=banned):
                self.assertNotIn(banned,self.helper)

    def test_no_success_on_wrong_source_cells_or_stale_chat(self):
        # The real source helper used by each /testforblock still captures
        # its own new log offset; V303 does not add a generic source match.
        source=self.source
        start=source.index("def send_verified_original_chat_reply(")
        end=source.index("def send_verified_chunk_teleport(",start)
        reader=source[start:end]
        self.assertIn("cursor = log.stat().st_size",reader)
        self.assertIn("handle.seek(cursor)",reader)
        self.assertIn("if expected in fresh:",reader)
        self.assertIn("raise AssertionError(",reader)


if __name__=="__main__":
    import unittest
    unittest.main()
