# S17 — No Nebula identity

This is a real production-code UI change stacked on S16 [PR #838](https://github.com/Trexzo/customMCclient/pull/838).

## Direct reference observations
- **Slinky** (official slinky.gg product interface): understated graphite surfaces, warm orange active controls and minimal visual clutter; no giant identical HUD tiles. Reinterpret the spacing and compact toggles rather than copying icons or branding.
- **Vape V4** (official docs.vape.gg/intro/quick-start): narrow category navigation, lean module rows, unobtrusive per-module affordances and fast settings navigation. Do not inflate the whole GUI into a web-app dashboard.
- **Rise** (official riseclient.com and beta.riseclient.com): grouped module/settings navigation, tight setting rows, readable numeric values and unit labels. Adapt to real supported Java 1.8.9 renderer capabilities; no fabricated icon/font/shader pipeline.

## Implemented in production
- New **Graphite** default uses neutral charcoal #101113/#191B1F/#23262B with subtle warm #EE8755 active accent rather than the old purple/cyan look.
- Distinct **Blue**, **Mono**, **Frost** optional presets; live theme registry still powers ClickGUI, ArrayList and TargetHUD.
- No selectable or rendered `nebula` theme. Profiles saved with legacy `nebula` decode to `graphite`, via a bounded migration in the registered theme codec. The old API method and byId alias remain only for source/binary compatibility, both returning the new Graphite palette.
- Settings presents the field as **Appearance**, and displays descriptive preset names rather than technical IDs. Clicking cycles choices through the existing validated/persisted setting controller.
- TargetHUD proximity track now uses the selected theme's border color. No blue residue in actual default HUD rendering.
- Fixes earlier S16 exact-head compact TargetHUD test: check for the correct observed distance text without assuming the expanded three-line display.

## Explicitly NOT complete
No measured native font-width API, custom typeface, motion/animation, real target name/HP/avatar, or genuine game screenshots yet. All still require source/runtime acceptance. No gameplay/combat/input/World/hooks changes in this UI lane.

CI policy: Windows and Ubuntu Foundation and official Mojang 1.8.9 binary must pass at the exact commit; keep failures visible. Stay Draft, never merge to main without genuine on-screen UI acceptance.

## ArrayList minimal preset
Source-rendered ArrayList now defaults to **no backing rectangles** with compact measured-estimate text and the slim accent edge, closer to Slinky's minimal HUD ethos. The optional persisted **Row Background** switch enables tight per-label translucent backplates. The setting is registered with correct cleanup and profile persistence; width sorting and strict enable/disable semantics are unchanged.
