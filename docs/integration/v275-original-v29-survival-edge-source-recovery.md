# V275 — original Mojang V29 verified Survival grounding and one source fall recovery

**Parent:** V274 Draft PR #745 exact SHA `7383d5b4e3676a7678ad5db6c674148ad3efb615`.

## Genuine failed original
V274 original Mojang 1.8.9 X11 graphical client, integrated singleplayer original Survival placement step **V29 FAILED** after 34 successful steps in [run #39596576163](https://github.com/Trexzo/customMCclient/actions/runs/39596576163), job `119210258838`:
`CUSTOMMC_189_SINGLEPLAYER_WORLD_FAILED: original Survival player not grounded on real support nativeLast=[(1.699999988079071, 99.0, 0.5), ...] realCorrection=False`.

This is not a failure of the two-player B priority diagnostic, and V274 remains native-uncertified despite Foundation Windows/Ubuntu 10/10 PASS. The mapped actual Minecraft player stabilized over the **target AIR cell** at x≈1.7,y=99.0 instead of x≈1.05,y=100.0 on the real STONE-support edge. Original V29 harness had issued the final `/tp` **before** `/gamemode 0` using fire-and-forget vanilla chat and only corrected a different drift case (x<1.3 at y=100). A fresh native source sample must not be inferred from either chat command; underlying reason for this displacement is not fully proven.

## Strict source-fixture correction
1. After the existing *genuine positive changed World fills* for stone support x0,y99 and air target x1,y99, issue a real physical X11 `/gamemode 0`, **await a fresh original integrated-server chat response**, then a genuine final `/tp CIProbe 1.05 100 0.5 90 88.6` **awaiting its new exact original server chat acknowledgement**. This ensures final teleport is the last intentional original server state change before strict native position proof.
2. The existing 12-second deadline and requirement of **3 source-native local player x≈1.05 y≈100 z≈0.5 samples** are unchanged. Additional narrow predicate may request **at most one** re-teleport ONLY if 3 actual original source samples show a stable displacement into the adjacent AIR cell (1.3<x<2, y≈99, original z within cell), matching this real crash. It does **not** pass V29 or override a missing World, and the real teleport must have a FRESH native game chat acknowledgement.
3. All genuine original Scaffold `ItemBlock`, module-on, `onPlayerRightClick`, native `accepted=true`, changed source WorldClient stone target and survival stack debit, and module-disabled negative checks remain mandatory afterward. No synthetic stone, item, controller click, hit, player-coordinate update, broadened acceptance position, reused log marker, timeout increase or acceptance bypass.

7 Python regression tests cover native original fall classification, moving/unrelated/short samples, exactly one true vanilla correction followed by three real native feet samples, correction still failing when feet never recover, fresh command ordering and unchanged exact sample criterion.

**Status:** test-fixture hardening only, not a confirmed real original root-cause fix. Require exact new SHA Foundation both OS 10/10, first FULL original pinned Mojang Minecraft 1.8.9 X11 and vanilla dedicated-server 78/78, and SECOND independent identical SHA FULL original 78/78. Preserve earlier actual V274 V29 FAIL and original V272 simultaneous crash. All PRs Draft, main untouched.
