"""V307 strict original three-client fixture safety; source tests are not native proof."""
import ast
from pathlib import Path
import tempfile
import unittest
import RunMojang189LocalhostTwoPlayerSmoke as smoke

HOST="tp CIHost 243 5 444 0 0"
A="tp CITarget 243 5 446 180 0"
B="tp CITargetB 244 5 446 180 0"

class GenuineUuidWorldFixtureV307Tests(unittest.TestCase):
    def test_positive_full_rejoin_platform_source_cells(self):
        fill, cells=smoke.actual_server_rejoin_three_player_floor_fixture(HOST,A,B)
        self.assertEqual(fill, "fill 241 4 441 245 4 447 stone")
        self.assertEqual(cells, ((243,4,444),(243,4,446),(244,4,446)))

    def test_original_negative_chunk_coordinates(self):
        fill,cells=smoke.actual_server_rejoin_three_player_floor_fixture(
            "tp CIHost -34 5 -34 180 0",
            "tp CITarget -34 5 -36 0 0",
            "tp CITargetB -35 5 -36 180 0")
        self.assertEqual(fill,"fill -36 4 -37 -32 4 -31 stone")
        self.assertEqual(cells[2],(-35,4,-36))

    def test_unknown_player_owner_fails_closed(self):
        with self.assertRaisesRegex(AssertionError,"owners"):
            smoke.actual_server_rejoin_three_player_floor_fixture(
                HOST,A,B.replace("CITargetB","Other"))

    def test_incomplete_command_fails_closed(self):
        with self.assertRaisesRegex(AssertionError,"complete"):
            smoke.actual_server_rejoin_three_player_floor_fixture(
                HOST,A,B.rsplit(" ",1)[0])

    def test_nonintegral_server_coordinates_rejected(self):
        with self.assertRaisesRegex(AssertionError,"nonintegral"):
            smoke.actual_server_rejoin_three_player_floor_fixture(
                HOST,A,B.replace("244 ","244.5 "))

    def test_misplaced_b_or_different_height_rejected(self):
        for bad in (B.replace(" 446 "," 447 "),B.replace(" 5 "," 6 ")):
            with self.subTest(bad=bad), self.assertRaisesRegex(
                    AssertionError,"geometry"):
                smoke.actual_server_rejoin_three_player_floor_fixture(
                    HOST,A,bad)

    def test_real_world_height_and_border_rejected(self):
        h=HOST.replace(" 5 "," 253 ")
        a=A.replace(" 5 "," 253 ")
        b=B.replace(" 5 "," 253 ")
        with self.assertRaisesRegex(AssertionError,"bounded"):
            smoke.actual_server_rejoin_three_player_floor_fixture(h,a,b)
        with self.assertRaisesRegex(AssertionError,"bounded"):
            smoke.actual_server_rejoin_three_player_floor_fixture(
                HOST.replace("243","998"),
                A.replace("243","998"),
                B.replace("244","999"))

    def test_real_locked_a_far_is_12_server_cells(self):
        with tempfile.TemporaryDirectory() as directory:
            path=Path(directory)/"dedicated.log"
            path.write_bytes(
                b"CIHost[/127.0.0.1:42110] logged in with entity id 5 "
                b"at (243.5, 4.0, 444.5)\\n")
            host,a,b0,b1,far=smoke.actual_server_three_client_locked_fixture(path)
            self.assertEqual(far,"tp CITarget 255 5 446 180 0")
            self.assertEqual(int(far.split()[2])-int(a.split()[2]),12)
            self.assertEqual(
                smoke.original_locked_tp_server_reply(far),
                b"Teleported CITarget to 255.5, 5.0, 446.5")

    def test_real_out_of_world_far_fails_closed(self):
        with tempfile.TemporaryDirectory() as directory:
            path=Path(directory)/"dedicated.log"
            path.write_bytes(
                b"CIHost[/127.0.0.1:42110] logged in with entity id 5 "
                b"at (994.5, 4.0, 444.5)\\n")
            with self.assertRaisesRegex(AssertionError,"outside server world"):
                smoke.actual_server_three_client_locked_fixture(path)

    def test_fresh_vanilla_fill_receipts_include_idempotent_existing_stone(self):
        valid = (
            b"[16:07:40] [Server thread/INFO]: 35 blocks filled\n",
            b"[16:07:40] [Server thread/INFO]: No blocks filled\n")
        for receipt in valid:
            with self.subTest(receipt=receipt):
                self.assertTrue(
                    smoke.actual_server_rejoin_floor_fill_receipt(receipt))

    def test_invalid_floor_receipts_fail_closed(self):
        invalid = (
            b"", b"no blocks filled",
            b"[16:07:40] [Server thread/INFO]: 34 blocks filled\n",
            b"[16:07:40] [Server thread/INFO]: No blocks filled malicious\n",
            b"[16:07:40] [Server thread/INFO]: Unknown or incomplete command\n",
            b"[16:07:40] [Server thread/WARN]: No blocks filled\n")
        for receipt in invalid:
            with self.subTest(receipt=receipt):
                self.assertFalse(
                    smoke.actual_server_rejoin_floor_fill_receipt(receipt))

    def test_runner_has_positive_support_receipts_before_actual_tp(self):
        text=Path(smoke.__file__).read_text(encoding="utf-8")
        node=ast.parse(text)
        blocks=[
            ast.get_source_segment(text,n) or "" for n in ast.walk(node)
            if isinstance(n,ast.If) and isinstance(n.test,ast.BoolOp)
            and isinstance(n.test.op,ast.Or)
            and "if args.rejoin_target or args.rejoin_release:"
                in (ast.get_source_segment(text,n) or "")[:70]
            and "actual_server_rejoin_three_player_floor_fixture("
                in (ast.get_source_segment(text,n) or "")]
        self.assertTrue(blocks)
        block=max(blocks,key=len)
        for token in (
                "actual_server_rejoin_three_player_floor_fixture(",
                'expected=b"blocks filled"',
                "actual_server_rejoin_floor_fill_receipt(fill_reply)",
                'expected=b"successfully found the block"',
                "expected=original_locked_tp_server_reply(b_outer)",
                "wait_for(host, host_log, REJOIN_READY, deadline,"):
            self.assertIn(token,block)
        self.assertLess(block.index("support_fill, support_cells"),
                        block.index("send_server_command(server, server_log, host_tp)"))
        self.assertLess(block.index("expected=original_locked_tp_server_reply(b_outer)"),
                        block.index("wait_for(host, host_log, REJOIN_READY"))

    def test_native_uuid_and_damage_gates_remain_exact(self):
        text=Path(smoke.__file__).read_text(encoding="utf-8")
        for token in (
                "wait_for(host, host_log, LOCK_READY, deadline,",
                "wait_for(host, host_log, LOCK_ARMED, deadline,",
                "wait_for(host, host_log, LOCK_RETAINED, deadline,",
                "wait_for(host, host_log, LOCK_RELEASE, deadline,",
                "wait_for(host, host_log, LOCK_HIT_B, deadline,",
                'wait_for_real_score(server, server_log, "CITargetB", deadline)',
                "wait_for(host, host_log, REJOIN_READY, deadline,"):
            self.assertIn(token,text)
        src=Path(__file__).resolve().parents[1]/"platform-1.8.9"/"src"/"main"/"java"/"dev"/"trexzo"/"custommc"/"platform"/"v1_8_9"/"Minecraft189LockedTargetAcceptance.java"
        lock=src.read_text(encoding="utf-8")
        self.assertIn("return da>4.5 && db>=1 && db<=3.5 && selected(b);",lock)
        self.assertIn('"CUSTOMMC_189_LOCK_A_INELIGIBLE_RELEASES_TO_B_PASS=YES"',lock)

if __name__=="__main__":
    unittest.main()
