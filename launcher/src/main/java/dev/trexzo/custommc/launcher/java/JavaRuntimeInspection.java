package dev.trexzo.custommc.launcher.java;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class JavaRuntimeInspection {
    private final Path home;
    private final JavaRuntime runtime;
    private final List<JavaRuntimeProblem> problems;

    public JavaRuntimeInspection(
            final Path home,
            final JavaRuntime runtime,
            final List<JavaRuntimeProblem> problems) {
        this.home = Objects.requireNonNull(
                home,
                "home").toAbsolutePath().normalize();
        this.runtime = runtime;
        this.problems = Collections.unmodifiableList(
                new ArrayList<JavaRuntimeProblem>(
                        Objects.requireNonNull(
                                problems,
                                "problems")));

        if ((runtime == null) == this.problems.isEmpty()) {
            throw new IllegalArgumentException(
                    "inspection must contain either a runtime or problems");
        }
    }

    public Path home() {
        return home;
    }

    public boolean usable() {
        return runtime != null;
    }

    public JavaRuntime runtime() {
        if (runtime == null) {
            throw new IllegalStateException(
                    "Java runtime is not usable: " + problems);
        }
        return runtime;
    }

    public List<JavaRuntimeProblem> problems() {
        return problems;
    }
}
