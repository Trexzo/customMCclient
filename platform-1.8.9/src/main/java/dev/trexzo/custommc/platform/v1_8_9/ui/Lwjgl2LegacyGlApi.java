package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiViewport;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

public final class Lwjgl2LegacyGlApi
        implements LegacyGlApi {
    private static final int ATTRIB_MASK =
            GL11.GL_CURRENT_BIT
                    | GL11.GL_ENABLE_BIT
                    | GL11.GL_COLOR_BUFFER_BIT
                    | GL11.GL_LINE_BIT
                    | GL11.GL_SCISSOR_BIT
                    | GL11.GL_TEXTURE_BIT;

    private boolean frameOpen;
    private boolean primitiveOpen;
    private int previousMatrixMode;

    @Override
    public void beginUi(
            final UiViewport viewport) {
        Objects.requireNonNull(
                viewport,
                "viewport");
        if (frameOpen) {
            throw new IllegalStateException(
                    "LWJGL2 UI frame already open");
        }

        previousMatrixMode =
                GL11.glGetInteger(
                        GL11.GL_MATRIX_MODE);

        boolean attribPushed = false;
        boolean projectionPushed = false;
        boolean modelViewPushed = false;

        try {
            GL11.glPushAttrib(ATTRIB_MASK);
            attribPushed = true;

            GL11.glMatrixMode(
                    GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            projectionPushed = true;
            GL11.glLoadIdentity();
            GL11.glOrtho(
                    0.0,
                    viewport.logicalWidth(),
                    viewport.logicalHeight(),
                    0.0,
                    -1.0,
                    1.0);

            GL11.glMatrixMode(
                    GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            modelViewPushed = true;
            GL11.glLoadIdentity();

            GL11.glDisable(
                    GL11.GL_SCISSOR_TEST);
            GL11.glDisable(
                    GL11.GL_DEPTH_TEST);
            GL11.glDisable(
                    GL11.GL_CULL_FACE);
            GL11.glDisable(
                    GL11.GL_LIGHTING);
            GL11.glEnable(
                    GL11.GL_BLEND);
            GL11.glBlendFunc(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_SRC_ALPHA);

            primitiveOpen = false;
            frameOpen = true;
        } catch (RuntimeException failure) {
            cleanupFailedBegin(
                    failure,
                    modelViewPushed,
                    projectionPushed,
                    attribPushed);
            throw failure;
        }
    }

    @Override
    public void endUi() {
        requireFrame();

        RuntimeException failure = null;

        if (primitiveOpen) {
            failure =
                    new IllegalStateException(
                            "LWJGL2 primitive left open");
            try {
                GL11.glEnd();
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(
                        cleanupFailure);
            } finally {
                primitiveOpen = false;
            }
        }

        try {
            GL11.glMatrixMode(
                    GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
        } catch (RuntimeException cleanupFailure) {
            failure = append(
                    failure,
                    cleanupFailure);
        }

        try {
            GL11.glMatrixMode(
                    GL11.GL_PROJECTION);
            GL11.glPopMatrix();
        } catch (RuntimeException cleanupFailure) {
            failure = append(
                    failure,
                    cleanupFailure);
        }

        try {
            GL11.glMatrixMode(
                    previousMatrixMode);
        } catch (RuntimeException cleanupFailure) {
            failure = append(
                    failure,
                    cleanupFailure);
        }

        try {
            GL11.glPopAttrib();
        } catch (RuntimeException cleanupFailure) {
            failure = append(
                    failure,
                    cleanupFailure);
        } finally {
            frameOpen = false;
            primitiveOpen = false;
        }

        if (failure != null) {
            throw failure;
        }
    }

    @Override
    public void prepareShapeState() {
        requireFrame();
        requireNoPrimitive();

        GL11.glDisable(
                GL11.GL_TEXTURE_2D);
        GL11.glDisable(
                GL11.GL_ALPHA_TEST);
        GL11.glDisable(
                GL11.GL_DEPTH_TEST);
        GL11.glDisable(
                GL11.GL_CULL_FACE);
        GL11.glDisable(
                GL11.GL_LIGHTING);
        GL11.glEnable(
                GL11.GL_BLEND);
        GL11.glBlendFunc(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void prepareTextState() {
        requireFrame();
        requireNoPrimitive();

        GL11.glEnable(
                GL11.GL_TEXTURE_2D);
        GL11.glEnable(
                GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(
                GL11.GL_GREATER,
                0.1F);
        GL11.glDisable(
                GL11.GL_DEPTH_TEST);
        GL11.glDisable(
                GL11.GL_CULL_FACE);
        GL11.glDisable(
                GL11.GL_LIGHTING);
        GL11.glEnable(
                GL11.GL_BLEND);
        GL11.glBlendFunc(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1.0F);
        GL11.glColor4f(
                1.0F,
                1.0F,
                1.0F,
                1.0F);
    }

    @Override
    public void setColorArgb(
            final int argb) {
        requireFrame();
        requireNoPrimitive();

        final float alpha =
                ((argb >>> 24) & 0xFF)
                        / 255.0F;
        final float red =
                ((argb >>> 16) & 0xFF)
                        / 255.0F;
        final float green =
                ((argb >>> 8) & 0xFF)
                        / 255.0F;
        final float blue =
                (argb & 0xFF)
                        / 255.0F;

        GL11.glColor4f(
                red,
                green,
                blue,
                alpha);
    }

    @Override
    public void setLineWidth(
            final float width) {
        requireFrame();
        requireNoPrimitive();

        if (Float.isNaN(width)
                || Float.isInfinite(width)
                || width <= 0.0F) {
            throw new IllegalArgumentException(
                    "width must be finite and positive");
        }

        GL11.glLineWidth(width);
    }

    @Override
    public void beginPrimitive(
            final LegacyUiPrimitiveMode primitive) {
        requireFrame();
        requireNoPrimitive();
        Objects.requireNonNull(
                primitive,
                "primitive");

        GL11.glBegin(
                glMode(primitive));
        primitiveOpen = true;
    }

    @Override
    public void vertex(
            final float x,
            final float y) {
        requireFrame();
        if (!primitiveOpen) {
            throw new IllegalStateException(
                    "LWJGL2 primitive is not open");
        }
        if (Float.isNaN(x)
                || Float.isInfinite(x)
                || Float.isNaN(y)
                || Float.isInfinite(y)) {
            throw new IllegalArgumentException(
                    "vertex coordinates must be finite");
        }

        GL11.glVertex2f(
                x,
                y);
    }

    @Override
    public void endPrimitive() {
        requireFrame();
        if (!primitiveOpen) {
            throw new IllegalStateException(
                    "LWJGL2 primitive is not open");
        }

        try {
            GL11.glEnd();
        } finally {
            primitiveOpen = false;
        }
    }

    @Override
    public void applyScissor(
            final LegacyFramebufferRect rect) {
        requireFrame();
        requireNoPrimitive();
        Objects.requireNonNull(
                rect,
                "rect");

        GL11.glEnable(
                GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
                rect.x(),
                rect.y(),
                rect.width(),
                rect.height());
    }

    @Override
    public void disableScissor() {
        requireFrame();
        requireNoPrimitive();

        GL11.glDisable(
                GL11.GL_SCISSOR_TEST);
    }

    public boolean frameOpen() {
        return frameOpen;
    }

    public boolean primitiveOpen() {
        return primitiveOpen;
    }

    private static int glMode(
            final LegacyUiPrimitiveMode primitive) {
        switch (primitive) {
            case QUADS:
                return GL11.GL_QUADS;
            case TRIANGLE_FAN:
                return GL11.GL_TRIANGLE_FAN;
            case LINE_LOOP:
                return GL11.GL_LINE_LOOP;
            default:
                throw new IllegalArgumentException(
                        "unsupported primitive: "
                                + primitive);
        }
    }

    private void requireFrame() {
        if (!frameOpen) {
            throw new IllegalStateException(
                    "LWJGL2 UI frame is not open");
        }
    }

    private void requireNoPrimitive() {
        if (primitiveOpen) {
            throw new IllegalStateException(
                    "LWJGL2 primitive must be closed first");
        }
    }

    private void cleanupFailedBegin(
            final RuntimeException failure,
            final boolean modelViewPushed,
            final boolean projectionPushed,
            final boolean attribPushed) {
        if (modelViewPushed) {
            try {
                GL11.glMatrixMode(
                        GL11.GL_MODELVIEW);
                GL11.glPopMatrix();
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(
                        cleanupFailure);
            }
        }

        if (projectionPushed) {
            try {
                GL11.glMatrixMode(
                        GL11.GL_PROJECTION);
                GL11.glPopMatrix();
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(
                        cleanupFailure);
            }
        }

        try {
            GL11.glMatrixMode(
                    previousMatrixMode);
        } catch (RuntimeException cleanupFailure) {
            failure.addSuppressed(
                    cleanupFailure);
        }

        if (attribPushed) {
            try {
                GL11.glPopAttrib();
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(
                        cleanupFailure);
            }
        }

        frameOpen = false;
        primitiveOpen = false;
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
