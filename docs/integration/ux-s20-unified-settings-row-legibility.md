# UX S20 — Unified source-rendered ClickGUI setting rows

## Branch and isolation
S20 is stacked on [S19 visual PR #849](https://github.com/Trexzo/customMCclient/pull/849), exact parent commit `6b4c257c3de395a2c3428ece25690b9dd2fea766`. It is **not** stacked on combat PRs and does not alter combat modules, vanilla controller input, keybind assignments, settings validators, or source-original 1.8.9 runtime mapping.

## Why this changes actual presentation
The previous general Settings and Module Details views rendered labels and values as unconstrained text commands over the same row. A long name/setting value could visually cross the value pocket, Boolean switch, or opposite text. Boolean settings also rendered the value as literal `true`/`false` next to the actual Boolean switch.

## Native source changes
- Both pages now call `UiSettingRowVisuals.compose`, a shared pure row renderer.
- Row labels are clipped within a bounded left region. Values are independently clipped in a right-hand pocket. No guessed 1.8.9 font widths are required to protect control regions, and the selected native resource-pack font can be wider without causing overlap.
- Boolean settings show only their native styled switch, while the actual `Setting<Boolean>` remains authoritative and bound to the existing `SettingEditorController`.
- Disabled/value-in-edit color roles continue to derive from the retained Graphite theme; numeric progress indicators continue to derive from authoritative values via the existing `UiSettingDecorations`.
- At small GUI sizes, zero/negative horizontal clip areas are omitted and narrow rows skip the separate value-well background instead of drawing an illegal rounded rectangle.
- All click/scroll/editing/keybind hitboxes, validators, focus contracts and setting values remain unchanged.

## Tests and required acceptance
- `UiSettingRowVisualsTest`: clipped long label vs long value, Boolean representation and geometry over narrow widths.
- Adapt `ModuleDetailPageContentTest` and `SettingListPageContentTest` to recurse through real clipped text-command groups, and assert Boolean render is not redundant.
- Exact-head Foundation CI on Windows + Ubuntu must pass, plus original SHA-verified Minecraft 1.8.9 binary preflight.
- Real in-game screenshot and short UI regression walkthrough remain separate requirements: small/large GUI scale, long names/resource-pack fonts, keyboard bind capture, numeric editing, theme cycling, scroll, and the native GL clip stack.

## Visual references
- [Vape quick start](https://docs.vape.gg/intro/quick-start): compact control association, distinct module/details interactions
- [Slinky](https://slinky.gg/): restrained charcoal foreground/background hierarchy
- [Rise](https://riseclient.com/): configurable rich settings without requiring gradients/glow

The result is native Java UI behavior, not a website mockup or a screenshot-based fake.

## Follow-on integration
Parallel [font width PR #848](https://github.com/Trexzo/customMCclient/pull/848) is independently stacked on S18. It is not overwritten by S20. When both UI paths pass exact tests, reconcile fonts and ClickGUI in an explicit follow-up PR; no cherry-pick into the independent combat stack.
