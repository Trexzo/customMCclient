# V292 — three genuine client/server teleports need exact fresh vanilla receipts

Direct immutable parent: V291 Draft PR #762 SHA `b24b8ad5be5b564bac5daf3abbcdfaf60c676d5e`.

## What real Mojang acceptance showed

V288 pinned original [run #39632718006](https://github.com/Trexzo/customMCclient/actions/runs/39632718006), unchanged SHA `4768e7186f23d9a7e0e21f76a79718d5a383e5de`, **FAILED** the original sticky A→B stage after 56 previous real steps because `CUSTOMMC_189_LOCK_TWO_ELIGIBLE_INITIAL_A_PASS=20` was not observed before the deadline. The source never armed Aura; no wrong locked UUID attack was proven. The genuine dedicated-server log actually contains the three teleport responses for B, CIHost and A; no claim that the lack of console acknowledgment caused that specific historical failure.

The original `RunMojang189LocalhostTwoPlayerSmoke.py` sticky-lock setup uses three distinct real dedicated-server teleport commands `b_outer`, `host_tp`, and `a_near`. Previously `send_server_command(...expected=None)` only wrote to the real server stdin and slept 0.30 seconds; it did **not require a fresh source server reply** matching which real player and coordinate received each teleport. Source World observations could therefore be evaluated after an unconfirmed delayed/rejected teleport. The actual V288 log proves those particular three did get ACKs; this is a **fixture integrity correction**, not a proven V288 root cause fix.

## Strict additional real-server evidence, zero game-mode logic changes

- Add `original_locked_tp_server_reply(command)`, a pure string-to-witness helper that admits only original CIHost/CITarget/CITargetB integer vanilla `tp NAME X Y Z YAW PITCH` commands, bounds original world height/coordinates/angles, and yields the **precise actual 1.8.9 server output** `Teleported NAME to X.5, Y.0, Z.5`, including negative coordinates correctly.
- Inside **only** `if args.locked_target`, run those same original three server commands in the same order through the already-existing `send_server_command(server, server_log, command, expected=...)` path. That helper reads from a **fresh original dedicated server log byte offset** captured before each command and fails closed unless the matching original player and exact coordinates are acknowledged within its existing 6-second command bound. No fake success, generic teleport string or stale response.
- The **same** original client World drives all decisions. No direct player edits, extra game/client native getter, player movement, controller call, target selection, FOV/UUID/range requirement, click, packet or World mutation besides the already present exact three genuine console teleports. No added retries, accepted fixtures, or relaxed original 20 consecutive A + 10 armed A + 15 A retained + actual both-player damage gate. No original server/GUI/X11 binary or original 78 stage change.
- Eight Python source/unit regressions cover exact fresh server witness for all three clients and negative grid coordinates, wrong identity, coordinate mismatch, malformed/missing/nonintegral commands, world bounds and pitch, unchanged real fixture ordering and the unchanged strict source-native marker/real server-damage dependencies.

These tests are **offline proof of command parsing and source workflow only**, not actual Mojang live World convergence. V291's newly added native phase-0 original source reason diagnostics remain in place to isolate FOV/source/index/3D problems if a later genuine run still fails at the initial A gate.

## Certification

All historical V276/V277 original UUID-phase failures, V282 real rear-FOV click failure, V283 second original Scaffold V29, V286 nonplayer V65, and V288 original sticky phase-0 timeout remain genuine failed original runs; new positive independent head runs never erase that evidence. V289's first unchanged-SHA genuine full 78/78 passed including the originally failing V65 nonplayer, and the required independent original rerun is still in progress. V285/V287 were double-native certified at their own exact commits.

V292 must independently pass Foundation Windows and Ubuntu 10/10 plus **two separate complete pinned original Mojang 1.8.9 X11 client + vanilla dedicated-server 78/78 jobs on exactly the same commit SHA** before certification. Draft only, `main` unchanged, no merge or release.
