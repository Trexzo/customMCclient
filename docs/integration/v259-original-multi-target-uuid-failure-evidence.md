# V259: Preserve strict stale-UUID acceptance; capture original source evidence

Parent V258 Draft PR #729, exact source `1cefee517d220ce1f3b4960ceb23f333ba50f3ad`.

## Failure observed
V258 exact-head official original CI #39441372820 failed at step 53. Original Mojang Minecraft 1.8.9 `CIHost` crashed, not merely timed out: `Minecraft189MultiTargetAcceptance.observe` threw `stale original target became selected` after server sent two adjacent genuine teleport commands. The crash report still showed A relatively near the local client and B farther away, but the witness had already entered the B-switch stage. This is *not* proof of whether target-selection logic, client snapshot arrival, a race during teleports, or harness sequencing was the root cause.

## Change
This is **test-only failure diagnostics**. Keep exact `stage >= 3 && aCandidate` condition and throw. Append the current same-callback native World UUID/index, XYZ, original A/B distance, local XYZ, nearest and rotation index/availability, stage and hit progression. The values originate only from the snapshots already fetched by the existing witness; do not call additional native getters, modify `KillAura`, fake a target, move the server, wait longer, alter timestamps, or weaken the strict UUID/LOS/FOV/PvP/Wall Check gates.

## Evidence and acceptance
Both V257 and V258 immutable original jobs were independently rerun before adding this diagnostic on the child. Preserve their results regardless of V259. Require fresh exact-head Windows/Ubuntu Foundation plus full original Mojang 1.8.9 X11 and dedicated-server 78/78, then another unchanged SHA full 78/78. Tests remain authoritative: the new message does **not** fix the crash and cannot be called gameplay PASS. All Draft, main unchanged.
