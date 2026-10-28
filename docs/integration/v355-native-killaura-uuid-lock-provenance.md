# V355 — source-native KillAura UUID lock transition provenance

Minecraft Java 1.8.9 · diagnostic evidence only · stacked after V354.

## Original acceptance evidence

Exact V353 and V354 original Mojang binary preflights **FAILED** in
the V47 real three-client + dedicated-server rejoin scenario:

- V353 run https://github.com/Trexzo/customMCclient/actions/runs/40692618277
- V354 run https://github.com/Trexzo/customMCclient/actions/runs/40692780636

Both had Windows/Ubuntu Foundation CI PASS, and neither native run
crashed in the older `verifyOwnedClick` way. Instead original client
remained alive, but `REJOIN_UUID` never appeared before the strict
deadline. The original source trace at phase 3 showed live B and
rejoined A, with A closer and **selected/locked**:

`phase=3 Aindex=6 Bindex=1 da=1.423162... db=2.236067...
selectedA=true selectedB=false locked=A auraFov=180`

The 180-degree FOV rules out a simple out-of-FOV B filter as the
only explanation. V353's persistent owner policy cannot restore a
UUID that has already been **cleared at another host source boundary**.
Possible paths in the actual source include GUI suspension, invalid
player rotation, native World identity replacement, stale World data,
invalid combat evidence, or legitimate selector retargeting.

## What changed

Under existing opt-in `custommc.acceptance.reportKillAuraRejoin`
**and only phase 2–4**, `Minecraft189RejoinTargetAcceptance` now
logs up to **12** transitions (with category limits of 4 each)
when the original B UUID is lost/reacquired or the original A UUID
is acquired:

`CUSTOMMC_189_REJOIN_NATIVE_LOCK_TRANSITION_DIAG phase=... source=... before=... after=... B_RELEASED=... B_ACQUIRED=... A_ACQUIRED=...`

Source strings are propagated through the actual existing
production mutation sites:

- `selector-live-candidate-or-absent` and
  `selector-verified-existing-uuid`
- `selector-invalid-original-state`,
  `selector-invalid-original-uuid`, `selector-missing-visibility`
- `host-suspend-gui-or-world`,
  `invalid-original-player-rotation`,
  `module-enable`, `module-disable`
- `original-world-identity-changed`,
  `original-world-source-revoked`,
  `original-world-combat-inconsistent`

The tracer **does not set any target**. Production still assigns
`lockedTargetUuid = enabled && lockTarget.get() ? uuid : null`
unchanged. No GUI handling, FOV, combat-packet dispatch, movement,
rotation, or controller behavior changed. It cannot print a PASS,
shorten the rejoin 20-tick requirement, or synthesize a native
controller or authoritative damage. B-only damage/original-UUID
acceptance must still PASS separately or the run remains a FAIL.

## Verification

- JUnit `Minecraft189KillAuraOwnerTransitionProvenanceTest`: explicit
  reason labels preserve source lock, suspension and disabled semantics.
- Python `scripts/test_mojang189_v355_killaura_native_lock_transition_trace.py`:
  checks the opt-in phase/quantity fences and real World / controller
  / server proof contracts.
- `scripts/test_mojang189_v353_killaura_sticky_uuid_owner.py` adjusted
  to check the same source-revocation sites with reason labels.
- Require exact-head Windows + Ubuntu Foundation CI and actual original
  Mojang 1.8.9 graphical / dedicated-server native acceptance.
- The next behavior change should only follow concrete evidence of
  which exact transition removed B; do not assume GUI or FOV alone
  without an original native source marker.

No `main` merge, unrelated UX/visual file changes or user-side
manual test is required to record this diagnostic.
