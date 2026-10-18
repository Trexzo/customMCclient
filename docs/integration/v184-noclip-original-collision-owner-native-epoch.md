# V184 — original NoClip native collision baseline owner and restoration epoch

## Evidence and scope
V181 immutable parent `b485a7bc8314f21e019af4122d1cdca280291bcf` passed full [Mojang original 1.8.9 X11/dedicated server workflow #39126842096](https://github.com/Trexzo/customMCclient/actions/runs/39126842096) (78 successful job steps) and both Foundation OS jobs. The same unchanged original job was independently re-requested for stability; a second PASS must not be asserted before its conclusion.

This child is cumulatively stacked on V182 Flight original player Y/X/Z getter/setter policy epochs and V183 persistent Step height getter/safe vanilla restore. Those production modifications require separate exact-head CI; do not transfer a parent test success.

## Actual Java production owner-lifetime risk
`Minecraft189NoClipModule` already has a source-owned original player baseline, restores only its OWN forced true when baseline was false, keeps an original true owner untouched, rejects old player baseline on respawn, and preserves baseline over OFF→ON before the next native callback. Those are important invariants retained.

Previously `player.customMcNoClip()` was called to acquire or restore a baseline **before** checking that the same module activation and GUI Require Sneaking policy still owned the native read. A source getter may synchronously reenter `onDisable`/`onEnable` or `requireSneaking.set`. The pre-reentrancy code could capture a now-obsolete baseline and write `noClip=true` after module OFF, or restore `noClip=false` over the user's freshly reenabled owner.

## V184 implementation
- Add `nativeReadEpoch` to NoClip, increment on every module enable/disable.
- Before first original native noClip getter, capture the current activation epoch and Require Sneaking policy. After source getter, recheck current module epoch, policy and actual supplied movement permission **before mutating baseline ownership**. A stale first getter leaves neither baselineCaptured nor a forced setter.
- After the second genuine source getter, recheck epoch, Require Sneaking and unchanged exact native player baseline owner before setting true. An old OFF/ON or GUI policy read cannot force noClip.
- In `restoreCaptured`, capture same original player owner, original baseline and epoch before original getter. After native read, do not issue a stale false setter or discard the new player's baseline when a callback has restarted module or changed baseline ownership. Re-check after original native setter in case it reenters module lifecycle.
- In the disabled pending restore path, clear pending **only if the same disabled native lifetime still owns it**; otherwise keep the new pending lease until a subsequent actual original-source callback.

No synthetic packets, new world player objects, fake collision values or new permissions. Genuine GUI focus release, Hold Sneak release, respawn owner partition and true baseline preservation remain mandatory.

## Offline regression
Ten Java JUnit original-interface fake-adapter tests cover first native getter OFF / OFF→ON; second getter disabled; mid-read Require Sneaking policy change; restore getter turning module ON; repeat-disable mid restore; setter true then disable; original physical GUI focus; never overriding true baseline / user false change; and preserving source player-identity ownership across respawn. Tests are **offline Java policy**, not original game collision runtime proof.

Draft stacked above V183 PR #649, V182 PR #648, and V181 #647. Require exact V184 head Foundation Windows/Ubuntu, complete original pinned Mojang 1.8.9 X11 + vanilla dedicated-server 78/78 and repeat stable SHA. No main changes or merge.
