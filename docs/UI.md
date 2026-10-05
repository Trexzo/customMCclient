# UI foundation

M13 introduces the first backend-neutral UI draw layer.

M14 adds logical viewport scaling, nine-point anchors and explicit HUD widget ownership.

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

`HudComposer`:

1. snapshots the registered widget plan;
2. measures each widget against the logical viewport;
3. resolves its anchor and offsets;
4. provides immutable bounds through `HudDrawContext`;
5. seals and returns the resulting UI command list.

This gives future movable HUD elements a stable ownership/layout boundary without direct renderer calls.

## Next layers

Later UI milestones can add:

- persisted movable widget positions;
- clipping/scissor descriptions;
- rounded rectangles and outlines;
- font/style handles;
- input/focus routing;
- retained ClickGUI widgets;
- render-backend batching.

Those layers should build on these contracts instead of bypassing them with direct graphics calls.
