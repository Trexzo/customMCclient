# UI foundation

M13 introduces the first backend-neutral UI draw layer.

M14 adds logical viewport scaling, nine-point anchors and explicit HUD widget ownership.

M15 separates widget defaults from mutable/persistable HUD placement state.

M16 adds immutable layout snapshots, hit-testing and cancellable HUD drag transactions.

M17 connects the HUD command model to the staged renderer through backend-neutral viewport and renderer contracts.

## Direction

The client UI is native and in-process. It does not require an embedded browser.

Widgets and HUD systems build a per-frame `UiCommandBuffer`. The eventual 1.8.9 graphics backend consumes the sealed command list and translates it into efficient draw batches.

## Draw commands

The initial primitives are:

- `UiRectCommand`
- `UiTextCommand`

Both carry a numeric layer and backend-neutral geometry/color data.

The core does not know about OpenGL, Minecraft font renderers, shaders or screen classes.

## Command buffer contract

- commands may be appended while the buffer is open;
- sealing creates one immutable ordered view;
- commands are sorted by layer;
- insertion order remains stable inside the same layer;
- sealing is idempotent;
- mutation after sealing is rejected;
- geometry rejects NaN/infinite coordinates and negative rectangle extents.

## Viewport and anchors

`UiViewport` separates physical pixel dimensions from logical UI coordinates through an explicit positive scale.

`UiAnchor` supports nine positions:

- top-left / top-center / top-right;
- center-left / center / center-right;
- bottom-left / bottom-center / bottom-right.

Offsets are applied after anchor resolution in logical coordinates.

## HUD widget ownership and layout

`HudWidgetRegistry` owns stable widget ids and a deterministic cached plan ordered by priority then id.

`HudLayoutEngine` converts the current widget plan plus placement resolver into an immutable `HudLayoutSnapshot`.

Hit-testing walks the snapshot in reverse render order, so overlapping UI selects the topmost widget deterministically.

## Mutable HUD placement and drag ownership

`HudLayoutState` stores placement overrides by stable widget id.

`HudDragController` owns one drag transaction at a time. Movement only changes runtime layout state; commit/cancel decides whether that state remains. Durable profile writes remain outside input handling.

## Render bridge

`HudRenderPass` is a normal `RenderPass` fixed to the `HUD` stage.

It receives:

- `HudWidgetRegistry`;
- `HudPlacementResolver`;
- `UiViewportProvider`;
- `UiRenderer`.

On a HUD render frame it resolves the viewport, composes the command list, then passes the immutable commands to `UiRenderer`.

The Minecraft 1.8.9 hook therefore reaches UI through the existing staged renderer:

`Minecraft189Hooks -> RenderPipeline -> HudRenderPass -> UiRenderer`

The backend contract receives `RenderFrame`, `UiViewport` and the sealed command list. No Minecraft or OpenGL type crosses into core.

## Next layers

Later UI milestones can add:

- a concrete 1.8.9 graphics backend;
- clipping/scissor descriptions;
- rounded rectangles and outlines;
- font/style handles;
- generic focus/key routing;
- retained ClickGUI widgets;
- draw batching and state minimization.

Those layers should build on these contracts instead of bypassing them with direct graphics calls.
