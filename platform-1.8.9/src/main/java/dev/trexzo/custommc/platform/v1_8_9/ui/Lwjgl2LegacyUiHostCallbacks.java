package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.Objects;

public final class Lwjgl2LegacyUiHostCallbacks
        implements LegacyUiBatchHostCallbacks {
    private final LegacyUiViewportSource viewportSource;
    private final LegacyUiTextRenderer textRenderer;
    private final LegacyGlApi gl;
    private final LegacyUiGeometryFactory geometryFactory;

    private LegacyScissorStateController scissor;
    private boolean frameOpen;
    private boolean shapeBatchOpen;

    public Lwjgl2LegacyUiHostCallbacks(
            final LegacyUiViewportSource viewportSource,
            final LegacyUiTextRenderer textRenderer) {
        this(
                viewportSource,
                textRenderer,
                new Lwjgl2LegacyGlApi(),
                new LegacyUiGeometryFactory());
    }

    public Lwjgl2LegacyUiHostCallbacks(
            final LegacyUiViewportSource viewportSource,
            final LegacyUiTextRenderer textRenderer,
            final LegacyGlApi gl,
            final LegacyUiGeometryFactory geometryFactory) {
        this.viewportSource =
                Objects.requireNonNull(
                        viewportSource,
                        "viewportSource");
        this.textRenderer =
                Objects.requireNonNull(
                        textRenderer,
                        "textRenderer");
        this.gl =
                Objects.requireNonNull(
                        gl,
                        "gl");
        this.geometryFactory =
                Objects.requireNonNull(
                        geometryFactory,
                        "geometryFactory");
    }

    @Override
    public int framebufferWidth() {
        return viewportSource.framebufferWidth();
    }

    @Override
    public int framebufferHeight() {
        return viewportSource.framebufferHeight();
    }

    @Override
    public float uiScale() {
        return viewportSource.uiScale();
    }

    @Override
    public void beginUi(
            final UiViewport viewport) {
        Objects.requireNonNull(
                viewport,
                "viewport");
        if (frameOpen) {
            throw new IllegalStateException(
                    "LWJGL2 host UI frame already open");
        }

        gl.beginUi(viewport);
        scissor =
                new LegacyScissorStateController(
                        viewport,
                        new LegacyScissorStateSink() {
                            @Override
                            public void apply(
                                    final LegacyFramebufferRect rect) {
                                gl.applyScissor(rect);
                            }

                            @Override
                            public void disable() {
                                gl.disableScissor();
                            }
                        });
        shapeBatchOpen = false;
        frameOpen = true;
    }

    @Override
    public void beginShapeBatch() {
        requireFrame();
        if (shapeBatchOpen) {
            throw new IllegalStateException(
                    "LWJGL2 host shape batch already open");
        }

        gl.prepareShapeState();
        shapeBatchOpen = true;
    }

    @Override
    public void endShapeBatch() {
        requireFrame();
        if (!shapeBatchOpen) {
            throw new IllegalStateException(
                    "LWJGL2 host shape batch is not open");
        }
        shapeBatchOpen = false;
    }

    @Override
    public void fillRect(
            final float x,
            final float y,
            final float width,
            final float height,
            final int argb) {
        requireFrame();
        prepareShapeIfNeeded();
        drawGeometry(
                geometryFactory.rectangle(
                        x,
                        y,
                        width,
                        height),
                argb);
    }

    @Override
    public void fillRoundedRect(
            final float x,
            final float y,
            final float width,
            final float height,
            final float radius,
            final int argb) {
        requireFrame();
        prepareShapeIfNeeded();
        drawGeometry(
                geometryFactory.roundedRectangle(
                        x,
                        y,
                        width,
                        height,
                        radius),
                argb);
    }

    @Override
    public void strokeRect(
            final float x,
            final float y,
            final float width,
            final float height,
            final float thickness,
            final int argb) {
        requireFrame();
        prepareShapeIfNeeded();
        gl.setLineWidth(thickness);
        drawGeometry(
                geometryFactory.outlineRectangle(
                        x,
                        y,
                        width,
                        height),
                argb);
    }

    @Override
    public void pushClip(
            final float x,
            final float y,
            final float width,
            final float height) {
        requireFrame();
        requireNoShapeBatch();
        scissor.push(
                new UiBounds(
                        x,
                        y,
                        width,
                        height));
    }

    @Override
    public void popClip() {
        requireFrame();
        requireNoShapeBatch();
        scissor.pop();
    }

    @Override
    public void drawText(
            final UiFontHandle font,
            final float x,
            final float y,
            final String text,
            final int argb) {
        requireFrame();
        requireNoShapeBatch();

        gl.prepareTextState();
        textRenderer.drawText(
                Objects.requireNonNull(
                        font,
                        "font"),
                x,
                y,
                Objects.requireNonNull(
                        text,
                        "text"),
                argb);
    }

    @Override
    public void endUi() {
        requireFrame();

        RuntimeException failure = null;

        if (shapeBatchOpen) {
            failure =
                    new IllegalStateException(
                            "LWJGL2 host shape batch unbalanced");
            shapeBatchOpen = false;
        }

        try {
            scissor.reset();
        } catch (RuntimeException cleanupFailure) {
            failure = append(
                    failure,
                    cleanupFailure);
        }

        try {
            gl.endUi();
        } catch (RuntimeException cleanupFailure) {
            failure = append(
                    failure,
                    cleanupFailure);
        } finally {
            frameOpen = false;
            shapeBatchOpen = false;
            scissor = null;
        }

        if (failure != null) {
            throw failure;
        }
    }

    public boolean frameOpen() {
        return frameOpen;
    }

    public boolean shapeBatchOpen() {
        return shapeBatchOpen;
    }

    private void prepareShapeIfNeeded() {
        if (!shapeBatchOpen) {
            gl.prepareShapeState();
        }
    }

    private void drawGeometry(
            final LegacyUiGeometry geometry,
            final int argb) {
        gl.setColorArgb(argb);
        gl.beginPrimitive(
                geometry.primitive());

        RuntimeException failure = null;
        try {
            for (int index = 0;
                 index < geometry.vertexCount();
                 index++) {
                gl.vertex(
                        geometry.x(index),
                        geometry.y(index));
            }
        } catch (RuntimeException drawFailure) {
            failure = drawFailure;
            throw drawFailure;
        } finally {
            try {
                gl.endPrimitive();
            } catch (RuntimeException closeFailure) {
                if (failure != null) {
                    failure.addSuppressed(
                            closeFailure);
                } else {
                    throw closeFailure;
                }
            }
        }
    }

    private void requireFrame() {
        if (!frameOpen) {
            throw new IllegalStateException(
                    "LWJGL2 host UI frame is not open");
        }
    }

    private void requireNoShapeBatch() {
        if (shapeBatchOpen) {
            throw new IllegalStateException(
                    "LWJGL2 host shape batch must be closed first");
        }
    }

    private static RuntimeException append(
            final RuntimeException existing,
            final RuntimeException next) {
        if (existing == null) {
            return next;
        }
        existing.addSuppressed(next);
        return existing;
    }
}
