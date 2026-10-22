# V300 — original V16 real-source verified support and adjacent World transitions

Direct immutable parent: V299 Draft PR #770 exact corrected SHA `340cf78e9bf7a836b5edd39315569f0b86d07b40`.

## Actual independent native failure

V297 exact SHA `637958c3171265cb93292e84ad531c83ef056eee` [second genuine pinned original Minecraft 1.8.9 X11 GUI + vanilla dedicated-server job #39748434784](https://github.com/Trexzo/customMCclient/actions/runs/39748434784) job `119737689798` **COMPLETED FAILURE** at V16 `prove native original World air-stone-air server block transitions`, after **30** prior original stages passed. Exact original error: `CUSTOMMC_189_SINGLEPLAYER_WORLD_FAILED: vanilla player lacks sustained stone support at (0,99,0)`. The first independent same-SHA original had full **78/78 PASS**, but because this second run failed, V297 is **NOT double-native certified**.

Read-only source audit of V16 found it sends vanilla `/tp 0.5 120 0.5`, `/setblock 0 99 0 stone`, `/setblock 1 99 0 air`, `/tp 0.5 100 0.5`, later alternating `/setblock 1 99 0 stone` and `air` **without separately requiring actual original-server command replies**. Merely not seeing the unloaded-chunk failure string is not affirmative changed-block evidence. Vanilla original spawn/terrain may already contain stone or the state may remain unavailable when the source player descends; this creates a fixture provenance gap. The original player-World position loop correctly required **three** stable real player y100 samples and failed when that proof was absent. This genuine failure does not show Scaffold production World adapter behavior was wrong; exact root cause (missing setblock, latency, source World chunk, player's movement) remains unproven.

## Scoped V300 original runner repair

Only the `if native_scaffold_world` **genuine original V16 singleplayer test branch** changes. It uses the existing true vanilla X11 client chat and V297 shared strict readback (no source Java changes):

1. Replace unchecked initial high teleport with `/tp CIProbe 0 120 0 0 0` and an exact **fresh** original server `[CHAT] Teleported CIProbe to 0.5, 120.0, 0.5` acknowledgment. True source destination chunk (0,0) must be loaded.
2. Send actual `/fill 0 99 0 0 99 0 air` as a **sacrificial** source-verified air preclear (fresh positive count OR genuine already-air no-op), then require a separately new, **strict POSITIVE block-change** `/fill 0 99 0 0 99 0 stone` receipt. This forces genuine original stone support instead of trusting a fire-and-forget /setblock or a no-change response.
3. Separately true-source verify `/fill 1 99 0 1 99 0 air` as an adjacent sacrificial preclear, with fresh server reply and no claim that the native World client has seen air.
4. Require exact fresh `/tp CIProbe 0 100 0 0 0` actual source acknowledgment, then **independent fresh** `/testforblock 0 99 0 stone` original vanilla server reply before sampling player support. This is **server stone authority only**, never a native player-sustained or native World-cell PASS.
5. Keep the **original unchanged native position triple-stability gate** (three actual `native_safe_walk_positions` player samples, x/z0.5±0.3, y100±0.2 within existing 10-second stabilization deadline). If genuine original client cannot remain supported, test still FAILS. All original native World probes, unloaded-chunk veto and marker deadlines stay unchanged.
6. After independent real source `AIR` marker of adjacent cell, require **real strictly POSITIVE** source change `/fill 1 99 0 1 99 0 stone` before still-mandatory genuine client native SOLID marker, then **real strictly POSITIVE** `/fill 1 99 0 1 99 0 air` before still-mandatory client native reverted-AIR marker. These additional real command receipts never substitute for native callback success.

Nine Python source AST/fixture contract tests check actual player-specific teleports, support air contrast and strictly positive stone reply, source post-reseat stone authority, unchanged strict original three player-position samples, distinct unchanged native AIR/SOLID/reverted markers, all three mandatory changed-block counts, source command ordering, no direct World/ItemStack mutations, packet, controller or fake original marker.

All extra commands use **actual existing vanilla command chat**, independently fresh original source log cursor, existing eight-second command bounds; no additional native World getter, game Java modification, fake block, packet, lowered eligibility, native timer extension or skipped original acceptance. This is a bounded **test-fixture authority correction**, not a declared gameplay code fix.

## Certification

The genuine V297 second-run V16 red remains FAILED even if V300 later passes. The first V297 original run 78/78 PASS and Foundation both OS 10/10 do **not** constitute double-native certification. V296 genuine second V19, V295 V23, V293/V292 second V29, V291 V19, V288 initial UUID A, V286 nonplayer, V283 second V29, V282 rear FOV and V276/V277 UUID reds remain unwaived historical evidence at issue #503. V294, V290 and V289 have two separate completed full original 78/78 successes and Foundation both OS 10/10 on their own distinct exact SHAs.

V300 requires **BOTH Windows Foundation 10/10 and Ubuntu Foundation 10/10**, plus **TWO independently completed genuine pinned Minecraft 1.8.9 GUI/X11 + vanilla dedicated-server full 78/78 PASS jobs on one identical immutable commit SHA**, before native double-certification. All PRs Draft, `main` unchanged, no merge or release.
