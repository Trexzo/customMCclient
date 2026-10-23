# V307 genuine V47 second-run source-distance failure — real initial floor and teleport ACK

V306 [Draft PR #778](https://github.com/Trexzo/customMCclient/pull/778), immutable
head `9d4f8ba6f19cb5c04a6348075a8c419968478270`, has completed Foundation Windows+Ubuntu 10/10 and a first
genuine original pinned Mojang Minecraft 1.8.9 GUI/X11+vanilla server 78/78
PASS, [run #39800937704 attempt 1](https://github.com/Trexzo/customMCclient/actions/runs/39800937704/attempts/1)
job `119901083388`. Independent [attempt 2](https://github.com/Trexzo/customMCclient/actions/runs/39800937704/attempts/2)
job `120454663132` **COMPLETED FAILURE at real V47**, 27 prior steps PASS.
This historical FAILURE MUST remain red. V306 is NOT double-native-certified.

## Verified original evidence, not an inferred source-code failure

The original live host tail repeats:
`CUSTOMMC_189_REJOIN_INITIAL_PRELOCK_EVIDENCE validEvidence=true
disabledPreferredTicks=0 Aindex=19 Bindex=18 A_distance=1.8288593610499415
B_distance=11.224972160321824 A_selected=true ... nearestFound=true
rotationSelected=true auraActive=false`.
The true `Evidence.aPreferred()` predicate requires original `B` **between
1.0 and 3.5 blocks** as well as A in the original 1.0–3.25 range and
`A + .12 < B`. Original B=11.225 FAILS the legitimate reach predicate.
`auraActive=false` is intentional while disabled `phase=0` accumulates 20
successful actual-source ticks; it is NOT by itself evidence of broken Aura.
The dedicated server had acknowledged game startup and the genuine live
clients, and the Python harness printed the intended B server teleport
*without verifying the /tp command response*; a simple 0.30s send was never
proof of native source convergence. Original test V47 then failed
`missing original genuine A UUID locked before disconnect marker within deadline`.

The source geometry intends A=2.0 and B=sqrt(5), both same Y; later V419
phase4 already uses a true verified *three-player stone floor* because real
creative B can drift/fall away from native 3D reach. The V47 **initial**
phase did not provision any such support. The observed 11.225 source B
distance is consistent with unacknowledged/stale server teleport or a
vertical Y separation; exact cause among these remains unproven.

## Strict original-only fixture repair

- Before initial host/A/B /tp, derive a **bounded real vanilla stone floor**
  at server-proven common source feet Y-1 for all THREE true player positions,
  including a three-cell lateral buffer. Require real dedicated-server
  `fill` reply and **three independent positive** `testforblock ... stone`
  source receipts, before any player teleport.
- Require a real successful vanilla server console `Teleported CITargetB to`
  receipt for the final B /tp, instead of writing and waiting 0.30s.
  Keep planned original A-before-B teleport sequence and genuine World
  observation. Neither console receipt can count as a native player lock.
- Preserve **unchanged** native `Evidence.aPreferred`, <=3.5 reach,
  .12 source margin, 20 disabled ticks, exact A UUID lock, vanilla
  controller, true PvP damage, negative UID/FOV/World gating, all original
  78 stages and their strict timeboxes. No Java gameplay changes or synthetic
  collision/camera/entity/attack evidence.
- Add six source-only Python regressions for source player IDs, geometry,
  positive floor location, forbidden height/chunk crossing, acknowledgement
  and unchanged genuine acceptance. These tests are NOT original gameplay.

V307 is a separately stacked Draft child of V306 and must independently pass
exact-head Foundation Windows+Ubuntu 10/10 **and TWO complete independent
real Mojang original GUI/X11+dedicated-server 78/78 jobs on the same V307 SHA**
before double-native certification. V304's historic certification remains
valid only on its own immutable SHA; V305 second original attempt may still
be running. All PRs Draft, main untouched, no merge/release.
