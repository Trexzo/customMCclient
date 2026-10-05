package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
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

    @Test
    void leftPressOnBooleanRowTogglesLiveSetting() {
        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<Boolean> enabled =
                new Setting<Boolean>(
                        "render.esp.enabled",
                        Boolean.FALSE,
                        value -> true,
                        SettingCodecs.BOOLEAN);
        settings.register(enabled);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "render.esp.enabled",
                        "ESP Enabled",
                        SettingValueKind.BOOLEAN,
                        0));

        final SettingListPageContent content =
                new SettingListPageContent(
                        settings,
                        presentations);
        final ClickGuiPage page =
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0);
        final UiBounds bounds =
                new UiBounds(
                        200.0F,
                        100.0F,
                        600.0F,
                        400.0F);
        final ClickGuiContentInputContext context =
                new ClickGuiContentInputContext(
                        new ClickGuiSnapshot(
                                true,
                                "settings",
                                "",
                                Arrays.asList(page)),
                        page,
                        bounds);

        final boolean handled =
                content.pointer(
                        context,
                        new UiPointerEvent(
                                240.0F,
                                170.0F,
                                UiPointerButton.LEFT,
                                UiPointerAction.PRESS));

        assertTrue(handled);
        assertTrue(enabled.get());
    }

    @Test
    void booleanToggleStillHonorsSettingValidator() {
        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<Boolean> locked =
                new Setting<Boolean>(
                        "locked",
                        Boolean.FALSE,
                        value -> !value,
                        SettingCodecs.BOOLEAN);
        settings.register(locked);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "locked",
                        "Locked",
                        SettingValueKind.BOOLEAN,
                        0));

        final ClickGuiPage page =
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0);
        final boolean handled =
                new SettingListPageContent(
                        settings,
                        presentations)
                        .pointer(
                                new ClickGuiContentInputContext(
                                        new ClickGuiSnapshot(
                                                true,
                                                "settings",
                                                "",
                                                Arrays.asList(page)),
                                        page,
                                        new UiBounds(
                                                200.0F,
                                                100.0F,
                                                600.0F,
                                                400.0F)),
                                new UiPointerEvent(
                                        240.0F,
                                        170.0F,
                                        UiPointerButton.LEFT,
                                        UiPointerAction.PRESS));

        assertTrue(handled);
        assertFalse(locked.get());
    }

    @Test
    void nonBooleanRowsAreNotMutatedByBooleanEditor() {
        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<Integer> range =
                new Setting<Integer>(
                        "combat.range",
                        3,
                        value -> value >= 1 && value <= 6,
                        SettingCodecs.INTEGER);
        settings.register(range);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "combat.range",
                        "Attack Range",
                        SettingValueKind.INTEGER,
                        0));

        final ClickGuiPage page =
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0);
        final boolean handled =
                new SettingListPageContent(
                        settings,
                        presentations)
                        .pointer(
                                new ClickGuiContentInputContext(
                                        new ClickGuiSnapshot(
                                                true,
                                                "settings",
                                                "",
                                                Arrays.asList(page)),
                                        page,
                                        new UiBounds(
                                                200.0F,
                                                100.0F,
                                                600.0F,
                                                400.0F)),
                                new UiPointerEvent(
                                        240.0F,
                                        170.0F,
                                        UiPointerButton.LEFT,
                                        UiPointerAction.PRESS));

        assertFalse(handled);
        assertTrue(range.get() == 3);
    }

    @Test
    void integerRowsStepLeftAndRightWithinPresentationBounds() {
        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<Integer> range =
                new Setting<Integer>(
                        "combat.range",
                        3,
                        value -> value >= 1 && value <= 6,
                        SettingCodecs.INTEGER);
        settings.register(range);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "combat.range",
                        "Attack Range",
                        SettingValueKind.INTEGER,
                        0,
                        new SettingNumericSpec(
                                1.0D,
                                6.0D,
                                1.0D)));

        final SettingListPageContent content =
                new SettingListPageContent(
                        settings,
                        presentations);
        final ClickGuiPage page =
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0);
        final ClickGuiContentInputContext context =
                new ClickGuiContentInputContext(
                        new ClickGuiSnapshot(
                                true,
                                "settings",
                                "",
                                Arrays.asList(page)),
                        page,
                        new UiBounds(
                                200.0F,
                                100.0F,
                                600.0F,
                                400.0F));

        assertTrue(
                content.pointer(
                        context,
                        new UiPointerEvent(
                                240.0F,
                                170.0F,
                                UiPointerButton.LEFT,
                                UiPointerAction.PRESS)));
        assertEquals(
                Integer.valueOf(4),
                range.get());

        assertTrue(
                content.pointer(
                        context,
                        new UiPointerEvent(
                                240.0F,
                                170.0F,
                                UiPointerButton.RIGHT,
                                UiPointerAction.PRESS)));
        assertEquals(
                Integer.valueOf(3),
                range.get());

        range.set(6);
        assertTrue(
                content.pointer(
                        context,
                        new UiPointerEvent(
                                240.0F,
                                170.0F,
                                UiPointerButton.LEFT,
                                UiPointerAction.PRESS)));
        assertEquals(
                Integer.valueOf(6),
                range.get());
    }

    @Test
    void integerEditorStillHonorsSettingValidator() {
        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<Integer> locked =
                new Setting<Integer>(
                        "locked.range",
                        3,
                        value -> value <= 3,
                        SettingCodecs.INTEGER);
        settings.register(locked);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "locked.range",
                        "Locked Range",
                        SettingValueKind.INTEGER,
                        0,
                        new SettingNumericSpec(
                                1.0D,
                                6.0D,
                                1.0D)));

        final ClickGuiPage page =
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0);
        final boolean handled =
                new SettingListPageContent(
                        settings,
                        presentations)
                        .pointer(
                                new ClickGuiContentInputContext(
                                        new ClickGuiSnapshot(
                                                true,
                                                "settings",
                                                "",
                                                Arrays.asList(page)),
                                        page,
                                        new UiBounds(
                                                200.0F,
                                                100.0F,
                                                600.0F,
                                                400.0F)),
                                new UiPointerEvent(
                                        240.0F,
                                        170.0F,
                                        UiPointerButton.LEFT,
                                        UiPointerAction.PRESS));

        assertTrue(handled);
        assertEquals(
                Integer.valueOf(3),
                locked.get());
    }

    @Test
    void integerDescriptorWithoutNumericSpecRemainsReadOnly() {
        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<Integer> range =
                new Setting<Integer>(
                        "combat.range",
                        3,
                        value -> true,
                        SettingCodecs.INTEGER);
        settings.register(range);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "combat.range",
                        "Attack Range",
                        SettingValueKind.INTEGER,
                        0));

        final ClickGuiPage page =
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0);
        final boolean handled =
                new SettingListPageContent(
                        settings,
                        presentations)
                        .pointer(
                                new ClickGuiContentInputContext(
                                        new ClickGuiSnapshot(
                                                true,
                                                "settings",
                                                "",
                                                Arrays.asList(page)),
                                        page,
                                        new UiBounds(
                                                200.0F,
                                                100.0F,
                                                600.0F,
                                                400.0F)),
                                new UiPointerEvent(
                                        240.0F,
                                        170.0F,
                                        UiPointerButton.LEFT,
                                        UiPointerAction.PRESS));

        assertFalse(handled);
        assertEquals(
                Integer.valueOf(3),
                range.get());
    }

}
