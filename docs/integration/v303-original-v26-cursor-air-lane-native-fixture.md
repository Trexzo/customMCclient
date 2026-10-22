# V303 — original V26: acknowledge an unobstructed EAST-face cursor ray corridor

Direct immutable parent: V302 Draft PR #773 exact commit `77f6556bacb80b7c8665e3eef7f33a62ce7acd59`.

## New true independent Mojang client failure and exact evidence

V301 immutable SHA `1fff834e7a63761d5d7901bd0178072ded6d9272` first authentic GUI/X11+vanilla-server [original run #39756854654](https://github.com/Trexzo/customMCclient/actions/runs/39756854654), original job `119750919863`, completed **78/78 PASS**. Its independent SECOND same-SHA original job `119767360723` **COMPLETED FAILURE at V26** after 33 prior genuinely successful stages: `no original runTick cursor/eye/reach/World/item agree on side support checkpoint before bounded deadline`. Therefore **V301 is NOT double-native certified**, whatever its first result.

Actual mapped original game V26 callback diagnostic repeatedly returned:
`native_cursor_face_or_plane_mismatch candidate=EAST support=0,99,0 pos=1.05,100.0,0.5 eye=101.62000000476837 reach=4.5 ground=true stack=8 cells=1,1,2 hit=[1.0,1.0,101.0,0.0,0.0,1.0348987728282213,101.0,0.5]`.

The native original Minecraft cursor saw a real block hit at **(1,101,0)** / recorded hit geometry around y101, not the required EAST face plane on genuine support `BlockPos(0,99,0)`. The actual original player was correctly grounded x1.05 y100 z.5; real selected ItemBlock stack count was 8, World adjacent target and below were AIR, support West was FULL_SOLID. The original face/plane gate **correctly rejected** this mismatch. No claim that the original planner or controller is faulty or that a simulated cursor proves acceptance is justified. Why source World contained/hit the obstructing above-edge block was not separately established; existing V26 original fixture had only unacknowledged `/setblock` for the two y99 cells and never cleared original y100–102 corridor.

## V303 scoped original fixture correction, no original ray relaxation

Change **only** original V26 `if native_scaffold_candidate` branch and add its focused source prep helper in `scripts/RunMojang189SingleplayerSmoke.py`:

1. Source-acknowledge real vanilla original high TP `/tp CIProbe 0 120 0 0 0` before changing the loaded original World. Use existing strict exact fresh `[CHAT] Teleported CIProbe to 0.5, 120.0, 0.5` reply.
2. Clear the genuine above-edge native cursor lane x0..1, y100..102 with real `/fill 0 100 0 1 102 0 air`. Require a **fresh** exact original vanilla air-preclear source response, legitimate positive changed count or verified already-air `No blocks filled`. This specifically includes the coordinate **(1,101,0)** where the failing actual original ray collided.
3. Contrast source support `(0,99,0)` with fresh original vanilla `/fill ... air` preclear, then require the next original `/fill 0 99 0 0 99 0 stone` to yield a **genuinely positive changed-block count**. As elsewhere, no-op can NEVER satisfy mandatory positive support; this replaces two old unchecked `/setblock` assumptions.
4. Require actual source-acknowledged AIR at adjacent original `(1,99,0)`. Preserve the same original player target point, yaw=90, pitch=88.6 in `/tp CIProbe 1.05 100 0.5 90 88.6` with its own **fresh real 1.8.9 server TP reply**, not a synthetic camera/source cursor modification.
5. **After** that genuine low TP, independently confirm the real source-server target remains `stone` at (0,99,0), `air` at (1,99,0) and all 3 AIR cells (1,100,0), (1,101,0), (1,102,0), with five separate fresh per-block vanilla `/testforblock` commands and exact no-space coordinate replies from original 1.8.9. Source-chat receipt never substitutes for client World native cell or cursor evidence.
6. Source-file log cursor is captured after the new original TP receipt; the existing V26 **same three genuine per-tick player x/y/z positions and 12-second ground stabilization** now use only fresh **post-TP native lines**, preserving unchanged `original_edge_grounding_stable`. The same existing original no-early-positive-before-grant check, actual original `/give CIProbe stone 8` command, source-selected true ItemBlock and hard-positive native runTick `CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_NATIVE_CANDIDATE_PASS=YES` marker continue to decide real V26 PASS, unchanged and without retry.
7. All original production `Minecraft189ScaffoldNativeFaceGate.matches`, `Minecraft189ScaffoldRayCandidateGate.verify`, World lease, real native cursor and its expected original face/support plane, original 78-step CI deadlines/negative gating remain **unaltered**. No extra native World getter, altered original ray/camera geometry or actual placement, no spoofed result, no fake PASS.

**13 Python source-only/ordering regressions** cover exact actual high/low TP commands, original correct x1 y101 air lane, source support air contrast followed by positive stone, exact low TP before five source material checks, fail closed on source TP/air reset rejection, support-positive/target checks, unchanged native selected eight + positive marker and three source-stable player ticks, fresh byte cursors and no synthetic World, ray or item writes. They do **not** prove a real 1.8.9 native client ray or solve historic failure until authentic CI runs do.

## CI and historical gates

V299 original SHA `340cf78e9bf7a836b5edd39315569f0b86d07b40` achieved Foundation Windows+Ubuntu 10/10 and **TWO independent full original pinned 78/78 PASS**, newly double-native certified; authentic jobs `119740618611`, `119757841661` [original #39753802030](https://github.com/Trexzo/customMCclient/actions/runs/39753802030). V302 immutable parent SHA `77f6556bacb80b7c8665e3eef7f33a62ce7acd59` Foundation both OS 10/10 and FIRST original 78/78 PASS job `119759604689` [#39759426876](https://github.com/Trexzo/customMCclient/actions/runs/39759426876); independent second job requested, **outcome pending**, so V302 not double-native certified yet. V289/V290/V294 also double-native certified on their own exact heads.

The genuine V301 second V26, V298 second V19 (3,048 native wrong-face hits), V297 second V16, V296 second V19, V295 V23, V293/V292 second V29 and older V291/V288/V286/V283/V282/V276/V277 original failed jobs remain historically **FAILED**. Future passing commits never relabel true reds.

V303 requires its **own** immutable exact SHA Foundation **Windows 10/10 and Ubuntu 10/10 plus TWO distinct full original Mojang Minecraft 1.8.9 GUI/X11 + vanilla dedicated-server **78/78 PASS** jobs** before double-native certification. All PRs Draft, `main` untouched, no merge or release.
