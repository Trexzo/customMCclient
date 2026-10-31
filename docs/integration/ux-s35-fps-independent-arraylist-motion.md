# UX S35 — frame-rate-independent ArrayList entry motion

## Source/design reference

The existing S29 visual review chose an understated **Slinky-style text-first enabled-module stack** with **Rise-style compact right alignment / easing**, without copying proprietary assets. That earlier implementation advanced each newly enabled row by a fixed 0.22 **per drawn frame**. Its total entrance time therefore varied with rendering FPS (for example, radically different at 30 vs 144 FPS), while the original X11 game screenshot from S34 already proved text, font, and HUD position legible. S35 changes *timing only*. Fonts, labels, background preference, right-alignment, source-enabled modules, and the Graphite palette are unchanged.

## Change

- The module's animation snapshots now use `System.nanoTime` **elapsed nanoseconds** with a ~180-ms full entrance rather than counting HUD redraws.
- Keep the already established first-visible opacity/slide phase at 0.22, smoothstep spatial easing, and exact fully visible initial module-list snapshot. The first rendered frame after a newly enabled module is still visible.
- Cap any positive elapsed interval at 200 ms, refuse negative/backward clock differences, and reset the time base together with the motion map when the ArrayList module is disabled.
- Disabling removes the entry immediately (no ghost or false-enabled list). Reduced-motion `animate=false` remains immediate at full opacity, with no new persistence IDs or defaults.
- An injectable package-private `LongSupplier` clock makes offline animation tests deterministic without test-only flags or sleeps in production.

## Acceptance/evidence

- `Minecraft189ArrayListMotionTest` drives identical elapsed time at 30 and 144 calls/sec; proves matching phase, bounded stall, reversed timestamp, initial fully visible, disabled immediate, snapshot immutable and reduced motion.
- `Minecraft189ArrayListModuleTest` retains actual platform UI text and state assertions, but must **not** assume five render calls equal 180 ms; time completion is proved by the deterministic clock-driven test.
- Require Windows and Ubuntu Foundation CI and official Mojang 1.8.9 binary preflight at the exact branch head. The original S34 two-player PNG remains useful for default appearance; this static PNG **cannot prove runtime animation timing**, only the code/tests can. No gameplay or combat module changes.

S35 stacks on S34 #884. Keep draft, do not merge until exact-head native/full CI is checked, including the unrelated but mandatory concurrent-PvP regression.
