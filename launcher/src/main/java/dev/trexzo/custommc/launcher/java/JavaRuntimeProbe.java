package dev.trexzo.custommc.launcher.java;

import dev.trexzo.custommc.launcher.runtime.OperatingSystem;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public final class JavaRuntimeProbe {
    public JavaRuntimeInspection inspect(
            final Path javaHome,
            final OperatingSystem operatingSystem)
            throws IOException {
        Objects.requireNonNull(javaHome, "javaHome");
        Objects.requireNonNull(
                operatingSystem,
                "operatingSystem");

        final Path home =
                javaHome.toAbsolutePath().normalize();
        final List<JavaRuntimeProblem> problems =
                new ArrayList<JavaRuntimeProblem>();

        if (!Files.isDirectory(home)) {
            problems.add(JavaRuntimeProblem.HOME_MISSING);
            return new JavaRuntimeInspection(
                    home,
                    null,
                    problems);
        }

        final Path releaseFile =
                home.resolve("release");
        final String executableName =
                operatingSystem == OperatingSystem.WINDOWS
                        ? "java.exe"
                        : "java";
        final Path executable =
                home.resolve("bin")
                        .resolve(executableName);

        if (!Files.isRegularFile(releaseFile)) {
            problems.add(
                    JavaRuntimeProblem.RELEASE_FILE_MISSING);
        }
        if (!Files.isRegularFile(executable)) {
            problems.add(
                    JavaRuntimeProblem.JAVA_EXECUTABLE_MISSING);
        }

        String version = null;
        Integer major = null;

        if (Files.isRegularFile(releaseFile)) {
            final Properties properties =
                    new Properties();
            try (Reader reader = Files.newBufferedReader(
                    releaseFile,
                    StandardCharsets.UTF_8)) {
                properties.load(reader);
            }

            final String raw =
                    properties.getProperty("JAVA_VERSION");
            if (raw == null
                    || stripQuotes(raw).trim().isEmpty()) {
                problems.add(
                        JavaRuntimeProblem.JAVA_VERSION_MISSING);
            } else {
                version = stripQuotes(raw);
                try {
                    major = Integer.valueOf(
                            parseMajor(version));
                } catch (IllegalArgumentException invalid) {
                    problems.add(
                            JavaRuntimeProblem.JAVA_VERSION_INVALID);
                }
            }
        }

        if (!problems.isEmpty()) {
            return new JavaRuntimeInspection(
                    home,
                    null,
                    problems);
        }

        return new JavaRuntimeInspection(
                home,
                new JavaRuntime(
                        home,
                        executable,
                        version,
                        major.intValue()),
                java.util.Collections
                        .<JavaRuntimeProblem>emptyList());
    }

    static int parseMajor(final String version) {
        Objects.requireNonNull(version, "version");
        String numeric = version.trim();

        if (numeric.startsWith("1.")) {
            numeric = numeric.substring(2);
        }

        int end = 0;
        while (end < numeric.length()
                && Character.isDigit(
                        numeric.charAt(end))) {
            end++;
        }

        if (end == 0) {
            throw new IllegalArgumentException(
                    "invalid Java version: " + version);
        }

        final int major = Integer.parseInt(
                numeric.substring(0, end));
        if (major <= 0) {
            throw new IllegalArgumentException(
                    "invalid Java major version: " + version);
        }
        return major;
    }

    private static String stripQuotes(final String value) {
        final String trimmed = value.trim();
        if (trimmed.length() >= 2
                && trimmed.startsWith("\"")
                && trimmed.endsWith("\"")) {
            return trimmed.substring(
                    1,
                    trimmed.length() - 1);
        }
        return trimmed;
    }
}
