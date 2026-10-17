# V143 — opt-in source-native AutoPot downward self-splash pitch

## Cumulative ancestry and problem

Draft child of V142 #608 at exact source parent `a4543095547be608c7c5f6ad7507046d99e275ad`. The old AutoPot path correctly checks original low health, currentItem source ItemPotion, V142 viable hotbar alternatives and live Regeneration, but then sends `Minecraft.rightClickMouse()` in whatever pitch the user currently looks. A splash thrown forward/horizontally can land far from the player and fail to heal. This batch addresses the **actual aim of the existing production AutoPot synthetic right-click**, not only another ranking option or fixture.

## New user-facing functionality

Combat ClickGUI exposes persisted `combat.autoPot.aimDownForSplash` / **Aim Down for Splash**, boolean **false by default**. This makes old configured-slot and Auto Detect behavior completely unchanged unless the user opts in. With it enabled, the original game's **already mapped** `Entity.rotationPitch` field (original 1.8.9 native field `z:F`, SRG `field_70125_A`) is temporarily set to **85 degrees downward** for just AutoPot's verified synthetic item right-click. The same player's original pitch is restored immediately in `finally`. Manual right-clicks, non-potion attacks, AutoRod, mouse input, yaw and other module rotations are never programmatically changed by this feature.

The pitch action is an **owned synchronous native lease** `Minecraft189AutoPotPitchLease`:

1. A genuine local EntityPlayer source must implement the already-existing original `Minecraft189PlayerRotationControl` getter/setter. Reject missing owner or native pitch outside finite [-90,90].
2. Obtain the V129–V142 same-player genuine currentItem + ItemStack + regeneration source/owner checks **before** moving pitch. The original AutoBlock release callback runs beforehand.
3. Set mapped pitch to exactly +85. Reject ignored source setters, reentrant pitch/currentItem changes and wrong/respawned owners; if an original setter mutates then throws, rollback if still owned and cancel synthetic right-click without crashing the client.
4. Immediately before **the actual original `Minecraft.rightClickMouse()`**, recheck the exact active pitch owner and exact source AutoPot item eligibility. Missing, mutated or foreign owner denies use and must not commit cooldown credit.
5. Restore pitch in `finally` before the existing AutoPot captured hotbar restore. If the player has changed pitch in the meantime, do **not** overwrite their genuine mouse input. Do not leak a lease into any subsequent tick or deferred task; repeated finally is idempotent.

This is no packet injection or Java fake potion. Source pitch comes from actual transformed Minecraft Entity. AutoPot's active Regeneration duplicate protection and V142 safe fallback still apply.

## Regression and original binary evidence

- `Minecraft189AutoPotPitchLeaseTest`: source original pitch -> +85 -> restore, double cleanup safe, retained manual user pitch, foreign source/respawn owner denial, no-write if already aimed down, ignored setter denial, invalid/out-of-range original pitch, and setter mutating then throwing restores without granting a use.
- `Minecraft189CombatItemUseBoundaryTest`: real-shaped HostRuntime original player/item/InventoryPlayer: default-off preserves pitch, opt-in selects legal source potion and temporarily sets +85, validates final original same-owner item and pitch, disallows mid-click manual pitch or hotbar changes, preserves user mouse/hotbar changes and revokes a reentrant original setter that mutates currentItem; existing next-use lease remains available.
- `Minecraft189AutoPotModuleTest`: SettingRegistry persisted encoded default false and true behavior under normal module lifecycle.
- Mandatory pinned graphical Mojang 1.8.9 original X11 `--native-rod-potion-discovery` extends the *same retained* V132–V142 original ItemPotion/PotionEffect/rod/fishEntity/GUI checks. After V142 native alternative healing proof, it takes an actual source Instant Health II lease, verifies original transformed same-player pitch getter and mapped setter reach exactly 85, checks actual final-use permission, and verifies the real original pitch **and** physical selected currentItem return to their original values in finally. The runner requires `CUSTOMMC_OFFICIAL_189_AUTOPOT_NATIVE_DOWNWARD_PITCH_RESTORE_PASS=YES` then prints `CUSTOMMC_OFFICIAL_189_REAL_AUTOPOT_DOWNWARD_PITCH_SOURCE_PASS=YES`. No prior native X11 or UUID markers removed.

## Evidence boundaries and release

The production synthetic right-click now runs while the original player looks down. This improves the chance of a splash landing at the player's feet; **source 85-degree aim by itself does NOT prove** the physical splash hit the player, that server health changed, or that a network server accepted the potion. The original GUI test proves pitch/currentItem and permission in a genuine world, but it does not actually throw a potion during that specific check. A later independent server-observed thrown-potion/health delta acceptance is required for that stronger claim.

Release gate: exact-head Windows+Ubuntu Foundation CI SUCCESS and **entire** pinned original Mojang 1.8.9 graphical/dedicated binary suite SUCCESS, including real two-live UUID server damage, original FOV/Scaffold and historical GUI, AutoRod physical cast/reel and Potion tests. Keep Draft, do not merge overlapping descendants individually, and leave main unchanged.
