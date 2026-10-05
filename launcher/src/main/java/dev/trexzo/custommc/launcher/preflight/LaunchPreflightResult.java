package dev.trexzo.custommc.launcher.preflight;

import dev.trexzo.custommc.launcher.command.LaunchCommand;
import dev.trexzo.custommc.launcher.integrity.LaunchIntegrityReport;
import dev.trexzo.custommc.launcher.metadata.MinecraftLaunchTemplate;
import dev.trexzo.custommc.launcher.natives.NativeStager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class LaunchPreflightResult
        implements AutoCloseable {
    private final MinecraftLaunchTemplate template;
    private final LaunchIntegrityReport integrityReport;
    private final Path nativeDirectory;
    private final LaunchCommand command;

    public LaunchPreflightResult(
            final MinecraftLaunchTemplate template,
            final LaunchIntegrityReport integrityReport,
            final Path nativeDirectory,
            final LaunchCommand command) {
        this.template = Objects.requireNonNull(
                template,
                "template");
        this.integrityReport = Objects.requireNonNull(
                integrityReport,
                "integrityReport");
        this.nativeDirectory = Objects.requireNonNull(
                nativeDirectory,
                "nativeDirectory")
                .toAbsolutePath()
                .normalize();
        this.command = Objects.requireNonNull(
                command,
                "command");
    }

    public MinecraftLaunchTemplate template() {
        return template;
    }

    public LaunchIntegrityReport integrityReport() {
        return integrityReport;
    }

    public Path nativeDirectory() {
        return nativeDirectory;
    }

    public LaunchCommand command() {
        return command;
    }

    @Override
    public void close() throws IOException {
        NativeStager.cleanup(nativeDirectory);
    }
}
