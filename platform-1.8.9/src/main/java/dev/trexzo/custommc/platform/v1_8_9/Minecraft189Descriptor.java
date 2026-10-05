package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.RuntimeArtifact;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Minecraft189Descriptor {
    public static final String VERSION = "1.8.9";
    public static final String MAIN_CLASS =
            "net.minecraft.client.main.Main";

    private Minecraft189Descriptor() {
    }

    public static List<RuntimeArtifact> requiredArtifacts() {
        return Collections.unmodifiableList(Arrays.asList(
                new RuntimeArtifact(
                        "minecraft-client",
                        "3966a3fbe04c31f127b0255781c7cbc3a146a0c2",
                        8461484L),
                new RuntimeArtifact(
                        "authlib",
                        "083b74a9bfbe5c90355f16b91693a7a6c0ea32f2",
                        64412L)
        ));
    }
}
