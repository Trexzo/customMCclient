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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189MovementStatusModuleTest {
    @Test
    void movementStatusHudReadsLiveStateAndPersistsPosition() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                new SettingRegistry();
        final ServiceRegistry services =
                new ServiceRegistry();
        services.register(
                RenderPipeline.class,
                new RenderPipeline());

        final Minecraft189Platform platform =
                new Minecraft189Platform();
        platform.attach(
                new PlatformContext(
                        new EventBus(),
                        modules,
                        controller,
                        services));

        final RecordingHost host =
                new RecordingHost();
        final Minecraft189HostRuntime runtime =
                Minecraft189HostRuntime.install(
                        platform,
                        new ModulePresentationRegistry(),
                        new ModuleCategoryRegistry(),
                        new ModuleSettingRegistry(
                                modules,
                                settings),
                        null,
                        null,
                        settings,
                        new SettingPresentationRegistry(),
                        host);

        try {
            final Minecraft189MovementStatusModule movement =
                    runtime.featureCatalog()
                            .movementStatus();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189MovementStatusModule.ID));

            movement.xSetting().set(160);
            movement.ySetting().set(420);
            runtime.playerMovementState(
                    state(
                            true,
                            false,
                            false));

            controller.enable(
                    Minecraft189MovementStatusModule.ID);
            assertTrue(
                    movement.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);
            assertEquals(
                    "Movement: Normal | Ground",
                    host.lastText);
            assertEquals(
                    160.0F,
                    host.lastX);
            assertEquals(
                    420.0F,
                    host.lastY);
            assertEquals(
                    "160",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189MovementStatusModule.X_SETTING_ID));
            assertEquals(
                    "420",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189MovementStatusModule.Y_SETTING_ID));

            runtime.playerMovementState(
                    state(
                            true,
                            true,
                            false));
            runtime.renderHud(
                    1L,
                    0.0F);
            assertEquals(
                    "Movement: Sneak | Ground",
                    host.lastText);

            runtime.playerMovementState(
                    state(
                            false,
                            false,
                            true));
            runtime.renderHud(
                    2L,
                    0.0F);
            assertEquals(
                    "Movement: Sprint | Air",
                    host.lastText);

            runtime.playerMovementState(
                    state(
                            false,
                            true,
                            true));
            runtime.renderHud(
                    3L,
                    0.0F);
            assertEquals(
                    "Movement: Sprint+Sneak | Air",
                    host.lastText);

            final Minecraft189PlayerMovementState.Snapshot snapshot =
                    runtime.playerMovementState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertFalse(
                    snapshot.onGround());
            assertTrue(
                    snapshot.sneaking());
            assertTrue(
                    snapshot.sprinting());

            host.lastText = null;
            runtime.playerMovementState(
                    null);
            runtime.renderHud(
                    4L,
                    0.0F);
            assertNull(
                    host.lastText);
            assertFalse(
                    runtime.playerMovementState()
                            .snapshot()
                            .available());

            controller.disable(
                    Minecraft189MovementStatusModule.ID);
            assertFalse(
                    movement.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189MovementStatusModule.ID));
        assertNull(
                settings.find(
                        Minecraft189MovementStatusModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189MovementStatusModule.Y_SETTING_ID));
    }

    private static Minecraft189PlayerMovementStateAccess state(
            final boolean onGround,
            final boolean sneaking,
            final boolean sprinting) {
        return new Minecraft189PlayerMovementStateAccess() {
            @Override
            public boolean customMcOnGround() {
                return onGround;
            }

            @Override
            public boolean customMcSneaking() {
                return sneaking;
            }

            @Override
            public boolean customMcSprinting() {
                return sprinting;
            }
        };
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private String lastText;
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
            lastText = text;
            lastX = x;
            lastY = y;
        }

        @Override
        public void endUi() {
        }
    }
}
