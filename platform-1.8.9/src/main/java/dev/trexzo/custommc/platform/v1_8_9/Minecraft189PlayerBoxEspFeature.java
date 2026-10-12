package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.*;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.setting.*;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import java.util.ArrayList;
import java.util.List;

/** Bootstrap- and module-controller-owned native 2D Box ESP registration. */
final class Minecraft189PlayerBoxEspFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189PlayerBoxEspModule module;
    private final List<AutoCloseable> owned;
    private boolean closed;
    private Minecraft189PlayerBoxEspFeature(final ModuleController controller,
            final Minecraft189PlayerBoxEspModule module,
            final List<AutoCloseable> owned) {
        this.controller = controller;
        this.module = module;
        this.owned = owned;
    }

    static Minecraft189PlayerBoxEspFeature install(final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry bindings,
            final SettingRegistry settings,
            final SettingPresentationRegistry labels,
            final Minecraft189WorldCameraFrameState camera,
            final RenderPipeline pipeline, final LegacyUiHostCallbacks host) {
        final Minecraft189PlayerBoxEspModule module =
                new Minecraft189PlayerBoxEspModule(camera, pipeline, host);
        final List<AutoCloseable> owned = new ArrayList<AutoCloseable>();
        try {
            owned.add(modules.register(module));
            owned.add(presentations.register(new ModuleDescriptor(
                    module.id(), "Player Box ESP",
                    "Projected native player bounding boxes, strict per-frame GL source.",
                    Minecraft189FeatureCatalog.VISUALS_CATEGORY_ID, 59)));
            owned.add(settings.register(module.borderWidthSetting()));
            owned.add(labels.register(new SettingDescriptor(
                    Minecraft189PlayerBoxEspModule.BORDER_WIDTH, "Outline Width",
                    SettingValueKind.INTEGER, 0,
                    new SettingNumericSpec(1, 4, 1))));
            owned.add(bindings.register(new ModuleSettingBinding(
                    module.id(), Minecraft189PlayerBoxEspModule.BORDER_WIDTH, 0)));
            return new Minecraft189PlayerBoxEspFeature(controller, module, owned);
        } catch (RuntimeException e) {
            closeOwned(owned, e);
            throw e;
        }
    }

    Minecraft189PlayerBoxEspModule module() {
        if (closed) throw new IllegalStateException("box ESP feature closed");
        return module;
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        try {
            if (controller.stateOf(module.id()) != ModuleState.DISABLED)
                controller.disable(module.id());
        } catch (RuntimeException e) { failure = e; }
        failure = closeOwned(owned, failure);
        if (failure != null) throw failure;
    }

    private static RuntimeException closeOwned(final List<AutoCloseable> registrations,
            RuntimeException failure) {
        for (int i = registrations.size()-1; i>=0; i--) {
            try { registrations.get(i).close(); }
            catch (Exception e) {
                final RuntimeException wrapped = e instanceof RuntimeException
                        ? (RuntimeException)e : new IllegalStateException(e);
                if (failure == null) failure = wrapped;
                else failure.addSuppressed(wrapped);
            }
        }
        return failure;
    }
}
