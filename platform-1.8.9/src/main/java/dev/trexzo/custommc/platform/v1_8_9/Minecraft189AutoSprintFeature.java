package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;

final class Minecraft189AutoSprintFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AutoSprintModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private boolean closed;

    private Minecraft189AutoSprintFeature(
            final ModuleController controller,
            final Minecraft189AutoSprintModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
    }

    static Minecraft189AutoSprintFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations) {
        final Minecraft189AutoSprintModule module =
                new Minecraft189AutoSprintModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189AutoSprintModule.ID,
                                    "Auto Sprint",
                                    "Keeps sprint enabled while possible without overriding sneak.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    0));

            return new Minecraft189AutoSprintFeature(
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

    Minecraft189AutoSprintModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "auto-sprint feature is closed");
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
                    Minecraft189AutoSprintModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189AutoSprintModule.ID);
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
