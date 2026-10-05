package dev.trexzo.custommc.launcher.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class InstallationInspection {
    private final MinecraftInstallation installation;
    private final List<InstallationProblem> problems;

    public InstallationInspection(
            final MinecraftInstallation installation,
            final List<InstallationProblem> problems) {
        this.installation = Objects.requireNonNull(
                installation,
                "installation");
        this.problems = Collections.unmodifiableList(
                new ArrayList<InstallationProblem>(
                        Objects.requireNonNull(
                                problems,
                                "problems")));
    }

    public MinecraftInstallation installation() {
        return installation;
    }

    public List<InstallationProblem> problems() {
        return problems;
    }

    public boolean launchReady() {
        return problems.isEmpty();
    }
}
