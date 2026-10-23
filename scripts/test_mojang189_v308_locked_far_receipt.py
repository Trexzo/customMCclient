"""Strict source-only regression for V308 genuine locked-target far fixture."""
import importlib
import math
from pathlib import Path
import sys
import tempfile
import unittest

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
source = importlib.import_module("RunMojang189LocalhostTwoPlayerSmoke")


class V308RealLockedFarReceiptTests(unittest.TestCase):
    def test_real_forward_original_server_source_a_far_margin(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d)/"server.log"
            log.write_bytes(b"CIHost[/127.0.0.1:41234] logged in with entity id 5 at (243.5, 4.0, 444.5)\n")
            host, a, b_outer, b_near, a_far = (
                source.actual_server_three_client_locked_fixture(log))
        self.assertEqual("tp CITarget 249 5 446 180 0", a_far)
        hx,hz=(int(v) for v in (host.split()[2],host.split()[4]))
        ax,az=(int(v) for v in (a_far.split()[2],a_far.split()[4]))
        self.assertGreater(math.hypot(ax-hx,az-hz),5.5)
        self.assertIn("tp CITargetB",b_near)

    def test_real_reverse_original_server_source_a_far_margin(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d)/"server.log"
            log.write_bytes(b"CIHost[/127.0.0.1:41234] logged in with entity id 5 at (-33.5, 4.0, -33.5)\n")
            host, a, b_outer, b_near, a_far = (
                source.actual_server_three_client_locked_fixture(log))
        self.assertEqual("tp CITarget -28 5 -36 180 0", a_far)
        self.assertGreater(
            math.hypot(int(a_far.split()[2])-int(host.split()[2]),
                       int(a_far.split()[4])-int(host.split()[4])),
            5.5)

    def test_strict_b_source_and_original_native_locked_acceptance_retained(self):
        src=(HERE/"RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        stage=src.split("if args.locked_target:",1)[1].split("if args.simultaneous:",1)[0]
        marker="CUSTOMMC_189_LOCK_A_FAR_REAL_SERVER_TELEPORT_ACK=YES"
        self.assertIn("expected=original_locked_tp_server_reply(a_far)",stage)
        self.assertIn(marker,stage)
        self.assertIn("wait_for(host, host_log, LOCK_RELEASE, deadline,",stage)
        self.assertLess(stage.index("expected=original_locked_tp_server_reply(a_far)"),
                        stage.index("wait_for(host, host_log, LOCK_RELEASE, deadline,"))
        self.assertIn("wait_for(host, host_log, LOCK_HIT_B, deadline,",stage)
        self.assertIn("wait_for_real_score(server, server_log, \"CITargetB\", deadline)",stage)
        java=(HERE.parent/"platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9/Minecraft189LockedTargetAcceptance.java").read_text()
        self.assertIn("return da>4.5 && db>=1 && db<=3.5 && selected(b);",java)
        self.assertIn("e.aFarBSelected()",java)

    def test_existing_early_target_switch_source_fixture_unchanged(self):
        src=(HERE/"RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        early=src.split("def actual_server_three_client_target_switch_fixture",1)[1].split("def actual_server_three_client_simultaneous_fixture",1)[0]
        self.assertIn('a_far = "tp CITarget {} {} {} 180 0".format(x + 5, y, z)',early)


if __name__ == "__main__":
    unittest.main()
