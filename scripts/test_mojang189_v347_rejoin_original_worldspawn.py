"""V347 bounded original-world rejoin spawn, no fabricated PvP evidence."""
from pathlib import Path
import importlib.util
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPTS = ROOT / "scripts"
HARNESS_SOURCE = (SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py"
                  ).read_text(encoding="utf-8")


def load_harness():
    sys.path.insert(0, str(SCRIPTS))
    spec = importlib.util.spec_from_file_location(
        "v347_rejoin_native_spawn_harness",
        SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class V347OriginalServerSpawnEvidence(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.harness = load_harness()

    def test_source_command_before_any_original_client_is_rejoin_only(self):
        run = HARNESS_SOURCE.split("def run(", 1)[1].split(
            "    except BaseException:", 1)[0]
        native_ready = run.index("CUSTOMMC_OFFICIAL_189_LOCAL_DEDICATED_SERVER_READY")
        # An earlier condition also controls fixture log bookkeeping.
        # Bind this assertion to the actual server-ready command block.
        conditional = run.index(
            "if args.rejoin_target or args.rejoin_release:", native_ready)
        native_set = run.index('"setworldspawn 0 4 0"')
        native_ack = run.index('expected=b"Set world spawn to"')
        native_host = run.index("host_cmd = client_command(")
        self.assertLess(native_ready, conditional)
        self.assertLess(conditional, native_set)
        self.assertLess(native_set, native_ack)
        self.assertLess(native_ack, native_host)
        self.assertIn("send_server_command(server, server_log,", run)
        self.assertIn("wait_for(server, server_log, HOST_JOIN,", run)

    def test_server_login_remains_only_source_for_original_combat_coordinates(self):
        with tempfile.TemporaryDirectory() as folder:
            source = Path(folder) / "server.log"
            source.write_text(
                "[Server thread/INFO]: CIHost[/127.0.0.1:53219] "
                "logged in with entity id 10 at (0.5, 4.0, 0.5)\n",
                encoding="ascii")
            host, target = self.harness.actual_server_spawn_fixture(source)
            self.assertEqual("tp CIHost 0 5 0 0 0", host)
            self.assertEqual("tp CITarget 0 5 4 180 0", target)
            # A bare command acknowledgement alone is never an admissible
            # original spawn source without the real dedicated login line.
            source.write_text(
                "[Server thread/INFO]: Set world spawn to (0, 4, 0)\n",
                encoding="ascii")
            with self.assertRaises(AssertionError):
                self.harness.actual_server_spawn_fixture(source)

    def test_world_boundary_and_original_uuid_damage_gates_unchanged(self):
        self.assertIn('"max-world-size=1024\\n"', HARNESS_SOURCE)
        self.assertIn(
            'raise AssertionError("original rejoin login outside 1024 server world")',
            HARNESS_SOURCE)
        for evidence in (
            "wait_for(host, host_log, CONTROLLER_ATTACK,",
            "TRIGGER_HURT if args.triggerbot_pvp else AURA_HURT,",
            "wait_for(host, host_log, REJOIN_B_HIT, deadline,",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(evidence, HARNESS_SOURCE)
        self.assertNotIn("V347_NATIVE_PVP_SUCCESS", HARNESS_SOURCE)


if __name__ == "__main__":
    unittest.main()
