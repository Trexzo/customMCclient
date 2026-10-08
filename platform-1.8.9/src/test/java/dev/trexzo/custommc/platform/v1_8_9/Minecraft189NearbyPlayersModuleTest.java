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

final class Minecraft189NearbyPlayersModuleTest {
    private static final int REMOTE =
            Minecraft189WorldEntityKindState.LIVING
                    | Minecraft189WorldEntityKindState.PLAYER;
    private static final int LOCAL =
            REMOTE | Minecraft189WorldEntityKindState.LOCAL_PLAYER;

    @Test
    void countUsesInclusiveRadiusAndExcludesLocalAndNonPlayers() {
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        assertFalse(Minecraft189NearbyPlayersModule.countNearby(
                local.snapshot(), positions.snapshot(), kinds.snapshot(), 32).available());
        local.update(0, 0, 0);
        positions.update(new double[]{
                0, 0, 0,
                3, 4, 0,
                10, 0, 0,
                0, 0, -33,
                0, 0, 1
        });
        kinds.update(new int[]{LOCAL, REMOTE, REMOTE, REMOTE,
                Minecraft189WorldEntityKindState.LIVING});

        Minecraft189NearbyPlayersModule.NearbySnapshot out =
                Minecraft189NearbyPlayersModule.countNearby(
                        local.snapshot(), positions.snapshot(), kinds.snapshot(), 5);
        assertTrue(out.available());
        assertEquals(1, out.count());
        assertEquals(5.0D, out.nearestDistance(), 0.00001D);

        out = Minecraft189NearbyPlayersModule.countNearby(
                local.snapshot(), positions.snapshot(), kinds.snapshot(), 10);
        assertTrue(out.available());
        assertEquals(2, out.count());
        assertEquals(5.0D, out.nearestDistance(), 0.00001D);

        out = Minecraft189NearbyPlayersModule.countNearby(
                local.snapshot(), positions.snapshot(), kinds.snapshot(), 1);
        assertTrue(out.available());
        assertEquals(0, out.count());
        assertEquals(0.0D, out.nearestDistance(), 0.00001D);

        assertFalse(Minecraft189NearbyPlayersModule.countNearby(
                local.snapshot(), positions.snapshot(), kinds.snapshot(), 0).available());
        assertFalse(Minecraft189NearbyPlayersModule.countNearby(
                local.snapshot(), positions.snapshot(), kinds.snapshot(), 129).available());
        kinds.update(new int[]{LOCAL});
        assertFalse(Minecraft189NearbyPlayersModule.countNearby(
                local.snapshot(), positions.snapshot(), kinds.snapshot(), 32).available());
        kinds.clear();
        assertFalse(Minecraft189NearbyPlayersModule.countNearby(
                local.snapshot(), positions.snapshot(), kinds.snapshot(), 32).available());
        assertFalse(Minecraft189NearbyPlayersModule.countNearby(
                null, positions.snapshot(), kinds.snapshot(), 32).available());
    }

    @Test
    void yawProjectionUsesForwardAtTopAndRightAtScreenRight() {
        final float[] south = Minecraft189NearbyPlayersModule.projectRadar(
                0.0D, 5.0D, 0.0F, 5);
        assertEquals(0.0F, south[0], 0.0001F);
        assertEquals(-46.0F, south[1], 0.0001F);
        final float[] west = Minecraft189NearbyPlayersModule.projectRadar(
                0.0D, 5.0D, 90.0F, 5);
        assertEquals(46.0F, west[0], 0.0001F);
        assertEquals(0.0F, west[1], 0.0001F);
        final float[] east = Minecraft189NearbyPlayersModule.projectRadar(
                5.0D, 0.0D, 0.0F, 5);
        assertEquals(46.0F, east[0], 0.0001F);
        assertEquals(0.0F, east[1], 0.0001F);
        assertNull(Minecraft189NearbyPlayersModule.projectRadar(
                Double.NaN, 0.0D, 0.0F, 5));
        assertNull(Minecraft189NearbyPlayersModule.projectRadar(
                0.0D, 1.0D, Float.NaN, 5));
        assertNull(Minecraft189NearbyPlayersModule.projectRadar(
                0.0D, 1.0D, 0.0F, 0));
        // Fixed map uses world axes: east +X, north -Z.
        final float[] north = Minecraft189NearbyPlayersModule.projectNorthUp(
                0.0D, -5.0D, 5);
        assertEquals(0.0F, north[0], 0.0001F);
        assertEquals(-46.0F, north[1], 0.0001F);
        final float[] eastFixed = Minecraft189NearbyPlayersModule.projectNorthUp(
                5.0D, 0.0D, 5);
        assertEquals(46.0F, eastFixed[0], 0.0001F);
        assertEquals(0.0F, eastFixed[1], 0.0001F);
        assertNull(Minecraft189NearbyPlayersModule.projectNorthUp(
                Double.NaN, 0.0D, 5));
        assertNull(Minecraft189NearbyPlayersModule.projectNorthUp(
                0.0D, 1.0D, 129));
    }

    @Test
    void heightColorsHaveStrictTwoBlockThresholdAndStableDefault() {
        assertEquals(0xFFFFB56B,
                Minecraft189NearbyPlayersModule.radarBlipColor(9.0D, false));
        assertEquals(0xFFFFB56B,
                Minecraft189NearbyPlayersModule.radarBlipColor(-9.0D, false));
        assertEquals(0xFFFFB56B,
                Minecraft189NearbyPlayersModule.radarBlipColor(2.0D, true));
        assertEquals(0xFFFFB56B,
                Minecraft189NearbyPlayersModule.radarBlipColor(-2.0D, true));
        assertEquals(0xFFE391FF,
                Minecraft189NearbyPlayersModule.radarBlipColor(2.01D, true));
        assertEquals(0xFF6EA8FF,
                Minecraft189NearbyPlayersModule.radarBlipColor(-2.01D, true));
        assertEquals(0xFFFFB56B,
                Minecraft189NearbyPlayersModule.radarBlipColor(Double.NaN, true));
    }

    @Test
    void visualHudReadsMappedSnapshotsAndUnregistersCleanly() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules,
                controller, services));
        final RecordingHost host = new RecordingHost();
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(), host);
        try {
            final Minecraft189NearbyPlayersModule radar =
                    runtime.featureCatalog().nearbyPlayers();
            assertEquals(ModuleState.DISABLED,
                    controller.stateOf(Minecraft189NearbyPlayersModule.ID));
            assertFalse(radar.renderPassInstalled());
            assertFalse(radar.showRadarSetting().get().booleanValue());
            assertFalse(radar.northUpSetting().get().booleanValue());
            assertFalse(radar.heightColorsSetting().get().booleanValue());
            assertFalse(radar.highlightNearestSetting().get().booleanValue());
            runtime.renderHud(0L, 0.0F);
            assertTrue(host.texts.isEmpty());
            radar.xSetting().set(25);
            radar.ySetting().set(200);
            radar.radiusSetting().set(5);
            controller.enable(Minecraft189NearbyPlayersModule.ID);
            assertTrue(radar.renderPassInstalled());
            runtime.renderHud(1L, 0.0F);
            assertTrue(host.texts.isEmpty());

            runtime.playerPositionState().update(0, 0, 0);
            // 3-4-5 horizontal target at x=3,z=4, not x=3,y=4.
            runtime.worldEntityPositionState().update(new double[]{
                    0, 0, 0, 3, 0, 4, 6, 0, 0
            });
            runtime.worldEntityKindState().update(new int[]{
                    LOCAL, REMOTE, REMOTE
            });
            runtime.renderHud(2L, 0.0F);
            assertEquals(2, host.texts.size());
            assertEquals("PLAYERS  1 / 5m", host.texts.get(0));
            assertEquals("NEAREST  5.0m", host.texts.get(1));
            assertEquals(2, host.roundedRects);
            assertEquals(1, host.begins);
            assertEquals(1, host.ends);
            assertEquals(37.0F, host.lastX, 0.0001F);
            assertEquals(224.0F, host.lastY, 0.0001F);
            assertEquals(156.0F, host.lastCardWidth, 0.0001F);
            assertEquals(42.0F, host.lastCardHeight, 0.0001F);
            assertEquals("25", settings.snapshotEncoded()
                    .get(Minecraft189NearbyPlayersModule.X_SETTING_ID));
            assertEquals("200", settings.snapshotEncoded()
                    .get(Minecraft189NearbyPlayersModule.Y_SETTING_ID));
            assertEquals("5", settings.snapshotEncoded()
                    .get(Minecraft189NearbyPlayersModule.RADIUS_SETTING_ID));

            radar.radiusSetting().set(1);
            runtime.renderHud(3L, 0.0F);
            assertEquals("PLAYERS  0 / 1m", host.texts.get(2));
            assertEquals("NEAREST  --", host.texts.get(3));
            // With map mode ON but no mapped yaw, preserve count card
            // rather than rendering an invented facing direction.
            radar.showRadarSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded()
                    .get(Minecraft189NearbyPlayersModule.SHOW_RADAR_SETTING_ID));
            radar.radiusSetting().set(5);
            runtime.renderHud(41L, 0.0F);
            assertEquals(6, host.texts.size());
            assertEquals(0, host.markerXs.size());
            assertEquals(42.0F, host.lastCardHeight, 0.001F);

            // Existing yaw authority projects a real forward/right blip.
            runtime.playerRotationState().update(0.0F, 0.0F);
            runtime.renderHud(42L, 0.0F);
            assertEquals(8, host.texts.size());
            assertEquals(1, host.markerXs.size());
            assertEquals(152.0F, host.lastExpandedHeight, 0.001F);
            assertEquals(128.6F, host.markerXs.get(0), 0.001F);
            assertEquals(257.2F, host.markerYs.get(0), 0.001F);

            runtime.playerRotationState().update(90.0F, 0.0F);
            runtime.renderHud(43L, 0.0F);
            assertEquals(2, host.markerXs.size());
            assertEquals(137.8F, host.markerXs.get(1), 0.001F);
            assertEquals(321.6F, host.markerYs.get(1), 0.001F);

            // A missing yaw snapshot makes the radar map disappear.
            runtime.playerRotationState().clear();
            runtime.renderHud(44L, 0.0F);
            assertEquals(2, host.markerXs.size());
            // Fixed north-up mode uses real positions without requiring yaw.
            radar.northUpSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NearbyPlayersModule.NORTH_UP_SETTING_ID));
            runtime.renderHud(45L, 0.0F);
            assertEquals(3, host.markerXs.size());
            assertEquals(128.6F, host.markerXs.get(2), 0.001F);
            assertEquals(330.8F, host.markerYs.get(2), 0.001F);
            assertEquals(152.0F, host.lastCardHeight, 0.001F);
            // In fixed orientation, turning the player does not rotate blips.
            runtime.playerRotationState().update(90.0F, 0.0F);
            runtime.renderHud(46L, 0.0F);
            assertEquals(4, host.markerXs.size());
            assertEquals(host.markerXs.get(2), host.markerXs.get(3));
            assertEquals(host.markerYs.get(2), host.markerYs.get(3));
            // Default relative mode still fails closed if yaw is absent.
            radar.northUpSetting().set(Boolean.FALSE);
            runtime.playerRotationState().clear();
            runtime.renderHud(47L, 0.0F);
            assertEquals(4, host.markerXs.size());
            assertEquals(42.0F, host.lastCardHeight, 0.001F);
            radar.showRadarSetting().set(Boolean.FALSE);

            runtime.worldEntityPositionState().clear();
            runtime.renderHud(4L, 0.0F);
            assertEquals(18, host.texts.size());

            controller.disable(Minecraft189NearbyPlayersModule.ID);
            assertFalse(radar.renderPassInstalled());
            controller.enable(Minecraft189NearbyPlayersModule.ID);
            assertTrue(radar.renderPassInstalled());
            controller.disable(Minecraft189NearbyPlayersModule.ID);
        } finally {
            runtime.close();
        }
        assertNull(modules.find(Minecraft189NearbyPlayersModule.ID));
        assertNull(settings.find(Minecraft189NearbyPlayersModule.X_SETTING_ID));
        assertNull(settings.find(Minecraft189NearbyPlayersModule.Y_SETTING_ID));
        assertNull(settings.find(Minecraft189NearbyPlayersModule.RADIUS_SETTING_ID));
        assertNull(settings.find(Minecraft189NearbyPlayersModule.SHOW_RADAR_SETTING_ID));
        assertNull(settings.find(Minecraft189NearbyPlayersModule.NORTH_UP_SETTING_ID));
        assertNull(settings.find(Minecraft189NearbyPlayersModule.HEIGHT_COLORS_SETTING_ID));
        assertNull(settings.find(Minecraft189NearbyPlayersModule.HIGHLIGHT_NEAREST_SETTING_ID));
    }

    @Test
    void heightColorsUseLiveMeasuredYAndRespectThreeDimensionalRadius() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules,
                controller, services));
        final RecordingHost host = new RecordingHost();
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(), host);
        try {
            final Minecraft189NearbyPlayersModule radar =
                    runtime.featureCatalog().nearbyPlayers();
            radar.showRadarSetting().set(Boolean.TRUE);
            radar.northUpSetting().set(Boolean.TRUE);
            radar.radiusSetting().set(5);
            controller.enable(Minecraft189NearbyPlayersModule.ID);
            runtime.playerPositionState().update(0, 0, 0);
            runtime.worldEntityPositionState().update(new double[]{
                    0, 0, 0,  // Local
                    1, 0, 0,  // Level
                    0, 3, 0,  // Above
                    0, -3, 0, // Below
                    0, 7, 0,  // Remote player outside radius
                    0, 3, 0   // Non-player, must not appear
            });
            runtime.worldEntityKindState().update(new int[]{
                    LOCAL, REMOTE, REMOTE, REMOTE, REMOTE,
                    Minecraft189WorldEntityKindState.LIVING});
            runtime.renderHud(51L, 0.0F);
            assertEquals(3, host.markerColors.size());
            assertEquals(0xFFFFB56B, host.markerColors.get(0).intValue());
            assertEquals(0xFFFFB56B, host.markerColors.get(1).intValue());
            assertEquals(0xFFFFB56B, host.markerColors.get(2).intValue());

            radar.heightColorsSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NearbyPlayersModule.HEIGHT_COLORS_SETTING_ID));
            runtime.renderHud(52L, 0.0F);
            assertEquals(6, host.markerColors.size());
            assertEquals(0xFFFFB56B, host.markerColors.get(3).intValue());
            assertEquals(0xFFE391FF, host.markerColors.get(4).intValue());
            assertEquals(0xFF6EA8FF, host.markerColors.get(5).intValue());

            // Altitude never bypasses inclusive 3D radius filtering.
            radar.radiusSetting().set(2);
            runtime.renderHud(53L, 0.0F);
            assertEquals(7, host.markerColors.size());
            assertEquals(0xFFFFB56B, host.markerColors.get(6).intValue());

            // Relative orientation shares the exact same altitude palette.
            radar.radiusSetting().set(5);
            radar.northUpSetting().set(Boolean.FALSE);
            runtime.playerRotationState().update(90.0F, 0.0F);
            runtime.renderHud(54L, 0.0F);
            assertEquals(10, host.markerColors.size());
            assertEquals(0xFFE391FF, host.markerColors.get(8).intValue());
            assertEquals(0xFF6EA8FF, host.markerColors.get(9).intValue());
            controller.disable(Minecraft189NearbyPlayersModule.ID);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189NearbyPlayersModule.HEIGHT_COLORS_SETTING_ID));
    }

    @Test
    void nearestIndexUsesExactThreeDimensionalDistanceAndStableTies() {
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        local.update(0, 0, 0);
        positions.update(new double[]{
                0, 0, 0,    // Local
                7, 0, 0,    // Far eligible
                0, 2, 0,    // Nearest: 2m vertical separation
                2, 0, 0,    // Same distance: later index must lose
                0, 0, 0.5,  // Nonplayer, closer but ineligible
                11, 0, 0   // Remote outside 10m radius
        });
        kinds.update(new int[]{LOCAL, REMOTE, REMOTE, REMOTE,
                Minecraft189WorldEntityKindState.LIVING, REMOTE});
        Minecraft189NearbyPlayersModule.NearbySnapshot count =
                Minecraft189NearbyPlayersModule.countNearby(
                        local.snapshot(), positions.snapshot(), kinds.snapshot(), 10);
        assertTrue(count.available());
        assertEquals(3, count.count());
        assertEquals(2.0D, count.nearestDistance(), 0.00001D);
        assertEquals(2, count.nearestIndex());

        count = Minecraft189NearbyPlayersModule.countNearby(
                local.snapshot(), positions.snapshot(), kinds.snapshot(), 1);
        assertTrue(count.available());
        assertEquals(0, count.count());
        assertEquals(-1, count.nearestIndex());
        assertEquals(0.0D, count.nearestDistance(), 0.00001D);
        kinds.clear();
        count = Minecraft189NearbyPlayersModule.countNearby(
                local.snapshot(), positions.snapshot(), kinds.snapshot(), 10);
        assertFalse(count.available());
        assertEquals(-1, count.nearestIndex());
    }

    @Test
    void nearestRadarHaloRespectsDistanceAndModes() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules,
                controller, services));
        final RecordingHost host = new RecordingHost();
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(), host);
        try {
            final Minecraft189NearbyPlayersModule radar =
                    runtime.featureCatalog().nearbyPlayers();
            assertFalse(radar.highlightNearestSetting().get().booleanValue());
            radar.showRadarSetting().set(Boolean.TRUE);
            radar.northUpSetting().set(Boolean.TRUE);
            radar.radiusSetting().set(10);
            controller.enable(Minecraft189NearbyPlayersModule.ID);
            runtime.playerPositionState().update(0, 0, 0);
            runtime.worldEntityPositionState().update(new double[]{
                    0, 0, 0,  // Local ignored
                    7, 0, 0,  // Remote farther
                    2, 0, 0,  // Nearest eligible
                    0, 0, 2,  // Same distance: later index loses tie
                    0, 0, 11, // Beyond radius
                    0, 0, 1   // Nonplayer, although closer
            });
            runtime.worldEntityKindState().update(new int[]{
                    LOCAL, REMOTE, REMOTE, REMOTE, REMOTE,
                    Minecraft189WorldEntityKindState.LIVING});
            runtime.renderHud(61L, 0.0F);
            assertEquals(0, host.haloXs.size());
            assertEquals(3, host.markerXs.size());
            assertTrue(host.texts.contains("NEAREST  2.0m"));

            radar.highlightNearestSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NearbyPlayersModule.HIGHLIGHT_NEAREST_SETTING_ID));
            runtime.renderHud(62L, 0.0F);
            assertEquals(1, host.haloXs.size());
            // Default position x=18/y=278, fixed projection of x=2 radius 10.
            assertEquals(18.0F + 78.0F + 9.2F - 4.0F,
                    host.haloXs.get(0), 0.001F);
            assertEquals(278.0F + 96.0F - 4.0F,
                    host.haloYs.get(0), 0.001F);
            assertEquals(0xFFF5F8FF, host.haloColors.get(0).intValue());

            // With yaw unavailable, relative radar must fail closed.
            radar.northUpSetting().set(Boolean.FALSE);
            runtime.renderHud(63L, 0.0F);
            assertEquals(1, host.haloXs.size());
            runtime.playerRotationState().update(90.0F, 0.0F);
            runtime.renderHud(64L, 0.0F);
            assertEquals(2, host.haloXs.size());

            // Reducing the 3D radius removes all players and the halo.
            radar.northUpSetting().set(Boolean.TRUE);
            radar.radiusSetting().set(1);
            runtime.renderHud(65L, 0.0F);
            assertEquals(2, host.haloXs.size());
            assertTrue(host.texts.contains("NEAREST  --"));
            radar.radiusSetting().set(10);
            runtime.worldEntityPositionState().clear();
            runtime.renderHud(66L, 0.0F);
            assertEquals(2, host.haloXs.size());
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189NearbyPlayersModule.HIGHLIGHT_NEAREST_SETTING_ID));
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private final java.util.List<String> texts = new java.util.ArrayList<String>();
        private int roundedRects;
        private float lastExpandedHeight;
        private final java.util.List<Float> markerXs = new java.util.ArrayList<Float>();
        private final java.util.List<Float> markerYs = new java.util.ArrayList<Float>();
        private final java.util.List<Integer> markerColors = new java.util.ArrayList<Integer>();
        private final java.util.List<Float> haloXs = new java.util.ArrayList<Float>();
        private final java.util.List<Float> haloYs = new java.util.ArrayList<Float>();
        private final java.util.List<Integer> haloColors = new java.util.ArrayList<Integer>();
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
        }

        @Override
        public void fillRoundedRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float radius,
                final int argb) {
            if (width == 156.0F) {
                lastCardWidth = width;
                lastCardHeight = height;
                if (height == 152.0F) lastExpandedHeight = height;
            }
            if (width == 4.0F && height == 4.0F) {
                markerXs.add(x);
                markerYs.add(y);
                markerColors.add(argb);
            }
            if (width == 8.0F && height == 8.0F) {
                haloXs.add(x);
                haloYs.add(y);
                haloColors.add(argb);
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
