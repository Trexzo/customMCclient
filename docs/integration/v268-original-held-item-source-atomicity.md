# V268 — genuine held ItemStack owner and atomic native readback

Parent V267 Draft #738, SHA b38c71b596cdcaf52ec8151edef6920fb9a417c5.

## Original source defect
`Minecraft189HostRuntime.playerHeldItem` previously published `tickHeldItemAccess=player` *before* calling native mapped player held ItemStack getter and 4 original ItemStack getters (displayName, stackSize, itemDamage, maxDamage). Each native callback may synchronously reenter with a different/null original player or throw. The old original caller would still publish a mixed/obsolete held-item HUD snapshot or retain a stale player owner for downstream genuine AutoBlock sword checks and AutoPot/AutoRod ownership. Getter failures or rejected invalid original items preserved previous HUD evidence and could leave a stale mutable item-use source.

## Production change
Use typed shared `Minecraft189ExactSourceLease<Minecraft189PlayerHeldItemAccess>` around the real player `customMcHeldItem()` getter plus four genuine item getters. On first admitted callback, clear old `tickHeldItemAccess`. Check exact player reference after **each** native getter; nested same player is ignored as a duplicate while nested different/null player irreversibly revokes the outer readback. A null ItemStack/source clears previous state. Original state update validates genuine original name, positive quantity and non-negative damage+maximum as before, then publishes the one validated immutable readback. Publish `tickHeldItemAccess` for subsequent AutoBlock and AutoPot ownership only after all real getters and validations succeed. Any RuntimeException/Error clears both held-item HUD and downstream item-use owner; original exception propagates; finally releases source lease. Original maxDamage=0 item remains allowed.

No native item modifications, fake sword classification, original mapping, packets, AutoBlock target condition, slot writes, click timing, GUI or acceptance gate changes.

20 source-level Java 8 JUnit regressions cover exact typed owner and recovery, all 5 getter stages (same/different/null source reentries), genuine real item values/zero max, invalid quantity/name/damage, exception/Error per stage and source restoration. These are not real original Minecraft certification.

**Strict gates:** exact-head Windows+Ubuntu Foundation, full pinned official Mojang 1.8.9 GUI X11+vanilla dedicated server 78/78, followed by second independent 78/78 on unchanged SHA. All PRs remain Draft, main untouched, prior native failed runs retained.
