# UX S3 — live native TargetHUD viewport recovery

This small native 1.8.9 UX improvement is stacked after S2, with no combat/controller changes.

## Behavior
The default and optional compact/coordinates target cards retain their exact width, height, colors, texts and existing valid positions on a large viewport. On smaller logical viewports or after Minecraft GUI-scale/resolution changes, TargetHUD clamps its **render-only** X/Y into the visible region each frame. The persisted integer settings remain unchanged. When a card exceeds the whole viewport, it is pinned to the top/left where it is at least discoverable, instead of permanently offscreen. No fake target data is created.

The guard executes only after the original target/rotation matching-source predicate already passes and before `beginUi`; it cannot modify source identity, UUID, combat or packet behavior.

## Tests and evidence
- Existing TargetHUD runtime tests preserve original draw geometry at the default 1280 × 720 logical viewport.
- New core-free native-module unit tests cover logical scaling 1 and 2, extreme configured X/Y, oversized cards, and rejection of nonfinite positions.
- Foundation CI and genuine original Minecraft 1.8.9 CI remain separate. No claim of full manual in-game drag is made. This PR does not wire `HudDragController` to native mouse movement or persistence.

## Merge policy
Keep Draft, do not change main or other chat's V326/V327 branches; complete independent exact-head verification first.
