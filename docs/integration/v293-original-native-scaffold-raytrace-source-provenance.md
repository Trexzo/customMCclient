# V293 — actual original V19 World-cell / native raytrace source provenance

Immutable parent V292 Draft PR #763 corrected SHA `23bbe2997069533c322f0da51342f7a44180ba11`.

## Real original failure; exact limits of current evidence

V291 genuine Mojang Minecraft 1.8.9 X11 client [run #39639804243](https://github.com/Trexzo/customMCclient/actions/runs/39639804243), exact SHA `b24b8ad5be5b564bac5daf3abbcdfaf60c676d5e`, job `119360062551`, **FAILED** at original V19 `Prove original World raytrace exact support face and wrong-hit vetoes`, after 46 prior complete genuine stages. It reached real World/player scene, a **fresh** original vanilla `/fill` stone reply and *post-teleport original server* `/testforblock 0 99 0 stone` readback. The actual source player was stable at x0.5 y100 z0.5 in original native position logs. Yet required `CUSTOMMC_OFFICIAL_189_SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES` was missing through unchanged real bound; process reported `no genuine World.rayTraceBlocks hits original stone UP face checkpoint before bounded deadline`.

This does not prove *why* a ray missed: original World callback could be absent; the transformed native read/ray interfaces could be unavailable; original **client World** cell could differ from the integrated-server cell after confirmed chat; original native ray method could return false even with client solid; or original event order might be different. Server-side `/testforblock` is source evidence of real server stone at that time, not a substitute for native client World-cell/ray evidence. Any original wrong-face or wrong-block ray accepted would remain a genuine immediate hard failure.

## Scoped diagnostic-only implementation

Under existing `custommc.acceptance.requireNativeScaffoldRayTrace` (true **only for the original test scenario**) instrument the **unchanged** original `Minecraft189RuntimeBridge.worldEntityPositions` callback:

- Count actual source callback invocations, how often original World-read vs World-ray transformed interfaces are absent. No added getter, no extra callback and no fake World.
- Invoke `customMcScaffoldCellCode(0,99,0)` **exactly once**, as the original source code already did; store the original numeric code and counts of expected `FULL_SOLID` vs other/unavailable code, independent of the server chat readback. The exact same full-solid comparison continues to decide whether the original native ray is even attempted.
- Only when original client source reports `FULL_SOLID`, call the exact three **preexisting** genuine `customMcScaffoldRayTraceSupport` operations (expected actual block UP face; intentionally wrong face; intentionally wrong BlockPos) exactly once each, with **identical original from/to endpoints**. Count the **returned native booleans**, including non-UP false and any unexpected negative true.
- Preserve the **unchanged hard exception** if wrong-face or wrong-block original native test returns true; preserve the **unchanged real positive marker** only if the real correct source ray returns true. Diagnostic counters never take part in original acceptance.
- Print a bounded first/every-100th original source callback outcome (and real positive completion) with interface readiness, original client cell code, total solid vs other cells, actual native expected-face ray batches/hits, wrong-face/block hits, and last rejected/accepted step. No packet/World/player modification, no alternate ray geometry, no retries, no extended timer and no new PASS.
- New `Minecraft189OriginalScaffoldRayTraceProvenance` is an independent synchronized recorder of already-available boolean/numeric results only. Installation reset clears counters. Twelve Java 8 JUnit regressions cover absent transformed interfaces, real-cell mismatch vs correct FULL_SOLID, native correct-ray miss/hit, forbidden wrong-face/block booleans, repeated outcomes, immutable source snapshots, reset, and explicit no fabricated official PASS.

These are **diagnostic unit tests**, not original Mojang binary evidence or an original Scaffold fix. Future genuine original V19 logs can differentiate absent callback/interface, source client cell not full solid, vs native ray return false with a real solid cell. No attribution of the historical V291 miss to a specific mechanism without that evidence.

## Strict release/evidence gate

Historical real V276/V277 UUID-phase, V282 rear-FOV, V283 second V29 Scaffold, V286 V65 nonplayer, V288 locked-A phase0 and V291 V19 raytrace failures remain TRUE FAILED original jobs; later passing commits do not rewrite original results.

V289 and V290 **fully double certified** on their own exact heads: Foundation Windows+Ubuntu 10/10, TWO separate complete authentic 78/78 original Mojang GUI+vanilla dedicated-server runs. V292 parent exact head Foundation Windows+Ubuntu 10/10, FIRST authentic native 78/78 PASS; separate second same SHA original already scheduled, not yet double certified.

V293 requires its own unchanged-SHA **Windows and Ubuntu Foundation 10/10 PLUS two independent full authentic pinned Minecraft 1.8.9 X11 client + vanilla dedicated-server 78/78** before certification. All PRs Draft; `main` unchanged; no merge or release.
