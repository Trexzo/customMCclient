# Rendering foundation

M9 introduces a renderer contract without OpenGL, Minecraft, UI toolkit or shader dependencies.

M10 wires the Minecraft 1.8.9 adapter to that contract through `ServiceRegistry`.

## Goals

- explicit render stages;
- deterministic pass ordering;
- stable pass identity;
- frame-local immutable timing input;
- no hidden global renderer ownership;
- no browser requirement for ClickGUI;
- no direct Minecraft types in core.

## Stages

1. `WORLD`
2. `WORLD_OVERLAY`
3. `HUD`
4. `POST_PROCESS`

Passes are ordered by numeric priority, then stable id. Registration is explicit.

## Platform routing

`Minecraft189Hooks` translates version-specific render callbacks into the generic stages.

The adapter obtains `RenderPipeline` through the platform's explicit `ServiceRegistry`, so there is no renderer singleton and no Minecraft type crosses into core.

GL state ownership, batching, cached frame snapshots, shader resources and post-processing targets belong in later renderer milestones.
