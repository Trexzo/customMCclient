package dev.trexzo.custommc.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class UiCommandBuffer {
    private static final Comparator<UiDrawCommand> LAYER_ORDER =
            new Comparator<UiDrawCommand>() {
                @Override
                public int compare(
                        final UiDrawCommand left,
                        final UiDrawCommand right) {
                    return Integer.compare(
                            left.layer(),
                            right.layer());
                }
            };

    private final List<UiDrawCommand> commands =
            new ArrayList<UiDrawCommand>();
    private List<UiDrawCommand> sealedView;

    public synchronized void add(final UiDrawCommand command) {
        requireOpen();
        commands.add(Objects.requireNonNull(command, "command"));
    }

    public synchronized int size() {
        return commands.size();
    }

    public synchronized boolean sealed() {
        return sealedView != null;
    }

    public synchronized List<UiDrawCommand> seal() {
        if (sealedView != null) {
            return sealedView;
        }

        final List<UiDrawCommand> ordered =
                new ArrayList<UiDrawCommand>(commands);
        Collections.sort(ordered, LAYER_ORDER);
        sealedView = Collections.unmodifiableList(ordered);
        return sealedView;
    }

    private void requireOpen() {
        if (sealedView != null) {
            throw new IllegalStateException(
                    "UI command buffer is sealed");
        }
    }
}
