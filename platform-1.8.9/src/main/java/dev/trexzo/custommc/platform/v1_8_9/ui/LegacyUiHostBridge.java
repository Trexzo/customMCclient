package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.Objects;

public final class LegacyUiHostBridge
        implements LegacyUiGraphics, LegacyUiViewportSource {
    private final LegacyUiHostCallbacks callbacks;
    private boolean frameOpen;
    private int clipDepth;

    public LegacyUiHostBridge(
            final LegacyUiHostCallbacks callbacks) {
        this.callbacks = Objects.requireNonNull(
                callbacks,
                "callbacks");
    }

    @Override
    public int framebufferWidth() {
        return callbacks.framebufferWidth();
    }

    @Override
    public int framebufferHeight() {
        return callbacks.framebufferHeight();
    }

    @Override
    public float uiScale() {
        return callbacks.uiScale();
    }

    @Override
    public synchronized void begin(
            final UiViewport viewport) {
        Objects.requireNonNull(
                viewport,
                "viewport");
        if (frameOpen) {
            throw new IllegalStateException(
                    "legacy UI frame already open");
        }

        callbacks.beginUi(viewport);
        frameOpen = true;
        clipDepth = 0;
    }

    @Override
    public synchronized void fillRect(
            final float x,
            final float y,
            final float width,
            final float height,
            final int argb) {
        requireFrame();
        callbacks.fillRect(
                x,
                y,
                width,
                height,
                argb);
    }

    @Override
    public synchronized void fillRoundedRect(
            final float x,
            final float y,
            final float width,
            final float height,
            final float radius,
            final int argb) {
        requireFrame();
        callbacks.fillRoundedRect(
                x,
                y,
                width,
                height,
                radius,
                argb);
    }

    @Override
    public synchronized void strokeRect(
            final float x,
            final float y,
            final float width,
            final float height,
            final float thickness,
            final int argb) {
        requireFrame();
        callbacks.strokeRect(
                x,
                y,
                width,
                height,
                thickness,
                argb);
    }

    @Override
    public synchronized void pushClip(
            final float x,
            final float y,
            final float width,
            final float height) {
        requireFrame();
        callbacks.pushClip(
                x,
                y,
                width,
                height);
        clipDepth++;
    }

    @Override
    public synchronized void popClip() {
        requireFrame();
        if (clipDepth <= 0) {
            throw new IllegalStateException(
                    "legacy UI clip stack underflow");
        }

        callbacks.popClip();
        clipDepth--;
    }

    @Override
    public synchronized void drawText(
            final UiFontHandle font,
            final float x,
            final float y,
            final String text,
            final int argb) {
        requireFrame();
        callbacks.drawText(
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
    public synchronized void end() {
        requireFrame();

        RuntimeException failure = null;
        if (clipDepth != 0) {
            failure =
                    new IllegalStateException(
                            "legacy UI clip stack unbalanced: "
                                    + clipDepth);
        }

        while (clipDepth > 0) {
            try {
                callbacks.popClip();
            } catch (RuntimeException cleanupFailure) {
                if (failure == null) {
                    failure = cleanupFailure;
                } else {
                    failure.addSuppressed(
                            cleanupFailure);
                }
            } finally {
                clipDepth--;
            }
        }

        try {
            callbacks.endUi();
        } catch (RuntimeException endFailure) {
            if (failure == null) {
                failure = endFailure;
            } else {
                failure.addSuppressed(
                        endFailure);
            }
        } finally {
            frameOpen = false;
            clipDepth = 0;
        }

        if (failure != null) {
            throw failure;
        }
    }

    public synchronized boolean frameOpen() {
        return frameOpen;
    }

    public synchronized int clipDepth() {
        return clipDepth;
    }

    private void requireFrame() {
        if (!frameOpen) {
            throw new IllegalStateException(
                    "legacy UI frame is not open");
        }
    }
}
