# V272 — actual 1.8.9 entity sentinel source-readiness diagnostics

Parent V271 corrected immutable SHA `557b39da159cd85d80067105b4d3967b49ded090`, Draft PR #742.

## Real independent unchanged-SHA native failure
V270 Draft PR #741 immutable SHA `a5717b3a320fbe0b8608f41b59d21a8cdb7fb65a` passed Foundation Windows/Ubuntu 10/10 and its FIRST complete official Mojang 1.8.9 X11/dedicated-server 78/78 run. Its SECOND independent original [workflow #39586250356](https://github.com/Trexzo/customMCclient/actions/runs/39586250356) original job `119188589449` **FAILED** at the genuine native attackEntity/rejoin-B step (after 50 earlier successful steps): `missing original host observed new native nonplayer before A rejoin marker within deadline`. The real vanilla dedicated server positively acknowledged **four genuine summon ArmorStand commands**, but the original host never printed the required **four-additional-native-nonplayers** acknowledgment while original UUID A was disconnected and real B still locked/selected. The host's read-only log showed B actual UUID/index and source ray to B; A absent. This is a REAL original native failure, not a Foundation regression or a deterministic proven root cause.

## V272 change: diagnostics only
At the existing **once-per-80-native-callback** original KillAura rejoin diagnostic, report the baseline original nonplayer count, strict current `Evidence` nonplayer count, independently read-only aligned raw UUID/kinds World count, raw UUID and kind snapshot lengths, B selected/locked status, and a bounded textual reason. This distinguishes:
- Missing baseline captured before original A disconnect;
- Missing or misaligned native UUID/kinds or strict combat snapshots;
- B no longer source-selected/locked;
- Original nonplayer count falling below baseline;
- Zero, one, two, three versus at least **four** newly observed original entities.

The existing exact `nativeNonPlayerBaseline+4` **ACCEPTANCE CONDITION IS UNCHANGED**. The existing `CUSTOMMC_189_REJOIN_REAL_SENTINEL_NATIVE_OBSERVED_PASS=YES` is still printed **only** by that original source-owned gate. These diagnostics **cannot** authorize a native click, create entities, invent a new count/UUID, mutate World, alter original server fixture geometry, reset the source owner, relax a deadline, increase retries, or bypass the four-stand criterion.

15 source-level Java/JUnit regressions verify strict diagnostic labels for invalid source, stale B owner, 0–3 real additions, exactly 4, 4+, and numeric boundary cases. The classification itself never certifies gameplay.

## Strict original acceptance
Run exact-head Foundation Windows+Ubuntu 10/10 and full original pinned Mojang Minecraft 1.8.9 X11 client + vanilla dedicated-server genuine 78/78, then independent unchanged-SHA second complete 78/78. Also evaluate first-attempt test stability and every historical failed step. Previous V270 second original remains FAILED; this diagnostic does not fix or erase it. V271 first original 78/78 PASS; its unchanged-SHA second run was requested. All PRs Draft and `main` untouched.
