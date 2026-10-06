package dev.trexzo.custommc.bootstrap;

public interface BootstrapRuntimeSession
        extends AutoCloseable {
    @Override
    void close() throws Exception;
}
