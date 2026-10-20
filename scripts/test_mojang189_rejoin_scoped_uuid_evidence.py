#!/usr/bin/env python3
"""V253: original Minecraft host UUID witness is post-relaunch and full-log scoped."""
import tempfile
import time
import unittest
from pathlib import Path
from unittest.mock import patch

import RunMojang189LocalhostTwoPlayerSmoke as original


class OriginalProcess:
    def __init__(self, exit_code=None):
        self.exit_code = exit_code

    def poll(self):
        return self.exit_code


class RejoinSourceWindowTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.path = Path(self.temp.name) / "original-host-jvm.log"
        self.path.write_bytes(b"")
        self.process = OriginalProcess()
        self.marker = original.REJOIN_UUID
        self.phase = "actual same original UUID revalidated after new login"

    def witness(self, offset=0, marker=None, deadline=None):
        return original.wait_for_new_original_source_marker(
            self.process, self.path, offset,
            self.marker if marker is None else marker,
            time.monotonic() + 10 if deadline is None else deadline,
            self.phase)

    def failed_now(self, offset=0, marker=None):
        # Never waste wall time to exercise the strict deadline.
        with patch.object(original.time, "monotonic",
                          side_effect=[100.0, 101.0]):
            return self.witness(offset, marker, deadline=100.5)

    def test_exact_new_original_uuid_is_admitted(self):
        self.path.write_bytes(self.marker + b"\n")
        self.assertIsNone(self.witness())

    def test_pre_relaunch_marker_is_not_admitted(self):
        self.path.write_bytes(self.marker + b"\nold-rejoin-boundary\n")
        offset = self.path.stat().st_size
        with self.assertRaisesRegex(AssertionError, "missing fresh original source"):
            self.failed_now(offset)

    def test_marker_emitted_after_real_launch_is_admitted(self):
        self.path.write_bytes(self.marker + b"\nold\n")
        offset = self.path.stat().st_size
        with self.path.open("ab") as stream:
            stream.write(b"brand new vanilla client join\n")
            stream.write(self.marker + b"\n")
        self.assertIsNone(self.witness(offset))

    def test_new_marker_survives_more_than_100kb_unrelated_native_noise(self):
        self.path.write_bytes(b"relaunch start\n")
        offset = self.path.stat().st_size
        with self.path.open("ab") as stream:
            stream.write(self.marker + b"\n")
            stream.write(b"[LWJGL/ALSA/native]: diagnostic only\n" * 5500)
        self.assertNotIn(self.marker, original.log_tail(self.path))
        self.assertIsNone(self.witness(offset))

    def test_large_old_marker_cannot_pass_even_if_present_in_full_file(self):
        self.path.write_bytes(self.marker + b"\n" + b"old output\n" * 20000)
        offset = self.path.stat().st_size
        with self.assertRaises(AssertionError):
            self.failed_now(offset)

    def test_exact_offset_immediately_before_marker_succeeds(self):
        self.path.write_bytes(b"prefix\n" + self.marker)
        self.assertIsNone(self.witness(len(b"prefix\n")))

    def test_offset_after_entire_marker_cannot_reuse_it(self):
        self.path.write_bytes(self.marker)
        with self.assertRaises(AssertionError):
            self.failed_now(len(self.marker))

    def test_prefix_of_marker_does_not_count_as_complete_source_proof(self):
        self.path.write_bytes(self.marker[:-1])
        with self.assertRaises(AssertionError):
            self.failed_now()

    def test_one_byte_different_uuid_marker_must_fail(self):
        self.path.write_bytes(self.marker[:-1] + b"N")
        with self.assertRaises(AssertionError):
            self.failed_now()

    def test_unrelated_rejoin_phase_cannot_satisfy_required_marker(self):
        self.path.write_bytes(original.REJOIN_B_RETAINED + b"\n")
        with self.assertRaises(AssertionError):
            self.failed_now()

    def test_original_sentinel_only_without_real_uuid_marker_is_insufficient(self):
        self.path.write_bytes(
            b"CUSTOMMC_189_REJOIN_NATIVE_SERVER_SENTINELS_SUMMONED=4\n")
        with self.assertRaises(AssertionError):
            self.failed_now()

    def test_marker_can_arrive_from_live_original_process_on_later_poll(self):
        def append_marker(_):
            with self.path.open("ab") as stream:
                stream.write(self.marker + b"\n")
        with patch.object(original.time, "sleep", side_effect=append_marker) as sleeper:
            self.assertIsNone(self.witness())
        sleeper.assert_called_once()

    def test_source_witness_split_across_real_process_writes_is_detected(self):
        piece = len(self.marker) // 2
        self.path.write_bytes(self.marker[:piece])
        def append_rest(_):
            with self.path.open("ab") as stream:
                stream.write(self.marker[piece:] + b"\n")
        with patch.object(original.time, "sleep", side_effect=append_rest):
            self.assertIsNone(self.witness())

    def test_old_and_fresh_duplicate_markers_require_fresh_instance(self):
        self.path.write_bytes(self.marker + b"\n")
        offset = self.path.stat().st_size
        with self.path.open("ab") as stream:
            stream.write(b"actual new login\n" + self.marker)
        self.assertIsNone(self.witness(offset))

    def test_exited_original_jvm_is_not_accepted_even_with_marker(self):
        self.path.write_bytes(self.marker)
        self.process.exit_code = 8
        with self.assertRaisesRegex(AssertionError, "process exited early"):
            self.witness()

    def test_negative_log_boundary_rejected(self):
        with self.assertRaisesRegex(AssertionError, "needs offset/bytes"):
            self.witness(-1)

    def test_text_marker_cannot_be_used_as_original_byte_witness(self):
        with self.assertRaisesRegex(AssertionError, "needs offset/bytes"):
            self.witness(marker="not genuine bytes")

    def test_empty_native_marker_rejected(self):
        with self.assertRaisesRegex(AssertionError, "needs offset/bytes"):
            self.witness(marker=b"")

    def test_deadline_remains_a_hard_gate_when_no_uuid_witness(self):
        with self.assertRaisesRegex(AssertionError, "missing fresh original source"):
            self.failed_now()
        
    def test_native_marker_can_follow_arbitrary_binary_output(self):
        self.path.write_bytes(b"\xff\x00\xfe\n" + self.marker + b"\n")
        self.assertIsNone(self.witness())

    def test_script_pins_original_host_log_offset_before_rejoin_jvm_launch(self):
        script = (Path(__file__).parent /
                  "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        pinned = script.index("rejoin_host_evidence_offset = (")
        launch = script.index('cmd_rejoin = client_command(', pinned)
        native = script.index("wait_for_new_original_source_marker(\n"
                              "                host, host_log, rejoin_host_evidence_offset", launch)
        self.assertLess(pinned, launch)
        self.assertLess(launch, native)
        self.assertIn("REJOIN_UUID, deadline,", script[native:native + 180])

    def test_same_original_monotonic_deadline_is_passed_to_scoped_witness(self):
        script = (Path(__file__).parent /
                  "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        call = script.index("wait_for_new_original_source_marker(\n"
                            "                host, host_log, rejoin_host_evidence_offset")
        self.assertIn("REJOIN_UUID, deadline,", script[call:call + 220])
        self.assertNotIn("deadline +", script[call:call + 220])


if __name__ == "__main__":
    unittest.main()
