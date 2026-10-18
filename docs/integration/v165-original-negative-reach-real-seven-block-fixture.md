# V165 — dedicated original server far-player negative witness

## Real original failure and strict limits

The pinned genuine Mojang Minecraft 1.8.9 [V161 original run 39109466516](https://github.com/Trexzo/customMCclient/actions/runs/39109466516) failed at **"Verify enabled Kill Aura rejects real out-of-range server player"**, after 69 completed workflow steps and many prior real positive/negative tests. The actual original host diagnostic immediately before failure reported:
`CUSTOMMC_189_RANGE_REAL_EVIDENCE local=true positions=true kinds=true entities=2 remotePlayers=1 distanceSquared=2.3134765625000506 qualifyingIndex=-1`.
This is a **real source distance ≈1.52 blocks**, well inside the configured 3.5 Aura reach, in a test that expected a distant target. Its fail-closed acceptance threw `IllegalStateException: out-of-range real player caused synthetic attack`, correctly rejecting the invalid negative-world condition. The independent original dedicated server range test must require no scoreboard damage and 80 genuine out-of-range callbacks. The diagnostic alone does **not** show whether target motion, original X11 input, spawn collision or unacknowledged teleport caused the unexpected proximity.

Previously the negative reused `actual_server_spawn_fixture` at a real **4-block** server separation within one chunk, and administered real Slowness to CITarget. That fixture is shared with other original positive tests and cannot safely be changed globally. Four blocks allow only 0.5 blocks of motion before a real source 3.5 boundary is crossed.

## V165 real fixture isolation

This change modifies **only** the `--range-veto` real source harness code in `scripts/RunMojang189LocalhostTwoPlayerSmoke.py`, not any Java gameplay policy:

- New `actual_server_spawn_range_negative_fixture(server_log)` derives exact real CIHost original dedicated-server spawn position (negative or positive coordinates, not guessed).
- It selects an actual vanilla server teleport of **7 physical blocks** along ±Z without leaving CIHost's currently generated and loaded 16-block Z chunk; host and real remote target retain same original X/Y support level. The host/remote yaw faces each other. Both are real local/dedicated JVM players, never mocked World entries or synthetic native player ids.
- The original native Aura 3.5 distance limit, physical target/player state and the old **80 consecutive source tick** range veto are untouched. This reserve of 3.5 blocks against movement makes invalid in-range source evidence less likely, but does not guarantee the player cannot move farther (the test will still fail in that case). The existing survival mode, vanilla Slowness effect and player damage baseline/zero-damage server scoreboard validation remain mandatory.
- Original 4-block simultaneous/initial/spawn tests, 2-block positive PvP, FOV and wall fixture are **unaltered**. No extending test deadline or permitting any wrong native attack, no modifying real server scoreboard or injecting packets.

## Offline negative controls

New `scripts/test_mojang189_far_range_source.py` tests:
1. Actual positive spawn near a chunk high edge -> -7 source teleport, original positive 4-block untouched.
2. Actual negative spawn near a chunk low edge -> +7 source teleport, exact native yaw and original four-block unchanged.
3. Every signed Z origin in a 17-block span -> both authentic player commands remain in one source chunk and separated by exactly seven true blocks.
4. Missing original server host login rejects; no guessed origin or entity.
5. Source code retains independent negative `RANGE_VETO` and dedicated scoreboard exactly zero, while existing positive PvP fixture remains.

These Python tests are **source policy only**, not a native GUI/server pass. Exact-head Windows/Ubuntu Foundation and complete pinned Mojang GUI/dedicated-server run including M405/M419 original UUID/damage, M425 physical X11 manual ray/hurt, original Wall Check off/on, GUI negative and Scaffold remain mandatory. Repeat successful native runs at immutable SHA to document intermittent reproducibility. V165 remains cumulative Draft child of V164 #630, #503 reliability stays open and `main` unchanged.
