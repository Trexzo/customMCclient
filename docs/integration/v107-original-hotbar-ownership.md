# V107 — original native hotbar authority and rollback

## Goal and ancestry

Protect source-native `InventoryPlayer.currentItem` from incorrect
synthetic slot ownership while retaining the genuine 1.8.9 player's own
choices. V107 is a single review surface for eight original production/test
commits stacked on:

- V103 #569, source-native original Mojang Reach/ClickGUI acceptance
- V104 #570, AutoRod and AutoPot verification
- V105 #571, fixed CombatSlot lease and rollback
- V106 #572, AutoWeapon stale scan and original setter parity

The V103→V106 GitHub comparison confirmed **8 commits**, strictly ahead
with zero behind, across **8 Java files** before this integration document.
Do not independently merge all child PRs and then merge V107. All Draft,
`main` unchanged pending separate authorization.

## Native source authority

| Action | Verified production invariant |
| --- | --- |
| AutoRod | Source ItemStack must be exactly mapped ItemFishingRod, positive stack count, and still in the module's originally captured inventory and selected target slot **after** native held-item read |
| AutoPot | Exact mapped potion ItemStack, positive stack count, and unchanged owned native inventory/slot after the real held-item read |
| Both above | A reentrant user hotbar change, module disable or empty stack revokes eligibility, restores only a still-module-owned slot and does not spend the one-use cooldown |
| CombatSlot | Captures original InventoryPlayer identity, currentItem and chosen source slot in a single lease; mutable ClickGUI Preferred Slot does not retroactively change which slot finally restores |
| CombatSlot nested action | Cannot supersede a previous unfinished original slot lease; wrong inventory/token cannot force restoration |
| CombatSlot setter exception | If original setter mutates currentItem and throws, roll back only still-owned target; preserve independent user changes and attached rollback failures |
| CombatSlot silent setter ignore / redirection | The module does not report a successful switch if the actual original currentItem differs from the captured requested target |
| AutoWeapon native item scan | If real currentItem changes while source mapped sword descriptors are read, skip selection rather than overwrite the later user choice |
| AutoWeapon native setter | If setter silently rejects/redirects, no successful lease is reported; an in-setter module disable restores only the still-owned sword slot |
| All | No display-name based sword/rod/potion authority, synthetic game packets, server heal/damage inference, or invented original source snapshots |

## Regression evidence

Host-level source-access-interface tests cover:

1. Rod/Pot zero and negative stack sizes, valid positive return, native
   getter reentering and moving to another selected hotbar slot.
2. Module disable during the same rod/potion held-item lookup, owner
   identity and no stale lease on retry.
3. CombatSlot changed GUI preferred-slot during the action, nested attempt,
   foreign inventory/restore token, disable before finally, and manual
   user slot changes.
4. CombatSlot native setter that **mutates then throws**, silently does
   nothing or redirects to an unexpected selected slot.
5. AutoWeapon native inventory scan changing currentItem, silent/no-op
   setter, reentrant different native selection, module disabled inside
   setter, followed by successful retry.

The host regressions employ Java doubles for native inventory interfaces.
They do **not** prove a dedicated Minecraft 1.8.9 server actually observed
fishing rod projectiles, potions or sword switching. This is a source
correctness and rollback package, not original-game item-use certification.

## Review and release gate

- Foundation CI must pass on **both** Windows and Ubuntu on the **exact
  V107 head**, including all existing Java tests and native build preflight.
- Full pinned original Mojang 1.8.9 CI must pass on this same head, keeping
  genuine physical X11 Step, NoClip and Reach GUI acceptance and **all**
  older UUID/rejoin/FOV/PvP/Scaffold negatives.
- The V103 original Reach acceptance proved local renderer ray operand on
  a targeted native test. Its full original suite and subsequent V104–V107
  suite statuses must be verified separately by exact SHA before claiming
  overall certification.
- Long-running original multiplayer tests have occasionally failed with
  missing UUID markers. Preserve their fail-closed assertions, inspect
  exact logs and same-head reruns, never bypass a genuine negative or
  label a module tested simply because Foundation passed.
- No merging to `main` without user approval. Preserve rollback back to
  the already-certified V102 original GUI integration baseline.
