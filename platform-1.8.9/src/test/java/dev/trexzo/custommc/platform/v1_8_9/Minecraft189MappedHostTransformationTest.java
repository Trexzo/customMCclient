package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPass;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189FontRendererAccess;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189MappedHostTransformationTest {
    private static final String GUI_SETTINGS_ACCESS =
            "dev/trexzo/custommc/platform/v1_8_9/ui/"
                    + "Minecraft189GuiSettingsAccess";
    private static final String FONT_RENDERER_ACCESS =
            "dev/trexzo/custommc/platform/v1_8_9/ui/"
                    + "Minecraft189FontRendererAccess";
    private static final String HOST_BINDING =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189LwjglHostBinding";
    private static final String KEYBOARD_BINDING =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189LwjglKeyboardBinding";
    private static final String MOUSE_BINDING =
            "dev/trexzo/custommc/platform/v1_8_9/"
                    + "Minecraft189LwjglMouseBinding";

    @Test
    void transformerClaimsOnlyStableMainAndMappedHostOwners() {
        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();

        assertTrue(
                transformer.handles(
                        Minecraft189ClassTransformer
                                .TARGET_MAIN_CLASS));
        assertTrue(transformer.handles("ave"));
        assertTrue(transformer.handles("avb"));
        assertTrue(transformer.handles("avh"));
        assertTrue(transformer.handles("avn"));
        assertTrue(transformer.handles("avo"));
        assertTrue(transformer.handles("bfk"));
        assertTrue(transformer.handles("pk"));
        assertTrue(transformer.handles("pr"));
        assertTrue(transformer.handles("wn"));
        assertTrue(transformer.handles("bet"));
        assertTrue(transformer.handles("bdc"));
        assertTrue(transformer.handles("bde"));
        assertTrue(transformer.handles("zx"));
        assertTrue(transformer.handles("xg"));
        assertTrue(transformer.handles("pf"));

        assertFalse(
                transformer.handles(
                        "net.minecraft.client.Minecraft"));
    }


    @Test
    void mappedKeyBindingForwardsRawMouseStateBeforeOriginalBody()
            throws Exception {
        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();
        final byte[] transformed =
                transformer.transform(
                        "avb",
                        keyBindingShape());

        assertEquals(
                1,
                mouseForwardCalls(
                        transformed));

        final ByteMapClassLoader loader =
                new ByteMapClassLoader(
                        getClass().getClassLoader());
        loader.put(
                "avb",
                transformed);

        final Class<?> keyBinding =
                loader.loadClass("avb");
        keyBinding.getMethod(
                        "a",
                        int.class,
                        boolean.class)
                .invoke(
                        null,
                        30,
                        true);

        assertEquals(
                1,
                keyBinding.getField("calls")
                        .getInt(null));
    }

    @Test
    void mappedSettingsAndFontBecomeTypedParentOwnedAccessors()
            throws Exception {
        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();

        final byte[] settingsBytes =
                transformer.transform(
                        "avh",
                        gameSettingsShape());
        final byte[] fontBytes =
                transformer.transform(
                        "avn",
                        fontRendererShape());

        assertTrue(
                hasInterface(
                        settingsBytes,
                        GUI_SETTINGS_ACCESS));
        assertTrue(
                hasInterface(
                        fontBytes,
                        FONT_RENDERER_ACCESS));

        final ByteMapClassLoader loader =
                new ByteMapClassLoader(
                        getClass().getClassLoader());
        loader.put("avh", settingsBytes);
        loader.put("avn", fontBytes);

        final Class<?> settingsClass =
                loader.loadClass("avh");
        final Object settings =
                settingsClass.getDeclaredConstructor()
                        .newInstance();
        final Field viewBobbing =
                settingsClass.getField("d");
        final Field fov =
                settingsClass.getField("aI");
        final Field gamma =
                settingsClass.getField("aJ");
        final Field guiScale =
                settingsClass.getField("aL");
        final Field unicode =
                settingsClass.getField("aO");
        viewBobbing.setBoolean(settings, true);
        fov.setFloat(settings, 70.0F);
        gamma.setFloat(settings, 0.35F);
        guiScale.setInt(settings, 3);
        unicode.setBoolean(settings, true);

        final Minecraft189GuiSettingsAccess settingsAccess =
                (Minecraft189GuiSettingsAccess) settings;
        assertTrue(
                settingsAccess.viewBobbing());
        settingsAccess.viewBobbing(false);
        assertFalse(
                viewBobbing.getBoolean(settings));
        assertEquals(
                70.0F,
                settingsAccess.fovSetting());
        settingsAccess.fovSetting(
                95.0F);
        assertEquals(
                95.0F,
                fov.getFloat(settings));
        assertEquals(
                0.35F,
                settingsAccess.gammaSetting());
        settingsAccess.gammaSetting(
                1.25F);
        assertEquals(
                1.25F,
                gamma.getFloat(settings));
        assertEquals(
                3,
                settingsAccess.configuredGuiScale());
        assertTrue(settingsAccess.unicode());

        final Class<?> fontClass =
                loader.loadClass("avn");
        final Minecraft189FontRendererAccess fontAccess =
                (Minecraft189FontRendererAccess)
                        fontClass.getDeclaredConstructor()
                                .newInstance();

        assertEquals(
                91,
                fontAccess.drawString(
                        "hello",
                        1.0F,
                        2.0F,
                        0xFFFFFFFF,
                        false));
    }

    @Test
    void mappedMinecraftStartGameInstallsTheExistingHostBinding()
            throws Exception {
        final Minecraft189ClassTransformer transformer =
                new Minecraft189ClassTransformer();
        final byte[] transformedMinecraft =
                transformer.transform(
                        "ave",
                        minecraftShape());

        assertEquals(
                1,
                hostInstallCalls(
                        transformedMinecraft));
        assertEquals(
                1,
                gameTickCalls(
                        transformedMinecraft));
        assertEquals(
                1,
                keyboardForwardCalls(
                        transformedMinecraft));
        assertEquals(
                1,
                wheelForwardCalls(
                        transformedMinecraft));

        final ByteMapClassLoader loader =
                new ByteMapClassLoader(
                        getClass().getClassLoader());
        loader.put(
                "avh",
                transformer.transform(
                        "avh",
                        gameSettingsShape()));
        loader.put(
                "avl",
                transformer.transform(
                        "avl",
                        timerShape()));
        loader.put(
                "adm",
                transformer.transform(
                        "adm",
                        worldShape()));
        loader.put(
                "bdb",
                worldClientShape());
        loader.put(
                "bda",
                transformer.transform(
                        "bda",
                        playerControllerShape()));
        loader.put(
                "bde",
                transformer.transform(
                        "bde",
                        serverDataShape()));
        loader.put(
                "avn",
                transformer.transform(
                        "avn",
                        fontRendererShape()));
        loader.put(
                "pk",
                transformer.transform(
                        "pk",
                        entityShape()));
        loader.put(
                "zx",
                transformer.transform(
                        "zx",
                        itemStackShape()));
        loader.put(
                "pf",
                transformer.transform(
                        "pf",
                        potionEffectShape()));
        loader.put(
                "pr",
                transformer.transform(
                        "pr",
                        entityLivingBaseShape()));
        loader.put(
                "xg",
                transformer.transform(
                        "xg",
                        foodStatsShape()));
        loader.put(
                "wm",
                transformer.transform(
                        "wm",
                        inventoryPlayerShape()));
        loader.put(
                "wn",
                transformer.transform(
                        "wn",
                        entityPlayerShape()));
        loader.put(
                "bdc",
                transformer.transform(
                        "bdc",
                        networkPlayerInfoShape()));
        loader.put(
                "bet",
                transformer.transform(
                        "bet",
                        abstractClientPlayerShape()));
        loader.put(
                "beu",
                transformer.transform(
                        "beu",
                        movementInputShape()));
        loader.put(
                "bew",
                transformer.transform(
                        "bew",
                        playerShape()));
        loader.put(
                "bfk",
                transformer.transform(
                        "bfk",
                        entityRendererShape()));
        loader.put(
                "avo",
                transformer.transform(
                        "avo",
                        guiIngameShape()));
        loader.put(
                "ave",
                transformedMinecraft);

        final Minecraft189BootstrapRuntime runtime =
                Minecraft189BootstrapRuntime.create(
                        new BootstrapContext(
                                Minecraft189ClassTransformer
                                        .TARGET_MAIN_CLASS,
                                new String[0]));
        try {
            assertFalse(
                    Minecraft189RuntimeBridge
                            .hostInstalled());

            final List<Long> ticks =
                    new ArrayList<Long>();
            runtime.events()
                    .subscribe(
                            Minecraft189Hooks.TickEvent.class,
                            event -> ticks.add(
                                    event.tickIndex()));

            final List<String> hudFrames =
                    new ArrayList<String>();
            final List<String> postFrames =
                    new ArrayList<String>();
            final List<String> renderOrder =
                    new ArrayList<String>();
            runtime.renderPipeline()
                    .register(
                            new RenderPass() {
                                @Override
                                public String id() {
                                    return "m81-test-hud";
                                }

                                @Override
                                public RenderStage stage() {
                                    return RenderStage.HUD;
                                }

                                @Override
                                public int priority() {
                                    return 0;
                                }

                                @Override
                                public void render(
                                        final RenderFrame frame) {
                                    final String value =
                                            frame.frameIndex()
                                                    + "@"
                                                    + frame.partialTicks();
                                    hudFrames.add(value);
                                    renderOrder.add(
                                            "hud:" + value);
                                }
                            });
            runtime.renderPipeline()
                    .register(
                            new RenderPass() {
                                @Override
                                public String id() {
                                    return "m82-test-post";
                                }

                                @Override
                                public RenderStage stage() {
                                    return RenderStage.POST_PROCESS;
                                }

                                @Override
                                public int priority() {
                                    return 0;
                                }

                                @Override
                                public void render(
                                        final RenderFrame frame) {
                                    final String value =
                                            frame.frameIndex()
                                                    + "@"
                                                    + frame.partialTicks();
                                    postFrames.add(value);
                                    renderOrder.add(
                                            "post:" + value);
                                }
                            });

            final Class<?> minecraftClass =
                    loader.loadClass("ave");
            final Object minecraft =
                    minecraftClass.getDeclaredConstructor()
                            .newInstance();
            minecraftClass.getField("ap")
                    .setInt(
                            minecraft,
                            4);
            minecraftClass.getField("ag")
                    .setInt(
                            minecraft,
                            7);

            final Class<?> timerClass =
                    loader.loadClass("avl");
            final Object timer =
                    timerClass.getDeclaredConstructor()
                            .newInstance();
            timerClass.getField("d")
                    .setFloat(
                            timer,
                            1.0F);
            minecraftClass.getField("Y")
                    .set(
                            minecraft,
                            timer);

            final Class<?> playerControllerClass =
                    loader.loadClass("bda");
            final Object playerController =
                    playerControllerClass
                            .getDeclaredConstructor()
                            .newInstance();
            playerControllerClass.getField("g")
                    .setInt(
                            playerController,
                            4);
            playerControllerClass.getField("e")
                    .setFloat(
                            playerController,
                            0.25F);
            playerControllerClass.getField("h")
                    .setBoolean(
                            playerController,
                            true);
            minecraftClass.getField("c")
                    .set(
                            minecraft,
                            playerController);

            final Class<?> playerClass =
                    loader.loadClass("bew");
            final Object player =
                    playerClass.getDeclaredConstructor()
                            .newInstance();
            playerClass.getField("s")
                    .setDouble(
                            player,
                            123.25D);
            playerClass.getField("t")
                    .setDouble(
                            player,
                            64.5D);
            playerClass.getField("u")
                    .setDouble(
                            player,
                            -42.75D);
            playerClass.getField("y")
                    .setFloat(
                            player,
                            91.25F);
            playerClass.getField("am")
                    .setInt(
                            player,
                            -1);
            playerClass.getField("C")
                    .setBoolean(
                            player,
                            false);
            playerClass.getField("S")
                    .setFloat(
                            player,
                            1.25F);
            playerClass.getField("O")
                    .setFloat(
                            player,
                            9.25F);
            playerClass.getField("H")
                    .setBoolean(
                            player,
                            true);
            playerClass.getField("sneaking")
                    .setBoolean(
                            player,
                            true);
            playerClass.getField("sprinting")
                    .setBoolean(
                            player,
                            false);
            playerClass.getField("health")
                    .setFloat(
                            player,
                            17.5F);
            playerClass.getField("maxHealth")
                    .setFloat(
                            player,
                            20.0F);
            playerClass.getField("bB")
                    .setInt(
                            player,
                            27);
            playerClass.getField("bC")
                    .setInt(
                            player,
                            12345);
            playerClass.getField("bD")
                    .setFloat(
                            player,
                            0.5F);

            final Class<?> inventoryClass =
                    loader.loadClass("wm");
            final Object inventory =
                    inventoryClass.getDeclaredConstructor()
                            .newInstance();
            inventoryClass.getField("c")
                    .setInt(
                            inventory,
                            4);
            playerClass.getField("bi")
                    .set(
                            player,
                            inventory);

            final Class<?> playerInfoClass =
                    loader.loadClass("bdc");
            final Object playerInfo =
                    playerInfoClass.getDeclaredConstructor()
                            .newInstance();
            playerInfoClass.getField("responseTime")
                    .setInt(
                            playerInfo,
                            57);
            playerClass.getField("playerInfo")
                    .set(
                            player,
                            playerInfo);

            final Class<?> itemStackClass =
                    loader.loadClass("zx");
            final Object equipmentSlots =
                    java.lang.reflect.Array.newInstance(
                            itemStackClass,
                            5);
            final Object heldItem =
                    itemStackClass.getDeclaredConstructor()
                            .newInstance();
            itemStackClass.getField("b")
                    .setInt(
                            heldItem,
                            1);
            itemStackClass.getField("displayName")
                    .set(
                            heldItem,
                            "Diamond Sword");
            itemStackClass.getField("itemDamage")
                    .setInt(
                            heldItem,
                            27);
            itemStackClass.getField("maxDamage")
                    .setInt(
                            heldItem,
                            1561);
            java.lang.reflect.Array.set(
                    equipmentSlots,
                    0,
                    heldItem);
            final Object bootsItem =
                    itemStackClass.getDeclaredConstructor()
                            .newInstance();
            itemStackClass.getField("itemDamage")
                    .setInt(
                            bootsItem,
                            15);
            itemStackClass.getField("maxDamage")
                    .setInt(
                            bootsItem,
                            195);
            java.lang.reflect.Array.set(
                    equipmentSlots,
                    1,
                    bootsItem);

            final Object chestplateItem =
                    itemStackClass.getDeclaredConstructor()
                            .newInstance();
            itemStackClass.getField("itemDamage")
                    .setInt(
                            chestplateItem,
                            28);
            itemStackClass.getField("maxDamage")
                    .setInt(
                            chestplateItem,
                            528);
            java.lang.reflect.Array.set(
                    equipmentSlots,
                    3,
                    chestplateItem);

            final Object helmetItem =
                    itemStackClass.getDeclaredConstructor()
                            .newInstance();
            itemStackClass.getField("itemDamage")
                    .setInt(
                            helmetItem,
                            10);
            itemStackClass.getField("maxDamage")
                    .setInt(
                            helmetItem,
                            363);
            java.lang.reflect.Array.set(
                    equipmentSlots,
                    4,
                    helmetItem);
            playerClass.getField("equipmentSlots")
                    .set(
                            player,
                            equipmentSlots);

            final Class<?> foodStatsClass =
                    loader.loadClass("xg");
            final Object foodStats =
                    foodStatsClass.getDeclaredConstructor()
                            .newInstance();
            foodStatsClass.getField("foodLevel")
                    .setInt(
                            foodStats,
                            17);
            foodStatsClass.getField("saturationLevel")
                    .setFloat(
                            foodStats,
                            6.5F);
            playerClass.getField("foodStats")
                    .set(
                            player,
                            foodStats);

            final Class<?> potionEffectClass =
                    loader.loadClass("pf");
            final Object speedEffect =
                    potionEffectClass.getDeclaredConstructor()
                            .newInstance();
            potionEffectClass.getField("potionId")
                    .setInt(
                            speedEffect,
                            1);
            potionEffectClass.getField("duration")
                    .setInt(
                            speedEffect,
                            1800);
            potionEffectClass.getField("amplifier")
                    .setInt(
                            speedEffect,
                            1);
            potionEffectClass.getField("effectName")
                    .set(
                            speedEffect,
                            "potion.moveSpeed");

            final Object regenerationEffect =
                    potionEffectClass.getDeclaredConstructor()
                            .newInstance();
            potionEffectClass.getField("potionId")
                    .setInt(
                            regenerationEffect,
                            10);
            potionEffectClass.getField("duration")
                    .setInt(
                            regenerationEffect,
                            400);
            potionEffectClass.getField("amplifier")
                    .setInt(
                            regenerationEffect,
                            0);
            potionEffectClass.getField("effectName")
                    .set(
                            regenerationEffect,
                            "potion.regeneration");

            playerClass.getField("activePotionEffects")
                    .set(
                            player,
                            java.util.Arrays.asList(
                                    speedEffect,
                                    regenerationEffect));

            minecraftClass.getField("h")
                    .set(
                            minecraft,
                            player);

            final Class<?> worldClass =
                    loader.loadClass("bdb");
            final Object world =
                    worldClass.getDeclaredConstructor()
                            .newInstance();
            worldClass.getField("worldTime")
                    .setLong(
                            world,
                            6000L);
            worldClass.getField("raining")
                    .setBoolean(
                            world,
                            true);
            worldClass.getField("thundering")
                    .setBoolean(
                            world,
                            false);
            minecraftClass.getField("f")
                    .set(
                            minecraft,
                            world);

            final Class<?> serverDataClass =
                    loader.loadClass("bde");
            final Object serverData =
                    serverDataClass.getDeclaredConstructor()
                            .newInstance();
            serverDataClass.getField("b")
                    .set(
                            serverData,
                            "play.example.net:25565");
            minecraftClass.getField("Q")
                    .set(
                            minecraft,
                            serverData);

            final Class<?> settingsClass =
                    loader.loadClass("avh");
            final Object liveSettings =
                    settingsClass.getDeclaredConstructor()
                            .newInstance();
            settingsClass.getField("d")
                    .setBoolean(
                            liveSettings,
                            true);
            settingsClass.getField("aI")
                    .setFloat(
                            liveSettings,
                            70.0F);
            settingsClass.getField("aJ")
                    .setFloat(
                            liveSettings,
                            0.35F);
            minecraftClass.getField("t")
                    .set(
                            minecraft,
                            liveSettings);
            minecraftClass.getField("k")
                    .set(
                            minecraft,
                            loader.loadClass("avn")
                                    .getDeclaredConstructor()
                                    .newInstance());

            final Method startGame =
                    minecraftClass.getMethod("am");
            startGame.invoke(minecraft);

            assertTrue(
                    Minecraft189RuntimeBridge
                            .hostInstalled());
            assertTrue(runtime.hostInstalled());
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189FullbrightModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189FovModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189NoBobbingModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189AutoSprintModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189AutoJumpModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189AirJumpModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189StepModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189NoFallModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189NoWebModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189NoClipModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189FlightModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189StrafeModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189GlideModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189FastFallModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189FreezeModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189LongJumpModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189BunnyHopModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189AutoSneakModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189NoSlowModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189FastPlaceModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189FastBreakModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189SpeedMineModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189TimerSpeedModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189NoHitDelayModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189AutoClickerModule.ID)
                            != null);
            assertTrue(
                    runtime.modules()
                            .find(
                                    Minecraft189VelocityModule.ID)
                            != null);
            assertEquals(
                    0.35F,
                    settingsClass.getField("aJ")
                            .getFloat(
                                    liveSettings));

            assertEquals(
                    70.0F,
                    settingsClass.getField("aI")
                            .getFloat(
                                    liveSettings));
            assertTrue(
                    settingsClass.getField("d")
                            .getBoolean(
                                    liveSettings));
            runtime.moduleController()
                    .enable(
                            Minecraft189NoBobbingModule.ID);
            assertFalse(
                    settingsClass.getField("d")
                            .getBoolean(
                                    liveSettings));
            runtime.moduleController()
                    .disable(
                            Minecraft189NoBobbingModule.ID);
            assertTrue(
                    settingsClass.getField("d")
                            .getBoolean(
                                    liveSettings));

            runtime.moduleController()
                    .enable(
                            Minecraft189FovModule.ID);
            assertEquals(
                    110.0F,
                    settingsClass.getField("aI")
                            .getFloat(
                                    liveSettings));
            runtime.requireHostRuntime()
                    .featureCatalog()
                    .fovChanger()
                    .targetFovSetting()
                    .set(120);
            runtime.events()
                    .publish(
                            new Minecraft189Hooks.TickEvent(
                                    99L));
            assertEquals(
                    120.0F,
                    settingsClass.getField("aI")
                            .getFloat(
                                    liveSettings));
            runtime.moduleController()
                    .disable(
                            Minecraft189FovModule.ID);
            assertEquals(
                    70.0F,
                    settingsClass.getField("aI")
                            .getFloat(
                                    liveSettings));
            ticks.clear();

            runtime.moduleController()
                    .enable(
                            Minecraft189FullbrightModule.ID);
            assertEquals(
                    Minecraft189FullbrightModule.fullbrightGamma(),
                    settingsClass.getField("aJ")
                            .getFloat(
                                    liveSettings));

            runtime.moduleController()
                    .disable(
                            Minecraft189FullbrightModule.ID);
            assertEquals(
                    0.35F,
                    settingsClass.getField("aJ")
                            .getFloat(
                                    liveSettings));

            final Method runTick =
                    minecraftClass.getMethod("s");
            runTick.invoke(minecraft);
            runTick.invoke(minecraft);

            assertEquals(
                    Arrays.asList(
                            0L,
                            1L),
                    ticks);
            assertEquals(
                    4,
                    minecraftClass.getField("ap")
                            .getInt(
                                    minecraft));
            assertEquals(
                    7,
                    minecraftClass.getField("ag")
                            .getInt(
                                    minecraft));
            assertEquals(
                    4,
                    playerControllerClass.getField("g")
                            .getInt(
                                    playerController));
            assertEquals(
                    0,
                    minecraftClass.getField("clickMouseCalls")
                            .getInt(
                                    minecraft));
            assertEquals(
                    Minecraft189StepModule.VANILLA_STEP_HEIGHT,
                    playerClass.getField("S")
                            .getFloat(
                                    player),
                    0.000001F);
            assertEquals(
                    9.25F,
                    playerClass.getField("O")
                            .getFloat(
                                    player),
                    0.000001F);
            assertTrue(
                    playerClass.getField("H")
                            .getBoolean(
                                    player));

            final Minecraft189PlayerPositionState.Snapshot position =
                    runtime.requireHostRuntime()
                            .playerPositionState()
                            .snapshot();
            assertTrue(
                    position.available());
            assertEquals(
                    123.25D,
                    position.x());
            assertEquals(
                    64.5D,
                    position.y());
            assertEquals(
                    -42.75D,
                    position.z());
            final Minecraft189MovementSpeedTracker.Snapshot speed =
                    runtime.requireHostRuntime()
                            .movementSpeedTracker()
                            .snapshot();
            assertTrue(
                    speed.available());
            assertEquals(
                    0.0D,
                    speed.blocksPerSecond());

            final Minecraft189PlayerRotationState.Snapshot rotation =
                    runtime.requireHostRuntime()
                            .playerRotationState()
                            .snapshot();
            assertTrue(
                    rotation.available());
            assertEquals(
                    91.25F,
                    rotation.yaw());

            final Minecraft189PlayerDimensionState.Snapshot dimension =
                    runtime.requireHostRuntime()
                            .playerDimensionState()
                            .snapshot();
            assertTrue(
                    dimension.available());
            assertEquals(
                    -1,
                    dimension.dimensionId());

            final Minecraft189PlayerMovementState.Snapshot movement =
                    runtime.requireHostRuntime()
                            .playerMovementState()
                            .snapshot();
            assertTrue(
                    movement.available());
            assertFalse(
                    movement.onGround());
            assertTrue(
                    movement.sneaking());
            assertFalse(
                    movement.sprinting());

            runtime.moduleController()
                    .enable(
                            Minecraft189AutoSprintModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .autoSprint()
                            .active());

            playerClass.getField("sneaking")
                    .setBoolean(
                            player,
                            false);
            playerClass.getField("sprinting")
                    .setBoolean(
                            player,
                            false);
            runTick.invoke(minecraft);
            assertTrue(
                    playerClass.getField("sprinting")
                            .getBoolean(
                                    player));

            playerClass.getField("sneaking")
                    .setBoolean(
                            player,
                            true);
            playerClass.getField("sprinting")
                    .setBoolean(
                            player,
                            false);
            runTick.invoke(minecraft);
            assertFalse(
                    playerClass.getField("sprinting")
                            .getBoolean(
                                    player));

            runtime.moduleController()
                    .disable(
                            Minecraft189AutoSprintModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .autoSprint()
                            .active());

            playerClass.getField("sneaking")
                    .setBoolean(
                            player,
                            false);
            playerClass.getField("sprinting")
                    .setBoolean(
                            player,
                            false);
            runTick.invoke(minecraft);
            assertFalse(
                    playerClass.getField("sprinting")
                            .getBoolean(
                                    player));

            playerClass.getField("sneaking")
                    .setBoolean(
                            player,
                            true);
            playerClass.getField("sprinting")
                    .setBoolean(
                            player,
                            false);

            assertEquals(
                    0,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));
            runtime.moduleController()
                    .enable(
                            Minecraft189AutoJumpModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .autoJump()
                            .active());

            playerClass.getField("C")
                    .setBoolean(
                            player,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    1,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));
            runTick.invoke(minecraft);
            assertEquals(
                    1,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));

            playerClass.getField("C")
                    .setBoolean(
                            player,
                            false);
            runTick.invoke(minecraft);
            playerClass.getField("C")
                    .setBoolean(
                            player,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    2,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));

            runtime.moduleController()
                    .disable(
                            Minecraft189AutoJumpModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .autoJump()
                            .active());

            playerClass.getField("C")
                    .setBoolean(
                            player,
                            false);
            runTick.invoke(minecraft);
            runtime.moduleController()
                    .enable(
                            Minecraft189AirJumpModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .airJump()
                            .active());

            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    3,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));
            runTick.invoke(minecraft);
            assertEquals(
                    3,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));

            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runTick.invoke(minecraft);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    4,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));

            runtime.moduleController()
                    .disable(
                            Minecraft189AirJumpModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .airJump()
                            .active());
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runTick.invoke(minecraft);

            runtime.moduleController()
                    .enable(
                            Minecraft189StepModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .step()
                            .active());
            runtime.requireHostRuntime()
                    .featureCatalog()
                    .step()
                    .heightPercentSetting()
                    .set(175);
            runTick.invoke(minecraft);
            assertEquals(
                    1.75F,
                    playerClass.getField("S")
                            .getFloat(
                                    player),
                    0.000001F);

            runtime.moduleController()
                    .disable(
                            Minecraft189StepModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .step()
                            .active());
            runTick.invoke(minecraft);
            assertEquals(
                    Minecraft189StepModule.VANILLA_STEP_HEIGHT,
                    playerClass.getField("S")
                            .getFloat(
                                    player),
                    0.000001F);

            runtime.moduleController()
                    .enable(
                            Minecraft189NoFallModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noFall()
                            .active());
            playerClass.getField("O")
                    .setFloat(
                            player,
                            6.5F);
            runTick.invoke(minecraft);
            assertEquals(
                    0.0F,
                    playerClass.getField("O")
                            .getFloat(
                                    player),
                    0.000001F);

            runtime.moduleController()
                    .disable(
                            Minecraft189NoFallModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noFall()
                            .active());
            playerClass.getField("O")
                    .setFloat(
                            player,
                            4.25F);
            runTick.invoke(minecraft);
            assertEquals(
                    4.25F,
                    playerClass.getField("O")
                            .getFloat(
                                    player),
                    0.000001F);

            runtime.moduleController()
                    .enable(
                            Minecraft189NoWebModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noWeb()
                            .active());
            playerClass.getField("H")
                    .setBoolean(
                            player,
                            true);
            runTick.invoke(minecraft);
            assertFalse(
                    playerClass.getField("H")
                            .getBoolean(
                                    player));

            runtime.moduleController()
                    .disable(
                            Minecraft189NoWebModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noWeb()
                            .active());
            playerClass.getField("H")
                    .setBoolean(
                            player,
                            true);
            runTick.invoke(minecraft);
            assertTrue(
                    playerClass.getField("H")
                            .getBoolean(
                                    player));

            playerClass.getField("T")
                    .setBoolean(
                            player,
                            false);
            runtime.moduleController()
                    .enable(
                            Minecraft189NoClipModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noClip()
                            .active());
            runTick.invoke(minecraft);
            assertTrue(
                    playerClass.getField("T")
                            .getBoolean(
                                    player));

            runtime.moduleController()
                    .disable(
                            Minecraft189NoClipModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noClip()
                            .restorePending());
            runTick.invoke(minecraft);
            assertFalse(
                    playerClass.getField("T")
                            .getBoolean(
                                    player));
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noClip()
                            .restorePending());

            playerClass.getField("T")
                    .setBoolean(
                            player,
                            true);
            runtime.moduleController()
                    .enable(
                            Minecraft189NoClipModule.ID);
            runTick.invoke(minecraft);
            runtime.moduleController()
                    .disable(
                            Minecraft189NoClipModule.ID);
            runTick.invoke(minecraft);
            assertTrue(
                    playerClass.getField("T")
                            .getBoolean(
                                    player));

            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.LEFT_SHIFT,
                            false);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.RIGHT_SHIFT,
                            false);
            playerClass.getField("w")
                    .setDouble(
                            player,
                            0.42D);
            runtime.moduleController()
                    .enable(
                            Minecraft189FlightModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .flight()
                            .active());
            runTick.invoke(minecraft);
            assertEquals(
                    Minecraft189FlightModule.HOVER_MOTION_Y,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    Minecraft189FlightModule.ASCEND_MOTION_Y,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.LEFT_SHIFT,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    Minecraft189FlightModule.HOVER_MOTION_Y,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runTick.invoke(minecraft);
            assertEquals(
                    Minecraft189FlightModule.DESCEND_MOTION_Y,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.LEFT_SHIFT,
                            false);

            playerClass.getField("y")
                    .setFloat(
                            player,
                            0.0F);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    0.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    Minecraft189FlightModule.HORIZONTAL_MOTION,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.A,
                            true);
            runTick.invoke(minecraft);
            final double flightDiagonal =
                    Minecraft189FlightModule.HORIZONTAL_MOTION
                            / Math.sqrt(2.0D);
            assertEquals(
                    flightDiagonal,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    flightDiagonal,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.A,
                            false);
            playerClass.getField("y")
                    .setFloat(
                            player,
                            90.0F);
            runTick.invoke(minecraft);
            assertEquals(
                    -Minecraft189FlightModule.HORIZONTAL_MOTION,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.0D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);
            runTick.invoke(minecraft);
            assertEquals(
                    0.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.0D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.requireHostRuntime()
                    .featureCatalog()
                    .flight()
                    .horizontalSpeedSetting()
                    .set(
                            0.60D);
            runtime.requireHostRuntime()
                    .featureCatalog()
                    .flight()
                    .verticalSpeedSetting()
                    .set(
                            0.45D);
            playerClass.getField("y")
                    .setFloat(
                            player,
                            0.0F);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    0.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.45D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.60D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);

            runtime.moduleController()
                    .disable(
                            Minecraft189FlightModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .flight()
                            .active());
            playerClass.getField("v")
                    .setDouble(
                            player,
                            0.11D);
            playerClass.getField("w")
                    .setDouble(
                            player,
                            0.42D);
            playerClass.getField("x")
                    .setDouble(
                            player,
                            -0.17D);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    0.11D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.42D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    -0.17D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);

            playerClass.getField("v")
                    .setDouble(
                            player,
                            0.12D);
            playerClass.getField("x")
                    .setDouble(
                            player,
                            0.13D);
            playerClass.getField("y")
                    .setFloat(
                            player,
                            0.0F);
            runtime.requireHostRuntime()
                    .featureCatalog()
                    .strafe()
                    .speedSetting()
                    .set(
                            0.55D);
            runtime.moduleController()
                    .enable(
                            Minecraft189StrafeModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .strafe()
                            .active());
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    0.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.55D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.moduleController()
                    .enable(
                            Minecraft189FlightModule.ID);
            runTick.invoke(minecraft);
            assertEquals(
                    0.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.60D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.moduleController()
                    .disable(
                            Minecraft189FlightModule.ID);
            runTick.invoke(minecraft);
            assertEquals(
                    0.55D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.moduleController()
                    .disable(
                            Minecraft189StrafeModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .strafe()
                            .active());
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);

            playerClass.getField("C")
                    .setBoolean(
                            player,
                            false);
            playerClass.getField("w")
                    .setDouble(
                            player,
                            -0.40D);
            runtime.requireHostRuntime()
                    .featureCatalog()
                    .glide()
                    .fallSpeedSetting()
                    .set(
                            0.15D);
            runtime.moduleController()
                    .enable(
                            Minecraft189GlideModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .glide()
                            .active());
            runTick.invoke(minecraft);
            assertEquals(
                    -0.15D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.requireHostRuntime()
                    .featureCatalog()
                    .fastFall()
                    .fallSpeedSetting()
                    .set(
                            0.45D);
            runtime.moduleController()
                    .enable(
                            Minecraft189FastFallModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .fastFall()
                            .active());
            playerClass.getField("w")
                    .setDouble(
                            player,
                            -0.10D);
            runTick.invoke(minecraft);
            assertEquals(
                    -0.45D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.moduleController()
                    .enable(
                            Minecraft189FlightModule.ID);
            runTick.invoke(minecraft);
            assertEquals(
                    Minecraft189FlightModule.HOVER_MOTION_Y,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.moduleController()
                    .disable(
                            Minecraft189FlightModule.ID);
            playerClass.getField("w")
                    .setDouble(
                            player,
                            -0.10D);
            runTick.invoke(minecraft);
            assertEquals(
                    -0.45D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.moduleController()
                    .disable(
                            Minecraft189FastFallModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .fastFall()
                            .active());
            playerClass.getField("w")
                    .setDouble(
                            player,
                            -0.40D);
            runTick.invoke(minecraft);
            assertEquals(
                    -0.15D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);

            playerClass.getField("C")
                    .setBoolean(
                            player,
                            true);
            playerClass.getField("w")
                    .setDouble(
                            player,
                            -0.40D);
            runTick.invoke(minecraft);
            assertEquals(
                    -0.40D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.moduleController()
                    .disable(
                            Minecraft189GlideModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .glide()
                            .active());

            playerClass.getField("v")
                    .setDouble(
                            player,
                            0.31D);
            playerClass.getField("w")
                    .setDouble(
                            player,
                            -0.27D);
            playerClass.getField("x")
                    .setDouble(
                            player,
                            0.44D);
            playerClass.getField("y")
                    .setFloat(
                            player,
                            0.0F);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runtime.moduleController()
                    .enable(
                            Minecraft189FlightModule.ID);
            runtime.moduleController()
                    .enable(
                            Minecraft189FreezeModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .freeze()
                            .active());
            runTick.invoke(minecraft);
            assertEquals(
                    0.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.0D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.0D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.moduleController()
                    .disable(
                            Minecraft189FreezeModule.ID);
            runTick.invoke(minecraft);
            assertEquals(
                    0.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.60D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);
            runtime.moduleController()
                    .disable(
                            Minecraft189FlightModule.ID);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);

            runtime.requireHostRuntime()
                    .featureCatalog()
                    .longJump()
                    .speedSetting()
                    .set(
                            0.85D);
            runtime.moduleController()
                    .enable(
                            Minecraft189LongJumpModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .longJump()
                            .active());
            playerClass.getField("C")
                    .setBoolean(
                            player,
                            true);
            playerClass.getField("y")
                    .setFloat(
                            player,
                            0.0F);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runTick.invoke(minecraft);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    5,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));
            assertEquals(
                    0.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.85D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);
            runTick.invoke(minecraft);
            assertEquals(
                    5,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));
            runtime.moduleController()
                    .disable(
                            Minecraft189LongJumpModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .longJump()
                            .active());
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);

            runtime.requireHostRuntime()
                    .featureCatalog()
                    .bunnyHop()
                    .speedSetting()
                    .set(
                            0.55D);
            runtime.moduleController()
                    .enable(
                            Minecraft189BunnyHopModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .bunnyHop()
                            .active());
            playerClass.getField("C")
                    .setBoolean(
                            player,
                            true);
            playerClass.getField("y")
                    .setFloat(
                            player,
                            0.0F);
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    6,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));
            assertEquals(
                    0.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    0.55D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);
            runTick.invoke(minecraft);
            assertEquals(
                    6,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));
            playerClass.getField("C")
                    .setBoolean(
                            player,
                            false);
            runTick.invoke(minecraft);
            playerClass.getField("C")
                    .setBoolean(
                            player,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    7,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));
            runtime.moduleController()
                    .disable(
                            Minecraft189BunnyHopModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .bunnyHop()
                            .active());
            runtime.requireHostRuntime()
                    .inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);

            playerClass.getField("C")
                    .setBoolean(
                            player,
                            false);
            runTick.invoke(minecraft);
            playerClass.getField("C")
                    .setBoolean(
                            player,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    7,
                    playerClass.getField("jumpCalls")
                            .getInt(
                                    player));

            playerClass.getField("sneaking")
                    .setBoolean(
                            player,
                            false);
            runtime.moduleController()
                    .enable(
                            Minecraft189AutoSneakModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .autoSneak()
                            .active());
            runTick.invoke(minecraft);
            assertTrue(
                    playerClass.getField("sneaking")
                            .getBoolean(
                                    player));
            runTick.invoke(minecraft);
            assertTrue(
                    playerClass.getField("sneaking")
                            .getBoolean(
                                    player));

            runtime.moduleController()
                    .disable(
                            Minecraft189AutoSneakModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .autoSneak()
                            .active());
            playerClass.getField("sneaking")
                    .setBoolean(
                            player,
                            false);
            runTick.invoke(minecraft);
            assertFalse(
                    playerClass.getField("sneaking")
                            .getBoolean(
                                    player));

            final Class<?> movementInputClass =
                    loader.loadClass("beu");
            final Object movementInput =
                    movementInputClass
                            .getDeclaredConstructor()
                            .newInstance();
            playerClass.getField("b")
                    .set(
                            player,
                            movementInput);
            final Method onLivingUpdate =
                    playerClass.getMethod("m");

            movementInputClass.getField("a")
                    .setFloat(
                            movementInput,
                            1.0F);
            movementInputClass.getField("b")
                    .setFloat(
                            movementInput,
                            -0.75F);
            onLivingUpdate.invoke(player);
            assertEquals(
                    0.2F,
                    movementInputClass.getField("a")
                            .getFloat(
                                    movementInput),
                    0.000001F);
            assertEquals(
                    -0.15F,
                    movementInputClass.getField("b")
                            .getFloat(
                                    movementInput),
                    0.000001F);

            runtime.moduleController()
                    .enable(
                            Minecraft189NoSlowModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noSlow()
                            .active());

            movementInputClass.getField("a")
                    .setFloat(
                            movementInput,
                            1.0F);
            movementInputClass.getField("b")
                    .setFloat(
                            movementInput,
                            -0.75F);
            onLivingUpdate.invoke(player);
            assertEquals(
                    1.0F,
                    movementInputClass.getField("a")
                            .getFloat(
                                    movementInput),
                    0.000001F);
            assertEquals(
                    -0.75F,
                    movementInputClass.getField("b")
                            .getFloat(
                                    movementInput),
                    0.000001F);

            runtime.moduleController()
                    .disable(
                            Minecraft189NoSlowModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noSlow()
                            .active());

            movementInputClass.getField("a")
                    .setFloat(
                            movementInput,
                            1.0F);
            movementInputClass.getField("b")
                    .setFloat(
                            movementInput,
                            -0.75F);
            onLivingUpdate.invoke(player);
            assertEquals(
                    0.2F,
                    movementInputClass.getField("a")
                            .getFloat(
                                    movementInput),
                    0.000001F);
            assertEquals(
                    -0.15F,
                    movementInputClass.getField("b")
                            .getFloat(
                                    movementInput),
                    0.000001F);

            runtime.moduleController()
                    .enable(
                            Minecraft189FastPlaceModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .fastPlace()
                            .active());

            minecraftClass.getField("ap")
                    .setInt(
                            minecraft,
                            4);
            runTick.invoke(minecraft);
            assertEquals(
                    0,
                    minecraftClass.getField("ap")
                            .getInt(
                                    minecraft));

            runtime.requireHostRuntime()
                    .featureCatalog()
                    .fastPlace()
                    .delayTicksSetting()
                    .set(2);
            minecraftClass.getField("ap")
                    .setInt(
                            minecraft,
                            4);
            runTick.invoke(minecraft);
            assertEquals(
                    2,
                    minecraftClass.getField("ap")
                            .getInt(
                                    minecraft));

            minecraftClass.getField("ap")
                    .setInt(
                            minecraft,
                            1);
            runTick.invoke(minecraft);
            assertEquals(
                    1,
                    minecraftClass.getField("ap")
                            .getInt(
                                    minecraft));

            runtime.moduleController()
                    .disable(
                            Minecraft189FastPlaceModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .fastPlace()
                            .active());
            minecraftClass.getField("ap")
                    .setInt(
                            minecraft,
                            4);
            runTick.invoke(minecraft);
            assertEquals(
                    4,
                    minecraftClass.getField("ap")
                            .getInt(
                                    minecraft));

            runtime.moduleController()
                    .enable(
                            Minecraft189FastBreakModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .fastBreak()
                            .active());

            playerControllerClass.getField("g")
                    .setInt(
                            playerController,
                            4);
            runTick.invoke(minecraft);
            assertEquals(
                    0,
                    playerControllerClass.getField("g")
                            .getInt(
                                    playerController));

            playerControllerClass.getField("g")
                    .setInt(
                            playerController,
                            3);
            runTick.invoke(minecraft);
            assertEquals(
                    0,
                    playerControllerClass.getField("g")
                            .getInt(
                                    playerController));

            runtime.moduleController()
                    .disable(
                            Minecraft189FastBreakModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .fastBreak()
                            .active());
            playerControllerClass.getField("g")
                    .setInt(
                            playerController,
                            4);
            runTick.invoke(minecraft);
            assertEquals(
                    4,
                    playerControllerClass.getField("g")
                            .getInt(
                                    playerController));

            playerControllerClass.getField("h")
                    .setBoolean(
                            playerController,
                            true);
            playerControllerClass.getField("e")
                    .setFloat(
                            playerController,
                            0.25F);
            runTick.invoke(minecraft);
            assertEquals(
                    0.25F,
                    playerControllerClass.getField("e")
                            .getFloat(
                                    playerController),
                    0.000001F);

            runtime.moduleController()
                    .enable(
                            Minecraft189SpeedMineModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .speedMine()
                            .active());

            playerControllerClass.getField("h")
                    .setBoolean(
                            playerController,
                            true);
            playerControllerClass.getField("e")
                    .setFloat(
                            playerController,
                            0.25F);
            runTick.invoke(minecraft);
            assertEquals(
                    0.70F,
                    playerControllerClass.getField("e")
                            .getFloat(
                                    playerController),
                    0.000001F);

            playerControllerClass.getField("e")
                    .setFloat(
                            playerController,
                            0.90F);
            runTick.invoke(minecraft);
            assertEquals(
                    0.90F,
                    playerControllerClass.getField("e")
                            .getFloat(
                                    playerController),
                    0.000001F);

            playerControllerClass.getField("h")
                    .setBoolean(
                            playerController,
                            false);
            playerControllerClass.getField("e")
                    .setFloat(
                            playerController,
                            0.10F);
            runTick.invoke(minecraft);
            assertEquals(
                    0.10F,
                    playerControllerClass.getField("e")
                            .getFloat(
                                    playerController),
                    0.000001F);

            runtime.requireHostRuntime()
                    .featureCatalog()
                    .speedMine()
                    .progressPercentSetting()
                    .set(90);
            playerControllerClass.getField("h")
                    .setBoolean(
                            playerController,
                            true);
            playerControllerClass.getField("e")
                    .setFloat(
                            playerController,
                            0.20F);
            runTick.invoke(minecraft);
            assertEquals(
                    0.90F,
                    playerControllerClass.getField("e")
                            .getFloat(
                                    playerController),
                    0.000001F);

            runtime.moduleController()
                    .disable(
                            Minecraft189SpeedMineModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .speedMine()
                            .active());
            playerControllerClass.getField("e")
                    .setFloat(
                            playerController,
                            0.20F);
            runTick.invoke(minecraft);
            assertEquals(
                    0.20F,
                    playerControllerClass.getField("e")
                            .getFloat(
                                    playerController),
                    0.000001F);

            timerClass.getField("d")
                    .setFloat(
                            timer,
                            1.25F);
            runTick.invoke(minecraft);
            assertEquals(
                    1.0F,
                    timerClass.getField("d")
                            .getFloat(
                                    timer),
                    0.000001F);

            runtime.moduleController()
                    .enable(
                            Minecraft189TimerSpeedModule.ID);
            runtime.requireHostRuntime()
                    .featureCatalog()
                    .timerSpeed()
                    .speedPercentSetting()
                    .set(150);
            runTick.invoke(minecraft);
            assertEquals(
                    1.5F,
                    timerClass.getField("d")
                            .getFloat(
                                    timer),
                    0.000001F);

            runtime.requireHostRuntime()
                    .featureCatalog()
                    .timerSpeed()
                    .speedPercentSetting()
                    .set(50);
            runTick.invoke(minecraft);
            assertEquals(
                    0.5F,
                    timerClass.getField("d")
                            .getFloat(
                                    timer),
                    0.000001F);

            runtime.moduleController()
                    .disable(
                            Minecraft189TimerSpeedModule.ID);
            runTick.invoke(minecraft);
            assertEquals(
                    1.0F,
                    timerClass.getField("d")
                            .getFloat(
                                    timer),
                    0.000001F);

            runtime.moduleController()
                    .enable(
                            Minecraft189NoHitDelayModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noHitDelay()
                            .active());
            minecraftClass.getField("ag")
                    .setInt(
                            minecraft,
                            7);
            runTick.invoke(minecraft);
            assertEquals(
                    0,
                    minecraftClass.getField("ag")
                            .getInt(
                                    minecraft));

            minecraftClass.getField("ag")
                    .setInt(
                            minecraft,
                            -1);
            runTick.invoke(minecraft);
            assertEquals(
                    -1,
                    minecraftClass.getField("ag")
                            .getInt(
                                    minecraft));

            runtime.moduleController()
                    .disable(
                            Minecraft189NoHitDelayModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .noHitDelay()
                            .active());
            minecraftClass.getField("ag")
                    .setInt(
                            minecraft,
                            7);
            runTick.invoke(minecraft);
            assertEquals(
                    7,
                    minecraftClass.getField("ag")
                            .getInt(
                                    minecraft));

            runtime.requireHostRuntime()
                    .featureCatalog()
                    .autoClicker()
                    .minCpsSetting()
                    .set(10);
            runtime.requireHostRuntime()
                    .featureCatalog()
                    .autoClicker()
                    .maxCpsSetting()
                    .set(10);
            runtime.moduleController()
                    .enable(
                            Minecraft189AutoClickerModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .autoClicker()
                            .active());

            runTick.invoke(minecraft);
            assertEquals(
                    0,
                    minecraftClass.getField("clickMouseCalls")
                            .getInt(
                                    minecraft));

            runtime.requireHostRuntime()
                    .inputState()
                    .pointerButton(
                            Minecraft189ClickRateTracker.LEFT_BUTTON,
                            true);
            runTick.invoke(minecraft);
            assertEquals(
                    0,
                    minecraftClass.getField("clickMouseCalls")
                            .getInt(
                                    minecraft));
            runTick.invoke(minecraft);
            assertEquals(
                    1,
                    minecraftClass.getField("clickMouseCalls")
                            .getInt(
                                    minecraft));

            runtime.requireHostRuntime()
                    .inputState()
                    .pointerButton(
                            Minecraft189ClickRateTracker.LEFT_BUTTON,
                            false);
            runTick.invoke(minecraft);
            runTick.invoke(minecraft);
            assertEquals(
                    1,
                    minecraftClass.getField("clickMouseCalls")
                            .getInt(
                                    minecraft));

            runtime.moduleController()
                    .disable(
                            Minecraft189AutoClickerModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .autoClicker()
                            .active());
            runtime.requireHostRuntime()
                    .inputState()
                    .pointerButton(
                            Minecraft189ClickRateTracker.LEFT_BUTTON,
                            true);
            runTick.invoke(minecraft);
            runTick.invoke(minecraft);
            assertEquals(
                    1,
                    minecraftClass.getField("clickMouseCalls")
                            .getInt(
                                    minecraft));
            runtime.requireHostRuntime()
                    .inputState()
                    .pointerButton(
                            Minecraft189ClickRateTracker.LEFT_BUTTON,
                            false);

            final Class<?> entityClass =
                    loader.loadClass("pk");
            final Method knockBack =
                    playerClass.getMethod(
                            "a",
                            entityClass,
                            Float.TYPE,
                            Double.TYPE,
                            Double.TYPE);

            playerClass.getField("v")
                    .setDouble(
                            player,
                            2.0D);
            playerClass.getField("w")
                    .setDouble(
                            player,
                            3.0D);
            playerClass.getField("x")
                    .setDouble(
                            player,
                            4.0D);
            knockBack.invoke(
                    player,
                    player,
                    Float.valueOf(0.4F),
                    Double.valueOf(1.0D),
                    Double.valueOf(-2.0D));
            assertEquals(
                    3.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    3.4D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    2.0D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.moduleController()
                    .enable(
                            Minecraft189VelocityModule.ID);
            assertTrue(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .velocity()
                            .active());

            playerClass.getField("v")
                    .setDouble(
                            player,
                            2.0D);
            playerClass.getField("w")
                    .setDouble(
                            player,
                            3.0D);
            playerClass.getField("x")
                    .setDouble(
                            player,
                            4.0D);
            knockBack.invoke(
                    player,
                    player,
                    Float.valueOf(0.4F),
                    Double.valueOf(1.0D),
                    Double.valueOf(-2.0D));
            assertEquals(
                    2.0D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    3.0D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    4.0D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.requireHostRuntime()
                    .featureCatalog()
                    .velocity()
                    .horizontalPercentSetting()
                    .set(50);
            runtime.requireHostRuntime()
                    .featureCatalog()
                    .velocity()
                    .verticalPercentSetting()
                    .set(25);
            playerClass.getField("v")
                    .setDouble(
                            player,
                            2.0D);
            playerClass.getField("w")
                    .setDouble(
                            player,
                            3.0D);
            playerClass.getField("x")
                    .setDouble(
                            player,
                            4.0D);
            knockBack.invoke(
                    player,
                    player,
                    Float.valueOf(0.4F),
                    Double.valueOf(1.0D),
                    Double.valueOf(-2.0D));
            assertEquals(
                    2.5D,
                    playerClass.getField("v")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    3.1D,
                    playerClass.getField("w")
                            .getDouble(
                                    player),
                    0.000001D);
            assertEquals(
                    3.0D,
                    playerClass.getField("x")
                            .getDouble(
                                    player),
                    0.000001D);

            runtime.moduleController()
                    .disable(
                            Minecraft189VelocityModule.ID);
            assertFalse(
                    runtime.requireHostRuntime()
                            .featureCatalog()
                            .velocity()
                            .active());

            final Minecraft189PlayerHealthState.Snapshot health =
                    runtime.requireHostRuntime()
                            .playerHealthState()
                            .snapshot();
            assertTrue(
                    health.available());
            assertEquals(
                    17.5F,
                    health.health());
            assertEquals(
                    20.0F,
                    health.maxHealth());

            final Minecraft189PlayerArmorState.Snapshot armor =
                    runtime.requireHostRuntime()
                            .playerArmorState()
                            .snapshot();
            assertTrue(
                    armor.available());
            assertEquals(
                    13,
                    armor.mask());
            assertEquals(
                    3,
                    armor.equippedCount());
            assertTrue(
                    armor.helmet());
            assertTrue(
                    armor.chestplate());
            assertFalse(
                    armor.leggings());
            assertTrue(
                    armor.boots());
            assertTrue(
                    armor.hasDurabilityDetails());
            assertEquals(
                    353,
                    armor.helmetDurability()
                            .durabilityRemaining());
            assertEquals(
                    500,
                    armor.chestplateDurability()
                            .durabilityRemaining());
            assertFalse(
                    armor.leggingsDurability()
                            .available());
            assertEquals(
                    180,
                    armor.bootsDurability()
                            .durabilityRemaining());

            final Minecraft189PlayerHungerState.Snapshot hunger =
                    runtime.requireHostRuntime()
                            .playerHungerState()
                            .snapshot();
            assertTrue(
                    hunger.available());
            assertEquals(
                    17,
                    hunger.foodLevel());
            assertEquals(
                    6.5F,
                    hunger.saturationLevel());

            final Minecraft189PlayerPotionEffectsState.StateSnapshot potionEffects =
                    runtime.requireHostRuntime()
                            .playerPotionEffectsState()
                            .snapshot();
            assertTrue(
                    potionEffects.available());
            assertEquals(
                    2,
                    potionEffects.effects()
                            .size());
            assertEquals(
                    "potion.moveSpeed",
                    potionEffects.effects()
                            .get(0)
                            .effectName());
            assertEquals(
                    1,
                    potionEffects.effects()
                            .get(0)
                            .potionId());
            assertEquals(
                    1800,
                    potionEffects.effects()
                            .get(0)
                            .durationTicks());
            assertEquals(
                    1,
                    potionEffects.effects()
                            .get(0)
                            .amplifier());
            assertEquals(
                    "potion.regeneration",
                    potionEffects.effects()
                            .get(1)
                            .effectName());

            final Minecraft189PlayerExperienceState.Snapshot experience =
                    runtime.requireHostRuntime()
                            .playerExperienceState()
                            .snapshot();
            assertTrue(
                    experience.available());
            assertEquals(
                    27,
                    experience.level());
            assertEquals(
                    12345,
                    experience.total());
            assertEquals(
                    0.5F,
                    experience.progress());
            assertEquals(
                    42,
                    experience.barCap());
            assertEquals(
                    21,
                    experience.progressPoints());

            final Minecraft189PlayerPingState.Snapshot ping =
                    runtime.requireHostRuntime()
                            .playerPingState()
                            .snapshot();
            assertTrue(
                    ping.available());
            assertEquals(
                    57,
                    ping.milliseconds());

            final Minecraft189HotbarSlotState.Snapshot hotbarSlot =
                    runtime.requireHostRuntime()
                            .hotbarSlotState()
                            .snapshot();
            assertTrue(
                    hotbarSlot.available());
            assertEquals(
                    4,
                    hotbarSlot.zeroBasedSlot());
            assertEquals(
                    5,
                    hotbarSlot.displaySlot());

            final Minecraft189WorldTimeState.Snapshot worldTime =
                    runtime.requireHostRuntime()
                            .worldTimeState()
                            .snapshot();
            assertTrue(
                    worldTime.available());
            assertEquals(
                    6000L,
                    worldTime.worldTime());
            assertEquals(
                    6000L,
                    worldTime.timeOfDayTicks());
            assertEquals(
                    12,
                    worldTime.hour());
            assertEquals(
                    0,
                    worldTime.minute());

            final Minecraft189WorldWeatherState.Snapshot weather =
                    runtime.requireHostRuntime()
                            .worldWeatherState()
                            .snapshot();
            assertTrue(
                    weather.available());
            assertTrue(
                    weather.raining());
            assertFalse(
                    weather.thundering());

            final Minecraft189ServerAddressState.Snapshot server =
                    runtime.requireHostRuntime()
                            .serverAddressState()
                            .snapshot();
            assertTrue(
                    server.available());
            assertEquals(
                    "play.example.net:25565",
                    server.address());

            final Minecraft189HeldItemState.Snapshot held =
                    runtime.requireHostRuntime()
                            .heldItemState()
                            .snapshot();
            assertTrue(
                    held.available());
            assertEquals(
                    "Diamond Sword",
                    held.displayName());
            assertEquals(
                    1,
                    held.stackSize());
            assertEquals(
                    27,
                    held.itemDamage());
            assertEquals(
                    1561,
                    held.maxDamage());
            assertEquals(
                    1534,
                    held.durabilityRemaining());

            java.lang.reflect.Array.set(
                    equipmentSlots,
                    0,
                    null);
            runTick.invoke(minecraft);
            assertFalse(
                    runtime.requireHostRuntime()
                            .heldItemState()
                            .snapshot()
                            .available());

            playerClass.getField("bi")
                    .set(
                            player,
                            null);
            runTick.invoke(minecraft);
            assertFalse(
                    runtime.requireHostRuntime()
                            .hotbarSlotState()
                            .snapshot()
                            .available());

            playerClass.getField("playerInfo")
                    .set(
                            player,
                            null);
            runTick.invoke(minecraft);
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerPingState()
                            .snapshot()
                            .available());

            minecraftClass.getField("f")
                    .set(
                            minecraft,
                            null);
            runTick.invoke(minecraft);
            assertFalse(
                    runtime.requireHostRuntime()
                            .worldTimeState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .worldWeatherState()
                            .snapshot()
                            .available());

            minecraftClass.getField("Q")
                    .set(
                            minecraft,
                            null);
            runTick.invoke(minecraft);
            assertFalse(
                    runtime.requireHostRuntime()
                            .serverAddressState()
                            .snapshot()
                            .available());

            minecraftClass.getField("h")
                    .set(
                            minecraft,
                            null);
            runTick.invoke(minecraft);
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerPositionState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .movementSpeedTracker()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerRotationState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerDimensionState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerMovementState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerHealthState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerArmorState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerHungerState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerPotionEffectsState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerExperienceState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .playerPingState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .serverAddressState()
                            .snapshot()
                            .available());
            assertFalse(
                    runtime.requireHostRuntime()
                            .heldItemState()
                            .snapshot()
                            .available());

            final Object entityRenderer =
                    loader.loadClass("bfk")
                            .getDeclaredConstructor()
                            .newInstance();
            final Method renderFrame =
                    entityRenderer.getClass()
                            .getMethod(
                                    "a",
                                    float.class,
                                    long.class);

            renderFrame.invoke(
                    entityRenderer,
                    0.25F,
                    100L);
            renderFrame.invoke(
                    entityRenderer,
                    0.5F,
                    101L);

            assertEquals(
                    Arrays.asList(
                            "0@0.25",
                            "1@0.5"),
                    hudFrames);
            assertEquals(
                    Arrays.asList(
                            "0@0.25",
                            "1@0.5"),
                    postFrames);
            assertEquals(
                    Arrays.asList(
                            "hud:0@0.25",
                            "post:0@0.25",
                            "hud:1@0.5",
                            "post:1@0.5"),
                    renderOrder);
            assertTrue(
                    runtime.requireHostRuntime()
                            .frameRateTracker()
                            .framesPerSecond()
                            >= 1);
        } finally {
            runtime.close();
        }

        assertFalse(
                Minecraft189RuntimeBridge.active());
    }

    private static boolean hasInterface(
            final byte[] bytes,
            final String expected) {
        final boolean[] found =
                new boolean[]{false};
        new ClassReader(bytes)
                .accept(
                        new ClassVisitor(
                                Opcodes.ASM9) {
                            @Override
                            public void visit(
                                    final int version,
                                    final int access,
                                    final String name,
                                    final String signature,
                                    final String superName,
                                    final String[] interfaces) {
                                if (interfaces != null) {
                                    for (String current : interfaces) {
                                        if (expected.equals(current)) {
                                            found[0] = true;
                                        }
                                    }
                                }
                            }
                        },
                        ClassReader.SKIP_CODE
                                | ClassReader.SKIP_DEBUG
                                | ClassReader.SKIP_FRAMES);
        return found[0];
    }

    private static int hostInstallCalls(
            final byte[] bytes) {
        final int[] calls =
                new int[]{0};
        new ClassReader(bytes)
                .accept(
                        new ClassVisitor(
                                Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(
                                    final int access,
                                    final String name,
                                    final String descriptor,
                                    final String signature,
                                    final String[] exceptions) {
                                if (!"am".equals(name)
                                        || !"()V".equals(
                                        descriptor)) {
                                    return null;
                                }
                                return new MethodVisitor(
                                        Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            final int opcode,
                                            final String owner,
                                            final String methodName,
                                            final String methodDescriptor,
                                            final boolean isInterface) {
                                        if (opcode
                                                == Opcodes.INVOKESTATIC
                                                && HOST_BINDING.equals(
                                                owner)
                                                && "install".equals(
                                                methodName)) {
                                            calls[0]++;
                                        }
                                    }
                                };
                            }
                        },
                        0);
        return calls[0];
    }

    private static int gameTickCalls(
            final byte[] bytes) {
        final int[] calls =
                new int[]{0};
        new ClassReader(bytes)
                .accept(
                        new ClassVisitor(
                                Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(
                                    final int access,
                                    final String name,
                                    final String descriptor,
                                    final String signature,
                                    final String[] exceptions) {
                                if (!"s".equals(name)
                                        || !"()V".equals(
                                        descriptor)) {
                                    return null;
                                }
                                return new MethodVisitor(
                                        Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            final int opcode,
                                            final String owner,
                                            final String methodName,
                                            final String methodDescriptor,
                                            final boolean isInterface) {
                                        if (opcode
                                                == Opcodes.INVOKESTATIC
                                                && "dev/trexzo/custommc/platform/v1_8_9/Minecraft189RuntimeBridge"
                                                .equals(owner)
                                                && "gameTick".equals(
                                                methodName)
                                                && "()V".equals(
                                                methodDescriptor)) {
                                            calls[0]++;
                                        }
                                    }
                                };
                            }
                        },
                        0);
        return calls[0];
    }

    private static int keyboardForwardCalls(
            final byte[] bytes) {
        final int[] calls =
                new int[]{0};
        new ClassReader(bytes)
                .accept(
                        new ClassVisitor(
                                Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(
                                    final int access,
                                    final String name,
                                    final String descriptor,
                                    final String signature,
                                    final String[] exceptions) {
                                if (!"Z".equals(name)
                                        || !"()V".equals(
                                        descriptor)) {
                                    return null;
                                }
                                return new MethodVisitor(
                                        Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            final int opcode,
                                            final String owner,
                                            final String methodName,
                                            final String methodDescriptor,
                                            final boolean isInterface) {
                                        if (opcode
                                                == Opcodes.INVOKESTATIC
                                                && KEYBOARD_BINDING.equals(
                                                owner)
                                                && "forwardCurrentEvent"
                                                .equals(methodName)
                                                && "()V".equals(
                                                methodDescriptor)) {
                                            calls[0]++;
                                        }
                                    }
                                };
                            }
                        },
                        0);
        return calls[0];
    }

    private static int mouseForwardCalls(
            final byte[] bytes) {
        final int[] calls =
                new int[]{0};
        new ClassReader(bytes)
                .accept(
                        new ClassVisitor(
                                Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(
                                    final int access,
                                    final String name,
                                    final String descriptor,
                                    final String signature,
                                    final String[] exceptions) {
                                if (!"a".equals(name)
                                        || !"(IZ)V".equals(
                                        descriptor)
                                        || (access & Opcodes.ACC_STATIC) == 0) {
                                    return null;
                                }
                                return new MethodVisitor(
                                        Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            final int opcode,
                                            final String owner,
                                            final String methodName,
                                            final String methodDescriptor,
                                            final boolean isInterface) {
                                        if (opcode
                                                == Opcodes.INVOKESTATIC
                                                && MOUSE_BINDING.equals(
                                                owner)
                                                && "forwardKeyBindingState"
                                                .equals(methodName)
                                                && "(IZ)V".equals(
                                                methodDescriptor)) {
                                            calls[0]++;
                                        }
                                    }
                                };
                            }
                        },
                        0);
        return calls[0];
    }

    private static byte[] keyBindingShape() {
        final ClassWriter writer =
                classWriter("avb");
        writer.visitField(
                        Opcodes.ACC_PUBLIC
                                | Opcodes.ACC_STATIC,
                        "calls",
                        "I",
                        null,
                        null)
                .visitEnd();

        final MethodVisitor method =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC
                                | Opcodes.ACC_STATIC,
                        "a",
                        "(IZ)V",
                        null,
                        null);
        method.visitCode();
        method.visitFieldInsn(
                Opcodes.GETSTATIC,
                "avb",
                "calls",
                "I");
        method.visitInsn(
                Opcodes.ICONST_1);
        method.visitInsn(
                Opcodes.IADD);
        method.visitFieldInsn(
                Opcodes.PUTSTATIC,
                "avb",
                "calls",
                "I");
        method.visitInsn(
                Opcodes.RETURN);
        method.visitMaxs(
                2,
                2);
        method.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static int wheelForwardCalls(
            final byte[] bytes) {
        final int[] calls =
                new int[]{0};
        new ClassReader(bytes)
                .accept(
                        new ClassVisitor(
                                Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(
                                    final int access,
                                    final String name,
                                    final String descriptor,
                                    final String signature,
                                    final String[] exceptions) {
                                if (!"s".equals(name)
                                        || !"()V".equals(
                                        descriptor)) {
                                    return null;
                                }
                                return new MethodVisitor(
                                        Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            final int opcode,
                                            final String owner,
                                            final String methodName,
                                            final String methodDescriptor,
                                            final boolean isInterface) {
                                        if (opcode
                                                == Opcodes.INVOKESTATIC
                                                && MOUSE_BINDING.equals(
                                                owner)
                                                && "forwardWheelDelta"
                                                .equals(methodName)
                                                && "(I)V".equals(
                                                methodDescriptor)) {
                                            calls[0]++;
                                        }
                                    }
                                };
                            }
                        },
                        0);
        return calls[0];
    }

    private static byte[] itemStackShape() {
        final ClassWriter writer =
                classWriter("zx");
        field(
                writer,
                "b",
                "I");
        field(
                writer,
                "displayName",
                "Ljava/lang/String;");
        field(
                writer,
                "itemDamage",
                "I");
        field(
                writer,
                "maxDamage",
                "I");
        endDefaultConstructor(
                writer,
                "zx");

        final MethodVisitor displayName =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "q",
                        "()Ljava/lang/String;",
                        null,
                        null);
        displayName.visitCode();
        displayName.visitVarInsn(
                Opcodes.ALOAD,
                0);
        displayName.visitFieldInsn(
                Opcodes.GETFIELD,
                "zx",
                "displayName",
                "Ljava/lang/String;");
        displayName.visitInsn(
                Opcodes.ARETURN);
        displayName.visitMaxs(
                1,
                1);
        displayName.visitEnd();

        final MethodVisitor itemDamage =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "h",
                        "()I",
                        null,
                        null);
        itemDamage.visitCode();
        itemDamage.visitVarInsn(
                Opcodes.ALOAD,
                0);
        itemDamage.visitFieldInsn(
                Opcodes.GETFIELD,
                "zx",
                "itemDamage",
                "I");
        itemDamage.visitInsn(
                Opcodes.IRETURN);
        itemDamage.visitMaxs(
                1,
                1);
        itemDamage.visitEnd();

        final MethodVisitor maxDamage =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "j",
                        "()I",
                        null,
                        null);
        maxDamage.visitCode();
        maxDamage.visitVarInsn(
                Opcodes.ALOAD,
                0);
        maxDamage.visitFieldInsn(
                Opcodes.GETFIELD,
                "zx",
                "maxDamage",
                "I");
        maxDamage.visitInsn(
                Opcodes.IRETURN);
        maxDamage.visitMaxs(
                1,
                1);
        maxDamage.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] timerShape() {
        final ClassWriter writer =
                classWriter("avl");
        field(
                writer,
                "d",
                "F");
        endDefaultConstructor(
                writer,
                "avl");
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] worldShape() {
        final ClassWriter writer =
                classWriter("adm");
        field(
                writer,
                "worldTime",
                "J");
        field(
                writer,
                "raining",
                "Z");
        field(
                writer,
                "thundering",
                "Z");
        endDefaultConstructor(
                writer,
                "adm");

        final MethodVisitor getWorldTime =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "L",
                        "()J",
                        null,
                        null);
        getWorldTime.visitCode();
        getWorldTime.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getWorldTime.visitFieldInsn(
                Opcodes.GETFIELD,
                "adm",
                "worldTime",
                "J");
        getWorldTime.visitInsn(
                Opcodes.LRETURN);
        getWorldTime.visitMaxs(
                2,
                1);
        getWorldTime.visitEnd();

        final MethodVisitor isRaining =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "S",
                        "()Z",
                        null,
                        null);
        isRaining.visitCode();
        isRaining.visitVarInsn(
                Opcodes.ALOAD,
                0);
        isRaining.visitFieldInsn(
                Opcodes.GETFIELD,
                "adm",
                "raining",
                "Z");
        isRaining.visitInsn(
                Opcodes.IRETURN);
        isRaining.visitMaxs(
                1,
                1);
        isRaining.visitEnd();

        final MethodVisitor isThundering =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "R",
                        "()Z",
                        null,
                        null);
        isThundering.visitCode();
        isThundering.visitVarInsn(
                Opcodes.ALOAD,
                0);
        isThundering.visitFieldInsn(
                Opcodes.GETFIELD,
                "adm",
                "thundering",
                "Z");
        isThundering.visitInsn(
                Opcodes.IRETURN);
        isThundering.visitMaxs(
                1,
                1);
        isThundering.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] worldClientShape() {
        final ClassWriter writer =
                new ClassWriter(0);
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                "bdb",
                null,
                "adm",
                null);

        final MethodVisitor constructor =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        "()V",
                        null,
                        null);
        constructor.visitCode();
        constructor.visitVarInsn(
                Opcodes.ALOAD,
                0);
        constructor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "adm",
                "<init>",
                "()V",
                false);
        constructor.visitInsn(
                Opcodes.RETURN);
        constructor.visitMaxs(
                1,
                1);
        constructor.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] serverDataShape() {
        final ClassWriter writer =
                classWriter("bde");
        field(
                writer,
                "b",
                "Ljava/lang/String;");
        endDefaultConstructor(
                writer,
                "bde");
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] gameSettingsShape() {
        final ClassWriter writer =
                classWriter("avh");
        field(writer, "d", "Z");
        field(writer, "aI", "F");
        field(writer, "aJ", "F");
        field(writer, "aL", "I");
        field(writer, "aO", "Z");
        endDefaultConstructor(writer, "avh");
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] fontRendererShape() {
        final ClassWriter writer =
                classWriter("avn");
        endDefaultConstructor(writer, "avn");

        final MethodVisitor draw =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "a",
                        "(Ljava/lang/String;FFIZ)I",
                        null,
                        null);
        draw.visitCode();
        draw.visitIntInsn(
                Opcodes.BIPUSH,
                91);
        draw.visitInsn(
                Opcodes.IRETURN);
        draw.visitMaxs(1, 6);
        draw.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] playerControllerShape() {
        final ClassWriter writer =
                classWriter("bda");
        field(
                writer,
                "g",
                "I");
        field(
                writer,
                "e",
                "F");
        field(
                writer,
                "h",
                "Z");
        endDefaultConstructor(
                writer,
                "bda");
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] minecraftShape() {
        final ClassWriter writer =
                classWriter("ave");
        field(writer, "h", "Lbew;");
        field(writer, "f", "Lbdb;");
        field(writer, "c", "Lbda;");
        field(writer, "k", "Lavn;");
        field(writer, "o", "Lbfk;");
        field(writer, "q", "Lavo;");
        field(writer, "t", "Lavh;");
        field(writer, "Q", "Lbde;");
        field(writer, "ap", "I");
        field(writer, "ag", "I");
        field(writer, "Y", "Lavl;");
        field(writer, "clickMouseCalls", "I");
        endDefaultConstructor(writer, "ave");

        final MethodVisitor getMinecraft =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC
                                | Opcodes.ACC_STATIC,
                        "A",
                        "()Lave;",
                        null,
                        null);
        getMinecraft.visitCode();
        getMinecraft.visitInsn(
                Opcodes.ACONST_NULL);
        getMinecraft.visitInsn(
                Opcodes.ARETURN);
        getMinecraft.visitMaxs(1, 0);
        getMinecraft.visitEnd();

        voidMethod(writer, "am");
        runTickMethod(writer);

        final MethodVisitor clickMouse =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "aw",
                        "()V",
                        null,
                        null);
        clickMouse.visitCode();
        clickMouse.visitVarInsn(
                Opcodes.ALOAD,
                0);
        clickMouse.visitInsn(
                Opcodes.DUP);
        clickMouse.visitFieldInsn(
                Opcodes.GETFIELD,
                "ave",
                "clickMouseCalls",
                "I");
        clickMouse.visitInsn(
                Opcodes.ICONST_1);
        clickMouse.visitInsn(
                Opcodes.IADD);
        clickMouse.visitFieldInsn(
                Opcodes.PUTFIELD,
                "ave",
                "clickMouseCalls",
                "I");
        clickMouse.visitInsn(
                Opcodes.RETURN);
        clickMouse.visitMaxs(
                3,
                1);
        clickMouse.visitEnd();

        voidMethod(writer, "ax");
        voidMethod(writer, "az");
        voidMethod(writer, "Z");

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] entityShape() {
        final ClassWriter writer =
                classWriter("pk");
        field(writer, "s", "D");
        field(writer, "t", "D");
        field(writer, "u", "D");
        field(writer, "y", "F");
        field(writer, "v", "D");
        field(writer, "w", "D");
        field(writer, "x", "D");
        field(writer, "am", "I");
        field(writer, "C", "Z");
        field(writer, "S", "F");
        field(writer, "O", "F");
        field(writer, "H", "Z");
        field(writer, "T", "Z");
        field(writer, "sneaking", "Z");
        field(writer, "sprinting", "Z");
        endDefaultConstructor(writer, "pk");

        final MethodVisitor isSneaking =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "av",
                        "()Z",
                        null,
                        null);
        isSneaking.visitCode();
        isSneaking.visitVarInsn(
                Opcodes.ALOAD,
                0);
        isSneaking.visitFieldInsn(
                Opcodes.GETFIELD,
                "pk",
                "sneaking",
                "Z");
        isSneaking.visitInsn(
                Opcodes.IRETURN);
        isSneaking.visitMaxs(
                1,
                1);
        isSneaking.visitEnd();

        final MethodVisitor isSprinting =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "aw",
                        "()Z",
                        null,
                        null);
        isSprinting.visitCode();
        isSprinting.visitVarInsn(
                Opcodes.ALOAD,
                0);
        isSprinting.visitFieldInsn(
                Opcodes.GETFIELD,
                "pk",
                "sprinting",
                "Z");
        isSprinting.visitInsn(
                Opcodes.IRETURN);
        isSprinting.visitMaxs(
                1,
                1);
        isSprinting.visitEnd();

        final MethodVisitor setSprinting =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "d",
                        "(Z)V",
                        null,
                        null);
        setSprinting.visitCode();
        setSprinting.visitVarInsn(
                Opcodes.ALOAD,
                0);
        setSprinting.visitVarInsn(
                Opcodes.ILOAD,
                1);
        setSprinting.visitFieldInsn(
                Opcodes.PUTFIELD,
                "pk",
                "sprinting",
                "Z");
        setSprinting.visitInsn(
                Opcodes.RETURN);
        setSprinting.visitMaxs(
                2,
                2);
        setSprinting.visitEnd();

        final MethodVisitor setSneaking =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "c",
                        "(Z)V",
                        null,
                        null);
        setSneaking.visitCode();
        setSneaking.visitVarInsn(
                Opcodes.ALOAD,
                0);
        setSneaking.visitVarInsn(
                Opcodes.ILOAD,
                1);
        setSneaking.visitFieldInsn(
                Opcodes.PUTFIELD,
                "pk",
                "sneaking",
                "Z");
        setSneaking.visitInsn(
                Opcodes.RETURN);
        setSneaking.visitMaxs(
                2,
                2);
        setSneaking.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] entityLivingBaseShape() {
        final ClassWriter writer =
                new ClassWriter(0);
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                "pr",
                null,
                "pk",
                null);
        field(writer, "health", "F");
        field(writer, "maxHealth", "F");
        field(writer, "equipmentSlots", "[Lzx;");
        field(writer, "activePotionEffects", "Ljava/util/Collection;");
        field(writer, "jumpCalls", "I");

        final MethodVisitor constructor =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        "()V",
                        null,
                        null);
        constructor.visitCode();
        constructor.visitVarInsn(
                Opcodes.ALOAD,
                0);
        constructor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "pk",
                "<init>",
                "()V",
                false);
        constructor.visitInsn(
                Opcodes.RETURN);
        constructor.visitMaxs(
                1,
                1);
        constructor.visitEnd();

        final MethodVisitor getHealth =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "bn",
                        "()F",
                        null,
                        null);
        getHealth.visitCode();
        getHealth.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getHealth.visitFieldInsn(
                Opcodes.GETFIELD,
                "pr",
                "health",
                "F");
        getHealth.visitInsn(
                Opcodes.FRETURN);
        getHealth.visitMaxs(
                1,
                1);
        getHealth.visitEnd();

        final MethodVisitor getMaxHealth =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "bu",
                        "()F",
                        null,
                        null);
        getMaxHealth.visitCode();
        getMaxHealth.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getMaxHealth.visitFieldInsn(
                Opcodes.GETFIELD,
                "pr",
                "maxHealth",
                "F");
        getMaxHealth.visitInsn(
                Opcodes.FRETURN);
        getMaxHealth.visitMaxs(
                1,
                1);
        getMaxHealth.visitEnd();

        final MethodVisitor getEquipmentInSlot =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "p",
                        "(I)Lzx;",
                        null,
                        null);
        getEquipmentInSlot.visitCode();
        getEquipmentInSlot.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getEquipmentInSlot.visitFieldInsn(
                Opcodes.GETFIELD,
                "pr",
                "equipmentSlots",
                "[Lzx;");
        getEquipmentInSlot.visitVarInsn(
                Opcodes.ILOAD,
                1);
        getEquipmentInSlot.visitInsn(
                Opcodes.AALOAD);
        getEquipmentInSlot.visitInsn(
                Opcodes.ARETURN);
        getEquipmentInSlot.visitMaxs(
                2,
                2);
        getEquipmentInSlot.visitEnd();

        final MethodVisitor getActivePotionEffects =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "bl",
                        "()Ljava/util/Collection;",
                        null,
                        null);
        getActivePotionEffects.visitCode();
        getActivePotionEffects.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getActivePotionEffects.visitFieldInsn(
                Opcodes.GETFIELD,
                "pr",
                "activePotionEffects",
                "Ljava/util/Collection;");
        getActivePotionEffects.visitInsn(
                Opcodes.ARETURN);
        getActivePotionEffects.visitMaxs(
                1,
                1);
        getActivePotionEffects.visitEnd();

        final MethodVisitor jump =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "bF",
                        "()V",
                        null,
                        null);
        jump.visitCode();
        jump.visitVarInsn(
                Opcodes.ALOAD,
                0);
        jump.visitInsn(
                Opcodes.DUP);
        jump.visitFieldInsn(
                Opcodes.GETFIELD,
                "pr",
                "jumpCalls",
                "I");
        jump.visitInsn(
                Opcodes.ICONST_1);
        jump.visitInsn(
                Opcodes.IADD);
        jump.visitFieldInsn(
                Opcodes.PUTFIELD,
                "pr",
                "jumpCalls",
                "I");
        jump.visitInsn(
                Opcodes.RETURN);
        jump.visitMaxs(
                3,
                1);
        jump.visitEnd();

        final MethodVisitor knockBack =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "a",
                        "(Lpk;FDD)V",
                        null,
                        null);
        knockBack.visitCode();

        knockBack.visitVarInsn(
                Opcodes.ALOAD,
                0);
        knockBack.visitInsn(
                Opcodes.DUP);
        knockBack.visitFieldInsn(
                Opcodes.GETFIELD,
                "pk",
                "v",
                "D");
        knockBack.visitVarInsn(
                Opcodes.DLOAD,
                3);
        knockBack.visitInsn(
                Opcodes.DADD);
        knockBack.visitFieldInsn(
                Opcodes.PUTFIELD,
                "pk",
                "v",
                "D");

        knockBack.visitVarInsn(
                Opcodes.ALOAD,
                0);
        knockBack.visitInsn(
                Opcodes.DUP);
        knockBack.visitFieldInsn(
                Opcodes.GETFIELD,
                "pk",
                "w",
                "D");
        knockBack.visitVarInsn(
                Opcodes.FLOAD,
                2);
        knockBack.visitInsn(
                Opcodes.F2D);
        knockBack.visitInsn(
                Opcodes.DADD);
        knockBack.visitFieldInsn(
                Opcodes.PUTFIELD,
                "pk",
                "w",
                "D");

        knockBack.visitVarInsn(
                Opcodes.ALOAD,
                0);
        knockBack.visitInsn(
                Opcodes.DUP);
        knockBack.visitFieldInsn(
                Opcodes.GETFIELD,
                "pk",
                "x",
                "D");
        knockBack.visitVarInsn(
                Opcodes.DLOAD,
                5);
        knockBack.visitInsn(
                Opcodes.DADD);
        knockBack.visitFieldInsn(
                Opcodes.PUTFIELD,
                "pk",
                "x",
                "D");

        knockBack.visitInsn(
                Opcodes.RETURN);
        knockBack.visitMaxs(
                5,
                7);
        knockBack.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] potionEffectShape() {
        final ClassWriter writer =
                classWriter("pf");
        field(writer, "potionId", "I");
        field(writer, "duration", "I");
        field(writer, "amplifier", "I");
        field(writer, "effectName", "Ljava/lang/String;");
        endDefaultConstructor(
                writer,
                "pf");

        final MethodVisitor getPotionId =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "a",
                        "()I",
                        null,
                        null);
        getPotionId.visitCode();
        getPotionId.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getPotionId.visitFieldInsn(
                Opcodes.GETFIELD,
                "pf",
                "potionId",
                "I");
        getPotionId.visitInsn(
                Opcodes.IRETURN);
        getPotionId.visitMaxs(
                1,
                1);
        getPotionId.visitEnd();

        final MethodVisitor getDuration =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "b",
                        "()I",
                        null,
                        null);
        getDuration.visitCode();
        getDuration.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getDuration.visitFieldInsn(
                Opcodes.GETFIELD,
                "pf",
                "duration",
                "I");
        getDuration.visitInsn(
                Opcodes.IRETURN);
        getDuration.visitMaxs(
                1,
                1);
        getDuration.visitEnd();

        final MethodVisitor getAmplifier =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "c",
                        "()I",
                        null,
                        null);
        getAmplifier.visitCode();
        getAmplifier.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getAmplifier.visitFieldInsn(
                Opcodes.GETFIELD,
                "pf",
                "amplifier",
                "I");
        getAmplifier.visitInsn(
                Opcodes.IRETURN);
        getAmplifier.visitMaxs(
                1,
                1);
        getAmplifier.visitEnd();

        final MethodVisitor getEffectName =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "g",
                        "()Ljava/lang/String;",
                        null,
                        null);
        getEffectName.visitCode();
        getEffectName.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getEffectName.visitFieldInsn(
                Opcodes.GETFIELD,
                "pf",
                "effectName",
                "Ljava/lang/String;");
        getEffectName.visitInsn(
                Opcodes.ARETURN);
        getEffectName.visitMaxs(
                1,
                1);
        getEffectName.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] foodStatsShape() {
        final ClassWriter writer =
                classWriter("xg");
        field(writer, "foodLevel", "I");
        field(writer, "saturationLevel", "F");
        endDefaultConstructor(
                writer,
                "xg");

        final MethodVisitor getFoodLevel =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "a",
                        "()I",
                        null,
                        null);
        getFoodLevel.visitCode();
        getFoodLevel.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getFoodLevel.visitFieldInsn(
                Opcodes.GETFIELD,
                "xg",
                "foodLevel",
                "I");
        getFoodLevel.visitInsn(
                Opcodes.IRETURN);
        getFoodLevel.visitMaxs(
                1,
                1);
        getFoodLevel.visitEnd();

        final MethodVisitor getSaturationLevel =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "e",
                        "()F",
                        null,
                        null);
        getSaturationLevel.visitCode();
        getSaturationLevel.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getSaturationLevel.visitFieldInsn(
                Opcodes.GETFIELD,
                "xg",
                "saturationLevel",
                "F");
        getSaturationLevel.visitInsn(
                Opcodes.FRETURN);
        getSaturationLevel.visitMaxs(
                1,
                1);
        getSaturationLevel.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] entityPlayerShape() {
        final ClassWriter writer =
                new ClassWriter(0);
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                "wn",
                null,
                "pr",
                null);
        field(writer, "foodStats", "Lxg;");
        field(writer, "bi", "Lwm;");
        field(writer, "bB", "I");
        field(writer, "bC", "I");
        field(writer, "bD", "F");

        final MethodVisitor constructor =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        "()V",
                        null,
                        null);
        constructor.visitCode();
        constructor.visitVarInsn(
                Opcodes.ALOAD,
                0);
        constructor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "pr",
                "<init>",
                "()V",
                false);
        constructor.visitInsn(
                Opcodes.RETURN);
        constructor.visitMaxs(
                1,
                1);
        constructor.visitEnd();

        final MethodVisitor getFoodStats =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "cl",
                        "()Lxg;",
                        null,
                        null);
        getFoodStats.visitCode();
        getFoodStats.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getFoodStats.visitFieldInsn(
                Opcodes.GETFIELD,
                "wn",
                "foodStats",
                "Lxg;");
        getFoodStats.visitInsn(
                Opcodes.ARETURN);
        getFoodStats.visitMaxs(
                1,
                1);
        getFoodStats.visitEnd();

        final MethodVisitor xpBarCap =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "ck",
                        "()I",
                        null,
                        null);
        xpBarCap.visitCode();
        xpBarCap.visitIntInsn(
                Opcodes.BIPUSH,
                42);
        xpBarCap.visitInsn(
                Opcodes.IRETURN);
        xpBarCap.visitMaxs(
                1,
                1);
        xpBarCap.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] inventoryPlayerShape() {
        final ClassWriter writer =
                classWriter("wm");
        field(
                writer,
                "c",
                "I");
        endDefaultConstructor(
                writer,
                "wm");
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] networkPlayerInfoShape() {
        final ClassWriter writer =
                classWriter("bdc");
        field(writer, "responseTime", "I");
        endDefaultConstructor(
                writer,
                "bdc");

        final MethodVisitor getResponseTime =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "c",
                        "()I",
                        null,
                        null);
        getResponseTime.visitCode();
        getResponseTime.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getResponseTime.visitFieldInsn(
                Opcodes.GETFIELD,
                "bdc",
                "responseTime",
                "I");
        getResponseTime.visitInsn(
                Opcodes.IRETURN);
        getResponseTime.visitMaxs(
                1,
                1);
        getResponseTime.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] abstractClientPlayerShape() {
        final ClassWriter writer =
                new ClassWriter(0);
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                "bet",
                null,
                "wn",
                null);
        field(writer, "playerInfo", "Lbdc;");

        final MethodVisitor constructor =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        "()V",
                        null,
                        null);
        constructor.visitCode();
        constructor.visitVarInsn(
                Opcodes.ALOAD,
                0);
        constructor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "wn",
                "<init>",
                "()V",
                false);
        constructor.visitInsn(
                Opcodes.RETURN);
        constructor.visitMaxs(
                1,
                1);
        constructor.visitEnd();

        final MethodVisitor getPlayerInfo =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "b",
                        "()Lbdc;",
                        null,
                        null);
        getPlayerInfo.visitCode();
        getPlayerInfo.visitVarInsn(
                Opcodes.ALOAD,
                0);
        getPlayerInfo.visitFieldInsn(
                Opcodes.GETFIELD,
                "bet",
                "playerInfo",
                "Lbdc;");
        getPlayerInfo.visitInsn(
                Opcodes.ARETURN);
        getPlayerInfo.visitMaxs(
                1,
                1);
        getPlayerInfo.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] playerShape() {
        final ClassWriter writer =
                new ClassWriter(0);
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                "bew",
                null,
                "bet",
                null);
        field(
                writer,
                "b",
                "Lbeu;");
        final MethodVisitor constructor =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        "()V",
                        null,
                        null);
        constructor.visitCode();
        constructor.visitVarInsn(
                Opcodes.ALOAD,
                0);
        constructor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "bet",
                "<init>",
                "()V",
                false);
        constructor.visitInsn(
                Opcodes.RETURN);
        constructor.visitMaxs(
                1,
                1);
        constructor.visitEnd();

        final MethodVisitor onLivingUpdate =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "m",
                        "()V",
                        null,
                        null);
        onLivingUpdate.visitCode();

        onLivingUpdate.visitVarInsn(
                Opcodes.ALOAD,
                0);
        onLivingUpdate.visitFieldInsn(
                Opcodes.GETFIELD,
                "bew",
                "b",
                "Lbeu;");
        onLivingUpdate.visitInsn(
                Opcodes.DUP);
        onLivingUpdate.visitFieldInsn(
                Opcodes.GETFIELD,
                "beu",
                "a",
                "F");
        onLivingUpdate.visitLdcInsn(
                Float.valueOf(0.2F));
        onLivingUpdate.visitInsn(
                Opcodes.FMUL);
        onLivingUpdate.visitFieldInsn(
                Opcodes.PUTFIELD,
                "beu",
                "a",
                "F");

        onLivingUpdate.visitVarInsn(
                Opcodes.ALOAD,
                0);
        onLivingUpdate.visitFieldInsn(
                Opcodes.GETFIELD,
                "bew",
                "b",
                "Lbeu;");
        onLivingUpdate.visitInsn(
                Opcodes.DUP);
        onLivingUpdate.visitFieldInsn(
                Opcodes.GETFIELD,
                "beu",
                "b",
                "F");
        onLivingUpdate.visitLdcInsn(
                Float.valueOf(0.2F));
        onLivingUpdate.visitInsn(
                Opcodes.FMUL);
        onLivingUpdate.visitFieldInsn(
                Opcodes.PUTFIELD,
                "beu",
                "b",
                "F");

        onLivingUpdate.visitInsn(
                Opcodes.RETURN);
        onLivingUpdate.visitMaxs(
                3,
                1);
        onLivingUpdate.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] movementInputShape() {
        final ClassWriter writer =
                classWriter("beu");
        field(
                writer,
                "a",
                "F");
        field(
                writer,
                "b",
                "F");
        endDefaultConstructor(
                writer,
                "beu");
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] guiIngameShape() {
        final ClassWriter writer =
                classWriter("avo");
        endDefaultConstructor(writer, "avo");

        final MethodVisitor overlay =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "a",
                        "(F)V",
                        null,
                        null);
        overlay.visitCode();
        overlay.visitInsn(
                Opcodes.RETURN);
        overlay.visitMaxs(0, 2);
        overlay.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] entityRendererShape() {
        final ClassWriter writer =
                classWriter("bfk");
        endDefaultConstructor(writer, "bfk");

        final MethodVisitor render =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "a",
                        "(FJ)V",
                        null,
                        null);
        render.visitCode();
        render.visitTypeInsn(
                Opcodes.NEW,
                "avo");
        render.visitInsn(
                Opcodes.DUP);
        render.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "avo",
                "<init>",
                "()V",
                false);
        render.visitVarInsn(
                Opcodes.FLOAD,
                1);
        render.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "avo",
                "a",
                "(F)V",
                false);
        render.visitInsn(
                Opcodes.RETURN);
        render.visitMaxs(3, 4);
        render.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] emptyClass(
            final String name) {
        final ClassWriter writer =
                classWriter(name);
        endDefaultConstructor(writer, name);
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static ClassWriter classWriter(
            final String name) {
        final ClassWriter writer =
                new ClassWriter(0);
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                name,
                null,
                "java/lang/Object",
                null);
        return writer;
    }

    private static void field(
            final ClassWriter writer,
            final String name,
            final String descriptor) {
        final FieldVisitor field =
                writer.visitField(
                        Opcodes.ACC_PUBLIC,
                        name,
                        descriptor,
                        null,
                        null);
        field.visitEnd();
    }

    private static void endDefaultConstructor(
            final ClassWriter writer,
            final String owner) {
        final MethodVisitor constructor =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        "()V",
                        null,
                        null);
        constructor.visitCode();
        constructor.visitVarInsn(
                Opcodes.ALOAD,
                0);
        constructor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V",
                false);
        constructor.visitInsn(
                Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
    }

    private static void runTickMethod(
            final ClassWriter writer) {
        final MethodVisitor method =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "s",
                        "()V",
                        null,
                        null);
        method.visitCode();
        final org.objectweb.asm.Label skipWheel =
                new org.objectweb.asm.Label();
        method.visitInsn(
                Opcodes.ICONST_0);
        method.visitJumpInsn(
                Opcodes.IFEQ,
                skipWheel);
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "org/lwjgl/input/Mouse",
                "getEventDWheel",
                "()I",
                false);
        method.visitInsn(
                Opcodes.POP);
        method.visitLabel(
                skipWheel);
        method.visitFrame(
                Opcodes.F_SAME,
                0,
                null,
                0,
                null);
        method.visitInsn(
                Opcodes.RETURN);
        method.visitMaxs(
                1,
                1);
        method.visitEnd();
    }

    private static void voidMethod(
            final ClassWriter writer,
            final String name) {
        final MethodVisitor method =
                writer.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        name,
                        "()V",
                        null,
                        null);
        method.visitCode();
        method.visitInsn(
                Opcodes.RETURN);
        method.visitMaxs(0, 1);
        method.visitEnd();
    }

    private static final class ByteMapClassLoader
            extends ClassLoader {
        private final Map<String, byte[]> classes =
                new LinkedHashMap<String, byte[]>();

        private ByteMapClassLoader(
                final ClassLoader parent) {
            super(parent);
        }

        private void put(
                final String name,
                final byte[] bytes) {
            classes.put(
                    name,
                    bytes.clone());
        }

        @Override
        protected Class<?> findClass(
                final String name)
                throws ClassNotFoundException {
            final byte[] bytes =
                    classes.get(name);
            if (bytes == null) {
                throw new ClassNotFoundException(
                        name);
            }
            return defineClass(
                    name,
                    bytes,
                    0,
                    bytes.length);
        }
    }
}
