package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189TargetHudModuleTest {
    @Test
    void targetHudUsesRealRemotePlayerAuthorityAndOwnsRenderPass() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(),
                modules, controller, services));
        final RecordingHost host = new RecordingHost();
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(), host);
        try {
            final Minecraft189TargetHudModule hud =
                    runtime.featureCatalog().targetHud();
            assertEquals(ModuleState.DISABLED,
                    controller.stateOf(Minecraft189TargetHudModule.ID));
            assertFalse(hud.renderPassInstalled());
            runtime.renderHud(0L, 0.0F);
            assertTrue(host.texts.isEmpty());

            hud.xSetting().set(24);
            hud.ySetting().set(202);
            controller.enable(Minecraft189TargetHudModule.ID);
            assertTrue(hud.renderPassInstalled());
            runtime.renderHud(1L, 0.0F);
            assertTrue(host.texts.isEmpty());

            runtime.playerPositionState().update(0.0D, 0.0D, 0.0D);
            runtime.worldEntityPositionState().update(
                    new double[]{0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 9.0D});
            runtime.worldEntityKindState().update(new int[]{
                    Minecraft189WorldEntityKindState.LIVING
                            | Minecraft189WorldEntityKindState.PLAYER
                            | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                    Minecraft189WorldEntityKindState.LIVING
                            | Minecraft189WorldEntityKindState.PLAYER
            });
            runtime.nearestPlayerTargetState().update(
                    runtime.playerPositionState().snapshot(),
                    runtime.worldEntityPositionState().snapshot(),
                    runtime.worldEntityKindState().snapshot());
            runtime.targetRotationState().update(
                    runtime.playerPositionState().snapshot(),
                    runtime.nearestPlayerTargetState().snapshot());
            runtime.renderHud(2L, 0.0F);
            assertEquals(3, host.texts.size());
            assertEquals("TARGET  #2", host.texts.get(0));
            assertEquals("DIST  9.0m", host.texts.get(1));
            assertEquals("YAW  0°    PITCH  0°", host.texts.get(2));
            assertEquals(2, host.roundedRects);
            assertEquals(1, host.begins);
            assertEquals(1, host.ends);
            assertEquals(36.0F, host.lastX, 0.001F);
            assertEquals(241.0F, host.lastY, 0.001F);
            assertEquals("24", settings.snapshotEncoded()
                    .get(Minecraft189TargetHudModule.X_SETTING_ID));
            assertEquals("202", settings.snapshotEncoded()
                    .get(Minecraft189TargetHudModule.Y_SETTING_ID));

            runtime.nearestPlayerTargetState().clear();
            runtime.targetRotationState().clear();
            runtime.renderHud(3L, 0.0F);
            assertEquals(3, host.texts.size());

            controller.disable(Minecraft189TargetHudModule.ID);
            assertFalse(hud.renderPassInstalled());
            controller.enable(Minecraft189TargetHudModule.ID);
            assertTrue(hud.renderPassInstalled());
            controller.disable(Minecraft189TargetHudModule.ID);
        } finally {
            runtime.close();
        }
        assertNull(modules.find(Minecraft189TargetHudModule.ID));
        assertNull(settings.find(Minecraft189TargetHudModule.X_SETTING_ID));
        assertNull(settings.find(Minecraft189TargetHudModule.Y_SETTING_ID));
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private final java.util.List<String> texts = new java.util.ArrayList<String>();
        private int roundedRects;
        private int begins;
        private int ends;
        private float lastX;
        private float lastY;

        @Override
        public int framebufferWidth() {
            return 1280;
        }

        @Override
        public int framebufferHeight() {
            return 720;
        }

        @Override
        public float uiScale() {
            return 1.0F;
        }

        @Override
        public void beginUi(
                final UiViewport viewport) {
            begins++;
        }

        @Override
        public void fillRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final int argb) {
        }

        @Override
        public void fillRoundedRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float radius,
                final int argb) {
            roundedRects++;
        }

        @Override
        public void strokeRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float thickness,
                final int argb) {
        }

        @Override
        public void pushClip(
                final float x,
                final float y,
                final float width,
                final float height) {
        }

        @Override
        public void popClip() {
        }

        @Override
        public void drawText(
                final UiFontHandle font,
                final float x,
                final float y,
                final String text,
                final int argb) {
            texts.add(text);
            lastX = x;
            lastY = y;
        }

        @Override
        public void endUi() {
            ends++;
        }
    }
}
