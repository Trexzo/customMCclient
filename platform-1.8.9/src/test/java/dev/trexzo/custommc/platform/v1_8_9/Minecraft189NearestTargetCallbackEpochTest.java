package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Original mapped World player-position candidate selection source adapter.
 * Candidate callbacks may synchronously replace source ownership.
 * These are offline JUnit tests, not a substitute for actual Mojang CI.
 */
final class Minecraft189NearestTargetCallbackEpochTest {
    private static final class Fixture {
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        Fixture() {
            local.update(0D,64D,0D);
            positions.update(new double[]{
                    0D,64D,0D,
                    2D,64D,0D,
                    7D,64D,0D,
                    9D,64D,0D
            });
            kinds.update(new int[]{
                    Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                    Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LIVING,
                    Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LIVING,
                    Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LIVING
            });
        }
        void scan() {
            target.update(local.snapshot(),positions.snapshot(),kinds.snapshot());
        }
        void scan(final Minecraft189NearestPlayerTargetState.CandidateFilter filter) {
            target.update(local.snapshot(),positions.snapshot(),kinds.snapshot(),
                    0D,20D,filter);
        }
        void score(final Minecraft189NearestPlayerTargetState.CandidateScore score) {
            target.update(local.snapshot(),positions.snapshot(),kinds.snapshot(),
                    0D,20D,null,score);
        }
        void select(final int index) {
            scan(candidate -> candidate.entityIndex()==index);
        }
        int selected() {return target.snapshot().entityIndex();}
    }

    @Test void stableOriginalNearestPlayerRemainsTheSameIndex() {
        Fixture f=new Fixture();
        f.scan();
        assertEquals(1,f.selected());
        assertEquals(4D,f.target.snapshot().distanceSquared(),0D);
    }
    @Test void ordinaryFilterStillSelectsFurtherSourcePlayer() {
        Fixture f=new Fixture();
        f.select(2);
        assertEquals(2,f.selected());
        assertEquals(49D,f.target.snapshot().distanceSquared(),0D);
    }
    @Test void originalExternalFilterClearCancelsOlderCandidateResult() {
        Fixture f=new Fixture();f.scan();
        f.scan(candidate->{f.target.clear();return true;});
        assertFalse(f.target.snapshot().available());
        assertFalse(f.target.snapshot().found());
        assertEquals(-1,f.selected());
    }
    @Test void actualCallbackNewWorldSelectionBeatsOuterNearestPlayer() {
        Fixture f=new Fixture();
        f.scan(candidate->{f.select(3);return true;});
        assertTrue(f.target.snapshot().available());
        assertEquals(3,f.selected());
        assertEquals(81D,f.target.snapshot().distanceSquared(),0D);
    }
    @Test void evenFilterRejectionCannotReplaceNestedNewSourceChoice() {
        Fixture f=new Fixture();
        f.scan(candidate->{f.select(2);return false;});
        assertEquals(2,f.selected());
    }
    @Test void nestedCandidateFilterMaySelectNoCurrentPlayerWithoutOldFallback() {
        Fixture f=new Fixture();
        f.scan(candidate->{f.scan(inner->false);return true;});
        assertTrue(f.target.snapshot().available());
        assertFalse(f.target.snapshot().found());
        assertEquals(-1,f.selected());
    }
    @Test void nestedCandidateScoreClearRevokesAllOldTargetAuthority() {
        Fixture f=new Fixture();f.scan();
        f.score(candidate->{f.target.clear();return 0D;});
        assertFalse(f.target.snapshot().available());
        assertEquals(-1,f.selected());
    }
    @Test void nestedCandidateScoreNewOriginalSelectionKeepsNewUUIDIndex() {
        Fixture f=new Fixture();
        f.score(candidate->{f.select(3);return 0D;});
        assertTrue(f.target.snapshot().found());
        assertEquals(3,f.selected());
    }
    @Test void scoreReturningInvalidValueAfterNewSourceCannotOverrideNewSelection() {
        Fixture f=new Fixture();
        f.score(candidate->{f.select(2);return Double.NaN;});
        assertEquals(2,f.selected());
    }
    @Test void callbackNestedInvalidWorldRevokesOldTargetInsteadOfRevivingIt() {
        Fixture f=new Fixture();f.scan();
        f.scan(candidate->{
            f.target.update(f.local.snapshot(),f.positions.snapshot(),
                    f.kinds.snapshot(),10D,2D);
            return true;
        });
        assertFalse(f.target.snapshot().available());
        assertEquals(-1,f.selected());
    }
    @Test void realLatestSourceCanBeSelectedNormallyAfterPreviousRevocation() {
        Fixture f=new Fixture();
        f.scan(candidate->{f.target.clear();return true;});
        assertFalse(f.target.snapshot().available());
        f.select(1);
        assertTrue(f.target.snapshot().available());
        assertEquals(1,f.selected());
    }
    @Test void unchangedScoringPolicyStillPicksMinimalNonnegativeScore() {
        Fixture f=new Fixture();
        f.score(candidate->candidate.entityIndex()==2?0D:10D);
        assertEquals(2,f.selected());
        assertEquals(49D,f.target.snapshot().distanceSquared(),0D);
    }
    @Test void nestedRealUpdateDoesNotCauseLateOuterCandidateLoop() {
        Fixture f=new Fixture();
        final int[] callbacks={0};
        f.scan(candidate->{
            callbacks[0]++;
            if(callbacks[0]==1)f.select(3);
            return true;
        });
        assertEquals(1,callbacks[0]);
        assertEquals(3,f.selected());
    }
    @Test void freshStableRealFilterAfterReentrantScoreHasNormalTieBreaking() {
        Fixture f=new Fixture();
        f.score(candidate->{f.select(2);return 0D;});
        f.score(candidate->candidate.entityIndex()==1?1D:2D);
        assertEquals(1,f.selected());
    }
}
