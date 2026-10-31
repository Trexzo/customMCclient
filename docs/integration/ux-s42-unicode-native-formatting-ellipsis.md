# UX S42 — Unicode- and Minecraft-format-safe native HUD ellipsis

## Reference and defect

The real 854×480 Minecraft 1.8.9 native S41 screenshot confirmed an unchanged restrained ArrayList and Target HUD with native Mojang font widths, light text shadow and the original-game watermark. S36, S39 and S41 now share `Minecraft189ArrayListLayout.fitLabel` to shorten lengthy UI strings, but the old loop decremented one **UTF-16 char** at a time. A truncation could leave half of a supplementary Unicode glyph (e.g., 💎), cut inside a Minecraft `§a` format pair or append dots after an invisible trailing style token. It also measured `width(prefix)+width("...")`, which is not always the same as **native** `FontRenderer.getStringWidth(prefix+"...")` because a `§l` bold code in the prefix also affects the appended dots.

## Source-preserving correction

- Keep the same original Mojang `FontRenderer` rendering, S35 FPS-independent motion, S37 11px row rhythm, S38 native shadow and S39/S41 source-preserving text fitting. No new font or artwork.
- When an ellipsis is needed, decrement over **complete UTF-16 code point boundaries**, skip incomplete `§` codes and trim complete format pairs that would be left dangling with no visible glyph. Trim trailing ASCII spaces in a shortened label just as before.
- Ask the real native `measureText(prefix + "...")` for **the actual complete candidate**, not the sum of measurements of two separate fragments. In-game bold or color tokens can carry formatting across the cut.
- Never rewrite any enabled module name, settings, saved watermark label, player target identity, distance, coordinates or rotations. If a label fits, return it unchanged. Existing clip bounds remain active as a separate safety net.
- Shared utility automatically corrects ArrayList, Watermark and Target HUD; no other combat, world, packet or GUI input changes.

## Proof

- Deterministic tests check supplementary 💎 under narrow and wide widths, `§a` and `§l` truncation, retained visible style prefixes, and an extremely small ellipsis-only window.
- 1,500 seeded randomized combinations of valid Minecraft formatting codes, Unicode symbols, ASCII glyphs and spaces are fitted under randomized pixel budgets against a fake font that models bold inheritance. Reject any result wider than the actual offered width, incomplete surrogate, trailing lone `§`, or invisible `§a`-only suffix.
- Original Minecraft 1.8.9 screenshot must remain unchanged for the common ASCII labels in the existing real two-player native visual test. Long names and unusual formatting are covered by targeted tests rather than falsely claiming the ordinary screenshot exercises those inputs.
- Require exact-head Foundation Windows/Ubuntu and full original binary preflight, including S40 watermark, S41 Target HUD and independent combat gates. Keep S42 **draft/unmerged** stacked on S41 #891.
