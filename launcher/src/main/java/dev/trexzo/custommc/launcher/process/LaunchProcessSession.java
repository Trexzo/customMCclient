package dev.trexzo.custommc.launcher.process;

import dev.trexzo.custommc.launcher.command.LaunchCommand;
import dev.trexzo.custommc.launcher.preflight.LaunchPreflightResult;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class LaunchProcessSession
        implements AutoCloseable {
    private static final Duration DEFAULT_CLOSE_GRACE =
            Duration.ofSeconds(5L);

    private final LaunchPreflightResult preflight;
    private final Process process;
    private boolean cleaned;
    private boolean closed;

    LaunchProcessSession(
            final LaunchPreflightResult preflight,
            final Process process) {
        this.preflight = Objects.requireNonNull(
                preflight,
                "preflight");
        this.process = Objects.requireNonNull(
                process,
                "process");
    }

    public Process process() {
        return process;
    }

    public long pid() {
        return process.pid();
    }

    public LaunchCommand command() {
        return preflight.command();
    }

    public List<String> redactedCommand() {
        return preflight.command()
                .redactedArguments();
    }

    public synchronized boolean closed() {
        return closed;
    }

    public synchronized boolean cleaned() {
        return cleaned;
    }

    public int awaitExit()
            throws InterruptedException, IOException {
        final int exitCode =
                process.waitFor();
        cleanupPreflight();
        return exitCode;
    }

    @Override
    public void close()
            throws IOException {
        close(DEFAULT_CLOSE_GRACE);
    }

    public void close(final Duration grace)
            throws IOException {
        Objects.requireNonNull(
                grace,
                "grace");
        if (grace.isNegative()) {
            throw new IllegalArgumentException(
                    "grace must not be negative");
        }

        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }

        InterruptedException interrupted = null;

        if (process.isAlive()) {
            process.destroy();

            try {
                if (!process.waitFor(
                        grace.toMillis(),
                        TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly();
                    process.waitFor();
                }
            } catch (InterruptedException failure) {
                interrupted = failure;
                process.destroyForcibly();
            }
        }

        IOException cleanupFailure = null;
        try {
            cleanupPreflight();
        } catch (IOException failure) {
            cleanupFailure = failure;
        }

        if (interrupted != null) {
            Thread.currentThread().interrupt();
            final IOException failure =
                    new IOException(
                            "interrupted while closing launched process",
                            interrupted);
            if (cleanupFailure != null) {
                failure.addSuppressed(
                        cleanupFailure);
            }
            throw failure;
        }

        if (cleanupFailure != null) {
            throw cleanupFailure;
        }
    }

    private void cleanupPreflight()
            throws IOException {
        synchronized (this) {
            if (cleaned) {
                return;
            }
            cleaned = true;
        }
        preflight.close();
    }
}
