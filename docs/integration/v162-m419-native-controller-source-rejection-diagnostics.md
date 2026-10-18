# V162 — preserve strict Mojang M419 admission with actual native-source rejection reasons

## Evidence and immutable parent

V162 is stacked on [V161 PR #627](https://github.com/Trexzo/customMCclient/pull/627), exact parent `66e3e32b5ff4424507c3e46458c06c4ca3af70ad`. The parent Foundation Windows+Ubuntu has **SUCCESS**, but its full original pinned Mojang 1.8.9 run was still underway at this implementation point. The last unchanged original V156 had **two full 78/78 successful original binary runs**, not automatically applicable to newer source commits. V157/V158/V159 original failures remain documented.

One especially important [V160 full original job 39109075105](https://github.com/Trexzo/customMCclient/actions/runs/39109075105) FAILED after source-native **`CUSTOMMC_189_REJOIN_ORIGINAL_UUID_REVALIDATED_PASS`, `STICKY_B_RETAINED_PASS`, `OLD_A_INDEX=2 NEW_A_INDEX=6` and real wrong-A-UUID veto**. At the original controller-B phase it **never printed** the positive `CUSTOMMC_189_REJOIN_REAL_B_NATIVE_CONTROLLER_UUID_PASS` marker within the original bounded deadline. The real host did not necessarily crash: the original test fail-closed timed out. Earlier failures instead had a **real client crash** in the test-only M419 strict verifier when A was too close (V159 source `da<1`) or A's true 3D vertical motion reduced `da+0.12<db` below threshold (V158). These are distinct problems; no synthetic server damage or success marker is allowed.

## Actual production/testing source modification

**Only the opt-in original native acceptance witness** `Minecraft189RejoinTargetAcceptance` is changed. The original fail-closed `Evidence.aCloserButBSelected()` conditions remain mandatory and unchanged: genuine alive A/B players, original UUID identities, source-selected B, source locked B UUID, native source A 3D distance `1.0<=da<=3.25`, B `1.0<=db<=3.5`, and original A at least 0.12 closer than B. The actual original PlayerControllerMP.attackEntity call and independent positive-B/zero-A server scoreboard proof are not modified.

An observational `m419RejectionReason` helper classifies **which existing source condition rejects an actual controller attack**, without generating any approval or override:

- Source evidence absent or real A/B native player missing.
- A/B nonfinite or out-of-range actual 3D source distances, including original V159 lower bound and V158 A+.12<B.
- B not selected by mapped original target/rotation.
- Rejoined A/survivor B real UUID mismatch.
- Aura's original B UUID lock missing.
- Real PlayerControllerMP/controller/player/entity argument unavailable.

If original `verifyRealControllerB` fails, the same strict exception now includes that reason, indices, exact 3D A/B source ranges, B selected state, current locked UUID and expected B UUID. During original phase 4 while waiting for a positive B controller invocation, **one bounded read-only diagnostic every 80 source ticks** prints actual source status/rejection reason. This distinguishes a missing source controller despite valid A/B lock from wrong geometry, without moving a target, setting player rotation, changing source UI, altering actual hitboxes or faking an attack packet.

## Regressions

New `Minecraft189M419NativeControllerSourceDiagnosticTest` has independent Java JUnit cases for:

- Valid original interior A/B geometry and A+.9 native vertical motion that retain strict B distance margin.
- Actual V159 too-close A 3D distances and V158 too-close-to-B 3D distances.
- Missing/NaN/infinite native snapshots, missing live A/B player and excessive B reach.
- Exact original 0.12 margin threshold.
- Wrong native B selection, A UUID mismatch, B UUID mismatch, stale KillAura lock, and absent real original controller argument.

All negative cases return rejection reasons. Tests validate Java predicates **only**, never original-game acceptance, and do not weaken any existing exact source constraints.

## Integration and release gate

Require exact-head V162 Foundation Windows+Ubuntu and full **genuine pinned Mojang 1.8.9 X11 graphical/dedicated-server 78/78 original suite** including original M419 B controller argument UUID and dedicated server positive B/zero A damage, original M425 vanilla physical ray/hurt and V158 Scaffold ItemBlock. If original intermittent M405 blocks, retain failure logs and rerun on the **same SHA**. Source-test PASS cannot be claimed as original-game verified. All cumulative PRs remain Draft and `main` stays untouched; source stability #503 and module parity #537 stay open.
