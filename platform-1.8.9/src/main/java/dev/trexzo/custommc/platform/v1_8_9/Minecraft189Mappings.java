package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Objects;

public final class Minecraft189Mappings {
    public static final String VERSION =
            "1.8.9";

    public static final String SOURCE_REPOSITORY =
            "BigBroadBean/mappings-extracted";
    public static final String SOURCE_COMMIT =
            "2265da88e93c20411ec70f0b892f4fbc84ebc3c9";
    public static final String CLASSES_BLOB_SHA =
            "17967e48db6c8ec20ae622409be13971e2c77706";
    public static final String FIELDS_BLOB_SHA =
            "a8c5928cb64455dcc2712078dbfe018ae97cafb9";
    public static final String METHODS_BLOB_SHA =
            "683d45abbd02a1525008c5654a40b50efbc47c6d";
    public static final String JOINED_SRG_BLOB_SHA =
            "0b1e3f1d0156abcbd70e2b09b720379fc0c1eae6";

    public static final MappedClass MINECRAFT =
            new MappedClass(
                    "ave",
                    "net/minecraft/client/Minecraft");
    public static final MappedClass WORLD =
            new MappedClass(
                    "adm",
                    "net/minecraft/world/World");
    public static final MappedClass WORLD_CLIENT =
            new MappedClass(
                    "bdb",
                    "net/minecraft/client/multiplayer/WorldClient");
    public static final MappedClass PLAYER_CONTROLLER_MP =
            new MappedClass(
                    "bda",
                    "net/minecraft/client/multiplayer/PlayerControllerMP");
    public static final MappedClass KEY_BINDING =
            new MappedClass(
                    "avb",
                    "net/minecraft/client/settings/KeyBinding");
    public static final MappedClass GAME_SETTINGS =
            new MappedClass(
                    "avh",
                    "net/minecraft/client/settings/GameSettings");
    public static final MappedClass FONT_RENDERER =
            new MappedClass(
                    "avn",
                    "net/minecraft/client/gui/FontRenderer");
    public static final MappedClass GUI_INGAME =
            new MappedClass(
                    "avo",
                    "net/minecraft/client/gui/GuiIngame");
    public static final MappedClass ENTITY_RENDERER =
            new MappedClass(
                    "bfk",
                    "net/minecraft/client/renderer/EntityRenderer");
    public static final MappedClass ENTITY_PLAYER_SP =
            new MappedClass(
                    "bew",
                    "net/minecraft/client/entity/EntityPlayerSP");
    public static final MappedClass ABSTRACT_CLIENT_PLAYER =
            new MappedClass(
                    "bet",
                    "net/minecraft/client/entity/AbstractClientPlayer");
    public static final MappedClass NETWORK_PLAYER_INFO =
            new MappedClass(
                    "bdc",
                    "net/minecraft/client/network/NetworkPlayerInfo");
    public static final MappedClass SERVER_DATA =
            new MappedClass(
                    "bde",
                    "net/minecraft/client/multiplayer/ServerData");
    public static final MappedClass ENTITY_PLAYER =
            new MappedClass(
                    "wn",
                    "net/minecraft/entity/player/EntityPlayer");
    public static final MappedClass INVENTORY_PLAYER =
            new MappedClass(
                    "wm",
                    "net/minecraft/entity/player/InventoryPlayer");
    public static final MappedClass ENTITY =
            new MappedClass(
                    "pk",
                    "net/minecraft/entity/Entity");
    public static final MappedClass ENTITY_LIVING_BASE =
            new MappedClass(
                    "pr",
                    "net/minecraft/entity/EntityLivingBase");
    public static final MappedClass ITEM_STACK =
            new MappedClass(
                    "zx",
                    "net/minecraft/item/ItemStack");
    // Vanilla MCP 1.8.9 class identities, not display-name heuristics.
    // The ItemStack-to-Item field is located by its unique verified type.
    public static final MappedClass ITEM =
            new MappedClass("zw", "net/minecraft/item/Item");
    public static final MappedClass ITEM_SWORD =
            new MappedClass("aay", "net/minecraft/item/ItemSword");
    public static final MappedClass ITEM_FISHING_ROD =
            new MappedClass("zq", "net/minecraft/item/ItemFishingRod");
    public static final MappedClass ITEM_POTION =
            new MappedClass("aai", "net/minecraft/item/ItemPotion");
    public static final MappedClass FOOD_STATS =
            new MappedClass(
                    "xg",
                    "net/minecraft/util/FoodStats");
    public static final MappedClass MOVING_OBJECT_POSITION =
            new MappedClass(
                    "auh",
                    "net/minecraft/util/MovingObjectPosition");
    public static final MappedClass MOVING_OBJECT_TYPE =
            new MappedClass(
                    "auh$a",
                    "net/minecraft/util/MovingObjectPosition$MovingObjectType");
    public static final MappedClass MOVEMENT_INPUT =
            new MappedClass(
                    "beu",
                    "net/minecraft/util/MovementInput");
    public static final MappedClass TIMER =
            new MappedClass(
                    "avl",
                    "net/minecraft/util/Timer");
    public static final MappedClass POTION_EFFECT =
            new MappedClass(
                    "pf",
                    "net/minecraft/potion/PotionEffect");

    // Proven in the pinned joined.srg blob (0b1e3f1d...).
    // Crosshair hit state is separate from proximity-only player targeting.
    public static final MappedField MINECRAFT_OBJECT_MOUSE_OVER =
            new MappedField(
                    MINECRAFT,
                    "s",
                    "Lauh;",
                    "field_71476_x",
                    "objectMouseOver");
    public static final MappedField MOVING_OBJECT_TYPE_OF_HIT =
            new MappedField(
                    MOVING_OBJECT_POSITION,
                    "a",
                    "Lauh$a;",
                    "field_72313_a",
                    "typeOfHit");
    public static final MappedField MOVING_OBJECT_ENTITY_HIT =
            new MappedField(
                    MOVING_OBJECT_POSITION,
                    "d",
                    "Lpk;",
                    "field_72308_g",
                    "entityHit");
    public static final MappedField MOVING_OBJECT_TYPE_ENTITY =
            new MappedField(
                    MOVING_OBJECT_TYPE,
                    "c",
                    "Lauh$a;",
                    "ENTITY",
                    "ENTITY");
    public static final MappedField MINECRAFT_PLAYER =
            new MappedField(
                    MINECRAFT,
                    "h",
                    "Lbew;",
                    "field_71439_g",
                    "thePlayer");
    public static final MappedField MINECRAFT_WORLD =
            new MappedField(
                    MINECRAFT,
                    "f",
                    "Lbdb;",
                    "field_71441_e",
                    "theWorld");
    public static final MappedField MINECRAFT_PLAYER_CONTROLLER =
            new MappedField(
                    MINECRAFT,
                    "c",
                    "Lbda;",
                    "field_71442_b",
                    "playerController");
    public static final MappedField MINECRAFT_FONT_RENDERER =
            new MappedField(
                    MINECRAFT,
                    "k",
                    "Lavn;",
                    "field_71466_p",
                    "fontRendererObj");
    public static final MappedField MINECRAFT_ENTITY_RENDERER =
            new MappedField(
                    MINECRAFT,
                    "o",
                    "Lbfk;",
                    "field_71460_t",
                    "entityRenderer");
    public static final MappedField MINECRAFT_INGAME_GUI =
            new MappedField(
                    MINECRAFT,
                    "q",
                    "Lavo;",
                    "field_71456_v",
                    "ingameGUI");
    public static final MappedField MINECRAFT_GAME_SETTINGS =
            new MappedField(
                    MINECRAFT,
                    "t",
                    "Lavh;",
                    "field_71474_y",
                    "gameSettings");
    public static final MappedField MINECRAFT_CURRENT_SERVER_DATA =
            new MappedField(
                    MINECRAFT,
                    "Q",
                    "Lbde;",
                    "field_71422_O",
                    "currentServerData");
    public static final MappedField MINECRAFT_RIGHT_CLICK_DELAY_TIMER =
            new MappedField(
                    MINECRAFT,
                    "ap",
                    "I",
                    "field_71467_ac",
                    "rightClickDelayTimer");
    public static final MappedField MINECRAFT_LEFT_CLICK_COUNTER =
            new MappedField(
                    MINECRAFT,
                    "ag",
                    "I",
                    "field_71429_W",
                    "leftClickCounter");
    public static final MappedField MINECRAFT_TIMER =
            new MappedField(
                    MINECRAFT,
                    "Y",
                    "Lavl;",
                    "field_71428_T",
                    "timer");
    public static final MappedField WORLD_LOADED_ENTITY_LIST =
            new MappedField(
                    WORLD,
                    "f",
                    "Ljava/util/List;",
                    "field_72996_f",
                    "loadedEntityList");
    public static final MappedField TIMER_SPEED =
            new MappedField(
                    TIMER,
                    "d",
                    "F",
                    "field_74278_d",
                    "timerSpeed");
    public static final MappedField PLAYER_CONTROLLER_BLOCK_HIT_DELAY =
            new MappedField(
                    PLAYER_CONTROLLER_MP,
                    "g",
                    "I",
                    "field_78781_i",
                    "blockHitDelay");
    public static final MappedField PLAYER_CONTROLLER_CUR_BLOCK_DAMAGE =
            new MappedField(
                    PLAYER_CONTROLLER_MP,
                    "e",
                    "F",
                    "field_78770_f",
                    "curBlockDamageMP");
    public static final MappedField PLAYER_CONTROLLER_IS_HITTING_BLOCK =
            new MappedField(
                    PLAYER_CONTROLLER_MP,
                    "h",
                    "Z",
                    "field_78778_j",
                    "isHittingBlock");
    public static final MappedField SERVER_DATA_SERVER_IP =
            new MappedField(
                    SERVER_DATA,
                    "b",
                    "Ljava/lang/String;",
                    "field_78845_b",
                    "serverIP");
    public static final MappedField ITEM_STACK_SIZE =
            new MappedField(
                    ITEM_STACK,
                    "b",
                    "I",
                    "field_77994_a",
                    "stackSize");
    public static final MappedField ENTITY_POS_X =
            new MappedField(
                    ENTITY,
                    "s",
                    "D",
                    "field_70165_t",
                    "posX");
    public static final MappedField ENTITY_POS_Y =
            new MappedField(
                    ENTITY,
                    "t",
                    "D",
                    "field_70163_u",
                    "posY");
    public static final MappedField ENTITY_POS_Z =
            new MappedField(
                    ENTITY,
                    "u",
                    "D",
                    "field_70161_v",
                    "posZ");
    public static final MappedField ENTITY_ROTATION_YAW =
            new MappedField(
                    ENTITY,
                    "y",
                    "F",
                    "field_70177_z",
                    "rotationYaw");
    public static final MappedField ENTITY_ROTATION_PITCH =
            new MappedField(
                    ENTITY,
                    "z",
                    "F",
                    "field_70125_A",
                    "rotationPitch");
    public static final MappedField ENTITY_MOTION_X =
            new MappedField(
                    ENTITY,
                    "v",
                    "D",
                    "field_70159_w",
                    "motionX");
    public static final MappedField ENTITY_MOTION_Y =
            new MappedField(
                    ENTITY,
                    "w",
                    "D",
                    "field_70181_x",
                    "motionY");
    public static final MappedField ENTITY_MOTION_Z =
            new MappedField(
                    ENTITY,
                    "x",
                    "D",
                    "field_70179_y",
                    "motionZ");
    public static final MappedField ENTITY_DIMENSION =
            new MappedField(
                    ENTITY,
                    "am",
                    "I",
                    "field_71093_bK",
                    "dimension");
    public static final MappedField ENTITY_ON_GROUND =
            new MappedField(
                    ENTITY,
                    "C",
                    "Z",
                    "field_70122_E",
                    "onGround");
    public static final MappedField ENTITY_STEP_HEIGHT =
            new MappedField(
                    ENTITY,
                    "S",
                    "F",
                    "field_70138_W",
                    "stepHeight");
    public static final MappedField ENTITY_FALL_DISTANCE =
            new MappedField(
                    ENTITY,
                    "O",
                    "F",
                    "field_70143_R",
                    "fallDistance");
    public static final MappedField ENTITY_IS_IN_WEB =
            new MappedField(
                    ENTITY,
                    "H",
                    "Z",
                    "field_70134_J",
                    "isInWeb");
    public static final MappedField ENTITY_NO_CLIP =
            new MappedField(
                    ENTITY,
                    "T",
                    "Z",
                    "field_70145_X",
                    "noClip");
    public static final MappedField ENTITY_LIVING_BASE_HURT_TIME =
            new MappedField(
                    ENTITY_LIVING_BASE,
                    "au",
                    "I",
                    "field_70737_aN",
                    "hurtTime");
    public static final MappedField ENTITY_PLAYER_SP_MOVEMENT_INPUT =
            new MappedField(
                    ENTITY_PLAYER_SP,
                    "b",
                    "Lbeu;",
                    "field_71158_b",
                    "movementInput");
    public static final MappedField MOVEMENT_INPUT_MOVE_STRAFE =
            new MappedField(
                    MOVEMENT_INPUT,
                    "a",
                    "F",
                    "field_78902_a",
                    "moveStrafe");
    public static final MappedField MOVEMENT_INPUT_MOVE_FORWARD =
            new MappedField(
                    MOVEMENT_INPUT,
                    "b",
                    "F",
                    "field_78900_b",
                    "moveForward");
    public static final MappedField ENTITY_PLAYER_INVENTORY =
            new MappedField(
                    ENTITY_PLAYER,
                    "bi",
                    "Lwm;",
                    "field_71071_by",
                    "inventory");
    // Exact MCP 1.8.9 joined.srg: wm.a [Lzx; field_70462_a.
    public static final MappedField INVENTORY_PLAYER_MAIN_INVENTORY =
            new MappedField(INVENTORY_PLAYER, "a", "[Lzx;",
                    "field_70462_a", "mainInventory");
    public static final MappedField INVENTORY_PLAYER_CURRENT_ITEM =
            new MappedField(
                    INVENTORY_PLAYER,
                    "c",
                    "I",
                    "field_70461_c",
                    "currentItem");

    public static final MappedField ENTITY_PLAYER_EXPERIENCE_LEVEL =
            new MappedField(
                    ENTITY_PLAYER,
                    "bB",
                    "I",
                    "field_71068_ca",
                    "experienceLevel");
    public static final MappedField ENTITY_PLAYER_EXPERIENCE_TOTAL =
            new MappedField(
                    ENTITY_PLAYER,
                    "bC",
                    "I",
                    "field_71067_cb",
                    "experienceTotal");
    public static final MappedField ENTITY_PLAYER_EXPERIENCE_PROGRESS =
            new MappedField(
                    ENTITY_PLAYER,
                    "bD",
                    "F",
                    "field_71106_cc",
                    "experience");

    public static final MappedField GAME_SETTINGS_VIEW_BOBBING =
            new MappedField(
                    GAME_SETTINGS,
                    "d",
                    "Z",
                    "field_74336_f",
                    "viewBobbing");
    public static final MappedField GAME_SETTINGS_FOV =
            new MappedField(
                    GAME_SETTINGS,
                    "aI",
                    "F",
                    "field_74334_X",
                    "fovSetting");
    public static final MappedField GAME_SETTINGS_GAMMA =
            new MappedField(
                    GAME_SETTINGS,
                    "aJ",
                    "F",
                    "field_74333_Y",
                    "gammaSetting");
    public static final MappedField GAME_SETTINGS_GUI_SCALE =
            new MappedField(
                    GAME_SETTINGS,
                    "aL",
                    "I",
                    "field_74335_Z",
                    "guiScale");
    public static final MappedField GAME_SETTINGS_FORCE_UNICODE =
            new MappedField(
                    GAME_SETTINGS,
                    "aO",
                    "Z",
                    "field_151455_aw",
                    "forceUnicodeFont");

    public static final MappedMethod ENTITY_LIVING_BASE_KNOCK_BACK =
            new MappedMethod(
                    ENTITY_LIVING_BASE,
                    "a",
                    "(Lpk;FDD)V",
                    "func_70653_a",
                    "knockBack");

    public static final MappedMethod ENTITY_PLAYER_SP_ON_LIVING_UPDATE =
            new MappedMethod(
                    ENTITY_PLAYER_SP,
                    "m",
                    "()V",
                    "func_70636_d",
                    "onLivingUpdate");

    // MCP 1.8.9 official joined.srg: pk/aK ()Ljava/util/UUID;
    // Source-backed Entity.func_110124_au/getUniqueID.
    public static final MappedMethod ENTITY_GET_UNIQUE_ID =
            new MappedMethod(ENTITY, "aK", "()Ljava/util/UUID;",
                    "func_110124_au", "getUniqueID");

    public static final MappedMethod ENTITY_IS_SNEAKING =
            new MappedMethod(
                    ENTITY,
                    "av",
                    "()Z",
                    "func_70093_af",
                    "isSneaking");
    public static final MappedMethod ENTITY_IS_SPRINTING =
            new MappedMethod(
                    ENTITY,
                    "aw",
                    "()Z",
                    "func_70051_ag",
                    "isSprinting");
    public static final MappedMethod ENTITY_SET_SPRINTING =
            new MappedMethod(
                    ENTITY,
                    "d",
                    "(Z)V",
                    "func_70031_b",
                    "setSprinting");
    public static final MappedMethod ENTITY_SET_SNEAKING =
            new MappedMethod(
                    ENTITY,
                    "c",
                    "(Z)V",
                    "func_70095_a",
                    "setSneaking");

    public static final MappedMethod WORLD_GET_WORLD_TIME =
            new MappedMethod(
                    WORLD,
                    "L",
                    "()J",
                    "func_72820_D",
                    "getWorldTime");
    public static final MappedMethod WORLD_IS_RAINING =
            new MappedMethod(
                    WORLD,
                    "S",
                    "()Z",
                    "func_72896_J",
                    "isRaining");
    public static final MappedMethod WORLD_IS_THUNDERING =
            new MappedMethod(
                    WORLD,
                    "R",
                    "()Z",
                    "func_72911_I",
                    "isThundering");

    public static final MappedMethod MINECRAFT_GET_MINECRAFT =
            new MappedMethod(
                    MINECRAFT,
                    "A",
                    "()Lave;",
                    "func_71410_x",
                    "getMinecraft");
    public static final MappedMethod MINECRAFT_START_GAME =
            new MappedMethod(
                    MINECRAFT,
                    "am",
                    "()V",
                    "func_71384_a",
                    "startGame");
    public static final MappedMethod MINECRAFT_RUN_TICK =
            new MappedMethod(
                    MINECRAFT,
                    "s",
                    "()V",
                    "func_71407_l",
                    "runTick");
    public static final MappedMethod MINECRAFT_CLICK_MOUSE =
            new MappedMethod(
                    MINECRAFT,
                    "aw",
                    "()V",
                    "func_147116_af",
                    "clickMouse");
    public static final MappedMethod MINECRAFT_RIGHT_CLICK_MOUSE =
            new MappedMethod(
                    MINECRAFT,
                    "ax",
                    "()V",
                    "func_147121_ag",
                    "rightClickMouse");
    // Pinned Minecraft 1.8.9 joined.srg source mapping.
    public static final MappedMethod PLAYER_CONTROLLER_STOP_USING_ITEM =
            new MappedMethod(PLAYER_CONTROLLER_MP, "c", "(Lwn;)V",
                    "func_78766_c", "onStoppedUsingItem");
    public static final MappedMethod MINECRAFT_MIDDLE_CLICK_MOUSE =
            new MappedMethod(
                    MINECRAFT,
                    "az",
                    "()V",
                    "func_147112_ai",
                    "middleClickMouse");
    public static final MappedMethod MINECRAFT_DISPATCH_KEYPRESSES =
            new MappedMethod(
                    MINECRAFT,
                    "Z",
                    "()V",
                    "func_152348_aa",
                    "dispatchKeypresses");
    public static final MappedMethod KEY_BINDING_SET_KEY_BIND_STATE =
            new MappedMethod(
                    KEY_BINDING,
                    "a",
                    "(IZ)V",
                    "func_74510_a",
                    "setKeyBindState");
    public static final MappedMethod FONT_RENDERER_DRAW_STRING =
            new MappedMethod(
                    FONT_RENDERER,
                    "a",
                    "(Ljava/lang/String;FFIZ)I",
                    "func_175065_a",
                    "drawString");
    public static final MappedMethod GUI_INGAME_RENDER_OVERLAY =
            new MappedMethod(
                    GUI_INGAME,
                    "a",
                    "(F)V",
                    "func_175180_a",
                    "renderGameOverlay");
    public static final MappedMethod ENTITY_RENDERER_UPDATE_CAMERA_AND_RENDER =
            new MappedMethod(
                    ENTITY_RENDERER,
                    "a",
                    "(FJ)V",
                    "func_181560_a",
                    "updateCameraAndRender");
    public static final MappedMethod ENTITY_LIVING_BASE_GET_HEALTH =
            new MappedMethod(
                    ENTITY_LIVING_BASE,
                    "bn",
                    "()F",
                    "func_110143_aJ",
                    "getHealth");
    public static final MappedMethod ENTITY_LIVING_BASE_GET_MAX_HEALTH =
            new MappedMethod(
                    ENTITY_LIVING_BASE,
                    "bu",
                    "()F",
                    "func_110138_aP",
                    "getMaxHealth");
    // Pinned vanilla 1.8.9 joined.srg pr.c(Lpr;)Z.
    public static final MappedMethod ENTITY_LIVING_BASE_IS_ON_SAME_TEAM =
            new MappedMethod(ENTITY_LIVING_BASE, "c", "(Lpr;)Z",
                    "func_142014_c", "isOnSameTeam");
    public static final MappedMethod ENTITY_LIVING_BASE_GET_EQUIPMENT_IN_SLOT =
            new MappedMethod(
                    ENTITY_LIVING_BASE,
                    "p",
                    "(I)Lzx;",
                    "func_71124_b",
                    "getEquipmentInSlot");
    public static final MappedMethod ENTITY_LIVING_BASE_JUMP =
            new MappedMethod(
                    ENTITY_LIVING_BASE,
                    "bF",
                    "()V",
                    "func_70664_aZ",
                    "jump");
    public static final MappedMethod ENTITY_PLAYER_IS_USING_ITEM =
            new MappedMethod(ENTITY_PLAYER, "bS", "()Z",
                    "func_71039_bw", "isUsingItem");
    public static final MappedMethod ENTITY_PLAYER_GET_FOOD_STATS =
            new MappedMethod(
                    ENTITY_PLAYER,
                    "cl",
                    "()Lxg;",
                    "func_71024_bL",
                    "getFoodStats");
    public static final MappedMethod ENTITY_PLAYER_XP_BAR_CAP =
            new MappedMethod(
                    ENTITY_PLAYER,
                    "ck",
                    "()I",
                    "func_71050_bK",
                    "xpBarCap");
    public static final MappedMethod ABSTRACT_CLIENT_PLAYER_GET_PLAYER_INFO =
            new MappedMethod(
                    ABSTRACT_CLIENT_PLAYER,
                    "b",
                    "()Lbdc;",
                    "func_175155_b",
                    "getPlayerInfo");
    public static final MappedMethod NETWORK_PLAYER_INFO_GET_RESPONSE_TIME =
            new MappedMethod(
                    NETWORK_PLAYER_INFO,
                    "c",
                    "()I",
                    "func_178853_c",
                    "getResponseTime");
    // Exact MCP 1.8.9 joined.srg: aay.g()F / func_150931_i.
    public static final MappedMethod ITEM_SWORD_GET_DAMAGE_VS_ENTITY =
            new MappedMethod(ITEM_SWORD, "g", "()F",
                    "func_150931_i", "getDamageVsEntity");
    public static final MappedMethod ITEM_STACK_GET_DISPLAY_NAME =
            new MappedMethod(
                    ITEM_STACK,
                    "q",
                    "()Ljava/lang/String;",
                    "func_82833_r",
                    "getDisplayName");
    public static final MappedMethod ITEM_STACK_GET_ITEM_DAMAGE =
            new MappedMethod(
                    ITEM_STACK,
                    "h",
                    "()I",
                    "func_77952_i",
                    "getItemDamage");
    public static final MappedMethod ITEM_STACK_GET_MAX_DAMAGE =
            new MappedMethod(
                    ITEM_STACK,
                    "j",
                    "()I",
                    "func_77958_k",
                    "getMaxDamage");

    public static final MappedMethod FOOD_STATS_GET_FOOD_LEVEL =
            new MappedMethod(
                    FOOD_STATS,
                    "a",
                    "()I",
                    "func_75116_a",
                    "getFoodLevel");
    public static final MappedMethod FOOD_STATS_GET_SATURATION_LEVEL =
            new MappedMethod(
                    FOOD_STATS,
                    "e",
                    "()F",
                    "func_75115_e",
                    "getSaturationLevel");
    public static final MappedMethod ENTITY_LIVING_BASE_GET_ACTIVE_POTION_EFFECTS =
            new MappedMethod(
                    ENTITY_LIVING_BASE,
                    "bl",
                    "()Ljava/util/Collection;",
                    "func_70651_bq",
                    "getActivePotionEffects");
    public static final MappedMethod POTION_EFFECT_GET_POTION_ID =
            new MappedMethod(
                    POTION_EFFECT,
                    "a",
                    "()I",
                    "func_76456_a",
                    "getPotionID");
    public static final MappedMethod POTION_EFFECT_GET_DURATION =
            new MappedMethod(
                    POTION_EFFECT,
                    "b",
                    "()I",
                    "func_76459_b",
                    "getDuration");
    public static final MappedMethod POTION_EFFECT_GET_AMPLIFIER =
            new MappedMethod(
                    POTION_EFFECT,
                    "c",
                    "()I",
                    "func_76458_c",
                    "getAmplifier");
    public static final MappedMethod POTION_EFFECT_GET_EFFECT_NAME =
            new MappedMethod(
                    POTION_EFFECT,
                    "g",
                    "()Ljava/lang/String;",
                    "func_76453_d",
                    "getEffectName");

    private Minecraft189Mappings() {
    }

    public static final class MappedClass {
        private final String obfuscatedInternalName;
        private final String mcpInternalName;

        private MappedClass(
                final String obfuscatedInternalName,
                final String mcpInternalName) {
            this.obfuscatedInternalName =
                    requireText(
                            obfuscatedInternalName,
                            "obfuscatedInternalName");
            this.mcpInternalName =
                    requireText(
                            mcpInternalName,
                            "mcpInternalName");
        }

        public String obfuscatedInternalName() {
            return obfuscatedInternalName;
        }

        public String obfuscatedBinaryName() {
            return obfuscatedInternalName.replace(
                    '/',
                    '.');
        }

        public String mcpInternalName() {
            return mcpInternalName;
        }
    }

    public static final class MappedField {
        private final MappedClass owner;
        private final String obfuscatedName;
        private final String descriptor;
        private final String seargeName;
        private final String mcpName;

        private MappedField(
                final MappedClass owner,
                final String obfuscatedName,
                final String descriptor,
                final String seargeName,
                final String mcpName) {
            this.owner =
                    Objects.requireNonNull(
                            owner,
                            "owner");
            this.obfuscatedName =
                    requireText(
                            obfuscatedName,
                            "obfuscatedName");
            this.descriptor =
                    requireText(
                            descriptor,
                            "descriptor");
            this.seargeName =
                    requireText(
                            seargeName,
                            "seargeName");
            this.mcpName =
                    requireText(
                            mcpName,
                            "mcpName");
        }

        public MappedClass owner() {
            return owner;
        }

        public String obfuscatedName() {
            return obfuscatedName;
        }

        public String descriptor() {
            return descriptor;
        }

        public String seargeName() {
            return seargeName;
        }

        public String mcpName() {
            return mcpName;
        }
    }

    public static final class MappedMethod {
        private final MappedClass owner;
        private final String obfuscatedName;
        private final String descriptor;
        private final String seargeName;
        private final String mcpName;

        private MappedMethod(
                final MappedClass owner,
                final String obfuscatedName,
                final String descriptor,
                final String seargeName,
                final String mcpName) {
            this.owner =
                    Objects.requireNonNull(
                            owner,
                            "owner");
            this.obfuscatedName =
                    requireText(
                            obfuscatedName,
                            "obfuscatedName");
            this.descriptor =
                    requireText(
                            descriptor,
                            "descriptor");
            this.seargeName =
                    requireText(
                            seargeName,
                            "seargeName");
            this.mcpName =
                    requireText(
                            mcpName,
                            "mcpName");
        }

        public MappedClass owner() {
            return owner;
        }

        public String obfuscatedName() {
            return obfuscatedName;
        }

        public String descriptor() {
            return descriptor;
        }

        public String seargeName() {
            return seargeName;
        }

        public String mcpName() {
            return mcpName;
        }
    }

    private static String requireText(
            final String value,
            final String name) {
        final String text =
                Objects.requireNonNull(
                        value,
                        name)
                        .trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(
                    name + " must not be blank");
        }
        return text;
    }
}
