# V194 — original Minecraft FOV GameSettings transaction and native tick owner

## Immutable parent/source evidence
The original Minecraft 1.8.9 X11 graphical + vanilla dedicated-server 78-step suites successfully completed at exact V181 twice and individually for V182–V189. V190 original pinned suite **failed** a genuine GL Box ESP framebuffer pixel assertion (despite correct remote UUID and HUD draw); V193's independent source-rooted retry fix is still under full original game verification at time of authoring. No previous native original pass can be transferred to V194.

## Production source defect
The real `Minecraft189FovModule` uses the mapped original Minecraft `GameSettings.fovSetting()` getter/setter and listens to true client ticks to apply the current persisted FOV GUI setting. Before V194 its `onEnable()` read/overrode the true original FOV before installing the event subscription and setting `active=true`. An original getter or setter callback could reenter `onDisable()` while `active` remained false; disable would return and the old enable callback could then commit an obsolete FOV override/subscription. A native setter that threw after applying the override could leave it installed without an active module. On normal disable, it also unconditionally wrote the old pre-enable FOV even if a native user preference had been changed since then, unlike source-correct Gamma/Bobbing ownership.

These are real Java source transaction/reentrancy gaps, not assertions that they occurred in a native game crash.

## V194 implementation
- Explicit `enabling` / `disabling` transaction flags and monotonic `nativeSettingsEpoch` to revoke a synchronous original FOV getter/setter reentrant disable, and reject recursive onEnable before baseline corruption.
- Capture only the finite **actual mapped original FOV**, not a synthetic value. Require same activation epoch before issuing the first override and before attaching a real tick listener.
- If an original setter mutates GameSettings and then throws or a module disable cancels the unfinished enable transaction, roll back **only** the exact module-injected value still present, preserving any distinguishable concurrent user FOV edits. Close incomplete subscriptions.
- Track **last module-injected FOV** before real tick setter, permitting reentrant native disable during that setter to restore the exact owned FOV. An external user's later native FOV preference is preserved at disable if it differs from the module's last injected override; normal user-configured live target changes continue to apply on actual client ticks.
- Existing FOV bounds (30–179), normal FOV target 110, event subscription cleanup and real controller/world native ray/FOV tests stay intact. No fake GL frames, world entities, packets or relaxed acceptance.

## Offline JUnit regressions
Eleven adversarial tests of the same mapped Minecraft GUI settings adapter: original getter disable cancellation, setter disable after mutation, setter throws after write, native user FOV edit retention during active/disabled or cancelled owner, recursive getter/setter enable veto, nonfinite source and recovery, exact changed target on real tick, original setter's tick-time disable, tick-unsubscription, repeated original preference ownership. These are **offline source-interface policy tests**, not Mojang runtime certification.

Draft cumulative child V193 #659. Require V194 exact-head Foundation Windows and Ubuntu, complete pinned genuine Mojang 1.8.9 graphical X11 + dedicated server 78-step suite and independent same-SHA repeat before promotion. All PRs Draft, `main` unchanged.
