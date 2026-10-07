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
    public static final MappedClass ENTITY_PLAYER =
            new MappedClass(
                    "wn",
                    "net/minecraft/entity/player/EntityPlayer");
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
    public static final MappedClass FOOD_STATS =
            new MappedClass(
                    "xg",
                    "net/minecraft/util/FoodStats");
    public static final MappedClass POTION_EFFECT =
            new MappedClass(
                    "pf",
                    "net/minecraft/potion/PotionEffect");

    public static final MappedField MINECRAFT_PLAYER =
            new MappedField(
                    MINECRAFT,
                    "h",
                    "Lbew;",
                    "field_71439_g",
                    "thePlayer");
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
    public static final MappedMethod ENTITY_LIVING_BASE_GET_EQUIPMENT_IN_SLOT =
            new MappedMethod(
                    ENTITY_LIVING_BASE,
                    "p",
                    "(I)Lzx;",
                    "func_71124_b",
                    "getEquipmentInSlot");
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
