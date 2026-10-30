#!/usr/bin/env python3
"""Fail-closed original two-player X11 visual proof of enabled HUD widgets.

Never calls a Minecraft controller/model directly. Only authentic X11
keyboard/mouse events on the original host can toggle modules. Accepts the
real-localhost-fixture callbacks rather than creating fake player data.
"""
from pathlib import Path
import subprocess
import time

from Mojang189VisualEvidence import (
    capture_original_x11, parse_x11_geometry, require_visible_graphite)


def hud_controls(width, height):
    """Reproduce the authoritative ClickGuiLayoutEngine 1.8.9 geometry."""
    if not (200 <= width <= 3840 and 160 <= height <= 2160):
        raise ValueError("genuine X11 window dimensions out of bounds")
    w = min(738.0, width * .88)
    h = min(490.0, height * .78)
    x, y = (width - w) / 2.0, (height - h) / 2.0
    sidebar = min(158.0, w * .28)
    padding = min(13.0, w * .025)
    title = min(32.0, h * .10)
    search_h = min(28.0, h * .09)
    search_w = max(0.0, sidebar - padding * 2.0)
    # Search focus and search clear hotspot.
    search_focus = (round(x + padding + 15), round(y + padding + title + search_h / 2.0))
    search_clear = (round(x + padding + search_w - 14), search_focus[1])
    # Filter to a SINGLE original Visuals module. The category header is
    # 18px and the untouched native row starts at content.y + 48 + 18.
    module_toggle = (round(x + sidebar + 18 + 25),
                     round(y + 18 + 30 + 18 + 14))
    for pt in (search_focus, search_clear, module_toggle):
        if not (0 <= pt[0] < width and 0 <= pt[1] < height):
            raise ValueError("computed genuine GUI point outside X11 window")
    return search_focus, search_clear, module_toggle


def require_fresh(host, log, marker, start, deadline, check, name):
    if not isinstance(marker, bytes) or not marker or start < 0:
        raise ValueError("invalid original source witness")
    while time.monotonic() < deadline:
        check(host, "S31 original " + name)
        with Path(log).open("rb") as stream:
            stream.seek(start)
            fresh = stream.read()
        if marker in fresh:
            return
        time.sleep(.13)
    raise AssertionError("no fresh real Minecraft source witness: " + name)


def original_pause_button_signature(samples):
    """Four independent true-original vanilla Game Menu button pixels.

    Sampling the left blank part of four buttons avoids text and prevents
    a stale log or synthetic Java screen state from granting menu recovery.
    A single naturally grey in-game block must not count as Game Menu.
    """
    if len(samples) != 4:
        return False
    return all(len(rgb) == 3
               and 85 <= min(rgb) <= max(rgb) <= 145
               and max(rgb) - min(rgb) <= 3
               for rgb in samples)


def original_pause_menu_visible(window, dimensions):
    """Read-only real X11 frame probe, not an inferred GUI-open flag."""
    from mss import mss
    w, h = dimensions["width"], dimensions["height"]
    x = round(w / 2.0 - 177)
    ys = (round(h / 4.0 + 16), round(h / 4.0 + 64),
          round(h / 4.0 + 160), round(h / 4.0 + 208))
    if not (0 <= x < w and all(0 <= y < h for y in ys)):
        raise AssertionError("original pause-menu witness outside window")
    with mss() as capture:
        image = capture.grab(dimensions)
        raw = image.rgb
    samples = [tuple(raw[(y * w + x) * 3:(y * w + x) * 3 + 3])
               for y in ys]
    return original_pause_button_signature(samples)


def capture_two_player_hud(args, host, host_log, host_window, server,
                           server_log, deadline, send_server_command,
                           spawn_fixture, wait_for, check_process, remote):
    if not str(host_window).isdigit() or not args.hud_visual_dir:
        raise AssertionError("S31 must own exactly one authentic host X11 window")
    output = Path(args.hud_visual_dir)
    if output.exists() or output.is_symlink():
        raise ValueError("refusing existing/symlink visual artifact destination")
    output.mkdir(parents=True)
    window = str(host_window)

    # Genuine server-reported players and teleports, not spoofed client
    # snapshots. REMOTE was already seen at run() admission.
    host_tp, target_tp = spawn_fixture(server_log)
    send_server_command(server, server_log, "gamemode 1 CITarget")
    send_server_command(server, server_log, host_tp)
    send_server_command(server, server_log, target_tp)
    wait_for(host, host_log, remote, deadline,
             "genuine nonlocal Minecraft player source")

    def key(name):
        subprocess.check_call(["xdotool", "key", "--clearmodifiers", name],
                              timeout=6)

    def click(point):
        subprocess.check_call(["xdotool", "mousemove", "--window", window,
                               str(point[0]), str(point[1])], timeout=6)
        subprocess.check_call(["xdotool", "click", "--window", window, "1"],
                              timeout=6)

    subprocess.check_call(["xdotool", "windowactivate", "--sync", window],
                          timeout=8)
    dims = parse_x11_geometry(subprocess.check_output(
        ["xdotool", "getwindowgeometry", "--shell", window],
        timeout=8).decode("utf-8", "replace"))
    focus, clear, module = hud_controls(dims["width"], dims["height"])
    if original_pause_menu_visible(window, dims):
        # Genuine previous CI failure #41057213142: vanilla Game Menu
        # overlaid Graphite ClickGUI. Click Back to Game ONLY if four
        # independent original button pixels prove that menu is visible.
        click((round(dims["width"] / 2.0),
               round(dims["height"] / 4.0 + 16.0)))
        time.sleep(.6)
        if original_pause_menu_visible(window, dims):
            raise AssertionError("S31 original vanilla Back to Game did not dismiss menu")
        print("CUSTOMMC_189_S31_REAL_VANILLA_PAUSE_DISMISSED=YES", flush=True)
    before = capture_original_x11(window, output / "world-before.png")
    print("CUSTOMMC_189_S31_TRUE_TWO_PLAYER_BEFORE=" + str(before), flush=True)

    start = Path(host_log).stat().st_size
    key("Shift_R")
    require_fresh(host, host_log, b"CUSTOMMC_189_NATIVE_VISUAL_PAGE=modules",
                  start, deadline, check_process, "physical GUI open")
    time.sleep(.35)
    gui = capture_original_x11(window, output / "native-modules.png")
    require_visible_graphite(before, gui)
    if original_pause_menu_visible(window, dims):
        raise AssertionError("S31 original vanilla pause menu overlaps real ClickGUI")

    for idx, (query, marker) in enumerate((
        ("array list", b"CUSTOMMC_189_REAL_ARRAYLIST_RENDERED=YES"),
        ("target hud", b"CUSTOMMC_189_REAL_TARGET_HUD_RENDERED=YES"),
    )):
        # Always route by ORIGINAL ClickGuiInputController search and
        # actual ModuleListPageContent row hitbox. No Java module injection.
        click(focus if idx == 0 else clear)
        subprocess.check_call(
            ["xdotool", "type", "--clearmodifiers", "--delay", "55", query],
            timeout=15)
        key("Return")
        time.sleep(.35)
        # Preserve the actual source window AFTER physical keyboard search:
        # when a native cursor is still grabbed, its empty search field
        # must be visible in this genuine original-game artifact.
        label = "arraylist" if idx == 0 else "targethud"
        filtered = capture_original_x11(
            window, output / ("filtered-" + label + ".png"))
        print("CUSTOMMC_189_S31_REAL_FILTERED_SEARCH=" + str(filtered),
              flush=True)
        start = Path(host_log).stat().st_size
        click(module)
        time.sleep(.35)
        clicked = capture_original_x11(
            window, output / ("clicked-" + label + ".png"))
        print("CUSTOMMC_189_S31_REAL_ROW_CLICK=" + str(clicked), flush=True)
        require_fresh(host, host_log, marker, start,
                      min(deadline, time.monotonic() + 8.0),
                      check_process, "native enabled " + query)
        print("CUSTOMMC_189_S31_TRUE_X11_MODULE_ENABLED=" + query, flush=True)

    key("Shift_R")
    time.sleep(.9)
    check_process(host, "real Minecraft post-GUI HUD")
    after = capture_original_x11(window, output / "enabled-hud.png")
    if original_pause_menu_visible(window, dims):
        raise AssertionError("S31 original Game Menu obscures enabled HUD")
    if before["dimensions"] != gui["dimensions"] or before["dimensions"] != after["dimensions"]:
        raise AssertionError("X11 Minecraft window changed size during visual proof")
    if before["sha256"] == after["sha256"]:
        raise AssertionError("before and after real HUD frame are identical")
    # A fully opened Graphite ClickGUI must NOT still cover the scene.
    if after["graphite_hits"] >= gui["graphite_hits"] - gui["graphite_samples"] // 12:
        raise AssertionError("original Graphite GUI still obscures real HUD capture")
    print("CUSTOMMC_189_S31_GENUINE_ENABLED_HUD_PIXELS=" + str(after), flush=True)
    print("CUSTOMMC_189_S31_TWO_REAL_CLIENTS_ARRAYLIST_TARGETHUD_PASS=YES",
          flush=True)
