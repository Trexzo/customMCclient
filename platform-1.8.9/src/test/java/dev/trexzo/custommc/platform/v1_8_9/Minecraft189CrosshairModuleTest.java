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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189CrosshairModuleTest {
    @Test
    void crosshairUsesLogicalViewportCenterAndLiveSettings() {
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
            final Minecraft189CrosshairModule crosshair =
                    runtime.featureCatalog()
                            .crosshair();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189CrosshairModule.ID));

            crosshair.lengthSetting().set(6);
            crosshair.gapSetting().set(3);
            crosshair.thicknessSetting().set(2);
            crosshair.dotSetting().set(Boolean.TRUE);
            assertEquals(Boolean.FALSE, crosshair.sprintExpansionSetting().get());
            assertEquals(Integer.valueOf(4), crosshair.sprintGapBonusSetting().get());

            controller.enable(
                    Minecraft189CrosshairModule.ID);
            assertTrue(
                    crosshair.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    Arrays.asList(
                            "311.0,179.0,6.0,2.0",
                            "323.0,179.0,6.0,2.0",
                            "319.0,171.0,2.0,6.0",
                            "319.0,183.0,2.0,6.0",
                            "319.0,179.0,2.0,2.0"),
                    host.rectangles);
            assertEquals(
                    "6",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189CrosshairModule.LENGTH_SETTING_ID));
            assertEquals(
                    "3",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189CrosshairModule.GAP_SETTING_ID));
            assertEquals(
                    "2",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189CrosshairModule.THICKNESS_SETTING_ID));
            assertEquals(
                    "true",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189CrosshairModule.DOT_SETTING_ID));

            // Sprinting must not alter the default crosshair while expansion is OFF.
            host.rectangles.clear();
            runtime.playerMovementState().update(true, false, true);
            runtime.renderHud(1L, 0.0F);
            assertEquals(Arrays.asList(
                    "311.0,179.0,6.0,2.0",
                    "323.0,179.0,6.0,2.0",
                    "319.0,171.0,2.0,6.0",
                    "319.0,183.0,2.0,6.0",
                    "319.0,179.0,2.0,2.0"), host.rectangles);

            crosshair.sprintExpansionSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189CrosshairModule.SPRINT_EXPANSION_SETTING_ID));
            assertEquals("4", settings.snapshotEncoded().get(
                    Minecraft189CrosshairModule.SPRINT_GAP_BONUS_SETTING_ID));
            host.rectangles.clear();
            runtime.renderHud(2L, 0.0F);
            assertEquals(Arrays.asList(
                    "307.0,179.0,6.0,2.0",
                    "327.0,179.0,6.0,2.0",
                    "319.0,167.0,2.0,6.0",
                    "319.0,187.0,2.0,6.0",
                    "319.0,179.0,2.0,2.0"), host.rectangles);

            crosshair.sprintGapBonusSetting().set(2);
            assertEquals("2", settings.snapshotEncoded().get(
                    Minecraft189CrosshairModule.SPRINT_GAP_BONUS_SETTING_ID));
            host.rectangles.clear();
            runtime.renderHud(3L, 0.0F);
            assertEquals(Arrays.asList(
                    "309.0,179.0,6.0,2.0",
                    "325.0,179.0,6.0,2.0",
                    "319.0,169.0,2.0,6.0",
                    "319.0,185.0,2.0,6.0",
                    "319.0,179.0,2.0,2.0"), host.rectangles);

            // Walking, missing authority and toggle OFF restore base geometry.
            runtime.playerMovementState().update(true, false, false);
            host.rectangles.clear();
            runtime.renderHud(4L, 0.0F);
            assertEquals("311.0,179.0,6.0,2.0", host.rectangles.get(0));
            runtime.playerMovementState().clear();
            host.rectangles.clear();
            runtime.renderHud(5L, 0.0F);
            assertEquals("311.0,179.0,6.0,2.0", host.rectangles.get(0));
            runtime.playerMovementState().update(true, false, true);
            crosshair.sprintExpansionSetting().set(Boolean.FALSE);
            host.rectangles.clear();
            runtime.renderHud(6L, 0.0F);
            assertEquals("311.0,179.0,6.0,2.0", host.rectangles.get(0));
            assertThrows(IllegalArgumentException.class,
                    () -> crosshair.sprintGapBonusSetting().set(0));
            assertThrows(IllegalArgumentException.class,
                    () -> crosshair.sprintGapBonusSetting().set(13));

            host.rectangles.clear();
            controller.disable(
                    Minecraft189CrosshairModule.ID);
            assertFalse(
                    crosshair.renderPassInstalled());

            runtime.renderHud(
                    1L,
                    0.0F);
            assertTrue(
                    host.rectangles.isEmpty());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189CrosshairModule.ID));
        assertNull(
                settings.find(
                        Minecraft189CrosshairModule.LENGTH_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189CrosshairModule.GAP_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189CrosshairModule.THICKNESS_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189CrosshairModule.DOT_SETTING_ID));
        assertNull(settings.find(
                Minecraft189CrosshairModule.SPRINT_EXPANSION_SETTING_ID));
        assertNull(settings.find(
                Minecraft189CrosshairModule.SPRINT_GAP_BONUS_SETTING_ID));
    }

    @Test
    void effectiveGapRequiresCertifiedSprintStateAndNeverInfersMovement() {
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        assertEquals(3.0F, Minecraft189CrosshairModule.effectiveGap(
                3.0F, false, 4, null));
        assertEquals(3.0F, Minecraft189CrosshairModule.effectiveGap(
                3.0F, true, 4, null));
        assertEquals(3.0F, Minecraft189CrosshairModule.effectiveGap(
                3.0F, true, 4, movement.snapshot()));
        movement.update(true, false, false);
        assertEquals(3.0F, Minecraft189CrosshairModule.effectiveGap(
                3.0F, true, 4, movement.snapshot()));
        movement.update(false, false, true);
        assertEquals(7.0F, Minecraft189CrosshairModule.effectiveGap(
                3.0F, true, 4, movement.snapshot()));
        assertEquals(3.0F, Minecraft189CrosshairModule.effectiveGap(
                3.0F, false, 4, movement.snapshot()));
        movement.clear();
        assertEquals(3.0F, Minecraft189CrosshairModule.effectiveGap(
                3.0F, true, 4, movement.snapshot()));
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private final List<String> rectangles =
                new ArrayList<String>();

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
            return 2.0F;
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
            rectangles.add(
                    Float.toString(x)
                            + ","
                            + Float.toString(y)
                            + ","
                            + Float.toString(width)
                            + ","
                            + Float.toString(height));
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
