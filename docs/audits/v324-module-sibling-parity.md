# Source-parity audit: divergent movement, combat and player sibling PRs #542–#546

**Date:** 2026-10-25. Target: immutable **V324 SHA `060445b541f7b83b975d3311683e7778a431bf0e`** (and V325 `55aef8aeaae76b3345c1e03c7bf8d21aaf1d8540`, which changes only unrelated M417 sources). These five historic Draft heads remain **Git-diverged**, but Git ancestry does not determine feature presence.

## Method and result

Fetched exact donor patches for each of the five PRs and the entire current V325 source of every modified path; compared nonblank, noncomment donor-added lines to the trimmed original file lines, then manually examined the one later-expanded guard. This is targeted **source inclusion and regression continuity**, not byte-for-byte whole-program semantic equivalence or independent runtime certification of each historic branch.

| Historical PR | Exact previously proposed behavior | Current source and test finding |
|---|---|---|
| **#542** – V76 | AutoSprint pauses synthetic original sprint writes while ClickGUI has actual focus, without modifying native sprint source | All meaningful added lines appear in current `Minecraft189AutoSprintModule`, host wiring and its focused JUnit regression |
| **#543** – V77 | WTap native sprint setter failure invalidates pending synthetic sprint reset and prevents held-click replay | All meaningful added lines appear in current `Minecraft189WTapModule` and its setter-throw regression |
| **#544** – V78 | AutoJump ClickGUI focus revokes stale grounded jump lease until a new ground contact | All meaningful added lines appear in the modern AutoJump module, host binding and its ground-contact regression |
| **#545** – V79 | FastPlace, FastBreak and SpeedMine reject modification of native vanilla cooldown/mining state while the real ClickGUI owns focus | All meaningful donor-added lines appear in three module implementations, host wiring and all three respective regression classes |
| **#546** – V80 | NoFall preserves negative, nonfinite and invalid native fall distance; NoSlow never creates nonfinite adjusted speed from invalid source | The finite/positive guard and NoSlow defensive arithmetic are preserved, and both regression classes retain all meaningful donor additions. One NoFall `if (Float.isFinite(distance)` source line has been **restructured** into a stronger combined guard requiring current native read epoch, enabled state, stable player, no GUI focus/revocation and the same threshold, followed by `Float.isFinite(distance)` and positive finite distance checks. Do not reintroduce the old isolated conditional. |

### Explicit limitations

- All five PRs are still Git-diverged from V324; the review finds their identified source/test additions already implemented in the cumulative tree and therefore **no blind cherry-pick is warranted**.
- Source-line inclusion does not independently prove every original test's temporal behavior in all runtime settings. The cumulative current V324 code is original-Mojang-CI double certified (2 × 80/80), but that does not claim isolated double certification of any older branch or an independently tested Windows shareable installer.
- V325 has its own independent current CI; this audit neither imports its results nor certifies it by inheritance.
- No Git branch rewritten, no main merge, no PR closure, no release.
