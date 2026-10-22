# V298 — original transformed World.rayTraceBlocks return branch evidence

Direct immutable parent: V297 Draft PR #768, exact SHA `637958c3171265cb93292e84ad531c83ef056eee`.

## Confirmed genuine original runtime failure (not an offline JUnit)

V296 exact SHA `9b7ec9a9c06659e03bb8757aba755ebfb99a6270` [second independently run authentic pinned Minecraft 1.8.9 GUI/X11 + vanilla server #39732203428](https://github.com/Trexzo/customMCclient/actions/runs/39732203428), actual second job `119720123721`, **COMPLETED FAILURE after 46 genuine earlier PASS stages**, at genuine V19 `Prove original World raytrace exact support face and wrong-hit vetoes`. The first independent exact-SHA full run succeeded **78/78**, but this authentic second failure means V296 **NOT double-native certified**. Previous V291 native V19 also failed. Do not retroactively certify V296 on the strength of its first pass.

This second genuine original V19 job produced V293's observer evidence (source acceptance stats, not synthetic fixtures): actual independent original vanilla `/fill` and post-reseat `/testforblock 0 99 0 stone` succeeded; **3,044 real original client World `FULL_SOLID=2` cells** and **3,044 unchanged original `customMcScaffoldRayTraceSupport` source-native ray batches** reported **ZERO valid expected UP-face hits**, ZERO wrong-face or wrong-BlockPos return true. Missing World ray interface is not the dominant failing condition; non-solid counts and early interface absences occurred during setup, but thousands of later original **client full-solid** checks still yielded returned false. The exact branch inside transformed `World.rayTraceBlocks` that caused the false result was NOT known. Possibilities: `null` MovingObjectPosition, non-BLOCK MovingObjectPosition, absent EnumFacing, wrong face or wrong source BlockPos. Do not assume geometry or ray semantics without actual branch evidence.

## Next minimally scoped native return diagnosis

The actual transformed original 1.8.9 `World` implementation of `customMcScaffoldRayTraceSupport` does one original `World.rayTraceBlocks` call and then returns false if the hit is null, the MovingObjectPosition type is not BLOCK, the native side is null, native `EnumFacing.getIndex` is not the expected index, or original `BlockPos.equals(expected)` returned false, and returns true **only** if all source checks succeeded.

In V298, instrument those **same six original return paths** in `Minecraft189ClassTransformer.addScaffoldNativeRayTraceSupport` without invoking any extra Minecraft/native method:
- All four short-circuit original false branches pass their **exact already determined** boolean through a tiny static read-only return recorder: `MISS`, `WRONG_TYPE`, `MISSING_FACE`, `WRONG_FACE`.
- The existing final native `BlockPos.equals` boolean is evaluated **once** and read once; the exact false/true outcome is forwarded unchanged as `WRONG_BLOCK` or `EXACT_MATCH`. There is no second comparison or ray.
- The original generated method continues to expose the **same signature, endpoints, bytecode source field ownership, native ray arguments, control flow decisions and returned boolean**. Native World/mouse/controller/entity/BlockPos getters are not added or changed. The recorder cannot change the game result.
- `Minecraft189OriginalScaffoldNativeRayReturnTrace.recordResult` is opt-in for the existing original `custommc.acceptance.requireNativeScaffoldRayTrace` test property only. When disabled, it simply returns the original boolean and performs no tracking. When enabled it counts **only** the exact positive V19 original source expected `BlockPos(0,99,0)` with face index UP=1; the two **unchanged real negative rays** (wrong face=2, wrong x=1) never contaminate the positive ray reason histogram.
- Original `Minecraft189RuntimeBridge` logs the native-return reason counts alongside V293's already-existing source World full-solid, missing-interface and original correct/wrong native ray witness counters. All counts reset at the existing acceptance session reset. All original failure/positive markers and original fixed 78-step pipeline stay unchanged, including the **immediate exception** if wrong-face or wrong-BlockPos original ray returns true.
- Fourteen Java 8 JUnit tests exercise property-off original return passthrough, six exact native return reasons, negative ray isolation, exact histogram accounting, reset and independent read-only snapshot. These are **offline recorder tests** and do not prove the genuine original ray succeeded or failed for a specific reason; the full actual Mojang binary job must provide this.

No source World mutation, bytecode replacement of native ray method, alternate hit, extra ray, click, ItemStack change, packet, source GUI change, synthetic PASS, retry or timeout extension is allowed.

## Requirements and history

V294 immutable exact SHA `99f914c9e8a4310c2ff5841275efde4089f780e7` has **TWO independently completed full original 78/78 PASS** and both OS Foundation 10/10, double-native certified on that SHA. V289/V290 likewise double-certified on their exact heads. V296 first run green but second true V19 red, not double-certified. V297 Foundation Windows/Ubuntu 10/10 passed, genuine [first original #39748434784](https://github.com/Trexzo/customMCclient/actions/runs/39748434784) was still in progress at V298 creation, with no full 78/78 claim or second run.

Historical genuine original V276/V277 wrong UUID/phase, V282 FOV, V283 second V29, V286 nonplayer V65, V288 initial A, V291 V19, V292 second V29, V293 second V29 and V295 V23 and V296 second V19 remain authentic FAILED original jobs in issue #503. Later successes cannot rewrite them.

**V298 strict exact-head certification:** Foundation Windows+Ubuntu **10/10 each** and **TWO separate full original Mojang Minecraft 1.8.9 X11 GUI + vanilla dedicated-server 78/78 PASS jobs on the identical immutable commit SHA**. All PRs Draft, `main` unchanged; no merge or release.
