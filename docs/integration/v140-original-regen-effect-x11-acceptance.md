# V140 — original Minecraft 1.8.9 /effect native AutoPot veto acceptance

## Motivation, ancestry, and baseline

Draft direct child of V139 #605 exact head `8e24781ba7a706aeeee0830dd9bb1dc3e88046fc`. Main untouched. V139 introduced a default-off setting that avoids duplicating native source splash Regeneration if the same player already has a long-lived vanilla regeneration effect (PotionEffect ID 10). Production JUnit and source-shaped owner tests cover the stateful permission and rollback decisions, but without this acceptance the feature would only be synthetically exercised. V140 adds strict real native world evidence, rather than weakening or replacing old tests.

## Genuine graphical X11 and integrated-server evidence

Extend **the same mandatory** `--native-rod-potion-discovery` job in the pinned Mojang 1.8.9 full binary workflow, immediately after the retained V132–V137 hotbar, potion metadata, durable rod, physical GUI, real fishing-cast/non-null fishEntity, live AutoRod veto and manual fishing reel checks. The Java fixture remains bound to the exact original transformed EntityPlayer and InventoryPlayer objects, not mocks.

1. Actual Mojang creative integrated server grants an additional **real splash Regeneration II** via physical in-world vanilla chat `/give CIProbe potion 1 16417`. Java requires genuine source ItemPotion class, stack count, data and original source slot, no active regeneration on that same player, and normally enables AutoPot via ModuleController with the V139 persistent effect guard. Its exact configured vanilla source slot acquires the real hotbar lease, passes final-use eligibility and restores physical user currentItem. Mandatory no-effect positive marker.
2. Python sends physical chat `/effect CIProbe 10 30 1` to the genuine original server. Java waits for the **original living player's actual native PotionEffect object** to appear with ID 10 and duration >=40 ticks; no timer guess or module-produced fake. Production `HostRuntime.selectAutoPotSlot()` must now return -1 **and restore the exact physical owner slot**; no potion right-click is dispatched. Mandatory negative marker.
3. While the same source regeneration effect remains active, fixture selects the existing genuine Instant Health II ItemPotion from the original source inventory, using configured source slot and `AutoPot` native setter and last-use gate. It **must be allowed**, thus preventing a dangerously broad veto of emergency instant healing. Captured hotbar finally restores original user slot and setting is returned to the Regeneration slot. Mandatory Instant Health exception marker.
4. Python sends real vanilla `/effect CIProbe clear`. Only after the same source player reports no ID-10 PotionEffect may the same original Regeneration splash reacquire native selection, pass final-use eligibility and restore physical currentItem. Finally the normal ModuleController disables Rod/Pot. Mandatory clear-and-rearm marker.

This is genuine integrated-server effect application/removal + original client live PotionEffect + source ItemStack and right-click eligibility. The fixture **deliberately does not actually throw the potion** or assert health restored; effect is applied by a vanilla command, not by AutoPot. It does not prove remote dedicated server potion replication, splash physics or medical-style heal amounts.

## Required original client markers

- `CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_NO_EFFECT_POTION_ALLOWED_PASS=YES`
- `CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_ACTIVE_EFFECT_DUPLICATE_VETO_PASS=YES`
- `CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_INSTANT_HEAL_EXEMPT_PASS=YES`
- `CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_EFFECT_CLEAR_REARM_PASS=YES`
- `CUSTOMMC_OFFICIAL_189_REAL_REGENERATION_EFFECT_GUARD_X11_PASS=YES`

No previous historical marker is removed, skipped or renamed. The runner uses actual Minecraft X11 chat with open/close confirmations. Failure to grant the item, apply actual effect, observe the correct original local player, prove full owner-safe rollback, or clear the effect fails the pinned original binary job.

## CI, ownership and deployment

Stacked Draft branch `native/v140-genuine-regeneration-effect-veto-x11` based on V139. Release requires exact-head Foundation Windows+Ubuntu **both SUCCESS** and the **entire** pinned original Mojang 1.8.9 binary/dedicated suite SUCCESS, including strict simultaneous live A/B UUID server damages, real physical fishing cast/reel, HUD/FOV/Scaffold and GUI checks. No merging of individual overlapping PRs, no changes to main, and no claims of complete certification before full exact-head results.
