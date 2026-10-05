# UI foundation

M13 introduces the first backend-neutral UI draw layer.

M14 adds logical viewport scaling, nine-point anchors and explicit HUD widget ownership.

M15 separates widget defaults from mutable/persistable HUD placement state.

M16 adds immutable layout snapshots, hit-testing and cancellable HUD drag transactions.

## Direction

The client UI is native and in-process. It does not require an embedded browser.

Widgets and HUD systems build a per-frame `UiCommandBuffer`. The eventual 1.8.9 renderer backend consumes the sealed command list and translates it into efficient draw batches.

## Draw commands

M13 starts with two primitives:

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

Offsets are applied after anchor resolution in logical coordinates. Widgets therefore do not need to know the actual framebuffer scale.

## HUD widget ownership

`HudWidgetRegistry` owns stable widget ids and a deterministic cached plan ordered by priority then id.

Registering a widget returns a lifetime handle. Closing it removes exactly that widget and is idempotent.

## Layout snapshots

`HudLayoutEngine` converts the current widget plan plus placement resolver into an immutable `HudLayoutSnapshot`.

Each entry contains the widget, the placement used for that frame and its resolved bounds. Hit-testing walks the snapshot in reverse render order, so overlapping UI selects the topmost widget deterministically.

`HudComposer` consumes the same snapshot when issuing draw commands. Input and rendering therefore agree on the exact bounds for a frame instead of recomputing them independently.

## Mutable HUD placement

Widget code continues to declare a default anchor and offsets. User movement is stored separately.

`HudPlacement` contains:

- anchor;
- logical X offset;
- logical Y offset.

`HudLayoutState` stores overrides by stable widget id. Clearing an override returns the widget to its declared default without mutating the widget implementation.

`HudPlacementCodec` implements the existing typed `SettingCodec` contract, so a placement can be persisted through the same profile/config system introduced in M2.

## Drag transaction

`HudDragController` owns one drag transaction at a time.

- only the left pointer button begins a drag;
- the topmost hit widget is captured from a layout snapshot;
- pointer movement updates only `HudLayoutState`;
- commit keeps the new runtime placement;
- cancel restores the exact previous override, or clears the temporary override when the widget originally used its default;
- invalid re-entrant or inactive operations are rejected.

Persistence is still outside the drag controller. A later orchestration layer can decide when committed layout state is written to a profile.

## Next layers

Later UI milestones can add:

- generic pointer/focus routing beyond HUD dragging;
- clipping/scissor descriptions;
- rounded rectangles and outlines;
- font/style handles;
- retained ClickGUI widgets;
- render-backend batching.

Those layers should build on these contracts instead of bypassing them with direct graphics calls.
