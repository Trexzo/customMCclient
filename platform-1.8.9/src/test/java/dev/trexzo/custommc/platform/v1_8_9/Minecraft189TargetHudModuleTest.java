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
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiRuntime;
import dev.trexzo.custommc.core.ui.clickgui.HudPositionPageContent;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.UiThemeSettings;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiThemes;
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
    void nativeTwoPlayerScreenshotDefaultAvoidsChatWithoutRewritingSavedPosition() {
        // Exact original 854x480 screenshot #12989026500 has a
        // 427x240 logical UI at scale 2. Previous default Y=210
        // clamped a 142x38 card to Y=202: physical top 404,
        // overlapping chat and hotbar. The S32 fresh-profile position
        // is deliberately above the original lower game HUD.
        final UiViewport original =
                new UiViewport(854, 480, 2.0F);
        final float desired = Minecraft189TargetHudModule.DEFAULT_Y;
        assertEquals(68.0F, desired, 0.001F);
        final UiBounds initial = Minecraft189TargetHudModule.visibleCardBounds(
                original, 18.0F, desired, 142.0F, 38.0F);
        assertEquals(68.0F, initial.y(), 0.001F);
        assertTrue(initial.y() + initial.height() < 160.0F);
        // A saved, intentionally moved position remains authoritative:
        // no migration or background rewriting of persisted settings.
        final UiBounds saved = Minecraft189TargetHudModule.visibleCardBounds(
                original, 24.0F, 186.0F, 142.0F, 38.0F);
        assertEquals(186.0F, saved.y(), 0.001F);
        assertEquals(24.0F, saved.x(), 0.001F);
    }

    @Test
    void liveThemeChangesActualNativeTargetHudAccentWithoutChangingSourceData() {
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
            controller.enable(Minecraft189TargetHudModule.ID);
            runtime.playerPositionState().update(0.0D, 0.0D, 0.0D);
            runtime.worldEntityPositionState().update(
                    new double[] {0, 0, 0, 0, 0, 9});
            runtime.worldEntityKindState().update(new int[] {
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
            runtime.renderHud(1L, 0F);
            assertEquals(0xFFEE8755, host.lastAccentColor);
            // The original target identity/distance is unchanged, but
            // hierarchy is quieter and all source text is clipped to the
            // actual 142x38 compact card minus its 12px interior insets.
            assertEquals(1, host.outerStrokes);
            assertEquals(1, host.pushClips);
            assertEquals(1, host.popClips);
            assertEquals(118.0F, host.lastClipWidth, 0.001F);
            assertEquals(30.0F, host.lastClipHeight, 0.001F);
            assertEquals(UiThemes.graphite().color(UiColorRole.TEXT_MUTED),
                    host.textColors.get(0).intValue());
            assertEquals(UiThemes.graphite().color(UiColorRole.TEXT_PRIMARY),
                    host.textColors.get(1).intValue());
            assertTrue(UiThemeSettings.cycle(
                    settings.find(UiThemeSettings.ID), true));
            runtime.renderHud(2L, 0F);
            assertEquals(0xFF5994EA, host.lastAccentColor);
            assertTrue(host.texts.contains("9.0 m  away"));
            assertEquals("blue",
                    settings.snapshotEncoded().get(UiThemeSettings.ID));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(UiThemeSettings.ID));
    }

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
            assertTrue(hud.compactSetting().get().booleanValue());
            assertTrue(hud.proximityMeterSetting().get().booleanValue());
            // Explicit full-size, meter-off legacy acceptance exercised below.
            hud.compactSetting().set(Boolean.FALSE);
            hud.proximityMeterSetting().set(Boolean.FALSE);
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
            assertEquals("NEARBY  02", host.texts.get(0));
            assertEquals("9.0 m  away", host.texts.get(1));
            assertEquals("LOOK  0°  /  0°", host.texts.get(2));
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
            assertEquals("NEARBY  02", host.texts.get(3));
            assertEquals("9.0 m  away", host.texts.get(4));
            assertEquals(4, host.roundedRects);
            assertEquals(142.0F, host.lastCardWidth, 0.001F);
            assertEquals(38.0F, host.lastCardHeight, 0.001F);
            assertEquals(2, host.begins);
            assertEquals(2, host.ends);

            hud.compactSetting().set(Boolean.FALSE);
            runtime.renderHud(22L, 0.0F);
            assertEquals(8, host.texts.size());
            assertEquals("LOOK  0°  /  0°", host.texts.get(7));
            assertEquals(164.0F, host.lastCardWidth, 0.001F);
            assertEquals(56.0F, host.lastCardHeight, 0.001F);

            // M265: optional meter is distance-only, not HP or reach.
            hud.proximityMeterSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.PROXIMITY_METER_SETTING_ID));
            runtime.renderHud(23L, 0.0F);
            assertEquals(2, host.bars.size());
            assertBar(host.bars.get(0), 36.0F, 253.0F,
                    140.0F, 2.0F, 0xFF30343B);
            assertBar(host.bars.get(1), 36.0F, 253.0F,
                    61.25F, 2.0F, 0xFFEE8755);

            // Range beyond 9m -> proportional fill; below 9m -> no fill.
            hud.proximityRangeSetting().set(8);
            host.bars.clear();
            runtime.renderHud(24L, 0.0F);
            assertEquals(1, host.bars.size()); // Empty track only.
            assertBar(host.bars.get(0), 36.0F, 253.0F,
                    140.0F, 2.0F, 0xFF30343B);

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
                    118.0F, 2.0F, 0xFF30343B);
            assertBar(host.bars.get(1), 36.0F, 235.0F,
                    91.45F, 2.0F, 0xFFFFA171);

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
                    200.0F, 2.0F, 0xFF30343B);

            hud.showCoordinatesSetting().set(Boolean.FALSE);
            hud.proximityMeterSetting().set(Boolean.FALSE);
            host.bars.clear();
            final int textsBeforeToggleOff = host.texts.size();
            runtime.renderHud(29L, 0.0F);
            assertEquals(textsBeforeToggleOff + 3, host.texts.size());
            assertEquals(164.0F, host.lastCardWidth, 0.001F);
            assertEquals(56.0F, host.lastCardHeight, 0.001F);
            assertTrue(host.bars.isEmpty()); // Full legacy geometry returns.

            // Offscreen stored coordinates must recover at actual native
            // rendering without silently rewriting the user's settings.
            hud.xSetting().set(4096);
            hud.ySetting().set(4096);
            final int beforeRecoveredFrame = host.texts.size();
            runtime.renderHud(29L, 0.0F);
            assertEquals(beforeRecoveredFrame + 3, host.texts.size());
            assertEquals(1128.0F, host.lastX, 0.001F);
            assertEquals(703.0F, host.lastY, 0.001F);
            assertEquals("4096", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.X_SETTING_ID));
            assertEquals("4096", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.Y_SETTING_ID));
            hud.xSetting().set(24);
            hud.ySetting().set(202);

            // S5: the actual installed ClickGUI path edits the very same
            // TargetHUD settings used by this native-platform render pass.
            final ClickGuiRuntime gui = runtime.clickGuiRuntime().coreRuntime();
            gui.model().open();
            assertTrue(gui.model().select(ClickGuiRuntime.HUD_PAGE_ID));
            final UiViewport editorViewport = new UiViewport(1280, 720, 1.0F);
            final UiBounds editorArea = new ClickGuiLayoutEngine()
                    .layout(editorViewport).content();
            final UiBounds preview = HudPositionPageContent.previewBounds(
                    editorArea, editorViewport);
            assertTrue(preview != null);
            assertTrue(gui.input().pointer(new UiPointerEvent(
                    preview.x() + preview.width() * 0.5F,
                    preview.y() + preview.height() * 0.5F,
                    UiPointerButton.LEFT, UiPointerAction.PRESS),
                    editorViewport));
            assertEquals("558", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.X_SETTING_ID));
            assertEquals("332", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.Y_SETTING_ID));

            // Real platform input adapter: continuous held mouse positions
            // use exactly the same native persistent settings as a press.
            final int moveX = Math.round(
                    preview.x() + preview.width() * 0.75F);
            final int moveBottomY = editorViewport.pixelHeight() - 1
                    - Math.round(preview.y() + preview.height() * 0.6F);
            assertTrue(runtime.pointerMove(moveX, moveBottomY));
            final int draggedX = hud.xSetting().get().intValue();
            final int draggedY = hud.ySetting().get().intValue();
            assertTrue(draggedX > 558);
            assertTrue(draggedY > 332);
            gui.model().close();
            assertFalse(runtime.pointerMove(moveX, moveBottomY));
            final int beforeEditorRender = host.texts.size();
            runtime.renderHud(29L, 0.0F);
            assertEquals(beforeEditorRender + 3, host.texts.size());
            assertEquals(draggedX + 12.0F, host.lastX, 0.001F);
            assertEquals(draggedY + 39.0F, host.lastY, 0.001F);

            gui.model().open();
            final UiBounds reset = HudPositionPageContent.resetBounds(preview);
            assertTrue(gui.input().pointer(new UiPointerEvent(
                    reset.x() + 5.0F, reset.y() + 5.0F,
                    UiPointerButton.LEFT, UiPointerAction.PRESS),
                    editorViewport));
            gui.model().close();
            assertEquals("18", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.X_SETTING_ID));
            assertEquals(String.valueOf(Minecraft189TargetHudModule.DEFAULT_Y),
                    settings.snapshotEncoded().get(
                            Minecraft189TargetHudModule.Y_SETTING_ID));

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
        assertEquals(0xFFEE8755,
                Minecraft189TargetHudModule.proximityFillColor(0.749F));
        assertEquals(0xFFFFA171,
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

    @Test
    void adaptiveAccentUsesConfirmedDistanceOnlyAndPreservesDefaultCyan() {
        assertEquals(0xFFEE8755, Minecraft189TargetHudModule.accentColor(
                0.0D, 16, false));
        assertEquals(0xFFFFA171, Minecraft189TargetHudModule.accentColor(
                4.0D, 16, true)); // Inclusive 75%-fraction boundary.
        assertEquals(0xFFEE8755, Minecraft189TargetHudModule.accentColor(
                4.01D, 16, true));
        assertEquals(0xFFEE8755, Minecraft189TargetHudModule.accentColor(
                Double.NaN, 16, true));
        assertEquals(0xFFEE8755, Minecraft189TargetHudModule.accentColor(
                Double.POSITIVE_INFINITY, 16, true));
        assertEquals(0xFFEE8755, Minecraft189TargetHudModule.accentColor(
                -1.0D, 16, true));
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
            final Minecraft189TargetHudModule hud = runtime.featureCatalog().targetHud();
            assertFalse(hud.adaptiveAccentSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.ADAPTIVE_ACCENT_SETTING_ID));
            controller.enable(Minecraft189TargetHudModule.ID);
            runtime.playerPositionState().update(0.0D, 0.0D, 0.0D);
            runtime.worldEntityPositionState().update(new double[]{
                    0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 9.0D});
            runtime.worldEntityKindState().update(new int[]{
                    Minecraft189WorldEntityKindState.LIVING
                            | Minecraft189WorldEntityKindState.PLAYER
                            | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                    Minecraft189WorldEntityKindState.LIVING
                            | Minecraft189WorldEntityKindState.PLAYER});
            runtime.nearestPlayerTargetState().update(
                    runtime.playerPositionState().snapshot(),
                    runtime.worldEntityPositionState().snapshot(),
                    runtime.worldEntityKindState().snapshot());
            runtime.targetRotationState().update(
                    runtime.playerPositionState().snapshot(),
                    runtime.nearestPlayerTargetState().snapshot());
            runtime.renderHud(0L, 0.0F);
            assertEquals(0xFFEE8755, host.lastAccentColor);
            assertEquals(2, host.roundedRects);

            hud.adaptiveAccentSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189TargetHudModule.ADAPTIVE_ACCENT_SETTING_ID));
            runtime.renderHud(1L, 0.0F); // 9m at 16m range is not close.
            assertEquals(0xFFEE8755, host.lastAccentColor);
            hud.proximityRangeSetting().set(40);
            runtime.renderHud(2L, 0.0F); // 9m <= 10m close threshold.
            assertEquals(0xFFFFA171, host.lastAccentColor);
            assertEquals(6, host.roundedRects);
            hud.proximityRangeSetting().set(8);
            runtime.renderHud(3L, 0.0F);
            assertEquals(0xFFEE8755, host.lastAccentColor);
            hud.adaptiveAccentSetting().set(Boolean.FALSE);
            hud.proximityRangeSetting().set(40);
            runtime.renderHud(4L, 0.0F);
            assertEquals(0xFFEE8755, host.lastAccentColor);
            controller.disable(Minecraft189TargetHudModule.ID);
            final int beforeDisabled = host.roundedRects;
            runtime.renderHud(5L, 0.0F);
            assertEquals(beforeDisabled, host.roundedRects);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189TargetHudModule.ADAPTIVE_ACCENT_SETTING_ID));
        assertNull(modules.find(Minecraft189TargetHudModule.ID));
    }

    @Test
    void targetCardStaysWithinLogicalScreenAcrossUiScaleChanges() {
        final UiViewport desktop = new UiViewport(1280, 720, 1.0F);
        UiBounds normal = Minecraft189TargetHudModule.visibleCardBounds(
                desktop, 24.0F, 202.0F, 164.0F, 56.0F);
        assertEquals(24.0F, normal.x());
        assertEquals(202.0F, normal.y());

        UiBounds rightBottom = Minecraft189TargetHudModule.visibleCardBounds(
                desktop, 4096.0F, 4096.0F, 224.0F, 72.0F);
        assertEquals(1056.0F, rightBottom.x());
        assertEquals(648.0F, rightBottom.y());

        final UiViewport scaled = new UiViewport(1280, 720, 2.0F);
        UiBounds scaledCard = Minecraft189TargetHudModule.visibleCardBounds(
                scaled, 4096.0F, 4096.0F, 224.0F, 72.0F);
        assertEquals(416.0F, scaledCard.x());
        assertEquals(288.0F, scaledCard.y());
        // The render-only projection has no reference to mutable settings.
    }

    @Test
    void targetCardRecoversOnSmallViewportAndRejectsInvalidDimensions() {
        final UiViewport tiny = new UiViewport(120, 80, 1.0F);
        final UiBounds oversized = Minecraft189TargetHudModule.visibleCardBounds(
                tiny, 4096.0F, 4096.0F, 224.0F, 72.0F);
        assertEquals(0.0F, oversized.x());
        assertEquals(8.0F, oversized.y());
        final UiBounds tooTall = Minecraft189TargetHudModule.visibleCardBounds(
                new UiViewport(100, 60, 2.0F),
                -100.0F, 999.0F, 224.0F, 72.0F);
        assertEquals(0.0F, tooTall.x());
        assertEquals(0.0F, tooTall.y());
        assertThrows(IllegalArgumentException.class,
                () -> Minecraft189TargetHudModule.visibleCardBounds(
                        tiny, Float.NaN, 0.0F, 100.0F, 50.0F));
        assertThrows(IllegalArgumentException.class,
                () -> Minecraft189TargetHudModule.visibleCardBounds(
                        tiny, 0.0F, 0.0F, -1.0F, 50.0F));
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private final java.util.List<String> texts = new java.util.ArrayList<String>();
        private final java.util.List<Integer> textColors =
                new java.util.ArrayList<Integer>();
        private final java.util.List<Bar> bars = new java.util.ArrayList<Bar>();
        private int outerStrokes;
        private int pushClips;
        private int popClips;
        private float lastClipWidth;
        private float lastClipHeight;
        private int roundedRects;
        private int lastAccentColor;
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
            if (roundedRects % 2 == 1 && width == 3.0F) {
                lastAccentColor = argb;
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
            outerStrokes++;
        }

        @Override
        public void pushClip(
                final float x,
                final float y,
                final float width,
                final float height) {
            pushClips++;
            lastClipWidth = width;
            lastClipHeight = height;
        }

        @Override
        public void popClip() {
            popClips++;
        }

        @Override
        public void drawText(
                final UiFontHandle font,
                final float x,
                final float y,
                final String text,
                final int argb) {
            texts.add(text);
            textColors.add(Integer.valueOf(argb));
            lastX = x;
            lastY = y;
        }

        @Override
        public void endUi() {
            ends++;
        }
    }
}
