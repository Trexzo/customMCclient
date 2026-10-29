# UX S30 — actual 1.8.9 Target HUD readability (Graphite)

## Visual sources and acceptance limits

Reference direction remains the user's Library analysis of Slinky/Rise/Vape: Slinky's restrained in-world HUD, Rise's text-first hierarchy and Vape's compact utility controls. There is no current verified set of all three clients' UI binaries/screenshots in Library; this PR takes *design cues*, not implementation or proprietary assets. The source-grounded baseline is S29 [#871](https://github.com/Trexzo/customMCclient/pull/871), which passed **Windows/Ubuntu Foundation + the complete original Mojang Minecraft 1.8.9 native suite** on exact commit `1a8d85cfe97915184b42a8b379fe2256d2791b69`.

## What actually changes

- Preserve real-source-player target admission: same exact mapped nearest-player index and squared source distance as the target-angle snapshot. The UI must never make up a name, target health, reach or line of sight.
- Source-rendered Target HUD stays in its **same persisted x/y position** and the same `142x38` compact, `164x56` expanded, and coordinates-expanded card dimensions. No changes to native target selection, combat, or rendering lifecycle.
- Reduce visual clutter: remove the second, redundant top highlight line. Retain exactly one subtle outline and the existing thin, state-grounded left accent rail. Card contents remain compact with no heavy glass/blur/purple Nebula decoration.
- Improve typography hierarchy with the **existing original Minecraft font**: `NEARBY  02` acts as a subdued small label, true `9.0 m  away` becomes bright readable primary information, and optional true yaw/pitch and target coordinates stay muted. The distance meter remains bounded and sourced from the same verified position, not inferred health.
- Native GL scissor clips all text inside the actual card's 12px insets. Leave the lower edge reserved for the proximity meter and always pop the clip in `finally`. This guards long localized text/coordinates without font replacement or changing the real target values.

## Verification

The extended `Minecraft189TargetHudModuleTest` checks the original active target's exact display strings and state, left accent integrity, one outer outline, matching native clip push/pop, the 118x30 clip for an actual 142x38 compact card and theme-authentic muted-label / primary-distance colors. Existing tests continue to validate compact/expanded dimensions, original source target matching, native location, distance meter values, theme switching, and no render when target evidence is stale.

The existing screenshot CI creates an authentic original *singleplayer* world. It captures ClickGUI, Modules and Settings, but does **not** put a second real remote player in view or enable Target HUD; therefore a green screenshot step there is NOT independent visual proof of the Target HUD itself. A later authentic two-player screenshot or equivalent actual-player rendering stage must be attached before premium HUD quality can be visually certified. No synthetic/mock screenshot is claimed as real gameplay evidence.

This stacked draft PR only touches Target HUD UI painting, its tests, and this note. No source in the independent S26 mouse-focus lane or concurrent combat/native-resolution code is modified.
