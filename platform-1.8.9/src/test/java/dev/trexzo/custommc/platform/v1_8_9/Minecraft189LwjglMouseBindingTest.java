package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189LwjglMouseBindingTest {
    @Test
    void rawLeftButtonStateUsesLiveCursorCoordinatesWithoutOwningMouseQueue() {
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

            final UiViewport viewport =
                    new UiViewport(
                            1200,
                            800,
                            1.0F);
            final ClickGuiLayout layout =
                    new ClickGuiLayoutEngine()
                            .layout(viewport);
            final int pixelX =
                    Math.round(
                            layout.search().x()
                                    + 2.0F);
            final int logicalY =
                    Math.round(
                            layout.search().y()
                                    + 2.0F);
            final int pixelYFromBottom =
                    viewport.pixelHeight()
                            - 1
                            - logicalY;

            assertTrue(
                    Minecraft189LwjglMouseBinding
                            .forwardKeyBindingState(
                                    -100,
                                    true,
                                    new FixedMousePosition(
                                            pixelX,
                                            pixelYFromBottom)));

            assertTrue(
                    Minecraft189LwjglKeyboardBinding
                            .forward(
                                    new CharacterKeyboardEvent(
                                            'e')));
            assertEquals(
                    "e",
                    model.snapshot()
                            .searchQuery());

            assertFalse(
                    Minecraft189LwjglMouseBinding
                            .forwardKeyBindingState(
                                    -100,
                                    false,
                                    new FixedMousePosition(
                                            pixelX,
                                            pixelYFromBottom)));
        } finally {
            runtime.close();
        }
    }

    @Test
    void wheelDeltaRoutesAtLiveCursorWithoutConsumingVanillaValue() {
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

            final UiViewport viewport =
                    new UiViewport(
                            1200,
                            800,
                            1.0F);
            final ClickGuiLayout layout =
                    new ClickGuiLayoutEngine()
                            .layout(viewport);
            final int pixelX =
                    Math.round(
                            layout.navigation().x()
                                    + 2.0F);
            final int logicalY =
                    Math.round(
                            layout.navigation().y()
                                    + 2.0F);
            final int pixelYFromBottom =
                    viewport.pixelHeight()
                            - 1
                            - logicalY;

            assertTrue(
                    Minecraft189LwjglMouseBinding
                            .forwardWheelDelta(
                                    -120,
                                    new FixedMousePosition(
                                            pixelX,
                                            pixelYFromBottom)));

            final CountingMousePosition zero =
                    new CountingMousePosition();
            assertFalse(
                    Minecraft189LwjglMouseBinding
                            .forwardWheelDelta(
                                    0,
                                    zero));
            assertEquals(
                    0,
                    zero.reads);
        } finally {
            runtime.close();
        }
    }

    @Test
    void keyboardAndUnsupportedMouseCodesDoNotReadCursorState() {
        final CountingMousePosition source =
                new CountingMousePosition();

        assertFalse(
                Minecraft189LwjglMouseBinding
                        .forwardKeyBindingState(
                                30,
                                true,
                                source));
        assertFalse(
                Minecraft189LwjglMouseBinding
                        .forwardKeyBindingState(
                                -97,
                                true,
                                source));
        assertEquals(
                0,
                source.reads);
    }

    private static final class FixedMousePosition
            implements Minecraft189LwjglMouseBinding
            .MousePositionSource {
        private final int x;
        private final int y;

        private FixedMousePosition(
                final int x,
                final int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int x() {
            return x;
        }

        @Override
        public int yFromBottom() {
            return y;
        }
    }

    private static final class CountingMousePosition
            implements Minecraft189LwjglMouseBinding
            .MousePositionSource {
        private int reads;

        @Override
        public int x() {
            reads++;
            return 0;
        }

        @Override
        public int yFromBottom() {
            reads++;
            return 0;
        }
    }

    private static final class CharacterKeyboardEvent
            implements Minecraft189LwjglKeyboardBinding
            .KeyboardEventSource {
        private final char character;

        private CharacterKeyboardEvent(
                final char character) {
            this.character = character;
        }

        @Override
        public int eventKey() {
            return 18;
        }

        @Override
        public char eventCharacter() {
            return character;
        }

        @Override
        public boolean eventKeyState() {
            return true;
        }

        @Override
        public boolean repeatEvent() {
            return false;
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
            return 1200;
        }

        @Override
        public int framebufferHeight() {
            return 800;
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
