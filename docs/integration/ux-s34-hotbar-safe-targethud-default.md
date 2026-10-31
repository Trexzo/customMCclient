# UX S34 — hotbar-safe Target HUD initial placement

## Scope

The authentic S31/S33 original Minecraft 1.8.9 two-player 854×480 screenshot proved the previous Target HUD default Y=210 was clamped to the bottom of the GUI viewport, overlapping vanilla hotbar/UI. S33 diagnosed and corrected a separate OpenGL font-texture cache corruption, with real text visible at a temporary **CI-only** Y=84. Its full native suite ultimately failed in an **independent** two-simultaneous-UUID PvP damage test: the dedicated server observed `CITargetB taken=20` where the negative control required zero. Do not weaken, skip or describe that negative control as passed.

## S34 changes (stacked on S33, do not merge)

- Define Target HUD `DEFAULT_Y=84` logical pixels for **new profiles and explicit reset**. X default remains 18. This matches the proven clear native screenshot area, retaining normal text clipping and the S33 original font-texture repair.
- Remove the S33 acceptance-only `ySetting().set(84)` override. The next original two-player capture now exercises **the actual production default** instead of a CI substitution.
- Retain exact existing settings IDs, codecs, 0..4096 bounds and render-only viewport clamping. A loaded saved Y value (including old `210`) is unchanged, preserving explicit user placements. No silent profile migration or combat changes.
- Unit regression covers initial compact (38px), expanded (54/72px), noncompact (56px) card heights and GUI scales 1, 2, 3, 4, with a 36px bottom region reserved for native UI on representative viewports, and verifies the old saved 210 still projects deterministically to Y=202 on the 854×480 / 2× fixture.
- The existing HUD editor reset test is adjusted to the new default value; dragging still persists custom coordinates.

## Evidence gate

Require exact-head Windows/Ubuntu Foundation and genuine Mojang 1.8.9 two-player RGB/text screenshot. Check readability, absence of hotbar overlap and retained native text clipping. Full original-binary acceptance **also includes all historic combat tests**: the previously failed concurrent target-damage negative control remains mandatory and may not be claimed green based on screenshot success.

S34 is a **draft source change**, not a release and not a migration of existing users' profiles.
