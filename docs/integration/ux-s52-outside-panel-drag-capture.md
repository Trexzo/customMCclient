# UX S52 — owner-only outside-panel drag routing

## Verified source defect
S48/S49 scrollrails and S51 numeric scrubbers keep their own pointer ownership and already accept MOVE/RELEASE outside their page boundaries. But `ClickGuiInputController.pointer` rejected every MOVE outside `layout.content()` before calling the selected page, so an actual mouse drag could not follow the cursor past the panel edge. S51 direct-controller tests did not traverse this top-level gate. A release outside still dispatched.

## Correction
- Introduce a non-mutating, default-false `ClickGuiPageContent.capturesPointerDrag()` contract.
- The source Modules page reports capture only while its native scroll rail owns a drag; global Settings and contextual Module Details report their rail or numeric scrub ownership. Other pages remain default-false.
- If and only if the **currently selected page** owns a drag, route MOVE outside content to its existing pointer handler, including across search/navigation bounds. Do **not** turn motion into a press, change tabs, toggle modules, or focus search.
- Keep the existing normal inside-content MOVE and outside RELEASE pathways unchanged. Existing value clamps, step authority, scroll bounds and original client rendering stay source-native. No new settings, assets, or combat changes.

## Evidence / gates
- New end-to-end `ClickGuiInputControllerTest` uses real `SettingListPageContent`, `ClickGuiInputController`, and `SettingNumericSpec(0,100,5)` to prove unowned outside MOVE is ignored; active drag can reach 100 to the right and 0 to the left, release outside clears ownership, subsequent outside motion is ignored, and page selection does not change. Before S52 the first outside MOVE assertion fails.
- Existing S48/S49 rail tests and S51 numeric integration tests remain mandatory. Require exact-head Foundation CI on Windows/Ubuntu and complete original Mojang Minecraft 1.8.9 binary preflight. The latter has an independent known intermittent combat scoreboard failure on S51 parent (target B got 10 despite expected 0); do not declare S52 accepted or weaken any gameplay gate if it recurs.
- Keep PR draft and **unmerged** atop S51 #902. No changes to renderer default pixels, packet handling, module/combat state, font assets or saved settings.
