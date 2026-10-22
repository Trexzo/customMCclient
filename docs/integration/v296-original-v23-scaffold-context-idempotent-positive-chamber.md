# V296 — original V23 chamber always establishes real positive block changes

Immutable direct Draft parent: V295 #766 exact SHA `5e70c22455754d7d92286dec5e6d34560995e5c7`.

## Actual independent original-game failure

V295 [genuine original pinned Mojang Minecraft 1.8.9 GUI/X11 and vanilla server run #39678292068](https://github.com/Trexzo/customMCclient/actions/runs/39678292068), exact job `119493801936`, **COMPLETED FAILURE** at the original V23 source-native Scaffold context stage after 32 preceding successful steps. Exact last error:

`CUSTOMMC_189_SINGLEPLAYER_WORLD_FAILED: no fresh original vanilla /fill success for dry chamber before bounded deadline`

Native server/client evidence: actual source `/tp CIProbe 2 120 2 0 0` was confirmed with `[CHAT] Teleported CIProbe to 2.5, 120.0, 2.5`, actual local source player at x2.5 and falling/stabilizing above chamber; the *next* original `/fill 0 99 0 4 104 4 stone` returned genuine `[CHAT] No blocks filled`. This is **not** a successful changed-block receipt and the existing strict `send_verified_fill` correctly rejected it. The unchanged test requires a positive stone fill followed by a positive air hollow and three actual stable native player ticks. It never reached the new V295 eight-stone selected-hotbar V29 fix, so V295's proposed V29 solution remains untested on this failed original run.

The confirmed failure demonstrates a concrete **initial terrain-state fixture error**: vanilla may already have stone in the requested region, so a blindly repeated stone `/fill` cannot guarantee a positive block-change receipt. No altered Minecraft Java runtime, native World/player getters, controller or Scaffold gameplay failure is established. Genuine original V295 run remains historically **FAILED**, not waived.

## Bounded in-world correction

Modify **only** `prepare_native_scaffold_context_chamber` in `scripts/RunMojang189SingleplayerSmoke.py` by adding one *sacrificial* original vanilla `/fill 0 99 0 4 104 4 air` between the existing source-acknowledged initial high teleport and existing two mandatory positive fill commands.

- New helper `send_verified_original_scaffold_context_preclear` issues an actual Minecraft vanilla chat command through existing X11 UI and records the current original client log byte offset **before sending**, requiring a **fresh** source reply. It reuses the already regression-tested `original_dry_chamber_preclear_reply` classification: a genuine positive `N blocks filled` is accepted, or `No blocks filled` means the existing region is already air and may be accepted **ONLY in this sacrificial preclear**. Outside-world, oversized area, stale/no reply or failed source commands still hard fail within the existing **8-second** source acknowledgment bound; no retries or extended acceptance deadline.
- Original source player is still teleported to (2.5,120,2.5) and must receive its existing exact server acknowledgment **before** changing World cells. A genuine acknowledged air reset means the following original `/fill 0 99 0 4 104 4 stone` **MUST** change blocks and return a fresh *positive* vanilla numeric `N blocks filled` receipt. The following actual `/fill 0 100 0 4 104 4 air` MUST also change blocks and return a fresh positive numeric reply; no-op is still a failure at either mandatory step.
- Keep original final source `/tp CIProbe 2 100 2 0 0` and exact chat witness. Original runTick source still must see three actual grounded player samples and genuinely selected vanilla ItemBlock before the existing V23 eye/reach/context original markers are emitted. The new preclear return is **not** a V23 pass signal, client World readback, genuine ray hit or physical Scaffold click.
- **No changes** to production Java, FOV/range/target selection, native controller, World bytes/ItemStack through a software bypass, original X11 test controls except that single genuine air reset, original 78-step order, native 20/80/A/B gating, or deadline extensions.

Update prior V23 ground chamber regression's actual command-order mock (now high teleport → sacrificial preclear → mandatory positive stone → mandatory positive hollow → final teleport). Eight new Python source/receipt regressions cover verified air-reset positive/no-op and fail closed on world errors, stale byte offset cannot satisfy fresh receipt, exact command ordering, mandatory two real positive fills, unchanged ground-source gate, and no fake original PASS.

**Source/offline regressions are not original-game evidence.** Root-cause for earlier V293 and V292 V29 failures is separately established as genuine V293 source selected stack `1` vs required `8` under CLEAR GUI and valid original ray candidate; V295's genuine inventory clear/hotbar selection is not yet verified by a successful actual V29 run. V294's first complete original native 78/78 PASS and Foundation both OS 10/10 are true positives; independent second same-SHA run was requested but needs an actual success result for double certification.

## Strict certification and preserved failures

Preserve original V295 V23, V293 second V29, V292 second V29, V291 V19, V288 initial A, V286 nonplayer, V283 second V29, V282 FOV and V276/V277 UUID/phase as genuine historical failed jobs under issue #503, regardless of later green outcomes. V289/V290 had Foundation Windows/Ubuntu 10/10 and two independent original Mojang GUI+server 78/78 PASS on their separate exact SHAs.

V296 requires exact-head Foundation **Windows 10/10 and Ubuntu 10/10**, plus **TWO independent complete original Mojang Minecraft 1.8.9 GUI/X11 and vanilla dedicated-server 78/78 PASS jobs on the exact identical SHA**. All PRs Draft, `main` unchanged, no merge or release.
