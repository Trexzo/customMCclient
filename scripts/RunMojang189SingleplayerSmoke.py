#!/usr/bin/env python3
"""Controlled offline Minecraft 1.8.9 singleplayer-world acceptance.

Uses a genuine Java 8/Xvfb client with Mojang assets, real X11 mouse input
and CustomMC's opt-in live world tick checkpoint. Never connects to a server,
supplies online credentials, or claims entity attacks/hits.
"""
import argparse
import re
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import time

from RunMojang189GraphicalSmoke import build_command
from Mojang189VisualEvidence import (capture_original_x11,
                                    require_visible_graphite)

def v313_original_crash_report_header(game_dir, max_bytes=6000,
                                      started_after_ns=None):
    """Read one genuine local Minecraft crash report, diagnosis only.

    Original vanilla can produce thousands of repetitive obfuscated frames.
    Last-12KB stdout then contains only stack-tail noise, suppressing the
    first real Throwable and Description. Only inspect the local game's
    crash-reports directory, bounded to its FIRST 6000 report bytes.
    The return value never proves success, and all failures still re-raise.
    """
    if not isinstance(max_bytes, int) or not 128 <= max_bytes <= 6000:
        raise ValueError("V313 original crash header limit invalid")
    if started_after_ns is not None and (
            not isinstance(started_after_ns, int)
            or isinstance(started_after_ns, bool)
            or started_after_ns < 0):
        raise ValueError("V314 current original crash run clock invalid")
    root = Path(game_dir) / "crash-reports"
    if root.is_symlink() or not root.is_dir():
        return None
    # V314: select ONLY a genuine current-run local report. A previous
    # singleplayer stage can leave stale crash-*.txt in the same game root.
    # The existing latest-file heuristic could misattribute that stale crash
    # to an entirely different game's timeout. This is diagnostics, not PASS.
    reports = []
    for p in root.glob("crash-*.txt"):
        if p.is_symlink() or not p.is_file():
            continue
        stamp = p.stat().st_mtime_ns
        if started_after_ns is None or stamp >= started_after_ns:
            reports.append((stamp, p.name, p))
    if not reports:
        return None
    reports.sort(reverse=True)
    report = reports[0][2]
    # Actually bound I/O to max_bytes, not read_bytes() of a potentially
    # enormous recursive 1.8.9 vanilla StackOverflowError crash report.
    with report.open("rb") as reader:
        raw = reader.read(max_bytes)
    if not raw:
        return None
    return "file=" + report.name + "\n" + raw.decode("utf-8", "replace")


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
TIMER_DISABLED = b"CUSTOMMC_OFFICIAL_189_TIMER_DISABLED_NATIVE_ONE_X_PASS=20"
TIMER_ENABLED = b"CUSTOMMC_OFFICIAL_189_TIMER_NORMAL_MODULE_ENABLE_PASS=YES"
TIMER_BOOST = b"CUSTOMMC_OFFICIAL_189_TIMER_NATIVE_FIVE_1_5_X_CALLBACKS_PASS=YES"
TIMER_RESTORED = b"CUSTOMMC_OFFICIAL_189_TIMER_ORIGINAL_NATIVE_RESTORE_ONE_X_PASS=YES"
STEP_ARMED = b"CUSTOMMC_OFFICIAL_189_STEP_NATIVE_MODULE_ARMED_PASS=YES"
STEP_GUI_OPEN = b"CUSTOMMC_OFFICIAL_189_STEP_GUI_REAL_KEY_OPEN_NATIVE_VANILLA_PASS=YES"
STEP_GUI_CLOSED = b"CUSTOMMC_OFFICIAL_189_STEP_GUI_REAL_KEY_CLOSE_NATIVE_BOOST_PASS=YES"
STEP_RESTORED = b"CUSTOMMC_OFFICIAL_189_STEP_GUI_REAL_KEY_DISABLE_NATIVE_VANILLA_PASS=YES"
NOCLIP_ARMED = b"CUSTOMMC_OFFICIAL_189_NOCLIP_NATIVE_MODULE_ARMED_PASS=YES"
NOCLIP_GUI_OPEN = b"CUSTOMMC_OFFICIAL_189_NOCLIP_GUI_REAL_KEY_OPEN_NATIVE_FALSE_PASS=YES"
NOCLIP_GUI_CLOSED = b"CUSTOMMC_OFFICIAL_189_NOCLIP_GUI_REAL_KEY_CLOSE_NATIVE_TRUE_PASS=YES"
NOCLIP_RESTORED = b"CUSTOMMC_OFFICIAL_189_NOCLIP_GUI_REAL_KEY_DISABLE_NATIVE_FALSE_PASS=YES"
REACH_ARMED = b"CUSTOMMC_OFFICIAL_189_REACH_NATIVE_MODULE_ARMED_PASS=YES"
REACH_ENABLED = b"CUSTOMMC_OFFICIAL_189_REACH_SOURCE_NATIVE_ENABLED_PASS=YES"
REACH_GUI_OPEN = b"CUSTOMMC_OFFICIAL_189_REACH_GUI_REAL_KEY_OPEN_NATIVE_PASS=YES"
REACH_GUI_CLOSED = b"CUSTOMMC_OFFICIAL_189_REACH_GUI_REAL_KEY_CLOSE_NATIVE_PASS=YES"
REACH_DISABLED = b"CUSTOMMC_OFFICIAL_189_REACH_NATIVE_DISABLE_VANILLA_PASS=YES"
COMBATSLOT_ARMED = b"CUSTOMMC_OFFICIAL_189_COMBATSLOT_NATIVE_SELECTION_RESTORE_PASS=YES"
COMBATSLOT_GUI_OPEN = b"CUSTOMMC_OFFICIAL_189_COMBATSLOT_GUI_REAL_KEY_OPEN_VETO_PASS=YES"
COMBATSLOT_GUI_CLOSED = b"CUSTOMMC_OFFICIAL_189_COMBATSLOT_GUI_REAL_KEY_CLOSE_CONFIG_LEASE_PASS=YES"
COMBATSLOT_WRONG_TOKEN = b"CUSTOMMC_OFFICIAL_189_COMBATSLOT_NATIVE_WRONG_TOKEN_VETO_PASS=YES"
COMBATSLOT_MELEE_OWNER = b"CUSTOMMC_OFFICIAL_189_COMBATSLOT_NATIVE_MELEE_DISPATCH_OWNER_PASS=YES"
COMBATSLOT_DISABLED = b"CUSTOMMC_OFFICIAL_189_COMBATSLOT_NATIVE_DISABLE_ORIGINAL_PASS=YES"
NATIVE_ITEMS_ARMED = b"CUSTOMMC_189_NATIVE_ROD_POTION_SOURCE_MODULES_ARMED=YES"
NATIVE_ROD_EMPTY_HOOK = b"CUSTOMMC_OFFICIAL_189_ROD_NATIVE_FISH_ENTITY_EMPTY_BASELINE_PASS=YES"
NATIVE_ROD_PHYSICAL_SLOT_PREFIX = b"CUSTOMMC_OFFICIAL_189_NATIVE_CAST_ROD_SLOT="
NATIVE_ROD_PHYSICAL_SELECTED = b"CUSTOMMC_OFFICIAL_189_NATIVE_ROD_PHYSICAL_SLOT_SELECTED_PASS=YES"
NATIVE_ROD_ACTUAL_CAST = b"CUSTOMMC_OFFICIAL_189_REAL_PHYSICAL_ROD_CAST_ACTIVE_HOOK_GUARD_PASS=YES"
NATIVE_ROD_ACTUAL_REEL = b"CUSTOMMC_OFFICIAL_189_REAL_PHYSICAL_ROD_REEL_HOOK_CLEARED_PASS=YES"
NATIVE_REGEN_NO_EFFECT = b"CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_NO_EFFECT_POTION_ALLOWED_PASS=YES"
NATIVE_REGEN_WEAKER_UPGRADE = b"CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_WEAKER_EFFECT_UPGRADE_PASS=YES"
NATIVE_REGEN_AUTODETECT_HEAL = b"CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_AUTODETECT_FALLBACK_HEAL_PASS=YES"
NATIVE_REGEN_AUTODETECT_RANK = b"CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_AUTODETECT_RANK_COMPOSE_PASS=YES"
NATIVE_AUTOPOT_SOURCE_PITCH_RESTORE = b"CUSTOMMC_OFFICIAL_189_AUTOPOT_NATIVE_DOWNWARD_PITCH_RESTORE_PASS=YES"
NATIVE_REAL_SPLASH_SLOT_PREFIX = b"CUSTOMMC_OFFICIAL_189_REAL_SPLASH_SOURCE_SLOT="
NATIVE_REAL_SPLASH_READY = b"CUSTOMMC_OFFICIAL_189_REAL_SPLASH_PHYSICAL_CAST_READY_PASS=YES"
NATIVE_REAL_SPLASH_EFFECT = b"CUSTOMMC_OFFICIAL_189_REAL_SPLASH_REGEN_IMPACT_SOURCE_EFFECT_PASS=YES"
NATIVE_AUTOPOT_HEAL_SURVIVAL_READY = b"CUSTOMMC_OFFICIAL_189_AUTOPOT_REAL_SURVIVAL_SOURCE_READY_PASS=YES"
NATIVE_AUTOPOT_HEAL_LOW_ARMED = b"CUSTOMMC_OFFICIAL_189_AUTOPOT_REAL_DAMAGED_PLAYER_ARMED_PASS=YES"
NATIVE_AUTOPOT_HEAL_CAST = b"CUSTOMMC_OFFICIAL_189_AUTOPOT_ACTUAL_SYNTHETIC_RIGHTCLICK_PASS=YES"
NATIVE_AUTOPOT_HEAL_GAIN = b"CUSTOMMC_OFFICIAL_189_AUTOPOT_REAL_SURVIVAL_HEALTH_GAIN_PASS=YES"
NATIVE_TIER_REAL_BOTH = b"CUSTOMMC_OFFICIAL_189_HEALTH_TIER_TWO_REAL_POTIONS_READY_PASS=YES"
NATIVE_TIER_REAL_LARGE = b"CUSTOMMC_OFFICIAL_189_HEALTH_TIER_REAL_LARGE_DEFICIT_STRONG_PASS=YES"
NATIVE_TIER_REAL_SMALL = b"CUSTOMMC_OFFICIAL_189_HEALTH_TIER_REAL_SMALL_DEFICIT_WEAK_PASS=YES"
NATIVE_TIER_REAL_LEGACY = b"CUSTOMMC_OFFICIAL_189_HEALTH_TIER_REAL_DEFAULT_STRONG_PASS=YES"
NATIVE_WEAPON_FRAGILE = b"CUSTOMMC_OFFICIAL_189_AUTO_WEAPON_REAL_FRAGILE_DIAMOND_VETO_PASS=YES"
NATIVE_WEAPON_FRESH_TIE = b"CUSTOMMC_OFFICIAL_189_AUTO_WEAPON_REAL_DURABLE_TIE_PASS=YES"
NATIVE_ROD_SAFE_FALLBACK = b"CUSTOMMC_OFFICIAL_189_AUTO_ROD_NATIVE_FRAGILE_FALLBACK_PASS=YES"
NATIVE_ROD_FIXED_RANK = b"CUSTOMMC_OFFICIAL_189_AUTO_ROD_NATIVE_FIXED_SLOT_AND_RANK_PASS=YES"
NATIVE_REGEN_VETO = b"CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_ACTIVE_EFFECT_DUPLICATE_VETO_PASS=YES"
NATIVE_REGEN_INSTANT_EXEMPT = b"CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_INSTANT_HEAL_EXEMPT_PASS=YES"
NATIVE_REGEN_CLEARED = b"CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_EFFECT_CLEAR_REARM_PASS=YES"
NATIVE_ITEMS_NONHEALING = b"CUSTOMMC_OFFICIAL_189_ROD_POTION_NATIVE_NONHEALING_VETO_PASS=YES"
NATIVE_ITEMS_HEAL_PRIORITY = b"CUSTOMMC_OFFICIAL_189_AUTOPOT_NATIVE_STRONG_HEAL_PRIORITY_PASS=YES"
NATIVE_ITEMS_ROD_DURABILITY = b"CUSTOMMC_OFFICIAL_189_AUTOROD_NATIVE_DURABLE_ROD_PRIORITY_PASS=YES"
NATIVE_ITEMS_SELECTED = b"CUSTOMMC_OFFICIAL_189_ROD_POTION_NATIVE_SOURCE_SELECTION_PASS=YES"
NATIVE_ITEMS_GUI_OPEN = b"CUSTOMMC_OFFICIAL_189_ROD_POTION_NATIVE_GUI_VETO_PASS=YES"
NATIVE_ITEMS_GUI_CLOSE = b"CUSTOMMC_OFFICIAL_189_ROD_POTION_NATIVE_GUI_CLOSE_RESTORE_PASS=YES"


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


def await_fresh_native_visual_page(process, log, page, cursor, deadline):
    """Require a NEW post-render native page transition after physical X11 input.

    A stale 'modules' marker from the earlier screenshot must never certify
    an actual successful back press, even if the GUI stays on Module Details.
    """
    marker = ("CUSTOMMC_189_NATIVE_VISUAL_PAGE=" + page).encode("ascii")
    while time.monotonic() < deadline:
        require_running(process, "genuine original ClickGUI " + page)
        if not log.exists():
            time.sleep(.15)
            continue
        with log.open("rb") as handle:
            handle.seek(cursor)
            fresh = handle.read()
        # JVM log adapters may prefix each line with a timestamp, but
        # only a complete freshly emitted marker line can certify the page.
        if any(line.endswith(marker) for line in fresh.splitlines()):
            return
        time.sleep(.15)
    raise AssertionError(
        "no fresh source-rendered " + page
        + " page after physical original X11 navigation")


def await_original_gui_cursor_ready(process, log, deadline):
    """Demand actual native GUI_READY evidence before physical X11 click.

    A pre-existing ungrabbed original cursor may require no vanilla release;
    either way production input still checks real LWJGL/native ownership.
    """
    positives = (
        b"CUSTOMMC_189_NATIVE_GUI_CURSOR_RELEASE=YES",
        b"CUSTOMMC_189_NATIVE_GUI_CURSOR_READY=YES",
    )
    negatives = (
        b"CUSTOMMC_189_NATIVE_GUI_FOCUS_REJECTED=YES",
        b"CUSTOMMC_189_NATIVE_GUI_FOCUS_RELEASE_FAILED=YES",
        b"CUSTOMMC_189_NATIVE_GUI_FOCUS_VERIFY_FAILED=YES",
    )
    while time.monotonic() < deadline:
        require_running(process, "original GUI cursor admission")
        fresh = read_tail(log)
        if any(marker in fresh for marker in negatives):
            raise AssertionError("real original mouse focus failed before GUI click")
        if any(marker in fresh for marker in positives):
            print("CUSTOMMC_189_NATIVE_VISUAL_MOUSE_READY_SOURCE=YES",
                  flush=True)
            return
        time.sleep(.15)
    raise AssertionError(
        "no original GUI_READY/ungrabbed cursor source marker before deadline")


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



def send_verified_original_chat_reply(process, log, window, command,
                                      expected):
    """Fail closed on a FRESH original integrated-server chat confirmation.

    GUI open/close and a successfully typed X11 command alone never prove
    a teleport or a genuine Minecraft entity spawn happened. This helper
    provides no gameplay shortcuts, synthetic entities or bypassed hit gates.
    """
    if not expected or not expected.startswith(b"[CHAT] "):
        raise ValueError("require an exact original-client chat witness")
    cursor = log.stat().st_size if log.exists() else 0
    send_command(process, log, window, command)
    deadline = time.monotonic() + 8
    while time.monotonic() < deadline:
        require_running(process, "original manual melee native fixture")
        with log.open("rb") as handle:
            handle.seek(cursor)
            fresh = handle.read()
        if (b"[CHAT] Unknown command" in fresh
                or b"[CHAT] Unable to summon object" in fresh
                or b"[CHAT] No entity was found" in fresh):
            raise AssertionError(
                "genuine original command rejected manual melee fixture: "
                + command.split(" ", 1)[0])
        if expected in fresh:
            return
        time.sleep(0.2)
    raise AssertionError(
        "missing new source integrated-server reply for manual melee "
        + command.split(" ", 1)[0])


def send_verified_chunk_teleport(process, log, window):
    """Teleport the real vanilla player to LOAD the fixture chunk first.

    A filled region wholly inside chunk (0,0) still fails if that chunk is
    unloaded. The source player must visit it and receive the genuine
    integrated-server teleport response before issuing /fill.
    """
    cursor = log.stat().st_size if log.exists() else 0
    send_command(process, log, window, "/tp CIProbe 8 100 8")
    deadline = time.monotonic() + 8
    while time.monotonic() < deadline:
        require_running(process, "original fixture chunk load")
        with log.open("rb") as handle:
            handle.seek(cursor)
            fresh = handle.read()
        if b"[CHAT] Teleported CIProbe to 8.5, 100.0, 8.5" in fresh:
            # Allow at least one real client/server tick of chunk loading.
            time.sleep(1.0)
            return
        time.sleep(0.2)
    raise AssertionError(
        "no fresh original vanilla teleport reply confirming fixture "
        "chunk location before bounded deadline")


def send_verified_fill(process, log, window, command):
    """Require a *fresh* vanilla success response before relying on a world cell.

    A /fill spanning unloaded chunks can be rejected by integrated-server
    vanilla even though the X11 chat opened/closed successfully. Merely
    sending the command is not proof that the player is in a dry fixture.
    """
    cursor = log.stat().st_size if log.exists() else 0
    send_command(process, log, window, command)
    deadline = time.monotonic() + 8
    while time.monotonic() < deadline:
        require_running(process, "original Survival dry chamber fill")
        with log.open("rb") as handle:
            handle.seek(cursor)
            fresh = handle.read()
        if (b"Cannot place blocks outside of the world" in fresh
                or b"Too many blocks in the specified area" in fresh
                or b"There are no blocks to fill" in fresh):
            raise AssertionError(
                "vanilla refused the dry chamber /fill; original "
                "Survival health acceptance cannot proceed safely")
        # Genuine Mojang 1.8.9 replies "729 blocks filled" rather
        # than the newer-version "Successfully filled 729 blocks".
        # Require source chat, a POSITIVE numeric count and fresh bytes.
        if re.search(rb"\[CHAT\] [1-9][0-9]* blocks filled", fresh):
            return
        time.sleep(0.2)
    raise AssertionError(
        "no fresh original vanilla /fill success for dry chamber "
        "before bounded deadline")


def original_dry_chamber_preclear_reply(fresh):
    """Classify only a FRESH original Minecraft 1.8.9 /fill air reply.

    'No blocks filled' is legitimate solely for this sacrificial pre-clear:
    it does NOT establish the required positive subsequent stone chamber.
    No result from this classifier is a gameplay or health PASS.
    """
    if (b"Cannot place blocks outside of the world" in fresh
            or b"Too many blocks in the specified area" in fresh
            or b"There are no blocks to fill" in fresh):
        raise AssertionError("original dry preclear vanilla command rejected")
    if re.search(rb"\[CHAT\] [1-9][0-9]* blocks filled", fresh):
        return "source-blocks-cleared"
    if b"[CHAT] No blocks filled" in fresh:
        return "source-already-air"
    return None


def send_verified_original_air_preclear(process, log, window, command,
                                        stage, missing_reply):
    """Shared fail-closed original vanilla AIR reset used by V19/V23/V29/dry.

    Capture the ACTUAL client log end BEFORE sending the X11 vanilla command.
    A positive changed-block chat reply or an independently fresh literal
    'No blocks filled' is legitimate ONLY for this sacrificial preclear;
    neither proves a later required positive World change or any native ray.
    """
    if not command.startswith("/fill ") or not command.endswith(" air"):
        raise ValueError("original preclear requires actual vanilla air fill")
    cursor = log.stat().st_size if log.exists() else 0
    send_command(process, log, window, command)
    stop = time.monotonic() + 8
    while time.monotonic() < stop:
        require_running(process, stage)
        with log.open("rb") as handle:
            handle.seek(cursor)
            fresh = handle.read()
        observed = original_dry_chamber_preclear_reply(fresh)
        if observed is not None:
            return observed
        time.sleep(.2)
    raise AssertionError(missing_reply)


def prepare_verified_native_dry_chamber(process, log, window):
    """Real World pre-clear, mandatory positive stone fill, positive carve.

    An original V165 Mojang run proved that /fill stone can genuinely
    return 'No blocks filled' after previous World commands. Always begin
    this separate healing-potion fixture with the *opposite* vanilla
    material, air. The preclear can be an acknowledged no-op; BOTH
    subsequent original /fill commands must alter real server blocks.
    """
    send_verified_original_air_preclear(
        process, log, window, "/fill 4 79 4 12 87 12 air",
        "original dry chamber genuine air preclear",
        "no fresh original vanilla preclear acknowledgment before "
        "bounded dry chamber deadline")
    # These MUST each return an original, fresh positive changed-block count.
    # Never interpret the preclear (even when changed) as either proof.
    send_verified_fill(process, log, window,
                       "/fill 4 79 4 12 87 12 stone")
    send_verified_fill(process, log, window,
                       "/fill 5 80 5 11 86 11 air")


def prepare_verified_original_scaffold_raytrace_support(
        process, log, window):
    """Build the genuine V19 World.rayTraceBlocks stone source fixture.

    Old V19 used fire-and-forget /setblock; an original run can keep the
    player stably at x=.5,y=100,z=.5 yet never prove the block under them
    was stone. Fresh vanilla AIR contrast and a strictly changed STONE
    reply prove World construction ONLY, not source ray-hit acceptance.
    """
    send_verified_original_air_preclear(
        process, log, window, "/fill 0 99 0 0 99 0 air",
        "genuine original Scaffold raytrace air reset",
        "missing fresh vanilla V19 raytrace support preclear reply")
    # V299: the genuine source ray starts at (0.5,102.0,0.5) and
    # ends INSIDE the 0,99,0 stone (y=99.5). Previous V19 only
    # verified the destination stone, not the original World cells
    # at y=100..102 that the same original ray crosses FIRST.
    # A world-generated obstacle could make the original hit a
    # different block or face while the destination is FULL_SOLID.
    # Clear the real ray column using the existing FRESH original
    # source-reply helper; an already-air no-op is acceptable only
    # for this sacrificial reset, never for the required stone fill.
    send_verified_original_air_preclear(
        process, log, window, "/fill 0 100 0 0 102 0 air",
        "genuine original V19 unobstructed native ray column",
        "missing fresh vanilla V19 air-column preclear reply")
    # Neither air receipt is a positive raycast or source World
    # certification. The genuine stone change remains mandatory.
    send_verified_fill(process, log, window,
                       "/fill 0 99 0 0 99 0 stone")


def verify_original_v19_stone_after_reseat(process, log, window):
    """Read genuine vanilla World cell AFTER the final V19 source teleport.

    A successful /fill ACK means stone changed at command time. It does NOT
    prove the server World still has that stone after returning the original
    player to y=100. Never replace the real UP-face rayTraceBlocks marker.
    """
    cursor = log.stat().st_size if log.exists() else 0
    send_command(process, log, window, "/testforblock 0 99 0 stone")
    stop = time.monotonic() + 8
    while time.monotonic() < stop:
        require_running(process, "original V19 post-reseat stone readback")
        with log.open("rb") as handle:
            handle.seek(cursor)
            fresh = handle.read().lower()
        # Source vanilla 1.8.9 testforblock acknowledgement is distinct
        # from the V19 native raytracer. This is ONLY World-cell evidence.
        if (b"[chat] unknown command" in fresh
                or b"[chat] the block at" in fresh
                or b"[chat] cannot" in fresh):
            raise AssertionError(
                "original V19 post-reseat vanilla stone testforblock rejected")
        if b"[chat] successfully found the block" in fresh:
            print("CUSTOMMC_189_V19_ORIGINAL_STONE_READBACK=YES", flush=True)
            return
        time.sleep(.2)
    raise AssertionError(
        "no fresh original V19 post-reseat vanilla stone readback "
        "before bounded deadline")


def verify_original_v19_air_column_after_reseat(process, log, window):
    """Require three FRESH real vanilla AIR World replies after final V19 TP.

    V298 second authentic original V19 yielded 3,048 wrong-face source ray
    rejections with destination FULL_SOLID, never a null ray or BlockPos
    rejection. Existing V299 clears the three cells above that stone BEFORE
    the low teleport. A later obstruction is still possible; require actual
    original source-server material readback of EACH original crossed cell
    AFTER teleport. No vanilla chat reply is a client-native ray PASS.
    """
    for y in (100, 101, 102):
        # V300 authentic native source proves 1.8.9 coordinate replies use
        # 0,99,0 (no spaces). Never match a generic success or another cell.
        command = "/testforblock 0 {} 0 air".format(y)
        expected = (
            "[CHAT] Successfully found the block at 0,{},0.".format(y)
        ).encode("ascii")
        send_verified_original_chat_reply(
            process, log, window, command, expected)
    print("CUSTOMMC_189_V19_ORIGINAL_AIR_COLUMN_READBACK=YES", flush=True)


def prepare_verified_original_v26_candidate_source_lane(process, log, window):
    """Prepare ONLY true vanilla World cells before the genuine V26 ray.

    The second original V301 V26 log actually returned a cursor block at
    (1,101,0), rather than the intended EAST face of (0,99,0), while original
    selected ItemBlock count was eight and client player was grounded. Earlier
    V26 only wrote y99 and never verified the three higher air cells.

    All changes below use ordinary original X11 vanilla commands with fresh
    exact integrated-server source replies. No client World setter, spoofed
    ray, source cursor rewrite, acceptance shortcut or extra placement.
    """
    send_verified_original_chat_reply(
        process, log, window, "/tp CIProbe 0 120 0 0 0",
        b"[CHAT] Teleported CIProbe to 0.5, 120.0, 0.5")
    # Original native V26 source cursor travels near x1 above the target.
    # An unrelated source stone at (1,101,0) can steal that real cursor
    # and correctly fail the existing exact-plane native face gate.
    send_verified_original_air_preclear(
        process, log, window, "/fill 0 100 0 1 102 0 air",
        "original V26 real source cursor unobstructed air lane",
        "missing fresh original V26 air-lane preclear reply")
    send_verified_original_air_preclear(
        process, log, window, "/fill 0 99 0 0 99 0 air",
        "original V26 true support-stone contrast air preclear",
        "missing fresh original V26 support reset reply")
    # Unlike the old unacknowledged /setblock, this MUST change real
    # vanilla blocks; No blocks filled is never allowed for support.
    send_verified_fill(process, log, window,
                       "/fill 0 99 0 0 99 0 stone")
    send_verified_original_air_preclear(
        process, log, window, "/fill 1 99 0 1 99 0 air",
        "original V26 true adjacent air target",
        "missing fresh original V26 adjacent air reply")
    send_verified_original_chat_reply(
        process, log, window, "/tp CIProbe 1.05 100 0.5 90 88.6",
        b"[CHAT] Teleported CIProbe to 1.05, 100.0, 0.5")
    # A real server teleport receipt is not native x/y/z evidence.
    # The caller separately requires THREE real post-TP native ticks.
    post_tp_source_cursor = log.stat().st_size if log.exists() else 0
    for x, y, block in (
        (0, 99, "stone"),
        (1, 99, "air"),
        (1, 100, "air"),
        (1, 101, "air"),
        (1, 102, "air"),
    ):
        # Original Mojang 1.8.9 actual /testforblock coordinate formatting
        # is x,y,z with NO spaces, independently learned from real V300.
        send_verified_original_chat_reply(
            process, log, window,
            "/testforblock {} {} 0 {}".format(x, y, block),
            ("[CHAT] Successfully found the block at {},{},0."
             .format(x, y)).encode("ascii"))
    print("CUSTOMMC_189_V26_ORIGINAL_WORLD_LANE_READBACK=YES",flush=True)
    return post_tp_source_cursor


def original_v29_inventory_clear_receipt(fresh):
    """Classify actual 1.8.9 /clear reply from a caller's fresh log cursor."""
    if re.search(
            rb"\[CHAT\] Cleared the inventory of CIProbe, removing [1-9][0-9]* items",
            fresh):
        return "prior-items-removed"
    if b"[CHAT] Could not clear the inventory of CIProbe, no items to remove" in fresh:
        return "already-empty"
    return None


def original_v29_eight_stone_grant_receipt(fresh):
    """Require exact original named player and *eight* stone items."""
    return bool(re.search(
        rb"\[CHAT\] Given (?:\[)?Stone(?:\])? \* 8 to CIProbe(?:\r|\n|$)",
        fresh))


def prepare_verified_original_v29_stone_stack(process, log, window):
    """V295: normalize the genuine selected original Survival hotbar.

    Independent real V293 original V29 failure reported a READY ray candidate
    and CLEAR screen but selected native stone stack=1, whereas the unchanged
    original module's source readiness requires exactly eight. A blind /give
    stone 8 can leave an older selected one-block stack untouched.
    This helper uses ONLY actual vanilla chat/X11, never an inventory edit.
    Both source replies must be new; native runTick still must independently
    witness the actual eight-block selected ItemBlock before module enable.
    """
    cursor = log.stat().st_size if log.exists() else 0
    send_command(process, log, window, "/clear CIProbe")
    stop = time.monotonic() + 8
    cleared = None
    while time.monotonic() < stop:
        require_running(process, "real original V29 inventory reset")
        with log.open("rb") as handle:
            handle.seek(cursor)
            fresh = handle.read()
        cleared = original_v29_inventory_clear_receipt(fresh)
        if cleared is not None:
            break
        if b"[CHAT] Unknown command" in fresh:
            raise AssertionError("original V29 /clear was not recognized")
        time.sleep(.2)
    if cleared is None:
        raise AssertionError("no fresh original 1.8.9 /clear server inventory reply")
    print("CUSTOMMC_189_V29_REAL_INVENTORY_RESET_SOURCE=" + cleared,
          flush=True)

    # The actual user's 1 key selects original hotbar slot zero; do not edit
    # selectedItemIndex or synthesize an ItemStack through the bridge.
    resume_if_paused(process, log, window)
    xdotool("key", "--clearmodifiers", "1")

    cursor = log.stat().st_size if log.exists() else 0
    send_command(process, log, window, "/give CIProbe stone 8")
    stop = time.monotonic() + 8
    while time.monotonic() < stop:
        require_running(process, "real original V29 eight stone grant")
        with log.open("rb") as handle:
            handle.seek(cursor)
            fresh = handle.read()
        if original_v29_eight_stone_grant_receipt(fresh):
            print("CUSTOMMC_189_V29_REAL_STONE_EIGHT_GRANT_SOURCE=YES",
                  flush=True)
            return
        if b"[CHAT] Unknown command" in fresh:
            raise AssertionError("original V29 /give not recognized")
        time.sleep(.2)
    raise AssertionError(
        "no fresh original /give stone eight acknowledgment; cannot arm V29")


def prepare_verified_original_scaffold_survival_edge(
        process, log, window):
    """A genuine block-edge fixture with independently changed World cells.

    Prior original V169 V29 source player was stable at 1.05/100/.5 but
    the enabled Scaffold did not admit a vanilla controller click. Its
    original /setblock stone/air messages were fire-and-forget. Require
    a fresh vanilla reply to a sacrificial AIR preclear, and actual
    positive World changes to BOTH support and target with real /fill.
    This is only World preparation, never a synthetic placement PASS.
    """
    send_verified_original_air_preclear(
        process, log, window, "/fill 0 99 0 1 99 0 air",
        "original survival Scaffold real air reset",
        "no fresh original Scaffold air-preclear vanilla reply")
    # The fresh air acknowledgment is NOT an enabled placement proof.
    # Genuine changed-block confirmations here are mandatory:
    # (0,99,0) stays STONE support; (1,99,0) stays AIR target.
    send_verified_fill(process, log, window,
                       "/fill 0 99 0 1 99 0 stone")
    send_verified_fill(process, log, window,
                       "/fill 1 99 0 1 99 0 air")


def send_verified_original_scaffold_context_preclear(process, log, window):
    """Require FRESH vanilla air-reset response; no-op is allowed HERE ONLY.

    Real V295 original V23 encountered an already-stone 5x6x5 room:
    direct stone /fill returned "No blocks filled". This sacrificial air
    reset makes the following two mandatory positive fills independent of
    arbitrary original world terrain. It cannot count as a World or V23 PASS.
    """
    observed = send_verified_original_air_preclear(
        process, log, window, "/fill 0 99 0 4 104 4 air",
        "original V23 true vanilla context preclear",
        "no FRESH original vanilla V23 context air-preclear source reply")
    print("CUSTOMMC_189_V23_NATIVE_CONTEXT_PRECLEAR_SOURCE="
          + observed, flush=True)
    return observed


def prepare_native_scaffold_context_chamber(process, log, window):
    """Make a real, idempotently source-verified chamber in loaded chunk 0.

    Vanilla World terrain may ALREADY be all stone at this location. The
    sacrificial fresh source-acknowledged air-preclear may be a genuine
    no-op; both subsequent /fill commands STILL require positive real
    changed-block receipts before the actual original native player ticks.
    """
    send_verified_original_chat_reply(
        process, log, window, "/tp CIProbe 2 120 2 0 0",
        b"[CHAT] Teleported CIProbe to 2.5, 120.0, 2.5")
    # The actual original player stays above the room while changing cells.
    send_verified_original_scaffold_context_preclear(
        process, log, window)
    # The opposite air material is source-acknowledged. Both real positive
    # changes are now mandatory independent of the initial terrain.
    send_verified_fill(process, log, window,
                       "/fill 0 99 0 4 104 4 stone")
    send_verified_fill(process, log, window,
                       "/fill 0 100 0 4 104 4 air")
    send_verified_original_chat_reply(
        process, log, window, "/tp CIProbe 2 100 2 0 0",
        b"[CHAT] Teleported CIProbe to 2.5, 100.0, 2.5")


def native_scaffold_context_grounded(readings):
    """Use only genuine player position ticks, not a teleport command log."""
    if len(readings) < 3:
        return False
    last = readings[-3:]
    if not all(abs(x - 2.5) < .15 and abs(y - 100.0) < .15
               and abs(z - 2.5) < .15 for x, y, z in last):
        return False
    return all(max(p[axis] for p in last) - min(p[axis] for p in last)
               <= .03 for axis in range(3))


def native_safe_walk_positions(log, since=None):
    """Exact native source x/y/z; optionally ONLY complete lines after cursor.

    V301 V16 proves post-teleport native grounding. Earlier V16 read only
    the last bytes of the full log, so pre-teleport native samples could
    satisfy its three-position predicate. The optional byte cursor reads
    only original client lines appended AFTER its genuine TP receipt.
    """
    if since is None:
        source = read_tail(log)
    else:
        if not isinstance(since, int) or since < 0:
            raise AssertionError("invalid original native position log cursor")
        with log.open("rb") as handle:
            handle.seek(0, 2)
            size = handle.tell()
            if since > size:
                raise AssertionError("original native position cursor exceeds log")
            handle.seek(since)
            source = handle.read()
        # Never parse the tail of a concurrently written native source line.
        # A complete newline-terminated original callback is required.
        newline = source.rfind(b"\n")
        source = source[:newline + 1] if newline >= 0 else b""
    prefix = b"CUSTOMMC_189_NATIVE_LOCAL_POS="
    samples = []
    for line in source.splitlines():
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


def original_edge_grounding_stable(readings):
    """Require stable native feet within the real adjacent support interior.

    Vanilla may resolve requested z=.5 to z=.30 through collision. The
    genuine source-ray, ItemBlock and original-world candidate gates remain
    mandatory later; this pregrant check authorizes no placement.
    """
    if len(readings) < 3:
        return False
    tail = readings[-3:]
    if not all(abs(x - 1.05) < .05 and abs(y - 100.0) < .15
               and .20 <= z <= .80 for x, y, z in tail):
        return False
    return all(max(p[axis] for p in tail) - min(p[axis] for p in tail) <= .02
               for axis in range(3))


def original_v29_position_accepted(readings):
    """Unchanged three native per-tick x/y/z samples at the real edge."""
    return len(readings) >= 3 and all(
        abs(x - 1.05) < .05 and abs(y - 100.0) < .15
        and abs(z - .5) < .05 for x, y, z in readings[-3:])


def original_v29_settled_edge_drift(readings):
    """Detect motion loss; this NEVER satisfies original placement proof."""
    if len(readings) < 3:
        return False
    last = readings[-3:]
    return (all(.30 <= x <= 1.30 and abs(y - 100.0) < .15
                and .20 <= z <= .80 and abs(x - 1.05) >= .05
                for x, y, z in last)
            and all(max(p[axis] for p in last) - min(p[axis] for p in last)
                    <= .02 for axis in range(3)))


def original_v275_native_edge_fall(readings):
    """A stable source-native fall into the adjacent air cell is NOT grounding.

    A genuine new vanilla teleport and three original feet ticks must follow.
    """
    if len(readings) < 3:
        return False
    tail = readings[-3:]
    return (all(1.30 < x < 2.00 and 98.5 <= y <= 99.5
                and .20 <= z <= .80 for x, y, z in tail)
            and all(max(p[axis] for p in tail) - min(p[axis] for p in tail)
                    <= .03 for axis in range(3)))


def await_original_v29_survival_edge_grounding(process, log, window):
    """One actual acknowledged corrective /tp for settled native momentum.

    Original V257 rerun confirmed native x after genuine /tp 1.05 moved
    to .9499999985, .6953999884 and then .6404239223. Preserve the
    original strict x/y/z requirement and original 12-second deadline.
    """
    started = time.monotonic()
    corrected = False
    last = []
    while time.monotonic() - started < 12:
        last = native_safe_walk_positions(log)[-5:]
        if original_v29_position_accepted(last):
            return
        require_running(process, "source-native survival ledge grounding")
        if (not corrected and time.monotonic() - started >= .75
                and (original_v29_settled_edge_drift(last)
                     or original_v275_native_edge_fall(last))):
            corrected = True
            send_verified_original_chat_reply(
                process, log, window,
                "/tp CIProbe 1.05 100 0.5 90 88.6",
                b"[CHAT] Teleported CIProbe to 1.05, 100.0, 0.5")
            # Original integrated-server chat is not a native position.
            # Continue demanding three real mapped local-player samples.
            continue
        time.sleep(.15)
    raise AssertionError(
        "original Survival player not grounded on real support"
        + " nativeLast=" + repr(last[-3:])
        + " realCorrection=" + str(corrected))


def run(command, game, timeout, attack=False, auto_clicker=False,
        kill_aura_nonplayer=False, timer_speed=False, native_world_camera=False,
        native_safe_walk=False, safe_walk_edge=None, native_scaffold_world=False,
        native_scaffold_tick=False, native_scaffold_raytrace=False,
        native_scaffold_context=False, native_scaffold_candidate=False,
        native_scaffold_placement=None, native_step_clickgui=False,
        native_noclip_clickgui=False, native_reach_clickgui=False,
        native_combatslot_clickgui=False, native_rod_potion_discovery=False,
        visual_capture_dir=None, visual_capture_details=False):
    if sum(bool(x) for x in (attack, auto_clicker, kill_aura_nonplayer,
                             timer_speed, native_step_clickgui,
                             native_noclip_clickgui,
                             native_reach_clickgui,
                             native_combatslot_clickgui,
                             native_rod_potion_discovery)) > 1:
        raise ValueError("manual and module combat tests are exclusive")
    if visual_capture_details and not visual_capture_dir:
        raise ValueError("contextual detail capture requires real visual capture directory")
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
                                    or native_scaffold_tick or native_scaffold_candidate):
        raise ValueError("original Scaffold context fixture must be independent")
    if native_scaffold_candidate and (controlled_combat or safe_walk_edge
                                      or native_safe_walk or native_scaffold_world
                                      or native_scaffold_tick or native_scaffold_raytrace
                                      or native_scaffold_placement):
        raise ValueError("native candidate fixture must be independent")
    if native_scaffold_placement not in (None, "enabled", "disabled"):
        raise ValueError("invalid native original controller placement mode")
    if native_scaffold_placement and (
            controlled_combat or safe_walk_edge or native_safe_walk
            or native_scaffold_world or native_scaffold_tick
            or native_scaffold_raytrace or native_scaffold_context
            or native_scaffold_candidate):
        raise ValueError("native survival placement must run in an isolated world")
    # A real rendered world camera requires a focused Minecraft window;
    # vanilla singleplayer otherwise automatically opens the pause GUI.
    needs_active_window = controlled_combat or timer_speed or native_world_camera or native_safe_walk or bool(safe_walk_edge) or native_scaffold_world or native_scaffold_tick or native_scaffold_raytrace or native_scaffold_context or native_scaffold_candidate or bool(native_scaffold_placement) or native_step_clickgui or native_noclip_clickgui or native_reach_clickgui or native_combatslot_clickgui or native_rod_potion_discovery or bool(visual_capture_dir)
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
    if native_scaffold_placement:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireOriginalScaffoldContext=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireOriginalScaffoldReach=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireOriginalScaffoldEye=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportNativeSafeWalkPosition=true")
        if native_scaffold_placement == "enabled":
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireOriginalScaffoldPlacement=true")
        else:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireOriginalScaffoldDisabledControl=true")
    if native_scaffold_candidate:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireOriginalScaffoldCandidate=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportNativeSafeWalkPosition=true")
    if native_scaffold_context:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireOriginalScaffoldReach=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireOriginalScaffoldEye=true")
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
    if visual_capture_dir:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportNativeVisualPage=true")
    command.insert(command.index("-cp"), "-Dcustommc.acceptance.reportLiveWorld=true")
    if timer_speed:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireRealTimerMultiplier=true")
    if native_step_clickgui:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeStepGuiFocus=true")
    if native_noclip_clickgui:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeNoClipGuiFocus=true")
    if native_reach_clickgui:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeReachGuiFocus=true")
    if native_combatslot_clickgui:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeCombatSlotGuiFocus=true")
    if native_rod_potion_discovery:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireNativeRodPotionDiscovery=true")
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
            # Capture this run's wall-clock before starting the actual game.
            # This is only for crash-file provenance, never timeout authority.
            v314_native_process_started_ns = time.time_ns()
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
            if controlled_combat or safe_walk_edge or native_scaffold_world or native_scaffold_tick or native_scaffold_raytrace or native_scaffold_context or native_scaffold_candidate or native_scaffold_placement or native_rod_potion_discovery:
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
            if visual_capture_dir:
                # The genuine Mojang world is loaded, source-currentScreen
                # is CLEAR and the live OpenGL window is X11-focused.
                # This is an independently opted-in visual diagnostic, not
                # a synthetic Java renderer or a combat acceptance shortcut.
                import json
                resume_if_paused(process, log, window)
                xdotool("windowactivate", "--sync", window)
                output = Path(visual_capture_dir)
                if output.is_symlink():
                    raise ValueError("visual output directory may not be a symlink")
                before = capture_original_x11(window, output / "world.png")
                print("CUSTOMMC_189_NATIVE_VISUAL_WORLD="
                      + json.dumps(before, sort_keys=True), flush=True)
                xdotool("key", "--clearmodifiers", "Shift_R")
                await_marker(process, log,
                             b"CUSTOMMC_189_NATIVE_VISUAL_PAGE=modules",
                             deadline, "source-rendered Modules page")
                time.sleep(.4)
                require_running(process, "native ClickGUI screenshot")
                modules = capture_original_x11(window, output / "modules.png")
                print("CUSTOMMC_189_NATIVE_VISUAL_MODULES="
                      + json.dumps(modules, sort_keys=True), flush=True)
                require_visible_graphite(before, modules)
                print("CUSTOMMC_189_NATIVE_VISUAL_GRAPHITE_PANEL_PASS=YES",
                      flush=True)
                if visual_capture_details:
                    # A real original-game cursor grab must have been
                    # released by vanilla Minecraft's ave.o() first. The
                    # existing strict physical-mouse admission is unchanged.
                    await_original_gui_cursor_ready(
                        process, log, min(deadline, time.monotonic() + 6))
                    # Genuine source-rendered S25 Module Details, reached
                    # through the actual visible first module's right click.
                    # Exactly 854x480/GUI scale 1 is pinned by this harness.
                    if modules["dimensions"] != [854, 480]:
                        raise AssertionError(
                            "detail pointer proof requires pinned 854x480 X11 geometry")
                    detail_cursor = log.stat().st_size
                    xdotool("mousemove", "--window", window, 300, 114)
                    xdotool("click", "--window", window, "3")
                    await_fresh_native_visual_page(
                        process, log, "module-detail", detail_cursor,
                        min(deadline, time.monotonic() + 9))
                    time.sleep(.35)
                    detail_image = capture_original_x11(
                        window, output / "module-detail.png")
                    print("CUSTOMMC_189_NATIVE_VISUAL_DETAIL_CAPTURE="
                          + json.dumps(detail_image, sort_keys=True), flush=True)
                    require_visible_graphite(before, detail_image)

                    # Actual left click at the fixed top-right S25
                    # < Modules return action. The source marker must be
                    # NEW: the previous modules.png marker cannot count.
                    back_cursor = log.stat().st_size
                    # At native 854x480 scale 1, layout root y=52.8,
                    # detail-back button y=56.8..73.8. The old y=47
                    # landed OUTSIDE the real button.
                    xdotool("mousemove", "--window", window, 733, 65)
                    xdotool("click", "--window", window, "1")
                    await_fresh_native_visual_page(
                        process, log, "modules", back_cursor,
                        min(deadline, time.monotonic() + 9))
                    time.sleep(.35)
                    returned_image = capture_original_x11(
                        window, output / "modules-return.png")
                    print("CUSTOMMC_189_NATIVE_VISUAL_BACK_CAPTURE="
                          + json.dumps(returned_image, sort_keys=True),
                          flush=True)
                    require_visible_graphite(before, returned_image)
                    print("CUSTOMMC_189_NATIVE_VISUAL_DETAIL_BACK_PASS=YES",
                          flush=True)
                # Down is a physical original Keyboard.next event routed to
                # ClickGuiInputController page navigation, not direct model mutation.
                xdotool("key", "--clearmodifiers", "Down")
                await_marker(process, log,
                             b"CUSTOMMC_189_NATIVE_VISUAL_PAGE=settings",
                             deadline, "source-rendered Settings page")
                time.sleep(.35)
                require_running(process, "native Settings screenshot")
                settings_image = capture_original_x11(
                    window, output / "settings.png")
                print("CUSTOMMC_189_NATIVE_VISUAL_SETTINGS_CAPTURE="
                      + json.dumps(settings_image, sort_keys=True),
                      flush=True)
                xdotool("key", "--clearmodifiers", "Shift_R")
                if visual_capture_details:
                    await_marker(
                        process, log,
                        b"CUSTOMMC_189_NATIVE_GUI_CURSOR_RESTORE=YES",
                        min(deadline, time.monotonic() + 6),
                        "original vanilla cursor restoration after GUI close")
                print("CUSTOMMC_189_NATIVE_VISUAL_SCREENSHOTS_RECORDED=YES",
                      flush=True)
            if timer_speed:
                resume_if_paused(process, log, window)
                await_marker(process, log, TIMER_DISABLED, deadline,
                             "twenty original disabled Timer 1x callbacks")
                await_marker(process, log, TIMER_ENABLED, deadline,
                             "normal Timer module activation")
                await_marker(process, log, TIMER_BOOST, deadline,
                             "five real Mojang Timer 1.5x callbacks")
                await_marker(process, log, TIMER_RESTORED, deadline,
                             "original Game Timer exact vanilla 1x after disable")
                print("CUSTOMMC_OFFICIAL_189_TIMER_REAL_NATIVE_MULTIPLIER_PASS=YES",
                      flush=True)
            if native_step_clickgui:
                resume_if_paused(process, log, window)
                await_marker(process, log, STEP_ARMED, deadline,
                             "12 actual source-native vanilla Step baselines and normal module enable")
                # Use the real X11-focused window and original Mojang
                # Keyboard.next() input path. Never call a fake Java GUI
                # toggle from the acceptance harness.
                xdotool("windowactivate", "--sync", window)
                xdotool("key", "Shift_R")
                print("CUSTOMMC_189_X11_REAL_RIGHT_SHIFT_GUI_OPEN_SENT=YES", flush=True)
                await_marker(process, log, STEP_GUI_OPEN, deadline,
                             "real Right Shift GUI-open source-native 0.6 stepHeight")
                xdotool("key", "Shift_R")
                print("CUSTOMMC_189_X11_REAL_RIGHT_SHIFT_GUI_CLOSE_SENT=YES", flush=True)
                await_marker(process, log, STEP_GUI_CLOSED, deadline,
                             "real Right Shift GUI-close source-native 1.8 stepHeight")
                await_marker(process, log, STEP_RESTORED, deadline,
                             "normal ModuleController disable and source-native vanilla 0.6")
                print("CUSTOMMC_OFFICIAL_189_STEP_REAL_GUI_NATIVE_LIFECYCLE_PASS=YES",
                      flush=True)
            if native_noclip_clickgui:
                resume_if_paused(process, log, window)
                await_marker(process, log, NOCLIP_ARMED, deadline,
                             "twelve genuine native NoClip false baselines then source module enable")
                # Real X11 input through the focused original client,
                # not a synthetic Java GUI toggle or fabricated player.
                xdotool("windowactivate", "--sync", window)
                xdotool("key", "Shift_R")
                print("CUSTOMMC_189_X11_NOCLIP_REAL_SHIFT_GUI_OPEN_SENT=YES", flush=True)
                await_marker(process, log, NOCLIP_GUI_OPEN, deadline,
                             "source noClip false on actual menu open")
                xdotool("key", "Shift_R")
                print("CUSTOMMC_189_X11_NOCLIP_REAL_SHIFT_GUI_CLOSE_SENT=YES", flush=True)
                await_marker(process, log, NOCLIP_GUI_CLOSED, deadline,
                             "source noClip true on menu close")
                await_marker(process, log, NOCLIP_RESTORED, deadline,
                             "native false after original module disable")
                print("CUSTOMMC_OFFICIAL_189_NOCLIP_REAL_GUI_NATIVE_LIFECYCLE_PASS=YES",
                      flush=True)
            if native_reach_clickgui:
                resume_if_paused(process, log, window)
                await_marker(process, log, REACH_ARMED, deadline,
                             "12 original disabled EntityRenderer ray operand callbacks")
                await_marker(process, log, REACH_ENABLED, deadline,
                             "normal Reach ModuleController enable observed on original raycast")
                xdotool("windowactivate", "--sync", window)
                xdotool("key", "Shift_R")
                print("CUSTOMMC_189_X11_REACH_REAL_GUI_OPEN_SENT=YES", flush=True)
                await_marker(process, log, REACH_GUI_OPEN, deadline,
                             "original renderer operand restored by physical ClickGUI open")
                xdotool("key", "Shift_R")
                print("CUSTOMMC_189_X11_REACH_REAL_GUI_CLOSE_SENT=YES", flush=True)
                await_marker(process, log, REACH_GUI_CLOSED, deadline,
                             "original renderer Reach override resumed by physical GUI close")
                await_marker(process, log, REACH_DISABLED, deadline,
                             "original ray operand restored on controller disable")
                print("CUSTOMMC_OFFICIAL_189_REACH_REAL_RENDERER_GUI_PASS=YES",
                      flush=True)
            if native_combatslot_clickgui:
                resume_if_paused(process, log, window)
                await_marker(process, log, COMBATSLOT_ARMED, deadline,
                             "12 stable original InventoryPlayer currentItem callbacks and real CombatSlot switch/restore")
                xdotool("windowactivate", "--sync", window)
                xdotool("key", "Shift_R")
                print("CUSTOMMC_189_X11_COMBATSLOT_REAL_GUI_OPEN_SENT=YES", flush=True)
                await_marker(process, log, COMBATSLOT_GUI_OPEN, deadline,
                             "physical ClickGUI open vetoes real CombatSlot currentItem write")
                xdotool("key", "Shift_R")
                print("CUSTOMMC_189_X11_COMBATSLOT_REAL_GUI_CLOSE_SENT=YES", flush=True)
                await_marker(process, log, COMBATSLOT_WRONG_TOKEN, deadline,
                             "wrong positive and nested source restore tokens cannot unlock genuine Mojang currentItem")
                await_marker(process, log, COMBATSLOT_MELEE_OWNER, deadline,
                             "original Minecraft melee dispatch allowed only under true native hotbar token and no GUI focus")
                await_marker(process, log, COMBATSLOT_GUI_CLOSED, deadline,
                             "real InventoryPlayer selected slot remains owned across live Preferred Slot edit")
                await_marker(process, log, COMBATSLOT_DISABLED, deadline,
                             "normal CombatSlot disable returns unchanged native selected slot")
                print("CUSTOMMC_OFFICIAL_189_COMBATSLOT_REAL_HOTBAR_GUI_LIFECYCLE_PASS=YES",
                      flush=True)
            if native_rod_potion_discovery:
                resume_if_paused(process, log, window)
                await_marker(process, log, NATIVE_ITEMS_ARMED, deadline,
                             "original Mojang player/currentItem and normally enabled source modules")
                await_marker(process, log, NATIVE_ROD_EMPTY_HOOK, deadline,
                             "genuine Mojang 1.8.9 EntityPlayer fishEntity read-only absent baseline")
                # Actual 1.8.9 /give commands on a real creative integrated
                # server; no fabricated Java ItemStack or injected packet.
                send_command(process, log, window, "/give CIProbe fishing_rod 1 60")
                send_command(process, log, window, "/give CIProbe fishing_rod 1 0")
                send_command(process, log, window, "/give CIProbe potion 1 0")
                await_marker(process, log, NATIVE_ITEMS_NONHEALING, deadline,
                             "actual Mojang source water ItemPotion cannot authorize AutoPot")
                send_command(process, log, window, "/give CIProbe potion 1 16389")
                send_command(process, log, window, "/give CIProbe potion 1 16421")
                await_marker(process, log, NATIVE_ITEMS_ROD_DURABILITY, deadline,
                             "genuine Mojang pristine fishing rod outranks earlier damaged rod when opted in")
                await_marker(process, log, NATIVE_ITEMS_HEAL_PRIORITY, deadline,
                             "real source splash instant-heal II outranks earlier I when opted in")
                await_marker(process, log, NATIVE_ITEMS_SELECTED, deadline,
                             "real transformed ItemFishingRod/ItemPotion discovery and owner-safe restore")
                xdotool("windowactivate", "--sync", window)
                xdotool("key", "Shift_R")
                await_marker(process, log, NATIVE_ITEMS_GUI_OPEN, deadline,
                             "physical Right Shift ClickGUI vetoes source Rod/Potion selection")
                xdotool("key", "Shift_R")
                await_marker(process, log, NATIVE_ITEMS_GUI_CLOSE, deadline,
                             "source Rod/Potion discovery resumes, restores, and normal controller disables")
                await_marker(process, log, NATIVE_ROD_PHYSICAL_SLOT_PREFIX, deadline,
                             "native original ItemFishingRod slot handoff for real physical key")
                slot_values = [line.split(NATIVE_ROD_PHYSICAL_SLOT_PREFIX, 1)[1].strip()
                               for line in read_tail(log).splitlines()
                               if NATIVE_ROD_PHYSICAL_SLOT_PREFIX in line]
                if len(slot_values) != 1 or slot_values[0] not in (
                        b"1", b"2", b"3", b"4", b"5",
                        b"6", b"7", b"8", b"9"):
                    raise AssertionError("missing/invalid genuine original fishing rod hotbar slot")
                xdotool("windowactivate", "--sync", window)
                xdotool("key", "--clearmodifiers",
                        slot_values[0].decode("ascii"))
                await_marker(process, log, NATIVE_ROD_PHYSICAL_SELECTED, deadline,
                             "physical 1..9 key selected true source ItemFishingRod with empty hook")
                # Actual original Mojang right-click, not a Java test fake
                # or an injected packet. The acceptance side MUST witness
                # fishEntity become non-null and guard a real selected rod.
                xdotool("mousemove", "--window", window, 427, 240)
                xdotool("click", "--window", window, "3")
                await_marker(process, log, NATIVE_ROD_ACTUAL_CAST, deadline,
                             "physical original right click created live fishEntity and AutoRod vetoed reeling")
                # Manual input must remain vanilla even with the optional
                # automated guard enabled. A second real right-click reels.
                xdotool("click", "--window", window, "3")
                await_marker(process, log, NATIVE_ROD_ACTUAL_REEL, deadline,
                             "physical vanilla right-click cleared original fishEntity and resumed owner gate")
                print("CUSTOMMC_OFFICIAL_189_ROD_POTION_REAL_SOURCE_GUI_ACCEPTANCE_PASS=YES",
                      flush=True)
                print("CUSTOMMC_OFFICIAL_189_PHYSICAL_FISH_ENTITY_CAST_GUARD_REEL_PASS=YES",
                      flush=True)
                # This is genuine Mojang 1.8.9 /give and /effect,
                # not a fabricated PotionEffectAccess test double.
                send_command(process, log, window, "/give CIProbe potion 1 16417")
                await_marker(process, log, NATIVE_REGEN_NO_EFFECT, deadline,
                             "real splash Regeneration II selected when actual player has no effect")
                send_command(process, log, window, "/effect CIProbe 10 30 0")
                await_marker(process, log, NATIVE_REGEN_WEAKER_UPGRADE, deadline,
                             "vanilla Regeneration I amplifier 0 permits stronger source Regeneration II upgrade")
                send_command(process, log, window, "/effect CIProbe 10 30 1")
                await_marker(process, log, NATIVE_REGEN_VETO, deadline,
                             "native real source Regeneration II amplifier 1 blocks duplicate source splash")
                await_marker(process, log, NATIVE_REGEN_INSTANT_EXEMPT, deadline,
                             "genuine Instant Health II remains eligible despite original active regeneration")
                send_command(process, log, window, "/effect CIProbe clear")
                await_marker(process, log, NATIVE_REGEN_CLEARED, deadline,
                             "vanilla source /effect clear restores legal potion lease and owner rollback")
                print("CUSTOMMC_OFFICIAL_189_REAL_REGENERATION_EFFECT_GUARD_X11_PASS=YES",
                      flush=True)
                # Regrant the source item order through the *real vanilla*
                # inventory, not a test adapter or Java array edit.
                send_command(process, log, window, "/clear CIProbe potion")
                send_command(process, log, window, "/give CIProbe potion 1 16417")
                send_command(process, log, window, "/give CIProbe potion 1 16421")
                send_command(process, log, window, "/effect CIProbe 10 30 1")
                await_marker(process, log, NATIVE_REGEN_AUTODETECT_HEAL, deadline,
                             "actual native early redundant Regen II is skipped for later Instant Health II")
                await_marker(process, log, NATIVE_REGEN_AUTODETECT_RANK, deadline,
                             "V134 native best-healing rank composes with V142 active-effect eligibility")
                await_marker(process, log, NATIVE_AUTOPOT_SOURCE_PITCH_RESTORE, deadline,
                             "real Mojang Entity.rotationPitch aims self-splash down and restores physical player's pitch")
                print("CUSTOMMC_OFFICIAL_189_NATIVE_REGEN_AUTO_DISCOVERY_ALTERNATIVE_X11_PASS=YES",
                      flush=True)
                print("CUSTOMMC_OFFICIAL_189_REAL_AUTOPOT_DOWNWARD_PITCH_SOURCE_PASS=YES",
                      flush=True)
                # V150: establish a genuinely loaded, dry original-world
                # chamber BEFORE the V144 physical splash. Earlier original
                # runs could reach the source item/pitch proof while the
                # player's world-generated water spawn prevented an actual
                # self-impact. Retain a real thrown EntityPotion and require
                # a subsequent same-player native Regeneration effect;
                # chamber construction is NOT a substitute for that proof.
                # Load target chunk using genuine Creative teleport FIRST.
                send_command(process, log, window, "/gamemode creative CIProbe")
                # A previous TRUE original V132 native failure showed
                # CIProbe killed by an actual Creeper AFTER AutoPot cast
                # but BEFORE the required same-player +4 health proof.
                # Disable NEW natural hostile spawns in the genuine
                # original integrated server and remove already-spawned
                # Creepers with a vanilla command while still Creative.
                # Never set Peaceful or grant health: both would fake or
                # obscure the Survival potion healing assertion.
                send_command(process, log, window, "/gamerule doMobSpawning false")
                await_marker(process, log,
                             b"[CHAT] Game rule has been updated",
                             deadline, "vanilla original game rule stops new mobs")
                send_command(process, log, window, "/kill @e[type=Creeper]")
                print("CUSTOMMC_189_V213_ACTUAL_SURVIVAL_CREEPER_GUARD_ACK=YES",
                      flush=True)
                send_verified_chunk_teleport(process, log, window)
                # V166 original dry-chamber contrast reset: the old
                # initial solid /fill sometimes returned genuine vanilla
                # '[CHAT] No blocks filled' after other original-world work.
                # An acknowledged air preclear followed by TWO real changed
                # /fill replies supplies fresh source-world proof.
                prepare_verified_native_dry_chamber(process, log, window)
                send_command(process, log, window, "/tp CIProbe 8 80 8")
                print("CUSTOMMC_189_DRY_CHAMBER_BEFORE_PHYSICAL_SPLASH_PASS=YES",
                      flush=True)
                # V144: real vanilla player physically throws a fresh source
                # splash Regeneration I at own feet. This phase explicitly
                # contains NO /effect command: a new original player
                # Regeneration 10 effect must originate from the thrown
                # vanilla potion instead of fixture-supplied effects.
                send_command(process, log, window, "/effect CIProbe clear")
                send_command(process, log, window, "/clear CIProbe potion")
                send_command(process, log, window, "/give CIProbe potion 1 16449")
                await_marker(process, log, NATIVE_REAL_SPLASH_SLOT_PREFIX, deadline,
                             "genuine new extended Regeneration splash source slot handoff")
                physical_splash_slots = [
                    line.split(NATIVE_REAL_SPLASH_SLOT_PREFIX, 1)[1].strip()
                    for line in read_tail(log).splitlines()
                    if NATIVE_REAL_SPLASH_SLOT_PREFIX in line
                ]
                if len(physical_splash_slots) != 1 or physical_splash_slots[0] not in (
                        b"1", b"2", b"3", b"4", b"5",
                        b"6", b"7", b"8", b"9"):
                    raise AssertionError(
                        "original splash source slot missing or ambiguous")
                xdotool("windowactivate", "--sync", window)
                xdotool("mousemove", "--window", window, 427, 240)
                xdotool("key", "--clearmodifiers",
                        physical_splash_slots[0].decode("ascii"))
                await_marker(process, log, NATIVE_REAL_SPLASH_READY, deadline,
                             "real original splash potion and 85-degree physical pitch armed with no native effect")
                # Cursor already centered BEFORE the source pitch lease
                # was installed. Moving it after native +85 could count as
                # real mouse aim movement and revoke the cast evidence.
                # Physical vanilla right-click creates EntityPotion.
                xdotool("click", "--window", window, "3")
                await_marker(process, log, NATIVE_REAL_SPLASH_EFFECT, deadline,
                             "vanilla projectile splash generated NEW original local Regeneration without /effect")
                print("CUSTOMMC_OFFICIAL_189_REAL_VANILLA_SPLASH_IMPACT_PASS=YES",
                      flush=True)
                # V145: physical V144 splash already completed inside the
                # verified dry cell. No further physical clicks: genuine
                # AutoPot owns the eventual original native right click.
                send_command(process, log, window, "/effect CIProbe clear")
                send_command(process, log, window, "/clear CIProbe potion")
                # Do not reconstruct/replace the chamber or teleport the
                # source between the real splash and survival health proof.
                # Genuine splash world remains unchanged, but remove
                # hostiles which entered the chamber while placing the
                # original stone fixture. This does not heal CIProbe.
                send_command(process, log, window, "/kill @e[type=Creeper]")
                send_command(process, log, window, "/gamemode survival CIProbe")
                xdotool("windowactivate", "--sync", window)
                xdotool("key", "--clearmodifiers", "9")
                send_command(process, log, window, "/give CIProbe potion 1 16421")
                await_marker(process, log, NATIVE_AUTOPOT_HEAL_SURVIVAL_READY, deadline,
                             "real source player has full Survival health and unique genuine Instant Health II splash")
                send_command(process, log, window, "/effect CIProbe 7 1 0")
                await_marker(process, log, NATIVE_AUTOPOT_HEAL_LOW_ARMED, deadline,
                             "actual original Survival player damaged by vanilla Instant Damage and native AutoPot armed")
                await_marker(process, log, NATIVE_AUTOPOT_HEAL_CAST, deadline,
                             "true production AutoPot executes original Minecraft.rightClickMouse at source hurt player health")
                await_marker(process, log, NATIVE_AUTOPOT_HEAL_GAIN, deadline,
                             "same original hurt Survival player gains four actual health from source potion impact")
                print("CUSTOMMC_OFFICIAL_189_REAL_SYNTHETIC_AUTOPOT_SELF_HEAL_PASS=YES",
                      flush=True)
                # V147 native source health-aware ranking: the original
                # Survival world applies actual vanilla health/damage,
                # not a fabricated Java player state or ItemStack.
                # First restore full HP; /give strong before weak so a
                # held-first/default rank would select strong.
                send_command(process, log, window, "/effect CIProbe clear")
                send_command(process, log, window, "/effect CIProbe 6 1 1")
                send_command(process, log, window, "/clear CIProbe potion")
                xdotool("windowactivate", "--sync", window)
                xdotool("key", "--clearmodifiers", "9")
                send_command(process, log, window, "/give CIProbe potion 1 16421")
                send_command(process, log, window, "/give CIProbe potion 1 16389")
                await_marker(process, log, NATIVE_TIER_REAL_BOTH, deadline,
                             "genuine original full-health Survival and both source healing tiers")
                send_command(process, log, window, "/effect CIProbe 7 1 0")
                await_marker(process, log, NATIVE_TIER_REAL_LARGE, deadline,
                             "real native player missing >4 HP selects original Instant Health II")
                send_command(process, log, window, "/effect CIProbe 6 1 0")
                await_marker(process, log, NATIVE_TIER_REAL_SMALL, deadline,
                             "real native player missing <=4 HP conserves strong II")
                await_marker(process, log, NATIVE_TIER_REAL_LEGACY, deadline,
                             "same source player defaults to stronger II when conservation off")
                print("CUSTOMMC_OFFICIAL_189_REAL_SURVIVAL_HEALTH_TIER_RANK_PASS=YES",
                      flush=True)
                # V148: genuine original vanilla source ItemSword durability.
                # Three actual in-world /give commands; no mocked Java
                # source sword objects and no combat packet fabricated.
                send_command(process, log, window, "/clear CIProbe")
                xdotool("windowactivate", "--sync", window)
                xdotool("key", "--clearmodifiers", "9")
                send_command(process, log, window, "/give CIProbe diamond_sword 1 1558")
                send_command(process, log, window, "/give CIProbe iron_sword 1 10")
                await_marker(process, log, NATIVE_WEAPON_FRAGILE, deadline,
                             "original worn diamond sword is rejected in favor of durable genuine iron sword")
                send_command(process, log, window, "/give CIProbe diamond_sword 1 0")
                await_marker(process, log, NATIVE_WEAPON_FRESH_TIE, deadline,
                             "native pristine diamond wins equal true ItemSword base-damage durability tie")
                print("CUSTOMMC_OFFICIAL_189_REAL_SWORD_DURABILITY_POLICY_X11_PASS=YES",
                      flush=True)
                # V149: two real original Mojang ItemFishingRod stacks,
                # with only 2 versus 64 remaining source uses.
                send_command(process, log, window, "/clear CIProbe")
                xdotool("windowactivate", "--sync", window)
                xdotool("key", "--clearmodifiers", "9")
                send_command(process, log, window, "/give CIProbe fishing_rod 1 62")
                send_command(process, log, window, "/give CIProbe fishing_rod 1 0")
                await_marker(process, log, NATIVE_ROD_SAFE_FALLBACK, deadline,
                             "real original worn fishing rod skipped for later source full rod")
                await_marker(process, log, NATIVE_ROD_FIXED_RANK, deadline,
                             "protected original fixed-slot rod veto and V135 durable priority")
                print("CUSTOMMC_OFFICIAL_189_REAL_ROD_DURABILITY_PROTECTION_X11_PASS=YES",
                      flush=True)
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
                # V300: the V297 second *real* original V16 failed
                # sustained-ground proof after fire-and-forget /setblock
                # commands. Establish source-acknowledged native support
                # in the actually loaded destination before reseating.
                send_verified_original_chat_reply(
                    process, log, window, "/tp CIProbe 0 120 0 0 0",
                    b"[CHAT] Teleported CIProbe to 0.5, 120.0, 0.5")
                # A sacrificial actual original air reset can be a no-op;
                # the next support STONE command still MUST return a fresh
                # positive changed-block count from the vanilla server.
                send_verified_original_air_preclear(
                    process, log, window, "/fill 0 99 0 0 99 0 air",
                    "original V16 real support air contrast",
                    "missing fresh vanilla V16 support air reset")
                send_verified_fill(process, log, window,
                                   "/fill 0 99 0 0 99 0 stone")
                # Also establish genuine source AIR at the adjacent target.
                # The old unchecked setblock could silently leave stone
                # or an unloaded state; the original client-native AIR
                # witness must still independently confirm the World.
                send_verified_original_air_preclear(
                    process, log, window, "/fill 1 99 0 1 99 0 air",
                    "original V16 genuine adjacent AIR setup",
                    "missing fresh vanilla V16 adjacent air setup")
                send_verified_original_chat_reply(
                    process, log, window, "/tp CIProbe 0 100 0 0 0",
                    b"[CHAT] Teleported CIProbe to 0.5, 100.0, 0.5")
                # V301: source log boundary strictly AFTER genuine original
                # low TP acknowledgment, not an arbitrary old world-tick
                # sample still present in read_tail(log).
                v16_post_teleport_native_cursor = (
                    log.stat().st_size if log.exists() else 0)
                send_verified_original_chat_reply(
                    process, log, window,
                    "/testforblock 0 99 0 stone",
                    # V300 authentic original #39754691161 FAILED because
                    # Mojang 1.8.9 actually emits "0,99,0." (no spaces).
                    # Match the observed fresh server reply exactly, with
                    # no generic acceptance of other block coordinates.
                    b"[CHAT] Successfully found the block at 0,99,0.")
                # Same original three-native-position x/y/z predicate,
                # but all three must originate after the final TP reply.
                # A prior singleplayer moment at y100 cannot fabricate V16.
                stable_start = time.monotonic()
                actual = []
                while time.monotonic() - stable_start < 10:
                    actual = native_safe_walk_positions(
                        log, since=v16_post_teleport_native_cursor)[-5:]
                    if len(actual) >= 3 and all(
                            abs(x - 0.5) < 0.3 and abs(y - 100.0) < 0.2
                            and abs(z - 0.5) < 0.3 for x, y, z in actual[-3:]):
                        break
                    require_running(process, "real native Scaffold fixture grounding")
                    time.sleep(0.15)
                else:
                    print("CUSTOMMC_189_V16_FRESH_NATIVE_GROUND_REJECTION"
                          + " afterTeleportCursor="
                          + str(v16_post_teleport_native_cursor)
                          + " freshNativeSamples=" + str(len(actual))
                          + " lastNativePositions=" + repr(actual[-5:]),
                          file=sys.stderr)
                    raise AssertionError("vanilla player lacks sustained stone support at (0,99,0)")
                print("CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_SUPPORT_GROUNDED=YES",
                      flush=True)
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_AIR_PASS=YES",
                             deadline, "actual original World AIR at adjacent (1,99,0)")
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_UNLOADED_CHUNK_VETO_PASS=YES",
                             deadline, "source native remote unloaded chunk rejects false AIR")
                # Preserve distinct genuine positive changed-server-block
                # receipts, not just a command sent or a stale client ray.
                send_verified_fill(process, log, window,
                                   "/fill 1 99 0 1 99 0 stone")
                await_marker(process, log,
                             b"CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_SOLID_PASS=YES",
                             deadline, "genuine vanilla stone at adjacent (1,99,0)")
                send_verified_fill(process, log, window,
                                   "/fill 1 99 0 1 99 0 air")
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
                # V158: earlier V155 unchanged-source original run failed with
                # source position x=-0.4096 instead of expected x=0.5 after a
                # fire-and-forget /setblock and /tp at the chunk border. Build
                # a real, acknowledged hollow stone chamber first, and place
                # the original source player safely at its center. Keep ALL
                # post-ItemBlock original runTick/controller evidence strict.
                resume_if_paused(process, log, window)
                send_command(process, log, window, "/time set 1000")
                prepare_native_scaffold_context_chamber(process, log, window)
                stable_start = time.monotonic()
                while time.monotonic() - stable_start < 12:
                    readings = native_safe_walk_positions(log)[-5:]
                    if native_scaffold_context_grounded(readings):
                        break
                    require_running(process, "original context source grounding")
                    time.sleep(.15)
                else:
                    raise AssertionError(
                        "native original Scaffold context player not grounded "
                        "at confirmed source-chunk chamber center")
                tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_TICK_CONTEXT_PASS=YES"
                eye_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_PLAYER_EYE_PASS=YES"
                reach_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CONTROLLER_REACH_PASS=YES"
                if any(marker in read_tail(log) for marker in (tag, eye_tag, reach_tag)):
                    raise AssertionError("original context/eye/reach claimed before vanilla ItemBlock")
                send_command(process, log, window, "/give CIProbe stone 8")
                await_marker(process, log, eye_tag, deadline,
                             "source-native player dynamic eye method on original runTick")
                await_marker(process, log, reach_tag, deadline,
                             "source-native controller conservative reach in original runTick")
                await_marker(process, log, tag, deadline,
                             "real Minecraft tick has original player/World/cursor/ItemBlock")
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_SOURCE_TICK_CONTEXT_PASS=YES",
                      flush=True)
            if native_scaffold_placement:
                # V29: actual integrated-server vanilla survival block placement.
                # The WorldClient+stack receipt is NOT yet an independent
                # dedicated-server persistence certification.
                resume_if_paused(process, log, window)
                send_command(process, log, window, "/time set 1000")
                send_command(process, log, window, "/tp 0.5 120 0.5 0 0")
                time.sleep(2.0)
                # V171: old original V29 fire-and-forget /setblock did
                # not prove the actual support and air target existed.
                # This uses two fresh POSITIVE original vanilla /fill
                # World changes, after a strictly acknowledged air reset.
                prepare_verified_original_scaffold_survival_edge(
                    process, log, window)
                # Require FRESH vanilla integrated-server source replies.
                # Teleport last, AFTER changing game mode, to minimize
                # player drift before the strict original native ticks.
                send_verified_original_chat_reply(
                    process, log, window, "/gamemode 0",
                    b"[CHAT] Your game mode has been updated")
                send_verified_original_chat_reply(
                    process, log, window,
                    "/tp CIProbe 1.05 100 0.5 90 88.6",
                    b"[CHAT] Teleported CIProbe to 1.05, 100.0, 0.5")
                # Command replies never substitute for player source ticks.
                await_original_v29_survival_edge_grounding(process, log, window)
                module_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_MODULE_OWNER_PASS=YES"
                attempt_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CONTROLLER_ATTEMPT=YES"
                readback_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CLIENT_SURVIVAL_READBACK_PASS=YES"
                disabled_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_DISABLED_SURVIVAL_WORLD_AIR_STACK_UNCHANGED_PASS=YES"
                if any(tag in read_tail(log) for tag in (module_tag, attempt_tag, readback_tag, disabled_tag)):
                    raise AssertionError("native placement witness appeared before server ItemBlock grant")
                # V295: original V293 second native V29 had a legitimate
                # candidate and clear screen but selected source stack=1.
                # Clear prior items, reselect vanilla hotbar slot 0 and
                # require genuine original eight-stone server grant before
                # the UNCHANGED native selected-eight/ModuleController gate.
                prepare_verified_original_v29_stone_stack(
                    process, log, window)
                if native_scaffold_placement == "enabled":
                    await_marker(process, log, module_tag, deadline,
                                 "real enabled Scaffold module admitted vanilla click")
                    await_marker(process, log, attempt_tag, deadline,
                                 "vanilla onPlayerRightClick admitted by original source gate")
                    if b"accepted=true" not in read_tail(log):
                        raise AssertionError("vanilla native onPlayerRightClick returned false")
                    await_marker(process, log, readback_tag, deadline,
                                 "source WorldClient stone target and one survival stack debit")
                    print("CUSTOMMC_OFFICIAL_189_SURVIVAL_VANILLA_CONTROLLER_CLIENT_READBACK_PASS=YES",
                          flush=True)
                else:
                    await_marker(process, log, disabled_tag, deadline,
                                 "original World air and unchanged eight-stack while disabled")
                    if any(tag in read_tail(log) for tag in (module_tag, attempt_tag, readback_tag)):
                        raise AssertionError("disabled placement entered native controller")
                    print("CUSTOMMC_OFFICIAL_189_DISABLED_SURVIVAL_SCAFFOLD_NEGATIVE_PASS=YES",
                          flush=True)
            if native_scaffold_candidate:
                # Original vanilla 1.8.9 stone support at (0,99,0), air below
                # edge position (1,99,0), original mouse/camera from real game.
                # Player overlaps the stone support by vanilla AABB width;
                # this is NOT a block placement test.
                resume_if_paused(process, log, window)
                send_command(process, log, window, "/time set 1000")
                # V303: prepare a verified original vanilla AIR ray lane
                # above EAST side (x1,y100-102) and unchanged original
                # support (0,99,0). Original V301 second native V26 failed
                # at a different real hit block (1,101,0).
                v26_post_tp_cursor = prepare_verified_original_v26_candidate_source_lane(
                    process, log, window)
                stable_start = time.monotonic()
                while time.monotonic() - stable_start < 12:
                    readings = native_safe_walk_positions(
                        log, since=v26_post_tp_cursor)[-5:]
                    if original_edge_grounding_stable(readings):
                        print("CUSTOMMC_V55_EDGE_NATIVE_STABLE_SOURCE_POSITION=YES "
                              "last={}".format(readings[-1]), flush=True)
                        break
                    require_running(process, "source-native edge candidate grounding")
                    time.sleep(.15)
                else:
                    raise AssertionError("vanilla player not stably grounded on adjacent support")
                candidate_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_NATIVE_CANDIDATE_PASS=YES"
                if candidate_tag in read_tail(log):
                    raise AssertionError("candidate was accepted before any selected original ItemBlock")
                send_command(process, log, window, "/give CIProbe stone 8")
                await_marker(process, log, candidate_tag, deadline,
                             "original runTick cursor/eye/reach/World/item agree on side support")
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_GENUINE_SUPPORTED_AIR_TARGET_PASS=YES",
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
                # V172: the old /setblock had no actual server-changed
                # confirmation; a stable original player position alone
                # does not prove the UP-face target exists in World.
                prepare_verified_original_scaffold_raytrace_support(
                    process, log, window)
                send_command(process, log, window, "/tp 0.5 100 0.5 0 0")
                # Added World evidence is STRICTLY upstream of both
                # unchanged true native raytrace + wrong-face checkpoints.
                # It must never act as their success signal.
                verify_original_v19_stone_after_reseat(process, log, window)
                # V302: source proof that ALL three traversed above-stone
                # cells are still AIR AFTER the actual low original TP.
                # This is separate vanilla World evidence, never the true
                # original up-face native World.rayTraceBlocks acceptance.
                verify_original_v19_air_column_after_reseat(
                    process, log, window)
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
                if attack:
                    # V152: the SAME unchanged V150 commit passed genuine
                    # M425 vanilla melee in full run #1, but its identical
                    # second run missed native raycast before any legal hit.
                    # One reproducible cause to isolate is /fill relative to
                    # a random spawn/chunk or falling source mob. Construct
                    # a real closed, in-chunk room BEFORE summoning or X11
                    # clicking, with independently acknowledged commands.
                    # Do not infer Pig ray-hit or attack success from these
                    # acknowledgments: the existing ORIGINAL native armed,
                    # hurt and UUID-ownership markers remain mandatory.
                    # Load source chunk (0,0) while the player is safely
                    # ABOVE the future room. First construct a genuine small
                    # stone support at y=119. Then teleport and keep source
                    # player standing on it while building the sealed room
                    # y=99..104 below; no freefall or temporary suffocation.
                    send_verified_original_chat_reply(
                        process, log, window,
                        "/tp CIProbe 8 200 8",
                        b"[CHAT] Teleported CIProbe to 8.5, 200.0, 8.5")
                    send_verified_fill(process, log, window,
                                       "/fill 8 119 8 8 119 8 stone")
                    send_verified_original_chat_reply(
                        process, log, window,
                        "/tp CIProbe 8 120 8",
                        b"[CHAT] Teleported CIProbe to 8.5, 120.0, 8.5")
                    time.sleep(1.0)  # source chunk and support are loaded
                    send_verified_fill(process, log, window,
                                       "/fill 4 99 4 12 104 13 stone")
                    send_verified_fill(process, log, window,
                                       "/fill 5 100 5 11 103 12 air")
                    send_verified_original_chat_reply(
                        process, log, window,
                        "/tp CIProbe 8 100 8 0 26",
                        b"[CHAT] Teleported CIProbe to 8.5, 100.0, 8.5")
                    send_verified_original_chat_reply(
                        process, log, window,
                        "/summon Pig 8.5 100 10.5 {NoAI:1b}",
                        b"[CHAT] Object successfully summoned")
                    print("CUSTOMMC_189_MANUAL_MELEE_FIXED_NATIVE_ROOM_PASS=YES",
                          flush=True)
                elif kill_aura_nonplayer:
                    # V289: the same actually verified in-chunk original
                    # vanilla chamber as successful manual melee. Previously
                    # this scenario relied on unacknowledged relative
                    # /fill, /tp and /summon around a random spawn. A real
                    # summon acknowledgment alone never proves a native
                    # crosshair raycast or any module veto.
                    # Load chunk (0,0), establish genuine overhead support,
                    # then build a closed chamber BELOW the grounded host.
                    send_verified_original_chat_reply(
                        process, log, window,
                        "/tp CIProbe 8 200 8",
                        b"[CHAT] Teleported CIProbe to 8.5, 200.0, 8.5")
                    send_verified_fill(process, log, window,
                                       "/fill 8 119 8 8 119 8 stone")
                    send_verified_original_chat_reply(
                        process, log, window,
                        "/tp CIProbe 8 120 8",
                        b"[CHAT] Teleported CIProbe to 8.5, 120.0, 8.5")
                    time.sleep(1.0)
                    send_verified_fill(process, log, window,
                                       "/fill 4 99 4 12 104 13 stone")
                    send_verified_fill(process, log, window,
                                       "/fill 5 100 5 11 103 12 air")
                    send_verified_original_chat_reply(
                        process, log, window,
                        "/tp CIProbe 8 100 8 0 26",
                        b"[CHAT] Teleported CIProbe to 8.5, 100.0, 8.5")
                    send_verified_original_chat_reply(
                        process, log, window,
                        "/summon Pig 8.5 100 10.5 {NoAI:1b}",
                        b"[CHAT] Object successfully summoned")
                    print(
                        "CUSTOMMC_189_NONPLAYER_NATIVE_CHAMBER_SOURCE_READY=YES",
                        flush=True)
                else:
                    # The separate AutoClicker scenario keeps its prior
                    # geometry, original source callbacks and hit gates.
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
            if native_scaffold_placement:
                lines = [line for line in log.read_bytes().splitlines()
                         if b"SCAFFOLD_MODULE_OWNER" in line
                         or b"SCAFFOLD_ORIGINAL_CONTROLLER_ATTEMPT" in line
                         or b"SCAFFOLD_ORIGINAL_CLIENT_SURVIVAL_READBACK" in line
                         or b"SCAFFOLD_DISABLED_SURVIVAL" in line
                         or b"SCAFFOLD_ORIGINAL_TICK_CONTEXT" in line
                         # Genuine source data already emitted by the original
                         # client; this is only a bounded failure-log extract.
                         or b"SCAFFOLD_ARM_WAIT" in line
                         or b"ORIGINAL_SCAFFOLD_V29_REFUSAL_DIAGNOSTIC" in line]
                print("V29_ORIGINAL_PLACEMENT_DIAGNOSTICS_START", file=sys.stderr)
                for line in lines[-45:]:
                    print(line.decode("utf-8","replace"), file=sys.stderr)
                print("V29_ORIGINAL_PLACEMENT_DIAGNOSTICS_END", file=sys.stderr)
            if native_scaffold_candidate:
                tagged = [line for line in log.read_bytes().splitlines()
                          if b"CUSTOMMC_V26_SOURCE_REJECTION=" in line
                          or b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_NATIVE_CANDIDATE_PASS" in line
                          or b"CHAT" in line and b"permission" in line]
                print("V26_NATIVE_REJECTION_DIAGNOSTICS_START", file=sys.stderr)
                for line in tagged[-24:]:
                    print(line.decode("utf-8", "replace"), file=sys.stderr)
                print("V26_NATIVE_REJECTION_DIAGNOSTICS_END", file=sys.stderr)
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
            # V313: the first frames of the original server crash report
            # identify the actual Throwable. The existing 12KB stdout tail
            # can be entirely recursive StackOverflowError frames.
            # Failure-only, bounded; neither log nor report grants a PASS.
            # V314 fixes a real V313 exception-handler scope defect:
            # run() has 'game', not 'args.game'. It also rejects crash
            # files predating this original process, including leftovers
            # from another original singleplayer acceptance stage.
            try:
                v313_report = v313_original_crash_report_header(
                    game, started_after_ns=(
                        v314_native_process_started_ns
                        if process is not None else time.time_ns()))
            except (OSError, ValueError) as diagnostic_error:
                # Diagnostic extraction must NEVER mask the real underlying
                # original-game failure or synthesize a PASS.
                print("V314_ORIGINAL_MOJANG_CRASH_DIAG_UNAVAILABLE "
                      + type(diagnostic_error).__name__, file=sys.stderr)
            else:
                if v313_report is not None:
                    print("V313_ORIGINAL_MOJANG_CRASH_HEADER_START", file=sys.stderr)
                    print(v313_report, file=sys.stderr)
                    print("V313_ORIGINAL_MOJANG_CRASH_HEADER_END", file=sys.stderr)
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
    parser.add_argument("--visual-capture-dir", default=None,
                        help="opt-in genuine X11 PNG evidence of world/Modules/Settings")
    parser.add_argument("--visual-capture-details", action="store_true",
                        help="physically right-click original module, capture Details, then use real back-link")
    parser.add_argument("--attack", action="store_true",
                        help="create offline creative world and verify genuine melee hurt")
    parser.add_argument("--timer-speed", action="store_true",
                        help="prove actual game Timer disabled 1x, 1.5x on, restored 1x")
    parser.add_argument("--native-step-clickgui", action="store_true",
                        help="prove actual Mojang player stepHeight across real X11 Right Shift CustomMC GUI open/close")
    parser.add_argument("--native-noclip-clickgui", action="store_true",
                        help="prove native Mojang player noClip collision lease across physical X11 Right Shift GUI focus")
    parser.add_argument("--native-reach-clickgui", action="store_true",
                        help="prove original Mojang renderer Reach ray operand and physical X11 ClickGUI focus")
    parser.add_argument("--native-combatslot-clickgui", action="store_true",
                        help="prove original Mojang InventoryPlayer.currentItem CombatSlot lease, settings edit and real X11 ClickGUI focus")
    parser.add_argument("--native-rod-potion-discovery", action="store_true",
                        help="prove opt-in source Rod/Potion auto-selection on genuine Mojang /give ItemStacks and physical X11 GUI veto")
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
    parser.add_argument("--native-scaffold-candidate", action="store_true",
                        help="genuine original loaded air target, side support, cursor/eye/reach/item proof")
    parser.add_argument("--native-scaffold-placement", choices=("enabled","disabled"),
                        help="isolated genuine survival onPlayerRightClick and disabled readback")
    args = parser.parse_args()
    try:
        command = build_command(
            args.java, args.client, args.libraries, args.assets,
            args.natives, args.overlay, args.game
        )
        run(command, args.game, args.timeout, attack=args.attack,
            auto_clicker=args.auto_clicker,
            kill_aura_nonplayer=args.kill_aura_nonplayer,
            timer_speed=args.timer_speed,
            native_step_clickgui=args.native_step_clickgui,
            native_noclip_clickgui=args.native_noclip_clickgui,
            native_reach_clickgui=args.native_reach_clickgui,
            native_combatslot_clickgui=args.native_combatslot_clickgui,
            native_rod_potion_discovery=args.native_rod_potion_discovery,
            visual_capture_dir=args.visual_capture_dir,
            visual_capture_details=args.visual_capture_details,
            native_world_camera=args.native_world_camera,
            native_safe_walk=args.native_safe_walk,
            safe_walk_edge=args.safe_walk_edge,
            native_scaffold_world=args.native_scaffold_world,
            native_scaffold_tick=args.native_scaffold_tick,
            native_scaffold_raytrace=args.native_scaffold_raytrace,
            native_scaffold_context=args.native_scaffold_context,
            native_scaffold_candidate=args.native_scaffold_candidate,
            native_scaffold_placement=args.native_scaffold_placement)
    except Exception as failure:
        print("CUSTOMMC_189_SINGLEPLAYER_WORLD_FAILED: " + str(failure),
              file=sys.stderr)
        sys.exit(1)
