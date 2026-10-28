#!/usr/bin/env python3
"""Capture real Mojang 1.8.9 X11 frames; no synthetic UI rendering.

Only used inside the independent visual-evidence mode of the real game
singleplayer smoke. Requires mss==10.1.0 on the Linux CI runner.
"""
import hashlib
from pathlib import Path
import re
import subprocess

# The source-rendered Graphite palette in core/.../UiThemes.java.
# Check color evidence before calling a frame an actual CustomMC GUI frame.
GRAPHITE = ((16, 17, 19), (25, 27, 31),
            (35, 38, 43), (48, 52, 59))


def parse_x11_geometry(output):
    """Require one bounded, positive, real X11 window geometry."""
    if not isinstance(output, str):
        raise ValueError("X11 geometry must be text")
    pairs = dict(re.findall(r"(?m)^(X|Y|WIDTH|HEIGHT)=(-?\d+)\s*$", output))
    if set(pairs) != {"X", "Y", "WIDTH", "HEIGHT"}:
        raise ValueError("missing X11 window geometry fields")
    x, y, width, height = (int(pairs[k]) for k in ("X", "Y", "WIDTH", "HEIGHT"))
    if not (200 <= width <= 3840 and 160 <= height <= 2160):
        raise ValueError("invalid real Minecraft window dimensions")
    if not (-4096 <= x <= 4096 and -4096 <= y <= 4096):
        raise ValueError("window location outside bounded X11 range")
    return {"left": x, "top": y, "width": width, "height": height}


def graphite_sample_count(rgb, width, height):
    """Sparse pixel probe, only evidence of the source Graphite palette.

    Sampling is inside the expected compact centered ClickGUI, away from
    the Minecraft scene's edges. Never fabricates pixels or native state.
    """
    if width <= 0 or height <= 0 or len(rgb) != width * height * 3:
        raise ValueError("invalid RGB frame dimensions")
    matches = 0
    sampled = 0
    for y in range(int(height * .18), int(height * .82), 8):
        for x in range(int(width * .31), int(width * .87), 8):
            offset = (y * width + x) * 3
            red, green, blue = rgb[offset:offset + 3]
            sampled += 1
            if any(max(abs(red - r), abs(green - g), abs(blue - b)) <= 7
                   for r, g, b in GRAPHITE):
                matches += 1
    return matches, sampled


def capture_original_x11(window, png_file):
    """Screenshot the exact original X11 game window, never a mock canvas."""
    from mss import mss, tools

    if not str(window).isdigit():
        raise ValueError("window must be a real positive X11 integer ID")
    try:
        raw_geometry = subprocess.check_output(
            ["xdotool", "getwindowgeometry", "--shell", str(window)],
            timeout=10, stderr=subprocess.STDOUT
        ).decode("utf-8", "replace")
    except subprocess.CalledProcessError as error:
        raise RuntimeError("original X11 game window disappeared") from error
    rectangle = parse_x11_geometry(raw_geometry)
    target = Path(png_file)
    target.parent.mkdir(parents=True, exist_ok=True)
    with mss() as capture:
        image = capture.grab(rectangle)
        rgb = image.rgb
        matches, sampled = graphite_sample_count(
            rgb, rectangle["width"], rectangle["height"])
        tools.to_png(rgb, image.size, output=str(target))
    png = target.read_bytes()
    if not png.startswith(b"\x89PNG\r\n\x1a\n") or len(png) < 1024:
        raise AssertionError("original X11 screenshot is not a valid PNG")
    return {
        "filename": target.name,
        "sha256": hashlib.sha256(png).hexdigest(),
        "bytes": len(png),
        "dimensions": [rectangle["width"], rectangle["height"]],
        "graphite_hits": matches,
        "graphite_samples": sampled,
    }


def require_visible_graphite(before, after):
    """Reject a screenshot labeled as ClickGUI when Graphite isn't seen."""
    if before["dimensions"] != after["dimensions"]:
        raise AssertionError("original game window changed dimensions")
    # A non-GUI world frame must not satisfy this native visual witness.
    # Keep threshold conservative to avoid resource-pack false positives.
    samples = after["graphite_samples"]
    if samples < 100 or after["graphite_hits"] < samples // 7:
        raise AssertionError(
            "no sufficient source Graphite palette in real X11 GUI screenshot")
    if after["graphite_hits"] < before["graphite_hits"] + samples // 12:
        raise AssertionError(
            "ClickGUI screenshot lacks new Graphite panel evidence vs world")
