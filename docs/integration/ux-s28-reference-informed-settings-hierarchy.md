# UX S28 — Graphite Settings hierarchy, actual controller semantics

## Reference evidence and constraints

The user Library contains earlier reference-client research (including a source-grounded Vape V4 vs Slinky technical comparison and Vape/Slinky/Rise visual design notes), but the Library search did **not** return verified current client screenshot binaries for all three vendors. The comparison explicitly notes Vape is official-docs-only and the user's recovered Slinky Java bundle was not verified as an official signed client. We therefore derive UX patterns from those **documented references**, not claim visual/technical parity with proprietary binaries or copy asset/code.
- Vape: compact navigation, small controls, directly discoverable module configuration
- Slinky: restrained HUD and low visual noise in dense configurations
- Rise: supporting text hierarchy and appropriately separated state/value controls
- CustomMC: own Graphite identity with no purple Nebula, copied graphics, shaders or external runtime dependencies

The *actual* original Minecraft 1.8.9 screenshot artifact from S27 [#40843184933](https://github.com/Trexzo/customMCclient/actions/runs/40843184933), ID `12883398118`, shows the improved two-line module list and the still-boxy Settings page. Screenshot and archival stages passed. The **entire native preflight eventually failed at separate V29 genuine survival controller placement**, so source-wide original Minecraft acceptance remains RED. Windows/Ubuntu Foundation for that exact S27 head passed.

## Actual implementation

1. Shared `UiSettingRowVisuals` now uses a quiet 1px row divider instead of outlining every individual setting. While editing a TEXT setting, it renders a focused Graphite accent outline. It uses the **same** original 38px row bounds and underlying editor.
2. Rows wide/tall enough include a muted secondary line that tells the actual controller interaction:
   - BOOLEAN: `CLICK TO TOGGLE`
   - INTEGER/DOUBLE: `LEFT + / RIGHT -` (the controller is step-based; this is NOT a drag-to-set slider)
   - TEXT: `CLICK TO EDIT`; focused text: `ENTER SAVE / ESC CANCEL`
   - `ui.theme.preset`: `LEFT NEXT / RIGHT PREVIOUS` (handled by actual `UiThemeSettings.cycle`).
   Hints disappear at narrow widths so neither the setting name nor native value/switch is overdrawn; both are separately clipped.
3. Numeric `UiSettingDecorations` now places the **read-only normalized fill witness** inside the native 132px value well, not under the entire label area. It preserves clamp/bounds and never promises drag support. Actual state/validator/numeric `SettingNumericSpec` and pointer authority are untouched.
4. `SettingListPageContent` now provides a count of **currently filtered** settings, an unobtrusive header cue and an explicit empty-search state, without moving any row bounds or changes to wheel/footer clipping.
5. Both the global Settings tab and per-module detail settings use this **same shared renderer**, so the improvements do not diverge into separate skins. No edits to module/combat/runtime/FontRenderer code and no persistent-profile migration.

## Tests and acceptance

- Updated `UiSettingDecorationsTest`: actual fill geometry stays inside value well at 1% and 100% state; zero and narrow cases remain safe.
- Extended `UiSettingRowVisualsTest`: bool/numeric/text/theme/focused hints, no false drag instruction, narrow viewport and independent clipping.
- New `SettingsVisualHierarchyTest`: authoritative filtered counts, empty search, actual step-up/step-down by unchanged real first-row x/y coordinates.
- Existing content/scroller/validator/focus tests remain authoritative. The S28 PR must pass exact-head Windows and Ubuntu Foundation CI and the original Minecraft 1.8.9 screenshot capture before claiming real rendered visual success. A full original-game red result in unrelated combat/scaffold stages must be reported, never hidden.

This branch stacks on S27 [PR #868](https://github.com/Trexzo/customMCclient/pull/868) exact head `8c4d6472a753d442e27f314b8fd77617b554ac31`. S26 [#863](https://github.com/Trexzo/customMCclient/pull/863) is an independent native focus lane, not included here. Other combat/font branches and `main` remain unchanged.
