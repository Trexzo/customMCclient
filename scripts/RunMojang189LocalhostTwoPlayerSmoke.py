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
TRIGGER_DISABLED = b"CUSTOMMC_189_TRIGGERBOT_DISABLED_REAL_PLAYER_TICKS_PASS=20"
TRIGGER_ENABLED = b"CUSTOMMC_189_TRIGGERBOT_NORMAL_MODULE_ENABLE_PASS=YES"
TRIGGER_CLICK = b"CUSTOMMC_189_TRIGGERBOT_NATIVE_RAY_UUID_OWNED_CLICK_PASS=YES"
TRIGGER_RAY = b"CUSTOMMC_189_TRIGGERBOT_ORIGINAL_PLAYER_RAYCAST_ARMED=YES"
TRIGGER_CONTROLLER = b"CUSTOMMC_189_TRIGGERBOT_REAL_MOJANG_CONTROLLER_ARGUMENT_PASS=YES"
TRIGGER_HURT = b"CUSTOMMC_OFFICIAL_189_TRIGGERBOT_REAL_PLAYER_HURT_PASS=YES"
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


def v320_original_enabled_scaffold_escape_evidence(native_phase_bytes,
                                                   escape_already_sent=False):
    """Prove the original client has a persistent OPEN GUI before one real Esc.

    V319 original run #40105144305 attempt 3 showed repeated REAL original
    "screen=OPEN source=missing_player_world_cursor_or_selected_item".
    The old positive-candidate-only Escape rule deadlocked: an open GUI
    itself can suppress a positive World/cursor/item candidate. Use two
    consecutive complete original ARM_WAIT observations, scoped to the
    enabled-placement phase. A newer CLEAR observation vetoes recovery.
    Neither this decision nor Esc grants module/placement/server PASS.
    """
    if not isinstance(native_phase_bytes, bytes) or not isinstance(
            escape_already_sent, bool):
        raise ValueError("V320 needs original bytes and one-shot Escape flag")
    if escape_already_sent:
        return False
    marker = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ARM_WAIT "
    lines = native_phase_bytes.split(b"\n")
    # The final (possibly partially written) line is never authoritative.
    complete = lines[:-1]
    reports = [line.rstrip(b"\r") for line in complete
               if line.startswith(marker)]
    if not reports:
        return False
    current = reports[-1]
    if not current.startswith(marker + b"screen=OPEN source="):
        return False
    if current == marker + b"screen=OPEN source=positive_candidate_already_available":
        return True
    return (len(reports) >= 2
            and reports[-2].startswith(marker + b"screen=OPEN source="))


def v318_original_negative_native_source(log, scene, limit=1048576):
    """Bounded original JVM source lines on a FAILED native acceptance only.

    Never use these read-only lines as attack, FOV, Wall Check or server
    scoreboard authorization. The game process writes its own real evidence.
    """
    tags = {
        "fov": b"CUSTOMMC_189_FOV_ORIGINAL_SOURCE_OBSERVATION ",
        "wall": b"CUSTOMMC_189_WALL_LIFECYCLE_NATIVE_SOURCE_DIAG ",
    }
    if scene not in tags or not isinstance(limit, int) or isinstance(limit, bool) or not 128 <= limit <= 1048576:
        raise ValueError("V318 invalid native source diagnostic request")
    original = Path(log)
    if original.is_symlink() or not original.is_file():
        return []
    with original.open("rb") as stream:
        size = stream.seek(0, 2)
        start = max(0, size - limit)
        stream.seek(start)
        raw = stream.read(limit)
    # Do not accept a partial original Java log line from the bounded tail.
    if start:
        newline = raw.find(b"\n")
        raw = raw[newline + 1:] if newline >= 0 else b""
    newline = raw.rfind(b"\n")
    raw = raw[:newline + 1] if newline >= 0 else b""
    result = []
    for line in raw.splitlines():
        if line.startswith(tags[scene]):
            # Original Minecraft numeric callback, not an invented PASS.
            result.append(line.decode("utf-8", "replace"))
    return result[-24:]


def v345_original_screen_timeline(log, max_bytes=16777216):
    """First and last original gameTick GuiScreen classes, FAILURES ONLY.

    Bounded source read and bounded output; no class names are synthesized,
    no GUI contents are touched and NO PvP/controller permission is inferred.
    """
    if (not isinstance(max_bytes, int) or isinstance(max_bytes, bool)
            or not 1 <= max_bytes <= 16777216):
        raise ValueError("V345 original screen read limit invalid")
    source = Path(log)
    if source.is_symlink() or not source.is_file():
        return []

    marker = b"CUSTOMMC_189_ACCEPTANCE_SCREEN="
    early, recent = [], []
    scanned = 0
    with source.open("rb") as stream:
        for raw in stream:
            scanned += len(raw)
            if scanned > max_bytes:
                break
            if not raw.endswith(b"\n") or not raw.startswith(marker):
                continue
            witness = raw.rstrip(b"\r\n")[:384].decode("utf-8", "replace")
            if len(early) < 5:
                early.append(witness)
            else:
                recent.append(witness)
                if len(recent) > 5:
                    recent.pop(0)
    return early + recent


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


def wait_for_new_original_source_marker(proc, log, from_offset,
                                       marker, deadline, phase):
    """Read original Minecraft JVM evidence emitted AFTER a server command.

    Earlier native stdout cannot satisfy the current World convergence
    fence. No player objects or server-only inferred positions are used.
    """
    if from_offset < 0 or not isinstance(marker, bytes) or not marker:
        raise AssertionError("original source observation needs offset/bytes")
    while time.monotonic() < deadline:
        check_process(proc, phase)
        with log.open("rb") as reader:
            reader.seek(from_offset)
            if marker in reader.read():
                return
        time.sleep(0.2)
    raise AssertionError("missing fresh original source after real teleport: " + phase)


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


def actual_server_spawn_range_negative_fixture(server_log):
    """Real source-logged spawn chunk; far-player range veto only.

    The general four-block two-client fixture is used by POSITIVE cases,
    and must not change. In an observed negative original run, target
    movement shrank the source distance to sqrt(2.31) before 80 callback
    confirmations and the correctly strict native out-of-range assertion
    crashed. Reserve seven genuine world blocks of same-chunk clearance
    for only the negative reach case, using vanilla server teleports.
    """
    host, _normal_target = actual_server_spawn_fixture(server_log)
    source = host.split()
    if len(source) != 7 or source[:2] != ["tp", "CIHost"]:
        raise AssertionError("range negative original host teleport malformed")
    hx, hy, hz = (int(n) for n in source[2:5])
    offset = 7 if hz % 16 <= 8 else -7
    target_z = hz + offset
    if hz // 16 != target_z // 16:
        raise AssertionError("range negative crossed real host spawn chunk")
    if abs(offset) < 7 or abs(offset) > 8:
        raise AssertionError("source range negative lacks original reach clearance")
    target_yaw = 180 if offset > 0 else 0
    host_yaw = 0 if offset > 0 else 180
    return (
        "tp CIHost {} {} {} {} 0".format(hx, hy, hz, host_yaw),
        "tp CITarget {} {} {} {} 0".format(hx, hy, target_z, target_yaw),
    )


def actual_server_fov_ground_support_fixture(host_tp, target_tp):
    """Two real rear-player test feet get a verified vanilla stone floor.

    The V175 original late FOV run measured horizontal=2.0 yet native 3D
    distance=5.85, invalidating the genuine <3.5 reach prerequisite. This
    helper derives ONLY actual server commands from that same login-based
    host/target, never changes source player coordinates or FOV checks.
    """
    host = host_tp.split()
    target = target_tp.split()
    if (len(host) != 7 or len(target) != 7
            or host[:2] != ["tp", "CIHost"]
            or target[:2] != ["tp", "CITarget"]):
        raise AssertionError("FOV support requires original server teleports")
    try:
        hx, hy, hz = (int(v) for v in host[2:5])
        tx, ty, tz = (int(v) for v in target[2:5])
    except ValueError as error:
        raise AssertionError("FOV floor source coordinates invalid") from error
    if hx != tx or hy != ty or abs(tz-hz) != 2 or not (1 <= hy <= 254):
        raise AssertionError("FOV original two-player ground geometry unexpected")
    if hx//16 != tx//16 or hz//16 != tz//16:
        raise AssertionError("FOV genuine floor crosses original loaded chunk")
    # Real 3x5 solid floor at player feet y-1, with a 1-block lateral
    # margin for normal original player movement on either side.
    x0, x1 = hx-1, hx+1
    z0, z1 = min(hz, tz)-1, max(hz, tz)+1
    prefix = "fill {} {} {} {} {} {}".format(
        x0, hy-1, z0, x1, hy-1, z1)
    return prefix + " air", prefix + " stone"


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


def actual_server_simultaneous_a_knockback_wall_fixture(
        host_tp, a_near, b_outer, b_near):
    """Physical vanilla server stone wall behind real A, never fake target proof.

    A's first actual Survival PvP hit can knock A farther away than B before
    the deliberate B /tp, so B is validly selected/hit prematurely. Keep the
    original both-eligible real-player phase and strict zero-B scoreboard gate.
    The wall is behind A, not between the host and either remote player.
    """
    parts = [command.split() for command in
             (host_tp, a_near, b_outer, b_near)]
    if any(len(v) != 7 or v[0] != "tp" for v in parts):
        raise AssertionError("simultaneous wall needs four genuine tp commands")
    if [v[1] for v in parts] != ["CIHost", "CITarget",
                                 "CITargetB", "CITargetB"]:
        raise AssertionError("simultaneous wall source player ownership invalid")
    try:
        h = tuple(int(v) for v in parts[0][2:5])
        a = tuple(int(v) for v in parts[1][2:5])
        b = tuple(int(v) for v in parts[2][2:5])
        near = tuple(int(v) for v in parts[3][2:5])
    except ValueError as error:
        raise AssertionError("simultaneous wall nonintegral server source") from error
    sign = 1 if a[2] > h[2] else -1
    if (h[1] != a[1] or a[1] != b[1] or b[1] != near[1]
            or h[0] != a[0] or abs(a[2] - h[2]) != 2
            or b[2] != a[2] or abs(b[0] - a[0]) != 2
            or near[2] != h[2] + sign
            or abs(near[0] - a[0]) != 1):
        raise AssertionError("simultaneous original A/B geometry changed")
    # Minecraft 1.8.9 server /tp integer coordinates locate the player
    # at x+0.5,z+0.5. The wall is one FULL stone tile beyond A, so
    # it cannot intersect the genuine initially teleported player box.
    wall_z = a[2] + sign
    x0, x1 = a[0] - 1, a[0] + 1
    y0, y1 = a[1], a[1] + 2
    if abs(wall_z) >= 29999990 or abs(x0) >= 29999990 or abs(x1) >= 29999990:
        raise AssertionError("simultaneous original wall out of world")
    if not (2 <= y0 and y1 <= 253):
        raise AssertionError("simultaneous original wall outside vanilla height")
    # Force nine actual server blocks, independent vanilla command replies.
    return ("fill {} {} {} {} {} {} stone".format(
                x0, y0, wall_z, x1, y1, wall_z),
            ((x0, y0, wall_z), (a[0], y0 + 1, wall_z),
             (x1, y1, wall_z)))


def actual_server_original_rejoin_prelock_a_fixture(host_tp, a_near, b_outer):
    """Real original A must beat real B by .12 in initial source World.

    V177 genuine final late rejoin saw A=2.222268337, B=2.236067977:
    A was selected but .014 native separation did NOT satisfy the
    existing .12 initial UUID lock prerequisite over 20 callbacks.
    Position only the ORIGINAL real A 1.55 blocks forward (rather than
    two); keep genuine B at sqrt(5), actual server chunk, normal vanilla
    player/camera, untouched target and unchanged acceptance thresholds.
    """
    host=host_tp.split()
    a=a_near.split()
    b=b_outer.split()
    if (len(host)!=7 or len(a)!=7 or len(b)!=7
            or host[:2]!=["tp","CIHost"] or a[:2]!=["tp","CITarget"]
            or b[:2]!=["tp","CITargetB"]):
        raise AssertionError("initial original A source commands malformed")
    try:
        hx,hy,hz=(int(v) for v in host[2:5])
        ax,ay,az=(int(v) for v in a[2:5])
        bx,by,bz=(int(v) for v in b[2:5])
    except ValueError as exc:
        raise AssertionError("initial original A source coordinates invalid") from exc
    direction=1 if az>hz else -1
    if (ax!=hx or ay!=hy or by!=hy or bz!=az
            or abs(az-hz)!=2 or abs(bx-hx)!=1):
        raise AssertionError("initial original A/B source geometry unexpected")
    new_z=hz+0.5+direction*1.55
    new_x=hx+0.5
    if (math.floor(new_z/16)!=hz//16
            or math.floor(new_x/16)!=hx//16
            or bz//16!=hz//16 or bx//16!=hx//16):
        raise AssertionError("initial original A source crossed loaded chunk")
    # A true 1.55 original 3D block baseline survives +/-0.30
    # source-native drift; actual Mojang World still decides the 20-tick
    # eligible window and exact lock/UUID/ray before any controller PASS.
    d_a=math.hypot(new_x-(hx+.5),new_z-(hz+.5))
    d_b=math.hypot(bx-hx,bz-hz)
    if not (d_a-.30>1 and d_a+.30+.12<d_b
            and abs(bx-(hx))==1 and abs(az-hz)==2):
        raise AssertionError("initial original A real 3D source margin lost")
    return "tp CITarget {:.2f} {} {:.2f} {} {}".format(
        new_x,ay,new_z,a[5],a[6])


def actual_server_rejoined_a_controller_fixture(host_tp, a_near, b_outer):
    """Original M419 A must satisfy BOTH native reach lower and upper bounds.

    Real prior runs prove both bounds fail: one integer-forward /tp left
    moving native A only .79 from host (below da>=1), while the original
    1.5-decimal fixture later let A drift to da≈2.330 vs B db≈2.428,
    violating the unchanged da+.12<db mandatory real 3D gap (≈.098).
    V172 proves a THIRD independent failure: true native B UUID/rotation
    selected with a valid 3D margin, yet original objectMouseOver actually
    hits *A*, entity index 6, instead of B at index 1, yielding no new B
    server damage. Keep A 1.35 blocks forward, but offset genuine A 0.45
    blocks laterally AWAY from B's original x-side. This changes only the
    dedicated original Minecraft server's REAL A /tp coordinates, not
    player objects, source ray, controller, UUID or acceptance thresholds.
    The original ray-B-and-server-B-damage checks still decide success.
    """
    host = host_tp.split()
    a = a_near.split()
    b = b_outer.split()
    if (len(host) != 7 or host[:2] != ["tp", "CIHost"]
            or len(a) != 7 or a[:2] != ["tp", "CITarget"]
            or len(b) != 7 or b[:2] != ["tp", "CITargetB"]):
        raise AssertionError("M419 original server teleport positions invalid")
    hx, hy, hz = (int(value) for value in host[2:5])
    ax, ay, az = (int(value) for value in a[2:5])
    bx, by, bz = (int(value) for value in b[2:5])
    if (ax != hx or ay != hy or by != hy or bz != az
            or abs(az - hz) != 2 or abs(bx - hx) != 1):
        raise AssertionError("M419 source A/B world geometry unexpected")
    direction = 1 if az > hz else -1
    # Vanilla integer /tp centers at x/z+.5. Explicit DECIMAL coordinates
    # place a real original source player without integer block centering.
    # Real B remains the original two-forward/one-lateral off-axis player.
    # Offset only rejoined A's x center to the OPPOSITE side of the ray
    # toward B. Both A and B remain actual separate vanilla 0.6 hitboxes.
    near_z = hz + 0.5 + direction * 1.35
    b_side = 1 if bx > hx else -1
    near_x = hx + 0.5 - b_side * 0.45
    if (math.floor(near_x / 16) != hx // 16
            or math.floor(near_z / 16) != hz // 16
            or hx // 16 != bx // 16
            or hz // 16 != bz // 16):
        raise AssertionError("M419 real source A/B crossed loaded chunk")
    real_host_z = hz + 0.5
    real_b_z = bz + 0.5
    real_host_x = hx + 0.5
    real_b_x = bx + 0.5
    real_distance_b = math.hypot(real_b_x-real_host_x,
                                  real_b_z-real_host_z)
    real_distance_a = math.hypot(near_x-real_host_x,
                                 near_z-real_host_z)
    # 2D ray toward genuine B crosses rejoined A's Z band. It must never
    # intersect A's original 0.6-wide x hitbox on these source commands.
    # Minecraft's full real 3D objectMouseOver remains the actual proof.
    b_ray_x_at_a_near = (real_b_x-real_host_x) * (
        (abs(near_z-real_host_z)-0.30) / abs(real_b_z-real_host_z))
    if abs(b_ray_x_at_a_near - (near_x-real_host_x)) <= 0.30:
        raise AssertionError("M419 rejoined A blocks source ray toward B")
    # Source acceptance STILL requires da>=1, da<=3.25 and da+.12<db.
    # Check 0.30 native A movement toward the host against lower bound,
    # independent .9 vertical movement against closer-than-B upper margin.
    # Native World may move farther; actual original runtime still fails
    # closed if this conservative source fixture cannot hold.
    if not (real_distance_a-0.3 >= 1.0
            and math.hypot(real_distance_a,0.9) + 0.12 < real_distance_b
            and 1 < real_distance_a < real_distance_b
            and abs(bx - ax) == 1):
        raise AssertionError("M419 source native A/B 3D reach window lost")
    return "tp CITarget {:.2f} {} {:.2f} {} {}".format(
        near_x, ay, near_z, a[5], a[6])


def actual_server_rejoined_host_recenter_fixture(host_tp, a_rejoined, b_outer):
    """Restore the ACTUAL CIHost source center before phase4 native B click.

    In exact V176 native M419 original crash source, host shifted to
    (-1.20,4,-.09) while A and B stayed at (-.06,4,-2.15) and
    (-1.50,4,-2.50); this produced da=2.349, db=2.428,
    violating the intact da+.12<db gate. A server-acknowledged real host
    /tp after all earlier rejoin assertions reduces accumulated movement
    from earlier combat, without altering target UUID/ray/damage acceptance.
    """
    host = host_tp.split()
    a = a_rejoined.split()
    b = b_outer.split()
    if (len(host) != 7 or len(a) != 7 or len(b) != 7
            or host[:2] != ["tp", "CIHost"]
            or a[:2] != ["tp", "CITarget"]
            or b[:2] != ["tp", "CITargetB"]):
        raise AssertionError("M419 phase4 original server geometry malformed")
    try:
        hx, hy, hz = (int(x) for x in host[2:5])
        ax, ay, az = (float(x) for x in a[2:5])
        bx, by, bz = (int(x) for x in b[2:5])
    except ValueError as error:
        raise AssertionError("M419 phase4 server coordinates invalid") from error
    if not all(math.isfinite(v) for v in (ax, ay, az)):
        raise AssertionError("M419 phase4 nonfinite source player")
    # Grounded A is 1.35 ahead and 0.45 opposite B, untouched.
    if (ay != hy or by != hy or abs(bz-hz) != 2
            or abs(bx-hx) != 1 or abs(az-(hz+0.5)) < 1.34
            or abs(az-(hz+0.5)) > 1.36
            or abs(ax-(hx+0.5)) < .44
            or abs(ax-(hx+0.5)) > .46
            or (ax-(hx+.5))*(bx-hx) >= 0):
        raise AssertionError("M419 phase4 recenter changes source A/B geometry")
    ideal_a = math.hypot(ax-(hx+.5), az-(hz+.5))
    ideal_b = math.hypot(bx-hx, bz-hz)
    if not (1.0 < ideal_a < 3.25 and ideal_a+.12 < ideal_b):
        raise AssertionError("M419 rejoined A/B real host recenter loses margin")
    return host_tp


def actual_server_rejoined_b_margin_anchor_fixture(host_tp, a_rejoined, b_outer):
    """Re-seat genuine survivor B 0.50 block outward on the REAL server.

    V320 immutable original rerun #40196757559 attempt 2 had actual native
    A=1.335000585 and B=1.481988213, both native UUIDs correct, but a
    B-A gap of only 0.146988 failed the unchanged 0.30 stable-source
    fence despite satisfying the 0.12 native controller-selection gate.
    Move ONLY actual vanilla B after its first supported reseat; neither
    fabricate host World data nor weaken 20 fresh native samples, ray,
    correct UUID or independent server B-only damage.
    """
    actual_server_rejoined_host_recenter_fixture(
        host_tp, a_rejoined, b_outer)
    h, a, b = host_tp.split(), a_rejoined.split(), b_outer.split()
    hx, hy, hz = (int(v) for v in h[2:5])
    ax, ay, az = (float(v) for v in a[2:5])
    bx, by, bz = (int(v) for v in b[2:5])
    dx, dz = bx - hx, bz - hz
    distance = math.hypot(dx, dz)
    if not (abs(dx) == 1 and abs(dz) == 2 and hy == by
            and ay == hy and distance > 0):
        raise AssertionError("V322 genuine B original source geometry invalid")
    b_x = bx + .5 + .5 * dx / distance
    b_z = bz + .5 + .5 * dz / distance
    a_distance = math.hypot(ax-(hx+.5), az-(hz+.5))
    b_distance = math.hypot(b_x-(hx+.5), b_z-(hz+.5))
    if not (2.7 < b_distance < 3.5 and
            b_distance-a_distance > .75 and
            abs(b_x) < 1024 and abs(b_z) < 1024):
        raise AssertionError("V322 physical B buffer outside native 3D bounds")
    # Must stand on the SAME actual server stone floor already verified
    # beneath all 3 vanilla players. An unsupported /tp cannot pass.
    floor = actual_server_rejoined_phase4_grounded_floor_fixture(
        host_tp, a_rejoined, b_outer).split()
    x0, y0, z0, x1, y1, z1 = (int(v) for v in floor[1:7])
    if not (x0 <= math.floor(b_x) <= x1
            and z0 <= math.floor(b_z) <= z1
            and y0 == y1 == by-1):
        raise AssertionError("V322 genuine B anchor lacks server stone")
    return "tp CITargetB {:.2f} {} {:.2f} {} {}".format(
        b_x, by, b_z, b[5], b[6])


def actual_server_rejoined_phase4_grounded_floor_fixture(host_tp, a_rejoined, b_outer):
    """Return a real dedicated-server stone floor under ALL 3 phase4 players.

    V193 original M419 failed after native phase4 source convergence:
    actual 3D A=2.468526924, B=2.428179743, violating the unchanged
    strict da+.12<db gate. A four-block survival floor alone did not
    constrain both the original host and creative B's vertical physics.
    Place genuine vanilla stone below all original server /tp feet,
    preserving real player UUIDs, camera rays, exact target rotation,
    original native controller and strict 3D checks.
    """
    # Existing source validation still requires 1.35 forward real A,
    # offset 0.45 opposite B, same loaded chunk and non-occluding ray.
    actual_server_rejoined_host_recenter_fixture(
        host_tp, a_rejoined, b_outer)
    host = host_tp.split()
    a = a_rejoined.split()
    b = b_outer.split()
    hx, hy, hz = (int(v) for v in host[2:5])
    ax, ay, az = (float(v) for v in a[2:5])
    bx, by, bz = (int(v) for v in b[2:5])
    if (hy != ay or hy != by):
        raise AssertionError("M419 all real players must have same planned feet Y")
    fx, fz = math.floor(ax), math.floor(az)
    # The previous tiny floor under planned positions FAILED original
    # 1.8.9 V199 with genuine B outside it: source B at (-595.59375,
    # 4,-309.09375) vs actual vanilla /tp (-596.5,5,-309.5), host at
    # (-597.5,5,-311.5). Cover the observed possible drift with a finite
    # three-block per-side buffer of REAL STONE at the same y=feet-1.
    # A real server fill+testforblock and real client World still decide.
    padding = 3
    lo_x, hi_x = min(fx, hx, bx) - padding, max(fx + 1, hx, bx) + padding
    lo_z, hi_z = min(fz, hz, bz) - padding, max(fz + 1, hz, bz) + padding
    if lo_x > hi_x or lo_z > hi_z:
        raise AssertionError("M419 original floor bounds invalid")
    return "fill {} {} {} {} {} {} stone".format(
        lo_x, hy - 1, lo_z, hi_x, hy - 1, hi_z)


def actual_server_three_client_rejoin_fixture(server_log):
    """Real server positions, with A nearer and locked survivor B in ray reach.

    Unlike the separate simultaneous-player fixture, the rejoin scenario
    must make a real original Minecraft raycast and UUID-locked controller
    attack after A disconnects. The prior B at a diagonal sqrt(8) blocks
    was repeatedly selectable (native target index and UUID correct), but
    intermittently NOT under Minecraft.objectMouseOver when its original
    camera was yaw -45 at feet y=4. Retain a genuine two-axis target
    (to exercise real native rotation), while bringing B inward to sqrt(5)
    blocks and leaving A at two. No synthetic game data or widened raycast.
    """
    host, a_near, _b_outer, _b_near = (
        actual_server_three_client_simultaneous_fixture(server_log))
    parts = a_near.split()
    hparts = host.split()
    if (len(parts) != 7 or parts[:2] != ["tp", "CITarget"]
            or len(hparts) != 7 or hparts[:2] != ["tp", "CIHost"]):
        raise AssertionError("original rejoin fixture lacks server-derived positions")
    x, y, z = (int(value) for value in parts[2:5])
    hx, hy, hz = (int(value) for value in hparts[2:5])
    if x != hx or y != hy or abs(z - hz) != 2:
        raise AssertionError("unexpected original A/host rejoin geometry")
    # V319: real ORIGINAL Mojang dedicated-server login #40098773646
    # reported host (-1022.5,4,-876.5): floor() gives x=-1023.
    # Under the unchanged loopback max-world-size=1024 the original
    # 5x7 physical floor would reach x=-1025, so the STRICT V315 floor
    # boundary correctly vetoed that source before any native rejoin proof.
    # Re-anchor ONLY THIS REJOIN SCENARIO using REAL /tp commands derived
    # from that genuine login, inward by at most 3 native source cells.
    # All three original player commands are actually sent to the vanilla
    # server; the 35 actual stone blocks, three testforblock witnesses,
    # original World/UUID/controller/ray and no-wrong-target assertions remain.
    if abs(hx) >= 1024 or abs(hz) >= 1024:
        raise AssertionError("original rejoin login outside 1024 server world")
    safe_x = max(-1020, min(1020, hx))
    safe_z = max(-1020, min(1020, hz))
    if (safe_x, safe_z) != (hx, hz):
        dx, dz = safe_x - hx, safe_z - hz
        if (abs(dx) > 3 or abs(dz) > 3
                or hx // 16 != safe_x // 16
                or hz // 16 != safe_z // 16
                or z // 16 != (z + dz) // 16
                or safe_z // 16 != (safe_z + (z - hz)) // 16):
            raise AssertionError("original rejoin inward source left loaded spawn chunk")
        source_host = hparts[:]
        source_a = parts[:]
        source_host[2], source_host[4] = str(safe_x), str(safe_z)
        source_a[2], source_a[4] = str(safe_x), str(z + dz)
        host = " ".join(source_host)
        a_near = " ".join(source_a)
        hx, hz, x, z = safe_x, safe_z, safe_x, z + dz
    direction = 1 if x % 16 <= 13 else -1
    bx = x + direction
    if x // 16 != bx // 16:
        raise AssertionError("original rejoin B crossed source-loaded chunk")
    # Distances in the original horizontal plane, from actual server TP
    # coordinates: A=2, B=sqrt(5), both <3, two distinct non-overlapping
    # vanilla 0.6-wide player hitboxes, no forced teleport after A unload.
    if not (math.hypot(bx - hx, z - hz) < 3.0
            and math.hypot(x - hx, z - hz)
            < math.hypot(bx - hx, z - hz)
            and abs(bx - x) >= 1):
        raise AssertionError("source rejoin B not distinct and in native reach")
    b_outer = "tp CITargetB {} {} {} 180 0".format(bx, y, z)
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


def actual_server_rejoin_three_player_floor_fixture(host_tp, a_near, b_outer):
    """Build a genuine vanilla stone floor supporting ALL three live players.

    V306 second original V47 observed A native 1.83D but B 11.22D after
    teleport, failing the original strict B-in-reach source requirement.
    All three players teleport at actual server login-derived Y, but an
    original remote may drift downwards while another remains high.
    Server-fill 35 physical stone blocks directly BELOW their target feet.
    This never supplies entity positions, UUIDs, a synthetic selector,
    camera ray, controller attack, or a native acceptance PASS.
    """
    parsed = [command.split() for command in (host_tp, a_near, b_outer)]
    if any(len(parts) != 7 or parts[0] != "tp" for parts in parsed):
        raise AssertionError("rejoin floor needs three complete real server tp commands")
    if [parts[1] for parts in parsed] != ["CIHost", "CITarget", "CITargetB"]:
        raise AssertionError("rejoin floor requires original source player owners")
    try:
        (hx, hy, hz), (ax, ay, az), (bx, by, bz) = [
            tuple(int(n) for n in parts[2:5]) for parts in parsed]
    except ValueError as error:
        raise AssertionError("rejoin floor nonintegral source coordinates") from error
    if not (hy == ay == by and hx == ax and abs(az-hz) == 2
            and bz == az and abs(bx-hx) == 1):
        raise AssertionError("rejoin floor source geometry mismatch")
    # V315: real Mojang dedicated-server run 40086218858 attempt 2
    # spawned CIHost at (-349.5,4,1013.5) under the actual loopback
    # max-world-size=1024. V312's arbitrary +/-1000 anchor rejected Z=1013
    # BEFORE any original stone/world or UUID/controller check. Compute a
    # conservative geometric guard from that ORIGINAL test-server border:
    # the 5x7 physical stone floor extends +/-2 X and +/-3 Z; at 1020
    # every floor/witness cell still lies inside +/-1023 (< 1024).
    # Owners, heights, signed native layout and real server fill+3 witness
    # checks remain strictly required with no fake game-world positions.
    if hy < 3 or hy > 252 or abs(hx) > 1020 or abs(hz) > 1020:
        raise AssertionError("rejoin floor outside bounded flat vanilla world")
    y = hy - 1
    fill = "fill {} {} {} {} {} {} stone".format(
        hx-2, y, hz-3, hx+2, y, hz+3)
    witness = ((hx, y, hz), (ax, y, az), (bx, y, bz))
    if len(set(witness)) != 3:
        raise AssertionError("rejoin floor real player support overlaps")
    return fill, witness


def actual_server_rejoin_floor_fill_receipt(fresh_log, expected_count=35):
    """Accept ONLY exact fresh vanilla server INFO acknowledgement for this floor.

    Vanilla can say 'No blocks filled' when the physical stone already exists.
    That reply is not a fixture PASS: genuine testforblock witnesses for all
    three real players must still follow, and original native V47 gates remain.
    """
    marker = b"[server thread/info]: "
    if expected_count not in (4, 35):
        raise AssertionError("unsupported original floor fill count")
    valid = (str(expected_count).encode("ascii") + b" blocks filled",
             b"no blocks filled")
    for line in fresh_log.lower().splitlines():
        if marker in line and line.split(marker, 1)[1].strip() in valid:
            return True
    return False


def actual_server_three_client_locked_fixture(server_log):
    """Native M413 simultaneous geometry plus real A range-expiration command."""
    host, a_near, b_outer, b_near = (
        actual_server_three_client_simultaneous_fixture(server_log))
    coords = a_near.split()
    if len(coords) != 7 or coords[:2] != ["tp", "CITarget"]:
        raise AssertionError("invalid genuine A target teleport")
    x, y, z = (int(v) for v in coords[2:5])
    # V307: a five-cell original teleport did not immediately establish
    # the mandatory >4.5D native A distance. The real B click could be
    # selected at A=4.273D and the strict acceptance correctly crashed.
    # Use a 12-cell server displacement, still in the 3-chunk view radius;
    # do NOT change the native >4.5D guard or the actual B/UUID checks.
    # V315: genuine 1024-border spawns may occur at x=1013; moving A
    # +12 would leave this original server. Keep EXACTLY 12 cells and
    # unchanged native >4.5D / UUID controller checks, choosing the inward
    # direction only when the positive side cannot fit with a 4-cell margin.
    # Reject invalid original source positions; do not clamp or invent a tp.
    if abs(x) > 1020 or abs(z) > 1020 or y < 2 or y > 252:
        raise AssertionError("original locked A far fixture outside server world")
    far_x = x + 12 if abs(x + 12) < 1020 else x - 12
    if abs(far_x) >= 1020 or abs(far_x - x) != 12:
        raise AssertionError("original locked A far fixture outside server world")
    a_far = "tp CITarget {} {} {} 180 0".format(far_x, y, z)
    return host, a_near, b_outer, b_near, a_far


def official_offline_uuid(name):
    """Java UUID.nameUUIDFromBytes('OfflinePlayer:' + actual username)."""
    import hashlib
    import uuid
    raw = bytearray(hashlib.md5(("OfflinePlayer:" + name).encode("utf-8")).digest())
    raw[6] = (raw[6] & 0x0F) | 0x30
    raw[8] = (raw[8] & 0x3F) | 0x80
    return str(uuid.UUID(bytes=bytes(raw)))


def v311_original_b_priority_source(log, source_offset):
    """Audit ONE genuine B-priority event from the original game's stdout.

    The V310 Java witness uses the very same native position/UUID/nearest
    read that authorized the B priority transition, not a later World read.
    Accept no fabricated fallback, stale run output or external source.
    """
    marker = b"CUSTOMMC_189_SIMULTANEOUS_B_PRIORITY_NATIVE_SOURCE_DIAG "
    if not isinstance(source_offset, int) or source_offset < 0:
        raise AssertionError("V311 native source offset invalid")
    with Path(log).open("rb") as original_log:
        if source_offset > original_log.seek(0, 2):
            raise AssertionError("V311 native source cursor outside original log")
        original_log.seek(source_offset)
        candidates = [line.rstrip(b"\r")
                      for line in original_log.read().splitlines()
                      if marker in line]
    if len(candidates) != 1:
        raise AssertionError(
            "V311 requires exactly one fresh original B-priority source "
            "witness, observed {}".format(len(candidates)))
    pattern = (rb"^CUSTOMMC_189_SIMULTANEOUS_B_PRIORITY_NATIVE_SOURCE_DIAG "
               rb"path=(observe|original-click) aIndex=(\d+) bIndex=(\d+) "
               rb"aDistance=(\d+(?:\.\d+)?) bDistance=(\d+(?:\.\d+)?) "
               rb"gapBCloser=(\d+(?:\.\d+)?)$")
    match = re.fullmatch(pattern, candidates[0])
    if match is None:
        raise AssertionError("V311 malformed or prefixed original source")
    a_index, b_index = int(match.group(2)), int(match.group(3))
    a_distance, b_distance, native_gap = (
        float(match.group(i)) for i in (4, 5, 6))
    if (a_index == b_index or a_index < 0 or b_index < 0
            or not all(math.isfinite(x)
                       for x in (a_distance, b_distance, native_gap))
            or not (1.0 <= a_distance <= 3.5)
            or not (1.0 <= b_distance <= 3.5)
            or native_gap <= 0.12
            or not math.isclose(native_gap, a_distance - b_distance,
                                rel_tol=0.0, abs_tol=1e-12)):
        raise AssertionError("V311 original native B-priority values invalid")
    return candidates[0].decode("ascii")


def v320_original_controller_argument_source(
        original_log, source_start, source_end):
    """Bounded genuine vanilla controller Entity-UUID provenance, no PASS.

    Correlates the current original host JVM before B's planned teleport.
    Absence means no observed native controller callback, NEVER proof
    that the server damage was / wasn't caused by a specific player.
    """
    if (any(isinstance(v, bool) or not isinstance(v, int)
            for v in (source_start, source_end))
            or source_start < 0 or source_end < source_start
            or source_end - source_start > 2_097_152):
        raise AssertionError("V320 original controller source cursor invalid")
    prefix = b"CUSTOMMC_189_SIMULTANEOUS_ORIGINAL_CONTROLLER_ARGUMENT_DIAG "
    with Path(original_log).open("rb") as stream:
        if source_end > stream.seek(0, 2):
            raise AssertionError("V320 original controller source past EOF")
        stream.seek(source_start)
        data = stream.read(source_end - source_start)
    # JVM stdout writes can be in progress: a nonterminated final line
    # provides no accepted original provenance.
    eol = data.rfind(b"\n")
    complete = data[:eol + 1] if eol >= 0 else b""
    lines = [line.rstrip(b"\r") for line in complete.splitlines()
             if line.startswith(prefix)]
    if len(lines) > 32:
        raise AssertionError("V320 original controller source exceeds native cap")
    evidence = []
    pattern = (rb"^CUSTOMMC_189_SIMULTANEOUS_ORIGINAL_CONTROLLER_ARGUMENT_DIAG "
               rb"event=([1-9]\d?) phase=([0-3]) "
               rb"firstOwnedClick=(true|false) nativeUuidAccess=(true|false) "
               rb"nativeUuid=(null|[a-f0-9-]{36}) "
               rb"owner=(A|B|OTHER|UNAVAILABLE)$")
    previous = 0
    a_uuid = official_offline_uuid("CITarget")
    b_uuid = official_offline_uuid("CITargetB")
    for line in lines:
        match = re.fullmatch(pattern, line)
        if match is None:
            raise AssertionError("V320 malformed original controller argument")
        number, phase, owned, has_uuid, raw_uuid, owner = match.groups()
        number = int(number)
        if number <= previous or number > 32:
            raise AssertionError("V320 original controller event order invalid")
        previous = number
        native = raw_uuid.decode("ascii")
        owner = owner.decode("ascii")
        access = has_uuid == b"true"
        if (not access and native != "null"
                or native == "null" and owner != "UNAVAILABLE"
                or native == a_uuid and owner != "A"
                or native == b_uuid and owner != "B"
                or native not in ("null", a_uuid, b_uuid)
                    and owner != "OTHER"
                or native != "null"
                    and not re.fullmatch(
                        r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-"
                        r"[0-9a-f]{4}-[0-9a-f]{12}", native)):
            raise AssertionError("V320 original controller UUID owner mismatch")
        evidence.append(line.decode("ascii"))
    return evidence


def v320_original_after_a_b_timeline(original_log, source_start, source_end):
    """Read actual post-A tick samples before original failed B=0 scoreboard.

    The only values reported were written by the genuine Mojang graphical JVM
    from its OWN source snapshots, not generated from server /tp coordinates.
    This never authorizes native B selection or server damage. A missing
    sample is negative diagnostic evidence, not a pass or a crash bypass.
    """
    marker = b"CUSTOMMC_189_SIMULTANEOUS_POST_A_ORIGINAL_TICK_SOURCE_DIAG "
    if (any(isinstance(v, bool) or not isinstance(v, int)
            for v in (source_start, source_end))
            or source_start < 0 or source_end < source_start
            or source_end - source_start > 2097152):
        raise AssertionError("V320 original post-A cursor invalid")
    with Path(original_log).open("rb") as source:
        size = source.seek(0, 2)
        if source_end > size:
            raise AssertionError("V320 original post-A cursor past source EOF")
        source.seek(source_start)
        raw = source.read(source_end - source_start)
    # A truncated first or last line cannot manufacture a native sample.
    if source_start:
        with Path(original_log).open("rb") as source:
            source.seek(source_start - 1)
            prior = source.read(1)
        if prior != b"\n":
            first = raw.find(b"\n")
            raw = raw[first + 1:] if first >= 0 else b""
    tail = raw.rfind(b"\n")
    raw = raw[:tail + 1] if tail >= 0 else b""
    matches = [line.rstrip(b"\r")
               for line in raw.splitlines() if line.startswith(marker)]
    if len(matches) > 20:
        raise AssertionError("V320 original post-A tick sample count invalid")
    for ordinal, line in enumerate(matches, 1):
        # Verify the exact native tick cadence and phase before printing.
        m = re.fullmatch(
            rb"CUSTOMMC_189_SIMULTANEOUS_POST_A_ORIGINAL_TICK_SOURCE_DIAG "
            rb"phase=1 ticksAfterA=(\d+) bothEligible=(true|false) "
            rb"Aindex=(-?\d+) Bindex=(-?\d+) "
            rb"A3D=(NaN|\d+(?:\.\d+)?) B3D=(NaN|\d+(?:\.\d+)?) "
            rb"sourceANearest=(true|false) sourceBNearest=(true|false) "
            rb"sameNativeReadReason=(.*)", line)
        if m is None or int(m.group(1)) != ordinal * 5:
            raise AssertionError("V320 invalid original source-phase tick")
    result = "CUSTOMMC_189_SIMULTANEOUS_POST_A_SOURCE_TIMELINE_DIAG count=" + str(len(matches))
    return result + (("\n" + "\n".join(m.decode("ascii") for m in matches))
                     if matches else "")


def v316_preteleport_original_b_priority_marker(
        original_log, source_start, source_end):
    """Observe only ALREADY EMITTED original B priority before the B /tp.

    A true V308 game run registered B=10 BEFORE the intended server B-nearer
    teleport. V310's same-native-read B priority witness was introduced only
    later, so that historical failure cannot supply its distances. This
    bounded, read-only time slice of a current original client log can.
    Neither missing nor present markers grant an attack or PASS.
    """
    marker = b"CUSTOMMC_189_SIMULTANEOUS_B_PRIORITY_NATIVE_SOURCE_DIAG "
    if (any(isinstance(n, bool) or not isinstance(n, int)
            for n in (source_start, source_end))
            or source_start < 0 or source_end < source_start
            or source_end - source_start > 2_097_152):
        raise AssertionError("V316 native pre-teleport cursor invalid")
    with Path(original_log).open("rb") as stream:
        if source_end > stream.seek(0, 2):
            raise AssertionError("V316 native pre-teleport cursor past log EOF")
        stream.seek(source_start)
        source = stream.read(source_end - source_start)
    # A concurrent JVM may be writing the last source line. Only inspect
    # newline-terminated complete original lines BEFORE our captured cursor.
    last_newline = source.rfind(b"\n")
    complete = source[:last_newline + 1] if last_newline >= 0 else b""
    witnesses = [line.rstrip(b"\r") for line in complete.splitlines()
                 if marker in line]
    prefix = "CUSTOMMC_189_SIMULTANEOUS_B_PRETELEPORT_SOURCE_DIAG "
    if not witnesses:
        return prefix + "completeMarker=ABSENT"
    if len(witnesses) != 1:
        raise AssertionError("V316 duplicate original pre-teleport marker")
    pattern = (rb"^CUSTOMMC_189_SIMULTANEOUS_B_PRIORITY_NATIVE_SOURCE_DIAG "
               rb"path=(observe|original-click) aIndex=(\d+) bIndex=(\d+) "
               rb"aDistance=(\d+(?:\.\d+)?) bDistance=(\d+(?:\.\d+)?) "
               rb"gapBCloser=(\d+(?:\.\d+)?)$")
    match = re.fullmatch(pattern, witnesses[0])
    if match is None:
        raise AssertionError("V316 malformed original pre-teleport source")
    ai, bi = (int(match.group(i)) for i in (2, 3))
    da, db, gap = (float(match.group(i)) for i in (4, 5, 6))
    if (ai == bi or min(ai, bi) < 0
            or not all(math.isfinite(v) for v in (da, db, gap))
            or not (1.0 <= da <= 3.5 and 1.0 <= db <= 3.5)
            or gap <= 0.12
            or not math.isclose(gap, da - db, rel_tol=0.0,
                                abs_tol=1e-12)):
        raise AssertionError("V316 invalid original pre-teleport distances")
    return prefix + "completeMarker=PRESENT " + witnesses[0].decode("ascii")


def v320_original_b_damage_source_window(log, source_start, source_end):
    """Only genuine, complete current-scenario JVM A-click/B-hurt evidence.

    Neither this original diagnostic nor absence of evidence can authorize
    an attack, change UUIDs, excuse early B damage or supply a test PASS.
    """
    prefixes = (
        b"CUSTOMMC_189_SIMULTANEOUS_FIRST_A_CLICK_SOURCE_DIAG ",
        b"CUSTOMMC_189_SIMULTANEOUS_B_HURT_NATIVE_SOURCE_DIAG ",
    )
    if (any(isinstance(n, bool) or not isinstance(n, int)
            for n in (source_start, source_end))
            or source_start < 0 or source_end < source_start
            or source_end - source_start > 2_097_152):
        raise AssertionError("V320 invalid original source window")
    original = Path(log)
    if original.is_symlink() or not original.is_file():
        raise AssertionError("V320 original JVM log unavailable")
    with original.open("rb") as stream:
        if source_end > stream.seek(0, 2):
            raise AssertionError("V320 original source end after EOF")
        stream.seek(source_start)
        raw = stream.read(source_end - source_start)
    # The log is written concurrently. Ignore incomplete final line and
    # any first line whose prefix began before this scenario's byte cursor.
    if source_start:
        with original.open("rb") as stream:
            stream.seek(source_start - 1)
            preceding = stream.read(1)
        if preceding != b"\n":
            raw = raw[raw.find(b"\n") + 1:] if b"\n" in raw else b""
    final = raw.rfind(b"\n")
    raw = raw[:final + 1] if final >= 0 else b""
    found = []
    for line in raw.splitlines():
        line = line.rstrip(b"\r")
        if any(line.startswith(prefix) for prefix in prefixes):
            if len(line) > 900 or b"PASS=YES" in line:
                raise AssertionError("V320 malformed original JVM source")
            found.append(line.decode("ascii"))
    if len(found) > 2 or len(set(x.split(" ", 1)[0] for x in found)) != len(found):
        raise AssertionError("V320 duplicate original JVM source marker")
    return tuple(found)


def v311_original_b_failure_evidence(log, source_offset):
    """A failed strict B=0 gate must remain failed while source is visible."""
    try:
        return v311_original_b_priority_source(log, source_offset)
    except (AssertionError, UnicodeError, OSError) as error:
        # This is negative diagnostic context ONLY. Never mark a bad source
        # as PASS or hide the independent server's original B-damage failure.
        return ("CUSTOMMC_189_SIMULTANEOUS_B_PRIORITY_SOURCE_UNAVAILABLE_DIAG "
                + str(error))


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


def v320_original_server_score_query(server, server_log, owner):
    """Read genuine server-owned damage scores after a FAILED original B=0.

    This only uses vanilla scoreboard players list. It neither edits scores
    nor provides permission for a player attack or a native test PASS.
    The independent earlier failed B=0 AssertionError is always re-raised.
    """
    if owner not in ("CIHost", "CITarget", "CITargetB"):
        raise ValueError("invalid V320 original server-score owner")
    source = Path(server_log)
    if source.is_symlink() or not source.is_file():
        raise OSError("V320 original server log unavailable")
    start = source.stat().st_size
    send_server_command(server, source, "scoreboard players list " + owner)
    with source.open("rb") as reader:
        end = reader.seek(0, 2)
        if end < start or end - start > 8192:
            raise AssertionError("V320 original server score query bounded source invalid")
        reader.seek(start)
        raw = reader.read(end - start)
    # A vanilla dedicated server writes its own INFO lines. No substring
    # can elevate this forensic material to an acceptance decision.
    lines = [line.strip() for line in raw.splitlines()
             if b"[server thread/info]:" in line.lower()]
    return " | ".join(line.decode("utf-8", "replace")
                      for line in lines[-8:])


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
        # V312 genuine spawn at -999.5 floors to -1000, and the real
        # 5x7 vanilla server floor extends to x=-1002. Keep this loopback
        # test world bounded while allowing its physical source witnesses.
        "max-world-size=1024\n"
        "enable-command-block=false\n"
    )
    path.write_text(content, encoding="ascii")


def original_locked_tp_server_reply(command):
    """Exact FRESH dedicated-server 1.8.9 reply for a real locked-target /tp.

    Three graphical clients use native integer World cells; vanilla /tp to
    integer X/Z centers at +0.5. Never accept a generic "Teleported" line,
    a different player, or an unrelated/stale earlier success as evidence.
    """
    parts = command.split()
    if (len(parts) != 7 or parts[0] != "tp"
            or parts[1] not in ("CIHost", "CITarget", "CITargetB")):
        raise AssertionError("locked UUID fixture tp source owner invalid")
    try:
        x, y, z = (int(value) for value in parts[2:5])
        yaw, pitch = (int(value) for value in parts[5:7])
    except ValueError as error:
        raise AssertionError("locked UUID tp requires integral source cells") from error
    if abs(x) >= 29999990 or abs(z) >= 29999990 or y < 1 or y > 255:
        raise AssertionError("locked UUID tp outside original vanilla World")
    if not -360 <= yaw <= 360 or not -90 <= pitch <= 90:
        raise AssertionError("locked UUID tp angle invalid")
    return ("Teleported {} to {:.1f}, {:.1f}, {:.1f}".format(
        parts[1], x + 0.5, float(y), z + 0.5)).encode("ascii")


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


def v348_latest_original_gui_screen(log, max_bytes=16777216):
    """Last COMPLETE source-original gameTick GUI transition in bounded log.

    This is a negative/physical-input eligibility witness only; it never
    represents original attack success or fakes a clear screen.
    """
    if (not isinstance(max_bytes, int) or isinstance(max_bytes, bool)
            or not 128 <= max_bytes <= 16777216):
        raise ValueError("V348 native GUI evidence bound invalid")
    source = Path(log)
    if source.is_symlink() or not source.is_file():
        return None
    marker = b"CUSTOMMC_189_ACCEPTANCE_SCREEN="
    with source.open("rb") as stream:
        size = stream.seek(0, 2)
        start = max(0, size - max_bytes)
        stream.seek(start)
        raw = stream.read(max_bytes)
    if start:
        first = raw.find(b"\n")
        raw = raw[first + 1:] if first >= 0 else b""
    last_complete = raw.rfind(b"\n")
    raw = raw[:last_complete + 1] if last_complete >= 0 else b""
    state = None
    for line in raw.splitlines():
        if line.startswith(marker):
            value = line[len(marker):].rstrip(b"\r")
            if 0 < len(value) <= 128:
                try:
                    state = value.decode("ascii", "strict")
                except UnicodeDecodeError:
                    state = None
    return state


def v348_resume_confirmed_original_pause(host, host_log, deadline,
                                          original_host_window):
    """Physically dismiss only original axp pause GUI in strict rejoin CI.

    The game itself must first report the pause state. Never send blind
    Escape: on a CLEAR screen it would CREATE the pause GUI. A fresh
    original gameTick CLEAR line after real XTest Escape is compulsory.
    No manual attack click or native leftClickCounter rewrite occurs.
    """
    if not (Path(host_log).exists() and host.poll() is None):
        raise AssertionError("V348 source host not alive for GUI recovery")
    timeout = min(deadline, time.monotonic() + 5.0)
    screen = None
    while time.monotonic() < timeout:
        check_process(host, "original GUI recovery source")
        screen = v348_latest_original_gui_screen(host_log)
        if screen == "axp" or screen == "(in-game/no GUI)":
            break
        time.sleep(0.1)
    if screen != "axp":
        # A verified CLEAR is already in-game; an unknown/ConnectScreen
        # never grants permission to send Escape or fabricate gameplay.
        return False

    # V349: LWJGL2 Minecraft 1.8.9 may omit _NET_WM_PID. Thus a late
    # process-ID-filtered X11 lookup is NOT native ownership proof and
    # cannot safely choose a window after CITarget/CITargetB launch.
    # Instead the original CIHost window ID was captured while it was
    # the ONLY Minecraft window in our isolated fresh Xvfb display,
    # before any target started. Revalidate that exact original ID is
    # still visible and named Minecraft; do not select from candidates.
    window = str(original_host_window)
    if not window.isdigit():
        raise AssertionError("V349 no pre-target original host window")
    visible = subprocess.run(
        ["xdotool", "search", "--onlyvisible", "--name", "Minecraft"],
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=4)
    if visible.returncode != 0:
        raise AssertionError("V349 original Minecraft windows missing")
    candidates = set(visible.stdout.decode("ascii", "strict").split())
    if window not in candidates:
        raise AssertionError("V349 captured original host window disappeared")
    title = subprocess.run(
        ["xdotool", "getwindowname", window],
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=4)
    if title.returncode != 0 or "Minecraft" not in title.stdout.decode(
            "utf-8", "replace"):
        raise AssertionError("V349 original host window title invalid")
    # No GUI status or native attack is inferred from X11 title/focus.
    if v348_latest_original_gui_screen(host_log) != "axp":
        return False
    offset = Path(host_log).stat().st_size
    send_real_host_key(host, window, "Escape")
    wait_for_new_original_source_marker(
        host, host_log, offset,
        b"CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)",
        min(deadline, time.monotonic() + 5.0),
        "real XTest Escape closed original axp pause")
    print("CUSTOMMC_189_V348_SOURCE_PAUSE_CLOSED_BY_REAL_ESCAPE=YES",
          flush=True)
    return True


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
        if args.scaffold_placement in ("enabled", "menu-open", "repeat-two", "production-repeat-two", "production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-two-keyboard-walk", "production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk"):
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
            if args.scaffold_placement in ("production-repeat-three", "production-repeat-three-history", "production-repeat-three-keyboard-walk"):
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireProductionScaffoldThreeBlockProof=true")
                if args.scaffold_placement == "production-repeat-three-keyboard-walk":
                    command.insert(command.index("-cp"),
                                   "-Dcustommc.acceptance.requireProductionScaffoldKeyboardWalkProof=true")
                if args.scaffold_placement == "production-repeat-three-history":
                    command.insert(command.index("-cp"),
                                   "-Dcustommc.acceptance.requireProductionScaffoldHistoryVeto=true")
            if args.scaffold_placement in ("production-repeat-four", "production-repeat-four-keyboard-walk"):
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireProductionScaffoldFourBlockProof=true")
                if args.scaffold_placement == "production-repeat-four-keyboard-walk":
                    command.insert(command.index("-cp"),
                                   "-Dcustommc.acceptance.requireProductionScaffoldKeyboardWalkProof=true")
            if args.scaffold_placement == "production-repeat-five-keyboard-walk":
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireProductionScaffoldFiveBlockProof=true")
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireProductionScaffoldKeyboardWalkProof=true")
            if args.scaffold_placement == "production-repeat-six-keyboard-walk":
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireProductionScaffoldSixBlockProof=true")
                command.insert(command.index("-cp"),
                               "-Dcustommc.acceptance.requireProductionScaffoldKeyboardWalkProof=true")
        else:
            command.insert(command.index("-cp"),
                           "-Dcustommc.acceptance.requireOriginalScaffoldDisabledControl=true")
    if remote and getattr(args, "scaffold_gui_abort", False):
        if args.scaffold_placement != "production-repeat-three":
            raise ValueError("V308 E abort needs original three-placement fixture")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireProductionScaffoldThreeGuiAbort=true")
    if remote and getattr(args, "scaffold_hotbar_abort", False):
        if args.scaffold_placement != "production-repeat-three":
            raise ValueError("V309 hotbar abort requires genuine three-block production")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.requireProductionScaffoldThreeHotbarAbort=true")
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
                       "-Dcustommc.acceptance.reportTriggerBotRealPvp=true"
                       if args.triggerbot_pvp else
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
    # Physically switch from selected slot 0 to slot 1 in V309 only.
    # Every other unreviewed key remains blocked from xdotool.
    if key not in ("e", "1", "2", "Escape"):
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
    if step not in (1, 2, 3, 4, 5, 6):
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


def latest_original_native_local_player_position(log):
    """Read unchanged source-native player movement logs from real runTick."""
    regex = re.compile(
        rb"CUSTOMMC_189_NATIVE_LOCAL_POS=x=(-?\d+(?:\.\d+)?)"
        rb" y=(-?\d+(?:\.\d+)?) z=(-?\d+(?:\.\d+)?)")
    matches = regex.findall(log_tail(log, 25000))
    if not matches:
        return None
    latest = tuple(float(v) for v in matches[-1])
    if not all(math.isfinite(v) for v in latest):
        raise AssertionError("nonfinite original runtime movement sample")
    return latest


def original_scaffold_native_edge_failure_evidence(
        log, fourth_source, expected_marker):
    """Failure-only evidence from real original source-mapped position logs.

    Never makes a native placement PASS decision and never reads or edits
    Minecraft World/ItemBlock/controller state. The fourth source is the
    genuine verified step=4 controller position, not an inferred waypoint.
    """
    expression = re.compile(
        rb"CUSTOMMC_189_NATIVE_LOCAL_POS=x=(-?\d+(?:\.\d+)?)"
        rb" y=(-?\d+(?:\.\d+)?) z=(-?\d+(?:\.\d+)?)")
    recent = log_tail(log, 25000)
    observed = [
        tuple(float(v) for v in row)
        for row in expression.findall(recent)[-8:]]
    if not observed:
        return (" D=" + repr(fourth_source)
                + " nativeSamples=0 nativeLast=unavailable"
                + " step5MarkerInTail=" + str(expected_marker in recent))
    if not all(math.isfinite(v) for point in observed for v in point):
        raise AssertionError("nonfinite native Scaffold stall evidence")
    last = observed[-1]
    drift = last[0] - fourth_source[0]
    span = tuple(
        max(point[axis] for point in observed)
        - min(point[axis] for point in observed)
        for axis in range(3))
    return (" D=" + repr(fourth_source)
            + " nativeSamples=" + str(len(observed))
            + " nativeLast=" + repr(last)
            + " nativeDeltaX=" + repr(drift)
            + " lastNativeSpanXYZ=" + repr(span)
            + " step5MarkerInTail=" + str(expected_marker in recent))


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
    # LWJGL2 consumes genuine focused X11 keyboard-device events. Do NOT
    # supply xdotool --window: it switches to XSendEvent, which legacy
    # XInput pollers may ignore. Use the same XTest path as the already
    # certified real '1', 'e' and Escape key acceptance controls.
    subprocess.run(["xdotool", "windowactivate", "--sync", str(host_window)],
                   check=True, timeout=6, stdout=subprocess.DEVNULL)
    focused = subprocess.check_output(["xdotool", "getactivewindow"],
                                      timeout=4).decode("ascii").strip()
    if int(focused) != int(host_window):
        raise AssertionError("original host window not actually active for movement")
    subprocess.run(["xdotool", "keydown" if pressed else "keyup", "s"],
                   check=True, timeout=6, stdout=subprocess.DEVNULL)
    print("CUSTOMMC_189_REAL_HOST_S_BACKWARD_{}=YES".format(
          "PRESSED" if pressed else "RELEASED"), flush=True)


def set_original_host_sneak_key(host, host_window, pressed):
    """Use real XTEST left Shift to keep native player on the ledge.

    This is ordinary vanilla player input. The World ray, selected
    ItemBlock, onGround, and actual controller click remain authoritative.
    """
    check_process(host, "original native sneak key")
    subprocess.run(["xdotool", "windowactivate", "--sync", str(host_window)],
                   check=True, timeout=6, stdout=subprocess.DEVNULL)
    focused = subprocess.check_output(["xdotool", "getactivewindow"],
                                      timeout=4).decode("ascii").strip()
    if int(focused) != int(host_window):
        raise AssertionError("original Minecraft X11 focus changed before sneak")
    subprocess.run(["xdotool", "keydown" if pressed else "keyup", "Shift_L"],
                   check=True, timeout=6, stdout=subprocess.DEVNULL)
    print("CUSTOMMC_189_REAL_HOST_SHIFT_{}=YES".format(
          "PRESSED" if pressed else "RELEASED"), flush=True)


def move_original_host_mouse_up_for_edge(host, host_window, pixels):
    """One bounded REAL XTEST pointer motion in focused original Minecraft.

    Original vanilla sensitivity converts mouse delta to pitch. This does
    not write camera fields, raytrace hits, blocks, players or game packets.
    """
    if not 1 <= pixels <= 90:
        raise ValueError("bounded native mouse adjustment 1..90 pixels only")
    check_process(host, "original side-face physical mouse input")
    subprocess.run(["xdotool", "windowactivate", "--sync", str(host_window)],
                   check=True, timeout=6, stdout=subprocess.DEVNULL)
    focused = subprocess.check_output(["xdotool", "getactivewindow"],
                                      timeout=4).decode("ascii").strip()
    if int(focused) != int(host_window):
        raise AssertionError("original Minecraft not focused for real mouse aim")
    # Vanilla 1.8.9 default mouse sensitivity approximately 0.15 deg/px.
    # -55px aims from an original 88.6-degree almost-downward ray to
    # about 80 degrees, at the visible stone's EAST side from the edge.
    subprocess.run(["xdotool", "mousemove_relative", "--", "0", str(-pixels)],
                   check=True, timeout=6, stdout=subprocess.DEVNULL)
    print("CUSTOMMC_189_ORIGINAL_XTEST_MOUSE_UP_FOR_EDGE={}PX".format(
          pixels), flush=True)


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

        if args.rejoin_target or args.rejoin_release:
            # V347: the original FLAT vanilla server may choose a fresh
            # login spawn beyond the deliberately restricted 1024-cell
            # rejoin-world fixture. Set ONLY these rejoin scenarios' real
            # world spawn before any player connects, using an actual
            # acknowledged dedicated-server 1.8.9 console command.
            # The later CIHost login coordinates are STILL parsed from
            # original server.log, and all native player/UUID/ray, 35
            # physical stone and server damage gates remain unchanged.
            send_server_command(server, server_log,
                                "setworldspawn 0 4 0",
                                expected=b"Set the world spawn point to (0, 4, 0)")
            print("CUSTOMMC_189_V347_REJOIN_NATIVE_WORLDSPAWN_ACK=YES",
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
        if args.scaffold_placement in ("enabled", "menu-open", "repeat-two", "production-repeat-two", "production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-two-keyboard-walk", "production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk"):
            # Unique real Minecraft client on this freshly allocated Xvfb
            # display; later target windows cannot be mistaken for CIHost.
            host_window = identify_original_host_window_before_target(host)
        if (args.rejoin_target or args.rejoin_release
                or args.locked_target or args.simultaneous
                or args.multi_target):
            # Capture host only while it remains the *sole* Minecraft
            # window, not by ambiguous late --pid once targets exist.
            # V366 R8: sticky, simultaneous and multi-target fixtures use
            # only source-confirmed axp with this pre-target original window.
            if host_window is not None:
                raise AssertionError(
                    "V349 host window already captured by another fixture")
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

        if args.rejoin_target or args.rejoin_release:
            # V348 original 'awz -> CLEAR -> axp' was captured during
            # genuine rejoin acceptance. Only the latest original axp
            # transition authorizes a physical, host-window-owned Escape.
            # This occurs BEFORE rejoin PvP baseline/damage assertions.
            v348_resume_confirmed_original_pause(
                host, host_log, deadline, host_window)

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
            fifth_target_xyz = (x+5, y-1, z)
            sixth_target_xyz = (x+6, y-1, z)
            seventh_target_xyz = (x+7, y-1, z)
            send_server_command(server, server_log, far_target_tp)
            send_server_command(server, server_log,
                                "setblock {} {} {} stone".format(*support))
            send_server_command(server, server_log,
                                "setblock {} {} {} air".format(*target_xyz))
            if args.scaffold_placement in ("repeat-two", "production-repeat-two", "production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-two-keyboard-walk", "production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk"):
                send_server_command(server, server_log,
                                    "setblock {} {} {} air".format(*second_target_xyz))
                if args.scaffold_placement in ("production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk"):
                    send_server_command(server, server_log,
                                        "setblock {} {} {} air".format(*third_target_xyz))
                if args.scaffold_placement in ("production-repeat-four", "production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk"):
                    send_server_command(server, server_log,
                                        "setblock {} {} {} air".format(*fourth_target_xyz))
                if args.scaffold_placement in (
                        "production-repeat-five-keyboard-walk",
                        "production-repeat-six-keyboard-walk"):
                    send_server_command(server, server_log,
                                        "setblock {} {} {} air".format(*fifth_target_xyz))
                    send_server_command(server, server_log,
                                        "setblock {} {} {} air".format(*sixth_target_xyz))
                if args.scaffold_placement == "production-repeat-six-keyboard-walk":
                    send_server_command(server, server_log,
                                        "setblock {} {} {} air".format(*seventh_target_xyz))
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
                "production-repeat-two", "production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-two-keyboard-walk", "production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk")
            six_keyboard_walk = args.scaffold_placement == "production-repeat-six-keyboard-walk"
            five_keyboard_walk = args.scaffold_placement in (
                "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk")
            four_keyboard_walk = args.scaffold_placement in (
                "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk",
                "production-repeat-six-keyboard-walk")
            four_production = args.scaffold_placement in (
                "production-repeat-four", "production-repeat-four-keyboard-walk",
                "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk")
            three_production = args.scaffold_placement in (
                "production-repeat-three", "production-repeat-three-history", "production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk") or four_production
            history_negative = args.scaffold_placement == "production-repeat-three-history"
            three_keyboard_walk = args.scaffold_placement in ("production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk")
            keyboard_walk = args.scaffold_placement in (
                "production-repeat-two-keyboard-walk", "production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk")
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
            if args.scaffold_placement in ("enabled", "repeat-two", "production-repeat-two", "production-repeat-two-keyboard-walk", "production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk"):
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
            if args.scaffold_placement in ("repeat-two", "production-repeat-two", "production-repeat-three", "production-repeat-three-history", "production-repeat-four", "production-repeat-two-keyboard-walk", "production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk"):
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

                if getattr(args, "scaffold_gui_abort", False):
                    # Genuine first original controller click, WorldClient receipt,
                    # dedicated server stone and 7 Survival items already proven.
                    # Before another click, physically press E in the owned X11 game.
                    if repeat_attempt + b" step=2" in log_tail(host_log):
                        raise AssertionError("second Scaffold click before real E")
                    send_real_host_key(host, host_window, "e")
                    wait_for(host, host_log,
                             b"CUSTOMMC_189_SCAFFOLD_THREE_REAL_GUI_ABORT_AFTER_ONE_PASS=YES state=OPEN",
                             deadline, "genuine post-receipt inventory E veto")
                    for target in (second_target_xyz, third_target_xyz):
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*target),
                                            expected=b"successfully found the block")
                    send_server_command(server, server_log, "clear CIHost stone 0 0",
                                        expected=b"has 7 items that match the criteria")
                    # Resume with physical Escape, verify 60+120 actual GUI-clear
                    # native ticks in two genuine server-owned player positions.
                    send_real_host_key(host, host_window, "Escape")
                    send_server_command(server, server_log,
                                        "tp CIHost {:.2f} {} {:.2f} 90 88.6".format(
                                            x+2.05, y, z+.5))
                    wait_for(host, host_log,
                             b"CUSTOMMC_189_SCAFFOLD_THREE_GUI_RESUMED_DISABLED_TICKS=60",
                             deadline, "60 native disabled GUI-clear ticks at B")
                    send_server_command(server, server_log,
                                        "testforblock {} {} {} air".format(*second_target_xyz),
                                        expected=b"successfully found the block")
                    # Separate genuine support stone to the east of target C.
                    send_server_command(server, server_log,
                                        "setblock {} {} {} stone".format(x+4, y-1, z))
                    send_server_command(server, server_log,
                                        "tp CIHost {:.2f} {} {:.2f} 270 85".format(
                                            x+3.85, y, z+.5))
                    wait_for(host, host_log,
                             b"CUSTOMMC_189_SCAFFOLD_THREE_GUI_RESUMED_DISABLED_TICKS=120",
                             deadline, "120 native disabled GUI-clear ticks at C")
                    if log_tail(host_log, 200000).count(repeat_attempt) != 1:
                        raise AssertionError("GUI veto allowed extra original controller")
                    if log_tail(host_log, 200000).count(repeat_receipt) != 1:
                        raise AssertionError("GUI veto allowed extra original receipt")
                    for target in (second_target_xyz, third_target_xyz):
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*target),
                                            expected=b"successfully found the block")
                    send_server_command(server, server_log, "clear CIHost stone 0 0",
                                        expected=b"has 7 items that match the criteria")
                    print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_THREE_REAL_E_ABORT_"
                          "RESUME_SERVER_ONE_STONE_SEVEN_PASS=YES", flush=True)
                    return

                if getattr(args, "scaffold_hotbar_abort", False):
                    # Original click A, original WorldClient 8->7 receipt and
                    # independent vanilla dedicated-server stone/7 items are
                    # already confirmed. Press REAL host hotbar '2', not a
                    # native inventory setter or synthetic Minecraft click.
                    if repeat_attempt + b" step=2" in log_tail(host_log):
                        raise AssertionError("second original click before real hotbar 2")
                    send_real_host_key(host, host_window, "2")
                    wait_for(host, host_log,
                             b"CUSTOMMC_189_SCAFFOLD_THREE_REAL_HOTBAR_ABORT_AFTER_ONE_PASS=YES slot=1",
                             deadline, "native slot 1 invalidated live repeat lease")
                    for target in (second_target_xyz, third_target_xyz):
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*target),
                                            expected=b"successfully found the block")
                    send_server_command(server, server_log, "clear CIHost stone 0 0",
                                        expected=b"has 7 items that match the criteria")
                    # Re-select original stone with REAL X11 key 1; same module
                    # activation must stay stopped during two different
                    # genuine server-owned placements' opportunities.
                    send_real_host_key(host, host_window, "1")
                    send_server_command(server, server_log,
                                        "tp CIHost {:.2f} {} {:.2f} 90 88.6".format(
                                            x+2.05, y, z+.5))
                    wait_for(host, host_log,
                             b"CUSTOMMC_189_SCAFFOLD_THREE_HOTBAR_RESUMED_VETO_TICKS=60",
                             deadline, "60 original GUI-clear veto ticks at B")
                    send_server_command(server, server_log,
                                        "testforblock {} {} {} air".format(*second_target_xyz),
                                        expected=b"successfully found the block")
                    send_server_command(server, server_log,
                                        "setblock {} {} {} stone".format(x+4, y-1, z))
                    send_server_command(server, server_log,
                                        "tp CIHost {:.2f} {} {:.2f} 270 85".format(
                                            x+3.85, y, z+.5))
                    wait_for(host, host_log,
                             b"CUSTOMMC_189_SCAFFOLD_THREE_HOTBAR_RESUMED_VETO_TICKS=120",
                             deadline, "120 original GUI-clear veto ticks at C")
                    if log_tail(host_log, 200000).count(repeat_attempt) != 1:
                        raise AssertionError("reselected hotbar issued extra original clicks")
                    if log_tail(host_log, 200000).count(repeat_receipt) != 1:
                        raise AssertionError("reselected hotbar issued extra readbacks")
                    for target in (second_target_xyz, third_target_xyz):
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*target),
                                            expected=b"successfully found the block")
                    send_server_command(server, server_log, "clear CIHost stone 0 0",
                                        expected=b"has 7 items that match the criteria")
                    print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_THREE_REAL_HOTBAR_"
                          "RESELECT_SERVER_ONE_STONE_SEVEN_PASS=YES", flush=True)
                    return

                if keyboard_walk:
                    # Immutable server command boundary. V64 must not hide
                    # fixture teleporting between distinct claimed keyboard
                    # placements. These are REAL server log acknowledgements.
                    initial_host_teleports = server_log.read_bytes().count(
                        b"Teleported CIHost to")
                    if initial_host_teleports < 1:
                        raise AssertionError(
                            "initial genuine server host teleport not logged")
                    # V60: EXACTLY ONE initial server tp to the starting
                    # original edge. No server TP after first receipt.
                    # Original human-input S key must move the actual player
                    # over newly placed A until the native cursor sees B.
                    # All other V43/V54/V58/V59 cases retain server TP.
                    first_source = source_native_scaffold_position(host_log, 1)
                    # V60 R3: R1 S-only overshot and fell; R2 stopping
                    # at +0.82 still coasted off the one-stone bridge.
                    # A human can hold the ORIGINAL sneak key while
                    # walking backward to the ledge: vanilla collision
                    # keeps feet on the genuine server-owned support.
                    # Keep both keys physically held only until original
                    # controller B has a real receipt, with an independent
                    # 9-second motion deadline and fail-closed position.
                    sneak_held = False
                    backward_held = False
                    source_after_input = None
                    try:
                        set_original_host_sneak_key(host, host_window, True)
                        sneak_held = True
                        set_original_host_backward_key(host, host_window, True)
                        backward_held = True
                        keyboard_deadline = min(deadline, time.monotonic() + 9.0)
                        # FIRST: walk on the original A stone with the real
                        # physical SHIFT+S keys and unchanged nearly-downward
                        # vanilla camera. Keep the genuine negative gate:
                        # NO second click while the native cursor still
                        # points at lower AIR/MISS instead of A's side.
                        at_real_edge = False
                        while time.monotonic() < keyboard_deadline:
                            current = latest_original_native_local_player_position(
                                host_log)
                            if current is not None:
                                if (abs(current[2] - first_source[2]) > .65
                                        or current[1] < first_source[1] - .42
                                        or current[0] < first_source[0] - .25
                                        or current[0] > first_source[0] + 1.40):
                                    raise AssertionError(
                                        "original sneaking traversal left real"
                                        " supported edge first={} current={}".format(
                                            first_source, current))
                                source_after_input = current
                                if current[0] >= first_source[0] + 1.16:
                                    at_real_edge = True
                                    break
                            if second_attempt in log_tail(host_log):
                                raise AssertionError(
                                    "original near-vertical ray unexpectedly"
                                    " placed B before actual physical mouse aim")
                            time.sleep(.035)
                        if not at_real_edge:
                            raise AssertionError(
                                "original sneak input did not reach supported"
                                " edge before camera action first={} last={}".format(
                                    first_source, source_after_input))
                        if second_attempt in log_tail(host_log):
                            raise AssertionError(
                                "original B was admitted before new side-ray")
                        print("CUSTOMMC_189_ORIGINAL_SNEAK_EDGE_READY_BEFORE_MOUSE=YES"
                              " nativePosition={}".format(source_after_input),
                              flush=True)
                        # SECOND: change ONLY the physical XTEST mouse pitch,
                        # then wait for original World/cursor/item/ground gates
                        # to authorize the REAL B controller themselves.
                        # This is neither a rotation-field setter nor an
                        # invented MovingObjectPosition or packet.
                        move_original_host_mouse_up_for_edge(
                            host, host_window, 55)
                        while time.monotonic() < keyboard_deadline:
                            current = latest_original_native_local_player_position(
                                host_log)
                            if current is not None:
                                if (abs(current[2] - first_source[2]) > .65
                                        or current[1] < first_source[1] - .42
                                        or current[0] > first_source[0] + 1.40):
                                    raise AssertionError(
                                        "original mouse-aimed sneaking motion"
                                        " lost support first={} current={}".format(
                                            first_source, current))
                                source_after_input = current
                            if second_attempt in log_tail(host_log):
                                break
                            time.sleep(.035)
                        else:
                            raise AssertionError(
                                "original side-aimed controller B missing"
                                " within 9-second physical input deadline"
                                " first={} last={}".format(
                                    first_source, source_after_input))
                    finally:
                        # Even an XTEST S key-release exception must never
                        # leave vanilla Shift held in the original window.
                        try:
                            if backward_held:
                                set_original_host_backward_key(
                                    host, host_window, False)
                        finally:
                            if sneak_held:
                                set_original_host_sneak_key(
                                    host, host_window, False)
                    wait_for(host, host_log, repeat_receipt + b" step=2",
                             deadline,
                             "original second WorldClient receipt after"
                             " source-native sneaking bridge")
                    second_source = source_native_scaffold_position(host_log, 2)
                    # The original Java MovementStateAccess must itself
                    # confirm real sneaking and grounded state at the EXACT
                    # original second controller click, not merely that
                    # xdotool claimed to press physical Shift.
                    native_second = re.compile(
                        rb"CUSTOMMC_OFFICIAL_189_SCAFFOLD_KEYBOARD_WALK_"
                        rb"ORIGINAL_SOURCE_POSITION step=2 "
                        rb"x=(-?\d+(?:\.\d+)?) y=(-?\d+(?:\.\d+)?) "
                        rb"z=(-?\d+(?:\.\d+)?) onGround=true sneaking=true")
                    if len(native_second.findall(log_tail(host_log, 200000))) != 1:
                        raise AssertionError(
                            "source-native second controller was not grounded"
                            " and sneaking despite physical held Shift+S")
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
                    if not three_keyboard_walk:
                        # The older three/four placement fixtures deliberately
                        # exercise separate source-native edge positions using TP.
                        send_server_command(server, server_log,
                                            "tp CIHost {:.2f} {} {:.2f} 90 88.6".format(
                                                x+3.05, y, z+.5))
                    else:
                        # V64: no server teleport after initial spawn. Start
                        # another physical Shift+S leg from the B controller
                        # coordinate; the original cursor/right-click and
                        # dedicated server still exclusively authorize C.
                        second_source = source_native_scaffold_position(host_log, 2)
                        third_sneak = third_backward = False
                        try:
                            set_original_host_sneak_key(host, host_window, True)
                            third_sneak = True
                            set_original_host_backward_key(host, host_window, True)
                            third_backward = True
                            third_deadline = min(deadline, time.monotonic() + 9.0)
                            third_expected = (repeat_attempt
                                + b" step=3 accepted=true target="
                                + "{},{},{}".format(*third_target_xyz).encode())
                            while time.monotonic() < third_deadline:
                                actual = latest_original_native_local_player_position(
                                    host_log)
                                if actual is not None and (
                                        abs(actual[2] - second_source[2]) > .65
                                        or actual[1] < second_source[1] - .42
                                        or actual[0] > second_source[0] + 1.80):
                                    raise AssertionError(
                                        "physical B-to-C traversal left supported"
                                        " Minecraft edge B={} now={}".format(
                                            second_source, actual))
                                if third_expected in log_tail(host_log):
                                    break
                                time.sleep(.035)
                            else:
                                raise AssertionError(
                                    "no original third controller click during"
                                    " physical B-to-C input")
                        finally:
                            try:
                                if third_backward:
                                    set_original_host_backward_key(
                                        host, host_window, False)
                            finally:
                                if third_sneak:
                                    set_original_host_sneak_key(
                                        host, host_window, False)
                    third_attempt = (repeat_attempt
                        + b" step=3 accepted=true target="
                        + "{},{},{}".format(*third_target_xyz).encode())
                    wait_for(host, host_log, third_attempt, deadline,
                             "third genuine vanilla click at fresh block and stack six")
                    wait_for(host, host_log, repeat_receipt + b" step=3", deadline,
                             "third original WorldClient stone and 6-to-5 receipt")
                    if three_keyboard_walk:
                        third_source = source_native_scaffold_position(host_log, 3)
                        native_third = re.compile(
                            rb"CUSTOMMC_OFFICIAL_189_SCAFFOLD_KEYBOARD_WALK_"
                            rb"ORIGINAL_SOURCE_POSITION step=3 "
                            rb"x=(-?\d+(?:\.\d+)?) y=(-?\d+(?:\.\d+)?) "
                            rb"z=(-?\d+(?:\.\d+)?) onGround=true sneaking=true")
                        if len(native_third.findall(log_tail(host_log, 200000))) != 1:
                            raise AssertionError(
                                "third original controller not grounded+sneaking")
                        if not (second_source[0] + .45 <= third_source[0]
                                <= second_source[0] + 1.8
                                and abs(third_source[1] - second_source[1]) <= .35
                                and abs(third_source[2] - second_source[2]) <= .55):
                            raise AssertionError(
                                "third original source did not physically advance"
                                " B={} C={}".format(second_source, third_source))
                        print(
                            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_THREE_REAL_KEYBOARD_LEGS_PASS=YES"
                            " nativeXFirst={} nativeXSecond={} nativeXThird={}".format(
                                first_source[0], second_source[0], third_source[0]),
                            flush=True)
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
                        if not four_keyboard_walk:
                            send_server_command(server, server_log,
                                                "tp CIHost {:.2f} {} {:.2f} 90 88.6".format(
                                                    x+4.05, y, z+.5))
                        else:
                            # V66 actual Shift+S motion from third original
                            # native click source to fourth; never a server TP.
                            third_source = source_native_scaffold_position(host_log, 3)
                            fourth_sneak = fourth_backward = False
                            try:
                                set_original_host_sneak_key(host, host_window, True)
                                fourth_sneak = True
                                set_original_host_backward_key(host, host_window, True)
                                fourth_backward = True
                                fourth_deadline = min(deadline, time.monotonic() + 9.0)
                                fourth_expected = (repeat_attempt
                                    + b" step=4 accepted=true target="
                                    + "{},{},{}".format(*fourth_target_xyz).encode())
                                while time.monotonic() < fourth_deadline:
                                    actual = latest_original_native_local_player_position(
                                        host_log)
                                    if actual is not None and (
                                            abs(actual[2] - third_source[2]) > .65
                                            or actual[1] < third_source[1] - .42
                                            or actual[0] > third_source[0] + 1.80):
                                        raise AssertionError(
                                            "physical C-to-D traversal lost native"
                                            " support C={} now={}".format(
                                                third_source, actual))
                                    if fourth_expected in log_tail(host_log):
                                        break
                                    time.sleep(.035)
                                else:
                                    raise AssertionError(
                                        "no original fourth controller click"
                                        " during physical C-to-D input")
                            finally:
                                try:
                                    if fourth_backward:
                                        set_original_host_backward_key(
                                            host, host_window, False)
                                finally:
                                    if fourth_sneak:
                                        set_original_host_sneak_key(
                                            host, host_window, False)
                        fourth_attempt = (repeat_attempt
                            + b" step=4 accepted=true target="
                            + "{},{},{}".format(*fourth_target_xyz).encode())
                        wait_for(host, host_log, fourth_attempt, deadline,
                                 "fourth original controller click at fresh distinct D")
                        wait_for(host, host_log, repeat_receipt + b" step=4", deadline,
                                 "fourth original WorldClient stone and 5-to-4 debit")
                        if not five_keyboard_walk:
                            wait_for(host, host_log,
                                     b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_"
                                     b"FOUR_CLIENT_RECEIPTS_PASS=YES attempts=4",
                                     deadline, "four positive original receipts and native clicks")
                        if four_keyboard_walk:
                            fourth_source = source_native_scaffold_position(host_log, 4)
                            native_fourth = re.compile(
                                rb"CUSTOMMC_OFFICIAL_189_SCAFFOLD_KEYBOARD_WALK_"
                                rb"ORIGINAL_SOURCE_POSITION step=4 "
                                rb"x=(-?\d+(?:\.\d+)?) y=(-?\d+(?:\.\d+)?) "
                                rb"z=(-?\d+(?:\.\d+)?) onGround=true sneaking=true")
                            if len(native_fourth.findall(log_tail(host_log, 200000))) != 1:
                                raise AssertionError("fourth native controller not grounded+sneaking")
                            if not (third_source[0] + .45 <= fourth_source[0]
                                    <= third_source[0] + 1.80
                                    and abs(fourth_source[1] - third_source[1]) <= .35
                                    and abs(fourth_source[2] - third_source[2]) <= .55):
                                raise AssertionError(
                                    "fourth original source didn't move via keys"
                                    " C={} D={}".format(third_source, fourth_source))
                            print(
                                "CUSTOMMC_OFFICIAL_189_SCAFFOLD_FOUR_REAL_KEYBOARD_LEGS_PASS=YES"
                                " A={} B={} C={} D={}".format(
                                    first_source[0], second_source[0],
                                    third_source[0], fourth_source[0]),
                                flush=True)
                        expected_attempts = 4
                        expected_targets = (
                            target_xyz, second_target_xyz,
                            third_target_xyz, fourth_target_xyz)
                        expected_items = b"has 4 items that match the criteria"
                        if five_keyboard_walk:
                            # V67 original Survival source A-D must be confirmed
                            # before holding the next real Shift+S leg for E.
                            for target in (target_xyz, second_target_xyz,
                                           third_target_xyz, fourth_target_xyz):
                                send_server_command(server, server_log,
                                                    "testforblock {} {} {} stone".format(*target),
                                                    expected=b"successfully found the block")
                            send_server_command(server, server_log,
                                                "testforblock {} {} {} air".format(*fifth_target_xyz),
                                                expected=b"successfully found the block")
                            send_server_command(server, server_log,
                                                "clear CIHost stone 0 0",
                                                expected=b"has 4 items that match the criteria")
                            fourth_source = source_native_scaffold_position(host_log, 4)
                            fifth_sneak = fifth_backward = False
                            try:
                                set_original_host_sneak_key(host, host_window, True)
                                fifth_sneak = True
                                set_original_host_backward_key(host, host_window, True)
                                fifth_backward = True
                                fifth_deadline = min(deadline, time.monotonic() + 9.0)
                                fifth_expected = (repeat_attempt
                                    + b" step=5 accepted=true target="
                                    + "{},{},{}".format(*fifth_target_xyz).encode())
                                while time.monotonic() < fifth_deadline:
                                    actual = latest_original_native_local_player_position(host_log)
                                    if actual is not None and (
                                            abs(actual[2] - fourth_source[2]) > .65
                                            or actual[1] < fourth_source[1] - .42
                                            or actual[0] > fourth_source[0] + 1.80):
                                        raise AssertionError(
                                            "real D-to-E keyboard walk lost native support"
                                            " D={} current={}".format(fourth_source, actual))
                                    if fifth_expected in log_tail(host_log):
                                        break
                                    time.sleep(.035)
                                else:
                                    raise AssertionError(
                                        "fifth original controller did not click"
                                        " during physical D-to-E traversal"
                                        + original_scaffold_native_edge_failure_evidence(
                                            host_log, fourth_source,
                                            fifth_expected))
                            finally:
                                try:
                                    if fifth_backward:
                                        set_original_host_backward_key(host, host_window, False)
                                finally:
                                    if fifth_sneak:
                                        set_original_host_sneak_key(host, host_window, False)
                            wait_for(host, host_log, fifth_expected, deadline,
                                     "fifth genuine original controller click at E")
                            wait_for(host, host_log, repeat_receipt + b" step=5", deadline,
                                     "fifth original WorldClient+inventory receipt 4-to-3")
                            if not six_keyboard_walk:
                                wait_for(host, host_log,
                                         b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_"
                                         b"FIVE_CLIENT_RECEIPTS_PASS=YES attempts=5",
                                         deadline, "five original controller and receipt pairs")
                            fifth_source = source_native_scaffold_position(host_log, 5)
                            fifth_native = re.compile(
                                rb"CUSTOMMC_OFFICIAL_189_SCAFFOLD_KEYBOARD_WALK_"
                                rb"ORIGINAL_SOURCE_POSITION step=5 "
                                rb"x=(-?\d+(?:\.\d+)?) y=(-?\d+(?:\.\d+)?) "
                                rb"z=(-?\d+(?:\.\d+)?) onGround=true sneaking=true")
                            if len(fifth_native.findall(log_tail(host_log, 200000))) != 1:
                                raise AssertionError("fifth original controller not grounded+sneaking")
                            if not (fourth_source[0] + .45 <= fifth_source[0]
                                    <= fourth_source[0] + 1.80
                                    and abs(fifth_source[1] - fourth_source[1]) <= .35
                                    and abs(fifth_source[2] - fourth_source[2]) <= .55):
                                raise AssertionError(
                                    "fifth controller source failed actual keyboard advance"
                                    " D={} E={}".format(fourth_source, fifth_source))
                            expected_attempts = 5
                            expected_targets = (
                                target_xyz, second_target_xyz, third_target_xyz,
                                fourth_target_xyz, fifth_target_xyz)
                            expected_items = b"has 3 items that match the criteria"
                            print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_FIVE_REAL_KEYBOARD_LEGS_PASS=YES"
                                  " A={} B={} C={} D={} E={}".format(
                                      first_source[0], second_source[0],
                                      third_source[0], fourth_source[0],
                                      fifth_source[0]), flush=True)
                            if six_keyboard_walk:
                                # V68 physical E->F without server teleport.
                                # Server first witnesses five distinct native
                                # STONE receipts and exactly three items.
                                for target in (target_xyz, second_target_xyz,
                                               third_target_xyz, fourth_target_xyz,
                                               fifth_target_xyz):
                                    send_server_command(server, server_log,
                                                        "testforblock {} {} {} stone".format(*target),
                                                        expected=b"successfully found the block")
                                send_server_command(server, server_log,
                                                    "testforblock {} {} {} air".format(*sixth_target_xyz),
                                                    expected=b"successfully found the block")
                                send_server_command(server, server_log,
                                                    "clear CIHost stone 0 0",
                                                    expected=b"has 3 items that match the criteria")
                                fifth_source = source_native_scaffold_position(host_log, 5)
                                sixth_sneak = sixth_backward = False
                                try:
                                    set_original_host_sneak_key(host, host_window, True)
                                    sixth_sneak = True
                                    set_original_host_backward_key(host, host_window, True)
                                    sixth_backward = True
                                    sixth_deadline = min(deadline, time.monotonic() + 9.0)
                                    sixth_expected = (repeat_attempt
                                        + b" step=6 accepted=true target="
                                        + "{},{},{}".format(*sixth_target_xyz).encode())
                                    while time.monotonic() < sixth_deadline:
                                        actual = latest_original_native_local_player_position(
                                            host_log)
                                        if actual is not None and (
                                                abs(actual[2] - fifth_source[2]) > .65
                                                or actual[1] < fifth_source[1] - .42
                                                or actual[0] > fifth_source[0] + 1.80):
                                            raise AssertionError(
                                                "physical E-to-F lost source support E={} now={}"
                                                .format(fifth_source, actual))
                                        if sixth_expected in log_tail(host_log):
                                            break
                                        time.sleep(.035)
                                    else:
                                        raise AssertionError(
                                            "no sixth native controller admitted"
                                            " during real Shift+S E-to-F traversal")
                                finally:
                                    try:
                                        if sixth_backward:
                                            set_original_host_backward_key(
                                                host, host_window, False)
                                    finally:
                                        if sixth_sneak:
                                            set_original_host_sneak_key(
                                                host, host_window, False)
                                wait_for(host, host_log, sixth_expected, deadline,
                                         "sixth original native click on distinct F")
                                wait_for(host, host_log, repeat_receipt + b" step=6",
                                         deadline, "sixth genuine client World+inventory 3-to-2")
                                wait_for(host, host_log,
                                         b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_"
                                         b"SIX_CLIENT_RECEIPTS_PASS=YES attempts=6",
                                         deadline, "six genuinely separate native receipts")
                                sixth_source = source_native_scaffold_position(host_log, 6)
                                native_sixth = re.compile(
                                    rb"CUSTOMMC_OFFICIAL_189_SCAFFOLD_KEYBOARD_WALK_"
                                    rb"ORIGINAL_SOURCE_POSITION step=6 "
                                    rb"x=(-?\d+(?:\.\d+)?) y=(-?\d+(?:\.\d+)?) "
                                    rb"z=(-?\d+(?:\.\d+)?) onGround=true sneaking=true")
                                if len(native_sixth.findall(log_tail(host_log, 200000))) != 1:
                                    raise AssertionError(
                                        "sixth original controller not grounded+sneaking")
                                if not (fifth_source[0] + .45 <= sixth_source[0]
                                        <= fifth_source[0] + 1.80
                                        and abs(sixth_source[1] - fifth_source[1]) <= .35
                                        and abs(sixth_source[2] - fifth_source[2]) <= .55):
                                    raise AssertionError(
                                        "sixth native click did not physically advance"
                                        " E={} F={}".format(fifth_source, sixth_source))
                                expected_attempts = 6
                                expected_targets = (
                                    target_xyz, second_target_xyz,
                                    third_target_xyz, fourth_target_xyz,
                                    fifth_target_xyz, sixth_target_xyz)
                                expected_items = b"has 2 items that match the criteria"
                                print(
                                    "CUSTOMMC_OFFICIAL_189_SCAFFOLD_SIX_REAL_KEYBOARD_LEGS_PASS=YES"
                                    " A={} B={} C={} D={} E={} F={}".format(
                                        first_source[0], second_source[0], third_source[0],
                                        fourth_source[0], fifth_source[0],
                                        sixth_source[0]), flush=True)
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
                    if three_keyboard_walk and server_log.read_bytes().count(
                            b"Teleported CIHost to") != initial_host_teleports:
                        raise AssertionError(
                            "unexpected server TP after initial A during keyboard bridge")
                    if not three_keyboard_walk:
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*third_target_xyz),
                                            expected=b"successfully found the block")
                    elif not four_keyboard_walk:
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*fourth_target_xyz),
                                            expected=b"successfully found the block")
                    elif not five_keyboard_walk:
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*fifth_target_xyz),
                                            expected=b"successfully found the block")
                    elif not six_keyboard_walk:
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*sixth_target_xyz),
                                            expected=b"successfully found the block")
                    else:
                        send_server_command(server, server_log,
                                            "testforblock {} {} {} air".format(*seventh_target_xyz),
                                            expected=b"successfully found the block")
                    print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_REAL_KEYBOARD_"
                          + ("SERVER_SIX_STONE_TWO_PASS=YES" if six_keyboard_walk
                             else "SERVER_FIVE_STONE_THREE_PASS=YES" if five_keyboard_walk
                             else "SERVER_FOUR_STONE_FOUR_PASS=YES" if four_keyboard_walk
                             else "SERVER_THREE_STONE_FIVE_PASS=YES" if three_keyboard_walk
                             else "SERVER_TWO_STONE_SIX_PASS=YES"), flush=True)
                print("CUSTOMMC_OFFICIAL_189_SCAFFOLD_"
                      + repeat_marker.decode("ascii")
                      + ("_SERVER_HISTORY_VETO_TWO_STONE_FIVE_PASS=YES"
                         if history_negative else
                         "_SERVER_SIX_STONE_TWO_PASS=YES" if six_keyboard_walk
                         else "_SERVER_FIVE_STONE_THREE_PASS=YES" if five_keyboard_walk
                         else "_SERVER_FOUR_STONE_FOUR_PASS=YES" if four_production
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
                # Original run 40105144305 attempt 3 persisted at
                # screen=OPEN source=missing_player_world_cursor_or_selected_item;
                # requiring positive_candidate_already_available prevented
                # any real Escape and moduleActivation remained zero. Only
                # accept actual current-phase complete native ARM_WAIT lines.
                enabled_gui_source_start = host_log.stat().st_size
                dismissed_enabled_gui = False
                while time.monotonic() < deadline:
                    check_process(host, "enabled Scaffold source GUI readiness")
                    host_data = log_tail(host_log)
                    if module in host_data:
                        break
                    with host_log.open("rb") as native_reader:
                        native_reader.seek(enabled_gui_source_start)
                        enabled_source_bytes = native_reader.read(131072)
                    if v320_original_enabled_scaffold_escape_evidence(
                            enabled_source_bytes, dismissed_enabled_gui):
                        send_real_host_key(host, host_window, "Escape")
                        dismissed_enabled_gui = True
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
            # V307: the real server must prove a single 35-block physical
            # platform under original A, B and host BEFORE their teleports.
            # In the V306 red original job B's measured 3D distance was
            # 11.22 even after the intended server TP. Floor/position
            # convergence is never inferred from this block receipt alone:
            # the unchanged original 20-tick A/B source gate decides it.
            support_fill, support_cells = (
                actual_server_rejoin_three_player_floor_fixture(
                    host_tp, a_near, b_outer))
            # A repeated vanilla fill of already-stone terrain legitimately
            # returns "No blocks filled". Read ONLY fresh log bytes; accept
            # precisely the successful 35-block or idempotent no-change
            # server reply, then require independent REAL stone witnesses.
            before_floor_log = server_log.stat().st_size
            send_server_command(
                server, server_log, support_fill,
                expected=b"blocks filled")
            with server_log.open("rb") as floor_reader:
                floor_reader.seek(before_floor_log)
                fill_reply = floor_reader.read()
            if not actual_server_rejoin_floor_fill_receipt(fill_reply):
                raise AssertionError(
                    "original vanilla floor fill lacks a valid fresh receipt: "
                    + fill_reply[-800:].decode("ascii", "replace"))
            for support_x, support_y, support_z in support_cells:
                send_server_command(
                    server, server_log,
                    "testforblock {} {} {} stone".format(
                        support_x, support_y, support_z),
                    expected=b"successfully found the block")
            # V52: move HOST into the genuine server-owned combat chunk
            # BEFORE moving B. Sending B's teleport first could leave the
            # remote original loadedEntityList with a stale B spawn/position
            # after host's own teleport. The native source witness in V50
            # observed actual A=2.06 but B=5.74 even when dedicated server
            # acknowledged B's intended sqrt(8) teleport.
            #
            # The final server-authoritative teleport is deliberately B,
            # after host and A are tracking the real nearby entity. V153
            # makes B sqrt(5), not previous sqrt(8), from the original
            # host (A stays nearer at 2 blocks), preserving real client
            # raycast, UUID ownership, native controller and server damage.
            # No synthetic positions or weakened original acceptance.
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, a_near)
            # The genuine late V177 source had only 0.014 initial A/B
            # distance margin even while the actual A was selected. Move
            # ONLY original real A forward in the same loaded chunk,
            # before B's final real server teleport; do not change the
            # source UUID lock criterion or any subsequent rejoined A.
            prelock_a=actual_server_original_rejoin_prelock_a_fixture(
                host_tp,a_near,b_outer)
            send_server_command(server, server_log, prelock_a,
                                expected=b"Teleported CITarget to")
            send_server_command(server, server_log, b_outer,
                                expected=original_locked_tp_server_reply(b_outer))
            print("CUSTOMMC_189_REJOIN_INITIAL_A_REAL_SOURCE_BUFFER_TELEPORT_PASS=YES",
                  flush=True)
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
            # Pin the host JVM log offset BEFORE launching a brand-new
            # original Minecraft client. A genuine rejoin UUID witness
            # must be emitted after this source admission boundary.
            # Avoid log_tail() dropping a valid marker if ALSA/LWJGL
            # diagnostics grow beyond its fixed 100 KiB window.
            rejoin_host_evidence_offset = (
                host_log.stat().st_size if host_log.exists() else 0)
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
            # A second actual client JVM may take X11 focus away from
            # CIHost. Clear only a newly observed original axp pause, never
            # manufacture attacks or reset Minecraft's click guard.
            v348_resume_confirmed_original_pause(
                host, host_log, deadline, host_window)
            send_server_command(server, server_log, "gamemode 1 CITarget")
            # M419 alone uses a more stable real A server position.
            # The original earlier A/B interaction remains unchanged.
            a_rejoined = (actual_server_rejoined_a_controller_fixture(
                host_tp, a_near, b_outer)
                if args.require_rejoined_controller_b else a_near)
            send_server_command(server, server_log, a_rejoined)
            print("CUSTOMMC_189_REJOIN_REAL_A_NEW_JVM_RECONNECTED_PASS=YES",
                  flush=True)
            wait_for_new_original_source_marker(
                host, host_log, rejoin_host_evidence_offset,
                REJOIN_UUID, deadline,
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
                ap=a_rejoined.split()
                if len(ap)!=7 or ap[:2]!=["tp","CITarget"]:
                    raise AssertionError("invalid native A floor coordinate")
                ax, ay, az=(float(v) for v in ap[2:5])
                fx, fz=math.floor(ax), math.floor(az)
                floor="fill {} {} {} {} {} {} stone".format(
                    fx,int(ay)-1,fz,fx+1,int(ay)-1,fz+1)
                # First stone fill may be idempotent because the earlier
                # 35-cell genuine original floor overlaps this 2x2 region.
                # Require the FRESH exact vanilla INFO response, and then
                # four independent REAL testforblock receipts. Neither
                # response alone proves native player/UUID/controller state.
                before_phase4_floor = server_log.stat().st_size
                send_server_command(server,server_log,floor,
                                    expected=b"blocks filled")
                with server_log.open("rb") as phase4_reader:
                    phase4_reader.seek(before_phase4_floor)
                    phase4_floor_reply = phase4_reader.read()
                if not actual_server_rejoin_floor_fill_receipt(
                        phase4_floor_reply, expected_count=4):
                    raise AssertionError(
                        "original 2x2 survival floor lacks a valid fresh receipt: "
                        + phase4_floor_reply[-800:].decode("ascii", "replace"))
                for dx in (0, 1):
                    for dz in (0, 1):
                        send_server_command(
                            server, server_log,
                            "testforblock {} {} {} stone".format(
                                fx + dx, int(ay) - 1, fz + dz),
                            expected=b"successfully found the block")
                # V193 exact native original runtime later exposed REAL
                # phase4 player-height divergence (A3D=2.469, B3D=2.428).
                # A's 2x2 stone floor alone does not ground host and B:
                # two vanilla players may fall differently during native
                # rejoin, invalidating the strict .12 real 3D margin.
                # V199 source-native original XYZ proved that a small
                # center-only stone platform did not cover survivor B,
                # who landed 1 block below and about 1 block past the
                # intended /tp position. Extend actual server stone
                # across a bounded 3-block safety perimeter, then reseat
                # genuine B once on this now-verified platform before
                # the mandatory 20 actual host World source samples.
                grounded_floor = actual_server_rejoined_phase4_grounded_floor_fixture(
                    host_tp, a_rejoined, b_outer)
                send_server_command(server,server_log,grounded_floor,
                                    expected=b"blocks filled")
                hp, ap, bp = host_tp.split(), a_rejoined.split(), b_outer.split()
                floor_y = int(hp[3]) - 1
                original_stone_columns = (
                    (int(hp[2]), int(hp[4])),
                    (math.floor(float(ap[2])), math.floor(float(ap[4]))),
                    (int(bp[2]), int(bp[4])))
                for px,pz in set(original_stone_columns):
                    send_server_command(server,server_log,
                                        "testforblock {} {} {} stone".format(
                                            px,floor_y,pz),
                                        expected=b"successfully found the block")
                print("CUSTOMMC_189_M419_NATIVE_THREE_PLAYER_GROUNDED_STONE_PASS=YES",
                      flush=True)
                # V197 without a second B /tp also failed (native B moved
                # outside reach), as did V196 WITH a second B /tp but only
                # a tiny floor. V200 changes both support and movement
                # simultaneously. Real Slowness 5 on creative B stops
                # ordinary walking in the original client; the true
                # server /tp reseats B above the wide stone platform.
                # Every ACK is genuine vanilla and the strict B UUID,
                # 3D margin, ray and real damage remain independent.
                send_server_command(server,server_log,
                                    "effect CITargetB 2 600 5 true",
                                    expected=b"Given")
                send_server_command(server,server_log,b_outer,
                                    expected=b"Teleported CITargetB to")
                print("CUSTOMMC_189_M419_NATIVE_B_WIDE_PLATFORM_RESEAT_PASS=YES",
                      flush=True)
                # V322 genuine original runner: after the real supported B
                # reseat, increase only B's planned native 3D gap from A.
                # The original client still must observe 20 consecutive
                # eligible B-locked World snapshots after the host /tp.
                b_anchor = actual_server_rejoined_b_margin_anchor_fixture(
                    host_tp, a_rejoined, b_outer)
                send_server_command(server,server_log,b_anchor,
                                    expected=b"Teleported CITargetB to")
                bp_anchor = b_anchor.split()
                send_server_command(server,server_log,
                    "testforblock {} {} {} stone".format(
                        math.floor(float(bp_anchor[2])), floor_y,
                        math.floor(float(bp_anchor[4]))),
                    expected=b"successfully found the block")
                print("CUSTOMMC_189_V322_REAL_B_BUFFER_STONE_VERIFIED=YES",
                      flush=True)
                send_server_command(server,server_log,"gamemode 0 CITarget")
                send_server_command(server,server_log,a_rejoined,
                                    expected=b"Teleported CITarget to")
                # V176 actual original crash: CIHost drifted ~0.7 from
                # server-authored position during earlier live B combat,
                # reducing strict 3D A/B separation to 0.079 < 0.120.
                # Recenter ONLY the original host, after rejoined A/floor,
                # with a fresh genuine dedicated-server teleport reply.
                # No source/entity snapshots or native gate changes.
                phase4_host = actual_server_rejoined_host_recenter_fixture(
                    host_tp, a_rejoined, b_outer)
                # Source JVM stdout offset is captured before the server
                # teleport. Its console ACK alone proves no CLIENT World
                # position, so require 20 fresh, original-source stable
                # B-locked/selected snapshots with buffered 3D margin.
                original_source_start = host_log.stat().st_size
                send_server_command(server,server_log,phase4_host,
                                    expected=b"Teleported CIHost to")
                print("CUSTOMMC_189_REJOIN_PHASE4_ORIGINAL_HOST_RECENTER_SERVER_PASS=YES",
                      flush=True)
                wait_for_new_original_source_marker(
                    host, host_log, original_source_start,
                    b"CUSTOMMC_189_M419_NATIVE_PHASE4_SOURCE_STABLE=20",
                    deadline, "M419 live B lock after genuine phase4 host teleport")
                print("CUSTOMMC_189_M419_LIVE_SOURCE_STABLE_BEFORE_B_DAMAGE=YES",
                      flush=True)
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
            # V292: REQUIRE a fresh, coordinate-precise original
            # dedicated-server teleport receipt for EACH genuine remote
            # and host, not simply 0.3 seconds of Python command delay.
            # Actual source-native A selection still must pass 20 ticks.
            for original_tp in (b_outer, host_tp, a_near):
                send_server_command(
                    server, server_log, original_tp,
                    expected=original_locked_tp_server_reply(original_tp))
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
            # V366 R6: three real client windows may leave CIHost in Mojang's
            # original axp pause menu. Preserve the strict GUI combat veto.
            # Only the known host window and a source-confirmed axp may
            # authorize a real XTest Escape; no synthetic attack, screen
            # mutation or arbitrary window choice is allowed here.
            v348_resume_confirmed_original_pause(
                host, host_log, deadline, host_window)
            if v348_latest_original_gui_screen(host_log) != "(in-game/no GUI)":
                raise AssertionError(
                    "sticky A original host GUI did not establish native CLEAR "
                    "before genuine controller/server-damage acceptance")
            print("CUSTOMMC_189_V366_LOCK_PREATTACK_ORIGINAL_GUI_CLEAR_PASS=YES",
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
            send_server_command(
                server, server_log, a_far,
                expected=original_locked_tp_server_reply(a_far))
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
            v311_original_b_source_start = host_log.stat().st_size
            # BOTH actual remote EntityPlayers stay inside default 3.5 range.
            # Movement uses the real server, not fabricated entity snapshots.
            send_server_command(server, server_log,
                                "scoreboard objectives add taken stat.damageTaken")
            # V320: a second genuine vanilla-server stat distinguishes HOST
            # damage dealt from B's total damage taken. Neither implies an
            # authorized B click; B=0 negative remains entirely unchanged.
            send_server_command(server, server_log,
                                "scoreboard objectives add dealt stat.damageDealt")
            send_server_command(server, server_log,
                                "scoreboard players set CIHost dealt 0")
            send_server_command(server, server_log,
                                "scoreboard players test CIHost dealt 0 0",
                                expected=b"score 0 is in range 0 to 0")
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
            # Real server wall keeps actual original A's PvP knockback from
            # changing the closest live player during phase A. This is NOT
            # a target-selector override: A and B remain vulnerable Survival
            # EntityPlayers and all strict UUID, controller, ray and
            # scoreboard checks below are unchanged. Server build must work.
            wall, witness_columns = (
                actual_server_simultaneous_a_knockback_wall_fixture(
                    host_tp, a_near, b_outer, b_near))
            send_server_command(server, server_log, wall,
                                expected=b"9 blocks filled")
            for wx, wy, wz in witness_columns:
                send_server_command(
                    server, server_log,
                    "testforblock {} {} {} stone".format(wx, wy, wz),
                    expected=b"successfully found the block")
            print("CUSTOMMC_189_SIMULTANEOUS_REAL_A_KNOCKBACK_WALL_PASS=YES",
                  flush=True)
            send_server_command(server, server_log, b_outer)
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, a_near)
            print("CUSTOMMC_189_SIMULTANEOUS_TWO_PLAYERS_IN_REACH_PASS=YES",
                  flush=True)
            wait_for(host, host_log, SIM_READY, deadline,
                     "20 disabled callbacks: both genuine eligible, A nearer")
            # V366 R7: a newly spawned third Minecraft window can leave
            # the REAL host in vanilla axp pause, as R5 independently
            # proved for the sticky-A fixture. Never bypass the production
            # GUI veto. Only the retained original CIHost X11 window,
            # source-confirmed axp and a new native CLEAR transition
            # authorize continuation to actual vanilla controller damage.
            v348_resume_confirmed_original_pause(
                host, host_log, deadline, host_window)
            if v348_latest_original_gui_screen(host_log) != "(in-game/no GUI)":
                raise AssertionError(
                    "simultaneous native host screen not CLEAR after "
                    "source-verified original pause recovery")
            print("CUSTOMMC_189_V366_SIMULTANEOUS_PREATTACK_ORIGINAL_GUI_CLEAR_PASS=YES",
                  flush=True)
            wait_for(host, host_log, SIM_HIT_A, deadline,
                     "UUID A native-owned attack among two eligible players")
            wait_for_real_score(server, server_log, "CITarget", deadline)
            try:
                send_server_command(server, server_log,
                                    "scoreboard players test CITargetB taken 0 0",
                                    expected=b"score 0 is in range 0 to 0")
            except AssertionError:
                # V317: V316's pre-teleport timeline was only emitted AFTER
                # B=0 succeeded, so the genuine V308 B=10 failure (precisely
                # what this source timeline investigates) could never emit
                # V316 evidence. Sample ONLY the current original host JVM
                # stdout before any deliberate B-nearer server teleport.
                # Diagnostic failures cannot mask the real negative B-score.
                try:
                    v317_bad_b_end = host_log.stat().st_size
                    print("CUSTOMMC_189_SIMULTANEOUS_EARLY_B_DAMAGE_SOURCE_DIAG "
                          + v316_preteleport_original_b_priority_marker(
                              host_log, v311_original_b_source_start,
                              v317_bad_b_end), flush=True)
                    print(v320_original_after_a_b_timeline(
                        host_log, v311_original_b_source_start,
                        v317_bad_b_end), flush=True)
                except (AssertionError, OSError, UnicodeError) as error:
                    print("CUSTOMMC_189_SIMULTANEOUS_EARLY_B_SOURCE_UNAVAILABLE_DIAG "
                          + type(error).__name__, flush=True)
                # V320: report original native A-click and B hurt-time
                # source lines from this SAME failing phase, without giving
                # them any authority over original server B=0.
                try:
                    v320_bad_b_end = host_log.stat().st_size
                    samples = v320_original_b_damage_source_window(
                        host_log, v311_original_b_source_start,
                        v320_bad_b_end)
                    print("CUSTOMMC_189_SIMULTANEOUS_EARLY_B_NATIVE_HURT_DIAG "
                          + ("completeMarkers=" + str(len(samples))),
                          flush=True)
                    for sample in samples:
                        print(sample, flush=True)
                except (AssertionError, OSError, UnicodeError) as error:
                    print("CUSTOMMC_189_SIMULTANEOUS_EARLY_B_NATIVE_HURT_UNAVAILABLE "
                          + type(error).__name__, flush=True)
                # V320: correlate the ACTUAL Mojang attackEntity Entity
                # argument UUID with the server's adverse B damage. No
                # controller event => source unknown, NOT proof of harmless
                # damage or permission to relax the original B=0 veto.
                try:
                    for line in v320_original_controller_argument_source(
                            host_log, v311_original_b_source_start,
                            host_log.stat().st_size):
                        print("CUSTOMMC_189_SIMULTANEOUS_EARLY_B_CONTROLLER_SOURCE_DIAG "
                              + line, flush=True)
                except (AssertionError, OSError, UnicodeError) as error:
                    print("CUSTOMMC_189_SIMULTANEOUS_CONTROLLER_SOURCE_UNAVAILABLE_DIAG "
                          + type(error).__name__, flush=True)
                # Preserve V311 negative context and bare rethrow. No
                # source marker ever converts B>0 into a passing test.
                print(v311_original_b_failure_evidence(
                    host_log, v311_original_b_source_start), flush=True)
                # V320: query genuine vanilla dedicated-server scoreboards
                # ONLY after strict B=0 has already failed. Never infer the
                # owner/cause of damage from a damageTaken score alone.
                for owner in ("CIHost", "CITarget", "CITargetB"):
                    try:
                        print("V320_ORIGINAL_SERVER_DAMAGE_SOURCE owner=" + owner
                              + " " + v320_original_server_score_query(
                                  server, server_log, owner), flush=True)
                    except (AssertionError, OSError, ValueError, UnicodeError) as error:
                        print("V320_ORIGINAL_SERVER_DAMAGE_SOURCE_UNAVAILABLE owner="
                              + owner + " reason=" + type(error).__name__,
                              flush=True)
                raise
            print("CUSTOMMC_189_SIMULTANEOUS_A_DAMAGED_B_STILL_ZERO_PASS=YES",
                  flush=True)
            # V316 read-only timeline: capture the original JVM stdout byte
            # cursor BEFORE the deliberate real-server B-nearer teleport.
            # Do not query, advance, or override a live World target. Missing
            # diagnostics must never authorize or veto gameplay by itself.
            try:
                v316_before_b_tp_end = host_log.stat().st_size
                print(v316_preteleport_original_b_priority_marker(
                    host_log, v311_original_b_source_start,
                    v316_before_b_tp_end), flush=True)
            except (AssertionError, OSError, UnicodeError) as error:
                print("CUSTOMMC_189_SIMULTANEOUS_B_PRETELEPORT_SOURCE_UNAVAILABLE_DIAG "
                      + type(error).__name__, flush=True)
            # V320: expose only exact current original vanilla controller
            # Entity arguments before any B-nearer server teleport.
            # Neither absence nor presence confers a game/scoreboard PASS.
            try:
                for line in v320_original_controller_argument_source(
                        host_log, v311_original_b_source_start,
                        host_log.stat().st_size):
                    print("CUSTOMMC_189_SIMULTANEOUS_PRETELEPORT_CONTROLLER_SOURCE_DIAG "
                          + line, flush=True)
            except (AssertionError, OSError, UnicodeError) as error:
                print("CUSTOMMC_189_SIMULTANEOUS_CONTROLLER_SOURCE_UNAVAILABLE_DIAG "
                      + type(error).__name__, flush=True)
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
            # After independent positive A/B server damage, expose the exact
            # original-client B priority transition from this test's cursor.
            # No server message, cached stdout or synthetic location can pass.
            print(v311_original_b_priority_source(
                host_log, v311_original_b_source_start), flush=True)
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
            # V366 R8: the third original Minecraft JVM may leave CIHost
            # in Mojang's real axp pause GUI. Preserve the production
            # original-screen veto. A source-confirmed axp alone permits
            # a physical Escape to our pre-target host X11 window, and
            # native CLEAR is mandatory before actual UUID combat proof.
            v348_resume_confirmed_original_pause(
                host, host_log, deadline, host_window)
            if v348_latest_original_gui_screen(host_log) != "(in-game/no GUI)":
                raise AssertionError(
                    "multi-target original host GUI not source CLEAR "
                    "before real A UUID attack and server damage")
            print("CUSTOMMC_189_V366_MULTI_PREATTACK_ORIGINAL_GUI_CLEAR_PASS=YES",
                  flush=True)
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
                try:
                    wait_for(host, host_log, WALL_LIFECYCLE_SELECTED, deadline,
                             "20 real hidden-player selections while Wall Check off")
                except AssertionError:
                    # V318: preserve original failure; report ONLY the real
                    # source-native phase-1 heartbeat, bounded and read-only.
                    try:
                        samples = v318_original_negative_native_source(
                            host_log, "wall")
                        print("V318_WALL_ORIGINAL_FAILURE_SOURCE_START",
                              file=sys.stderr)
                        for sample in samples:
                            print(sample, file=sys.stderr)
                        print("V318_WALL_ORIGINAL_FAILURE_SOURCE_END",
                              file=sys.stderr)
                    except (OSError, ValueError) as diag:
                        print("V318_WALL_ORIGINAL_SOURCE_UNAVAILABLE "
                              + type(diag).__name__, file=sys.stderr)
                    raise
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
            fov_air, fov_stone = actual_server_fov_ground_support_fixture(
                host_tp, target_tp)
            # Sacrificial vanilla contrast reset. ONLY the air preclear
            # allows a fresh "No blocks filled" or positive changed reply.
            # It is NEVER a gameplay PASS. Require genuine positive
            # 15-block stone fill after reset, then teleport both REAL
            # clients onto stable source-grounded positions.
            send_server_command(server, server_log, fov_air,
                                expected=b"blocks filled")
            send_server_command(server, server_log, fov_stone,
                                expected=b"15 blocks filled")
            send_server_command(server, server_log, host_tp)
            send_server_command(server, server_log, target_tp)
            print("CUSTOMMC_189_FOV_REAL_SERVER_FIFTEEN_STONE_SUPPORT_PASS=YES",
                  flush=True)
            print("CUSTOMMC_189_FOV_REAL_SERVER_SPAWN_CHUNK_PASS=YES",
                  flush=True)
            wait_for(host, host_log, FOV_DISABLED, deadline,
                     "20 real rear-player target callbacks with module disabled")
            wait_for(host, host_log, FOV_ENABLED, deadline,
                     "ordinary Kill Aura module enable")
            wait_for(host, host_log, FOV_VETO, deadline,
                     "80 real rear-player FOV veto callbacks")
            try:
                send_server_command(server, server_log,
                                    "scoreboard players test CITarget taken 0 0",
                                    expected=b"score 0 is in range 0 to 0")
            except AssertionError:
                # V318: an original FOV veto does not certify server B=0.
                # V316 attempt 2 genuinely registered 10 server damage
                # despite the original FOV veto marker. Keep that failure.
                try:
                    samples = v318_original_negative_native_source(
                        host_log, "fov")
                    print("V318_FOV_ORIGINAL_FAILURE_SOURCE_START",
                          file=sys.stderr)
                    for sample in samples:
                        print(sample, file=sys.stderr)
                    print("V318_FOV_ORIGINAL_FAILURE_SOURCE_END",
                          file=sys.stderr)
                except (OSError, ValueError) as diag:
                    print("V318_FOV_ORIGINAL_SOURCE_UNAVAILABLE "
                          + type(diag).__name__, file=sys.stderr)
                raise
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
            host_tp, target_tp = actual_server_spawn_range_negative_fixture(
                server_log)
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
            if args.triggerbot_pvp:
                wait_for(host, host_log, TRIGGER_DISABLED, deadline,
                         "20 original zero-hurt disabled TriggerBot player rays")
                wait_for(host, host_log, TRIGGER_ENABLED, deadline,
                         "normal isolated TriggerBot ModuleController enable")
            else:
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
            if args.triggerbot_pvp:
                wait_for(host, host_log, TRIGGER_CLICK, deadline,
                         "one normal TriggerBot original crosshair UUID-owned click")
                wait_for(host, host_log, TRIGGER_RAY, deadline,
                         "genuine original remote player entity under mouse")
                print("CUSTOMMC_189_TRIGGERBOT_OWNED_REAL_PLAYER_CLICK=YES",
                      flush=True)
            else:
                wait_for(host, host_log, AURA_ATTACK, deadline,
                         "host-owned Kill Aura synthetic rotated player attack")
                print("CUSTOMMC_189_CLIENT_AURA_CLICK_OBSERVED=YES", flush=True)
            # An owned synthetic action is only a potential hit. Real PvP
            # must enter Mojang PlayerControllerMP.attackEntity and produce
            # both native player hurt-time and server-authoritative damage.
            if args.triggerbot_pvp:
                wait_for(host, host_log, TRIGGER_CONTROLLER,
                         min(deadline, time.monotonic() + 10),
                         "original Mojang controller TriggerBot player entity")
            else:
                wait_for(host, host_log, CONTROLLER_ATTACK,
                         min(deadline, time.monotonic() + 10),
                         "actual Mojang PlayerControllerMP.attackEntity method")
            print("CUSTOMMC_189_NATIVE_ATTACK_DISPATCH_PATH_ENTERED=YES",
                  flush=True)
            # Distinguish "module clicked but client never saw hurt" from
            # "client saw hurt but the dedicated server never counted damage".
            # The client marker alone never certifies PvP; both gates remain
            # mandatory. Fail quickly when there is no native hurt response.
            wait_for(host, host_log,
                     TRIGGER_HURT if args.triggerbot_pvp else AURA_HURT,
                     min(deadline, time.monotonic() + 18),
                     "same real original remote player receiving native hurt")
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
            if args.triggerbot_pvp:
                print("CUSTOMMC_OFFICIAL_189_TRIGGERBOT_NATIVE_CONTROLLER_"
                      "AND_SERVER_DAMAGE_PASS=YES", flush=True)
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
            if name == "host":
                # V366 R3: the ordinary 6KB host tail can omit phase-3
                # callbacks from a full native-run deadline. Report bounded
                # first/last read-only witnesses from the ENTIRE genuine
                # Mojang process output, including a count of zero when
                # neither the tick nor melee dispatch hooks ran.
                phase3_markers = (
                    b"CUSTOMMC_189_V366_LOCK_PHASE3_TICK_DIAG",
                    b"CUSTOMMC_189_V366_LOCK_PHASE3_NATIVE_RAY_DIAG",
                    b"CUSTOMMC_189_V366_LOCK_PHASE3_SCHEDULE_DIAG",
                )
                phase3_total = 0
                phase3_first, phase3_last = [], []
                if log.exists():
                    with log.open("rb") as original_host:
                        for native_line in original_host:
                            if not any(marker in native_line
                                       for marker in phase3_markers):
                                continue
                            phase3_total += 1
                            witness = native_line[:1200].decode(
                                "utf-8", "replace").rstrip()
                            if len(phase3_first) < 12:
                                phase3_first.append(witness)
                            else:
                                if len(phase3_last) == 12:
                                    phase3_last.pop(0)
                                phase3_last.append(witness)
                print("CUSTOMMC_189_V366_PHASE3_FORENSICS_START"
                      + " count=" + str(phase3_total), file=sys.stderr)
                for native_line in phase3_first + phase3_last:
                    print(native_line, file=sys.stderr)
                print("CUSTOMMC_189_V366_PHASE3_FORENSICS_END",
                      file=sys.stderr)
                # V356: bounded forensic extraction from the WHOLE original
                # Minecraft host stdout. The prior 6,000-byte tail loses
                # the first phase-3 B UUID revocation because rejoin can
                # continue for hundreds of genuine native callbacks.
                # Read-only on failure: never admits an action, fake World
                # evidence, new client instance or altered timeout.
                witness = b"CUSTOMMC_189_REJOIN_NATIVE_LOCK_TRANSITION_DIAG"
                lock_transitions = []
                total_transitions = 0
                if log.exists():
                    with log.open("rb") as original_host_log:
                        for native_line in original_host_log:
                            if witness in native_line:
                                total_transitions += 1
                                if len(lock_transitions) < 16:
                                    lock_transitions.append(
                                        native_line[:1200].decode(
                                            "utf-8", "replace").rstrip())
                print("CUSTOMMC_189_V356_ORIGINAL_HOST_LOCK_TRANSITIONS_START"
                      + " count=" + str(total_transitions), file=sys.stderr)
                for native_line in lock_transitions:
                    print(native_line, file=sys.stderr)
                print("CUSTOMMC_189_V356_ORIGINAL_HOST_LOCK_TRANSITIONS_END",
                      file=sys.stderr)
                # V345 failure-only forensic evidence. The normal positive
                # native controller/UUID/hurt/server gates are unmodified.
                print("CUSTOMMC_189_V345_ORIGINAL_SCREEN_TIMELINE_START",
                      file=sys.stderr)
                for screen in v345_original_screen_timeline(log):
                    print(screen, file=sys.stderr)
                print("CUSTOMMC_189_V345_ORIGINAL_SCREEN_TIMELINE_END",
                      file=sys.stderr)
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


def configure_original_scaffold_gui_negative(args):
    """Only select the verified V54 fixture geometry for V308's E veto."""
    args.scaffold_gui_abort = (
        args.scaffold_placement == "production-repeat-three-menu-after-first")
    if args.scaffold_gui_abort:
        args.scaffold_placement = "production-repeat-three"
    return args


def configure_original_scaffold_hotbar_negative(args):
    """Map isolated V309 real hotbar veto to original verified three-stone setup."""
    args.scaffold_hotbar_abort = (
        args.scaffold_placement == "production-repeat-three-hotbar-after-first")
    if args.scaffold_hotbar_abort:
        args.scaffold_placement = "production-repeat-three"
    return args


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("java", "server", "client", "libraries", "assets",
                 "natives", "overlay", "root"):
        parser.add_argument("--" + name, required=True)
    parser.add_argument("--timeout", type=int, default=150)
    parser.add_argument("--scaffold-placement", choices=("enabled","disabled","menu-open","repeat-two","production-repeat-two","production-repeat-three","production-repeat-three-history","production-repeat-four","production-repeat-two-keyboard-walk","production-repeat-three-keyboard-walk", "production-repeat-four-keyboard-walk", "production-repeat-five-keyboard-walk", "production-repeat-six-keyboard-walk",
                         "production-repeat-three-menu-after-first",
                         "production-repeat-three-hotbar-after-first"),
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
    group.add_argument("--triggerbot-pvp", action="store_true",
                       help="isolated source-native TriggerBot original controller "
                            "attack against real UUID player plus server damage")
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
    configure_original_scaffold_gui_negative(args)
    configure_original_scaffold_hotbar_negative(args)
    if args.triggerbot_pvp:
        if args.unlocked_scoped_veto or args.unlocked_remote_owner_veto:
            parser.error("TriggerBot must remain isolated from Aura owner fixtures")
        # Share actual positive dedicated-server geometry/stat ownership only;
        # never reuse Aura-specific Java props or PASS markers.
        args.pvp = True
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
