# V279 — trigger genuine Minecraft 1.8.9 CI for every platform production Java source

Parent: V278 Draft PR #749, corrected immutable SHA `c9d18f6455109745b6d803c8950e259f525d36d5`.

## Confirmed scheduling gap

Read-only inspection of the exact parent tree found 336 Java production files directly under `platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9/`; **89 did not match the original Mojang workflow's existing `pull_request.paths` allowlist**. Examples include actual AutoPot source helpers, KillAura target selector, GUI binding, native player state/access contracts, hotbar and hooks. The nested `input/` and `ui/` packages also require coverage.

A PR editing only one omitted production helper could pass Foundation while never scheduling the pinned real GUI/server workflow. This is a **CI scheduling defect**, not evidence that any of those helpers caused an in-game failure.

## Implementation and deterministic regression

Add one scoped trigger:

```yaml
- 'platform-1.8.9/src/main/java/**'
```

This covers all production Java source files under the platform module, including nested input/UI packages, without adding unrelated docs or root-level global wildcard triggers. Existing specific path patterns remain. The exact 78-step original Mojang binary/vanilla-server workflow and its strict native failures, validation criteria, timeouts and retry logic remain untouched.

Add four Python regression tests to the already-enforced offline workflow paths test:
- Every **actual** Java production file discovered under the platform module must match the real original CI PR filter, not an example-only whitelist.
- Eight previously uncovered representative source helpers must match.
- All actual nested input/UI package Java files must match.
- The scoped production pattern must remain present without including unrelated documentation or core paths via this new filter.

These regressions are offline scheduling checks, not Minecraft gameplay proof.

## Certification policy

Require exact-head Windows and Ubuntu Foundation 10/10 PASS plus **two separate complete 78/78 genuine original Minecraft 1.8.9 X11-client/dedicated-vanilla-server passes at unchanged commit SHA** before certifying V279. Preserve real historical V266/V270/V272/V274 failures and the unresolved intermittent runtime causes under issues #503 and #537. No gameplay implementation, packet, controller, World mutation or actual acceptance requirement changed.

All stacked PRs stay Draft; `main` untouched; no release/merge.
