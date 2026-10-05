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

## Architecture

The UI remains native, in-process and backend-neutral. Rendering emits `UiDrawCommand` objects; platform adapters translate them.

`ClickGuiModel` owns retained shell state. `ClickGuiContentRegistry` owns page-content bindings with explicit registration lifetimes.

`ClickGuiComposer` is still a generic shell compositor. Registered page content receives an immutable `ClickGuiContentContext` for drawing.

M31 adds a separate `ClickGuiContentInputContext` for input. It contains only the retained snapshot, selected page descriptor and logical content bounds.

`ClickGuiPageContent.pointer(...)` defaults to no handling, so read-only page implementations do not need input code.

## Module page lifecycle authority

`ModuleListPageContent` reads module identity from `ModuleRegistry` and lifecycle state from `ModuleController`.

Rendering and row hit-testing share the same filtering and row geometry inside `ModuleListPageContent`. A retained search query therefore cannot make the visible row and interactive row disagree.

Left-clicking a visible module row requests lifecycle change only through `ModuleController`:

- `DISABLED -> enable`;
- `ENABLED -> disable`;
- `FAILED -> disable` to return through the controller cleanup path;
- transitional `ENABLING` / `DISABLING` states are not mutated by the UI.

ClickGUI does not write a parallel enabled flag and does not invoke module callbacks directly.

## Setting presentation ownership

`SettingRegistry.snapshot()` exposes registration-order immutable setting membership while the `Setting<T>` objects themselves remain the live value/validation authority.

`SettingDescriptor` carries only UI presentation metadata: stable setting id, display label, explicit `SettingValueKind` and deterministic priority. `SettingPresentationRegistry` owns those descriptors with explicit registration lifetimes and rejects duplicate presentation authority.

`SettingListPageContent` joins the setting snapshot with registered descriptors at composition time. Settings without a presentation descriptor remain absent from ClickGUI, which lets internal/runtime settings stay intentionally hidden.

The retained search query matches setting ids and labels case-insensitively. Current values are read live from `Setting<T>`; M32 does not mutate settings, bypass validators, or persist profiles.

## Input routing

`ClickGuiInputController` still owns shell-level routing: search focus, navigation selection, navigation wheel scrolling and root hit-testing.

For a pointer press inside the content region, it resolves only the selected page's registered content and supplies `ClickGuiContentInputContext`. The content implementation decides whether a row-specific action exists.

Shell clicks remain consumed inside the ClickGUI root even when page content has no row action, preventing gameplay click-through.

`UiPointerEvent`, `UiScrollEvent` and `UiKeyEvent` remain independent of Minecraft/LWJGL classes.

## Rendering boundary

`ClickGuiRenderPass` participates in the normal `HUD` render stage and forwards backend-neutral commands through `UiRenderer`.

The platform path remains:

`Minecraft189Hooks -> RenderPipeline -> ClickGuiRenderPass -> Minecraft189UiRenderer -> LegacyUiGraphics`

No concrete Minecraft, LWJGL, OpenGL, font-atlas or profile-storage object crosses into core UI.

## Next layers

Later milestones can add:

- validated setting editors built on the M32 descriptors;
- content-region scrolling;
- module metadata beyond stable ids;
- concrete host callback wiring into `Minecraft189InputHooks`;
- backend batching/state minimization;
- concrete legacy GL implementation.

Those layers should extend the existing ownership seams rather than bypassing them.
