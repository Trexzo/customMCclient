#!/usr/bin/env python3
"""Real 1.8.9 dedicated server and TWO real Minecraft clients on loopback only.

Authenticates no accounts, opens no external listener, and uses no fake
EntityPlayer. Default mode verifies real joins and nonlocal player-kind
callbacks; opt-in --pvp requires the genuine Mojang attack controller,
same-player hurt-time and server-side damage-stat increase.
"""
import argparse
import math
from pathlib import Path
import re
import socket
import subprocess
import sys
import time
import os

from RunMojang189GraphicalSmoke import build_command, required_file

DONE = b"Done ("
HOST_JOIN = b"CIHost joined the game"
TARGET_JOIN = b"CITarget joined the game"
REMOTE = b"CUSTOMMC_OFFICIAL_189_REMOTE_PLAYER_KIND_20_TICKS_PASS=YES"
GRAPHICAL = b"CUSTOMMC_OFFICIAL_189_OPENGL_FRAME_PASS=YES"
AURA_DISABLED = b"CUSTOMMC_189_KILLAURA_DISABLED_REAL_PLAYER_TICKS_PASS=20"
AURA_ENABLED = b"CUSTOMMC_189_KILLAURA_PLAYER_MODULE_ENABLE_PASS=YES"
AURA_ATTACK = b"CUSTOMMC_189_KILLAURA_SYNTHETIC_PLAYER_ATTACK_PASS=YES"
CONTROLLER_ATTACK = b"CUSTOMMC_189_MOJANG_PLAYER_CONTROLLER_ATTACK_ENTITY_ENTERED=YES"
AURA_HURT = b"CUSTOMMC_OFFICIAL_189_KILLAURA_PLAYER_HURT_PASS=YES"
UNLOCKED_SCOPE_VETO = b"CUSTOMMC_189_UNLOCKED_AURA_NATIVE_LOCAL_INTENT_REMOTE_ARGUMENT_VETO_PASS=YES"
UNLOCKED_SCOPE_FRESH = b"CUSTOMMC_189_UNLOCKED_AURA_FRESH_NATIVE_REMOTE_DISPATCH_PASS=YES"
REMOTE_PAIR_SCOPE_VETO = b"CUSTOMMC_189_UNLOCKED_TWO_REAL_REMOTE_UUID_MISMATCH_VETO_PASS=YES"
REMOTE_PAIR_SCOPE_FRESH = b"CUSTOMMC_189_UNLOCKED_TWO_REAL_REMOTE_FRESH_A_DISPATCH_PASS=YES"
RANGE_DISABLED = b"CUSTOMMC_189_KILLAURA_RANGE_DISABLED_TICKS_PASS=20"
RANGE_ENABLED = b"CUSTOMMC_189_KILLAURA_RANGE_MODULE_ENABLE_PASS=YES"
RANGE_VETO = b"CUSTOMMC_OFFICIAL_189_KILLAURA_REAL_PLAYER_RANGE_VETO_PASS=YES"
FOV_DISABLED = b"CUSTOMMC_189_KILLAURA_FOV_DISABLED_TARGET_TICKS_PASS=20"
FOV_ENABLED = b"CUSTOMMC_189_KILLAURA_FOV_MODULE_ENABLE_PASS=YES"
FOV_VETO = b"CUSTOMMC_OFFICIAL_189_KILLAURA_REAL_PLAYER_FOV_VETO_PASS=YES"
WALL_DISABLED = b"CUSTOMMC_189_KILLAURA_WALL_DISABLED_OCCLUDED_TICKS_PASS=20"
WALL_ENABLED = b"CUSTOMMC_189_KILLAURA_WALL_MODULE_ENABLE_PASS=YES"
WALL_VETO = b"CUSTOMMC_OFFICIAL_189_KILLAURA_REAL_PLAYER_WALL_VETO_PASS=YES"
WALL_RECOVERY_VISIBLE = b"CUSTOMMC_189_WALL_RECOVERY_NATIVE_VISIBLE_SELECTED_PASS=YES"
WALL_RECOVERY_CLICK = b"CUSTOMMC_189_WALL_RECOVERY_OWNED_VISIBLE_ATTACK_PASS=YES"
WALL_RECOVERY_CONTROLLER = b"CUSTOMMC_189_WALL_RECOVERY_NATIVE_CONTROLLER_ATTACK_PASS=YES"
WALL_LIFECYCLE_DISABLED = b"CUSTOMMC_189_WALL_LIFECYCLE_NORMAL_DISABLE_PASS=YES"
WALL_LIFECYCLE_SELECTED = b"CUSTOMMC_189_WALL_LIFECYCLE_OCCLUDED_SELECT_WHEN_OFF_PASS=20"
WALL_LIFECYCLE_REENABLED = b"CUSTOMMC_189_WALL_LIFECYCLE_NORMAL_REENABLE_PASS=YES"
WALL_LIFECYCLE_VETO = b"CUSTOMMC_OFFICIAL_189_WALL_LIFECYCLE_STALE_TARGET_CLEARED_PASS=YES"
LOCK_READY = b"CUSTOMMC_189_LOCK_TWO_ELIGIBLE_INITIAL_A_PASS=20"
LOCK_HIT_A = b"CUSTOMMC_189_LOCK_A_NATIVE_UUID_OWNED_ATTACK_PASS=YES"
LOCK_ARMED = b"CUSTOMMC_189_LOCK_A_NATIVE_UUID_ARMED_PASS=10"
LOCK_RETAINED = b"CUSTOMMC_189_LOCK_A_RETAINED_WITH_CLOSER_B_PASS=15"
LOCK_RELEASE = b"CUSTOMMC_189_LOCK_A_INELIGIBLE_RELEASES_TO_B_PASS=YES"
LOCK_HIT_B = b"CUSTOMMC_189_LOCK_B_NATIVE_UUID_OWNED_ATTACK_PASS=YES"
REJOIN_READY = b"CUSTOMMC_189_REJOIN_ORIGINAL_UUID_LOCK_ARMED=10"
REJOIN_B_LOCK = b"CUSTOMMC_189_REJOIN_OLD_A_UNLOADED_B_LOCKED_PASS=15"
REJOIN_B_HIT = b"CUSTOMMC_189_REJOIN_B_NATIVE_OWNED_ATTACK_PASS=YES"
REJOIN_UUID = b"CUSTOMMC_189_REJOIN_ORIGINAL_UUID_REVALIDATED_PASS=YES"
REJOIN_B_RETAINED = b"CUSTOMMC_189_REJOIN_STICKY_B_RETAINED_PASS=20"
REJOIN_A_RELOCK = b"CUSTOMMC_189_REJOIN_SURVIVOR_B_UNLOADED_A_RELOCKED_PASS=15"
REJOIN_A_HIT = b"CUSTOMMC_189_REJOIN_FRESH_A_NATIVE_OWNED_ATTACK_PASS=YES"
REJOIN_REAL_INDEX_REORDER = b"CUSTOMMC_OFFICIAL_189_REJOIN_ACTUAL_INDEX_REORDER_PASS=YES"
REJOIN_WRONG_UUID_VETO = b"CUSTOMMC_189_REJOIN_WRONG_IN_RANGE_UUID_PROPOSAL_VETO_PASS=YES"
REJOIN_REAL_CONTROLLER_B = b"CUSTOMMC_189_REJOIN_REAL_B_NATIVE_CONTROLLER_UUID_PASS=YES"
REJOIN_CONTROLLER_ARGUMENT_UUID_B = b"CUSTOMMC_189_REJOIN_NATIVE_CONTROLLER_ENTITY_UUID_B_PASS=YES"
REJOIN_STALE_INTENT = b"CUSTOMMC_189_REAL_B_PRE_EXIT_INTENT_CAPTURED_PASS=YES"
REJOIN_STALE_DISPATCH_VETO = b"CUSTOMMC_189_REAL_B_EXIT_STALE_CONTROLLER_DISPATCH_VETO_PASS=YES"
REJOIN_FRESH_A_DISPATCH = b"CUSTOMMC_189_REAL_A_FRESH_CONTROLLER_DISPATCH_PASS=YES"
REJOIN_SCOPED_VETO = b"CUSTOMMC_189_SCOPED_NATIVE_A_INTENT_REAL_B_DISPATCH_VETO_PASS=YES"
REJOIN_SCOPED_FRESH = b"CUSTOMMC_189_SCOPED_NATIVE_FRESH_B_DISPATCH_PASS=YES"
MULTI_READY = b"CUSTOMMC_189_MULTI_TWO_REAL_UUID_PLAYERS_PASS=YES"
MULTI_HIT_A = b"CUSTOMMC_189_MULTI_UUID_A_OWNED_ATTACK_PASS=YES"
MULTI_SWITCH = b"CUSTOMMC_189_MULTI_TARGET_SWITCH_TO_UUID_B_PASS=YES"
MULTI_HIT_B = b"CUSTOMMC_189_MULTI_UUID_B_OWNED_ATTACK_PASS=YES"
SIM_READY = b"CUSTOMMC_189_SIMULTANEOUS_TWO_NATIVE_ELIGIBLE_PLAYERS_PASS=20"
SIM_HIT_A = b"CUSTOMMC_189_SIMULTANEOUS_UUID_A_OWNED_ATTACK_PASS=YES"
SIM_SWITCH = b"CUSTOMMC_189_SIMULTANEOUS_NATIVE_UUID_B_PRIORITY_SWITCH_PASS=YES"
SIM_HIT_B = b"CUSTOMMC_189_SIMULTANEOUS_UUID_B_OWNED_ATTACK_PASS=YES"


def local_port():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as listener:
        listener.bind(("127.0.0.1", 0))
        return listener.getsockname()[1]


def log_tail(path, length=100000):
    if not path.exists():
        return b""
    with path.open("rb") as handle:
        handle.seek(max(0, path.stat().st_size - length))
        return handle.read()


def check_process(proc, phase):
    code = proc.poll()
    if code is not None:
        raise AssertionError(phase + " process exited early: " + str(code))


def wait_for(proc, log, phrase, deadline, phase):
    while time.monotonic() < deadline:
        check_process(proc, phase)
        if phrase in log_tail(log):
            return
        time.sleep(0.3)
    raise AssertionError("missing " + phase + " marker within deadline")


def actual_server_spawn_fixture(server_log):
    """Place both real players within one generated spawn chunk.

    Dedicated-server 1.8.9 initial spawn varies per generated FLAT world.
    Sending both clients to origin (0,0) detached the target from the
    observed host-world entity inventory in consecutive genuine CI runs.
    Instead use the exact server-recorded CIHost login position, keeping
    a four-block separation inside the already-loaded spawn chunk.
    """
    data = log_tail(server_log, 250000)
    match = re.search(
        rb"CIHost\[/127\.0\.0\.1:\d+\] logged in with entity id \d+ at "
        rb"\((-?\d+(?:\.\d+)?), (-?\d+(?:\.\d+)?), (-?\d+(?:\.\d+)?)\)",
        data,
    )
    if not match:
        raise AssertionError("actual dedicated-server CIHost spawn unknown")
    import math
    host_x = math.floor(float(match.group(1)))
    host_y = math.floor(float(match.group(2))) + 1
    host_z = math.floor(float(match.group(3)))
    # Both real players stay within the same 16-block Z chunk so the
    # test cannot pass from a stale or unloaded remote player placeholder.
    dz = 4 if host_z % 16 <= 11 else -4
    target_z = host_z + dz
    if host_z // 16 != target_z // 16:
        raise AssertionError("Mojang spawn-based fixture crossed chunk boundary")
    yaw = 0 if dz > 0 else 180
    return (
        "tp CIHost {} {} {} {} 0".format(host_x, host_y, host_z, yaw),
        "tp CITarget {} {} {} {} 0".format(host_x, host_y, target_z,
                                          (yaw + 180) % 360),
    )


def actual_server_spawn_fov_fixture(server_log):
    """Position an actual nonlocal server player 2 blocks behind CIHost."""
    import math
    data = log_tail(server_log, 250000)
    match = re.search(
        rb"CIHost\[/127\.0\.0\.1:\d+\] logged in with entity id \d+ at "
        rb"\((-?\d+(?:\.\d+)?), (-?\d+(?:\.\d+)?), (-?\d+(?:\.\d+)?)\)",
        data,
    )
    if not match:
        raise AssertionError("Mojang dedicated server actual host spawn unknown")
    x = math.floor(float(match.group(1)))
    y = math.floor(float(match.group(2))) + 1
    z = math.floor(float(match.group(3)))
    dz = 2 if z % 16 <= 13 else -2
    if z // 16 != (z + dz) // 16:
        raise AssertionError("FOV fixture crosses real loaded spawn chunk")
    # Host looks away from the real target; never forge camera snapshots.
    away_yaw = 180 if dz > 0 else 0
    return (
        "tp CIHost {} {} {} {} 0".format(x, y, z, away_yaw),
        "tp CITarget {} {} {} {} 0".format(x, y, z + dz, (away_yaw + 180) % 360),
    )



def actual_server_spawn_wall_fixture(server_log):
    """Genuine players are in reach/facing each other with solid server wall."""
    import math
    data = log_tail(server_log, 250000)
    match = re.search(
        rb"CIHost\[/127\.0\.0\.1:\d+\] logged in with entity id \d+ at "
        rb"\((-?\d+(?:\.\d+)?), (-?\d+(?:\.\d+)?), (-?\d+(?:\.\d+)?)\)",
        data,
    )
    if not match:
        raise AssertionError("actual dedicated server wall host spawn unknown")
    x = math.floor(float(match.group(1)))
    y = math.floor(float(match.group(2))) + 1
    z = math.floor(float(match.group(3)))
    dz = 2 if z % 16 <= 13 else -2
    if z // 16 != (z + dz) // 16:
        raise AssertionError("wall fixture crossed genuine spawn chunk")
    sign = 1 if dz > 0 else -1
    yaw = 0 if sign > 0 else 180
    return (
        "tp CIHost {} {} {} {} 0".format(x, y, z, yaw),
        "tp CITarget {} {} {} {} 0".format(x, y, z + dz, (yaw + 180) % 360),
        "fill {} {} {} {} {} {} stone".format(
            x - 1, y, z + sign, x + 1, y + 3, z + sign
        ),
    )



def actual_server_spawn_positive_pvp_fixture(server_log):
    """Real in-range PvP positions without a wall, within loaded spawn chunk."""
    host_tp, target_tp, _unused_wall = actual_server_spawn_wall_fixture(server_log)
    return host_tp, target_tp


def actual_server_clear_wall_command(wall_fill):
    """Reverse ONLY the genuine fixture's server-created stone barrier."""
    parts = wall_fill.split()
    if (len(parts) != 8 or parts[0] != "fill"
            or parts[7] != "stone"):
        raise AssertionError("not a real fixture stone-wall fill command")
    try:
        tuple(int(value) for value in parts[1:7])
    except ValueError as error:
        raise AssertionError("malformed stone wall coordinates") from error
    return " ".join(parts[:7] + ["air"])


def actual_server_three_client_target_switch_fixture(server_log):
    """Actual spawn-chunk A, B: only one player eligible at a time."""
    host, a_near, _stone = actual_server_spawn_wall_fixture(server_log)
    values = a_near.split()
    if len(values) != 7 or values[:2] != ["tp", "CITarget"]:
        raise AssertionError("malformed real spawn target")
    x, y, z = (int(value) for value in values[2:5])
    b_far = "tp CITargetB {} {} {} 180 0".format(x + 5, y, z)
    a_far = "tp CITarget {} {} {} 180 0".format(x + 5, y, z)
    b_near = "tp CITargetB {} {} {} 180 0".format(x, y, z)
    return host, a_near, b_far, a_far, b_near


def actual_server_three_client_simultaneous_fixture(server_log):
    """Both real remote players remain within combat reach before/after swap."""
    host, a_near, _stone = actual_server_spawn_wall_fixture(server_log)
    parts = a_near.split()
    if len(parts) != 7 or parts[:2] != ["tp", "CITarget"]:
        raise AssertionError("native A spawn command malformed")
    x, y, z = (int(v) for v in parts[2:5])
    sign = 1 if x % 16 <= 13 else -1
    # Initial distances: A=2, B=sqrt(8), both in real attack reach.
    # Move ONLY B once to a separate diagonal tile of sqrt(2) distance,
    # making B nearer without overlapping or moving the already-hurt A.
    # Actual native raycast still owns every possible player attack.
    hvals = host.split()
    if len(hvals) != 7 or hvals[:2] != ["tp", "CIHost"]:
        raise AssertionError("native host spawn teleport malformed")
    host_z = int(hvals[4])
    zsign = 1 if z > host_z else -1
    b_outer = "tp CITargetB {} {} {} 180 0".format(x + sign * 2, y, z)
    b_near = "tp CITargetB {} {} {} 180 0".format(
        x + sign, y, host_z + zsign)
    if (x // 16 != (x + sign * 2) // 16
            or host_z // 16 != (host_z + zsign) // 16):
        raise AssertionError("native simultaneous fixture crossed spawn chunk")
    return host, a_near, b_outer, b_near


def actual_server_three_client_rejoin_fixture(server_log):
    """Server-observed real spawn: A initially nearer; B survives disconnect."""
    host, a_near, b_outer, _b_near = (
        actual_server_three_client_simultaneous_fixture(server_log))
    return host, a_near, b_outer


def wait_for_log_occurrences(proc, log, phrase, minimum, deadline, context):
    """Require an additional actual native server event, not a stale first join."""
    if minimum < 1 or not phrase:
        raise AssertionError("invalid native server occurrence requirement")
    while time.monotonic() < deadline:
        check_process(proc, context)
        if log.read_bytes().count(phrase) >= minimum:
            return
        time.sleep(0.3)
    raise AssertionError("missing repeated native event: " + context)


def actual_server_three_client_locked_fixture(server_log):
    """Native M413 simultaneous geometry plus real A range-expiration command."""
    host, a_near, b_outer, b_near = (
        actual_server_three_client_simultaneous_fixture(server_log))
    coords = a_near.split()
    if len(coords) != 7 or coords[:2] != ["tp", "CITarget"]:
        raise AssertionError("invalid genuine A target teleport")
    x, y, z = (int(v) for v in coords[2:5])
    a_far = "tp CITarget {} {} {} 180 0".format(x + 5, y, z)
    return host, a_near, b_outer, b_near, a_far


def official_offline_uuid(name):
    """Java UUID.nameUUIDFromBytes('OfflinePlayer:' + actual username)."""
    import hashlib
    import uuid
    raw = bytearray(hashlib.md5(("OfflinePlayer:" + name).encode("utf-8")).digest())
    raw[6] = (raw[6] & 0x0F) | 0x30
    raw[8] = (raw[8] & 0x3F) | 0x80
    return str(uuid.UUID(bytes=bytes(raw)))


def wait_for_real_score(server, server_log, player, deadline):
    if player not in ("CITarget", "CITargetB"):
        raise AssertionError("unknown server-owned target")
    while time.monotonic() < deadline:
        try:
            send_server_command(server, server_log,
                "scoreboard players test {} taken 1 999999".format(player),
                expected=b"is in range 1 to 999999", timeout=1.3)
            return
        except AssertionError as exc:
            if "dedicated server unavailable" in str(exc):
                raise
            check_process(server, "server damage for " + player)
    raise AssertionError("server did not record positive damage for " + player)


def write_server_properties(path, port, max_players=2):
    if not (1024 < port < 65536):
        raise ValueError("invalid loopback fixture port")
    # Bind only to localhost; 1.8.9 offline-mode is *never* exposed to a
    # public network or supported as an external multiplayer configuration.
    content = (
        "server-ip=127.0.0.1\n"
        "server-port=" + str(port) + "\n"
        "online-mode=false\n"
        "max-players=" + str(max_players) + "\n"
        "enable-rcon=false\n"
        "enable-query=false\n"
        "white-list=false\n"
        "server-name=CustomMC-localhost-acceptance\n"
        "motd=Local-only verification\n"
        "level-name=acceptance-world\n"
        "level-type=FLAT\n"
        "gamemode=1\n"
        "force-gamemode=true\n"
        "difficulty=0\n"
        "spawn-monsters=false\n"
        "spawn-animals=false\n"
        "pvp=true\n"
        "allow-flight=true\n"
        "spawn-protection=0\n"
        "view-distance=3\n"
        "max-world-size=1000\n"
        "enable-command-block=false\n"
    )
    path.write_text(content, encoding="ascii")


def send_server_command(server, log, command, expected=None, timeout=6):
    """Run a genuine Mojang dedicated-server console command and check reply.

    Only literal, source-controlled commands are passed; no remote RCON,
    scripting plugins or direct health/NBT modification can prove damage.
    """
    if server.poll() is not None or server.stdin is None:
        raise AssertionError("dedicated server unavailable for console command")
    before = log.stat().st_size
    server.stdin.write((command + "\n").encode("ascii"))
    server.stdin.flush()
    if expected is None:
        time.sleep(0.30)
        check_process(server, "dedicated server console " + command)
        return
    stop = time.monotonic() + timeout
    while time.monotonic() < stop:
        check_process(server, "dedicated server console " + command)
        with log.open("rb") as reader:
            reader.seek(before)
            after = reader.read().lower()
        if expected.lower() in after:
            return
        time.sleep(0.15)
    raise AssertionError("server did not confirm console command: " + command
                         + " log=" + after[-800:].decode("ascii", "replace"))


def restore_real_attacker_focus(host, target, deadline):
    """Reclaim real X11 active-window ownership after the second JVM starts.

    Minecraft 1.8.9 suppresses clickMouse while leftClickCounter > 0.
    Window activation can set leftClickCounter=10000, and vanilla clears
    that guard in subsequent focused runTick cycles when attack is released.
    Do not synthesize a manual attack or bypass the vanilla cooldown here.
    """
    window = None
    while time.monotonic() < deadline:
        check_process(host, "CIHost game focus")
        result = subprocess.run(
            ["xdotool", "search", "--onlyvisible", "--pid", str(host.pid),
             "--name", "Minecraft"],
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=4
        )
        if result.returncode == 0 and result.stdout.strip():
            window = result.stdout.decode("ascii", "strict").splitlines()[-1]
            break
        time.sleep(0.3)
    if window is None:
        raise AssertionError("genuine host JVM Minecraft X11 window not found")
    subprocess.check_call(["xdotool", "windowactivate", "--sync", window],
                          timeout=6)
    focused = subprocess.check_output(
        ["xdotool", "getactivewindow"], timeout=4
    ).decode("ascii", "strict").strip()
    if int(focused) != int(window):
        raise AssertionError("host window not active after EWMH activation")
    # Before any server damage baseline is armed and before the players are
    # placed for PvP, physically grab the actual client mouse through X11.
    # This invokes vanilla setIngameFocus; a later released-button tick
    # clears its leftClickCounter using Mojang's own runTick logic.
    subprocess.check_call(["xdotool", "mousemove", "--window", window,
                           "427", "240"], timeout=4)
    subprocess.check_call(["xdotool", "click", "--window", window, "1"],
                          timeout=4)
    print("CUSTOMMC_189_REAL_ATTACKER_PREARM_MOUSE_GRAB=YES", flush=True)
    print("CUSTOMMC_189_REAL_ATTACKER_X11_FOCUS_RESTORED=YES", flush=True)
    time.sleep(2.0)
    check_process(host, "CIHost refocused Minecraft game")
    # Put the target window back in the foreground BEFORE server teleport
    # reorients CIHost. Otherwise the captured host mouse's X11 relative
    # motion can continuously steer the crosshair off its actual target.
    # A real vanilla mouse-release tick has already cleared the focus guard.
    target_window = None
    while time.monotonic() < deadline:
        check_process(target, "CITarget game focus")
        result = subprocess.run(
            ["xdotool", "search", "--onlyvisible", "--pid",
             str(target.pid), "--name", "Minecraft"],
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=4
        )
        if result.returncode == 0 and result.stdout.strip():
            target_window = result.stdout.decode(
                "ascii", "strict").splitlines()[-1]
            break
        time.sleep(0.3)
    if target_window is None:
        raise AssertionError("genuine target Minecraft X11 window not found")
    subprocess.check_call(
        ["xdotool", "windowactivate", "--sync", target_window], timeout=6)
    if int(subprocess.check_output(
            ["xdotool", "getactivewindow"], timeout=4
    ).decode("ascii", "strict").strip()) != int(target_window):
        raise AssertionError("target window not active after handoff")
    print("CUSTOMMC_189_REAL_HOST_PREARM_MOUSE_RELEASE_COMPLETED=YES",
          flush=True)


def client_command(args, game, username, uuid, port, remote):
    if not re.fullmatch(r"[A-Za-z0-9_]{3,16}", username):
        raise ValueError("invalid offline fixture username")
    if not re.fullmatch(r"[a-f0-9]{32}", uuid):
        raise ValueError("invalid offline fixture UUID")
    command = build_command(
        args.java, args.client, args.libraries, args.assets,
        args.natives, args.overlay, game
    )
    command[command.index("--username") + 1] = username
    command[command.index("--uuid") + 1] = uuid
    if remote:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportRemotePlayer=true")
    if remote and args.scaffold_placement:
        for value in (
                "-Dcustommc.acceptance.requireOriginalScaffoldContext=true",
                "-Dcustommc.acceptance.requireOriginalScaffoldEye=true",
                "-Dcustommc.acceptance.requireOriginalScaffoldReach=true",
                "-Dcustommc.acceptance.reportNativeSafeWalkPosition=true"):
            command.insert(command.index("-cp"), value)
        if args.scaffold_placement in ("enabled", "menu-open", "repeat-two", "production-repeat-two", "production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-two-keyboard-walk"):
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireOriginalScaffoldPlacement=true")
            if args.scaffold_placement == "menu-open":
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireOriginalScaffoldMenuVeto=true")
            if args.scaffold_placement == "repeat-two":
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireOriginalScaffoldTwoBlockProof=true")
            if args.scaffold_placement in ("production-repeat-two", "production-repeat-two-keyboard-walk"):
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireProductionScaffoldTwoBlockProof=true")
                if args.scaffold_placement == "production-repeat-two-keyboard-walk":
                    command.insert(command.index("-cp"),
                                   "-Dcustommc.acceptance.requireProductionScaffoldKeyboardWalkProof=true")
            if args.scaffold_placement in ("production-repeat-three", "production-repeat-three-history"):
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireProductionScaffoldThreeBlockProof=true")
                if args.scaffold_placement == "production-repeat-three-history":
                    command.insert(command.index("-cp"),
                                   "-Dcustommc.acceptance.requireProductionScaffoldHistoryVeto=true")
            if args.scaffold_placement == "production-repeat-four":
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireProductionScaffoldFourBlockProof=true")
        else:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireOriginalScaffoldDisabledControl=true")
    if remote and args.esp_visual:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.enableRealRemotePlayerBoxEsp=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.expectedRealRemotePlayerBoxUuid="
                       + official_offline_uuid("CITarget"))
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireRealRemoteBoxPixel=true")
    if remote and args.range_veto:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraRangeVeto=true")
    if remote and args.wall_veto:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraWallVeto=true")
    if remote and args.wall_recovery:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraWallRecovery=true")
    if remote and args.wall_lifecycle:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraWallLifecycle=true")
    if remote and args.fov_veto:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraFovVeto=true")
    if remote and (args.rejoin_target or args.rejoin_release):
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraRejoin=true")
        if args.rejoin_release:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.reportKillAuraRejoinRelease=true")
        if args.require_index_reorder:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireRejoinIndexReorder=true")
        if args.require_wrong_target_veto:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireRejoinWrongTargetVeto=true")
        if args.require_rejoined_controller_b:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireRejoinedControllerB=true")
        if args.require_controller_entity_uuid:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireRejoinedControllerEntityUuid=true")
        if args.require_stale_dispatch_veto:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireStaleDispatchVeto=true")
        if args.require_scoped_aura_owner_veto:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireScopedAuraOwnerVeto=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.targetAUuid=" + official_offline_uuid("CITarget"))
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.targetBUuid=" + official_offline_uuid("CITargetB"))
    if remote and args.locked_target:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraLockedTarget=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.targetAUuid=" + official_offline_uuid("CITarget"))
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.targetBUuid=" + official_offline_uuid("CITargetB"))
    if remote and args.simultaneous:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraSimultaneous=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.targetAUuid=" + official_offline_uuid("CITarget"))
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.targetBUuid=" + official_offline_uuid("CITargetB"))
    if remote and args.multi_target:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraMultiTarget=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.targetAUuid=" + official_offline_uuid("CITarget"))
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.targetBUuid=" + official_offline_uuid("CITargetB"))
    if remote and args.pvp:
        if args.unlocked_scoped_veto:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireUnlockedAuraOwnerVeto=true")
        if args.unlocked_remote_owner_veto:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireRemotePairAuraOwnerVeto=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportVanillaMelee=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraLocalPvp=true")
    command += ["--server", "127.0.0.1", "--port", str(port)]
    # Limit GUI/test process heap so both instances and server fit CI RAM.
    command[command.index("-Xmx1536m")] = "-Xmx1024m"
    return command


def identify_original_host_window_before_target(host):
    """Capture the sole Minecraft X11 window before any second client starts.

    Vanilla LWJGL2 sometimes omits _NET_WM_PID, so xdotool getwindowpid is
    NOT a reliable ownership proof. The fixture owns a fresh Xvfb display,
    starts CIHost first, and must see exactly one visible Minecraft window
    BEFORE launching CITarget. Never pick one among multiple windows.
    """
    check_process(host, "host-only X11 window discovery")
    result = subprocess.run(
        ["xdotool", "search", "--onlyvisible", "--name", "Minecraft"],
        capture_output=True, text=True, check=False,
    )
    windows = list(dict.fromkeys(result.stdout.split()))
    if result.returncode != 0 or len(windows) != 1:
        raise AssertionError(
            "expected exactly one original host Minecraft window before "
            "target launch; found {}".format(len(windows)))
    window = windows[0]
    title = subprocess.run(
        ["xdotool", "getwindowname", window], capture_output=True,
        text=True, check=False)
    if title.returncode != 0 or "Minecraft" not in title.stdout:
        raise AssertionError("host-only X11 window has no Minecraft title")
    print("CUSTOMMC_189_ORIGINAL_HOST_X11_UNIQUE_PRE_TARGET=YES", flush=True)
    return window


def send_real_host_key(host, host_window, key):
    """Send whitelisted vanilla key to previously identified original host."""
    if key not in ("e", "1", "Escape"):
        raise ValueError("invalid original host Minecraft key")
    check_process(host, "host X11 key")
    if not host_window:
        raise AssertionError("original host X11 window was not identified")
    title = subprocess.run(
        ["xdotool", "getwindowname", str(host_window)],
        capture_output=True, text=True, check=False)
    if title.returncode != 0 or "Minecraft" not in title.stdout:
        raise AssertionError("original host X11 window no longer exists")
    subprocess.run(
        ["xdotool", "windowactivate", "--sync", str(host_window)],
        check=True, stdout=subprocess.DEVNULL)
    subprocess.run(
        ["xdotool", "key", "--clearmodifiers", key],
        check=True, stdout=subprocess.DEVNULL)


def source_native_scaffold_position(log, step):
    """Read exact ORIGINAL controller-tick coordinates, never inferred TP state."""
    if step not in (1, 2):
        raise ValueError("unsupported keyboard source step")
    regex = re.compile(
        rb"CUSTOMMC_OFFICIAL_189_SCAFFOLD_KEYBOARD_WALK_ORIGINAL_SOURCE_POSITION"
        rb" step=" + str(step).encode("ascii")
        + rb" x=(-?\d+(?:\.\d+)?) y=(-?\d+(?:\.\d+)?)"
        rb" z=(-?\d+(?:\.\d+)?)")
    observations = regex.findall(log_tail(log, 200000))
    if len(observations) != 1:
        raise AssertionError("missing/duplicate original controller position step {}".format(step))
    values = tuple(float(v) for v in observations[0])
    if not all(math.isfinite(v) for v in values):
        raise AssertionError("nonfinite original controller source position")
    return values


def set_original_host_backward_key(host, host_window, pressed):
    """Actual X11 S key down/up on uniquely identified Mojang host only.

    This sends ONLY a physical key event; it cannot set game position,
    spoof WorldClient state, use server teleport or invoke a click.
    """
    check_process(host, "physical Minecraft backward key")
    if host_window is None:
        raise AssertionError("no original host X11 owner for movement")
    title = subprocess.run(["xdotool", "getwindowname", str(host_window)],
                           capture_output=True, text=True, check=False)
    if title.returncode != 0 or "Minecraft" not in title.stdout:
        raise AssertionError("original host Minecraft X11 window missing")
    if pressed:
        subprocess.run(["xdotool", "windowactivate", "--sync", str(host_window)],
                       check=True, timeout=6, stdout=subprocess.DEVNULL)
        focused = subprocess.check_output(["xdotool", "getactivewindow"],
                                          timeout=4).decode("ascii").strip()
        if int(focused) != int(host_window):
            raise AssertionError("original host window not actually active before movement")
    subprocess.run(["xdotool", "keydown" if pressed else "keyup",
                    "--window", str(host_window), "s"], check=True, timeout=6,
                   stdout=subprocess.DEVNULL)
    print("CUSTOMMC_189_REAL_HOST_S_BACKWARD_{}=YES".format(
          "PRESSED" if pressed else "RELEASED"), flush=True)


def close_process(process):
    if process is None or process.poll() is not None:
        return
    process.terminate()
    try:
        process.wait(timeout=5)
    except subprocess.TimeoutExpired:
        process.kill()
        process.wait(timeout=5)


def run(args):
    if not os.environ.get("DISPLAY"):
        raise ValueError("localhost multiplayer requires actual X11 display")
    if not 75 <= args.timeout <= 240:
        raise ValueError("bounded timeout must be 75..240 seconds")
    server_jar = required_file(args.server)
    java = required_file(args.java)
    root = Path(args.root).resolve()
    if root.exists() or not root.parent.is_dir():
        raise ValueError("refusing existing or invalid fixture workspace")
    root.mkdir()
    server_dir = root / "dedicated"
    server_dir.mkdir()
    (server_dir / "eula.txt").write_text("eula=true\n", encoding="ascii")
    port = local_port()
    write_server_properties(server_dir / "server.properties", port,
                            max_players=3 if (args.multi_target or args.simultaneous or args.locked_target or args.rejoin_target or args.rejoin_release or args.unlocked_remote_owner_veto) else 2)

    server_log = root / "server.log"
    host_log = root / "host.log"
    target_log = root / "target.log"
    target_b_log = root / "target-b.log"
    target_rejoin_log = root / "target-rejoin.log"
    logs = [("dedicated", server_log), ("host", host_log),
            ("target", target_log)]
    if args.multi_target or args.simultaneous or args.locked_target or args.rejoin_target or args.rejoin_release or args.unlocked_remote_owner_veto:
        logs.append(("target_b", target_b_log))
    if args.rejoin_target or args.rejoin_release:
        logs.append(("target_rejoin", target_rejoin_log))
    processes = []
    wm = None
    try:
        env = dict(os.environ)
        env["LIBGL_ALWAYS_SOFTWARE"] = "1"
        env["MESA_GL_VERSION_OVERRIDE"] = "2.1"
        env.pop("JAVA_TOOL_OPTIONS", None)
        env.pop("CUSTOMMC_ACCESS_TOKEN", None)
        wm = subprocess.Popen(
            ["openbox", "--sm-disable"],
            stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
            env=env
        )
        time.sleep(0.5)
        check_process(wm, "X11 Openbox")

        started = time.monotonic()
        deadline = started + args.timeout
        with server_log.open("wb") as output:
            server = subprocess.Popen(
                [str(java), "-Xms128m", "-Xmx896m",
                 "-jar", str(server_jar), "nogui"],
                cwd=str(server_dir), env=env,
                stdin=subprocess.PIPE, stdout=output,
                stderr=subprocess.STDOUT, start_new_session=True
            )
        processes.append(server)
        wait_for(server, server_log, DONE, min(deadline, started + 65),
                 "actual dedicated-server readiness")
        print("CUSTOMMC_OFFICIAL_189_LOCAL_DEDICATED_SERVER_READY=YES",
              flush=True)

        host_cmd = client_command(
            args, root / "host-game", "CIHost",
            "00000000000000000000000000000001", port, remote=True
        )
        target_cmd = client_command(
            args, root / "target-game", "CITarget",
            "00000000000000000000000000000002", port, remote=False
        )
        with host_log.open("wb") as out:
            host = subprocess.Popen(
                host_cmd, env=env, stdout=out,
                stderr=subprocess.STDOUT, start_new_session=True
            )
        processes.append(host)
        wait_for(host, host_log, GRAPHICAL,
                 min(deadline, time.monotonic() + 65),
                 "host graphical client")
        host_window = None
        if args.scaffold_placement in ("enabled", "menu-open", "repeat-two", "production-repeat-two", "production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-two-keyboard-walk"):
            # Unique real Minecraft client on this freshly allocated Xvfb
            # display; later target windows cannot be mistaken for CIHost.
            host_window = identify_original_host_window_before_target(host)
        with target_log.open("wb") as out:
            target = subprocess.Popen(
                target_cmd, env=env, stdout=out,
                stderr=subprocess.STDOUT, start_new_session=True
            )
        processes.append(target)
        target_b = None
        if args.multi_target or args.simultaneous or args.locked_target or args.rejoin_target or args.rejoin_release or args.unlocked_remote_owner_veto:
            cmd_b = client_command(
                args, root / "target-b-game", "CITargetB",
                "00000000000000000000000000000003", port, remote=False)
            with target_b_log.open("wb") as out:
                target_b = subprocess.Popen(cmd_b, env=env, stdout=out,
                    stderr=subprocess.STDOUT, start_new_session=True)
            processes.append(target_b)

        wait_for(server, server_log, HOST_JOIN, deadline,
                 "dedicated server CIHost join")
        wait_for(server, server_log, TARGET_JOIN, deadline,
                 "dedicated server CITarget join")
        if args.multi_target or args.simultaneous or args.locked_target or args.rejoin_target or args.rejoin_release or args.unlocked_remote_owner_veto:
            wait_for(server, server_log, b"CITargetB joined the game", deadline,
                     "real CITargetB dedicated-server join")
            wait_for(target_b, target_b_log, GRAPHICAL, deadline,
                     "third genuine graphical Minecraft client")
        wait_for(target, target_log, GRAPHICAL, deadline,
                 "target graphical client")
        wait_for(host, host_log, REMOTE, deadline,
                 "genuine host world nonlocal EntityPlayer for 20 ticks")

        if args.scaffold_placement:
            # V30: actual dedicated ORIGINAL 1.8.9 server owns both clients,
            # target block and survival inventory. No client-owned fixture
            # can satisfy testforblock or clear-stone readback in server.log.
            host_tp, far_target_tp = actual_server_spawn_fixture(server_log)
            parts = host_tp.split()
            if len(parts) != 7 or parts[:2] != ["tp", "CIHost"]:
                raise AssertionError("unsupported original spawn-derived fixture")
            x, z = int(parts[2]), int(parts[4])
            y = 100
            support = (x, y-1, z)
            target_xyz = (x+1, y-1, z)
            second_target_xyz = (x+2, y-1, z)
            third_target_xyz = (x+3, y-1, z)
            fourth_target_xyz = (x+4, y-1, z)
            send_server_command(server, server_log, far_target_tp)
            send_server_command(server, server_log,
                                "setblock {} {} {} stone".format(*support))
            send_server_command(server, server_log,
                                "setblock {} {} {} air".format(*target_xyz))
            if args.scaffold_placement in ("repeat-two", "production-repeat-two", "production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-two-keyboard-walk"):
                send_server_command(server, server_log,
                                    "setblock {} {} {} air".format(*second_target_xyz))
                if args.scaffold_placement in ("production-repeat-three", "production-repeat-three-history", "production-repeat-four"):
                    send_server_command(server, server_log,
                                        "setblock {} {} {} air".format(*third_target_xyz))
                if args.scaffold_placement == "production-repeat-four":
                    send_server_command(server, server_log,
                                        "setblock {} {} {} air".format(*fourth_target_xyz))
            # Capture a byte-precise diagnostic boundary for menu-negative
            # CI only; older startup OPEN reports are never authority to
            # press Escape in the currently running original Minecraft UI.
            menu_diag_start = (
                host_log.stat().st_size
                if args.scaffold_placement == "menu-open" else None)
            send_server_command(server, server_log, "gamemode 0 CIHost")
            if args.scaffold_placement == "menu-open":
                # V49: the original GUI-negative acceptance must NOT arm in a
                # transient creative/empty startup World. Supply the ACTUAL
                # dedicated-server Survival eight-stone inventory BEFORE
                # measuring 60 stable original GUI-clear+source ticks.
                # This is real keyboard+server authority, not spoofed client
                # inventory/ItemStack state. No placement candidate is set
                # under the host until AFTER the real inventory E veto.
                send_real_host_key(host, host_window, "1")
                print("CUSTOMMC_189_SCAFFOLD_GUI_NEGATIVE_REAL_HOTBAR_ONE_KEY=YES",
                      flush=True)
                send_server_command(server, server_log, "give CIHost stone 8")
                send_server_command(server, server_log,
                                    "clear CIHost stone 0 0",
                                    expected=b"has 8 items that match the criteria")
                print("CUSTOMMC_189_SCAFFOLD_GUI_PREARM_SERVER_EIGHT_PASS=YES",
                      flush=True)
                # A genuinely OPEN vanilla startup/menu GUI cannot satisfy
                # V35's 60 consecutive original GUI-CLEAR ticks. Dismiss a
                # persistent pre-existing original screen with one real Esc
                # before attempting a CI-only module enable. A single
                # transient/stale OPEN diagnostic must NOT trigger Escape:
                # on a CLEAR screen, Escape itself would open pause.
                original_open = (
                    b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ARM_WAIT screen=OPEN")
                armed = (
                    b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_GUI_ARMED_IN_WORLD=YES")
                dismissed_startup_menu = False
                while time.monotonic() < deadline:
                    check_process(host, "original host GUI-clear startup")
                    with host_log.open("rb") as observed:
                        observed.seek(menu_diag_start)
                        fresh_host_log = observed.read()
                    if armed in fresh_host_log:
                        break
                    if (not dismissed_startup_menu
                            and fresh_host_log.count(original_open) >= 2):
                        send_real_host_key(host, host_window, "Escape")
                        dismissed_startup_menu = True
                        print(
                            "CUSTOMMC_189_SCAFFOLD_STARTUP_MENU_DISMISSED_BY_REAL_ESCAPE=YES",
                            flush=True)
                    time.sleep(0.25)
                # Fail with the original host's ACTUAL diagnostics, not only
                # the generic missing-marker timeout. Never fabricate a
                # CLEAR state or send an unverified second Escape.
                if armed not in fresh_host_log:
                    source_lines = [
                        line.decode("utf-8", errors="replace")[-420:]
                        for line in fresh_host_log.splitlines()
                        if (b"SCAFFOLD_ARM_WAIT" in line
                            or b"SCAFFOLD_MENU_PREARM_HEARTBEAT" in line
                            or b"SCAFFOLD_GUI_ARMED_IN_WORLD" in line
                            or b"ACCEPTANCE_SCREEN" in line
                            or b"SCAFFOLD_ORIGINAL_GUI_VETO" in line)
                    ]
                    print("CUSTOMMC_189_GUI_NEGATIVE_PREARM_DIAG"
                          " originalOpenReports={} dismissed={} evidence={}".format(
                              fresh_host_log.count(original_open),
                              dismissed_startup_menu, source_lines[-12:]),
                          flush=True)
                    raise AssertionError(
                        "original in-world GUI-clear arm failed before actual E")
                wait_for(host,host_log,armed,deadline,
                         "stable original GUI-clear World armed before inventory key")
                if b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_GUI_VETO=YES" in log_tail(host_log):
                    raise AssertionError("GUI was vetoed before actual inventory E keypress")
                # Open actual vanilla inventory with E on original host XID.
                # Only THIS new real inventory-open transition may revoke
                # the arm; the server must later prove air and eight items.
                send_real_host_key(host, host_window, "e")
                wait_for(host,host_log,
                         b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_GUI_VETO=YES state=OPEN",
                         deadline,"native original Minecraft GUI cancels armed Scaffold")
                print("CUSTOMMC_189_SCAFFOLD_REAL_INVENTORY_OPEN_VETO_PASS=YES",
                      flush=True)
            send_server_command(server, server_log,
                                "tp CIHost {:.2f} {} {:.2f} 90 88.6".format(
                                    x+1.05, y, z+.5))
            send_server_command(server, server_log,
                                "testforblock {} {} {} air".format(*target_xyz),
                                expected=b"successfully found the block")
            module=b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_MODULE_OWNER_PASS=YES"
            attempt=b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CONTROLLER_ATTEMPT=YES"
            receipt=b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CLIENT_SURVIVAL_READBACK_PASS=YES"
            disabled=b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_DISABLED_SURVIVAL_WORLD_AIR_STACK_UNCHANGED_PASS=YES"
            # CI-only repeat and production opt-in must have DIFFERENT markers.
            # A V37 marker never certifies a V43 production controller call.
            is_production_repeat = args.scaffold_placement in (
                "production-repeat-two", "production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-two-keyboard-walk")
            four_production = args.scaffold_placement == "production-repeat-four"
            three_production = args.scaffold_placement in (
                "production-repeat-three", "production-repeat-three-history") or four_production
            history_negative = args.scaffold_placement == "production-repeat-three-history"
            keyboard_walk = args.scaffold_placement == "production-repeat-two-keyboard-walk"
            repeat_marker = (b"PRODUCTION" if is_production_repeat else b"REPEAT")
            repeat_attempt = (b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_" + repeat_marker
                              + b"_ORIGINAL_CONTROLLER_ATTEMPT=YES")
            repeat_receipt = (b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_" + repeat_marker
                              + b"_CLIENT_READBACK_PASS=YES")
            repeat_arm = (b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_" + repeat_marker
                          + b"_MODULE_ARMED=YES")
            if any(tag in log_tail(host_log)
                   for tag in (module,attempt,receipt,repeat_attempt,repeat_receipt,repeat_arm)):
                raise AssertionError("native placement or repeat arm before accepted placement fixture")
            if args.scaffold_placement in ("enabled", "repeat-two", "production-repeat-two", "production-repeat-two-keyboard-walk"):
                # Genuine host keyboard selects hotbar slot 0. This is not
                # a spoofed inventory setter or a fabricated ItemStack.
                # Server sends the real eight-block Survival grant afterward.
                send_real_host_key(host, host_window, "1")
                print("CUSTOMMC_189_SCAFFOLD_ORIGINAL_HOTBAR_ONE_KEY=YES", flush=True)
            if args.scaffold_placement != "menu-open":
                send_server_command(server, server_log, "give CIHost stone 8")
            # A legitimate enabled client may place the first block the
            # very next original runTick. Requiring a server count of 8
            # after grant in the enabled mode races the intended 8 -> 7
            # survival decrement. Only the disabled control may retain
            # an eight-item post-grant baseline.
            if args.scaffold_placement in ("repeat-two", "production-repeat-two", "production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-two-keyboard-walk"):
                # V37 proves the isolated sequence; V43 independently runs
                # the real default-off setting explicitly enabled for CI.
                # V37 is an isolated opt-in new original-client JVM and
                # dedicated-server fixture: strictly 2 different raytrace
                # targets, one native controller action per original receipt.
                # Reuse the same original host X11 ownership and fail-shut
                # startup menu diagnostics as V35's one-block case.
                menu_blocked = (
                    b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ARM_WAIT"
                    b" screen=OPEN source=positive_candidate_already_available"
                )
                while time.monotonic() < deadline:
                    check_process(host, "original two-block host readiness")
                    live_host = log_tail(host_log)
                    if repeat_arm in live_host:
                        break
                    if menu_blocked in live_host:
                        send_real_host_key(host, host_window, "Escape")
                        print("CUSTOMMC_189_SCAFFOLD_REPEAT_INITIAL_GUI_REAL_ESCAPE=YES",
                              flush=True)
                        break
                    time.sleep(0.25)
                wait_for(host, host_log, repeat_arm, deadline,
                         "V37 original module-owned two-block activation")
                first_attempt = (
                    repeat_attempt + b" step=1 accepted=true target="
                    + "{},{},{}".format(*target_xyz).encode())
                second_attempt = (
                    repeat_attempt + b" step=2 accepted=true target="
                    + "{},{},{}".format(*second_target_xyz).encode())
                wait_for(host, host_log, first_attempt, deadline,
                         "first genuine vanilla controller click at first target")
                wait_for(host, host_log, repeat_receipt + b" step=1", deadline,
                         "source original WorldClient first stone + 8-to-7 debit")
                send_server_command(server, server_log,
                                    "testforblock {} {} {} stone".format(*target_xyz),
                                    expected=b"successfully found the block")
                send_server_command(server, server_log,
                                    "testforblock {} {} {} air".format(*second_target_xyz),
                                    expected=b"successfully found the block")
                send_server_command(server, server_log,
                                    "clear CIHost stone 0 0",
                                    expected=b"has 7 items that match the criteria")
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_"
                      + repeat_marker.decode("ascii")
                      + "_SERVER_FIRST_STONE_SEVEN_PASS=YES",
                      flush=True)

                if keyboard_walk:
                    # V60: EXACTLY ONE initial server tp to the starting
                    # original edge. No server TP after first receipt.
                    # Original human-input S key must move the actual player
                    # over newly placed A until the native cursor sees B.
                    # All other V43/V54/V58/V59 cases retain server TP.
                    first_source = source_native_scaffold_position(host_log, 1)
                    pressed = False
                    try:
                        set_original_host_backward_key(host, host_window, True)
                        pressed = True
                        wait_for(host, host_log, second_attempt, deadline,
                                 "source-native B click after actual held S key")
                        wait_for(host, host_log, repeat_receipt + b" step=2", deadline,
                                 "original B WorldClient receipt after held S")
                    finally:
                        if pressed:
                            set_original_host_backward_key(host, host_window, False)
                    second_source = source_native_scaffold_position(host_log, 2)
                    if not (first_source[0] + .70 <= second_source[0]
                            <= first_source[0] + 2.30
                            and abs(second_source[1] - first_source[1]) <= .35
                            and abs(second_source[2] - first_source[2]) <= .55):
                        raise AssertionError(
                            "original controller source did not advance on physical S"
                            " first={} second={}".format(first_source, second_source))
                    print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_REAL_KEYBOARD_TRAVERSAL_PASS=YES"
                          " nativeXBefore={} nativeXAfter={}".format(
                              first_source[0], second_source[0]), flush=True)
                else:
                    # Legacy dedicated-server fixture moves actual host to
                    # the next edge; the independent keyboard path above
                    # proves original user input can drive real placement.
                    send_server_command(server, server_log,
                                        "tp CIHost {:.2f} {} {:.2f} 90 88.6".format(
                                            x+2.05, y, z+.5))
                    wait_for(host, host_log, second_attempt, deadline,
                             "second genuine vanilla click with NEW target and 7-stack")
                    wait_for(host, host_log, repeat_receipt + b" step=2", deadline,
                             "source original WorldClient second stone + 7-to-6 debit")
                if three_production:
                    # Third adjacent target has no client-side synthetic
                    # source, cursor or inventory: the real dedicated
                    # server moves its actual host player and native ray.
                    for target in (target_xyz, second_target_xyz):
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} stone".format(*target),
                                            expected=b"successfully found the block")
                    send_server_command(server, server_log,
                                        "testforblock {} {} {} air".format(*third_target_xyz),
                                        expected=b"successfully found the block")
                    send_server_command(server, server_log,
                                        "clear CIHost stone 0 0",
                                        expected=b"has 6 items that match the criteria")
                    if history_negative:
                        # Server-authoritative rollback makes previously
                        # placed A genuinely AIR again, with the original
                        # stone support to its west still untouched.
                        # This exercise is a NEGATIVE historical-reuse test:
                        # it may never masquerade as three persistent native
                        # stones; the independent V54 positive retains that.
                        send_server_command(server, server_log,
                                            "setblock {} {} {} air".format(*target_xyz))
                        send_server_command(server, server_log,
                                            "tp CIHost {:.2f} {} {:.2f} 90 88.6".format(
                                                x+1.05, y, z+.5))
                        wait_for(host, host_log,
                                 b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_HISTORY_REUSE_VETO=YES",
                                 deadline, "original first target A readmissible but historical veto")
                        if log_tail(host_log).count(repeat_attempt) != 2:
                            raise AssertionError("revisited source A incorrectly consumed third click")
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*target_xyz),
                                            expected=b"successfully found the block")
                        send_server_command(server, server_log,
                                            "clear CIHost stone 0 0",
                                            expected=b"has 6 items that match the criteria")
                        print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_HISTORY_A_REUSE_NO_CLICK_PASS=YES",
                              flush=True)
                    send_server_command(server, server_log,
                                        "tp CIHost {:.2f} {} {:.2f} 90 88.6".format(
                                            x+3.05, y, z+.5))
                    third_attempt = (repeat_attempt
                        + b" step=3 accepted=true target="
                        + "{},{},{}".format(*third_target_xyz).encode())
                    wait_for(host, host_log, third_attempt, deadline,
                             "third genuine vanilla click at fresh block and stack six")
                    wait_for(host, host_log, repeat_receipt + b" step=3", deadline,
                             "third original WorldClient stone and 6-to-5 receipt")
                    if not four_production:
                        wait_for(host, host_log,
                                 b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_"
                                 b"THREE_CLIENT_RECEIPTS_PASS=YES attempts=3",
                                 deadline, "exactly three native attempts and receipts")
                    expected_attempts = 3
                    expected_targets = ((second_target_xyz, third_target_xyz)
                                        if history_negative else
                                        (target_xyz, second_target_xyz, third_target_xyz))
                    expected_items = b"has 5 items that match the criteria"
                    if four_production:
                        # A NEW original vanilla ray/cursor/controller target;
                        # never reuse a client-side source or fabricate a
                        # stack. The server proves A+B+C and five BEFORE
                        # teleporting the genuine host to fresh D.
                        for target in (target_xyz, second_target_xyz, third_target_xyz):
                            send_server_command(server, server_log,
                                                "testforblock {} {} {} stone".format(*target),
                                                expected=b"successfully found the block")
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*fourth_target_xyz),
                                            expected=b"successfully found the block")
                        send_server_command(server, server_log,
                                            "clear CIHost stone 0 0",
                                            expected=b"has 5 items that match the criteria")
                        send_server_command(server, server_log,
                                            "tp CIHost {:.2f} {} {:.2f} 90 88.6".format(
                                                x+4.05, y, z+.5))
                        fourth_attempt = (repeat_attempt
                            + b" step=4 accepted=true target="
                            + "{},{},{}".format(*fourth_target_xyz).encode())
                        wait_for(host, host_log, fourth_attempt, deadline,
                                 "fourth original controller click at fresh distinct D")
                        wait_for(host, host_log, repeat_receipt + b" step=4", deadline,
                                 "fourth original WorldClient stone and 5-to-4 debit")
                        wait_for(host, host_log,
                                 b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_"
                                 b"FOUR_CLIENT_RECEIPTS_PASS=YES attempts=4",
                                 deadline, "four positive original receipts and native clicks")
                        expected_attempts = 4
                        expected_targets = (
                            target_xyz, second_target_xyz,
                            third_target_xyz, fourth_target_xyz)
                        expected_items = b"has 4 items that match the criteria"
                else:
                    wait_for(host, host_log,
                             b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_" + repeat_marker
                             + b"_TWO_CLIENT_RECEIPTS_PASS=YES attempts=2",
                             deadline, "no extra original controller attempt")
                    expected_attempts = 2
                    expected_targets = (target_xyz, second_target_xyz)
                    expected_items = b"has 6 items that match the criteria"
                if log_tail(host_log).count(repeat_attempt) != expected_attempts:
                    raise AssertionError("repeat fixture issued unexpected original clicks")
                for target in expected_targets:
                    send_server_command(server, server_log,
                                        "testforblock {} {} {} stone".format(*target),
                                        expected=b"successfully found the block")
                send_server_command(server, server_log,
                                    "clear CIHost stone 0 0", expected=expected_items)
                if keyboard_walk:
                    send_server_command(server, server_log,
                                        "testforblock {} {} {} air".format(*third_target_xyz),
                                        expected=b"successfully found the block")
                    print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_REAL_KEYBOARD_"
                          "SERVER_TWO_STONE_SIX_PASS=YES", flush=True)
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_"
                      + repeat_marker.decode("ascii")
                      + ("_SERVER_HISTORY_VETO_TWO_STONE_FIVE_PASS=YES"
                         if history_negative else
                         "_SERVER_FOUR_STONE_FOUR_PASS=YES" if four_production
                         else "_SERVER_THREE_STONE_FIVE_PASS=YES" if three_production
                         else "_SERVER_TWO_STONE_SIX_PASS=YES"),
                      flush=True)
            elif args.scaffold_placement == "enabled":
                # V35's production veto is correct: an OPEN original GUI
                # forbids arming even with a valid ray, World and eight
                # genuine ItemBlocks. Some LWJGL startup screens persist
                # after the remote dedicated-world teleport. Only when
                # original native diagnostics explicitly prove this GUI
                # blocker AND a ready native candidate may CI press a
                # genuine Escape in the uniquely captured host X11 window.
                # Do not send Escape blindly: on a CLEAR screen that would
                # create the pause menu and invalidate legitimate placement.
                menu_blocked = (
                    b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ARM_WAIT"
                    b" screen=OPEN source=positive_candidate_already_available"
                )
                while time.monotonic() < deadline:
                    check_process(host, "enabled Scaffold source GUI readiness")
                    host_data = log_tail(host_log)
                    if module in host_data:
                        break
                    if menu_blocked in host_data:
                        send_real_host_key(host, host_window, "Escape")
                        print(
                            "CUSTOMMC_189_SCAFFOLD_ORIGINAL_GUI_CLOSED_BY_REAL_ESCAPE=YES",
                            flush=True)
                        break
                    time.sleep(0.25)
                wait_for(host,host_log,module,deadline,
                         "source-enabled Scaffold module owns dedicated placement")
                wait_for(host,host_log,attempt,deadline,
                         "real vanilla right click controller on dedicated server")
                wait_for(host,host_log,receipt,deadline,
                         "original client World stone + one survival stack decrement")
                send_server_command(server,server_log,
                                    "testforblock {} {} {} stone".format(*target_xyz),
                                    expected=b"successfully found the block")
                send_server_command(server,server_log,
                                    "clear CIHost stone 0 0",
                                    expected=b"has 7 items that match the criteria")
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_DEDICATED_SERVER_STONE_AND_SEVEN_ITEMS_PASS=YES",
                      flush=True)
            elif args.scaffold_placement == "menu-open":
                if any(tag in log_tail(host_log) for tag in (module,attempt,receipt)):
                    raise AssertionError("menu-vetoed module entered vanilla placement")
                send_server_command(server,server_log,
                                    "testforblock {} {} {} air".format(*target_xyz),
                                    expected=b"successfully found the block")
                send_server_command(server,server_log,
                                    "clear CIHost stone 0 0",
                                    expected=b"has 8 items that match the criteria")
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_DEDICATED_SERVER_MENU_OPEN_AIR_EIGHT_PASS=YES",
                      flush=True)
            else:
                wait_for(host,host_log,disabled,deadline,
                         "disabled current World air and same eight-block original hotbar")
                if any(tag in log_tail(host_log) for tag in (module,attempt,receipt)):
                    raise AssertionError("disabled module reached native vanilla controller")
                send_server_command(server,server_log,
                                    "testforblock {} {} {} air".format(*target_xyz),
                                    expected=b"successfully found the block")
                send_server_command(server,server_log,
                                    "clear CIHost stone 0 0",
                                    expected=b"has 8 items that match the criteria")
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_DEDICATED_SERVER_DISABLED_NEGATIVE_PASS=YES",
                      flush=True)
            return

        if args.esp_visual:
            # Two ORIGINAL native clients on a real dedicated server.
            # Use the actual login chunk and forward native player camera.
            host_tp, target_tp = actual_server_spawn_fixture(server_log)
            send_server_command(server, server_log, "gamemode 1 CITarget")
            send_server_command(server, server_log, "effect CITarget 2 600 5 true")
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, target_tp)
            wait_for(host, host_log,
                     b"CUSTOMMC_OFFICIAL_189_REAL_REMOTE_PLAYER_BOX_HUD_DRAW_PASS=YES uuid="
                     + official_offline_uuid("CITarget").encode("ascii"),
                     deadline, "actual native UUID-matched remote 2D ESP HUD draw")
            print("CUSTOMMC_OFFICIAL_189_REMOTE_PLAYER_NATIVE_BOX_VISUAL_CALLBACK_PASS=YES",
                  flush=True)
            wait_for(host, host_log,
                     b"CUSTOMMC_OFFICIAL_189_REAL_REMOTE_BOX_FRAMEBUFFER_PIXEL_PASS=YES uuid="
                     + official_offline_uuid("CITarget").encode("ascii"),
                     deadline, "actual original OpenGL framebuffer pixel from exact remote AABB")
            print("CUSTOMMC_OFFICIAL_189_REMOTE_PLAYER_NATIVE_BOX_RGB_PIXEL_PASS=YES",
                  flush=True)

        if args.rejoin_target or args.rejoin_release:
            # THREE original Mojang 1.8.9 client JVMs + genuine localhost
            # dedicated server. The old CITarget client is later terminated,
            # then a NEW graphical Minecraft JVM rejoins with the SAME
            # username/offline server UUID. No synthetic entity/packet.
            send_server_command(server, server_log,
                                "scoreboard objectives add taken stat.damageTaken")
            for player in ("CITarget", "CITargetB"):
                send_server_command(server, server_log,
                                    "scoreboard players set {} taken 0".format(player))
                send_server_command(server, server_log,
                                    "scoreboard players test {} taken 0 0".format(player),
                                    expected=b"score 0 is in range 0 to 0")
            send_server_command(server, server_log, "gamemode 0 CIHost")
            send_server_command(server, server_log, "gamemode 1 CITarget")
            # Both targets remain genuine living, selectable native players.
            # Creative B cannot be knocked out of reach during the mandatory
            # 15-callback A-disappeared / B-UUID-lock proof.
            send_server_command(server, server_log, "gamemode 1 CITargetB")
            send_server_command(server, server_log,
                                "gamerule naturalRegeneration false")
            host_tp, a_near, b_outer = (
                actual_server_three_client_rejoin_fixture(server_log))
            # V52: move HOST into the genuine server-owned combat chunk
            # BEFORE moving B. Sending B's teleport first could leave the
            # remote original loadedEntityList with a stale B spawn/position
            # after host's own teleport. The native source witness in V50
            # observed actual A=2.06 but B=5.74 even when dedicated server
            # acknowledged B's intended sqrt(8) teleport.
            #
            # The final server-authoritative teleport is deliberately B,
            # after host and A are tracking the real nearby entity. There
            # are no synthetic positions, client-side entity writes or
            # weakened in-range/UUID/real-controller assertions.
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, a_near)
            send_server_command(server, server_log, b_outer)
            print("CUSTOMMC_189_REJOIN_FINAL_B_SERVER_TELEPORT_AFTER_HOST_A=YES",
                  flush=True)
            wait_for(host, host_log, REJOIN_READY, deadline,
                     "original genuine A UUID locked before disconnect")
            for player in ("CITarget", "CITargetB"):
                send_server_command(server, server_log,
                                    "scoreboard players test {} taken 0 0".format(player),
                                    expected=b"score 0 is in range 0 to 0")
            close_process(target)
            wait_for(server, server_log, b"CITarget left the game", deadline,
                     "original real Minecraft target left dedicated server")
            print("CUSTOMMC_189_REJOIN_REAL_A_DISCONNECTED_PASS=YES", flush=True)
            wait_for(host, host_log, REJOIN_B_LOCK, deadline,
                     "15 genuine native A-absent snapshots retaining locked B")
            for player in ("CITarget", "CITargetB"):
                send_server_command(server, server_log,
                                    "scoreboard players test {} taken 0 0".format(player),
                                    expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_189_REJOIN_B_ONLY_STABLE_NO_DAMAGE_PASS=YES",
                  flush=True)
            # Only AFTER the survivor's native lock and list-index change
            # are established, permit true survival PvP knockback/damage.
            send_server_command(server, server_log, "gamemode 0 CITargetB")
            wait_for(host, host_log, REJOIN_B_HIT, deadline,
                     "genuine surviving UUID B original combat click")
            wait_for_real_score(server, server_log, "CITargetB", deadline)
            send_server_command(server, server_log,
                                "scoreboard players test CITarget taken 0 0",
                                expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_189_REJOIN_B_DAMAGE_A_STILL_ZERO_PASS=YES",
                  flush=True)
            # Suppress further knockback ONLY after positive B server damage.
            # Both players will be CREATIVE during the final lock-retention
            # state proof, and B's genuine original damage score remains.
            send_server_command(server, server_log, "gamemode 1 CITargetB")
            send_server_command(server, server_log, b_outer)
            if args.require_index_reorder:
                # A single vanilla ArmorStand can fill precisely the slot
                # vacated by A, giving the new A an unchanged list index.
                # Require FOUR distinct genuine Mojang server-spawned stands
                # to reach the original host source list before reconnecting
                # A. Neither client arrays nor native list indices are edited.
                # Existing original-A != rejoined-A index proof remains strict.
                parts = a_near.split()
                if len(parts) != 7 or parts[:2] != ["tp", "CITarget"]:
                    raise AssertionError("cannot derive real spawn sentinels")
                ax, ay, az = (int(value) for value in parts[2:5])
                for dx, dz in ((6, 6), (7, 6), (6, 7), (7, 7)):
                    sentinel = "summon ArmorStand {} {} {}".format(
                        ax + dx, ay, az + dz)
                    send_server_command(server, server_log, sentinel,
                                        expected=b"Object successfully summoned")
                print("CUSTOMMC_189_REJOIN_NATIVE_SERVER_SENTINELS_SUMMONED=4",
                      flush=True)
                wait_for(host, host_log,
                         b"CUSTOMMC_189_REJOIN_REAL_SENTINEL_NATIVE_OBSERVED_PASS=YES",
                         deadline,
                         "original host observed new native nonplayer before A rejoin")
            cmd_rejoin = client_command(
                args, root / "target-rejoin-game", "CITarget",
                "00000000000000000000000000000002", port, remote=False)
            with target_rejoin_log.open("wb") as out:
                target = subprocess.Popen(
                    cmd_rejoin, env=env, stdout=out,
                    stderr=subprocess.STDOUT, start_new_session=True)
            processes.append(target)
            wait_for(target, target_rejoin_log, GRAPHICAL, deadline,
                     "brand-new same-UUID genuine Minecraft graphical client")
            wait_for_log_occurrences(
                server, server_log, TARGET_JOIN, 2, deadline,
                "same real CITarget username second dedicated-server join")
            send_server_command(server, server_log, "gamemode 1 CITarget")
            send_server_command(server, server_log, a_near)
            print("CUSTOMMC_189_REJOIN_REAL_A_NEW_JVM_RECONNECTED_PASS=YES",
                  flush=True)
            wait_for(host, host_log, REJOIN_UUID, deadline,
                     "actual same original UUID revalidated after new login")
            wait_for(host, host_log, REJOIN_B_RETAINED, deadline,
                     "20 callbacks current B UUID retained while rejoined A closer")
            if args.require_index_reorder:
                wait_for(host, host_log, REJOIN_REAL_INDEX_REORDER, deadline,
                         "actual live rejoined UUID A changes native entity list index")
            if args.require_wrong_target_veto:
                wait_for(host, host_log, REJOIN_WRONG_UUID_VETO, deadline,
                         "in-range wrong native A UUID proposal rejected while B locked")
            if args.require_stale_dispatch_veto:
                wait_for(host, host_log, REJOIN_STALE_INTENT, deadline,
                         "genuine locked B UUID retained as pre-exit attack intent")
            if args.require_rejoined_controller_b:
                # Prior genuine B positive damage is already certified here.
                # Reset the REAL B scoreboard to zero, make both real remote
                # players vulnerable, then demand a FRESH native controller
                # invocation and NEW authoritative damage only to locked B.
                # Survival A stands on 4 genuine stone blocks (no gravity
                # drift; B remains separately visible and eligible).
                ap=a_near.split()
                if len(ap)!=7 or ap[:2]!=["tp","CITarget"]:
                    raise AssertionError("invalid native A floor coordinate")
                ax, ay, az=(float(v) for v in ap[2:5])
                fx, fz=math.floor(ax), math.floor(az)
                floor="fill {} {} {} {} {} {} stone".format(
                    fx,int(ay)-1,fz,fx+1,int(ay)-1,fz+1)
                send_server_command(server,server_log,floor,
                                    expected=b"4 blocks filled")
                send_server_command(server,server_log,"gamemode 0 CITarget")
                send_server_command(server,server_log,a_near)
                send_server_command(server,server_log,
                                    "scoreboard players set CITargetB taken 0")
                send_server_command(server,server_log,
                                    "scoreboard players test CITargetB taken 0 0",
                                    expected=b"score 0 is in range 0 to 0")
                if args.require_scoped_aura_owner_veto:
                    # B is still creative/vulnerable=no while the genuine
                    # selected+locked native B Aura dispatch must reject
                    # the deliberately old real A UUID owner. Any unrelated
                    # native crosshair mismatch is also safely vetoed.
                    wait_for(host, host_log, REJOIN_SCOPED_VETO, deadline,
                             "scoped production Aura rejects real A intent against actual B argument")
                    send_server_command(server,server_log,
                                        "scoreboard players test CITargetB taken 0 0",
                                        expected=b"score 0 is in range 0 to 0")
                    send_server_command(server,server_log,
                                        "scoreboard players test CITarget taken 0 0",
                                        expected=b"score 0 is in range 0 to 0")
                send_server_command(server,server_log,"gamemode 0 CITargetB")
                wait_for(host,host_log,REJOIN_REAL_CONTROLLER_B,deadline,
                         "original vanilla player controller owns rejoined-phase UUID B")
                if args.require_controller_entity_uuid:
                    wait_for(host,host_log,REJOIN_CONTROLLER_ARGUMENT_UUID_B,deadline,
                             "actual original Mojang attackEntity argument exact B UUID")
                if args.require_scoped_aura_owner_veto:
                    wait_for(host,host_log,REJOIN_SCOPED_FRESH,deadline,
                             "production scoped Aura accepts genuine B after veto")
                wait_for_real_score(server,server_log,"CITargetB",deadline)
                send_server_command(server,server_log,
                                    "scoreboard players test CITarget taken 0 0",
                                    expected=b"score 0 is in range 0 to 0")
                print("CUSTOMMC_189_REJOIN_POSTRECONNECT_B_DAMAGE_A_ZERO_PASS=YES",
                      flush=True)
                # M419 ends at its independently proven real controller B
                # callback + fresh positive B / zero A server damage. The
                # new PvP hit legitimately knocks B out of its former native
                # selection geometry. Do not keep an enabled, phase-4-only
                # test guard running across unrelated post-proof teleports:
                # those can select A and falsely convert a completed
                # isolated proof into a stale-target crash. Independent M418
                # and M416 tests still cover the full reverse-disconnect path.
                check_process(host, "M419 genuine attacker after server damage proof")
                check_process(target, "M419 rejoined real A after damage proof")
                check_process(target_b, "M419 genuine B after server damage proof")
                check_process(server, "M419 dedicated server after damage proof")
                print("CUSTOMMC_OFFICIAL_189_REJOIN_CONTROLLER_B_SERVER_DAMAGE_PASS=YES",
                      flush=True)
                if args.require_controller_entity_uuid:
                    print("CUSTOMMC_OFFICIAL_189_NATIVE_CONTROLLER_ENTITY_UUID_B_SERVER_DAMAGE_PASS=YES",
                          flush=True)
                if args.require_scoped_aura_owner_veto:
                    print("CUSTOMMC_OFFICIAL_189_PRODUCTION_SCOPED_AURA_OWNER_UUID_VETO_AND_FRESH_B_DAMAGE_PASS=YES",
                          flush=True)
                return
            send_server_command(server, server_log,
                                "scoreboard players test CITarget taken 0 0",
                                expected=b"score 0 is in range 0 to 0")
            send_server_command(server, server_log,
                                "scoreboard players test CITargetB taken 1 999999",
                                expected=b"is in range 1 to 999999")
            print("CUSTOMMC_OFFICIAL_189_REJOIN_A_ZERO_B_POSITIVE_DAMAGE_PASS=YES",
                  flush=True)
            if args.rejoin_release and not args.require_rejoined_controller_b:
                # Reverse direction: B's ORIGINAL graphical client disconnects
                # while A remains a genuine, freshly rejoined living player.
                # No local entity list is manipulated by our fixture.
                close_process(target_b)
                wait_for(server, server_log, b"CITargetB left the game",
                         deadline, "real survivor B disconnected")
                print("CUSTOMMC_189_REJOIN_REAL_B_DISCONNECTED_PASS=YES",
                      flush=True)
                wait_for(host, host_log, REJOIN_A_RELOCK, deadline,
                         "15 genuine native B-absent callbacks with A UUID lock")
                send_server_command(server, server_log,
                                    "scoreboard players test CITarget taken 0 0",
                                    expected=b"score 0 is in range 0 to 0")
                # The fresh player A is creative for the identity gate only.
                # Switch to vanilla survival and require REAL server damage.
                if args.require_stale_dispatch_veto:
                    wait_for(host, host_log, REJOIN_STALE_DISPATCH_VETO,
                             deadline, "original attackEntity rejects stale B intent after real exit")
                    send_server_command(server, server_log,
                                        "scoreboard players test CITarget taken 0 0",
                                        expected=b"score 0 is in range 0 to 0")
                send_server_command(server, server_log,
                                    "gamemode 0 CITarget")
                wait_for(host, host_log, REJOIN_A_HIT, deadline,
                         "fresh source-native A UUID owns real Aura click")
                if args.require_stale_dispatch_veto:
                    wait_for(host, host_log, REJOIN_FRESH_A_DISPATCH,
                             deadline, "fresh native controller dispatch owns actual A UUID")
                wait_for_real_score(server, server_log, "CITarget", deadline)
                send_server_command(server, server_log,
                                    "scoreboard players test CITargetB taken 1 999999",
                                    expected=b"is in range 1 to 999999")
                print("CUSTOMMC_OFFICIAL_189_REJOIN_REACQUIRED_A_SERVER_DAMAGE_PASS=YES",
                      flush=True)

        if args.locked_target:
            # Genuine original 1.8.9 dedicated server with 3 Java 8 JVMs.
            # A and B stay eligible while B overtakes A; only then A goes
            # out-of-range. No fake hit, damage or target identity is used.
            send_server_command(server, server_log,
                                "scoreboard objectives add taken stat.damageTaken")
            for player in ("CITarget", "CITargetB"):
                send_server_command(server, server_log,
                                    "scoreboard players set {} taken 0".format(player))
                send_server_command(server, server_log,
                                    "scoreboard players test {} taken 0 0".format(player),
                                    expected=b"score 0 is in range 0 to 0")
            # Creative A remains a genuine live selectable player but
            # receives no PvP damage/knockback during the lock-only phase.
            # After proving retention, restore survival and demand true
            # server-authoritative PvP damage on A.
            send_server_command(server, server_log, "gamemode 0 CIHost")
            send_server_command(server, server_log, "gamemode 1 CITarget")
            send_server_command(server, server_log, "gamemode 0 CITargetB")
            send_server_command(server, server_log,
                                "gamerule naturalRegeneration false")
            host_tp, a_near, b_outer, b_near, a_far = (
                actual_server_three_client_locked_fixture(server_log))
            send_server_command(server, server_log, b_outer)
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, a_near)
            wait_for(host, host_log, LOCK_READY, deadline,
                     "20 disabled callbacks selecting genuine nearer A")
            wait_for(host, host_log, LOCK_ARMED, deadline,
                     "10 real active callbacks locking source-native A UUID")
            # Move ONLY B closer, leave original A at its original tile.
            send_server_command(server, server_log, b_near)
            wait_for(host, host_log, LOCK_RETAINED, deadline,
                     "15 callbacks holding source-native A while B nearer")
            for player in ("CITarget", "CITargetB"):
                send_server_command(server, server_log,
                                    "scoreboard players test {} taken 0 0".format(player),
                                    expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_OFFICIAL_189_LOCK_NEARER_B_STILL_ZERO_DAMAGE_PASS=YES",
                  flush=True)
            # A now enters normal survival, and real attacks MUST produce
            # genuine positive server damage, not merely a creative click.
            send_server_command(server, server_log, "gamemode 0 CITarget")
            wait_for(host, host_log, LOCK_HIT_A, deadline,
                     "original native locked A click after survival")
            wait_for_real_score(server, server_log, "CITarget", deadline)
            send_server_command(server, server_log,
                                "scoreboard players test CITargetB taken 0 0",
                                expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_189_LOCK_A_DAMAGE_B_ZERO_PASS=YES", flush=True)
            # One server teleport makes previous locked A out of Aura range.
            send_server_command(server, server_log, a_far)
            wait_for(host, host_log, LOCK_RELEASE, deadline,
                     "A out-of-range; actual locked UUID and production target become B")
            wait_for(host, host_log, LOCK_HIT_B, deadline,
                     "original native attack on newly eligible UUID B")
            wait_for_real_score(server, server_log, "CITargetB", deadline)
            send_server_command(server, server_log,
                                "scoreboard players test CITarget taken 1 999999",
                                expected=b"is in range 1 to 999999")
            print("CUSTOMMC_OFFICIAL_189_LOCK_RELEASE_BOTH_SERVER_DAMAGE_PASS=YES",
                  flush=True)

        if args.simultaneous:
            # BOTH actual remote EntityPlayers stay inside default 3.5 range.
            # Movement uses the real server, not fabricated entity snapshots.
            send_server_command(server, server_log,
                                "scoreboard objectives add taken stat.damageTaken")
            for player in ("CITarget", "CITargetB"):
                send_server_command(server, server_log,
                                    "scoreboard players set {} taken 0".format(player))
                send_server_command(server, server_log,
                                    "scoreboard players test {} taken 0 0".format(player),
                                    expected=b"score 0 is in range 0 to 0")
            for player in ("CIHost", "CITarget", "CITargetB"):
                send_server_command(server, server_log, "gamemode 0 " + player)
            send_server_command(server, server_log,
                                "gamerule naturalRegeneration false")
            host_tp, a_near, b_outer, b_near = (
                actual_server_three_client_simultaneous_fixture(server_log))
            send_server_command(server, server_log, b_outer)
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, a_near)
            print("CUSTOMMC_189_SIMULTANEOUS_TWO_PLAYERS_IN_REACH_PASS=YES",
                  flush=True)
            wait_for(host, host_log, SIM_READY, deadline,
                     "20 disabled callbacks: both genuine eligible, A nearer")
            wait_for(host, host_log, SIM_HIT_A, deadline,
                     "UUID A native-owned attack among two eligible players")
            wait_for_real_score(server, server_log, "CITarget", deadline)
            send_server_command(server, server_log,
                                "scoreboard players test CITargetB taken 0 0",
                                expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_189_SIMULTANEOUS_A_DAMAGED_B_STILL_ZERO_PASS=YES",
                  flush=True)
            # One server teleport, no movement of already-hurt A: B goes
            # from sqrt(8) to sqrt(2) blocks, at a DIFFERENT diagonal tile.
            # Both live native players are in reach before and after.
            send_server_command(server, server_log, b_near)
            wait_for(host, host_log, SIM_SWITCH, deadline,
                     "genuine native UUID B becomes nearer than living A")
            wait_for(host, host_log, SIM_HIT_B, deadline,
                     "production Aura click is owned by real UUID B")
            wait_for_real_score(server, server_log, "CITargetB", deadline)
            send_server_command(server, server_log,
                                "scoreboard players test CITarget taken 1 999999",
                                expected=b"is in range 1 to 999999")
            print("CUSTOMMC_OFFICIAL_189_SIMULTANEOUS_BOTH_SERVER_DAMAGE_PASS=YES",
                  flush=True)

        if args.multi_target:
            # One genuine server and THREE separate Java 8 Minecraft clients.
            # Exactly two living remotes stay in the real loadedEntityList.
            send_server_command(server, server_log,
                                "scoreboard objectives add taken stat.damageTaken")
            for player in ("CITarget", "CITargetB"):
                send_server_command(server, server_log,
                                    "scoreboard players set {} taken 0".format(player))
                send_server_command(server, server_log,
                                    "scoreboard players test {} taken 0 0".format(player),
                                    expected=b"score 0 is in range 0 to 0")
            for player in ("CIHost", "CITarget", "CITargetB"):
                send_server_command(server, server_log, "gamemode 0 " + player)
            send_server_command(server, server_log,
                                "gamerule naturalRegeneration false")
            host_tp, a_near, b_far, a_far, b_near = (
                actual_server_three_client_target_switch_fixture(server_log))
            send_server_command(server, server_log, b_far)
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, a_near)
            print("CUSTOMMC_189_MULTI_TWO_REAL_PLAYERS_POSITIONED_PASS=YES",
                  flush=True)
            wait_for(host, host_log, MULTI_READY, deadline,
                     "20 native A candidate callbacks, second UUID out of range")
            wait_for(host, host_log, MULTI_HIT_A, deadline,
                     "native A UUID owned Kill Aura attack")
            wait_for_real_score(server, server_log, "CITarget", deadline)
            send_server_command(server, server_log,
                                "scoreboard players test CITargetB taken 0 0",
                                expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_189_MULTI_A_SERVER_DAMAGE_B_ZERO_PASS=YES",
                  flush=True)
            # Same server, unchanged Aura: move A outside 3.5 and put B at
            # A's previous eligible location. No fake packets or GUI clicks.
            send_server_command(server, server_log, a_far)
            send_server_command(server, server_log, b_near)
            wait_for(host, host_log, MULTI_SWITCH, deadline,
                     "production selector switches to native UUID B")
            wait_for(host, host_log, MULTI_HIT_B, deadline,
                     "actual UUID B owned native player attack")
            wait_for_real_score(server, server_log, "CITargetB", deadline)
            send_server_command(server, server_log,
                                "scoreboard players test CITarget taken 1 999999",
                                expected=b"is in range 1 to 999999")
            print("CUSTOMMC_OFFICIAL_189_MULTI_PER_TARGET_SERVER_DAMAGE_PASS=YES",
                  flush=True)

        if args.wall_veto or args.wall_recovery or args.wall_lifecycle:
            # Unmodified Mojang server places a real full-height stone wall.
            # Target is close and IN camera FOV; only native LOS can veto.
            send_server_command(server, server_log,
                                "scoreboard objectives add taken stat.damageTaken")
            send_server_command(server, server_log,
                                "scoreboard players set CITarget taken 0")
            send_server_command(server, server_log,
                                "scoreboard players test CITarget taken 0 0",
                                expected=b"score 0 is in range 0 to 0")
            send_server_command(server, server_log, "gamemode 0 CIHost")
            send_server_command(server, server_log, "gamemode 0 CITarget")
            send_server_command(server, server_log,
                                "gamerule naturalRegeneration false")
            host_tp, target_tp, wall_fill = actual_server_spawn_wall_fixture(
                server_log)
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, target_tp)
            # Genuine Mojang 1.8.9 prints "12 blocks filled" for
            # the 3-wide by 4-tall single-block-thick stone barrier.
            send_server_command(server, server_log, wall_fill,
                                expected=b"12 blocks filled")
            print("CUSTOMMC_189_WALL_ACTUAL_SERVER_STONE_BARRIER_PASS=YES",
                  flush=True)
            wait_for(host, host_log, WALL_DISABLED, deadline,
                     "20 live native-occluded player callbacks before Aura")
            wait_for(host, host_log, WALL_ENABLED, deadline,
                     "normal Wall Check plus Kill Aura activation")
            wait_for(host, host_log, WALL_VETO, deadline,
                     "80 genuine native-occluded Aura veto callbacks")
            send_server_command(server, server_log,
                                "scoreboard players test CITarget taken 0 0",
                                expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_OFFICIAL_189_REAL_PLAYER_WALL_SERVER_NO_DAMAGE_PASS=YES",
                  flush=True)
            if args.wall_lifecycle:
                # The stone wall stays intact while the normal module
                # controller disables and re-enables Wall Check. Production
                # targeting may select occluded player only when OFF;
                # raycast/controller must never actually attack through stone.
                wait_for(host, host_log, WALL_LIFECYCLE_DISABLED, deadline,
                         "Wall Check disabled with active Kill Aura")
                wait_for(host, host_log, WALL_LIFECYCLE_SELECTED, deadline,
                         "20 real hidden-player selections while Wall Check off")
                wait_for(host, host_log, WALL_LIFECYCLE_REENABLED, deadline,
                         "normal Wall Check reenable while Aura stays active")
                wait_for(host, host_log, WALL_LIFECYCLE_VETO, deadline,
                         "30 native hidden callbacks with stale player deselected")
                send_server_command(server, server_log,
                                    "scoreboard players test CITarget taken 0 0",
                                    expected=b"score 0 is in range 0 to 0")
                print("CUSTOMMC_OFFICIAL_189_WALL_LIFECYCLE_SERVER_ZERO_DAMAGE_PASS=YES",
                      flush=True)
            if args.wall_recovery or args.wall_lifecycle:
                # SAME genuine server + clients + unchanged Aura settings.
                # Remove only the barrier using the server's vanilla command.
                # Never click manually or reset CITarget's scoreboard.
                air_fill = actual_server_clear_wall_command(wall_fill)
                send_server_command(server, server_log, air_fill,
                                    expected=b"12 blocks filled")
                print("CUSTOMMC_189_WALL_RECOVERY_REAL_SERVER_AIR_REMOVAL_PASS=YES",
                      flush=True)
                wait_for(host, host_log, WALL_RECOVERY_VISIBLE, deadline,
                         "same native remote player visible and selected after wall removal")
                wait_for(host, host_log, WALL_RECOVERY_CLICK, deadline,
                         "Kill Aura owned real visible-player attack after wall removal")
                wait_for(host, host_log, WALL_RECOVERY_CONTROLLER, deadline,
                         "genuine Mojang PlayerController attackEntity post-wall")
                # A client click/controller entry cannot certify server PvP.
                # Only a positive, read-only vanilla scoreboard result can.
                recovered = False
                while time.monotonic() < deadline and not recovered:
                    try:
                        send_server_command(
                            server, server_log,
                            "scoreboard players test CITarget taken 1 999999",
                            expected=b"is in range 1 to 999999", timeout=1.4)
                        recovered = True
                    except AssertionError as error:
                        if "dedicated server unavailable" in str(error):
                            raise
                        check_process(server, "recovered wall-clear server damage probe")
                if not recovered:
                    send_server_command(server, server_log,
                                        "scoreboard players list CITarget")
                    raise AssertionError(
                        "real wall removal did not restore server-accepted PvP damage")
                print("CUSTOMMC_OFFICIAL_189_WALL_REMOVAL_SERVER_DAMAGE_PASS=YES",
                      flush=True)

        if args.fov_veto:
            # The second player is genuinely close enough to hit, but the
            # attacker looks away by ~180 degrees, exceeding default 90 FOV.
            send_server_command(server, server_log,
                                "scoreboard objectives add taken stat.damageTaken")
            send_server_command(server, server_log,
                                "scoreboard players set CITarget taken 0")
            send_server_command(server, server_log,
                                "scoreboard players test CITarget taken 0 0",
                                expected=b"score 0 is in range 0 to 0")
            send_server_command(server, server_log, "gamemode 0 CIHost")
            send_server_command(server, server_log, "gamemode 0 CITarget")
            send_server_command(server, server_log,
                                "gamerule naturalRegeneration false")
            host_tp, target_tp = actual_server_spawn_fov_fixture(server_log)
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, target_tp)
            print("CUSTOMMC_189_FOV_REAL_SERVER_SPAWN_CHUNK_PASS=YES",
                  flush=True)
            wait_for(host, host_log, FOV_DISABLED, deadline,
                     "20 real rear-player target callbacks with module disabled")
            wait_for(host, host_log, FOV_ENABLED, deadline,
                     "ordinary Kill Aura module enable")
            wait_for(host, host_log, FOV_VETO, deadline,
                     "80 real rear-player FOV veto callbacks")
            send_server_command(server, server_log,
                                "scoreboard players test CITarget taken 0 0",
                                expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_OFFICIAL_189_REAL_PLAYER_FOV_SERVER_NO_DAMAGE_PASS=YES",
                  flush=True)

        if args.range_veto:
            # Separate negative-only process. The second real signed client
            # must be in sight of world telemetry yet physically beyond
            # Kill Aura's configured reach (default 3.5 blocks).
            send_server_command(
                server, server_log,
                "scoreboard objectives add taken stat.damageTaken")
            send_server_command(
                server, server_log, "scoreboard players set CITarget taken 0")
            send_server_command(
                server, server_log,
                "scoreboard players test CITarget taken 0 0",
                expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_189_RANGE_TARGET_DAMAGE_BASELINE_ZERO_PASS=YES",
                  flush=True)
            send_server_command(server, server_log, "gamemode 0 CIHost")
            send_server_command(server, server_log, "gamemode 0 CITarget")
            send_server_command(server, server_log,
                                "gamerule naturalRegeneration false")
            # The second live player can walk ~0.6 blocks from its
            # initial teleport before the 80-tick veto finishes, becoming
            # legitimately in-range. Restrict target input/movement using
            # the vanilla dedicated-server Slowness effect; never change
            # any combat range, selector or damage gate.
            send_server_command(server, server_log,
                                "effect CITarget 2 600 5 true")
            host_tp, target_tp = actual_server_spawn_fixture(server_log)
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, target_tp)
            print("CUSTOMMC_189_RANGE_ACTUAL_SERVER_SPAWN_CHUNK_PASS=YES",
                  flush=True)
            print("CUSTOMMC_189_REAL_PLAYER_OUTSIDE_AURA_REACH_POSITIONED=YES",
                  flush=True)
            wait_for(host, host_log, RANGE_DISABLED, deadline,
                     "20 live far-player callbacks with Aura disabled")
            wait_for(host, host_log, RANGE_ENABLED, deadline,
                     "normal Aura activation with genuine far player")
            wait_for(host, host_log, RANGE_VETO, deadline,
                     "80 active Aura callbacks rejecting real distant player")
            # Authoritative NEGATIVE proof: no server-accepted PvP damage.
            # The bridge also throws immediately on any real controller
            # attack or host synthetic attack, making fake passes impossible.
            send_server_command(
                server, server_log,
                "scoreboard players test CITarget taken 0 0",
                expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_OFFICIAL_189_REAL_PLAYER_OUT_OF_RANGE_SERVER_NO_DAMAGE_PASS=YES",
                  flush=True)

        if args.pvp:
            # Launching the target's graphical JVM can unfocus CIHost.
            # Grab the real CIHost mouse BEFORE the authoritative baseline,
            # then overwrite both camera yaw and world positions by normal
            # dedicated-server teleport commands below. There are no
            # manual X11 attack clicks after arming.
            restore_real_attacker_focus(
                host, target, min(deadline, time.monotonic() + 16))
            # The same unmodified dedicated server owns BOTH real players
            # and the authoritative damage-taken scoreboard statistic.
            # Stat data is set to zero BEFORE the attack and never edited
            # after arming; only actual accepted server damage may raise it.
            send_server_command(
                server, server_log,
                "scoreboard objectives add taken stat.damageTaken")
            send_server_command(
                server, server_log, "scoreboard players set CITarget taken 0")
            if args.unlocked_remote_owner_veto:
                send_server_command(
                    server, server_log, "scoreboard players set CITargetB taken 0")
                send_server_command(
                    server, server_log,
                    "scoreboard players test CITargetB taken 0 0",
                    expected=b"score 0 is in range 0 to 0")
            send_server_command(
                server, server_log,
                "scoreboard players test CITarget taken 0 0",
                expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_189_SERVER_TARGET_DAMAGE_BASELINE_ZERO_PASS=YES",
                  flush=True)
            send_server_command(server, server_log, "gamemode 0 CIHost")
            # Hold the real remote player in vanilla creative until the
            # required mismatched original controller attempt was rejected.
            # The authoritative scoreboard must remain zero at the veto.
            send_server_command(server, server_log,
                                "gamemode 1 CITarget" if (
                                    args.unlocked_scoped_veto
                                    or args.unlocked_remote_owner_veto)
                                else "gamemode 0 CITarget")
            if args.unlocked_remote_owner_veto:
                send_server_command(server, server_log, "gamemode 1 CITargetB")
            send_server_command(server, server_log, "gamerule naturalRegeneration false")
            # The dedicated server generates a new FLAT spawn for each
            # trial. Teleporting BOTH clients to 0,0 can leave the remote
            # EntityPlayer outside the locally loaded entity inventory.
            # Reuse the ACTUAL login spawn chunk and a genuine two-block
            # separation, just as the certified wall controls do, without
            # creating a wall or changing any attack/range checks.
            host_tp, target_tp = actual_server_spawn_positive_pvp_fixture(
                server_log)
            if args.unlocked_remote_owner_veto:
                # Real remote B is source-native loaded but out of combat reach.
                # A is the only eligible vanilla target for unlocked Aura.
                v = target_tp.split()
                if len(v) != 7 or v[:2] != ["tp", "CITarget"]:
                    raise AssertionError("M424 native A position malformed")
                bx, by, bz = (int(q) for q in v[2:5])
                send_server_command(server, server_log,
                                    "tp CITargetB {} {} {} 180 0".format(
                                        bx + 7, by, bz))
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, target_tp)
            print("CUSTOMMC_189_PVP_REAL_SERVER_SPAWN_CHUNK_PASS=YES",
                  flush=True)
            print("CUSTOMMC_189_SERVER_REAL_PLAYER_PVP_POSITIONS_SET=YES",
                  flush=True)
            wait_for(host, host_log, AURA_DISABLED, deadline,
                     "20 disabled genuine nonlocal player target callbacks")
            wait_for(host, host_log, AURA_ENABLED, deadline,
                     "normal real-player Kill Aura module enable")
            if args.unlocked_remote_owner_veto:
                wait_for(host, host_log, REMOTE_PAIR_SCOPE_VETO, deadline,
                         "native controller A argument vetoes real remote B owner")
                send_server_command(server, server_log,
                                    "scoreboard players test CITarget taken 0 0",
                                    expected=b"score 0 is in range 0 to 0")
                send_server_command(server, server_log,
                                    "scoreboard players test CITargetB taken 0 0",
                                    expected=b"score 0 is in range 0 to 0")
                send_server_command(server, server_log, "gamemode 0 CITarget")
                wait_for(host, host_log, REMOTE_PAIR_SCOPE_FRESH, deadline,
                         "fresh native controller A argument matches actual selected A owner")
            if args.unlocked_scoped_veto:
                wait_for(host, host_log, UNLOCKED_SCOPE_VETO, deadline,
                         "original native controller rejected genuine local UUID scoped as remote")
                send_server_command(server, server_log,
                                    "scoreboard players test CITarget taken 0 0",
                                    expected=b"score 0 is in range 0 to 0")
                send_server_command(server, server_log, "gamemode 0 CITarget")
                wait_for(host, host_log, UNLOCKED_SCOPE_FRESH, deadline,
                         "original controller dispatch accepted real remote native UUID")
            wait_for(host, host_log, AURA_ATTACK, deadline,
                     "host-owned Kill Aura synthetic rotated player attack")
            print("CUSTOMMC_189_CLIENT_AURA_CLICK_OBSERVED=YES", flush=True)
            # An owned synthetic action is only a potential hit. Real PvP
            # must enter Mojang PlayerControllerMP.attackEntity and produce
            # both native player hurt-time and server-authoritative damage.
            wait_for(host, host_log, CONTROLLER_ATTACK,
                     min(deadline, time.monotonic() + 10),
                     "actual Mojang PlayerControllerMP.attackEntity method")
            print("CUSTOMMC_189_NATIVE_ATTACK_DISPATCH_PATH_ENTERED=YES",
                  flush=True)
            # Distinguish "module clicked but client never saw hurt" from
            # "client saw hurt but the dedicated server never counted damage".
            # The client marker alone never certifies PvP; both gates remain
            # mandatory. Fail quickly when there is no native hurt response.
            wait_for(host, host_log, AURA_HURT,
                     min(deadline, time.monotonic() + 18),
                     "same actual remote player receiving native hurt after Aura click")
            print("CUSTOMMC_189_CLIENT_REAL_PLAYER_HURT_OBSERVED=YES",
                  flush=True)
            # Server response comes first: determine whether real PvP damage
            # was accepted before interpreting any client animation/hurt flag.
            # Require a *server* stat.damageTaken objective change on the
            # exact target. Never infer PvP success only from the host client.
            verified_server_damage = False
            while time.monotonic() < deadline and not verified_server_damage:
                try:
                    send_server_command(
                        server, server_log,
                        "scoreboard players test CITarget taken 1 999999",
                        expected=b"is in range 1 to 999999", timeout=1.4)
                    verified_server_damage = True
                except AssertionError as error:
                    if "dedicated server unavailable" in str(error):
                        raise
                    check_process(server, "server-authoritative damage probe")
            if not verified_server_damage:
                # Official server console diagnostics, never fabricated.
                # This is a read-only scoreboard report, not a test bypass.
                send_server_command(server, server_log,
                                    "scoreboard players list CITarget")
                print("CUSTOMMC_189_SERVER_DAMAGE_SCORE_NOT_POSITIVE=YES",
                      flush=True)
                raise AssertionError("no dedicated-server damage statistic for CITarget")
            print("CUSTOMMC_OFFICIAL_189_SERVER_ACCEPTED_PLAYER_DAMAGE_PASS=YES",
                  flush=True)
            if args.unlocked_remote_owner_veto:
                send_server_command(server, server_log,
                                    "scoreboard players test CITargetB taken 0 0",
                                    expected=b"score 0 is in range 0 to 0")
                print("CUSTOMMC_189_UNLOCKED_REMOTE_B_SERVER_DAMAGE_STILL_ZERO_PASS=YES",
                      flush=True)

        for phase, proc in (
                ("dedicated server", server),
                ("host game", host),
                ("target game", target)):
            check_process(proc, phase)
        time.sleep(2)
        for phase, proc in (
                ("dedicated server", server),
                ("host game", host),
                ("target game", target)):
            check_process(proc, phase)

        if args.multi_target or args.simultaneous or args.locked_target or args.rejoin_target or args.rejoin_release or args.unlocked_remote_owner_veto:
            if not args.rejoin_release:
                check_process(target_b, "third native real player JVM")
            print("CUSTOMMC_OFFICIAL_189_THREE_REAL_CLIENTS_JOINED_PASS=YES",
                  flush=True)
        print("CUSTOMMC_OFFICIAL_189_LOOPBACK_ONLY_SERVER=YES", flush=True)
        print("CUSTOMMC_OFFICIAL_189_TWO_REAL_CLIENTS_JOINED_PASS=YES",
              flush=True)
        print("CUSTOMMC_OFFICIAL_189_REMOTE_PLAYER_KIND_SUSTAINED_PASS=YES",
              flush=True)
        if args.rejoin_target or args.rejoin_release:
            print("CUSTOMMC_OFFICIAL_189_REAL_PLAYER_REJOIN_UUID_LOCK_PASS=YES",
                  flush=True)
        if args.rejoin_release and not args.require_rejoined_controller_b:
            print("CUSTOMMC_OFFICIAL_189_SURVIVOR_DISCONNECT_UUID_REACQUIRE_PASS=YES",
                  flush=True)
            if args.require_stale_dispatch_veto:
                print("CUSTOMMC_OFFICIAL_189_REAL_EXIT_STALE_B_DISPATCH_VETO_AND_FRESH_A_DAMAGE_PASS=YES",
                      flush=True)
        if args.require_wrong_target_veto:
            print("CUSTOMMC_OFFICIAL_189_REJOIN_WRONG_UUID_CLICK_VETO_PASS=YES",
                  flush=True)
        if args.require_rejoined_controller_b:
            print("CUSTOMMC_OFFICIAL_189_REJOIN_CONTROLLER_B_SERVER_DAMAGE_PASS=YES",
                  flush=True)
        if args.locked_target:
            print("CUSTOMMC_OFFICIAL_189_REAL_UUID_LOCK_RELEASE_PASS=YES",
                  flush=True)
        if args.simultaneous:
            print("CUSTOMMC_OFFICIAL_189_SIMULTANEOUS_REAL_UUID_ARBITRATION_PASS=YES",
                  flush=True)
        if args.multi_target:
            print("CUSTOMMC_OFFICIAL_189_REAL_UUID_TARGET_SWITCH_PASS=YES",
                  flush=True)
        if args.pvp:
            print("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_PLAYER_PVP_PASS=YES",
                  flush=True)
            if args.unlocked_scoped_veto:
                print("CUSTOMMC_OFFICIAL_189_UNLOCKED_AURA_NATIVE_UUID_VETO_AND_FRESH_REMOTE_DAMAGE_PASS=YES",
                      flush=True)
            if args.unlocked_remote_owner_veto:
                print("CUSTOMMC_OFFICIAL_189_UNLOCKED_TWO_REMOTE_UUID_VETO_AND_FRESH_A_DAMAGE_PASS=YES",
                      flush=True)
        elif args.range_veto:
            print("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_RANGE_NEGATIVE_PASS=YES",
                  flush=True)
        elif args.wall_lifecycle:
            print("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_WALL_LIFECYCLE_PASS=YES",
                  flush=True)
        elif args.wall_recovery:
            print("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_WALL_RECOVERY_PASS=YES",
                  flush=True)
        elif args.wall_veto:
            print("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_WALL_NEGATIVE_PASS=YES",
                  flush=True)
        elif args.fov_veto:
            print("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_FOV_NEGATIVE_PASS=YES",
                  flush=True)
        else:
            print("CUSTOMMC_OFFICIAL_189_KILLAURA_PLAYER_HIT_TESTED=NO",
                  flush=True)
            print("CUSTOMMC_OFFICIAL_189_SERVER_ACCEPTED_DAMAGE_TESTED=NO",
                  flush=True)
    except BaseException:
        for name, log in logs:
            print("M405_" + name.upper() + "_TAIL_START",
                  file=sys.stderr)
            print(log_tail(log, 6000).decode("utf-8", "replace"),
                  file=sys.stderr)
            print("M405_" + name.upper() + "_TAIL_END",
                  file=sys.stderr)
        raise
    finally:
        for process in reversed(processes):
            close_process(process)
        close_process(wm)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("java", "server", "client", "libraries", "assets",
                 "natives", "overlay", "root"):
        parser.add_argument("--" + name, required=True)
    parser.add_argument("--timeout", type=int, default=150)
    parser.add_argument("--scaffold-placement", choices=("enabled","disabled","menu-open","repeat-two","production-repeat-two","production-repeat-three","production-repeat-three-history","production-repeat-four","production-repeat-two-keyboard-walk"),
                        help="dedicated Mojang server-authoritative Scaffold placement/disabled proof")
    parser.add_argument("--require-index-reorder", action="store_true",
                        help="fail-closed actual native entity-index change on fresh A rejoin")
    parser.add_argument("--require-wrong-target-veto", action="store_true",
                        help="exercise original native click gate against in-range wrong UUID after rejoin")
    parser.add_argument("--require-rejoined-controller-b", action="store_true",
                        help="prove genuine vanilla controller B attack and fresh B server damage while A survival remains undamaged")
    parser.add_argument("--require-controller-entity-uuid", action="store_true",
                        help="require actual Mojang controller attackEntity argument UUID equals locked native B")
    parser.add_argument("--unlocked-remote-owner-veto", action="store_true",
                        help="three real players: reject genuine remote B owner UUID for A, then damage A only")
    parser.add_argument("--unlocked-scoped-veto", action="store_true",
                        help="isolated real local-UUID owner negative on unlocked Aura plus fresh remote server damage")
    parser.add_argument("--require-scoped-aura-owner-veto", action="store_true",
                        help="verify production scoped Aura outgoing UUID veto using real stale B intent and fresh A damage")
    parser.add_argument("--require-stale-dispatch-veto", action="store_true",
                        help="veto once at real controller boundary using retained genuine B intent after B disconnect and fresh A reacquire")
    group = parser.add_mutually_exclusive_group()
    group.add_argument("--rejoin-release", action="store_true",
                       help="real A disconnect/rejoin, then real B disconnect; reacquire fresh A UUID and damage")
    group.add_argument("--rejoin-target", action="store_true",
                       help="genuine old player exits and fresh JVM rejoins same UUID; verify native B ownership")
    group.add_argument("--locked-target", action="store_true",
                       help="genuine three-player sticky UUID lock retained then released on invalid A")
    group.add_argument("--simultaneous", action="store_true",
                       help="two simultaneously eligible genuine UUID players; verify priority changes and server damage")
    group.add_argument("--multi-target", action="store_true",
                       help="three actual clients with two real UUID PvP targets")
    group.add_argument("--pvp", action="store_true",
                       help="prove Kill Aura attack and real server damage statistic")
    group.add_argument("--range-veto", action="store_true",
                       help="prove real out-of-range player cannot be attacked")
    group.add_argument("--wall-veto", action="store_true",
                       help="prove native LOS-veto for in-range player behind server wall")
    group.add_argument("--wall-recovery", action="store_true",
                       help="prove stone wall veto then same server wall removal restores damage")
    group.add_argument("--wall-lifecycle", action="store_true",
                       help="verify real wall-check off/on invalidates occluded targets before damage recovery")
    group.add_argument("--fov-veto", action="store_true",
                       help="prove in-range real player behind camera is excluded")
    group.add_argument("--esp-visual", action="store_true",
                       help="require exact remote Mojang UUID actual Box ESP HUD draw")
    args = parser.parse_args()
    if args.unlocked_scoped_veto and not args.pvp:
        parser.error("--unlocked-scoped-veto requires --pvp")
    if args.unlocked_remote_owner_veto and (
            not args.pvp or args.unlocked_scoped_veto):
        parser.error("--unlocked-remote-owner-veto requires --pvp and excludes --unlocked-scoped-veto")
    if args.require_index_reorder and not args.rejoin_release:
        parser.error("--require-index-reorder requires --rejoin-release")
    if args.require_wrong_target_veto and (
            not args.rejoin_release or not args.require_index_reorder):
        parser.error("--require-wrong-target-veto requires --rejoin-release and --require-index-reorder")
    if args.require_rejoined_controller_b and (
            not args.rejoin_release or not args.require_index_reorder
            or not args.require_wrong_target_veto):
        parser.error("--require-rejoined-controller-b requires --rejoin-release --require-index-reorder --require-wrong-target-veto")
    if args.require_controller_entity_uuid and not args.require_rejoined_controller_b:
        parser.error("--require-controller-entity-uuid requires --require-rejoined-controller-b")
    if args.require_stale_dispatch_veto and (
            not args.rejoin_release or not args.require_index_reorder
            or not args.require_wrong_target_veto or args.require_rejoined_controller_b):
        parser.error("--require-stale-dispatch-veto requires --rejoin-release --require-index-reorder --require-wrong-target-veto and excludes --require-rejoined-controller-b")
    if args.require_scoped_aura_owner_veto and (
            not args.rejoin_release or not args.require_index_reorder
            or not args.require_wrong_target_veto
            or not args.require_rejoined_controller_b
            or not args.require_controller_entity_uuid
            or args.require_stale_dispatch_veto):
        parser.error("--require-scoped-aura-owner-veto needs --rejoin-release --require-index-reorder --require-wrong-target-veto --require-rejoined-controller-b --require-controller-entity-uuid; excludes the M421 test")
    run(args)


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("CUSTOMMC_OFFICIAL_189_LOCAL_MULTIPLAYER_FAILED: "
              + str(error), file=sys.stderr)
        sys.exit(1)
