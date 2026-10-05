package dev.trexzo.custommc.launcher.command;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class LaunchCommand {
    private final List<String> arguments;
    private final Path workingDirectory;
    private final Set<Integer> sensitiveIndexes;

    public LaunchCommand(
            final List<String> arguments,
            final Path workingDirectory,
            final Set<Integer> sensitiveIndexes) {
        this.arguments = Collections.unmodifiableList(
                new ArrayList<String>(
                        Objects.requireNonNull(
                                arguments,
                                "arguments")));
        this.workingDirectory =
                Objects.requireNonNull(
                        workingDirectory,
                        "workingDirectory")
                        .toAbsolutePath()
                        .normalize();
        this.sensitiveIndexes =
                Collections.unmodifiableSet(
                        new LinkedHashSet<Integer>(
                                Objects.requireNonNull(
                                        sensitiveIndexes,
                                        "sensitiveIndexes")));

        for (Integer index : this.sensitiveIndexes) {
            if (index.intValue() < 0
                    || index.intValue()
                    >= this.arguments.size()) {
                throw new IllegalArgumentException(
                        "sensitive argument index out of range: "
                                + index);
            }
        }
    }

    public List<String> arguments() {
        return arguments;
    }

    public Path workingDirectory() {
        return workingDirectory;
    }

    public List<String> redactedArguments() {
        final List<String> redacted =
                new ArrayList<String>(arguments);

        for (Integer index : sensitiveIndexes) {
            redacted.set(
                    index.intValue(),
                    "<redacted>");
        }

        return Collections.unmodifiableList(redacted);
    }
}
