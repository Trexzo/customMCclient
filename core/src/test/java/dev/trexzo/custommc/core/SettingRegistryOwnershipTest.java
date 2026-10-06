package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SettingRegistryOwnershipTest {
    @Test
    void registrationRemovesOnlyExactOwnedSetting() {
        final SettingRegistry registry =
                new SettingRegistry();
        final Setting<Integer> first =
                new Setting<Integer>(
                        "watermark.x",
                        8,
                        value -> value >= 0,
                        SettingCodecs.INTEGER);

        final SettingRegistry.Registration registration =
                registry.register(first);

        assertTrue(registration.active());
        assertEquals(
                first,
                registry.find("watermark.x"));

        registration.close();

        assertFalse(registration.active());
        assertNull(
                registry.find("watermark.x"));

        registration.close();
        assertNull(
                registry.find("watermark.x"));
    }
}
