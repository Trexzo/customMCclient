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

## Second original-game failure — focus/pointer now isolated

Native job [#41058470436](https://github.com/Trexzo/customMCclient/actions/runs/41058470436) **also correctly failed** at `no fresh real Minecraft source witness: native enabled array list`. Its original X11 artifact `12978541837` proves the vanilla pause overlay was genuinely dismissed (a new `CUSTOMMC_189_S31_REAL_VANILLA_PAUSE_DISMISSED=YES` result, real blue-sky `world-before.png`, unobstructed `native-modules.png` with the source Graphite page). That is progress, **not a passing native click**.

The actual physical module search/row input may be obstructed by a still-grabbed 1.8.9 client cursor; earlier separate S26 [#863](https://github.com/Trexzo/customMCclient/pull/863) native source focus logs reported `Mouse.grabbed=true` and failed physical module-detail navigation even after the exact original focus release attempt. Do not merge S26 code blindly into this independent stacked branch. S31 now captures `filtered-arraylist.png` and `clicked-arraylist.png` (and Target HUD equivalents if the first toggle succeeds) in the *actual original X11 window*, before insisting on the unchanged fresh render witness. Per-action failures terminate after eight seconds, retaining original screenshots and RED test outcome. This distinguishes original search-text input from module-row clicks without spoofing event dispatch or granting focus authority.

## Third real X11 failure and independent source fixes

Official [native run #41059457312](https://github.com/Trexzo/customMCclient/actions/runs/41059457312) failed at the same strict *no fresh genuine enabled ArrayList* witness; its original screenshot archive **12979230060** includes real `filtered-arraylist.png` and `clicked-arraylist.png`. They prove **the Search modules placeholder never changed and the visual rows remained disabled** after physical X11 actions. That is a native input/coordinate issue, not evidence the HUD renderer is drawing a fake target.

Two separate confirmed causes were then addressed, without bypasses:

1. The existing `Minecraft189InputHooks` deliberately forbids physical GUI edits while `Mouse.isGrabbed=true` or original `ave.w` reports gameplay focus. S26's independently developed original-game native focus lease provided the exact verified path through original `ave.o() / ave.n()`. S31 ports its **strict** focus decision, verified native post-release, native mouse gate unchanged, and weak original runTick `this` provenance (the overlay class loader's singleton can otherwise be NULL). No invented focus or JVM mouse-state override. Original S26 itself previously did **not** complete the physical module-detail acceptance; S31 must independently test it, and will not call it solved until that succeeds.
2. The old S31 test hitboxes treated screenshot framebuffer dimensions as logical GUI dimensions, placing row clicks around X259,Y133. But the actual source GUI is rendered using a nontrivial `UiViewport.scale()` (in screenshots, the Combat row is visibly at Y~165+). S31 now emits the original render-pass `pixelWidth×pixelHeight,scale` under the opt-in evidence flag, requires **one fresh** exact-framebuffer viewport reading, and calculates X11 hitboxes by applying the original source logical-to-physical scale. Width/height mismatch, missing/duplicate source scale and invalid scale fail closed; no hard-coded 4/3 assumption.

S31 now also prints opt-in genuine LWJGL pointer-admission and handled-event diagnostics. They do not synthesize mouse events, change module selection or weaken `Minecraft189OverlayFocusGuardian.permitsProductionMouseInput`. New negative tests cover original false tick inputs, exact vanilla focus methods, physically captured/unknown cursor rejection and original scale-specific X11 hitbox regression.

## Native screenshot #41075240124 — real search works; second click denied

The source-confirmed exact S31 run [#41075240124](https://github.com/Trexzo/customMCclient/actions/runs/41075240124) preserved GREEN Windows/Ubuntu Foundation and reached the original two-player X11 stage. The real `filtered-arraylist.png` in artifact **12987047613** shows exactly **one** filtered Visuals entry, `Array List`, with the real search query `array`. That disproves the earlier mistaken belief that keyboard search was broken: the previous `array list` query had contained T, allowing vanilla chat to close the overlay. The safe query succeeds.

The same JVM's original pointer diagnostics show **first search click** `nativeAllowed:true` and `handled:true`, but **second row click** `nativeAllowed:false` even though the GUI was still visible and the row was correctly positioned at original UI scale `2.0` (X348,Y214). This is a genuine native cursor ownership/re-capture problem, not an enabled HUD visual success.

The strict S31 screenshot harness now performs an additional pair of **physical RightShift toggles** after search, waiting for the original GUI transition to close then reopen and requiring a new exact `CUSTOMMC_189_NATIVE_GUI_CURSOR_RELEASE=YES` or `CURSOR_READY=YES` marker from the native Mojang focus lease before clicking the row. The actual search remains persisted by `ClickGuiModel.close()/toggle()`. This *tests* the source HUD independently of incidental vanilla cursor re-capture; it does **not** claim that production GUI multi-click focus is fully repaired. A separate focused UX fix may be required once actual mouse ownership diagnostics are verified. No mouse gate bypass or direct controller enable is permitted.

## What this does NOT certify
A source marker plus a screenshot is not necessarily perfect pixel-level OCR of every rendered HUD word. The proof is the genuine remote player, actual module lifecycle via real UI, source-native completed frames and original rendered X11 pixels, not an inferred health or a synthetic frame. Visual layout still needs human review of `enabled-hud.png` for typography, clipping, placement and contrast. No automated anti-cheat or combat bypass claim is made.

The CI `S31` stage is independent of the existing S22 singleplayer screenshot, is fail-closed on a missing module/witness, and archives all evidence on failure. `scripts/test_mojang189_s31_hud_visual.py` covers original 854x480 and 1280x720 physical hitbox calculations and rejects stale log markers. Old native tests remain intact. Do not merge if CI is red or screenshots fail review. Reference direction remains user Library Slinky/Rise/Vape comparison research (text-first, subdued Graphite), without claiming that proprietary UI source/assets were retrieved.
