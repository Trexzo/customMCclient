"""V320: preserve actual original Minecraft 1.8.9 B=0 failure.

Secondary vanilla-server stat.damageDealt and fresh player score lists
are forensic observations only, not attacker-UUID proof or a new PASS.
"""
import ast
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import RunMojang189LocalhostTwoPlayerSmoke as smoke


SOURCE = Path(smoke.__file__)


class V320OriginalServerDamageEvidence(unittest.TestCase):
    def test_original_player_owners_are_exact_and_others_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "server.log"
            log.write_bytes(b"")
            for owner in ("CIHostX", "", "CITarget\nscoreboard players set",
                          "../CIHost", "CITargetB "):
                with self.subTest(owner=owner):
                    with self.assertRaises(ValueError):
                        smoke.v320_original_server_score_query(None, log, owner)

    def test_missing_source_does_not_supply_original_server_evidence(self):
        with tempfile.TemporaryDirectory() as root:
            with self.assertRaises(OSError):
                smoke.v320_original_server_score_query(
                    None, Path(root) / "missing-server.log", "CIHost")

    def test_symlink_source_is_not_native_proof(self):
        with tempfile.TemporaryDirectory() as root:
            file = Path(root) / "vanilla.log"
            file.write_bytes(b"[server thread/info]: Old score=10\n")
            alias = Path(root) / "alias.log"
            alias.symlink_to(file)
            with self.assertRaises(OSError):
                smoke.v320_original_server_score_query(
                    None, alias, "CITargetB")

    def test_exact_real_vanilla_query_reads_only_fresh_server_lines(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "vanilla-dedicated.log"
            log.write_bytes(b"[server thread/info]: stale scoreboard 900\n")
            fresh = (
                b"[Server thread/INFO]: Showing 2 tracked objective(s) for CIHost:\n"
                b"[Server thread/INFO]: - dealt: 10 (dealt)\n"
                b"[Server thread/INFO]: - taken: 0 (taken)\n")
            def emit(server, logfile, command):
                self.assertEqual(command, "scoreboard players list CIHost")
                with Path(logfile).open("ab") as writer:
                    writer.write(fresh)
            with patch.object(smoke, "send_server_command", side_effect=emit) as mock:
                result = smoke.v320_original_server_score_query(
                    object(), log, "CIHost")
                mock.assert_called_once()
            self.assertIn("dealt: 10", result)
            self.assertIn("taken: 0", result)
            self.assertNotIn("stale scoreboard", result)
            self.assertNotIn("PASS=YES", result)

    def test_non_server_lines_cannot_supply_score_source(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "server.log"
            log.write_bytes(b"")
            def emit(server, logfile, command):
                with Path(logfile).open("ab") as writer:
                    writer.write(
                        b"fake CIHost dealt=100\n"
                        b"[client thread/info]: counterfeit 100\n"
                        b"[server thread/info]: actual vanilla score 2\n")
            with patch.object(smoke, "send_server_command", side_effect=emit):
                result = smoke.v320_original_server_score_query(
                    object(), log, "CIHost")
            self.assertEqual(result, "[server thread/info]: actual vanilla score 2")

    def test_read_limit_is_strict_and_cannot_mask_real_negative(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "vanilla.log"
            log.write_bytes(b"")
            def emit(server, logfile, command):
                with Path(logfile).open("ab") as writer:
                    writer.write(b"[server thread/info]: " + b"x" * 8200)
            with patch.object(smoke, "send_server_command", side_effect=emit):
                with self.assertRaisesRegex(AssertionError, "bounded"):
                    smoke.v320_original_server_score_query(object(), log, "CITargetB")

    def test_original_server_failure_survives_score_query_exception(self):
        source = SOURCE.read_text("utf-8")
        area = source[source.index("        if args.simultaneous:\n"):
                      source.index("        if args.multi_target:\n")]
        self.assertIn('"scoreboard players test CITargetB taken 0 0"', area)
        self.assertIn('expected=b"score 0 is in range 0 to 0"', area)
        self.assertIn('V320_ORIGINAL_SERVER_DAMAGE_SOURCE_UNAVAILABLE', area)
        self.assertIn('v311_original_b_failure_evidence(', area)
        self.assertTrue(area.rstrip().endswith(
            'print("CUSTOMMC_OFFICIAL_189_SIMULTANEOUS_BOTH_SERVER_DAMAGE_PASS=YES",\n'
            '                  flush=True)'))
        tree = ast.parse(source)
        original = [n for n in ast.walk(tree) if isinstance(n, ast.Try)
                    and any(isinstance(s, ast.Expr)
                            and isinstance(s.value, ast.Call)
                            and any(isinstance(k, ast.Constant)
                                    and k.value == "scoreboard players test CITargetB taken 0 0"
                                    for k in ast.walk(s))
                            for s in n.body)]
        self.assertEqual(len(original), 1)
        handler = original[0].handlers[0]
        self.assertIsInstance(handler.type, ast.Name)
        self.assertEqual(handler.type.id, "AssertionError")
        self.assertIsInstance(handler.body[-1], ast.Raise)
        self.assertIsNone(handler.body[-1].exc)

    def test_original_vanilla_attacker_dealt_stat_armed_before_aura(self):
        source = SOURCE.read_text("utf-8")
        area = source[source.index("        if args.simultaneous:\n"):
                      source.index("        if args.multi_target:\n")]
        order = [
            area.index('"scoreboard objectives add taken stat.damageTaken"'),
            area.index('"scoreboard objectives add dealt stat.damageDealt"'),
            area.index('"scoreboard players set CIHost dealt 0"'),
            area.index('"scoreboard players test CIHost dealt 0 0"'),
            area.index('"scoreboard players set {} taken 0"'),
            area.index('"CUSTOMMC_189_SIMULTANEOUS_TWO_PLAYERS_IN_REACH_PASS'),
            area.index("wait_for(host, host_log, SIM_HIT_A"),
            area.index('"scoreboard players test CITargetB taken 0 0"'),
            area.index('"V320_ORIGINAL_SERVER_DAMAGE_SOURCE owner="'),
            area.index("send_server_command(server, server_log, b_near)"),
        ]
        self.assertEqual(order, sorted(order))

    def test_previous_real_original_controller_and_B_gate_unchanged(self):
        source = SOURCE.read_text("utf-8")
        area = source[source.index("        if args.simultaneous:\n"):
                      source.index("        if args.multi_target:\n")]
        self.assertIn("v316_preteleport_original_b_priority_marker(", area)
        self.assertIn("v311_original_b_priority_source(", area)
        self.assertIn("wait_for(host, host_log, SIM_HIT_B, deadline", area)
        self.assertIn("wait_for_real_score(server, server_log, \"CITargetB\"", area)
        self.assertIn("CUSTOMMC_OFFICIAL_189_SIMULTANEOUS_BOTH_SERVER_DAMAGE_PASS=YES", area)
        self.assertNotIn("scoreboard players set CITargetB taken 1", area)

    def test_secondary_diagnostic_never_asserts_source_attacker_ownership(self):
        source = SOURCE.read_text("utf-8")
        a = source.index("def v320_original_server_score_query(")
        b = source.index("def write_server_properties(", a)
        helper = source[a:b]
        for forbidden in ("PASS=YES", "host.featureCatalog", "attackEntity",
                          "scoreboard players set ", "scoreboard objectives add",
                          "return True"):
            self.assertNotIn(forbidden, helper)
        self.assertIn('"scoreboard players list " + owner', helper)

    def test_existing_other_scenarios_do_not_collect_attacker_dealt_stat(self):
        source = SOURCE.read_text("utf-8")
        start = source.index("        if args.simultaneous:\n")
        end = source.index("        if args.multi_target:\n")
        self.assertEqual(source.count("scoreboard objectives add dealt stat.damageDealt"), 1)
        self.assertNotIn("stat.damageDealt", source[:start])
        self.assertNotIn("stat.damageDealt", source[end:])


if __name__ == "__main__":
    unittest.main()
