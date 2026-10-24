"""V320 authentic original Mojang controller-argument UUID provenance.

Server B=10 with *no* original B-priority marker is an adverse result.
This optional source observation distinguishes a native controller B argument
from other causes but MUST NOT certify missing events, relax B=0 or hit B.
"""
from pathlib import Path
import tempfile
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    official_offline_uuid,
    v320_original_controller_argument_source as source,
)


def entry(number=1, owner="A", phase=1, first=True):
    owners = {
        "A": official_offline_uuid("CITarget"),
        "B": official_offline_uuid("CITargetB"),
        "OTHER": "34567890-abcd-3333-8333-34567890abcd",
        "UNAVAILABLE": "null",
    }
    access = "false" if owner == "UNAVAILABLE" else "true"
    return (
        "CUSTOMMC_189_SIMULTANEOUS_ORIGINAL_CONTROLLER_ARGUMENT_DIAG "
        f"event={number} phase={phase} "
        f"firstOwnedClick={str(first).lower()} "
        f"nativeUuidAccess={access} nativeUuid={owners[owner]} "
        f"owner={owner}\n"
    ).encode("ascii")


class RealControllerArgumentProvenance(unittest.TestCase):
    def evidence(self, payload, start=0, end=None):
        with tempfile.TemporaryDirectory() as root:
            logfile = Path(root) / "original-host.log"
            logfile.write_bytes(payload)
            return source(logfile, start, len(payload) if end is None else end)

    def test_real_a_controller_argument_only(self):
        lines = self.evidence(b"original callback\n" + entry())
        self.assertEqual(len(lines), 1)
        self.assertIn("owner=A", lines[0])
        self.assertNotIn("PASS=YES", str(lines))

    def test_genuine_early_b_argument_is_diagnostic_never_a_pass(self):
        lines = self.evidence(entry(1, "A") + entry(2, "B"))
        self.assertEqual(len(lines), 2)
        self.assertIn("owner=B", lines[-1])
        self.assertNotIn("BOTH_SERVER_DAMAGE_PASS", str(lines))
        self.assertNotIn("PASS=YES", str(lines))

    def test_native_source_absent_does_not_fabricate_b_owner(self):
        original_v319_negative = (
            b"CUSTOMMC_189_SIMULTANEOUS_UUID_A_OWNED_ATTACK_PASS=YES\n"
            b"CUSTOMMC_189_SIMULTANEOUS_EARLY_B_DAMAGE_SOURCE_DIAG "
            b"CUSTOMMC_189_SIMULTANEOUS_B_PRETELEPORT_SOURCE_DIAG "
            b"completeMarker=ABSENT\n"
        )
        self.assertEqual(self.evidence(original_v319_negative), [])

    def test_original_phase_and_native_uuid_source_validation(self):
        for owner in ("A", "B", "OTHER", "UNAVAILABLE"):
            with self.subTest(owner=owner):
                row = self.evidence(entry(owner=owner))
                self.assertEqual(len(row), 1)
                self.assertIn("owner=" + owner, row[0])

    def test_actual_other_owner_cannot_impersonate_a_or_b(self):
        wrong = entry(owner="A").replace(b"owner=A", b"owner=B")
        with self.assertRaisesRegex(AssertionError, "owner mismatch"):
            self.evidence(wrong)
        wrong = entry(owner="B").replace(b"owner=B", b"owner=OTHER")
        with self.assertRaisesRegex(AssertionError, "owner mismatch"):
            self.evidence(wrong)

    def test_invalid_or_unavailable_native_uuid_never_names_owner(self):
        wrong = entry(owner="UNAVAILABLE").replace(
            b"nativeUuid=null", b"nativeUuid=34567890-abcd-3333-8333-34567890abcd")
        with self.assertRaisesRegex(AssertionError, "owner mismatch"):
            self.evidence(wrong)
        wrong = entry(owner="A").replace(
            b"nativeUuidAccess=true", b"nativeUuidAccess=false")
        with self.assertRaisesRegex(AssertionError, "owner mismatch"):
            self.evidence(wrong)

    def test_no_stale_prior_run_marker_after_current_cursor(self):
        a = entry(owner="B")
        current = b"current runtime original scene\n" + entry(owner="A")
        lines = self.evidence(a + current, start=len(a))
        self.assertEqual(len(lines), 1)
        self.assertIn("owner=A", lines[0])

    def test_no_future_native_source_can_backfill_before_teleport(self):
        early = entry(owner="A")
        later = entry(number=2, owner="B", phase=2)
        lines = self.evidence(early + later, end=len(early))
        self.assertEqual(len(lines), 1)
        self.assertIn("owner=A", lines[0])
        self.assertNotIn("owner=B", str(lines))

    def test_partial_last_java_log_line_has_no_original_authority(self):
        early = entry(owner="A")
        unfinished = entry(number=2, owner="B").rstrip(b"\n")
        self.assertEqual(len(self.evidence(early + unfinished)), 1)
        self.assertIn("owner=A", self.evidence(early + unfinished)[0])

    def test_duplicate_decreasing_native_sequence_fails_closed(self):
        with self.assertRaisesRegex(AssertionError, "event order"):
            self.evidence(entry(2) + entry(1, "B"))
        with self.assertRaisesRegex(AssertionError, "event order"):
            self.evidence(entry(1) + entry(1, "B"))

    def test_malformed_prefixed_source_cannot_earn_identity(self):
        with self.assertRaisesRegex(AssertionError, "malformed"):
            self.evidence(entry().replace(
                b"owner=A", b"owner=A extra=synthetic"))
        self.assertEqual(self.evidence(b"fake " + entry()), [])

    def test_invalid_byte_cursors_and_unbounded_input_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            logfile = Path(root) / "original-host.log"
            logfile.write_bytes(entry())
            for begin, end in ((-1, 1), (6, 5),
                               (0, 2_097_153), (0, len(entry()) + 1),
                               (False, 0), (0, True), ("0", 4)):
                with self.subTest(begin=begin, end=end):
                    with self.assertRaises(AssertionError):
                        source(logfile, begin, end)

    def test_cap_enforces_only_thirty_two_original_controller_events(self):
        accepted = b"".join(entry(n, "A") for n in range(1, 33))
        self.assertEqual(len(self.evidence(accepted)), 32)
        with self.assertRaisesRegex(AssertionError, "exceeds native cap"):
            self.evidence(accepted + entry(33, "B"))

    def test_strict_original_b_zero_test_always_fails_before_diagnostic(self):
        code = Path(__file__).with_name(
            "RunMojang189LocalhostTwoPlayerSmoke.py").read_text("utf-8")
        block = code[code.index("        if args.simultaneous:\n"):
                     code.index("        if args.multi_target:\n",
                                code.index("        if args.simultaneous:\n"))]
        score = block.index('"scoreboard players test CITargetB taken 0 0"')
        first_diag = block.index(
            "v320_original_controller_argument_source(")
        native_tp = block.index("send_server_command(server, server_log, b_near)")
        self.assertLess(score, first_diag)
        self.assertLess(first_diag, native_tp)
        self.assertIn("except AssertionError:", block)
        self.assertIn("v311_original_b_failure_evidence(", block)
        self.assertIn("raise\n", block)
        self.assertNotIn("server damage assumed", block)

    def test_native_controller_hook_and_diagnostic_do_not_authorize_attack(self):
        root = Path(__file__).parents[1]
        j = (root / "platform-1.8.9" / "src" / "main" / "java" /
             "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9")
        bridge = (j / "Minecraft189RuntimeBridge.java").read_text("utf-8")
        acceptance = (j / "Minecraft189SimultaneousTargetAcceptance.java"
                      ).read_text("utf-8")
        self.assertIn(".observeOriginalControllerArgument(entity)", bridge)
        self.assertIn("nativeEntity instanceof Minecraft189NativeEntityUuidAccess",
                      acceptance)
        self.assertIn("originalControllerArgumentsObserved >= 32",
                      acceptance)
        self.assertIn("System.out.println(", acceptance)
        diag = acceptance[
            acceptance.index("static String nativeControllerOwner("):
            acceptance.index("static void observe(final Minecraft189HostRuntime host)")]
        self.assertNotIn("attackEntity(", diag)
        self.assertNotIn("send", diag)
        self.assertNotIn("PASS=YES", diag)


if __name__ == "__main__":
    unittest.main()
