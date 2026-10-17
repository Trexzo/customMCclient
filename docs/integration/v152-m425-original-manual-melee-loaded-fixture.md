# V152 — original manual X11 melee: loaded single-chunk source fixture

## Unmodified original 1.8.9 evidence

The **exact same V150 SHA** `89d45bba33aa8ffdd05e08a51b360c0f73550bc9` had one full [official original Mojang success (attempt 1)](https://github.com/Trexzo/customMCclient/actions/runs/39053329820) with all 78 job steps passing. Its deliberately independent second original client run, on the unchanged SHA, passed 60 steps but failed pre-existing M425 real vanilla manual melee at `CUSTOMMC_189_SINGLEPLAYER_WORLD_FAILED: no actual raycast entity under vanilla left click checkpoint before bounded deadline`.

The failing graphical test visibly opened/closed genuine 1.8.9 chat, and the actual integrated server returned the vanilla accepted `57 blocks filled`, `42 blocks filled`, `Teleported CIProbe...`, `Object successfully summoned`. Nonetheless, it failed before any true source ray-hit arm witness; that single log does **not** tell us whether the underlying reason was player/pig world geometry, entity update timing, X11 mouse focus, a stale camera or other scheduling. This is an **intermittent genuine acceptance fixture** investigation tracked in [#503](https://github.com/Trexzo/customMCclient/issues/503), not proven new source gameplay breakage.

## Strict new original-world preparation

Instead of relative `/fill ~...` and `/summon Pig ~...` at random spawn coordinates, the **manual** `--attack` variant now:

1. Sends a real X11 `/tp CIProbe 8 100 8`, and waits for a **fresh** genuine integrated-server chat reply confirming x=8.5, y=100, z=8.5 plus one tick of chunk load.
2. Sends genuine `/fill 4 99 4 12 104 13 stone` and `/fill 5 100 5 11 103 12 air`; each must have a **fresh positive** original 1.8.9 blocks-filled response. This builds a bounded single-chunk floor/ceiling/side enclosure rather than relying on ungenerated ground or unrelated loaded chunks. No fabricated World mutations.
3. Sends a second real X11 `/tp CIProbe 8 100 8 0 26` and requires a **new, precise original client teleport reply** after that command, retaining the existing native manual 0-yaw, 26-pitch orientation.
4. Sends `/summon Pig 8.5 100 10.5 {NoAI:1b}` in the same chamber and requires an original chat `Object successfully summoned` **from that command**, not a stale earlier line.
5. Prints `CUSTOMMC_189_MANUAL_MELEE_FIXED_NATIVE_ROOM_PASS=YES` **only as a room/summon diagnostic**. This marker MUST NOT substitute for the existing `CUSTOMMC_189_VANILLA_MELEE_TARGET_ARMED=YES`, real vanilla `EntityLivingBase.hurtTime` proof, or unscoped native `PlayerControllerMP.attackEntity` UUID witness. The original **12 actual X11 left mouse clicks** and strictly bounded native checks are unchanged.

The other original nonplayer KillAura and AutoClicker modes remain unchanged, avoiding collateral fixture expansion.

## Offline regressions and certification

New `scripts/test_mojang189_manual_melee_room.py` checks fresh verified Pig summon, previously successful logs not authorizing a later failed command, exact target teleport source reply not satisfied by unrelated coordinates, closed source chunk geometry, command order and preservation of the REAL M425 native arm/hurt/UUID gates. Mocked source log fixtures prove Python policy only; **only GitHub Actions original Mojang binary** confirms real gameplay.

This V152 branch is a **Draft cumulative child of V151 #617** at `087293e99da1fe6e532ad0155a5345de6371fec6`. Require both Windows/Ubuntu Foundation and **entire original GUI/dedicated server suite PASS** on V152's exact new commit, then an independent repeat on that same SHA before suggesting improved reliability. First V150 success remains valid evidence; second failure is never overwritten, deleted or falsely marked PASS. No merges/main changes.
