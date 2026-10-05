package dev.trexzo.custommc.platform;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LaunchPlan {
    private final Path javaExecutable;
    private final String mainClass;
    private final List<String> jvmArgs;
    private final List<String> gameArgs;
    private final List<Path> classpath;

    public LaunchPlan(
            final Path javaExecutable,
            final String mainClass,
            final List<String> jvmArgs,
            final List<String> gameArgs,
            final List<Path> classpath) {
        this.javaExecutable =
                Objects.requireNonNull(javaExecutable, "javaExecutable");
        this.mainClass = requireText(mainClass, "mainClass");
        this.jvmArgs = immutableCopy(jvmArgs, "jvmArgs");
        this.gameArgs = immutableCopy(gameArgs, "gameArgs");
        this.classpath = Collections.unmodifiableList(
                new ArrayList<Path>(
                        Objects.requireNonNull(classpath, "classpath")));
    }

    public Path javaExecutable() {
        return javaExecutable;
    }

    public String mainClass() {
        return mainClass;
    }

    public List<String> jvmArgs() {
        return jvmArgs;
    }

    public List<String> gameArgs() {
        return gameArgs;
    }

    public List<Path> classpath() {
        return classpath;
    }

    private static List<String> immutableCopy(
            final List<String> values,
            final String name) {
        return Collections.unmodifiableList(
                new ArrayList<String>(
                        Objects.requireNonNull(values, name)));
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
