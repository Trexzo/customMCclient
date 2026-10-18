# V197 — preserve real original locked-B World through three-player ground stabilization

## Reproduced exact V196 original failure
At commit `36b045e969a4fbe65bbdb2ec53d32bda1c615b7c`, [pinned Mojang 1.8.9 official X11 + dedicated-server workflow #39137125584](https://github.com/Trexzo/customMCclient/actions/runs/39137125584) finished **FAILURE** at the early strict original A/B UUID rejoin test. Its offline Foundation Windows+Ubuntu passed, including V196's grounded-three-player fixture regressions, but the **actual original client** exited after the phase4 host server recenter with:

`rejoin stale or wrong UUID attack phase=4 actual=A locked=A A=A B=B aIndex=6 bIndex=1 da=1.7891839931361735 db=3.960637161313068 selectedA=true selectedB=false`.

Native B was **beyond 3.5 block reach** and original KillAura selected actual source player A before the required live B-stability gate. The real fail-closed UUID guard correctly rejected the attack. This is a new native failure observed after V196 added a real stone floor for all three AND also issued a redundant server teleport of previously locked B immediately before A and host recenter. The evidence proves transient original B-out-of-range and wrong selection at that moment, not conclusively *why* B's position drifted; the extra B teleport is an actionable source-state disruption not required by the grounding fix. V193 earlier failed differently, with `A3D=2.4685269, B3D=2.4281797` under a floor for A only.

## Narrow source-rooted corrective action
- Keep V196's actual original 1.8.9 server-confirmed **stone floor under all three genuine A/B/host positions**, exact original-world command validation, original genuine `testforblock ... stone` checks and unchanged 3D admission gate.
- **Remove the second B `/tp` command in the phase4 stabilization window.** B was already teleported to its canonical position before genuine first B lock and damage; a second teleport of B after that lock is unnecessary and risks transiently losing source B reach / sticky UUID before 20 genuinely stable client World callbacks.
- Keep the original A post-Survival real teleport **once**, as already in V193 and before V196. Keep original host recenter exactly once and require 20 fresh original-native B locked and selected source snapshots after the server ACK. This is not a new shortcut or allowing an invalid controller event.
- Preserve original `PlayerControllerMP.attackEntity` third-argument UUID match, `A + 0.12 < B`, original `db-da>=0.30` stable-window buffer, `B <= 3.5` native reach, actual ray and server-side **B-only** scoreboard damage.
- Keep read-only host/A/B original mapped XYZ on later strict callback failures; these diagnostics never authorize an action.

## Offline test changes
Adapt V196's existing original-floor ordering test to assert **no second B server teleport after three-player floor**, exactly one post-Survival original A server teleport, then host recenter, 20 new genuine source stable callbacks, B-only damage scoreboard. Add a negative test using exact V196 original A/B distance values to assert B beyond genuine native 3.5 reach remains rejected, selected A not reinterpreted as valid locked B. Previous five actual positive/negative spawn/floor-geometry tests retained.

Only exact **V197 full pinned Mojang Minecraft 1.8.9 X11 graphical + dedicated server suite** can show this correction succeeds in real source runtime, followed by an independent same-SHA repeat. V196 historical FAIL stays FAIL, just as V190 Box ESP and V193 M419 failures do. This cumulative Draft child of V196 #662 is not a merge and does not modify `main`.
