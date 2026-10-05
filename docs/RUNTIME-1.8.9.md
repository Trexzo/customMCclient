# Minecraft 1.8.9 runtime boundary

M3 introduces the first version-specific module without committing Minecraft source or binaries.

## Rules

- `platform-1.8.9` describes the version contract and launch planning.
- `launcher` owns filesystem verification and future acquisition.
- runtime artifacts are identified by stable logical ids plus expected digest/size;
- the repository contains metadata only, never Mojang binaries;
- the launcher must resolve artifacts from a legitimate installation or an allowed upstream source before launch;
- the core does not import Minecraft classes.

The initial launch planner is intentionally incomplete. It proves that version-specific arguments/classpath ownership can live behind a narrow boundary before native libraries, assets, logging config, authentication/session details and full dependency resolution are added.

No donor implementation is copied here; the design independently adopts the direct-launch responsibility split identified during the donor audit.
