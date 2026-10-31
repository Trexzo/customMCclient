# UX S33 — preserve Minecraft 1.8.9 FontRenderer texture binding across native HUD passes

## Actual evidence

The S31 exact-head original two-player PNG at 854×480 shows the native ArrayList legible, but Target HUD text samples look like unrelated colored atlas fragments. The separate S32 diagnostic disabled only Target HUD's scissor and repositioned its card, using the same **genuine two-client Mojang source window**. The glyph corruption persisted. Therefore scissor clipping and bottom-screen hotbar overlap are **not** the primary font corruption.

## Mechanism to test

Our `Lwjgl2LegacyGlApi` wraps independently scheduled native HUD passes in `glPushAttrib(GL_TEXTURE_BIT)` and `glPopAttrib`. Vanilla Minecraft 1.8.9 `GlStateManager` caches its texture binding in Java, but raw OpenGL attribute restoration changes the actual binding **without updating that Java cache**. This can leave the next `FontRenderer.drawString` believing `font.png` is already bound, when a completely different texture actually is. The colored-icon look in the original screenshot is consistent with sampling the wrong atlas. This is a hypothesis until a new genuine screenshot proves it.

## Narrow production change

Remove only `GL_TEXTURE_BIT` from the LWJGL2 host's GL attribute snapshot, while still restoring model/projection matrices, enable flags, color, scissor, blend settings and line state. The existing original native font binding can then remain consistent with the Java-side `GlStateManager` cache from one HUD pass to the next. No new fonts, no shadow/fake text, no texture upload, and no combat or world changes.

For apples-to-apples original-game proof, the isolated screenshot fixture positions the real Target HUD at logical Y=84, well away from the hotbar. Production and persisted positions remain unchanged. Unlike S32, the **native text scissor stays enabled** in this comparison.

## Acceptance

1. Exact-head Windows + Ubuntu Foundation CI and official Mojang 1.8.9 Binary Preflight must pass.
2. Download the genuine two-player PNG and visually compare text glyphs to S31/S32: colored atlas patches should be replaced by readable `NEARBY` and source-distance labels, with healthy ArrayList and Box ESP.
3. Check that original vanilla textures (hotbar, player, sky, GUI) remain intact. If the defect remains, stop and investigate vanilla texture cache active unit before considering merge.
4. Keep draft/unmerged pending native visual evidence. CI green alone is insufficient to certify the glyph fix.
