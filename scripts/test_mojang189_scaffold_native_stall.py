"""V261 diagnostics for genuine original Mojang six-leg Scaffold D-to-E failure.

Only real Minecraft official-binary preflight may prove physical placement.
These tests ensure a real failure remains a failure with useful witness data.
"""
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import RunMojang189LocalhostTwoPlayerSmoke as smoke


class OriginalScaffoldNativeStallWitnessTests(unittest.TestCase):
    ORIGINAL = (999.284714025933327, 100.0, 291.5)
    FIFTH = (b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_"
             b"ORIGINAL_CONTROLLER_ATTEMPT=YES step=5 accepted=true "
             b"target=1001,99,291")

    def evidence(self, original_bytes):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "real-source-log.txt"
            path.write_bytes(original_bytes)
            return smoke.original_scaffold_native_edge_failure_evidence(
                path, self.ORIGINAL, self.FIFTH)

    def test_native_stall_at_real_bounding_box_edge_is_diagnostic_not_pass(self):
        samples = b"".join(
            b"CUSTOMMC_189_NATIVE_LOCAL_POS=x=999.6999999880791 "
            b"y=100.0 z=291.5\n" for _ in range(8))
        diagnostics = self.evidence(samples)
        self.assertIn("nativeSamples=8", diagnostics)
        self.assertIn("nativeLast=(999.6999999880791, 100.0, 291.5)",
                      diagnostics)
        self.assertIn("lastNativeSpanXYZ=(0.0, 0.0, 0.0)", diagnostics)
        self.assertIn("step5MarkerInTail=False", diagnostics)

    def test_missing_original_position_remains_unavailable(self):
        evidence = self.evidence(b"vanilla chat only\n")
        self.assertIn("nativeSamples=0 nativeLast=unavailable", evidence)
        self.assertNotIn("nativeDeltaX=0", evidence)

    def test_real_position_movement_records_variation_without_approval(self):
        pos = b"".join(
            ("CUSTOMMC_189_NATIVE_LOCAL_POS=x={} y=100.0 z=291.5\n"
             .format(x)).encode()
            for x in (999.29, 999.49, 999.69))
        evidence = self.evidence(pos)
        self.assertIn("nativeSamples=3", evidence)
        self.assertIn("nativeLast=(999.69, 100.0, 291.5)", evidence)
        self.assertIn("lastNativeSpanXYZ=", evidence)

    def test_missing_evidence_never_claims_fifth_controller_click(self):
        result = self.evidence(
            b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_"
            b"ORIGINAL_CONTROLLER_ATTEMPT=YES step=4 accepted=true\n")
        self.assertIn("step5MarkerInTail=False", result)

    def test_report_present_marker_is_not_an_acceptance_shortcut(self):
        result = self.evidence(
            self.FIFTH + b"\n"
            + b"CUSTOMMC_189_NATIVE_LOCAL_POS=x=1000.5 "
              b"y=100.0 z=291.5\n")
        self.assertIn("step5MarkerInTail=True", result)
        # If the actual accept-or-fail gate wasn't already satisfied,
        # diagnostics cannot turn this into a valid World/stack receipt.
        self.assertNotIn("PASS=YES", result)
