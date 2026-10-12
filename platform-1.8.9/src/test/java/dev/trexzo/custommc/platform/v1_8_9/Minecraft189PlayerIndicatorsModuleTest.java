package dev.trexzo.custommc.platform.v1_8_9;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189PlayerIndicatorsModuleTest {
    private static final int REMOTE =
            Minecraft189WorldEntityKindState.LIVING
                    | Minecraft189WorldEntityKindState.PLAYER;
    private static final int LOCAL = REMOTE
            | Minecraft189WorldEntityKindState.LOCAL_PLAYER;

    @Test void yawMatchesCertifiedPlayerRadarOrientation() {
        float[] ahead = Minecraft189PlayerIndicatorsModule.bearing(0, 5, 0, 100);
        assertEquals(0.0F, ahead[0], 0.001F);
        assertEquals(-100.0F, ahead[1], 0.001F);
        float[] right = Minecraft189PlayerIndicatorsModule.bearing(0, 5, 90, 100);
        assertEquals(100.0F, right[0], 0.001F);
        assertEquals(0.0F, right[1], 0.001F);
        assertNull(Minecraft189PlayerIndicatorsModule.bearing(0, 0, 0, 100));
        assertNull(Minecraft189PlayerIndicatorsModule.bearing(Double.NaN, 1, 0, 100));
        assertNull(Minecraft189PlayerIndicatorsModule.bearing(1, 1, Float.NaN, 100));
    }

    @Test void acceptsOnlyLiveSourceMappedRemotePlayersWithinThreeDimensionalRange() {
        Minecraft189PlayerPositionState me = new Minecraft189PlayerPositionState();
        Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        assertTrue(Minecraft189PlayerIndicatorsModule.select(me.snapshot(),
                positions.snapshot(), kinds.snapshot(), rotation.snapshot(), 16, 80).isEmpty());
        me.update(0, 0, 0);
        rotation.update(0);
        positions.update(new double[]{
                0,0,0, 0,0,5, 1,0,1, 0,0,7, 0,20,0, 0,0,5
        });
        kinds.update(new int[]{LOCAL, REMOTE,
                Minecraft189WorldEntityKindState.LIVING,
                REMOTE, REMOTE, LOCAL});
        List<Minecraft189PlayerIndicatorsModule.Marker> out =
                Minecraft189PlayerIndicatorsModule.select(me.snapshot(),
                    positions.snapshot(), kinds.snapshot(),
                    rotation.snapshot(), 7, 80);
        assertEquals(2, out.size());
        assertEquals(1, out.get(0).index);
        assertEquals(3, out.get(1).index);
        assertEquals(5.0F, out.get(0).distance, 0.001F);
        assertThrows(UnsupportedOperationException.class, () -> out.clear());
        kinds.update(new int[]{LOCAL});
        assertTrue(Minecraft189PlayerIndicatorsModule.select(me.snapshot(),
                positions.snapshot(), kinds.snapshot(), rotation.snapshot(), 7, 80).isEmpty());
        kinds.clear();
        assertTrue(Minecraft189PlayerIndicatorsModule.select(me.snapshot(),
                positions.snapshot(), kinds.snapshot(), rotation.snapshot(), 7, 80).isEmpty());
        assertTrue(Minecraft189PlayerIndicatorsModule.select(me.snapshot(),
                positions.snapshot(), kinds.snapshot(), rotation.snapshot(), 3, 80).isEmpty());
    }
}
