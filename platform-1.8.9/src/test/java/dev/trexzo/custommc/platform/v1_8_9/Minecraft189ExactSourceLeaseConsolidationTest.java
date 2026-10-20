package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Byte-for-byte ownership parity for four original getter source stacks.
 * Core source regression only: the pinned Mojang game CI is independent.
 */
final class Minecraft189ExactSourceLeaseConsolidationTest {
    private static final class Movement implements Minecraft189PlayerMovementStateAccess {
        @Override public boolean customMcOnGround(){return true;}
        @Override public boolean customMcSneaking(){return false;}
        @Override public boolean customMcSprinting(){return false;}
    }
    private static final class Position implements Minecraft189PlayerPositionAccess {
        @Override public double customMcPositionX(){return 1D;}
        @Override public double customMcPositionY(){return 2D;}
        @Override public double customMcPositionZ(){return 3D;}
    }
    private static final class World implements Minecraft189WorldEntityPositionsAccess {
        @Override public double[] customMcLoadedEntityPositions(){return new double[]{1D,2D,3D};}
    }
    private static final class EqualWorld {
        @Override public boolean equals(final Object o){return o instanceof EqualWorld;}
        @Override public int hashCode(){return 0;}
    }
    @Test void movementFirstRealSourceAdmitted(){final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();final Movement a=new Movement();assertTrue(l.enter(a));assertTrue(l.stillOwned());l.exit();assertFalse(l.stillOwned());}
    @Test void movementSameNativeSourceCannotSpendDuplicateTick(){final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();final Movement a=new Movement();assertTrue(l.enter(a));assertFalse(l.enter(a));assertTrue(l.stillOwned());l.exit();}
    @Test void movementDifferentOriginalReferenceRevokesIrreversibly(){final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();final Movement a=new Movement(),b=new Movement();assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.stillOwned());assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
    @Test void movementNestedNullSourceRevokesCurrentOriginal(){final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();assertTrue(l.enter(new Movement()));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
    @Test void movementInitialNullCannotBorrowLaterNativeSource(){final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();assertTrue(l.enter(null));assertTrue(l.stillOwned());assertFalse(l.enter(new Movement()));assertFalse(l.stillOwned());l.exit();}
    @Test void movementNextRealTickMayAcquireAfterRevokedFinally(){final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();final Movement a=new Movement(),b=new Movement();assertTrue(l.enter(a));assertFalse(l.enter(b));try{throw new IllegalStateException("genuine getter");}catch(IllegalStateException e){assertEquals("genuine getter",e.getMessage());}finally{l.exit();}assertTrue(l.enter(b));assertTrue(l.stillOwned());l.exit();}
    @Test void positionFirstRealSourceAdmitted(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();final Position a=new Position();assertTrue(l.enter(a));assertTrue(l.stillOwned());l.exit();assertFalse(l.stillOwned());}
    @Test void positionSameNativeSourceCannotSpendDuplicateTick(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();final Position a=new Position();assertTrue(l.enter(a));assertFalse(l.enter(a));assertTrue(l.stillOwned());l.exit();}
    @Test void positionDifferentOriginalReferenceRevokesIrreversibly(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();final Position a=new Position(),b=new Position();assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.stillOwned());assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
    @Test void positionNestedNullSourceRevokesCurrentOriginal(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();assertTrue(l.enter(new Position()));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
    @Test void positionInitialNullCannotBorrowLaterNativeSource(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();assertTrue(l.enter(null));assertTrue(l.stillOwned());assertFalse(l.enter(new Position()));assertFalse(l.stillOwned());l.exit();}
    @Test void positionNextRealTickMayAcquireAfterRevokedFinally(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();final Position a=new Position(),b=new Position();assertTrue(l.enter(a));assertFalse(l.enter(b));try{throw new IllegalStateException("genuine getter");}catch(IllegalStateException e){assertEquals("genuine getter",e.getMessage());}finally{l.exit();}assertTrue(l.enter(b));assertTrue(l.stillOwned());l.exit();}
    @Test void worldPositionsFirstRealSourceAdmitted(){final Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();final World a=new World();assertTrue(l.enter(a));assertTrue(l.stillOwned());l.exit();assertFalse(l.stillOwned());}
    @Test void worldPositionsSameNativeSourceCannotSpendDuplicateTick(){final Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();final World a=new World();assertTrue(l.enter(a));assertFalse(l.enter(a));assertTrue(l.stillOwned());l.exit();}
    @Test void worldPositionsDifferentOriginalReferenceRevokesIrreversibly(){final Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();final World a=new World(),b=new World();assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.stillOwned());assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
    @Test void worldPositionsNestedNullSourceRevokesCurrentOriginal(){final Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();assertTrue(l.enter(new World()));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
    @Test void worldPositionsInitialNullCannotBorrowLaterNativeSource(){final Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();assertTrue(l.enter(null));assertTrue(l.stillOwned());assertFalse(l.enter(new World()));assertFalse(l.stillOwned());l.exit();}
    @Test void worldPositionsNextRealTickMayAcquireAfterRevokedFinally(){final Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();final World a=new World(),b=new World();assertTrue(l.enter(a));assertFalse(l.enter(b));try{throw new IllegalStateException("genuine getter");}catch(IllegalStateException e){assertEquals("genuine getter",e.getMessage());}finally{l.exit();}assertTrue(l.enter(b));assertTrue(l.stillOwned());l.exit();}
    @Test void worldDownstreamFirstRealSourceAdmitted(){final Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();final World a=new World();assertTrue(l.enter(a));assertTrue(l.stillOwned());l.exit();assertFalse(l.stillOwned());}
    @Test void worldDownstreamSameNativeSourceCannotSpendDuplicateTick(){final Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();final World a=new World();assertTrue(l.enter(a));assertFalse(l.enter(a));assertTrue(l.stillOwned());l.exit();}
    @Test void worldDownstreamDifferentOriginalReferenceRevokesIrreversibly(){final Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();final World a=new World(),b=new World();assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.stillOwned());assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
    @Test void worldDownstreamNestedNullSourceRevokesCurrentOriginal(){final Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();assertTrue(l.enter(new World()));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
    @Test void worldDownstreamInitialNullCannotBorrowLaterNativeSource(){final Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();assertTrue(l.enter(null));assertTrue(l.stillOwned());assertFalse(l.enter(new World()));assertFalse(l.stillOwned());l.exit();}
    @Test void worldDownstreamNextRealTickMayAcquireAfterRevokedFinally(){final Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();final World a=new World(),b=new World();assertTrue(l.enter(a));assertFalse(l.enter(b));try{throw new IllegalStateException("genuine getter");}catch(IllegalStateException e){assertEquals("genuine getter",e.getMessage());}finally{l.exit();}assertTrue(l.enter(b));assertTrue(l.stillOwned());l.exit();}
    @Test void exactEqualityNotValueBased(){Minecraft189ExactSourceLease<EqualWorld> l=new Minecraft189ExactSourceLease<EqualWorld>();EqualWorld a=new EqualWorld(),b=new EqualWorld();assertEquals(a,b);assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.stillOwned());l.exit();}
    @Test void twoIndependentOriginalGetterStacksCannotRevokeEachOther(){Minecraft189PositionHostSourceLease a=new Minecraft189PositionHostSourceLease();Minecraft189MovementSnapshotSourceLease b=new Minecraft189MovementSnapshotSourceLease();Position p=new Position();Movement m=new Movement();assertTrue(a.enter(p));assertTrue(b.enter(m));assertFalse(a.enter(new Position()));assertFalse(a.stillOwned());assertTrue(b.stillOwned());b.exit();a.exit();}
}
