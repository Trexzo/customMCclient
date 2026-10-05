package dev.trexzo.custommc.launcher.runtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MinecraftHomeLocatorTest {
    @TempDir
    Path tempDir;

    @Test
    void explicitOverrideAlwaysWins() {
        final Path explicit =
                tempDir.resolve("custom-home");
        final MinecraftHomeLocator locator =
                new MinecraftHomeLocator();

        assertEquals(
                explicit.toAbsolutePath().normalize(),
                locator.locate(
                        explicit,
                        OperatingSystem.WINDOWS,
                        Collections.<String, String>emptyMap(),
                        tempDir.toString()));
    }

    @Test
    void windowsUsesAppDataWhenPresent() {
        final Map<String, String> environment =
                new HashMap<String, String>();
        final Path appData = tempDir.resolve("Roaming");
        environment.put("APPDATA", appData.toString());

        assertEquals(
                appData.resolve(".minecraft")
                        .toAbsolutePath()
                        .normalize(),
                new MinecraftHomeLocator().locate(
                        null,
                        OperatingSystem.WINDOWS,
                        environment,
                        tempDir.toString()));
    }

    @Test
    void platformDefaultsAreDeterministic() {
        final MinecraftHomeLocator locator =
                new MinecraftHomeLocator();

        assertEquals(
                tempDir.resolve(".minecraft")
                        .toAbsolutePath()
                        .normalize(),
                locator.locate(
                        null,
                        OperatingSystem.LINUX,
                        Collections.<String, String>emptyMap(),
                        tempDir.toString()));

        assertEquals(
                tempDir.resolve("Library")
                        .resolve("Application Support")
                        .resolve("minecraft")
                        .toAbsolutePath()
                        .normalize(),
                locator.locate(
                        null,
                        OperatingSystem.MACOS,
                        Collections.<String, String>emptyMap(),
                        tempDir.toString()));

        assertEquals(
                tempDir.resolve("AppData")
                        .resolve("Roaming")
                        .resolve(".minecraft")
                        .toAbsolutePath()
                        .normalize(),
                locator.locate(
                        null,
                        OperatingSystem.WINDOWS,
                        Collections.<String, String>emptyMap(),
                        tempDir.toString()));
    }
}
