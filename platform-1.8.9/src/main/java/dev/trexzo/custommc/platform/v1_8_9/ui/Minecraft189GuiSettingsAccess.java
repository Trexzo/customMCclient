package dev.trexzo.custommc.platform.v1_8_9.ui;

public interface Minecraft189GuiSettingsAccess {
    float gammaSetting();

    void gammaSetting(float value);

    int configuredGuiScale();

    boolean unicode();
}
