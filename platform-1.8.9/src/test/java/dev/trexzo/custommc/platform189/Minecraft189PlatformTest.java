package dev.trexzo.custommc.platform189;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.platform.GameEventBridge;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.event.ClientTickEvent;
import dev.trexzo.custommc.platform.event.RenderFrameEvent;
import dev.trexzo.custommc.platform.event.WorldPresenceEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189PlatformTest {
    @Test
    void attachPublishesOrderedEventsAndDetachRevokesBridge() {
        final EventBus events = new EventBus();
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ServiceRegistry services =
                new ServiceRegistry();
        final PlatformContext context =
                new PlatformContext(
                        events,
                        modules,
                        new ModuleController(modules),
                        services);

        final List<ClientTickEvent> ticks =
                new ArrayList<ClientTickEvent>();
        final List<WorldPresenceEvent> worlds =
                new ArrayList<WorldPresenceEvent>();
        final AtomicInteger frames =
                new AtomicInteger();

        events.subscribe(
                ClientTickEvent.class,
                ticks::add);
        events.subscribe(
                WorldPresenceEvent.class,
                worlds::add);
        events.subscribe(
                RenderFrameEvent.class,
                event -> frames.incrementAndGet());

        final Minecraft189Platform platform =
                new Minecraft189Platform();
        platform.attach(context);

        assertTrue(
                services.contains(
                        GameEventBridge.class));

        final GameEventBridge bridge =
                platform.bridge();

        bridge.clientTickStart();
        bridge.clientTickEnd();
        bridge.renderFrame(0.5F);
        bridge.worldPresenceChanged(true);
        bridge.worldPresenceChanged(true);
        bridge.worldPresenceChanged(false);

        assertEquals(2, ticks.size());
        assertEquals(
                ClientTickEvent.Phase.START,
                ticks.get(0).phase());
        assertEquals(
                ClientTickEvent.Phase.END,
                ticks.get(1).phase());
        assertEquals(0L, ticks.get(0).sequence());
        assertEquals(0L, ticks.get(1).sequence());

        assertEquals(1, frames.get());

        assertEquals(2, worlds.size());
        assertTrue(worlds.get(0).present());
        assertFalse(worlds.get(1).present());
        assertEquals(0L, worlds.get(0).sequence());
        assertEquals(1L, worlds.get(1).sequence());

        platform.detach();

        assertFalse(
                services.contains(
                        GameEventBridge.class));
        assertThrows(
                IllegalStateException.class,
                bridge::clientTickStart);
        assertThrows(
                IllegalStateException.class,
                platform::bridge);
    }

    @Test
    void tickPhaseOrderingIsEnforced() {
        final EventBus events = new EventBus();
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ServiceRegistry services =
                new ServiceRegistry();
        final Minecraft189Platform platform =
                new Minecraft189Platform();

        platform.attach(
                new PlatformContext(
                        events,
                        modules,
                        new ModuleController(modules),
                        services));

        final GameEventBridge bridge =
                platform.bridge();

        assertThrows(
                IllegalStateException.class,
                bridge::clientTickEnd);

        bridge.clientTickStart();

        assertThrows(
                IllegalStateException.class,
                bridge::clientTickStart);
    }
}
