package dev.trexzo.custommc.launcher.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ExpandedArguments {
    private final List<String> arguments;
    private final Set<Integer> sensitiveIndexes;

    public ExpandedArguments(
            final List<String> arguments,
            final Set<Integer> sensitiveIndexes) {
        this.arguments = Collections.unmodifiableList(
                new ArrayList<String>(
                        Objects.requireNonNull(
                                arguments,
                                "arguments")));
        this.sensitiveIndexes =
                Collections.unmodifiableSet(
                        new LinkedHashSet<Integer>(
                                Objects.requireNonNull(
                                        sensitiveIndexes,
                                        "sensitiveIndexes")));
    }

    public List<String> arguments() {
        return arguments;
    }

    public Set<Integer> sensitiveIndexes() {
        return sensitiveIndexes;
    }
}
