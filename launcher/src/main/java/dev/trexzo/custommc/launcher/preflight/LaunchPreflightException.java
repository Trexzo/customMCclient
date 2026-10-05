package dev.trexzo.custommc.launcher.preflight;

public final class LaunchPreflightException extends Exception {
    private static final long serialVersionUID = 1L;

    public LaunchPreflightException(final String message) {
        super(message);
    }

    public LaunchPreflightException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }
}
