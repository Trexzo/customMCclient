package dev.trexzo.custommc.launcher.command;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

public final class CustomMcBootstrapOverlay {
    public static final String BOOTSTRAP_MAIN_CLASS =
            "dev.trexzo.custommc.bootstrap.CustomMcBootstrapMain";

    private CustomMcBootstrapOverlay() {
    }

    public static LaunchRuntimeOverlay create(
            final Path bootstrapArtifact,
            final String targetMainClass) {
        Objects.requireNonNull(
                bootstrapArtifact,
                "bootstrapArtifact");
        Objects.requireNonNull(
                targetMainClass,
                "targetMainClass");

        final String target =
                targetMainClass.trim();
        if (target.isEmpty()) {
            throw new IllegalArgumentException(
                    "targetMainClass must not be blank");
        }
        if (BOOTSTRAP_MAIN_CLASS.equals(target)) {
            throw new IllegalArgumentException(
                    "bootstrap cannot target itself");
        }

        return new LaunchRuntimeOverlay(
                Collections.singletonList(
                        bootstrapArtifact),
                BOOTSTRAP_MAIN_CLASS,
                Collections.singletonList(
                        target));
    }
}
