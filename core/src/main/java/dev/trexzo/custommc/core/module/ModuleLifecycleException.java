package dev.trexzo.custommc.core.module;

public final class ModuleLifecycleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ModuleLifecycleException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }
}
