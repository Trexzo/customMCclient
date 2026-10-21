# V274 — genuine simultaneous B-priority rejection source diagnostics

**Parent** V273 immutable Draft #744 `a3d0598fde617546a674c8a72445c01b2d01e355`.

## Original evidence, distinct from V270 rejoin/sentinel failure

V272 first pinned original Mojang 1.8.9 X11 + vanilla dedicated-server 78-step workflow [#39593532156](https://github.com/Trexzo/customMCclient/actions/runs/39593532156), job `119200139727` **FAILED after 57 passed steps** at genuine `Verify two SIMULTANEOUS real eligible UUID targets arbitrate and take separate damage`. The client process exited code 255 because the original runtime deliberately threw an `IllegalStateException` from `Minecraft189SimultaneousTargetAcceptance.verifyOwnedClick` line 109:

`simultaneous early B click without both-eligible live native priority evidence`

Vanilla server log confirmed a genuine first A hit score 20 and unchanged B score 0, original host then issued real B teleport to 521.5,5,449.5. The crash occurred after the B teleport, before proof of a separate valid B hit. This does NOT establish whether A was knocked outside native 3.5-reach, B's original position had not arrived, the nearest/rotation snapshots were stale, player A died/unloaded or some other exact source condition prevented the two-player fresh owner. We must not invent a conclusion or weaken the original guard.

## Change
**On this specific existing genuine fail-closed exception only**, take an independent second read of the actual original Mojang local-player + loadedEntityList UUID, kind, combat, position, nearest and selected rotation snapshots. Include a bounded diagnostic reason, number of remote players, exact A/B current native list indices, alive flags, real source 3D distances, UUID/kind/combat/position entity counts, nearest selection index and rotation index, and actual clicked index in the thrown exception. This second read is explicitly diagnostic: the World can change between initial strict check and diagnostic; it is *not* used to authorize clicks, forgive or retry a rejected action, suppress a crash or replace the first original snapshot. If the second read shows valid B it still cannot retroactively PASS the first failed native callback.

**Strict native behavior unchanged:** both real living A/B UUIDs simultaneously eligible, original 3.5 reach, B >0.12 closer, exact fresh real crosshair owner, nearest/rotation = clicked B, and later two distinct real server-owned damage events. Identical original crash-on-rejected-B remains in place, now with actionable source evidence. No production module targeting, packet, server, GUI, scaffold, mouse, clock or world mutation changed.

16 new Java 8 JUnit regressions cover genuine native priority boundaries: unavailable/misaligned snapshots, missing A/B, two remote players only, A/B alive, two reach ranges, B 0.12 closer, nearest and rotation B, NaN/infinity cases, no fake acceptance markers. These source-level tests do not certify real Minecraft behavior.

**Certification gates:** unchanged-SHA Foundation Windows+Ubuntu 10/10, full original Mojang Minecraft 1.8.9 X11 GUI + vanilla dedicated server 78/78, then second independent full unchanged-SHA 78/78; preserve earlier failed original V272, V270, V266 runs. No waivers, all PRs Draft, main untouched.
