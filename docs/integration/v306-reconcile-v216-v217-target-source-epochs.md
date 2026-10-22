# V306 — reconcile genuine World target selection and HUD rotation-source parity

Direct immutable parent: Draft V305 #776 at `a9c8ea5419149f7c7a473d12ec6d18eb440a6478`. V305 restores
the V218 CPS clock bugfix on top of separately **double-original-certified**
V304 `a7a4f8e227fc2b7454fbc9c97c696cc74d357f0e`.

## Why this is an actual omitted implementation, not a redundant side PR

- [Combat V216 #682](https://github.com/Trexzo/customMCclient/pull/682)
  head `14a581174f6c843dfd009c8248e6ed29c18feb99`: an external
  `CandidateFilter.accepts` or `CandidateScore.score` may synchronously
  reenter `Minecraft189NearestPlayerTargetState.update/clear`. The older
  outer source scan previously committed an obsolete player after a newer
  original World decision. V216 increments source generation on any update
  or clear, and refuses stale outer publication after callbacks. Same
  World entity/player identity/range, rank/tie constraints, no fabricated
  game data or clicks. Its **14 JUnit adversarial source callback tests**
  distinguish nested clear, replacement, invalid World, NaN and recovery.
- [Render V217 #683](https://github.com/Trexzo/customMCclient/pull/683)
  head `f90e4d029c15f6de4a6366beb712249261fdd399`: the target HUD could
  paint currently selected World player B's distance alongside still-cached
  player A's original rotation (or a new distance from an old rotation).
  V217 requires matching original source **selected entity index** and
  **finite nonnegative distanceSquared** between selector and rotation
  before rendering. No spoofed yaw/pitch/UUID, geometry, renderer bypass,
  gameplay target selection, or native-controller authorization. Its
  **14 source JUnit tests** protect mismatches, valid parity, and resets.

**Blob-level integration evidence** (both direct donor parents vs V304):
- `Minecraft189NearestPlayerTargetState.java`: V304 exact old blob
  `f66362bb74ae3531aa4090a71806281b3f167f7b` **matches V216's
  original parent** `99c165b4693e9d204c378aa15563b2a50862d51d`;
  donor corrected source blob `39b3a00e0258cf5c3f12d77f93f81e406ad836ac`.
- `Minecraft189TargetHudModule.java`: V304 exact old blob
  `dae275a9c5f06a9b737b664fcaded871e564b8fc` **matches V217's
  original parent** `33bcaef425acade855220ca456acba34dead445c`;
  corrected source blob `96cf031a8d07dbda551e27faabc21544c1b12d7f`.
- The two 14-case JUnit files are absent at V304 and V305 before this port.
  Reviewed test blobs `182b18d54ccfa17efad7e29aafe88b525dc86fd2`
  and `b8275e8708fb37c1d1710c4805ded49d8e2cf196`.
- Four donor Java blobs are imported **unchanged**. No old donor
  `.github/workflows` file or branch history transplanted: V305 already
  inherits the broad production+test Java original-CI paths filter from
  V304, and V304's revised X11 provisioning must remain intact.

This is one tightly-related source-authority group in the larger
[issue #777](https://github.com/Trexzo/customMCclient/issues/777)
398-open-PR / 606-file integration audit. It does **not** claim all other
side branches are reconciled.

## Strict original verification

- V304 original exact-head Foundation Windows/Ubuntu 10/10 and two
  complete pinned original Mojang 1.8.9 GUI/X11 + vanilla-server 78/78
  remain historical results for **V304 only**.
- V305 is under its own original binary validation and cannot inherit
  double certification from V304.
- V306 must achieve **its own** Foundation Windows+Ubuntu 10/10 and
  **two distinct FULL pinned genuine original Mojang 1.8.9 78/78 PASS
  jobs on exactly the same V306 SHA**. Do not count mocked source unit
  tests or an upstream PR's checks as V306 native evidence.
- Preserve all 78 original hard gates including Scaffold V19/V26/V29,
  UUID/FOV/GUI real negatives and positives, item/controller/server
  authority, original CI timeboxes, and full failure history.
- All relevant PRs stay Draft; `main` unchanged, no merge or release.
