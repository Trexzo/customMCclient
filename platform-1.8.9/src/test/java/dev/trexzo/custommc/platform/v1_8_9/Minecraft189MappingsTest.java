package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189MappingsTest {
    @Test
    void authorityPinsExactSourceProvenanceAndCoreClassNames() {
        assertEquals(
                "1.8.9",
                Minecraft189Mappings.VERSION);
        assertEquals(
                "BigBroadBean/mappings-extracted",
                Minecraft189Mappings.SOURCE_REPOSITORY);
        assertEquals(
                "2265da88e93c20411ec70f0b892f4fbc84ebc3c9",
                Minecraft189Mappings.SOURCE_COMMIT);
        assertEquals(
                "17967e48db6c8ec20ae622409be13971e2c77706",
                Minecraft189Mappings.CLASSES_BLOB_SHA);
        assertEquals(
                "a8c5928cb64455dcc2712078dbfe018ae97cafb9",
                Minecraft189Mappings.FIELDS_BLOB_SHA);
        assertEquals(
                "683d45abbd02a1525008c5654a40b50efbc47c6d",
                Minecraft189Mappings.METHODS_BLOB_SHA);
        assertEquals(
                "0b1e3f1d0156abcbd70e2b09b720379fc0c1eae6",
                Minecraft189Mappings.JOINED_SRG_BLOB_SHA);

        assertClass(
                Minecraft189Mappings.MINECRAFT,
                "ave",
                "net/minecraft/client/Minecraft");
        assertClass(
                Minecraft189Mappings.WORLD,
                "adm",
                "net/minecraft/world/World");
        assertClass(
                Minecraft189Mappings.WORLD_CLIENT,
                "bdb",
                "net/minecraft/client/multiplayer/WorldClient");
        assertClass(
                Minecraft189Mappings.KEY_BINDING,
                "avb",
                "net/minecraft/client/settings/KeyBinding");
        assertClass(
                Minecraft189Mappings.GAME_SETTINGS,
                "avh",
                "net/minecraft/client/settings/GameSettings");
        assertClass(
                Minecraft189Mappings.FONT_RENDERER,
                "avn",
                "net/minecraft/client/gui/FontRenderer");
        assertClass(
                Minecraft189Mappings.GUI_INGAME,
                "avo",
                "net/minecraft/client/gui/GuiIngame");
        assertClass(
                Minecraft189Mappings.ENTITY_RENDERER,
                "bfk",
                "net/minecraft/client/renderer/EntityRenderer");
        assertClass(
                Minecraft189Mappings.ENTITY_PLAYER_SP,
                "bew",
                "net/minecraft/client/entity/EntityPlayerSP");
        assertClass(
                Minecraft189Mappings.ABSTRACT_CLIENT_PLAYER,
                "bet",
                "net/minecraft/client/entity/AbstractClientPlayer");
        assertClass(
                Minecraft189Mappings.NETWORK_PLAYER_INFO,
                "bdc",
                "net/minecraft/client/network/NetworkPlayerInfo");
        assertClass(
                Minecraft189Mappings.SERVER_DATA,
                "bde",
                "net/minecraft/client/multiplayer/ServerData");
        assertClass(
                Minecraft189Mappings.ENTITY_PLAYER,
                "wn",
                "net/minecraft/entity/player/EntityPlayer");
        assertClass(
                Minecraft189Mappings.INVENTORY_PLAYER,
                "wm",
                "net/minecraft/entity/player/InventoryPlayer");
        assertClass(
                Minecraft189Mappings.ENTITY,
                "pk",
                "net/minecraft/entity/Entity");
        assertClass(
                Minecraft189Mappings.ENTITY_LIVING_BASE,
                "pr",
                "net/minecraft/entity/EntityLivingBase");
        assertClass(
                Minecraft189Mappings.ITEM_STACK,
                "zx",
                "net/minecraft/item/ItemStack");
        assertClass(
                Minecraft189Mappings.FOOD_STATS,
                "xg",
                "net/minecraft/util/FoodStats");
        assertClass(
                Minecraft189Mappings.POTION_EFFECT,
                "pf",
                "net/minecraft/potion/PotionEffect");
    }

    @Test
    void authorityPinsExactFieldsAndMethodsNeededByHostHooks() {
        assertField(
                Minecraft189Mappings.MINECRAFT_PLAYER,
                Minecraft189Mappings.MINECRAFT,
                "h",
                "Lbew;",
                "field_71439_g",
                "thePlayer");
        assertField(
                Minecraft189Mappings.MINECRAFT_WORLD,
                Minecraft189Mappings.MINECRAFT,
                "f",
                "Lbdb;",
                "field_71441_e",
                "theWorld");
        assertField(
                Minecraft189Mappings.MINECRAFT_FONT_RENDERER,
                Minecraft189Mappings.MINECRAFT,
                "k",
                "Lavn;",
                "field_71466_p",
                "fontRendererObj");
        assertField(
                Minecraft189Mappings.MINECRAFT_ENTITY_RENDERER,
                Minecraft189Mappings.MINECRAFT,
                "o",
                "Lbfk;",
                "field_71460_t",
                "entityRenderer");
        assertField(
                Minecraft189Mappings.MINECRAFT_INGAME_GUI,
                Minecraft189Mappings.MINECRAFT,
                "q",
                "Lavo;",
                "field_71456_v",
                "ingameGUI");
        assertField(
                Minecraft189Mappings.MINECRAFT_GAME_SETTINGS,
                Minecraft189Mappings.MINECRAFT,
                "t",
                "Lavh;",
                "field_71474_y",
                "gameSettings");
        assertField(
                Minecraft189Mappings.MINECRAFT_CURRENT_SERVER_DATA,
                Minecraft189Mappings.MINECRAFT,
                "Q",
                "Lbde;",
                "field_71422_O",
                "currentServerData");
        assertField(
                Minecraft189Mappings.SERVER_DATA_SERVER_IP,
                Minecraft189Mappings.SERVER_DATA,
                "b",
                "Ljava/lang/String;",
                "field_78845_b",
                "serverIP");
        assertField(
                Minecraft189Mappings.ITEM_STACK_SIZE,
                Minecraft189Mappings.ITEM_STACK,
                "b",
                "I",
                "field_77994_a",
                "stackSize");
        assertField(
                Minecraft189Mappings.ENTITY_POS_X,
                Minecraft189Mappings.ENTITY,
                "s",
                "D",
                "field_70165_t",
                "posX");
        assertField(
                Minecraft189Mappings.ENTITY_POS_Y,
                Minecraft189Mappings.ENTITY,
                "t",
                "D",
                "field_70163_u",
                "posY");
        assertField(
                Minecraft189Mappings.ENTITY_POS_Z,
                Minecraft189Mappings.ENTITY,
                "u",
                "D",
                "field_70161_v",
                "posZ");
        assertField(
                Minecraft189Mappings.ENTITY_ROTATION_YAW,
                Minecraft189Mappings.ENTITY,
                "y",
                "F",
                "field_70177_z",
                "rotationYaw");
        assertField(
                Minecraft189Mappings.ENTITY_DIMENSION,
                Minecraft189Mappings.ENTITY,
                "am",
                "I",
                "field_71093_bK",
                "dimension");
        assertField(
                Minecraft189Mappings.ENTITY_PLAYER_INVENTORY,
                Minecraft189Mappings.ENTITY_PLAYER,
                "bi",
                "Lwm;",
                "field_71071_by",
                "inventory");
        assertField(
                Minecraft189Mappings.INVENTORY_PLAYER_CURRENT_ITEM,
                Minecraft189Mappings.INVENTORY_PLAYER,
                "c",
                "I",
                "field_70461_c",
                "currentItem");
        assertField(
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_LEVEL,
                Minecraft189Mappings.ENTITY_PLAYER,
                "bB",
                "I",
                "field_71068_ca",
                "experienceLevel");
        assertField(
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_TOTAL,
                Minecraft189Mappings.ENTITY_PLAYER,
                "bC",
                "I",
                "field_71067_cb",
                "experienceTotal");
        assertField(
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_PROGRESS,
                Minecraft189Mappings.ENTITY_PLAYER,
                "bD",
                "F",
                "field_71106_cc",
                "experience");

        assertField(
                Minecraft189Mappings.GAME_SETTINGS_VIEW_BOBBING,
                Minecraft189Mappings.GAME_SETTINGS,
                "d",
                "Z",
                "field_74336_f",
                "viewBobbing");
        assertField(
                Minecraft189Mappings.GAME_SETTINGS_FOV,
                Minecraft189Mappings.GAME_SETTINGS,
                "aI",
                "F",
                "field_74334_X",
                "fovSetting");
        assertField(
                Minecraft189Mappings.GAME_SETTINGS_GAMMA,
                Minecraft189Mappings.GAME_SETTINGS,
                "aJ",
                "F",
                "field_74333_Y",
                "gammaSetting");
        assertField(
                Minecraft189Mappings.GAME_SETTINGS_GUI_SCALE,
                Minecraft189Mappings.GAME_SETTINGS,
                "aL",
                "I",
                "field_74335_Z",
                "guiScale");
        assertField(
                Minecraft189Mappings.GAME_SETTINGS_FORCE_UNICODE,
                Minecraft189Mappings.GAME_SETTINGS,
                "aO",
                "Z",
                "field_151455_aw",
                "forceUnicodeFont");

        assertMethod(
                Minecraft189Mappings.WORLD_GET_WORLD_TIME,
                Minecraft189Mappings.WORLD,
                "L",
                "()J",
                "func_72820_D",
                "getWorldTime");
        assertMethod(
                Minecraft189Mappings.WORLD_IS_RAINING,
                Minecraft189Mappings.WORLD,
                "S",
                "()Z",
                "func_72896_J",
                "isRaining");
        assertMethod(
                Minecraft189Mappings.WORLD_IS_THUNDERING,
                Minecraft189Mappings.WORLD,
                "R",
                "()Z",
                "func_72911_I",
                "isThundering");
        assertMethod(
                Minecraft189Mappings.MINECRAFT_GET_MINECRAFT,
                Minecraft189Mappings.MINECRAFT,
                "A",
                "()Lave;",
                "func_71410_x",
                "getMinecraft");
        assertMethod(
                Minecraft189Mappings.MINECRAFT_START_GAME,
                Minecraft189Mappings.MINECRAFT,
                "am",
                "()V",
                "func_71384_a",
                "startGame");
        assertMethod(
                Minecraft189Mappings.MINECRAFT_RUN_TICK,
                Minecraft189Mappings.MINECRAFT,
                "s",
                "()V",
                "func_71407_l",
                "runTick");
        assertMethod(
                Minecraft189Mappings.MINECRAFT_CLICK_MOUSE,
                Minecraft189Mappings.MINECRAFT,
                "aw",
                "()V",
                "func_147116_af",
                "clickMouse");
        assertMethod(
                Minecraft189Mappings.MINECRAFT_RIGHT_CLICK_MOUSE,
                Minecraft189Mappings.MINECRAFT,
                "ax",
                "()V",
                "func_147121_ag",
                "rightClickMouse");
        assertMethod(
                Minecraft189Mappings.MINECRAFT_MIDDLE_CLICK_MOUSE,
                Minecraft189Mappings.MINECRAFT,
                "az",
                "()V",
                "func_147112_ai",
                "middleClickMouse");
        assertMethod(
                Minecraft189Mappings.MINECRAFT_DISPATCH_KEYPRESSES,
                Minecraft189Mappings.MINECRAFT,
                "Z",
                "()V",
                "func_152348_aa",
                "dispatchKeypresses");
        assertMethod(
                Minecraft189Mappings.KEY_BINDING_SET_KEY_BIND_STATE,
                Minecraft189Mappings.KEY_BINDING,
                "a",
                "(IZ)V",
                "func_74510_a",
                "setKeyBindState");
        assertMethod(
                Minecraft189Mappings.FONT_RENDERER_DRAW_STRING,
                Minecraft189Mappings.FONT_RENDERER,
                "a",
                "(Ljava/lang/String;FFIZ)I",
                "func_175065_a",
                "drawString");
        assertMethod(
                Minecraft189Mappings.GUI_INGAME_RENDER_OVERLAY,
                Minecraft189Mappings.GUI_INGAME,
                "a",
                "(F)V",
                "func_175180_a",
                "renderGameOverlay");
        assertMethod(
                Minecraft189Mappings.ENTITY_RENDERER_UPDATE_CAMERA_AND_RENDER,
                Minecraft189Mappings.ENTITY_RENDERER,
                "a",
                "(FJ)V",
                "func_181560_a",
                "updateCameraAndRender");
        assertMethod(
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_HEALTH,
                Minecraft189Mappings.ENTITY_LIVING_BASE,
                "bn",
                "()F",
                "func_110143_aJ",
                "getHealth");
        assertMethod(
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_MAX_HEALTH,
                Minecraft189Mappings.ENTITY_LIVING_BASE,
                "bu",
                "()F",
                "func_110138_aP",
                "getMaxHealth");
        assertMethod(
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_EQUIPMENT_IN_SLOT,
                Minecraft189Mappings.ENTITY_LIVING_BASE,
                "p",
                "(I)Lzx;",
                "func_71124_b",
                "getEquipmentInSlot");
        assertMethod(
                Minecraft189Mappings.ENTITY_PLAYER_GET_FOOD_STATS,
                Minecraft189Mappings.ENTITY_PLAYER,
                "cl",
                "()Lxg;",
                "func_71024_bL",
                "getFoodStats");
        assertMethod(
                Minecraft189Mappings.ENTITY_PLAYER_XP_BAR_CAP,
                Minecraft189Mappings.ENTITY_PLAYER,
                "ck",
                "()I",
                "func_71050_bK",
                "xpBarCap");
        assertMethod(
                Minecraft189Mappings.ABSTRACT_CLIENT_PLAYER_GET_PLAYER_INFO,
                Minecraft189Mappings.ABSTRACT_CLIENT_PLAYER,
                "b",
                "()Lbdc;",
                "func_175155_b",
                "getPlayerInfo");
        assertMethod(
                Minecraft189Mappings.NETWORK_PLAYER_INFO_GET_RESPONSE_TIME,
                Minecraft189Mappings.NETWORK_PLAYER_INFO,
                "c",
                "()I",
                "func_178853_c",
                "getResponseTime");
        assertMethod(
                Minecraft189Mappings.ITEM_STACK_GET_DISPLAY_NAME,
                Minecraft189Mappings.ITEM_STACK,
                "q",
                "()Ljava/lang/String;",
                "func_82833_r",
                "getDisplayName");
        assertMethod(
                Minecraft189Mappings.ITEM_STACK_GET_ITEM_DAMAGE,
                Minecraft189Mappings.ITEM_STACK,
                "h",
                "()I",
                "func_77952_i",
                "getItemDamage");
        assertMethod(
                Minecraft189Mappings.ITEM_STACK_GET_MAX_DAMAGE,
                Minecraft189Mappings.ITEM_STACK,
                "j",
                "()I",
                "func_77958_k",
                "getMaxDamage");
        assertMethod(
                Minecraft189Mappings.FOOD_STATS_GET_FOOD_LEVEL,
                Minecraft189Mappings.FOOD_STATS,
                "a",
                "()I",
                "func_75116_a",
                "getFoodLevel");
        assertMethod(
                Minecraft189Mappings.FOOD_STATS_GET_SATURATION_LEVEL,
                Minecraft189Mappings.FOOD_STATS,
                "e",
                "()F",
                "func_75115_e",
                "getSaturationLevel");
        assertMethod(
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_ACTIVE_POTION_EFFECTS,
                Minecraft189Mappings.ENTITY_LIVING_BASE,
                "bl",
                "()Ljava/util/Collection;",
                "func_70651_bq",
                "getActivePotionEffects");
        assertMethod(
                Minecraft189Mappings.POTION_EFFECT_GET_POTION_ID,
                Minecraft189Mappings.POTION_EFFECT,
                "a",
                "()I",
                "func_76456_a",
                "getPotionID");
        assertMethod(
                Minecraft189Mappings.POTION_EFFECT_GET_DURATION,
                Minecraft189Mappings.POTION_EFFECT,
                "b",
                "()I",
                "func_76459_b",
                "getDuration");
        assertMethod(
                Minecraft189Mappings.POTION_EFFECT_GET_AMPLIFIER,
                Minecraft189Mappings.POTION_EFFECT,
                "c",
                "()I",
                "func_76458_c",
                "getAmplifier");
        assertMethod(
                Minecraft189Mappings.POTION_EFFECT_GET_EFFECT_NAME,
                Minecraft189Mappings.POTION_EFFECT,
                "g",
                "()Ljava/lang/String;",
                "func_76453_d",
                "getEffectName");
    }

    @Test
    void mappedClassShapeGatesAcceptExactOwnersAndMembers() {
        Minecraft189ClassShapeVerifier.verifyMinecraft(
                minecraftShape());
        Minecraft189ClassShapeVerifier.verifyWorld(
                worldShape());
        Minecraft189ClassShapeVerifier.verifyKeyBinding(
                keyBindingShape());
        Minecraft189ClassShapeVerifier.verifyGameSettings(
                gameSettingsShape());
        Minecraft189ClassShapeVerifier.verifyFontRenderer(
                fontRendererShape());
        Minecraft189ClassShapeVerifier.verifyGuiIngame(
                guiIngameShape());
        Minecraft189ClassShapeVerifier.verifyEntityRenderer(
                entityRendererShape());
        Minecraft189ClassShapeVerifier.verifyEntity(
                entityShape());
        Minecraft189ClassShapeVerifier.verifyEntityLivingBase(
                entityLivingBaseShape());
        Minecraft189ClassShapeVerifier.verifyEntityPlayer(
                entityPlayerShape());
        Minecraft189ClassShapeVerifier.verifyInventoryPlayer(
                inventoryPlayerShape());
        Minecraft189ClassShapeVerifier.verifyAbstractClientPlayer(
                abstractClientPlayerShape());
        Minecraft189ClassShapeVerifier.verifyNetworkPlayerInfo(
                networkPlayerInfoShape());
        Minecraft189ClassShapeVerifier.verifyServerData(
                serverDataShape());
        Minecraft189ClassShapeVerifier.verifyItemStack(
                itemStackShape());
        Minecraft189ClassShapeVerifier.verifyFoodStats(
                foodStatsShape());
        Minecraft189ClassShapeVerifier.verifyPotionEffect(
                potionEffectShape());
    }

    @Test
    void mappedClassShapeGatesRejectOwnerOrMemberDrift() {
        final IllegalStateException ownerFailure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyMinecraft(
                                        emptyClass(
                                                "wrong")));
        assertEquals(
                "Minecraft 1.8.9 mapping owner mismatch: expected ave but found wrong",
                ownerFailure.getMessage());

        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.MINECRAFT
                                .obfuscatedInternalName());
        addMinecraftFields(writer);
        addMethod(
                writer,
                Minecraft189Mappings.MINECRAFT_GET_MINECRAFT);
        addMethod(
                writer,
                Minecraft189Mappings.MINECRAFT_START_GAME);

        final IllegalStateException memberFailure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyMinecraft(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: ave.s()V (runTick)",
                memberFailure.getMessage());
    }

    @Test
    void minecraftShapeGateRejectsMissingWorldField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.MINECRAFT
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_PLAYER);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_FONT_RENDERER);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_ENTITY_RENDERER);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_INGAME_GUI);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_GAME_SETTINGS);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_CURRENT_SERVER_DATA);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyMinecraft(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: ave.f Lbdb; (theWorld)",
                failure.getMessage());
    }

    @Test
    void minecraftShapeGateRejectsMissingCurrentServerDataField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.MINECRAFT
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_PLAYER);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_WORLD);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_FONT_RENDERER);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_ENTITY_RENDERER);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_INGAME_GUI);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_GAME_SETTINGS);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyMinecraft(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: ave.Q Lbde; (currentServerData)",
                failure.getMessage());
    }

    @Test
    void worldShapeGateRejectsMissingWorldTimeMethod() {
        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyWorld(
                                        emptyClass("adm")));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: adm.L()J (getWorldTime)",
                failure.getMessage());
    }

    @Test
    void worldShapeGateRejectsMissingRainingMethod() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.WORLD
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.WORLD_GET_WORLD_TIME);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyWorld(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: adm.S()Z (isRaining)",
                failure.getMessage());
    }

    @Test
    void worldShapeGateRejectsMissingThunderingMethod() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.WORLD
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.WORLD_GET_WORLD_TIME);
        addMethod(
                writer,
                Minecraft189Mappings.WORLD_IS_RAINING);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyWorld(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: adm.R()Z (isThundering)",
                failure.getMessage());
    }

    private static byte[] worldShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.WORLD
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.WORLD_GET_WORLD_TIME);
        addMethod(
                writer,
                Minecraft189Mappings.WORLD_IS_RAINING);
        addMethod(
                writer,
                Minecraft189Mappings.WORLD_IS_THUNDERING);
        return finish(writer);
    }

    @Test
    void serverDataShapeGateRejectsMissingServerIpField() {
        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyServerData(
                                        emptyClass("bde")));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: bde.b Ljava/lang/String; (serverIP)",
                failure.getMessage());
    }

    private static byte[] serverDataShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.SERVER_DATA
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.SERVER_DATA_SERVER_IP);
        return finish(writer);
    }

    @Test
    void gameSettingsShapeGateRejectsMissingViewBobbingField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.GAME_SETTINGS
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_FOV);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_GAMMA);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_GUI_SCALE);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_FORCE_UNICODE);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyGameSettings(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: avh.d Z (viewBobbing)",
                failure.getMessage());
    }

    @Test
    void gameSettingsShapeGateRejectsMissingFovField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.GAME_SETTINGS
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_VIEW_BOBBING);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_GAMMA);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_GUI_SCALE);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_FORCE_UNICODE);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyGameSettings(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: avh.aI F (fovSetting)",
                failure.getMessage());
    }

    @Test
    void gameSettingsShapeGateRejectsMissingGammaField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.GAME_SETTINGS
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_VIEW_BOBBING);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_FOV);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_GUI_SCALE);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_FORCE_UNICODE);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyGameSettings(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: avh.aJ F (gammaSetting)",
                failure.getMessage());
    }

    @Test
    void entityShapeGateRejectsMissingPositionField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_X);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_Y);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyEntity(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: pk.u D (posZ)",
                failure.getMessage());
    }

    @Test
    void entityShapeGateRejectsMissingRotationYawField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_X);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_Y);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_Z);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_DIMENSION);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyEntity(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: pk.y F (rotationYaw)",
                failure.getMessage());
    }

    @Test
    void entityShapeGateRejectsMissingDimensionField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_X);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_Y);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_Z);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_ROTATION_YAW);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyEntity(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: pk.am I (dimension)",
                failure.getMessage());
    }

    @Test
    void entityLivingBaseShapeGateRejectsMissingMaxHealthMethod() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY_LIVING_BASE
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_HEALTH);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyEntityLivingBase(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: pr.bu()F (getMaxHealth)",
                failure.getMessage());
    }

    @Test
    void entityLivingBaseShapeGateRejectsMissingEquipmentMethod() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY_LIVING_BASE
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_HEALTH);
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_MAX_HEALTH);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyEntityLivingBase(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: pr.p(I)Lzx; (getEquipmentInSlot)",
                failure.getMessage());
    }

    private static byte[] minecraftShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.MINECRAFT
                                .obfuscatedInternalName());
        addMinecraftFields(writer);
        addMethod(
                writer,
                Minecraft189Mappings.MINECRAFT_GET_MINECRAFT);
        addMethod(
                writer,
                Minecraft189Mappings.MINECRAFT_START_GAME);
        addMethod(
                writer,
                Minecraft189Mappings.MINECRAFT_RUN_TICK);
        addMethod(
                writer,
                Minecraft189Mappings.MINECRAFT_CLICK_MOUSE);
        addMethod(
                writer,
                Minecraft189Mappings.MINECRAFT_RIGHT_CLICK_MOUSE);
        addMethod(
                writer,
                Minecraft189Mappings.MINECRAFT_MIDDLE_CLICK_MOUSE);
        addMethod(
                writer,
                Minecraft189Mappings.MINECRAFT_DISPATCH_KEYPRESSES);
        return finish(writer);
    }

    private static byte[] keyBindingShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.KEY_BINDING
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings
                        .KEY_BINDING_SET_KEY_BIND_STATE);
        return finish(writer);
    }

    private static byte[] gameSettingsShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.GAME_SETTINGS
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_VIEW_BOBBING);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_FOV);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_GAMMA);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_GUI_SCALE);
        addField(
                writer,
                Minecraft189Mappings.GAME_SETTINGS_FORCE_UNICODE);
        return finish(writer);
    }

    private static byte[] fontRendererShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.FONT_RENDERER
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.FONT_RENDERER_DRAW_STRING);
        return finish(writer);
    }

    private static byte[] guiIngameShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.GUI_INGAME
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.GUI_INGAME_RENDER_OVERLAY);
        return finish(writer);
    }

    private static byte[] entityShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_X);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_Y);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_POS_Z);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_ROTATION_YAW);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_DIMENSION);
        return finish(writer);
    }

    private static byte[] entityLivingBaseShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY_LIVING_BASE
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_HEALTH);
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_MAX_HEALTH);
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_EQUIPMENT_IN_SLOT);
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_LIVING_BASE_GET_ACTIVE_POTION_EFFECTS);
        return finish(writer);
    }

    @Test
    void entityPlayerShapeGateRejectsMissingInventoryField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY_PLAYER
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_LEVEL);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_TOTAL);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_PROGRESS);
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_GET_FOOD_STATS);
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_XP_BAR_CAP);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyEntityPlayer(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: wn.bi Lwm; (inventory)",
                failure.getMessage());
    }

    @Test
    void inventoryPlayerShapeGateRejectsMissingCurrentItemField() {
        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyInventoryPlayer(
                                        emptyClass("wm")));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: wm.c I (currentItem)",
                failure.getMessage());
    }

    @Test
    void entityPlayerShapeGateRejectsMissingExperienceField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY_PLAYER
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_INVENTORY);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_LEVEL);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_TOTAL);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyEntityPlayer(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: wn.bD F (experience)",
                failure.getMessage());
    }

    @Test
    void entityPlayerShapeGateRejectsMissingXpBarCapMethod() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY_PLAYER
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_INVENTORY);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_LEVEL);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_TOTAL);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_PROGRESS);
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_GET_FOOD_STATS);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyEntityPlayer(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: wn.ck()I (xpBarCap)",
                failure.getMessage());
    }

    @Test
    void abstractClientPlayerShapeGateRejectsMissingPlayerInfoMethod() {
        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyAbstractClientPlayer(
                                        emptyClass("bet")));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: bet.b()Lbdc; (getPlayerInfo)",
                failure.getMessage());
    }

    @Test
    void networkPlayerInfoShapeGateRejectsMissingResponseTimeMethod() {
        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyNetworkPlayerInfo(
                                        emptyClass("bdc")));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: bdc.c()I (getResponseTime)",
                failure.getMessage());
    }

    private static byte[] abstractClientPlayerShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ABSTRACT_CLIENT_PLAYER
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.ABSTRACT_CLIENT_PLAYER_GET_PLAYER_INFO);
        return finish(writer);
    }

    private static byte[] networkPlayerInfoShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.NETWORK_PLAYER_INFO
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.NETWORK_PLAYER_INFO_GET_RESPONSE_TIME);
        return finish(writer);
    }

    @Test
    void itemStackShapeGateRejectsMissingStackSizeField() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ITEM_STACK
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.ITEM_STACK_GET_DISPLAY_NAME);
        addMethod(
                writer,
                Minecraft189Mappings.ITEM_STACK_GET_ITEM_DAMAGE);
        addMethod(
                writer,
                Minecraft189Mappings.ITEM_STACK_GET_MAX_DAMAGE);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyItemStack(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping field missing: zx.b I (stackSize)",
                failure.getMessage());
    }

    @Test
    void itemStackShapeGateRejectsMissingDisplayNameMethod() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ITEM_STACK
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.ITEM_STACK_SIZE);
        addMethod(
                writer,
                Minecraft189Mappings.ITEM_STACK_GET_ITEM_DAMAGE);
        addMethod(
                writer,
                Minecraft189Mappings.ITEM_STACK_GET_MAX_DAMAGE);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyItemStack(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: zx.q()Ljava/lang/String; (getDisplayName)",
                failure.getMessage());
    }

    private static byte[] itemStackShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ITEM_STACK
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.ITEM_STACK_SIZE);
        addMethod(
                writer,
                Minecraft189Mappings.ITEM_STACK_GET_DISPLAY_NAME);
        addMethod(
                writer,
                Minecraft189Mappings.ITEM_STACK_GET_ITEM_DAMAGE);
        addMethod(
                writer,
                Minecraft189Mappings.ITEM_STACK_GET_MAX_DAMAGE);
        return finish(writer);
    }

    @Test
    void foodStatsShapeGateRejectsMissingSaturationMethod() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.FOOD_STATS
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.FOOD_STATS_GET_FOOD_LEVEL);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyFoodStats(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: xg.e()F (getSaturationLevel)",
                failure.getMessage());
    }

    private static byte[] entityPlayerShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY_PLAYER
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_INVENTORY);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_LEVEL);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_TOTAL);
        addField(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_EXPERIENCE_PROGRESS);
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_GET_FOOD_STATS);
        addMethod(
                writer,
                Minecraft189Mappings.ENTITY_PLAYER_XP_BAR_CAP);
        return finish(writer);
    }

    private static byte[] inventoryPlayerShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.INVENTORY_PLAYER
                                .obfuscatedInternalName());
        addField(
                writer,
                Minecraft189Mappings.INVENTORY_PLAYER_CURRENT_ITEM);
        return finish(writer);
    }

    private static byte[] foodStatsShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.FOOD_STATS
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.FOOD_STATS_GET_FOOD_LEVEL);
        addMethod(
                writer,
                Minecraft189Mappings.FOOD_STATS_GET_SATURATION_LEVEL);
        return finish(writer);
    }

    @Test
    void potionEffectShapeGateRejectsMissingEffectNameMethod() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.POTION_EFFECT
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.POTION_EFFECT_GET_POTION_ID);
        addMethod(
                writer,
                Minecraft189Mappings.POTION_EFFECT_GET_DURATION);
        addMethod(
                writer,
                Minecraft189Mappings.POTION_EFFECT_GET_AMPLIFIER);

        final IllegalStateException failure =
                assertThrows(
                        IllegalStateException.class,
                        () -> Minecraft189ClassShapeVerifier
                                .verifyPotionEffect(
                                        finish(writer)));
        assertEquals(
                "Minecraft 1.8.9 mapping method missing: pf.g()Ljava/lang/String; (getEffectName)",
                failure.getMessage());
    }

    private static byte[] potionEffectShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.POTION_EFFECT
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings.POTION_EFFECT_GET_POTION_ID);
        addMethod(
                writer,
                Minecraft189Mappings.POTION_EFFECT_GET_DURATION);
        addMethod(
                writer,
                Minecraft189Mappings.POTION_EFFECT_GET_AMPLIFIER);
        addMethod(
                writer,
                Minecraft189Mappings.POTION_EFFECT_GET_EFFECT_NAME);
        return finish(writer);
    }

    private static byte[] entityRendererShape() {
        final ClassWriter writer =
                writer(
                        Minecraft189Mappings.ENTITY_RENDERER
                                .obfuscatedInternalName());
        addMethod(
                writer,
                Minecraft189Mappings
                        .ENTITY_RENDERER_UPDATE_CAMERA_AND_RENDER);
        return finish(writer);
    }

    private static void addMinecraftFields(
            final ClassWriter writer) {
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_PLAYER);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_WORLD);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_FONT_RENDERER);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_ENTITY_RENDERER);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_INGAME_GUI);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_GAME_SETTINGS);
        addField(
                writer,
                Minecraft189Mappings.MINECRAFT_CURRENT_SERVER_DATA);
    }

    private static ClassWriter writer(
            final String internalName) {
        final ClassWriter writer =
                new ClassWriter(0);
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                internalName,
                null,
                "java/lang/Object",
                null);
        return writer;
    }

    private static byte[] emptyClass(
            final String internalName) {
        return finish(
                writer(internalName));
    }

    private static void addField(
            final ClassWriter writer,
            final Minecraft189Mappings.MappedField field) {
        writer.visitField(
                Opcodes.ACC_PUBLIC,
                field.obfuscatedName(),
                field.descriptor(),
                null,
                null)
                .visitEnd();
    }

    private static void addMethod(
            final ClassWriter writer,
            final Minecraft189Mappings.MappedMethod method) {
        writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                method.obfuscatedName(),
                method.descriptor(),
                null,
                null)
                .visitEnd();
    }

    private static byte[] finish(
            final ClassWriter writer) {
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void assertClass(
            final Minecraft189Mappings.MappedClass mapped,
            final String obfuscated,
            final String mcp) {
        assertEquals(
                obfuscated,
                mapped.obfuscatedInternalName());
        assertEquals(
                obfuscated.replace('/', '.'),
                mapped.obfuscatedBinaryName());
        assertEquals(
                mcp,
                mapped.mcpInternalName());
    }

    private static void assertField(
            final Minecraft189Mappings.MappedField mapped,
            final Minecraft189Mappings.MappedClass owner,
            final String obfuscated,
            final String descriptor,
            final String searge,
            final String mcp) {
        assertEquals(
                owner,
                mapped.owner());
        assertEquals(
                obfuscated,
                mapped.obfuscatedName());
        assertEquals(
                descriptor,
                mapped.descriptor());
        assertEquals(
                searge,
                mapped.seargeName());
        assertEquals(
                mcp,
                mapped.mcpName());
    }

    private static void assertMethod(
            final Minecraft189Mappings.MappedMethod mapped,
            final Minecraft189Mappings.MappedClass owner,
            final String obfuscated,
            final String descriptor,
            final String searge,
            final String mcp) {
        assertEquals(
                owner,
                mapped.owner());
        assertEquals(
                obfuscated,
                mapped.obfuscatedName());
        assertEquals(
                descriptor,
                mapped.descriptor());
        assertEquals(
                searge,
                mapped.seargeName());
        assertEquals(
                mcp,
                mapped.mcpName());
    }
}
