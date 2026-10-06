package dev.trexzo.custommc.launcher.launch;

import dev.trexzo.custommc.launcher.command.LaunchIdentity;
import dev.trexzo.custommc.launcher.command.Minecraft189RuntimeBundle;
import dev.trexzo.custommc.launcher.java.JavaRuntimeInspection;
import dev.trexzo.custommc.launcher.java.JavaRuntimeProbe;
import dev.trexzo.custommc.launcher.process.LaunchProcessSession;
import dev.trexzo.custommc.launcher.runtime.MinecraftInstallation;
import dev.trexzo.custommc.launcher.runtime.RuntimeTarget;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Minecraft189LaunchCli {
    public static final String DEFAULT_ACCESS_TOKEN_ENV =
            "CUSTOMMC_ACCESS_TOKEN";
    private static final int DEFAULT_MINIMUM_MEMORY_MB = 512;
    private static final int DEFAULT_MAXIMUM_MEMORY_MB = 2048;

    private Minecraft189LaunchCli() {
    }

    public static Minecraft189LaunchRequest prepare(
            final String[] arguments,
            final Map<String, String> environment,
            final RuntimeTarget runtimeTarget)
            throws IOException {
        Objects.requireNonNull(arguments, "arguments");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(runtimeTarget, "runtimeTarget");

        final Map<String, String> options =
                parse(arguments);

        final Path minecraftHome =
                path(required(
                        options,
                        "--minecraft-home"));
        final Path javaHome =
                path(required(
                        options,
                        "--java-home"));
        final Path runtimeOverlay =
                path(required(
                        options,
                        "--runtime-overlay"));

        final String playerName =
                required(
                        options,
                        "--player-name");
        final String uuid =
                required(
                        options,
                        "--uuid");
        final String accessTokenEnv =
                optional(
                        options,
                        "--access-token-env",
                        DEFAULT_ACCESS_TOKEN_ENV);
        final String accessToken =
                environment.containsKey(
                        accessTokenEnv)
                        ? Objects.requireNonNull(
                        environment.get(
                                accessTokenEnv),
                        "access token environment value")
                        : "";

        final String userProperties =
                optional(
                        options,
                        "--user-properties",
                        "{}");
        final String userType =
                optional(
                        options,
                        "--user-type",
                        "mojang");

        final Path gameDirectory =
                options.containsKey(
                        "--game-dir")
                        ? path(options.get(
                        "--game-dir"))
                        : minecraftHome;
        final Path nativeStagingParent =
                options.containsKey(
                        "--native-staging")
                        ? path(options.get(
                        "--native-staging"))
                        : gameDirectory
                        .resolve(".custommc")
                        .resolve("native-staging")
                        .toAbsolutePath()
                        .normalize();

        final int minimumMemoryMb =
                positiveInt(
                        optional(
                                options,
                                "--min-mb",
                                Integer.toString(
                                        DEFAULT_MINIMUM_MEMORY_MB)),
                        "--min-mb");
        final int maximumMemoryMb =
                positiveInt(
                        optional(
                                options,
                                "--max-mb",
                                Integer.toString(
                                        DEFAULT_MAXIMUM_MEMORY_MB)),
                        "--max-mb");

        final JavaRuntimeInspection javaInspection =
                new JavaRuntimeProbe().inspect(
                        javaHome,
                        runtimeTarget.operatingSystem());
        if (!javaInspection.usable()) {
            throw new IllegalArgumentException(
                    "Java runtime is not usable: "
                            + javaInspection.problems());
        }

        return new Minecraft189LaunchRequest(
                MinecraftInstallation.forVersion(
                        minecraftHome,
                        Minecraft189LaunchRequest.VERSION_ID),
                runtimeTarget,
                javaInspection.runtime(),
                new LaunchIdentity(
                        playerName,
                        uuid,
                        accessToken,
                        userProperties,
                        userType),
                gameDirectory,
                nativeStagingParent,
                minimumMemoryMb,
                maximumMemoryMb,
                Minecraft189RuntimeBundle.fromDirectory(
                        runtimeOverlay));
    }

    public static int run(
            final String[] arguments)
            throws Exception {
        final Minecraft189LaunchRequest request =
                prepare(
                        arguments,
                        System.getenv(),
                        RuntimeTarget.current());

        try (LaunchProcessSession session =
                     new Minecraft189Launcher()
                             .start(request)) {
            return session.awaitExit();
        }
    }

    private static Map<String, String> parse(
            final String[] arguments) {
        final Map<String, String> options =
                new LinkedHashMap<String, String>();

        for (int index = 0;
             index < arguments.length;
             index += 2) {
            final String key =
                    Objects.requireNonNull(
                            arguments[index],
                            "argument");
            if (!key.startsWith("--")) {
                throw new IllegalArgumentException(
                        "expected option name: "
                                + key);
            }
            if (index + 1 >= arguments.length) {
                throw new IllegalArgumentException(
                        "missing value for "
                                + key);
            }
            if (!knownOption(key)) {
                throw new IllegalArgumentException(
                        "unknown launch option: "
                                + key);
            }
            if (options.containsKey(key)) {
                throw new IllegalArgumentException(
                        "duplicate launch option: "
                                + key);
            }

            final String value =
                    Objects.requireNonNull(
                            arguments[index + 1],
                            "option value");
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "blank value for "
                                + key);
            }
            options.put(key, value);
        }

        return options;
    }

    private static boolean knownOption(
            final String key) {
        return "--minecraft-home".equals(key)
                || "--java-home".equals(key)
                || "--runtime-overlay".equals(key)
                || "--player-name".equals(key)
                || "--uuid".equals(key)
                || "--access-token-env".equals(key)
                || "--user-properties".equals(key)
                || "--user-type".equals(key)
                || "--game-dir".equals(key)
                || "--native-staging".equals(key)
                || "--min-mb".equals(key)
                || "--max-mb".equals(key);
    }

    private static String required(
            final Map<String, String> options,
            final String key) {
        final String value =
                options.get(key);
        if (value == null) {
            throw new IllegalArgumentException(
                    "missing required launch option: "
                            + key);
        }
        return value;
    }

    private static String optional(
            final Map<String, String> options,
            final String key,
            final String fallback) {
        final String value =
                options.get(key);
        return value == null
                ? fallback
                : value;
    }

    private static int positiveInt(
            final String value,
            final String key) {
        final int parsed;
        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException(
                    "invalid integer for "
                            + key
                            + ": "
                            + value,
                    invalid);
        }
        if (parsed <= 0) {
            throw new IllegalArgumentException(
                    key
                            + " must be positive");
        }
        return parsed;
    }

    private static Path path(
            final String value) {
        return Paths.get(value)
                .toAbsolutePath()
                .normalize();
    }
}
