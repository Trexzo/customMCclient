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


## Runtime bootstrap artifact

M64 adds a dedicated Java-8-compatible `bootstrap` artifact with manifest main class `dev.trexzo.custommc.bootstrap.CustomMcBootstrapMain`. The bootstrap contract is deliberately explicit: argument 0 is the original resolved Minecraft main class and every remaining argument is forwarded unchanged to that target `static void main(String[])`.

The bootstrap resolves only that explicitly supplied class through the thread context class loader. It does not scan for modules, discover hooks by reflection, transform Minecraft bytecode, or install global state. Target exceptions are unwrapped so launcher/process diagnostics reflect the original game failure rather than a reflective wrapper.

`CustomMcBootstrapOverlay.create(...)` is the launcher-side factory for M63. It prepends the bootstrap artifact to the classpath, overrides process entry to the bootstrap main, and prefixes the original Mojang main class ahead of the unchanged game argument vector. The bootstrap module is included in `verifyFoundation`, so its Java-8 bytecode/tests participate in the same Ubuntu + Windows certification gate.

M64 establishes executable bootstrap handoff only. In-process CustomMC runtime installation and Minecraft hook binding remain later bootstrap phases rather than hidden inside argument parsing.


## Explicit bootstrap runtime initialization

M65 extends the executable M64 bootstrap with an optional, explicitly named runtime initializer. Initialized invocations use the prefix `--custommc-runtime <initializerClass> <targetMainClass>`; legacy M64 invocations that begin directly with the target main class remain valid.

The initializer class is resolved only by its exact supplied name and must implement `BootstrapRuntimeInitializer` with a public no-argument constructor. It receives an immutable `BootstrapContext` containing the already-parsed target main class and a defensive copy of the original Minecraft arguments, and it returns a non-null `BootstrapRuntimeSession`.

That session owns the in-process CustomMC lifetime around the target game main: initialization completes before Minecraft main is invoked, and session close runs after the target returns or throws. Target failures remain primary; a cleanup failure is attached as suppressed evidence rather than replacing the game/runtime failure.

`CustomMcBootstrapOverlay.createWithRuntime(...)` constructs this protocol explicitly. The classpath order is bootstrap artifact first, followed by ordered runtime artifacts, then the resolved Minecraft classpath from M63. There is still no classpath scanning, module discovery, bytecode transformation, JNI/JVMTI agent, or implicit global bootstrap state.


## Minecraft 1.8.9 bootstrap runtime assembly

M66 provides the first concrete implementation of the M65 runtime-initializer contract: `Minecraft189BootstrapInitializer`.

Initialization constructs one owned in-process runtime graph before Minecraft main executes:

- `EventBus`;
- `ModuleRegistry` + `ModuleController`;
- `ServiceRegistry`;
- `RenderPipeline`, published as a managed service;
- `SettingRegistry` + setting presentation registry;
- module presentation/category registries;
- explicit module-to-setting ownership registry;
- module keybind registry + M51 assignment owner;
- attached `Minecraft189Platform`;
- installed `Minecraft189ModuleKeybindRuntime`, publishing the keybind controller service.

`Minecraft189BootstrapRuntime` is the M65 session object and owns teardown. Closing it removes the keybind-controller service, closes only bootstrap-owned keybind assignments, removes the render-pipeline service, and detaches the 1.8.9 platform. Repeated close is idempotent. Construction is rollback-safe: a failure after partial attachment unwinds the pieces already installed before surfacing the error.

M66 intentionally has no global runtime holder and does not yet install ClickGUI, LWJGL host callbacks, Minecraft event hooks, modules, settings, or class transformation. Those require the next explicit hook/binding layer rather than being hidden inside bootstrap assembly.


## Canonical Minecraft 1.8.9 runtime bundle

M67 gives the launcher one canonical bundle definition for the executable M64-M66 runtime path. `Minecraft189RuntimeBundle` owns exactly four normalized, distinct CustomMC artifacts in classpath order:

1. `bootstrap.jar`;
2. `core.jar`;
3. `platform-api.jar`;
4. `platform-1.8.9.jar`.

The bundle fixes the concrete initializer to `dev.trexzo.custommc.platform.v1_8_9.Minecraft189BootstrapInitializer` and produces the initialized M65 overlay for the resolved Mojang main class. `fromDirectory(...)` maps the stable assembly filenames directly, avoiding launch-time filename/version reconstruction.

The root `assembleRuntimeOverlay` Gradle task builds those four module jars and copies only those artifacts into `build/runtime-overlay` using the stable names above. `assembleRuntimeOverlay` is a Gradle `Sync` task, so its destination is reconciled to exactly those four stable jar names on every run; `verifyFoundation` depends on that assembly task.

M67 also corrects the top-level verification gate to include `:platform-1.8.9:check`. Platform adapter tests are therefore explicitly part of the same Ubuntu + Windows authority gate as bootstrap, core, platform API and launcher tests.


## Template-aware runtime overlay resolution

M68 removes a launch-order mismatch between version metadata and the M67 runtime bundle. A runtime overlay may now be represented by `LaunchRuntimeOverlayResolver`, which receives the authoritative resolved `MinecraftLaunchTemplate` and returns the concrete overlay for that launch.

Existing fixed `LaunchRuntimeOverlay` request constructors remain valid; they are wrapped as fixed resolvers and preserve their previous behavior. `Minecraft189RuntimeBundle` implements the resolver directly and derives the bootstrap target from `template.mainClass()`.

`LaunchPreflight` now resolves the template first, passes that template to the overlay resolver, validates the resulting overlay entries, and only then stages natives/builds the JVM command. The launcher therefore no longer has to predict or separately re-resolve Mojang's main class merely to construct the CustomMC bootstrap overlay.

## Canonical Minecraft 1.8.9 launch orchestration

M69 adds `Minecraft189LaunchRequest` and `Minecraft189Launcher` as the first one-call launcher composition over the certified launch path. The request is explicitly version-locked to Minecraft 1.8.9 and converts directly to the M68 template-aware `LaunchPreflightRequest` using the canonical `Minecraft189RuntimeBundle`.

`Minecraft189Launcher.start(...)` performs only two ownership transfers: M7/M68 `LaunchPreflight.prepare(...)`, then M62 `LaunchProcessRunner.start(...)`. It does not duplicate metadata resolution, artifact verification, overlay validation, native staging, command construction, child-process cleanup or session lifetime rules.

## Runnable launcher distribution

M71 composes the existing Gradle application distribution with the M67 canonical runtime overlay. `assembleLauncherDistribution` emits `build/custommc-distribution` without rebuilding or repackaging the underlying jars: launcher scripts/libs come from `:launcher:installDist`, while `runtime-overlay/` comes from the certified `assembleRuntimeOverlay` task.

`verifyLauncherDistribution` is part of `verifyFoundation` and requires both platform launcher scripts, a non-empty launcher `lib/` directory, the README, and exactly the four stable runtime-overlay jar names. This makes the final handoff one reproducible directory rather than a set of separately located build outputs.


## Bootstrap-owned Minecraft 1.8.9 host runtime

M72 joins the M66 bootstrap graph to the already-certified M54-M61 host façade without creating a second set of module, setting, service or render owners. `Minecraft189BootstrapRuntime.installHost(...)` installs exactly one `Minecraft189HostRuntime` against the bootstrap-owned registries and attached platform.

Host activation is explicit and one-shot. A duplicate activation or activation after bootstrap shutdown is rejected. Bootstrap teardown closes the host runtime before keybind services, render-pipeline service registration and platform attachment are released, so ClickGUI services cannot outlive the owners they route through. The host still does not own or detach the platform itself.

M72 deliberately stops at this ownership seam. The subsequent Minecraft callback/binding layer can now target one bootstrap-owned host runtime instead of reconstructing UI/input/render state or relying on an unowned global runtime.


## Lifecycle-owned Minecraft callback bridge

M73 adds `Minecraft189RuntimeBridge` as the narrow static target future Minecraft 1.8.9 bytecode/callback bindings can call. Bootstrap owns the bridge through an explicit registration token: only one bootstrap runtime may be published at a time, overlapping runtime creation is rejected, and shutdown unpublishes the bridge before host/UI, keybind, render-pipeline and platform teardown begins.

The bridge does not expose the bootstrap runtime object or create parallel state. Host installation still delegates to the M72 owner, while tick/render/input entry points forward into that same `Minecraft189HostRuntime`. Callback entry points are deliberately inert when no active host exists, and input returns `false`; this makes late callbacks during startup/shutdown harmless rather than able to reach partially torn-down services.

M73 is only the stable callback target. It still performs no Minecraft class transformation and imports no Mojang implementation classes.


## Concrete LWJGL2 viewport and default-font host binding

M74 supplies the first concrete M73 host composition that can be installed without importing Mojang implementation classes. `Lwjgl2Minecraft189ViewportSource` reads the live LWJGL2 display dimensions and reproduces Minecraft 1.8.9 GUI scale-factor selection: GUI scale 0 behaves as auto, scaling stops before the logical viewport would fall below 320x240, and Unicode mode corrects odd scale factors above one down to an even factor.

Minecraft-owned GUI settings remain behind the narrow `Minecraft189GuiSettingsAccess` contract so the upcoming game callback patch can supply the live values without reflection or obfuscated-field lookup inside the platform renderer. Invalid non-positive framebuffer dimensions and negative scale settings fail explicitly instead of creating an invalid `UiViewport`.

`Minecraft189DefaultFontRenderer` maps only the semantic `UiFonts.DEFAULT` handle to `Minecraft189FontRendererAccess.drawString(..., shadow=false)`. Unknown semantic fonts are rejected rather than silently aliasing to the Minecraft font. `Minecraft189LwjglHostBinding.install(...)` composes that text adapter with the existing certified LWJGL2 GL renderer and installs the resulting host through the M73 runtime bridge.

M74 still does not patch Minecraft classes. It leaves one deliberately small game-side responsibility: provide the live GUI-setting/font-renderer access objects and forward actual 1.8.9 callbacks into the M73 bridge.
