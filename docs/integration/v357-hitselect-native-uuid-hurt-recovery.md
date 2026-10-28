# V357 — HitSelect native UUID hurt-recovery state machine

**Minecraft Java 1.8.9 / Combat.** Draft, stacked on V356 #861.
Do not merge unless exact-source Windows/Ubuntu Foundation and original
vanilla Minecraft controller/server acceptance pass.

## New module behavior

HitSelect previously had one final synthetic-click admission:
`targetHurtTime <= maxTargetHurtTicks`. V357 retains that historical
behavior **by default**, and adds two independently persisted controls:

- **Minimum Target Hurt Ticks**: integer 0–20, default 0. Valid original
  hurt window is `min <= originalHurtTime <= max`. A live `min > max`
  combination fails shut.
- **Require Observed Hurt Recovery**: checkbox, default **OFF**. When
  enabled, one scheduled click into a remote original UUID must witness
  an actual native hurt-time rise **above max**, followed by a new original
  sample within the configured ready window, before another scheduled
  click for that same UUID is admitted.

Each original UUID owns its own bounded recovery state:

```
READY -- final synthetic click admitted --> WAIT_FOR_HURT
WAIT_FOR_HURT -- genuine native hurt > max --> WAIT_FOR_READY
WAIT_FOR_READY -- genuine native min <= hurt <= max --> READY
```

The **16-entry** UUID map prevents briefly switching A→B→A from
rearming A on an old unchanged hurt snapshot. Original World index
reordering does not transfer one player's recovery state to another.
No scheduler-credit banking or delayed attack is introduced.
In-flight GUI/source revocation and module disable clear all recovery
states. Missing, duplicated, dead, local or mismatched original UUID
evidence cannot advance a state or authorize a synthetic attack.

## Original module coordination

The existing `Minecraft189HostRuntime.shouldAutoClickInternal` already
arbitrates **Aura > TriggerBot > AutoClicker** and enforces AntiBot,
TeamGuard, FriendGuard, WallCheck, AttackRange, Criticals and original
source/controller leases. V357:

1. **Observes** original loadedEntityList UUID/kind/combat snapshots
   on every indexed combat scheduler callback when the mode is enabled,
   even if Auto Clicker/Aura/Trigger does not schedule a click.
2. At final synthetic admission, checks the same real remote UUID,
   world/kind/combat list lengths, ready hurt window and a fresh
   `tickUuidWorld.customMcLoadedEntityUuids()` identity match.
3. Advances the chosen UUID to `WAIT_FOR_HURT` **only after all other
   cross-module gates** admit the click. There is no new direct
   `PlayerControllerMP` call or forged packet.

This records a *scheduled original synthetic click*, **not a
guaranteed server-damaging hit**. Original player controller and
server-authoritative damage remain separate acceptance tests.
No manual vanilla clickMouse event is intercepted; optional mode off
keeps historical behavior.

## Verification

- Java JUnit: `Minecraft189HitSelectRecoveryCycleTest` validates
  historical default, min/max bounds, genuine rise/fall state sequence,
  A/B/A UUID switching, invalid source evidence, GUI/World lifecycle,
  module disable and legacy index-only bypass denial.
- Source contract:
  `scripts/test_mojang189_v357_hitselect_uuid_recovery.py`.
- Required: exact SHA Foundation CI on Windows/Ubuntu, original Mojang
  binary preflight with real attack controller / dedicated server.
- Existing V47 rejoin failure is **not claimed fixed** by this module.

No HUD/ArrayList/ClickGUI files or `main` branch are modified.
