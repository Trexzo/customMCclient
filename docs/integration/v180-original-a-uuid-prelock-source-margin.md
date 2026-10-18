# V180 — genuine initial A prelock 3D source separation

## Exact original Mojang failure evidence
Original Minecraft 1.8.9 full [V177 run 39124397967](https://github.com/Trexzo/customMCclient/actions/runs/39124397967) **failed**, late in the full suite, on the independently repeated genuine three-client rejoin phase before A disconnect. A separate earlier V177 M419 passed genuine source-B controller and positive server B damage; that earlier pass does **not** establish the later entire run as green.

The failing ORIGINAL Minecraft client printed:
- `validEvidence=true`, `Aindex=5`, `Bindex=6`, `A_selected=true`, `auraActive=false`
- `A_distance=2.2222683371951284`, `B_distance=2.23606797749979`
- `disabledPreferredTicks=0` across repeated original-source observations

Even though A was visibly closer and actually selected, B−A was only **0.0138**, below the **unchanged 0.12** live-native `Evidence.aPreferred()` admission requirement for 20 consecutive callback observations. Failure `missing original genuine A UUID locked before disconnect marker within deadline` was therefore expected and correct: this is the same family of world-geometry stability failures seen later on M419, but in phase 0, **before any lock**.

## New real dedicated-server placement, not fake client behavior
The original rejoin-only harness now derives a **second real A teleport** from the actual dedicated-server host/first-A/B commands, preserving real login-derived block/chunk ownership, genuine players, B position, Y/yaw/pitch and all native selection/UUID checks.

Before the existing final B teleport, command `tp CITarget <host center x> <same Y> <host center z +/- 1.55> <original yaw> <original pitch>` positions **only initial A** 1.55 blocks ahead rather than two full blocks ahead. The known real B remains one block lateral/two ahead, distance sqrt(5). Source geometry is validated before command submission: no untrusted usernames, no crossing loaded source chunks, `da-0.30>1` and `da+0.30+0.12<sqrt(5)`. Genuine Mojang server confirms the A teleport, then B remains its last actual server teleport.

Actual native World A/B coordinates, 20 consecutive original A-first callbacks, real source lock, original outgoing controller and independent positive/zero server-damage assertions still decide success or failure. There is no synthetic World list, no edit to `Evidence.aPreferred()`, no packet, no skipped gate, no image/GUI workaround.

## Regression and CI gate
Four additional Python offline fixture tests cover positive/negative source-chunk geometry, unchanged original Y/yaw, loaded chunk retention, direct recorded native failure witness, invalid UUID-like player identity/geometry and final real server B-command ordering.

This is **Draft V180 stacked above V179 #645**. Re-run exact-head Foundation on Windows and Ubuntu plus the entire pinned original Mojang Minecraft 1.8.9 GUI + dedicated-server test battery (all 78/78), then independently repeat exact-SHA for stability; older CI failures stay classified FAIL. `main` unchanged.
