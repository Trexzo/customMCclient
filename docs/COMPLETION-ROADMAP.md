# Completing CustomMC: certified feature roadmap

## Status as of M349

The repository is a launcher-owned, clean-room Minecraft 1.8.9 client with a
native ClickGUI, persisted settings/keybinds and 62 earlier module implementations.
M349 adds Target Strafe as a new movement module. **A complete, field-verified
modern PvP client is not yet certified.** GitHub matrix CI only verifies source,
unit/integration fixtures and launcher smoke, not real server acceptance.

## Required completion lanes

1. **Install and runtime** — reproducible Windows launcher distribution,
   installation and game-classpath preflight, full Java/game startup, native
   loading, first-run settings, safe shutdown and update/rollback. Exercise a
   real installed 1.8.9 client with user-owned legal binaries.
2. **Core PvP** — KillAura, TriggerBot, AutoBlock, criticals, targeting/team/
   friend filters, CPS and item-use interoperability. These require proving
   existing **attack dispatch, hit-result, equipment, raytrace and hit timing**
   authority first. Do not claim features that only simulate a nearby target.
3. **Render/UI** — 2D/3D ESP, skeleton, box/outline, nametag, health and
   equipment overlays, target hit effects, drag editor, ClickGUI search/themes.
   Projection/depth/render state must be mapped and runtime tested before ESP
   is called complete.
4. **Movement/Player** — Target Strafe (M349), Scaffold, inventory assist,
   safe movement and other modules only after necessary collision/block/
   inventory hooks are proven. Target Strafe is snapshot-based and **not**
   wall/path aware; it makes no line-of-sight or anticheat bypass claims.
5. **Compatibility/quality** — legacy Java 8 game runtime, Windows packaging,
   correct focus/input, full settings profile roundtrip, error reporting
   behind debug flags, FPS and high-refresh UI testing.
6. **Release gate** — hosted Linux/Windows CI, launcher smoke, clean build,
   dedicated real-game acceptance matrix in singleplayer and permitted
   multiplayer, module interoperability, frame/heap regression and a
   reproducible release with checksums/rollback.

## M350 Ray-hit mapping authority

The pinned Minecraft 1.8.9 `joined.srg` mapping blob
(`0b1e3f1d0156abcbd70e2b09b720379fc0c1eae6`) identifies
`Minecraft.objectMouseOver` as `ave.s:Lauh;`, with
`MovingObjectPosition` `auh`, `typeOfHit` as `auh.a:Lauh$a;`
and `entityHit` as `auh.d:Lpk;`. M350 adds these to the mapping
registry, verifies the Minecraft objectMouseOver field and verifies the
hit-result class shape fail-closed at transformation time. It does **not**
yet read or act on a live hit result, introduce a TriggerBot, or claim a
verified player raycast. M351 adds a read-only
`Minecraft189CrosshairHitAccess` implemented on transformed Minecraft:
only non-null `objectMouseOver` with exact `MovingObjectType.ENTITY`
identity and an `EntityPlayer` instance returns true. Synthetic runtime
tests distinguish null, nonplayer, mismatched type, and player hits.
No automatic attacks are dispatched, and real-game acceptance is unverified.

## M349 Target Strafe behavior

Uses only mapped local position and nearest-player snapshots. With the module
enabled and W held (configurable), steers horizontal X/Z motion around a
nearest player: orbit radius, speed, clockwise direction, target range, and
ground-only restriction are live settings. Flight, Long Jump, Bunny Hop,
Movement Speed and Air Speed remain higher priority; Target Strafe takes
priority over plain Strafe only while it actually qualifies. It yields
without writing on missing target, invalid local state, tiny zero-radius
geometry, out-of-range target and incompatible movement ownership.
**No collision detection, line-of-sight guarantee or anti-cheat promise.**

## Development discipline

Use immutable pinned commits. One narrow change per PR; run exact hosted
Ubuntu+Windows Foundation CI including launcher smoke; only merge verified
successful heads. No fabricated Minecraft class mappings. Preserve existing
behavior by default. Record source and game acceptance separately.
