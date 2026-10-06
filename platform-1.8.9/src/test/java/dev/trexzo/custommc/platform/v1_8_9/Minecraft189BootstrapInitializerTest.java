package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.bootstrap.BootstrapRuntimeSession;
import dev.trexzo.custommc.core.module.ModuleKeybindController;
import dev.trexzo.custommc.core.render.RenderPipeline;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189BootstrapInitializerTest {
    @Test
    void initializerAssemblesExplicitRuntimeOwnersAndServices() {
        final BootstrapContext context =
                new BootstrapContext(
                        "net.minecraft.client.main.Main",
                        new String[]{
                                "--username",
                                "Player One"
                        });

        final BootstrapRuntimeSession session =
                new Minecraft189BootstrapInitializer()
                        .initialize(context);

        assertTrue(
                session instanceof Minecraft189BootstrapRuntime);

        final Minecraft189BootstrapRuntime runtime =
                (Minecraft189BootstrapRuntime) session;

        assertSame(
                context,
                runtime.bootstrapContext());
        assertArrayEquals(
                new String[]{
                        "--username",
                        "Player One"
                },
                runtime.bootstrapContext()
                        .targetArguments());

        assertTrue(
                runtime.platform()
                        .attached());
        assertFalse(runtime.closed());

        assertSame(
                runtime.renderPipeline(),
                runtime.services()
                        .require(
                                RenderPipeline.class));
        assertTrue(
                runtime.services()
                        .contains(
                                ModuleKeybindController.class));

        assertTrue(
                runtime.modules()
                        .snapshot()
                        .isEmpty());
        assertTrue(
                runtime.settings()
                        .snapshot()
                        .isEmpty());

        runtime.close();

        assertTrue(runtime.closed());
        assertFalse(
                runtime.platform()
                        .attached());
        assertFalse(
                runtime.services()
                        .contains(
                                RenderPipeline.class));
        assertFalse(
                runtime.services()
                        .contains(
                                ModuleKeybindController.class));
        assertTrue(
                runtime.moduleKeybindAssignments()
                        .closed());

        runtime.close();
        assertTrue(runtime.closed());
    }
}
