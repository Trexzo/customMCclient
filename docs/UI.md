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

M28 adds the Minecraft 1.8.9 primitive input translation bridge.

M29 adds retained, clamped navigation scrolling and legacy wheel translation.

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

Rounded radius may not exceed half the shortest edge. Outlines require finite positive thickness. Invalid geometry is rejected before it reaches a renderer backend.

## Command ordering

`UiCommandBuffer` orders top-level commands by numeric layer while preserving insertion order within a layer.

A `UiClipCommand` is one atomic top-level command. Its child list is immutable and renders in the order supplied. This prevents top-level layer sorting from separating clip entry and exit. A nested `UiCommandBuffer` can be sealed first when a clipped subtree needs its own layer ordering.

The 1.8.9 renderer preserves the sealed top-level order and recursively translates clipped child commands.

## Clip ownership

`Minecraft189UiRenderer` translates a clip scope to `LegacyUiGraphics.pushClip` / `popClip`.

Clip exit is guaranteed in a `finally` block if a nested command fails. Nested clip scopes are therefore safe to compose without leaking scissor state into later UI.

The eventual concrete 1.8.9 backend owns conversion from logical clip bounds to framebuffer scissor coordinates and intersection with any parent clip.

## Theme ownership

`UiColorRole` defines semantic color intent instead of backend or widget-specific constants.

`UiTheme` is an immutable complete mapping from every semantic role to an ARGB value. Missing roles are rejected at construction, and palette input is defensively copied.

`UiThemeProvider` gives retained UI a small runtime seam for later theme switching without coupling widgets to profile storage.

`UiThemes.darkDefault()` provides the first neutral modern palette. Widgets should request semantic roles such as `SURFACE`, `TEXT_PRIMARY` or `ACCENT` rather than embedding those palette values directly.

## Font and text measurement

`UiFontHandle` is a stable logical font identity. It does not expose a Minecraft `FontRenderer`, texture id, atlas or other backend resource.

`UiFonts.DEFAULT` preserves the existing text-command behavior through the logical `minecraft-default` handle, while callers may opt into named handles such as `ui-medium`.

`UiTextMetrics` carries validated logical width, height and baseline geometry. `UiTextMeasurer` is the backend-neutral measurement contract used by future retained layout before commands are emitted.

`UiTextCommand` carries its font handle to the 1.8.9 graphics boundary. The eventual concrete backend resolves that handle to its owned font implementation.

## Focus and key routing

`UiFocusManager` owns exactly one focused target at a time. Targets register under stable ids and receive deterministic focus-change callbacks.

Registration is an explicit lifetime. Closing the focused target's registration clears focus and emits the matching focus-loss callback, preventing stale retained widgets from continuing to receive keyboard input.

`UiKey`, `UiKeys`, `UiKeyAction` and `UiKeyEvent` describe logical keyboard input without exposing LWJGL or Minecraft event classes.

Only the focused target receives `dispatchKey`; its boolean return indicates whether the event was consumed.

## Retained ClickGUI shell

`ClickGuiModel` owns UI state that must survive across frames instead of being recreated during rendering.

Registered `ClickGuiPage` descriptors use stable ids and deterministic priority/id ordering. Registrations have explicit lifetimes.

Automatic selection follows the deterministic page order until the user makes an explicit page selection. Explicit selection remains stable as later pages register. Removing the selected page falls back to the first remaining ordered page.

Open/closed state, selected page id, search query and navigation scroll offset are retained independently of rendering. `ClickGuiSnapshot` exposes an immutable frame-safe view.

## ClickGUI layout and composition

`ClickGuiLayoutEngine` computes a responsive centered wide panel in logical UI coordinates, with explicit sidebar, search, clipped navigation and content regions.

`ClickGuiComposer` converts one immutable shell snapshot plus the current viewport/theme into the existing backend-neutral command model.

Navigation rows are shifted by the clamped retained scroll offset inside the existing `UiClipCommand`. `ClickGuiMetrics` owns row/content/max-scroll geometry so composition and input use the same calculation.

The composer does not call Minecraft, OpenGL or profile storage directly.

`ClickGuiRenderPass` places the retained shell into the existing `HUD` render stage. It snapshots the model first and exits immediately while the GUI is closed.

## ClickGUI input

`UiPointerEvent`, `UiPointerAction` and `UiScrollEvent` describe validated logical pointer input without exposing LWJGL or Minecraft classes.

`ClickGuiInputController` handles left-button press hit-testing for the search field, page navigation and shell surface. Wheel input is consumed only inside the navigation bounds and adjusts retained scrolling by a shared logical step.

Navigation hit-testing applies the same clamped scroll offset as rendering, so a visible row and its click target cannot drift apart.

The search field is a normal `UiFocusTarget` owned through `UiFocusManager`. Printable characters append to the retained query, Backspace deletes one character, and Enter/Escape release search focus. Escape with no focused search field closes the ClickGUI.

`LegacyInputTranslator` converts primitive mouse/key/wheel data into backend-neutral events. Pointer and wheel Y coordinates are converted from the legacy bottom-origin framebuffer convention into top-origin logical UI coordinates. Legacy wheel magnitude is normalized to a stable direction step before entering core.

`Minecraft189InputHooks` resolves `ClickGuiInputController` through the existing platform `ServiceRegistry`. Zero wheel deltas and unsupported extra mouse buttons are ignored before retained UI mutation.

## HUD layout/input

`HudWidgetRegistry`, `HudLayoutEngine`, `HudLayoutState` and `HudDragController` own widget identity, resolved bounds, placement overrides and drag transactions respectively.

Input and rendering can share the same immutable layout snapshot.

## Render bridge

`HudRenderPass` runs in the standard HUD render stage and sends the sealed command list to `UiRenderer`.

The 1.8.9 path is:

`Minecraft189Hooks -> RenderPipeline -> HudRenderPass -> Minecraft189UiRenderer -> LegacyUiGraphics`

The actual Minecraft/LWJGL implementation of `LegacyUiGraphics` remains outside core.

## Next layers

Later UI milestones can add:

- concrete host callback wiring into `Minecraft189InputHooks`;
- page-specific module/setting views;
- backend batching/state minimization;
- concrete legacy GL implementation.

Those layers should build on these contracts instead of bypassing them with direct graphics calls.
