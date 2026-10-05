package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.UnknownSettingPolicy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class SettingRegistryTest {
    @Test
    void snapshotIsCanonicalAndSorted() {
        final SettingRegistry registry = new SettingRegistry();
        registry.register(new Setting<Integer>(
                "z.range",
                3,
                value -> value >= 1 && value <= 6,
                SettingCodecs.INTEGER));
        registry.register(new Setting<Boolean>(
                "a.enabled",
                false,
                value -> true,
                SettingCodecs.BOOLEAN));

        assertEquals(
                java.util.Arrays.asList("a.enabled", "z.range"),
                new ArrayList<String>(
                        registry.snapshotEncoded().keySet()));
    }

    @Test
    void applyIsTransactionalWhenAnyValueIsInvalid() {
        final Setting<Integer> range =
                new Setting<Integer>(
                        "range",
                        3,
                        value -> value >= 1 && value <= 6,
                        SettingCodecs.INTEGER);
        final Setting<Integer> delay =
                new Setting<Integer>(
                        "delay",
                        2,
                        value -> value >= 0 && value <= 5,
                        SettingCodecs.INTEGER);

        final SettingRegistry registry = new SettingRegistry();
        registry.register(range);
        registry.register(delay);

        final Map<String, String> incoming =
                new LinkedHashMap<String, String>();
        incoming.put("range", "5");
        incoming.put("delay", "99");

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.applyEncoded(
                        incoming,
                        UnknownSettingPolicy.REJECT));

        assertEquals(3, range.get());
        assertEquals(2, delay.get());
    }

    @Test
    void unknownKeysHaveExplicitPolicy() {
        final SettingRegistry registry = new SettingRegistry();
        final Map<String, String> incoming =
                new LinkedHashMap<String, String>();
        incoming.put("future.setting", "1");

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.applyEncoded(
                        incoming,
                        UnknownSettingPolicy.REJECT));

        registry.applyEncoded(
                incoming,
                UnknownSettingPolicy.IGNORE);
    }
}
