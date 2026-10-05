# UI foundation

M13 introduces the backend-neutral UI command layer.

M14 adds logical viewport scaling, nine-point anchors and explicit HUD widget ownership.

M15 separates widget defaults from mutable/persistable HUD placement state.

M16 adds immutable layout snapshots, hit-testing and cancellable HUD drag transactions.

M17 connects HUD composition to the staged renderer.

M18 adds the Minecraft 1.8.9 translation boundary.

M19 adds rounded-rectangle and outline primitives for modern native HUD/ClickGUI surfaces.

M20 adds atomic clipping scopes for scrollable and nested retained UI.

M21 adds semantic theme tokens and immutable palette ownership.

M22 adds stable font handles and backend-neutral text metrics.

M23 adds explicit focus ownership and backend-neutral key routing.

M24 adds the retained ClickGUI shell state model.

M25 adds responsive backend-neutral ClickGUI shell composition.

M26 connects the retained ClickGUI to the staged HUD renderer.

M27 adds backend-neutral ClickGUI pointer and search-key interaction.

## Direction

The UI is native and in-process. It does not require an embedded browser.

Widgets describe each frame through backend-neutral commands. The platform renderer translates the sealed command list.

## Supported commands

The core currently defines:

- `UiRectCommand` — filled rectangle;
- `UiRoundedRectCommand` — filled rectangle with validated corner radius;
- `UiOutlineCommand` — rectangular outline with positive thickness;
- `UiTextCommand` — text run carrying a stable `UiFontHandle`;
- `UiClipCommand` — atomic clipped child-command scope.

All geometry uses logical UI coordinates.

## Retained ClickGUI shell

`ClickGuiModel` owns open state, selected page, search query and deterministic page registrations across frames.

`ClickGuiLayoutEngine` computes the centered shell regions. `ClickGuiComposer` and `ClickGuiInputController` both use the shared `ClickGuiMetrics` page-row geometry so visual rows and pointer hit-testing cannot drift independently.

`ClickGuiRenderPass` places the shell in the normal HUD render stage and returns before resolving render dependencies while the GUI is closed.

## ClickGUI input

`UiPointerEvent` and `UiPointerAction` describe validated logical pointer input without exposing LWJGL or Minecraft classes.

`ClickGuiInputController` handles left-button press hit-testing for the search field, page navigation and shell surface.

The search field is a normal `UiFocusTarget` registered with `UiFocusManager`. Printable character input appends to the retained query, Backspace removes the last character, and Enter/Escape releases search focus. Escape with no focused search field closes the ClickGUI.

Page clicks update the retained selected page and release text focus. Closing the input controller closes its focus registration so a disposed GUI cannot remain ghost-focused.

## Theme, font and clipping ownership

Semantic colors remain owned by `UiTheme`; text uses stable `UiFontHandle` identities; navigation clipping remains an atomic `UiClipCommand`. None of these layers imports Minecraft/OpenGL types.

## HUD layout/input

`HudWidgetRegistry`, `HudLayoutEngine`, `HudLayoutState` and `HudDragController` own HUD widget identity, resolved bounds, placement overrides and drag transactions.

## Render bridge

The UI route remains:

`Minecraft189Hooks -> RenderPipeline -> HUD passes -> UiRenderer -> LegacyUiGraphics`

The actual Minecraft/LWJGL implementations remain outside core.

## Next layers

Later UI milestones can add:

- platform pointer/key translation into the backend-neutral input events;
- page-specific module/setting views;
- navigation scrolling;
- backend batching/state minimization;
- concrete legacy GL implementation.

Those layers should build on these contracts instead of bypassing them with direct platform calls.
