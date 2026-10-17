# V158 — deterministic *original* Scaffold runTick source grounding

## Logged native CI evidence

The unchanged-source official V155 original Mojang GUI binary run [39072052514](https://github.com/Trexzo/customMCclient/actions/runs/39072052514) failed the V23 original Scaffold context step after ~32 successful job steps with `CUSTOMMC_189_SINGLEPLAYER_WORLD_FAILED: native original Scaffold context player not grounded`. It reported repeated **genuine original source** positions x=-0.409576..., y=100, z=0.5 despite test logic assuming x=0.5 after unacknowledged `/setblock 0 99 0 stone` and `/tp 0.5 100 0.5`. The older fixture was at the chunk edge. These logs do not prove whether the position mismatch was neighboring terrain collision, teleport timing, incomplete chunk/world preparation or X11/game scheduling. The issue is tracked as intermittent original-world fixture evidence in #503, not proven native Scaffold algorithm failure. An unrelated V154 run exited early in the M405 remote-player phase (status 255); this change does **not** purport to fix that.

The original exact V156 parent [39072406473](https://github.com/Trexzo/customMCclient/actions/runs/39072406473) had fully **PASS 78/78**, including old original Scaffold V23. V157 exact Foundation Windows/Ubuntu is **SUCCESS**. V158 is a separate Draft stacked child of V157; green parent CI never substitutes for the new original game test.

## Genuine original vanilla fixture, no fabricated game state

For **only** `--native-scaffold-context`:

1. Original X11 vanilla chat teleports genuine local `CIProbe` player to **(2.5,120,2.5)** via `/tp CIProbe 2 120 2 0 0`, requiring the exact **fresh original server [CHAT] Teleported...** reply, inside chunk (0,0), away from chunk edges and above the future floor.
2. Actual integrated-server `/fill 0 99 0 4 104 4 stone` constructs a single-chunk stone cell, requiring a fresh **positive original blocks-filled** reply.
3. Actual integrated-server `/fill 0 100 0 4 104 4 air` carves it, also requiring a fresh **positive original blocks-filled** reply; leaves a real stone y=99 support floor below.
4. Original X11 `/tp CIProbe 2 100 2 0 0` with exact new server confirmation places the original player at (2.5,100,2.5) safely above that real floor.
5. A bounded poll of source-mapped `CUSTOMMC_189_NATIVE_LOCAL_POS` logs must confirm **three consecutive actual source player updates**, each x/z within .15 of 2.5, feet within .15 of 100.0 and coordinate drift <=.03. No artificial player onGround state or fabricated position data is supplied.

The original `/give CIProbe stone 8` still **must** deliver a real vanilla ItemBlock after grounding; native exact player eye calculation, conservative PlayerControllerMP reach and complete original World/current cursor/ItemBlock runTick proof remain mandatory. A fresh room/teleport success is not a fabricated successful placement or a valid Scaffold gameplay PASS by itself. Other Scaffold, SafeWalk, PvP, Combat, renderer and M405/M425 original cases use their existing independent fixtures without relaxation.

## Offline source policy regressions

New `scripts/test_mojang189_scaffold_context_chamber.py` covers exact source X11 verified command order and replies (using mocks for helper invocation only), same-chunk closed support geometry, observed native grounding false on fewer than 3 ticks, previous border position, previous high air position, moving player drift and wrong x, positive on three stable real-source-coordinate triples, and unchanged original ItemBlock/eye/reach/full tick verifier code. These Python checks are *not* original in-game proof.

## Release conditions

Require exact-head V158 Foundation Ubuntu and Windows + full pinned original Mojang 1.8.9 binary GUI and dedicated server preflight, including real initial Scaffold V23, M405 real UUID/controller/server damage and M425 physical X11 ray/hurt and original V151 rod/potion, Render and GUI acceptance. A separate exact-SHA rerun is recommended because previous failures were nondeterministic. All PRs remain Draft, `main` unchanged; do not close #503 or #537 from one successful test.
