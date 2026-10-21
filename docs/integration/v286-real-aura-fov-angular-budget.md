# V286 — actual Aura rotation source and angular-step invariants

Parent V285 Draft PR #756 unchanged head `722764de474c6e9c6e5119d9207017ea692174ab`.

## Why these tests

V282's genuine original Minecraft 1.8.9 GUI + vanilla server [run #39620847862](https://github.com/Trexzo/customMCclient/actions/runs/39620847862) **FAILED** V46 real rear-player FOV. The earlier actual original camera yaw was 0° with actual remote target yaw −180°, source-aligned real rear geometry, absent target selection and 16 enabled strict FOV-veto callbacks. At the original unexpected automatic attack, the actual camera yaw was −180° so current source-native yaw delta was 0°, independent FOV gate `permits=true`, remote index 1 in reach, and the strict original negative test correctly threw.

Read-only analysis of actual production `Minecraft189KillAuraModule.aim` found a default **90-degree FOV** early refusal, a default **10-degree maximum yaw/pitch movement per callback**, and a separate native `Minecraft189KillAuraNativeFovGate` that validates current original source yaw at attack. The selector also applies FOV filtering to the real current source snapshots. Therefore an instantaneous **single default Aura aim invocation cannot directly turn yaw 0° to −180°**. This does NOT prove Aura did not participate across several native callbacks, nor prove whether upstream server, mouse, module or fixture scheduling changed yaw. V285's opt-in aim-return trace is intended to gather live provenance; no actual FOV root fix is claimed.

## Source-level production regressions

Add 10 Java 8 JUnit tests with the **real** `Minecraft189KillAuraTargetSelector`, `Minecraft189KillAuraModule.aim`, `Minecraft189TargetRotationState` and **real** independent source-native FOV click gate, plus a simple setter-counting test rotation control, using synthetic source geometry:

1. Real selector and actual click gate reject remote player at source 180° behind yaw 0°.
2. Previously eligible but now stale rear target cannot start a new default Aura aim from yaw 0°, even if the rotation snapshot was published at yaw −180°.
3. Newly eligible current yaw −100° can aim toward −180° by **one −10°** native write, not 80°.
4. Eight separately source-updated eligible aim callbacks are needed to progress −100° to −180°, each no more than 10°.
5. Even configuring a larger angular step while keeping default 90° FOV does not authorize rear-target selection or aim.
6. An **externally supplied** new original source yaw −180° makes the same target FOV-eligible with **no Aura setter call**.
7. Out-of-reach target cannot be aimed into range.
8. Missing current source combat evidence clears stale target and prevents aiming.
9. Signed yaw wrap near 180° is correctly treated as a small eligible change, not a 359° jump.
10. Synthetic offline source geometry does not emit original Mojang PASS markers.

These are **offline production Java invariants**, not authentic Minecraft 1.8.9 GUI/server observations. They cannot replace either an actual original FOV failed run or genuine native acceptance. No production Java, FOV threshold, aim step, selector, World input, native setter, original fixture, 78-step workflow, timeout/retry, or PASS marker was modified.

Certify only with Windows/Ubuntu Foundation 10/10 and two independent full original pinned Mojang Minecraft 1.8.9 X11 client plus vanilla dedicated-server 78/78 on the exact unchanged SHA. The V276 and V277 native UUID/phase failures, and V282 V46 original FOV failure, remain genuine FAILs in issue #503. All PRs Draft; `main` unchanged; no merge/release.
