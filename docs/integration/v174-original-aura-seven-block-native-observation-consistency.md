# V174 — repair seven-block original player range-negative source observer (test-only)

## Confirmed deterministic mismatch in real original CI

The source-specific initial far-range fixture was changed in V165 from a genuine four-block dedicated-server player teleport to a **seven-block separation inside the same real loaded chunk**. This was motivated by a recorded V161 original-client failure: despite a nominal four-block server teleport, the real source player moved to actual `distanceSquared=2.3134765625` (≈1.52 blocks), inside KillAura's configured normal **3.5** attack reach. The test properly rejected its own premise. V165 maintained native actual 80 positive veto observations and independent server zero damage while adding actual World margin.

However the **test-only source witness** `Minecraft189RuntimeBridge` under `custommc.acceptance.reportKillAuraRangeVeto` was never updated. It still demanded the only real nonlocal living player have **squared distance 14.44 to 23.04**, i.e. only 3.8–4.8 blocks, and forcibly set `farIndex=-1` for any other distance. Genuine dedicated Minecraft 1.8.9 player `distanceSquared=49.0` under the new seven-block fixture was therefore **permanently rejected**, resetting the required original callback counters. This was **not** an Aura gameplay range or hit failure; it was deterministic inconsistency between two test components.

Direct immutable original evidence:
- V168 first run [39114922799 attempt 1](https://github.com/Trexzo/customMCclient/actions/runs/39114922799) **FAILED** at original negative Aura-disabled: repeated genuine `distanceSquared=49.0 qualifyingIndex=-1`, then `missing 20 live far-player callbacks with Aura disabled marker within deadline`. An initial sample ~72.29 also remained outside the narrow old source window.
- V171 [39116726705](https://github.com/Trexzo/customMCclient/actions/runs/39116726705) **FAILED** after 69 prior successful original steps: original source `distanceSquared=49.0 qualifyingIndex=-1`, then `missing 80 active Aura callbacks rejecting real distant player marker within deadline`. Two different missing 20/80 acceptance markers had the SAME real player eligibility rejection.
- V168 exact-SHA rerun later failed unrelated V19 native stone UP-face raytrace; V172 addresses the separate original-world verification gap. No previously failed run is retroactively counted PASS.

## Production/gameplay remains unchanged

In `platform-1.8.9/.../Minecraft189RuntimeBridge.java` the **opt-in original test-only** observer now uses a pure `originalSevenBlockRangeFixtureDistance(squared)` predicate with original source sample range **6.5–7.5 genuine 3D blocks inclusive**, i.e. squared **42.25–56.25**. It rejects any NaN, infinity, near real Aura-range player, old 3.8–4.8-only geometry or remote too far from the known seven-block server setup.

The actual native seven-block player requires all ORIGINAL World source gates separately: available original local player position, genuine World entity positions and kind snapshots with equal count; real one and only one nonlocal player who is also living; original source squared distance in that exact range; stable same native entity index. None is synthesized.

The existing 20 consecutive original **Aura-disabled** live source callbacks, actual in-test normal module activation, 80 consecutive original **Aura-enabled and unselected/no target rotation** observations, fail-closed unexpected real synthetic attack/controller veto and independent genuine dedicated-server scoreboard **zero target damage** remain **unchanged**. No target selection, game hitbox, configured Aura reach **3.5**, attack rate, mouse packet, native server damage, cooldown, wall check, rendering or real player movement changed. Successful source eligibility alone is NOT a gameplay PASS.

The native log now also includes `validSevenBlockRange=`, `auraEnabledForAcceptance=` and true disabled/enabled consecutive counts in the existing bounded every-200 observation diagnostic. This surfaces source classification and module/tick progress without automatically incrementing counters or fabricating success.

## Four new negative/positive Java tests

`Minecraft189AuraRealRangeSourceWindowTest` verifies genuine 7-block (=49.0) and its 6.5/7.5 endpoints are eligible; prior old 4, 3.8, 4.8 and actual V161 near 1.52 source are explicitly rejected; NaN/∞/out-of-window real distances remain fail-closed; and the source eligibility predicate is not a native attack or controller invocation. Tests are JUnit source-only, not Mojang physical game proof.

## Exact native integration and policy

V174 is cumulative Draft child of [V173 PR #639](https://github.com/Trexzo/customMCclient/pull/639), preserving all V151–V173 real production module fixes, new genuine V19 stone source and V29 Scaffold source preparations, improved M419 A 3D gap fixture. Require V174 exact-head Foundation Windows+Ubuntu, full pinned official Mojang Minecraft 1.8.9 original graphical GUI/integrated/dedicated server **78/78**, including actual negative Aura range 20/80 source callbacks **and dedicated-server zero damage**, native M405/M419 original B UUID + actual positive-B/zero-A damage, M425 X11 physical mouse ray/hurt, V19 UP-face stone raytrace, V29 survival Scaffold native controller/world ItemBlock receipt, physical rod/potion effects, Wall Check and FOV. On one full 78/78 run, repeat **identical SHA** independently to confirm deterministic reliability before merging.

Earlier V162 exact `66db7002abadb07207fbdfbf07171311bf494828` remains the only baseline with two separately completed full original native 78/78 passes. Newer branch Foundation passes are not equivalent. All PRs Draft, no merge, `main` untouched, original reliability #503 and real module parity #537 remain OPEN.
