# V258 — original local-player Health native source lease

**Parent:** cumulative V257 Draft PR #728 exact `59ee22fbee9ad245d044ac81c2c856eb475ea06e`.

## Concrete production gap
`Minecraft189HostRuntime.playerHealth` reads the original mapped `customMcHealth()` and `customMcMaxHealth()` separately without an owner spanning both native calls. A native getter can synchronously reenter HostRuntime with a different or null local player, after which the prior outer player publishes a mixed, stale health/maxHealth pair. Getter and value-validation failures previously also preserved stale health state.

## Implementation
- Add typed `Minecraft189HealthHostSourceLease` using existing exact-reference V251 `Minecraft189ExactSourceLease<S>` semantics.
- Admit one original player at callback entry; nested same-player callback does not start a second sample, changed/null original source revokes pending sample.
- Check same exact source after **each** original mapped getter and before publication. On mismatch, null original callback, native exception or invalid original value clear old Health source evidence. Propagate original `RuntimeException` or `Error`, release guard in finally.
- Preserve the existing `Minecraft189PlayerHealthState` validation: finite health/maxHealth, positive maxHealth; negative/zero finite original health remains accepted. No invented maxHealth cap, no new original native mapping, no GUI/gameplay setter or Combat/Render policy edits.

## Tests and evidence
26 new Java 8/JUnit source-shaped tests: identity-only lease, revocation, nested callbacks, real HostRuntime integration, changed/null source during both getters, native exception/Error, nonfinite or invalid values, null clearing and recovery. They do not prove actual original game behavior.

**Must verify exact V258 SHA:** Windows+Ubuntu Foundation, real pinned Mojang Minecraft 1.8.9 X11 graphical client + vanilla dedicated-server full 78-step original preflight, then a second independent full same-SHA 78/78 before original certification. Retain all earlier genuine source/scoreboard/FOV/UUID failures, never relax acceptance. All PRs Draft, main unchanged.
