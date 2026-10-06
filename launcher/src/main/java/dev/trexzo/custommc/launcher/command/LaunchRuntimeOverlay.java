package dev.trexzo.custommc.launcher.command;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class LaunchRuntimeOverlay {
    private static final LaunchRuntimeOverlay NONE =
            new LaunchRuntimeOverlay(
                    Collections.<Path>emptyList(),
                    null,
                    Collections.<String>emptyList());

    private final List<Path> classpathPrefix;
    private final String mainClassOverride;
    private final List<String> mainArgumentsPrefix;

    public LaunchRuntimeOverlay(
            final List<Path> classpathPrefix,
            final String mainClassOverride,
            final List<String> mainArgumentsPrefix) {
        Objects.requireNonNull(
                classpathPrefix,
                "classpathPrefix");
        Objects.requireNonNull(
                mainArgumentsPrefix,
                "mainArgumentsPrefix");

        final List<Path> normalized =
                new ArrayList<Path>(
                        classpathPrefix.size());
        final Set<Path> seen =
                new LinkedHashSet<Path>();
        for (Path path : classpathPrefix) {
            final Path value =
                    Objects.requireNonNull(
                            path,
                            "classpath path")
                            .toAbsolutePath()
                            .normalize();
            if (!seen.add(value)) {
                throw new IllegalArgumentException(
                        "duplicate overlay classpath entry: "
                                + value);
            }
            normalized.add(value);
        }

        final List<String> arguments =
                new ArrayList<String>(
                        mainArgumentsPrefix.size());
        for (String argument : mainArgumentsPrefix) {
            arguments.add(
                    Objects.requireNonNull(
                            argument,
                            "main argument"));
        }

        if (mainClassOverride != null
                && mainClassOverride.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "mainClassOverride must not be blank");
        }

        this.classpathPrefix =
                Collections.unmodifiableList(
                        normalized);
        this.mainClassOverride =
                mainClassOverride == null
                        ? null
                        : mainClassOverride.trim();
        this.mainArgumentsPrefix =
                Collections.unmodifiableList(
                        arguments);
    }

    public static LaunchRuntimeOverlay none() {
        return NONE;
    }

    public List<Path> classpathPrefix() {
        return classpathPrefix;
    }

    public boolean overridesMainClass() {
        return mainClassOverride != null;
    }

    public String mainClass(
            final String fallbackMainClass) {
        return mainClassOverride == null
                ? Objects.requireNonNull(
                        fallbackMainClass,
                        "fallbackMainClass")
                : mainClassOverride;
    }

    public List<String> mainArgumentsPrefix() {
        return mainArgumentsPrefix;
    }

    public boolean empty() {
        return classpathPrefix.isEmpty()
                && mainClassOverride == null
                && mainArgumentsPrefix.isEmpty();
    }
}
