# V219 — genuine original AutoSneak native setter reentrancy guard

## Reproducible source defect

`Minecraft189AutoSneakModule.apply` used the available original Minecraft player movement snapshot and four persisted GUI vetoes, but dispatched `EntityPlayer.setSneaking(true)` without protecting its native call stack. A mapped setter can synchronously invoke the host movement callback before vanilla updates the actual player sneaking bit. The nested callback sees the old `sneaking=false` snapshot and may issue another `setSneaking(true)`, recursively. Disabling then re-enabling the module inside the original setter does not remove the risk.

## Actual production change

Add `nativeSneakDispatching` as a same-stack guard. Reject nested player sneak setters while the original callback is active, including OFF→ON, settings-edit, and different original-player reentrancy. Use `try/finally` to release the guard even if the genuine mapped setter partially mutates the real player and then throws. Preserve existing physical user sneaking, current GUI focus, W/WASD, ground and sprint gates; this version NEVER invents an `unsneak` rollback or changes vanilla/player/network state through a mock.

## Regression and certification boundary

13 targeted Java 8/JUnit adversarial tests in `Minecraft189AutoSneakNativeDispatchTest`: genuine positive, same-player and cross-player nested native calls, OFF→ON, GUI edit during native write, throw-after-mutating recovery, reentrant throw recovery, original GUI veto, missing source movement, physical Sneak preservation, four opt-in gates, disabled state and W-not-inferred legacy path.

These are source-adapter unit tests, not proof of a real original Minecraft client or server state. Require exact-head Windows and Ubuntu Foundation, all 78 original Mojang 1.8.9 X11 graphical + vanilla dedicated-server gates, then an independent same-commit repeat before certification. The V213 parent lineage and other previous 78/78 results cannot certify this new source. Keep Draft; do not merge to main.