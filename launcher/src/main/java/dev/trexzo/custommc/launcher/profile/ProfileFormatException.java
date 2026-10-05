package dev.trexzo.custommc.launcher.profile;

public final class ProfileFormatException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ProfileFormatException(final String message) {
        super(message);
    }

    public ProfileFormatException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }
}
