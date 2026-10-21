# V280 — same-native-source evidence for original UUID/phase failures

Immutable parent V279 Draft PR #750: `59ccb20619a1abeedcb8d21e6a396a337c2765f3`.
The actual fault history is in CI reliability issue #503; these failures must remain **red**, not be characterized as skipped or repaired.

## Confirmed original runtime failures

- V276 exact SHA `9bc3a344c06a8b96cb48a324887c1a23f605c342` second real [Mojang run #39602478276](https://github.com/Trexzo/customMCclient/actions/runs/39602478276), job `119254372765`: after genuine native sticky UUID setup, a real click was rejected by `Minecraft189LockedTargetAcceptance.verifyOwnedClick` as `wrong locked target UUID phase=2`. The rejected clicked UUID was B rather than A while the acceptance witness was requiring A retention. The specific reason why original selection moved to B is **unproven**.
- V277 exact SHA `6bb6e63a9fa7efac47a4a6f308cfec9ee50fc849` second real [Mojang run #39604653282](https://github.com/Trexzo/customMCclient/actions/runs/39604653282), job `119254404378`: real simultaneous two-player original target acceptance rejected `simultaneous attack crossed source-native UUID/phase=1` after genuine two-player reach and knockback-wall witnesses. The evidence shows B was clicked before the first A-owned attack had been recorded. The source reason is **unproven**.

Neither failed run establishes whether the underlying problem is production KillAura selection, original tick scheduling, entity movement, temporary FOV/eligibility changes, or fixture timing.

## Bounded implementation

On the **existing rejecting branch only**, append stable, machine-readable `sameRead*` fields to the unchanged original `IllegalStateException`:

- Sticky: report phase, expected original A/B UUID, actually clicked UUID/index, actual current module locked UUID, same native A/B indices, genuine local 3D distances and nearest/rotation owner indices. Distinguish unexpected B click before A retention, B before A-owned attack, unexpected A and non-A/B. Use precisely the already-read `Evidence`; no second live getter or native source reread.
- Simultaneous: report phase, whether first A click was genuinely owned, clicked UUID/index, expected original A/B UUID, aligned original source snapshot counts, nearest/rotation owner indices, clicked-native player/alive evidence. Use only already-read snapshots after the existing hard current-click validator; **no new native read**.
- Sticky source-unavailable and invalid indices now produce the intended hard `IllegalStateException` instead of attempting `ids.at(index)` before its index validation. No invalid source can pass.
- 13 source-level Java 8 JUnit cases cover representative phase, source, UUID and native evidence variants. Their results are diagnostic/offline evidence, not a Mojang runtime PASS.

The existing accepted-hit predicate, UUID selection/lock rules, three-dimensional reach, server scores, original GUI/FOV/World/Scaffold acceptance conditions, pass markers, named 78 original workflow steps, real server and X11 binaries, retries and timeouts are unchanged. **Rejected original clicks continue to throw.** No product gameplay change, packet, controller call or successful attack was introduced.

## Certification

Require exact-head Windows+Ubuntu Foundation 10/10 and two independent authentic original Mojang Minecraft 1.8.9 GUI/X11 + vanilla server complete 78/78 jobs on unchanged SHA; no claims before their full results. A clean run does not erase the earlier V276/V277 real crashes, nor prove their root cause fixed.

Stacked Draft only, `main` untouched, no merge/release. Avoid new gameplay tuning until actual same-rejecting-native evidence is available.
