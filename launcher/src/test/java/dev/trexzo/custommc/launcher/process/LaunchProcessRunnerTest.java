package dev.trexzo.custommc.launcher.process;

import dev.trexzo.custommc.launcher.command.LaunchCommand;
import dev.trexzo.custommc.launcher.integrity.LaunchIntegrityReport;
import dev.trexzo.custommc.launcher.metadata.MinecraftLaunchTemplate;
import dev.trexzo.custommc.launcher.preflight.LaunchPreflightResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LaunchProcessRunnerTest {
    @TempDir
    Path tempDir;

    @Test
    void successfulStartOwnsStagingUntilProcessExits()
            throws Exception {
        final Path nativeDirectory =
                nativeDirectory();
        final FakeProcess process =
                new FakeProcess(17);
        final AtomicReference<ProcessBuilder> seen =
                new AtomicReference<ProcessBuilder>();

        final LaunchProcessRunner runner =
                new LaunchProcessRunner(
                        builder -> {
                            seen.set(builder);
                            return process;
                        });

        final LaunchPreflightResult preflight =
                preflight(nativeDirectory);

        final LaunchProcessSession session =
                runner.start(preflight);

        assertTrue(
                Files.exists(nativeDirectory));
        assertFalse(session.cleaned());
        assertFalse(session.closed());

        final ProcessBuilder builder =
                seen.get();
        assertEquals(
                preflight.command().arguments(),
                builder.command());
        assertEquals(
                preflight.command()
                        .workingDirectory()
                        .toFile(),
                builder.directory());
        assertEquals(
                ProcessBuilder.Redirect.INHERIT,
                builder.redirectInput());
        assertEquals(
                ProcessBuilder.Redirect.INHERIT,
                builder.redirectOutput());
        assertEquals(
                ProcessBuilder.Redirect.INHERIT,
                builder.redirectError());

        assertEquals(
                17,
                session.awaitExit());

        assertTrue(session.cleaned());
        assertFalse(
                Files.exists(
                        nativeDirectory));
        assertEquals(
                Arrays.asList(
                        "java",
                        "-cp",
                        "game.jar",
                        "Main",
                        "<redacted>"),
                session.redactedCommand());

        session.close();
        assertTrue(session.closed());
        assertEquals(
                0,
                process.destroyCalls);
    }

    @Test
    void startFailureCleansStagingImmediately()
            throws Exception {
        final Path nativeDirectory =
                nativeDirectory();
        final LaunchPreflightResult preflight =
                preflight(nativeDirectory);

        final LaunchProcessRunner runner =
                new LaunchProcessRunner(
                        builder -> {
                            throw new IOException(
                                    "cannot start");
                        });

        assertThrows(
                IOException.class,
                () -> runner.start(preflight));

        assertFalse(
                Files.exists(
                        nativeDirectory));
    }

    @Test
    void closeDestroysLiveChildBeforeCleaningStaging()
            throws Exception {
        final Path nativeDirectory =
                nativeDirectory();
        final FakeProcess process =
                new FakeProcess(0);

        final LaunchProcessSession session =
                new LaunchProcessRunner(
                        builder -> process)
                        .start(
                                preflight(
                                        nativeDirectory));

        assertTrue(process.isAlive());

        session.close(
                Duration.ofMillis(10L));

        assertTrue(session.closed());
        assertTrue(session.cleaned());
        assertEquals(
                1,
                process.destroyCalls);
        assertFalse(process.isAlive());
        assertFalse(
                Files.exists(
                        nativeDirectory));

        session.close();
        assertEquals(
                1,
                process.destroyCalls);
    }

    @Test
    void negativeCloseGraceIsRejectedWithoutClosingSession()
            throws Exception {
        final FakeProcess process =
                new FakeProcess(0);
        final LaunchProcessSession session =
                new LaunchProcessRunner(
                        builder -> process)
                        .start(
                                preflight(
                                        nativeDirectory()));

        assertThrows(
                IllegalArgumentException.class,
                () -> session.close(
                        Duration.ofMillis(-1L)));

        assertFalse(session.closed());
        assertTrue(process.isAlive());

        session.close(
                Duration.ZERO);
        assertTrue(session.closed());
    }

    private Path nativeDirectory()
            throws IOException {
        final Path directory =
                tempDir.resolve(
                        "natives-"
                                + System.nanoTime());
        Files.createDirectories(directory);
        Files.write(
                directory.resolve("native.bin"),
                new byte[] {1, 2, 3});
        return directory;
    }

    private LaunchPreflightResult preflight(
            final Path nativeDirectory) {
        final MinecraftLaunchTemplate template =
                new MinecraftLaunchTemplate(
                        "1.8.9",
                        "Main",
                        "",
                        "1.8",
                        Collections.emptyList(),
                        Collections.emptyList());

        final LaunchCommand command =
                new LaunchCommand(
                        Arrays.asList(
                                "java",
                                "-cp",
                                "game.jar",
                                "Main",
                                "secret"),
                        tempDir.resolve("game"),
                        Collections.singleton(
                                Integer.valueOf(4)));

        return new LaunchPreflightResult(
                template,
                new LaunchIntegrityReport(
                        Collections.emptyList()),
                nativeDirectory,
                command);
    }

    private static final class FakeProcess
            extends Process {
        private final int exitCode;
        private boolean alive = true;
        private int destroyCalls;

        FakeProcess(final int exitCode) {
            this.exitCode = exitCode;
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(
                    new byte[0]);
        }

        @Override
        public InputStream getErrorStream() {
            return new ByteArrayInputStream(
                    new byte[0]);
        }

        @Override
        public int waitFor()
                throws InterruptedException {
            alive = false;
            return exitCode;
        }

        @Override
        public boolean waitFor(
                final long timeout,
                final TimeUnit unit)
                throws InterruptedException {
            alive = false;
            return true;
        }

        @Override
        public int exitValue() {
            if (alive) {
                throw new IllegalThreadStateException(
                        "still running");
            }
            return exitCode;
        }

        @Override
        public void destroy() {
            destroyCalls++;
            alive = false;
        }

        @Override
        public Process destroyForcibly() {
            alive = false;
            return this;
        }

        @Override
        public boolean isAlive() {
            return alive;
        }

        @Override
        public long pid() {
            return 4242L;
        }
    }
}
