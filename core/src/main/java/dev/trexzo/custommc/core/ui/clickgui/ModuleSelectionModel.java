package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleRegistry;

import java.util.Objects;

public final class ModuleSelectionModel {
    private final ModuleRegistry modules;
    private String selectedModuleId;

    public ModuleSelectionModel(
            final ModuleRegistry modules) {
        this.modules = Objects.requireNonNull(
                modules,
                "modules");
    }

    public synchronized void select(
            final String moduleId) {
        final String id =
                Objects.requireNonNull(
                        moduleId,
                        "moduleId");
        if (modules.find(id) == null) {
            throw new IllegalArgumentException(
                    "unknown module: " + id);
        }
        selectedModuleId = id;
    }

    public synchronized void clear() {
        selectedModuleId = null;
    }

    public synchronized boolean hasSelection() {
        return selectedModuleId != null;
    }

    public synchronized String selectedModuleId() {
        return selectedModuleId;
    }

    public synchronized Module selectedModule() {
        return selectedModuleId == null
                ? null
                : modules.find(selectedModuleId);
    }

    public synchronized boolean isSelected(
            final String moduleId) {
        return selectedModuleId != null
                && selectedModuleId.equals(
                Objects.requireNonNull(
                        moduleId,
                        "moduleId"));
    }
}
