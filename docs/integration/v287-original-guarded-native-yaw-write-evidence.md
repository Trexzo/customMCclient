# V287 — distinguish Aura aim eligibility from successfully completed native yaw writes

Parent V286 Draft PR #757 exact immutable `0198339a1331e54062e4c620823e49097d6a8bb1`.

## What real original Mojang evidence requires this

V282's original [run #39620847862](https://github.com/Trexzo/customMCclient/actions/runs/39620847862) genuinely **FAILED** V46 rear-player FOV: original player yaw first **0°**, actual remote target yaw **−180°**, 16 real rear-player veto callbacks with no target selected; at the unexpected synthetic attack original player yaw **−180°**, aligned target and `independentlyPermits=true`. The original negative test hard-failed. The cause of this yaw transition is unproven.

V284 recorded last already-verified rear yaw at the existing hard rejection; V285 captured the true return value of `Minecraft189KillAuraModule.aim` and real pre-aim target/yaw source, but **`aim()` returning true does NOT mean the guarded player yaw setter actually executed**. The existing `Minecraft189RotationHostSourceLease` can independently veto native setters after source/GUI revocation. This is a real ambiguity in the forensic record.

V286's 10 tests against **production selector/aim/native FOV** confirmed default 90° FOV rejects yaw 0° to rear target −180°, and an eligible actual Aura write is bounded by default 10° step. That is an offline source invariant, NOT proof of which original player rotation handler moved the camera.

## Actual guarded native write evidence, with no additional game actions

- Extend existing `Minecraft189RotationHostSourceLease.guardedControl()` with an optional `NativeYawWriteObserver`; the original two-argument method still delegates to the same implementation with a null observer. In the guarded yaw setter, **run exactly the original `nativeSource.customMcSetRotationYaw(yaw)` once**, then notify observer **only after the genuine original setter completed normally**. A revoked lease executes neither the setter nor observer; a native setter exception cannot be miscounted as a completed write.
- In the existing original `Minecraft189HostRuntime.playerRotation`, pass a recorder callback **only if** the explicitly opt-in `custommc.acceptance.reportKillAuraFovVeto` system property is true. No extra original player yaw, World, entity or packet getter. The unchanged Aura `aim()` call still happens once with its same arguments. Count completed setter callbacks before the call, and after existing source ownership validation attribute **only the increase within that exact Aura call window**. Other rotation module writes do not falsely become Aura-owned.
- `Minecraft189OriginalFovGuardedYawWriteTrace` stores the cumulative number of successful original guarded setter completions, the number completed in an observed Aura call window and last requested yaw for each type; no player, control, World or service access. Existing original bridge reset clears it.
- V284/V285's **existing unconditional real unexpected-FOV-click exception** now prints `completedGuardedNativeYawWrites`, `auraCallbackNativeYawWrites`, `lastCompletedNativeYawSetter`, and `lastAuraNativeYawSetter` alongside the previously read original source positions/yaw, rear witness, and Aura aim return counts. The exception still throws, does not allow or veto an additional click, and has no new native getter.
- Eleven Java 8 JUnit source regressions exercise the **actual guard path** (original delegate setter exactly once before observer, GUI revocation veto, throwing native setter not counted, original observer-free method unchanged), correct Aura window attribution, invalid before-count fail closed, finite signed yaw preservation and reset. Offline Java fixture is NOT Mojang native acceptance.

### Evidence interpretation and bounds

If a future genuine FOV failure reports `auraCallbackNativeYawWrites>0`, the original callback returned from a real native yaw setter in an observed Aura aim window. This makes Aura a plausible contributor but still doesn't establish all yaw motion or server authority. If the count is zero and original yaw changed, upstream vanilla/server/input/another rotation module remains plausible. A setter invocation that *throws after changing local yaw* is not reported as successfully completed; it is separately visible as an exception. No single count establishes a cause beyond recorded JVM behavior.

**No change** to production FOV eligibility, target selection, AIM step, client mouse/World/packet access, native source guards, GUI focus veto, actual 20+80 witness gates, strict hard failure, original 78-step binary acceptance, retries or timeouts. All real V276/V277 native UUID phase failures and V282 negative FOV failure remain genuine red historical evidence tracked under issue #503.

Certification requires **both** Windows and Ubuntu Foundation 10/10 **and two independent full** actual original Mojang 1.8.9 X11 GUI + vanilla dedicated-server 78/78 on exactly the same SHA. PR remains Draft; `main` unchanged; no merge or release.
