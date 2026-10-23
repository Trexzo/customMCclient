# V307 — original UUID fixture source reconciliation (2026-10-23)

Exact Draft parent V306 `9d4f8ba6f19cb5c04a6348075a8c419968478270`, [PR #778](https://github.com/Trexzo/customMCclient/pull/778). V304 original double-certification is historical **only** for unchanged `a7a4f8e`. Neither V305 nor V306 double-certified.

## Real original red jobs: diagnostic evidence

- [V305 attempt 2](https://github.com/Trexzo/customMCclient/actions/runs/39798056714/attempts/2) job `120454645153`: 56 workflow steps passed, genuine original sticky UUID test failed. Mojang **Minecraft crash report**, not unknown OS abort: `Minecraft189LockedTargetAcceptance.verifyOwnedClick` rejected B's exact real UUID in phase 4 after server teleported A. Same-source native A=**4.27320473283224** blocks (mandatory >**4.5**), B=**1.4142**; World native selector, rotation, and locked UUID all B. Real original A server /tp used only `x+5`; during native position movement B became eligible before A met the intentional 4.5-block safety margin. Preserve native rejection and historical failure.
- [V306 attempt 2](https://github.com/Trexzo/customMCclient/actions/runs/39800937704/attempts/2) job `120454663132`: 27 prior original stages PASS, original V47 pre-lock did not produce ten original UUID-lock ticks. Native host diagnostic repeated `validEvidence=true`, A selected, A=**1.828859361** blocks, B=**11.22497216** blocks, `disabledPreferredTicks=0`. Actual original source A must beat B while **both within 3.5** to pass 20-tick prelock. The server had acknowledged B's /tp at initial high login-derived height, but a server reply alone is not native position convergence; an absent real support floor permits players to occupy different real Y levels and lose B 3D reach. Do not weaken source/UUID gates or claim ground-state cause definitively until new original run.

## Fixture-only corrective changes

1. Sticky A server-away command uses **x+12** instead of x+5, still inside actual view-distance 3 chunks. Demand exact fresh original `Teleported CITarget to ...` server reply. The unchanged fail-closed native A>4.5/real B UUID/angle/damage checks still decide PASS. A premature B click can still genuinely FAIL.
2. For original rejoin-only scenario, derive one **35-block stone platform at Y-1** from actual login-based server-host/A/B teleports; `fill` and independently verify three block cells using genuine `/testforblock` source responses **before the three players' teleports**. Keep the original real 20-tick A/B selection proof, 10-tick lock, loadedEntityList UUID/rejoin, genuine vanilla controller, PvP damage, negative rejects intact. A floor receipt is *not* a World/player/UUID PASS; the real Mojang game must independently converge.
3. Demand exact fresh B /tp server receipt. No arbitrary sleeps, timeout expansion, mocked hit events, input/packet controller override, synthetic entity data, production Java changes, or retry of failed native stages. Original X11 package preflight, all 78 workflow acceptance stages unchanged.
4. 11 new offline tests cover positive forward/backward world-cell support, invalid ownership/geometry/height/border, actual x+12 displacement, exact genuine server receipt, ordering, real native gates. Update two original fixture expected values. Source tests are **not** original Minecraft certification.

## Certification

V307 requires Foundation Windows and Ubuntu 10/10 plus **two separate full real pinned Mojang 1.8.9 GUI/X11 + vanilla-server 78/78 PASS** on the exact same final V307 SHA. V305 and V306 second original failures remain red. V304 retains its own two verified original 78/78 PASS jobs. All PRs Draft, `main` unchanged; no merge or release. Audit in [issue #777](https://github.com/Trexzo/customMCclient/issues/777), original failure log in [issue #503](https://github.com/Trexzo/customMCclient/issues/503).


## Corrective verification of idempotent vanilla fill response (2026-10-23)

[Original #39982461933](https://github.com/Trexzo/customMCclient/actions/runs/39982461933)
**COMPLETED FAILURE** at real V47 after successful earlier native stages.
The dedicated original server logged `No blocks filled` for the exact 35-cell
stone support fill, whereas the fixture insisted on `35 blocks filled`.
That disagreement does **not** prove missing stone, and the historical job
stays FAIL.

The corrected runner snapshots the dedicated-server log before the actual
original fill command and checks the fresh vanilla INFO reply for **only**
`35 blocks filled` or `No blocks filled`. In either case, each of the three
distinct genuine underfoot cells MUST still receive its own successful original
`testforblock ... stone` reply before teleport. An empty, malformed, unknown
or partial-fill reply is not accepted. This is an idempotent command receipt,
**not a substitute** for World entity positions, V47 A/B preference, native
UUID lock, vanilla controller or server damage. No native criteria/timeouts,
game Java or production code change. Source-only parser tests cover both
allowed responses and invalid alternatives. Two independent full original
78/78 runs on one exact corrected SHA remain mandatory for certification.


## Genuine second V47 failure and independently verified 2x2 phase-4 support (2026-10-23)

On immutable `fac31b561b654619be1974fee6e568d31125f421`,
[original Mojang #39985969482](https://github.com/Trexzo/customMCclient/actions/runs/39985969482)
**COMPLETED FAILURE** at V47 after the genuine three-player original first
floor returned `35 blocks filled`, original rejoin, actual native B UUID owner,
and actual native controller/crosshair checks progressed. The original vanilla
second `fill 1 4 477 2 4 478 stone` reported `No blocks filled`, but the
script demanded `4 blocks filled`. That reply is consistent with prior stone,
yet alone cannot prove stone remains.

Corrected source-only fixture (still Draft): the second fill must produce its
own fresh exact vanilla INFO `4 blocks filled` or `No blocks filled`
receipt, followed by **four** independent genuine `testforblock ... stone`
receipts for all 2x2 cells. A different positive count, empty log, forged WARN,
or error fails closed. The prior **three** original host/A/B floor witnesses
also remain strict. This does not change Java, native V47 original entity/range,
UUID ownership, camera, controller or PvP damage acceptance, timeout, server
processes, or expected full 78 stages. Prior immutable FAIL is retained.
Both Foundation gates and two FULL independent genuine original 78/78
on one corrected exact SHA are still mandatory before double certification.
