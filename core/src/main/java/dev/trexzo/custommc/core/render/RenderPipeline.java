package dev.trexzo.custommc.core.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RenderPipeline {
    private final Map<String, RenderPass> passes =
            new LinkedHashMap<String, RenderPass>();

    public synchronized void register(final RenderPass pass) {
        Objects.requireNonNull(pass, "pass");
        final String id = requireId(pass.id());
        if (passes.containsKey(id)) {
            throw new IllegalArgumentException(
                    "duplicate render pass id: " + id);
        }
        Objects.requireNonNull(pass.stage(), "pass.stage");
        passes.put(id, pass);
    }

    public synchronized void unregister(final String id) {
        passes.remove(Objects.requireNonNull(id, "id"));
    }

    public void render(
            final RenderStage stage,
            final RenderFrame frame) {
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(frame, "frame");

        final List<RenderPass> snapshot =
                snapshotFor(stage);
        for (RenderPass pass : snapshot) {
            pass.render(frame);
        }
    }

    public synchronized List<RenderPass> snapshotFor(
            final RenderStage stage) {
        Objects.requireNonNull(stage, "stage");

        final List<RenderPass> result =
                new ArrayList<RenderPass>();
        for (RenderPass pass : passes.values()) {
            if (pass.stage() == stage) {
                result.add(pass);
            }
        }

        Collections.sort(
                result,
                new Comparator<RenderPass>() {
                    @Override
                    public int compare(
                            final RenderPass left,
                            final RenderPass right) {
                        final int priority =
                                Integer.compare(
                                        left.priority(),
                                        right.priority());
                        if (priority != 0) {
                            return priority;
                        }
                        return left.id().compareTo(right.id());
                    }
                });

        return Collections.unmodifiableList(result);
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "render pass id must not be blank");
        }
        return id;
    }
}
