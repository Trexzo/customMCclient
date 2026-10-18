# V188 — Strafe and Freeze original player-motion getter ownership

## Verification baseline
V181 SHA `b485a7bc8314f21e019af4122d1cdca280291bcf` passed two independently executed complete pinned Mojang 1.8.9 original client+dedicated-server 78-step CI jobs. V182 and V183 further completed successful exact-SHA 78-step original game jobs (respectively workflow #39129065961 and #39129376191). This does **not** automatically certify new V184–V188 source.

## Why this is a real source-code change
Strafe and Freeze are production movement owners writing to actual source-mapped `Minecraft189PlayerMotionControl` X/Y/Z. These original Java interface getters/setters can reenter module enable/disable, persisted GUI settings or physical input. Prior versions checked eligibility *before* reading real native player movement, but not after those calls. A cancelled or restarted Strafe could use an obsolete W/A/S/D and smoothing policy to write velocity; original Freeze could continue forcibly zeroing Y/Z after disabling or changing axis ownership inside a previous source read/set.

These are code-level reentrancy hazards, not a claimed empirical runtime crash.

## Actual production behavior
**Strafe**: per activation `nativeReadEpoch` increments at onEnable/onDisable. Capture physical W/S/A/D, Speed, Smooth Acceleration, Acceleration Percent, Ground Only and Pause While Sneaking policy before mapped original X getter. Revalidate current owner after X before original Z getter, after Z before any source write, and again after actual X setter before dependent Z. Existing genuine yaw and diagonal normalization, original mapped current motion blending, suspended/module focus veto and actual movement snapshot Ground/Sneak permissions remain unchanged.

**Freeze**: capture activation epoch, horizontal/vertical ownership and Pause While ClickGUI before mapping real source X, Y or Z. Recheck immediately after each mapped getter and setter, so an older callback cannot overwrite the next axis after an OFF→ON, horizontal/vertical setting edit or GUI ownership policy edit. Preserve independent axis settings, exact native zero comparison, GUI pause yielding original source without suppressing higher-priority Freeze ownership, and non-enabled/no-player no-read behavior.

No synthetic World state, packet, additional movement permission, target selection, server damage or altered original game acceptance.

## Fifteen offline regressions
New `Minecraft189StrafeFreezeNativeEpochTest` exercises Strafe original X getter OFF→ON, physical W release, Z getter live Speed/Ground Only edits, X setter module disable/Speed policy edit, valid diagonal and source-smoothing target, absent/suspended source; Freeze X getter OFF→ON or axis edit, X setter disable before Y/Z, Y getter disable, Y setter axis edit blocking Z, independent vertical-only source and ClickGUI Pause veto.

JUnit Java adapter regressions are offline code policy evidence, not genuine Minecraft native execution. V188 cumulative Draft child of V187 #653. Require **exact V188 head** Foundation Windows/Ubuntu, full pinned Mojang Minecraft 1.8.9 X11 GUI and vanilla dedicated-server 78-step original test suite, and independent same-SHA stability rerun before release. No merge, `main` unchanged.
