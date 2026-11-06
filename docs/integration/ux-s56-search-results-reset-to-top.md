# UX S56 — reset filtered list to first result

## Source issue
Global Settings and Modules keep source-native scroll offsets across searches. A user viewing row 30 and typing a new search can be left in the middle of the newly filtered list, concealing the first results even when the query still matches many rows. Merely clamping a previous offset is not a search UX reset.

## Source change
- A minimal `ClickGuiContentScrollState.resetForSearchChange(query)` resets offset to zero **only when the actual raw search query changes**, and reports whether a projection transition occurred.
- Both Modules and global Settings invoke it before row filtering in their existing shared compose/pointer/scroll pathway. Any held native rail is canceled; Settings also cancels an active numeric scrub. They retain their real rows, layout, source setting validators and scroll wheel.
- Reassigning an identical query does not change scroll offset or interrupt a fresh user gesture. Module Details does not filter settings by query and is intentionally not changed.

## Evidence
Core regression drives real `ClickGuiComposer`, `ClickGuiInputController`, wheel inputs and 20 source modules / 20 registered settings. It verifies 56px scroll, reset to the original first-row Y even when a new query matches all 20 rows, preserves a 28px wheel offset on no-op search, and first-row native toggle hit-testing on Modules.
Require exact-head Foundation Windows/Ubuntu, official 1.8.9 binary/gameplay and visual baseline. Preserve combat/network gates. Stacked draft on S55 #907, unmerged.
