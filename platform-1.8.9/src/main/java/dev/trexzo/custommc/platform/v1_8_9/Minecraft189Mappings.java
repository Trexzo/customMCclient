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
    public static final MappedClass ENTITY_PLAYER_SP =
            new MappedClass(
                    "bew",
                    "net/minecraft/client/entity/EntityPlayerSP");
    public static final MappedClass ENTITY =
            new MappedClass(
                    "pk",
                    "net/minecraft/entity/Entity");
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

    public static final MappedField MINECRAFT_THE_PLAYER =
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
