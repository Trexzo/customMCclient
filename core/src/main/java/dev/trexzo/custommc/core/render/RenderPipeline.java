package dev.trexzo.custommc.core.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RenderPipeline {
    private static final Comparator<RenderPass> PASS_ORDER =
            new Comparator<RenderPass>() {
                @Override
                public int compare(
                        final RenderPass left,
                        final RenderPass right) {
                    final int priority = Integer.compare(
                            left.priority(),
                            right.priority());
                    if (priority != 0) {
                        return priority;
                    }
                    return left.id().compareTo(right.id());
                }
            };

    private final Map<String, RenderPass> passes =
            new LinkedHashMap<String, RenderPass>();
    private final Map<RenderStage, List<RenderPass>> plans =
            new EnumMap<RenderStage, List<RenderPass>>(RenderStage.class);

    public RenderPipeline() {
        for (RenderStage stage : RenderStage.values()) {
            plans.put(
                    stage,
                    Collections.<RenderPass>emptyList());
        }
    }

    public synchronized Registration register(final RenderPass pass) {
        Objects.requireNonNull(pass, "pass");
        final String id = requireId(pass.id());
        final RenderStage stage =
                Objects.requireNonNull(pass.stage(), "pass.stage");

        if (passes.containsKey(id)) {
            throw new IllegalArgumentException(
                    "duplicate render pass id: " + id);
        }

        passes.put(id, pass);
        rebuild(stage);

        return new RegistrationImpl(
                this,
                id,
                pass);
    }

    public synchronized void unregister(final String id) {
        final RenderPass removed =
                passes.remove(Objects.requireNonNull(id, "id"));
        if (removed != null) {
            rebuild(removed.stage());
        }
    }

    public void render(
            final RenderStage stage,
            final RenderFrame frame) {
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(frame, "frame");

        final List<RenderPass> plan = snapshotFor(stage);
        for (RenderPass pass : plan) {
            pass.render(frame);
        }
    }

    public synchronized List<RenderPass> snapshotFor(
            final RenderStage stage) {
        return plans.get(Objects.requireNonNull(stage, "stage"));
    }

    private synchronized void unregister(
            final String id,
            final RenderPass expected) {
        final RenderPass current = passes.get(id);
        if (current != expected) {
            return;
        }

        passes.remove(id);
        rebuild(current.stage());
    }

    private void rebuild(final RenderStage stage) {
        final List<RenderPass> result =
                new ArrayList<RenderPass>();

        for (RenderPass pass : passes.values()) {
            if (pass.stage() == stage) {
                result.add(pass);
            }
        }

        Collections.sort(result, PASS_ORDER);
        plans.put(
                stage,
                Collections.unmodifiableList(result));
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "render pass id must not be blank");
        }
        return id;
    }

    public interface Registration extends AutoCloseable {
        RenderPass pass();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final RenderPipeline pipeline;
        private final String id;
        private final RenderPass pass;
        private boolean active = true;

        RegistrationImpl(
                final RenderPipeline pipeline,
                final String id,
                final RenderPass pass) {
            this.pipeline = pipeline;
            this.id = id;
            this.pass = pass;
        }

        @Override
        public RenderPass pass() {
            return pass;
        }

        @Override
        public synchronized boolean active() {
            return active;
        }

        @Override
        public void close() {
            synchronized (this) {
                if (!active) {
                    return;
                }
                active = false;
            }

            pipeline.unregister(id, pass);
        }
    }
}
