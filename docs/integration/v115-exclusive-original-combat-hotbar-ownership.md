# V115 — exclusive source-native combat hotbar ownership

## Integration lineage and merge discipline

**Certified baseline:** V110 #576 at commit
`7ee908208731103883497896ca326066263fabd3`. Both Foundation
Windows/Ubuntu and the entire pinned Mojang Minecraft 1.8.9 original
client workflow passed on that exact commit.

**Cumulative change sequence:**
- V111 #577: original Rod/Pot local-player identity and captured old
  inventory restoration across source callback replacement.
- V112 #578: melee CombatSlot/AutoWeapon owner captures the inventory
  that really acquired the slot rather than current mutable tick state.
- V113 #579: previous two source fixes in a cumulative review/document.
- V114 #580: one cross-module exclusive original hotbar action owner,
  including the reservation while native inventory getters/setters can
  reenter and original GUI focus can open.
- **V115**: this single integration review and evidence document.

GitHub compare V110→V114 final source head: **10 commits strictly ahead,
0 behind, 4 changed code/test/document paths** before this integration
document. Do not merge all individual stacked PRs plus the cumulative
one, or the source changes would be duplicated. All new PRs remain
Draft; `main` unchanged without separate user authorization.

## Original source-native ownership contracts

| Path | Contract |
| --- | --- |
| Host `playerHotbarSlot` | Identifies the true local player object and current original InventoryPlayer |
| Host `playerHeldItem` | Must be the **same original local player** supplying currentItem for Rod/Pot eligibility |
| Rod/Pot source selection | Capture same genuine inventory before reading native ItemStack; after getter, check source player, inventory, held-item owner, module type/count and actual slot |
| Rod/Pot native getter reentry | Any callback that substitutes the original player or inventory fails admission and restores the old selected slot on the captured owner only |
| Rod/Pot native GUI focus | GUI already open blocks selection; if physical CustomMC ClickGUI opens **inside the native held-item getter**, reject and restore the temporary selection immediately |
| Rod/Pot final original item use | After any AutoBlock stopUsingItem, check exact owner/currentItem plus positive stack count and mapped class **again** before actual Minecraft rightClickMouse; no stale click/cooldown on failure |
| Melee fixed CombatSlot | Preserve original inventory/previous slot/captured target despite changing GUI configuration during an attack |
| Melee AutoWeapon | Preserve exact owner inventory and source-ranked ItemSword. Module disable/reentrant callbacks cannot change which module owns the in-flight finally |
| Shared native hotbar lock | Only one of Rod, Potion or melee may **select** the source InventoryPlayer.currentItem at a time; a reservation exists even during reentrant native getters/setters before lease publication |
| Nested source action | A nested Rod/Pot/melee selection returns -1, does not overwrite original owner; nested `restore(-1)` never consumes outer melee lease |
| Source respawn while a lease is active | Refuse a stale action in the next owner context. On exit, restore **only** the old inventory's still-owned temporary slot. Never write another player's hotbar |
| User-selected alternate slot | Real independent currentItem changes beat synthetic restore if original module-selected target is no longer active |

### Trust / fail-closed boundaries

These rules concern an **in-process original client module's synthetic
hotbar changes**, not server inventory authority. They do not classify
items by display name, fake player or UUID values, create server packets,
infer potion heal/projectile/attack success or replace the original
Minecraft combat controller.

The host should never infer a successful source switch from a silent
native setter no-op; it checks actual currentItem after callback returns.
Setter exceptions and original module rollback rules are preserved.
A temporary source lease exists only when a module really changed
currentItem and recorded the owner. When no module selected a slot,
original gameplay is not blocked by a synthetic owner token.

## Regressions and observed native proof

V111–V114 host-level tests cover mixed original player identities,
reentrant getter callbacks that publish a new local player, old
inventory restoration and new inventory untouched, fixed CombatSlot
and AutoWeapon module ownership across nested attacks, and Rod/Pot
interleaving during other native hotbar selection/while each owns it.

The V114 tests additionally demonstrate:
1. Rod getter reenters Potion/melee before returning the original
   ItemStack; they cannot take over the same hotbar.
2. Rod→Potion→melee sequential actions each succeed only **after**
   the previous action's original finally released its own lease.
3. A source held-item getter or native hotbar setter replacing the
   original local player invalidates selection and restores only the
   old source inventory; reservation prevents nested takeover.
4. The GUI opens inside a native Rod/Pot ItemStack getter. Both
   selections reject and restore immediately, while a fresh action
   after menu close can be selected normally.

These regression tests use Java source-interface doubles and actual
CustomMC host GUI toggle; they are **not** proof of real Minecraft
server item-use or original-game respawn during a melee callback.

Already-confirmed external original-game evidence:
- **V110** full pinned Mojang 1.8.9 original-client CI **SUCCESS**.
- **V108** genuine original Mojang `InventoryPlayer.currentItem`
  physical X11 ClickGUI test and whole original binary **SUCCESS**.
- The earlier original-game Step, NoClip and Reach physical X11 proof
  was retained in V110 and all descendants.

At this document's creation, **V113** original-client CI's first
attempt failed in an existing post-rejoin surviving-player UUID B
combat marker requirement, while the four physical GUI tests passed.
Same-commit failed-job rerun was requested. This is not enough to
establish either a production regression or pure CI flakiness.
V111 and V112 exact-head original-client suites completed SUCCESS.
The *final* V115 head must be separately certified.

## Mandatory full-release verification

1. Exact V115 head **Foundation Windows and Ubuntu SUCCESS**, covering
   all prior tests plus V114 new real host interleaving and GUI tests.
2. Exact V115 head **entire pinned Mojang 1.8.9 original-client
   workflow SUCCESS**, not simply the targeted graphical fixtures.
   Four mandatory physical X11 source-state tests: Step, NoClip,
   Reach and CombatSlot.
3. Preserve original FOV, UUID lock/rejoin, Aura/PvP, Scaffold and
   authoritative dedicated server negative checks intact. If an
   original test fails, capture its run/job step and logs and either
   reproduce, investigate or same-SHA rerun **without** relaxing the
   asserted ownership contract.
4. A separate genuine Mojang in-world **respawn during source right
   click / Rod/Pot use** would be needed to directly establish native
   respawn parity. Do not claim such proof from Java mocks alone.
5. Preserve V110 as rollback point. All child and integration PRs
   remain Draft and `main` unchanged until explicit reviewed merge
   authorization.

This is an integration technical truth record, not evidence that
the newly launched V115 original binary job has passed.
