package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.render.RenderFrame;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189PlayerBoxEspModuleTest {
    private static final double[] IDENTITY={
            1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1
    };
    @Test void rejectsStaleWrongPartialTicksWrongViewportAndMissingNativeOrigin() {
        final Minecraft189WorldCameraFrameState state =
                new Minecraft189WorldCameraFrameState();
        state.accept(7,0,0.5F,IDENTITY,IDENTITY,new int[]{0,0,800,600});
        assertFalse(Minecraft189PlayerBoxEspModule.admissible(
                new RenderFrame(7,0.5F), state.snapshot(),
                null,800,600,1));
        state.captureOrigin(7,0,0.5F,new Minecraft189PlayerPositionAccess(){
            public double customMcPositionX(){return 0;}
            public double customMcPositionY(){return 0;}
            public double customMcPositionZ(){return 0;}
            public double customMcLastTickPositionX(){return 0;}
            public double customMcLastTickPositionY(){return 0;}
            public double customMcLastTickPositionZ(){return 0;}
        });
        final Minecraft189WorldCameraFrameState.Snapshot camera=state.snapshot();
        state.captureNativePlayerBoxes(7,0,0.5F,new Object[0],
                new int[0],new UUID[0]);
        final Minecraft189ProjectedPlayerFrame p=state.projectedPlayers();
        assertNotNull(p);
        assertTrue(Minecraft189PlayerBoxEspModule.admissible(
                new RenderFrame(7,0.5F),camera,p,800,600,1));
        assertFalse(Minecraft189PlayerBoxEspModule.admissible(
                new RenderFrame(8,0.5F),camera,p,800,600,1));
        assertFalse(Minecraft189PlayerBoxEspModule.admissible(
                new RenderFrame(7,0.6F),camera,p,800,600,1));
        assertFalse(Minecraft189PlayerBoxEspModule.admissible(
                new RenderFrame(7,0.5F),camera,p,799,600,1));
        assertFalse(Minecraft189PlayerBoxEspModule.admissible(
                new RenderFrame(7,0.5F),camera,p,800,600,0));
        state.clear();
        assertNull(state.projectedPlayers());
    }
}
