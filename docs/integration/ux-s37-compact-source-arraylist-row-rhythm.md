# UX S37 — compact ArrayList vertical rhythm with original Mojang font

## Source and visual reference

The actual 854×480 original Minecraft 1.8.9 native S36 screenshot shows three properly right-aligned, readable ArrayList labels in the top right, with the original Mojang glyphs, while Target HUD / actual remote player Box ESP remain visible. The earlier documented S29 comparison favored a **minimal Slinky-like text stack** and **Rise-like compact enabled-module alignment** over box-heavy arrays, oversized neon backgrounds, or Nebula styling. S36 also removed incorrect hand-estimated glyph widths on the genuine original client. It must remain intact.

The remaining small spacing discrepancy: native Minecraft glyph rows occupy ~9 logical pixels while ArrayList was using **12** logical pixels between rows. That creates a taller stack than required for the reference-inspired compact text-only style.

## Exact S37 change

- Set **11 logical pixels** of ArrayList vertical pitch (9-pixel glyph + 2-pixel spacing).
- Share `Minecraft189ArrayListLayout.LINE_HEIGHT` between the renderer's Y step and the layout's capacity/overflow calculation: they cannot drift when a GUI scale or window size changes.
- Leave 11px native clip/chip envelope, right-edge accent, original Minecraft font render + actual width, user X/Y settings, text colors, source enabled-state ownership and S35 FPS-independent entrance motion unchanged.
- Test source-rendered positions for 1280×720, densely packed 120px viewport, original 854×480 and one-row saved Y=4096 recovery. Overflow must still count only genuine enabled modules, never fictitious entries.

## Native admission

Require exact-head Windows/Ubuntu Foundation CI and **genuine original Mojang 1.8.9 Binary Preflight**, including actual two-player HUD/ArrayList screenshot, `REAL_ARRAYLIST_NATIVE_FONT_METRICS_PASS`, and every existing PvP/Scaffold native gate. A still frame can show native rendered spacing and legibility, **not** S35 animation timing; do not infer that from a screenshot.

S37 is stacked on S36 #886, in a separate draft PR; no merge, no user-data migration and no changes to the independent combat development branch.
