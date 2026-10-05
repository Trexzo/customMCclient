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
