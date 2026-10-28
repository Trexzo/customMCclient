# UX S25 — contextual Module Details, authentic Minecraft 1.8.9 navigation

## Visual evidence

The original Java8/X11 854×480 S24 capture from [native run #40695014711](https://github.com/Trexzo/customMCclient/actions/runs/40695014711), artifact `12819332207`, passed its dedicated screenshot stage. The actual `modules.png` and `settings.png` show a correct clean Graphite lower footer and also reveal an unnecessary persistent fourth sidebar entry, **Module Details**, even though it is opened contextually by clicking a module's `...` or right-clicking its row.

## Changes to real source

- New `ClickGuiNavigationPages.visible(snapshot)` filters only the dedicated `module-detail` ID from **root sidebar navigation**. The underlying page registration/content, module selection and detail settings all remain fully intact. Arbitrary extension pages remain visible.
- Both the `ClickGuiComposer` and the actual `ClickGuiInputController` share this projection for rendered rows, physical pointer selection, keyboard Up/Down/Home/End and wheel scroll accounting. The hidden phantom row cannot be reached via input.
- The sidebar highlights Modules while a contextual Module Details view is open; contextual page is not a standalone root tab.
- New compact, fixed `< Modules` return link at the top right of Module Details; source-native `UiModuleDetailBackLink` owns the same exact draw/hitbox geometry, and a left press through `ClickGuiNavigator` returns to Modules with selected module retained. Absent on too-small viewports.
- Unfocused Escape inside contextual details returns to Modules first; a second Escape closes ClickGUI. Existing focus/editor Escape handling still has first refusal to cancel edits and keybind capture.
- No new native font atlas, text scaling, binary patch, combat/controller/world/UUID/packet edits or changes to the separate #848 font-width lane.

## Tests and acceptance

`ClickGuiContextNavigationTest` checks registered vs visible page counts, no phantom sidebar item in actual render command clip, pointer/keyboard navigation, contextual highlight, Escape back/close, and back-link geometry. `ModuleDetailPageContentTest` verifies a live selected module remains selected after the real detail back action and right click is not interpreted as back.

S25 must pass exact-head Foundation Windows/Ubuntu CI and the pinned original Minecraft 1.8.9 screenshot step. Until those are inspected, S24 screenshots motivate the change but cannot certify S25. Genuine module-detail screenshot (opened by actual physical 1.8.9 pointer) and native font improvements remain separate acceptance gates.

S25 stacks only on S24 PR #860 at exact source head `5e24db68d4fd25e09dfa156ce2d8a7f8e99abec5`. Keep draft and preserve all independently changing combat PRs.
