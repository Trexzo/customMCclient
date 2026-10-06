package dev.trexzo.custommc.bootstrap;

public interface BootstrapRuntimeInitializer {
    BootstrapRuntimeSession initialize(
            BootstrapContext context)
            throws Exception;
}
