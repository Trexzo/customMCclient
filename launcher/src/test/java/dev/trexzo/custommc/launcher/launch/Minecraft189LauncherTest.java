package dev.trexzo.custommc.launcher.launch;

import dev.trexzo.custommc.launcher.command.LaunchCommand;
import dev.trexzo.custommc.launcher.command.LaunchIdentity;
import dev.trexzo.custommc.launcher.command.Minecraft189RuntimeBundle;
import dev.trexzo.custommc.launcher.integrity.LaunchIntegrityReport;
import dev.trexzo.custommc.launcher.java.JavaRuntime;
import dev.trexzo.custommc.launcher.metadata.MinecraftLaunchTemplate;
import dev.trexzo.custommc.launcher.preflight.LaunchPreflightException;
import dev.trexzo.custommc.launcher.preflight.LaunchPreflightRequest;
import dev.trexzo.custommc.launcher.preflight.LaunchPreflightResult;
import dev.trexzo.custommc.launcher.runtime.CpuArchitecture;
import dev.trexzo.custommc.launcher.runtime.MinecraftInstallation;
import dev.trexzo.custommc.launcher.runtime.OperatingSystem;
import dev.trexzo.custommc.launcher.runtime.RuntimeTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189LauncherTest {
    @TempDir
    Path tempDir;

    @Test
    void canonicalRequestCarriesBundleIntoTemplateAwarePreflight() {
        final Minecraft189RuntimeBundle bundle =
                runtimeBundle();
        final Minecraft189LaunchRequest request =
                request(bundle);

        final LaunchPreflightRequest preflight =
                request.preflightRequest();

        assertSame(
                request.installation(),
                preflight.installation());
        assertSame(
                request.runtimeTarget(),
                preflight.runtimeTarget());
        assertSame(
                request.javaRuntime(),
                preflight.javaRuntime());
        assertSame(
                request.identity(),
                preflight.identity());
        assertEquals(
                request.gameDirectory(),
                preflight.gameDirectory());
        assertEquals(
                request.nativeStagingParent(),
                preflight.nativeStagingParent());
        assertEquals(
                512,
                preflight.minimumMemoryMb());
        assertEquals(
                2048,
                preflight.maximumMemoryMb());

        final MinecraftLaunchTemplate template =
                template();
        assertEquals(
                bundle.resolve(template)
                        .classpathPrefix(),
                preflight.resolveRuntimeOverlay(template)
                        .classpathPrefix());
        assertEquals(
                bundle.resolve(template)
                        .mainClassOverride(),
                preflight.resolveRuntimeOverlay(template)
                        .mainClassOverride());
    }

    @Test
    void launcherHandsExactPreflightResultToProcessStart()
            throws Exception {
        final LaunchPreflightResult prepared =
                preflightResult();
        final AtomicReference<LaunchPreflightRequest> seenRequest =
                new AtomicReference<LaunchPreflightRequest>();
        final AtomicReference<LaunchPreflightResult> seenResult =
                new AtomicReference<LaunchPreflightResult>();
        final IOException marker =
                new IOException("process marker");

        final Minecraft189Launcher launcher =
                new Minecraft189Launcher(
                        request -> {
                            seenRequest.set(request);
                            return prepared;
                        },
                        result -> {
                            seenResult.set(result);
                            throw marker;
                        });

        final IOException failure =
                assertThrows(
                        IOException.class,
                        () -> launcher.start(
                                request(runtimeBundle())));

        assertSame(marker, failure);
        assertSame(
                prepared,
                seenResult.get());
        assertEquals(
                Minecraft189LaunchRequest.VERSION_ID,
                seenRequest.get()
                        .installation()
                        .versionId());

        prepared.close();
    }

    @Test
    void preflightFailurePreventsProcessStart()
            throws Exception {
        final AtomicBoolean processStarted =
                new AtomicBoolean();
        final LaunchPreflightException marker =
                new LaunchPreflightException(
                        "preflight marker");

        final Minecraft189Launcher launcher =
                new Minecraft189Launcher(
                        request -> {
                            throw marker;
                        },
                        result -> {
                            processStarted.set(true);
                            return null;
                        });

        final LaunchPreflightException failure =
                assertThrows(
                        LaunchPreflightException.class,
                        () -> launcher.start(
                                request(runtimeBundle())));

        assertSame(marker, failure);
        assertFalse(processStarted.get());
    }

    @Test
    void canonicalRequestRejectsOtherMinecraftVersions() {
        final MinecraftInstallation wrong =
                MinecraftInstallation.forVersion(
                        tempDir.resolve("minecraft"),
                        "1.12.2");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Minecraft189LaunchRequest(
                        wrong,
                        runtimeTarget(),
                        javaRuntime(),
                        identity(),
                        tempDir.resolve("game"),
                        tempDir.resolve("natives"),
                        512,
                        2048,
                        runtimeBundle()));
    }

    private Minecraft189LaunchRequest request(
            final Minecraft189RuntimeBundle bundle) {
        return new Minecraft189LaunchRequest(
                MinecraftInstallation.forVersion(
                        tempDir.resolve("minecraft"),
                        Minecraft189LaunchRequest.VERSION_ID),
                runtimeTarget(),
                javaRuntime(),
                identity(),
                tempDir.resolve("game"),
                tempDir.resolve("native-staging"),
                512,
                2048,
                bundle);
    }

    private Minecraft189RuntimeBundle runtimeBundle() {
        final Path root =
                tempDir.resolve("runtime-overlay");
        return Minecraft189RuntimeBundle.fromDirectory(
                root);
    }

    private JavaRuntime javaRuntime() {
        final Path home =
                tempDir.resolve("java");
        return new JavaRuntime(
                home,
                home.resolve("bin/java"),
                "1.8.0_412",
                8);
    }

    private static RuntimeTarget runtimeTarget() {
        return new RuntimeTarget(
                OperatingSystem.LINUX,
                CpuArchitecture.X64,
                "6.0");
    }

    private static LaunchIdentity identity() {
        return new LaunchIdentity(
                "Player",
                "uuid",
                "secret",
                "{}",
                "mojang");
    }

    private MinecraftLaunchTemplate template() {
        return new MinecraftLaunchTemplate(
                Minecraft189LaunchRequest.VERSION_ID,
                "net.minecraft.client.main.Main",
                "",
                "1.8",
                Collections.emptyList(),
                Collections.emptyList());
    }

    private LaunchPreflightResult preflightResult()
            throws IOException {
        final Path natives =
                tempDir.resolve("prepared-natives");
        Files.createDirectories(natives);

        return new LaunchPreflightResult(
                template(),
                new LaunchIntegrityReport(
                        Collections.emptyList()),
                natives,
                new LaunchCommand(
                        Collections.singletonList("java"),
                        tempDir.resolve("game"),
                        Collections.emptySet()));
    }
}
