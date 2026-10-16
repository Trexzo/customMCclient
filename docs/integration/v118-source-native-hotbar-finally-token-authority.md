# V118 — original source-native combat hotbar finally-token authority

## Ancestry and review shape

This branch is a **single cumulative review** of V116 #582 and V117 #583
on fully certified V115 #581. The verified GitHub comparison
`a5a5e7b... → 05d962b...` contains **5 linear commits, zero behind**, and
four Java/Python production, fixture and regression paths before this
document. Its main production change is in
`Minecraft189HostRuntime`, and the native evidence upgrade is in
`Minecraft189OriginalCombatSlotAcceptance` plus the full original
Minecraft 1.8.9 Python harness.

Prior certified baseline:

- V110 #576: Foundation Windows/Ubuntu and full original Mojang CI PASS.
- V111 #577 and V112 #578: both Foundation and full original client CI PASS.
- V115 #581: **Foundation Windows/Ubuntu and complete original Mojang
  1.8.9 official binary workflow PASS**, including physical X11 source
  Step, NoClip, Reach and CombatSlot proofs and historical multiplayer
  UUID/FOV/PvP/Scaffold requirements.

V113 #579 ran an original client test that **failed twice** on a retained
post-rejoin UUID/combat witness (different missing original markers in
the first and rerun). Root cause is not established. Do not classify a
successful later aggregate run as proof that this historical fixture is
perfectly deterministic; do not remove or weaken its checks.

## Production change — no foreign positive finally token can unlock currentItem

V115 gave each combat action a captured native
`InventoryPlayer.currentItem` inventory owner and an exclusive host
hotbar-selection reservation spanning native reentrant getters/setters.

Before V116, a **foreign positive** `restore*Slot(originalSlot)`
argument could clear HostRuntime's recorded lease without passing the
underlying module's own original-slot token check. That module correctly
refused a native slot write, but a different Rod, Potion or melee action
could now acquire the same source inventory while the first temporary
slot was still selected.

V116 fixes the split-brain source owner:

| Temporary action owner | Exact host finally protection |
| --- | --- |
| AutoRod | Stores the original pre-switch slot with captured original inventory. Wrong positive or -1 token does not release the host owner or call native module restore |
| AutoPot | Independent identical token rule, distinct from Rod |
| Fixed CombatSlot | Captured original pre-switch slot and native InventoryPlayer identity; foreign positive or nested -1 does not consume the outer melee lease |
| Ranked AutoWeapon | Same host melee lane, but preserves which module acquired the original sword slot despite unrelated configuration/module state changes |
| Valid `finally` | Only exact owner original slot token releases host lock and executes the underlying module's safe original-inventory restoration |
| Native setter exception | The pre-selection reservation clears in its original Java `finally`; the modules retain their strict native-slot rollback, preserving genuine user-selected currentItem |

These rules are scoped to the **single synchronous synthetic action**.
This is not a general multithreaded game-wide inventory lock, not a
network packet protocol and not a claim of server-owned inventory state.
A numeric previous-slot token alone cannot represent a globally unique
lease generation; do not extrapolate protection to an invented identity
protocol when two unrelated future actions happen to share the same
original index.

## Adversarial Java regressions (V116)

Original HostRuntime with Java source-native inventory controls verifies:

1. Rod temporary native selected slot, then foreign **positive token**
   and -1 restoration; currentItem stays module-owned, no Potion/melee
   second selection becomes eligible. Correct Rod finally restores.
2. Potion same scenario, blocking Rod until exact Potion finally.
3. Fixed CombatSlot same scenario, blocking Rod/Pot until exact melee
   finally, then new correct gameplay action is eligible.
4. Original source hotbar setter mutates its backing currentItem then
   throws during Rod, Potion and melee selections. Reentrant attempts
   cannot steal the selecting reservation; native module rolls back
   old source slot; the next valid module can acquire successfully.

These source-interface doubles certify deterministic host behavior,
**not** a real Mojang native respawn, server-authorized rod cast, potion
heal or sword damage.

## Genuine original Mojang negative proof (V117)

The existing V108 original-game `InventoryPlayer.currentItem` physical
X11 ClickGUI acceptance now also demands **V117's additional native
negative marker**. The witness uses a genuine transformed Mojang local
player and its actual currentItem, not a mocked player or fabricated
item stack:

1. Twelve stable source-player/InventoryPlayer baseline callbacks.
2. Normal original CombatSlot ModuleController sets a preferred slot;
   production native selected slot is verified and restored.
3. Real physical X11 Right Shift opens CustomMC ClickGUI; temporary
   slot selection is vetoed and native currentItem remains original.
4. Physical Right Shift closes GUI. Normal source CombatSlot selects
   an alternate real currentItem. An intentionally **wrong but valid
   positive original-slot token** is passed to actual
   `HostRuntime.restoreCombatSlotAfterSyntheticClick`.
5. That wrong token must **not** change native currentItem or allow a
   nested source selection. A second nested `restore(-1)` likewise
   must not alter it. Witness emits the distinct
   `CUSTOMMC_OFFICIAL_189_COMBATSLOT_NATIVE_WRONG_TOKEN_VETO_PASS=YES`.
6. Mutate actual ClickGUI Preferred Slot *during* the still-held lease.
   The original **true** token must restore the baseline native slot.
   Disable via ordinary ModuleController and prove the original
   currentItem remains unchanged.

The X11/Python runner now awaits this new evidence **in addition to**
the preexisting V108 mandatory markers; it neither omits Step/NoClip/
Reach/CombatSlot acceptance nor weakens historical server and combat
negative tests.

This proves local genuine original client **source currentItem ownership
under an invalid restore token**, not a successfully executed attack,
rod cast, potion effect, or server synchronization.

## Mandatory release gates

- Exact V118 head: Foundation Windows + Ubuntu SUCCESS, including
  strict-token source host regressions.
- Exact V118 head: entire pinned Mojang 1.8.9 graphical original client,
  dedicated-server and historical negative CI SUCCESS, including
  *all five* mandatory V117 source CombatSlot/GUI marker stages.
- All child PRs **Draft** and `main` untouched. Review V116–V117 through
  V118 as one cumulative integration; don't independently merge child
  PRs and this aggregate.
- Maintain the already-proven V115 head as rollback anchor. CI run
  statuses must be checked by exact SHA; this document itself is not
  a claim that a newly started V118 workflow has already passed.
