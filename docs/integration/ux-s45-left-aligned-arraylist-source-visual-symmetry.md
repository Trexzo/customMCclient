# UX S45 — source-consistent left-aligned ArrayList styling

## Visual reference and defect

The verified S40–S44 original Mojang Minecraft 1.8.9 854×480 two-player screenshot shows the intentionally restrained right-aligned ArrayList, inspired by Slinky/Rise: exact native font width, subtle shadow, 11 logical pixel row pitch, a narrow 2×9px graphite accent rail, and an optional intrinsic text-length background chip (off by default). The same ArrayList has a persisted `rightAligned=false` left-side mode. Prior to S45 its left branch drew text only: no accent rail, and `showBackground=true` silently had no effect on that side.

## S45 change

- When left-aligned, keep the **existing exact user-configured text X** and entry slide; do not change saved x/y or native glyph positions.
- Draw the original source-theme **2×9px accent rail** to the left of each actual source-enabled module row, at the 8px leading inset when there is room. Skip a rail that would overlap an entering label or a user-configured x=4 label. Respect the original 4px viewport margin.
- If the already-persisted `showBackground` setting is enabled, draw the same subtle intrinsic measured-width **11px chip** (radius ≤2) that exists on the right, bounded to the actual logical viewport. Keep **showBackground=false as default**, so no extra backplate is introduced for existing profiles.
- Use existing `UiThemeSettings` colors (Graphite, Blue, etc.), existing `Minecraft189ArrayListMotion.withOpacity` and the same original host clipping and native Minecraft 1.8.9 font. Leave right-aligned path and source/renderer data untouched.

## Regression and acceptance

- A real platform runtime with three source-enabled modules checks left X=40 unchanged, rail x=32, three intrinsic chips only when requested, original Graphite/Blue theme accent colors, balanced native clip callbacks and persistent `rightAligned=false`/`showBackground=true` values.
- On a shrunk 854px window, a saved `x=4096` recovers text x=8 while preserving the stored preference; with x=4 no overlapping rail is painted. Existing right-aligned integration tests must still pass.
- Exact-head Windows+Ubuntu Foundation, plus full original Mojang 1.8.9 Binary Preflight with genuine native two-player right-aligned HUD screenshot and gameplay tests remain required. The original screenshot can prove **right-aligned nonregression**, not the opt-in left mode: the left layout is verified by source-runtime geometry and host-call tests.
- S45 is a separate **draft, unmerged** PR stacked on S44 #894. No combat, packets, gameplay control, target ownership, event timing or default user settings changed.
