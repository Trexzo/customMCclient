# Rendering foundation

M9 introduces a renderer contract without OpenGL, Minecraft, UI toolkit or shader dependencies.

M10 wires the Minecraft 1.8.9 adapter to that contract through `ServiceRegistry`.

M11 moves structural render work off the per-frame hot path and gives pass registrations explicit lifetimes.

M12 adds explicit render-resource ownership so later OpenGL resources have deterministic cleanup.

## Goals

- explicit render stages;
- deterministic pass ordering;
- stable pass identity;
- frame-local immutable timing input;
- no hidden global renderer ownership;
- no browser requirement for ClickGUI;
- no direct Minecraft types in core;
- no repeated pass filtering/sorting during steady-state rendering;
- deterministic ownership for future GPU resources.

## Stages

1. `WORLD`
2. `WORLD_OVERLAY`
3. `HUD`
4. `POST_PROCESS`

Passes are ordered by numeric priority, then stable id. Registration is explicit.

## Cached render plans

`RenderPipeline` maintains one immutable plan per stage.

Registering or unregistering a pass rebuilds only the affected stage. A normal render callback reads the already-sorted immutable plan and invokes it directly; it does not scan every registered pass or sort a fresh list each frame.

Unrelated stage mutations do not invalidate another stage's plan.

## Pass lifetime

`register` returns a `RenderPipeline.Registration`.

Closing that registration removes exactly the pass instance it owns and is idempotent. This gives future module lifecycle code a deterministic cleanup handle instead of relying on global unregister calls.

## Render resource lifetime

`RenderResourceRegistry` is intentionally independent of OpenGL.

Future shader programs, framebuffer targets, vertex buffers, font atlases and similar renderer-owned objects can implement `RenderResource` and receive a stable id plus deterministic lifetime.

- duplicate ids are rejected;
- an individual registration can release exactly its resource;
- closing the registry releases remaining resources in reverse registration order;
- one failed resource close does not prevent later resources from being released;
- no resources may be registered after the registry closes.

This is the ownership layer only. M12 does not introduce OpenGL calls or GPU allocation.

## Platform routing

`Minecraft189Hooks` translates version-specific render callbacks into the generic stages.

The adapter obtains `RenderPipeline` through the platform's explicit `ServiceRegistry`, so there is no renderer singleton and no Minecraft type crosses into core.

GL state ownership, batching, cached frame snapshots, shader implementations and post-processing targets belong in later renderer milestones.
