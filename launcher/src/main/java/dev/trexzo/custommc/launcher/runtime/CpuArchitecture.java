package dev.trexzo.custommc.launcher.runtime;

import java.util.Locale;
import java.util.Objects;

public enum CpuArchitecture {
    X86("32"),
    X64("64"),
    ARM64(null),
    UNKNOWN(null);

    private final String classifierToken;

    CpuArchitecture(final String classifierToken) {
        this.classifierToken = classifierToken;
    }

    public static CpuArchitecture current() {
        return detect(System.getProperty("os.arch"));
    }

    public static CpuArchitecture detect(final String value) {
        final String normalized = Objects.requireNonNull(
                value,
                "value").toLowerCase(Locale.ROOT);

        if (normalized.equals("amd64")
                || normalized.equals("x86_64")
                || normalized.equals("x64")) {
            return X64;
        }
        if (normalized.equals("x86")
                || normalized.equals("i386")
                || normalized.equals("i486")
                || normalized.equals("i586")
                || normalized.equals("i686")) {
            return X86;
        }
        if (normalized.equals("aarch64")
                || normalized.equals("arm64")) {
            return ARM64;
        }
        return UNKNOWN;
    }

    public String classifierToken() {
        if (classifierToken == null) {
            throw new IllegalStateException(
                    "legacy native classifier unsupported for "
                            + name());
        }
        return classifierToken;
    }

    public boolean matchesRule(final String ruleArchitecture) {
        final String normalized = Objects.requireNonNull(
                ruleArchitecture,
                "ruleArchitecture").toLowerCase(Locale.ROOT);

        switch (this) {
            case X86:
                return normalized.equals("x86")
                        || normalized.equals("ia32")
                        || normalized.equals("i386");
            case X64:
                return normalized.equals("x86_64")
                        || normalized.equals("amd64")
                        || normalized.equals("x64");
            case ARM64:
                return normalized.equals("arm64")
                        || normalized.equals("aarch64");
            default:
                return false;
        }
    }
}
