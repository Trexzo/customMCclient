# V171 — real original Minecraft 1.8.9 Scaffold V29 survival support/air target fixture

## Direct evidence from a failing original client, no inferred success

The complete pinned Mojang Minecraft 1.8.9 graphical/integrated-server original [V169 workflow 39115611367](https://github.com/Trexzo/customMCclient/actions/runs/39115611367), at exact parent commit `ee5769f5197cc8b1ca85cf8fe3cbc53819e52795`, reported a **failed original V29 native survival controller placement step** after the 1.8.9 original Scaffold World, tick, context and candidate preflights passed. The actual original script error was **`CUSTOMMC_189_SINGLEPLAYER_WORLD_FAILED: no real enabled Scaffold module admitted vanilla click checkpoint before bounded deadline`**. The player had actual original source position repeatedly `x=1.05 y=100.0 z=0.5`, the specified narrow edge posture. Earlier native context and source ItemBlock parity were positive; there was *no* positive enabled-Scaffold module ownership/controller accepted=true/real WorldClient stone plus inventory debit marker. The failed test MUST stay failed; no synthetic placement claim.

Original V29 preparation used **fire-and-forget** `/setblock 0 99 0 stone` and `/setblock 1 99 0 air` immediately followed by other original commands. Those calls had no positively verified original-server changed-block acknowledgements. As a result, the test script did not prove that source support was stone and target cell was air when checking the real module click. That is an evidence gap, **not proof** that this caused the failure (native geometry, original ItemBlock selection, tick timing and controller reach could also be responsible).

## Exactly isolated source-fixture fix

New `prepare_verified_original_scaffold_survival_edge(process,log,window)` in `scripts/RunMojang189SingleplayerSmoke.py` applies **only** to `--native-scaffold-placement enabled/disabled` V29:

1. Genuine original X11-integrated-server player is first teleported to `0.5 120 0.5` above the support cell, in an already loaded original World.
2. Issue a real **fresh** vanilla `/fill 0 99 0 1 99 0 air` as sacrificial contrasting preclear; a new exact `[CHAT] No blocks filled` or a new strictly positive blocks-filled response is allowed here **only**, not as source placement or admission proof. Missing reply, invalid World coordinates, too many blocks or no valid blocks rejects.
3. Independently require `send_verified_fill` positive changed-cell original server reply for `/fill 0 99 0 1 99 0 stone`, which creates actual support+temporary target-solid after air, regardless of initial generated material.
4. Independently require a second **fresh and positive original server reply** for `/fill 1 99 0 1 99 0 air`, leaving actual stone at source (0,99,0) and actual air at target (1,99,0). This is World preparation, not a synthetic scaffold block placement.
5. Keep existing genuine original player narrow edge posture `/tp 1.05 100 0.5 90 88.6`, gamemode survival, real stable original source position ticks, vanilla `/give CIProbe stone 8`, and original `Minecraft.runTick` source controller/player/World/ItemBlock checks.

The previous **STRICT** enabled gates are unchanged: `SCAFFOLD_MODULE_OWNER_PASS` from real source, vanilla `onPlayerRightClick` original controller attempt, `accepted=true`, real original `WorldClient` block and one survival 8-item stack decrement. The **STRICT** disabled negative remains original-world air unchanged and stack remains eight, without enabled-controller admission. No player position, real item stack, target-hitbox, raycast, packet, inventory mutation, success marker, timer or deadline is fabricated or relaxed. No other Scaffold or Minecraft PvP mode is modified.

## New source-only regressions

`scripts/test_mojang189_scaffold_survival_edge.py` tests: exact positive/world no-op **preclear only** and mandatory order of two independent positive fills; actual 1.05 player AABB overlaps source support's x range without placing the target block; stale earlier vanilla chat ack cannot count as the new air preclear; invalid World commands fail before either positive fill; and the enabled module/controller/World+stack positive and disabled air+stack negative original source gate strings all remain present and ordered. Offline mocks are **not** genuine original-game placement proof.

## CI admission and no merge

Require **exact V171 SHA** Foundation on Ubuntu+Windows and entire pinned original Mojang 1.8.9 graphical X11 + dedicated server 78/78 native suite, including V29 original survival enabled/disabled, M405/M419 real B UUID/controller and independent positive B/zero A server damage, M425 genuine X11 melee ray/hurt, original WallCheck/Scaffold, AutoRod/AutoPot physical cast and potion Regeneration. If the old original V29 failure recurs despite positively confirmed World cells, retain the new native logs and investigate true module/candidate/eye/face. Never call one parent's game PASS certification for a child. Repeat same SHA after a first full original PASS for intermittent stability.

V171 cumulative Draft child of [V170 #636](https://github.com/Trexzo/customMCclient/pull/636), above V169 actual rod/pot production, no merges; `main` stays unchanged. Reliability issue #503 and module parity #537 stay open.
