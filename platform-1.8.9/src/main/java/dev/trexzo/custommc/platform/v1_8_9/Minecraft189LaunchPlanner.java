package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.LaunchPlan;
import dev.trexzo.custommc.platform.RuntimeLayout;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Minecraft189LaunchPlanner {
    public LaunchPlan create(
            final Path javaExecutable,
            final RuntimeLayout runtime,
            final String username,
            final String accessToken,
            final Path gameDirectory) {
        Objects.requireNonNull(runtime, "runtime");

        final List<Path> classpath = new ArrayList<Path>();
        classpath.add(runtime.requireArtifact("minecraft-client"));
        classpath.add(runtime.requireArtifact("authlib"));

        final List<String> gameArgs = Arrays.asList(
                "--username",
                requireText(username, "username"),
                "--version",
                Minecraft189Descriptor.VERSION,
                "--gameDir",
                Objects.requireNonNull(gameDirectory, "gameDirectory")
                        .toAbsolutePath()
                        .toString(),
                "--accessToken",
                requireText(accessToken, "accessToken"));

        return new LaunchPlan(
                Objects.requireNonNull(javaExecutable, "javaExecutable"),
                Minecraft189Descriptor.MAIN_CLASS,
                Collections.<String>emptyList(),
                gameArgs,
                classpath);
    }

    private static String requireText(
            final String value,
            final String name) {
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
