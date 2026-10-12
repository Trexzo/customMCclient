package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ProjectedPlayerFrameTest {
    private static final double[] IDENTITY = {
            1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1
    };

    private static final class Entity implements Minecraft189NativeEntityUuidAccess,
            Minecraft189EntityHitboxBoundsAccess, Minecraft189PlayerPositionAccess {
        final UUID uuid;
        final double x;
        Entity(UUID uuid, double x) { this.uuid = uuid; this.x = x; }
        public UUID customMcNativeEntityUuid() { return uuid; }
        public double customMcPositionX() { return x; }
        public double customMcPositionY() { return 0; }
        public double customMcPositionZ() { return 0; }
        public double customMcLastTickPositionX() { return x-0.2D; }
        public double customMcLastTickPositionY() { return 0; }
        public double customMcLastTickPositionZ() { return 0; }
        public double[] customMcHitboxBounds() {
            return new double[]{x-0.1D,-0.2D,-0.1D,x+0.1D,0.2D,0.1D};
        }
    }

    private static Minecraft189WorldCameraFrameState.Snapshot camera() {
        Minecraft189WorldCameraFrameState state =
                new Minecraft189WorldCameraFrameState();
        state.accept(12, 0, 0.5F, IDENTITY, IDENTITY,
                new int[]{0,0,800,600});
        state.captureOrigin(12, 0, 0.5F, new Minecraft189PlayerPositionAccess() {
            public double customMcPositionX() { return 0; }
            public double customMcPositionY() { return 0; }
            public double customMcPositionZ() { return 0; }
            public double customMcLastTickPositionX() { return 0; }
            public double customMcLastTickPositionY() { return 0; }
            public double customMcLastTickPositionZ() { return 0; }
        });
        return state.snapshot();
    }

    @Test void nativeUuidPlayerTypeAabbAndInterpolatedOriginDetermineRects() {
        UUID me=UUID.randomUUID(), a=UUID.randomUUID(), mob=UUID.randomUUID();
        Object[] objects={new Entity(me,0),new Entity(a,0.4D),new Entity(mob,0.3D)};
        int[] kinds={
            Minecraft189WorldEntityKindState.LIVING
                |Minecraft189WorldEntityKindState.PLAYER
                |Minecraft189WorldEntityKindState.LOCAL_PLAYER,
            Minecraft189WorldEntityKindState.LIVING
                |Minecraft189WorldEntityKindState.PLAYER,
            Minecraft189WorldEntityKindState.LIVING};
        Minecraft189ProjectedPlayerFrame f =
                Minecraft189ProjectedPlayerFrame.collect(12,camera(),objects,kinds,
                        new UUID[]{me,a,mob});
        assertNotNull(f);
        assertEquals(12,f.frame());
        assertEquals(1,f.remoteCount());
        assertEquals(1,f.visible().size());
        assertEquals(a,f.visible().get(0).uuid);
        assertEquals(480.0F,f.visible().get(0).rect.left,0.01F);
        assertThrows(UnsupportedOperationException.class,
                () -> f.visible().clear());
    }

    @Test void staleOrMismatchedNativeListNeverProducesProjectedPlayer() {
        UUID a=UUID.randomUUID(), b=UUID.randomUUID();
        Object[] objects={new Entity(a,0)};
        int[] remote={Minecraft189WorldEntityKindState.LIVING
                |Minecraft189WorldEntityKindState.PLAYER};
        assertNull(Minecraft189ProjectedPlayerFrame.collect(13,camera(),
                objects,remote,new UUID[]{a}));
        assertNull(Minecraft189ProjectedPlayerFrame.collect(12,camera(),
                objects,new int[0],new UUID[]{a}));
        assertNull(Minecraft189ProjectedPlayerFrame.collect(12,camera(),
                objects,remote,new UUID[]{b}));
        assertNull(Minecraft189ProjectedPlayerFrame.collect(12,camera(),
                new Object[]{new Entity(a,0),new Entity(a,1)},
                new int[]{remote[0],remote[0]},new UUID[]{a,a}));
        assertNull(Minecraft189ProjectedPlayerFrame.collect(12,camera(),
                objects,new int[]{Minecraft189WorldEntityKindState.PLAYER},
                new UUID[]{a}));
        Minecraft189ProjectedPlayerFrame empty =
                Minecraft189ProjectedPlayerFrame.collect(12,camera(),
                        new Object[0],new int[0],new UUID[0]);
        assertNotNull(empty);
        assertEquals(0,empty.remoteCount());
        assertTrue(empty.visible().isEmpty());
    }
}
