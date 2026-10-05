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

M30 adds explicit page-content ownership and the first live module-list content view.

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

`ClickGuiModel` owns state that survives across frames: open/closed state, selected page, search query and navigation scroll.

Registered `ClickGuiPage` descriptors use stable ids and deterministic priority/id ordering. Registrations have explicit lifetimes.

`ClickGuiSnapshot` exposes one immutable frame-safe view.

## ClickGUI page content

`ClickGuiContentRegistry` maps stable page ids to `ClickGuiPageContent` implementations. Content registrations have explicit lifetimes and duplicate ownership is rejected.

`ClickGuiComposer` remains a generic shell compositor. If the selected page has registered content, the shell creates a clipped content scope and delegates through `ClickGuiContentContext`; otherwise it retains the simple title fallback.

`ClickGuiContentContext` contains only the immutable shell snapshot, selected page descriptor, content bounds and current semantic theme. Page content does not receive Minecraft/OpenGL objects or profile storage.

`ModuleListPageContent` is the first real content implementation. It reads the current `ModuleRegistry` snapshot and asks `ModuleController` for lifecycle state each composition. ClickGUI does not duplicate module enable/disable authority.

The retained search query filters module ids case-insensitively. Enabled/failed/transitional states are represented through semantic theme roles. M30 is intentionally read-only; module toggling becomes a separate input ownership milestone.

`ClickGuiRenderPass` has an overload that accepts the content registry, while the original constructor remains valid with an empty registry.

## Navigation scrolling

Navigation rows are shifted by the clamped retained scroll offset inside `UiClipCommand`.

`ClickGuiMetrics` owns row/content/max-scroll geometry so composition and input use the same calculation.

`UiScrollEvent` is backend-neutral. `LegacyInputTranslator` normalizes legacy wheel direction and converts bottom-origin framebuffer coordinates to top-origin logical UI coordinates before `Minecraft189InputHooks` routes the event.

## Focus and keyboard input

`UiFocusManager` owns exactly one focused target at a time with explicit registration lifetimes.

The search field is a normal `UiFocusTarget`. Printable characters edit the retained query, Backspace deletes one character, Enter/Escape release focus, and Escape without search focus closes the ClickGUI.

## Rendering boundary

`ClickGuiRenderPass` participates in the normal `HUD` stage and forwards backend-neutral commands through `UiRenderer`.

The 1.8.9 path remains:

`Minecraft189Hooks -> RenderPipeline -> ClickGuiRenderPass -> Minecraft189UiRenderer -> LegacyUiGraphics`

No concrete Minecraft, LWJGL, OpenGL, font atlas or profile-storage object crosses into core UI.

## Next layers

Later UI milestones can add:

- module-row pointer interaction through `ModuleController`;
- setting descriptors/editors;
- content-region scrolling;
- concrete host callback wiring into `Minecraft189InputHooks`;
- backend batching/state minimization;
- concrete legacy GL implementation.

Those layers should build on these contracts instead of bypassing them with direct graphics calls.
