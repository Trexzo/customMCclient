# Architecture

## Foundation rules

The project is launcher-owned and version-aware. Minecraft integration is deliberately kept outside the core.

### Modules

- `launcher` — process/runtime ownership, installation discovery, update and launch orchestration.
- `core` — version-independent events, modules, settings, lifecycle and future services.
- `platform-api` — narrow contract between the core and a game-version adapter.
- future `platform-1.8.9` — Minecraft 1.8.9 integration. It must not leak Minecraft classes into `core`.

## Bytecode boundary

`launcher` is built with Java 21.

`core` and `platform-api` compile with `--release 8`. This preserves the option to run the in-process client side on a legacy-compatible JVM even while the launcher uses a modern JVM.

## Explicit ownership

The permanent architecture must avoid:

- reflection-based module discovery;
- generated event-bus source;
- global mutable singleton state without lifecycle ownership;
- config writes hidden inside setting/module setters;
- a browser engine as a mandatory ClickGUI dependency;
- JNI/JVMTI injection when the launcher already owns process creation.

## Rendering direction

Rendering is not implemented in M0. The planned design is a staged renderer with cached frame data and explicit render passes. UI and effects must be measurable independently.

## Minecraft acquisition

The repository will not contain Mojang/Minecraft source or game binaries. A later launcher milestone will resolve legally installed user-owned game assets/libraries and verify them before launch.
