package dev.trexzo.custommc.platform;

public interface GamePlatform {
    String id();

    String gameVersion();

    void attach(PlatformContext context);

    void detach();
}
