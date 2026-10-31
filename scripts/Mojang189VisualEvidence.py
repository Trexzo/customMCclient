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


def original_target_hud_native_text_contrast(rgb, width, height):
    """Read only ORIGINAL framebuffer bytes inside the known S33 Graphite card.

    The fixed native CI window is 854x480, guiScale 2. The genuine
    original-client color #EE8755 is the thin Target HUD accent rail.
    Detect that rail from X11 pixels (not from mocked HUD data), then
    require near-neutral bright native-font pixels in BOTH actual text
    lines. S32's wrong texture atlas supplied only 3/3 such pixels versus
    S33's real 536/424 in these positions. Fail closed for absent/other
    resolution or missing evidence. This is a bounded visual smoke gate,
    not text recognition, a style score or an inference of target identity.
    """
    if width != 854 or height != 480 or len(rgb) != width * height * 3:
        raise AssertionError("native Target HUD pixel witness needs exact CI window")
    target = (238, 135, 85)
    rail = None
    for x in range(15, 100):
        if rail is not None:
            break
        streak = 0
        for y in range(40, 340):
            at = (y * width + x) * 3
            color = rgb[at:at + 3]
            if all(abs(color[i] - target[i]) <= 3 for i in range(3)):
                streak += 1
            else:
                streak = 0
            if streak >= 35:
                rail = (x, y - streak + 1)
                break
    if rail is None:
        raise AssertionError("actual native HUD accent rail absent in screenshot")
    x, y = rail

    def neutral_bright_count(x0, x1, y0, y1):
        count = 0
        for py in range(max(0, y0), min(height, y1)):
            for px in range(max(0, x0), min(width, x1)):
                offset = (py * width + px) * 3
                red, green, blue = rgb[offset:offset + 3]
                if min(red, green, blue) > 130 \
                        and max(red, green, blue) - min(red, green, blue) < 50:
                    count += 1
        return count

    # Native text inset + 12px logical from card left and corresponding
    # original FontRenderer baselines. Exclude the proximity meter and scene.
    first = neutral_bright_count(x + 21, x + 191, y + 11, y + 32)
    second = neutral_bright_count(x + 21, x + 191, y + 42, y + 65)
    if first < 80 or second < 80:
        raise AssertionError(
            "actual native Target HUD glyph/texture pixels implausible: "
            "nearby={} distance={}".format(first, second))
    return {"rail": [x, y], "nearby_neutral_pixels": first,
            "distance_neutral_pixels": second}


def capture_original_x11(window, png_file, require_target_hud_text=False):
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
    result = {
        "filename": target.name,
        "sha256": hashlib.sha256(png).hexdigest(),
        "bytes": len(png),
        "dimensions": [rectangle["width"], rectangle["height"]],
        "graphite_hits": matches,
        "graphite_samples": sampled,
    }
    if require_target_hud_text:
        result["native_target_hud_text"] = original_target_hud_native_text_contrast(
            rgb, rectangle["width"], rectangle["height"])
    return result


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
