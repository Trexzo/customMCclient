package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentInputContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.SettingListPageContent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SettingsVisualHierarchyTest {
    private static final UiBounds ROOT = new UiBounds(200, 100, 500, 320);
    private static final ClickGuiPage PAGE =
            new ClickGuiPage("settings", "Settings", 0);

    @Test
    void liveSearchCountAndEmptyMessageUseOriginalVisibleRegistry() {
        final SettingRegistry settings = new SettingRegistry();
        settings.register(new Setting<Boolean>(
                "visual.enabled", false, v -> v != null, SettingCodecs.BOOLEAN));
        settings.register(new Setting<Boolean>(
                "combat.enabled", false, v -> v != null, SettingCodecs.BOOLEAN));
        final SettingPresentationRegistry descriptions =
                new SettingPresentationRegistry();
        descriptions.register(new SettingDescriptor(
                "visual.enabled", "Visuals", SettingValueKind.BOOLEAN, 0));
        descriptions.register(new SettingDescriptor(
                "combat.enabled", "Combat", SettingValueKind.BOOLEAN, 1));
        final SettingListPageContent content =
                new SettingListPageContent(settings, descriptions);
        final List<UiDrawCommand> filtered = content.compose(
                new ClickGuiContentContext(
                        snapshot("visual"), PAGE, ROOT, UiThemes.graphite()));
        assertTrue(containsText(filtered, "1 settings"));
        assertTrue(containsText(filtered, "CLICK TO TOGGLE"));
        assertTrue(containsText(filtered, "SELECT A CONTROL TO CONFIGURE"));
        final List<UiDrawCommand> empty = content.compose(
                new ClickGuiContentContext(
                        snapshot("not-a-setting"), PAGE, ROOT, UiThemes.graphite()));
        assertTrue(containsText(empty, "0 settings"));
        assertTrue(containsText(empty, "No settings match your search"));
    }

    @Test
    void visualNumericLevelRemainsStepBasedUnderOriginalRealRowClick() {
        final SettingRegistry settings = new SettingRegistry();
        final Setting<Integer> number = new Setting<Integer>(
                "combat.delay", 3, v -> v != null && v >= 1 && v <= 6,
                SettingCodecs.INTEGER);
        settings.register(number);
        final SettingPresentationRegistry descriptors =
                new SettingPresentationRegistry();
        descriptors.register(new SettingDescriptor(
                "combat.delay", "Delay", SettingValueKind.INTEGER, 0,
                new SettingNumericSpec(1, 6, 1)));
        final SettingListPageContent page =
                new SettingListPageContent(settings, descriptors);
        final ClickGuiSnapshot snapshot = snapshot("");
        final List<UiDrawCommand> draw = page.compose(
                new ClickGuiContentContext(snapshot, PAGE, ROOT,
                        UiThemes.graphite()));
        assertTrue(containsText(draw, "LEFT +  /  RIGHT -"));

        // The existing 38px native first-row hitbox is x224..676,
        // y158..196. Visual changes must not move or rewrite input.
        final ClickGuiContentInputContext input =
                new ClickGuiContentInputContext(snapshot, PAGE, ROOT);
        assertTrue(page.pointer(input, new UiPointerEvent(
                240, 170, UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertEquals(Integer.valueOf(4), number.get());
        assertTrue(page.pointer(input, new UiPointerEvent(
                240, 170, UiPointerButton.RIGHT, UiPointerAction.PRESS)));
        assertEquals(Integer.valueOf(3), number.get());
    }

    private static ClickGuiSnapshot snapshot(final String query) {
        return new ClickGuiSnapshot(
                true, "settings", query, Arrays.asList(PAGE));
    }

    private static boolean containsText(
            final List<UiDrawCommand> commands, final String text) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand
                    && text.equals(((UiTextCommand) command).text())) {
                return true;
            }
            if (command instanceof UiClipCommand
                    && containsText(((UiClipCommand) command).commands(), text)) {
                return true;
            }
        }
        return false;
    }
}
