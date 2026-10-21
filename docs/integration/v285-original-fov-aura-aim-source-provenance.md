# V285 — did original production Aura aim participate in the FOV yaw transition?

Immutable stack parent: V284 Draft PR #755, SHA `f158ad77d5ea674eeaf22e536d630037f37aea49`.

## Actual pinned original failure requiring evidence

V282 [original Mojang 1.8.9 X11+vanilla server workflow #39620847862](https://github.com/Trexzo/customMCclient/actions/runs/39620847862) FAILED genuine V46 rear-player FOV acceptance, at 26 earlier completed steps. Original camera had yaw 0° when the genuine remote target was at yaw −180°, with 16 source-aligned rear-veto callbacks and no selected player. At the hard rejected real click, original camera yaw −180° and pitch about 26.565° aligned with the target; original current-source independent FOV gate `permits=true`, selected index 1, distance 2.236, so the negative original acceptance threw `out-of-FOV real player caused synthetic attack`. A rotation occurred, but the previously available real evidence does not show who produced it.

V284 only preserves the earlier genuine rear camera yaw and callback tick on the existing rejecting click. To distinguish normal upstream native player rotation from Aura's own target-aim execution, V285 traces the **real result of the original unmodified `Minecraft189KillAuraModule.aim` call**.

## Narrow recording approach

- **Only when** JVM system property `custommc.acceptance.reportKillAuraFovVeto=true`, after the existing rotation-host source lease is confirmed, inspect values the original `Minecraft189HostRuntime.playerRotation` already possesses: real pre-aim source yaw, existing current `TargetRotationState.Snapshot` selected yaw/index and the actual return boolean from the *same existing* Aura aim call.
- Use the **identical** `targetRotationState.snapshot()` call as before, exactly once. `aura.aim(...)` still executes exactly once, with identical player-control handle, rotation, selected target, input and movement; its boolean return is only observed instead of discarded. No additional original player getter, controller call, yaw setter, World read or gameplay mutation.
- Record aggregate callback count, count of aim calls returning true, latest pre-aim original source yaw, latest *accepted* aim's pre-aim yaw, target yaw and native selected index in a small synchronized in-memory trace. The word *eligible* means `aim()` **returned true**, **NOT** that the guarded native setter successfully changed the player rotation.
- V284's **existing unconditional hard exception** for unexpected native FOV click now logs the trace fields alongside its prior native current-yaw and rear-witness data. No changes to permission, error branch or existing pass markers.
- The trace is reset by the existing original bridge reset. It never fabricates source values: unavailable yaw is NaN, unavailable selected target is index -1.
- Ten Java 8 JUnit tests directly exercise the recorder, eligibility counts, original pre-aim and target yaw, source disappearance, malformed data, reset, and absence of fabricated PASS markers.

Interpretation: If the real V46 failure recurs with `originalAimEligibleCallbacks>0` before the unexpected click, original production Aura participated in aim execution; it still does **not** prove the guarded native yaw setter succeeded or that Aura was solely responsible. If the counter is zero, this recorder did not observe successful Aura aim in this original-host callback window; investigate original input/server yaw or upstream source publication next. Neither result is a gameplay fix.

## Boundaries and certification

The production target selector, angular FOV gate, actual source/camera setters, original event ordering, pre-existing module activation and `aura.aim` inputs, original real-server fixture, the real 20-disabled/80-enabled rear-veto requirements, original player/world, original X11 GUI and vanilla server, outgoing packets, current negative hard rejection, 78 workflow stages, timeouts and retries are **unchanged**.

Source-level Java 8 JUnit success is not native certification. Require exact-head Foundation Windows and Ubuntu 10/10 plus **two separate full 78/78 authentic Mojang Minecraft 1.8.9 X11 client + vanilla server jobs on unchanged SHA**. Preserve earlier V276/V277 original UUID failures and V282 original rear-FOV failure as true FAIL history, issue #503. PR remains Draft, `main` untouched, no merge or release.
