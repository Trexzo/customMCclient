package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V35 CI activation readiness is observational; no original controller,
 * GUI instance, cursor, packets or fake world state is manipulated here.
 */
final class Minecraft189ScaffoldGuiClearStabilityTest {
    @Test void requiresSixtyConsecutiveOriginalGuiClearTicks() {
        final Minecraft189ScaffoldGuiClearStability gate =
                new Minecraft189ScaffoldGuiClearStability();
        Object minecraft=new Object(), player=new Object(), world=new Object();
        for (int i=1; i<Minecraft189ScaffoldGuiClearStability.MIN_STABLE_TICKS; i++) {
            assertFalse(gate.observe(minecraft,player,world,true),
                    "partial stable observations may not arm");
            assertEquals(i,gate.witnessedClearTicks());
        }
        assertTrue(gate.observe(minecraft,player,world,true));
        assertTrue(gate.observe(minecraft,player,world,true),
                "same continuous World and clear GUI stays stable");
        assertEquals(Minecraft189ScaffoldGuiClearStability.MIN_STABLE_TICKS,
                gate.witnessedClearTicks(),"count must saturate");
    }

    @Test void originalMenuOpeningResetsTheEntireReadinessPeriod() {
        final Minecraft189ScaffoldGuiClearStability gate =
                new Minecraft189ScaffoldGuiClearStability();
        Object m=new Object(), p=new Object(), w=new Object();
        for (int i=0;i<60;i++) gate.observe(m,p,w,true);
        assertEquals(60,gate.witnessedClearTicks());
        assertFalse(gate.observe(m,p,w,false),"OPEN or unavailable GUI must reset");
        assertEquals(0,gate.witnessedClearTicks());
        assertFalse(gate.observe(m,p,w,true));
        for (int i=0;i<58;i++) assertFalse(gate.observe(m,p,w,true));
        assertTrue(gate.observe(m,p,w,true));
    }

    @Test void staleWorldPlayerOrMinecraftNeverInheritClearTicks() {
        final Minecraft189ScaffoldGuiClearStability gate =
                new Minecraft189ScaffoldGuiClearStability();
        Object m=new Object(), p=new Object(), w=new Object();
        for (int i=0;i<59;i++) gate.observe(m,p,w,true);
        assertFalse(gate.observe(m,p,new Object(),true));
        assertEquals(1,gate.witnessedClearTicks());
        assertFalse(gate.observe(m,new Object(),w,true));
        assertEquals(1,gate.witnessedClearTicks());
        assertFalse(gate.observe(new Object(),p,w,true));
        assertEquals(1,gate.witnessedClearTicks());
        assertFalse(gate.observe(m,p,w,true));
        assertEquals(1,gate.witnessedClearTicks());
    }

    @Test void absentOriginalEvidenceOrExplicitShutdownDropsState() {
        final Minecraft189ScaffoldGuiClearStability gate =
                new Minecraft189ScaffoldGuiClearStability();
        Object m=new Object(), p=new Object(), w=new Object();
        for (int i=0;i<59;i++) gate.observe(m,p,w,true);
        assertFalse(gate.observe(m,p,null,true));
        assertEquals(0,gate.witnessedClearTicks());
        assertFalse(gate.observe(null,p,w,true));
        assertFalse(gate.observe(m,null,w,true));
        assertFalse(gate.observe(m,p,w,true));
        gate.reset();
        assertEquals(0,gate.witnessedClearTicks());
        assertFalse(gate.observe(m,p,w,true));
    }
}
