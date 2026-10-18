# V172 — actual original Mojang 1.8.9 V19 native raytrace stone support verification

## Evidence from the immutable second V168 native binary run

The true official pinned original Minecraft 1.8.9 X11 graphical/integrated-server [V168 workflow 39114922799 attempt 2](https://github.com/Trexzo/customMCclient/actions/runs/39114922799) on the immutable source `ff3001c1956bc1309b109916f103ce9003b6c56e` **FAILED** at V19 after 46 successful GitHub steps. Actual original source player remained stably at `x=0.5, y=100.0, z=0.5`; the mandatory original marker from `World.rayTraceBlocks` **never** reported hitting stone support at the correct UP face within the original bounded deadline. The exact harness exception was `no genuine World.rayTraceBlocks hits original stone UP face checkpoint before bounded deadline`. First V168 run failed a later original *negative Aura-disabled 20 consecutive far-player callbacks* check while true original World data showed seven blocks separation. This is not evidence of a Java rod/pot cooldown fault, nor proof that the native ray logic itself is wrong.

At the V19 source fixture, the script did an unacknowledged `/setblock 0 99 0 stone` before `/tp 0.5 100 0.5 0 0` and immediately awaited the genuine original wrong-face/wrong-block negative and UP-face positive ray markers. Player source position does **not** establish that the original vanilla integrated server actually changed the support block from air to stone. This is a **test fixture evidence gap**; it is not proof that the real reason for missed raycast was incorrect stone, geometry/eye movement and source timing still require original full run.

## Isolated source World preparation

In cumulative V172, [Draft child of V171 #637](https://github.com/Trexzo/customMCclient/pull/637), new `prepare_verified_original_scaffold_raytrace_support(process,log,window)` is used **only** by V19 `--native-scaffold-raytrace` in `scripts/RunMojang189SingleplayerSmoke.py`.

- After original source player has genuinely entered loaded chunk (0,0) above the required cell at y=120, issue a real original integrated server `/fill 0 99 0 0 99 0 air` sacrificial contrasting preclear. It needs an actual new log response, either native `[CHAT] No blocks filled` when already air or native positive changed-block count, no other reply is allowed; invalid/out-of-world/rejected original World operations fail closed. This preclear **is not a gameplay/raycast PASS**.
- Next issue the genuine original `/fill 0 99 0 0 99 0 stone` through the existing **strict `send_verified_fill`**. Unlike the preclear, it MUST acknowledge a fresh positive changed-block response. If source World remains unchanged, test fails rather than inferring a stone existed.
- Original source player then returns to the **unchanged** `/tp 0.5 100 0.5 0 0` and the test still requires the same genuine `CUSTOMMC_OFFICIAL_189_SCAFFOLD_RAYTRACE_WRONG_FACE_BLOCK_VETO_PASS=YES` and separately `CUSTOMMC_OFFICIAL_189_SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES`, produced only by original mapped `World.rayTraceBlocks` on real stone in the real game. Do not fake hit coordinates, support collision, source eye, orientation, or substitute fill commands for ray acceptance.
- No Java gameplay, ItemStack, native controller, mouse inputs, target selection, server damages, test timeout, render settings, GUI veto, or other separate V171 survival edge/candidate/Scaffold fixtures changed.

## Source-only regression checks

Five new Python regression cases `scripts/test_mojang189_scaffold_raytrace_support.py`: accepted fresh original preclear replies (changed/acknowledged already-air) still require the same positive stone command; source helper cannot emit ray PASS; old stored log reply is not a current World acknowledgment; invalid/outside-World replies do not proceed to positive stone; actual original wrong-hit veto and genuine UP-face ray accepted marker remain demanded afterward. The independent prior original potion dry-room test's source-function scope was updated because adding a new helper before V171's helper broadened its old text slice; this **only** corrects the regression test's independent source analysis, not the original dry room or its positive fill requirement.

These mocks prove control-flow policy only. They are not original Mojang world/ray observations.

## Exact native certification

Require V172 exact immutable source SHA Foundation CI Ubuntu+Windows, full pinned Mojang Minecraft 1.8.9 native original GUI + integrated and dedicated server suite **78/78**, including V19 real negative and UP ray, V29 V171 real Scaffold enabled/disabled controller block+stack, M405/M419 locked B UUID with actual original source ray/controller and authoritative positive B/zero A damage, M425 original physical X11 ray and hurt, original GUI/Scaffold/Wall/rod/potion controls, and genuine original seven-block Aura negative. Repeat a fully successful pinned 78/78 run unchanged SHA independently to document flaky source-world tests.

The immutable V162 `66db7002abadb07207fbdfbf07171311bf494828` had two complete original 78/78 runs on the same commit; those cannot certify V172. Other original failed runs remain recorded, all PRs cumulative Draft and `main` unchanged; reliability #503, real-module parity #537 remain open.
