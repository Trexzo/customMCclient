# V351 — Auto Clicker trigger UUID through original native controller

Minecraft Java 1.8.9 · Native combat ownership · Draft only

## Original gap and reason for this module

V350 attaches an original loaded World UUID to the Auto Clicker's
trigger-mode crosshair confirmation, CPS and native HostRuntime admission.
However, vanilla `clickMouse` can reach the original mapped
`PlayerControllerMP.attackEntity` method after intermediate callbacks.
Before V351, the V350 UUID was not enforced against that method's **actual
original Entity argument**. A ray-target change during the synchronous
vanilla call could therefore differ from the previously scheduled target.

## Exact ownership stages

1. The existing V350 HostRuntime still requires source-native UUID,
   kind, current crosshair index and live loaded World readback to earn
   one Auto Clicker trigger action. No old target's CPS credit is reused.
2. At final shared admission, HostRuntime arms exactly one
   `Minecraft189AutoClickerControllerLease` for the verified UUID plus
   Auto Clicker's enabled-module epoch. The lease clears on new action
   scheduling, GUI veto, runtime close and combat-cycle exit.
3. Immediately before invoking the **one unchanged vanilla clickMouse**
   call, the bridge consumes the lease against current source World UUID,
   module enable/trigger setting, other automatic click owners and GUI.
   Failed or stale grants are spent and can never be replayed.
4. Only this source-verified Auto Clicker trigger call holds a synchronous
   ThreadLocal `Intent`; `finally` always removes it.
5. The original mapped `acceptanceControllerDispatchAllowed` hook reads
   the actual outgoing original native `Entity` UUID and requires it to
   match the scoped `Intent`. It rechecks module epoch, trigger setting,
   KillAura/TriggerBot ownership and GUI before native controller side
   effects. A changed A→B player, source owner or context fails closed.
6. Manual clicks, unindexed historical test fixtures, ordinary hold-mode
   Auto Clicker, AutoPot, AutoRod, KillAura and TriggerBot do not receive
   this intent. The preexisting UUID gates for Aura/TriggerBot remain.
   No new packet emitters, controller call, artificial hit/cooldown
   bypass or server-owned health change is introduced.

## State-machine compatibility

The existing action cycle remains:

```
QUIESCENT -> EVALUATING
             -> POTION
             -> ROD
             -> MELEE_ATTEMPT -> [optional REBLOCK_ATTEMPT]
-> QUIESCENT (finally)
```

The ControllerLease is a *secondary target-identity sub-state* of the
existing MELEE_ATTEMPT lane, not a second concurrent primary action.
The old `Minecraft189CombatCycleArbiter` and priority order
**AutoPot > AutoRod > melee > conditional AutoBlock** are unchanged.

## Tests and proof limits

- Java: `Minecraft189AutoClickerControllerLeaseTest` exercises A/B
  original UUID substitutions, one-shot consumption, reentry-like
  replacement, module epoch/enable/trigger policy, GUI and owner
  invalidation, failure with missing entity UUID, and cleanup.
- Python: `scripts/test_mojang189_v351_autoclicker_controller_intent.py`
  checks production wiring, *one* `customMcClickMouse` call, actual
  original controller argument UUID, all previous action and safety gates.
- Require exact-head Foundation Windows and Ubuntu, official Mojang 1.8.9
  binary and vanilla dedicated-server combat acceptance. A green source
  or pure Java test does not certify controller hits, hurt-time or damage.
- Preserve and report any V349/V350 native acceptance red; do not weaken
  the original oracle, merge to main or touch the parallel UX branch.
