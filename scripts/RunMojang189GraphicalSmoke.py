#!/usr/bin/env python3
"""Run the actual 1.8.9 CustomMC Minecraft client under a real X11 display.

CI acceptance requires a loaded LWJGL OpenGL Display, three genuine
Minecraft/CustomMC frame callbacks and a still-running game process.
The test never logs credentials, joins servers, or persists Mojang files.
"""
import argparse
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import time

MARKER = "CUSTOMMC_OFFICIAL_189_OPENGL_FRAME_PASS=YES"
REQUIRED_OVERLAY = (
    "bootstrap.jar", "core.jar", "platform-api.jar", "asm.jar",
    "platform-1.8.9.jar"
)


def required_file(raw):
    path = Path(raw).resolve(strict=True)
    if not path.is_file():
        raise ValueError("expected regular input file: " + str(path))
    return path


def build_command(java, client, manifest, assets, natives, overlay, game):
    java = required_file(java)
    client = required_file(client)
    manifest = required_file(manifest)
    assets = Path(assets).resolve(strict=True)
    natives = Path(natives).resolve(strict=True)
    overlay = Path(overlay).resolve(strict=True)
    if not assets.is_dir() or not natives.is_dir() or not overlay.is_dir():
        raise ValueError("expected staged asset/native/runtime directories")
    required_file(assets / "indexes" / "1.8.json")
    native_files = tuple(natives.glob("*.so"))
    if len(native_files) < 2:
        raise ValueError("missing Mojang Linux native shared libraries")
    urls = [required_file(overlay / name) for name in REQUIRED_OVERLAY]
    libraries = []
    for line in manifest.read_text(encoding="utf-8").splitlines():
        if not line or not line.strip():
            raise ValueError("empty line in official library manifest")
        path = required_file(line)
        if not path.is_relative_to(manifest.parent):
            raise ValueError("managed library path escaped transient verified directory")
        libraries.append(path)
    if len(libraries) < 12:
        raise ValueError("incomplete Mojang managed library classpath")
    urls.extend(libraries)
    urls.append(client)

    game = Path(game).resolve()
    game.mkdir(parents=True, exist_ok=False)
    (game / "options.txt").write_text(
        "fullscreen:false\nmaxFps:60\nrenderDistance:2\n"
        "fancyGraphics:false\nparticles:2\n",
        encoding="utf-8"
    )
    return [
        str(java), "-Xms256m", "-Xmx1536m",
        "-Djava.awt.headless=false",
        "-Dorg.lwjgl.librarypath=" + str(natives),
        "-Djava.library.path=" + str(natives),
        "-Dcustommc.acceptance.reportGraphicalFrame=true",
        "-cp", os.pathsep.join(str(path) for path in urls),
        "dev.trexzo.custommc.bootstrap.CustomMcBootstrapMain",
        "--custommc-runtime",
        "dev.trexzo.custommc.platform.v1_8_9.Minecraft189BootstrapInitializer",
        "net.minecraft.client.main.Main",
        "--username", "CIProbe",
        "--version", "1.8.9",
        "--gameDir", str(game),
        "--assetsDir", str(assets),
        "--assetIndex", "1.8",
        "--uuid", "00000000000000000000000000000000",
        "--accessToken", "0",
        "--userProperties", "{}",
        "--userType", "mojang",
        "--width", "854",
        "--height", "480",
    ]


def smoke(command, timeout):
    if not os.environ.get("DISPLAY"):
        raise ValueError("no X11 DISPLAY; graphical acceptance must use Xvfb")
    if timeout < 10 or timeout > 180:
        raise ValueError("invalid graphical smoke timeout")
    env = dict(os.environ)
    env["LIBGL_ALWAYS_SOFTWARE"] = "1"
    env["MESA_GL_VERSION_OVERRIDE"] = "2.1"
    # No launcher account credentials or online-auth environment inherited.
    env.pop("CUSTOMMC_ACCESS_TOKEN", None)
    env.pop("JAVA_TOOL_OPTIONS", None)

    with tempfile.TemporaryDirectory(prefix="custommc-189-graphical-") as temp:
        logfile = Path(temp) / "stdout.txt"
        observed = False
        process = None
        try:
            with logfile.open("wb") as output:
                process = subprocess.Popen(
                    command, env=env, stdout=output,
                    stderr=subprocess.STDOUT, start_new_session=True
                )
                start = time.monotonic()
                first_observed = None
                while time.monotonic() - start < timeout:
                    status = process.poll()
                    if status is not None:
                        raise AssertionError(
                            "Minecraft exited before sustained OpenGL acceptance; exit="
                            + str(status)
                        )
                    # Keep marker checking bounded; no streaming proprietary data.
                    with logfile.open("rb") as reader:
                        reader.seek(max(0, logfile.stat().st_size - 100000))
                        tail = reader.read()
                    if MARKER.encode("utf-8") in tail:
                        if first_observed is None:
                            first_observed = time.monotonic()
                        if time.monotonic() - first_observed >= 2.0:
                            if process.poll() is not None:
                                raise AssertionError("Minecraft exited after frame marker")
                            observed = True
                            break
                    time.sleep(0.25)
                if not observed:
                    raise AssertionError("no live LWJGL Display frame within smoke timeout")
            print("CUSTOMMC_OFFICIAL_189_GRAPHICAL_DISPLAY_SUSTAINED_PASS=YES")
            print("CUSTOMMC_OFFICIAL_189_REAL_PROCESS_USED=YES")
            print("CUSTOMMC_OFFICIAL_189_MULTIPLAYER_TESTED=NO")
        except BaseException as failure:
            if logfile.exists():
                data = logfile.read_bytes()[-7000:]
                # Tail is diagnostic only; no binary content exposed.
                print("GRAPHICAL_SMOKE_OUTPUT_TAIL_START", file=sys.stderr)
                print(data.decode("utf-8", "replace"), file=sys.stderr)
                print("GRAPHICAL_SMOKE_OUTPUT_TAIL_END", file=sys.stderr)
            raise
        finally:
            if process is not None and process.poll() is None:
                process.terminate()
                try:
                    process.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait(timeout=5)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("java", "client", "libraries", "assets", "natives", "overlay", "game"):
        parser.add_argument("--" + name, required=True)
    parser.add_argument("--timeout", type=int, default=90)
    args = parser.parse_args()
    cmd = build_command(
        args.java, args.client, args.libraries, args.assets,
        args.natives, args.overlay, args.game
    )
    smoke(cmd, args.timeout)


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("CUSTOMMC_OFFICIAL_189_GRAPHICAL_SMOKE_FAILED: " + str(error),
              file=sys.stderr)
        sys.exit(1)
