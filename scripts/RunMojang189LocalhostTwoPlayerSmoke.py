#!/usr/bin/env python3
"""Real 1.8.9 dedicated server and TWO real Minecraft clients on loopback only.

Authenticates no accounts, opens no external listener, and uses no fake
EntityPlayer. Default mode verifies real joins and nonlocal player-kind
callbacks; opt-in --pvp requires the genuine Mojang attack controller,
same-player hurt-time and server-side damage-stat increase.
"""
import argparse
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
RANGE_DISABLED = b"CUSTOMMC_189_KILLAURA_RANGE_DISABLED_TICKS_PASS=20"
RANGE_ENABLED = b"CUSTOMMC_189_KILLAURA_RANGE_MODULE_ENABLE_PASS=YES"
RANGE_VETO = b"CUSTOMMC_OFFICIAL_189_KILLAURA_REAL_PLAYER_RANGE_VETO_PASS=YES"
FOV_DISABLED = b"CUSTOMMC_189_KILLAURA_FOV_DISABLED_TARGET_TICKS_PASS=20"
FOV_ENABLED = b"CUSTOMMC_189_KILLAURA_FOV_MODULE_ENABLE_PASS=YES"
FOV_VETO = b"CUSTOMMC_OFFICIAL_189_KILLAURA_REAL_PLAYER_FOV_VETO_PASS=YES"
WALL_DISABLED = b"CUSTOMMC_189_KILLAURA_WALL_DISABLED_OCCLUDED_TICKS_PASS=20"
WALL_ENABLED = b"CUSTOMMC_189_KILLAURA_WALL_MODULE_ENABLE_PASS=YES"
WALL_VETO = b"CUSTOMMC_OFFICIAL_189_KILLAURA_REAL_PLAYER_WALL_VETO_PASS=YES"


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


def write_server_properties(path, port):
    if not (1024 < port < 65536):
        raise ValueError("invalid loopback fixture port")
    # Bind only to localhost; 1.8.9 offline-mode is *never* exposed to a
    # public network or supported as an external multiplayer configuration.
    content = (
        "server-ip=127.0.0.1\n"
        "server-port=" + str(port) + "\n"
        "online-mode=false\n"
        "max-players=2\n"
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
    if remote and args.range_veto:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraRangeVeto=true")
    if remote and args.wall_veto:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraWallVeto=true")
    if remote and args.fov_veto:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraFovVeto=true")
    if remote and args.pvp:
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportVanillaMelee=true")
        command.insert(command.index("-cp"),
                       "-Dcustommc.acceptance.reportKillAuraLocalPvp=true")
    command += ["--server", "127.0.0.1", "--port", str(port)]
    # Limit GUI/test process heap so both instances and server fit CI RAM.
    command[command.index("-Xmx1536m")] = "-Xmx1024m"
    return command


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
    write_server_properties(server_dir / "server.properties", port)

    server_log = root / "server.log"
    host_log = root / "host.log"
    target_log = root / "target.log"
    logs = [("dedicated", server_log), ("host", host_log),
            ("target", target_log)]
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
        with target_log.open("wb") as out:
            target = subprocess.Popen(
                target_cmd, env=env, stdout=out,
                stderr=subprocess.STDOUT, start_new_session=True
            )
        processes.append(target)

        wait_for(server, server_log, HOST_JOIN, deadline,
                 "dedicated server CIHost join")
        wait_for(server, server_log, TARGET_JOIN, deadline,
                 "dedicated server CITarget join")
        wait_for(target, target_log, GRAPHICAL, deadline,
                 "target graphical client")
        wait_for(host, host_log, REMOTE, deadline,
                 "genuine host world nonlocal EntityPlayer for 20 ticks")

        if args.wall_veto:
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
            send_server_command(
                server, server_log,
                "scoreboard players test CITarget taken 0 0",
                expected=b"score 0 is in range 0 to 0")
            print("CUSTOMMC_189_SERVER_TARGET_DAMAGE_BASELINE_ZERO_PASS=YES",
                  flush=True)
            send_server_command(server, server_log, "gamemode 0 CIHost")
            send_server_command(server, server_log, "gamemode 0 CITarget")
            send_server_command(server, server_log, "gamerule naturalRegeneration false")
            # Vanilla FLAT overworld with y=5. CIHost facing +Z.
            # Stationary CITarget 2 blocks ahead, genuinely in PvP reach.
            send_server_command(server, server_log, "tp CIHost 0 5 0 0 0")
            send_server_command(server, server_log, "tp CITarget 0 5 2 180 0")
            print("CUSTOMMC_189_SERVER_REAL_PLAYER_PVP_POSITIONS_SET=YES",
                  flush=True)
            wait_for(host, host_log, AURA_DISABLED, deadline,
                     "20 disabled genuine nonlocal player target callbacks")
            wait_for(host, host_log, AURA_ENABLED, deadline,
                     "normal real-player Kill Aura module enable")
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

        print("CUSTOMMC_OFFICIAL_189_LOOPBACK_ONLY_SERVER=YES", flush=True)
        print("CUSTOMMC_OFFICIAL_189_TWO_REAL_CLIENTS_JOINED_PASS=YES",
              flush=True)
        print("CUSTOMMC_OFFICIAL_189_REMOTE_PLAYER_KIND_SUSTAINED_PASS=YES",
              flush=True)
        if args.pvp:
            print("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_PLAYER_PVP_PASS=YES",
                  flush=True)
        elif args.range_veto:
            print("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_RANGE_NEGATIVE_PASS=YES",
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
    group = parser.add_mutually_exclusive_group()
    group.add_argument("--pvp", action="store_true",
                       help="prove Kill Aura attack and real server damage statistic")
    group.add_argument("--range-veto", action="store_true",
                       help="prove real out-of-range player cannot be attacked")
    group.add_argument("--wall-veto", action="store_true",
                       help="prove native LOS-veto for in-range player behind server wall")
    group.add_argument("--fov-veto", action="store_true",
                       help="prove in-range real player behind camera is excluded")
    args = parser.parse_args()
    run(args)


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("CUSTOMMC_OFFICIAL_189_LOCAL_MULTIPLAYER_FAILED: "
              + str(error), file=sys.stderr)
        sys.exit(1)
