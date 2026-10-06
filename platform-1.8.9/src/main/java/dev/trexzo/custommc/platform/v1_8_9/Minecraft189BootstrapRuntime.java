package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.bootstrap.BootstrapRuntimeSession;
import dev.trexzo.custommc.bootstrap.BootstrapTargetClassLoaderProvider;
import dev.trexzo.custommc.bootstrap.TransformingTargetClassLoader;
import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.io.IOException;
import java.util.Objects;

public final class Minecraft189BootstrapRuntime
        implements BootstrapRuntimeSession,
        BootstrapTargetClassLoaderProvider {
    private final BootstrapContext bootstrapContext;
    private final EventBus events;
    private final ModuleRegistry modules;
    private final ModuleController moduleController;
    private final ServiceRegistry services;
    private final RenderPipeline renderPipeline;
    private final SettingRegistry settings;
    private final SettingPresentationRegistry settingPresentations;
    private final ModulePresentationRegistry modulePresentations;
    private final ModuleCategoryRegistry moduleCategories;
    private final ModuleSettingRegistry moduleSettings;
    private final ModuleKeybindRegistry moduleKeybinds;
    private final ModuleKeybindAssignments moduleKeybindAssignments;
    private final Minecraft189Platform platform;
    private final ServiceRegistry.Registration renderPipelineRegistration;
    private final Minecraft189ModuleKeybindRuntime keybindRuntime;
    private Minecraft189RuntimeBridge.Registration bridgeRegistration;
    private TransformingTargetClassLoader targetLoader;
    private Minecraft189HostRuntime hostRuntime;
    private boolean targetMainEntered;
    private long nextTickIndex;
    private long nextFrameIndex;
    private long currentFrameIndex = -1L;
    private boolean closed;

    private Minecraft189BootstrapRuntime(
            final BootstrapContext bootstrapContext,
            final EventBus events,
            final ModuleRegistry modules,
            final ModuleController moduleController,
            final ServiceRegistry services,
            final RenderPipeline renderPipeline,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final ModulePresentationRegistry modulePresentations,
            final ModuleCategoryRegistry moduleCategories,
            final ModuleSettingRegistry moduleSettings,
            final ModuleKeybindRegistry moduleKeybinds,
            final ModuleKeybindAssignments moduleKeybindAssignments,
            final Minecraft189Platform platform,
            final ServiceRegistry.Registration renderPipelineRegistration,
            final Minecraft189ModuleKeybindRuntime keybindRuntime) {
        this.bootstrapContext = bootstrapContext;
        this.events = events;
        this.modules = modules;
        this.moduleController = moduleController;
        this.services = services;
        this.renderPipeline = renderPipeline;
        this.settings = settings;
        this.settingPresentations = settingPresentations;
        this.modulePresentations = modulePresentations;
        this.moduleCategories = moduleCategories;
        this.moduleSettings = moduleSettings;
        this.moduleKeybinds = moduleKeybinds;
        this.moduleKeybindAssignments = moduleKeybindAssignments;
        this.platform = platform;
        this.renderPipelineRegistration = renderPipelineRegistration;
        this.keybindRuntime = keybindRuntime;
    }

    public static Minecraft189BootstrapRuntime create(
            final BootstrapContext bootstrapContext) {
        final BootstrapContext context =
                Objects.requireNonNull(
                        bootstrapContext,
                        "bootstrapContext");

        final EventBus events =
                new EventBus();
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController moduleController =
                new ModuleController(modules);
        final ServiceRegistry services =
                new ServiceRegistry();
        final RenderPipeline renderPipeline =
                new RenderPipeline();
        final SettingRegistry settings =
                new SettingRegistry();
        final SettingPresentationRegistry settingPresentations =
                new SettingPresentationRegistry();
        final ModulePresentationRegistry modulePresentations =
                new ModulePresentationRegistry();
        final ModuleCategoryRegistry moduleCategories =
                new ModuleCategoryRegistry();
        final ModuleSettingRegistry moduleSettings =
                new ModuleSettingRegistry(
                        modules,
                        settings);
        final ModuleKeybindRegistry moduleKeybinds =
                new ModuleKeybindRegistry(
                        modules);
        final ModuleKeybindAssignments moduleKeybindAssignments =
                new ModuleKeybindAssignments(
                        moduleKeybinds);
        final Minecraft189Platform platform =
                new Minecraft189Platform();

        ServiceRegistry.Registration renderRegistration = null;
        Minecraft189ModuleKeybindRuntime keybindRuntime = null;
        try {
            renderRegistration =
                    services.registerManaged(
                            RenderPipeline.class,
                            renderPipeline);

            platform.attach(
                    new PlatformContext(
                            events,
                            modules,
                            moduleController,
                            services));

            keybindRuntime =
                    Minecraft189ModuleKeybindRuntime.install(
                            platform,
                            moduleKeybinds);

            final Minecraft189BootstrapRuntime runtime =
                    new Minecraft189BootstrapRuntime(
                            context,
                            events,
                            modules,
                            moduleController,
                            services,
                            renderPipeline,
                            settings,
                            settingPresentations,
                            modulePresentations,
                            moduleCategories,
                            moduleSettings,
                            moduleKeybinds,
                            moduleKeybindAssignments,
                            platform,
                            renderRegistration,
                            keybindRuntime);
            runtime.bridgeRegistration =
                    Minecraft189RuntimeBridge.install(
                            runtime);
            return runtime;
        } catch (RuntimeException failure) {
            if (keybindRuntime != null) {
                keybindRuntime.close();
            }
            if (platform.attached()) {
                platform.detach();
            }
            moduleKeybindAssignments.close();
            if (renderRegistration != null) {
                renderRegistration.close();
            }
            throw failure;
        }
    }

    public BootstrapContext bootstrapContext() {
        return bootstrapContext;
    }

    public EventBus events() {
        return events;
    }

    public ModuleRegistry modules() {
        return modules;
    }

    public ModuleController moduleController() {
        return moduleController;
    }

    public ServiceRegistry services() {
        return services;
    }

    public RenderPipeline renderPipeline() {
        return renderPipeline;
    }

    public SettingRegistry settings() {
        return settings;
    }

    public SettingPresentationRegistry settingPresentations() {
        return settingPresentations;
    }

    public ModulePresentationRegistry modulePresentations() {
        return modulePresentations;
    }

    public ModuleCategoryRegistry moduleCategories() {
        return moduleCategories;
    }

    public ModuleSettingRegistry moduleSettings() {
        return moduleSettings;
    }

    public ModuleKeybindRegistry moduleKeybinds() {
        return moduleKeybinds;
    }

    public ModuleKeybindAssignments moduleKeybindAssignments() {
        return moduleKeybindAssignments;
    }

    public Minecraft189Platform platform() {
        return platform;
    }

    @Override
    public synchronized ClassLoader targetClassLoader(
            final ClassLoader bootstrapLoader) {
        requireOpen();
        if (targetLoader == null) {
            targetLoader =
                    TransformingTargetClassLoader
                            .fromJavaClassPath(
                                    Objects.requireNonNull(
                                            bootstrapLoader,
                                            "bootstrapLoader"),
                                    new Minecraft189ClassTransformer());
        }
        return targetLoader;
    }

    synchronized void markTargetMainEntered() {
        requireOpen();
        if (targetMainEntered) {
            throw new IllegalStateException(
                    "minecraft 1.8.9 target main already entered");
        }
        targetMainEntered = true;
    }

    public synchronized boolean targetMainEntered() {
        return targetMainEntered;
    }

    public synchronized Minecraft189HostRuntime installHost(
            final LegacyUiHostCallbacks hostCallbacks) {
        requireOpen();
        if (hostRuntime != null) {
            throw new IllegalStateException(
                    "minecraft 1.8.9 bootstrap host is already installed");
        }

        final Minecraft189HostRuntime installed =
                Minecraft189HostRuntime.install(
                        platform,
                        modulePresentations,
                        moduleCategories,
                        moduleSettings,
                        moduleKeybinds,
                        moduleKeybindAssignments,
                        settings,
                        settingPresentations,
                        Objects.requireNonNull(
                                hostCallbacks,
                                "hostCallbacks"));
        hostRuntime = installed;
        return installed;
    }

    public synchronized boolean hostInstalled() {
        return !closed
                && hostRuntime != null
                && !hostRuntime.closed();
    }

    synchronized void publishGameTick() {
        if (closed
                || hostRuntime == null
                || hostRuntime.closed()) {
            return;
        }

        final long tickIndex =
                nextTickIndex;
        nextTickIndex =
                Math.addExact(
                        nextTickIndex,
                        1L);
        hostRuntime.publishTick(
                tickIndex);
    }

    synchronized void beginRenderFrame(
            final float partialTicks) {
        if (closed
                || hostRuntime == null
                || hostRuntime.closed()) {
            return;
        }
        requirePartialTicks(partialTicks);
        currentFrameIndex =
                nextFrameIndex;
        nextFrameIndex =
                Math.addExact(
                        nextFrameIndex,
                        1L);
    }

    synchronized void renderHudFrame(
            final float partialTicks) {
        if (closed
                || hostRuntime == null
                || hostRuntime.closed()
                || currentFrameIndex < 0L) {
            return;
        }
        hostRuntime.renderHud(
                currentFrameIndex,
                partialTicks);
    }

    synchronized Minecraft189HostRuntime requireHostRuntime() {
        requireOpen();
        if (hostRuntime == null
                || hostRuntime.closed()) {
            throw new IllegalStateException(
                    "minecraft 1.8.9 bootstrap host is not installed");
        }
        return hostRuntime;
    }

    public synchronized boolean closed() {
        return closed;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }

        RuntimeException failure = null;

        if (bridgeRegistration != null) {
            try {
                bridgeRegistration.close();
            } catch (RuntimeException closeFailure) {
                failure = closeFailure;
            }
        }

        if (targetLoader != null) {
            try {
                targetLoader.close();
            } catch (IOException closeFailure) {
                failure = append(
                        failure,
                        new IllegalStateException(
                                "failed closing Minecraft 1.8.9 target loader",
                                closeFailure));
            }
        }

        if (hostRuntime != null) {
            try {
                hostRuntime.close();
            } catch (RuntimeException closeFailure) {
                failure = append(
                        failure,
                        closeFailure);
            }
        }

        try {
            keybindRuntime.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            moduleKeybindAssignments.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            renderPipelineRegistration.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            platform.detach();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        if (failure != null) {
            throw failure;
        }
    }

    private static void requirePartialTicks(
            final float partialTicks) {
        if (Float.isNaN(partialTicks)
                || partialTicks < 0.0F
                || partialTicks > 1.0F) {
            throw new IllegalArgumentException(
                    "partialTicks must be within [0,1]");
        }
    }

    private synchronized void requireOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "minecraft 1.8.9 bootstrap runtime is closed");
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
