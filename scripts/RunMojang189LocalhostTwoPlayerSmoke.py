#!/usr/bin/env python3
"""Real 1.8.9 dedicated server and TWO real Minecraft clients on loopback only.

Authenticates no accounts, opens no external listener, and uses no fake
EntityPlayer. This acceptance checks actual server joins and real client
nonlocal player-kind callbacks, NOT Kill Aura attack or PvP damage.
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
AURA_HURT = b"CUSTOMMC_OFFICIAL_189_KILLAURA_PLAYER_HURT_PASS=YES"


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

        if args.pvp:
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
            send_server_command(server, server_log, "gamemode 1 CIHost")
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
            wait_for(host, host_log, AURA_HURT, deadline,
                     "same native remote player hurt after Kill Aura click")
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
    parser.add_argument("--pvp", action="store_true",
                        help="prove Kill Aura player click and real server damage statistic")
    args = parser.parse_args()
    run(args)


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("CUSTOMMC_OFFICIAL_189_LOCAL_MULTIPLAYER_FAILED: "
              + str(error), file=sys.stderr)
        sys.exit(1)
