# V266 — original Ping and mutable hotbar source ownership

**Parent:** V265 Draft PR #736 commit f3a391961d8f6c81ad120a4c1bda0ec78be9d6ef.

## Concrete source problem
`Minecraft189HostRuntime.playerPing` previously performed a genuine native player ping getter and published its value without exact native player ownership or exception clearing. A native getter that synchronously reentered with a different/null original player could publish obsolete ping even though the source had been revoked. Ping < 0 remains unavailable as before.

`playerHotbarSlot` previously assigned `tickHotbarPlayer=player` and `tickHotbarControl` from a real inventory before the native `customMcSelectedHotbarSlot()` finished; both getters could synchronously reenter with another/null source and the outer callback could still publish its stale hotbar slot. The hotbar control is more than a HUD field: it owns subsequent real AutoPot/AutoRod/AutoWeapon slot writes. An invalid getter/error could leave that control as a stale reference.

## Production changes
Add typed `Minecraft189ExactSourceLease` wrappers for original `Minecraft189PlayerPingAccess` and `Minecraft189PlayerInventoryAccess`. For Ping, publish only nonnegative original measured milliseconds whose exact reference owner remains valid at getter return. Any exception/Error clears stale ping, propagates unchanged and releases lease. Nested same player never consumes a duplicate getter; changed/null nested source invalidates old outer.

For Hotbar, clear the previous tick inventory ownership on first callback, read native player inventory, check exact source ownership, then read genuine native selected slot and validate its unchanged 0..8 range under the SAME owner. Publish `tickHotbarPlayer`, mutable native `tickHotbarControl` and selected-slot snapshot only after both getters succeed under the unchanged player; on invalid or revoked source leave everything unowned. On native getter RuntimeException/Error explicitly clear mutable owner, HUD state and rethrow; release source lease. Never mutate a slot here.

20 new deterministic Java 8/JUnit tests cover genuine values and original native getter count, same/different/null reentry, exact source lease and recovery, negative ping and out-of-range slot, exceptions/errors at both hotbar getters and stale state clearing. Original mappings, source methods, controller actions, module knobs, gameplay, timers and strict native 78-step acceptance unchanged. Require exact-head Windows/Ubuntu Foundation, genuine pinned original Mojang 1.8.9 X11 + vanilla server 78/78 and independent unchanged SHA full 78/78. All PRs Draft, main unchanged.
