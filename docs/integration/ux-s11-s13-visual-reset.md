# CustomMC Visual Reset S11–S13

This is presentation-only code stacked on UX S10 PR #828.

## Real changes
- Nebula default palette with Frost/Mono immutable programmatic alternatives.
- ClickGUI shell: shadow layer, distinctive CustomMC monogram/wordmark, accent trim, sidebar and search refinements.
- Module, setting and detail pages: consistent card outlines, inset values and active accent rail.
- HUD Layout editor: coordinated preview and existing reversible placement controls.
- ArrayList: actual right-side anchored dark rounded cards and clipped labels; persistent Align Right setting in the registered feature, reversible to original left-X behavior. Existing category/group/overflow source behavior remains.
- TargetHUD: redesigned border and surface colors, blue/violet adaptive accent, unchanged strict source-matched distance/rotation/proximity semantics.

## Accurate limits
- 1.8.9 native font does not expose measured text widths through the current host callback. Cards have fixed 184-pixel width; they are not yet width-sorted, character-perfect aligned or animated.
- No authoritative target HP/name/avatar source is currently wired here: do not fabricate a health bar, player portrait or damage flash.
- Theme preset selection is programmatic only; no persisted theme picker in this cut.
- Original Minecraft physical cursor/focus acceptance is independently unresolved in issue #821. Source tests and native preflight are not substitute screenshots.

## Safety and acceptance
Existing UI input/setting registries, target provenance, combat admission, UUID, World, native controller and packet code are not modified. Preserve all CI and native acceptance gates. Do not merge to main without exact-head Foundation Windows+Ubuntu, original Minecraft 1.8.9 verification, and visual review.