package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SettingPresentationTest {
    @Test
    void settingSnapshotIsOrderedImmutableAndLiveValuesRemainOwnedBySetting() {
        final SettingRegistry registry =
                new SettingRegistry();
        final Setting<Boolean> first =
                new Setting<Boolean>(
                        "combat.autoblock",
                        Boolean.FALSE,
                        value -> true,
                        SettingCodecs.BOOLEAN);
        final Setting<Integer> second =
                new Setting<Integer>(
                        "combat.cps",
                        12,
                        value -> value >= 1 && value <= 20,
                        SettingCodecs.INTEGER);

        registry.register(first);
        registry.register(second);

        final List<Setting<?>> snapshot =
                registry.snapshot();

        assertEquals(
                "combat.autoblock",
                snapshot.get(0).id());
        assertEquals(
                "combat.cps",
                snapshot.get(1).id());
        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.clear());

        first.set(Boolean.TRUE);
        assertEquals(
                Boolean.TRUE,
                snapshot.get(0).get());
    }

    @Test
    void presentationRegistrationHasExplicitLifetime() {
        final SettingPresentationRegistry registry =
                new SettingPresentationRegistry();
        final SettingDescriptor descriptor =
                new SettingDescriptor(
                        "combat.autoblock",
                        "Auto Block",
                        SettingValueKind.BOOLEAN,
                        10);

        final SettingPresentationRegistry.Registration registration =
                registry.register(descriptor);

        assertTrue(registration.active());
        assertEquals(
                descriptor,
                registry.find(
                        "combat.autoblock"));
        assertThrows(
                IllegalArgumentException.class,
                () -> registry.register(
                        new SettingDescriptor(
                                "combat.autoblock",
                                "Duplicate",
                                SettingValueKind.BOOLEAN,
                                20)));

        registration.close();
        registration.close();

        assertNull(
                registry.find(
                        "combat.autoblock"));
    }
}
