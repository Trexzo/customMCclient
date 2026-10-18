# V173 — original M419 rejoined A source geometry interior to both native distance bounds

## Distinct real original Minecraft failures — and evidence limits

The full official original Mojang 1.8.9 X11 GUI/dedicated-server V170 [run 39115962179](https://github.com/Trexzo/customMCclient/actions/runs/39115962179), immutable SHA `7d61042aabf6e9132fb814cb25ae820b6bd40c30`, **FAILED** with genuine Minecraft client test-only exception inside actual `PlayerControllerMP.attackEntity`: `reason=rejoined-A-not-0.12-closer-in-3D` in phase 4. The real captured source distances were A=`2.3297257584887228` and B=`2.4281797437686015`, leaving gap≈0.0985, below the existing mandatory real distance condition `da + 0.12 < db`. Actual world source original host (-1.20,4,-1.09), A (-.50,4,-3.31), B (-1.50,4,-3.50), all actual vanilla remote player objects. Original B UUID lock/selection were correct. Real outgoing controller was entered but correctly rejected by the precondition; there is no positive new B server damage certification. V170's new phase4 read-only true source ray diagnostics did **not** cause or fix the true geometry rejection.

The prior V159 original [39108548698](https://github.com/Trexzo/customMCclient/actions/runs/39108548698) **FAILED the other bound**, with native A only ≈0.79 blocks from original host after an old one-block server teleport. The original strict lower bound requires A 3D distance `>=1`. V161 moved rejoined A to a **true fractional 1.5 blocks** forward of server-logged host center, preserving the lower bound on observed earlier motion, but V170 proved that runtime host/A drift can still consume its small nearer-than-B margin. These are real distinct original failures, not grounds to weaken original acceptance rules.

## V173 actual original dedicated-server fixture only

Cumulative child of [V172 Draft PR #638](https://github.com/Trexzo/customMCclient/pull/638), which retains V171 original positive World scaffold support and V170 phase4 real ray observation. V173 changes **only** `actual_server_rejoined_a_controller_fixture(host_tp,a_near,b_outer)` of the real `--require-rejoined-controller-b` original 3-client/rejoin scenario in `scripts/RunMojang189LocalhostTwoPlayerSmoke.py`:

- Compute the original host's actual source server world center from parsed Mojang login. Use a real vanilla console `tp CITarget` with explicit *two decimal* Z to position actual rejoined A **1.35 physical blocks** forward, rather than the prior 1.5. Example: host source `tp CIHost 243 5 444 0 0` produces host (243.5,5,444.5); new A `tp CITarget 243 5 445.85 180 0`. Negative signed/chunk case is equally grounded (host (-33.5,-33.5), A Z=-34.85). This command is executed by the actual original dedicated Minecraft server, **not** by editing any client World, entity position or packet.
- Real B remains untouched: exactly two original blocks forward, one to the side, source singleton/UUID and real distinct 0.6-wide vanilla player box. Original player A **must remain closer/in range**, B **must remain in original reach and selected/locked**. The unchanged acceptance still evaluates actual source A/B 3D distances, rejoined original same A UUID with true changed loaded entity index, true wrong-A UUID veto and actual native controller argument B UUID plus **independent server-positive B and zero A damage scoreboard**. No unconditional test marker, synthetic hit, fake server damage, target ray or extended deadline is added.
- The existing **fail-closed fixture bounds** explicitly require 1.35-.30 >=1.0 even if actual A moves towards host, and `hypot(1.35,.9)+.12 < sqrt(5)` even after a separate vertical hop; original in-game motion beyond these assumed margins will STILL make the actual original check fail. This is fixture risk reduction, **not proof** of all possible movement.
- Original initial A/B positions, B survival and disconnect, original PvP/FOV/Walls/rod/potion/Scaffold worlds unchanged. The real second rejoined A survival floor and second server teleport use the exact same new decimal A source coordinate; never a forced local entity reset.

## Source regression evidence and limitations

Expanded `OriginalM419RejoinedNativeDistanceFixtureTest`:
1. Positive/negative true dedicated-server-login source chunks, exact native fractional A coordinate and genuine B loaded separate hitbox, required lower+upper 3D fixture margin;
2. Historical V159 source crash coordinate lower-bound `da≈.79` must remain negative for prior one-block geometry and positive *counterfactual* at the new source 1.35;
3. Historical V170 real A/B crash coordinates require the old ≈.098 gap to fail but model an extra .15 towards-host A server-placement shift that would make gap >.12 on the same recorded lateral drift; **explicitly counterfactual, not evidence of a new real runtime success**;
4. Unchanged rejection of wrong original player names, overlap, noncontiguous chunks, wrong A/B forward geometry and unchanged original B/UUID/controller/server damage source gates.

These are Python source-only fixture and adversarial tests; no true Minecraft Java or server-side gameplay can be certified without the full pinned original game.

## Exact native admission

Require exact-head V173 Foundation Windows+Ubuntu and full original Mojang 1.8.9 GUI + dedicated 78/78 (strict original M419 native B controller UUID and actual positive B/zero A damage, M405 earlier lock, M425 physical mouse original ray/hurt, V19 raytrace/ V29 Scaffold original World evidence, AutoRod/Pot original cast/regeneration, Wall Check and FOV), then repeat the **identical V173 SHA** independently due to documented nondeterminism. Parent V162 exact `66db7002abadb07207fbdfbf07171311bf494828` had two full 78/78 official successes, but cannot certify newer versions. All children Draft, no merges, `main` unchanged; keep #503 original stability and #537 broader modules open.
