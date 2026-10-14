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
    private final Minecraft189WorldCameraFrameState worldCamera =
            new Minecraft189WorldCameraFrameState();
    private Minecraft189PlayerBoxEspFeature playerBoxEspFeature;
    private Minecraft189SafeWalkFeature safeWalkFeature;
    private Minecraft189ScaffoldFeature scaffoldFeature;
    private boolean acceptanceNativeWorldCameraReported;
    private boolean acceptanceNativeWorldOriginReported;
    private boolean acceptanceNativePlayerBoxesReported;
    private boolean acceptanceCameraFrameStartReported;
    private int acceptanceCameraDiagnosticsPrinted;
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
        try {
            playerBoxEspFeature = Minecraft189PlayerBoxEspFeature.install(
                    modules, moduleController, modulePresentations,
                    moduleSettings, settings, settingPresentations,
                    worldCamera, renderPipeline, hostCallbacks);
            safeWalkFeature = Minecraft189SafeWalkFeature.install(
                    modules, moduleController, modulePresentations);
            scaffoldFeature = Minecraft189ScaffoldFeature.install(
                    modules, moduleController, modulePresentations);
            // Existing original-game V28-V31 acceptance opt-in now goes through
            // the SAME module-controller path as an actual user toggle.
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireOriginalScaffoldPlacement"))
                moduleController.enable(Minecraft189ScaffoldModule.ID);
            if (Boolean.getBoolean("custommc.acceptance.requireNativeSafeWalk"))
                moduleController.enable(Minecraft189SafeWalkModule.ID);
            // Explicit CI-only opt-in; production module starts DISABLED.
            if (Boolean.getBoolean("custommc.acceptance.enableRealRemotePlayerBoxEsp"))
                moduleController.enable(Minecraft189PlayerBoxEspModule.ID);
        } catch (RuntimeException installationFailure) {
            if (scaffoldFeature != null) {
                try { scaffoldFeature.close(); }
                catch (RuntimeException closeFailure) {
                    installationFailure.addSuppressed(closeFailure);
                } finally { scaffoldFeature = null; }
            }
            // SafeWalk is installed after Box ESP. Roll back both module
            // registrations before closing the host, including partial
            // autoenable failures. Do not leak either module into a
            // half-initialized runtime.
            if (safeWalkFeature != null) {
                try {
                    safeWalkFeature.close();
                } catch (RuntimeException closeFailure) {
                    installationFailure.addSuppressed(closeFailure);
                } finally {
                    safeWalkFeature = null;
                }
            }
            if (playerBoxEspFeature != null) {
                try {
                    playerBoxEspFeature.close();
                } catch (RuntimeException closeFailure) {
                    installationFailure.addSuppressed(closeFailure);
                } finally {
                    playerBoxEspFeature = null;
                }
            }
            try {
                installed.close();
            } catch (RuntimeException closeFailure) {
                installationFailure.addSuppressed(closeFailure);
            }
            throw installationFailure;
        }
        hostRuntime = installed;
        return installed;
    }

    public synchronized boolean hostInstalled() {
        return !closed
                && hostRuntime != null
                && !hostRuntime.closed();
    }

    synchronized Minecraft189ScaffoldModule scaffoldModule() {
        if (closed || hostRuntime == null || hostRuntime.closed()
                || scaffoldFeature == null) return null;
        return scaffoldFeature.module();
    }

    synchronized boolean nativeSafeWalkSneaking(
            final boolean originalSneaking, final Object entity) {
        return !closed && hostRuntime != null && !hostRuntime.closed()
                && safeWalkFeature != null
                && safeWalkFeature.module().effectiveSneaking(
                        originalSneaking, entity);
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
        worldCamera.clear();
        hostRuntime.frameStarted(
                currentFrameIndex,
                partialTicks);
        if (!acceptanceCameraFrameStartReported
                && Boolean.getBoolean("custommc.acceptance.requireNativeWorldCamera")) {
            acceptanceCameraFrameStartReported = true;
            System.out.println("CUSTOMMC_CAMERA_DIAG_FRAME_STARTED=YES frame="
                    + currentFrameIndex
                    + " captureProperty="
                    + Boolean.getBoolean("custommc.visuals.captureNativeWorldCamera")
                    + " hostInstalled=" + hostInstalled());
            System.out.flush();
        }
    }

    /** Normal 2D ESP module lifecycle enables capture without global flags. */
    private boolean shouldCaptureNativeWorldCamera() {
        return Boolean.getBoolean("custommc.visuals.captureNativeWorldCamera")
                || (playerBoxEspFeature != null
                        && playerBoxEspFeature.module().renderPassInstalled());
    }

    synchronized void captureNativeWorldCamera(
            final int worldPass, final float partialTicks) {
        final boolean gated = closed || hostRuntime == null
                || hostRuntime.closed() || currentFrameIndex < 0L
                || !shouldCaptureNativeWorldCamera();
        if (gated) {
            printAcceptanceCameraDiagnostic("gated", worldPass, partialTicks,
                    null);
            return;
        }
        // Purely observational, read-only GL state sampling.
        worldCamera.captureNative(currentFrameIndex, worldPass, partialTicks);
        printAcceptanceCameraDiagnostic("captured", worldPass, partialTicks,
                worldCamera.snapshot());
        if (!acceptanceNativeWorldCameraReported
                && Boolean.getBoolean("custommc.acceptance.requireNativeWorldCamera")
                && worldPass >= 0 && worldPass <= 2) {
            final Minecraft189WorldCameraFrameState.Snapshot snapshot =
                    worldCamera.snapshot();
            if (snapshot != null && snapshot.frame() == currentFrameIndex
                    && snapshot.pass() == worldPass
                    && Math.abs(snapshot.partialTicks() - partialTicks) <= 0.0001F) {
                final double[] projection = snapshot.projection();
                final int[] viewport = snapshot.viewport();
                // OpenGL perspective has w'=-z, unlike the HUD's
                // orthographic w'=1. Require actual window-sized GL state.
                if (Math.abs(projection[11] + 1.0D) < 0.0001D
                        && Math.abs(projection[15]) < 0.0001D
                        && viewport[2] >= 640 && viewport[3] >= 400) {
                    acceptanceNativeWorldCameraReported = true;
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_NATIVE_WORLD_CAMERA_GL_PASS=YES pass="
                                    + worldPass);
                    System.out.flush();
                }
            }
        }
    }

    /** Limited to first four calls in explicit original-game camera acceptance. */
    private void printAcceptanceCameraDiagnostic(final String stage,
            final int pass, final float partialTicks,
            final Minecraft189WorldCameraFrameState.Snapshot snapshot) {
        if (!Boolean.getBoolean("custommc.acceptance.requireNativeWorldCamera")
                || acceptanceCameraDiagnosticsPrinted++ >= 4) return;
        final double[] projection = snapshot == null ? null : snapshot.projection();
        final int[] viewport = snapshot == null ? null : snapshot.viewport();
        System.out.println("CUSTOMMC_CAMERA_DIAG_CAPTURE stage=" + stage
                + " frame=" + currentFrameIndex
                + " pass=" + pass
                + " partial=" + partialTicks
                + " outcome=" + worldCamera.captureStatus()
                + " projection11=" + (projection == null ? "n/a" : projection[11])
                + " projection15=" + (projection == null ? "n/a" : projection[15])
                + " viewport=" + (viewport == null ? "n/a" :
                        java.util.Arrays.toString(viewport)));
        System.out.flush();
    }

    synchronized void captureNativeWorldOrigin(final Object nativeEntity,
            final int pass, final float partialTicks) {
        if (closed || hostRuntime == null || hostRuntime.closed()
                || currentFrameIndex < 0L
                || !shouldCaptureNativeWorldCamera())
            return;
        final Minecraft189PlayerPositionAccess typed =
                nativeEntity instanceof Minecraft189PlayerPositionAccess
                        ? (Minecraft189PlayerPositionAccess) nativeEntity : null;
        worldCamera.captureOrigin(currentFrameIndex, pass, partialTicks, typed);
        final Minecraft189WorldCameraFrameState.Snapshot result =
                worldCamera.snapshot();
        if (!acceptanceNativeWorldOriginReported
                && Boolean.getBoolean("custommc.acceptance.requireNativeWorldOrigin")
                && result != null && result.originAvailable()
                && result.frame() == currentFrameIndex
                && result.pass() == pass
                && Math.abs(result.partialTicks() - partialTicks) <= 0.0001F) {
            acceptanceNativeWorldOriginReported = true;
            System.out.println(
                    "CUSTOMMC_OFFICIAL_189_NATIVE_RENDER_ORIGIN_PASS=YES");
            System.out.flush();
        }
    }

    synchronized void captureNativePlayerBoxes(final Object nativeWorld,
            final int pass, final float partialTicks) {
        if (closed || hostRuntime == null || hostRuntime.closed()
                || currentFrameIndex < 0L
                || !shouldCaptureNativeWorldCamera())
            return;
        if (!(nativeWorld instanceof Minecraft189WorldLoadedEntityObjectsAccess)
                || !(nativeWorld instanceof Minecraft189WorldEntityKindsAccess)
                || !(nativeWorld instanceof Minecraft189WorldEntityUuidAccess)) {
            worldCamera.clear();
            return;
        }
        try {
            final Object[] entities =
                    ((Minecraft189WorldLoadedEntityObjectsAccess)nativeWorld)
                            .customMcLoadedEntityObjects();
            final int[] kinds =
                    ((Minecraft189WorldEntityKindsAccess)nativeWorld)
                            .customMcLoadedEntityKinds();
            final java.util.UUID[] uuids =
                    ((Minecraft189WorldEntityUuidAccess)nativeWorld)
                            .customMcLoadedEntityUuids();
            worldCamera.captureNativePlayerBoxes(currentFrameIndex, pass,
                    partialTicks, entities, kinds, uuids);
            final Minecraft189ProjectedPlayerFrame witnessed =
                    worldCamera.projectedPlayers();
            if (!acceptanceNativePlayerBoxesReported
                    && Boolean.getBoolean("custommc.acceptance.requireNativePlayerBoxFrame")
                    && witnessed != null && witnessed.frame() == currentFrameIndex) {
                acceptanceNativePlayerBoxesReported = true;
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_NATIVE_PLAYER_BOX_FRAME_PASS=YES remote="
                                + witnessed.remoteCount()
                                + " visible=" + witnessed.visible().size());
                System.out.flush();
            }
        } catch (RuntimeException | LinkageError invalidSource) {
            worldCamera.clear();
        }
    }

    synchronized Minecraft189WorldCameraFrameState.Snapshot worldCameraSnapshot() {
        if (closed || currentFrameIndex < 0L) return null;
        return worldCamera.snapshot();
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

    synchronized void renderPostProcessFrame(
            final float partialTicks) {
        if (closed
                || hostRuntime == null
                || hostRuntime.closed()
                || currentFrameIndex < 0L) {
            return;
        }

        try {
            hostRuntime.renderPostProcess(
                    currentFrameIndex,
                    partialTicks);
        } finally {
            worldCamera.clear();
            currentFrameIndex = -1L;
        }
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

        if (scaffoldFeature != null) {
            try {
                scaffoldFeature.close();
            } catch (RuntimeException closeFailure) {
                failure = append(failure, closeFailure);
            } finally {
                scaffoldFeature = null;
            }
        }

        if (safeWalkFeature != null) {
            try {
                safeWalkFeature.close();
            } catch (RuntimeException closeFailure) {
                failure = append(failure, closeFailure);
            } finally {
                safeWalkFeature = null;
            }
        }

        if (playerBoxEspFeature != null) {
            try {
                playerBoxEspFeature.close();
            } catch (RuntimeException closeFailure) {
                failure = append(failure, closeFailure);
            } finally {
                playerBoxEspFeature = null;
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
