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

final class Minecraft189HotbarSlotModuleTest {
    @Test
    void hotbarSlotHudReadsLiveSlotAndPersistsPosition() {
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
            final Minecraft189HotbarSlotModule hotbarSlot =
                    runtime.featureCatalog()
                            .hotbarSlot();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189HotbarSlotModule.ID));

            hotbarSlot.xSetting().set(112);
            hotbarSlot.ySetting().set(372);
            runtime.playerHotbarSlot(
                    playerWithSlot(
                            4));

            controller.enable(
                    Minecraft189HotbarSlotModule.ID);
            assertTrue(
                    hotbarSlot.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Slot: 5/9",
                    host.lastText);
            assertEquals(
                    112.0F,
                    host.lastX);
            assertEquals(
                    372.0F,
                    host.lastY);
            assertEquals(
                    "112",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HotbarSlotModule.X_SETTING_ID));
            assertEquals(
                    "372",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HotbarSlotModule.Y_SETTING_ID));

            final Minecraft189HotbarSlotState.Snapshot snapshot =
                    runtime.hotbarSlotState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertEquals(
                    4,
                    snapshot.zeroBasedSlot());
            assertEquals(
                    5,
                    snapshot.displaySlot());

            host.lastText = null;
            runtime.playerHotbarSlot(
                    playerWithoutInventory());
            runtime.renderHud(
                    1L,
                    0.0F);
            assertNull(
                    host.lastText);
            assertFalse(
                    runtime.hotbarSlotState()
                            .snapshot()
                            .available());

            runtime.playerHotbarSlot(
                    playerWithSlot(
                            9));
            assertFalse(
                    runtime.hotbarSlotState()
                            .snapshot()
                            .available());

            controller.disable(
                    Minecraft189HotbarSlotModule.ID);
            assertFalse(
                    hotbarSlot.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189HotbarSlotModule.ID));
        assertNull(
                settings.find(
                        Minecraft189HotbarSlotModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189HotbarSlotModule.Y_SETTING_ID));
    }

    @Test
    void hotbarSlotStateRejectsOutOfRangeSlots() {
        final Minecraft189HotbarSlotState state =
                new Minecraft189HotbarSlotState();

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(-1));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(9));
    }

    @Test
    void optionalHotbarStripDrawsExactlyNineCellsWithOneLiveSelection() {
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
            final Minecraft189HotbarSlotModule hud =
                    runtime.featureCatalog().hotbarSlot();
            assertFalse(hud.showStripSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189HotbarSlotModule.SHOW_STRIP_SETTING_ID));
            hud.xSetting().set(112);
            hud.ySetting().set(372);
            controller.enable(Minecraft189HotbarSlotModule.ID);
            runtime.playerHotbarSlot(playerWithSlot(4));
            runtime.renderHud(0L, 0.0F);
            assertEquals("Slot: 5/9", host.lastText);
            assertEquals(0, host.slotCellsDrawn);

            hud.showStripSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189HotbarSlotModule.SHOW_STRIP_SETTING_ID));
            host.lastText = null;
            runtime.renderHud(1L, 0.0F);
            assertEquals(9, host.slotCellsDrawn);
            assertEquals(1, host.activeSlotCells);
            assertEquals(192.0F, host.activeSlotX, 0.0001F);
            assertEquals(372.0F, host.activeSlotY, 0.0001F);
            assertEquals("9", host.lastText);
            assertEquals(278.0F, host.lastX, 0.0001F);

            runtime.playerHotbarSlot(playerWithSlot(8));
            runtime.renderHud(2L, 0.0F);
            assertEquals(18, host.slotCellsDrawn);
            assertEquals(2, host.activeSlotCells);
            assertEquals(272.0F, host.activeSlotX, 0.0001F);

            runtime.playerHotbarSlot(playerWithoutInventory());
            final int drawnBeforeUnknown = host.slotCellsDrawn;
            runtime.renderHud(3L, 0.0F);
            assertEquals(drawnBeforeUnknown, host.slotCellsDrawn);
            assertEquals(2, host.activeSlotCells);

            hud.showStripSetting().set(Boolean.FALSE);
            runtime.playerHotbarSlot(playerWithSlot(0));
            runtime.renderHud(4L, 0.0F);
            assertEquals("Slot: 1/9", host.lastText);
            assertEquals(18, host.slotCellsDrawn);

            controller.disable(Minecraft189HotbarSlotModule.ID);
            runtime.renderHud(5L, 0.0F);
            assertEquals(18, host.slotCellsDrawn);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189HotbarSlotModule.SHOW_STRIP_SETTING_ID));
        assertNull(modules.find(Minecraft189HotbarSlotModule.ID));
    }

    private static Minecraft189PlayerInventoryAccess playerWithSlot(
            final int slot) {
        return new Minecraft189PlayerInventoryAccess() {
            @Override
            public Minecraft189InventoryHotbarAccess customMcInventory() {
                return new Minecraft189InventoryHotbarAccess() {
                    @Override
                    public int customMcSelectedHotbarSlot() {
                        return slot;
                    }
                };
            }
        };
    }

    private static Minecraft189PlayerInventoryAccess playerWithoutInventory() {
        return new Minecraft189PlayerInventoryAccess() {
            @Override
            public Minecraft189InventoryHotbarAccess customMcInventory() {
                return null;
            }
        };
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private String lastText;
        private float lastX;
        private float lastY;
        private int slotCellsDrawn;
        private int activeSlotCells;
        private float activeSlotX;
        private float activeSlotY;

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
            slotCellsDrawn++;
            if (argb == 0xFF70C9E8) {
                activeSlotCells++;
                activeSlotX = x;
                activeSlotY = y;
            }
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
