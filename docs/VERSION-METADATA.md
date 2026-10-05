# Version metadata resolution

M4 reads the already-installed Minecraft version JSON and converts it into an immutable launch template.

For the initial Minecraft 1.8.9 target the resolver supports the legacy metadata shape:

- `mainClass`;
- `minecraftArguments`;
- `assets` or `assetIndex.id`;
- ordered library rules;
- OS name/version/architecture rules;
- legacy native classifiers including `${arch}`;
- library artifact paths;
- Maven-coordinate fallback paths;
- client/library/native SHA-1 and size metadata when present;
- native extraction exclusions.

## Safety boundary

Paths supplied by version metadata are normalized under the installation's `libraries/` root. A metadata path that escapes that root is rejected.

M4 does not download, extract or execute anything.

## Rule semantics

Libraries with no rules are allowed.

Libraries with rules start disallowed. Matching rules are applied in order, with later matching rules overriding earlier ones. OS name, OS version regex and CPU architecture constraints are evaluated.

No optional launcher features are enabled yet. A rule requiring a feature to be true therefore does not match; a false feature requirement can match.

## Integrity

The launch template carries declared SHA-1 and size values where present. A later integrity milestone will hash the installed bytes before any artifact is placed on the executable classpath or extracted as a native.

## Dependency note

JSON parsing is launcher-only and uses Gson. The core and platform API remain free of this dependency.
