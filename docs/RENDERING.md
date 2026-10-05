# Rendering foundation

M9 introduces a renderer contract without OpenGL, Minecraft, UI toolkit or shader dependencies.

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

The platform adapter will later translate actual game render hooks into these stages. GL state ownership, batching, cached frame snapshots, shader resources and post-processing targets belong in later renderer milestones rather than leaking into this generic core contract.
