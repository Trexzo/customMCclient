package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiTheme;
import dev.trexzo.custommc.core.ui.UiThemeSettings;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiThemeSettingsTest {
    @Test
    void themeCyclesAndKeepsTheInjectedProviderByDefault() {
        final SettingRegistry registry = new SettingRegistry();
        final Setting<String> selected = UiThemeSettings.setting();
        final UiTheme injected = UiThemes.frost();
        try (SettingRegistry.Registration registration =
                     registry.register(selected)) {
            assertEquals("default", selected.get());
            assertEquals(injected.id(),
                    UiThemeSettings.resolve(registry, injected).id());
            assertTrue(UiThemeSettings.cycle(selected, true));
            assertEquals("frost", selected.get());
            assertEquals(UiThemes.frost().color(UiColorRole.ACCENT),
                    UiThemeSettings.resolve(registry, UiThemes.nebula())
                            .color(UiColorRole.ACCENT));
            assertTrue(UiThemeSettings.cycle(selected, true));
            assertEquals("mono", selected.get());
            assertTrue(UiThemeSettings.cycle(selected, false));
            assertEquals("frost", selected.get());
            assertTrue(UiThemeSettings.cycle(selected, false));
            assertEquals("default", selected.get());
            assertEquals(injected.id(),
                    UiThemeSettings.resolve(registry, injected).id());
        }
        assertEquals(injected.id(),
                UiThemeSettings.resolve(registry, injected).id());
    }

    @Test
    void rejectsUnknownProfilesWithoutChangingActiveTheme() {
        final Setting<String> selected = UiThemeSettings.setting();
        assertThrows(IllegalArgumentException.class,
                () -> selected.set("hacked"));
        assertThrows(IllegalArgumentException.class,
                () -> selected.set(""));
        assertEquals("default", selected.get());
        assertFalse(UiThemeSettings.cycle(null, true));
    }
}
