# UX S50 — compact sticky Module Details context (source-native 1.8.9)

## Issue observed from S48–S49

The original Mojang 1.8.9 ClickGUI Modules page already routes row detail dots/right-click into a true per-module Details page. S49 added a real draggable scrollbar there, verified with 24 actual settings. However the source-rendered module title, state and keybind scroll off the page; after dragging deep into a long set of controls, the user can no longer tell which real module is being configured. The fixed `< Modules` Back control is still available. Neither a new client theme nor a second details/settings UI is needed.

## S50 change

- **Only when the original 24px inset + 78px complete module detail header has passed out of view** (scroll >= 102 logical pixels), render a slim, original-font **26px sticky identity strip** at the top of the existing clipped detail content region. Default first-page screenshot remains unchanged (zero new draw commands at scroll 0).
- Show the **actual currently selected module's displayName** plus a source-bound `N settings` count from the real `ModuleSettingRegistry`. The identity is clipped to its own geometry, ending 22px before the original `< Modules` button. A thin source-theme status rail reflects the real `ModuleState` (accent enabled / muted disabled / warning transition / danger failed), not fabricated state or arbitrary color.
- Render bar after scrolling rows, but before the existing fixed Back control, preserving its exact 102×17px hit rectangle. Use the same active 26px geometry in the pointer path to consume presses in the obscured region, preventing accidental setting edits or keybind capture through the sticky bar.
- No change to original setting-row x/y, category/setting priority, native scroll rail, selection reset, keyboard bindings, live profile values, Minecraft fonts, saved settings, Packet/Combat behavior, or any independent visual/combat branch.
- Strictly omit the bar for scroll below 102, narrow content width < 210px, or insufficient content height. No new saved settings, GL dependencies, textures, font files, timers or event hooks.

## Acceptance

- Geometry unit tests check exactly **0 new commands** before the header disappears, and four commands at/after the 102px cutoff: source-theme fill, tiny status rail, clipped native-font module label/count, thin bottom border; clipping reserves actual Back button geometry.
- State tests check Enabled/Failed/Disabled theme rail colors, long source names, zero settings and small viewport omission.
- Integration uses a source-owned 24-setting Module Details panel and S49's actual scroll-to-end input. Verify sticky title appears only after scroll, all registered setting values stay unchanged after a press in the covered strip, and the fixed Back control still routes to Modules while sticky remains drawn.
- Require exact S50 head Windows+Ubuntu Foundation and full original Mojang Minecraft 1.8.9 native preflight, with genuine S22 global ClickGUI baseline screenshots to verify *global page nonregression*. Existing native artifact captures Modules/Settings rather than the scrolled contextual details state; the new sticky behavior is asserted in source-native draw-command and pointer tests, **not** claimed present in the default screenshots.
- Independent native multiplayer gameplay gate from S49 was **red** due to simultaneous-UUID target damage-score assertion (server reported score 10 instead of expected 0); source changes in S49 were ClickGUI-only. The failed native job was explicitly rerun without changing combat gates. A successful rerun is required before certifying S49 as fully native PASS.

S50 remains a separate, draft/unmerged PR stacked on S49 #900. Combat and network code stay entirely untouched.
