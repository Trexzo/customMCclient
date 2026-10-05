package dev.trexzo.custommc.platform189;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.platform.GameEventBridge;
import dev.trexzo.custommc.platform.event.ClientTickEvent;
import dev.trexzo.custommc.platform.event.RenderFrameEvent;
import dev.trexzo.custommc.platform.event.WorldPresenceEvent;

import java.util.Objects;

final class Minecraft189EventBridge
        implements GameEventBridge {
    private final EventBus events;

    private boolean active = true;
    private boolean tickOpen;
    private long tickSequence;
    private long frameSequence;
    private long worldSequence;
    private Boolean worldPresent;

    Minecraft189EventBridge(final EventBus events) {
        this.events = Objects.requireNonNull(
                events,
                "events");
    }

    @Override
    public synchronized void clientTickStart() {
        requireActive();
        if (tickOpen) {
            throw new IllegalStateException(
                    "client tick already started");
        }

        events.publish(
                new ClientTickEvent(
                        ClientTickEvent.Phase.START,
                        tickSequence));
        tickOpen = true;
    }

    @Override
    public synchronized void clientTickEnd() {
        requireActive();
        if (!tickOpen) {
            throw new IllegalStateException(
                    "client tick end without start");
        }

        events.publish(
                new ClientTickEvent(
                        ClientTickEvent.Phase.END,
                        tickSequence));
        tickOpen = false;
        tickSequence++;
    }

    @Override
    public synchronized void renderFrame(
            final float partialTicks) {
        requireActive();
        events.publish(
                new RenderFrameEvent(
                        frameSequence,
                        partialTicks));
        frameSequence++;
    }

    @Override
    public synchronized void worldPresenceChanged(
            final boolean present) {
        requireActive();

        if (worldPresent != null
                && worldPresent.booleanValue() == present) {
            return;
        }

        worldPresent = Boolean.valueOf(present);
        events.publish(
                new WorldPresenceEvent(
                        worldSequence,
                        present));
        worldSequence++;
    }

    synchronized void deactivate() {
        active = false;
        tickOpen = false;
    }

    private void requireActive() {
        if (!active) {
            throw new IllegalStateException(
                    "Minecraft 1.8.9 event bridge is detached");
        }
    }
}
