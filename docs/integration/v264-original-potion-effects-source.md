# V264 – original Minecraft potion effect host source identity

Parent V263 Draft [PR #734](https://github.com/Trexzo/customMCclient/pull/734), exact `ff258495bbfc8d174059edc79c87df895dcc4115`.

## Source gap
`Minecraft189HostRuntime.playerPotionEffects` formerly called the actual mapped `player.customMcPotionEffects()` and published effect records from `Minecraft189PlayerPotionEffectsState.update` without a typed exact original local-player owner. `update` invokes four native getters **per effect**: potion ID, duration ticks, amplifier and effect name. One of the calls may synchronously reenter with another/null original player or throw while the outer call still holds a stale source. Invalid native IDs/durations/amplifiers/names, null array/record or thrown getters could also retain previously published HUD potion effects.

## Change
Add typed `Minecraft189ExactSourceLease<Minecraft189PlayerPotionEffectsAccess>` owner, entered before mapped array retrieval, checked after the original array getter and after all actual effect getter calls have assembled the immutable snapshot. Nested same player is nonduplicating, changed/null source irrevocably revokes the in-flight publication; any thrown native RuntimeException/Error clears previously published potion effect state and propagates unchanged. The existing effects state already constructs and sorts an immutable local list before publishing it; preserve that transaction, native effect validation, real sorted name/ID order and the valid zero-effects case. No spoofed PotionEffects, new native mapping, World/entity packet, GUI/gameplay setting, original controller, acceptance shortcut or mutation.

20 additional deterministic Java 8/JUnit tests cover original owner reference identity and revocation, genuine original values, the empty-array case, null sources, array and four individual getter reentries with same/different/null players, failures/errors and invalid genuine effect fields, and recovery. These are source-only regressions, not genuine original-game evidence.

## Native acceptance
Require current exact-head Windows+Ubuntu Foundation, full pinned original Mojang Minecraft 1.8.9 X11 client + vanilla server 78/78, then independent unchanged-SHA 78/78. All prior genuine failure results remain recorded. All PRs Draft, `main` unchanged.
