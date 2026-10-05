package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleSettingRegistryTest {
    @Test
    void bindingsAreValidatedImmutableAndPriorityOrdered() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("combat.aura"));

        final SettingRegistry settings =
                new SettingRegistry();
        settings.register(booleanSetting("aura.enabled"));
        settings.register(booleanSetting("aura.rotation"));
        settings.register(booleanSetting("aura.targeting"));

        final ModuleSettingRegistry owners =
                new ModuleSettingRegistry(
                        modules,
                        settings);

        owners.register(
                new ModuleSettingBinding(
                        "combat.aura",
                        "aura.rotation",
                        20));
        owners.register(
                new ModuleSettingBinding(
                        "combat.aura",
                        "aura.enabled",
                        10));
        owners.register(
                new ModuleSettingBinding(
                        "combat.aura",
                        "aura.targeting",
                        20));

        final List<ModuleSettingBinding> bindings =
                owners.bindingsForModule(
                        "combat.aura");

        assertEquals(
                3,
                bindings.size());
        assertEquals(
                "aura.enabled",
                bindings.get(0).settingId());
        assertEquals(
                "aura.rotation",
                bindings.get(1).settingId());
        assertEquals(
                "aura.targeting",
                bindings.get(2).settingId());

        assertThrows(
                UnsupportedOperationException.class,
                () -> bindings.clear());
    }

    @Test
    void aSettingHasAtMostOneModuleOwnerUntilRegistrationCloses() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("combat.aura"));
        modules.register(module("render.esp"));

        final SettingRegistry settings =
                new SettingRegistry();
        settings.register(booleanSetting("shared.option"));

        final ModuleSettingRegistry owners =
                new ModuleSettingRegistry(
                        modules,
                        settings);
        final ModuleSettingRegistry.Registration registration =
                owners.register(
                        new ModuleSettingBinding(
                                "combat.aura",
                                "shared.option",
                                0));

        assertTrue(registration.active());
        assertEquals(
                "combat.aura",
                owners.ownerOf(
                        "shared.option")
                        .moduleId());

        assertThrows(
                IllegalArgumentException.class,
                () -> owners.register(
                        new ModuleSettingBinding(
                                "render.esp",
                                "shared.option",
                                0)));

        registration.close();

        assertFalse(registration.active());
        assertNull(
                owners.ownerOf(
                        "shared.option"));

        final ModuleSettingRegistry.Registration rebound =
                owners.register(
                        new ModuleSettingBinding(
                                "render.esp",
                                "shared.option",
                                0));
        assertEquals(
                "render.esp",
                rebound.binding().moduleId());
    }

    @Test
    void registrationAndQueriesRejectUnknownAuthorityIds() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("combat.aura"));

        final SettingRegistry settings =
                new SettingRegistry();
        settings.register(booleanSetting("aura.enabled"));

        final ModuleSettingRegistry owners =
                new ModuleSettingRegistry(
                        modules,
                        settings);

        assertThrows(
                IllegalArgumentException.class,
                () -> owners.register(
                        new ModuleSettingBinding(
                                "missing.module",
                                "aura.enabled",
                                0)));
        assertThrows(
                IllegalArgumentException.class,
                () -> owners.register(
                        new ModuleSettingBinding(
                                "combat.aura",
                                "missing.setting",
                                0)));
        assertThrows(
                IllegalArgumentException.class,
                () -> owners.bindingsForModule(
                        "missing.module"));
    }

    @Test
    void bindingNormalizesIdsAndRejectsBlankIds() {
        final ModuleSettingBinding binding =
                new ModuleSettingBinding(
                        " combat.aura ",
                        " aura.enabled ",
                        -5);

        assertEquals(
                "combat.aura",
                binding.moduleId());
        assertEquals(
                "aura.enabled",
                binding.settingId());
        assertEquals(
                -5,
                binding.priority());

        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleSettingBinding(
                        " ",
                        "aura.enabled",
                        0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleSettingBinding(
                        "combat.aura",
                        " ",
                        0));
    }

    private static Module module(
            final String id) {
        return new Module() {
            @Override
            public String id() {
                return id;
            }
        };
    }

    private static Setting<Boolean> booleanSetting(
            final String id) {
        return new Setting<Boolean>(
                id,
                Boolean.FALSE,
                value -> true,
                SettingCodecs.BOOLEAN);
    }
}
