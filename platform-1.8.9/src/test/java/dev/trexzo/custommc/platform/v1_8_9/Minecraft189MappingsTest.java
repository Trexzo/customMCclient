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
                Minecraft189Mappings.ENTITY,
                "pk",
                "net/minecraft/entity/Entity");
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
    }

    @Test
    void mappedClassShapeGatesAcceptExactOwnersAndMembers() {
        Minecraft189ClassShapeVerifier.verifyMinecraft(
                minecraftShape());
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
