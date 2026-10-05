# Configuration

Configuration is intentionally separated into two layers.

## Core setting model

`Setting<T>` owns:

- stable setting id;
- default value;
- validation;
- optional persistence codec;
- current in-memory value.

`SettingRegistry` owns identity and persistence snapshots.

Applying a profile is transactional with respect to validation: every supplied value is decoded and validated before the first setting is changed. Unknown keys have an explicit `REJECT` or `IGNORE` policy.

The core has no filesystem dependency.

## Launcher profile storage

`AtomicProfileStore` owns durable storage.

The file format is versioned and canonical:

- fixed `custommc-profile-v1` header;
- entries sorted by raw setting id before writing;
- keys and values encoded as URL-safe Base64;
- UTF-8;
- duplicate keys rejected;
- malformed encoding rejected.

Writes are performed through a temporary file. The temporary file is forced to disk before replacement. The store requests an atomic move and falls back to a normal replacement only when the filesystem reports that atomic moves are unsupported.

No setting setter writes to disk. UI/modules update in-memory state; profile orchestration decides when to persist.

## Future migration

A later milestone may add explicit profile schema migration. The format version is already separated so migrations can be deterministic instead of inferred from missing fields.
