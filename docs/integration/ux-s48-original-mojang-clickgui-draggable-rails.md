# UX S48 — actual draggable native ClickGUI scrollbar

## Real reference and usability issue

The S47 real, original Mojang Minecraft 1.8.9 ClickGUI screenshots at 854×480 show a compact dark Graphite Modules page (85 modules) and Settings page (397 controls). Following restrained Slinky/Vape/Rise visual references, both pages already draw a subtle 2px scroll track and 3px thumb, but the rail had **no pointer behavior**; the only way to traverse long lists was repeated mouse-wheel scrolling. This is particularly inconvenient on the global Settings page.

## Implementation

- Keep the original **2px track, 3px thumb, native theme, rows, search, typography, page size and saved profile values** visually unchanged. S48 converts the existing rail into a *real* draggable/seekable control, without adding another widget or an overlay.
- One canonical `UiContentScrollRail` geometry determines the existing native drawing, 12px invisible mouse hit corridor, actual thumb bounds and mapping of pointer travel onto scroll offset. The hit corridor is inside the unused right margin, away from original module toggle/detail and setting editor hitboxes.
- New isolated page-local `UiContentScrollDrag`: pressing a visible thumb retains its original grab point (no jump), pressing the empty track seeks directly to that region, MOVE drags, RELEASE ends capture even outside content (the already-existing host/controller dispatch forwards releases outside the viewport).
- Bounded `ClickGuiContentScrollState.setOffset` reuses the authoritative page-local scroll state and existing clamp. Search filtering and viewport-size changes clamp the resulting scroll; no new mouse wheel behavior or background timers.
- Source Modules and Settings pages consume rail pointer events **before** source module toggling, navigation and setting editing. Right clicks are unchanged. No combat, packet, module state or setting value changes from rail gestures.
- Does not alter the native Java8 renderer, GLSL/GL state, font atlas, visual colors or persisted GUI settings.

## Validation

- Deterministic 854×480 real-content viewport geometry: press thumb at 75% of its height and verify zero jump; drag to exact end and verify maximum scroll; release beyond panel and confirm later moves do not scroll; right press and no-overflow have no drag effect.
- Source Modules integration with 30 actual registered modules: rail press, move, release, scrollbar thumb visible at changed offset, and **all source module states remain DISABLED**.
- Source Settings integration with 30 actual registered setting values: same press/move/release, scrollbar thumb follows, **all settings values unchanged**.
- Existing search, mouse-wheel scrolling, module detail action, numeric settings editing, bottom footer click-through and keyboard ownership regression suites stay required.
- Require exact-head Foundation CI on Ubuntu and Windows, plus original Mojang Minecraft 1.8.9 binary preflight. The default screenshot confirms unchanged *idle visual design*; the pointer drag is proven by the above runtime tests, not a static image.

S48 remains a separate **draft/unmerged** PR stacked on S47 #897. Only core source ClickGUI presentation/input files and tests were changed; combat code and visuals under other concurrent tracks are untouched.
