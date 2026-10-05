# Runtime discovery

The launcher does not ship or commit Minecraft game binaries.

M3 introduces read-only discovery of an existing user installation.

## Home resolution

Resolution order is:

1. explicit launcher override;
2. platform default.

Platform defaults:

- Windows: `%APPDATA%\.minecraft`, with a deterministic user-home fallback;
- macOS: `~/Library/Application Support/minecraft`;
- Linux: `~/.minecraft`.

The resolver is fully unit-testable because operating system, environment and user home are inputs rather than hidden global assumptions.

## 1.8.9 installation contract

For the first platform target, the launcher expects an installed `1.8.9` version with:

- `versions/1.8.9/1.8.9.json`;
- `versions/1.8.9/1.8.9.jar`;
- `libraries/`;
- `assets/`.

M3 only reports whether those resources exist. It does not copy, patch, download or execute them.

A later milestone will parse the installed version metadata, resolve the exact classpath/natives/assets from that metadata, and verify hashes where authoritative hashes are available.

## CI policy

CI uses only synthetic directories and zero-byte/test files. No Mojang binary is required for hosted tests.
