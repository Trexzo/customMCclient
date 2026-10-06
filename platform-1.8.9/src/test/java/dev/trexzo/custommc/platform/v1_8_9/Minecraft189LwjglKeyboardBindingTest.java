package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189LwjglKeyboardBindingTest {
    @Test
    void currentKeyboardEventRoutesThroughInstalledHostWithoutOwningTheQueue() {
        final Minecraft189BootstrapRuntime runtime =
                Minecraft189BootstrapRuntime.create(
                        new BootstrapContext(
                                Minecraft189ClassTransformer
                                        .TARGET_MAIN_CLASS,
                                new String[0]));
        try {
            runtime.installHost(
                    new NoOpHostCallbacks());

            final ClickGuiModel model =
                    runtime.services()
                            .require(
                                    ClickGuiModel.class);
            model.open();

            final FakeKeyboardEventSource escape =
                    new FakeKeyboardEventSource(
                            LegacyKeyboardCodes.ESCAPE,
                            '\0',
                            true,
                            false);

            assertTrue(
                    Minecraft189LwjglKeyboardBinding
                            .forward(escape));
            assertFalse(
                    model.snapshot()
                            .open());

            assertFalse(
                    Minecraft189LwjglKeyboardBinding
                            .forward(
                                    new FakeKeyboardEventSource(
                                            LegacyKeyboardCodes.ESCAPE,
                                            '\0',
                                            false,
                                            false)));
        } finally {
            runtime.close();
        }
    }

    @Test
    void eventForwardingIsInertBeforeHostInstallAndAfterRuntimeClose() {
        final FakeKeyboardEventSource source =
                new FakeKeyboardEventSource(
                        LegacyKeyboardCodes.ESCAPE,
                        '\0',
                        true,
                        false);

        final Minecraft189BootstrapRuntime runtime =
                Minecraft189BootstrapRuntime.create(
                        new BootstrapContext(
                                Minecraft189ClassTransformer
                                        .TARGET_MAIN_CLASS,
                                new String[0]));

        assertFalse(
                Minecraft189LwjglKeyboardBinding
                        .forward(source));

        runtime.close();

        assertFalse(
                Minecraft189LwjglKeyboardBinding
                        .forward(source));
    }

    private static final class FakeKeyboardEventSource
            implements Minecraft189LwjglKeyboardBinding
            .KeyboardEventSource {
        private final int key;
        private final char character;
        private final boolean pressed;
        private final boolean repeat;

        private FakeKeyboardEventSource(
                final int key,
                final char character,
                final boolean pressed,
                final boolean repeat) {
            this.key = key;
            this.character = character;
            this.pressed = pressed;
            this.repeat = repeat;
        }

        @Override
        public int eventKey() {
            return key;
        }

        @Override
        public char eventCharacter() {
            return character;
        }

        @Override
        public boolean eventKeyState() {
            return pressed;
        }

        @Override
        public boolean repeatEvent() {
            return repeat;
        }

        @Override
        public boolean keyDown(
                final int keyCode) {
            return false;
        }
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
