# Combat V169 — immutable rod/potion native-use completion policy

## Actual source gap above V168

This cumulative Draft is based on [V168 PR #634](https://github.com/Trexzo/customMCclient/pull/634), exact immutable parent SHA `ff3001c1956bc1309b109916f103ce9003b6c56e`. V168 had Foundation Windows+Ubuntu SUCCESS; its full pinned original Mojang Minecraft 1.8.9 GUI/server job was still in progress at this change. An earlier exact V162 source SHA `66db7002abadb07207fbdfbf07171311bf494828` passed the entire original suite 78/78 **twice**, not equivalent to verifying V169.

The genuine mapped original `minecraft.customMcRightClickMouse()` uses a prevalidated actual original hotbar and ItemStack lease, then a post-click cooldown completion callback in the real production `Minecraft189RuntimeBridge` (rod) or `Minecraft189AutoPotSourceCast` (potion). V168 prevented a module OFF→ON from adopting its old click cooldown and added one-shot timer credit. Inspection identified a separate **same-lifetime GUI change after actual right-click**: `commitCompletedOriginalUse(inventory,oldSlot)` still checked only enabled/source inventory/lifecycle, then read the *current* user-selected `cooldown.get()`. If the genuine original native callback edits the module's cooldown or other persisted GUI setting before it returns, the old item use can debit the *new* GUI policy. A later caller could also retry the still-unused cooldown token after the GUI is reverted if the original completion returned false.

## Production changes in both modules

`Minecraft189AutoRodModule` and `Minecraft189AutoPotModule` now require the **entire captured original-use GUI policy** to match on completion, not just enabled module epoch, actual source inventory and selected prior slot. This reuses the already implemented **non-ItemStack** `capturedNativeUsePolicyMatches` predicate from original pre-click admission: fixed slot, auto-detection/ranking, original typed rod durability/hook options or potion health threshold/effect/conservation/aim settings, Require Hold/Require Target, cooldown setting, and original enabled module epoch. The one-use completion token is consumed **before** revalidating policy; an invalid GUI setting change fails closed and cannot be recredited if reverted. On success, cooldown counters receive the **captured pre-use cooldown duration** rather than a fresh post-click setting read.

Crucially, *no* original hotbar `currentItem` or ItemStack getter is called at post-click completion: the real vanilla potion may have been consumed/replaced by the accepted actual right-click; original slot may be changed by the user. The prior source item had to satisfy genuine original ItemStack type/instance, GUI focus and real health/target/World checks **before** issuing the original click. Restoring the temporary hotbar remains conditional on still owning actual selected index; manual changes win. No fake mouse action, cast/reel/splash, network packet or server success marker is created. Both modules' existing default policy/cooldown values remain unchanged.

## Six adversarial JUnit regressions

Three new tests in each original Java module test verify:
1. GUI Require Hold / Require Combat Target policy edited during a real original-click completion rejects the old cooldown; reverting settings does *not* make a second post-use commit possible, while a fresh independently selected native source lease works.
2. GUI Cooldown Ticks edited during original callback does not retroactively debit the new duration; an old completion is not replayable after reverting, and an independent next use still commits once.
3. Actual original post-use `InventoryPlayer.currentItem` changed or consumed is *not reread* by cooldown bookkeeping; the genuine original use retains credit under unchanged captured policy, and original hotbar finally does not override a manual user change.

These source mocked tests are **not** physical Minecraft fishing-hook or splash evidence. They test source-ownership and cooldown attribution only.

## Mandatory exact-head release gates

Exact V169 Foundation Windows+Ubuntu build and all source regressions, plus full pinned Mojang Minecraft 1.8.9 genuine X11 GUI and dedicated-server original **78/78** including real rod cast/reel, physical splash/Regeneration, M405/M419 exact native B controller UUID and positive B/zero A server damage, M425 manual X11 ray/hurt, World Scaffold, Wall Check and FOV negatives. A second *same-SHA* original rerun is necessary for stability certification because previously documented transient original-world failures exist. Full-parent CI success, if any, never certifies this child. Keep all PRs Draft, preserve source provenance, do not merge or alter `main`. Issues #503 native reliability and #537 module parity stay open.
