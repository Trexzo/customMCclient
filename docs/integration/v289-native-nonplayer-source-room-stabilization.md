# V289 — source-verified original nonplayer negative fixture room

Stack parent: V288 Draft PR #759, exact immutable SHA `4768e7186f23d9a7e0e21f76a79718d5a383e5de`.

## Actual native failure and independent working comparator

Original Minecraft 1.8.9/X11 run V286 [#39625775380](https://github.com/Trexzo/customMCclient/actions/runs/39625775380) **FAILED** after 64 prior source-native stages at `Verify real player-only Kill Aura excludes passive native mob` with `no disabled Kill Aura nonplayer negative control checkpoint before bounded deadline`. The original native GUI/player/world worked; commands `/fill`, `/tp` and `/summon Pig` were sent and the Pig summon was acknowledged, but **20 consecutive original living-nonplayer raycast callbacks were NOT observed** before timeout. This is not evidence of KillAura improperly selecting a mob: the module did not even reach the enabled negative-control stage. Source-native ray geometry, World/kind alignment and timing were not previously diagnosed.

Read-only audit found a real **fixture authority discrepancy**: V286 nonplayer test used **unchecked relative** `/fill ~...`, `/tp ~ ~ ~ 0 26` and `/summon Pig ~ ~ ~2` based on a random spawn. By contrast, the original manual melee test runs in a fixed positive-server-verified, single-chunk stone chamber, in the same exact signed Minecraft binary, and has reached real native ray/hit server proofs on multiple certified runs.

## Bounded actual runner repair (no gameplay production changes)

**Only** in `scripts/RunMojang189SingleplayerSmoke.py`'s original `kill_aura_nonplayer` scenario, create a *separate explicit branch* that uses the known working and server-witnessed original manual melee chamber:

1. Native original `/tp CIProbe 8 200 8` (chunk 0,0) **requires a fresh source-integrated-server chat acknowledgment**.
2. Native original `/fill 8 119 8 8 119 8 stone` **requires a fresh positive actual blocks-filled reply**.
3. Native original `/tp CIProbe 8 120 8` **requires a fresh matching chat acknowledgment**, leaving real player above the chamber's future stone fill and supported before any room construction.
4. In that exact loaded chunk, native original `/fill 4 99 4 12 104 13 stone` and `/fill 5 100 5 11 103 12 air` each **require a new positive original /fill receipt**. Nothing is edited in synthetic World arrays.
5. Native original `/tp CIProbe 8 100 8 0 26` **requires a new exact matching chat reply**, with original player in a closed real chamber, aimed down the +Z direction.
6. Native original `/summon Pig 8.5 100 10.5 {NoAI:1b}` **requires a fresh authentic successful summon reply**, placing the actual stationary Pig roughly two blocks in front.

The distinct original **manual melee** branch is unchanged; AutoClicker's separate original relative fixture is unchanged. There are **no player or Pig position/health/raycast edits**, no direct native controller call, no synthetic entity, no new mouse input, no timeout enlargement and no relaxed target/FOV/range or original acceptance logic. These explicit source receipts establish chunk/fill/teleport/summon *setup*, **not** successful crosshair raycast, 20/80 module-veto callbacks, native click, or server damage. The existing exact `AURA_DISABLED`, `AURA_ENABLED` and `AURA_VETO` sequential genuine original marker gates remain mandatory. V288's opt-in native raycast reject reason logging remains intact and may diagnose any remaining original problem.

Six new Python AST/source regressions assert scenario isolation (manual/AutoClicker unchanged), exact original witness calls and ordered commands, safe room geometry, no unverified commands, unchanged 20+80 native negative gate ordering and explicit no forged success. The tests are *not* native Mojang evidence.

## Certification policy

V289 cannot be called a gameplay fix or real raycast fix without actual pinned original acceptance. Native historical V286 V65, V283 second-run V29 Scaffold, V282 V46 FOV and V276/V277 UUID-phase failures remain *genuine failed runs* under issue #503, regardless of future successes.

Require **both OS Foundation Windows/Ubuntu 10/10 and two independent complete original Mojang Minecraft 1.8.9 X11 client + vanilla dedicated-server 78/78 runs on the same exact SHA** before strict certification. All stacked pull requests stay Draft; `main` remains untouched, no merge/release.
