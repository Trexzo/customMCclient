# V282 — genuine production selector sticky UUID transition regressions

Base: V281 Draft PR #752 exact immutable parent `a4548d74e7885f9dbe085f575d3e9ac7e5c001fa`.

## Investigated original Mojang failures

V276 [second exact-SHA native run #39602478276](https://github.com/Trexzo/customMCclient/actions/runs/39602478276) failed genuine original lock acceptance with `wrong locked target UUID phase=2`, clicking B when the native witness required retaining A. V277 [second exact-SHA native run #39604653282](https://github.com/Trexzo/customMCclient/actions/runs/39604653282) failed genuine simultaneous acceptance at phase 1 (B click before A-owned click). Do not reinterpret either red run as a PASS or infer a definitive production defect solely from the rejection.

Production `Minecraft189KillAuraTargetSelector` uses source-aligned UUIDs and gives the current still-eligible locked UUID priority score zero; if A remains actually eligible, its UUID must remain selected when B moves closer, even if native `loadedEntityList` indexes swap. If A becomes invalid (death, out-of-range, FOV), a replacement target is permissible; if the source is absent or misaligned, the selector fails closed and explicitly revokes its lock. `Minecraft189KillAuraModule.aim()` also explicitly revokes the lock when no live aim control is available. Those are distinct transitions that can superficially resemble B taking over too early. **These are code-derived hypotheses/branch behaviors, not proven reasons for the historical original-client failures.**

## Bounded source-level regression batch

New `Minecraft189StickyUuidSourceTransitionTest.java` instantiates the **actual production selector and module** and exercises 11 Java 8/JUnit tests:

1. The current locked A must remain selected across 30 consecutive snapshots when B moves much closer within 3.5 range.
2. The native indexed list can reorder A/B and A's UUID still remains locked through 20 ticks.
3. A at exact 3.5 range boundary remains eligible; just outside 3.5 releases to B.
4. A death legitimately disqualifies A and selects B.
5. A outside 45-degree current source FOV can legitimately release to B.
6. Missing native UUID snapshot revokes the lock; a later restored valid source can choose B.
7. Misaligned UUID/entity count fails closed.
8. Missing aim control invalidates module ownership; a later B takeover cannot count as an A-retained phase-2 witness.
9. Disabling lock setting selects nearest without retaining any UUID lock.
10. Missing combat source fails closed; recovery requires fresh eligible native source.
11. No source test claims actual original Mojang server or native success.

The tests use the real Java production classes; the geometry/entity arrays are **synthetic offline source input**. No test calls the original game, and tests are not a substitute for authentic pinned GUI/X11/vanilla server evidence.

**Production source unchanged.** Existing original real-client callback checks, 78-step workflow, UUID fail-closed assertions, client/server binary, packet, X11, retry, scoreboard and acceptance thresholds remain exactly as before. The broader V278 native-test-only PR filter guarantees this test file schedules the original Mojang suite.

## Certification

Foundation Windows and Ubuntu 10/10 and two independent full unchanged-SHA original Mojang 1.8.9 client and vanilla server 78/78 runs are both required for final certification. Historical native failures remain tracked under issue #503. Draft PR only; `main` untouched; no merge/release.
