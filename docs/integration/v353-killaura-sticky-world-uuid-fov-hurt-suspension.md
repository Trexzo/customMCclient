# V353 — KillAura source UUID lock survives temporary FOV/hurt veto

Minecraft Java 1.8.9 · source-mapped combat ownership · draft stacked after V352.

## Reproduced original failure and code-path diagnosis

The V350 official Mojang 1.8.9 preflight crash report, inside the
three-real-client dedicated-server acceptance, had:

`rejoin stale or wrong UUID attack phase=3 actual=A locked=A
Aindex=6 Bindex=1 da=1.53125 db=2.23606797749979 selectedA=true selectedB=false`.

The real A had rejoined and become closer while real B remained loaded.
The acceptance-only V352 veto correctly denies invalid phase-3 A actions,
but cannot fix the source selection cause by itself.

Source review of the production KillAura found two possible lock-loss
paths during legitimate original callbacks:

1. `Minecraft189KillAuraTargetSelector` previously considered the
   locked UUID only a **score preference after FOV/hurt filtering**.
   An existing B outside configured FOV or above hurt-time threshold was
   filtered out, allowing the just-rejoined nearer A to replace B.
2. `Minecraft189KillAuraModule.aim` called `clear()` when its source
   target was temporarily unavailable or outside FOV; `clear()`
   deleted the confirmed UUID and CPS state together.

## State-machine change

When `lockTargetWhileEligible` is enabled and the original-source
loaded World UUID of B is still a **unique remote living player**,
within the configured **range**, and permitted by the existing AntiBot,
TeamGuard, FriendGuard and WallCheck policies:

- Reserve B as the only target candidate. The selection must still
  pass **FOV**, **hurt-time**, and every previous candidate filter
  before it can publish a target/rotation or click.
- If B is outside the configured FOV or temporarily hurt, the selector
  publishes **found=false**. `aim()` resets CPS/previous index but
  retains only the original B UUID. No rotation setter or native
  synthetic melee is issued.
- If B becomes freshly eligible again, its source UUID is mapped to
  its *current* loadedEntityList index. A freshly rejoined closer A
  cannot substitute for the same UUID B.
- If B is genuinely unloaded/dead/out of range or blocked by the
  original safety policies, release its UUID and allow the normal
  eligible-player scan to select another target. A missing, malformed,
  duplicated or revoked identity snapshot cannot authorize a click.
- Explicit GUI suspension, module disable, original player/source
  invalidation, World revocation, dimension/runtime close still clear
  the UUID. Existing native final crosshair UUID/raycast, HitSelect,
  GUI, item-slot, cycle and original controller owner gates remain.

This is a **lock-when-source-valid** refinement, not a FOV or wall
bypass. A manually rotated camera outside FOV may temporarily
leave the module idle until the original target re-enters FOV or
becomes ineligible under the hard target gates.

## Proof and integration

- JUnit `Minecraft189KillAuraStickySourceOwnerTest`: an original
  A rejoin closer than B, B's entity-index move, temporary B FOV veto,
  B hurt-time veto, B's positive recovery, real B unload/out-of-range,
  and explicit module/source revocation.
- Python `scripts/test_mojang189_v353_killaura_sticky_uuid_owner.py`:
  source policy gates and original combat-controller/server oracles.
- Foundation Windows/Ubuntu and the strict actual Mojang 1.8.9 native
  three-client / vanilla-server acceptance are both required
  **on the exact PR head commit**, not on an ancestor.
- V350's original binary failure remains evidence; pure/source tests
  cannot certify native selection, real original hurt or server damage.
  Do not manufacture a PASS, loosen FOV/range, or merge before evidence.

Parallel ClickGUI/HUD/ArrayList UX PRs and `main` are untouched.
