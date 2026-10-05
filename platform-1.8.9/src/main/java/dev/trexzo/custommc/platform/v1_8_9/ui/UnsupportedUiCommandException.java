package dev.trexzo.custommc.platform.v1_8_9.ui;

public final class UnsupportedUiCommandException
        extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnsupportedUiCommandException(
            final String commandType) {
        super("unsupported UI command: " + commandType);
    }
}
