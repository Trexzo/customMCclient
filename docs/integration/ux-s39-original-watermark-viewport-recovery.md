# UX S39 — original Minecraft 1.8.9 watermark stays visible after GUI scaling

## Evidence and design reference

Existing S29–S38 Slinky/Rise-inspired HUD work keeps a source-native minimalist enabled-module ArrayList and genuine Minecraft font. Its `Minecraft189ArrayListLayout` already clips overlong names, recovers saved positions when 854×480 windows or GUI scaling make them off-screen, and shares native font widths when available. Target HUD has its own viewport clamp. By contrast, `Minecraft189WatermarkModule` still rendered its saved absolute X/Y directly; shrinking the window after saving X/Y=4096 made the brand label disappear completely. A plain text watermark is preferable here to inventing a new overlay.

## Implementation

- Keep exact vanilla source font, original white text, priority, customizable label, persisted X/Y setting IDs, and default at logical (8,8). **No additional background, gradients, font files or Nebula theme.**
- Reuse the already tested original-GUI `safeLeft` / `safeTop` recovery calculations, without overwriting saved coordinates, to recover visible text after GUI scaling.
- Use native `FontRenderer.getStringWidth` through the existing S36 callback to fit the label into the viewport at its effective X. Test-only hosts retain the historical conservative estimate. Ellipsize only the displayed label; the persisted custom string stays unchanged.
- Use an 11px high source-HUD clip around text to avoid drawing stray glyphs beyond the actual screen. Preserve balanced begin/end UI and clipping even when native draw throws. `Minecraft189HostRuntimeTest` tests the exact small original window shape (854×480, GUI scale 2), long text, unchanged saved X/Y and original 1280×720 appearance.
- No combat state, target UUID, input or network code changes.

## Release gates

S39 stacks on S38 PR #888 and remains a separate draft. Require exact-head Windows + Ubuntu Foundation CI and original Mojang 1.8.9 Binary Preflight, including the existing source-genuine two-player HUD, original font, ArrayList pitch, soft shadow and independent PvP gates. A static default screenshot by itself cannot prove recovery from an offscreen saved position; that is covered by the direct runtime regression. Do not merge before exact-head full native success.
