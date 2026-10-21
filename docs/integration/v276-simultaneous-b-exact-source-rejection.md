# V276 — source provenance at the exact native two-player B rejection

Parent: V275 Draft PR #746 immutable `a65f61f6bdb3f872decfcbf1622817907fac84ff`.

## Grounded crash that still needs root cause
V272 original Mojang 1.8.9 X11/vanilla server full test [#39593532156](https://github.com/Trexzo/customMCclient/actions/runs/39593532156), job `119200139727`, FAILED original simultaneous A/B two-UUID step after 57 prior real green steps. Vanilla server observed real A damage 20, real B zero and actual B teleport. Original client crashed from `Minecraft189SimultaneousTargetAcceptance.verifyOwnedClick`: B clicked without contemporaneous proof that **both** A/B were living, within original 1..3.5 three-dimensional range, and B was 0.12 closer, source-nearest and source-rotation-selected. It was a correct fail-closed admission rejection but exact cause remained unlogged.

V274 Draft #745 separately re-read source for an explanatory crash message AFTER the real source-native `read(host)` had rejected the click. This second snapshot may be different; it is inadequate to tell which condition failed at the instant of original rejection.

## V276 strictly diagnostic enhancement
The existing original native `read(host)` method now delegates to `read(host, StringBuilder)`; ordinary caller passes null (same admission path). Only the narrowly scoped original early-B click calls it with an optional diagnostic sink. **On the EXACT original World UUID/kinds/combat/positions/local/nearest/rotation snapshots that already decide the B click**, capture which original condition caused rejection and the relevant genuine values: source availability/list lengths, original remote-player count, original A/B UUID positions and alive status, raw 3D distances, the 0.12 priority margin, original selected nearest and rotation indices, and native clicked index ownership. If accepted-read evidence and clicked index contradict, mark original list index change. A contradictory diagnostic second sample from V274 remains explicitly secondary. Even if later source reads say B now valid, it must NOT override the rejecting read, retry/click, advance the phase, suppress the crash or claim a PASS.

The boolean admission clauses and exact source native inputs remain unchanged: real current two living UUIDs, 1..3.5 reach, B 0.12 nearer than A, actual crosshair owner and nearest/rotation B, and separately two authentic server-damage events. No fixtures, World/entity/player position, packets, input events, underlying KillAura or release path changed. No timeout extension, synthetic success, test weakening or merged PR.

Seventeen new Java 8 JUnit tests distinguish absent/misaligned original source, A/B missing/dead/out-of-range, B distance/nearest/rotation veto, remote-count mismatch, changed entity index, same-read valid-but-already-rejected classification, persistent old exact source values and absence of PASS markers.

**Certification:** Exact V276 SHA Foundation Windows+Ubuntu 10/10, full original Mojang 1.8.9 graphical X11 client+vanilla server 78/78 then independent unchanged SHA full 78/78. Retain V272 A/B and V274 V29 historical original failures; V275 first original had V29 PASS and remains full-running. V273 first 78/78 PASS, second independent in progress. All PRs Draft and `main` untouched.
