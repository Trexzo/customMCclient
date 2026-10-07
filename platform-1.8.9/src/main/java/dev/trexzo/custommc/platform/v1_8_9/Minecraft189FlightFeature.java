package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;

final class Minecraft189FlightFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189FlightModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private boolean closed;

    private Minecraft189FlightFeature(
            final ModuleController controller,
            final Minecraft189FlightModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
    }

    static Minecraft189FlightFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final Minecraft189InputState inputState) {
        final Minecraft189FlightModule module =
                new Minecraft189FlightModule(
                        inputState);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189FlightModule.ID,
                                    "Flight",
                                    "Controls vertical motion: Space ascends and Shift descends.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    80));

            return new Minecraft189FlightFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation);
        } catch (RuntimeException failure) {
            if (presentation != null) {
                presentation.close();
            }
            if (moduleRegistration != null) {
                moduleRegistration.close();
            }
            throw failure;
        }
    }

    Minecraft189FlightModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "flight feature is closed");
        }
        return module;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        RuntimeException failure = null;
        try {
            if (controller.stateOf(
                    Minecraft189FlightModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189FlightModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        try {
            presentation.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }
        try {
            moduleRegistration.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        if (failure != null) {
            throw failure;
        }
    }

    private static RuntimeException append(
            final RuntimeException primary,
            final RuntimeException next) {
        if (primary == null) {
            return next;
        }
        primary.addSuppressed(next);
        return primary;
    }
}
