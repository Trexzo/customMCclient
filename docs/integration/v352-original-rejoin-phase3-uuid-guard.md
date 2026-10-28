# V352 — original 1.8.9 rejoin phase-3 native owner veto

Minecraft Java 1.8.9 · Combat acceptance repair · Draft stacked on V351.

## Evidence from V350 native preflight

Exact V350 commit `df5a5b3a0ad1a83c2d6cdea23609cb97e785f498`:
[Official original-binary run #40686370625](https://github.com/Trexzo/customMCclient/actions/runs/40686370625)
FAILED. The test-harness summary said the original host process exited 255
while awaiting fresh same-A UUID evidence, but its actual original Minecraft
crash report disclosed the earlier cause:

- `Minecraft189RejoinTargetAcceptance.verifyOwnedClick(...):1020` threw
  `rejoin stale or wrong UUID attack`.
- **phase=3**, current/locked UUID was the genuine original A UUID
  `e3542dd1-56f3-3ab4-82cb-2c015df72d8d`, with source
  **Aindex=6, Bindex=1, selectedA=true, selectedB=false**.
- Native actual distances `A=1.53125`, `B=2.23606797749979`.
  The new original EntityPlayer A existed after genuine rejoin while B was
  still present. The 20-tick B-locked rejoin witness had not completed.
- The Minecraft crash produced process exit 255; the OpenAL warning was
  followed by silent sound fallback and was **not** the proven cause.

## Change

V351 already supplies combat scheduling/UUID controller ownership for the
Auto Clicker. V352 changes **only** the opt-in original KillAura native
rejoin acceptance pre-melee veto, not its production targeting algorithm.

The previous `m419MustVetoPendingSyntheticClick` rejected stale source
owner/ray only in **phase 4**. But rejoined A is source-visible during
phase 3 before the genuine 20 consecutive native B-locked snapshots.
A premature A click could reach `verifyOwnedClick` and crash the host.
The new predicate also denies a phase-3 synthetic action **only if**
both original A and B genuinely appear in the current native World snapshot
and the source owner/ray fail the existing B gate.

- Before rejoin, genuine phase-3 B-only clicks are untouched.
- With both A and B present in phase 3, allow only verified original
  B-selected, B-locked, B-source-ray owner with unchanged source distances,
  FOV/UUID/kind/selection safety criteria; otherwise **deny before
  native click scheduling**. The v352 log is a bounded diagnostic
  `CUSTOMMC_189_M419_TRANSIENT_WRONG_UUID_CLICK_VETO` with phase and
  actual source indices. No fake success.
- In phase 4 the original M419 veto remains exactly as before.
- Mandatory original A rejoin marker, B retention for 20 fresh callbacks,
  reordered native entity-index requirement (when requested), real
  PlayerControllerMP argument B UUID, original hurt and dedicated-server
  B-only damage gates are unchanged. A permanently incorrect owner
  remains a **timed strict native acceptance FAIL**, not a pass.
- Manual input, packets, original mouse, `leftClickCounter`, original
  ray source, gameplay target selection, module settings, UI, and
  `main` are not modified.

## Verification

- JUnit: `Minecraft189RejoinPhaseThreeOwnerVetoTest` asserts the
  exact phase-3 A/B predicate, phase-3 B-only preservation, phase-4
  parity, old overload and disabled-acceptance behavior.
- Python:
  `scripts/test_mojang189_v352_rejoin_phase3_owner_veto.py`
  verifies original pre-click ordering and strict server/UUID requirements.
- Require exact-head Windows and Ubuntu Foundation CI and SHA-verified
  original Mojang 1.8.9 three-player graphical JVM plus dedicated server.
  Source/unit pass does NOT certify gameplay; the previous failed
  V350 real-client evidence must remain visible.

The remaining observed root issue is that KillAura reselected/locked A
while B was still alive and in native range. This change protects the
test's original client during convergence and preserves evidence of the
underlying unwanted switch; it does NOT claim to repair the underlying
selection policy without a distinct source investigation.
