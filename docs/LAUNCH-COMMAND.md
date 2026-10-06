# Launch command construction

M6 converts a verified launch template plus launch-session values into an argument list suitable for Java `ProcessBuilder`.

It still does not execute the process.

## Shell-free construction

The launcher builds a list of arguments instead of constructing a shell command string. Paths containing spaces therefore remain a single process argument and do not require shell quoting.

The command currently contains:

1. selected Java executable;
2. minimum and maximum heap settings;
3. staged native directory;
4. resolved classpath;
5. metadata main class;
6. expanded legacy game arguments.

## Legacy argument expansion

The Minecraft 1.8.9 `minecraftArguments` string is tokenized before placeholders are expanded.

This matters because a replacement value may itself contain spaces. A JSON user-properties value, player path or game directory remains one argument even after expansion.

Supported placeholders:

- `${auth_player_name}`
- `${version_name}`
- `${game_directory}`
- `${assets_root}`
- `${assets_index_name}`
- `${auth_uuid}`
- `${auth_access_token}`
- `${user_properties}`
- `${user_type}`

Any unresolved placeholder aborts command construction.

## Secret handling

Arguments derived from the access-token placeholder are marked sensitive. `LaunchCommand.redactedArguments()` replaces those values with `<redacted>`.

Diagnostics should use the redacted view. The unredacted list exists only for eventual direct handoff to `ProcessBuilder`.

## Next gate

A later milestone will combine:

- installation inspection;
- metadata resolution;
- artifact integrity verification;
- native staging;
- Java runtime validation;
- command construction;

into one preflight transaction before process execution is enabled.


## CustomMC runtime overlay

M63-M65 allow the launcher to hand process entry to the Java-8 CustomMC bootstrap while preserving the original Minecraft argument vector.

`CustomMcBootstrapOverlay.create(...)` uses the M64 delegate-only mode:

1. prepend the bootstrap artifact to classpath;
2. set process main class to `CustomMcBootstrapMain`;
3. prefix the original resolved Minecraft main class;
4. append the unchanged expanded game arguments.

`createWithRuntime(...)` uses the M65 initialized mode:

1. prepend bootstrap, then ordered runtime artifacts;
2. set process main class to `CustomMcBootstrapMain`;
3. prefix `--custommc-runtime`, the exact initializer class, and original Minecraft main class;
4. append the unchanged expanded game arguments.

Sensitive argument indexes remain owned by the M63 command builder and are offset after these bootstrap-prefix arguments.


## Canonical assembled runtime directory

M67 adds `Minecraft189RuntimeBundle.fromDirectory(...)` for the output of the root `assembleRuntimeOverlay` task. The directory contains exactly:

- `bootstrap.jar`;
- `core.jar`;
- `platform-api.jar`;
- `platform-1.8.9.jar`.

Calling `bundle.overlay(resolvedMainClass)` produces the M65 initialized bootstrap overlay using the M66 concrete initializer. Existing M63 preflight remains responsible for verifying that every resulting classpath entry actually exists before native staging and process launch.


## Template-aware overlay resolution

M68 allows `LaunchPreflightRequest` to carry a `LaunchRuntimeOverlayResolver` instead of requiring a fully materialized overlay before Minecraft version metadata has been resolved. Fixed-overlay constructors remain source-compatible.

For the canonical M67 bundle, preflight order is now:

1. inspect installation;
2. resolve the authoritative `MinecraftLaunchTemplate`;
3. verify Minecraft artifacts;
4. resolve the runtime bundle overlay from `template.mainClass()`;
5. verify every CustomMC overlay classpath entry exists;
6. stage natives;
7. build the final JVM command;
8. hand the result to the M62 process owner.

This makes the M64-M67 bootstrap path consumable by the real preflight pipeline without duplicate main-class resolution.

## Canonical start path

M69 exposes `Minecraft189LaunchRequest` as the canonical fully-specified 1.8.9 launch input and `Minecraft189Launcher.start(...)` as the composition point that connects template-aware preflight to M62 process start. The M67 runtime bundle remains a resolver rather than a precomputed overlay, so the authoritative Minecraft main class is still obtained exactly once from resolved metadata before the bootstrap command is produced.

## M70 launcher command-line surface

`LauncherMain` keeps its no-argument foundation probe for automated smoke checks. Passing `launch` selects the canonical M69 execution path.

Required launch options are `--minecraft-home`, `--java-home`, `--runtime-overlay`, `--player-name` and `--uuid`. Optional values are `--game-dir`, `--native-staging`, `--min-mb`, `--max-mb`, `--user-type`, `--user-properties` and `--access-token-env`.

The access token itself is intentionally not accepted on the command line. M70 reads it from the named environment variable, defaulting to `CUSTOMMC_ACCESS_TOKEN`; an absent variable produces an empty token for local/offline-compatible launch scenarios. Parsed inputs are converted to the canonical `Minecraft189LaunchRequest`, the selected Java home is verified through `JavaRuntimeProbe`, and execution then delegates entirely to M69.
