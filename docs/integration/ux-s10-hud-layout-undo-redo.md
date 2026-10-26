# UX S10 — bounded undo/redo for persistent TargetHUD coordinates

This PR adds in-memory per-page edit history to existing live 1.8.9 TargetHUD X/Y placement.

- **Ctrl+Z** = Undo; **Ctrl+Y** or **Ctrl+Shift+Z** = Redo. Shortcuts belong only to HUD Layout when no search/text/keybind editor owns focus, without affecting the game's hotkeys outside ClickGUI.
- Three visible actions: Undo, Reset Position, Redo (two history buttons hidden on narrow preview canvas). All restore via existing SettingRegistry validation and persistent codecs, never bypassing any native setting validator.
- Store at most 64 previous coordinate pairs; repeated held-mouse MOVE events coalesce into a single undo entry per gesture, and pointer RELEASE resets the gesture, including when released outside the preview. A no-op initial pointer press does not lose the eventual gesture's undo checkpoint.
- After an external profile load or any direct setting mutation, obsolete undo/redo states are cleared on the next edit rather than overwriting the newly loaded profile. Undo history is transient UI memory and **never saved to profile**.
- No combat module, packet, World state or native controller changes. Inherits the existing source-vs-real-cursor distinction from S8 issue #821.
- JUnit covers undo/redo buttons and shortcuts, MOVE coalescing, external setting edits, 64-entry limit, no-op gesture and focus-aware dispatch.

Exact-head Ubuntu/Windows Foundation and original Minecraft 1.8.9 workflows are mandatory. Keep Draft.
