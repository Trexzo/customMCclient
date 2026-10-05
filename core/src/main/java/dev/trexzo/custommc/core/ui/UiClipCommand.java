package dev.trexzo.custommc.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class UiClipCommand
        implements UiDrawCommand {
    private final int layer;
    private final UiBounds bounds;
    private final List<UiDrawCommand> commands;

    public UiClipCommand(
            final int layer,
            final UiBounds bounds,
            final List<UiDrawCommand> commands) {
        this.layer = layer;
        this.bounds = Objects.requireNonNull(
                bounds,
                "bounds");
        Objects.requireNonNull(commands, "commands");

        final List<UiDrawCommand> copy =
                new ArrayList<UiDrawCommand>(commands.size());
        for (UiDrawCommand command : commands) {
            copy.add(
                    Objects.requireNonNull(
                            command,
                            "command"));
        }
        this.commands =
                Collections.unmodifiableList(copy);
    }

    @Override
    public int layer() {
        return layer;
    }

    public UiBounds bounds() {
        return bounds;
    }

    public List<UiDrawCommand> commands() {
        return commands;
    }
}
