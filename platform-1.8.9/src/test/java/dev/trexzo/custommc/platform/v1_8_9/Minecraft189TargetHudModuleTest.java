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
            assertFalse(hud.compactSetting().get().booleanValue());
            assertFalse(hud.proximityMeterSetting().get().booleanValue());
            assertFalse(hud.showCoordinatesSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.SHOW_COORDINATES_SETTING_ID));
            assertEquals(Integer.valueOf(16), hud.proximityRangeSetting().get());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.PROXIMITY_METER_SETTING_ID));
            assertEquals("16", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.PROXIMITY_RANGE_SETTING_ID));
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
            assertTrue(host.bars.isEmpty()); // Default HUD is pixel-compatible.
            assertEquals(36.0F, host.lastX, 0.001F);
            assertEquals(241.0F, host.lastY, 0.001F);
            assertEquals("24", settings.snapshotEncoded()
                    .get(Minecraft189TargetHudModule.X_SETTING_ID));
            assertEquals("202", settings.snapshotEncoded()
                    .get(Minecraft189TargetHudModule.Y_SETTING_ID));

            // M232: switch to the smaller source-grounded two-row layout
            // at runtime; no change to selected target or position.
            hud.compactSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded()
                    .get(Minecraft189TargetHudModule.COMPACT_SETTING_ID));
            runtime.renderHud(21L, 0.0F);
            assertEquals(5, host.texts.size());
            assertEquals("TARGET  #2", host.texts.get(3));
            assertEquals("DIST  9.0m", host.texts.get(4));
            assertEquals(4, host.roundedRects);
            assertEquals(142.0F, host.lastCardWidth, 0.001F);
            assertEquals(38.0F, host.lastCardHeight, 0.001F);
            assertEquals(2, host.begins);
            assertEquals(2, host.ends);

            hud.compactSetting().set(Boolean.FALSE);
            runtime.renderHud(22L, 0.0F);
            assertEquals(8, host.texts.size());
            assertEquals("YAW  0°    PITCH  0°", host.texts.get(7));
            assertEquals(164.0F, host.lastCardWidth, 0.001F);
            assertEquals(56.0F, host.lastCardHeight, 0.001F);

            // M265: optional meter is distance-only, not HP or reach.
            hud.proximityMeterSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.PROXIMITY_METER_SETTING_ID));
            runtime.renderHud(23L, 0.0F);
            assertEquals(2, host.bars.size());
            assertBar(host.bars.get(0), 36.0F, 253.0F,
                    140.0F, 2.0F, 0xFF303D4A);
            assertBar(host.bars.get(1), 36.0F, 253.0F,
                    61.25F, 2.0F, 0xFF70C9E8);

            // Range beyond 9m -> proportional fill; below 9m -> no fill.
            hud.proximityRangeSetting().set(8);
            host.bars.clear();
            runtime.renderHud(24L, 0.0F);
            assertEquals(1, host.bars.size()); // Empty track only.
            assertBar(host.bars.get(0), 36.0F, 253.0F,
                    140.0F, 2.0F, 0xFF303D4A);

            // Full and compact cards use the same proximity formula but
            // different in-card bar widths and positions.
            hud.proximityRangeSetting().set(40);
            assertEquals("40", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.PROXIMITY_RANGE_SETTING_ID));
            hud.compactSetting().set(Boolean.TRUE);
            host.bars.clear();
            runtime.renderHud(25L, 0.0F);
            assertEquals(2, host.bars.size());
            assertBar(host.bars.get(0), 36.0F, 235.0F,
                    118.0F, 2.0F, 0xFF303D4A);
            assertBar(host.bars.get(1), 36.0F, 235.0F,
                    91.45F, 2.0F, 0xFFFFB65C);

            hud.proximityMeterSetting().set(Boolean.FALSE);
            host.bars.clear();
            runtime.renderHud(26L, 0.0F);
            assertTrue(host.bars.isEmpty());
            assertThrows(IllegalArgumentException.class,
                    () -> hud.proximityRangeSetting().set(0));
            assertThrows(IllegalArgumentException.class,
                    () -> hud.proximityRangeSetting().set(65));

            // M280: Optional source-grounded target XYZ. Both compact and
            // expanded card heights grow only while a valid target exists.
            hud.showCoordinatesSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.SHOW_COORDINATES_SETTING_ID));
            final int textsBeforeCoordinates = host.texts.size();
            runtime.renderHud(27L, 0.0F);
            assertEquals(textsBeforeCoordinates + 3, host.texts.size());
            assertEquals("XYZ  0.0 / 0.0 / 9.0",
                    host.texts.get(host.texts.size() - 1));
            assertEquals(208.0F, host.lastCardWidth, 0.001F);
            assertEquals(54.0F, host.lastCardHeight, 0.001F);
            assertEquals(241.0F, host.lastY, 0.001F);

            hud.compactSetting().set(Boolean.FALSE);
            runtime.renderHud(28L, 0.0F);
            assertEquals("XYZ  0.0 / 0.0 / 9.0",
                    host.texts.get(host.texts.size() - 1));
            assertEquals(224.0F, host.lastCardWidth, 0.001F);
            assertEquals(72.0F, host.lastCardHeight, 0.001F);
            assertEquals(257.0F, host.lastY, 0.001F);
            hud.proximityMeterSetting().set(Boolean.TRUE);
            host.bars.clear();
            runtime.renderHud(29L, 0.0F);
            assertEquals(2, host.bars.size());
            assertBar(host.bars.get(0), 36.0F, 269.0F,
                    200.0F, 2.0F, 0xFF303D4A);

            hud.showCoordinatesSetting().set(Boolean.FALSE);
            hud.proximityMeterSetting().set(Boolean.FALSE);
            host.bars.clear();
            final int textsBeforeToggleOff = host.texts.size();
            runtime.renderHud(29L, 0.0F);
            assertEquals(textsBeforeToggleOff + 3, host.texts.size());
            assertEquals(164.0F, host.lastCardWidth, 0.001F);
            assertEquals(56.0F, host.lastCardHeight, 0.001F);
            assertTrue(host.bars.isEmpty()); // Full legacy geometry returns.

            final int renderedTextsBeforeClear = host.texts.size();
            final int renderedBarsBeforeClear = host.bars.size();
            runtime.nearestPlayerTargetState().clear();
            runtime.targetRotationState().clear();
            runtime.renderHud(30L, 0.0F);
            assertEquals(renderedTextsBeforeClear, host.texts.size());
            assertEquals(renderedBarsBeforeClear, host.bars.size());

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
        assertNull(settings.find(Minecraft189TargetHudModule.COMPACT_SETTING_ID));
        assertNull(settings.find(
                Minecraft189TargetHudModule.PROXIMITY_METER_SETTING_ID));
        assertNull(settings.find(
                Minecraft189TargetHudModule.PROXIMITY_RANGE_SETTING_ID));
        assertNull(settings.find(
                Minecraft189TargetHudModule.SHOW_COORDINATES_SETTING_ID));
    }

    @Test
    void proximityFractionIsBoundedAndRequiresFiniteGroundedDistance() {
        assertEquals(1.0F,
                Minecraft189TargetHudModule.proximityFraction(0.0D, 16), 0.000001F);
        assertEquals(0.75F,
                Minecraft189TargetHudModule.proximityFraction(4.0D, 16), 0.000001F);
        assertEquals(0.4375F,
                Minecraft189TargetHudModule.proximityFraction(9.0D, 16), 0.000001F);
        assertEquals(0.0F,
                Minecraft189TargetHudModule.proximityFraction(16.0D, 16), 0.000001F);
        assertEquals(0.0F,
                Minecraft189TargetHudModule.proximityFraction(100.0D, 16), 0.000001F);
        assertEquals(0.0F,
                Minecraft189TargetHudModule.proximityFraction(Double.NaN, 16), 0.000001F);
        assertEquals(0.0F,
                Minecraft189TargetHudModule.proximityFraction(
                        Double.POSITIVE_INFINITY, 16), 0.000001F);
        assertEquals(0.0F,
                Minecraft189TargetHudModule.proximityFraction(-1.0D, 16), 0.000001F);
        assertEquals(0.0F,
                Minecraft189TargetHudModule.proximityFraction(4.0D, 0), 0.000001F);
        assertEquals(0.0F,
                Minecraft189TargetHudModule.proximityFraction(4.0D, 65), 0.000001F);
        assertEquals(0xFF70C9E8,
                Minecraft189TargetHudModule.proximityFillColor(0.749F));
        assertEquals(0xFFFFB65C,
                Minecraft189TargetHudModule.proximityFillColor(0.75F));
    }

    @Test
    void coordinatesOnlyUseAvailableFiniteNearestPlayerAuthority() {
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        assertNull(Minecraft189TargetHudModule.coordinateText(nearest.snapshot()));
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{0.0D, 0.0D, 9.0D});
        kinds.update(new int[]{Minecraft189WorldEntityKindState.PLAYER
                | Minecraft189WorldEntityKindState.LIVING});
        nearest.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        assertEquals("XYZ  0.0 / 0.0 / 9.0",
                Minecraft189TargetHudModule.coordinateText(nearest.snapshot()));
        nearest.clear();
        assertNull(Minecraft189TargetHudModule.coordinateText(nearest.snapshot()));
    }

    private static void assertBar(
            final Bar bar,
            final float x, final float y,
            final float width, final float height, final int color) {
        assertEquals(x, bar.x, 0.0001F);
        assertEquals(y, bar.y, 0.0001F);
        assertEquals(width, bar.width, 0.0001F);
        assertEquals(height, bar.height, 0.0001F);
        assertEquals(color, bar.color);
    }

    private static final class Bar {
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final int color;
        private Bar(final float x, final float y, final float width,
                final float height, final int color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private final java.util.List<String> texts = new java.util.ArrayList<String>();
        private final java.util.List<Bar> bars = new java.util.ArrayList<Bar>();
        private int roundedRects;
        private float lastCardWidth;
        private float lastCardHeight;
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
            bars.add(new Bar(x, y, width, height, argb));
        }

        @Override
        public void fillRoundedRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float radius,
                final int argb) {
            if (roundedRects % 2 == 0) {
                lastCardWidth = width;
                lastCardHeight = height;
            }
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
