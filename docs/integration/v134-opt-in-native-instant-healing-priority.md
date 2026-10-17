# V134 — optional instant-healing priority for original 1.8.9 AutoPot

## Ancestry and change scope
Cumulative **Draft** child of V133 #599, parent exact `e1feaa64cbff3b89f15d1b10223e6e2099a1ce6f`. V130 #596 had Foundation and complete Mojang binary SUCCESS; V131–V133 full exact-head original workflows had not all completed when work began. Do not merge individual overlapping PRs or change `main`.

## User-facing gameplay
Original AutoPot has two mutually independent settings:
- `combat.autoPot.autoDetectSlot`: previously implemented opt-in source-native hotbar discovery. Still **off** by default.
- **New** `combat.autoPot.preferImmediateHealing`: **off** by default, persisted in SettingRegistry, exposed as **Prefer Instant Healing** in Combat ClickGUI. Only affects auto-discovery; configured fixed-slot selection remains unchanged.

Without priority, V131 still chooses a valid already-held splash potion, otherwise the first eligible hotbar index. With both options enabled, V134 ranks *verified original vanilla* `ItemPotion` splash item metadata by suitability for prompt health recovery:

| Source ItemStack damage | Known vanilla effect | Score |
|---|---|---|
| 16421 | Splash Instant Health II | 50 |
| 16389 | Splash Instant Health I | 40 |
| 16417 | Splash Regeneration II | 30 |
| 16385 | Splash Regeneration I | 20 |
| 16449 | Splash Extended Regeneration | 10 |
| anything else | Unknown, harmful, water or drinkable | 0, reject |

A same-rank tie favors the *already-held* item; otherwise the earliest slot wins. A higher-ranked eligible splash potion wins over lower-ranked already-held regen when the user opts into priority. The scan never guesses from names, hotbar icons, text, or unverified source mappings.

## Source ownership and native getter reentrancy
The ranked scan uses only transformed original `InventoryPlayer.mainInventory`, `ItemPotion` type, positive count, allowed damage values and `InventoryPlayer.currentItem`. It captures the actual source item wrapper and damage, rechecks through the V133 full native-candidate source gates, then requires consistent damage, currentItem and array membership after getter callbacks. A disappeared source wrapper or changed human selected slot cannot authorize a ranking. The chosen candidate is then subjected to the **unchanged** V130 hotbar setter acknowledgment, captured restoration lease, V133 post-selection/final right-click original ItemStack and metadata checks. No global atomic-inventory guarantee is claimed. The setting does not change Rod, combat targeting, health threshold, cooldown or server packet generation.

## New adversarial tests
- Source policy JUnit: exact 50/40/30/20/10 order and zero for harmful, drinkable, unknown IDs.
- AutoPot SettingRegistry: priority setting persists as false by default and encodes true after modification.
- Original-access hotbar tests: defaults favor first valid (regen) even when II exists; enabled priority selects actual Instant Health II over earlier regen/I; user-held lower rank is bypassed; a tied user-held II wins; metadata getter replaced ItemStack is excluded; a callback moving native currentItem invalidates selection.
- The mandatory V132 genuine original Mojang/X11 integrated-server test now grants `potion 1 0`, `potion 1 16389`, `potion 1 16421` using vanilla chat and real server inventory. It proves the earlier water cannot own an AutoPot lease, then proves default priority disabled picks actual source Instant Health I, enables the persisted option and requires native ItemStack Instant Health II, preserved hotbar restoration, wrong-token veto and physical ClickGUI open/close behavior, with new `CUSTOMMC_OFFICIAL_189_AUTOPOT_NATIVE_STRONG_HEAL_PRIORITY_PASS=YES`.

## Acceptance and limitations
Release requires **exact-head** Foundation CI SUCCESS on Windows and Ubuntu and **complete** pinned Mojang 1.8.9 original binary workflow SUCCESS, including all preexisting FOV/UUID/PvP/Scaffold/X11 tests. The new original source evidence is the *selection and final-use eligibility* of actual objects, **not** proof a right-click launches a potion, reaches the user, or causes a server-confirmed health improvement. Custom NBT and behavior on modified servers remain outside this certificate. Keep Draft until all tests have passed.
