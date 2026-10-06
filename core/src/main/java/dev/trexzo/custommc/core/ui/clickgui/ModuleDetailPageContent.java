package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModuleKeybind;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiScrollEvent;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ModuleDetailPageContent
        implements ClickGuiPageContent {
    private static final float PADDING = 24.0F;
    private static final float HEADER_HEIGHT = 78.0F;
    private static final float ROW_HEIGHT = 38.0F;
    private static final float ROW_GAP = 7.0F;
    private static final float ROW_RADIUS = 6.0F;

    private final ModuleSelectionModel selection;
    private final ModuleController controller;
    private final ModulePresentationRegistry modulePresentations;
    private final ModuleSettingRegistry moduleSettings;
    private final ModuleKeybindRegistry moduleKeybinds;
    private final SettingRegistry settings;
    private final SettingPresentationRegistry settingPresentations;
    private final SettingEditorController editor =
            new SettingEditorController();
    private final ClickGuiContentScrollState scroll =
            new ClickGuiContentScrollState();

    public ModuleDetailPageContent(
            final ModuleSelectionModel selection,
            final ModuleController controller,
            final ModulePresentationRegistry modulePresentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        this(
                selection,
                controller,
                modulePresentations,
                moduleSettings,
                null,
                settings,
                settingPresentations);
    }

    public ModuleDetailPageContent(
            final ModuleSelectionModel selection,
            final ModuleController controller,
            final ModulePresentationRegistry modulePresentations,
            final ModuleSettingRegistry moduleSettings,
            final ModuleKeybindRegistry moduleKeybinds,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        this.selection = Objects.requireNonNull(
                selection,
                "selection");
        this.controller = Objects.requireNonNull(
                controller,
                "controller");
        this.modulePresentations = Objects.requireNonNull(
                modulePresentations,
                "modulePresentations");
        this.moduleSettings = Objects.requireNonNull(
                moduleSettings,
                "moduleSettings");
        this.moduleKeybinds = moduleKeybinds;
        this.settings = Objects.requireNonNull(
                settings,
                "settings");
        this.settingPresentations = Objects.requireNonNull(
                settingPresentations,
                "settingPresentations");
    }

    @Override
    public List<UiDrawCommand> compose(
            final ClickGuiContentContext context) {
        Objects.requireNonNull(context, "context");

        final List<UiDrawCommand> commands =
                new ArrayList<UiDrawCommand>();
        final UiBounds bounds = context.bounds();
        final UiTheme theme = context.theme();
        final Module module = selection.selectedModule();

        if (module == null) {
            commands.add(
                    new UiTextCommand(
                            0,
                            bounds.x() + PADDING,
                            bounds.y() + PADDING,
                            UiFonts.DEFAULT,
                            "Select a module",
                            theme.color(
                                    UiColorRole.TEXT_PRIMARY)));
            commands.add(
                    new UiTextCommand(
                            0,
                            bounds.x() + PADDING,
                            bounds.y() + PADDING + 22.0F,
                            UiFonts.DEFAULT,
                            "Right-click a module to inspect its settings.",
                            theme.color(
                                    UiColorRole.TEXT_MUTED)));
            return commands;
        }

        final String moduleId = module.id();
        final ModuleDescriptor moduleDescriptor =
                modulePresentations.find(moduleId);
        final List<SettingEntry> visible =
                visibleSettings(moduleId);
        final float scrollOffset =
                scroll.offset(
                        contentHeight(visible.size()),
                        bounds.height());

        commands.add(
                new UiTextCommand(
                        0,
                        bounds.x() + PADDING,
                        bounds.y() + PADDING - scrollOffset,
                        UiFonts.DEFAULT,
                        moduleDescriptor == null
                                ? moduleId
                                : moduleDescriptor.displayName(),
                        theme.color(
                                UiColorRole.TEXT_PRIMARY)));

        final ModuleState state =
                controller.stateOf(moduleId);
        commands.add(
                new UiTextCommand(
                        0,
                        bounds.x() + PADDING,
                        bounds.y() + PADDING + 22.0F - scrollOffset,
                        UiFonts.DEFAULT,
                        state.name(),
                        theme.color(
                                stateColorRole(state))));

        if (moduleDescriptor != null
                && !moduleDescriptor.description().isEmpty()) {
            commands.add(
                    new UiTextCommand(
                            0,
                            bounds.x() + PADDING + 92.0F,
                            bounds.y() + PADDING + 22.0F - scrollOffset,
                            UiFonts.DEFAULT,
                            moduleDescriptor.description(),
                            theme.color(
                                    UiColorRole.TEXT_MUTED)));
        }

        if (moduleKeybinds != null) {
            final ModuleKeybind binding =
                    moduleKeybinds.findByModule(
                            moduleId);
            commands.add(
                    new UiTextCommand(
                            0,
                            bounds.x() + PADDING,
                            bounds.y() + PADDING + 44.0F - scrollOffset,
                            UiFonts.DEFAULT,
                            binding == null
                                    ? "Bind: Unbound"
                                    : "Bind: " + binding.chord(),
                            theme.color(
                                    UiColorRole.TEXT_MUTED)));
        }

        for (int index = 0;
             index < visible.size();
             index++) {
            final SettingEntry entry =
                    visible.get(index);
            final UiBounds row =
                    rowBounds(
                            bounds,
                            index,
                            scrollOffset);

            commands.add(
                    new UiRoundedRectCommand(
                            0,
                            row.x(),
                            row.y(),
                            row.width(),
                            row.height(),
                            Math.min(
                                    ROW_RADIUS,
                                    row.height() * 0.5F),
                            theme.color(
                                    UiColorRole.BACKGROUND)));

            commands.add(
                    new UiTextCommand(
                            0,
                            row.x() + 12.0F,
                            row.y() + 10.0F,
                            UiFonts.DEFAULT,
                            entry.descriptor.label(),
                            theme.color(
                                    UiColorRole.TEXT_PRIMARY)));

            final boolean editing =
                    editor.isEditing(
                            entry.setting);
            commands.add(
                    new UiTextCommand(
                            0,
                            Math.max(
                                    row.x() + 12.0F,
                                    row.x()
                                            + row.width()
                                            - 130.0F),
                            row.y() + 10.0F,
                            UiFonts.DEFAULT,
                            editor.displayValue(
                                    entry.setting),
                            theme.color(
                                    editing
                                            ? UiColorRole.ACCENT
                                            : UiColorRole.TEXT_MUTED)));
        }

        return commands;
    }

    @Override
    public boolean pointer(
            final ClickGuiContentInputContext context,
            final UiPointerEvent event) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(event, "event");

        final Module module = selection.selectedModule();
        if (module == null) {
            return false;
        }

        final List<SettingEntry> visible =
                visibleSettings(
                        module.id());
        final float scrollOffset =
                scroll.offset(
                        contentHeight(
                                visible.size()),
                        context.bounds().height());

        for (int index = 0;
             index < visible.size();
             index++) {
            final SettingEntry entry =
                    visible.get(index);
            if (!rowBounds(
                    context.bounds(),
                    index,
                    scrollOffset)
                    .contains(
                            event.x(),
                            event.y())) {
                continue;
            }

            return editor.pointer(
                    context,
                    entry.setting,
                    entry.descriptor,
                    event);
        }

        return false;
    }

    @Override
    public boolean scroll(
            final ClickGuiContentInputContext context,
            final UiScrollEvent event) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(event, "event");

        final Module module = selection.selectedModule();
        if (module == null) {
            return false;
        }

        scroll.scroll(
                event.deltaY(),
                contentHeight(
                        visibleSettings(
                                module.id())
                                .size()),
                context.bounds().height());
        return true;
    }

    private List<SettingEntry> visibleSettings(
            final String moduleId) {
        final List<SettingEntry> result =
                new ArrayList<SettingEntry>();

        for (ModuleSettingBinding binding :
                moduleSettings.bindingsForModule(
                        moduleId)) {
            final Setting<?> setting =
                    settings.find(
                            binding.settingId());
            final SettingDescriptor descriptor =
                    settingPresentations.find(
                            binding.settingId());

            if (setting == null
                    || descriptor == null) {
                continue;
            }

            result.add(
                    new SettingEntry(
                            setting,
                            descriptor));
        }

        return result;
    }

    private static UiBounds rowBounds(
            final UiBounds bounds,
            final int index,
            final float scrollOffset) {
        return new UiBounds(
                bounds.x() + PADDING,
                bounds.y()
                        + PADDING
                        + HEADER_HEIGHT
                        + index
                        * (ROW_HEIGHT + ROW_GAP)
                        - scrollOffset,
                Math.max(
                        0.0F,
                        bounds.width()
                                - PADDING * 2.0F),
                ROW_HEIGHT);
    }

    private static float contentHeight(
            final int rowCount) {
        if (rowCount <= 0) {
            return PADDING
                    + HEADER_HEIGHT
                    + PADDING;
        }
        return PADDING
                + HEADER_HEIGHT
                + rowCount * ROW_HEIGHT
                + (rowCount - 1) * ROW_GAP
                + PADDING;
    }

    private static UiColorRole stateColorRole(
            final ModuleState state) {
        switch (state) {
            case ENABLED:
                return UiColorRole.POSITIVE;
            case FAILED:
                return UiColorRole.DANGER;
            case ENABLING:
            case DISABLING:
                return UiColorRole.WARNING;
            case DISABLED:
            default:
                return UiColorRole.TEXT_MUTED;
        }
    }

    private static final class SettingEntry {
        private final Setting<?> setting;
        private final SettingDescriptor descriptor;

        SettingEntry(
                final Setting<?> setting,
                final SettingDescriptor descriptor) {
            this.setting = setting;
            this.descriptor = descriptor;
        }
    }
}
