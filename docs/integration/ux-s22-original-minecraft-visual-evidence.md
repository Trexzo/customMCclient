# UX S22 — real Minecraft 1.8.9 visual evidence, not mockups

## Independent UI chain / scope

Stack exclusively on S21 [#852](https://github.com/Trexzo/customMCclient/pull/852) at exact head `b3d6bcad30b83a1b3521b13abdf5acd48006c20d`. No combat production classes, keybind controller, player/World/controller state machine, Java client JAR, personal accounts, credentials or `main` are modified. [Native font-width PR #848](https://github.com/Trexzo/customMCclient/pull/848) remains independent.

## Gap addressed

Previous UX S16–S21 source tests proved UI commands and behavior, but **could not prove the actual LWJGL2/OpenGL Minecraft 1.8.9 image looked good**. The existing official native suite launched genuine Mojang game windows but discarded the frame on process termination.

S22 adds **opt-in evidence collection** on that exact original runtime: `RunMojang189SingleplayerSmoke.py --visual-capture-dir <folder>` runs the existing genuine Mojang Java8/Xvfb singleplayer world fixture, with strict original World tick and native GUI transitions. It activates the exact live Minecraft window through X11, captures `world.png` before overlay, sends physical Right Shift (the existing actual ClickGUI key), saves `modules.png`, then sends a real Down key and saves `settings.png`. It closes the overlay with Right Shift before process exit.

Python `mss==10.1.0` captures raw RGB directly from the original X11 window. A conservative Graphite color witness compares the before/after real pixel grid and refuses to call `modules.png` a verified Graphite panel when the expected palette does not appear. The script checks PNG signatures and sizes, records SHA256, exact dimensions and real palette hit counts for each file. This is **not** an artificial HTML/JVM rendering or CSS approximation.

The existing original-binary workflow now includes a separate S22 step after graphical smoke and uploads the actual PNG files as a 14-day artifact `custommc-189-real-graphite-visuals`. Upload is marked always() to retain negative images from a failure. The visual step itself is strict (no `continue-on-error`), so a missing original GUI evidence witness remains a real test failure.

## How to review genuine images

1. Open the S22 GitHub Actions run for **Official Minecraft 1.8.9 Binary Preflight**.
2. Download the workflow artifact `custommc-189-real-graphite-visuals` (three PNGs, if all captures reached completion).
3. Compare `world.png` against `modules.png`, then inspect `settings.png`: native text legibility, Graphite no-Nebula colors, module row density, clip boundaries and relative spacing.
4. Compare against genuine Vape/Slinky/Rise references visually rather than counting code branches. The PNG artifact by itself does not prove page navigation or aesthetic quality; the actual pixels require human review. A Down key is sent through original Minecraft input; the screenshot's text determines whether Settings navigation really succeeded.

The first S22 capture uses the existing native harness fixed 854×480 window and `guiScale:1`. Multiple scales, actual manual Settings selection and module detail screenshots must be added after this probe proves reproducible. Do not claim all scales or final visual acceptance until those images exist.

## Strict acceptance separation

- Existing source-original GUI/World/X11 checks stay intact.
- `scripts/test_mojang189_visual_evidence.py` tests geometry rejection, pixel evidence, before/after rejection and mismatch.
- On-screen screenshots are generated only by the pinned game test. No screenshot exists at PR creation time.
- Shader color recognition is a **corroboration**, not a substitute for checking the images or the original-game input routes.
- If native compilation or test provisioning fails before the S22 step, no screenshot is claimed.
