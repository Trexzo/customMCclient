package dev.trexzo.custommc.launcher.runtime;

import java.util.Objects;

public final class RuntimeTarget {
    private final OperatingSystem operatingSystem;
    private final CpuArchitecture architecture;
    private final String osVersion;

    public RuntimeTarget(
            final OperatingSystem operatingSystem,
            final CpuArchitecture architecture,
            final String osVersion) {
        this.operatingSystem = Objects.requireNonNull(
                operatingSystem,
                "operatingSystem");
        this.architecture = Objects.requireNonNull(
                architecture,
                "architecture");
        this.osVersion = Objects.requireNonNull(
                osVersion,
                "osVersion");
    }

    public static RuntimeTarget current() {
        return new RuntimeTarget(
                OperatingSystem.current(),
                CpuArchitecture.current(),
                System.getProperty("os.version", ""));
    }

    public OperatingSystem operatingSystem() {
        return operatingSystem;
    }

    public CpuArchitecture architecture() {
        return architecture;
    }

    public String osVersion() {
        return osVersion;
    }
}
