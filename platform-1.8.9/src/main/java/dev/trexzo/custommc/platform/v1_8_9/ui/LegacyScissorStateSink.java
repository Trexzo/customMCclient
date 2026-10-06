package dev.trexzo.custommc.platform.v1_8_9.ui;

public interface LegacyScissorStateSink {
    void apply(LegacyFramebufferRect rect);

    void disable();
}
