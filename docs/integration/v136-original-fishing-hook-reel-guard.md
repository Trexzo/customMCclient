# V136 — original EntityPlayer fishing-hook owner guard for AutoRod

## Scope and ancestry

Cumulative draft child of V135 #601 at head `b9ca7b5bf3221f6b60d2d656f81b733f5e5caaee`. V132 #598 was the last full-certified baseline when this feature was prepared. V133–V135 Foundation CI passed; their complete exact-head original Minecraft 1.8.9 workflows were still running. Keep Draft, avoid individually merging overlapping PR branches and leave `main` untouched.

## New user behavior: Preserve Active Fishing Hook

Vanilla Minecraft 1.8.9 fishing-rod right-click performs **two different actions**: cast a hook when `EntityPlayer.fishEntity == null`, or reel it back when an existing `fishEntity` is active. A cooldown is not source authority to conclude the hook has disappeared. Previously AutoRod could use the next eligible synthetic right-click to retract the user's already-deployed fishing hook.

New persistent Combat ClickGUI boolean `combat.autoRod.preserveActiveHook` / **Preserve Active Fishing Hook**, default **false** to preserve existing behavior. With this option on, the original live source player must implement `Minecraft189PlayerFishingHookAccess.customMcHasFishingHook()` through the class-transformer. A non-null genuine `EntityPlayer.fishEntity` vetoes AutoRod at its pre-target/action scheduling stage, before hotbar selection, after any reentrant native slot setter and at the final `mayExecuteAutoRodUse` gate before the original right-click. Missing original player/accessor evidence fails closed. Manual vanilla right-click input, the actual game hook object and player hotbar remain untouched. Option is independent of V131 auto slot and V135 durability preference.

### Original 1.8.9 mapping authority

Source-mapped `EntityPlayer` `wn`, field `bG` with descriptor `Lur;`, `field_71104_cf` / `fishEntity` (actual `EntityFishHook`). Added to strict `Minecraft189Mappings` and `Minecraft189ClassShapeVerifier.verifyEntityPlayer`: if a pinned original client disagrees, the official original-client mapping preflight MUST fail. The class transformer now adds a read-only public boolean accessor directly on the actual `EntityPlayer` loaded class; the getter does not fabricate a hook or inspect animations, durability or last-action state. It emits an explicit Java 8 branch stack-map frame.

### Native admission and lease safety

The new host gate checks the exact original `tickHotbarPlayer` object. It is evaluated before an AutoRod action can be scheduled, before the source slot is selected, after setter callbacks which might create the hook, and just before original `Minecraft.rightClickMouse()`. A late hook transition triggers the **existing** V130 captured-owner rollback. A changed user slot is never overwritten; a foreign inventory/respawn cannot lend evidence. AutoPot, melee and ordinary manual right-click behavior is unchanged.

## Tests and original evidence

- JUnit existing source-host acceptance extended: default false retains historical behavior even with a hook, opted-in live hook denies selection, hook appearing after selection vetoes final use, reentrant hook creation in the original slot setter forces rollback, and clearing the hook allows another safe attempt. Includes absence of zombie selected-slot leases.
- SettingRegistry regression: default false / persisted true and lifecycle cleanup.
- Pinned original Mojang 1.8.9 graphical/X11 suite's existing mandatory Rod/Potion test additionally insists the genuinely transformed `EntityPlayer` implements the new fish-hook accessor and the original creative baseline has no hook. Requires `CUSTOMMC_OFFICIAL_189_ROD_NATIVE_FISH_ENTITY_EMPTY_BASELINE_PASS=YES`, then enables the guard for all original source Rod slot/final-use/GUI checks.

**Current evidence limitation:** The new original-client test validates mapped source class/field and a genuinely null live-hook baseline. The hook-active positive path is covered by stateful source-adapter JUnit but does **not yet** certify a cast fishing bobber from the real server. An actual original graphical X11 cast and server-confirmed live-bobber/reel veto remains a separate future acceptance gate; no false claim of that proof.

## Certification

Require exact-head Foundation CI Windows+Ubuntu SUCCESS and the **entire** pinned official Mojang Minecraft 1.8.9 binary/dedicated workflow SUCCESS, with all historical FOV, UUID, rejoin, PvP, Scaffold, item and physical GUI tests retained. Draft remains unmerged. No proof of a server fishing cast, packet outcome or potion healing is inferred.
