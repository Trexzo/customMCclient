# V141 — source-native regeneration amplifier-aware AutoPot upgrade

## Ancestry and scope

Cumulative Draft child of V140 #606, exact parent `6e045334634e2b1bc52dcf93ee30a9c9a8ec286e`, main untouched. V137 #603 was the latest fully certified Foundation Windows+Ubuntu and complete pinned Mojang original client 1.8.9 binary baseline when this work began. V138–V140 complete original suites still running. Do not merge overlapping stacked PRs to main individually.

V139 added an optional, default-off `combat.autoPot.avoidActiveRegeneration` setting that conserves splash Regeneration when a same-player source-mapped PotionEffect ID 10 has >=40 ticks remaining. V140 proved the true vanilla command-applied Regeneration II duplicate veto and Instant Health exception in the genuine graphical Minecraft original client. However, this policy could also **block a useful stronger Regeneration II potion when a weaker Regeneration I effect was active**. V141 resolves that gameplay limitation without weakening source ownership or the existing equal-strength duplicate protection.

## Production semantics

With `avoidActiveRegeneration=true`:

| Incoming actual original ItemPotion metadata | Vanilla splash | Incoming amp |
| --- | --- | --- |
| 16385 | Regeneration I | 0 |
| 16449 | Extended Regeneration I | 0 |
| 16417 | Regeneration II | 1 |

Source Regeneration is redundant only if the **same original player** actually has PotionEffect ID 10 with native remaining duration **>=40 ticks** and **source active amplifier >= incoming splash amplifier**. Thus an active Regen I (amp 0) allows a genuine Regen II splash (amp 1), whereas active Regen I still rejects Regen I/extended I, active Regen II rejects II and I, and a source effect expiring within 39 ticks permits renewal. Negative/unknown effect amplifiers or durations fail closed, not guessed from GUI text.

Actual vanilla splash Instant Health I/II continues to bypass only the regeneration-duplication check, not the V133 ItemPotion whitelist and source ItemStack owner/membership/final-use gates. V139 double source PotionEffect snapshot reads and reentrant currentItem/metadata checks remain enforced both after the original native setter and immediately before original right-click. Default-off behavior, persisted settings, cooldown scheduling, target gates, production combat/payload/networking and manual vanilla input remain unchanged.

## Deterministic native adapter and bytecode regressions

- New unit cases in `Minecraft189AutoPotRegenerationGuardTest` prove source metadata amplification 0/1, weaker existing ID 10 effect allows a native Regen II upgrade, equal and stronger existing effect veto, extended/regular Regen I veto, nearly expired effect renewability and negative amplifier failure.
- Source-host `Minecraft189CombatItemUseBoundaryTest` exercises the actual `HostRuntime.selectAutoPotSlot`/last-use/owned finally path against real-shaped original player adapters. Switching from equal Regeneration II to a weaker existing Regeneration I permits source regeneration II, then returning to equal Regeneration II rejects, leaving selected currentItem/owner intact.
- V140's mandatory pinned Mojang original 1.8.9 graphical/X11 integrated-world `--native-rod-potion-discovery` acceptance is extended, **not replaced**. The already source-granted genuine Regeneration II ItemPotion is verified after physical `/effect CIProbe 10 30 0` creates actual Regen I (source PotionEffect ID 10 amp 0). The original AutoPot native lease/final-use MUST remain legal and restore currentItem: `CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_WEAKER_EFFECT_UPGRADE_PASS=YES`. Then actual in-world `/effect CIProbe 10 30 1` upgrades the live source effect to amp 1 and **the previously mandatory V140 duplicate-veto and Instant Health II exceptions still must pass**. Finally real `/effect CIProbe clear` must restore no-effect use and owned rollback. Every earlier V132–V140 original source Rod/Pot, real physical fishing cast/reel and GUI acceptance marker is retained.
- This direct Mojang original-world effect level proof must fail closed if the server refuses the /effect command, source effect level is wrong or the selected native potion does not match the verified genuine slot. No shortcuts or fabricated Java Potions.

## Evidence limits and release

The integrated-server test confirms real vanilla source effect amplification, original source ItemPotion selection/final-use eligibility and rollback, **not** that AutoPot's synthetic right-click threw, struck or healed the user. A future independent genuine thrown-potion health/duration test would be needed for that additional claim. Certification requires **exact-head Foundation Windows+Ubuntu SUCCESS plus entire pinned Mojang original 1.8.9 graphical/dedicated workflow SUCCESS**, including historical two real UUID player server damage, Scaffold, FOV, ClickGUI, AutoRod fishEntity cast and potion tests. Keep Draft until all gates complete; main remains unchanged.
