package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189VelocityModuleTest {
    @Test
    void velocityScalesOnlyKnockbackDeltaAndPreservesVanillaWhileDisabled() {
        final Minecraft189VelocityModule module =
                new Minecraft189VelocityModule();

        assertFalse(
                module.active());
        assertEquals(
                5.0D,
                module.adjustHorizontal(
                        2.0D,
                        5.0D),
                0.000001D);
        assertEquals(
                6.0D,
                module.adjustVertical(
                        4.0D,
                        6.0D),
                0.000001D);

        module.onEnable();
        assertTrue(
                module.active());
        assertEquals(
                2.0D,
                module.adjustHorizontal(
                        2.0D,
                        5.0D),
                0.000001D);
        assertEquals(
                4.0D,
                module.adjustVertical(
                        4.0D,
                        6.0D),
                0.000001D);

        module.horizontalPercentSetting()
                .set(50);
        module.verticalPercentSetting()
                .set(25);
        assertEquals(
                3.5D,
                module.adjustHorizontal(
                        2.0D,
                        5.0D),
                0.000001D);
        assertEquals(
                4.5D,
                module.adjustVertical(
                        4.0D,
                        6.0D),
                0.000001D);

        module.horizontalPercentSetting()
                .set(150);
        module.verticalPercentSetting()
                .set(200);
        assertEquals(
                6.5D,
                module.adjustHorizontal(
                        2.0D,
                        5.0D),
                0.000001D);
        assertEquals(
                8.0D,
                module.adjustVertical(
                        4.0D,
                        6.0D),
                0.000001D);

        assertThrows(
                IllegalArgumentException.class,
                () -> module.horizontalPercentSetting()
                        .set(-1));
        assertThrows(
                IllegalArgumentException.class,
                () -> module.verticalPercentSetting()
                        .set(201));

        module.onDisable();
        assertFalse(
                module.active());
        assertEquals(
                5.0D,
                module.adjustHorizontal(
                        2.0D,
                        5.0D),
                0.000001D);
    }

    @Test
    void onlyWhileSprintingGatePreservesLegacyScalingAndFailsClosed() {
        final Minecraft189VelocityModule module = new Minecraft189VelocityModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        assertFalse(module.onlyWhileSprintingSetting().get().booleanValue());
        module.onEnable();
        module.horizontalPercentSetting().set(50);
        module.verticalPercentSetting().set(25);
        // Default disabled guard: legacy scaling works without movement data.
        assertEquals(3.5D, module.adjustHorizontal(2.0D, 5.0D), 0.000001D);
        assertEquals(4.5D, module.adjustVertical(4.0D, 6.0D), 0.000001D);

        module.onlyWhileSprintingSetting().set(Boolean.TRUE);
        assertEquals(5.0D, module.adjustHorizontal(2.0D, 5.0D), 0.000001D);
        assertEquals(6.0D, module.adjustVertical(4.0D, 6.0D), 0.000001D);
        assertEquals(5.0D, module.adjustHorizontal(2.0D, 5.0D,
                movement.snapshot()), 0.000001D);

        movement.update(true, false, false);
        assertEquals(5.0D, module.adjustHorizontal(2.0D, 5.0D,
                movement.snapshot()), 0.000001D);
        assertEquals(6.0D, module.adjustVertical(4.0D, 6.0D,
                movement.snapshot()), 0.000001D);

        movement.update(false, false, true);
        assertEquals(3.5D, module.adjustHorizontal(2.0D, 5.0D,
                movement.snapshot()), 0.000001D);
        assertEquals(4.5D, module.adjustVertical(4.0D, 6.0D,
                movement.snapshot()), 0.000001D);

        movement.update(false, false, false);
        assertEquals(5.0D, module.adjustHorizontal(2.0D, 5.0D,
                movement.snapshot()), 0.000001D);
        movement.clear();
        assertEquals(5.0D, module.adjustHorizontal(2.0D, 5.0D,
                movement.snapshot()), 0.000001D);

        module.onlyWhileSprintingSetting().set(Boolean.FALSE);
        assertEquals(3.5D, module.adjustHorizontal(2.0D, 5.0D,
                movement.snapshot()), 0.000001D);
        module.onDisable();
        assertEquals(5.0D, module.adjustHorizontal(2.0D, 5.0D), 0.000001D);
        assertEquals(6.0D, module.adjustVertical(4.0D, 6.0D), 0.000001D);
    }

    @Test
    void mappedHostVelocityGatePersistsAndCleansUpAcrossAxes() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(
                new EventBus(), modules, controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(), new NoOpHost());
        try {
            final Minecraft189VelocityModule velocity =
                    runtime.featureCatalog().velocity();
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189VelocityModule.ONLY_WHILE_SPRINTING_SETTING_ID));
            controller.enable(Minecraft189VelocityModule.ID);
            velocity.horizontalPercentSetting().set(0);
            velocity.verticalPercentSetting().set(0);
            assertEquals(2.0D, runtime.adjustVelocityHorizontal(2.0D, 5.0D),
                    0.000001D);
            assertEquals(4.0D, runtime.adjustVelocityVertical(4.0D, 6.0D),
                    0.000001D);
            velocity.onlyWhileSprintingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189VelocityModule.ONLY_WHILE_SPRINTING_SETTING_ID));
            assertEquals(5.0D, runtime.adjustVelocityHorizontal(2.0D, 5.0D),
                    0.000001D);
            assertEquals(6.0D, runtime.adjustVelocityVertical(4.0D, 6.0D),
                    0.000001D);
            runtime.playerMovementState().update(true, false, false);
            assertEquals(5.0D, runtime.adjustVelocityHorizontal(2.0D, 5.0D),
                    0.000001D);
            runtime.playerMovementState().update(true, false, true);
            assertEquals(2.0D, runtime.adjustVelocityHorizontal(2.0D, 5.0D),
                    0.000001D);
            assertEquals(4.0D, runtime.adjustVelocityVertical(4.0D, 6.0D),
                    0.000001D);

            velocity.horizontalPercentSetting().set(50);
            velocity.verticalPercentSetting().set(25);
            assertEquals(3.5D, runtime.adjustVelocityHorizontal(2.0D, 5.0D),
                    0.000001D);
            assertEquals(4.5D, runtime.adjustVelocityVertical(4.0D, 6.0D),
                    0.000001D);
            runtime.playerMovementState().update(true, false, false);
            assertEquals(5.0D, runtime.adjustVelocityHorizontal(2.0D, 5.0D),
                    0.000001D);
            runtime.playerMovementState().clear();
            assertEquals(6.0D, runtime.adjustVelocityVertical(4.0D, 6.0D),
                    0.000001D);
            velocity.onlyWhileSprintingSetting().set(Boolean.FALSE);
            assertEquals(3.5D, runtime.adjustVelocityHorizontal(2.0D, 5.0D),
                    0.000001D);
            controller.disable(Minecraft189VelocityModule.ID);
            assertEquals(5.0D, runtime.adjustVelocityHorizontal(2.0D, 5.0D),
                    0.000001D);
            assertEquals(6.0D, runtime.adjustVelocityVertical(4.0D, 6.0D),
                    0.000001D);
        } finally {
            runtime.close();
        }
        assertEquals(null, settings.find(
                Minecraft189VelocityModule.ONLY_WHILE_SPRINTING_SETTING_ID));
    }

    private static final class NoOpHost
            implements LegacyUiHostCallbacks {
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
        }

        @Override
        public void endUi() {
        }
    }
}
