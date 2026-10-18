# V183 — persistent native Step height getter ownership

Parent V182 Draft #648 implements original Flight XYZ native-readback policy/lifecycle protection; V181 original Mojang Minecraft 1.8.9 actual binary 78-step suite completed success on immutable SHA `b485a7bc8314f21e019af4122d1cdca280291bcf` and the identical original job was explicitly re-requested on the same SHA for stability. Parent source confidence does not transfer to any new commit automatically.

## Production source problem, distinct from observed original failures
`Minecraft189StepModule` changes the original player entity's **persistent `customMcStepHeight` collision property**. It already restores vanilla .6 when a module is disabled, GUI owns focus or ground/sneak authority is missing. But the original mapped `customMcStepHeight()` getter can synchronously reenter a module lifecycle or the GUI settings. Prior code unconditionally proceeded to `customMcSetStepHeight` using policy from before that native getter. Such an old callback could force 1.8-height collision after its owner had been disabled, restarted or edited.

This is a **production static source correctness gap**, not a claimed empirical vanilla client crash.

## Implementation
- New `nativeReadEpoch`, incremented at **each** `onEnable` / `onDisable`.
- Snapshot the per-callback original collision-height policy: module epoch, Ground Only, Pause While Sneaking, Airborne Override, base height percentage and airborne height percentage before the real original getter.
- After the genuine mapped getter, compare the live epoch and **all** persisted settings. A change revokes the old boost and exits rather than writing a stale value.
- **Exception for safety restoration:** if the module actually became disabled while its old native read was executing, a persistent non-vanilla (or unknown) Step height is explicitly released to **vanilla .6**, not kept boosted and not forced to an old 1+ height. An OFF→ON cycle is a new owner; it receives no stale setter and the next independent real source callback applies it.
- Previously existing real World movement vetoes, GUI focus restoration, normal / airborne percentage, original native getter/setter and no-redundant-write behavior are kept for valid live policy.

## Offline test coverage
Ten new JUnit interface-adapter regressions: actual getter OFF→ON, same-getter disable and vanilla restore, changed base height, Ground Only edit, Sneak edit, Airborne Percent edit, NaN source + disable, GUI vanilla restoration, unchanged valid source default, unavailable/no-original-player gate. Offline Java policy tests are not genuine Minecraft GUI/collision runtime proof.

Cumulative Draft child of V182 #648. Require **exact V183 head** Windows+Ubuntu Foundation, full pinned original Mojang 1.8.9 X11 graphical and vanilla dedicated-server tests (78-step pass) and repeat. Until then `main` unchanged and no binary certification transferred from V181.
