# Combat V168 — one-shot native AutoRod and AutoPot post-rightClick cooldown owner

## Actual source gap

This is a cumulative Draft child of [V167 PR #633](https://github.com/Trexzo/customMCclient/pull/633), exact parent `37fe7bfb5cbfec9f78372c033c707fe684bdd7a6`. V167 has Windows+Ubuntu Foundation PASS but original Minecraft binary GUI/dedicated server CI was in progress as this code was written; no parent native pass automatically applies. Prior V162 immutable `66db7002abadb07207fbdfbf07171311bf494828` achieved **two full same-SHA 78/78 original passes**.

V167 already prevents the *pre-click* original ItemStack currentItem selection from carrying an older module lifetime: module onEnable/onDisable epoch, all original GUI persisted policy, genuine source inventory identity and real rod/potion ItemStack identity are required at source right-click admission. However inspection of the **production post-click path** found a separate hole:

- Actual mapped `minecraft.customMcRightClickMouse()` for AutoRod in `Minecraft189RuntimeBridge` is followed by `host.commitAutoRodUseAttempt()`.
- Actual `Minecraft189AutoPotSourceCast.execute` calls the same mapped original right-click and then `host.commitAutoPotUseAttempt()`.
- Those old host functions delegated to `module.commitUseAttempt()`, which checked merely **`enabled`** before resetting cooldown. A native reentrant callback during real right-click can disable then immediately reenable either module and return with enabled=true, causing the old right-click to spend scheduler cooldown in the **new** activation. The source hotbar item may have been consumed/swapped by the real vanilla click, so rechecking original ItemStack identity **after** use would be wrong; a one-use captured pre-use authority is required for completion bookkeeping.

## Production source fix

- Both `Minecraft189AutoRodModule` and `Minecraft189AutoPotModule` now track `cooldownCommittedForLease` reset only on admission of a new genuine hotbar lease or final restoration.
- New `commitCompletedOriginalUse(inventory,originalSlot)` succeeds **only once** if the exact original lease holder inventory, captured genuine prior slot and module epoch are still bound to the same active module after native right-click. It consumes the one-use cooldown token before updating timer fields. No source setter, item lookup, fake packet or synthetic mouse use is performed after the action.
- `Minecraft189HostRuntime.commitAutoRodUseAttempt(originalSlot)` / `commitAutoPotUseAttempt(originalSlot)` now require the precise source-captured runtime inventory/slot and matching current tick inventory, then call the new same-lifetime module bookkeeper; both no longer call the unconstrained module legacy timer method in production.
- `Minecraft189RuntimeBridge` and `Minecraft189AutoPotSourceCast` pass the original exact selected slot token after **actual** original right-click returns. If original right-click throws, no post-click commit occurs, as before.
- Existing `restoreSlot` finally owns actual original currentItem restoration only if still on the module selected slot. If user switched items during real use, their selection is not overwritten.
- Do **not** require the just-used original ItemStack to remain in the hotbar after real vanilla splash consumption: the *pre-click* host/item verifier already established a real 1.8.9 rod/potion and issued the original right-click. This change regulates only post-click timer attribution and does not give another item's identity.
- Module IDs, defaults, cooldown tick settings and positive legal use behavior unchanged. Existing `commitUseAttempt()` remains solely for direct scheduler legacy unit contracts; **production original-click callbacks** use the new lease method.

## Adversarial JUnit tests

Six new cases, three per module, in `Minecraft189AutoRodModuleTest` and `Minecraft189AutoPotModuleTest`:
1. A legitimate admitted original action can commit cooldown once, not twice; a finally-restored lease cannot be replayed; an entirely new source action may commit independently.
2. OFF→ON during the original rightClick boundary leaves original selected source inventory unchanged but revokes the old cooldown commit; a genuinely new module lifetime action can commit.
3. Missing pre-click selection, foreign InventoryPlayer and wrong previous slot fail; an actual vanilla post-use hotbar mutation does not lose an already permitted cooldown commit, and owner-only finally never overwrites new human currentItem.

JUnit mocks prove source lifetime and timer attribution only. These tests are **not physical rod hook, potion splash, actual player healing or dedicated-server hurt evidence**.

## Mandatory exact-head release

Require exact V168 Foundation Windows+Ubuntu and entire pinned original Mojang Minecraft 1.8.9 GUI/X11/dedicated server 78/78 including native physical rod/hook reel, splash/Regeneration from V166 verified physical chamber, original M405/M419 B UUID controller and independent positive B/zero A damage, M425 real X11 ray/hurt, FOV, Wall Check and Scaffold. Then rerun exact SHA for known intermittency. The original source-world V164 M419 failure where source diagnostic `reason=null` but no real server B damage remains tracked in #503, not silently fixed here. All cumulative PRs Draft, `main` untouched.
