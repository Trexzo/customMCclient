# UX S31 — exact original Mojang two-player HUD visual proof

## Goal
Close the genuine visual-evidence gap from the S29 ArrayList and S30 Target HUD source refactors. Their complete original Minecraft 1.8.9 native suites and Foundation suites passed, but the archived S22 screenshot showed ClickGUI only, not the two **enabled** in-world HUD widgets with real players.

## Test provenance and no-bypass constraints
This S31 PR stacks on S30 #873 (exact head `b242563e4a681bbb80e2369e8497a24506476766`) and changes ONLY the opt-in acceptance harness, original workflow, and **read-only** HUD source render witnesses. Combat, players, target identity, controller state machine, input mapping, graphics/font engines, profile storage and `main` are untouched.

The new `S31` original-game CI stage creates:
- One actual Mojang 1.8.9 dedicated server on **127.0.0.1** with normal World/player packets and no public RCON.
- Two genuine original Mojang graphical Minecraft 1.8.9 clients (`CIHost` and `CITarget`) with distinct identities. Original host `CITarget` must have existed in the loaded original World for 20 ticks before capture.
- Actual dedicated-server teleport of both genuine players, no artificial player entries/target snapshots or direct `ModuleController` calls.
- A uniquely identified original host X11 window before the second game launches; actual right-Shift GUI opening, actual native search text input, actual original module row left clicks to enable **Array List** then **Target HUD**, then actual physical GUI close.
- Original-game `world-before.png`, `native-modules.png` and `enabled-hud.png` captured from the exact host X11 window by `mss`.
- A fail-closed pixel gate: the GUI frame must gain source Graphite pixels relative to the genuine world and the final HUD screenshot must lose the large GUI panel. Identical before/after PNG bytes fail, as do changed dimensions.
- Opt-in exact original **native render pass** markers `CUSTOMMC_189_REAL_ARRAYLIST_RENDERED=YES` and `CUSTOMMC_189_REAL_TARGET_HUD_RENDERED=YES index=... distanceSquared=...`; the latter only appears after a real currently matched nearest remote player/target-angle snapshot rendered successfully. Both markers require *fresh log bytes after each real module click* and cannot be satisfied by old buffered output.
- The final `CUSTOMMC_189_S31_TWO_REAL_CLIENTS_ARRAYLIST_TARGETHUD_PASS=YES` is printed ONLY after those source and pixel conditions. No synthetic canvas, invisible debug client or bypass of the existing native admission tests.

All source markers are strictly behind `-Dcustommc.acceptance.reportNativeHudVisual=true` and only emit **after** a real render pass has completed. They do not enable or edit any modules or change target selection. Normal production behavior has no new UI log.

## First authentic failure and its bounded repair

Native job [#41057213142](https://github.com/Trexzo/customMCclient/actions/runs/41057213142) failed **correctly** because no fresh original enabled ArrayList HUD witness followed the physical click. The always-uploaded artifact `12977467355` contains two *genuine original X11* images. They independently prove the root cause: `world-before.png` showed the **vanilla Game Menu** still open; `native-modules.png` showed CustomMC's real Graphite Modules page rendered **behind** that same vanilla menu, which intercepted the row clicks. The source login/remote-player admission was fine; this was not evidence of a disabled renderer or broken combat.

The subsequent fix is strictly to the screenshot harness: read four separate original X11 pixels in the **actual Game Menu button surfaces**; if and only if all four match the classic vanilla neutral-grey buttons, physically click the original Back to Game button. Reject if the menu still appears, if it reappears over ClickGUI or if it remains at final HUD capture. No direct screen mutation, no blind Escape, no fabricated render, and no relaxation of the fresh ArrayList/Target HUD witness gate. `test_mojang189_s31_hud_visual.py` checks four-button positive/negative cases.

## What this does NOT certify
A source marker plus a screenshot is not necessarily perfect pixel-level OCR of every rendered HUD word. The proof is the genuine remote player, actual module lifecycle via real UI, source-native completed frames and original rendered X11 pixels, not an inferred health or a synthetic frame. Visual layout still needs human review of `enabled-hud.png` for typography, clipping, placement and contrast. No automated anti-cheat or combat bypass claim is made.

The CI `S31` stage is independent of the existing S22 singleplayer screenshot, is fail-closed on a missing module/witness, and archives all evidence on failure. `scripts/test_mojang189_s31_hud_visual.py` covers original 854x480 and 1280x720 physical hitbox calculations and rejects stale log markers. Old native tests remain intact. Do not merge if CI is red or screenshots fail review. Reference direction remains user Library Slinky/Rise/Vape comparison research (text-first, subdued Graphite), without claiming that proprietary UI source/assets were retrieved.
