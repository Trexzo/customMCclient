# V138 — actual original simultaneous-player UUID revalidation

## Why this is not a relaxed acceptance gate

V134 original Mojang 1.8.9 multiplayer acceptance run `39027796181` failed at the strict two-live-player UUID witness `Minecraft189SimultaneousTargetAcceptance.verifyOwnedClick`:

```
CUSTOMMC_189_SIMULTANEOUS_TWO_PLAYERS_IN_REACH_PASS=YES
CUSTOMMC_189_SIMULTANEOUS_A_DAMAGED_B_STILL_ZERO_PASS=YES
IllegalStateException: simultaneous attack crossed source-native UUID/phase=2
```

The first **real** A hit was already server-observed; phase 2 means A has been hit and the test is *waiting for the separate real B-owned hit*. In the old Java fixture, a subsequent attack on A in phase 2 threw, even if actual vanilla knockback, network movement, and fresh nearest/rotation caused a legitimate **B → A priority return** before B's hit. In original Minecraft physics that is possible even with both UUIDs alive and in range. The failure did not imply a fabricated UUID, but the original log also did not prove a genuine priority return, so it cannot be ignored as merely flaky.

This cumulative V138 Draft child of V137 #603 adds an exact source-owner **revalidation** rather than accepting all A clicks in phase 2:

1. Keep the preexisting complete **fresh** original crosshair hit, mapped UUID, living/nonlocal player kind, combat/alive, nearest target, rotation target, finite range <=3.5 proof before any phase logic.
2. A phase-2 return is eligible **only after A was previously attacked**, its current actual source UUID still matches pinned A, and the fresh `Minecraft189WorldEntity...` snapshots prove **exactly two** remote *living eligible* player UUIDs A/B with actual loadedEntityList indices, both within vanilla melee range, and their actual Euclidean distances make **A** the nearest/selected source target.
3. Revalidate that the current source crosshair player index equals the *new* native A index; a reordered index, third UUID, absent B, unknown owner, no strict nearest/rotation or stale player is rejected.
4. **Do not advance phase 2, emit the B-owned attack marker, or weaken the dedicated server's requirement for separate observed B damage.** Only a genuine B-owned attack with its own source selection may transition to phase 3 and claim the final PASS.

The production Kill Aura selector and packet ownership, actual target movement, server spawn, original binary FOV/range gates and player UUID mapping are all **unchanged**. This is a correctness fix to an independent *read-only acceptance witness*, not a shortcut around the real two-player outcome.

## New deterministic regression

`Minecraft189SimultaneousPriorityRevalidationTest` exercises the actual invoked phase-2 owner decision: legitimate both-live A return and index reordering, plus negative tests for wrong phase, missing previous hit, unknown or reordered index, third/null UUID and unavailable/two-player-incomplete priority evidence. The strict original combined source eligibility is still executed in `verifyOwnedClick`; unit tests cannot independently certify a live server hit.

## Additional integration blocker: a missing required-workflow trigger

Audit found that `.github/workflows/minecraft-189-real-client.yml` had a strict pull-request `paths` filter that did **not** include `Minecraft189SimultaneousTargetAcceptance.java`. V138's exact acceptance fix would therefore schedule Foundation alone, and could accidentally be called complete without any real server execution. This is fixed in the same branch:

- The full pinned binary workflow now triggers on **every** original-game `Minecraft189*Acceptance.java`, accompanying `Minecraft189*AcceptanceTest.java`, and the targeted native simultaneous revalidation test. It retains all preexisting path triggers and every historical X11/dedicated test step.
- `scripts/test_mojang189_workflow_acceptance_paths.py` runs automatically under the existing offline original-binary `unittest discover` step. It parses the **actual pull_request paths section**, checks representative original combat/rod/rejoin witness and module paths, and checks that the genuine two UUID server damage step and original Rod/Potion X11 step remain in the workflow.
- The test script path itself is a PR trigger so future edits cannot bypass verification. There are no new network dependencies and no lower-value short test substituted for the full official suite.

## Release and coordination

V138 is strictly stacked on V137 at parent `2b268e661c42b4bf6f1e8b39ef1541ffe9c46ebf`. No `main` modifications. V135 #601 was the latest fully certified baseline at the beginning of this batch (both Foundation and entire Mojang 1.8.9 original binary success). V134 original failure re-run was pending and should not be overwritten or declared fixed from a different commit.

Certification requires **exact-head** Foundation Windows+Ubuntu and the **entire** pinned official Minecraft 1.8.9 binary suite SUCCESS, including unchanged real A/B separate server damage proof, original GUI/X11 real hook cast/reel, Rod/Potion, FOV, Scaffold and owner/UUID gates. Historical test steps remain mandatory and retain their names; do not skip the failing test. Draft PR only.
