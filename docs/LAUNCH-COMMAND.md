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
