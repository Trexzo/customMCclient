package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;

final class Minecraft189WTapFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189WTapModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private boolean closed;

    private Minecraft189WTapFeature(
            final ModuleController controller,
            final Minecraft189WTapModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
    }

    static Minecraft189WTapFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations) {
        final Minecraft189WTapModule module =
                new Minecraft189WTapModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189WTapModule.ID,
                                    "W-Tap",
                                    "Resets sprint for one tick on a fresh physical left-click press.",
                                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID,
                                    50));

            return new Minecraft189WTapFeature(
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

    Minecraft189WTapModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "w-tap feature is closed");
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
                    Minecraft189WTapModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189WTapModule.ID);
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
