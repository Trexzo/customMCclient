# V181 — original Mojang FOV negative source World teleport convergence

## Actual original-client failure
The genuine pinned 1.8.9 [V179 original run 39126118415](https://github.com/Trexzo/customMCclient/actions/runs/39126118415) **FAILED** very early in the strict FOV rear-player negative. Its **real dedicated server** had independently acknowledged a sacrificial air preclear, then a fresh positive `15 blocks filled` real stone floor and authentic `tp CIHost -976.5 5.0 -424.5` / `tp CITarget -976.5 5.0 -422.5`. That server output alone did not mean the original client's World had received both teleports.

The genuine host Java JVM crashed with `out-of-FOV real player caused synthetic attack`, with its source-native independent readback `actualDx=-1.0`, `actualDz=-3.0`, `playerYaw=161.56505`, `actualYawDelta≈0`, `independentlyPermits=true`, and genuine World player listing host `(-976.50,5,-424.50)` against target **old** `(-977.50,4,-427.50)`. Hence source World was stale relative to the real server's new target position when the 20 disabled callbacks permitted test Aura ON; the negative premise (actual source target behind and at the scripted position) was no longer true. No FOV or target/click production bug is established by this diagnostic outcome.

A previous independent [V175 original run 39120851108](https://github.com/Trexzo/customMCclient/actions/runs/39120851108) failed the *late* FOV scenario with native horizontal distance `2.0` but full original 3D `5.852349955`, violating the in-range premise even when rear-facing yaw was correct. V177 had introduced an actual stone floor; this V181 acceptance-only guard adds a separate client-side convergence prerequisite.

## Narrow corrective change
Within `Minecraft189RuntimeBridge` **only in the opt-in original FOV acceptance observer**, retain all former real source constraints: one actual living player, original rotation and World snapshots, real distance [1.5, 3.25], real yaw separation >=120°, 20 consecutive module-OFF callback witnesses, 80 consecutive module-ON real native FOV veto callbacks, original no-click/controller and zero server damage.

Add a **stricter test source geometry prerequisite**: from those same actual (unforged) original client World snapshots require `|dx|<=.20`, `|dy|<=.30`, `||dz|-2|<=.25`, reflecting the real dedicated server's already-issued two-player shared X/Y and two-block Z teleports. The new read-only observer also reports `originalFovWorldAligned=` in periodic native source diagnostics.

The existing original target selection, production KillAura FOV, source-native independent click gate, vanilla controller, real player yaw and packet flow are unchanged. If the source has not yet consumed both server teleports, the test fails closed / waits rather than enabling Aura from a transient stale target. No gameplay PASS is forged.

## Regression and release gate
Four adversarial JUnit tests: exact real stale (-1,-1,-3) original target, prior observed 5.5 vertical deviation, positive genuine 2-block server geometry (both Z signs), and nonfinite/off-axis/too-far negatives. These are offline policy tests only.

Cumulative Draft child V180 #646; original client 78/78 remains unverified. Exact-head Windows/Ubuntu Foundation plus complete genuine Mojang 1.8.9 X11/vanilla dedicated-server suite and independent same-SHA repeat still mandatory; no merge/main mutation.
