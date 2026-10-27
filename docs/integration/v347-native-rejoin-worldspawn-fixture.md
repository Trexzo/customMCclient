# V347 — Genuine vanilla dedicated-server rejoin spawn stability

**Draft; acceptance-fixture-only; stacked on V346 → V345 → V344.**

## Root cause from the red original run

The official original Minecraft 1.8.9 workflow for V344
[run #40480304670](https://github.com/Trexzo/customMCclient/actions/runs/40480304670)
reached the real 1.8.9 local multiplayer fixture but failed with:

```
CUSTOMMC_OFFICIAL_189_LOCAL_MULTIPLAYER_FAILED:
  original rejoin login outside 1024 server world
```

This is a **test-fixture world-spawn admission failure**; it does not certify
the GUI/combat modules or the native controller. The existing FLAT dedicated
server chooses a spawn independently per fresh world. A spawn at or beyond
the fixture's constrained 1024-cell world fails the existing genuine rejoin
geometry validation before the relevant UUID/controller assertions run.

## Change

For **only** `rejoin_target` / `rejoin_release` strict original test
scenarios, after the genuine dedicated server reports Ready but **before
any original Minecraft graphical client joins**, send:

```text
setworldspawn 0 4 0
```

through the vanilla server's actual console stdin, requiring its source
confirmation `Set world spawn to`. The game server (not the harness)
uses that World spawn for the next authentic joins. The test then
continues to derive CIHost and all other rejoin geometry from the **real
Minecraft dedicated-server login line**. A setworldspawn acknowledgement
alone cannot prove that the original players joined, loaded or attacked.

The 1024 world boundary and tests that reject out-of-range original spawn
coordinates are left in place; so are exact World source UUIDs, target
identity/hurt, original PlayerControllerMP, natural raycast, source movement,
physical stone-floor witnesses, and the independent vanilla-server damage
scoreboard. Other scaffold, combat and graphical scenarios keep their
existing native world generation.

## Verification

Require exact-head Windows/Ubuntu Foundation, official SHA-pinned Mojang
1.8.9 binary + dedicated-server native acceptance, original rejoin UUID,
hurt and scoreboard proof. Do not call an acknowledged spawn command PvP
success. No changes to production game Java, UI or main.
