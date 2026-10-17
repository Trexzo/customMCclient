# V139 — optional original-player regeneration-aware AutoPot

## Origin / stacking

Draft child of V138 #604 at exact parent bca52d0e0166701c8e6ef9c26ab840d2930aa895. Main remains untouched. V135 #601 is the most recently full-certified base as of creation; V136–V138 complete original binary workflows may still be active. Do not individually merge this cumulative chain or claim complete certification from Foundation alone.

## New actual gameplay behavior

AutoPot already chooses source-verified vanilla 1.8.9 splash Instant Health or Regeneration potions (V133), and optionally ranks immediate healing ahead of regeneration (V134). Previously a player already benefiting from a long-running regeneration effect could waste another **Regeneration splash** on the same effect. V139 exposes a persisted Combat ClickGUI boolean **Avoid Duplicate Regeneration**, code setting `combat.autoPot.avoidActiveRegeneration`, **off by default**. Nothing changes unless the user opts in.

If enabled:
- Source ItemStack damage must still pass V133 verified original ItemPotion subclass, positive count, permitted vanilla splash type, exact source inventory membership, selected source slot, and original owner.
- Source **Instant Health I/II remains usable** even with regeneration active. Slow regeneration is not equivalent to instantaneous lifesaving health.
- Source **Regeneration I/II/extended splash** is vetoed only if the same original local EntityPlayer has a native PotionEffect ID 10 (regeneration) with >=40 ticks remaining. Nearly expired regeneration can be refreshed. Other vanilla effects do not block Regeneration.
- The original current source player **must** implement existing Minecraft189PlayerPotionEffectsAccess, sourced from transformed vanilla EntityLivingBase active effects; missing/null malformed evidence fails closed for the opted-in feature, not inferred from display names or stale HUD strings.
- Two fresh source effect reads surround the native item membership validation, explicitly guarding reentrant item getter changing the effect state. Original slot, wrapper identity, metadata, player identity are also read again.
- Production HostRuntime enforces the guard **after** actual native currentItem setter and **immediately before** final original right-click. An effect arriving mid-select triggers normal native V130 owner-safe hotbar restore. An effect arriving after selection but before final use vetoes dispatch; the existing finally still releases its captured lease.
- Native auto-slot selection, V134 healing priority, module cooldown, target validation, user manual potion input and non-potion modules are unchanged. A denied attempt never commits AutoPot use cooldown.

## Regression tests

- New Minecraft189AutoPotRegenerationGuardTest: source Regeneration ID 10 with 200 ticks vetoes, effect at 39 ticks permits, speed/other effects do not block, regeneration splash I/II/extended all veto while the effect is long, Instant Health I/II remains valid, water/poison/harming/drinkable denied, invalid effect/slot/item count and foreign owner fail closed, source inventory/item getter mutation invalidates original ItemStack membership, and a source callback starting Regeneration is rechecked before final use.
- Original-host Minecraft189CombatItemUseBoundaryTest: real module/controller, original-shaped currentItem setter, same-player potion-effects owner and source ItemPotion. Default-off behavior preserved. Opted-in long regeneration prevents a splash lease and restores user slot, Instant Health II remains permitted, an effect beginning after an earlier valid lease vetoes final right-click, an effect arising inside the native setter triggers rollback, and a later cleared effect can safely reacquire without a zombie lease.
- Existing Minecraft189AutoPotModuleTest: SettingRegistry encoded persistence default false/true.
- Existing pinned official Mojang 1.8.9 binary CI must run via the V138 path-filter correction because both AutoPotModule.java, AutoPotFeature.java and HostRuntime.java change. No old acceptance steps, source-mapping gates or gameplay scripts removed.

## Evidence limits and integration gate

Native active PotionEffect ownership is source-mapped from the actual local player; this batch has explicit **unit and source-host behavior proofs**, not yet an independent X11 `/effect`-driven positive original-world test. Do not claim live integrated-world Regen potion delivery or server-confirmed healing from this branch alone. Certification additionally requires exact-head Windows+Ubuntu Foundation CI **and complete** Mojang 1.8.9 graphical/dedicated original-binary workflow SUCCESS including real two UUID damage, physical original fishing cast/reel, Rod/Pot and Scaffold/FOV/GUI gates. Keep Draft, main unchanged.
