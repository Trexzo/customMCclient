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
