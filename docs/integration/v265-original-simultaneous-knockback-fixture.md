# V265 — genuine original two-player knockback fixture

Parent V264 Draft #735: e5a39893a18df3a6aabc2bbb8da2076ebd529e3b.

## Evidence of the actual test failure
Pinned Mojang Minecraft 1.8.9 original V263 run 39499883781 FAILED the full run at step 52: vanilla scoreboard confirmed CITargetB taken=10 when strict test expected B=0 *before* the deliberately issued B nearer teleport. The genuine original host already logged A-owned attack, native B priority switch and B-owned attack; those markers came before the test-controlled B teleport. This remains an actual FAIL. The host was at (-440,-288), A (-440,-290), B (-438,-290), and the expected B near target was (-439,-289). Initial A distance 2 and B distance sqrt8 allow first-hit vanilla A knockback to move A farther than B. That physical interpretation is plausible, not yet proven.

## Fixture repair, not weaker native acceptance
Derive an original world-wall command solely from the four real server-source teleport fixtures. Construct one actual vanilla STONE wall 3x3x1 one block BEHIND original A with a real server /fill before teleports. Verify an exact genuine 9 blocks filled vanilla reply plus THREE independent positive testforblock commands. Wall is behind A and away from native host-to-A/B controller ray; it protects A against knockback drift before the deliberate B change. Both original players remain real Survival, genuinely eligible and vulnerable.

No altered original selection/ray logic, click permissions, synthetic player, fake damage, moved acceptance deadline, skipped scoreboard, creative immunity, disabled Aura or native mutation. Strict original A native controller and server damage, B zero prior to intentional relocation, B original priority switch and B positive damage remain mandatory. New helper fails closed for missing original command source, wrong identity, malformed geometry, world border or unsafe build height.

Twelve Python source-only helper regressions, including exact failed-run coordinates. Success does not certify physical knockback behavior. Mandatory exact-head Foundation on Windows and Ubuntu, full strict original 78/78 real Mojang X11 + dedicated-server pass and separately independent unchanged SHA 78/78. Preserve earlier failures; all PRs Draft; main unchanged.
