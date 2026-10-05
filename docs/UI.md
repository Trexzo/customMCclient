# UI foundation

M13 introduces the first backend-neutral UI draw layer.

## Direction

The client UI is native and in-process. It does not require an embedded browser.

Widgets and HUD systems will build a per-frame `UiCommandBuffer`. The eventual 1.8.9 renderer backend consumes the sealed command list and translates it into efficient draw batches.

## Initial command types

M13 intentionally starts with only two primitives:

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

This lets later HUD/ClickGUI code describe a frame once, then lets a renderer batch or cache the resulting primitives without widgets issuing direct OpenGL state changes.

## Next layers

Later UI milestones can add:

- viewport-aware layout and anchors;
- clipping/scissor descriptions;
- rounded rectangles and outlines;
- font/style handles;
- input/focus routing;
- retained ClickGUI widgets;
- render-backend batching.

Those layers should build on this command contract instead of bypassing it with direct graphics calls.
