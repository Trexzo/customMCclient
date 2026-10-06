package dev.trexzo.custommc.core.module;

import java.util.Objects;

public final class ModuleKeybindController {
    private final ModuleKeybindRegistry keybinds;
    private final ModuleController modules;

    public ModuleKeybindController(
            final ModuleKeybindRegistry keybinds,
            final ModuleController modules) {
        this.keybinds = Objects.requireNonNull(
                keybinds,
                "keybinds");
        this.modules = Objects.requireNonNull(
                modules,
                "modules");
    }

    public boolean press(
            final ModuleKeyChord chord) {
        Objects.requireNonNull(chord, "chord");

        final ModuleKeybind binding =
                keybinds.findByChord(chord);
        if (binding == null) {
            return false;
        }

        final String moduleId =
                binding.moduleId();
        final ModuleState state =
                modules.stateOf(moduleId);

        switch (state) {
            case DISABLED:
                modules.enable(moduleId);
                return true;
            case ENABLED:
            case FAILED:
                modules.disable(moduleId);
                return true;
            case ENABLING:
            case DISABLING:
            default:
                return true;
        }
    }
}
