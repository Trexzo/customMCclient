# V356 — capture the first native B-owner transition, not only last 6 KB

Minecraft 1.8.9 · strict original dedicated-server/three-client acceptance.

## Verified prior result

V355 PR #859 exact-head Foundation Windows/Ubuntu CI **PASS**:
[Foundation run 40695005943](https://github.com/Trexzo/customMCclient/actions/runs/40695005943).

V355's actual original Mojang binary run **FAIL**:
[Preflight run 40695005968](https://github.com/Trexzo/customMCclient/actions/runs/40695005968).

The genuine phase-3 host source again had A index 6, B index 1,
A ~1.413 blocks, B ~2.236 blocks, A selected/locked and
`auraFov=180`. The existing M419 native wrong-A controller veto
prevented a wrong-target click, but the required B-owned original
rejoin evidence and fresh server damage did not pass.

V355 added bounded first lock transition log markers, but the
**failure-handler printed only the final 6,000 bytes** of the entire
original host stdout. A first B-owner revocation may have occurred
long before the final phase-3 iterations, so no V355 transition
appeared in the GitHub job excerpt. We cannot infer the source
from the absence of a line in that tail.

## V356 change

On an original-game harness exception, scan the **complete original
host stdout file** in read-only mode and emit at most the first 16
`CUSTOMMC_189_REJOIN_NATIVE_LOCK_TRANSITION_DIAG` lines, each
capped at 1,200 bytes, plus the total count. Emit as a separately
delimited failure-only section:

`CUSTOMMC_189_V356_ORIGINAL_HOST_LOCK_TRANSITIONS_START count=N`

This section is printed **before** the existing 6,000-byte native host,
server and target tails, so CI retains the first B loss even after
thousands of later original game callbacks.

Nothing changes the watchdog deadline, original JVM/GUI, World
snapshots, UUID lock, selection, rotation, sent packets, click guard,
20 consecutive rejoin observations, controller argument provenance,
native wrong UUID rejection or dedicated-server B-only damage proof.

- Regression: `scripts/test_mojang189_v356_native_rejoin_first_owner_transition_log.py`
  verifies extraction, failure-only operation and original-gate parity.
- Required: exact-head Foundation Windows+Ubuntu, then actual Mojang 1.8.9
  original-client/vanilla-server native acceptance.
- If the section reports `count=0`, the telemetry scope/phase or
  an uninstrumented reset path must be investigated. Do **not** guess
  a GUI fix or call the prior native FAIL a PASS.
- Keep draft stacked after V355 and do not overwrite the parallel
  visuals work or merge to `main`.
