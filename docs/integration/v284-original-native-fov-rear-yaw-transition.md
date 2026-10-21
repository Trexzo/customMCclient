# V284 — same native FOV rear witness to rejected-click yaw provenance

Stack parent: V283 Draft PR #754 exact immutable SHA `cbe33c7566aec66d4617ad3a8387ae38b3c4198a`.

## Genuine original failure motivating this scope

V282 Draft #753 original [run #39620847862](https://github.com/Trexzo/customMCclient/actions/runs/39620847862), job `119293490333` **FAILED** on a real pinned Mojang 1.8.9 GUI + vanilla server during V46 strict rear-player FOV acceptance, not during the V282 source-level UUID JUnit additions.

Original client error: `IllegalStateException: out-of-FOV real player caused synthetic attack`.

Exact native log evidence:
- At earlier original-source observation: player yaw **0.0**, real target yaw **−180.0**, yaw difference **−180.0**, native selector empty, rotation unavailable; Aura had been enabled, **16** stable FOV-veto callbacks already observed, genuine world/source alignment true.
- At original attack exception: same genuine remote player target yaw **−180.0**, but player yaw had changed to **−180.0**, so native yaw difference now **0.0**. `independentlyPermits=true`, selection and rotation both reported the remote index 1, actual source distance 2.236, configured FOV 90.0. The strict negative acceptance correctly **threw**, but current FOV predicate would admit the target at this newly observed yaw.
- It is not yet proven whether original Minecraft/server teleported/rotated the player, game input or production module changed yaw, or timing exposed an original source transition. The V282 PR was only a JUnit/doc change, so its code changes do not establish causation.

## Purely observational evidence improvement

In **only** `Minecraft189RuntimeBridge`'s opt-in `custommc.acceptance.reportKillAuraFovVeto` acceptance observer, retain the last actual native player yaw, callback tick and remote index when a true **rear-player** original-source observation is valid *after* Aura has been enabled. These are copied from already-read native snapshots used by the original acceptance; never change any source, motion, module, target, selector or click.

On the **existing unconditional failing original unexpected-click branch**, append `lastVerifiedRearYaw`, `lastVerifiedRearTick`, `lastVerifiedRearIndex` and `nativeYawShiftCause` before throwing the **same** original exception. The classifier uses finite yaw and modulo wrap, returns a neutral `original-native-yaw-changed-120-plus`, `original-native-yaw-changed-under-120`, `original-native-yaw-stable` or `original-native-rear-yaw-provenance-unavailable`. A yaw difference demonstrates a source transition, **not what caused it**. **No second World/native getter at rejection**, no new listener, and no attack authorization.

Eight Java 8 JUnit cases cover 0→−180 and reverse, wrap near ±180, unchanged/small drift, missing witness, non-finite readings and no forged native success marker.

No original FOV geometry/reach, 20+80 native evidence gates, real controller/packet, native X11/dedicated server, original 78 workflow steps, timeout, retry, acceptance PASS, or ability to attack is altered. The original V282 FAILED native run must remain FAILED. Strict exact-head certification requires both Foundation OS 10/10 and two independent unchanged-SHA full genuine original 78/78 runs. Issue #503 tracks failures; all PRs Draft, main unchanged, no merge/release.
