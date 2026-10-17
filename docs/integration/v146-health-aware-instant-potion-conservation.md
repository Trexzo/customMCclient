# V146 — opt-in source-health-aware Instant Health II conservation

## Context, scope, and ancestry

Draft direct child of V145 #611 at exact parent `748ed46823b4a361e4d2505fa4a82699f4d7a7e2`; `main` unchanged. V133–V142 already verify original vanilla 1.8.9 splash Instant Health/Regeneration metadata, source inventory hotbar and ownership, regeneration duplication veto and useful alternative discovery. V143 adds optional original pitch lease to aim actual AutoPot right-click toward feet, V144 proves a manually thrown vanilla splash applies native Regeneration, and V145 requires a genuine Survival original AutoPot synthetic cast, original survival item consumption and measured health recovery. V146 improves AutoPot's choice among available *instant* healing tiers without inventing a new potion type, packet, health measurement or world simulation.

## User-facing feature

New Combat ClickGUI persisted setting `combat.autoPot.conserveInstantHealingII` / **Conserve Instant Health II**, **false by default**. This feature is active only when **Auto Detect Potion Slot** and **Prefer Instant Healing** are both enabled, because only then does the module choose among genuine verified hotbar potions using original source metadata. When disabled, earlier V134/V142 selection remains byte-for-byte ranked the same way.

Given the SAME original local player and genuine source `EntityLivingBase.getHealth()/getMaxHealth()` observations, original legacy vanilla splash Instant Health I metadata `16389` nominally restores four health points at direct impact, while II metadata `16421` nominally restores eight. With **more than zero and at most four health points missing** and both tiers available, prefer preserving Instant Health II by selecting original Instant Health I. With a larger deficit, still prioritize Instant Health II. If only II is available during an emergency, II remains permitted; conservation must never falsely veto the sole verified source healing option. Delayed Regeneration I/II and unknown potion classes/metadata retain existing original priorities and safety gates; health-aware selection does not infer actual splash-strength or guaranteed heal from static metadata.

## Source-integrity and reentrancy

- Applies only if exact source player implements both original `Minecraft189PlayerHealthAccess` and `Minecraft189PlayerInventoryAccess` and owns the same genuine `InventoryPlayer` as the hotbar scan. Reject missing/foreign, nonfinite, nonpositive or impossible health/maxHealth evidence. No stale HUD health or unrelated player.
- Re-reads the exact source health deficit after candidate ranking and again after native slot setter and exact original held ItemStack class/identity verification. If source health changed during ItemStack/source accessor callbacks, rejects and restores the captured original selected slot.
- When V142 **Avoid Duplicate Regeneration** is enabled, only native potion candidates that pass the same source Regeneration and inventory gates are ranked, so priority never resurrects an unusable Regen potion. Existing original setter, physical hotbar, GUI, live effect and final-use checks stay active. No additional network packets and no changes to manual item use, Aura/Scaffold/Rod, or health thresholds.
- The conservation option is default-off and inert with fixed slot selection or without the existing **Prefer Instant Healing** setting.

## Evidence and regression

- `Minecraft189HealthAwareSplashRankTest`: strict source metadata health-deficit boundaries, default-off legacy ranking, unknown/harmful splash nonpromotion and preserved delayed regeneration priorities.
- `Minecraft189CombatItemUseBoundaryTest`: source-shaped player implements real-shaped health interface; original inventory holds stronger Instant Health II in a lower slot and weaker I in a later slot. With 2 or 4 health points missing, opt-in selects I; with 6 missing selects II. Without opt-in II still wins. If I is gone, II remains eligible, and reentrant source ItemPotion getter changing health during ranking fails closed without stealing the hotbar. No true low-health evidence or NaN cannot authorize source economy ranking.
- `Minecraft189AutoPotModuleTest`: encoded SettingRegistry default false/true persistence.

The prior required full pinned original Mojang 1.8.9 binary graphical and dedicated suite is not replaced by mocks, and existing V145 actual Survival synthetic cast and real health-delta test must pass unchanged on this child. V146 currently has deterministic original-source-shaped tests; it does not yet include a separate new in-world *two-tier* original hotbar outcome. Do not mistake vanilla nominal heal tiers for verified actual delivered splash points.

## Certification and integration

Require **exact-head Foundation CI Windows+Ubuntu SUCCESS and entire pinned official Mojang 1.8.9 original binary/dedicated workflow SUCCESS**, including separate live UUID A/B damage, native obstruction Wall Check, X11 physical fishing cast/live hook/manual reel, genuine original Regeneration /effect and splash, actual AutoPot pitch, Survival healing (V145), Scaffold/FOV and GUI controls. All child PRs remain **Draft**; do not merge overlapping descendants individually or change `main`.
