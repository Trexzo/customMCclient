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

final class Minecraft189HeldItemModuleTest {
    @Test
    void heldItemHudRendersDurabilityAndStackCountAndPersistsPosition() {
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
            final Minecraft189HeldItemModule held =
                    runtime.featureCatalog()
                            .heldItem();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189HeldItemModule.ID));

            held.xSetting().set(88);
            held.ySetting().set(340);
            runtime.playerHeldItem(
                    playerWith(
                            stack(
                                    " Diamond Sword ",
                                    1,
                                    27,
                                    1561)));

            controller.enable(
                    Minecraft189HeldItemModule.ID);
            assertTrue(
                    held.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Held: Diamond Sword | Dur: 1534/1561",
                    host.lastText);
            assertEquals(
                    88.0F,
                    host.lastX);
            assertEquals(
                    340.0F,
                    host.lastY);
            assertEquals(
                    "88",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HeldItemModule.X_SETTING_ID));
            assertEquals(
                    "340",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HeldItemModule.Y_SETTING_ID));

            runtime.playerHeldItem(
                    playerWith(
                            stack(
                                    "Ender Pearl",
                                    16,
                                    0,
                                    0)));
            runtime.renderHud(
                    1L,
                    0.0F);
            assertEquals(
                    "Held: Ender Pearl x16",
                    host.lastText);

            final Minecraft189HeldItemState.Snapshot snapshot =
                    runtime.heldItemState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertEquals(
                    "Ender Pearl",
                    snapshot.displayName());
            assertEquals(
                    16,
                    snapshot.stackSize());
            assertFalse(
                    snapshot.damageable());

            host.lastText = null;
            runtime.playerHeldItem(
                    playerWith(null));
            runtime.renderHud(
                    2L,
                    0.0F);
            assertNull(
                    host.lastText);
            assertFalse(
                    runtime.heldItemState()
                            .snapshot()
                            .available());

            controller.disable(
                    Minecraft189HeldItemModule.ID);
            assertFalse(
                    held.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189HeldItemModule.ID));
        assertNull(
                settings.find(
                        Minecraft189HeldItemModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189HeldItemModule.Y_SETTING_ID));
    }

    @Test
    void heldItemStateRejectsInvalidValues() {
        final Minecraft189HeldItemState state =
                new Minecraft189HeldItemState();

        assertThrows(
                NullPointerException.class,
                () -> state.update(
                        null,
                        1,
                        0,
                        0));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        "   ",
                        1,
                        0,
                        0));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        "Stone",
                        0,
                        0,
                        0));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        "Sword",
                        1,
                        -1,
                        100));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        "Sword",
                        1,
                        0,
                        -1));

        state.update(
                "Wool",
                16,
                14,
                0);
        assertTrue(
                state.snapshot()
                        .available());
        assertFalse(
                state.snapshot()
                        .damageable());
    }

    private static Minecraft189PlayerHeldItemAccess playerWith(
            final Minecraft189ItemStackAccess held) {
        return new Minecraft189PlayerHeldItemAccess() {
            @Override
            public Minecraft189ItemStackAccess customMcHeldItem() {
                return held;
            }
        };
    }

    private static Minecraft189ItemStackAccess stack(
            final String displayName,
            final int stackSize,
            final int itemDamage,
            final int maxDamage) {
        return new Minecraft189ItemStackAccess() {
            @Override
            public String customMcDisplayName() {
                return displayName;
            }

            @Override
            public int customMcStackSize() {
                return stackSize;
            }

            @Override
            public int customMcItemDamage() {
                return itemDamage;
            }

            @Override
            public int customMcMaxDamage() {
                return maxDamage;
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
