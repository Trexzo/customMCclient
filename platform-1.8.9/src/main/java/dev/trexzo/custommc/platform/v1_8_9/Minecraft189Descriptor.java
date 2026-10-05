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
                        "0000000000000000000000000000000000000000",
                        0L),
                new RuntimeArtifact(
                        "authlib",
                        "aefba0d5b53fbcb70860bc8046ab95d5854c07a5",
                        64412L)
        ));
    }
}
