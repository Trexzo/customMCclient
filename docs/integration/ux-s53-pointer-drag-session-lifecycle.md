# UX S53 — prevent stale ClickGUI pointer drag after session changes

## Source defect
S52 correctly routes out-of-panel MOVE for a page that currently reports a native scrollbar/numeric drag. But `ClickGuiModel.select`, `close`, `toggle`, and reopen do not end an already-held drag on a page that becomes inactive. A rapid A→B→A selection between physical pointer events can resurrect a stale capture, because comparing just the selected page ID is insufficient.

## Change
- `ClickGuiModel.pointerContextEpoch` advances only on actual selected-page changes and GUI open/close transitions, including auto-selection after unregistration. Same-page selection does not reset a valid gesture.
- The actual `ClickGuiInputController` records owner page + context epoch when a page starts a drag, checks that context before every mouse/key/scroll dispatch, and cancels stale transient capture on closed/reopened, changed-page, and fresh-pointer-press transitions.
- Existing navigation (sidebar, keyboard, module details Back), ESC close and runtime shutdown explicitly invalidate old capture. Every source drag page provides cancellation of **transient pointer state only** (Modules rail; Settings / Module Details rail plus numeric scrub). No enabled module, setting value, saved position or undo stack is changed by cancellation.
- Unchanged page + same GUI session retains S52 outside-panel MOVE/RELEASE handling.

## Regression and gate
- Real controller+Settings integration: start actual `SettingNumericSpec(0,100,5)` drag; switch Settings→Other→Settings without any intermediate pointer events, verify next outside MOVE does not change the value or resurrect drag. Repeat close+reopen; verify fresh drag still reaches 100 and outside RELEASE cancels. Verify controller shutdown also cancels.
- Require exact-head Foundation Windows/Ubuntu and full original Mojang 1.8.9 binary gameplay + screenshot gates. Keep PR as stacked draft atop S52 #903, with no combat/network/font/render changes.
