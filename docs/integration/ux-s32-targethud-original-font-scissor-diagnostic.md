# UX S32 — isolate the genuine Target HUD font/scissor fault

## Grounded original-game finding

S31 PR #881 exact-head `699aad785c9095009d70df813eb1c80a80237e0f` passed Foundation Windows/Ubuntu and complete genuine Minecraft 1.8.9 Binary Preflight. Its real 854×480 X11 two-player PNG shows a **source-rendered Target HUD over the native hotbar**, with scrambled-looking glyph patches in its two text lines, even though the ArrayList using the same native font is readable. Thus green native passes do not establish visual quality. The screenshot is not synthetic.

## Narrow diagnostic

In the **dedicated-server two-original-client acceptance process only**, set `captureRealHudVisuals` and the additional diagnostic property `diagnoseTargetHudNoTextClip`. The first property already enabled the normal modules. The second temporarily skips just the Target HUD's native GL scissor around the text, maintaining the source text values, original font, card shape, glyph colors and render pass. The original scissor remains active for all production/player launches. The acceptance bootstrap places the Target HUD at Y=84 logical pixels to remove native hotbar overlap *for comparison only*, without modifying default persisted profile settings. The rest of the original game, source-native player matching and UUID/server framebuffer gates remain unchanged.

## Acceptance

- Compare S31 production-clipped screenshot against this S32 test-only unclipped screenshot to determine whether native scissor is the culprit. Since position moves too, this comparison can establish whether glyph corruption persists but cannot isolate layout coordinates alone.
- Do not call S32 production-ready or merge this diagnostic. It is intentionally an instrumented branch, with full existing Windows/Ubuntu and original Minecraft 1.8.9 tests required. If the problem remains, diagnose native texture state/font atlas binding separately.
- No combat/world/packet changes, no copied visuals, no theme change, and no merge into `main`.
