# UX S41 — exact native-font bounded text in Target HUD

## Real original-game reference

The S40 genuine Mojang Minecraft 1.8.9 854×480 two-player screenshot shows the existing restrained Target HUD at the left: a thin native font, muted source index, primary real distance, and a narrow distance-only accent meter. The current clipping rectangle keeps drawing inside the card, but when live XYZ coordinates or distance/direction labels are very long, it can cut a glyph mid-word without any overflow indicator. The original Slinky/Rise-inspired text-first design should not add large rectangles or alter the source target.

## Scope

- Keep **all** existing Target HUD card sizes, actual player/index and rotation authority, meter, theme, saved X/Y, user toggles and current native font; no combat changes.
- For each existing source label (NEARBY index, distance, optional direction, optional XYZ), use the real original `FontRenderer.getStringWidth` via the S36 typed callback **during** the existing HUD render pass, with the existing conservative estimator only on test/alternate hosts returning -1.
- Fit the displayed text to the already-existing card text width using the proven S36 `Minecraft189ArrayListLayout.fitLabel` helper; overflow displays a bounded `...` rather than disappearing mid-number. Leave the exact full source distance, coordinates, target index and yaw/pitch **unchanged**.
- Existing native GL clip remains a second, fail-safe framebuffer boundary; it is not replaced by text fitting. The S40 original-game regression screenshot for standard 4.0m nearby player must remain pixel-identical because all its labels fit already.
- Unit regression configures a real target at very large finite world coordinates in the module runtime, injects a deterministic custom native-width probe and checks all text fits the compact 184 logical pixel expanded label region while full source remains intact. All existing compact/full mode tests remain mandatory.

## Verification and release

S41 is stacked on S40 #890 in a separate draft. Require exact-head Windows/Ubuntu Foundation CI and complete original Mojang Minecraft 1.8.9 Binary Preflight, including the genuine two-player screenshot, native shadow/width, S40 Watermark recovery and all unmodified combat/security gates. The ordinary native screenshot demonstrates no regression in usual Target HUD glyphs; it does **not** simulate huge coordinates. The source-level extreme-coordinates runtime regression tests that distinct behavior.
