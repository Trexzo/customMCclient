# V133 — Vanilla 1.8.9 source splash-healing potion semantics

## Ownership and ancestry

Draft on V132 #598 (head `25711f7f578c3a56a468ecfef9fb1e4800c65e0c`), not a duplicate V127–V132 implementation. V130 #596 is a fully certified Foundation + complete official binary ancestor. V131/V132 exact-head complete binary workflows are still running during this review. Main stays unchanged.

## Actual production correction

AutoPot previously accepted any ItemPotion with positive ItemStack count. This included ordinary water bottles, harmful potion types, and drinkable variants, although the production action invokes one original `Minecraft.rightClickMouse()` then restores the prior hotbar immediately, without completing a drinking use.

`Minecraft189LegacySplashHealingPotion` conservatively recognizes only *known vanilla pre-1.9 potion ItemStack metadata*: `16389` (splash instant health I), `16421` (splash instant health II), and `16385`/`16417`/`16449` (splash regeneration I/II/extended). All other damage values, including water (0), drinkable healing (8197/8229), poison (16388/16420), harming (16396/16428), invalid values and unknown types are denied. Source class `ItemPotion` must still be independently proven. No display-name heuristics, fabricated packets, or claimed server effects.

Both the fixed-slot selection and optional V131 hotbar auto-discovery check the actual transformed `ItemStack.getItemDamage()`, original ItemPotion subclass, positive item count, real source-array membership and captured native currentItem owner. Metadata is rechecked at source scan and again after native held-item/stack getters, including just before native right-click. Reentrant getter mutation fails this bounded check. V129/V130 strict native setter and rollback ownership remain unchanged. The source metadata policy is conservative; it does not certify NBT-custom potion effects or guaranteed healing on a server.

## Regression coverage

- Standalone `Minecraft189LegacySplashHealingPotionTest` covers positive vanilla IDs and negative water, harmful, drinkable and unknown values.
- Expanded `Minecraft189VerifiedCombatItemSlotTest` proves fixed-slot and auto-discovery denial of nonheal potions, skipping an earlier water bottle, successful valid healing source admission, and native damage-getter mutation before selection/final use.
- Existing Rod/Pot source-host tests now represent verified splash-healing metadata in mocks that previously represented all potion metadata as zero.
- The existing mandatory V132 original Mojang X11 test now first uses actual vanilla chat to grant a **water potion** and checks that a genuinely mapped source `ItemPotion` produces no AutoPot lease, requiring `CUSTOMMC_OFFICIAL_189_ROD_POTION_NATIVE_NONHEALING_VETO_PASS=YES`. Next, vanilla `/give CIProbe potion 1 16389` must deliver the real splash-healing item; V131 discovery must skip the earlier water potion and pass genuine source stack, currentItem/restore and physical GUI checks.

## Release gate

Require exact-head Foundation Windows+Ubuntu SUCCESS and the entire pinned original Mojang 1.8.9 binary suite SUCCESS, preserving all original UUID/FOV/Scaffold and X11 acceptance tests. This proves exact source item eligibility and native selection—not a server-confirmed potion throw/heal, dedicated-server projectile or custom-NBT potion semantics. Keep Draft until certified; never merge to main without authorization.
