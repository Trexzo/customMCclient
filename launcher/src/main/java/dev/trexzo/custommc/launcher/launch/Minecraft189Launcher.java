package dev.trexzo.custommc.launcher.launch;

import dev.trexzo.custommc.launcher.preflight.LaunchPreflight;
import dev.trexzo.custommc.launcher.preflight.LaunchPreflightException;
import dev.trexzo.custommc.launcher.preflight.LaunchPreflightResult;
import dev.trexzo.custommc.launcher.process.LaunchProcessRunner;
import dev.trexzo.custommc.launcher.process.LaunchProcessSession;

import java.io.IOException;
import java.util.Objects;

public final class Minecraft189Launcher {
    private final PreflightStep preflightStep;
    private final ProcessStep processStep;

    public Minecraft189Launcher() {
        this(
                new LaunchPreflight(),
                new LaunchProcessRunner());
    }

    public Minecraft189Launcher(
            final LaunchPreflight preflight,
            final LaunchProcessRunner processRunner) {
        this(
                Objects.requireNonNull(
                        preflight,
                        "preflight")::prepare,
                Objects.requireNonNull(
                        processRunner,
                        "processRunner")::start);
    }

    Minecraft189Launcher(
            final PreflightStep preflightStep,
            final ProcessStep processStep) {
        this.preflightStep = Objects.requireNonNull(
                preflightStep,
                "preflightStep");
        this.processStep = Objects.requireNonNull(
                processStep,
                "processStep");
    }

    public LaunchProcessSession start(
            final Minecraft189LaunchRequest request)
            throws LaunchPreflightException, IOException {
        Objects.requireNonNull(
                request,
                "request");

        final LaunchPreflightResult preflight =
                preflightStep.prepare(
                        request.preflightRequest());

        return processStep.start(preflight);
    }

    @FunctionalInterface
    interface PreflightStep {
        LaunchPreflightResult prepare(
                dev.trexzo.custommc.launcher.preflight.LaunchPreflightRequest request)
                throws LaunchPreflightException;
    }

    @FunctionalInterface
    interface ProcessStep {
        LaunchProcessSession start(
                LaunchPreflightResult preflight)
                throws IOException;
    }
}
