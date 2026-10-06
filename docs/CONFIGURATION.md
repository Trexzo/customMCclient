# Configuration

Configuration is intentionally separated into two layers.

## Core setting model

`Setting<T>` owns:

- stable setting id;
- default value;
- validation;
- optional persistence codec;
- current in-memory value.

`SettingRegistry` owns identity and persistence snapshots. M32 also exposes an immutable registration-order setting snapshot for read-only consumers such as ClickGUI; it does not copy setting values or create another setting authority.

Applying a profile is transactional with respect to validation: every supplied value is decoded and validated before the first setting is changed. Unknown keys have an explicit `REJECT` or `IGNORE` policy.

The core has no filesystem dependency.

## Presentation metadata

`SettingDescriptor` and `SettingPresentationRegistry` are deliberately separate from `Setting<T>`.

A descriptor contains UI-only metadata: setting id, label, explicit value kind and priority. M34 optionally adds `SettingNumericSpec` presentation bounds and step size for numeric editors; integer descriptors reject fractional numeric specs. Descriptor registration has an explicit lifetime and duplicate descriptors for the same setting id are rejected.

`SettingListPageContent` reads live setting values through `SettingRegistry` and uses descriptors only to decide which settings are exposed and how they are labelled. Settings without a descriptor remain hidden from that UI surface.

Presentation metadata does not replace setting validation or persistence authority. Interactive editors call the existing typed `Setting<T>.set()` path; boolean toggles plus bounded integer/double stepping therefore remain subject to the setting validator. Text editing keeps only a transient focus-owned draft and calls `Setting<String>.set()` on Enter; validation failure preserves the live value, while Escape/focus loss discards the draft. UI code does not write profiles directly.


## Launcher profile state

M53 adds `LauncherProfileState` as the orchestration layer that combines persistent settings with M51-owned module keybind assignments without changing either authority.

Setting ids remain the raw profile keys for backward compatibility. Keybinds use the reserved `@keybind/<moduleId>` namespace. Key chords are encoded deterministically as a versioned value containing the backend-neutral key id plus Shift/Control/Alt bits; `AtomicProfileStore` continues to treat all values as opaque strings.

Profile snapshot reads setting values from `SettingRegistry.snapshotEncoded()` and only keybinds currently owned by `ModuleKeybindAssignments`. Persistent setting ids using the reserved `@keybind/` namespace are rejected so profile interpretation cannot become ambiguous.

Profile apply decodes all keybind values before mutating either domain. Settings still use `SettingRegistry.applyEncoded(...)` and its explicit unknown-setting policy. Keybinds then use M53's transactional `ModuleKeybindAssignments.replaceOwnedBindings(...)`, which supports swaps between owned chords, rejects external ownership/conflicts, and restores the previous owned assignment set if registration fails. If keybind apply fails after settings changed, `LauncherProfileState` restores the previous persistent setting snapshot before surfacing the failure.

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

No setting setter or keybind editor writes to disk. UI/modules update in-memory authority; `LauncherProfileState` decides what belongs in a combined profile snapshot, and `AtomicProfileStore` decides when/how that map is persisted.

## Future migration

A later milestone may add explicit profile schema migration. The format version is already separated so migrations can be deterministic instead of inferred from missing fields.
