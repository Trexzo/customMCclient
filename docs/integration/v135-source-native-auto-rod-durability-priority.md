# V135 — opt-in source-native fishing rod durability priority

## Repo and source ancestry

Draft child of V134 #600, parent exactly `913862a6de1adbc7a4b2874f8d733bbb9b7ee17a`, in `Trexzo/customMCclient`. V132 #598 Foundation Windows+Ubuntu and full pinned original Mojang 1.8.9 binary/dedicated workflow both SUCCESS at start. V133/V134 full exact-head suites were still processing. Preserve cumulative linear chain; never merge overlapping draft children independently. Main unchanged.

## Practical AutoRod behavior

V131 auto-discovery normally keeps an already-held valid fishing rod or picks the first valid lower numbered slot. This can repeatedly use a rod with nearly exhausted source ItemStack durability despite a fresh rod in a later slot.

New persistent Combat ClickGUI setting `combat.autoRod.preferDurableRod` / **Prefer Durable Rod**, boolean **false** by default. It only changes the `autoDetectSlot=true` path. With both settings enabled the hotbar scan selects the *largest remaining native rod durability*, using original `ItemStack.getMaxDamage() - ItemStack.getItemDamage()`. An equal remaining value favors the already-held source rod; otherwise the lowest slot. Candidates require genuine original ItemFishingRod class, positive stack count, positive maxDamage and damage between 0 and maxDamage - 1. Unknown/depleted rods are not ranked. Original fixed configured slot behavior is unaffected. Rod target gate, cooldown, player filter and outgoing right-click code remain unchanged.

## Original object ownership and reentrancy

The ranked scan binds to genuine `InventoryPlayer.mainInventory` and original `InventoryPlayer.currentItem`. It captures source ItemStack identity plus maxDamage and itemDamage, runs the existing V131 native type/count/membership checks and checks *the same* original wrapper, damage, maxDamage, selected slot and array membership again after native getter callbacks. Any original getter that replaces a wrapper or modifies user selected hotbar slot vetoes the unsafe candidate/selection. The chosen rod then passes the unchanged V130 native setter/rollback and V129 final source held-item proof before right-click. This is bounded synchronous verification, not a global atomic inventory or cast guarantee.

## Regression coverage

- SettingRegistry default false and encoded true persistence with lifecycle cleanup; no change to existing settings.
- Source adapters: existing default selects previously held/earlier worn rod, opt-in chooses later pristine rod; lower-durability already-held loses; true tie favors already-held source; native metadata getter mutating InventoryPlayer.currentItem vetoes candidate; same-wrapper damage changes, displaced ItemStack, no native maxDamage and depleted rods fail closed.
- V132/V133/V134 mandatory original Minecraft 1.8.9 X11 acceptance now asks the genuine creative integrated server via vanilla chat for `/give CIProbe fishing_rod 1 60` (worn) and `/give CIProbe fishing_rod 1 0` (fresh), in addition to existing real water/Instant Health I/II potion grants. The original fixture requires source-damage evidence from both rods, old selection of worn rod while preference disabled, then preference enabled selecting the genuine fresh ItemFishingRod with owner token, final-use eligibility and source hotbar restore before printing `CUSTOMMC_OFFICIAL_189_AUTOROD_NATIVE_DURABLE_ROD_PRIORITY_PASS=YES`. Physical Right Shift ClickGUI open must continue to block both Rod/Pot; close then validates eligible source choices again.

## Certification boundary

Release gate is exact-head Foundation CI Windows/Ubuntu **and entire** pinned original Mojang Minecraft 1.8.9 graphical/dedicated workflow SUCCESS, including all historical PvP UUID, rejoin, FOV, reach, scaffold and GUI tests; no historical check removed or weakened. The new test proves native item **selection and permission**, not a real fishing projectile trajectory or server-confirmed rod cast. No claim of server-side healing. Main stays untouched.
