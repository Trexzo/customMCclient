package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.bootstrap.BootstrapRuntimeInitializer;
import dev.trexzo.custommc.bootstrap.BootstrapRuntimeSession;

public final class Minecraft189BootstrapInitializer
        implements BootstrapRuntimeInitializer {
    public Minecraft189BootstrapInitializer() {
    }

    @Override
    public BootstrapRuntimeSession initialize(
            final BootstrapContext context) {
        return Minecraft189BootstrapRuntime.create(
                context);
    }
}
