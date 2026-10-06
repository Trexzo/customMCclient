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

final class Minecraft189ArmorModuleTest {
    @Test
    void armorHudReadsMixedLiveSlotsAndPersistsPosition() {
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
            final Minecraft189ArmorModule armor =
                    runtime.featureCatalog()
                            .armor();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189ArmorModule.ID));

            armor.xSetting().set(40);
            armor.ySetting().set(212);
            runtime.playerArmor(
                    new Minecraft189PlayerArmorAccess() {
                        @Override
                        public boolean customMcArmorBoots() {
                            return true;
                        }

                        @Override
                        public boolean customMcArmorLeggings() {
                            return false;
                        }

                        @Override
                        public boolean customMcArmorChestplate() {
                            return true;
                        }

                        @Override
                        public boolean customMcArmorHelmet() {
                            return true;
                        }
                    });

            controller.enable(
                    Minecraft189ArmorModule.ID);
            assertTrue(
                    armor.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Armor: 3/4 [H C - B]",
                    host.lastText);
            assertEquals(
                    40.0F,
                    host.lastX);
            assertEquals(
                    212.0F,
                    host.lastY);
            assertEquals(
                    "40",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189ArmorModule.X_SETTING_ID));
            assertEquals(
                    "212",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189ArmorModule.Y_SETTING_ID));

            final Minecraft189PlayerArmorState.Snapshot snapshot =
                    runtime.playerArmorState()
                            .snapshot();
            assertEquals(
                    13,
                    snapshot.mask());
            assertEquals(
                    3,
                    snapshot.equippedCount());

            host.lastText = null;
            runtime.playerArmor(null);
            runtime.renderHud(
                    1L,
                    0.0F);
            assertNull(host.lastText);

            controller.disable(
                    Minecraft189ArmorModule.ID);
            assertFalse(
                    armor.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189ArmorModule.ID));
        assertNull(
                settings.find(
                        Minecraft189ArmorModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189ArmorModule.Y_SETTING_ID));
    }

    @Test
    void armorStatePreservesExactSlotIdentity() {
        final Minecraft189PlayerArmorState state =
                new Minecraft189PlayerArmorState();

        state.update(
                false,
                true,
                false,
                true);

        final Minecraft189PlayerArmorState.Snapshot snapshot =
                state.snapshot();
        assertTrue(
                snapshot.available());
        assertEquals(
                Minecraft189PlayerArmorState.LEGGINGS_BIT
                        | Minecraft189PlayerArmorState.HELMET_BIT,
                snapshot.mask());
        assertFalse(
                snapshot.boots());
        assertTrue(
                snapshot.leggings());
        assertFalse(
                snapshot.chestplate());
        assertTrue(
                snapshot.helmet());
        assertEquals(
                2,
                snapshot.equippedCount());

        state.clear();
        assertFalse(
                state.snapshot()
                        .available());
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
