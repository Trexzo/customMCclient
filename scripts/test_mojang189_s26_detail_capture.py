"""S26 source-level guardrails for *genuine* original 1.8.9 detail screenshots.

These tests do not simulate the game, do not supply a native PASS, and
cannot replace real Mojang/X11 screenshots or physical pointer acceptance.
"""
from pathlib import Path
import tempfile
import time
import unittest

from RunMojang189SingleplayerSmoke import (await_fresh_native_visual_page,
                                            await_original_gui_cursor_ready)

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

    def test_native_ready_source_can_be_existing_safe_gui_or_vanilla_release(self):
        with tempfile.TemporaryDirectory() as tmp:
            log = Path(tmp) / "client.log"
            for tag in (
                b"CUSTOMMC_189_NATIVE_GUI_CURSOR_READY=YES\\n",
                b"CUSTOMMC_189_NATIVE_GUI_CURSOR_RELEASE=YES\\n",
            ):
                log.write_bytes(tag)
                await_original_gui_cursor_ready(
                    LivingProcess(), log, time.monotonic() + .1,
                    announce=False)

    def test_original_cursor_ready_must_be_emitted_after_real_toggle(self):
        with tempfile.TemporaryDirectory() as tmp:
            log = Path(tmp) / "client.log"
            log.write_bytes(b"CUSTOMMC_189_NATIVE_GUI_CURSOR_READY=YES\\n")
            before_toggle = log.stat().st_size
            with self.assertRaisesRegex(AssertionError, "no original GUI_READY"):
                await_original_gui_cursor_ready(
                    LivingProcess(), log, time.monotonic() + .03,
                    after_cursor=before_toggle, announce=False)
            with log.open("ab") as stream:
                stream.write(b"CUSTOMMC_189_NATIVE_GUI_CURSOR_RELEASE=YES\\n")
            await_original_gui_cursor_ready(
                LivingProcess(), log, time.monotonic() + .1,
                after_cursor=before_toggle, announce=False)

    def test_original_mouse_rejection_cannot_be_labelled_safe(self):
        with tempfile.TemporaryDirectory() as tmp:
            log = Path(tmp) / "client.log"
            log.write_bytes(b"CUSTOMMC_189_NATIVE_GUI_FOCUS_REJECTED=YES\\n")
            with self.assertRaisesRegex(AssertionError, "mouse focus failed"):
                await_original_gui_cursor_ready(
                    LivingProcess(), log, time.monotonic() + .1,
                    announce=False)

    def test_original_back_click_is_inside_actual_854x480_layout(self):
        # Real ClickGuiLayoutEngine: width=min(738,w*.88),
        # height=min(490,h*.78), sidebar=min(158,width*.28);
        # UiModuleDetailBackLink: x=content.right-15-102,
        # y=content.top+4, size 102x17 (not a Python UI mock).
        width, height = 854, 480
        root_width = min(738.0, width * .88)
        root_height = min(490.0, height * .78)
        root_x = (width - root_width) / 2
        root_y = (height - root_height) / 2
        content_right = root_x + root_width
        button_left = content_right - 15.0 - 102.0
        button_top = root_y + 4.0
        self.assertTrue(button_left <= 733 < button_left + 102.0)
        self.assertTrue(button_top <= 65 < button_top + 17.0)
        self.assertFalse(button_top <= 47 < button_top + 17.0)
        self.assertIn(
            'xdotool("mousemove", "--window", window, 733, 65)',
            RUNNER.read_text(encoding="utf-8"))

    def test_focus_authority_is_only_original_pinned_run_tick_object(self):
        focus = (ROOT / "platform-1.8.9/src/main/java/dev/trexzo/"
                 "custommc/platform/v1_8_9/Minecraft189OverlayFocusGuardian.java")
        bridge = (ROOT / "platform-1.8.9/src/main/java/dev/trexzo/"
                  "custommc/platform/v1_8_9/Minecraft189RuntimeBridge.java")
        focus_source = focus.read_text(encoding="utf-8")
        bridge_source = bridge.read_text(encoding="utf-8")
        self.assertIn(
            "Minecraft189OverlayFocusGuardian.observeOriginalTickInstance(minecraft);",
            bridge_source)
        self.assertIn("original-runTick-this", focus_source)
        self.assertIn("new WeakReference<Object>(instance)", focus_source)
        self.assertIn("getDeclaredField(", focus_source)
        self.assertIn("wrong-descriptors", focus_source)
        self.assertIn("return Snapshot.unknown()", focus_source)
        self.assertIn("lastNativeDiagnostic", focus_source)

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
