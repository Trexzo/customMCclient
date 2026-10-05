package dev.trexzo.custommc.launcher.natives;

import java.io.IOException;

public final class NativeStagingException extends IOException {
    private static final long serialVersionUID = 1L;

    public NativeStagingException(final String message) {
        super(message);
    }
}
