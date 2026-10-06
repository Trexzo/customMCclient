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
