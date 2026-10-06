# Architecture

## Foundation rules

The project is launcher-owned and version-aware. Minecraft integration is deliberately kept outside the core.

### Modules

- `launcher` — process/runtime ownership, installation discovery, metadata resolution, integrity checks, native staging, command construction and launch preflight.
- `core` — version-independent events, modules, settings, lifecycle and services.
- `platform-api` — narrow contract between the core and a game-version adapter.
- `platform-1.8.9` — version-specific in-process adapter boundary for Minecraft 1.8.9.

## Bytecode boundary

`launcher` is built with Java 21.

`core`, `platform-api` and `platform-1.8.9` compile with `--release 8`. This preserves compatibility with a legacy-compatible in-process runtime while the launcher uses a modern JVM.

## Explicit ownership

Foundation state is explicit:

- `ModuleRegistry` owns module identity.
- `ModuleController` owns module lifecycle state and exposes failed transitions instead of hiding them.
- `ModuleKeybindRegistry` owns optional module-to-key-chord associations; `ModuleKeybindController` translates bound presses into the existing lifecycle authority.
- `ServiceRegistry` owns shared cross-module services by contract type.
- `EventBus` returns explicit subscriptions that must be closed.
- `PlatformContext` receives these owners rather than discovering global state.
- each `GamePlatform` has an explicit attach/detach lifecycle.

Module key chords are backend-neutral stable key ids plus Shift/Control/Alt flags. A module may own at most one active chord and a chord may target at most one module. Closing the registration releases only that association. Keybind presses never invoke module callbacks directly; they route through `ModuleController`, including the existing `FAILED -> disable` cleanup path.

The permanent architecture must avoid:

- reflection-based module discovery;
- generated event-bus source;
- global mutable singleton state without lifecycle ownership;
- config writes hidden inside setting/module setters;
- a browser engine as a mandatory ClickGUI dependency;
- JNI/JVMTI injection when the launcher already owns process creation.

## Minecraft 1.8.9 boundary

M8 establishes `platform-1.8.9` without importing or committing Mojang classes.

The adapter owns only the version-specific in-process boundary. Hooks publish typed events into the core through `PlatformContext`. Later integration work may bind those hooks to the actual game runtime, but Minecraft implementation types must not leak into `core`.

## Rendering direction

Rendering now uses a staged backend-neutral pipeline with cached stage plans, explicit resource ownership and retained UI command composition. `platform-1.8.9` translates those commands through a narrow legacy host boundary rather than leaking Minecraft/LWJGL types into core.

M54-M56 provide the guarded host UI bridge, shared-viewport input bridge and unified host runtime façade. M57-M58 add deterministic logical-to-framebuffer scissor mapping plus redundant-state suppression. M59 adds optional order-preserving shape batch scopes, M60 adds validated packed geometry, and M61 supplies the concrete compile-only LWJGL 2 GL11 renderer/state implementation. The remaining host-specific work is now limited to Minecraft 1.8.9 viewport/font adapters and forwarding real game callbacks into the certified façade.

## Minecraft acquisition

The repository does not contain Mojang/Minecraft source or game binaries. The launcher resolves and verifies a legitimate installed runtime before launch.

## Module input direction

M48 establishes the core module-keybind authority without coupling it to ClickGUI or Minecraft input classes. The core controller accepts a deliberate chord press and owns only bind lookup plus lifecycle routing.

M49 adds `Minecraft189ModuleKeybindRuntime`, which publishes one `ModuleKeybindController` through the attached platform `ServiceRegistry` with an explicit managed lifetime. `Minecraft189InputHooks.key(...)` gives an installed ClickGUI first chance to consume the translated event, suppresses module binds while that ClickGUI model is open, ignores repeat/release for module activation, then translates the remaining press into a `ModuleKeyChord` using the existing backend-neutral key id and modifier flags. Keybind routing therefore works even when ClickGUI is not installed, and runtime teardown removes only the keybind service.

M51 adds `ModuleKeybindAssignments` for deliberate dynamic rebinding without weakening the explicit registration model. It owns only registrations it creates, refuses to take over or remove externally-owned binds, preflights chord conflicts before releasing the current bind, and restores the previous owned binding if an unexpected replacement registration fails. `close()` releases only its owned assignments and is idempotent.

## Launch process ownership

M62 turns launch preflight into an actual launcher-owned child-process lifecycle without introducing shell command construction. `LaunchProcessRunner` passes the immutable argument vector from `LaunchCommand` directly to `ProcessBuilder`, creates the configured game working directory, and inherits standard input/output/error by default.

A successful start returns `LaunchProcessSession`, which owns both the child process and the associated `LaunchPreflightResult` native-staging lifetime. Staged natives remain present while the process runs. `awaitExit()` cleans them only after child termination; `close()` requests normal termination, escalates to forcible termination after a bounded grace period, and then cleans staging. Start failure cleans staging immediately. Repeated close/cleanup is idempotent.

M62 still launches the resolved Minecraft main class exactly as produced by the existing command builder. Custom client bootstrap/classpath overlay remains a separate follow-up so process ownership and in-process integration are not conflated.

## Runtime launch overlay

M63 adds `LaunchRuntimeOverlay` as the explicit launcher-owned seam for putting CustomMC runtime artifacts ahead of the resolved Minecraft classpath and, when needed, handing process entry to a bootstrap main class. Existing launch/preflight constructors default to `LaunchRuntimeOverlay.none()`, preserving the original Mojang main class, classpath and game-argument ordering when no overlay is requested.

Overlay classpath entries are normalized, ordered, immutable and duplicate-rejected. During command construction they are prepended ahead of the resolved Minecraft artifacts, allowing later CustomMC/bootstrap classes to have deliberate classpath precedence. An optional main-class override and immutable main-argument prefix are emitted before the unchanged expanded Minecraft game arguments; sensitive game-argument indexes are offset after the prefix so access-token redaction remains exact.

`LaunchPreflight` carries the overlay end-to-end and requires every overlay classpath entry to exist as a regular file or directory before native staging begins. M63 does not yet provide the bootstrap artifact itself or transform Minecraft classes; it only establishes the deterministic, preflight-certified command surface those later pieces require.
