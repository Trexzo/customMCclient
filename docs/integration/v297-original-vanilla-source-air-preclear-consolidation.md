# V297 — one strict original-client vanilla air-preclear authority for four fixtures

Immutable direct parent: V296 Draft PR #767 exact SHA `9b7ec9a9c06659e03bb8757aba755ebfb99a6270`.

## Proven actual original state motivating this consolidation

V295 original [Mojang 1.8.9 run #39678292068](https://github.com/Trexzo/customMCclient/actions/runs/39678292068) **FAILED** at V23 when actual original integrated server returned `[CHAT] No blocks filled` to a first noncontrast stone `/fill`; no native Scaffold V29 test was reached in that real run. The proper fix V296 inserted a *sacrificial* actual vanilla AIR preclear (which can validly be a genuine no-op) before still requiring two **positive changed-block** replies. On exact V296 SHA `9b7ec9a9c06659e03bb8757aba755ebfb99a6270`, [first genuine original #39732203428](https://github.com/Trexzo/customMCclient/actions/runs/39732203428) **COMPLETED SUCCESS 78/78**, including real V19, V23, V29 and the entire unchanged X11 GUI and vanilla server progression; Windows+Ubuntu Foundation both 10/10. A separate unchanged-SHA native rerun has been started; **do not call V296 double-certified until that exact second original run completes full 78/78 PASS**.

V294 parent-line exact SHA `99f914c9e8a4310c2ff5841275efde4089f780e7` already achieved Foundation Windows+Ubuntu 10/10 and two independent full pinned original 78/78 on the same commit. Historical genuine V295 V23, V293 second V29 selected stack=1, V292 second V29, V291 V19 and older genuine V276/V277/V282/V283/V286/V288 reds remain immutable FAILED runs.

## Identified maintainability risk, not a new gameplay failure

The Python original runner had **four independently copied eight-second polling loops** for sacrificial `/fill ... air` preclears: separate dry/healing chamber, original V19 raytrace support, original V29 Scaffold Survival edge, and newly added original V23 context chamber. Each takes an original client log byte offset, sends actual X11 chat, requires the real Mojang command server response, uses `original_dry_chamber_preclear_reply(fresh)` to distinguish **positive changed block count** from genuine **`No blocks filled`**, and fails closed on no reply/source errors. Copy-pasted implementations risk divergence in stale log handling, missed failure responses or accidentally allowing a no-op to satisfy one of the mandatory *positive* later fills. This is a concrete duplication in the active source, not proof those older helpers had failed.

## Bounded V297 refactor

One `send_verified_original_air_preclear(process, log, window, command, stage, missing_reply)` is now the only source read/validation owner across all four **existing** original fixture paths. It:

1. Rejects non-air-fill command strings before sending any command.
2. Captures a **fresh** original client logfile size **before** sending exactly one actual vanilla X11 chat `/fill ... air`.
3. Uses the **same** eight-second monotonic bound, calls `require_running`, reads from that fresh byte offset, invokes the existing original 1.8.x `original_dry_chamber_preclear_reply` classifier unchanged, and returns only `source-blocks-cleared` or `source-already-air` when **fresh** actual source responses justify them.
4. Propagates the original per-scenario error message on lack of a reply, preserving all hard rejections on outside-world/excessive/invalid original source responses.

Each scenario retains **the identical command**, order and downstream `send_verified_fill` strictly-positive changed-block requirements. V19 still demands one positive original stone fill, post-reseat genuine server `/testforblock` and actual original native World.rayTraceBlocks UP face. V23 still demands two positive original World changed-block replies, final source teleport and three stable real local-player ticks. V29 still demands real positive support and air target fills, exact grounded source player, true selected eight ItemBlocks, real controller click accepted, WorldClient stone readback and stack debit, and negative disabled module test. Dry chamber still demands two positive genuine World changes and authentic potion effects. **No** additional native getters, mutation or synthetic World, alternate raycast, timeout enlargement, fake PASS, command retries, change to original 78 stage policy, or production Java changes.

Ten new Python source/receipt regressions check both genuine preclear responses, wrong/non-air commands refused **without sending**, stale prior log bytes fail closed, world rejection remains a hard failure, exactly one common source freshness/8-second verifier, all four original commands and their distinct strict missing reply messages, every later required positive World fill retained, and no fake game/controller PASS. The existing V296 V23 AST regression is updated to inspect the **single shared reader** instead of a duplicate V23-specific `handle.seek`; its original fresh-noop/positive/stale tests still run against the actual V23 helper. All original preexisting V19/V29/dry chamber source regression cases remain.

## Strict verification

This is an **original acceptance runner maintainability and evidence-contract consolidation**, not a new Scaffold gameplay fix, not a root cause claim for the earlier genuine native V19/V29 reds, and not a weakening of `No blocks filled` hard rejection in positive fills. Native verification requires this V297 **exact SHA** to pass Foundation Windows 10/10 and Ubuntu 10/10 **AND two separate complete authentic pinned Mojang 1.8.9 X11 GUI+vanilla server 78/78 PASS jobs**. Unit regressions alone do not certify original Minecraft. All PRs Draft, `main` untouched, no merge/release.
