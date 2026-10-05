package dev.trexzo.custommc.platform;

public interface GameEventBridge {
    void clientTickStart();

    void clientTickEnd();

    void renderFrame(float partialTicks);

    void worldPresenceChanged(boolean present);
}
