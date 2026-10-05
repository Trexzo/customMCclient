package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.SettingListPageContent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SettingListPageContentTest {
    @Test
    void listUsesDescriptorsSearchPriorityAndLiveSettingValue() {
        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<Boolean> enabled =
                new Setting<Boolean>(
                        "render.esp.enabled",
                        Boolean.FALSE,
                        value -> true,
                        SettingCodecs.BOOLEAN);
        final Setting<Integer> range =
                new Setting<Integer>(
                        "combat.range",
                        3,
                        value -> value >= 1 && value <= 6,
                        SettingCodecs.INTEGER);

        settings.register(enabled);
        settings.register(range);
        enabled.set(Boolean.TRUE);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "render.esp.enabled",
                        "ESP Enabled",
                        SettingValueKind.BOOLEAN,
                        20));
        presentations.register(
                new SettingDescriptor(
                        "combat.range",
                        "Attack Range",
                        SettingValueKind.INTEGER,
                        10));

        final ClickGuiPage page =
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0);
        final ClickGuiSnapshot snapshot =
                new ClickGuiSnapshot(
                        true,
                        "settings",
                        "esp",
                        Arrays.asList(page));

        final List<UiDrawCommand> commands =
                new SettingListPageContent(
                        settings,
                        presentations)
                        .compose(
                                new ClickGuiContentContext(
                                        snapshot,
                                        page,
                                        new UiBounds(
                                                200.0F,
                                                100.0F,
                                                600.0F,
                                                400.0F),
                                        UiThemes.darkDefault()));

        boolean sawEspLabel = false;
        boolean sawTrue = false;
        boolean sawRange = false;

        for (UiDrawCommand command : commands) {
            if (!(command instanceof UiTextCommand)) {
                continue;
            }
            final String text =
                    ((UiTextCommand) command).text();
            sawEspLabel |= "ESP Enabled".equals(text);
            sawTrue |= "true".equals(text);
            sawRange |= "Attack Range".equals(text);
        }

        assertTrue(sawEspLabel);
        assertTrue(sawTrue);
        assertFalse(sawRange);
    }

    @Test
    void settingsWithoutPresentationDescriptorStayOutOfUi() {
        final SettingRegistry settings =
                new SettingRegistry();
        settings.register(
                new Setting<String>(
                        "internal.token",
                        "hidden",
                        value -> true,
                        SettingCodecs.STRING));

        final ClickGuiPage page =
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0);

        final List<UiDrawCommand> commands =
                new SettingListPageContent(
                        settings,
                        new SettingPresentationRegistry())
                        .compose(
                                new ClickGuiContentContext(
                                        new ClickGuiSnapshot(
                                                true,
                                                "settings",
                                                "",
                                                Arrays.asList(page)),
                                        page,
                                        new UiBounds(
                                                0.0F,
                                                0.0F,
                                                500.0F,
                                                300.0F),
                                        UiThemes.darkDefault()));

        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand) {
                assertFalse(
                        "hidden".equals(
                                ((UiTextCommand) command)
                                        .text()));
            }
        }
    }
}
