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


def run(command, game, timeout, attack=False, auto_clicker=False,
        kill_aura_nonplayer=False, timer_speed=False, native_world_camera=False,
        native_safe_walk=False, safe_walk_edge=None, native_scaffold_world=False,
        native_scaffold_tick=False, native_scaffold_raytrace=False,
        native_scaffold_context=False, native_scaffold_candidate=False,
        native_scaffold_placement=None, native_step_clickgui=False,
        native_noclip_clickgui=False, native_reach_clickgui=False,
        native_combatslot_clickgui=False, native_rod_potion_discovery=False):
    if sum(bool(x) for x in (attack, auto_clicker, kill_aura_nonplayer,
                             timer_speed, native_step_clickgui,
                             native_noclip_clickgui,
                             native_reach_clickgui,
                             native_combatslot_clickgui,
                             native_rod_potion_discovery)) > 1:
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
    needs_active_window = controlled_combat or timer_speed or native_world_camera or native_safe_walk or bool(safe_walk_edge) or native_scaffold_world or native_scaffold_tick or native_scaffold_raytrace or native_scaffold_context or native_scaffold_candidate or bool(native_scaffold_placement) or native_step_clickgui or native_noclip_clickgui or native_reach_clickgui or native_combatslot_clickgui or native_rod_potion_discovery
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
                # V145: the runner intentionally issues NO physical right
                # click after these chat commands. Only genuine production
                # AutoPot scheduling and Minecraft.rightClickMouse can cast.
                send_command(process, log, window, "/effect CIProbe clear")
                send_command(process, log, window, "/clear CIProbe potion")
                # Both earlier -3..3 AND single-chunk fill attempts
                # failed when the target chunks had not been loaded by the
                # actual integrated server. Move the original player to
                # x=8,z=8 in CREATIVE first to force loading of chunk 0,0;
                # only then request an enclosed source 7x7x7 dry cell.
                # Original server acknowledgements are mandatory.
                send_command(process, log, window, "/gamemode creative CIProbe")
                send_verified_chunk_teleport(process, log, window)
                send_verified_fill(process, log, window,
                                   "/fill 4 79 4 12 87 12 stone")
                send_verified_fill(process, log, window,
                                   "/fill 5 80 5 11 86 11 air")
                print("CUSTOMMC_189_SURVIVAL_DRY_CHAMBER_FILL_PASS=YES",
                      flush=True)
                send_command(process, log, window, "/tp CIProbe 8 80 8")
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
                send_command(process, log, window, "/setblock 0 99 0 stone")
                send_command(process, log, window, "/setblock 1 99 0 air")
                send_command(process, log, window, "/tp 1.05 100 0.5 90 88.6")
                send_command(process, log, window, "/gamemode 0")
                stable_start = time.monotonic()
                while time.monotonic() - stable_start < 12:
                    readings = native_safe_walk_positions(log)[-5:]
                    if len(readings) >= 3 and all(
                            abs(x - 1.05) < .05 and abs(y - 100.0) < .15
                            and abs(z - .5) < .05 for x, y, z in readings[-3:]):
                        break
                    require_running(process, "source-native survival ledge grounding")
                    time.sleep(.15)
                else:
                    raise AssertionError("original Survival player not grounded on real support")
                module_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_MODULE_OWNER_PASS=YES"
                attempt_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CONTROLLER_ATTEMPT=YES"
                readback_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CLIENT_SURVIVAL_READBACK_PASS=YES"
                disabled_tag = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_DISABLED_SURVIVAL_WORLD_AIR_STACK_UNCHANGED_PASS=YES"
                if any(tag in read_tail(log) for tag in (module_tag, attempt_tag, readback_tag, disabled_tag)):
                    raise AssertionError("native placement witness appeared before server ItemBlock grant")
                send_command(process, log, window, "/give CIProbe stone 8")
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
                send_command(process, log, window, "/tp 0.5 120 0.5 0 0")
                time.sleep(2.0)
                send_command(process, log, window, "/setblock 0 99 0 stone")
                send_command(process, log, window, "/setblock 1 99 0 air")
                # Look steeply down/left toward original support EAST face.
                send_command(process, log, window, "/tp 1.05 100 0.5 90 88.6")
                stable_start = time.monotonic()
                while time.monotonic() - stable_start < 12:
                    readings = native_safe_walk_positions(log)[-5:]
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
            if native_scaffold_placement:
                lines = [line for line in log.read_bytes().splitlines()
                         if b"SCAFFOLD_MODULE_OWNER" in line
                         or b"SCAFFOLD_ORIGINAL_CONTROLLER_ATTEMPT" in line
                         or b"SCAFFOLD_ORIGINAL_CLIENT_SURVIVAL_READBACK" in line
                         or b"SCAFFOLD_DISABLED_SURVIVAL" in line
                         or b"SCAFFOLD_ORIGINAL_TICK_CONTEXT" in line]
                print("V29_ORIGINAL_PLACEMENT_DIAGNOSTICS_START", file=sys.stderr)
                for line in lines[-24:]:
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
