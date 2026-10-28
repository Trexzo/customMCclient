"""V356 original-host failure log cannot discard first B UUID lock transition.

Source contract only; the genuine Mojang 1.8.9 server/native client run
must still meet all original rejoin, controller and server-damage gates.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
HARNESS = (ROOT / "scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")
OWNER = (ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/"
         "platform/v1_8_9/Minecraft189RejoinTargetAcceptance.java").read_text(
             encoding="utf-8")


class OriginalHostTraceExtractionV356(unittest.TestCase):
    def test_first_transitions_are_read_from_whole_native_host_log(self):
        self.assertIn("witness = b\"CUSTOMMC_189_REJOIN_NATIVE_LOCK_TRANSITION_DIAG\"",
                      HARNESS)
        self.assertIn('with log.open("rb") as original_host_log:', HARNESS)
        self.assertIn("for native_line in original_host_log:", HARNESS)
        self.assertIn("total_transitions += 1", HARNESS)
        self.assertIn("len(lock_transitions) < 16", HARNESS)
        self.assertIn("native_line[:1200].decode(", HARNESS)
        self.assertIn("CUSTOMMC_189_V356_ORIGINAL_HOST_LOCK_TRANSITIONS_START",
                      HARNESS)
        self.assertIn("CUSTOMMC_189_V356_ORIGINAL_HOST_LOCK_TRANSITIONS_END",
                      HARNESS)

    def test_diagnostic_is_fail_only_and_no_new_success_oracle(self):
        x = HARNESS.rindex('    except BaseException:\n')
        y = HARNESS.index('    finally:\n', x)
        handler = HARNESS[x:y]
        self.assertIn("V356_ORIGINAL_HOST_LOCK_TRANSITIONS", handler)
        self.assertNotIn("REJOIN_UUID =", handler)
        self.assertNotIn("REJOIN_B_RETAINED =", handler)
        self.assertNotIn("wait_for_new_original_source_marker(", handler)
        self.assertIn("M405_", handler)
        self.assertIn('log_tail(log, 6000)', handler)

    def test_original_rejoin_positive_control_does_not_change(self):
        self.assertIn("wait_for_new_original_source_marker(", HARNESS)
        self.assertIn("REJOIN_UUID, deadline,", HARNESS)
        self.assertIn("REJOIN_B_RETAINED, deadline,", HARNESS)
        self.assertIn("wait_for_real_score(", HARNESS)
        self.assertIn("verifyRealControllerB(", OWNER)
        self.assertIn("requireRejoinedControllerEntityUuid", OWNER)
        self.assertIn("rejoinedTicks>=20", OWNER)


if __name__ == "__main__":
    unittest.main()
