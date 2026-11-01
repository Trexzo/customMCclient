# UX S46 — optional source-native Watermark text shadow

## Reference and defect

The genuine Mojang Minecraft 1.8.9 screenshots from S40–S45 show the restrained original-font Watermark as white text and the ArrayList as compact text with a subtle native Minecraft shadow (S38). Unlike ArrayList, the Watermark lacks a user-selectable text-shadow option, so it can be hard to read over a bright game background. The existing native Mojang `FontRenderer.drawString(..., true)` adapter provides the correct understated shadow without new textures, shaders or font files.

## Narrow S46 change

- Add `render.watermark.textShadow` as a persisted Boolean in the **existing Watermark module** and ClickGUI settings. Default is **false** so prior profiles and the normal two-player original-game screenshot remain **pixel-identical**.
- When explicitly enabled, use the existing `LegacyUiHostCallbacks.drawTextShadow(UiFonts.DEFAULT,...)` from the **same Watermark render pass**. This flows into the native Mojang 1.8.9 FontRenderer adapter on the real client, and uses no separate renderer, GL state mutation or background overlay.
- Keep S39/S40 safe X/Y recovery, all text fitting, original font, color, clip boundaries, profile values, render priority, module ownership and cleanup unchanged.
- Register the setting in all three source registries: setting values, Boolean presentation, and per-module binding; close them in strict reverse order on normal close and installation rollback.

## Acceptance

- Source platform test with actual Watermark enabled asserts default text `CustomMC` at 8,8 and **zero shadow draws**, then toggle on with **one native-shadow draw**, persisted true, original glyph location unchanged and balanced clips.
- Test stored X/Y 4096 in 854×480 viewport: recovered draw at 8,464 without mutating saved values, still only one optional shadow. Turning off restores zero shadow draws; disabling module removes watermark; shutdown closes the setting registration.
- Exact-head Windows and Ubuntu Foundation tests, plus genuine original Mojang 1.8.9 full binary preflight (including S40 Watermark recovery X11 pixels and native HUD/ArrayList). The original screenshot proves unchanged **default-off** presentation, **not** optional shadow's actual original-game pixels; the opt-in path is covered by source runtime and existing S38 native drawString shadow adapter tests.

S46 is a separate draft/unmerged visual-only PR stacked on S45 #895. No combat, packets, player authority, gameplay logic or default user values changed.
