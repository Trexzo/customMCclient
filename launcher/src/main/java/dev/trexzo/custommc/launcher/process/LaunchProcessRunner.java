package dev.trexzo.custommc.launcher.process;

import dev.trexzo.custommc.launcher.command.LaunchCommand;
import dev.trexzo.custommc.launcher.preflight.LaunchPreflightResult;

import java.io.IOException;
import java.util.Objects;

public final class LaunchProcessRunner {
    private final ProcessStarter starter;

    public LaunchProcessRunner() {
        this(
                new ProcessStarter() {
                    @Override
                    public Process start(
                            final ProcessBuilder builder)
                            throws IOException {
                        return builder.start();
                    }
                });
    }

    LaunchProcessRunner(
            final ProcessStarter starter) {
        this.starter = Objects.requireNonNull(
                starter,
                "starter");
    }

    public LaunchProcessSession start(
            final LaunchPreflightResult preflight)
            throws IOException {
        Objects.requireNonNull(
                preflight,
                "preflight");

        final LaunchCommand command =
                preflight.command();
        final ProcessBuilder builder =
                new ProcessBuilder(
                        command.arguments());
        builder.directory(
                command.workingDirectory()
                        .toFile());
        builder.redirectInput(
                ProcessBuilder.Redirect.INHERIT);
        builder.redirectOutput(
                ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(
                ProcessBuilder.Redirect.INHERIT);

        final Process process;
        try {
            process = starter.start(builder);
        } catch (IOException
                | RuntimeException failure) {
            try {
                preflight.close();
            } catch (IOException cleanupFailure) {
                failure.addSuppressed(
                        cleanupFailure);
            }
            throw failure;
        }

        return new LaunchProcessSession(
                preflight,
                process);
    }
}
