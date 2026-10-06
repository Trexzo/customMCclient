package dev.trexzo.custommc.launcher.profile;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleKeyChord;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.UnknownSettingPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LauncherProfilePersistenceTest {
    @TempDir
    Path tempDir;

    @Test
    void atomicStoreRoundTripsSettingsAndKeybindNamespace()
            throws Exception {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(
                new Module() {
                    @Override
                    public String id() {
                        return "combat.aura";
                    }
                });

        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<Integer> range =
                new Setting<Integer>(
                        "combat.range",
                        3,
                        value -> value >= 1
                                && value <= 6,
                        SettingCodecs.INTEGER);
        settings.register(range);

        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules));
        assignments.bind(
                "combat.aura",
                new ModuleKeyChord(
                        "legacy-key-37",
                        true,
                        true,
                        false));
        range.set(5);
        controller.enable(
                "combat.aura");

        final LauncherProfileState profile =
                new LauncherProfileState(
                        settings,
                        assignments,
                        modules,
                        controller);
        final AtomicProfileStore store =
                new AtomicProfileStore();
        final Path target =
                tempDir.resolve(
                        "combined.profile");

        store.write(
                target,
                profile.snapshot());

        range.set(2);
        assignments.unbind(
                "combat.aura");
        controller.disable(
                "combat.aura");

        final Map<String, String> restored =
                store.read(target);
        profile.apply(
                restored,
                UnknownSettingPolicy.REJECT);

        assertEquals(
                Integer.valueOf(5),
                range.get());
        assertEquals(
                new ModuleKeyChord(
                        "legacy-key-37",
                        true,
                        true,
                        false),
                assignments.binding(
                        "combat.aura")
                        .chord());
        assertEquals(
                ModuleState.ENABLED,
                controller.stateOf(
                        "combat.aura"));

        assignments.close();
        assertTrue(assignments.closed());
    }
}
