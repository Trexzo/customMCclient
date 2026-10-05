package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyViewportAccess;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189ViewportProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class Minecraft189ViewportProviderTest {
    @Test
    void providerReadsCurrentLegacyViewportStatePerFrame() {
        final MutableViewport access =
                new MutableViewport();
        final Minecraft189ViewportProvider provider =
                new Minecraft189ViewportProvider(access);

        access.width = 1920;
        access.height = 1080;
        access.scale = 2.0F;

        UiViewport viewport =
                provider.viewport(
                        new RenderFrame(1L, 0.0F));

        assertEquals(960.0F, viewport.logicalWidth());
        assertEquals(540.0F, viewport.logicalHeight());

        access.width = 1280;
        access.height = 720;
        access.scale = 1.0F;

        viewport = provider.viewport(
                new RenderFrame(2L, 0.0F));

        assertEquals(1280.0F, viewport.logicalWidth());
        assertEquals(720.0F, viewport.logicalHeight());
    }

    private static final class MutableViewport
            implements LegacyViewportAccess {
        private int width;
        private int height;
        private float scale;

        @Override
        public int framebufferWidth() {
            return width;
        }

        @Override
        public int framebufferHeight() {
            return height;
        }

        @Override
        public float uiScale() {
            return scale;
        }
    }
}
