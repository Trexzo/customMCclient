package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.bootstrap.BootstrapRuntimeSession;
import dev.trexzo.custommc.core.module.ModuleKeybindController;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void bootstrapOwnsExactlyOneHostRuntimeAndClosesItBeforeDetach() {
        final Minecraft189BootstrapRuntime runtime =
                (Minecraft189BootstrapRuntime)
                        new Minecraft189BootstrapInitializer()
                                .initialize(
                                        new BootstrapContext(
                                                "net.minecraft.client.main.Main",
                                                new String[0]));

        assertFalse(runtime.hostInstalled());

        final Minecraft189HostRuntime hostRuntime =
                runtime.installHost(
                        new NoOpHostCallbacks());

        assertTrue(runtime.hostInstalled());
        assertTrue(
                runtime.services()
                        .contains(
                                ClickGuiModel.class));
        assertTrue(
                runtime.services()
                        .contains(
                                ClickGuiInputController.class));

        assertThrows(
                IllegalStateException.class,
                () -> runtime.installHost(
                        new NoOpHostCallbacks()));

        runtime.close();

        assertTrue(runtime.closed());
        assertTrue(hostRuntime.closed());
        assertFalse(runtime.hostInstalled());
        assertFalse(
                runtime.services()
                        .contains(
                                ClickGuiModel.class));
        assertFalse(
                runtime.services()
                        .contains(
                                ClickGuiInputController.class));
        assertFalse(
                runtime.platform()
                        .attached());

        assertThrows(
                IllegalStateException.class,
                () -> runtime.installHost(
                        new NoOpHostCallbacks()));
    }

    private static final class NoOpHostCallbacks
            implements LegacyUiHostCallbacks {
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
        }

        @Override
        public void endUi() {
        }
    }
}
