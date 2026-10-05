package dev.trexzo.custommc.launcher.runtime;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MinecraftInstallationProbe {
    public InstallationInspection inspect(
            final MinecraftInstallation installation) {
        Objects.requireNonNull(installation, "installation");

        final List<InstallationProblem> problems =
                new ArrayList<InstallationProblem>();

        if (!Files.isDirectory(installation.home())) {
            problems.add(InstallationProblem.HOME_MISSING);
        }
        if (!Files.isDirectory(
                installation.versionsDirectory())) {
            problems.add(
                    InstallationProblem.VERSIONS_DIRECTORY_MISSING);
        }
        if (!Files.isDirectory(
                installation.librariesDirectory())) {
            problems.add(
                    InstallationProblem.LIBRARIES_DIRECTORY_MISSING);
        }
        if (!Files.isDirectory(
                installation.assetsDirectory())) {
            problems.add(
                    InstallationProblem.ASSETS_DIRECTORY_MISSING);
        }
        if (!Files.isRegularFile(
                installation.versionJson())) {
            problems.add(
                    InstallationProblem.VERSION_JSON_MISSING);
        }
        if (!Files.isRegularFile(
                installation.versionJar())) {
            problems.add(
                    InstallationProblem.VERSION_JAR_MISSING);
        }

        return new InstallationInspection(
                installation,
                problems);
    }
}
