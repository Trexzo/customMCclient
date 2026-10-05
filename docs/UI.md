# UI foundation

M13 introduces the backend-neutral UI command layer.

M14 adds logical viewport scaling, nine-point anchors and explicit HUD widget ownership.

M15 separates widget defaults from mutable/persistable HUD placement state.

M16 adds immutable layout snapshots, hit-testing and cancellable HUD drag transactions.

M17 connects HUD composition to the staged renderer.

M18 adds the Minecraft 1.8.9 translation boundary.

M19 adds rounded-rectangle and outline primitives for modern native HUD/ClickGUI surfaces.

## Direction

The UI is native and in-process. It does not require an embedded browser.

Widgets describe each frame through backend-neutral commands. The platform renderer translates the sealed command list.

## Supported shape commands

The core currently defines:

- `UiRectCommand` — filled rectangle;
- `UiRoundedRectCommand` — filled rectangle with validated corner radius;
- `UiOutlineCommand` — rectangular outline with positive thickness;
- `UiTextCommand` — text run.

All geometry uses logical UI coordinates.

Rounded radius may not exceed half the shortest edge. Outlines require finite positive thickness. Invalid geometry is rejected before it reaches a renderer backend.

## Command ordering

`UiCommandBuffer` orders commands by numeric layer while preserving insertion order within a layer.

The 1.8.9 renderer preserves that sealed order when translating commands to `LegacyUiGraphics`.

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

- clipping/scissor commands;
- semantic theme/style tokens;
- font handles and metrics;
- generic focus/key routing;
- retained ClickGUI widgets;
- backend batching/state minimization;
- concrete legacy GL implementation.

Those layers should build on these contracts instead of bypassing them with direct graphics calls.
