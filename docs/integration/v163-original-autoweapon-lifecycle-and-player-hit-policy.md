# V163 — original AutoWeapon player-hit GUI policy and module lifetime

## Cumulative source baseline and real risk

V163 is a new cumulative Draft child of [V162 PR #628](https://github.com/Trexzo/customMCclient/pull/628), immutable parent `66db7002abadb07207fbdfbf07171311bf494828`. V162 Foundation/native CI was initiated and results are separate; earlier V161 Foundation is **SUCCESS Windows+Ubuntu**. The latest reliably reproduced unchanged SHA V156 has **two independent full genuine pinned original Mojang 1.8.9 GUI/server runs 78/78 PASS**. V157–V160 encountered intermittent true original FOV/rejoin/Scaffold source-state failures; test-only revisions must not obscure those.

The existing *production* `Minecraft189AutoWeaponModule` already discovers real `ItemSword` instances in the original nine-slot `InventoryPlayer.mainInventory`, ranks finite original native sword base damage and optional real durability, and binds the selected original Item identity and hotbar `currentItem`. A reentrant hotbar setter or live GUI setting edit must not let an old choice dispatch a newly unauthorized melee click. The module previously captured and rechecked Ignore Fragile, Minimum Remaining and Prefer Durable Ties, but **did not capture/recheck the originally required verified-player-hit GUI policy or enable/disable lifecycle**. A `Require Player Hit` edit or a complete OFF→ON cycle while an old temporary sword remained selected could therefore still authorize a synthetic action at the real original `HostRuntime.mayDispatchSyntheticMelee` boundary (which already delegates to `AutoWeapon.ownsVerifiedSwordAtDispatch`).

## Actual Java production change

- Every `onEnable/onDisable` advances an internal module lifecycle epoch, even if the module returns to enabled with the same settings.
- `select` captures `requirePlayerHit` and exact epoch together with the already captured original sword class/damage/durability policy. Source inventory getters and setters may reenter GUI changes; current policy and epoch are rechecked before and after reading native original `currentItem` and before publishing a successful temporary selected-ItemSword lease.
- `ownsVerifiedSwordAtDispatch`, which the host calls at the **actual original attackEntity/clickMouse gate**, compares captured Require Hit and epoch along with original item singleton identity, finite base damage, optional durability, original `InventoryPlayer.currentItem` and unchanged original stack membership. This check is repeated after native item getters because they can trigger reentrancy.
- A stale lease is denied and the existing synchronous finally remains owner of restoring the genuine selected slot **only if unchanged by the user**. This does not manufacture a sword, player hit, original mouse event, outgoing attack packet or server damage and does not change default settings, item-ranking rules or default-enabled state.
- The V160 fixed CombatSlot source lease remains independent; AutoWeapon continues to win original host module dispatch when active, and the unmodified manual vanilla attack path does not use a synthetic authorization.

## New adversarial original-interface JUnit regressions

Four new cases in `Minecraft189AutoWeaponModuleTest` cover:
1. source typed sword selected, then GUI Require Hit changed mid-lease: reject old action, restore owned currentItem, and permit a new independent click under the actual current persisted policy;
2. module Disable→Enable same sword/same hotbar/same settings cannot inherit previous native controller permission, while a new original item selection can;
3. native source setter executes synchronous Require Hit edit and module OFF→ON: reject/rollback selection before old lease publication, without zombie selected hotbar;
4. an original ItemStack getter edits Require Hit while dispatch is being verified: the original native attack is denied despite the previous correct sword type/currentItem.

These prove source Java lifecycle and policy logic only. They are not direct Minecraft survival PvP hit witnesses.

## Exact-head acceptance

Require V163 Foundation **Windows+Ubuntu** and complete pinned original Mojang **1.8.9 GUI+dedi server** full-suite acceptance on this exact SHA (including strict M405/M419 real B UUID controller and server positive B/zero A, M425 physical ray/hurt, original negative FOV/Scaffold/rod/potion and actual GUI). CI failures must remain recorded, and intermittent original runs repeated on identical SHA rather than claiming parent passes. All descendants Draft, `main` untouched.
