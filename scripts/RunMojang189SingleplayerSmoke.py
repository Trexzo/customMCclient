#!/usr/bin/env python3
"""Controlled offline Minecraft 1.8.9 singleplayer-world acceptance.

Uses a genuine Java 8/Xvfb client with Mojang assets, real X11 mouse input
and CustomMC's opt-in live world tick checkpoint. Never connects to a server,
supplies online credentials, or claims entity attacks/hits.
"""
import argparse
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import time

from RunMojang189GraphicalSmoke import build_command

FRAME = b"CUSTOMMC_OFFICIAL_189_OPENGL_FRAME_PASS=YES"
WORLD = b"CUSTOMMC_OFFICIAL_189_LIVE_WORLD_TICKS_PASS=YES"


def xdotool(*arguments):
    return subprocess.check_output(
        ["xdotool", *map(str, arguments)],
        stderr=subprocess.STDOUT, timeout=10
    ).decode("utf-8", "replace").strip()


def find_window(deadline):
    while time.monotonic() < deadline:
        try:
            matches = xdotool("search", "--onlyvisible", "--name", "Minecraft")
            if matches:
                return matches.splitlines()[-1]
        except subprocess.CalledProcessError:
            pass
        time.sleep(0.3)
    raise AssertionError("no actual visible Minecraft X11 window appeared")


def click(window, x, y):
    # Window-relative positions require guiScale:1 and the fixed 854x480 size.
    xdotool("mousemove", "--window", window, x, y)
    xdotool("click", "--window", window, "1")


def read_tail(path):
    if not path.exists():
        return b""
    with path.open("rb") as handle:
        handle.seek(max(0, path.stat().st_size - 90000))
        return handle.read()


def require_running(process, stage):
    status = process.poll()
    if status is not None:
        raise AssertionError("Minecraft exited during " + stage
                             + " exit=" + str(status))


def await_marker(process, log, marker, deadline, stage):
    while time.monotonic() < deadline:
        require_running(process, stage)
        if marker in read_tail(log):
            return
        time.sleep(0.3)
    raise AssertionError("no " + stage + " checkpoint before bounded deadline")


def run(command, game, timeout):
    if not os.environ.get("DISPLAY"):
        raise ValueError("this acceptance requires a real Xvfb DISPLAY")
    if not (70 <= timeout <= 240):
        raise ValueError("singleplayer timeout must be 70..240 seconds")
    # Java flags must precede the class name.
    command.insert(command.index("-cp"), "-Dcustommc.acceptance.reportLiveWorld=true")
    # Vanilla UI coordinates are measured in 854x480 GUI scale 1, not auto.
    options = Path(game) / "options.txt"
    with options.open("a", encoding="utf-8") as stream:
        stream.write("guiScale:1\n")
    env = dict(os.environ)
    env["LIBGL_ALWAYS_SOFTWARE"] = "1"
    env["MESA_GL_VERSION_OVERRIDE"] = "2.1"
    env.pop("CUSTOMMC_ACCESS_TOKEN", None)
    env.pop("JAVA_TOOL_OPTIONS", None)
    process = None
    with tempfile.TemporaryDirectory(prefix="custommc-189-world-") as temp:
        log = Path(temp) / "minecraft.txt"
        try:
            with log.open("wb") as output:
                process = subprocess.Popen(
                    command, env=env, stdout=output,
                    stderr=subprocess.STDOUT, start_new_session=True
                )
            started = time.monotonic()
            deadline = started + timeout
            await_marker(process, log, FRAME, min(deadline, started + 60),
                         "actual graphical frame")
            window = find_window(min(deadline, time.monotonic() + 15))
            print("CUSTOMMC_189_REAL_X11_WINDOW_FOUND=YES", flush=True)
            geometry = xdotool("getwindowgeometry", "--shell", window)
            print("CUSTOMMC_189_WINDOW_GEOMETRY="
                  + geometry.replace("\n", " "), flush=True)
            # Ensure a stable vanilla title menu before clicking.
            time.sleep(3)
            require_running(process, "Minecraft main menu")
            # GuiMainMenu: Singleplayer button is centered at x427, y~180.
            click(window, 427, 178)
            print("CUSTOMMC_189_GUI_CLICK=singleplayer", flush=True)
            time.sleep(2.0)
            require_running(process, "world selection GUI")
            # GuiSelectWorld: "Create New World" near bottom-left centre.
            click(window, 347, 437)
            print("CUSTOMMC_189_GUI_CLICK=create-world-menu", flush=True)
            time.sleep(2.0)
            require_running(process, "create world GUI")
            # GuiCreateWorld: default New World, no server or cheat commands.
            click(window, 346, 462)
            print("CUSTOMMC_189_NEW_WORLD_GUI_REQUEST_SENT=YES", flush=True)
            await_marker(process, log, WORLD, deadline, "real player/world-tick")
            require_running(process, "post world checkpoint")
            time.sleep(2)
            require_running(process, "world stability")
            print("CUSTOMMC_OFFICIAL_189_SINGLEPLAYER_WORLD_SUSTAINED_PASS=YES")
            print("CUSTOMMC_OFFICIAL_189_REAL_PLAYER_AND_WORLD_CALLBACKS=YES")
            print("CUSTOMMC_OFFICIAL_189_COMBAT_DAMAGE_TESTED=NO")
            print("CUSTOMMC_OFFICIAL_189_MULTIPLAYER_TESTED=NO")
        except BaseException:
            print("SINGLEPLAYER_SMOKE_OUTPUT_TAIL_START", file=sys.stderr)
            print(read_tail(log)[-12000:].decode("utf-8", "replace"), file=sys.stderr)
            print("SINGLEPLAYER_SMOKE_OUTPUT_TAIL_END", file=sys.stderr)
            raise
        finally:
            if process is not None and process.poll() is None:
                process.terminate()
                try:
                    process.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait(timeout=5)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    for field in ("java", "client", "libraries", "assets", "natives", "overlay", "game"):
        parser.add_argument("--" + field, required=True)
    parser.add_argument("--timeout", type=int, default=150)
    args = parser.parse_args()
    try:
        command = build_command(
            args.java, args.client, args.libraries, args.assets,
            args.natives, args.overlay, args.game
        )
        run(command, args.game, args.timeout)
    except Exception as failure:
        print("CUSTOMMC_189_SINGLEPLAYER_WORLD_FAILED: " + str(failure),
              file=sys.stderr)
        sys.exit(1)
