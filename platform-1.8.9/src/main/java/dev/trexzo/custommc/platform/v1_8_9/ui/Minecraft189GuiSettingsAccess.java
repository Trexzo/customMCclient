package dev.trexzo.custommc.platform.v1_8_9.ui;

public interface Minecraft189GuiSettingsAccess {
    boolean viewBobbing();

    void viewBobbing(boolean value);

    float fovSetting();

    void fovSetting(float value);

    float gammaSetting();

    void gammaSetting(float value);

    int configuredGuiScale();

    boolean unicode();
}
