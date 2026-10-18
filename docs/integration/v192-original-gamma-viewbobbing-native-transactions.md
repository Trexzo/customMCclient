# V192 — original Minecraft Gamma and View Bobbing native settings transactions

## Baseline distinction
V181 passed two independent complete 78-step genuine Minecraft 1.8.9 X11 GUI + dedicated-server tests on its exact SHA; V182–V188 each have at least one such exact-SHA run. V189 and V190's genuine binary tests and V191's new original suite are separate and must be checked. Successful offline tests never certify native game behavior for V192.

## Production source risk and invariant
The production `Minecraft189FullbrightModule` and `Minecraft189NoBobbingModule` use actual **original Minecraft GameSettings** getters and setters: Gamma and View Bobbing. They capture the real user's previous setting, apply an original settings override, and restore only their own exact override at disable. Existing code also rolled back native setter throws *after* mutation and preserved a concurrent user preference where detectably different.

However, during `onEnable()`, the external mapped getter/setter can synchronously reenter `onDisable()`: previously `active` remained false until after the setter, so `onDisable` returned without cancelling the unfinished write, and the old `onEnable` subsequently committed `active=true`. Reentrant nested onEnable during source read could also capture a different native baseline or trigger an additional override. These are source reentrancy hazards, not observed Mojang client failures.

## V192 implementation
- Both modules have explicit `enabling` / `disabling` states and a `nativeSettingsEpoch`.
- Each original getter and setter is performed within a concrete native settings transaction, with the epoch advanced before getter; an original setter is not even attempted if a reentrant disable cancelled the read, and an incomplete setter may not commit `active=true` after cancellation.
- Reentrant onEnable while another enable/disable transaction owns the native settings is rejected before changing the captured user baseline.
- Cancellation or a source setter that **mutated before throwing** rolls back only when the exact module-owned overridden Gamma (`16.0F`) or View Bobbing (`false` with prior `true`) is still present, retaining externally changed user preferences when distinguishable. Rollback exceptions are attached as suppressed to the original failure rather than hiding its cause.
- On real disable, old transaction epoch is invalidated before using the mapped original settings getter and setter. The already-owned Gamma/Bobbing override is restored only if its specific injected value remains current. GUI preference ownership and existing module-controller close behavior remain unchanged.

No packets, fabricated Minecraft settings state, invented gamma values or original FOV/controller changes.

## Thirteen adversarial offline regressions
New `Minecraft189NativeDisplaySettingsTransactionTest` uses the actual original GameSettings Java adapter interface to verify Gamma getter disable, setter disable after mutation, concurrent native user edit, partial setter failure, recursive enable rejection, changed preference preservation, nonfinite Gamma getter; and Bobbing getter disable, setter disable after mutation, concurrent change, partial setter failure, original false preference retention, recursive enable rejection.

This is a cumulative **Draft child V191 #657**, inheriting original persistent Timer native 1x restore and the prior real movement source changes. Require exact V192 Foundation Windows/Ubuntu plus full pinned Mojang Minecraft 1.8.9 78-step genuine binary GUI/dedicated-server suite, then same-SHA independent repeat before merge. Preserve all vanilla GameSettings/World/UUID/controller/ray/FOV/server score gates. `main` unchanged.
