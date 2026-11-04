# UX S54 — search invalidates stale numeric / scrollbar drag

## Defect
S53 tracks the selected page and open-session epoch but changing search terms can re-project the source-native Modules, global Settings, and contextual Settings rows without changing the page ID. A held S51 scrub still owns the prior `Setting<?>`, so an outside-panel S52 MOVE could continue to write a row hidden by a new filter.

## Implementation
- `ClickGuiModel.setSearchQuery` increments the existing pointer-context epoch **only if the query changes**. Reassigning the same text is a no-op, preserving valid S52 gesture continuity.
- The controller reconciles after focused key dispatch, since search focus can edit the query from inside `SearchFocusTarget`. Ctrl+F explicitly cancels any active page drag before transferring keyboard ownership to search; the existing search field pointer-press cancellation remains.
- Source-native capture cancellation from S53 is retained, and does not reset values, module enablement, saved settings, scroll offsets, rendering, fonts or combat.
- Regression test drives real ClickGuiInputController → SettingListPageContent, confirming a filtered-out setting cannot be modified by stale external mouse MOVE, unchanged-query drag keeps clamping to 100, and Ctrl+F cancels an active scrub before typing.

## Acceptance
Require exact-head Foundation Windows/Ubuntu + complete real Minecraft 1.8.9 client binary suite including visual screenshots and combat cross-checks. Draft/unmerged stack on S53 #904.
