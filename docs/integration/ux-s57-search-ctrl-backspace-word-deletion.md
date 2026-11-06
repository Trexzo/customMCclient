# UX S57 — word deletion in native ClickGUI search

## Source finding
The existing `SearchFocusTarget` search field only treats Backspace as deleting one character. Search supports up to 64 typed characters, and replacing long terms like `reach velocity` via repeated individual keypresses is awkward. The native UI already owns focused keyboard dispatch, so the change belongs there; it does not require a new host hook or rendering approximation.

## Implementation
- Inside the focused search target, Ctrl+Backspace deletes the last word and any whitespace **after** it. It preserves any whitespace that was **before** the deleted word, matching a simple end-of-string caret: `reach velocity  ` → `reach ` → empty.
- Plain Backspace keeps existing single-character behavior. Ctrl+Alt+Backspace also retains the pre-existing single-character handling, not an accidental word deletion.
- Empty queries, focus ownership, ENTER/ESC, regular typing, search filter epochs (S54), scroll reset-on-change (S56), visual pixels, selection, settings values, combat and network behavior are unchanged.
- Regression exercises real `ClickGuiInputController` focus, repeated Ctrl+Backspace, blank state, single-character Backspace, Ctrl+Alt modifier isolation and persistent page identity.

Require exact-head Foundation Windows/Ubuntu and full original Mojang 1.8.9 binary gameplay + visual acceptance; all PRs remain stacked drafts. Parent S56 #908 has green Foundation but original binary still in progress. No tests weakened.
