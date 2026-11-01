# UX S49 — contextual module settings navigation (original Minecraft 1.8.9)

## Confirmed source behavior

The S48 original-client screenshots and code audit confirm the existing route is **Modules → row “...” or right-click → contextual Module Details → the selected module's registered setting bindings**. There is no need to duplicate the global 397-setting list or invent new client modules. The detail page already draws the same 2px track / 3px thumb as S48 Modules and Settings, but it did not accept rail pointer input. Worse, it shared one scroll offset across changing module selections, so opening a second module could start halfway through its settings. This makes configuration especially confusing on clients with many modules.

## S49 targeted improvements

- Reuse S48's exact source-native `UiContentScrollDrag` and authoritative `UiContentScrollState` in **Module Details**. Users can grab the real rail or seek on the empty track in the unobtrusive unused 12px right margin. Intercept these gestures **before** setting editor, keybind capture and back-button navigation. Mouse wheel, module state, real setting input and ClickGUI sidebar remain unchanged.
- Track the current *source module ID*. Whenever the selected module differs, reset only the detail view's scroll offset and cancel any old thumb drag capture. Do not alter existing setting values, saved profiles or keybind assignments; switching away and back starts a module at its header/first setting again.
- Improve the content hierarchy in the same restrained Slinky/Vape/Rise-inspired style, without larger cards: a muted `N settings` annotation in the spare header space of sufficiently wide layouts; `No settings for this module` for zero actual registered controls. The count is derived exclusively from the real `ModuleSettingRegistry.bindingsForModule`, not guesses or global settings, and no row or action hitbox moves.
- Keep original Minecraft 1.8.9 renderer, no new fonts or shaders, original detail back link and existing source setting editor; no combat or packet changes.

## Verification

- Deterministic source runtime test with real module registry, 24 real settings on module A, 2 on B and 0 on C. Verify visible A-specific count, rail from top to near bottom with preserved original native thumb layout, release *outside* panel, all settings unchanged, then switch to B and confirm no overflowing rail and module-specific count; switch back A and confirm thumb resets to top.
- Mid-drag selection change to zero-setting C **cancels capture** and displays accurate empty hint; subsequent MOVE must not scroll until a new valid rail press.
- Existing global Modules+Settings S48 drag behavior, vanilla keyboard ownership, per-module keybind/setting editing, search, navigation and original S22 ClickGUI screenshots must remain passing.
- Validate exact S49 head with Windows/Ubuntu Foundation CI and full true original Mojang 1.8.9 Binary Preflight. The native default screenshot demonstrates global Modules/Settings **visual nonregression**; the new module-detail interaction is established by source-runtime tests, not falsely claimed from a static global screenshot.

S49 is an isolated **draft/unmerged** stacked PR on S48 #899. Only core ClickGUI presentation/input code and tests are edited. The separate combat track is untouched.
