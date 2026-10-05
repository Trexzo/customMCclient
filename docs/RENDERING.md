# Rendering foundation

M9 introduces a renderer contract without OpenGL, Minecraft, UI toolkit or shader dependencies.

M10 wires the Minecraft 1.8.9 adapter to that contract through `ServiceRegistry`.

M11 moves structural render work off the per-frame hot path and gives pass registrations explicit lifetimes.

M12 adds explicit render-resource ownership so later OpenGL resources have deterministic cleanup.

M17 adds the backend-neutral HUD render pass.

M18 adds the Minecraft 1.8.9 UI translation boundary without importing Minecraft/OpenGL types into core.

## Goals

- explicit render stages;
- deterministic pass ordering;
- stable pass identity;
- frame-local immutable timing input;
- no hidden global renderer ownership;
- no browser requirement for ClickGUI;
- no direct Minecraft types in core;
- no repeated pass filtering/sorting during steady-state rendering;
- deterministic ownership for future GPU resources;
- UI composition isolated from the concrete graphics backend.

## Cached render plans and lifetime

`RenderPipeline` maintains one immutable plan per stage. Registering or unregistering a pass rebuilds only the affected stage.

Pass registrations and `RenderResourceRegistry` make render lifecycle explicit.

## HUD render bridge

`HudRenderPass` composes backend-neutral UI commands in the normal `HUD` stage.

`UiViewportProvider` owns the viewport boundary and `UiRenderer` owns command translation.

The generic flow is:

`Minecraft189Hooks -> RenderPipeline -> HudRenderPass -> UiRenderer`

## Minecraft 1.8.9 UI backend boundary

M18 provides two platform-side facades:

- `LegacyViewportAccess` — current framebuffer dimensions and UI scale;
- `LegacyUiGraphics` — begin/end plus logical rectangle and text drawing operations.

`Minecraft189ViewportProvider` reads the current display values on every render frame and creates the core `UiViewport`.

`Minecraft189UiRenderer` translates the currently supported core commands:

- `UiRectCommand -> LegacyUiGraphics.fillRect`
- `UiTextCommand -> LegacyUiGraphics.drawText`

Command order is preserved. `end()` is guaranteed after a successful `begin()`, even when translation fails.

Unknown command types fail explicitly rather than silently disappearing. That makes future command additions versioned work at the backend boundary.

The actual Minecraft/LWJGL implementation of `LegacyUiGraphics` and `LegacyViewportAccess` remains a later integration milestone. This keeps the current source testable without committing Mojang classes.

## Platform routing

`Minecraft189Hooks` translates version-specific render callbacks into generic stages and obtains `RenderPipeline` through the explicit service registry.

Concrete GL state ownership, batching, font resources, shaders and post-processing targets remain later renderer milestones.
