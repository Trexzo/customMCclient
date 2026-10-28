"""S26 source-level guardrails for *genuine* original 1.8.9 detail screenshots.

These tests do not simulate the game, do not supply a native PASS, and
cannot replace real Mojang/X11 screenshots or physical pointer acceptance.
"""
from pathlib import Path
import tempfile
import time
import unittest

from RunMojang189SingleplayerSmoke import await_fresh_native_visual_page

ROOT = Path(__file__).resolve().parent.parent
RUNNER = ROOT / "scripts" / "RunMojang189SingleplayerSmoke.py"
WORKFLOW = ROOT / ".github" / "workflows" / "minecraft-189-real-client.yml"


class LivingProcess:
    def poll(self):
        return None


class OriginalDetailCaptureChecks(unittest.TestCase):
    def test_back_requires_new_source_marker_not_old_modules_page(self):
        with tempfile.TemporaryDirectory() as tmp:
            log = Path(tmp) / "client.log"
            log.write_bytes(b"CUSTOMMC_189_NATIVE_VISUAL_PAGE=modules\n")
            old_cursor = log.stat().st_size
            with self.assertRaisesRegex(AssertionError, "no fresh source-rendered"):
                await_fresh_native_visual_page(
                    LivingProcess(), log, "modules", old_cursor,
                    time.monotonic() + .03)
            with log.open("ab") as stream:
                stream.write(
                    b"CUSTOMMC_189_NATIVE_VISUAL_PAGE=module-detail\n"
                    b"CUSTOMMC_189_NATIVE_VISUAL_PAGE=modules\n")
            await_fresh_native_visual_page(
                LivingProcess(), log, "modules", old_cursor,
                time.monotonic() + .1)

    def test_detail_requires_actual_new_module_detail_marker(self):
        with tempfile.TemporaryDirectory() as tmp:
            log = Path(tmp) / "client.log"
            log.write_bytes(b"CUSTOMMC_189_NATIVE_VISUAL_PAGE=modules\n")
            cursor = log.stat().st_size
            with log.open("ab") as stream:
                stream.write(b"CUSTOMMC_189_NATIVE_VISUAL_PAGE=settings\n")
            with self.assertRaisesRegex(AssertionError, "module-detail"):
                await_fresh_native_visual_page(
                    LivingProcess(), log, "module-detail", cursor,
                    time.monotonic() + .03)

    def test_actual_native_pointer_and_original_window_are_mandatory(self):
        script = RUNNER.read_text(encoding="utf-8")
        workflow = WORKFLOW.read_text(encoding="utf-8")
        self.assertIn('"click", "--window", window, "3"', script)
        self.assertIn('"click", "--window", window, "1"', script)
        self.assertIn('capture_original_x11(\n                        window, output / "module-detail.png")', script)
        self.assertIn('capture_original_x11(\n                        window, output / "modules-return.png")', script)
        self.assertIn('min(deadline, time.monotonic() + 9)', script)
        self.assertIn('CUSTOMMC_189_NATIVE_VISUAL_DETAIL_BACK_PASS=YES', script)
        self.assertIn('--visual-capture-details', workflow)
        self.assertIn('name: custommc-189-real-graphite-visuals', workflow)
        self.assertNotIn('continue-on-error: true', workflow)


if __name__ == "__main__":
    unittest.main()
