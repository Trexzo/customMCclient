# V137 — genuine Mojang 1.8.9 physical fishing cast, hook guard and reel

## What changed

V136 #602 source-mapped the original EntityPlayer.fishEntity field and gated automated AutoRod against an active hook, but its pinned original-world acceptance covered only a null hook before a cast. V137 adds a mandatory **positive** end-to-end original-client acceptance with actual X11 mouse/keyboard. Draft stacked directly on V136 exact head `13c14cb53e4d65bfd4d4198e29252670f6dd8069`. No changes to main or unrelated multiplayer/UUID logic.

## Verified source events in official binary CI

The preexisting mandatory `--native-rod-potion-discovery` GUI/integrated-server test retains V132–V136 source item/potion/rod/GUI tests and additionally:

1. After passing the physical Right Shift open/close test and normally disabling Rod/Pot, Java scans genuine original InventoryPlayer.mainInventory for the best actual ItemFishingRod and prints one numeric source hotbar slot (1–9). It must differ from the original selected slot, or the test fails closed rather than claiming unobserved physical input.
2. The Python graphical runner parses the unique source slot from JVM output and sends the corresponding physical X11 1–9 key to Minecraft. The original Java callback independently requires actual source currentItem, ItemFishingRod and a still-null fishEntity.
3. Python sends a physical right mouse click within the actual Minecraft window, without any Java-created hook, guessed packet or manual inventory rewrite. Java must observe that the original transformed EntityPlayer.fishEntity on the same player becomes **non-null** and currentItem remains the same. This is a positive, actual vanilla cast source witness.
4. Java re-enables AutoRod through the normal production ModuleController while the real hook is active. V136's persisted Preserve Active Fishing Hook setting must block production HostRuntime.selectAutoRodSlot() with return -1 and no hotbar write. This proves the feature acts on an actually live native hook.
5. Python sends a second physical X11 right click for **manual vanilla reeling**. After the original hook disappears, Java must require that normal source AutoRod selection, final-use eligibility and owned finally restore work again, then normally disable the modules.

Mandatory new markers are `CUSTOMMC_OFFICIAL_189_NATIVE_CAST_ROD_SLOT=1..9` (source input), `CUSTOMMC_OFFICIAL_189_NATIVE_ROD_PHYSICAL_SLOT_SELECTED_PASS=YES`, `CUSTOMMC_OFFICIAL_189_REAL_PHYSICAL_ROD_CAST_ACTIVE_HOOK_GUARD_PASS=YES`, `CUSTOMMC_OFFICIAL_189_REAL_PHYSICAL_ROD_REEL_HOOK_CLEARED_PASS=YES` and overall `CUSTOMMC_OFFICIAL_189_PHYSICAL_FISH_ENTITY_CAST_GUARD_REEL_PASS=YES`. All older original binary and GUI markers remain mandatory.

## Deterministic bytecode regression

In addition, Minecraft189MappedHostTransformationTest invokes the real transformed/source-shaped EntityPlayer fishing-hook getter on a dynamically loaded class through the original mapped field while it transitions null -> non-null -> null. This rules out an accessor hardcoded to false and checks the emitted Java 8 bytecode access with a real-shaped loader.

## Evidence boundary and release

This is actual original Minecraft GUI physical input and real client-side fishEntity presence (stronger than JUnit mocks). It does NOT claim remote dedicated server bobber replication, bites, physics trajectory, PvP target disruption, or healing potions. Require exact-head Foundation Windows+Ubuntu SUCCESS **and full pinned original binary/dedicated suite SUCCESS**, not just a single original step. Preserve historical UUID/FOV/Scaffold/GUIs. Keep PR Draft, main unchanged, no overlapping child PR merge.
