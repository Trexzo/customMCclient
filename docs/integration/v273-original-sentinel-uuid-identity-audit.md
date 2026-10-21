# V273 — audit original four-sentinel World UUID arrivals (read only)

Parent V272 Draft PR #743 immutable `4710e12ab74cc50095d05d13cf0c88121d3f8284`.

## Why: source-level counting blind spot
V270's independent original game job [#119188589449](https://github.com/Trexzo/customMCclient/actions/runs/39586250356) genuinely failed *before* the new A client was even launched. Four server-origin `/summon ArmorStand` commands had authentic positive acknowledgements, but the host original World snapshot never reached its original 4-net-new-nonplayer marker. V272 added raw totals, but totals do not identify which real source entities were added vs vanished. Both of these interpretations can fit the old log:
1. World delivered four actual new ArmorStand UUIDs, but the old count lost four unrelated entities (net zero);
2. The client never observed four server-summoned entities even though the server acknowledged the commands.

This is a hypothesis needing native evidence, not a reason to loosen the strict gate.

## V273 identity evidence
During the existing original phase-2 transition after 15 real source-native B-only locked ticks, freeze original `loadedEntityList` nonplayer UUIDs as an **immutable baseline**. During phase 3, compare currently sourced exact UUID/kinds snapshots to that baseline, count distinct **new** source UUIDs from non-player entries. This is robust to entity-list reorder and to removal of old UUIDs. Missing/unavailable/mismatched actual native snapshots yield `-1`, never a positive count. An observed 4+ novel UUIDs may emit ONE **diagnostic-only** marker `CUSTOMMC_189_REJOIN_FOUR_NOVEL_NATIVE_UUID_DIAG=YES` including old net counts, and the previously existing periodic source log gains novel count. No fake armor-stand classification is inferred from mere nonplayer kind; the marker does NOT assert that the four are necessarily ArmorStands.

**The original four-net-nonplayer acceptance criterion remains intact:** `sourceNonPlayerCount(e) >= nativeNonPlayerBaseline + 4`, gated by actual original B-only selected+locked UUID, before A rejoin. V273 diagnostics are never included in the native admission expression or in any click path. It never changes original Word source / entity list, chunks, summons, server fixture, timer, retries, target, controller, clicks or packets. A four-new-UUID diagnostic with net increase less than four is useful proof of why this exact older fixture may be too strict, but it is NOT a PASS.

Seventeen deterministic Java 8 JUnit source regressions cover exact baseline freezing, 1–4 new identities, no changes, list permutation, local/remote player exclusion, four added with four old unloaded, nonplayer loss, missing source arrays, mismatched snapshot length and immutable baseline behavior. These cannot certify original-world behavior.

## Certification
Exact-head Foundation Windows+Ubuntu 10/10 and full genuine Mojang Minecraft 1.8.9 X11 GUI + vanilla dedicated server 78/78 followed by independent complete 78/78 at identical SHA. Retain historic native V270 failed attempt and all prior V266/V261/V263/initial V271 failures; no waivers. All PRs Draft, main untouched.
