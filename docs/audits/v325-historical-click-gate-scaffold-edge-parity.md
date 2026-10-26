# V325 source-parity audit: historical off-stack #505, #511, #512

Date: 2026-10-26. Documentation-only source comparison. Target is independently double-original-certified V325 `55aef8aeaae76b3345c1e03c7bf8d21aaf1d8540` (Draft #806). Historical donor branches are divergent; divergence alone never means production behavior is absent. No cherry-pick or CI weakening is authorized by this note.

## #511 — three-step Scaffold E/inventory interruption

**Donor paths inspected:** `.github/workflows/minecraft-189-real-client.yml`, `Minecraft189RuntimeBridge.java`, `scripts/RunMojang189LocalhostTwoPlayerSmoke.py`.

**Verified source inclusion:** V325 current bridge has the opt-in `custommc.acceptance.requireProductionScaffoldThreeGuiAbort` property and `SCAFFOLD_THREE_GUI_RESUMED_DISABLED_TICKS` marker. Current two-player original harness has the `production-repeat-three-menu-after-first` CLI fixture, corresponding property, and post-abort marker. Workflow still declares the fixture. This is strong source-level evidence that the negative behavior is in the cumulative target. Exact original donor script/log output, timing, historical test-object identity and runtime trace are **not** claimed identical.

**Decision:** no blind cherry-pick; source-superseded, original acceptance still mandatory.

## #512 — genuine Minecraft Scaffold edge drift

**Donor paths inspected:** `scripts/RunMojang189SingleplayerSmoke.py`, `scripts/test_mojang189_singleplayer_edge.py`, workflow path trigger.

**Verified source evolution:** old `stable_original_scaffold_edge_support` helper and donor test *filename* do not exist under those names in target V325. Instead, current `original_edge_grounding_stable(readings)` in `RunMojang189SingleplayerSmoke.py` requires three latest source x/y/z samples, x within 0.05 of 1.05, feet y within 0.15 of 100, z within [0.20,0.80], and per-axis spread ≤0.02. It explicitly handles vanilla z=0.30 collision settling. The actual original V26 candidate path calls this newer predicate on native positions read after a verified teleport cursor, then retains its independent genuine source ray/World/ItemBlock gate. This is a broader collision-tolerant predicate than donor [0.25,0.75], not byte-for-byte equivalence. The old marker `CUSTOMMC_189_SCAFFOLD_NATIVE_EDGE_STABLE_SOURCE` is replaced by `CUSTOMMC_V55_EDGE_NATIVE_STABLE_SOURCE_POSITION`; historical named regression file is absent. Do not claim exact donor unit coverage or fixture identity.

**Decision:** safety intent (three stable genuine original samples; tolerate z=0.30) is source-superseded, but exact historical unit-file/marker parity is not present. No automatic restore.

## #505 — historic original locked-B click-gate diagnostics

**Donor paths inspected:** `Minecraft189RejoinTargetAcceptance.java`, `Minecraft189RuntimeBridge.java`, `scripts/RunMojang189LocalhostTwoPlayerSmoke.py`.

**Verified difference:** donor `witnessOriginalSurvivorClickAdmission` and `CUSTOMMC_189_REJOIN_B_CLICK_GATE_NATIVE_EVIDENCE` (bounded, read-only once every 50 callbacks) are **not in V325** under those names; donor `CUSTOMMC_189_REJOIN_B_REAL_CLICK_TIMEOUT_EVIDENCE` collector is likewise absent from the current harness. This is a real **observability/diagnostic parity gap**, not proof of missing actual target lock, raycast, controller or damage behavior. Strict later original rejoin tests and historical B=10 failure in issue #503 must not be reclassified on source-string grounds. A future opt-in diagnostic could restore the bounded evidence without reintroducing older attack logic; require separate review of current production hook before any code change.

**Decision:** do not declare complete parity, do not cherry-pick old acceptance source, keep #503 open and treat diagnostic as a distinct review candidate.

## Integration implications

- #511: source-included/superseded, no production feature gap shown.
- #512: behavior superseded by a newer more permissive native collision interval with post-teleport evidence; donor exact marker/test name absent.
- #505: original click-gate diagnostic not retained; observable evidence parity unresolved, independent of normal combat implementation.
- This audit changes **no** production sources, tests or CI. V325's 2×80/80 certification remains pinned, V326/#807 remains red at original step 60 and V327/#808 is an independent diagnostic candidate. Main untouched. This is not packaging/security/license/Windows release approval.
