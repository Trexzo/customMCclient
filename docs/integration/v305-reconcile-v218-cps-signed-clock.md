# V305 — reconcile omitted V218 signed nanoTime CPS fix into certified V304

Immutable parent: V304 Draft #775 `a7a4f8e227fc2b7454fbc9c97c696cc74d357f0e`. Original donor:
[V218 Draft PR #685](https://github.com/Trexzo/customMCclient/pull/685),
head `698897858c7fb5ba047b9f5367e425d118e426b6`.

## Evidence of a real missed branch change

An exhaustive root Git-tree comparison established **606 changed files** from
`main` to V304 (518 additions, 88 modifications, zero removals);
GitHub Compare's 300-file response is only the first page/cap, not full coverage.
The current V304 stack has 296 direct PR parent links back to `main`, with
102 other open PR branch heads outside that direct chain. These are *not*
automatically stale or missing: some are Git ancestors, some diverged; explicit
per-file reconciliation is necessary.

V218 is a **material code omission**, not merely a divergent historical PR:
V304 production `Minecraft189ClickRateTracker.java` blob
`2a6484d1077312869c070789944108ed2cbb6d4a` still uses
`lastTimestamp=Long.MIN_VALUE`, `now - 1_000_000_000L` as absolute signed
threshold and `clickTimestamp <= threshold`. These can discard actual
presses when genuine `System.nanoTime()` starts near `Long.MIN_VALUE`;
using `Long.MIN_VALUE` as a never-sampled sentinel also confuses a legal first
timestamp. The reviewed V218 production blob `2b57e1441c528970f584fa592a6d25f3e4baa988`
and source JUnit test blob `479be35060d6f5df3978774488de38028ffe2673`
were **absent** from V304. In contrast, out-of-chain V256 #726 Hunger/XP
source-lease logic is already present at V304 and must NOT be blindly reapplied.

## Minimal exact-blob port

This PR adds only the two unchanged, independently readable V218 source/JUnit
blobs to the V304 integration head plus this evidence note. No cherry-pick of
V218's old workflow file or old paths-filter: V304 already has broad
`platform-1.8.9/src/main/java/**` and
`platform-1.8.9/src/test/java/**` genuine-Mojang CI triggers.

- Use `lastTimestampAvailable` boolean; `Long.MIN_VALUE` is valid input.
- Compute elapsed `now - recordedPress` and compare it unsigned to exactly
  one second; all actual clicks, left/right separation and bounded time
  windows remain unchanged.
- Keep signed backwards-clock error, unsupported-button rejection, reentrant
  synchronization, zero-duration valid clicks, and clear/reset behavior.
- Restore the **14 source-clock JUnit regressions** from V218 donor including
  min-value, negative-origin, exact one-second, two-button and backwards cases.
  Such JUnit tests **are not genuine original Mojang gameplay evidence**.
- Original native World, camera, controller, packet, A/B UUID, Scaffold
  V19/V26/V29 checks, first native timebox and all 78 original workflow
  steps remain unchanged.

## Strict verification and safety

V304 parent **remains double-certified** on unchanged SHA
`a7a4f8e227fc2b7454fbc9c97c696cc74d357f0e`: Foundation Windows+Ubuntu 10/10,
[original workflow attempt 1](https://github.com/Trexzo/customMCclient/actions/runs/39771146156/attempts/1)
and [attempt 2](https://github.com/Trexzo/customMCclient/actions/runs/39771146156/attempts/2)
both full pinned Minecraft 1.8.9 GUI/X11 + vanilla server **78/78 PASS**.

V305 must independently pass **its own exact-head** Foundation Windows 10/10
and Ubuntu 10/10, and **TWO separate completely successful actual Mojang
GUI/X11 + vanilla server 78/78** on the same V305 SHA before double
certification. Do not transfer V304's native-pass status to V305.
Historic failures stay FAILED. All PRs Draft, `main` unchanged,
no merge/release. This branch is only the first specific reconciliation
of sideline PRs, **not an assertion that all 102 are resolved**.
