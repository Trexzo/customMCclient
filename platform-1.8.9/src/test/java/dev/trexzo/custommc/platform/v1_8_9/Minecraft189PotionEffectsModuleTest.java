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

final class Minecraft189PotionEffectsModuleTest {
    @Test
    void potionEffectsHudCopiesSortsAndRendersLiveEffects() {
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
            final Minecraft189PotionEffectsModule potionEffects =
                    runtime.featureCatalog()
                            .potionEffects();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189PotionEffectsModule.ID));

            potionEffects.xSetting().set(56);
            potionEffects.ySetting().set(244);

            final Minecraft189PotionEffectAccess regeneration =
                    effect(
                            10,
                            400,
                            0,
                            "potion.regeneration");
            final Minecraft189PotionEffectAccess speed =
                    effect(
                            1,
                            1800,
                            1,
                            "potion.moveSpeed");

            runtime.playerPotionEffects(
                    new Minecraft189PlayerPotionEffectsAccess() {
                        @Override
                        public Minecraft189PotionEffectAccess[] customMcPotionEffects() {
                            return new Minecraft189PotionEffectAccess[]{
                                    regeneration,
                                    speed
                            };
                        }
                    });

            controller.enable(
                    Minecraft189PotionEffectsModule.ID);
            assertTrue(
                    potionEffects.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    Arrays.asList(
                            "potion.moveSpeed Lv 2 1:30",
                            "potion.regeneration Lv 1 0:20"),
                    host.texts);
            assertEquals(
                    Arrays.asList(
                            56.0F,
                            56.0F),
                    host.xs);
            assertEquals(
                    Arrays.asList(
                            244.0F,
                            256.0F),
                    host.ys);
            assertEquals(
                    "56",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189PotionEffectsModule.X_SETTING_ID));
            assertEquals(
                    "244",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189PotionEffectsModule.Y_SETTING_ID));
            assertEquals(Arrays.asList(0xFFFFFFFF, 0xFFFFFFFF), host.colors);
            assertFalse(potionEffects.sortByExpirySetting().get().booleanValue());
            assertFalse(potionEffects.expiryAlertSetting().get().booleanValue());
            assertEquals(Integer.valueOf(10),
                    potionEffects.expiryThresholdSecondsSetting().get());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189PotionEffectsModule.SORT_BY_EXPIRY_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189PotionEffectsModule.EXPIRY_ALERT_SETTING_ID));
            assertEquals("10", settings.snapshotEncoded().get(
                    Minecraft189PotionEffectsModule.EXPIRY_THRESHOLD_SETTING_ID));

            final Minecraft189PlayerPotionEffectsState.StateSnapshot snapshot =
                    runtime.playerPotionEffectsState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertEquals(
                    2,
                    snapshot.effects()
                            .size());
            assertEquals(
                    "potion.moveSpeed",
                    snapshot.effects()
                            .get(0)
                            .effectName());

            // Render-local expiry ordering is stable on equal durations;
            // alert compares exact source ticks, not display-rounded secs.
            potionEffects.sortByExpirySetting().set(Boolean.TRUE);
            potionEffects.expiryAlertSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189PotionEffectsModule.SORT_BY_EXPIRY_SETTING_ID));
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189PotionEffectsModule.EXPIRY_ALERT_SETTING_ID));
            runtime.playerPotionEffects(() -> new Minecraft189PotionEffectAccess[]{
                    effect(10, 400, 0, "potion.regeneration"),
                    effect(1, 1800, 1, "potion.moveSpeed"),
                    effect(5, 201, 0, "potion.blink"),
                    effect(6, 200, 0, "potion.aura"),
                    effect(7, 200, 0, "potion.absorption")
            });
            final Minecraft189PlayerPotionEffectsState.StateSnapshot sortedSource =
                    runtime.playerPotionEffectsState().snapshot();
            assertEquals(Arrays.asList(
                    "potion.absorption", "potion.aura", "potion.blink",
                    "potion.moveSpeed", "potion.regeneration"),
                    names(sortedSource));
            host.clear();
            runtime.renderHud(2L, 0.0F);
            assertEquals(Arrays.asList(
                    "potion.absorption Lv 1 0:10",
                    "potion.aura Lv 1 0:10",
                    "potion.blink Lv 1 0:10",
                    "potion.regeneration Lv 1 0:20",
                    "potion.moveSpeed Lv 2 1:30"), host.texts);
            assertEquals(Arrays.asList(
                    0xFFFFB65C, 0xFFFFB65C, 0xFFFFFFFF,
                    0xFFFFFFFF, 0xFFFFFFFF), host.colors);
            assertEquals(Arrays.asList(
                    244.0F, 256.0F, 268.0F, 280.0F, 292.0F), host.ys);
            // Stable source-state order must remain untouched by HUD sorting.
            assertEquals(Arrays.asList(
                    "potion.absorption", "potion.aura", "potion.blink",
                    "potion.moveSpeed", "potion.regeneration"),
                    names(runtime.playerPotionEffectsState().snapshot()));

            potionEffects.expiryThresholdSecondsSetting().set(11);
            assertEquals("11", settings.snapshotEncoded().get(
                    Minecraft189PotionEffectsModule.EXPIRY_THRESHOLD_SETTING_ID));
            host.clear();
            runtime.renderHud(3L, 0.0F);
            assertEquals(Arrays.asList(
                    0xFFFFB65C, 0xFFFFB65C, 0xFFFFB65C,
                    0xFFFFFFFF, 0xFFFFFFFF), host.colors);
            potionEffects.sortByExpirySetting().set(Boolean.FALSE);
            potionEffects.expiryAlertSetting().set(Boolean.FALSE);
            host.clear();
            runtime.renderHud(4L, 0.0F);
            assertEquals(Arrays.asList(
                    "potion.absorption Lv 1 0:10",
                    "potion.aura Lv 1 0:10",
                    "potion.blink Lv 1 0:10",
                    "potion.moveSpeed Lv 2 1:30",
                    "potion.regeneration Lv 1 0:20"), host.texts);
            assertEquals(Arrays.asList(
                    0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,
                    0xFFFFFFFF, 0xFFFFFFFF), host.colors);

            assertThrows(IllegalArgumentException.class,
                    () -> potionEffects.expiryThresholdSecondsSetting().set(0));
            assertThrows(IllegalArgumentException.class,
                    () -> potionEffects.expiryThresholdSecondsSetting().set(121));

            host.clear();
            runtime.playerPotionEffects(null);
            runtime.renderHud(
                    1L,
                    0.0F);
            assertTrue(
                    host.texts.isEmpty());

            controller.disable(
                    Minecraft189PotionEffectsModule.ID);
            assertFalse(
                    potionEffects.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189PotionEffectsModule.ID));
        assertNull(
                settings.find(
                        Minecraft189PotionEffectsModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189PotionEffectsModule.Y_SETTING_ID));
        assertNull(settings.find(
                Minecraft189PotionEffectsModule.SORT_BY_EXPIRY_SETTING_ID));
        assertNull(settings.find(
                Minecraft189PotionEffectsModule.EXPIRY_ALERT_SETTING_ID));
        assertNull(settings.find(
                Minecraft189PotionEffectsModule.EXPIRY_THRESHOLD_SETTING_ID));
    }

    @Test
    void potionEffectStateRejectsInvalidSnapshots() {
        final Minecraft189PlayerPotionEffectsState state =
                new Minecraft189PlayerPotionEffectsState();

        assertThrows(
                NullPointerException.class,
                () -> state.update(null));
        assertThrows(
                NullPointerException.class,
                () -> state.update(
                        new Minecraft189PotionEffectAccess[]{null}));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        new Minecraft189PotionEffectAccess[]{
                                effect(
                                        1,
                                        -1,
                                        0,
                                        "potion.moveSpeed")
                        }));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        new Minecraft189PotionEffectAccess[]{
                                effect(
                                        1,
                                        20,
                                        -1,
                                        "potion.moveSpeed")
                        }));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        new Minecraft189PotionEffectAccess[]{
                                effect(
                                        1,
                                        20,
                                        0,
                                        " ")
                        }));
    }

    private static List<String> names(
            final Minecraft189PlayerPotionEffectsState.StateSnapshot state) {
        final List<String> names = new ArrayList<String>();
        for (Minecraft189PlayerPotionEffectsState.Snapshot effect : state.effects()) {
            names.add(effect.effectName());
        }
        return names;
    }

    private static Minecraft189PotionEffectAccess effect(
            final int potionId,
            final int durationTicks,
            final int amplifier,
            final String effectName) {
        return new Minecraft189PotionEffectAccess() {
            @Override
            public int customMcPotionId() {
                return potionId;
            }

            @Override
            public int customMcDurationTicks() {
                return durationTicks;
            }

            @Override
            public int customMcAmplifier() {
                return amplifier;
            }

            @Override
            public String customMcEffectName() {
                return effectName;
            }
        };
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private final List<String> texts =
                new ArrayList<String>();
        private final List<Float> xs =
                new ArrayList<Float>();
        private final List<Float> ys =
                new ArrayList<Float>();
        private final List<Integer> colors =
                new ArrayList<Integer>();

        void clear() {
            texts.clear();
            xs.clear();
            ys.clear();
            colors.clear();
        }

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
            texts.add(text);
            xs.add(x);
            ys.add(y);
            colors.add(argb);
        }

        @Override
        public void endUi() {
        }
    }
}
