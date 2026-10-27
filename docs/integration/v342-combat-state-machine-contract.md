# V342 — Shared Combat State Machine and Source Authority

Minecraft Java 1.8.9 · isolated combat integration · **Draft / not certified**

## Model

The runtime does **not** give each module permission to invoke native
Minecraft input independently. There are separate, composable ownership
lanes:

| Lane | Owner | Invariant |
|---|---|---|
| Target | original ray + mapped World UUID, AntiBot, Friend/Team/Wall, AttackRange | A cached index is not a UUID. Never borrow another target's hurt window |
| Aim | AimAssist or KillAura rotation policy | Rotation cannot create a valid entity, target, or server-owned hit |
| Primary action | per-host `Minecraft189CombatCycleArbiter` | Exactly one of AutoPot, AutoRod, melee per native synthetic callback |
| Melee module | HostRuntime attack arbitration | KillAura > TriggerBot > AutoClicker, with module-specific source validation |
| Melee final dispatch | original clickMouse or original PlayerControllerMP.attackEntity | Same real UUID, currently allowed HitSelect hurt-time; no banked rejected attack |
| Item use | original InventoryPlayer/held source and rightClickMouse | Pot > Rod; AutoBlock only after a melee **attempt** and original sword-use proof |
| Sprint | WTap, KeepSprint, AutoSprint on genuine EntityPlayerSP | WTap owns resets; KeepSprint must not restore while WTap enabled; AutoSprint independently respects original movement policy |
| Velocity | original `pr.knockBack` receiver | Only exact local `bew` is allowed to scale motion; remote/mob vanilla motion untouched |
| Manual input | physical mouse, game GUI, original inventory | Never reissue or release user-owned mouse/buttons |
| Lifecycle | original World/player/screen and host cleanup | Non-reentrant cycle tokens, fresh source owner generations and `finally` restore |

## State transitions

```text
QUIESCENT
   └─ begin real host autoClick() ─▶ EVALUATING
        ├─ verified potion slot ─────▶ POTION ───────┐
        ├─ verified rod slot ────────▶ ROD ──────────┤
        └─ eligible genuine target ─▶ MELEE_ATTEMPT ├─ finally ─▶ QUIESCENT
                                        └─ verified source sword
                                           + no manual right input
                                           └─ REBLOCK_ATTEMPT ─────┘
```

A rejected target, missing hotbar slot, GUI interruption, native exception,
reentrancy or shutdown **never** generates a second action or a “makeup”
click later. `MELEE_ATTEMPT` means a synthetic native call was eligible,
**not** that the original controller accepted it or a server reported damage.

## Source compatibility requirements

- WTap V337: movement requirements revalidated during every outstanding
  multi-tick sprint reset. Recovered W or ground does not resurrect a prior
  held-attack click. KeepSprint's existing `wTap.active()` veto remains.
- HitSelect V338: current original crosshair index, actual same player UUID,
  alive/kind and newest hurt time checked at **all three** native melee
  call paths. Target A's damage is never transferred to target B.
- Velocity V339: source-mapped `EntityLivingBase` method remains vanilla for
  remote players and mobs. Only exact `EntityPlayerSP` accepts any scaling.
- AutoBlock V340.1: the same original `heldItemOwnerSerial` must survive
  the native sword use result. New same-type sword is a *different* source.
- Arbiter V341: one token per native callback; only melee allows one later
  AutoBlock reblock attempt. All early exits release the cycle. Does not
  replace source World/UUID, GUI, sword, hotbar or server checks.

## Scope and remaining release blockers

This V342 branch has the V337–V340.1 production code and tests **in the
same source tree as V341**. This eliminates standalone-PR compile illusions
and detects runtime-bridge/host merge conflicts in Foundation and original
Mojang binary preflight. The separate feature PRs remain unchanged.

The V326–V336 Auto Clicker/ConnectScreen original acceptance branch and
V333/V334 original vanilla-GUI veto are **not silently combined** into V342.
Those outstanding source-original GUI cases must be integrated and retested
as a *separate* stack after their original genuine server acceptance is
understood. A passed Foundation suite or basic original binary preflight
does not certify Auto Clicker/TriggerBot/Aura manual-player parity.

Required acceptance before main merge/release:
1. **Both** Windows and Ubuntu Foundation pass on the exact combined commit.
2. Genuine SHA-verified Mojang 1.8.9 binary/linkage + unmodified dedicated
   vanilla-server workflows pass; any prior red test remains visible.
3. Real native PlayerControllerMP original remote-player argument UUID,
   original hurt and server-owned scoreboard damage are verified.
4. Original inventory/manual mouse/vanilla GuiScreen input remains owned
   exclusively by Mojang, and a GUI transition cannot replay a stale action.
5. A dedicated simultaneous in-game scenario confirms switching modules
   during combat, target A→B replacement, Sprint/WTap ownership, Pot/Rod
   preemption and AutoBlock's original sword-use release.

Until all five gates are met, V341/V342 and the individual module PRs remain
Draft and must **not** be merged into `main` or releases.
