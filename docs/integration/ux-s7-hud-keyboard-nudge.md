# UX S7 — guaranteed mouse-free HUD editing shortcut

The native ClickGUI is an overlay. We do not yet have original-game evidence that a visible mouse pointer is reliably released from vanilla's grabbed camera while it is open. Accordingly, this independent keyboard path makes the previously introduced S5 TargetHUD editor useful without assuming cursor availability.

When **HUD Layout** is the selected ClickGUI page and no search/text/keybind editor owns focus, **Shift + arrow keys** move TargetHUD by four logical pixels. **Ctrl + Shift + arrows** use one-pixel steps. X/Y remain clamped to their native persistent 0..4096 validation range. This does not send combat, mouse, attack or movement events. Offscreen saved coordinates are already recovered during native render by S3. Holding other modifier combinations, repeated key events, other pages and focus owners cannot alter HUD coordinates.

Three new JUnit tests cover standard/fine keyboard nudges, other-page/text-field isolation, input repeats, closed GUI, range edges and unrelated setting integrity. The existing mouse-preview motion path from S6 remains independently testable and guarded by physical mouse capture state; **do not equate source tests with original Minecraft pointer-focus acceptance**.

Requires exact-head Windows/Ubuntu Foundation and genuine Mojang 1.8.9 CI. Keep Draft, do not merge main or modify the independent combat V326–V329 branches.
