# Launch preflight transaction

M7 composes the previously independent launcher gates into one non-executing preflight.

The sequence is:

1. inspect the selected Minecraft installation;
2. resolve installed version metadata;
3. verify classpath and native artifacts;
4. stage natives into a fresh directory;
5. construct the shell-free, redacted JVM command.

If any step after native staging fails, the staging directory is removed before the failure is returned.

A successful `LaunchPreflightResult` owns the staging directory and implements `AutoCloseable`. Closing the result removes those staged natives.

## Java runtime inspection

Java selection is separate from Minecraft discovery.

Candidate precedence is:

1. explicit Java-home override;
2. `JAVA_HOME`;
3. the launcher JVM's current `java.home`.

The Java runtime probe does not execute the selected binary. It reads the runtime's `release` metadata and verifies that the platform-specific `bin/java` or `bin/java.exe` file exists.

Legacy `1.8.x` version strings are normalized to major version 8. Modern version strings such as `21.0.12` resolve to major version 21.

M7 does not yet impose a single required Java major. That compatibility decision belongs to the 1.8.9 platform layer rather than hidden launcher discovery.

## Execution remains disabled

Preflight returns the final argument vector, but no `ProcessBuilder.start()` path exists yet.
