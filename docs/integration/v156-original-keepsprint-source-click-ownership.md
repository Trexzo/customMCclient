# Combat V156 — KeepSprint original source-local click lease

## Evidence and exact parent

This is a cumulative Draft child of [V155 PR #621](https://github.com/Trexzo/customMCclient/pull/621), commit `512e7ff9618e198979f8e165228e0ceeeb0f2b68`. The older unchanged V151 parent has full official original Minecraft 1.8.9 native acceptance green **twice** (78/78 steps each), but this does not certify any V152–V156 new SHA. Require exact-head native acceptance and Windows/Ubuntu Foundation for V156.

## Native source ownership gap and actual production fix

Previously `Minecraft189HostRuntime.shouldKeepSprintAfterSyntheticClick(playerHit)` consulted the mapped local player's genuine sprint, movement, W key and player hit, source GUI focus and WTap owner to decide a **pre-click** KeepSprint admission. However `restoreSprintAfterSyntheticClick()`, called synchronously after the original vanilla click, then only checked that *some* mapped `tickSprintControl` existed, KeepSprint was still enabled, WTap inactive and GUI closed, and unconditionally called `customMcSetSprinting(true)` on that potentially different player. It did not require that the post-click restoration was actually authorized by the same source player's earlier pre-click validation; a rejected click or a callback changing player identity/settings after that validation could still write sprint.

V156 directly hardens **production** `Minecraft189HostRuntime`:

- A qualifying `shouldKeepSprintAfterSyntheticClick` mints a **one-use lease** for the exact original `Minecraft189PlayerSprintControl` object and snapshots the persisted Require Forward, Require Player Hit and Pause While Sneaking policy, as well as this hit's authoritative player-hit boolean. No lease is minted for rejected clicks.
- `restoreSprintAfterSyntheticClick` consumes that one-use lease **before** invoking the real source setter, so a partial native write, exception or reentrant callback cannot replay the same grant.
- Restoration requires the exact original player object still bound as the source current-tick sprint control, unchanged explicit policy, current source-mapped native movement/sneak, live W key, WTap arbitration and actual GUI focus. It rechecks module's original `shouldRestore` predicate with the recorded valid **pre-hit sprint** authorization; the post-click vanilla attack is allowed to have changed its sprint bit. An unregistered/disabled KeepSprint module fails closed.
- Original player replacement and per-world-source position/tick clear and shutdown cancel any pending grant; no source-player object, sprint bit, packet, damage/UUID, or hit evidence is fabricated.

The original feature ID, setting keys, persisted values, default behavior for a **legitimate** same-player hit, module settings and manually controlled vanilla sprint remain unchanged.

## Integration/regression tests

New `Minecraft189KeepSprintRuntimeLeaseTest` exercises full installed host/catalog/controller and mocked source interfaces for:

1. No positive pre-click authorization, default non-player hit veto and exactly one native setter after a legitimate player hit, with duplicate restore blocked.
2. Original player instance replaced between admitted click and restoration; neither the old nor the new source player is mutated, while an independent positive next click works.
3. Live GUI setting changed between authorization and use, plus module disable; all revoke the old source click.
4. Genuine W physical key and source sneak changes after click are re-evaluated before restore.
5. Reentrant and throwing native sprint setter; the old grant is consumed even when the original setter mutates first and fails, while a distinct later click works.

These are **unit/source ownership tests**, not proof of actual Minecraft multiplayer sprint bit rendering, attack packet acceptability or genuine server knockback. Do not use them to claim original-game certification.

## Required gates

Exact-head Foundation on Ubuntu+Windows and complete pinned Mojang 1.8.9 GUI/dedicated server original acceptance must pass without relaxing M405 real B UUID controller/damage or M425 original X11 raycast and hurt, native Render/Scaffold/GUI and rod/potion behavior. A dedicated native KeepSprint physical sprint-before/after attack witness remains separate follow-up if needed. This PR stays Draft; `main` untouched, other ancestor branches immutable.
