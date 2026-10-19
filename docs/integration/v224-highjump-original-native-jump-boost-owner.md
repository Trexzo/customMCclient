# V224 — HighJump original native jump and follow-up motion owner

## Source defect
The cumulative HighJump implementation remembered `spaceWasPressed` only in the outer `applyJump` finally block, **after** the native original `EntityPlayer.jump()` callback. A mapped native jump can synchronously reenter the host before that callback returns, observing the original unconsumed physical Space edge and dispatching a second jump on one key press. Original motion getter/setter callbacks also need a single-player, single-stack owner for the already-pending boost; an old read must not spend the permission of a restarted module, different original player, or suspended host.

## Actual source changes
- Guard the mapped `customMcJump()` with `nativeJumpDispatching` through `try/finally`, including OFF→ON and native-throw-after-motion. Do not reset the guard in onEnable/onDisable. A nested call cannot borrow the same Space edge, even for a different original player. A released physical Space during native jump does not enqueue a later boost.
- For the genuine native `motionY` boost phase, guard original getter and setter with `nativeBoostDispatching` pinned to the source player. A nested changed original player or suspension revokes outer transaction; existing lifecycle epoch and target speed edits still veto stale reads. Reject nonfinite native Y rather than inventing a finite boost from invalid movement evidence. An already stronger original upward jump remains untouched.
- Consume each pending boost once, with only genuine Space, onGround and optional WASD permitted. On native exceptions, release same-stack guard without synthetic undo of vanilla jumping or Y motion. Preserve original user GUI, controller and vertical module priority.

## Verification boundary
20 Java 8/JUnit original-shaped adapter regressions cover true Space edge/boost, reentrant original jump same/other player, restart, physical Space release, native jump throw, GUI movement policy edit, nested Y getter/setter, other original player, suspension, source lifecycle and speed edit, native motion getter/setter exception, nonfinite Y, stronger native upward motion and held Space/WASD. They are **offline unit evidence**, not an authentic Mojang binary run.

Cumulative Draft child of V223. Require exact V224 Windows+Ubuntu Foundation, complete 78-step genuine Mojang Minecraft 1.8.9 X11 graphical + vanilla dedicated-server suite and a second unchanged-commit native repeat. Keep historical native failures, Draft state and `main` unchanged.
