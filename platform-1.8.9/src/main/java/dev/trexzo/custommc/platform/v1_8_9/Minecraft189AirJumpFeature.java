package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;

final class Minecraft189AirJumpFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AirJumpModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private boolean closed;

    private Minecraft189AirJumpFeature(
            final ModuleController controller,
            final Minecraft189AirJumpModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
    }

    static Minecraft189AirJumpFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final Minecraft189InputState inputState) {
        final Minecraft189AirJumpModule module =
                new Minecraft189AirJumpModule(
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
                                    Minecraft189AirJumpModule.ID,
                                    "Air Jump",
                                    "Allows one extra jump per fresh Space press while airborne.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    15));

            return new Minecraft189AirJumpFeature(
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

    Minecraft189AirJumpModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "air-jump feature is closed");
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
                    Minecraft189AirJumpModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189AirJumpModule.ID);
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
