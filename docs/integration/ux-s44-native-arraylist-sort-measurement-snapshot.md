# UX S44 — source-native ArrayList font measurement snapshot

## Reference and defect

The original Mojang 1.8.9 screenshot (S40–S43) verifies the restrained, right-aligned ArrayList layout inspired by Slinky and Rise: native Minecraft glyphs, 11 logical-pixel rows, light native text shadow and narrow accent edge, with optional grouped categories and user-owned labels. In S36, exact native Minecraft `FontRenderer.getStringWidth` replaced approximate widths for sorting. However, S36–S43 invoked that renderer inside `Comparator.compare`, repeatedly measuring the same enabled module labels on every sort comparison (**O(N log N)** native font calls). It also re-fetched display descriptors/labels in comparator and again when drawing each row.

## S44 change

- Build a **one-frame presentation snapshot** from the actual module registry and controller enabled states, containing the original module id, resolved label, category order and exactly one native `FontRenderer` width measurement per enabled module.
- Use a stable Java sort on the immutable per-frame width/category values: categories only when grouping is enabled, otherwise descending native width; equal widths retain source registry order.
- Reuse the captured label and module id when drawing and retrieving S43 entrance phases. The existing actual layout/render path still measures the final fitted label for text placement; the optimization is specifically sorting/descriptor lookup rather than silently making clipping approximate.
- Preserve real source module authority and enabled-state inclusion, existing `maxVisible`/overflow count, source-original FontRenderer, 11px row pitch, motion, shadow, native GL clip and all saved settings. No new setting or font assets.

## Verification

- Runtime host test with five real enabled source modules and `maxVisible=1` checks exactly **one native sort width probe** for each of the four undrawn labels, three calls for the single displayed label (one sort, one fitting, one final alignment), and original Watermark's own native width separately.
- Turn grouping and category display back on within the same source runtime and assert original text ordering and **three calls per visible label** rather than repeated comparator calls.
- Preserve existing tests for mixed category modes and equal-width stable labels. Require exact-head Windows/Ubuntu Foundation and full original Mojang Minecraft 1.8.9 Binary Preflight, including genuine two-player screenshot. The settled screenshot validates unchanged pixels, not performance; call-count tests validate reduced sorting workload.
- PR remains **draft and unmerged**, stacked on S43 #893; no combat, packets or player targeting changes.
