# V113 — original local-player and melee hotbar lease ownership

## One cumulative review over fully certified V110

This branch stacks the real source-host ownership fixes **V111 #577** and
**V112 #578** on the fully certified **V110 #576** base. Exact GitHub
comparison showed **four linear commits, zero behind**, touching three
production/test files from V110 through V112, before this document.

Do not separately merge V111, V112 and V113 into the same target. Keep a
single reviewed integration path and preserve V110 as the rollback boundary.
All work is Draft and `main` remains unchanged.

## Production invariants

| Source boundary | Correctness requirement |
| --- | --- |
| Original player hotbar callback | Record the genuine local player instance and its original `InventoryPlayer` inventory source |
| Held-item callback | Source `customMcHeldItem()` must belong to the **same original player instance** before Rod/Pot item use admission |
| Rod/Pot selection | Store a distinct capture of the exact inventory which acquired the real temporary currentItem lease |
| Rod/Pot final use | Require matching current player identity, native inventory identity, original pre-action slot token, selected slot, mapped item type and positive stack count, plus live module and GUI focus |
| Rod/Pot post-respawn cleanup | Do **not** apply a pre-respawn synthetic right-click or cooldown; only restore the exact old inventory's still-owned slot through its original synchronous finally |
| AutoWeapon vs CombatSlot selection | Capture which module genuinely acquired the source inventory lease; nested attempts cannot overwrite that module owner or inventory |
| Reentrant melee callback | A second selection returns -1 without touching the original lease; its `restore(-1)` never consumes the outer owner |
| Melee restore after local-player replacement | Restore via captured original `InventoryPlayer` control, not current per-tick `tickHotbarControl`, even if module activation state changed during the action |
| User-controlled new hotbar | Underlying modules may restore only when the native selected slot remains their captured temporary target; a genuine user switch wins |
| No ModuleController/host owner | Fail closed; no unknown item type, fake player or synthetic server authority inference |

The distinction is crucial: the source host's current-tick inventory is an
**observation**, while a temporary combat-slot selection is an **in-flight
lease**. Callback order, respawn, or nested source actions may change the
former without transferring the latter.

## Verification matrix and limits

V111 host JUnit deliberately synthesizes two different player-control
objects with independent inventories, invokes their original host callbacks
in reverse order and during in-flight source Rod/Pot leases, and checks
which inventory receives the `finally` restoration. It proves no use-item
admission for mixed owners and no mutations of the new player's hotbar.

V112 host JUnit covers both fixed CombatSlot and source ItemSword-ranked
AutoWeapon. The first action acquires a real module-owned temporary slot;
the test invokes a second local-player callback and nested selection
while the first action is still active, checks the nested -1/restore(-1)
cannot steal the lease, and requires the outer captured original
inventory to restore. It also disables AutoWeapon before the outer
`finally`, requiring the captured module choice rather than current
module state to control restoration.

These JUnit controls are source-interface doubles; they do **not** claim
a genuine Mojang game actually respawned and used an original Rod/Pot
during the test. The real original-client evidence in preceding V108
is a different scenario: physical X11 Right Shift menu focus and
genuine `InventoryPlayer.currentItem` CombatSlot selection/rollback.

### Observed certified ancestor

- V107 #573: complete original pinned Minecraft 1.8.9 binary workflow and
  Foundation Windows+Ubuntu **PASS**.
- V108 #574: Foundation Windows+Ubuntu **PASS**, full original Mojang
  binary **PASS**, including the original native CombatSlot X11 test.
- V109 #575: Foundation Windows+Ubuntu **PASS**, full original Mojang
  binary **PASS**.
- V110 #576: Foundation Windows+Ubuntu **PASS** and complete official
  Mojang 1.8.9 original-client workflow **PASS**.

These are exact-head statuses at review preparation, not a statement
that V111/V112/V113 entire original suites completed.

## Mandatory head-specific review gate

1. V113 exact-head Foundation passes on Windows and Ubuntu, including
   all new host-ownership regressions and unchanged existing tests.
2. V113 exact-head complete pinned Mojang 1.8.9 workflow passes. It
   must retain the **four real physical X11** Step, NoClip, Reach and
   CombatSlot acceptance steps, followed by strict historical
   UUID/target lock, FOV, Aura, PvP, Scaffold and vanilla world checks.
3. Any full-suite failure must be investigated by its original job
   and log; same-head reruns may diagnose nondeterminism but do not
   justify weakening negative controls.
4. Do not certify true native server-side potion heal, rod cast,
   sword hit or respawn authority from mock-host tests alone.
5. Keep the PR Draft and `main` unchanged until separately reviewed
   and approved for merge.

This document is a fixed design/verification record, not a claim that
V113's own newly launched CI has passed.
