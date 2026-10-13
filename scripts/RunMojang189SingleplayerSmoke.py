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
MELEE_ARMED = b"CUSTOMMC_189_VANILLA_MELEE_TARGET_ARMED=YES"
MELEE_HURT = b"CUSTOMMC_OFFICIAL_189_REAL_ENTITY_HURT_AFTER_VANILLA_CLICK_PASS=YES"
MANUAL_NATIVE_UNSCOPED = b"CUSTOMMC_189_MANUAL_NATIVE_CONTROLLER_UNSCOPED_UUID_PASS=YES"
AUTO_DISABLED = b"CUSTOMMC_189_AUTOCLICKER_DISABLED_TARGET_TICKS_PASS=20"
AUTO_ENABLED = b"CUSTOMMC_189_AUTOCLICKER_NORMAL_MODULE_ENABLE_PASS=YES"
AUTO_ARMED = b"CUSTOMMC_189_AUTOCLICKER_REAL_TARGET_ARMED=YES"
AUTO_HURT = b"CUSTOMMC_OFFICIAL_189_AUTOCLICKER_SAME_ENTITY_HURT_PASS=YES"
AUTO_NATIVE_UNSCOPED = b"CUSTOMMC_189_AUTOCLICKER_NATIVE_CONTROLLER_UNSCOPED_UUID_PASS=YES"
AURA_DISABLED = b"CUSTOMMC_189_KILLAURA_DISABLED_NONPLAYER_TICKS_PASS=20"
AURA_ENABLED = b"CUSTOMMC_189_KILLAURA_NORMAL_MODULE_ENABLE_PASS=YES"
AURA_VETO = b"CUSTOMMC_OFFICIAL_189_KILLAURA_NONPLAYER_VETO_PASS=YES"


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


def await_screen_change(process, log, previous, deadline, stage):
    """Require fresh vanilla screen transitions, not blind X11 timing."""
    prefix = b"CUSTOMMC_189_ACCEPTANCE_SCREEN="
    while time.monotonic() < deadline:
        require_running(process, stage)
        screens = [line.split(prefix, 1)[1].strip()
                   for line in read_tail(log).splitlines() if prefix in line]
        if screens and screens[-1] != previous:
            screen = screens[-1].decode("ascii", "replace")
            print("CUSTOMMC_189_SCREEN_TRANSITION=" + screen, flush=True)
            return screens[-1]
        time.sleep(0.25)
    raise AssertionError("no vanilla GUI transition after " + stage)


def await_marker(process, log, marker, deadline, stage):
    while time.monotonic() < deadline:
        require_running(process, stage)
        if marker in read_tail(log):
            return
        time.sleep(0.3)
    raise AssertionError("no " + stage + " checkpoint before bounded deadline")


def current_screen(log):
    """Read the most recent actual vanilla GUI callback from the JVM."""
    prefix = b"CUSTOMMC_189_ACCEPTANCE_SCREEN="
    screens = [line.split(prefix, 1)[1].strip()
               for line in read_tail(log).splitlines() if prefix in line]
    return screens[-1] if screens else None


def resume_if_paused(process, log, window):
    screen = current_screen(log)
    if screen == b"axp":
        # With a real WM on the acceptance display this activates the
        # window before clicking the vanilla "Back to Game" button.
        xdotool("windowactivate", "--sync", window)
        # Actual Minecraft 1.8.9 GuiIngameMenu button 4,
        # "Back to Game", is at width/2, height/4+8.
        click(window, 427, 128)
        print("CUSTOMMC_189_RESUME_FROM_ACTUAL_PAUSE_MENU=YES", flush=True)
        resumed = await_screen_change(
            process, log, b"axp", time.monotonic() + 6, "resume game")
        if resumed != b"(in-game/no GUI)":
            raise AssertionError("cannot resume paused Minecraft GUI: "
                                 + repr(resumed))
    elif screen != b"(in-game/no GUI)":
        raise AssertionError("cannot send in-world command from GUI "
                             + repr(screen))


def send_command(process, log, window, command):
    """Use actual Minecraft chat UI and require its open/close transitions."""
    if not command.startswith("/") or len(command) > 125:
        raise ValueError("refusing invalid offline acceptance command")
    resume_if_paused(process, log, window)
    # Never change X11 focus with windowfocus() mid-game: the old runner
    # triggered Minecraft's auto-pause-on-lost-focus and lost every command.
    xdotool("key", "--clearmodifiers", "t")
    opened = await_screen_change(
        process, log, b"(in-game/no GUI)", time.monotonic() + 6,
        "actual vanilla chat open")
    if opened in (b"axp", b"aya", b"axb"):
        raise AssertionError("not the in-world chat GUI: " + repr(opened))
    print("CUSTOMMC_189_REAL_CHAT_OPEN_SCREEN="
          + opened.decode("ascii", "replace"), flush=True)
    xdotool("type", "--clearmodifiers", "--delay", "12", command)
    xdotool("key", "--clearmodifiers", "Return")
    closed = await_screen_change(
        process, log, opened, time.monotonic() + 6,
        "actual vanilla chat close")
    if closed != b"(in-game/no GUI)":
        raise AssertionError("command did not close vanilla chat: "
                             + repr(closed))
    print("CUSTOMMC_189_OFFLINE_COMMAND_SENT=" + command.split(" ", 1)[0],
          flush=True)


def native_safe_walk_positions(log):
    """Native per-tick positions from source-mapped local player, never GUI guesses."""
    prefix = b"CUSTOMMC_189_NATIVE_LOCAL_POS="
    samples = []
    for line in read_tail(log).splitlines():
        if prefix not in line:
            continue
        parts = line.split(prefix, 1)[1].decode("ascii", "strict").split()
        if len(parts) != 3 or not all(part.startswith(
                key) for part, key in zip(parts, ("x=", "y=", "z="))):
            raise AssertionError("malformed native SafeWalk position witness")
        xyz = tuple(float(part[2:]) for part in parts)
        if any(not (-30000000.0 < value < 30000000.0) for value in xyz):
            raise AssertionError("invalid native SafeWalk player coordinates")
        samples.append(xyz)
    return samples


def run(command, game, timeout, attack=False, auto_clicker=False,
        kill_aura_nonplayer=False, native_world_camera=False,
        native_safe_walk=False, safe_walk_edge=None, native_scaffold_world=False,
        native_scaffold_tick=False, native_scaffold_raytrace=False,
        native_scaffold_context=False):
    if sum(bool(x) for x in (attack, auto_clicker, kill_aura_nonplayer)) > 1:
        raise ValueError("manual and module combat tests are exclusive")
    controlled_combat = attack or auto_clicker or kill_aura_nonplayer
    if safe_walk_edge not in (None, "enabled", "disabled"):
        raise ValueError("invalid SafeWalk edge control")
    if safe_walk_edge and (controlled_combat or native_safe_walk):
        raise ValueError("SafeWalk ledge fixture must be independent")
    if native_scaffold_world and (controlled_combat or safe_walk_edge or native_safe_walk):
        raise ValueError("Scaffold world cell fixture must be independent")
    if native_scaffold_tick and (controlled_combat or safe_walk_edge or native_safe_walk
                                 or native_scaffold_world):
        raise ValueError("native Scaffold tick fixture must be independent")
    if native_scaffold_raytrace and (controlled_combat or safe_walk_edge
                                    or native_safe_walk or native_scaffold_world
                                    or native_scaffold_tick or native_scaffold_context):
        raise ValueError("native Scaffold raytrace fixture must be independent")
    if native_scaffold_context and (controlled_combat or safe_walk_edge
                                    or native_safe_walk or native_scaffold_world
                                    or native_scaffold_tick):
        raise ValueError("original Scaffold context fixture must be independent")
    # A real rendered world camera requires a focused Minecraft window;
    # vanilla singleplayer otherwise automatically opens the pause GUI.
    needs_active_window = controlled_combat or native_world_camera or native_safe_walk or bool(safe_walk_edge) or native_scaffold_world or native_scaffold_tick or native_scaffold_raytrace or native_scaffold_context
    if not os.environ.get("DISPLAY"):
        raise ValueError("this acceptance requires a real Xvfb DISPLAY")
    if not (70 <= timeout <= 240):
        raise ValueError("singleplayer timeout must be 70..240 seconds")
    # Java flags must precede the class name.
    if native_safe_walk or safe_walk_edge == "enabled":
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeSafeWalk=true")
    if safe_walk_edge:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportNativeSafeWalkPosition=true")
    if native_scaffold_world:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeScaffoldWorldCell=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportNativeSafeWalkPosition=true")
    if native_scaffold_tick:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeScaffoldTickParity=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportNativeSafeWalkPosition=true")
    if native_scaffold_context:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireOriginalScaffoldContext=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportNativeSafeWalkPosition=true")
    if native_scaffold_raytrace:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeScaffoldRayTrace=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportNativeSafeWalkPosition=true")
    if native_world_camera:
        command.insert(command.index("-cp"),
                       "-Dcustommc.visuals.captureNativeWorldCamera=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeWorldCamera=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeWorldOrigin=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativePlayerBoxFrame=true")
    command.insert(command.index("-cp"), "-Dcustommc.acceptance.reportLiveWorld=true")
    if controlled_combat:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportVanillaMelee=true")
    if attack:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireManualUnscopedNativeController=true")
    if auto_clicker:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportAutoClicker=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireAutoClickerUnscopedNativeController=true")
    if kill_aura_nonplayer:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraNonPlayer=true")
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
    window_manager = None
    with tempfile.TemporaryDirectory(prefix="custommc-189-world-") as temp:
        log = Path(temp) / "minecraft.txt"
        try:
            if needs_active_window:
                # Xvfb alone has no EWMH window manager; LWJGL2 may lose
                # active focus and auto-pause the integrated server. Openbox
                # supplies real X11 activation without patching Minecraft.
                window_manager = subprocess.Popen(
                    ["openbox", "--sm-disable"],
                    stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL
                )
                time.sleep(1.0)
                if window_manager.poll() is not None:
                    raise AssertionError("Openbox failed to initialize X11 focus")
                print("CUSTOMMC_189_X11_WINDOW_MANAGER_ACTIVE=YES", flush=True)
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
            if needs_active_window:
                xdotool("windowactivate", "--sync", window)
            geometry = xdotool("getwindowgeometry", "--shell", window)
            print("CUSTOMMC_189_WINDOW_GEOMETRY="
                  + geometry.replace("\n", " "), flush=True)
            # Ensure a stable vanilla title menu before clicking.
            time.sleep(3)
            require_running(process, "Minecraft main menu")
            # GuiMainMenu: Singleplayer button is centered at x427, y~180.
            # The pinned official 1.8.9 title screen is obfuscated aya.
            await_marker(process, log, b"CUSTOMMC_189_ACCEPTANCE_SCREEN=aya",
                         min(deadline, time.monotonic() + 10), "title GUI")
            click(window, 427, 178)
            print("CUSTOMMC_189_GUI_CLICK=singleplayer", flush=True)
            select_screen = await_screen_change(
                process, log, b"aya", time.monotonic() + 8,
                "singleplayer menu")
            if select_screen != b"axv":
                raise AssertionError("unexpected 1.8.9 world selector "
                                     + repr(select_screen))
            # GuiSelectWorld: "Create New World" is on the RIGHT
            # (width/2+4 .. width/2+154), not the disabled Select World
            # button on the left when there are zero saves.
            click(window, 505, 437)
            print("CUSTOMMC_189_GUI_CLICK=create-world-menu", flush=True)
            create_screen = await_screen_change(
                process, log, select_screen, time.monotonic() + 8,
                "create world GUI")
            require_running(process, "create world GUI")
            # Actual 1.8.9 GuiCreateWorld Game Mode button is centered
            # at x427,y125. Survival -> Hardcore -> Creative; Creative
            # enables commands unless explicitly overridden.
            if controlled_combat or safe_walk_edge or native_scaffold_world or native_scaffold_tick or native_scaffold_raytrace:
                if create_screen != b"axb":
                    raise AssertionError("unexpected create-world GUI")
                click(window, 427, 125)
                time.sleep(0.25)
                click(window, 427, 125)
                print("CUSTOMMC_189_CREATIVE_COMMAND_WORLD_REQUESTED=YES",
                      flush=True)
            click(window, 346, 462)
            print("CUSTOMMC_189_NEW_WORLD_GUI_REQUEST_SENT=YES", flush=True)
            await_marker(process, log, WORLD, deadline, "real player/world-tick")
            if native_world_camera:
                # An already-created world can be paused by focus transition.
                # Resume using genuine GuiIngameMenu input, never fake ticks.
                resume_if_paused(process, log, window)
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_NATIVE_WORLD_CAMERA_GL_PASS=YES",
                             deadline, "source-mapped original perspective world camera")
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_NATIVE_RENDER_ORIGIN_PASS=YES",
                             deadline, "same-frame native lastTickPos render-view origin")
                print("CUSTOMMC_OFFICIAL_189_REAL_WORLD_CAMERA_CAPTURE_PASS=YES",
                      flush=True)
                print("CUSTOMMC_OFFICIAL_189_REAL_RENDER_ORIGIN_PASS=YES",
                      flush=True)
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_NATIVE_PLAYER_BOX_FRAME_PASS=YES",
                             deadline, "actual native loaded-entity UUID/kind/AABB frame")
                print("CUSTOMMC_OFFICIAL_189_REAL_PLAYER_AABB_FRAME_PASS=YES",
                      flush=True)
            if native_safe_walk:
                # Real original local Entity.moveEntity only: press actual
                # movement input briefly on a native onGround world tile.
                xdotool("windowactivate", "--sync", window)
                xdotool("keydown", "--window", window, "w")
                try:
                    await_marker(process, log,
                                 b"CUSTOMMC_OFFICIAL_189_NATIVE_LOCAL_SAFEWALK_BRANCH_PASS=YES",
                                 deadline, "genuine on-ground local moveEntity Sneak branch")
                finally:
                    xdotool("keyup", "--window", window, "w")
                print("CUSTOMMC_OFFICIAL_189_ORIGINAL_SAFEWALK_MOVEMENT_HOOK_PASS=YES",
                      flush=True)
            if safe_walk_edge:
                # Genuine offline integrated-server terrain, not a fake
                # collision AABB. A thin elevated solid ledge ends at z=3.
                # Vanilla yaw=0 faces +Z; W approaches the missing support.
                resume_if_paused(process, log, window)
                send_command(process, log, window, "/time set 1000")
                # Vanilla /fill refuses chunks outside the loaded world.
                # Move the real player to the destination before modifying it.
                # A high staging Y keeps the player above the future platform.
                send_command(process, log, window,
                             "/tp 0.5 120 0.5 0 0")
                time.sleep(2.0)
                send_command(process, log, window,
                             "/fill -4 99 -2 4 99 2 stone")
                send_command(process, log, window,
                             "/fill -4 99 3 4 99 10 air")
                send_command(process, log, window,
                             "/fill -4 100 -2 4 104 10 air")
                if b"Cannot place blocks outside of the world" in read_tail(log):
                    raise AssertionError("actual vanilla /fill rejected unloaded target chunks")
                send_command(process, log, window,
                             "/tp 0.5 100 0.5 0 0")
                start = time.monotonic()
                while time.monotonic() - start < 8:
                    readings = native_safe_walk_positions(log)
                    if readings and abs(readings[-1][0] - 0.5) < 0.3 \
                            and abs(readings[-1][1] - 100.0) < 0.2 \
                            and abs(readings[-1][2] - 0.5) < 0.3:
                        break
                    require_running(process, "native ledge preflight")
                    time.sleep(0.15)
                else:
                    raise AssertionError("actual local player never reached ledge start")
                # Require continuous real solid support before sending W;
                # a one-tick /tp coordinate is not evidence of a block.
                time.sleep(0.7)
                stationary = native_safe_walk_positions(log)[-5:]
                if len(stationary) < 3 or any(y < 99.95 for _, y, _ in stationary):
                    raise AssertionError("no stable native collision support on the actual stone platform")
                start_count = len(native_safe_walk_positions(log))
                xdotool("windowactivate", "--sync", window)
                resume_if_paused(process, log, window)
                xdotool("keydown", "--window", window, "w")
                try:
                    time.sleep(2.5)
                finally:
                    xdotool("keyup", "--window", window, "w")
                readings = native_safe_walk_positions(log)[start_count:]
                if len(readings) < 5:
                    raise AssertionError("not enough genuine local movement samples")
                maximum_z = max(z for _, _, z in readings)
                minimum_y = min(y for _, y, _ in readings)
                print("CUSTOMMC_189_SAFEWALK_LEDGE_OBSERVED_MAX_Z="
                      + str(maximum_z) + " MIN_Y=" + str(minimum_y),
                      flush=True)
                if safe_walk_edge == "enabled":
                    await_marker(process, log,
                                 b"CUSTOMMC_OFFICIAL_189_NATIVE_LOCAL_SAFEWALK_BRANCH_PASS=YES",
                                 deadline, "true original native SafeWalk branch")
                    if not 1.4 <= maximum_z < 3.5 or minimum_y < 99.95:
                        raise AssertionError("SafeWalk did not keep the local player on the real ledge")
                    print("CUSTOMMC_OFFICIAL_189_SAFEWALK_REAL_LEDGE_GUARD_PASS=YES",
                          flush=True)
                else:
                    if maximum_z < 4.0:
                        raise AssertionError("disabled SafeWalk negative control did not cross real ledge")
                    print("CUSTOMMC_OFFICIAL_189_SAFEWALK_DISABLED_REAL_EDGE_EXIT_PASS=YES",
                          flush=True)
            if native_scaffold_world:
                # No synthetic block state or client-side mutation.
                # Real vanilla integrated server /setblock updates the native
                # original client's World via normal world packets.
                resume_if_paused(process, log, window)
                send_command(process, log, window, "/time set 1000")
                # Load destination first; vanilla /setblock rejects unloaded
                # chunks. A permanent separate stone support prevents the
                # original player falling out of loaded range while observing.
                send_command(process, log, window, "/tp 0.5 120 0.5 0 0")
                time.sleep(2.0)
                send_command(process, log, window, "/setblock 0 99 0 stone")
                send_command(process, log, window, "/setblock 1 99 0 air")
                if b"Cannot place blocks outside of the world" in read_tail(log):
                    raise AssertionError("vanilla original world setup rejected unloaded chunk")
                send_command(process, log, window, "/tp 0.5 100 0.5 0 0")
                # Actually observe the source-native player's stable support,
                # not simply successful execution of the /tp command.
                stable_start = time.monotonic()
                while time.monotonic() - stable_start < 10:
                    actual = native_safe_walk_positions(log)[-5:]
                    if len(actual) >= 3 and all(
                            abs(x - 0.5) < 0.3 and abs(y - 100.0) < 0.2
                            and abs(z - 0.5) < 0.3 for x, y, z in actual[-3:]):
                        break
                    require_running(process, "real native Scaffold fixture grounding")
                    time.sleep(0.15)
                else:
                    raise AssertionError("vanilla player lacks sustained stone support at (0,99,0)")
                print("CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_SUPPORT_GROUNDED=YES",
                      flush=True)
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_AIR_PASS=YES",
                             deadline, "actual original World AIR at adjacent (1,99,0)")
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_UNLOADED_CHUNK_VETO_PASS=YES",
                             deadline, "source native remote unloaded chunk rejects false AIR")
                send_command(process, log, window, "/setblock 1 99 0 stone")
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_SOLID_PASS=YES",
                             deadline, "genuine vanilla stone at adjacent (1,99,0)")
                send_command(process, log, window, "/setblock 1 99 0 air")
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_REVERTED_AIR_PASS=YES",
                             deadline, "original World observes real stone-to-air server update")
                print("CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_REAL_WORLD_TRANSITIONS_PASS=YES",
                      flush=True)
            if native_scaffold_tick:
                # This is a separate genuine-original vanilla singleplayer
                # observation. No synthetic hotbar, block, or player sources.
                resume_if_paused(process, log, window)
                send_command(process, log, window, "/time set 1000")
                send_command(process, log, window, "/tp 0.5 120 0.5 0 0")
                time.sleep(2.0)
                send_command(process, log, window, "/setblock 0 99 0 stone")
                send_command(process, log, window, "/tp 0.5 100 0.5 0 0")
                start = time.monotonic()
                while time.monotonic() - start < 12:
                    readings = native_safe_walk_positions(log)[-5:]
                    if len(readings) >= 3 and all(
                            abs(x - 0.5) < 0.3 and abs(y - 100.0) < 0.2
                            and abs(z - 0.5) < 0.3 for x, y, z in readings[-3:]):
                        break
                    require_running(process, "original Scaffold tick grounded preflight")
                    time.sleep(0.15)
                else:
                    raise AssertionError("original player was not stable on native server stone")
                parity = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_NATIVE_TICK_PARITY_PASS=YES"
                if parity in read_tail(log):
                    raise AssertionError("Scaffold tick parity observed before any real ItemBlock")
                send_command(process, log, window, "/give CIProbe stone 8")
                await_marker(process, log, parity, deadline,
                             "source-native same-tick local player/World/ItemBlock parity")
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_NATIVE_ITEM_WORLD_PARITY_PASS=YES",
                      flush=True)
            if native_scaffold_context:
                # V23: Minecraft.runTick itself publishes original h/f/s.
                # No complete witness before genuine vanilla ItemBlock delivery.
                resume_if_paused(process, log, window)
                send_command(process, log, window, "/time set 1000")
                send_command(process, log, window, "/tp 0.5 120 0.5 0 0")
                time.sleep(2.0)
                send_command(process, log, window, "/setblock 0 99 0 stone")
                send_command(process, log, window, "/tp 0.5 100 0.5 0 0")
                stable_start = time.monotonic()
                while time.monotonic() - stable_start < 12:
                    readings = native_safe_walk_positions(log)[-5:]
                    if len(readings) >= 3 and all(
                            abs(x - .5) < .3 and abs(y - 100.0) < .2
                            and abs(z - .5) < .3 for x, y, z in readings[-3:]):
                        break
                    require_running(process, "original context source grounding")
                    time.sleep(.15)
                else:
                    raise AssertionError("native original Scaffold context player not grounded")
                tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_TICK_CONTEXT_PASS=YES"
                if tag in read_tail(log):
                    raise AssertionError("original h/f/s context claimed before vanilla ItemBlock")
                send_command(process, log, window, "/give CIProbe stone 8")
                await_marker(process, log, tag, deadline,
                             "real Minecraft tick has original player/World/cursor/ItemBlock")
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_SOURCE_TICK_CONTEXT_PASS=YES",
                      flush=True)
            if native_scaffold_raytrace:
                # Original Minecraft integrated-server stone support, never
                # a synthetic collision cube or client-side placement.
                resume_if_paused(process, log, window)
                send_command(process, log, window, "/time set 1000")
                send_command(process, log, window, "/tp 0.5 120 0.5 0 0")
                time.sleep(2.0)
                prior = read_tail(log)
                if b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES" in prior:
                    raise AssertionError("raytrace witness appeared before real stone support setup")
                send_command(process, log, window, "/setblock 0 99 0 stone")
                send_command(process, log, window, "/tp 0.5 100 0.5 0 0")
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_RAYTRACE_WRONG_FACE_BLOCK_VETO_PASS=YES",
                             deadline, "original raytrace rejects wrong support and facing")
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES",
                             deadline, "genuine World.rayTraceBlocks hits original stone UP face")
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_NATIVE_RAYTRACE_REAL_WORLD_PASS=YES",
                      flush=True)
            require_running(process, "post world checkpoint")
            time.sleep(2)
            require_running(process, "world stability")
            print("CUSTOMMC_OFFICIAL_189_SINGLEPLAYER_WORLD_SUSTAINED_PASS=YES")
            print("CUSTOMMC_OFFICIAL_189_REAL_PLAYER_AND_WORLD_CALLBACKS=YES")
            if controlled_combat:
                # Offline integrated-server commands only. No external server
                # join, forged damage or direct entity edits.
                send_command(process, log, window, "/time set 1000")
                # Deterministic platform and clear LOS. Random normal-world
                # spawns otherwise leave a no-AI pig falling into a ravine,
                # causing intermittent loss of the 20-tick raycast gate.
                # Both are genuine local integrated-server vanilla commands.
                send_command(process, log, window,
                             "/fill ~-3 ~-1 ~-3 ~3 ~-1 ~5 stone")
                send_command(process, log, window,
                             "/fill ~-2 ~ ~1 ~2 ~2 ~4 air")
                send_command(process, log, window, "/tp ~ ~ ~ 0 26")
                send_command(process, log, window,
                             "/summon Pig ~ ~ ~2 {NoAI:1b}")
                time.sleep(1.2)
                require_running(process, "live vanilla melee fixture")
                if kill_aura_nonplayer:
                    # No X11 attack events. Verify 20 observed native living
                    # nonplayer raycast target callbacks with module disabled,
                    # then 80 consecutive enabled Kill Aura callbacks that
                    # never select or strike that same nonplayer identity.
                    await_marker(process, log, AURA_DISABLED, deadline,
                                 "disabled Kill Aura nonplayer negative control")
                    await_marker(process, log, AURA_ENABLED, deadline,
                                 "normal Kill Aura module lifecycle enable")
                    await_marker(process, log, AURA_VETO, deadline,
                                 "80 enabled Kill Aura nonplayer veto callbacks")
                    require_running(process, "Kill Aura nonplayer veto")
                    time.sleep(1)
                    require_running(process, "Kill Aura veto stability")
                    print("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_NONPLAYER_NEGATIVE_PASS=YES",
                          flush=True)
                    print("CUSTOMMC_OFFICIAL_189_KILLAURA_PLAYER_HIT_TESTED=NO",
                          flush=True)
                elif auto_clicker:
                    # There are deliberately NO X11 attack button events here.
                    # Real crosshair targeting must be seen for 20 normal
                    # disabled-module callbacks, followed by a standard
                    # ModuleController.enable and synthetic click + hurtTime.
                    await_marker(process, log, AUTO_DISABLED,
                                 deadline, "20 target ticks with Auto Clicker disabled")
                    await_marker(process, log, AUTO_ENABLED,
                                 deadline, "normal Auto Clicker module enable")
                    await_marker(process, log, AUTO_ARMED,
                                 deadline, "module-owned click on real entity")
                    await_marker(process, log, AUTO_HURT,
                                 deadline, "same entity hurt after module-owned click")
                    await_marker(process, log, AUTO_NATIVE_UNSCOPED,
                                 deadline, "genuine Auto Clicker native UUID with no Aura owner scope")
                    print("CUSTOMMC_OFFICIAL_189_REAL_AUTOCLICKER_DAMAGE_PASS=YES",
                          flush=True)
                    print("CUSTOMMC_OFFICIAL_189_AUTOCLICKER_UNSCOPED_UUID_AND_HURT_PASS=YES",
                          flush=True)
                else:
                    # M402 legacy manual vanilla X11 attack acceptance.
                    for attempt in range(12):
                        if MELEE_HURT in read_tail(log):
                            break
                        resume_if_paused(process, log, window)
                        xdotool("click", "1")
                        time.sleep(0.33)
                    await_marker(process, log, MELEE_ARMED,
                                 min(deadline, time.monotonic() + 8),
                                 "actual raycast entity under vanilla left click")
                    await_marker(process, log, MELEE_HURT,
                                 min(deadline, time.monotonic() + 12),
                                 "same real loaded entity becoming hurt")
                    await_marker(process, log, MANUAL_NATIVE_UNSCOPED,
                                 min(deadline, time.monotonic() + 8),
                                 "genuine vanilla manual attackEntity UUID with no Aura scope")
                    print("CUSTOMMC_OFFICIAL_189_REAL_VANILLA_MELEE_DAMAGE_PASS=YES",
                          flush=True)
                    print("CUSTOMMC_OFFICIAL_189_MANUAL_UNSCOPED_UUID_AND_HURT_PASS=YES",
                          flush=True)
            else:
                print("CUSTOMMC_OFFICIAL_189_COMBAT_DAMAGE_TESTED=NO")
            print("CUSTOMMC_OFFICIAL_189_MULTIPLAYER_TESTED=NO")
        except BaseException:
            if native_scaffold_world:
                # Native per-world diagnostics can be earlier than the last
                # 12KB of very frequent player-position samples. Preserve
                # bounded, exact tagged lines from the real game process.
                tagged = [line for line in log.read_bytes().splitlines()
                          if b"CUSTOMMC_SCAFFOLD_WORLD_DIAG_" in line
                          or b"CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_" in line]
                print("SCAFFOLD_SOURCE_WORLD_DIAGNOSTICS_START", file=sys.stderr)
                for line in tagged[-45:]:
                    print(line.decode("utf-8", "replace"), file=sys.stderr)
                print("SCAFFOLD_SOURCE_WORLD_DIAGNOSTICS_END", file=sys.stderr)
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
            if window_manager is not None:
                window_manager.terminate()
                try:
                    window_manager.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    window_manager.kill()
                    window_manager.wait(timeout=5)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    for field in ("java", "client", "libraries", "assets", "natives", "overlay", "game"):
        parser.add_argument("--" + field, required=True)
    parser.add_argument("--timeout", type=int, default=150)
    parser.add_argument("--attack", action="store_true",
                        help="create offline creative world and verify genuine melee hurt")
    parser.add_argument("--auto-clicker", action="store_true",
                        help="verify enabled Auto Clicker causes offline entity hurt without X11 attack")
    parser.add_argument("--kill-aura-nonplayer", action="store_true",
                        help="verify enabled player-only Kill Aura vetoes genuine passive mob")
    parser.add_argument("--native-world-camera", action="store_true",
                        help="require actual OpenGL perspective camera matrix from vanilla world pass")
    parser.add_argument("--native-safe-walk", action="store_true",
                        help="prove native local moveEntity collision branch with module active")
    parser.add_argument("--safe-walk-edge", choices=("enabled", "disabled"),
                        help="test genuine elevated block ledge with local SafeWalk on/off")
    parser.add_argument("--native-scaffold-world", action="store_true",
                        help="verify original World sees vanilla server air-stone-air cell updates")
    parser.add_argument("--native-scaffold-tick", action="store_true",
                        help="verify genuine original local player/World/selected ItemBlock same-tick source parity")
    parser.add_argument("--native-scaffold-raytrace", action="store_true",
                        help="original World.rayTraceBlocks positive face and wrong-block/face negative controls")
    parser.add_argument("--native-scaffold-context", action="store_true",
                        help="original Minecraft.runTick h/f/s context after native ItemBlock delivery")
    args = parser.parse_args()
    try:
        command = build_command(
            args.java, args.client, args.libraries, args.assets,
            args.natives, args.overlay, args.game
        )
        run(command, args.game, args.timeout, attack=args.attack,
            auto_clicker=args.auto_clicker,
            kill_aura_nonplayer=args.kill_aura_nonplayer,
            native_world_camera=args.native_world_camera,
            native_safe_walk=args.native_safe_walk,
            safe_walk_edge=args.safe_walk_edge,
            native_scaffold_world=args.native_scaffold_world,
            native_scaffold_tick=args.native_scaffold_tick,
            native_scaffold_raytrace=args.native_scaffold_raytrace,
            native_scaffold_context=args.native_scaffold_context)
    except Exception as failure:
        print("CUSTOMMC_189_SINGLEPLAYER_WORLD_FAILED: " + str(failure),
              file=sys.stderr)
        sys.exit(1)
