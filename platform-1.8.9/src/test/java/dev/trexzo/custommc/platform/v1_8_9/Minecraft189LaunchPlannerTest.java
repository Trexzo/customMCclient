package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.LaunchPlan;
import dev.trexzo.custommc.platform.RuntimeLayout;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class Minecraft189LaunchPlannerTest {
    @Test
    void launchPlanIsVersionSpecificAndExplicit() {
        final Map<String, java.nio.file.Path> artifacts =
                new LinkedHashMap<String, java.nio.file.Path>();
        artifacts.put("minecraft-client", Paths.get("mc.jar"));
        artifacts.put("authlib", Paths.get("authlib.jar"));

        final RuntimeLayout runtime =
                new RuntimeLayout(Paths.get("."), artifacts);

        final LaunchPlan plan =
                new Minecraft189LaunchPlanner().create(
                        Paths.get("java"),
                        runtime,
                        "Player",
                        "token",
                        Paths.get("run"));

        assertEquals(
                "net.minecraft.client.main.Main",
                plan.mainClass());
        assertEquals(2, plan.classpath().size());
        assertEquals(
                "1.8.9",
                plan.gameArgs().get(3));
    }
}
