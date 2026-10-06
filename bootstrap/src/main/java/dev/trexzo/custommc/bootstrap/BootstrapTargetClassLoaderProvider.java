package dev.trexzo.custommc.bootstrap;

public interface BootstrapTargetClassLoaderProvider {
    ClassLoader targetClassLoader(
            ClassLoader bootstrapLoader)
            throws Exception;
}
