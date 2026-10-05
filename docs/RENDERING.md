# Rendering foundation

M9 introduces a renderer contract without OpenGL, Minecraft, UI toolkit or shader dependencies.

M10 wires the Minecraft 1.8.9 adapter to that contract through `ServiceRegistry`.

M11 moves structural render work off the per-frame hot path and gives pass registrations explicit lifetimes.

M12 adds explicit render-resource ownership so later OpenGL resources have deterministic cleanup.

M17 adds the first backend-neutral HUD render pass on top of the staged pipeline.

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

## Stages

1. `WORLD`
2. `WORLD_OVERLAY`
3. `HUD`
4. `POST_PROCESS`

Passes are ordered by numeric priority, then stable id. Registration is explicit.

## Cached render plans

`RenderPipeline` maintains one immutable plan per stage.

Registering or unregistering a pass rebuilds only the affected stage. A normal render callback reads the already-sorted immutable plan and invokes it directly.

## Pass and resource lifetime

`RenderPipeline.register` returns a pass registration lifetime.

`RenderResourceRegistry` owns future renderer resources such as shader programs, framebuffer targets, vertex buffers and font atlases.

Both models make cleanup explicit instead of relying on global renderer state.

## HUD render bridge

`HudRenderPass` participates in the normal `HUD` stage and composes backend-neutral UI commands.

`UiViewportProvider` owns the conversion from platform framebuffer state into the logical UI viewport.

`UiRenderer` owns translation from the command list into backend operations.

This separation means the later 1.8.9 renderer can use LWJGL/OpenGL without leaking those types into UI widgets or core layout logic.

## Platform routing

`Minecraft189Hooks` translates version-specific render callbacks into the generic stages.

The adapter obtains `RenderPipeline` through the platform's explicit `ServiceRegistry`, so there is no renderer singleton and no Minecraft type crosses into core.

Concrete GL state ownership, batching, font resources, shaders and post-processing targets belong in later renderer milestones.
