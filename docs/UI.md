# UI foundation

M13 introduces the backend-neutral UI command layer.

M14 adds logical viewport scaling, nine-point anchors and explicit HUD widget ownership.

M15 separates widget defaults from mutable/persistable HUD placement state.

M16 adds immutable layout snapshots, hit-testing and cancellable HUD drag transactions.

M17 connects HUD composition to the staged renderer.

M18 adds the Minecraft 1.8.9 translation boundary.

M19 adds rounded-rectangle and outline primitives for modern native HUD/ClickGUI surfaces.

M20 adds atomic clipping scopes for scrollable and nested retained UI.

M21 adds semantic theme tokens and immutable palette ownership.

M22 adds stable font handles and backend-neutral text metrics.

M23 adds explicit focus ownership and backend-neutral key routing.

M24 adds the retained ClickGUI shell state model.

M25 adds responsive backend-neutral ClickGUI shell composition.

M26 connects the retained ClickGUI to the staged HUD renderer.

M27 adds backend-neutral ClickGUI pointer and search-key interaction.

M28 adds the Minecraft 1.8.9 primitive input translation bridge.

M29 adds retained, clamped navigation scrolling and legacy wheel translation.

M30 adds explicit page-content ownership and the first live module-list content view.

M31 adds content-area pointer routing and module-row lifecycle interaction.

M32 adds stable setting presentation descriptors and the first read-only settings content view.

M33 adds validator-owned boolean setting interaction.

M34 adds bounded integer setting stepping through presentation metadata.

M35 adds bounded double setting stepping through the same numeric metadata.

M36 fixes end-to-end content pointer routing so right-click editors work through the real ClickGUI input controller.

M37 adds focus-owned transactional text setting editing.

M38 adds retained page-content scrolling shared by rendering and hit-testing.

M39 adds module presentation metadata without changing stable lifecycle identity.

M40 adds optional module categories and deterministic presentation ordering while preserving registration-order compatibility by default.

## Architecture

The UI remains native, in-process and backend-neutral. Rendering emits `UiDrawCommand` objects; platform adapters translate them.

`ClickGuiModel` owns retained shell state. `ClickGuiContentRegistry` owns page-content bindings with explicit registration lifetimes.

`ClickGuiComposer` is still a generic shell compositor. Registered page content receives an immutable `ClickGuiContentContext` for drawing.

`ClickGuiContentInputContext` contains the retained snapshot, selected page descriptor and logical content bounds. Controller-dispatched contexts also expose the backend-neutral `UiFocusManager` so interactive page content can participate in the same explicit focus authority as shell controls. The legacy three-argument context remains usable for read-only/direct content tests and carries no focus manager.

`ClickGuiPageContent.pointer(...)` defaults to no handling, so read-only page implementations do not need input code.

## Module page lifecycle and presentation authority

`ModuleRegistry` and `ModuleController` continue to use the exact stable `Module.id()` as module identity and lifecycle authority.

M39 adds `ModuleDescriptor` and `ModulePresentationRegistry` as a separate, optional UI-presentation layer. Descriptors carry a human-facing display name and description keyed by the stable module id. Registration rejects duplicate presentation authority and exposes an explicit closeable lifetime.

`ModuleListPageContent` resolves presentation metadata at composition/filter time. The display name is rendered when present; modules without descriptors retain their stable id as a compatibility fallback. Search matches stable id, display name and description.

M40 extends `ModuleDescriptor` with an optional category id and module priority. The legacy three-argument constructor remains valid and defaults to the `general` category with priority `0`. `ModuleCategoryDescriptor` and `ModuleCategoryRegistry` own human-facing category names plus category priority with the same explicit registration-lifetime pattern.

When category metadata is present, the module list renders category headers and sorts categories by category priority, then category id. Modules inside a category sort by module priority while equal-priority entries retain original `ModuleRegistry` registration order. With no category metadata, the previous ungrouped registration-order geometry is preserved. Search also matches category ids and category display names.

Pointer interaction never uses the display name as an identity key. Clicking a row still requests lifecycle changes through `ModuleController` with the underlying stable module id.

Left-clicking a visible module row requests lifecycle change only through `ModuleController`. Right/middle presses are ignored by module content even though the shell routes all content buttons:

- `DISABLED -> enable`;
- `ENABLED -> disable`;
- `FAILED -> disable` to return through the controller cleanup path;
- transitional `ENABLING` / `DISABLING` states are not mutated by the UI.

ClickGUI does not write a parallel enabled flag and does not invoke module callbacks directly.

## Setting presentation and edit authority

`SettingRegistry.snapshot()` exposes registration-order immutable setting membership while the `Setting<T>` objects themselves remain the live value/validation authority.

`SettingDescriptor` carries only UI presentation metadata: stable setting id, display label, explicit `SettingValueKind` and deterministic priority. `SettingPresentationRegistry` owns those descriptors with explicit registration lifetimes and rejects duplicate presentation authority.

`SettingListPageContent` joins the setting snapshot with registered descriptors for both rendering and row hit-testing, so search/order cannot diverge between what is shown and what is interactive.

Settings without a presentation descriptor remain absent from ClickGUI.

M33 allows a left press on a visible `BOOLEAN` row to request an inverted value through the existing `Setting<Boolean>.set(...)` method. The setting validator remains authoritative; a rejected transition leaves the value unchanged. No UI-owned value mirror or validator bypass exists.

M34 adds optional `SettingNumericSpec` metadata for numeric presentation bounds and step size. Integer descriptors validate that this metadata is integer-compatible. A left press steps a visible integer row upward and a right press steps it downward, clamped to the presentation bounds; the proposed value still goes through `Setting<Integer>.set(...)`, so the setting validator can reject it.

M35 applies the same left/right stepping contract to visible `DOUBLE` rows. The numeric spec clamps the proposed double value and `Setting<Double>.set(...)` remains the final validator.

Numeric descriptors without an explicit numeric spec remain read-only.

M37 adds transactional `TEXT` editing. A left press on a text row creates a temporary draft from the live setting and requests focus through the existing `UiFocusManager`. Printable characters and Backspace/Delete modify only that draft. Enter proposes the draft through `Setting<String>.set(...)`; validator rejection leaves the live value untouched and keeps focus so the draft can be corrected. Escape, another focus request, or input-controller shutdown cancels the draft without mutating the setting. While focused, composition renders the draft rather than inventing a second persisted value authority.

## Input routing

`ClickGuiInputController` owns shell-level routing: search focus, navigation selection, navigation wheel scrolling and root hit-testing.

For a pointer press inside the content region, it resolves only the selected page's registered content and supplies `ClickGuiContentInputContext`. Content receives left, right and middle button presses; the content implementation decides whether a row-specific action exists. Search and navigation actions remain left-click-only, while other button presses inside those shell regions are consumed without changing state.

Wheel input over navigation continues to use shell-owned navigation scroll. Wheel input over the content region is routed only to the selected page's content through `ClickGuiPageContent.scroll(...)`. M38 gives the module and setting list views independent retained/clamped scroll state; their render coordinates and row hit-testing derive from the same offset, so scrolling cannot desynchronize what is visible from what a click targets. Filter changes automatically clamp stale offsets against the new content height.

Shell clicks remain consumed inside the ClickGUI root even when page content has no row action, preventing gameplay click-through.

`UiPointerEvent`, `UiScrollEvent` and `UiKeyEvent` remain independent of Minecraft/LWJGL classes.

## Rendering boundary

`ClickGuiRenderPass` participates in the normal `HUD` render stage and forwards backend-neutral commands through `UiRenderer`.

The platform path remains:

`Minecraft189Hooks -> RenderPipeline -> ClickGuiRenderPass -> Minecraft189UiRenderer -> LegacyUiGraphics`

No concrete Minecraft, LWJGL, OpenGL, font-atlas or profile-storage object crosses into core UI.

## Next layers

Later milestones can add:

- module-owned setting association and per-module detail views;
- concrete host callback wiring into `Minecraft189InputHooks`;
- backend batching/state minimization;
- concrete legacy GL implementation.

Those layers should extend the existing ownership seams rather than bypassing them.
