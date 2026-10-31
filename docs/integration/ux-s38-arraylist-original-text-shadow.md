# UX S38 — native Minecraft FontRenderer soft shadow for ArrayList

## Visual reference and concrete issue

The original 854×480 Mojang Minecraft 1.8.9 two-player S36/S37 screenshots confirmed CustomMC's minimal right-aligned enabled-module ArrayList: readable original font, true native text widths and 11 logical pixel vertical pitch. Against light sky, thin white glyphs have low local contrast. The reference review for Slinky/Rise/Vape favors restrained, text-first visual hierarchy over large identical backplates or elaborate colored gradients.

## Implementation

- Introduce only the native source-backed Minecraft 1.8.9 shadow: `FontRenderer.drawString(text,x,y,color,true)`. This is not a new font, texture, rasterizer or generated asset. The original Java client already exposes the exact method through `Minecraft189FontRendererAccess`.
- Scoped **only to ArrayList row labels**. Watermark, Target HUD, ClickGUI and other modules still call `drawText(...)`, which forwards `shadow=false`. Preserve existing palette, 11px spacing, y/x, category sorting, original width measurements, S35 time-based animation, enabled module state and clips.
- New persisted boolean `render.array-list.textShadow`, default **true** for readable fresh profiles, exposed as `Text Shadow` in the ArrayList module's ClickGUI settings. Explicit false uses the legacy shadowless path. The entire setting registration/presentation/binding is closed on normal shutdown and rolled back on failed install.
- `LegacyUiTextRenderer` and `LegacyUiHostCallbacks` offer compatible default no-shadow fallback for alternate/test hosts; the production LWJGL host calls the native renderer with `shadow=true` and enforces the same open-frame/no-shape-batch GL state as plain text.
- Native receipt `CUSTOMMC_OFFICIAL_189_REAL_ARRAYLIST_NATIVE_SHADOW_PASS=YES` is emitted **only after original FontRenderer.drawString(..., true) returns** inside the Mojang font adapter under original-game visual acceptance flag. The genuine two-player screenshot step must see this marker (plus existing real enabled-state, measured text, original framebuffer screenshot gates). A source/mock render can never substitute for this native receipt.

## Test and safety gates

- Native renderer unit tests assert shadow flag `true`, normal text `false`, text/position/color pass-through and unsupported font veto.
- Actual module-runtime tests check two native-shadow row requests, preserved real enabled rows and encoded default; opting out switches to plain text while preserving labels/positions.
- Exact-head Foundation Windows/Ubuntu and complete original Mojang 1.8.9 binary workflow remain mandatory; a still screenshot demonstrates visual legibility but cannot alone prove render calls or contrast across resource packs.
- Stacked on S37 #887 in a separate **draft PR**; no merge or combat/world/packet changes.
