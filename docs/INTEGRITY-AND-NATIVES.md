# Integrity and native staging

M5 adds a gate between metadata resolution and execution.

## Artifact verification

Every classpath and native artifact is checked for presence.

When the installed version metadata declares a size, the actual file size must match.

When it declares a SHA-1, the launcher hashes the bytes and requires an exact match.

Results are explicit:

- `VERIFIED`
- `PRESENT_UNVERIFIED`
- `MISSING`
- `SIZE_MISMATCH`
- `SHA1_MISMATCH`

A launch integrity report fails if any artifact is missing or mismatched. It separately reports whether every artifact was cryptographically verified.

## Native staging

Native archives are never extracted in place.

Each launch receives a fresh staging directory.

Before extraction, the archive itself must pass the artifact gate. Extraction then:

- honors metadata exclusion prefixes such as `META-INF/`;
- rejects absolute paths;
- rejects backslash-based archive paths;
- rejects normalized paths escaping the staging directory;
- rejects duplicate native outputs across archives;
- removes the entire fresh staging directory if extraction fails.

M5 still does not execute Minecraft.
