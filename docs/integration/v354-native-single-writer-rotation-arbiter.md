# V354 — one native rotation writer per original Minecraft callback

Minecraft Java 1.8.9 · Combat rotation arbitration · GitHub-only draft.

## Problem

The existing host orders modules **Spin → KillAura → Aim Assist →
Jitter** and already pins the original EntityPlayerSP source via
`Minecraft189RotationHostSourceLease`. That source lease rejects changed
original players and GUI revocation. However the host's fallback logic
previously trusted module `apply()` **return values** when deciding whether
to invoke a lower-priority rotation module.

A real original yaw setter can reenter a module (ON→OFF/policy change)
after already writing native yaw, causing that module to return `false`.
This does not mean the callback was a no-op. Before V354, another module
could receive the same original pre-write yaw/pitch snapshot and attempt
a conflicting native pitch/yaw setter in that same callback.

## New native rotation sub-state

`Minecraft189RotationAxisArbiter` tracks a separate callback-scoped
writer, with owners `SPIN`, `KILL_AURA`, `AIM_ASSIST`, `JITTER`:

```
IDLE (enter original host)
  -- first native yaw OR pitch setter attempt --> OWNED(module)
OWNED(module)
  -- same module second axis --> OWNED(module)
  -- any different module attempt --> DENY
  -- finally / GUI loss / original source teardown --> REVOKED
REVOKED
  -- stale wrapper from prior callback --> DENY
next real host callback starts a new generation at IDLE
```

- The owner claim happens **before** invoking the original native
  setter. Reentrant or throwing setters cannot accidentally open a
  second module lane.
- A module that only reads a source angle or performs no setter may
  still yield to the existing lower-priority module, with no invented
  input or rotation.
- Four module-specific guarded controls wrap the same validated
  original source control. They never replace the underlying Mojang
  setter; original native yaw/pitch are still authoritative.
- An independent monotonic **generation** rejects stale module control
  objects from an earlier original host callback, even if another
  source has now entered.
- The host's existing conditions and **manual-input activation flags**
  (e.g. Aim Assist requireHold) remain. This PR does not claim to read
  raw mouse deltas or to implement unproven manual-input suppression.
- No changes to vanilla player position, target selection, combat
  owner/raycast/click, original controller, send packets or server
  damage. Native GUI and source lease remain mandatory.

## Tests and evidence requirements

- JUnit `Minecraft189RotationAxisArbiterTest`: exclusive two-axis
  ownership; reentrant native setter callback; no-op fallback;
  previous-generation control veto; thrown native setter still owned.
- Python `scripts/test_mojang189_v354_native_rotation_axis_arbiter.py`:
  validates module-specific wiring, unchanged priority, original
  source/GUI fencing and unchanged native melee controller.
- Exact-head Windows and Ubuntu Foundation CI and official Mojang
  Minecraft 1.8.9 binary preflight must pass before merging. Existing
  native rejoin target/UUID and server-owned damage failures remain
  visible and unresolved until separately proven.
- `main` and all parallel UI/HUD/ClickGUI/ArrayList PRs are untouched.

This ensures **single module rotation ownership per original native
callback**, not a claimed live mouse-controller or PvP certification.
