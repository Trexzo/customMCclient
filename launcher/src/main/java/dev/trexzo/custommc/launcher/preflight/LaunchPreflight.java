package dev.trexzo.custommc.launcher.preflight;

import dev.trexzo.custommc.launcher.command.JvmLaunchCommandBuilder;
import dev.trexzo.custommc.launcher.command.LaunchCommand;
import dev.trexzo.custommc.launcher.command.LaunchCommandException;
import dev.trexzo.custommc.launcher.command.LaunchRequest;
import dev.trexzo.custommc.launcher.integrity.ArtifactVerifier;
import dev.trexzo.custommc.launcher.integrity.LaunchIntegrityReport;
import dev.trexzo.custommc.launcher.metadata.MinecraftLaunchTemplate;
import dev.trexzo.custommc.launcher.metadata.MinecraftVersionResolver;
import dev.trexzo.custommc.launcher.natives.NativeStager;
import dev.trexzo.custommc.launcher.runtime.InstallationInspection;
import dev.trexzo.custommc.launcher.runtime.MinecraftInstallationProbe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class LaunchPreflight {
    private final MinecraftInstallationProbe installationProbe;
    private final MinecraftVersionResolver versionResolver;
    private final ArtifactVerifier artifactVerifier;
    private final NativeStager nativeStager;
    private final JvmLaunchCommandBuilder commandBuilder;

    public LaunchPreflight() {
        this(
                new MinecraftInstallationProbe(),
                new MinecraftVersionResolver(),
                new ArtifactVerifier(),
                new NativeStager(
                        new ArtifactVerifier()),
                new JvmLaunchCommandBuilder());
    }

    LaunchPreflight(
            final MinecraftInstallationProbe installationProbe,
            final MinecraftVersionResolver versionResolver,
            final ArtifactVerifier artifactVerifier,
            final NativeStager nativeStager,
            final JvmLaunchCommandBuilder commandBuilder) {
        this.installationProbe = Objects.requireNonNull(
                installationProbe,
                "installationProbe");
        this.versionResolver = Objects.requireNonNull(
                versionResolver,
                "versionResolver");
        this.artifactVerifier = Objects.requireNonNull(
                artifactVerifier,
                "artifactVerifier");
        this.nativeStager = Objects.requireNonNull(
                nativeStager,
                "nativeStager");
        this.commandBuilder = Objects.requireNonNull(
                commandBuilder,
                "commandBuilder");
    }

    public LaunchPreflightResult prepare(
            final LaunchPreflightRequest request)
            throws LaunchPreflightException {
        Objects.requireNonNull(request, "request");

        final InstallationInspection installationInspection =
                installationProbe.inspect(
                        request.installation());
        if (!installationInspection.launchReady()) {
            throw new LaunchPreflightException(
                    "Minecraft installation is not launch-ready: "
                            + installationInspection.problems());
        }

        Path nativeDirectory = null;
        try {
            final MinecraftLaunchTemplate template =
                    versionResolver.resolve(
                            request.installation(),
                            request.runtimeTarget());

            final LaunchIntegrityReport integrityReport =
                    artifactVerifier.verify(template);
            if (!integrityReport.passes()) {
                throw new LaunchPreflightException(
                        "artifact integrity gate failed");
            }

            final dev.trexzo.custommc.launcher.command.LaunchRuntimeOverlay
                    runtimeOverlay =
                    request.resolveRuntimeOverlay(
                            template);

            validateRuntimeOverlay(
                    runtimeOverlay);

            nativeDirectory = nativeStager.stage(
                    template.nativeArchives(),
                    request.nativeStagingParent());

            final LaunchRequest launchRequest =
                    new LaunchRequest(
                            request.javaRuntime()
                                    .executable(),
                            request.gameDirectory(),
                            request.installation()
                                    .assetsDirectory(),
                            nativeDirectory,
                            template,
                            request.identity(),
                            request.runtimeTarget()
                                    .operatingSystem(),
                            request.minimumMemoryMb(),
                            request.maximumMemoryMb(),
                            runtimeOverlay);

            final LaunchCommand command =
                    commandBuilder.build(launchRequest);

            return new LaunchPreflightResult(
                    template,
                    integrityReport,
                    nativeDirectory,
                    command);
        } catch (IOException
                | LaunchCommandException failure) {
            cleanupAfterFailure(
                    nativeDirectory,
                    failure);
            throw new LaunchPreflightException(
                    "launch preflight failed",
                    failure);
        } catch (LaunchPreflightException failure) {
            cleanupAfterFailure(
                    nativeDirectory,
                    failure);
            throw failure;
        } catch (RuntimeException failure) {
            cleanupAfterFailure(
                    nativeDirectory,
                    failure);
            throw failure;
        }
    }

    private static void validateRuntimeOverlay(
            final dev.trexzo.custommc.launcher.command.LaunchRuntimeOverlay overlay)
            throws LaunchPreflightException {
        for (Path entry : overlay.classpathPrefix()) {
            if (!Files.isRegularFile(entry)
                    && !Files.isDirectory(entry)) {
                throw new LaunchPreflightException(
                        "runtime overlay classpath entry is missing: "
                                + entry);
            }
        }
    }

    private static void cleanupAfterFailure(
            final Path nativeDirectory,
            final Exception failure) {
        if (nativeDirectory == null) {
            return;
        }

        try {
            NativeStager.cleanup(nativeDirectory);
        } catch (IOException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    private static void cleanupAfterFailure(
            final Path nativeDirectory,
            final RuntimeException failure) {
        if (nativeDirectory == null) {
            return;
        }

        try {
            NativeStager.cleanup(nativeDirectory);
        } catch (IOException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }
}
