package dev.trexzo.custommc.launcher.command;

import dev.trexzo.custommc.launcher.metadata.MinecraftLaunchTemplate;

public interface LaunchRuntimeOverlayResolver {
    LaunchRuntimeOverlay resolve(
            MinecraftLaunchTemplate template);

    static LaunchRuntimeOverlayResolver fixed(
            final LaunchRuntimeOverlay overlay) {
        if (overlay == null) {
            throw new NullPointerException("overlay");
        }
        return new LaunchRuntimeOverlayResolver() {
            @Override
            public LaunchRuntimeOverlay resolve(
                    final MinecraftLaunchTemplate template) {
                if (template == null) {
                    throw new NullPointerException("template");
                }
                return overlay;
            }
        };
    }
}
