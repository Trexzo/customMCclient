# Combat V167 — original AutoRod and AutoPot one-use source item leases survive only original module lifetime

## Exact cumulative base

V167 is a cumulative Draft child of [V166 PR #632](https://github.com/Trexzo/customMCclient/pull/632), exact immutable source parent `6d75178ccb4e070da82f0278b2b7f0430de07b2e`. The V166 Windows/Ubuntu Foundation has **PASS**, and its full pinned Mojang 1.8.9 GUI/dedicated original run started separately; parent game outcome cannot be substituted for V167. A prior immutable V162 `66db7002abadb07207fbdfbf07171311bf494828` has **two complete original 78/78 PASS on exactly the same code**. This does not certify newer V164–V167.

## Actual production source bug and patch

The *real production* `Minecraft189AutoRodModule` and `Minecraft189AutoPotModule` already lease the selected original 1.8.9 `InventoryPlayer.currentItem` index to a single synchronous right-click, verify original ItemStack identity before original item use, and capture all persisted GUI settings including rod durability and active hook protection, or potion health threshold/effect/use ranking. The original use right-click is admitted through `ownsSelectedSlot`/ `ownsVerifiedItem`; native source right-click and original server effect are not fabricated.

**Remaining gap:** After a module is disabled and immediately reenabled with unchanged settings, an old temporary original item slot could still pass the old lease verifier because it checked only `enabled`, same `currentItem` and matching settings. Further, original `customMcSelectedHotbarSlot` is a mapped source accessor that can synchronously reenter GUI/module state changes. Previously the verifier evaluated GUI policy **before** one such getter; a native setter/callback that changed a persisted policy between selection and return could publish a stale `selectSlot` permission.

V167 implements identical fail-closed policy in **both production modules**, not just tests:

- Each `onEnable/onDisable` increments a monotonic module lifetime epoch; even an OFF→ON ending enabled with all prior GUI values is a *different source-authority lifetime*.
- On native hotbar selection, persist the exact epoch with existing selected original source inventory, slot index, old source index and current rod/potion GUI policy. After the original hotbar setter returns, `selectSlot` now requires **the full source lease** (not merely `enabled` and matching currentItem) to still match. If original setter reenters a setting or module lifecycle change, the selection is denied and the existing owned-slot rollback applies. No new synthetic cast.
- `ownsSelectedSlot` retains all previous per-module captured GUI options **plus exact captured module epoch**. It reads genuine original source `currentItem` twice and revalidates the original GUI policy, inventory identity and epoch after both getter calls. Neither another player inventory, a changed human source slot, nor a reactivated module may spend that old original use permission.
- The original verified `ItemStack` object identity and real type/count/durability/healing effect checks remain mandatory. Existing `restoreSlot` finally can restore only its own original hotbar index even after policy/epoch changes, and never overwrites a new user-selected currentItem.
- Existing cooling, default settings, module IDs and actual hooked Minecraft right-click remain unchanged. This is a gameplay ownership correction, not a test harness exception.

## Regression and limits

**Six new adversarial JUnit cases**, three in `Minecraft189AutoRodModuleTest` and three in `Minecraft189AutoPotModuleTest`, cover: old selected source item after disable→enable with identical currentItem/GUI policy denied; fresh new original selection permitted; native hotbar setter reentrantly edits Require Hold/Require Combat Target or toggles module lifecycle and cannot publish a stale use-item action; native currentItem getter reentrantly edits settings or switches the user-selected index and cannot silently spend the older original cast nor override human input during rollback.

These are Java source mock tests and do **not** prove actual fishing hook cast, reel, potion throw, Regen effect or server PvP. The full pinned original Mojang 1.8.9 suite remains mandatory.

## Exact-head acceptance

Require complete V167 Foundation **Windows + Ubuntu**, plus entire pinned original Mojang 1.8.9 GUI and dedicated server, including native physical X11 rod/hook reel, original potion splash/heal proof in the V166 physically prepared chamber, M405/M419 native B UUID/controller and real B-positive/A-zero scoreboard, M425 original mouse ray/hurt, original Scaffold, FOV, Wall Check and GUI negatives. Repeat same SHA after first complete PASS for unstable original fixture paths. PR remains Draft stacked child of V166, no merges, `main` untouched; #503 and #537 remain open.
