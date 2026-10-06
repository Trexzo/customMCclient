# customMCclient

A ground-up Minecraft client and launcher project.

## Status

Early clean-room foundation work. The project is intentionally being built from first principles rather than by transplanting an existing client codebase.

## Design goals

- launcher-owned runtime and reproducible startup;
- explicit module, service, event and settings lifecycles;
- lightweight rendering and measurable performance work;
- deterministic configuration and packaging;
- version-specific Minecraft integration behind narrow platform boundaries;
- no committed Mojang/Minecraft binaries or source;
- donor projects may inform behavior and architecture, but recovered, proprietary, cracked or incompatible-license implementation is not copied.

See the foundation PRs for the evolving architecture and verification gates.

## Launcher CLI

Running the launcher with no arguments preserves the foundation smoke/probe behavior used by CI.

M70 adds an explicit Minecraft 1.8.9 launch command:

`LauncherMain launch --minecraft-home <path> --java-home <path> --runtime-overlay <path> --player-name <name> --uuid <uuid>`

Required values are explicit. The access token is never accepted as a command-line option; by default it is read from the `CUSTOMMC_ACCESS_TOKEN` environment variable. Optional launch arguments can override game directory, native staging directory, heap bounds, user type/properties, or the name of the environment variable containing the token.

## Runnable distribution

M71 adds the root `assembleLauncherDistribution` task. It composes the launcher application distribution with the canonical Minecraft 1.8.9 runtime overlay into one deterministic tree at `build/custommc-distribution`:

- `bin/launcher` and `bin/launcher.bat`;
- launcher runtime jars under `lib/`;
- exactly five stable runtime jars under `runtime-overlay/` (`bootstrap.jar`, `core.jar`, `platform-api.jar`, `asm.jar`, `platform-1.8.9.jar`);
- the project README.

`verifyLauncherDistribution` validates that shape and is part of `verifyFoundation`. A packaged launch therefore points `--runtime-overlay` at the sibling `runtime-overlay` directory inside this distribution.
