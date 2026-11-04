package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModuleKeybind;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
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
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
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
    private final ModuleKeybindEditorController keybindEditor;
    private final SettingRegistry settings;
    private final SettingPresentationRegistry settingPresentations;
    private final SettingEditorController editor =
            new SettingEditorController();
    private final ClickGuiContentScrollState scroll =
            new ClickGuiContentScrollState();
    private final UiContentScrollDrag railDrag = new UiContentScrollDrag();
    private final UiNumericSettingScrub numericScrub = new UiNumericSettingScrub();
    // A detail page is contextual: different module settings must never
    // inherit the previous module's scrolled-to-middle position.
    private String displayedModuleId;

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
        this(
                selection,
                controller,
                modulePresentations,
                moduleSettings,
                moduleKeybinds,
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
            final ModuleKeybindAssignments moduleKeybindAssignments,
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
        this.keybindEditor =
                moduleKeybindAssignments == null
                        ? null
                        : new ModuleKeybindEditorController(
                                moduleKeybindAssignments);
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
        syncSelection(module == null ? null : module.id());

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
                            "Open settings with the module dots or right-click.",
                            theme.color(
                                    UiColorRole.TEXT_MUTED)));
            return commands;
        }

        final String moduleId = module.id();
        final ModuleDescriptor moduleDescriptor =
                modulePresentations.find(moduleId);
        final List<SettingEntry> visible =
                visibleSettings(moduleId);
        final float totalHeight = contentHeight(visible.size());
        final float scrollOffset =
                scroll.offset(totalHeight, bounds.height());

        final ModuleState state = controller.stateOf(moduleId);
        final String displayName = moduleDescriptor == null
                ? moduleId : moduleDescriptor.displayName();
        final String description = moduleDescriptor == null
                ? null : moduleDescriptor.description();
        String bindText = null;
        boolean capturing = false;
        if (moduleKeybinds != null) {
            final ModuleKeybind binding = moduleKeybinds.findByModule(moduleId);
            final String captureLabel = keybindEditor == null
                    ? null : keybindEditor.captureLabel(moduleId);
            capturing = captureLabel != null;
            bindText = capturing ? captureLabel
                    : binding == null ? "Bind: Unbound"
                    : "Bind: " + binding.chord();
        }
        commands.addAll(UiModuleDetailHeader.compose(
                bounds, scrollOffset, displayName,
                state, description, bindText, capturing, theme));
        // The settings visible here belong to THIS selected module, not
        // the 397-item global settings list. Keep this quiet indicator
        // outside the original name, bind and back-button hit regions.
        if (bounds.width() >= 430.0F) {
            commands.add(new UiTextCommand(1,
                    bounds.x() + bounds.width() - 117.0F,
                    bounds.y() + PADDING + 44.0F - scrollOffset,
                    UiFonts.DEFAULT, visible.size() + " settings",
                    theme.color(UiColorRole.TEXT_MUTED)));
        }
        if (visible.isEmpty()) {
            commands.add(new UiTextCommand(1,
                    bounds.x() + PADDING,
                    bounds.y() + PADDING + HEADER_HEIGHT + 10.0F,
                    UiFonts.DEFAULT, "No settings for this module",
                    theme.color(UiColorRole.TEXT_MUTED)));
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

            commands.addAll(UiSettingRowVisuals.compose(
                    row, entry.setting, entry.descriptor,
                    editor.displayValue(entry.setting),
                    editor.isEditing(entry.setting), theme));
        }

        commands.addAll(UiContentScrollRail.compose(
                bounds, totalHeight, scrollOffset, theme));
        commands.addAll(UiContentBottomCue.compose(
                bounds, totalHeight, scrollOffset, theme));
        // Only after the full original header scrolls out, pin a quiet
        // source-derived identity strip above settings and their scrollrail.
        // Back navigation stays on top and retains its existing hitbox.
        commands.addAll(UiModuleDetailStickyHeader.compose(
                bounds, scrollOffset, displayName, visible.size(), state, theme));
        commands.addAll(UiModuleDetailBackLink.compose(bounds, theme));
        return commands;
    }

    @Override
    public void cancelPointerDrag() {
        railDrag.cancel();
        numericScrub.cancel();
    }

    @Override
    public boolean capturesPointerDrag() {
        return railDrag.dragging() || numericScrub.dragging();
    }

    @Override
    public boolean pointer(
            final ClickGuiContentInputContext context,
            final UiPointerEvent event) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(event, "event");

        final Module module = selection.selectedModule();
        syncSelection(module == null ? null : module.id());
        if (module == null) {
            return false;
        }

        final List<SettingEntry> visible =
                visibleSettings(module.id());
        final float totalHeight = contentHeight(visible.size());
        // S49: use the same S48 native rail drag in contextual details,
        // before keybind focus, back navigation and setting editing.
        if (railDrag.pointer(event, context.bounds(), totalHeight, scroll)) {
            if (event.action() == UiPointerAction.PRESS) {
                numericScrub.cancel();
            }
            return true;
        }
        if (numericScrub.pointer(event)) {
            return true;
        }
        if (event.action() == UiPointerAction.PRESS) {
            numericScrub.cancel();
        }
        final float scrollOffset =
                scroll.offset(totalHeight, context.bounds().height());
        if (event.action() == UiPointerAction.PRESS
                && UiContentBottomCue.blocksPointer(
                        context.bounds(), totalHeight, scrollOffset,
                        event.x(), event.y())) {
            return false;
        }
        if (event.action() == UiPointerAction.PRESS
                && event.button() == UiPointerButton.LEFT
                && UiModuleDetailBackLink.hit(
                        context.bounds(), event.x(), event.y())) {
            final ClickGuiNavigator navigator = context.navigator();
            return navigator != null
                    && navigator.selectPage(ClickGuiRuntime.MODULES_PAGE_ID);
        }
        // A settings row may physically scroll behind the sticky header.
        // It must never receive mouse edits through the visible strip.
        if (event.action() == UiPointerAction.PRESS
                && UiModuleDetailStickyHeader.blocksPointer(
                        context.bounds(), scrollOffset, event.x(), event.y())) {
            return true;
        }

        if (keybindEditor != null
                && event.action() == UiPointerAction.PRESS
                && event.button() == UiPointerButton.LEFT
                && bindBounds(
                context.bounds(),
                scrollOffset)
                .contains(
                        event.x(),
                        event.y())) {
            return keybindEditor.begin(
                    context,
                    module.id());
        }

        for (int index = 0;
             index < visible.size();
             index++) {
            final SettingEntry entry =
                    visible.get(index);
            final UiBounds row = rowBounds(
                    context.bounds(), index, scrollOffset);
            if (!row.contains(event.x(), event.y())) {
                continue;
            }
            if (numericScrub.begin(row, entry.setting,
                    entry.descriptor, event)) {
                return true;
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
        syncSelection(module == null ? null : module.id());
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

    private void syncSelection(final String moduleId) {
        if (!Objects.equals(displayedModuleId, moduleId)) {
            displayedModuleId = moduleId;
            scroll.reset();
            railDrag.cancel();
            numericScrub.cancel();
        }
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

    private static UiBounds bindBounds(
            final UiBounds bounds,
            final float scrollOffset) {
        return new UiBounds(
                bounds.x() + PADDING,
                bounds.y()
                        + PADDING
                        + 38.0F
                        - scrollOffset,
                Math.max(
                        0.0F,
                        Math.min(
                                280.0F,
                                bounds.width()
                                        - PADDING * 2.0F)),
                28.0F);
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
