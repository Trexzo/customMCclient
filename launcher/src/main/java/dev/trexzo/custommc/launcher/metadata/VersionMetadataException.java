package dev.trexzo.custommc.launcher.metadata;

import java.io.IOException;

public final class VersionMetadataException extends IOException {
    private static final long serialVersionUID = 1L;

    public VersionMetadataException(final String message) {
        super(message);
    }

    public VersionMetadataException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }
}
