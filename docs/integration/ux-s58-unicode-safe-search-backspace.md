# UX S58 — Unicode-safe Backspace for native ClickGUI search

## Source defect
The focused `SearchFocusTarget` (S57) removed `query.length()-1` on plain Backspace. This removes a **UTF-16 code unit**, not a Unicode code point, and can strand the high surrogate of an emoji or another supplementary Unicode symbol after the next keypress. The existing input bridge exposes character events, so users can type a valid surrogate pair as two events.

## Change
- Plain Backspace uses Java 8 `String.offsetByCodePoints(query.length(), -1)` to select the previous complete Unicode code point. For ASCII and ordinary BMP characters the behavior is unchanged.
- S57 Ctrl+Backspace continues to remove the trailing token; no change to typed characters, focus, search limits, filtering, S56 scroll, game packets or display pipeline.
- A real controller regression types a supplementary symbol via two char events, confirms removal in one Backspace, follows with space deletion and Ctrl+Backspace, and checks focus ownership and GUI openness.

The code-point fix does not claim grapheme-cluster segmentation for combining-character sequences and does not alter the input pipeline's 64-UTF-16-unit length limit. All changes remain in source core with no third-party fonts/assets. Require exact-head Foundation Windows/Ubuntu and original Mojang Minecraft 1.8.9 binary preflight. Draft/unmerged, stacked on S57 #910.
