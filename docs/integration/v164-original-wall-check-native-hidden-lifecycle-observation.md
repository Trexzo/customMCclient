# V164 — original 1.8.9 Wall Check lifecycle native-source diagnosis

## Exact prior original CI and limitations

V164 is a cumulative **Draft child of V163 PR #629**, exact parent `09e936885eaf84ed2351f765c5fffa3ab77ecc62`. [V163 official pinned original Minecraft 1.8.9 run 39110234098](https://github.com/Trexzo/customMCclient/actions/runs/39110234098) **FAILED** after ~73 completed GitHub steps in **original Wall Check off/on lifecycle**: `CUSTOMMC_OFFICIAL_189_LOCAL_MULTIPLAYER_FAILED: missing 30 native hidden callbacks with stale player deselected marker within deadline`. Actual original test had **already** proven original solid stone wall, no server damage while occluded, positive damage after wall removal in a separate original-world session, genuine original wall negative veto, and correct subsequent enable/disable markers in the lifecycle session. Its host log then ended after `CUSTOMMC_189_WALL_LIFECYCLE_NORMAL_REENABLE_PASS=YES`. No `STALE_TARGET_CLEARED_PASS`, no corresponding server proof. **The log does not yet show whether the cause was a changed real LOS/World source sample, stale selected native target/rotation after re-enable, original player moving/unloading, or tick scheduling.** Keep all possibilities open.

Independent [V161 original 39109466516](https://github.com/Trexzo/customMCclient/actions/runs/39109466516) **FAILED** the unrelated original far-range Aura negative due to actual remote/player distance changing; it cannot be called V163 Wall Check reproduction. [V162 original 39109954166](https://github.com/Trexzo/customMCclient/actions/runs/39109954166) **COMPLETED SUCCESS 78/78** across pinned real Mojang binary GUI/server, including original Wall Check; a *separate rerun on exact unchanged SHA* was requested for stability evidence and must not be assumed passed until complete.

## Changed test-only production-source acceptance witness, never gameplay gates

File: `platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9/Minecraft189RuntimeBridge.java`.

- Adds `originalWallLifecycleSourceReason(...)`, a **read-only fail-closed classifier** of the exact underlying 1.8.9 tick snapshots used by the mandatory acceptance: live original target index, available and correctly indexed WorldEntityKind/Combat/Visibility source samples, remote living player and native alive state, real `los.occluded(index)`, module active status, original `nearest.found()` and `targetRotation.available()` evidence.
- During **only** the opt-in original wall lifecycle's post re-enable phase 2, every 80 genuine original ticks emits one `CUSTOMMC_189_WALL_LIFECYCLE_NATIVE_SOURCE_DIAG` line with the reject-only reason and source index/counts, native B/source selection indices, module state and `acceptanceWallLifecycleReenabledVetoTicks` count.
- The existing required **30 consecutive original occluded, remote alive, NO nearest target, NO rotation target** callbacks are entirely unchanged. If any original source fact is absent/invalid, the old acceptance still resets its counter. The classifier is never consulted to authorize any source hit/click, never advances a counter, never creates an entity/packet, never bypasses real LOS, never emits a new synthetic PASS marker or changes the original deadline.
- This preserves direct evidence for distinguishing original world/scheduling instability from actual stale native module target state, instead of repeatedly guessing a fix.

## Six adversarial JUnit checks

`Minecraft189WallLifecycleSourceDiagnosticTest` proves separately: correctly occluded remote with both target states cleared is the only diagnostic `clear` result; either stale nearest or rotation independently reports a rejection; original entity kind/index/source data invalid does not count; native LOS unoccluded does not count; module disabled does not count; diagnostic values contain no PASS marker or synthetic 30-frame success. Tests use boolean native-source mock inputs **only**, not live original Minecraft world observations.

## Integration gate

Require exact-head Windows/Ubuntu Foundation and full pinned original Mojang 1.8.9 GUI/dedicated server **78/78** including original Wall Check lifecycle, M405/M419 B UUID controller real B+/A0 server damage, M425 manual physical ray/hurt, Scaffold real ItemBlock, GUI E and FOV negative original cases. If an original timeout repeats, collect diagnostic reason and fix the proven upstream state/fixture root cause; no weakened strict gates or blind longer timeouts. Both V163 and V164 remain Draft; no merge, no changes to `main`, #503 reliability and #537 module parity open.
