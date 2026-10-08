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

M67 originally gave the launcher one canonical bundle definition for the executable M64-M66 runtime path. At that milestone, `Minecraft189RuntimeBundle` owned exactly four normalized, distinct CustomMC artifacts in classpath order:

1. `bootstrap.jar`;
2. `core.jar`;
3. `platform-api.jar`;
4. `platform-1.8.9.jar`.

The bundle fixes the concrete initializer to `dev.trexzo.custommc.platform.v1_8_9.Minecraft189BootstrapInitializer` and produces the initialized M65 overlay for the resolved Mojang main class. `fromDirectory(...)` maps the stable assembly filenames directly, avoiding launch-time filename/version reconstruction.

At M67, the root `assembleRuntimeOverlay` Gradle task built those four module jars and reconciled `build/runtime-overlay` to those four stable names. M77 extends the canonical runtime shape with the separately versioned ASM runtime dependency described below.

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

`verifyLauncherDistribution` is part of `verifyFoundation` and requires both platform launcher scripts, a non-empty launcher `lib/` directory, the README, and the exact canonical runtime-overlay jar set. M71 initially certified four runtime jars; M77 extends the current set to five. This makes the final handoff one reproducible directory rather than a set of separately located build outputs.


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


## Runtime-selected target classloader handoff

M75 lets an initialized bootstrap session optionally implement `BootstrapTargetClassLoaderProvider`. The bootstrap initializer itself is still resolved and constructed through the original process context loader, but the session may explicitly select a different loader for the target Minecraft main class.

When a target loader is selected, `CustomMcBootstrapMain` uses it for target class resolution and temporarily makes it the thread context classloader for the entire target-main invocation. The previous thread context loader is restored in a `finally` block on both success and target failure, and the runtime session still closes after target execution.

Sessions that do not implement the provider preserve the exact M64-M74 behavior. M75 introduces no transformation by itself; it creates the non-agent ownership seam required for a future Minecraft 1.8.9 transforming loader while keeping initializer/runtime classes in the existing parent loader.


## Explicit transforming target classloader

M76 implements the non-agent execution mechanism enabled by M75. `TransformingTargetClassLoader` is constructed from an explicit URL classpath plus parent loader and `BootstrapClassTransformer`. A transformer must claim a binary class name before the loader will child-define it; every unclaimed class preserves ordinary parent delegation and therefore keeps the existing CustomMC/bootstrap/runtime type identities.

Claimed classes are read from the target loader's own classpath URLs, transformed before definition, and never silently delegated back to the parent if their bytes are missing. A null/empty transformation is rejected. `fromJavaClassPath(...)` provides the launcher-process composition path by converting the JVM's resolved `java.class.path` entries into target URLs.

Regression coverage does not mock class definition: it loads a second copy of a real test class through the child loader, rewrites the classfile UTF-8 constant `original` to the equal-length `mutated!`, and proves the method executed by the JVM returns the transformed value. The same coverage proves unclaimed classes retain parent identity and a claimed missing class cannot bypass transformation.

M76 still contains no Minecraft-specific bytecode edits. The next 1.8.9 milestone can implement a narrowly scoped transformer and return this loader from the M75 runtime-provider contract without using a Java agent, JNI or JVMTI injection.


## First Minecraft class transformation

M77 turns the M75-M76 loader path on for the real stable Minecraft entry class. The canonical runtime bundle now contains five distinct artifacts in classpath order: `bootstrap.jar`, `core.jar`, `platform-api.jar`, `asm.jar`, and `platform-1.8.9.jar`. ASM is an explicit runtime artifact rather than an undeclared transitive dependency, and both runtime-overlay and launcher-distribution verification require the five-file shape.

`Minecraft189BootstrapRuntime` now implements `BootstrapTargetClassLoaderProvider` and lazily owns one M76 `TransformingTargetClassLoader`. The loader uses `Minecraft189ClassTransformer`, whose first deliberately narrow target is only `net.minecraft.client.main.Main`. At the beginning of its exact static `main(String[])` method, the transformer injects a call to parent-owned `Minecraft189RuntimeBridge.targetMainEntered()`.

This is the first production Minecraft-specific bytecode edit in the project. It does not depend on MCP/deobfuscated Minecraft classes, guessed obfuscated names, a Java agent, JNI or JVMTI injection. Regression coverage loads a Minecraft-shaped target fixture through the child loader and proves the injected callback crosses back into the active parent-owned bootstrap runtime before the original main body executes.

M77 does not yet patch the obfuscated game loop, renderer, mouse or keyboard classes. Those callback sites remain the next mapping-sensitive layer.


## Pinned Minecraft 1.8.9 mapping authority

M78 replaces informal obfuscation-name assumptions with a checked-in mapping authority. The source is pinned to `BigBroadBean/mappings-extracted` commit `2265da88e93c20411ec70f0b892f4fbc84ebc3c9`, whose 1.8.9 metadata identifies its files as copied official MCP data. The exact source blobs retained in `Minecraft189Mappings` are:

- classes: `17967e48db6c8ec20ae622409be13971e2c77706`;
- fields: `a8c5928cb64455dcc2712078dbfe018ae97cafb9`;
- methods: `683d45abbd02a1525008c5654a40b50efbc47c6d`;
- joined SRG: `0b1e3f1d0156abcbd70e2b09b720379fc0c1eae6`.

The first callback/host mapping set is exact rather than inferred:

| MCP owner/member | Obfuscated owner/member | Descriptor |
| --- | --- | --- |
| `Minecraft` | `ave` | class |
| `Minecraft.fontRendererObj` | `ave.k` | `Lavn;` |
| `Minecraft.entityRenderer` | `ave.o` | `Lbfk;` |
| `Minecraft.ingameGUI` | `ave.q` | `Lavo;` |
| `Minecraft.gameSettings` | `ave.t` | `Lavh;` |
| `Minecraft.getMinecraft` | `ave.A` | `()Lave;` |
| `Minecraft.startGame` | `ave.am` | `()V` |
| `Minecraft.runTick` | `ave.s` | `()V` |
| `GameSettings` | `avh` | class |
| `GameSettings.guiScale` | `avh.aL` | `I` |
| `GameSettings.forceUnicodeFont` | `avh.aO` | `Z` |
| `FontRenderer` | `avn` | class |
| `FontRenderer.drawString` | `avn.a` | `(Ljava/lang/String;FFIZ)I` |
| `GuiIngame.renderGameOverlay` | `avo.a` | `(F)V` |
| `EntityRenderer.updateCameraAndRender` | `bfk.a` | `(FJ)V` |

`Minecraft189ClassShapeVerifier` turns those rows into runtime transformation preconditions. It validates the exact obfuscated class owner and every field/method name plus descriptor required by the planned host/tick/HUD/render hooks. A missing or drifted member fails explicitly before transformation instead of allowing a patch to bind to a coincidental method with the same short obfuscated name.

M78 does not inject any new callback. Its purpose is to make the next mapping-sensitive transformations depend on version-pinned evidence and structural verification rather than guessed 1.8.9 names.


## Mapped host-access transformation

M79 consumes the M78 mapping authority for the first live obfuscated host-binding path. `Minecraft189ClassTransformer` now claims exactly the mapped production `Minecraft` (`ave`), `GameSettings` (`avh`) and `FontRenderer` (`avn`) owners in addition to the stable launcher main class.

Every mapped class is passed through its M78 structural verifier before any bytes are changed. The transformed `GameSettings` class implements the existing parent-owned `Minecraft189GuiSettingsAccess` contract by exposing only mapped `guiScale` and `forceUnicodeFont` fields. The transformed `FontRenderer` class implements `Minecraft189FontRendererAccess` by delegating directly to the exact mapped `drawString(String,float,float,int,boolean)` method. No reflection or guessed names are introduced.

On every normal return from mapped `Minecraft.startGame()`, the transformed Minecraft class passes those two typed live objects to the existing M74 `Minecraft189LwjglHostBinding.install(...)`. Host ownership therefore remains M72/M73-owned; the transformed game classes supply only the minimum live access surface already defined by the platform adapter.

Regression coverage executes transformed `avh`, `avn` and `ave` fixture bytecode through a child classloader and proves that mapped field/font access works and that returning from `startGame()` installs the existing host runtime. M79 intentionally does not yet add tick, HUD, world-render or input callbacks; those can now be layered independently on top of a certified live host.


## Mapped game-tick callback

M80 adds the first recurring mapped game-loop callback on top of the certified M79 live host. The exact M78 `Minecraft.runTick` mapping (`ave.s()V`) is required by the existing structural verifier before transformation, and the transformer injects one parent-owned `Minecraft189RuntimeBridge.gameTick()` call at method entry.

Tick numbering is not static bridge state. `Minecraft189BootstrapRuntime` owns the monotonic sequence, beginning at `0` for each runtime lifetime, and publishes through the already-installed M72 `Minecraft189HostRuntime`. If the host is not installed or runtime teardown has begun, the callback is inert. Closing and recreating the bootstrap runtime therefore resets tick authority naturally.

Executable regression coverage loads the transformed mapped Minecraft fixture, installs the M79 host through transformed `startGame()`, invokes transformed `runTick()` twice, and proves the core event bus receives tick indexes `0` then `1`. M80 does not yet add HUD, world-render or input callbacks.


## Mapped render-frame and HUD callbacks

M81 establishes shared render-frame identity before adding more render stages. The exact M78 `EntityRenderer.updateCameraAndRender` mapping (`bfk.a(FJ)V`) now injects `Minecraft189RuntimeBridge.renderFrameStarted(partialTicks)` at method entry. The bootstrap runtime owns the monotonic frame sequence, beginning at `0` for each runtime lifetime and retaining the current frame index for downstream stage callbacks.

The exact mapped `GuiIngame.renderGameOverlay` method (`avo.a(F)V`) injects `renderHudFrame(partialTicks)` immediately before every normal return, so CustomMC HUD passes execute after the vanilla overlay while sharing the frame index established by the enclosing renderer call. Startup/teardown callbacks remain inert if no live host/current frame exists.

Both `bfk` and `avo` are M78-shape-verified before transformation. Executable regression coverage runs transformed frame-start and HUD fixture methods after the M79 host install and proves HUD frames `0@0.25` then `1@0.5`. M81 does not yet publish WORLD, WORLD_OVERLAY, POST_PROCESS, mouse, keyboard or wheel callbacks.


## Mapped post-process frame completion

M82 closes the shared M81 render-frame lifecycle at the exact mapped `EntityRenderer.updateCameraAndRender(F,J)` normal return. The transformer now invokes `Minecraft189RuntimeBridge.renderPostProcessFrame(partialTicks)` immediately before each normal `RETURN`, after the vanilla frame body has completed.

The bootstrap runtime publishes the existing POST_PROCESS stage with the current M81 frame index and then clears that active frame identity in a `finally` block. A late HUD callback after frame completion is therefore inert until the next mapped frame-start callback establishes a new index. Exceptional method exits deliberately do not synthesize a successful post-process stage; the next frame start overwrites any stale in-progress identity.

Executable regression coverage makes the transformed EntityRenderer fixture invoke transformed GuiIngame inside the mapped frame body, proving the production ordering for two frames: HUD `0@0.25` then POST_PROCESS `0@0.25`, followed by HUD `1@0.5` then POST_PROCESS `1@0.5`. WORLD and WORLD_OVERLAY still require more precise mapped anchors and remain out of M82.


## Pinned Minecraft input mapping authority

M83 extends the M78 mapping authority before any consuming/raw input transformation is attempted. The same pinned 1.8.9 `methods.csv` and `joined.srg` sources now explicitly require these exact Minecraft methods:

| MCP member | Obfuscated member | Descriptor |
| --- | --- | --- |
| `Minecraft.clickMouse` | `ave.aw` | `()V` |
| `Minecraft.rightClickMouse` | `ave.ax` | `()V` |
| `Minecraft.middleClickMouse` | `ave.az` | `()V` |
| `Minecraft.dispatchKeypresses` | `ave.Z` | `()V` |

The source rows resolve respectively through `func_147116_af`, `func_147121_ag`, `func_147112_ai`, and `func_152348_aa`. `Minecraft189ClassShapeVerifier.verifyMinecraft(...)` now requires all four methods in addition to the previously certified startup/tick members before `ave` is transformable.

M83 deliberately performs no input injection. Minecraft 1.8.9 drains LWJGL event queues inside `runTick()`; pinning the exact dispatch/action members first prevents a later keyboard/mouse hook from relying on ambiguous short obfuscated names or accidentally consuming vanilla events at the wrong site.


## Mapped non-consuming keyboard event forwarding

M84 turns the M83 `Minecraft.dispatchKeypresses` authority (`ave.Z()V`) into the first live raw-input bridge. The mapped method is shape-verified before transformation and receives exactly one call to parent-owned `Minecraft189LwjglKeyboardBinding.forwardCurrentEvent()` at method entry.

The binding reads the current LWJGL event key, character, press/release state, repeat flag and left/right Shift/Ctrl/Alt state, then forwards those primitives through the existing M73 `Minecraft189RuntimeBridge.key(...)` path. It never calls `Keyboard.next()`, never clears the queue and does not branch on the returned consumed flag, so vanilla 1.8.9 still processes the same event normally. This is intentionally observation/forwarding, not input suppression.

The LWJGL source is isolated behind a package-private event-source contract so regression coverage can execute the routing without native display initialization. Tests prove an injected keyboard event reaches the installed retained ClickGUI input controller, closes the open GUI on Escape, and is inert before host installation or after runtime teardown. Separate bytecode coverage proves exactly one forward call is injected into mapped `ave.Z()`.

Mouse buttons, wheel input, explicit ClickGUI-open policy and any consuming/suppression behavior remain separate milestones.


## Mapped raw mouse-button forwarding

M85 adds the first raw mouse button path without touching the LWJGL event iterator. The M78/M83 authority is extended with exact `KeyBinding` owner `avb` and static `setKeyBindState(int,boolean)` member `avb.a(IZ)V` / `func_74510_a`. The class is structurally verified before transformation.

Vanilla Minecraft calls this method for every raw mouse button transition using `button - 100`, so the transformed method forwards `-100/-99/-98` as left/right/middle press or release through parent-owned `Minecraft189LwjglMouseBinding`. The binding reads the current LWJGL X/Y position only for those supported mouse codes, then uses the existing M73 viewport-aware pointer bridge. Keyboard codes and unsupported mouse buttons do not read cursor state.

As with M84 keyboard forwarding, M85 does not advance `Mouse.next()` and does not suppress the original `KeyBinding.setKeyBindState` body. Regression coverage proves a raw left press can focus the retained ClickGUI search field and then accept keyboard text, while release is forwarded but not consumed by the current press-only UI controller. Separate transformed-bytecode coverage executes the child-defined `avb.a(IZ)V` method and proves the original body still runs after the injected callback.

Wheel input and any conditional suppression of vanilla gameplay actions remain separate milestones.


## Mapped non-consuming mouse-wheel forwarding

M86 adds wheel input without advancing or replacing the LWJGL event queue. Inside the already mapped and structurally verified `Minecraft.runTick()` method, the transformer requires exactly one stable `org.lwjgl.input.Mouse.getEventDWheel()I` invocation.

Immediately after that invocation, the transformer duplicates the returned integer, passes only the duplicate to `Minecraft189LwjglMouseBinding.forwardWheelDelta(int)`, and leaves the original integer on the operand stack. Vanilla therefore receives the exact same wheel delta for spectator or hotbar behavior. A missing or duplicated wheel-read site fails transformation explicitly instead of silently binding to an ambiguous location.

The parent-owned mouse binding ignores zero deltas without reading cursor state; non-zero deltas reuse the live LWJGL X/Y position and the existing viewport-aware M73 scroll bridge. Regression coverage proves retained ClickGUI scroll routing with a fake cursor source, proves zero-delta isolation, and verifies exactly one wheel callback is injected. The transformed tick fixture retains a JVM-verifiable but unreachable LWJGL wheel read so existing executable tick-index regressions remain native-free.

M86 is still non-consuming. Conditional suppression while the retained ClickGUI is open remains a later policy milestone.


## Host-owned ClickGUI toggle binding

M87 closes the remaining live-use gap in the retained UI: the M84 mapped keyboard path can now open the ClickGUI as well as interact with and close it. A dedicated `Minecraft189ClickGuiToggleController` is registered as a managed host service after ClickGUI installation and removed before ClickGUI teardown.

The default platform binding is explicitly named as LWJGL Right Shift (`LegacyKeyboardCodes.RIGHT_SHIFT = 54`). The controller owns the toggle policy rather than embedding a raw key-code comparison in the bytecode transformer. Only the initial press toggles; release and keyboard-repeat events are ignored so holding Right Shift cannot oscillate UI state.

`Minecraft189InputHooks.key(...)` routes events through the toggle controller before retained ClickGUI content input and module keybind handling. Regression coverage proves the actual M84 mapped-keyboard adapter opens a previously closed retained ClickGUI with Right Shift, Escape still closes it through the existing input controller, repeated Right Shift does not retrigger, and the toggle service disappears with the host runtime.

M87 deliberately does not persist or rebind this client-action chord yet. Module-key persistence remains a separate authority, and vanilla keyboard behavior is still non-consuming at the transformed Minecraft boundary.


## First runtime feature module

M88 crosses from framework-only runtime work into the first real client feature. `ModuleRegistry.register(...)` now returns an exact ownership handle; closing it removes only the same registered module instance. `ModuleController` keys lifecycle state by module identity instead of only stable id, so removing and later re-registering the same id starts from a fresh DISABLED state rather than inheriting stale lifecycle state.

The Minecraft 1.8.9 host now owns `Minecraft189FeatureCatalog`. Its first feature is `render.watermark` / **Watermark** in the **Visuals** category. Enabling the module registers a real HUD render pass; the pass opens the already-certified legacy UI host frame, draws `CustomMC` with the semantic default Minecraft font at the top-left, and restores the UI frame in a failure-safe `finally` path. Disabling the module unregisters that render pass.

The feature catalog owns the category, presentation and module registrations. Host shutdown disables the feature if necessary, removes its presentation/module/category ownership, then continues the existing ClickGUI teardown. Regression coverage proves disabled → no watermark draw, enabled → one HUD watermark draw, disabled again → no additional draw, and host close removes the module registration entirely.


## Configurable watermark settings

M89 makes the first runtime feature participate in the same explicit settings architecture as future modules. `SettingRegistry.register(...)` now returns an exact ownership handle, allowing host-owned feature settings to be removed deterministically instead of surviving after their feature lifetime.

The Watermark module now owns three persistent settings:

- `render.watermark.text` — text, default `CustomMC`, non-blank and at most 32 characters;
- `render.watermark.x` — integer HUD X coordinate, default `8`, range `0..4096`;
- `render.watermark.y` — integer HUD Y coordinate, default `8`, range `0..4096`.

The feature catalog registers each setting, its ClickGUI presentation descriptor and its explicit module-setting ownership binding. The existing module-detail editor can therefore edit the text and numeric position without feature-specific UI code. The watermark render pass reads the live setting values each HUD frame, while `SettingRegistry.snapshotEncoded()` automatically includes all three values through their existing persistent codecs.

Feature-catalog teardown closes module-setting bindings first, then presentation registrations, then the owned setting registrations before removing the module/category. Regression coverage proves persisted encoding, live rendered text/coordinates and complete setting removal on host shutdown.


## Enabled-module Array List

M90 adds the second real Visuals feature: `render.array-list` / **Array List**. It is independently lifecycle-owned by the host feature catalog and registers its own HUD render pass only while enabled.

The module reads the authoritative live `ModuleRegistry` and `ModuleController` state each HUD frame, resolves human-readable names through `ModulePresentationRegistry`, and renders only currently enabled modules. It therefore updates immediately when another module is enabled or disabled and does not maintain a second shadow list of module state.

Array List owns persistent X/Y settings (`render.array-list.x`, `render.array-list.y`) through the same M89 setting-registration, presentation and module-setting ownership paths. Its default position is 8,24 with 12 logical pixels between entries. The feature shares the existing Visuals category rather than creating a duplicate category owner.

Regression coverage enables Watermark plus Array List and proves the actual HUD output sequence is `CustomMC`, `Watermark`, `Array List` at the configured coordinates; after Watermark is disabled, the next HUD render contains only `Array List`. Host shutdown removes the Array List module and both owned settings.


## Live Keystrokes HUD

M91 adds host-owned raw input state and the third real Visuals feature: `render.keystrokes` / **Keystrokes**.

The input state is updated in `Minecraft189HostRuntime` before keyboard or pointer events are forwarded to ClickGUI and module-keybind handling. This means physical press/release state remains authoritative even when another input layer consumes the event. The state is lifecycle-owned by the host and is cleared before feature teardown.

Keystrokes renders W/A/S/D plus LMB/RMB through the existing HUD and legacy UI host path. Pressed and idle keys use distinct backgrounds/text colors, and the module owns persistent X/Y settings through the generic setting-registration and module-detail editing system.

Regression coverage proves a Right Shift event consumed by the ClickGUI toggle still updates raw state, then proves mapped-style W/LMB press and release transitions change the rendered Keystrokes state at configured coordinates. Host shutdown clears retained raw state and removes the Keystrokes module and its settings.


## Live FPS HUD

M92 derives FPS from the certified mapped render-frame lifecycle rather than adding a Minecraft debug-field mapping. `Minecraft189FrameRateTracker` is host-owned and records each `renderFrameStarted` callback into a rolling one-second monotonic-time window. Querying FPS ages stale frames out, and a backwards clock is rejected rather than silently corrupting the metric.

`Minecraft189BootstrapRuntime.beginRenderFrame(...)` now forwards each lifecycle-owned frame start to the installed host tracker after allocating the frame index. The fourth Visuals feature, `render.fps` / **FPS**, renders the current measured value as `FPS: N` through the existing HUD/font path and owns persistent X/Y settings through the generic module-detail settings system.

Deterministic clock tests prove the one-second rolling window and reset behavior. A module test proves live FPS text and configured position, host-lifetime tests prove its module/settings ownership is removed on shutdown, and the transformed EntityRenderer regression proves the real mapped frame-start hook reaches the FPS tracker.


## Live CPS HUD

M93 adds host-owned rolling click-rate authority and the fifth Visuals feature: `render.cps` / **CPS**.

The existing M85 mapped mouse path already filters raw `KeyBinding.setKeyBindState` calls down to encoded mouse buttons before reaching the host. `Minecraft189HostRuntime.pointerButton(...)` now compares the previous M91 raw button state with the incoming state and records a click only on a left/right `false -> true` transition. Repeated pressed-state updates while a button remains held therefore cannot inflate CPS.

`Minecraft189ClickRateTracker` keeps independent one-second rolling windows for LMB and RMB. The HUD reads both counts through one atomic timestamped snapshot and renders `CPS: L N | R N`, with persistent X/Y settings owned through the generic feature/settings architecture.

Deterministic tests prove independent rolling-window aging and atomic HUD output. Host integration coverage proves duplicate press updates count once, release-then-press counts again, left/right remain separate, and shutdown clears the tracker plus removes the CPS module and its settings.


## Module-state profile persistence

M94 extends the existing launcher profile namespace so settings, owned keybinds and stable module enabled/disabled state can round-trip through one atomic profile document. New snapshots reserve `@module/<module-id>` keys and encode only `enabled` or `disabled`; transient `ENABLING`, `DISABLING` and `FAILED` states are rejected as non-profile-stable.

The original two-argument `LauncherProfileState` constructor remains supported for settings/keybind-only callers. The new constructor additionally receives the authoritative `ModuleRegistry` and `ModuleController`. Profiles without any `@module/` entries leave current module states untouched, preserving compatibility with previously written profile files.

Profile apply preflights module entries before mutating settings. When module-state authority is present, settings are applied, owned keybinds are replaced, then requested module transitions are performed through `ModuleController`; no module callback is bypassed. A failure during keybind or module application triggers rollback of the previous stable module states, owned keybind set and persistent settings, with rollback failures attached as suppressed evidence.

Unknown `@module/` entries follow the existing profile unknown-value policy: REJECT fails before mutation, while IGNORE skips missing modules. Persistent setting IDs may not use either reserved `@keybind/` or `@module/` namespaces.

Regression coverage proves module-state snapshot/apply, compatibility with old profiles, unknown-module handling, rollback after a failing module enable, reserved namespace rejection, and round-trip through `AtomicProfileStore`.


## Player-position mapping authority

M95 extends the existing pinned Minecraft 1.8.9 mapping authority without yet transforming or reading player state. The same exact `BigBroadBean/mappings-extracted` commit and blob provenance now pins:

- `Minecraft.thePlayer` as `ave.h : Lbew;`;
- `EntityPlayerSP` as obfuscated class `bew`;
- base `Entity` as obfuscated class `pk`;
- `Entity.posX` as `pk.s : D`;
- `Entity.posY` as `pk.t : D`;
- `Entity.posZ` as `pk.u : D`.

The Minecraft class-shape gate now requires the exact `thePlayer` field in addition to the previously certified host fields. A new Entity shape gate requires all three exact position fields on `pk`. This milestone deliberately stops at authority: it does not add reflection, transform Entity, expose child-loader Minecraft objects, or publish coordinates yet.

The next consumer can therefore add a narrow parent-owned position-access interface and live Coordinates HUD only after this exact mapping surface is independently certified.


## Live Coordinates HUD

M96 consumes the independently certified M95 player-position mapping surface. The transforming loader now claims exact base Entity `pk`, shape-verifies `posX/posY/posZ`, and adds only the parent-owned `Minecraft189PlayerPositionAccess` interface with three double getters. No Minecraft implementation type crosses into the parent runtime.

Immediately before each normal return from exact mapped `Minecraft.runTick()`, transformed `ave` reads mapped `thePlayer` (`h : Lbew;`), casts it to the inherited position-access contract, and forwards it through `Minecraft189RuntimeBridge`. A null player clears position availability. `Minecraft189HostRuntime` owns one synchronized `Minecraft189PlayerPositionState` snapshot and clears it again during teardown.

The new Visuals module `render.coordinates` / **Coordinates** renders only when a live player position is available. It displays one-decimal `XYZ: x / y / z` using Locale.ROOT and owns persistent X/Y settings through the existing generic setting/presentation/module-binding path.

Regression coverage executes transformed `pk`, synthetic `bew extends pk`, and transformed `ave.runTick()` to prove mapped position values reach the host snapshot. Separate HUD coverage proves unavailable state draws nothing, live state renders the expected coordinates at configured position, clear hides output again, and non-finite coordinates are rejected.


## Live horizontal Speed HUD

M97 derives horizontal movement speed from the same certified post-runTick player-position samples introduced by M96; it adds no Minecraft mappings and performs no second game-state lookup. `Minecraft189HostRuntime` owns one `Minecraft189MovementSpeedTracker`. The first live sample establishes a baseline, each later sample computes `sqrt(dx^2 + dz^2) * 20` blocks per second, and a null player or host teardown clears both baseline and availability.

The new Visuals module `render.speed` / **Speed** renders `Speed: <value> BPS` with two decimal places only after two live position samples exist. It owns persistent X/Y settings through the same generic settings/presentation/module-binding path as the other HUD modules.

Regression coverage proves a 3-4-5 horizontal displacement in one tick produces `100.00 BPS`, the configured HUD location is respected, baseline/clear states render nothing, non-finite samples are rejected, and executed transformed `runTick` calls feed then clear the same host-owned speed authority.


## Live mapped Fullbright

M98 adds the first state-modifying runtime feature. The pinned 1.8.9 mapping authority now includes `GameSettings.gammaSetting` as exact obfuscated field `avh.aJ` (`field_74333_Y`, descriptor `F`). The GameSettings structural verifier requires that field before transformation, and the transformed parent-owned `Minecraft189GuiSettingsAccess` contract exposes only typed gamma get/set alongside the existing GUI-scale/Unicode access.

The canonical LWJGL host binding retains the live transformed GameSettings object long enough to install one **Fullbright** module through the existing host-owned feature catalog. No reflection or secondary Minecraft lookup is introduced.

Enabling Fullbright captures the exact current gamma value and sets a boosted gamma of `16.0F`. Disabling restores the captured value exactly. A later enable captures the then-current value again rather than restoring an obsolete startup value. Feature/catalog teardown disables the module first, so closing the client while Fullbright is enabled also restores the user's prior gamma.

Executable regressions prove the transformed `avh.aJ` getter/setter, canonical startGame installation, `0.35 -> 16.0 -> 0.35` live behavior, fresh capture on repeated enable cycles, and restoration/removal when the feature is closed while enabled.


## Live mapped FOV Changer

M99 extends the same exact GameSettings authority used by M98 with `fovSetting` as obfuscated `avh.aI` (`field_74334_X`, descriptor `F`). The GameSettings structural gate requires that field before transformation, and the parent-owned settings access contract exposes typed FOV get/set without reflection.

The canonical live LWJGL binding installs **FOV Changer** through the existing feature catalog. The module owns one persistent integer setting, `render.fov.value`, presented through the generic module-detail editor with range `30..179` and default `110`.

Enabling captures the exact current game FOV and applies the configured target immediately. While enabled, the module owns a subscription to the already-certified tick event and re-applies the latest configured value each tick, so ClickGUI edits become live without adding a separate setting-listener framework. Disabling closes that subscription and restores the exact pre-enable FOV. Re-enabling captures a fresh user value, and feature/catalog teardown restores the captured value before removing the module and its setting ownership.

Regression coverage proves exact `avh.aI` transformation, live `70 -> 110 -> 120 -> 70` behavior through a tick-delivered setting edit, fresh capture on later enable cycles, and restoration/removal when the feature is closed while enabled.


## Live mapped No Bobbing

M100 pins `GameSettings.viewBobbing` to exact obfuscated field `avh.d` (`field_74336_f`, descriptor `Z`) in the same version-locked mapping authority used by Fullbright and FOV Changer. The GameSettings shape verifier requires that field before transformation, and the transformed settings contract exposes typed boolean get/set without reflection.

The canonical live host binding installs **No Bobbing** through the existing Visuals feature catalog. Enabling captures the user's exact current view-bobbing preference and writes `false`. Disabling restores the captured value exactly. Re-enabling captures the then-current preference again, so a user who normally keeps bobbing disabled is restored to disabled rather than forced on.

Feature/catalog shutdown disables No Bobbing before removing its module registration, guaranteeing restoration if the client closes while the module is active. Executable regressions prove transformed `avh.d` access, canonical host installation, `true -> false -> true` live behavior, preservation of an originally-false preference, explicit mapping-drift rejection, and teardown restoration.


## Custom Crosshair

M101 adds **Custom Crosshair** as another independently owned Visuals module while preserving the concurrent M100 No Bobbing lane unchanged. It renders through the certified HUD geometry path at the logical viewport center, so GUI scaling is handled by the existing viewport authority rather than hard-coded framebuffer coordinates.

The module owns four persistent settings through the existing generic settings/ClickGUI path:

- `render.crosshair.length` — arm length, `1..20`, default `4`;
- `render.crosshair.gap` — center gap, `0..12`, default `2`;
- `render.crosshair.thickness` — arm thickness, `1..6`, default `1`;
- `render.crosshair.dot` — optional center dot, default `false`.

While enabled, the HUD pass draws four centered rectangles and optionally one center rectangle. Disabling removes the pass immediately; feature teardown removes the module, presentation, all four settings, setting presentations and module-setting bindings.

Regression coverage uses a 1280x720 framebuffer at UI scale 2 and proves the exact logical geometry for configured length 6, gap 3, thickness 2 and center dot enabled, plus persisted setting encoding and complete teardown removal.

## Player-rotation mapping authority

M102 extends the same pinned Minecraft 1.8.9 mapping authority with exact player-facing yaw metadata. Base `Entity.rotationYaw` is pinned as obfuscated `pk.y : F` / Searge `field_70177_z`, sourced from the already-recorded `BigBroadBean/mappings-extracted` 1.8.9 MCP conversion.

The Entity class-shape gate now requires `rotationYaw` alongside the already-certified `posX/posY/posZ` fields before any Entity transformation is accepted. Transformation fixtures were tightened to carry the exact field as well.

This milestone is authority-only: it does not expose a new runtime interface, read yaw from the child loader, or add a HUD. The next clean consumer is a narrow live rotation snapshot and Direction HUD built on this independently certified mapping.

## Live Direction HUD

M103 consumes the independently certified M102 `Entity.rotationYaw` mapping. Transformed base Entity now implements a second narrow parent-owned contract, `Minecraft189PlayerRotationAccess`, exposing only `customMcRotationYaw()F`; the existing position contract remains separate.

Immediately before each normal return from mapped `Minecraft.runTick()`, the exact mapped `thePlayer` reference is also forwarded through `Minecraft189RuntimeBridge.playerRotation(...)`. A null player clears the rotation snapshot. `Minecraft189HostRuntime` owns `Minecraft189PlayerRotationState` and clears it during teardown, so no child-loader Minecraft type escapes into feature code.

The new Visuals module `render.direction` / **Direction** renders only while a live yaw sample exists. It normalizes arbitrary finite yaw to `0..359`, uses Minecraft's yaw convention (`0=S`, `90=W`, `180=N`, `270=E`) with eight cardinal/intercardinal sectors, and displays the rounded yaw. Persistent `render.direction.x` and `render.direction.y` settings use the existing generic settings, presentation and module-binding path.

Executable regression coverage proves transformed `pk.y` reaches the host snapshot, null-player clearing removes it, the HUD renders `Direction: W (91°)` for `91.2`, negative yaw normalizes correctly, non-finite yaw is rejected, settings persist, and feature teardown removes all owned registrations.

## Player-health mapping authority

M104 extends the pinned Minecraft 1.8.9 MCP-derived mapping surface with exact player-health authority. `EntityLivingBase` is obfuscated class `pr`; `getHealth()` is `pr.bn()F` / Searge `func_110143_aJ`; and `getMaxHealth()` is `pr.bu()F` / Searge `func_110138_aP`.

A dedicated `EntityLivingBase` class-shape gate now requires both exact methods before any future transformation may consume this surface. Regression coverage pins the class, both method owners/names/descriptors, accepts the exact synthetic shape, and rejects a class missing `getMaxHealth()`.

This milestone is authority-only. It does not transform `pr`, read player health, or register a HUD. The next clean consumer can expose a narrow parent-owned health contract and a live Health HUD only after this exact head is certified.

## Live Health HUD

M105 consumes the independently certified M104 `EntityLivingBase` health method authority. The transforming loader now claims exact `pr`, shape-verifies `bn()F` / `bu()F`, and adds only a parent-owned `Minecraft189PlayerHealthAccess` contract. Its getters delegate to the exact mapped methods; no health field layout is guessed and no reflection is introduced.

Immediately before each normal return from mapped `Minecraft.runTick()`, the mapped `thePlayer` reference is forwarded through `Minecraft189RuntimeBridge.playerHealth(...)`. `Minecraft189HostRuntime` owns a synchronized `Minecraft189PlayerHealthState`; null player and host teardown clear availability. Samples reject non-finite values and non-positive maximum health.

The new Visuals module `render.health` / **Health** renders `Health: current / max` to one decimal place only while a live sample exists. Persistent X/Y settings use the existing generic setting, presentation, profile and module-binding architecture.

Executable regression coverage loads transformed `pk`, transformed `pr extends pk`, synthetic `bew extends pr`, and transformed `ave.runTick()` to prove live `17.5 / 20.0` health reaches the host snapshot and clears with a null player. Separate HUD coverage proves live text/position, persistent setting encoding, invalid-value rejection and complete feature teardown.

## Armor-equipment mapping authority

M106 extends the pinned Minecraft 1.8.9 equipment surface without yet exposing any item object to the parent runtime. `ItemStack` is obfuscated class `zx`, and `EntityLivingBase.getEquipmentInSlot(int)` is exact `pr.p(I)Lzx;` / Searge `func_71124_b`.

The existing `EntityLivingBase` shape gate now requires this exact method alongside the M104 health methods. Independent 1.8.9 source verification confirms the vanilla slot contract used by players: slot `0` is the held item and slots `1..4` are armor. A future consumer can therefore reduce the four armor slots to parent-owned booleans without allowing `ItemStack` to cross the transforming classloader boundary.

Regression coverage pins the `zx` class and exact equipment method owner/name/descriptor and rejects an `EntityLivingBase` shape that omits the method. This milestone deliberately stops before transformation or HUD registration.

## Live Armor HUD

M107 consumes the independently certified M106 equipment-slot authority without allowing any `ItemStack` instance to cross the child-loader boundary. Transformed `EntityLivingBase` now implements `Minecraft189PlayerArmorAccess` alongside the existing health contract. Four generated boolean accessors call exact mapped `pr.p(I)Lzx;` with vanilla armor slots `1..4` and reduce each result to present/absent inside transformed code.

Immediately before each normal return from mapped `Minecraft.runTick()`, the mapped player is forwarded through `Minecraft189RuntimeBridge.playerArmor(...)`. `Minecraft189HostRuntime` collapses boots, leggings, chestplate and helmet occupancy into a host-owned four-bit `Minecraft189PlayerArmorState`; null player and host teardown clear availability.

The new Visuals module `render.armor` / **Armor** renders a compact slot summary such as `Armor: 3/4 [H C - B]`, preserving slot identity rather than only reporting an aggregate count. Persistent X/Y settings participate in the existing settings, presentation, profile and module-binding paths.

Executable regression coverage loads synthetic `zx`, transformed `pk`, transformed `pr extends pk`, synthetic `bew extends pr`, and transformed `ave.runTick()`. A mixed slot array with boots/chestplate/helmet occupied and leggings empty must produce exact mask `13`, count `3`, and then clear on null player. Separate HUD coverage proves text, configured position, persistent settings and complete feature teardown.

## Hunger mapping authority

M108 extends the pinned Minecraft 1.8.9 gameplay-state surface with exact hunger authority. Base `EntityPlayer` is obfuscated class `wn`, `FoodStats` is obfuscated class `xg`, and the exact methods are:

- `wn.cl()Lxg;` / Searge `func_71024_bL` / `getFoodStats()`;
- `xg.a()I` / Searge `func_75116_a` / `getFoodLevel()`;
- `xg.e()F` / Searge `func_75115_e` / `getSaturationLevel()`.

Dedicated `EntityPlayer` and `FoodStats` shape gates require those exact methods before a future consumer may transform or read hunger state. Regression coverage pins both classes, all three methods, accepts exact synthetic shapes, and rejects missing player-food or saturation methods.

This milestone is authority-only. It does not yet transform `wn` or `xg`, publish hunger state, or register a HUD.

## Live Hunger HUD

M109 consumes the independently certified M108 hunger authority. The transforming loader now claims exact base `EntityPlayer` `wn` and shape-verifies exact `FoodStats` `xg`; `xg` itself remains otherwise unchanged.

Transformed `wn` implements only the parent-owned `Minecraft189PlayerHungerAccess` contract. Its generated primitive getters call exact mapped `wn.cl()Lxg;` and then `xg.a()I` / `xg.e()F`, so the child-loader `FoodStats` object never crosses into parent runtime code.

Immediately before each normal return from mapped `Minecraft.runTick()`, the mapped player is forwarded through `Minecraft189RuntimeBridge.playerHunger(...)`. `Minecraft189HostRuntime` owns a synchronized `Minecraft189PlayerHungerState`, validates food level `0..20` and finite saturation `0..20`, and clears availability on null player and teardown.

The new Visuals module `render.hunger` / **Hunger** renders `Hunger: 17/20 | Sat: 6.5` only while a live sample exists. Persistent X/Y settings use the existing generic setting, presentation, profile and module-binding paths.

Executable regression coverage now models the real inheritance chain `pk -> pr -> wn -> bew`, shape-verifies a synthetic `xg`, publishes live `17 / 6.5` through transformed `runTick()`, and proves null-player clearing. Separate HUD coverage proves rendered text/position, persistence, invalid-value rejection and complete feature teardown.

## Potion-effect mapping authority

M110 pins the minimal Minecraft 1.8.9 surface needed for collection-backed status effects without yet publishing a collection into the parent runtime. `PotionEffect` is exact obfuscated class `pf`. `EntityLivingBase.getActivePotionEffects()` is `pr.bl()Ljava/util/Collection;` / Searge `func_70651_bq`.

The exact `PotionEffect` primitive/string readers are `pf.a()I` / `getPotionID()`, `pf.b()I` / `getDuration()`, `pf.c()I` / `getAmplifier()`, and `pf.g()Ljava/lang/String;` / `getEffectName()`.

The existing `EntityLivingBase` gate now requires the active-effect collection method, and a dedicated `PotionEffect` shape gate requires all four readers. Existing transformed-host fixtures were updated to satisfy the stricter authority without introducing runtime potion behavior.

This milestone is authority-only. A future consumer must reduce the child-side collection into a narrow parent-owned snapshot rather than exposing raw potion collections.

## Live Potion Effects HUD

M111 consumes the independently certified M110 active-effect authority while preserving the transforming classloader boundary. Transformed `PotionEffect` (`pf`) implements the parent-owned `Minecraft189PotionEffectAccess` interface, whose surface contains only potion ID, duration ticks, amplifier and effect-name string. Transformed `EntityLivingBase` (`pr`) implements `Minecraft189PlayerPotionEffectsAccess`; its generated getter calls exact mapped `pr.bl()Ljava/util/Collection;` and immediately converts that raw collection with `Collection.toArray(T[])` into an array typed only to the parent-owned effect interface.

The concrete `pf` type and raw active-effect collection never appear in parent-facing method signatures. `Minecraft189HostRuntime` immediately copies every interface view into validated immutable parent-owned snapshots, sorted by effect name and potion ID. Null player and host teardown clear availability.

The new Visuals module `render.potionEffects` / **Potion Effects** renders one line per active effect, including exact mapped effect-name key, amplifier level and duration, for example `potion.moveSpeed Lv 2 1:30`. Persistent X/Y settings use the existing generic settings/profile path and lines advance by 12 UI units.

Executable transformed-host coverage now loads synthetic mapped `pf` instances into `pr`'s active-effect collection and proves mapped `runTick()` publishes exact IDs, durations, amplifiers and names before null-player clearing. Focused HUD coverage proves deterministic sorting, multi-line placement, persisted coordinates, invalid-snapshot rejection and complete feature teardown.

## Experience mapping authority

M112 pins the minimal Minecraft 1.8.9 player-experience surface on exact base `EntityPlayer` `wn`. The exact public fields are `wn.bB I` / Searge `field_71068_ca` / `experienceLevel`, `wn.bC I` / `field_71067_cb` / `experienceTotal`, and `wn.bD F` / `field_71106_cc` / `experience`. The exact experience-bar capacity method is `wn.ck()I` / `func_71050_bK` / `xpBarCap()`.

The existing `EntityPlayer` shape gate now requires all three experience fields in addition to the already-certified food-state surface and requires `xpBarCap()`. Regression coverage pins all four members, accepts the exact synthetic shape, and rejects missing experience progress or bar-cap authority.

The transformed-host synthetic `wn` fixture was updated to satisfy the stricter gate without adding runtime XP publication. This milestone is authority-only; no experience state or HUD is exposed yet.

## Live Experience HUD

M113 consumes the independently certified M112 experience authority. Transformed base `EntityPlayer` `wn` now implements the parent-owned `Minecraft189PlayerExperienceAccess` contract. Generated getters read exact mapped `experienceLevel`, `experienceTotal`, `experience` progress and call exact `xpBarCap()`; only primitive values cross the transforming classloader boundary.

Immediately before each normal return from mapped `Minecraft.runTick()`, the player is forwarded through `Minecraft189RuntimeBridge.playerExperience(...)`. `Minecraft189HostRuntime` owns a synchronized `Minecraft189PlayerExperienceState`, validates non-negative level/total, finite progress in `0..1`, and a positive bar cap, and clears availability on null player and teardown.

The new Visuals module `render.experience` / **Experience** renders level, current-bar progress and total XP, for example `XP: Lv 27 | 21/42 | Total 12345`. Persistent X/Y settings use the existing generic settings/profile path.

Executable transformed-host coverage now supplies exact synthetic `wn.bB=27`, `wn.bC=12345`, `wn.bD=0.5` and `wn.ck()=42`, proving mapped `runTick()` publishes all four values and that null-player clearing removes the snapshot. Focused HUD coverage proves rendering, persisted coordinates, validation, disable behavior and complete feature teardown.

## Ping mapping authority

M114 pins the minimal Minecraft 1.8.9 surface needed to resolve the local client's tab-list latency without introducing UUID/session lookup plumbing. Exact class `bet` is `net/minecraft/client/entity/AbstractClientPlayer`; exact class `bdc` is `net/minecraft/client/network/NetworkPlayerInfo`.

The exact lookup method is `bet.b()Lbdc;` / Searge `func_175155_b` / `getPlayerInfo()`. The exact latency reader is `bdc.c()I` / `func_178853_c` / `getResponseTime()`.

Dedicated shape gates require both methods, and regression coverage pins the class names, method names/descriptors and missing-method failure behavior. This milestone is authority-only. A future consumer may reduce the child-side `NetworkPlayerInfo` object to a primitive ping value before crossing the parent runtime boundary.

## Live Ping HUD

M115 consumes the independently certified M114 ping authority through the actual client-player inheritance path. Transformed `AbstractClientPlayer` `bet` implements parent-owned `Minecraft189PlayerPingAccess`; its generated accessor calls exact `bet.b()Lbdc;` / `getPlayerInfo()`, returns an internal `-1` unavailable sentinel when the player-info object is absent, otherwise immediately calls exact `bdc.c()I` / `getResponseTime()`. No `NetworkPlayerInfo` object crosses the parent runtime boundary.

Mapped `Minecraft.runTick()` forwards the local `bew` player through this inherited interface. `Minecraft189HostRuntime` clears ping availability for null players or the internal unavailable sentinel and otherwise copies a non-negative millisecond value into synchronized parent-owned `Minecraft189PlayerPingState`.

The new Visuals module `render.ping` / **Ping** renders `Ping: <milliseconds> ms`, with persistent X/Y settings through the existing generic settings/profile path.

Executable transformed-host coverage now models the real synthetic hierarchy `pk -> pr -> wn -> bet -> bew`, transforms exact synthetic `bet` and shape-verifies exact synthetic `bdc`, proves a response time of `57` reaches host state, proves missing `NetworkPlayerInfo` clears availability while the player still exists, and preserves null-player clearing. Focused HUD coverage proves rendering, persisted coordinates, sentinel handling, state validation, disable behavior and complete teardown.

## Server-address mapping authority

M116 pins the minimal Minecraft 1.8.9 field surface needed to expose the current multiplayer server address. Exact class `bde` is `net/minecraft/client/multiplayer/ServerData`.

The exact Minecraft field is `ave.Q Lbde;` / Searge `field_71422_O` / `currentServerData`. The exact address field is `bde.b Ljava/lang/String;` / `field_78845_b` / `serverIP`.

The existing `Minecraft` shape gate now requires `currentServerData`, and a dedicated `ServerData` gate requires `serverIP`. Regression coverage pins both descriptors and missing-field failures. The transformed-host synthetic `ave` fixture was updated for the stricter gate without adding runtime server-address behavior.

This milestone is authority-only. A future consumer may transform `bde` into a parent-owned string accessor so that no concrete `ServerData` object crosses the transforming classloader boundary.

## Live Server HUD

M117 consumes the independently certified M116 server-address authority. Exact `ServerData` `bde` is transformed to implement parent-owned `Minecraft189ServerDataAccess`; its generated accessor reads only exact mapped `bde.b Ljava/lang/String;` / `serverIP`. No concrete `ServerData` instance appears in parent-facing state.

Immediately before every normal return from mapped `Minecraft.runTick()`, exact `ave.Q Lbde;` / `currentServerData` is loaded, cast to the parent-owned access interface, and forwarded through `Minecraft189RuntimeBridge.serverAddress(...)`. A null server-data reference clears availability. The host also clears null or blank address strings and otherwise copies a trimmed string into synchronized parent-owned `Minecraft189ServerAddressState`.

The new Visuals module `render.server` / **Server** renders `Server: <address>` with persistent X/Y settings through the generic profile path.

Executable transformed-host coverage now transforms exact synthetic `bde`, assigns `ave.Q` to a server object whose `b` field is `play.example.net:25565`, proves mapped `runTick()` publishes that exact address, and proves clearing `ave.Q` removes Server availability independently of player state. Focused HUD coverage proves normalization, rendering, persistent coordinates, null/blank clearing, validation, disable behavior and complete feature teardown.

## Held-item mapping authority

M118 pins the minimal Minecraft 1.8.9 `ItemStack` surface needed for a held-item HUD. The held slot itself was already certified earlier through `EntityLivingBase.pr.p(I)Lzx;` / `getEquipmentInSlot(int)`, where slot `0` is the held item.

Exact `ItemStack` `zx` members added here are `zx.b I` / Searge `field_77994_a` / `stackSize`; `zx.q()Ljava/lang/String;` / `func_82833_r` / `getDisplayName()`; `zx.h()I` / `func_77952_i` / `getItemDamage()`; and `zx.j()I` / `func_77958_k` / `getMaxDamage()`.

A dedicated `ItemStack` shape gate requires the exact field and all three methods. Regression coverage pins their owners, names, descriptors and missing-member failure behavior. This milestone is authority-only; it does not yet transform `zx` or publish held-item state.

## Live Held Item HUD

M119 consumes the independently certified M118 `ItemStack` authority together with the earlier certified slot convention for `EntityLivingBase.getEquipmentInSlot(int)`, where slot `0` is the held item.

Transformed `ItemStack` `zx` implements parent-owned `Minecraft189ItemStackAccess`, exposing only display name, stack size, item damage and maximum damage. Transformed `EntityLivingBase` `pr` implements parent-owned `Minecraft189PlayerHeldItemAccess`; its generated getter calls exact `pr.p(0)Lzx;` and casts the result to the parent-owned item interface. A null slot remains null, and no concrete `zx` type appears in the parent-facing signature.

Mapped `Minecraft.runTick()` forwards the local player through this access boundary. `Minecraft189HostRuntime` immediately copies held-item values into synchronized `Minecraft189HeldItemState` and clears the state for a null player or empty hand. State validation requires a non-blank display name, positive stack size, non-negative item damage and non-negative maximum damage. Item damage is not constrained by max damage because non-damageable subtype items may use metadata while reporting max damage `0`.

The new Visuals module `render.heldItem` / **Held Item** uses persistent X/Y settings. Damageable items render remaining durability, for example `Held: Diamond Sword | Dur: 1534/1561`; non-damageable stacks render count when greater than one, for example `Held: Ender Pearl x16`.

Executable transformed-host coverage now transforms exact synthetic `zx`, places a sword in exact slot `0`, proves all four mapped values reach host state, and proves clearing only slot `0` removes Held Item availability while the player remains live. Focused HUD coverage proves durability/count formatting, normalization, persisted coordinates, empty-hand clearing, validation, disable behavior and complete feature teardown.

## Live Armor durability detail

M120 enriches the existing M107 **Armor** HUD by consuming only mappings already certified by M106 and M118. No new Minecraft member is guessed or introduced: exact armor slots remain `pr.p(I)Lzx;` / `getEquipmentInSlot(int)` with slots `1..4`, and exact `ItemStack` durability readers remain `zx.h()I` / `getItemDamage()` plus `zx.j()I` / `getMaxDamage()`.

The transformed `EntityLivingBase` armor boundary now also exposes parent-owned `Minecraft189ItemStackAccess` views for boots, leggings, chestplate and helmet. Concrete child-loader `zx` objects still never appear in host-facing signatures or retained host state. `Minecraft189HostRuntime` copies only item-damage and max-damage primitives into the existing synchronized armor snapshot each tick, while the original occupancy mask remains authoritative for equipped-slot identity.

The existing `render.armor` / **Armor** module keeps its established module ID and persistent X/Y settings. When durability detail is available it renders exact per-slot remaining/max values, for example `Armor: 3/4 [H 353/363 | C 500/528 | L - | B 180/195]`. Older or synthetic callers that provide only the original occupancy booleans retain the compact presence-only fallback.

Executable transformed-host coverage now places distinct synthetic `zx` instances in exact armor slots `1`, `3` and `4`, proves their mapped durability reaches immutable parent-owned snapshots, preserves the missing leggings slot, and keeps null-player teardown semantics unchanged. Focused Armor HUD coverage proves rich formatting while retaining the original presence-only compatibility path.

## World-time mapping authority

M121 pins the minimal Minecraft 1.8.9 world-time surface without publishing live world state yet. Exact base `World` is obfuscated class `adm`, exact client world is `WorldClient` `bdb`, and exact `Minecraft.theWorld` is `ave.f Lbdb;` / Searge `field_71441_e`.

The exact base-world time reader is `adm.L()J` / Searge `func_72820_D` / `getWorldTime()`. The existing Minecraft shape gate now requires the world field, and a dedicated World shape gate requires that exact method. Mapping regressions pin both world classes, the field owner/name/descriptor and the world-time method owner/name/descriptor.

The transformed-host Minecraft fixture now carries the exact `bdb` field dependency so the stricter authority remains executable. This milestone is authority-only: it does not yet transform `adm`, forward a world object, publish a time snapshot, or register a HUD.

## Live World Time HUD

M122 consumes the independently certified M121 world-time authority. Exact base `World` `adm` is transformed to implement the parent-owned `Minecraft189WorldTimeAccess` contract; its generated accessor delegates only to exact mapped `adm.L()J` / `getWorldTime()`. Exact `WorldClient` `bdb` inherits that interface, so no concrete child-loader world object appears in retained host state.

Immediately before every normal return from mapped `Minecraft.runTick()`, exact `ave.f Lbdb;` / `theWorld` is loaded and forwarded through `Minecraft189RuntimeBridge.worldTime(...)`. `Minecraft189HostRuntime` immediately copies the primitive long into synchronized parent-owned `Minecraft189WorldTimeState`; a null world clears availability, and host teardown clears the state.

The new Visuals module `render.worldTime` / **World Time** uses persistent X/Y settings and renders Minecraft's day clock with the vanilla offset, for example raw world time `6000` as `Time: 12:00` and `18000` as `Time: 00:00`. Time-of-day normalization uses floor-modulo `24000`, so negative and overflow world times remain deterministic without discarding the raw long.

Executable transformed-host coverage models exact `adm -> bdb` inheritance, publishes synthetic world time `6000` through transformed `runTick()`, proves the host snapshot and clock conversion, and proves null-world clearing independently of player, ping, server and held-item state. Focused HUD coverage proves rendering, persisted coordinates, negative/overflow normalization, disable behavior and complete feature teardown.

## Selected hotbar-slot mapping authority

M123 pins the minimal Minecraft 1.8.9 selected-hotbar-slot path without publishing live slot state yet. Exact `InventoryPlayer` is obfuscated class `wm`. Exact `EntityPlayer.inventory` is `wn.bi Lwm;` / Searge `field_71071_by`, and exact `InventoryPlayer.currentItem` is `wm.c I` / Searge `field_70461_c`.

The EntityPlayer class-shape gate now requires the inventory field, and a dedicated InventoryPlayer class-shape gate requires the selected-slot integer. Mapping regressions pin the exact class, owner, field name, descriptor and Searge/MCP names. The transformed-host EntityPlayer fixture now carries the exact `Lwm;` dependency and a matching synthetic `wm.c I` class so stricter verification remains executable.

This milestone is authority-only. It does not transform `wm`, publish a selected slot, change Held Item rendering, or register a new HUD.

## Live Hotbar Slot HUD

M124 consumes the independently certified M123 selected-slot authority. Exact `InventoryPlayer wm` is transformed to implement the parent-owned `Minecraft189InventoryHotbarAccess` contract and exposes only exact mapped `wm.c I` / `currentItem`. Exact `EntityPlayer wn` exposes its `wn.bi Lwm;` inventory only through parent-owned `Minecraft189PlayerInventoryAccess`; the host immediately copies the selected-slot integer and does not retain the child-loader inventory object.

Immediately before every normal return from mapped `Minecraft.runTick()`, the mapped player is forwarded through `Minecraft189RuntimeBridge.playerHotbarSlot(...)`. A null player, null inventory, or slot outside `0..8` clears availability. `Minecraft189HostRuntime` owns and clears synchronized `Minecraft189HotbarSlotState` during teardown.

The new Visuals module `render.hotbarSlot` / **Hotbar Slot** has persistent X/Y settings and renders the user-facing 1-based slot, for example raw `currentItem = 4` as `Slot: 5/9`.

Executable transformed-host coverage transforms exact `wm`, places it behind exact `wn.bi`, publishes synthetic slot `4` through transformed `runTick()`, proves the parent-owned state snapshot, and proves null-inventory clearing independently of ping, world time, server and held-item state. Focused HUD coverage proves rendering, persisted coordinates, invalid-slot clearing, disable behavior, state validation and complete feature teardown.

## Dimension mapping authority

M125 pins the minimal Minecraft 1.8.9 player-dimension source on base `Entity`: exact obfuscated field `pk.am I`, Searge `field_71093_bK`, MCP `dimension`. Because every live player inherits from `Entity`, no `WorldProvider` object boundary is required for the consumer milestone.

The existing Entity class-shape gate now requires this exact integer alongside position and yaw. Mapping regressions pin owner, obfuscated name, descriptor, Searge name and MCP name, and transformed-host fixtures carry exact `pk.am I` so stricter verification remains executable.

This milestone is authority-only. It does not publish dimension state, label dimensions, register a HUD, or change runtime behavior.

## Live Dimension HUD

M126 consumes certified M125 authority directly from transformed base `Entity pk`. The transformed class now implements parent-owned `Minecraft189PlayerDimensionAccess` and exposes only exact mapped `pk.am I` through `customMcDimension()`. All live player classes inherit that narrow interface from `Entity`.

Immediately before each normal return from mapped `Minecraft.runTick()`, the current player is forwarded through `Minecraft189RuntimeBridge.playerDimension(...)`. `Minecraft189HostRuntime` immediately copies the primitive dimension ID into synchronized `Minecraft189PlayerDimensionState`; no WorldProvider, World or concrete Entity object is retained. A null player clears availability, and runtime teardown clears the state.

The new Visuals module `render.dimension` / **Dimension** owns persistent X/Y settings. Vanilla IDs are rendered as `Dimension: Nether (-1)`, `Dimension: Overworld (0)` and `Dimension: End (1)`; arbitrary custom IDs remain visible as `Dimension: <id>`.

Executable transformed-host coverage sets inherited exact `pk.am` on the synthetic player, publishes it through the transformed runTick path, verifies the parent-owned snapshot, and verifies null-player clearing. Focused HUD coverage verifies vanilla/custom labels, persisted coordinates, disable behavior and complete feature teardown.

## Weather mapping authority

M127 pins the minimal Minecraft 1.8.9 weather-state methods on exact base `World adm`: `adm.S()Z` / Searge `func_72896_J` / MCP `isRaining`, and `adm.R()Z` / Searge `func_72911_I` / MCP `isThundering`.

The existing World class-shape gate now requires world time, rain and thunder authority together. Mapping regressions separately reject missing rain and missing thunder methods, and the transformed-host synthetic `adm` fixture exposes both exact boolean methods so stricter verification remains executable.

This milestone is authority-only. It does not publish live weather state, retain any additional world object, or register a HUD.

## Live Weather HUD

M128 consumes certified M127 weather authority through the existing transformed World boundary. Exact base `adm` now implements parent-owned `Minecraft189WorldWeatherAccess` alongside the World Time access contract; `customMcRaining()` delegates only to exact `adm.S()Z`, and `customMcThundering()` delegates only to exact `adm.R()Z`.

Immediately before each normal return from mapped `Minecraft.runTick()`, exact `ave.f Lbdb;` is forwarded through `Minecraft189RuntimeBridge.worldWeather(...)`. `Minecraft189HostRuntime` immediately copies two booleans into synchronized `Minecraft189WorldWeatherState`; no concrete World or WorldClient instance is retained. A null world clears availability, and runtime teardown clears the state.

The new Visuals module `render.weather` / **Weather** owns persistent X/Y settings. It renders `Weather: Clear`, `Weather: Rain`, or `Weather: Thunder`, with thunder taking display precedence when the mapped thunder flag is true.

Executable transformed-host coverage sets exact synthetic `adm` rain/thunder values behind inherited `bdb`, publishes them through transformed `runTick()`, verifies the parent-owned weather snapshot, and verifies null-world clearing independently. Focused HUD coverage verifies Clear/Rain/Thunder rendering, thunder precedence, persisted coordinates, disable behavior, and complete feature teardown.

## Movement-state mapping authority

M129 pins the minimal Minecraft 1.8.9 movement-status members on exact base `Entity pk`: `pk.av()Z` / Searge `func_70093_af` / MCP `isSneaking`, `pk.aw()Z` / Searge `func_70051_ag` / MCP `isSprinting`, and exact boolean field `pk.C Z` / Searge `field_70122_E` / MCP `onGround`.

The existing Entity class-shape gate now requires these three movement signals alongside position, yaw and dimension. Mapping regressions separately reject missing on-ground, sneaking and sprinting authority, and the transformed-host synthetic `pk` fixture exposes the exact field/method shapes so stricter verification remains executable.

This milestone is authority-only. It does not publish movement status, retain a player object, or register a HUD.

## Live Movement Status HUD

M130 consumes certified M129 movement-state authority directly from transformed base `Entity pk`. The transformed class now implements parent-owned `Minecraft189PlayerMovementStateAccess` alongside the existing position, rotation and dimension contracts. `customMcOnGround()` reads exact `pk.C Z`, `customMcSneaking()` delegates to exact `pk.av()Z`, and `customMcSprinting()` delegates to exact `pk.aw()Z`.

Immediately before each normal return from mapped `Minecraft.runTick()`, exact `ave.h Lbew;` is forwarded through `Minecraft189RuntimeBridge.playerMovementState(...)`. `Minecraft189HostRuntime` immediately copies only three booleans into synchronized `Minecraft189PlayerMovementState`; no concrete Entity/player object is retained. A null player clears availability, and runtime teardown clears the state.

The new Visuals module `render.movementStatus` / **Movement Status** owns persistent X/Y settings. It renders the exact mapped posture without inferring motion speed: `Normal`, `Sneak`, `Sprint`, or `Sprint+Sneak`, followed by `Ground` or `Air`.

Executable transformed-host coverage sets exact inherited `pk.C` plus synthetic backing state for exact `pk.av()/pk.aw()`, publishes through transformed `runTick()`, verifies the parent-owned snapshot, and verifies null-player clearing. Focused HUD coverage verifies all posture labels, ground/air rendering, persisted coordinates, disable behavior, and complete feature teardown.

## Sprint-control mapping authority

M131 pins the Minecraft 1.8.9 sprint mutator on exact base `Entity pk`: `pk.d(Z)V` / Searge `func_70031_b` / MCP `setSprinting`.

The existing Entity class-shape gate now requires this mutator alongside certified `isSneaking`, `isSprinting`, `onGround`, position, yaw and dimension authority. A dedicated regression rejects a missing sprint mutator, and the transformed-host synthetic `pk` fixture implements the exact boolean setter against its sprinting backing state.

This milestone is authority-only. It does not force sprinting, alter movement policy, or register a module.

## Auto Sprint movement control

M132 introduces the first action-oriented cheat module and a dedicated **Movement** category. `movement.autoSprint` is registered as **Auto Sprint** and is disabled by default like other modules.

Transformed base `Entity pk` implements parent-owned `Minecraft189PlayerSprintControl`. Its only operation, `customMcSetSprinting(boolean)`, delegates directly to certified M131 authority `pk.d(Z)V` / `func_70031_b` / `setSprinting`.

Mapped `Minecraft.runTick()` first publishes the current movement snapshot from exact `ave.h Lbew;`, then forwards the same player only transiently through `Minecraft189RuntimeBridge.playerSprintControl(...)`. Host policy reads the just-published primitive movement snapshot and calls `setSprinting(true)` only while Auto Sprint is enabled, movement state is available, the player is not sneaking, and the mapped sprint state is not already true. The player/control object is never retained.

Disabling Auto Sprint stops intervention; it deliberately does not call `setSprinting(false)`, so vanilla/manual sprint state is not clobbered. Focused tests cover disabled behavior, successful sprint forcing, no redundant setter call when already sprinting, sneak suppression, disable behavior, Movement-category lifecycle, and exact transformed `pk.d(Z)V` execution.

## Right-click delay mapping authority

M133 pins the Minecraft 1.8.9 right-click cooldown field on exact `Minecraft ave`: `ave.ap I` / Searge `field_71467_ac` / MCP `rightClickDelayTimer`.

The existing Minecraft class-shape gate now requires this integer field alongside the previously certified player/world/render/settings/server fields. A dedicated regression rejects a missing right-click delay field, and the transformed-host synthetic `ave` fixture carries exact `ap I` parity.

This milestone is authority-only. It does not alter click delay or register Fast Place.

## Fast Place player control

M134 introduces a dedicated **Player** category and the disabled-by-default `player.fastPlace` / **Fast Place** module. Its persisted integer `Delay` setting accepts 0 through 4 ticks and defaults to 0.

No child-loader object crosses the host boundary. Immediately before each normal return from mapped `Minecraft.runTick()`, transformed `ave` reads certified M133 `ave.ap I`, passes that primitive to `Minecraft189RuntimeBridge.rightClickDelay(int)`, and writes the returned primitive directly back to exact `ave.ap`.

While Fast Place is disabled, the delay is returned unchanged. While enabled, the module only lowers a delay that is greater than the configured value; it never increases an already-lower value. Disabling therefore stops intervention without restoring or inventing a timer value.

Transformed-host coverage proves exact `ave.ap` behavior for disabled `4`, default enabled `4 -> 0`, configured `4 -> 2`, lower-delay preservation at `1`, and disable behavior. Focused coverage verifies Player-category lifecycle, setting persistence, and complete teardown.

## Left-click counter mapping authority

M135 pins the Minecraft 1.8.9 left-click cooldown counter on exact `Minecraft ave`: `ave.ag I` / Searge `field_71429_W` / MCP `leftClickCounter`.

The Minecraft class-shape gate now requires this integer counter alongside certified right-click delay and the existing host-hook fields. A dedicated regression rejects a missing left-click counter, and the transformed-host synthetic `ave` fixture carries exact `ag I` parity.

This milestone is authority-only. It does not alter attack cooldown behavior or register a combat module.

## No Hit Delay combat control

M136 introduces the dedicated **Combat** category and disabled-by-default `combat.noHitDelay` / **No Hit Delay** module.

No Minecraft object crosses the host boundary. Immediately before each normal return from mapped `Minecraft.runTick()`, transformed `ave` reads certified M135 `ave.ag I`, passes that primitive to `Minecraft189RuntimeBridge.leftClickCounter(int)`, and writes the returned primitive directly back to exact `ave.ag`.

While disabled, the counter passes through unchanged. While enabled, the module converts only positive counter values to zero; zero and negative values are preserved. Disabling stops intervention without inventing or restoring a counter value.

Transformed-host coverage proves exact `ave.ag` behavior for disabled `7`, enabled `7 -> 0`, preservation of `-1`, and disable pass-through. Focused coverage verifies Combat-category lifecycle and complete module teardown.

## Auto Clicker combat scheduling

M137 adds disabled-by-default `combat.autoClicker` / **Auto Clicker** under the existing Combat category. Persisted integer settings `Min CPS` and `Max CPS` accept 1 through 20, defaulting to 8 and 12. If configured in reverse order, runtime scheduling safely normalizes the lower and upper bound.

The module uses the existing host-owned physical input state and only schedules clicks while legacy left mouse button 0 is held. Its phase accumulator is tied to the mapped 20 Hz game tick, so at most one synthetic click is emitted per tick and the configured maximum is explicitly 20 CPS. Releasing the physical button or disabling the module resets scheduler credit.

Immediately before each normal return from transformed `Minecraft.runTick()`, after primitive Fast Place and No Hit Delay policies have run, transformed `ave` is passed transiently through parent-owned `Minecraft189ClickMouseControl` to the runtime bridge. The injected bytecode remains straight-line and frame-neutral; the normal Java bridge performs the scheduling branch and, when due, calls `customMcClickMouse()`, whose generated delegate invokes already-certified exact `Minecraft.clickMouse = ave.aw()V / func_147116_af`. No child-loader object crosses into retained host state. Generated clicks are recorded in the existing click-rate tracker so the CPS HUD reflects synthetic clicks.

Transformed-host coverage instruments exact `ave.aw()V` and proves disabled, physical-hold, release-reset and disable behavior at deterministic 10 CPS. Focused coverage proves exactly 10 generated clicks across 20 held ticks at 10 CPS, persisted Min/Max settings, scheduler reset semantics, and teardown.

## Jump-control mapping authority

M138 pins exact Minecraft 1.8.9 jump-control authority on base `EntityLivingBase pr`: `pr.bF()V` / Searge `func_70664_aZ` / MCP `jump`.

The existing EntityLivingBase class-shape gate now requires this exact method alongside health, equipment and potion-effect authority. Mapping regression rejects a missing jump method explicitly, and the transformed-host synthetic `pr` fixture implements executable `bF()V` behavior through a test-only jump counter so later consumer coverage can prove the exact mapped invocation path.

This milestone is authority-only. It does not trigger jumps, retain a player object, or register a movement module.

## Auto Jump movement control

M139 adds disabled-by-default `movement.autoJump` / **Auto Jump** under the existing Movement category.

Transformed base `EntityLivingBase pr` implements parent-owned `Minecraft189PlayerJumpControl`; generated `customMcJump()` delegates only to certified M138 authority `pr.bF()V` / `func_70664_aZ`. Immediately before each normal return from mapped `Minecraft.runTick()`, exact `ave.h Lbew;` is forwarded transiently through the jump-control boundary after the current movement-state snapshot has been published. The host does not retain the player object.

Auto Jump uses exact mapped `onGround` state and fires once per grounded contact. After a jump, it remains disarmed while the snapshot is still grounded and re-arms only after observing an airborne snapshot, preventing duplicate jump calls across adjacent grounded ticks. Disabling the module resets its arm state.

Focused coverage verifies disabled behavior, one jump per ground contact, airborne re-arm, disable behavior, category lifecycle and teardown. Transformed-host coverage executes the inherited `bew -> ... -> pr` control interface and proves exact `pr.bF()V` invocation through the synthetic jump counter.

## Sneak-control mapping authority

M140 pins exact Minecraft 1.8.9 sneak-control authority on base `Entity pk`: `pk.c(Z)V` / Searge `func_70095_a` / MCP `setSneaking`.

The existing Entity class-shape gate now requires this exact mutator alongside movement-state and sprint-control authority. Mapping regression rejects a missing setSneaking method explicitly, and the transformed-host synthetic `pk` fixture implements executable `c(Z)V` behavior against its test-only sneaking field so the consumer milestone can prove the exact mapped invocation path.

This milestone is authority-only. It does not force sneak state or register a movement module.

## Auto Sneak movement control

M141 adds disabled-by-default `movement.autoSneak` / **Auto Sneak** under the existing Movement category.

Transformed base `Entity pk` implements parent-owned `Minecraft189PlayerSneakControl`; generated `customMcSetSneaking(boolean)` delegates only to certified M140 authority `pk.c(Z)V` / `func_70095_a`. Immediately before each normal return from mapped `Minecraft.runTick()`, exact `ave.h Lbew;` is forwarded transiently through the sneak-control boundary after the current movement-state snapshot has been published. The host retains no player object.

While enabled and a current mapped snapshot is available, Auto Sneak calls the exact setter only when the player is not already sneaking. While disabled it does not force either state, so ordinary vanilla/input state remains authoritative rather than being overwritten by a synthetic release.

Focused coverage verifies disabled behavior, one setter call when sneak needs enabling, no duplicate setter while already sneaking, disable behavior, Movement-category lifecycle and teardown. Transformed-host coverage proves the inherited player path reaches exact synthetic `pk.c(Z)V`.

## No Slow mapping authority

M142 pins the exact Minecraft 1.8.9 surface required for item-use movement slowdown: `MovementInput beu`, exact `EntityPlayerSP.movementInput = bew.b Lbeu;` / Searge `field_71158_b`, `MovementInput.moveStrafe = beu.a F` / `field_78902_a`, `MovementInput.moveForward = beu.b F` / `field_78900_b`, and `EntityPlayerSP.onLivingUpdate = bew.m()V` / `func_70636_d`.

Separate class-shape gates cover `bew` and `beu`, with regressions for each required member. The transformed-host fixture now includes a concrete synthetic `beu` and executable `bew.m()V` that performs the vanilla-style `moveStrafe *= 0.2F` and `moveForward *= 0.2F` stores.

This milestone is authority-only. It does not alter slowdown values or register a movement module.

## No Slow movement control

M143 adds disabled-by-default `movement.noSlow` / **No Slow** under the Movement category, consuming only the M142-certified `EntityPlayerSP.onLivingUpdate` and `MovementInput` authority.

The transformer now handles exact `EntityPlayerSP bew` and `MovementInput beu`. Inside exact `bew.m()V`, it requires exactly one `PUTFIELD beu.a F` and one `PUTFIELD beu.b F` target. Immediately before each certified store, the already-computed float is passed through the runtime bridge. No branch, local-variable rewrite, or child-loader object crosses the boundary.

When disabled, the bridge returns the value unchanged, preserving vanilla item-use slowdown. When enabled, `Minecraft189NoSlowModule` multiplies the already-slowed value by `5.0F`, exactly reversing vanilla's `0.2F` factor while leaving vanilla's own item-use/riding condition and surrounding `onLivingUpdate` flow intact.

Focused coverage verifies disabled/enabled/disabled movement-factor behavior and lifecycle cleanup. Transformed-host coverage executes the synthetic vanilla-style `bew.m()V`: inputs `1.0 / -0.75` become `0.2 / -0.15` when disabled, remain `1.0 / -0.75` when enabled, and return to vanilla slowdown after disable.

## Fast Break mapping authority

M144 pins the exact Minecraft 1.8.9 primitive surface required for block-hit delay control: `PlayerControllerMP bda`, `Minecraft.playerController = ave.c Lbda;` / Searge `field_71442_b`, and `PlayerControllerMP.blockHitDelay = bda.g I` / Searge `field_78781_i`.

The Minecraft class-shape gate now requires the exact controller field, while a dedicated PlayerControllerMP gate requires exact `bda.g I`. Regression coverage pins class, owner, obfuscated name, descriptor, Searge name and MCP name, and independently rejects a missing block-hit delay field.

The transformed-host fixture includes synthetic `bda.g I` and exact `ave.c Lbda;` parity so the consumer milestone can prove a real controller-object path. This milestone is authority-only and does not modify block breaking behavior.

## Fast Break player control

M145 adds disabled-by-default `player.fastBreak` / **Fast Break** under the Player category, consuming only M144-certified `Minecraft.playerController = ave.c Lbda;` and `PlayerControllerMP.blockHitDelay = bda.g I` authority.

Transformed `PlayerControllerMP bda` implements parent-owned `Minecraft189BlockHitDelayControl`; generated `customMcSetBlockHitDelay(int)` writes only exact `bda.g I`. Immediately before each normal return from mapped `Minecraft.runTick()`, exact `ave.c Lbda;` is forwarded transiently through the control interface. The host retains no child-loader controller object and the injected path adds no conditional branch.

While Fast Break is enabled, the host sets the current controller's block-hit delay to zero. While disabled, the module performs no write at all, leaving vanilla controller state authoritative.

Focused coverage verifies disabled/enabled/disabled behavior, null-controller tolerance, Player-category lifecycle and teardown. Transformed-host coverage proves exact `ave.c -> bda.g` execution against the mapped synthetic controller.

## Speed Mine mapping authority

M146 extends the certified Minecraft 1.8.9 PlayerControllerMP surface for mining-progress control: `PlayerControllerMP.curBlockDamageMP = bda.e F` / Searge `field_78770_f` and `PlayerControllerMP.isHittingBlock = bda.h Z` / Searge `field_78778_j`. Existing Fast Break authority `bda.g I` remains required.

The dedicated PlayerControllerMP class-shape gate now requires all three exact primitive fields. Regression coverage independently rejects drift in `blockHitDelay`, `curBlockDamageMP`, and `isHittingBlock`, while transformed-host fixture parity now carries concrete `g/e/h` fields.

This milestone is authority-only. It does not modify mining progress or register a Speed Mine module.

## Speed Mine player control

M147 adds disabled-by-default `player.speedMine` / **Speed Mine** under the Player category, consuming only M146-certified `PlayerControllerMP.curBlockDamageMP = bda.e F` and `PlayerControllerMP.isHittingBlock = bda.h Z` authority.

Transformed `PlayerControllerMP bda` implements parent-owned `Minecraft189BlockMiningControl` alongside the existing Fast Break control. The generated boundary exposes only the active-mining boolean, current primitive block-damage progress, and an exact progress setter. Mapped `Minecraft.runTick()` forwards exact `ave.c Lbda;` transiently to the host; no child-loader controller object is retained.

Speed Mine owns persisted integer setting `player.speedMine.progressPercent` from 0 through 100, default 70. While enabled and the controller reports an active block hit, current non-negative progress below the configured threshold is raised to that threshold. Progress already at or above the threshold, inactive mining, null controller state, and all disabled-module ticks are left untouched. No packet spoofing or guessed hardness calculation is introduced.

Focused coverage verifies disabled/enabled guards, active-mining gating, high-progress preservation, persisted 90% override, null tolerance and teardown. Transformed-host coverage proves exact `ave.c -> bda.h/e` execution at the 70% default and 90% override.

## Timer Speed mapping authority

M148 pins exact Minecraft 1.8.9 timer-rate authority: `Timer = avl`, `Minecraft.timer = ave.Y Lavl;` / Searge `field_71428_T`, and `Timer.timerSpeed = avl.d F` / Searge `field_74278_d`.

The Minecraft class-shape gate now requires the exact timer field and a dedicated Timer gate requires the exact float speed field. Mapping regressions pin class, owner, descriptor, Searge and MCP names, while transformed-host fixture parity includes a concrete synthetic `avl.d F` and exact `ave.Y Lavl;` field.

This milestone is authority-only. It does not change tick rate or register a Timer module.

## Configurable Timer Speed

M149 adds disabled-by-default `player.timer` / **Timer** under the Player category with persistent integer `Speed %` setting `player.timer.speedPercent`, default 100 and range 10–300.

Transformed `Timer avl` implements parent-owned `Minecraft189TimerSpeedControl`; generated primitive accessors read/write only certified M148 `avl.d F` / `field_74278_d`. Immediately before each normal return from mapped `Minecraft.runTick()`, exact `ave.Y Lavl;` is forwarded transiently to the host. No Timer object is retained.

While enabled, the module applies the configured multiplier as `percent / 100.0F`. While disabled, the live Timer is restored to vanilla `1.0F`. Focused coverage verifies persistence, multiple configured multipliers, reset behavior and teardown; transformed-host coverage proves exact `ave.Y -> avl.d` execution.

## Step-height mapping authority

M150 pins exact Minecraft 1.8.9 Entity step-height authority: `Entity.stepHeight = pk.S F` / Searge `field_70138_W`.

The existing Entity class-shape gate now requires this exact float field alongside position, rotation, dimension and movement-state authority. Mapping regression rejects a missing step-height field explicitly, and transformed-host fixture parity now carries concrete synthetic `pk.S F` state for the consumer milestone.

This milestone is authority-only. It does not modify step height or register a Step module.

## Configurable Step movement control

M151 consumes certified M150 `Entity.stepHeight = pk.S F / field_70138_W` through parent-owned `Minecraft189PlayerStepControl`. Transformed base `Entity pk` exposes only primitive getter/setter delegates; exact `ave.h Lbew;` is forwarded transiently from mapped `Minecraft.runTick()`, and the host retains no child-loader player object.

`movement.step` / **Step** is disabled by default. While disabled it restores vanilla player step height `0.6F`. While enabled it applies the persisted `movement.step.heightPercent` setting, default `100` (= `1.0F` block) with a supported range of `60..250` percent in 5-percent UI increments.

Focused coverage verifies registration, persistence, configured height changes, disable restoration, null safety and teardown. Transformed-host coverage proves the inherited `bew -> ... -> pk` boundary reads and writes exact synthetic `pk.S`, including disabled restoration to `0.6F`, enabled `1.75F`, and restoration after disable.

## Fall-distance mapping authority

M152 pins exact Minecraft 1.8.9 Entity fall-distance authority: `Entity.fallDistance = pk.O F` / Searge `field_70143_R`.

The existing Entity class-shape gate now requires this exact float field alongside position, rotation, dimension, movement-state and step-height authority. Mapping regression rejects a missing fall-distance field explicitly, and transformed-host fixture parity now carries concrete synthetic `pk.O F` state for the consumer milestone.

This milestone is authority-only. It does not reset fall distance or register a No Fall module.

## No Fall movement control

M153 consumes certified M152 `Entity.fallDistance = pk.O F / field_70143_R` through parent-owned `Minecraft189PlayerFallDistanceControl`. Transformed base `Entity pk` exposes only primitive getter/setter delegates; exact `ave.h Lbew;` is forwarded transiently from mapped `Minecraft.runTick()`, and the host retains no child-loader player object.

`movement.noFall` / **No Fall** is disabled by default. While enabled it clears non-zero local fall distance to `0.0F` on each forwarded tick. While disabled it performs no write at all: it does not restore, invent or cache a prior fall-distance value, so vanilla state remains authoritative immediately after disable.

Focused coverage verifies disabled preservation, enabled zeroing, no redundant write while already zero, subsequent zeroing, disable behavior, null safety and teardown. Transformed-host coverage proves the inherited `bew -> ... -> pk` control path reads and writes exact synthetic `pk.O`, including disabled preservation, enabled clearing, and no post-disable rewrite.

## Web-state mapping authority

M154 pins exact Minecraft 1.8.9 Entity web-state authority: `Entity.isInWeb = pk.H Z` / Searge `field_70134_J`.

The existing Entity class-shape gate now requires this exact boolean alongside position, movement-state, step-height and fall-distance authority. Mapping regression rejects a missing web-state field explicitly, and transformed-host fixture parity now carries concrete synthetic `pk.H Z` state for the consumer milestone.

This milestone is authority-only. It does not clear web state or register a No Web module.

## No Web movement control

M155 consumes certified M154 `Entity.isInWeb = pk.H Z / field_70134_J` through parent-owned `Minecraft189PlayerWebControl`. Transformed base `Entity pk` exposes only primitive boolean getter/setter delegates; exact `ave.h Lbew;` is forwarded transiently from mapped `Minecraft.runTick()`, and the host retains no child-loader player object.

`movement.noWeb` / **No Web** is disabled by default. While enabled it clears `isInWeb` only when the live mapped flag is true. While disabled it performs no synthetic write, so vanilla web state remains authoritative immediately after disable.

Focused coverage verifies disabled preservation, enabled clearing, no redundant write while already clear, repeated clearing, disable behavior, null safety and teardown. Transformed-host coverage proves the inherited `bew -> ... -> pk` path reads and writes exact synthetic `pk.H`, including disabled preservation, enabled clearing and post-disable preservation.

## Velocity mapping authority

M156 pins the exact Minecraft 1.8.9 knockback surface required for configurable velocity control: `Entity.motionX = pk.v D / field_70159_w`, `motionY = pk.w D / field_70181_x`, `motionZ = pk.x D / field_70179_y`, and `EntityLivingBase.knockBack = pr.a(Lpk;FDD)V / func_70653_a`.

The Entity class-shape gate now requires all three exact motion primitives after the previously certified movement fields, preserving targeted drift-regression ordering. The EntityLivingBase gate requires the exact knockBack method. Mapping tests pin exact owner/name/descriptor/Searge/MCP authority and reject missing motion or knockback members explicitly.

The transformed-host synthetic `pr.a(Lpk;FDD)V` is executable: it applies deterministic deltas to inherited `pk.v/w/x`, giving the consumer milestone an exact before/after motion target for delta-based knockback scaling.

This milestone is authority-only. It does not alter player velocity or register a combat module.

## Velocity combat control

M157 adds disabled-by-default `combat.velocity` / **Velocity** with persisted `Horizontal %` and `Vertical %` settings from 0–100. Both settings default to 0 while the module is enabled; disabling the module restores vanilla 100% knockback behavior without rewriting any stored player state.

Rather than patching global `EntityLivingBase pr`, transformed local-player class `bew` receives a generated override of certified `pr.a(Lpk;FDD)V` / `func_70653_a`. The override snapshots inherited `pk.v/w/x`, invokes the exact superclass knockback implementation, then passes only the before/after primitive motion values through the runtime bridge. Horizontal scaling applies independently to X/Z and vertical scaling to Y using `before + (after - before) * percent`.

This preserves pre-existing motion, limits the behavior to the local player, and retains no child-loader entity object in host state. Focused coverage verifies disabled 100%, enabled 0%, partial percentage scaling and setting bounds. Transformed-host coverage executes the generated `bew` override against the executable M156 knockback fixture and proves 100/0/partial delta behavior.

## No Clip mapping authority

M158 pins exact Minecraft 1.8.9 `Entity.noClip = pk.T Z` / Searge `field_70145_X`.

The Entity class-shape gate requires the field after the already-certified motion primitives so older targeted drift regressions retain their original failure targets. Mapping tests pin exact owner/name/descriptor/Searge/MCP authority and reject a missing noClip field explicitly. The transformed-host synthetic `pk` fixture now contains the exact boolean field for consumer execution.

This milestone is authority-only. It does not alter collision behavior or register a movement module.

## No Clip movement control

M159 adds disabled-by-default `movement.noClip` / **No Clip** under Movement using certified M158 authority `Entity.noClip = pk.T Z / field_70145_X`.

Transformed base `Entity pk` implements parent-owned `Minecraft189PlayerNoClipControl` with generated getter/setter delegates for exact `pk.T`. Each mapped `Minecraft.runTick()` forwards the live player transiently through that interface; the host retains no child-loader player object.

On enable, the module captures the player's current no-clip value the first time it receives a live player and then forces `true`. On disable, restoration is deferred until the next live-player control pass and restores exactly the captured baseline, including preserving a pre-existing `true` state rather than forcing `false`.

Focused coverage verifies disabled behavior, false-baseline enable/restore, true-baseline preservation, null safety, module/category teardown and pending-restore lifecycle. Transformed-host coverage proves exact inherited `bew -> ... -> pk.T` execution and both baseline cases through mapped `runTick()`.

## Air Jump movement control

M160 adds disabled-by-default `movement.airJump` / **Air Jump** without introducing any new Minecraft mapping authority. It reuses certified M138 jump control and M129 movement-state authority, plus the existing host-owned LWJGL input state.

The module requires a fresh Space key press while the local player is airborne. Holding Space does not retrigger jumps every tick, pressing Space while grounded does not arm a delayed midair jump, and leaving the ground while the same key press remains held does not fire. Releasing and pressing Space again while airborne permits another mapped `EntityLivingBase.jump()` invocation.

The mapped `runTick()` path still forwards the live player only through parent-owned interfaces; Air Jump retains no child-loader player object. Focused coverage verifies registration, fresh-press edge behavior, grounded suppression, held-key suppression, disable behavior and teardown. Transformed-host coverage executes the existing exact `bew -> pr.bF()V` jump delegate and proves repeat airborne jumps occur only on distinct Space presses.

## Flight movement control

M161 adds disabled-by-default `movement.flight` / **Flight** without introducing new mapping authority. It reuses certified M156 `Entity.motionY = pk.w D / field_70181_x` and the existing host-owned LWJGL input state.

Transformed base `Entity pk` now implements parent-owned `Minecraft189PlayerMotionControl` with primitive `motionY` getter/setter delegates. Mapped `Minecraft.runTick()` forwards the live player transiently through that interface; the parent runtime retains no child-loader entity object.

While enabled, Space sets vertical motion to `+0.30`, either Shift key sets it to `-0.30`, and neither or both inputs set it to `0.0` for hover. While disabled the module performs no motion write, so vanilla physics resumes without a synthetic restore value.

Focused coverage verifies disabled preservation, hover, ascend, both-key neutralization, left/right Shift descent, redundant-write avoidance, null safety and teardown. Transformed-host coverage proves exact inherited `bew -> ... -> pk.w` writes through mapped `runTick()` and post-disable preservation.

## Directional Flight movement control

M162 extends M161 **Flight** with yaw-relative horizontal movement using only already-certified M103 rotation authority and M156 `Entity.motionX/motionZ` authority. No new obfuscated member is introduced.

The parent-owned motion bridge now exposes primitive X/Y/Z getters and setters. While Flight is enabled and a mapped yaw snapshot is available, W/S provide forward/backward input and A/D provide left/right strafing. Diagonal input is normalized so W+A does not move faster than a single direction.

Minecraft's 1.8.9 yaw convention is preserved: yaw `0°` forward writes positive Z, yaw `90°` forward writes negative X. With no horizontal input Flight writes X/Z to zero for a stationary hover. If rotation authority is temporarily unavailable, horizontal motion is left untouched while M161 vertical control continues to operate.

Focused coverage verifies forward flight, normalized diagonal flight, yaw-relative turning, horizontal hover and post-disable preservation. Transformed-host coverage proves exact `pk.v` / `pk.x` writes from live mapped yaw through `Minecraft.runTick()`.

## Configurable Flight speeds

M163 keeps the certified M161/M162 Flight behavior but moves its fixed `0.30` horizontal and vertical motion magnitudes behind persistent module settings. `movement.flight.horizontalSpeed` and `movement.flight.verticalSpeed` are DOUBLE settings with default `0.30`, supported range `0.05..1.00`, and `0.05` UI increments.

The setting registrations use the normal `SettingRegistry`, `SettingPresentationRegistry`, and `ModuleSettingRegistry` lifecycle so configuration persists with the rest of the client and is removed cleanly during feature teardown.

Horizontal normalization, mapped-yaw direction, hover semantics, Space ascent, Shift descent, missing-rotation preservation and disabled no-write behavior are unchanged. Focused coverage proves default registration, configured `0.60` horizontal / `0.45` vertical execution and teardown. Transformed-host coverage proves those configured values reach exact mapped `pk.v/w/x` state through `Minecraft.runTick()`.

## Strafe movement control

M164 adds **Movement → Strafe** without introducing a new Minecraft mapping. It reuses the certified M103 yaw snapshot and M156/M162 primitive `Entity.motionX/motionZ` bridge.

While Strafe is enabled, a live mapped yaw snapshot exists, and at least one W/A/S/D key is held, the module writes configurable yaw-relative horizontal motion. Diagonal input is normalized so combined directions do not exceed the configured magnitude. With no movement input, unavailable rotation, a null player, or the module disabled, Strafe performs no motion write and leaves vanilla state untouched.

`movement.strafe.speed` is a persistent DOUBLE setting with default `0.30`, range `0.05..1.00`, and `0.05` UI increments. Flight has explicit precedence: when Flight is enabled, Strafe suspends its writes instead of competing for `motionX/Z`.

Focused coverage proves unavailable-yaw preservation, yaw-relative forward/diagonal motion, configured speed, no-input preservation, Flight precedence, disable behavior, and setting teardown. Transformed-host coverage proves exact live `pk.v/pk.x` writes through `Minecraft.runTick()` and the same Flight-over-Strafe precedence.

## Glide movement control

M165 adds **Movement → Glide** without introducing any new Minecraft mapping. The module reuses the certified M129 on-ground movement snapshot and M156/M161 primitive `Entity.motionY` bridge.

While Glide is enabled, the mapped movement snapshot is available, the player is airborne, and current vertical motion is more negative than the configured cap, Glide raises `motionY` to that cap. It never alters upward motion or a gentler descent, and grounded/unavailable/null/disabled states perform no write.

`movement.glide.fallSpeed` is a persistent DOUBLE setting with default `0.08`, range `0.01..0.50`, and `0.01` UI increments. Flight has explicit precedence: Glide suspends while Flight is active so the two modules never compete for `motionY`.

The mapped `runTick()` ordering is part of the authority boundary: live movement-state capture occurs before motion control, so Glide consumes the same-tick on-ground state rather than a guessed or reflected value. Focused coverage proves unavailable-state preservation, grounded preservation, excessive-descent capping, upward/gentle-descent preservation, configured speed, Flight precedence, disable behavior, and setting teardown. Transformed-host coverage proves exact `pk.w` behavior through `Minecraft.runTick()`.

## Fast Fall movement control

M166 adds **Movement → Fast Fall** without introducing any new Minecraft mapping. It reuses the same certified M129 airborne-state snapshot and M156/M161 primitive `Entity.motionY` bridge as Glide.

While Fast Fall is enabled, the mapped movement snapshot is available, the player is airborne, and vertical motion is already negative but gentler than the configured target, Fast Fall lowers `motionY` to that target. Upward motion, grounded state, unavailable state, null players, disabled state, and descent already faster than the configured target are left untouched.

`movement.fastFall.fallSpeed` is a persistent DOUBLE setting with default `0.30`, range `0.05..1.00`, and `0.05` UI increments. Motion ownership is explicit: **Flight > Fast Fall > Glide**. Flight suspends Fast Fall, and an enabled Fast Fall suspends Glide, so no pair competes for `motionY`.

Focused coverage proves gentle-descent acceleration, rising preservation, already-fast descent preservation, configured speed, Flight precedence, Fast-Fall-over-Glide precedence, grounded preservation, disable behavior, and setting teardown. Transformed-host coverage proves the same ownership chain against exact mapped `pk.w` state through `Minecraft.runTick()`.

## Freeze movement control

M167 adds **Movement → Freeze** using only the already-certified primitive `Entity.motionX/motionY/motionZ` bridge. No new Minecraft mapping is introduced.

While Freeze is enabled it writes each non-zero mapped motion component to zero and then terminates the shared motion-control pass. Redundant zero writes are avoided. Null players and the disabled state perform no writes.

Freeze has explicit top-priority ownership over the existing movement writers: **Freeze > Flight > Fast Fall > Glide**, while Strafe is also suspended whenever Freeze owns the pass. This prevents competing writes and keeps motion arbitration deterministic.

Focused coverage proves exact X/Y/Z zeroing, redundant-write avoidance, top-priority ownership over Flight/Strafe/Fast Fall/Glide, disable behavior, null safety, and teardown. Transformed-host coverage proves exact mapped `pk.v/pk.w/pk.x` zeroing through `Minecraft.runTick()` and confirms Flight resumes immediately after Freeze is disabled.

## Long Jump movement control

M168 adds **Movement → Long Jump** without introducing a new Minecraft mapping. It composes the certified M138 jump delegate, M103 yaw snapshot, M129 on-ground snapshot, and M156/M162 primitive `Entity.motionX/motionZ` bridge.

The mapped `runTick()` ordering is the authority boundary: movement state is captured first, jump control runs next, and motion control runs later in the same tick. A fresh Space press while grounded and while at least one W/A/S/D key is held invokes the mapped jump delegate and arms exactly one same-tick horizontal boost. Holding Space does not retrigger. The boost is yaw-relative, diagonal-normalized, and uses the persistent `movement.longJump.speed` DOUBLE setting (default `0.65`, range `0.10..1.50`, step `0.05`).

Ownership is explicit. Freeze and Flight suspend Long Jump. While Long Jump is enabled it suppresses Auto Jump so the two ground-jump writers cannot compete. On the single boost tick Long Jump owns horizontal motion over Strafe; after that tick Strafe resumes normally. Air Jump remains an independent airborne fresh-press feature.

Focused coverage proves the movement-input requirement, fresh-press semantics, default/configured yaw-relative boost, Long-Jump-over-Strafe ownership, Freeze/Flight suspension, Auto-Jump suppression, disable behavior, and setting teardown. Transformed-host coverage proves the real mapped jump call plus exact `pk.v/pk.x` boost through `Minecraft.runTick()`.

## Bunny Hop movement control

M169 adds **Movement → Bunny Hop** without introducing a new Minecraft mapping. It composes the certified M138 jump delegate, M103 yaw snapshot, M129 on-ground snapshot, and M156/M162 primitive `Entity.motionX/motionZ` bridge.

While Bunny Hop is enabled and at least one W/A/S/D key is held, a grounded mapped movement snapshot triggers one jump. The module stays disarmed while the snapshot remains grounded and re-arms only after an airborne snapshot, preventing repeated jump calls against a stale ground state. Its horizontal writer applies yaw-relative, diagonal-normalized motion using persistent `movement.bunnyHop.speed` (DOUBLE, default `0.38`, range `0.10..1.00`, step `0.05`).

Ownership is deterministic: **Freeze/Flight > Long Jump > Bunny Hop > Auto Jump/Strafe**. Long Jump suspends Bunny Hop entirely while active. Bunny Hop suppresses Auto Jump and owns horizontal motion over Strafe whenever movement input is present. Air Jump remains an independent airborne fresh-press feature.

Focused coverage proves movement-input gating, grounded jump arming, airborne re-arm, configured yaw-relative speed, Bunny-Hop-over-Strafe ownership, Auto-Jump suppression, Long-Jump/Flight suspension, disable behavior, and setting teardown. Transformed-host coverage proves repeated mapped jump cycles plus exact `pk.v/pk.x` horizontal motion through `Minecraft.runTick()`.

## High Jump movement control

M170 adds **Movement → High Jump** without introducing a new Minecraft mapping. It composes the certified M138 mapped jump delegate, M129 on-ground snapshot, and M156/M161 primitive `Entity.motionY` bridge.

A fresh Space press while the mapped movement snapshot is grounded invokes the real jump delegate and arms one same-tick vertical boost. Motion control then raises `motionY` to at least the configured `movement.highJump.verticalSpeed` value (DOUBLE, default `0.70`, range `0.42..1.50`, step `0.05`). Already-higher vertical motion is preserved rather than reduced. Holding Space does not retrigger.

Jump ownership is deterministic: **Freeze/Flight > Long Jump > High Jump > Bunny Hop > Auto Jump**. High Jump suppresses Bunny Hop and Auto Jump while enabled, but does not own horizontal motion, so Strafe may still operate alongside it. Air Jump remains an independent airborne fresh-press feature.

Focused coverage proves fresh-press semantics, default/configured vertical boost, Auto-Jump suppression and release, Strafe coexistence, Long-Jump precedence, Flight suspension, disable behavior, and setting teardown. Transformed-host coverage proves the real mapped jump delegate plus exact `pk.w` vertical boost through `Minecraft.runTick()`.

## Movement Speed control

M171 adds the classic **Movement → Speed** cheat as `movement.speed` while preserving the existing **Visuals → Speed** HUD at `render.speed`. The distinct IDs and Java types allow both modules to coexist without registry collision.

Movement Speed introduces no new Minecraft mapping. It reuses the certified M129 on-ground snapshot, M103 yaw snapshot, and M156/M162 primitive `Entity.motionX/motionZ` bridge. While enabled, grounded, supplied with a live yaw snapshot, and receiving W/A/S/D input, it writes configurable yaw-relative horizontal motion. Diagonal input is normalized. Airborne, unavailable-state, no-input, null-player, disabled, and suspended states perform no write.

`movement.speed.speed` is a persistent DOUBLE setting with default `0.45`, range `0.10..1.00`, and `0.05` UI increments. Horizontal ownership is deterministic: **Freeze/Flight > Long Jump boost > Bunny Hop > Speed > Strafe**. High Jump remains vertical-only and may coexist with Speed.

Focused coverage proves the `render.speed` / `movement.speed` ID separation, unavailable/airborne preservation, yaw-relative and diagonal motion, configured speed, Speed-over-Strafe ownership, Flight precedence, no-input behavior, disable behavior, and setting teardown. Transformed-host coverage proves exact grounded `pk.v/pk.x` writes and airborne preservation through `Minecraft.runTick()`.

## Low Hop movement control

M172 adds **Movement → Low Hop** without introducing a new Minecraft mapping. It composes the certified M138 mapped jump delegate, M129 on-ground snapshot, and M156/M161 primitive `Entity.motionY` bridge.

A fresh Space press while grounded invokes the real mapped jump delegate and arms one same-tick vertical adjustment. Motion control then caps `motionY` to at most the configured `movement.lowHop.verticalSpeed` value (DOUBLE, default `0.25`, range `0.05..0.41`, step `0.01`). Already-lower vertical motion is preserved rather than increased. Holding Space does not retrigger.

Jump ownership is deterministic: **Freeze/Flight > Long Jump > High Jump > Low Hop > Bunny Hop > Auto Jump**. Low Hop suppresses Bunny Hop and Auto Jump while enabled, but it remains vertical-only, so Movement Speed and Strafe can still provide horizontal motion alongside it.

Focused coverage proves fresh-press semantics, default/configured vertical cap, Auto-Jump suppression and release, Movement-Speed coexistence, High-Jump precedence, Flight suspension, disable behavior, and setting teardown. Transformed-host coverage proves the real mapped jump delegate plus exact `pk.w` low-hop cap through `Minecraft.runTick()`.

## No Gravity movement control

M173 adds **Movement → No Gravity** without introducing a new Minecraft mapping. It reuses the certified M129 on-ground snapshot and M156/M161 primitive `Entity.motionY` bridge.

While enabled and airborne, No Gravity cancels only negative vertical motion by writing `motionY = 0`. Upward motion, grounded state, unavailable movement state, null players, and disabled state are left untouched. This makes it distinct from Flight: it does not synthesize ascent or horizontal movement and only removes descent.

Vertical ownership is deterministic: **Freeze > Flight > No Gravity > Fast Fall > Glide**. High Jump, Low Hop, and Long Jump still own their same-tick jump adjustments before the player becomes airborne on the next movement snapshot.

Focused coverage proves unavailable/grounded/upward preservation, exact downward cancellation, No-Gravity-over-Fast-Fall/Glide ownership, Flight precedence, disable release, null safety, and teardown. Transformed-host coverage proves exact negative-to-zero `pk.w` behavior, upward preservation, and Fast Fall resumption through `Minecraft.runTick()`.

## Air Speed movement control

M174 adds **Movement → Air Speed** without introducing a new Minecraft mapping. It is the airborne counterpart to M171 `movement.speed`: both reuse the certified M129 on-ground snapshot, M103 yaw snapshot, and M156/M162 primitive `Entity.motionX/motionZ` bridge.

While Air Speed is enabled, the mapped movement snapshot is available and airborne, a live yaw snapshot exists, and W/A/S/D input is held, it writes configurable yaw-relative horizontal motion. Diagonal input is normalized. Grounded, unavailable-state, no-input, null-player, disabled, and suspended states perform no write.

`movement.airSpeed.speed` is a persistent DOUBLE setting with default `0.35`, range `0.10..1.00`, and `0.05` UI increments. Ground Speed and Air Speed are state-disjoint: Ground Speed owns only grounded ticks, Air Speed owns only airborne ticks. Horizontal ownership is deterministic: **Freeze/Flight > Long Jump boost > Bunny Hop > Ground/Air Speed > Strafe**.

Focused coverage proves unavailable/grounded preservation, exact airborne yaw-relative and diagonal motion, configured speed, Ground-Speed/Air-Speed state separation, Air-Speed-over-Strafe ownership, Flight precedence, no-input preservation, disable behavior, and setting teardown. Transformed-host coverage proves exact airborne `pk.v/pk.x` writes through `Minecraft.runTick()`.

## Reverse Step movement control

M175 adds **Movement → Reverse Step** without introducing a new Minecraft mapping. It reuses the certified M129 on-ground movement snapshot and the M156/M161 primitive `Entity.motionY` bridge.

The module tracks mapped ground-state transitions. The first available snapshot only primes state. A later **ground → air** transition is eligible for one downward snap, but only when current `motionY <= 0`; upward motion is preserved so normal jumps and the existing High Jump, Low Hop, Long Jump and Bunny Hop paths are not crushed. Staying airborne does not retrigger. Descent already faster than the configured target is also preserved.

`movement.reverseStep.speed` is a persistent DOUBLE setting with default `0.50`, range `0.05..1.50`, and `0.05` UI increments. Vertical ownership is deterministic: **Freeze/Flight/No Gravity > Reverse Step > Fast Fall/Glide**. Reverse Step still consumes movement-state transitions while suspended, preventing a delayed snap when the higher-priority module disables.

Focused coverage proves first-snapshot priming, one-shot edge transitions, no airborne retrigger, upward-jump preservation, already-fast descent preservation, configured speed, No-Gravity precedence without delayed activation, disable behavior, and setting teardown. Transformed-host coverage proves exact mapped `pk.w` snapping and upward-motion preservation through `Minecraft.runTick()`.

## Auto Clicker activation mode

M176 extends **Combat → Auto Clicker** on the existing certified `Minecraft.clickMouse()` bridge. No new Minecraft mapping is introduced.

The existing physical-hold behavior remains the default through persistent BOOLEAN setting `combat.autoClicker.requireHold = true`. When true, releasing LMB immediately resets the 20-tick CPS scheduler exactly as before. When explicitly set false, the enabled module continues the same configured Min/Max CPS schedule without requiring the physical left button.

Changing back to Require Hold while LMB is released resets scheduling on the next tick, preventing latent phase credit from producing a delayed click. Module disable still resets all schedule state. Generated clicks continue to feed the same click-rate tracker as held-mode clicks.

Focused coverage preserves the original hold-only schedule and proves toggle mode at deterministic 10 CPS, encoded setting persistence, reset-on-mode-return behavior, and teardown. Transformed-host coverage proves the real mapped `clickMouse()` delegate fires with LMB released only while Require Hold is false.

## Velocity amplification range

M177 extends **Combat → Velocity** without introducing any new Minecraft mapping or knockback hook. The existing certified `EntityLivingBase.knockBack` delta interception remains authoritative.

Horizontal and Vertical percentage settings now accept **0..200%** instead of only 0..100%. The module still scales only the incoming knockback delta relative to the pre-hit motion baseline: 0% cancels the delta, 100% preserves vanilla knockback, and values above 100% amplify the same delta without replacing pre-existing motion.

Defaults remain 0%, so existing AntiKB behavior is unchanged. Both settings retain 5% UI increments. Focused coverage proves 150% horizontal and 200% vertical amplification plus range rejection above 200%. Transformed-host coverage proves exact amplified mapped `pk.v/pk.w/pk.x` results through the real mapped knockback delegate.

## Configurable Fast Break delay

M178 extends **Player → Fast Break** without introducing a new Minecraft mapping. The existing certified player-controller block-hit-delay setter remains authoritative.

`player.fastBreak.delay` is now a persistent INTEGER setting with default `0`, range `0..5`, and step `1`. The default therefore preserves the previous Fast Break behavior exactly: every enabled control pass writes zero delay. Nonzero configured values let the same module retain a small local hit delay instead of forcing full removal.

The setting is registered through the normal setting, presentation, and module-binding lifecycle and is removed on feature teardown. Focused coverage proves the default zero behavior, a live configured delay of two ticks, disabled preservation, null safety, and setting teardown. Transformed-host coverage proves the configured value reaches the exact mapped player-controller delay field through `Minecraft.runTick()`.

## Configurable No Hit Delay

M179 extends **Combat → No Hit Delay** without introducing a new Minecraft mapping. The existing certified Minecraft left-click counter remains authoritative.

`combat.noHitDelay.delay` is now a persistent INTEGER setting with default `0`, range `0..10`, and step `1`. The default preserves the previous No Hit Delay behavior exactly: positive counters are clamped to zero. A nonzero configured value clamps only counters above that threshold, while equal, lower, zero, and negative counters are preserved.

The setting uses the normal registry, presentation, module-binding, and teardown lifecycle. Focused coverage proves the unchanged zero-delay default, a configured three-tick cap, preservation below the cap, disabled behavior, and teardown. Transformed-host coverage proves the same behavior against the exact mapped Minecraft left-click counter through `runTick()`.

## Configurable No Slow speed

M180 extends **Movement → No Slow** without introducing a new Minecraft mapping. The existing certified item-use movement-input adjustment remains authoritative.

`movement.noSlow.speedPercent` is a persistent INTEGER setting with default `100`, range `20..100`, and step `5`. Vanilla item use reduces movement input to 20% before the module's adjustment, so 20% leaves that vanilla slowdown unchanged while 100% applies the existing ×5 restoration and preserves the previous full NoSlow behavior exactly.

Intermediate values scale the already-slowed movement deterministically: for example 60% turns a vanilla 0.20 input into 0.60, and -0.15 into -0.45. Disabled behavior remains a pass-through.

The setting uses the normal registry, presentation, module-binding, and teardown lifecycle. Focused coverage proves the unchanged 100% default, 60% partial restoration, disabled pass-through, and teardown. Transformed-host coverage proves the same values through the exact mapped item-use `onLivingUpdate` movement-input path.

## Configurable No Fall threshold

M181 extends **Movement → No Fall** without introducing a new Minecraft mapping. The existing certified `Entity.fallDistance` primitive bridge remains authoritative.

`movement.noFall.threshold` is a persistent DOUBLE setting with default `0.0`, range `0.0..10.0`, and step `0.5`. The default preserves the previous behavior exactly: any meaningful non-zero fall distance is cleared. With a higher threshold, values at or below that threshold are preserved and only larger absolute fall-distance values are reset to zero.

The setting uses the normal registry, presentation, module-binding, and teardown lifecycle. Focused coverage proves the unchanged zero-threshold default, a configured `3.0` threshold, preservation at and below the threshold, clearing above it, disabled behavior, null safety, and teardown. Transformed-host coverage proves the same behavior against exact mapped `pk.O` through `Minecraft.runTick()`.

## Hurt-time mapping authority

M182 extends the pinned Minecraft 1.8.9 mapping authority with exact local combat-state metadata before any runtime consumer is added.

The already-pinned `BigBroadBean/mappings-extracted` commit `2265da88e93c20411ec70f0b892f4fbc84ebc3c9` proves the exact field through both retained authority blobs:

- `fields.csv` blob `a8c5928cb64455dcc2712078dbfe018ae97cafb9`: `pr,au,field_70737_aN,hurtTime`.
- `joined.srg` blob `0b1e3f1d0156abcbd70e2b09b720379fc0c1eae6`: `FD: pr/au net/minecraft/entity/EntityLivingBase/field_70737_aN`.

Therefore `EntityLivingBase.hurtTime` is pinned as exact obfuscated `pr.au : I` / Searge `field_70737_aN` / MCP `hurtTime`.

The `EntityLivingBase` structural gate now requires that exact integer field before any transformed consumer can rely on it. Mapping regression pins owner/name/descriptor/Searge/MCP identity, a dedicated drift regression rejects a shape missing `pr.au I`, existing method-drift regressions retain their original failure targets, and transformed-host fixtures now carry the exact field.

This milestone is authority-only: it does not expose hurt time across the classloader boundary, retain a player object, or alter combat behavior. A later consumer can build a narrow primitive hurt-time snapshot only after this exact authority head is certified.

## Live Hurt Time HUD

M183 consumes the independently certified M182 `EntityLivingBase.hurtTime = pr.au I / field_70737_aN` authority without exposing a Minecraft implementation object to the parent runtime.

Transformed `EntityLivingBase pr` implements the parent-owned `Minecraft189PlayerHurtTimeAccess` contract, whose only member is `customMcHurtTime()I`. The generated getter reads exact mapped `pr.au`. Immediately before each normal return from mapped `Minecraft.runTick()`, the current player reference is transiently cast to that parent-owned interface and forwarded through `Minecraft189RuntimeBridge.playerHurtTime(...)`. The host copies only the integer into synchronized `Minecraft189PlayerHurtTimeState`; a null player clears availability, and the child-loader player object is never retained.

The new Visuals module `render.hurtTime` / **Hurt Time** renders `Hurt Time: <ticks>` only while a live snapshot exists. Persistent X/Y settings use the existing generic setting, presentation, profile, and module-binding architecture.

Focused regression coverage proves live text and position, setting persistence, negative-value rejection, null-player hiding, render-pass lifecycle, and complete feature teardown. Transformed-host coverage sets exact inherited `pr.au`, proves the primitive value reaches parent state through `runTick()`, and proves a null mapped player clears that state.

## Hurt-time Damage Boost

M184 turns the certified M182/M183 local hurt-time signal into **Movement → Damage Boost** without adding a new Minecraft mapping or retaining a child-loader object.

The mapped `runTick()` return pipeline now snapshots hurt time before horizontal motion control, so the parent-owned motion arbitration sees the current tick's `pr.au` value. Damage Boost tracks only primitive hurt-time history. Its first available sample primes state. Normal countdowns and unchanged values do nothing; a later increase such as `4 → 10` is treated as a fresh hurt-time reset and applies one horizontal multiplier to the final current `motionX/motionZ`. This avoids multiplying every hurt tick while still recognizing another hit before the counter reaches zero.

`movement.damageBoost.multiplier` is a persistent DOUBLE setting with default `1.25`, range `1.0..3.0`, and step `0.05`.

Ownership is explicit: Freeze and Flight suppress the write, but the hurt-time reset is still consumed while suspended so disabling either module cannot cause a delayed boost. Outside those exclusive modes, Damage Boost runs after normal horizontal movement ownership and therefore multiplies the final horizontal motion for that one hit reset.

Focused coverage proves first-sample priming, countdown preservation, one-shot reset detection, configurable multiplier, constant-counter non-retrigger, Flight suppression without delayed activation, Freeze suppression without delayed activation, lifecycle, and setting teardown. Transformed-host coverage proves exact `pr.au` is sampled before motion control and that `4 → 10` produces the expected same-tick mapped `pk.v/pk.x` boost exactly once.

## Damage Boost vertical scaling

M185 extends **Movement → Damage Boost** on the certified M182/M183 hurt-time state and the existing primitive motion bridge. No new Minecraft mapping is introduced.

The original `movement.damageBoost.multiplier` setting remains the horizontal multiplier and keeps its existing default of `1.25`. A new persistent DOUBLE setting, `movement.damageBoost.verticalMultiplier`, controls `motionY` with default `1.00`, range `1.00..3.00`, and step `0.05`. The neutral vertical default preserves M184 behavior exactly unless the user explicitly opts into vertical amplification.

Both horizontal and vertical scaling occur only on the same one-shot fresh-hurt-time edge already certified by M184. Freeze/Flight suspension semantics remain unchanged and still consume hit resets to prevent delayed boosts. Focused coverage proves the neutral default and configured vertical scaling; transformed-host coverage proves exact mapped `pk.w` amplification alongside `pk.v/pk.x`.

## rotationPitch mapping authority

M186 pins Minecraft 1.8.9 `Entity.rotationPitch` before any feature is allowed to consume it.

The frozen mapping source already recorded by `Minecraft189Mappings` resolves the field as:

- owner: `pk` / `net/minecraft/entity/Entity`
- obfuscated field: `z`
- descriptor: `F`
- SRG: `field_70125_A`
- MCP: `rotationPitch`

The authority is now represented as `Minecraft189Mappings.ENTITY_ROTATION_PITCH`, included in the Entity class-shape gate, covered by an exact mapping assertion, and guarded by a dedicated missing-field failure regression. The transformed-host Entity fixture also contains exact `pk.z : F`, so future pitch consumers cannot silently rely on a synthetic host that omits the real mapped field.

No runtime feature writes or reads pitch in M186; this milestone establishes mapping authority only.

## Live rotation pitch bridge

M187 promotes the M186 `rotationPitch` mapping into the same loader-safe primitive snapshot boundary already used for yaw.

The transformed 1.8.9 Entity implementation now exposes `customMcRotationPitch()` through the parent-owned `Minecraft189PlayerRotationAccess` interface. `Minecraft189HostRuntime.playerRotation()` samples yaw and pitch together in one call, and `Minecraft189PlayerRotationState.Snapshot` carries both values atomically.

No child-loader Entity instance is retained by parent runtime code. Only primitive `float` yaw/pitch values cross the boundary. Non-finite yaw or pitch values are rejected before state mutation, and `clear()` resets availability plus both primitive values.

Focused state coverage proves initial unavailable state, atomic yaw/pitch updates, rejection of non-finite values without corrupting the prior snapshot, and clear semantics. Transformed-host coverage proves exact mapped `pk.y` / `pk.z` values reach the parent-owned snapshot during the normal `Minecraft.runTick()` path.

M187 is read-only authority plumbing; feature-level rotation mutation remains a later milestone.

## Rotation control bridge

M188 adds a loader-safe write boundary for the rotation primitives certified by M103/M186/M187.

`Minecraft189PlayerRotationControl` is a parent-owned interface extending the read-only rotation access contract with `customMcSetRotationYaw(float)` and `customMcSetRotationPitch(float)`. The transformed 1.8.9 Entity implements that interface and the generated setters write directly to exact mapped fields `pk.y : F` and `pk.z : F`.

The parent runtime does not retain the child Entity instance, and M188 does not add any automatic rotation behavior. It establishes only a narrow primitive control surface for later modules. Transformed-host coverage casts the real transformed player to the parent interface, writes yaw/pitch through the generated methods, asserts the exact mapped fields changed, then restores the original values before the normal runTick snapshot proof continues.

No new Minecraft mapping is introduced in M188.

## Combat Jitter

M189 adds **Combat → Jitter** entirely on the certified M187/M188 rotation snapshot and control bridge. No new Minecraft mapping or runTick transformer injection is introduced.

The module exposes persistent DOUBLE settings `combat.jitter.yawDegrees` and `combat.jitter.pitchDegrees`, both defaulting to `0.50`, ranging `0.0..5.0`, with `0.10` UI steps. Jitter is active only while the physical left mouse button is held; Auto Clicker's optional no-hold mode does not implicitly activate rotation jitter.

Each eligible rotation callback first captures the raw mapped yaw/pitch snapshot, then applies a deterministic alternating phase. The first held tick adds the configured offsets and the next held tick subtracts them from the newly sampled rotation, returning to baseline when the user has not moved the mouse. Releasing LMB, disabling the module, or losing a usable rotation control resets the next phase to positive. Pitch writes are clamped to Minecraft's `[-90, 90]` viewing range.

Focused coverage proves alternating phases, release reset, custom amplitudes, pitch clamping, lifecycle and setting teardown. Transformed-host coverage proves exact `pk.y/pk.z` writes through the M188 control bridge while the parent-owned snapshot remains the pre-jitter rotation for that tick.

## Configurable Jitter interval

M190 extends **Combat → Jitter** without introducing any new Minecraft mapping or rotation hook. The certified live yaw/pitch read-write bridge remains authoritative.

`combat.jitter.intervalTicks` is a persistent INTEGER setting with default `1`, range `1..10`, and step `1`. The default therefore preserves M189 exactly: while physical LMB is held, every eligible rotation tick alternates the configured positive and negative yaw/pitch offset. Larger values apply one offset immediately, skip `intervalTicks - 1` eligible ticks, then apply the opposite phase.

Releasing LMB, losing rotation authority, disabling the module, or receiving a null player resets both cadence and phase. The next valid held tick therefore always starts immediately with the positive phase instead of inheriting stale cooldown state.

Focused coverage proves default one-tick parity, a three-tick cadence, deterministic phase alternation, release reset, and setting teardown. Transformed-host coverage proves the same cadence against exact mapped `rotationYaw` and `rotationPitch` writes through `Minecraft.runTick()`.

## Jitter activation mode

M191 extends **Combat → Jitter** on the same certified live yaw/pitch control bridge.

`combat.jitter.requireHold` is a persistent BOOLEAN setting with default `true`. The default preserves existing behavior exactly: Jitter is eligible only while physical LMB is held, and release resets cadence and phase. When explicitly set to `false`, the enabled module runs continuously on eligible rotation ticks without requiring LMB.

Switching Require Hold back to `true` while LMB is released immediately returns to the inactive/reset path, so no stale cadence or phase is carried into the next held activation. The M190 interval setting remains authoritative in either activation mode.

Focused coverage proves default hold-only behavior, no-hold mode, mode return/reset, and setting teardown. Transformed-host coverage proves exact mapped yaw/pitch writes with LMB released only while Require Hold is false.

## Jitter axis controls

M192 extends **Combat → Jitter** with independent persistent BOOLEAN controls for the two already-certified rotation axes.

`combat.jitter.yawEnabled` and `combat.jitter.pitchEnabled` both default to `true`, preserving M191 behavior exactly. Disabling one axis leaves that mapped primitive untouched while the other axis continues using the configured amplitude, cadence and activation mode. Disabling both axes makes Jitter a no-op and resets cadence/phase so the next re-enabled axis starts from the deterministic positive phase.

No new Minecraft mapping, transformer injection or child-loader object retention is introduced. Focused coverage proves yaw-only, pitch-only, both-disabled reset behavior and teardown. Transformed-host coverage proves pitch-only operation leaves exact mapped `pk.y` unchanged while writing exact mapped `pk.z`.

## Mapped Spin

M193 adds **Combat → Spin** on the certified M187/M188 rotation snapshot/control bridge. No new Minecraft mapping or transformer injection is introduced.

`combat.spin.yawSpeed` is a persistent DOUBLE setting with default `20.0`, range `1.0..180.0`, and UI step `1.0`. While enabled, each eligible rotation callback adds the configured yaw delta and wraps the result into `[-180, 180)`; pitch is never written.

Rotation ownership is explicit: **Spin > Jitter**. If Spin writes yaw on a tick, Jitter is sent through its reset path instead of becoming a second writer. Disabling Spin immediately releases that ownership and Jitter resumes from its deterministic positive phase on the next eligible tick.

Focused coverage proves enable/disable behavior, configurable yaw speed, wraparound, pitch preservation, lifecycle and setting teardown. Transformed-host coverage proves exact mapped `pk.y` mutation, exact `pk.z` preservation, and Spin-over-Jitter precedence through the normal `Minecraft.runTick()` path.

## Spin direction control

M194 extends **Combat → Spin** with persistent BOOLEAN setting `combat.spin.reverse`, default `false`. The default preserves M193 exactly: each eligible tick adds the configured positive yaw speed. When Reverse is enabled, the same certified yaw authority applies the configured speed in the negative direction before normal `[-180, 180)` wrapping.

Pitch remains untouched and the existing **Spin > Jitter** ownership rule is unchanged. No new Minecraft mappings or transformer hooks are introduced.

Focused coverage proves the default direction, reverse direction, wraparound, pitch preservation and setting teardown. Transformed-host coverage proves exact reverse-direction writes to mapped `pk.y` while mapped `pk.z` remains unchanged.

## Spin activation mode

M195 extends **Combat → Spin** with persistent BOOLEAN setting `combat.spin.requireHold`, default `false`. The default preserves M194 exactly: enabled Spin owns yaw continuously on eligible rotation ticks. When Require Hold is enabled, Spin only writes while the physical left mouse button is held.

A released Require Hold tick returns `false` from Spin, so the existing rotation ownership chain is preserved: Jitter may run normally if it is independently eligible. When LMB is held and Spin writes, **Spin > Jitter** still applies and Jitter is reset rather than becoming a second writer.

No new Minecraft mapping or transformer injection is introduced. Focused coverage proves default continuous operation, hold-gated suppression/activation and teardown. Transformed-host coverage proves that released-LMB Spin performs no yaw write and yields ownership to the independently eligible pitch-only Jitter, then held LMB restores Spin ownership for an exact mapped reverse-yaw write while preserving the handed-off pitch.

## Configurable Spin interval

M196 extends **Combat → Spin** with persistent INTEGER setting `combat.spin.intervalTicks`, default `1`, range `1..10`, and UI step `1`. The default preserves M195 exactly: every eligible Spin tick writes the configured yaw delta.

Larger values write immediately, then skip `intervalTicks - 1` eligible ticks before the next Spin write. Ineligible state, disabling the module, missing rotation authority, or an unsatisfied Require Hold condition resets the cadence so the next eligible activation writes immediately.

Rotation ownership remains explicit per tick. A Spin write keeps **Spin > Jitter** and resets Jitter. A cadence skip returns `false`, deliberately yielding that tick to independently eligible Jitter. Focused coverage proves default parity, a three-tick cadence, reset semantics and setting teardown. Transformed-host coverage proves exact mapped yaw writes on Spin ticks and pitch-only Jitter ownership on the two skipped ticks.

## W-Tap sprint reset

M197 adds **Combat → W-Tap** using only the already-certified physical LMB input, player movement snapshot and mapped sprint control bridge.

The module is edge-triggered. On a fresh physical left-button press, if the captured movement snapshot says the player is sprinting, W-Tap writes sprint `false` exactly once. Holding LMB does not retrigger. Releasing LMB rearms the next press. Disabled, unavailable-player and unavailable-movement paths clear the edge state.

Sprint ownership is explicit: **W-Tap > Auto Sprint** on the reset tick. The host skips Auto Sprint only when W-Tap actually writes. On the next held tick W-Tap yields ownership, so Auto Sprint can restore sprint from the newly captured non-sprinting snapshot. This produces a deterministic one-tick sprint reset without adding any Minecraft mapping or transformer injection.

Focused coverage proves fresh-press triggering, held-button suppression, release rearming, non-sprinting no-op and lifecycle teardown. Transformed-host coverage proves exact mapped sprint reset on the fresh LMB tick followed by Auto Sprint restoration on the next held tick.

## W-Tap ground-only mode

M198 extends **Combat → W-Tap** with persistent BOOLEAN setting `combat.wTap.requireGround`, presented as **Ground Only** and defaulting to `false`. The default preserves M197 exactly.

When enabled, a fresh physical LMB press is consumed normally but only writes sprint `false` when the captured movement snapshot reports `onGround=true`. An airborne press therefore does not suddenly fire on landing while the same button remains held; release is still required to rearm the next edge.

No new Minecraft mapping or transformer hook is introduced. Focused coverage proves default parity, airborne suppression, held-button non-retrigger after landing, release rearm, grounded reset and teardown. Transformed-host coverage proves the same behavior against exact mapped `Entity.onGround` and sprint state while Auto Sprint remains enabled.

## Configurable W-Tap cooldown

M199 extends **Combat → W-Tap** with persistent INTEGER setting `combat.wTap.cooldownTicks`, presented as **Cooldown**, default `0`, range `0..20`, and step `1`. The default preserves M198 exactly.

After a successful sprint reset, the module suppresses new W-Tap resets for the configured number of subsequent valid sprint-control ticks. Physical press edges that occur during cooldown are consumed rather than deferred: holding the same click after cooldown expires never causes a delayed reset, and release is required to rearm another edge. Disable, re-enable, missing player authority or unavailable movement state clears cooldown and edge state.

Auto Sprint ownership remains unchanged. W-Tap still outranks Auto Sprint only on ticks where it actually writes sprint `false`; cooldown-suppressed presses yield normally, allowing Auto Sprint to keep or restore sprint. No new Minecraft mapping or transformer hook is introduced.

Focused and transformed-host coverage prove default parity, a two-tick cooldown, consumed suppressed presses, rearm after release, Auto Sprint cooperation and setting teardown.

## Configurable W-Tap reset duration

M200 extends **Combat → W-Tap** with persistent INTEGER setting `combat.wTap.resetTicks`, presented as **Reset Ticks**, default `1`, range `1..5`, and step `1`. The default preserves M199's exact one-tick sprint reset.

A successful fresh-press trigger owns sprint control for exactly the configured number of valid sprint-control ticks. The trigger tick is tick one; each remaining reset tick explicitly keeps sprint false and continues to outrank Auto Sprint. After the reset window expires, a still-held LMB does not retrigger because the existing edge state remains held, and Auto Sprint can immediately restore sprint.

The existing cooldown continues counting from the trigger and may overlap the reset window. Input edges seen during an active reset window are consumed through the same held-state tracking, so no delayed reset appears when ownership expires. Disable, re-enable or unavailable movement authority clears both reset-duration and cooldown state.

No new Minecraft mapping or transformer hook is introduced. Focused and transformed-host coverage prove a three-tick reset followed by immediate Auto Sprint restoration on tick four, plus default setting registration and teardown.

## W-Tap forward-key requirement

M201 extends **Combat → W-Tap** with persistent BOOLEAN setting `combat.wTap.requireForward`, presented as **Require Forward** and defaulting to `false`. The default preserves M200 exactly.

When enabled, a fresh physical LMB press can start a sprint reset only while the certified legacy W key is physically held. A press without W is consumed by the existing edge state and does not become a delayed reset if W is pressed later during the same LMB hold; release is required to rearm a fresh press.

Require Forward gates only creation of a new reset window. Once a reset has legitimately started, its configured Reset Ticks ownership is allowed to finish even if W is released, preserving deterministic reset duration. Cooldown, Ground Only and Auto Sprint precedence remain unchanged.

The host reads W exclusively from the existing parent-owned `Minecraft189InputState` using certified `LegacyKeyboardCodes.W = 17`; no new Minecraft mapping or transformer hook is introduced. Focused and transformed-host coverage prove no-W suppression, release rearm, W-held activation, setting registration and teardown.

## Auto Sprint forward-key requirement

M202 extends **Movement → Auto Sprint** with persistent BOOLEAN setting `movement.autoSprint.requireForward`, presented as **Require Forward** and defaulting to `false`. The default preserves all pre-M202 Auto Sprint behavior.

When enabled, Auto Sprint may write sprint `true` only while the certified legacy W key is physically held. Existing eligibility remains intact: unavailable movement state, sneaking, already-sprinting players, disabled Auto Sprint, or a false Require Forward gate produce no synthetic sprint write.

The host supplies W from the same parent-owned `Minecraft189InputState` and certified `LegacyKeyboardCodes.W = 17` authority already used by M201. W-Tap precedence is unchanged: any W-Tap tick that owns sprint returns before Auto Sprint; otherwise Auto Sprint evaluates its own Require Forward gate normally.

No new Minecraft mapping or transformer hook is introduced. Focused and transformed-host coverage prove default parity, W-released suppression, W-held activation, setting registration, teardown, and coexistence with the existing W-Tap ownership chain.

## Auto Jump forward-key requirement

M203 extends **Movement → Auto Jump** with persistent BOOLEAN setting `movement.autoJump.requireForward`, presented as **Require Forward** and defaulting to `false`. The default preserves all pre-M203 Auto Jump behavior.

Airborne state continues to rearm Auto Jump regardless of the forward key. When Require Forward is enabled, an armed grounded contact does not jump while W is released and remains armed; pressing W while still grounded then consumes that armed jump exactly once. Continued grounded ticks do not repeat the jump until a later airborne state rearms the module.

The host supplies W from the existing parent-owned `Minecraft189InputState` and certified `LegacyKeyboardCodes.W = 17`. Existing Long Jump, High Jump, Low Hop and Bunny Hop ownership remains unchanged because Auto Jump is still reached only when those higher-priority movement-jump modules are inactive.

No new Minecraft mapping or transformer hook is introduced. Focused and transformed-host coverage prove default parity, airborne rearm, W-released suppression, grounded W-held activation, one-shot arming, setting registration and teardown.

## World loaded-entity mapping authority

M204 pins the exact Minecraft 1.8.9 `World.loadedEntityList` field required by future target/entity snapshots. The existing mapping provenance already points at `BigBroadBean/mappings-extracted` commit `2265da88e93c20411ec70f0b892f4fbc84ebc3c9`; its pinned 1.8.9 sources establish `adm` as `net/minecraft/world/World`, `adm.f` as SRG `field_72996_f`, and MCP name `loadedEntityList`. The erased JVM descriptor is `Ljava/util/List;`.

`Minecraft189Mappings.WORLD_LOADED_ENTITY_LIST` records that authority and the strict World class-shape verifier now rejects runtime World classes missing the exact field before transformation proceeds. Mapping and transformed-host fixtures include the same field shape.

M204 intentionally does **not** expose the raw `List<Entity>` to parent-owned runtime code. Entity instances belong to the Minecraft child loader, so retaining or casting that list across the loader boundary would violate the existing isolation model. A later milestone must build a child-side traversal plus parent-owned primitive snapshot boundary before Aim Assist, ESP, Reach or other target-aware modules may consume world entities.

## Loader-safe world entity position snapshots

M205 converts the M204 `World.loadedEntityList` authority into a loader-safe read boundary without exposing Minecraft objects. Transformed `World` now implements `Minecraft189WorldEntityPositionsAccess` and traverses `adm.f` inside the child-loaded game class. Every loaded entity inherits the already-certified transformed `Entity` position interface, so the World bridge extracts only finite primitive `x/y/z` values into a freshly allocated flattened `double[]`.

The Minecraft end-of-tick hook forwards only that primitive array through `Minecraft189RuntimeBridge.worldEntityPositions(...)`. `Minecraft189WorldEntityPositionState` immediately validates x/y/z triple shape and finite values, copies the array, and exposes immutable snapshot-style indexed coordinates. Source arrays and exported packed arrays are defensively copied. A null world clears availability; a present world with zero loaded entities is represented as an available snapshot with entity count zero.

No `World`, `Entity`, `List<Entity>`, iterator, class-loader-owned collection or child-loaded object is retained by parent runtime state. Focused tests prove defensive copying, malformed/non-finite rejection and clear semantics. The transformed-host proof populates exact `adm.f` with two transformed entities and proves both primitive XYZ triples reach the parent snapshot, then proves `theWorld = null` clears it.

M205 intentionally stops at positions. It does not yet classify player/living entities, identify the local player, expose names/health/visibility, select targets, rotate toward targets or render ESP. Those capabilities require additional exact authority and snapshot fields before target-aware modules are allowed.

## Loader-safe world entity kind flags

M206 adds a second primitive snapshot aligned to the M205 loaded-entity order. Transformed `World` implements `Minecraft189WorldEntityKindsAccess` and emits a fresh `int[]` with three certified class-identity flags: `LIVING` for `pr / EntityLivingBase`, `PLAYER` for `wn / EntityPlayer`, and `LOCAL_PLAYER` for `bew / EntityPlayerSP`. These class mappings were already part of the pinned 1.8.9 mapping authority, so M206 introduces no new obfuscated field or method claim.

Classification is performed entirely inside child-loaded World bytecode using JVM `instanceof`; only integer bit flags cross the runtime bridge. `Minecraft189WorldEntityKindState` defensively copies arrays, rejects unknown bits, rejects impossible player-without-living and local-player-without-player/living combinations, and exposes immutable indexed predicates. Null World clears the state.

The transformed-host proof uses the same exact M205 `adm.f` list: the live `bew` entry arrives as living + player + local-player, while a base `pk / Entity` entry arrives with zero kind bits. This gives later target selection a safe way to exclude the local player and restrict candidates to players/living entities without retaining child-loader objects.

M206 still does not select targets, read names/teams, test visibility, read other entities' health, aim, attack or render ESP.

## Nearest remote-player target snapshot

M207 is the first parent-owned target-selection primitive built entirely on the certified M205/M206 snapshots. `Minecraft189NearestPlayerTargetState` consumes the current local-player position, aligned world entity XYZ positions and aligned entity kind flags. It selects the nearest entity whose kind is PLAYER but not LOCAL_PLAYER, using full three-dimensional squared distance and deterministic lowest-index tie breaking.

Selection is fail-closed. If the local position or either world snapshot is unavailable, or if position/kind entity counts disagree, the target snapshot becomes unavailable. A fully valid world with no eligible remote player is represented as `available=true, found=false`, preventing callers from confusing “no target” with stale/misaligned source data.

Host runtime clears the derived target whenever the local-player position or world positions are resampled. Because the transformed tick publishes world positions before world kinds, the later kind update recomputes M207 only after both current-tick world snapshots are present. This prevents mixed-tick target observations without exposing any Minecraft object.

The transformed-host proof upgrades the second M205 fixture from base `pk / Entity` to real `wn / EntityPlayer` and proves that the local `bew` entry is excluded while entity index 1 is selected at its exact primitive coordinates and distance.

M207 still performs no rotation, attack, visibility/raycast test, name/team filtering, health filtering or ESP rendering. It is a deterministic target data primitive for later modules.

## Target rotation solution snapshot

M208 is a non-authoritative dev-stack milestone on the hosted-green M207 head while GitHub's merge/ref write path is unavailable. It adds a pure parent-owned `Minecraft189TargetRotationState` derived from the local-player position and M207 nearest-player target snapshot; it does not write player rotation.

The solver follows the already-certified Minecraft yaw convention used by Direction: yaw 0 points south (+Z), +90 points west (-X), -90 points east (+X), and yaw is normalized to [-180, 180). Pitch follows Minecraft's sign convention: negative looks upward and positive looks downward. The solution uses `atan2` and horizontal `hypot`; a vertical-only target with zero horizontal separation fails closed because yaw is undefined.

Host runtime invalidates the rotation solution whenever local position or world positions are resampled, and recomputes it only after M207 has produced a current valid target. The transformed-host fixture proves the remote player at (130.0, 65.25, -40.0) from local (123.25, 64.5, -42.75) yields yaw approximately -67.833654 and pitch approximately -5.875010.

M208 is intentionally data-only. It does not own `Entity.rotationYaw` / `rotationPitch`, smooth angles, gate on mouse buttons, enforce FOV/range, test visibility, or attack. Those remain later module milestones after M207 promotion recovers.

## Basic Aim Assist rotation ownership

M209 turns the certified M207/M208 target pipeline into the first functional target-aware combat module: **Combat → Aim Assist** (`combat.aimAssist`). The module has deliberately narrow first-slice behavior: while enabled and physical LMB is held, it owns the player rotation tick whenever a certified M208 target-rotation solution is available, writing the exact desired yaw and pitch through the already-certified `Minecraft189PlayerRotationControl`. No new Minecraft mapping or transformer hook is introduced.

Rotation ownership is explicit: **Spin > Aim Assist > Jitter**. Spin retains highest priority. When Aim Assist is eligible it owns the tick even if the player is already aligned, so Jitter cannot perturb an acquired target. If Aim Assist is disabled, LMB is released or no target solution is available, Jitter retains its existing fallback behavior.

M209 also resolves the hook-order boundary required by a real rotation consumer. Minecraft tick instrumentation samples local position before rotation, but world entity snapshots rebuild later in the tick. The M208 target-rotation solution is therefore treated as a coherent one-tick latch: a non-null local-position resample clears the derived nearest-target state but preserves the prior complete target-rotation solution long enough for the immediately following rotation hook to consume it. World-position resampling later in the tick clears that latch, and world-kind publication rebuilds the complete M207/M208 chain for the next tick. A null local player still clears it immediately. This favors one-tick latency over mixed-tick target data.

Focused coverage proves disabled/no-hold/no-target gates, exact yaw/pitch writes, tick ownership while already aligned and lifecycle teardown. The transformed-host proof enables both Aim Assist and Jitter and proves held Aim Assist writes the exact certified target angles without the configured Jitter perturbation; releasing LMB yields and leaves rotation untouched.

M209 intentionally has no smoothing, range limit, FOV limit, visibility/raycast gate, team/name filter or automatic attack. Those remain separate reviewable milestones.

## Aim Assist smoothing controls

M210 adds persistent **Yaw Speed** (`combat.aimAssist.yawSpeed`) and **Pitch Speed** (`combat.aimAssist.pitchSpeed`) controls to Aim Assist. Both are DOUBLE settings from 0.1 to 180.0 degrees per tick and default to 180.0, preserving M209's exact-target behavior unless the user opts into smoothing.

Yaw uses the shortest wrapped delta in Minecraft's [-180, 180) convention, then limits that delta to the configured yaw speed. Pitch uses an independent linear delta limited by the configured pitch speed. Aim Assist still owns an eligible held-LMB tick even when a limited step is zero/already aligned, so Jitter cannot perturb the tracked target.

The transformed-host proof configures yaw speed 10 and pitch speed 4 from a live starting rotation of (25, 15) toward the certified M208 solution and proves the tick lands at (15, 11), while configured Jitter remains suppressed. No mapping, target-selection or transformer changes are introduced.

## Aim Assist activation mode

M211 adds persistent BOOLEAN setting `combat.aimAssist.requireHold`, presented as **Require Hold** and defaulting to `true`. The default preserves M209/M210 behavior: Aim Assist owns rotation only while physical LMB is held. Setting it to `false` allows continuous tracking whenever the module is enabled and the coherent M208 target-rotation latch is available.

Activation mode does not change rotation precedence or smoothing. Spin remains higher priority, Aim Assist still owns eligible ticks over Jitter, and configured yaw/pitch speed limits apply identically in held and continuous modes. Focused and transformed-host coverage prove the default, persistence registration, no-hold activation when disabled, and teardown.

## Aim Assist maximum distance

M212 extends the coherent M208 target-rotation latch with the selected target's primitive squared distance and exposes `distanceSquared()` / `distance()` on the snapshot. Aim Assist adds persistent DOUBLE setting `combat.aimAssist.maxDistance`, presented as **Max Distance**, ranging from 0.5 to 128.0 blocks and defaulting to 128.0 to preserve prior behavior for normal loaded-player ranges.

The range gate compares the latched target distance against the configured maximum before Aim Assist claims rotation ownership. Because distance is captured alongside the same entity index, yaw and pitch during M208 recomputation, M212 never combines a previous-tick angle with a separately refreshed distance. Out-of-range Aim Assist yields normally, so lower-priority Jitter remains eligible.

Focused coverage proves latched distance integrity, setting registration/defaults, out-of-range rejection and in-range activation. The transformed-host proof uses the certified ~7.33-block remote player: max distance 5 suppresses Aim Assist, while max distance 8 allows the configured smoothed step.

## Aim Assist maximum FOV

M213 extends **Combat → Aim Assist** with persistent DOUBLE setting `combat.aimAssist.maxFov`, presented as **Max FOV**. The default is 180 degrees, preserving all M212 behavior. Valid values are 1–180 degrees.

The gate compares the current player yaw with the certified target yaw using the same shortest-path wrapping already used by Aim Assist smoothing. Aim Assist may own rotation only when the absolute wrapped yaw delta is less than or equal to Max FOV. For example, Max FOV 45 accepts a target within ±45 degrees of the current horizontal view and rejects a target farther around the yaw circle.

Distance, Require Hold, smoothing and ownership precedence remain unchanged: Spin > Aim Assist > Jitter. M213 adds no Minecraft mapping, transformer hook or target-enumeration behavior. Focused and transformed-host tests prove default parity, out-of-FOV suppression and in-FOV activation.

## Aim Assist axis controls

M214 adds persistent BOOLEAN settings `combat.aimAssist.yawEnabled` and `combat.aimAssist.pitchEnabled`, presented as **Yaw Enabled** and **Pitch Enabled**. Both default to `true`, preserving M213 behavior.

When only Yaw Enabled is active, Aim Assist may adjust horizontal rotation while leaving pitch untouched. When only Pitch Enabled is active, it may adjust vertical rotation while leaving yaw untouched. If both axes are disabled, Aim Assist yields the tick instead of claiming rotation ownership with no possible write, allowing lower-priority Jitter to remain eligible.

Require Hold, Max Distance, Max FOV, yaw/pitch smoothing and Spin > Aim Assist > Jitter precedence are otherwise unchanged. M214 adds no Minecraft mappings or transformer hooks. Focused and transformed-host tests cover default parity, yaw-only, pitch-only and both-disabled behavior.

## Aim Assist dead zone

M215 introduces persistent DOUBLE setting `combat.aimAssist.deadZone`, shown as **Dead Zone** (0–30 degrees, default 0). With a positive dead zone, Aim Assist leaves each enabled axis untouched while its absolute angular error is within the threshold; yaw uses the certified shortest-path ±180-degree wrap, and pitch uses the linear difference. When both eligible axes are within the dead zone, Aim Assist yields rotation ownership to the existing lower-priority policy rather than claiming the tick. An eligible axis beyond the threshold continues to use its existing smoothing speed.

At the default 0, the M214 write and ownership behavior is preserved exactly. Max FOV, max distance, Require Hold, axis toggles and Spin > Aim Assist > Jitter precedence are unchanged. No new game mappings or transformer hooks. Focused and transformed-host tests exercise zero-default parity, single-axis suppression, both-axis yielding, and exact mapped rotation fields.

## Aim Assist vertical FOV

M216 adds persistent DOUBLE setting `combat.aimAssist.maxPitchFov`, presented as **Max Pitch FOV** (1–180 degrees, default 180). Aim Assist now optionally rejects the target when the absolute difference between the current player pitch and certified target pitch exceeds this limit. This is an independent vertical eligibility gate; existing **Max FOV** remains the shortest-path yaw gate. Defaults preserve all M215 behavior.

Only the already certified rotation snapshots are used. Max Distance, Require Hold, yaw/pitch enable switches, yaw/pitch smoothing, dead zone and Spin > Aim Assist > Jitter ownership are unchanged. Full settings registration/close symmetry, focused rejection/acceptance tests and exact transformed-host field assertions are included; no new Minecraft mappings or transformers.

## Aim Assist pitch offset

M217 adds persistent DOUBLE setting `combat.aimAssist.pitchOffset`, presented as **Pitch Offset** in Combat → Aim Assist (−30° to +30°, default 0°). Positive offsets move desired pitch downward in Minecraft's convention and negative offsets move it upward. Effective pitch is clamped to the legal [−90°, +90°] interval.

The adjusted pitch drives Max Pitch FOV eligibility, the pitch dead zone and existing pitch smoothing consistently. Max Distance, yaw FOV, activation, axis controls and Spin > Aim Assist > Jitter precedence remain unchanged. The zero-degree default preserves M216 behavior. Focused and transformed-host tests verify positive/negative offsets, vertical FOV suppression, dead-zone behavior, zero-default parity and setting lifecycle. No new Minecraft mappings or transformer hooks.

## Aim Assist yaw offset

M218 adds persistent DOUBLE setting `combat.aimAssist.yawOffset`, shown as **Yaw Offset** (−30° to +30°, default 0°). It adjusts the desired horizontal aim angle using certified yaw and shortest-path wrapping into [−180°, +180°). The effective angle consistently drives horizontal Max FOV, yaw dead-zone and yaw smoothing. Max Pitch FOV, pitch offset, distance, activation and Spin > Aim Assist > Jitter precedence are unchanged.

The zero offset preserves M217 behavior. Focused and transformed-host tests validate positive and negative offsets, wrapped angle handling, FOV rejection, dead-zone yielding, zero-default parity and complete registration/teardown lifecycle. No new mapping, new transformer or target selection rule.

## Aim Assist minimum distance

M219 introduces persistent DOUBLE `combat.aimAssist.minDistance` (**Min Distance**, 0–128 blocks, default 0). The existing nearest-remote-player target snapshot is eligible for Aim Assist only when its certified squared distance is inclusively within Min Distance and Max Distance. Inverted user configuration (Min Distance > Max Distance) fails closed without rotation writes. At the zero default, behavior is unchanged from M218.

The minimum distance is a filter on the **already selected nearest target**; it does not cause a different candidate to be selected. Max Distance, Max FOV, Max Pitch FOV, offsets, smoothing, dead zone, activation, axis controls and Spin > Aim Assist > Jitter ownership remain unchanged. Focused tests validate exact inclusive boundary, near-target suppression, inverted interval, and existing Max Distance interplay; transformed-host tests validate actual mapped yaw/pitch non-writes and reactivation. No new mappings or transformer hooks.

## Aim Assist Require Forward

M220 adds persistent BOOLEAN `combat.aimAssist.requireForward`, shown as **Require Forward** (default OFF). When enabled, Combat → Aim Assist is eligible only while the physical mapped W key is held (`Minecraft189InputState` / certified `LegacyKeyboardCodes.W = 17`). Releasing W immediately yields Aim Assist rotation ownership to the existing lower-priority Jitter policy; it does not defer a skipped correction or queue synthetic input. The existing Require Hold condition still applies independently. The zero-change default preserves M219 behavior.

The host forwards the already-certified physical W input state to Aim Assist only after Spin's higher-priority ownership check. An old four-argument package-level entrypoint is retained for existing focused callers but supplies no presumed W press (false), so Require Forward fails closed without a validated input. No new mappings or transformer hooks. Focused and exact transformed-host tests prove W-down writes, W-up suppression, release/re-press, hold precedence, disable gating and setting registration/teardown.

## Aim Assist Require Ground

M221 adds persistent BOOLEAN `combat.aimAssist.requireGround` (**Require Ground**, default OFF). When enabled, Aim Assist can own rotation only if the certified `Minecraft189PlayerMovementState.Snapshot` is available and `onGround()` is true. An absent, cleared or airborne state fails closed; with the setting off, M220 behavior remains unchanged.

The host forwards its existing movement snapshot only after Spin's higher-priority rotation ownership check. The prior Aim Assist apply overloads deliberately do not assume ground authority, and suppression leaves lower-priority Jitter eligible. Focused and transformed-host tests cover default parity, absent/cleared state, airborne/ground transitions, exact transformed `pk.C` on-ground field and full setting lifecycle. No new Minecraft mappings, bytecode hooks, or target-selection rules.

## Aim Assist axis-aware FOV gating

M222 corrects the M214 axis toggles' interaction with M213 horizontal FOV and M216 vertical FOV. When `yawEnabled=false`, Max FOV does not veto independent pitch correction; when `pitchEnabled=false`, Max Pitch FOV does not veto independent yaw correction. Each enabled axis retains its FOV hard gate; when both axes are enabled, both vetoes still apply exactly as in M221. With both disabled Aim Assist yields rotation ownership; no target or rotation writes. No settings, mapping, transformer or ownership changes. Focused and transformed-host tests cover both independent axes, their FOV vetoes, the all-off case, and restoration of defaults.

## Aim Assist Pause While Sneaking

M223 adds persistent default-OFF BOOLEAN `combat.aimAssist.pauseWhileSneaking` (**Pause While Sneaking**). When enabled, an available exact local player movement snapshot with `sneaking()` true yields Aim Assist rotation ownership without synthetic yaw or pitch writes. Missing/cleared movement snapshots fail closed while enabled. Releasing sneak allows the normal range/FOV/activation/smoothing gates to resume. When off, the pre-M223 behavior is unchanged (no movement snapshot required unless Require Ground is separately active). This setting and Require Ground are independent, with existing Spin > Aim Assist > Jitter precedence. The host's M221 same-tick movement refresh supplies the certified sneaking state; no new mapping or transformer hook. Focused and transformed-host tests cover transitions, default parity, missing authority, and lifecycle registration/close.

## Aim Assist Require Sprint

M224 adds default-OFF persistent BOOLEAN `combat.aimAssist.requireSprint` (**Require Sprint**). With the option enabled, Aim Assist can own rotation only while a certified, available local player movement snapshot reports `sprinting()` true; unavailable/cleared state or not sprinting fails closed. When disabled, M223 behavior is retained. It composes independently with Require Ground, Require Forward, Require Hold, Pause While Sneaking, per-axis FOV, smoothing and dead zone. The mapped host already refreshes the exact player onGround/sneak/sprint snapshot before rotation, so no new mapping, transformer or hook is added. Focused and transformed-host tests cover no-authority, false/true sprint transitions, combined ground requirement, legacy default compatibility, and full setting lifecycle.

## Aim Assist range-qualified nearest-player fallback

M225 builds on M219: with a nonzero Min Distance, Aim Assist now selects the nearest nonlocal mapped player inside the inclusive configured **Min Distance–Max Distance** range, rather than rejecting the globally nearest player if that player is too close. A general-purpose `Minecraft189NearestPlayerTargetState.update` overload performs finite, nonnegative, inclusive range filtering with stable entity-index ties and fails closed for inverted/invalid ranges. The existing 3-argument update retains its exact nearest-player semantics for other consumers and GUI snapshots. The host uses separate reusable range-qualified target/rotation state and only recomputes it when Aim Assist is active and Min Distance is positive. The Min Distance zero default retains the M224 fast path. FOV/axis checks, offsets, Require Sprint and other activation gates, and Spin > Aim Assist > Jitter precedence remain unchanged. Tests cover ordinary nearest behavior, too-close candidate exclusion, inclusive boundaries, no candidate, invalid input, and an exact transformed two-remote-player host scenario. The older M219 note that no fallback occurs is superseded by this milestone.

## Aim Assist FOV-qualified nearest-player fallback

M226 extends the M225 range-qualified candidate selection to the configured horizontal and vertical FOV limits. When either enabled axis has an actual restricted FOV (less than 180°), Aim Assist scans the certified remote-player positions in ascending distance and chooses the nearest candidate satisfying its range and **both enabled FOV checks**. It computes candidate angles via the already-certified `Minecraft189TargetRotationState.update` and reuses `Minecraft189AimAssistModule.targetWithinFov` as the single authority for yaw/pitch offsets, yaw wrap, clamped pitch, axis toggles and inclusive FOV boundaries. The unfiltered general nearest-player target snapshot is unchanged. Default unrestricted FOV, plus zero Min Distance, preserves the original M224 fast path. If no candidate is in range/view, Aim Assist yields to the existing Jitter ownership policy. No new mappings, hooks or GUI settings. Unit tests prove the candidate predicate fallback and no-match behavior; transformed-host tests prove horizontal fallback, yaw-offset-sensitive selection, wide-FOV default, vertical fallback, and original fixture restoration.

## Aim Assist Crosshair Priority

M227 adds default-OFF persistent BOOLEAN `combat.aimAssist.prioritizeCrosshair`, displayed as **Crosshair Priority**. Normally Aim Assist retains the M226 nearest-by-distance selection. When enabled, it ranks the mapped remote-player candidates by the **sum of squared angular yaw and pitch errors** from the current rotation. Yaw is wrap-aware, both offsets are included, and disabled axes contribute zero error. Existing inclusive Min–Max Distance and per-enabled-axis horizontal/vertical FOV requirements remain hard filters before ranking. Equal angular scores fall back to nearest distance, then lower entity index. Missing geometry and invalid scores fail closed. The candidate selector overload is source-compatible, and the unfiltered general nearest-player snapshot is unchanged. Selection reuses certified `Minecraft189TargetRotationState` and adds no mapping or bytecode hook. Unit and transformed-host regressions cover default distance ranking, crosshair ranking, offset-dependent priority, axis toggles, range restriction, finite-score rejection, stable ties, and cleanup/rollback.

## Auto Clicker Require Forward

M228 adds persistent default-OFF BOOLEAN `combat.autoClicker.requireForward` (**Require Forward**). When enabled, Auto Clicker emits its normal configured CPS clicks only if the certified input state reports the physical legacy W key held, independently of the existing physical left-button Require Hold option. Missing/released W resets the scheduler's phase credit and sampled target CPS, so restoring W requires a fresh full click interval rather than firing a queued click. The one-argument `shouldClick` overload is retained and correctly fails closed when Require Forward is enabled; default behavior unchanged. Host forwarding uses already-certified `LegacyKeyboardCodes.W`, no new mappings or bytecode hooks. Registrations, descriptions and reverse cleanup cover setting/presentation/binding. Focused and transformed-host regressions test default parity, W release/re-press cadence, independent left-button gating, live clickMouseCalls and full close cleanup.

## Auto Clicker Pause While Right Clicking

M229 adds persistent default-OFF BOOLEAN `combat.autoClicker.pauseWhileRightClicking` (**Pause While Right Clicking**) to Combat → Auto Clicker. When enabled, holding the exact mapped physical right mouse button suppresses synthetic left mouse clicks and resets the CPS scheduler's phase credit and sampled rate, preventing a queued click when right hold is released. When disabled, the existing M228 physical left-button Require Hold, physical W Require Forward and 20-tick Min/Max CPS behavior are unchanged. The right-button pause composes independently with those gates; no mappings, bytecode hooks or injection points change. Old one- and two-argument shouldClick overloads remain source-compatible; the exact host passes all three physical inputs. Focused lifecycle, defaults, cadence/resumption/combined-gate tests and transformed-host clickMouseCalls regressions cover suppression, W composition, enabling/disabling and cleanup.

## Auto Clicker live CPS retune cadence

M230 fixes phase carry-over when a user edits the existing Min CPS or Max CPS setting while Auto Clicker is active. Every eligible scheduler tick compares both bounds against the bounds that produced the current sampled target CPS. Changing either bound resets phase credit and the sampled target before accumulating at the new rate. Normal constant-rate timing (including 20 CPS one click per tick), random rate sampling, and existing hold/W/right-button gates remain unchanged. A suppressed tick or module enable/disable also clears observed bounds with the existing phase reset. No setting IDs, GUI descriptors, bytecode hooks, mappings or input semantics change. Focused and exact transformed-host tests prove 10→4→20→10 CPS transitions, single-bound changes during pending phase, pause/disable behavior and fresh-rate clickMouseCalls.

## M231 clean-room Target HUD

M231 adds a standalone Visuals > Target HUD module, inspired at a layout level by target panels in OpenMyau+, Project-Coolware, and Yuri. Donors are references only; no source is imported. The default-disabled module owns a HUD-stage render pass with persistent X/Y coordinates and shows the certified nearest remote-player entity index, range, yaw and pitch from the existing mapped snapshots. Absence or clearing of either snapshot renders nothing. An entity index is **not** a player name. No unverified health, armor, skins or combat history are displayed. The implementation uses existing LegacyUiHostCallbacks; no new mappings, transformers, network hooks or OpenGL code. Position, panel geometry, lifecycle and missing-snapshot behavior are host-tested. Future iterations can add verified identity/health mapping and style/dragging support.

## M232 Target HUD compact presentation

M232 introduces persistent default-OFF BOOLEAN "render.targetHud.compact" (Compact) as a light clean-room visual alternative to the expanded donor-inspired card. The compact card is 142x38 logical units, preserving the selected certified remote-player index and distance with a dark rounded background and accent edge; the expanded default remains 164x56 and includes certified yaw/pitch. Both layouts respect existing X/Y placement and suppress drawing on missing target authority. Switching layouts never changes combat targeting and requires no game mappings/hooks. Tests validate the live 3-line / 2-line / 3-line transition, card geometry, serialization, render lifetime and registry cleanup.

## M233 Auto Clicker nearby-player gate

M233 adds default-OFF persistent Boolean setting combat.autoClicker.requireNearbyPlayer and a separate validated range setting combat.autoClicker.maxPlayerDistance, default 4.0 blocks (0.5–16.0). Enabled gating requires an available mapped nearest nonlocal player at or inside the chosen range; missing/cleared/out-of-range data suppresses clicks and resets CPS scheduling phase. The host passes its existing read-only target snapshot: no new client mapping or world scan. This is **proximity only**, not a raycast, visibility/line-of-sight test, hitbox check, or proof the crosshair points at a player. The design takes only the target/range option concept from OpenMyau+ AutoClicker as a reference, rather than copying donor code. Existing physical left-hold, W hold, right-click pause and CPS settings compose without changes. Focused host tests prove default compatibility, missing authority, inclusive range, changing range, phase reset, persistence and teardown.

## M234 — Nearby Players HUD (read-only radar)

A separate default-disabled **Visuals → Nearby Players** module renders a small HUD panel showing the number of *mapped, loaded remote player entities* inside a configurable inclusive 1–128m 3D radius, plus the closest measured distance. Default radius 32m; X/Y placement and radius persist with normal setting registration. A local player or non-player entity never counts, and absent/mismatched position+kind authority hides the panel entirely rather than fabricating a number. Zero valid nearby remote players displays 0 and a dash for nearest range. This is **not** a server-wide player count, player identity, ESP or guarantee that a loaded player is visible. The new module reuses snapshots already owned by Minecraft189HostRuntime, owns its render pass via ModuleController, and adds no new game mappings, network hooks or donor source code. Its compact visual style borrows generic concepts (dark rounded panel, left accent, two-line information hierarchy) from the donor HUD review; test fixtures cover inclusive boundaries, mismatch fail-closed, draw output, live radius changes, and rollback/teardown.
