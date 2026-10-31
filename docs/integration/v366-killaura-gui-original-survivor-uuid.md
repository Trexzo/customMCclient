# V366 — Preserve genuine KillAura survivor UUID through GUI focus, not World loss

Minecraft Java 1.8.9 · Combat KillAura native A/B rejoin · stacked draft on
V365 JumpReset PR #877. No HUD, ArrayList, ClickGUI visuals or main edits.

## Original-binary diagnosis

The exact-head V365 original Mojang binary preflight failed the inherited
V47 early A/B rejoin, original B-controller and authoritative dedicated-
server damage test. Its original evidence shows:

- Early original A was acquired, then real A disconnected.
- Living remote B was genuinely loaded, acquired and selected by the
  selector as its own original UUID.
- During repeated real GUI/source-pause cycles in phase 3,
  `Minecraft189KillAuraModule.suspend()` cleared the *same* B UUID with
  `host-suspend-gui-or-world` rather than cancelling only synthetic
  action/scheduling credit.
- Rejoined original A was physically nearer than survivor B. With B's
  lock erased, the normal nearest-player selector chose A. The genuine
  vanilla ray/index then pointed at A, not B. The strict original server
  damage gate (actual retained B target and fresh B controller/damage)
  correctly failed: no invented B success.

## Precise production change

- `Minecraft189KillAuraModule.suspendForGui()` resets *all* CPS phase,
  sampled CPS, min/max and cached target index, but retains only an
  already source-verified UUID when the opt-in UUID target lock is ON.
- The original rotation GUI-focus, synthetic click GUI-focus, and
  original vanilla GuiScreen veto now use this distinct GUI pause.
  Nothing clicks or rotates while any GUI has focus; all scheduler and
  manual-input vetoes remain.
- Actual original player disappearance, World identity replacement,
  null/malformed native World, revoked or inconsistent World combat
  source, module OFF, loss of lock setting or unapproved source owner
  still hard-clear the UUID.
- On focus return, the existing unmodified
  `Minecraft189KillAuraTargetSelector` must *freshly* resolve the UUID
  from the actual original World list, validate uniqueness, live player
  kind (never local), range, FOV, optional AntiBot/team/friend/wall
  policies, and remap the genuine current player index. A closer newly
  rejoined A cannot steal a retained, eligible B solely because GUI
  focus temporarily interrupted automatic melee. An unloaded, dead,
  out-of-range or policy-blocked B cannot be retained.

## Evidence

- `Minecraft189KillAuraModuleTest`: GUI pause preserves original UUID
  but clears aim/CPS; original owner-loss and module OFF still erase it.
- `Minecraft189KillAuraTargetSelectorTest`: survivor B alone, GUI
  pause, original A rejoins closer at a new World index, reselect B
  by UUID; genuine B unload then legitimately reselect A; missing
  original identities fail closed.
- `scripts/test_mojang189_v366_killaura_gui_uuid_lease.py`:
  three GUI fences, hard source revocations, unchanged source selector
  filters and strict real-server V47 oracle.

Exact-head Foundation Windows and Ubuntu and genuine original Mojang
binary acceptance are required. In particular, V47 must still witness
actual original B native PlayerControllerMP `attackEntity` on its
source UUID and authoritative fresh dedicated-server B damage, with
stale A veto. A green offline or GUI-only test is NOT that evidence.

**Draft only, no merge.** If V47 remains red, preserve the failure and
diagnose its actual remaining source, geometry or controller context.
