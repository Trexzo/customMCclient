package dev.trexzo.custommc.core.render;

public final class RenderResourceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RenderResourceException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }
}
