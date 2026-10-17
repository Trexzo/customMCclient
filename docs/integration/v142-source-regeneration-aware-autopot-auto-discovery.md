# V142 — source-regeneration-aware original potion hotbar discovery

## Real gameplay problem and ancestry

Draft cumulative child of V141 #607 parent `eee086de87216b81bfdb19ed7e82500033cdd8da`. No main changes. V139–V141 added genuine same-player source PotionEffect ID 10 Regeneration duplicate/strength upgrade policy with opt-in `combat.autoPot.avoidActiveRegeneration`. Previously, when `autoDetectSlot=true`, V131/V134 original hotbar scanning chose a source Regen potion **before** it applied the effect guard. If the player already had equivalent Regeneration II, the native setter selected the unusable splash then V139 final permission rejected it and restored the hotbar. The entire AutoPot use lane was wasted even when a valid **Instant Health II** source ItemPotion existed elsewhere in the genuine InventoryPlayer.mainInventory.

## New production behavior

When and only when **both** pre-existing, persisted options `combat.autoPot.autoDetectSlot=true` and `combat.autoPot.avoidActiveRegeneration=true` are enabled, discovery performs a **read-only source potion viability scan** before acquiring any hotbar lease. No new toggles. It checks the original local player's InventoryPlayer identity, originally selected currentItem, genuine ItemPotion class, positive source ItemStack count, V133 permitted splash metadata, native candidate object membership, the same original player's live PotionEffects (read twice across callbacks) and the V141 effect duration/amplifier rule. Instant Health I/II is still viable while regeneration is active; Regen II remains viable when a weaker amp0 source Regen I is active. Equal or stronger native Regen blocks redundant weaker potions and scanning **continues** to other actual source hotbar ItemStacks.

If `Prefer Instant Healing=true`, preserve V134 actual source damage priority among **eligible** items. Otherwise preserve existing V131 held-first then ascending slot policy among eligible items, not among unusable items. A read-only snapshot cannot itself authorize the native cast: on choosing a candidate, the normal V130 original currentItem setter/owner-captured finally rollback, V129 exact selected source ItemStack and V139 final same-player effect/use gates still all run. A new effect or native wrapper swap between source scan, setter and last right-click is still rejected.

If `avoidActiveRegeneration=false`, all previous V131/V134 discovery behavior is unchanged. If `autoDetectSlot=false`, the exact configured-slot behavior and safe V139 final-use veto are unchanged. There is no network packet or display-name heuristic.

## Tests and provenance

- `Minecraft189AutoPotRegenerationGuardTest` checks read-only original selected-slot preservation while probing a **different actual source hotbar slot**, active Regen II blocks duplicate regen but not Instant Health II, native Regen I permits stronger Regen II, wrong original source currentItem rejects, ItemStack replaced by getter callback rejects and effect getter mutating the real selected slot rejects.
- `Minecraft189CombatItemUseBoundaryTest` drives an actual HostRuntime and original-shaped player+inventory adapters: configured slot veto first, Auto Detect then selects separate Instant Health II instead of the redundant Regeneration II, normal native last-use/rollback passes, and V134 highest instant-healing option composes with this new viability scan. Settings/default restore afterward; no fake packet.
- V141 original GUI 1.8.9 source-effects regression and V137 actual fishing-cast/reel test are preserved. The full pinned original Mojang binary suite remains a strict release gate and MUST not be misreported as a proven actual potion throw.

## Release rules

Require exact-head Foundation on Windows+Ubuntu plus the **entire** pinned Mojang original 1.8.9 graphical/dedicated CI SUCCESS, including real two-player UUID A/B damage, physical source fishEntity rod hook, actual vanilla Regeneration strength/clear, Scaffold, FOV, GUI, previous Rod/Pot and source owner gates. Main untouched. Stacked PR stays Draft. A genuine **thrown splash health or regeneration outcome** is not demonstrated merely by native selected item and final-use permission.
