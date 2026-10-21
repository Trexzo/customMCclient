# V290 — native nonplayer rejection distribution + identity-stable high-water witnesses

Immutable stack parent: V289 Draft PR #760, exact SHA `6adba02627da04b9e1e36e1deb8cc86452dd3026`.

## Actual failure and coverage gap

V286's real original Mojang 1.8.9 GUI + vanilla server [run #39625775380](https://github.com/Trexzo/customMCclient/actions/runs/39625775380), job `119310504470`, **FAILED** at V65's living nonplayer negative control after 64 passed original stages. The genuine original /fill, /tp and Pig /summon returned, but no 20 consecutive actual original living-nonplayer ray callbacks were confirmed. The negative test never enabled Kill Aura and never reached its real 80 enabled-mob veto phase; original root cause remains unknown. V288 added per-native-ray rejection reason and V289 replaced unverified relative random-spawn fixture commands with the genuine source-acknowledged absolute original single-chunk room used by successfully tested manual melee. Neither offline regression proves native mob raycast success.

A remaining diagnostic problem in V288: every-40 callback logs show total valid and **only the most recent** native ray rejection reason. If mixed ray failures occur (e.g., regular MISS combined with intermittent wrong loaded-index, or raw rays valid but switching native target identity), the last reason hides the actual frequency and the length of meaningful streaks.

## Narrow observational additions

In the same existing opt-in `Minecraft189OriginalNonPlayerRaycastTrace.observe()` (called **after exactly the preexisting real source ray/World/kind reads**, no extra getters):

- Count each original evaluation as one accepted or one rejected, and aggregate rejection reasons separately for: missing crosshair ray; non-entity hit type; World identity mismatch; native loadedEntityList object not found; hit index beyond kind source; nonliving/player or local kind bits; mapped reflection unavailable; unavailable original World/kind source; other original rejected branches. Every rejection increments **exactly one** diagnostic category.
- Record current and maximum consecutive **raw accepted ray evaluations**. This is explicitly **not** native-identity continuity evidence and never mints a 20-observation PASS.
- In original `Minecraft189RuntimeBridge` record separate high-water marks of the **unchanged actual identity-stable negative gate's** existing `killAuraDisabledTargetTicks` and `killAuraEnabledVetoTicks`. They are copied *after* the existing counters increment, cannot feed back into gating, and remain separate from raw ray streak. Bounded first / each 40th original callback log includes both kinds of counts and histogram. When the source kind snapshot is unavailable, source callback count and kind source availability are still reported.
- All counts are cleared at the already-existing original acceptance reset with the original World/test lifecycle. No new target, setter, raycast, packet, controller or World read. No change in the already-authoritative 20-disabled/80-enabled **consecutive same-entity** test, acceptance PASS marker, deadline or original server.

Eleven Java 8 JUnit tests validate all nine category groupings, exact accounting total, raw accepted streak resets vs max, independent snapshot values, actual reason/category integrity, reset and no forged original PASS. Synthetic source decisions are **offline diagnostic unit cases**. Their success is not evidence of original Minecraft GUI/server test success.

## Attribution limits and certification

A raw accepted streak of 25 across different native indices is explicitly **not** 25 original same-identity callbacks: only the existing bridge's unchanged strict native identity gate can certify that. Histograms discriminate repeated original crosshair misses from one-off source-index/kind faults when a *future real* V65 failure is captured; they do not by themselves identify original fixture geometry or fix a genuine gameplay/acceptance defect.

Preserve all true historical original failures: V276/V277 UUID/phase, V282 V46 rear-FOV, V283 second original V29 Scaffold, V286 V65 nonplayer. All remain failed regardless of later green runs. **Do not** extend timeouts, relax eligibility, invent PASS evidence, or represent offline source regression as native certification.

Exact-head certification: both Foundation Windows + Ubuntu 10/10 **and two independent complete original pinned Mojang Minecraft 1.8.9 X11 client + vanilla dedicated server 78/78 on identical SHA**. Draft PR, `main` untouched, no merge/release.
