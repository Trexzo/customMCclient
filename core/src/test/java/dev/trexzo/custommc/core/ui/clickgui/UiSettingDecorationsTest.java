package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiSettingDecorationsTest {
    private static final UiBounds NORMAL = new UiBounds(100, 50, 400, 36);

    @Test
    void booleanSwitchMirrorsAuthoritativeSettingWithoutEditingIt() {
        final Setting<Boolean> value = new Setting<Boolean>(
                "test.switch", false, x -> x != null, SettingCodecs.BOOLEAN);
        final SettingDescriptor descriptor = new SettingDescriptor(
                "test.switch", "Test Switch", SettingValueKind.BOOLEAN, 0);
        final List<UiDrawCommand> off = UiSettingDecorations.compose(
                NORMAL, value, descriptor, UiThemes.graphite());
        assertEquals(2, off.size());
        assertEquals(UiThemes.graphite().color(UiColorRole.BORDER),
                ((UiRoundedRectCommand) off.get(0)).argb());
        assertEquals(Boolean.FALSE, value.get());

        value.set(Boolean.TRUE);
        final List<UiDrawCommand> on = UiSettingDecorations.compose(
                NORMAL, value, descriptor, UiThemes.graphite());
        assertEquals(2, on.size());
        assertEquals(UiThemes.graphite().color(UiColorRole.ACCENT),
                ((UiRoundedRectCommand) on.get(0)).argb());
        assertEquals(Boolean.TRUE, value.get());
        assertTrue(((UiRoundedRectCommand) on.get(1)).x()
                > ((UiRoundedRectCommand) off.get(1)).x());
    }

    @Test
    void numericTrackClampsAndDoesNotExceedRowAtSmallRatios() {
        final Setting<Integer> value = new Setting<Integer>(
                "test.value", 1,
                x -> x != null && x >= 0 && x <= 100, SettingCodecs.INTEGER);
        final SettingDescriptor descriptor = new SettingDescriptor(
                "test.value", "Value", SettingValueKind.INTEGER, 0,
                new SettingNumericSpec(0, 100, 1));
        final List<UiDrawCommand> atOne = UiSettingDecorations.compose(
                NORMAL, value, descriptor, UiThemes.graphite());
        assertEquals(2, atOne.size());
        assertEquals(2.42F, ((UiRoundedRectCommand) atOne.get(1)).width(), 0.001F);
        assertEquals(Integer.valueOf(1), value.get());
        value.set(100);
        final List<UiDrawCommand> atMax = UiSettingDecorations.compose(
                NORMAL, value, descriptor, UiThemes.graphite());
        assertEquals(242F, ((UiRoundedRectCommand) atMax.get(1)).width(), .001F);
    }

    @Test
    void tinyRowsAndZeroFractionNeverMakeIllegalRoundedGeometry() {
        final Setting<Boolean> enabled = new Setting<Boolean>(
                "test.bool", true, x -> x != null, SettingCodecs.BOOLEAN);
        assertTrue(UiSettingDecorations.compose(
                new UiBounds(0, 0, 42, 12), enabled,
                new SettingDescriptor("test.bool", "Bool",
                        SettingValueKind.BOOLEAN, 0), UiThemes.graphite())
                .isEmpty());
        final Setting<Integer> zero = new Setting<Integer>(
                "test.zero", 0, x -> x != null, SettingCodecs.INTEGER);
        final SettingDescriptor numeric = new SettingDescriptor(
                "test.zero", "Zero", SettingValueKind.INTEGER, 0,
                new SettingNumericSpec(0, 100, 1));
        assertEquals(1, UiSettingDecorations.compose(
                NORMAL, zero, numeric, UiThemes.graphite()).size());
        assertTrue(UiSettingDecorations.compose(
                new UiBounds(0, 0, 60, 12), zero, numeric,
                UiThemes.graphite()).isEmpty());
    }
}
