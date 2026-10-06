# Rendering foundation

M9 introduces a renderer contract without OpenGL, Minecraft, UI toolkit or shader dependencies.

M10 wires the Minecraft 1.8.9 adapter to that contract through `ServiceRegistry`.

M11 moves structural render work off the per-frame hot path and gives pass registrations explicit lifetimes.

M12 adds explicit render-resource ownership so later OpenGL resources have deterministic cleanup.

M17 adds the backend-neutral HUD render pass.

M18 adds the Minecraft 1.8.9 UI translation boundary without importing Minecraft/OpenGL types into core.

M19 extends that translation boundary with rounded fills and outlines.

M20 adds exception-safe nested clip scopes without exposing GL scissor state to core.

M26 adds the retained ClickGUI as a normal HUD-stage render pass with a closed-state fast path.

## Goals

- explicit render stages;
- deterministic pass ordering;
- stable pass identity;
- frame-local immutable timing input;
- no hidden global renderer ownership;
- no browser requirement for ClickGUI;
- no direct Minecraft types in core;
- no repeated pass filtering/sorting during steady-state rendering;
- deterministic ownership for future GPU resources;
- UI composition isolated from the concrete graphics backend;
- scoped render state with deterministic restoration.

## Cached render plans and lifetime

`RenderPipeline` maintains one immutable plan per stage. Registering or unregistering a pass rebuilds only the affected stage.

Pass registrations and `RenderResourceRegistry` make render lifecycle explicit.

## HUD render bridge

`HudRenderPass` composes backend-neutral UI commands in the normal `HUD` stage.

`UiViewportProvider` owns the viewport boundary and `UiRenderer` owns command translation.

The generic flow is:

`Minecraft189Hooks -> RenderPipeline -> HudRenderPass -> UiRenderer`

## ClickGUI render bridge

`ClickGuiRenderPass` participates in the same deterministic `HUD` stage as other UI passes.

It snapshots retained ClickGUI state before resolving any rendering dependencies. When closed, it returns immediately. When open, it resolves `UiViewportProvider` and `UiThemeProvider`, composes commands, then invokes `UiRenderer`.

Pass priority determines ordering relative to the normal HUD pass; no second renderer singleton or special GUI rendering loop is introduced.

## Minecraft 1.8.9 UI backend boundary

The platform-side facades are:

- `LegacyViewportAccess` — current framebuffer dimensions and UI scale;
- `LegacyUiGraphics` — begin/end, shape/text operations and scoped clipping.

`Minecraft189ViewportProvider` reads the current display values on every render frame and creates the core `UiViewport`.

`Minecraft189UiRenderer` translates the currently supported core commands, including recursive `UiClipCommand` scopes.

Clip scopes call `pushClip` before nested commands and guarantee `popClip` afterward even if nested translation throws. The concrete backend will own logical-to-framebuffer scissor conversion and parent-clip intersection.

Command order is preserved. `end()` is guaranteed after a successful `begin()`, even when translation fails.

Unknown command types fail explicitly rather than silently disappearing. That makes future command additions versioned work at the backend boundary.

The actual Minecraft/LWJGL implementation of `LegacyUiGraphics` and `LegacyViewportAccess` remains a later integration milestone. This keeps the current source testable without committing Mojang classes.

## Platform routing

`Minecraft189Hooks` translates version-specific render callbacks into generic stages and obtains `RenderPipeline` through the explicit service registry.

Concrete GL state ownership, batching, font resources, shaders and post-processing targets remain later renderer milestones.

## M54 legacy host UI bridge

M54 adds `LegacyUiHostCallbacks` as the single host-facing callback contract for Minecraft 1.8.9 UI integration and `LegacyUiHostBridge` as the concrete adapter that implements both `LegacyUiGraphics` and `LegacyUiViewportSource` over that one host object.

The bridge owns frame-state validation: draw calls require an open UI frame, nested begins are rejected, clip underflow is rejected, and `end()` unwinds leaked clip scopes before resetting bridge state. This prevents a bad host callback path from leaving later frames in a permanently clipped/open state.

`Minecraft189ClickGuiRuntime.install(..., LegacyUiHostCallbacks)` constructs one bridge and supplies it to both the live viewport provider and renderer. Host framebuffer dimensions/UI scale and drawing therefore come from one integration object instead of separately wired adapters.

M54 still does not embed Minecraft/LWJGL classes in the platform module. The final injected/legacy host is responsible only for implementing `LegacyUiHostCallbacks` against the actual game/font/GL APIs.

## M56 unified legacy host runtime

M56 adds `Minecraft189HostRuntime` as the final platform-side composition façade over the already-certified hook/runtime pieces. Its installer accepts the attached `Minecraft189Platform`, module/setting presentation authorities, optional keybind authorities, and one M54 `LegacyUiHostCallbacks` instance.

The façade installs exactly one `Minecraft189ClickGuiRuntime`, then owns one `Minecraft189Hooks` and one M55 `Minecraft189HostInputBridge`. Host integrations can therefore forward tick, render-stage, mouse, wheel and keyboard callbacks through one object instead of manually composing those surfaces.

Ownership remains narrow: closing `Minecraft189HostRuntime` closes only the ClickGUI runtime it installed. It does not detach `Minecraft189Platform`, close borrowed module-keybind assignments, or remove unrelated services. After close, callback methods reject use deterministically and repeated close is idempotent.

The remaining final integration step is host-specific implementation of `LegacyUiHostCallbacks` plus forwarding the actual Minecraft/LWJGL callbacks into this façade.

## M57 framebuffer scissor mapping

M57 moves OpenGL scissor coordinate math into a tested platform utility instead of leaving it to the final host implementation. `LegacyUiFramebufferMapper` converts logical top-left `UiBounds` into bottom-left framebuffer `LegacyFramebufferRect` values using the live `UiViewport` scale.

Coverage is conservative: left/top edges use floor, right/bottom edges use ceil, then all edges clamp to the framebuffer. This preserves partially covered logical pixels and yields zero-area rectangles for fully off-screen clips without producing negative sizes.

`LegacyScissorStack` maps each pushed logical clip and intersects it with the currently active framebuffer clip. Push returns the active rectangle to apply to GL; pop returns the restored parent rectangle or `null` when scissoring should be disabled. Underflow is rejected.

The final LWJGL host therefore does not need to duplicate coordinate inversion, scale rounding, clamping or nested intersection logic; it only needs to apply the returned framebuffer rectangle to the actual scissor API.

## M58 scissor state minimization

M58 adds `LegacyScissorStateController` over the M57 mapping/stack utilities plus a minimal `LegacyScissorStateSink` host contract. The controller tracks the effective framebuffer rectangle already applied by the host and suppresses redundant state writes when a nested logical clip resolves to the same active intersection.

A real intersection change emits exactly one `apply(...)`; popping back to a different parent emits exactly one restore; leaving the outermost clip emits one `disable()`. `reset()` discards all retained nested clip state and disables scissoring once without replaying intermediate parent rectangles, making frame teardown cheap and deterministic.

The final GL callback implementation can map the sink directly to its scissor enable/box calls. M58 therefore moves both coordinate math and redundant-state suppression out of ad hoc host code while still leaving actual OpenGL ownership at the host boundary.

## M59 optional shape batching

M59 adds `LegacyUiBatchGraphics` as an optional renderer capability and `LegacyUiBatchHostCallbacks` as the matching host opt-in. Backends that do not advertise shape batching continue through the exact pre-M59 primitive call path.

When batching is supported, `Minecraft189UiRenderer` wraps only adjacent runs of two or more shape commands (`UiRectCommand`, `UiRoundedRectCommand`, `UiOutlineCommand`) in one `beginShapeBatch()/endShapeBatch()` scope. Text, clip scopes, unsupported commands and list boundaries always terminate the current batch, and batching recurses independently inside nested clips. Draw order is never changed.

Batch teardown is exception-safe: a shape failure still closes the batch before the frame-level `end()`. `LegacyUiHostBridge` also owns host batch-state validation, rejects clip/text operations while a batch is open, and closes a leaked host batch before clip/frame cleanup.

The concrete Minecraft/LWJGL backend can use these scopes to keep compatible shape state/geometry submission open across a run without forcing that optimization onto legacy or test backends.

## M60 GL-ready UI geometry

M60 adds immutable `LegacyUiGeometry` plus `LegacyUiPrimitiveMode` and `LegacyUiGeometryFactory` so concrete legacy GL code can consume tested packed XY geometry instead of rebuilding shape math inside OpenGL calls.

Rectangles use stable clockwise `QUADS` vertices, outlines use the same perimeter as a `LINE_LOOP`, and rounded rectangles use a convex `TRIANGLE_FAN`. Rounded-corner tessellation is deterministic: the factory owns an explicit segments-per-quarter value, defaults to 8, and rejects values outside 1..64. Radius 0 falls back to plain quad geometry.

Geometry defensively copies coordinates, rejects non-finite values, validates primitive vertex topology, and exposes indexed XY access plus a defensive packed-coordinate snapshot. M60 contains no GL calls and no Minecraft/LWJGL types; it is the geometry input layer for the concrete host renderer.

## M61 concrete LWJGL 2 host backend

M61 is the first concrete OpenGL implementation layer. The platform module now compiles against `org.lwjgl.lwjgl:lwjgl:2.9.3` as a non-transitive compile-only dependency; the launcher/client does not bundle a second copy of LWJGL or platform natives.

`Lwjgl2LegacyGlApi` implements the tested `LegacyGlApi` seam with GL11. It owns a scoped orthographic projection over logical UI coordinates, targeted server-state preservation through the attribute stack, blend/depth/cull/lighting/texture/alpha preparation, ARGB color conversion, line width, primitive begin/vertex/end, and framebuffer scissor application. Frame teardown restores model-view/projection matrices, the prior matrix mode, and pushed GL attributes; leaked primitive state is closed before restoration.

`Lwjgl2LegacyUiHostCallbacks` composes that GL API with the M57/M58 scissor controller, M59 batching scopes and M60 geometry factory. Consecutive batched shapes reuse prepared shape state, standalone shapes prepare it on demand, outlines apply their requested line width, and text switches to text-safe GL state before delegating to an injected `LegacyUiTextRenderer`.

Framebuffer dimensions/UI scale remain injected through `LegacyUiViewportSource`, and Minecraft font drawing remains injected through `LegacyUiTextRenderer`. This keeps Mojang classes out of the platform source while leaving only the final game-specific adapters/callback forwarding to bind.
