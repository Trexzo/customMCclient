# UX S51 — actual source-native numeric controls in global Settings and Module Details

## Verified current UI and reference rationale

The original Minecraft 1.8.9 source ClickGUI uses a compact dark Graphite layout (S22 X11 captures) with row-based settings inspired by the restrained controls in Vape, Slinky and Rise. Prior to S51, integer/double settings had a 132px dark value well, a 120px source-theme progress line, and an accurate hint `LEFT + / RIGHT -`. It was **not** a functional slider: pointer handling stepped the value once on clicks anywhere on the row. Users navigating large global Settings or a contextual module with many numeric controls could not drag from a value to another. This task adds a real direct-control behavior instead of merely styling a dummy slider.

## S51 implementation and boundaries

- A single shared `UiNumericSettingScrub` handles **actual numeric value well** left presses in both `SettingListPageContent` and `ModuleDetailPageContent`. The value well (x=row.right−143 through row.right−11) and inner progress track (x=row.right−137 through row.right−17, 120px) are the **pre-existing coordinates**. The first press updates the setting immediately; pointer MOVE sets a direct value along the same track; RELEASE ends capture, even when returned outside the page.
- Snap values to the real `SettingNumericSpec` minimum/step/maximum, and clamp both endpoints. Integer and double settings are supported only with a genuine numeric descriptor, correctly typed live setting, finite positive span and adequate row geometry. Non-integral maximum is still reachable at the far-right endpoint.
- Every setting write calls the existing authoritative `Setting.set(...)`, preserving source validators (an impossible value remains rejected). The operation cannot change any other setting, module enabled state, keybind capture, combat owner or game packet.
- **Legacy semantics are preserved elsewhere**: left-click outside the numeric well increases one step, right-click anywhere in the numeric row decreases one step, Boolean toggles and text editors still use their existing handlers. Search and row sorting, mouse wheel, S48–S49 page scrollbar drags and S50 fixed module identity remain unchanged.
- The native progress line gets a tiny 6×6px source-theme knob **inside the existing value well** at the actual setting ratio, including 0% and 100%; no new asset/font/shader/GL mechanism. The truthful help hint becomes `DRAG BAR / LEFT + / RIGHT -`. No settings row size, hitbox or window dimensions change.

## Regression gates

- Unit: real 0–100 integer step 5 scrubs 0/50/75/100, clamps beyond both ends, RELEASE kills capture. Real decimal 0.5–2.0 step 0.25 scrubs 1.25/2.0, unaligned 0–9 integer step 4 still reaches 9 exactly. A validator that rejects >3 must stay 3 even on attempted 100%. Missing specs, nonnumeric controls and narrow rows never begin scrubbing.
- Global Settings integration: genuine source page x200–800, first numeric row y158–196, value well x633–765; drag and release beyond content updates only setting, old left/right step semantics remain, native 6×6 knob drawn.
- Module Details integration: genuine source module registry owns A's numeric setting; drag at original y202–240 updates setting, switching to B mid-drag cancels capture without changing A again, switching back preserves the last valid setting value and row-step behavior.
- Update existing visual tests to assert 6×6 bounded knob and new truthful hint, keeping all original clipping, native theme, Boolean switch, read-only setting and original S22 visual checks intact.
- Require exact-head Windows + Ubuntu Foundation and complete original Mojang Minecraft 1.8.9 Binary Preflight. Unlike S48–S50, **S51 intentionally changes numeric setting rows** in the native Settings screenshot: new small knob and help hint are expected; unchanged global Modules / in-game HUD and all unrelated UI must be compared separately. A screenshot of the default view does not establish functional dragging, so source pointer integration tests remain necessary.

S51 is a separate **draft/unmerged** PR stacked on S50 #901. All edits are in the **core ClickGUI presentation and input** plus tests/documentation; zero combat/network/plugin/client-jar changes. Source references are existing production native 1.8.9 GUI implementation and genuine S22 original-game screenshots, not copied external client assets.
