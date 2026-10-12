package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;

/**
 * Opt-in test-only witness for real vanilla Minecraft 1.8.9 disconnect
 * and fresh same-username rejoin. Never supplies production target choice.
 */
final class Minecraft189RejoinTargetAcceptance {
    private static UUID originalUuid, survivorUuid;
    private static int phase, disabledTicks, armedTicks, aloneTicks, rejoinedTicks, releasedTicks;
    private static boolean reacquiredClicked;
    private static boolean survivorClicked;
    private static int firstOriginalIndex = -1;

    private Minecraft189RejoinTargetAcceptance() {}

    static void reset() {
        originalUuid = survivorUuid = null;
        phase = disabledTicks = armedTicks = aloneTicks = rejoinedTicks = releasedTicks = 0;
        reacquiredClicked = false;
        survivorClicked = false;
        firstOriginalIndex = -1;
    }

    private static Evidence read(Minecraft189HostRuntime host) {
        if (originalUuid == null) {
            originalUuid = UUID.fromString(System.getProperty(
                    "custommc.acceptance.targetAUuid"));
            survivorUuid = UUID.fromString(System.getProperty(
                    "custommc.acceptance.targetBUuid"));
            if (originalUuid.equals(survivorUuid))
                throw new IllegalStateException("duplicate real player UUID identities");
        }
        Minecraft189WorldEntityUuidState.Snapshot ids =
                host.worldEntityUuidState().snapshot();
        Minecraft189WorldEntityKindState.Snapshot kinds =
                host.worldEntityKindState().snapshot();
        Minecraft189WorldEntityCombatState.Snapshot combat =
                host.worldEntityCombatState().snapshot();
        Minecraft189WorldEntityPositionState.Snapshot positions =
                host.worldEntityPositionState().snapshot();
        Minecraft189PlayerPositionState.Snapshot local =
                host.playerPositionState().snapshot();
        Minecraft189NearestPlayerTargetState.Snapshot nearest =
                host.nearestPlayerTargetState().snapshot();
        Minecraft189TargetRotationState.Snapshot rotation =
                host.targetRotationState().snapshot();
        if (!ids.available() || !kinds.available() || !combat.available()
                || !positions.available() || !local.available()
                || ids.entityCount() != kinds.entityCount()
                || ids.entityCount() != combat.entityCount()
                || ids.entityCount() != positions.entityCount()) return null;
        int a=-1,b=-1,other=0;
        for (int i=0; i<ids.entityCount(); i++) {
            if (!kinds.player(i) || kinds.localPlayer(i)) continue;
            if (!kinds.living(i) || !combat.alive(i)) return null;
            UUID id=ids.at(i);
            if (originalUuid.equals(id)) a=i;
            else if (survivorUuid.equals(id)) b=i;
            else other++;
        }
        if (other!=0 || (b<0 && !(phase>=4 && a>=0))) return null;
        double da=Double.POSITIVE_INFINITY,db;
        if (a>=0) {
            double dx=positions.x(a)-local.x(),dy=positions.y(a)-local.y(),
                    dz=positions.z(a)-local.z();
            da=Math.sqrt(dx*dx+dy*dy+dz*dz);
        }
        if (b>=0) {
            double bx=positions.x(b)-local.x(),by=positions.y(b)-local.y(),
                    bz=positions.z(b)-local.z();
            db=Math.sqrt(bx*bx+by*by+bz*bz);
        } else db=Double.POSITIVE_INFINITY;
        if ((b>=0 && !Double.isFinite(db)) || (a>=0 && !Double.isFinite(da)))
            return null;
        return new Evidence(ids,kinds,combat,nearest,rotation,a,b,da,db);
    }

    static void observe(Minecraft189HostRuntime host) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraRejoin"))
            return;
        Evidence e=read(host);
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        if (phase==0) {
            if (aura.active()) throw new IllegalStateException("rejoin Aura preenabled");
            disabledTicks=e!=null && e.aPreferred()
                    ? disabledTicks+1 : 0;
            if (disabledTicks>=20) {
                firstOriginalIndex=e.a;
                host.featureCatalog().enableKillAuraForOfficialRejoinAcceptance();
                phase=1;
                System.out.println("CUSTOMMC_189_REJOIN_TWO_NATIVE_UUID_PLAYERS_READY=20");
                System.out.flush();
            }
            return;
        }
        if (!aura.active() || !aura.lockTargetSetting().get())
            throw new IllegalStateException("rejoin lost actual Kill Aura lock setting");
        if (phase==1) {
            if (e!=null && e.aPreferred()
                    && originalUuid.equals(aura.lockedTargetUuid())) {
                if (++armedTicks>=10) {
                    phase=2;
                    System.out.println("CUSTOMMC_189_REJOIN_ORIGINAL_UUID_LOCK_ARMED=10");
                    System.out.flush();
                }
            } else armedTicks=0;
        } else if (phase==2) {
            // A must ACTUALLY disappear from native loadedEntityList.
            // Only the living B UUID may remain, not a stale original index.
            if (e!=null && e.a==-1 && e.bOnlySelected()
                    && survivorUuid.equals(aura.lockedTargetUuid())) {
                if (++aloneTicks>=15) {
                    phase=3;
                    System.out.println("CUSTOMMC_189_REJOIN_OLD_A_UNLOADED_B_LOCKED_PASS=15");
                    System.out.flush();
                }
            } else aloneTicks=0;
        } else if (phase==3) {
            // Original A has a new real EntityPlayer instance and source-native
            // list index. Re-acquire by UUID, never a stored index.
            if (e!=null && e.aCloserButBSelected()
                    && survivorUuid.equals(aura.lockedTargetUuid())) {
                if (++rejoinedTicks>=20) {
                    phase=4;
                    System.out.println("CUSTOMMC_189_REJOIN_ORIGINAL_UUID_REVALIDATED_PASS=YES");
                    System.out.println("CUSTOMMC_189_REJOIN_STICKY_B_RETAINED_PASS=20");
                    System.out.println("CUSTOMMC_189_REJOIN_OLD_A_INDEX="
                            + firstOriginalIndex + " NEW_A_INDEX=" + e.a);
                    if (Boolean.getBoolean(
                            "custommc.acceptance.requireRejoinIndexReorder")) {
                        // M415/M416 did not certify index reordering. This
                        // optional M417 gate must witness a DIFFERENT real
                        // current index for the SAME source-native UUID.
                        if (firstOriginalIndex < 0 || e.a < 0
                                || firstOriginalIndex == e.a)
                            throw new IllegalStateException(
                                    "real rejoin did not actually reorder A index"
                                    + " original=" + firstOriginalIndex
                                    + " current=" + e.a);
                        if (!originalUuid.equals(e.ids.at(e.a))
                                || !e.kinds.player(e.a)
                                || e.kinds.localPlayer(e.a))
                            throw new IllegalStateException(
                                    "reordered entity index has wrong native UUID");
                        System.out.println(
                            "CUSTOMMC_OFFICIAL_189_REJOIN_ACTUAL_INDEX_REORDER_PASS=YES");
                    }
                    if (Boolean.getBoolean(
                            "custommc.acceptance.requireRejoinWrongTargetVeto"))
                        proveWrongTargetProposalRejected(host, e);
                    System.out.flush();
                }
            } else rejoinedTicks=0;
        } else if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraRejoinRelease")
                && phase==4) {
            // Real B player MUST disappear, not merely move away. Source-native
            // lock/selection then falls back to the fresh living A UUID.
            if (e!=null && e.b==-1 && e.aOnlySelected()
                    && originalUuid.equals(aura.lockedTargetUuid())) {
                if (++releasedTicks>=15) {
                    phase=5;
                    System.out.println(
                            "CUSTOMMC_189_REJOIN_SURVIVOR_B_UNLOADED_A_RELOCKED_PASS=15");
                    System.out.flush();
                }
            } else releasedTicks=0;
        }
    }

    /**
     * M418 test-only negative proposal. A is genuinely in reach and closer,
     * but the real current selected and locked UUID is B. Send NO attack or
     * packet: exercise the same fail-closed original click ownership gate
     * against A's actual current native index and require explicit rejection.
     */
    private static void proveWrongTargetProposalRejected(
            Minecraft189HostRuntime host, Evidence e) {
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        if (!e.aCloserButBSelected() || e.a==e.b || !e.aInRange()
                || !originalUuid.equals(e.ids.at(e.a))
                || !survivorUuid.equals(e.ids.at(e.b))
                || !survivorUuid.equals(aura.lockedTargetUuid()))
            throw new IllegalStateException(
                    "wrong-target veto fixture lacks real native A/B ownership");
        try {
            verifyOwnedClick(host, true, e.a);
        } catch (IllegalStateException rejected) {
            String message=rejected.getMessage();
            if (message==null
                    || !message.startsWith("rejoin click invalid source-native owner"))
                throw rejected;
            System.out.println(
                    "CUSTOMMC_189_REJOIN_WRONG_IN_RANGE_UUID_PROPOSAL_VETO_PASS=YES");
            return;
        }
        throw new IllegalStateException(
                "real wrong UUID proposal was NOT vetoed by original click gate");
    }

    static void verifyOwnedClick(Minecraft189HostRuntime host,
            boolean playerHit, int index) {
        Evidence e=read(host);
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        UUID id=(e!=null && index>=0 && index<e.ids.entityCount())
                ? e.ids.at(index) : null;
        if (!aura.active() || !aura.lockTargetSetting().get()
                || !playerHit || e==null || id==null
                || !e.kinds.player(index) || e.kinds.localPlayer(index)
                || !e.kinds.living(index) || !e.combat.alive(index)
                || !e.selected(index) || !Double.isFinite(e.rotation.distance())
                || e.rotation.distance()>3.5D
                || !id.equals(aura.lockedTargetUuid())) {
            throw new IllegalStateException(
                    "rejoin click invalid source-native owner phase="+phase
                    +" nativeUuid="+id+" locked="+aura.lockedTargetUuid()
                    +" index="+index);
        }
        if (phase<=2 && originalUuid.equals(id) && index==e.a
                && e.a>=0 && e.da<=3.5D) {
            // Original A is creative, never counted as genuine server damage.
        } else if (phase>=2 && survivorUuid.equals(id) && index==e.b
                && e.a==-1 && e.bInRange()) {
            // Selection-only creative B clicks never prove server damage.
            // Record a positive-owned click only once the required
            // 15-callback survivor-lock gate reaches phase 3.
            if (phase>=3 && !survivorClicked) {
                survivorClicked=true;
                System.out.println("CUSTOMMC_189_REJOIN_B_NATIVE_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else if (phase>=3 && phase<5 && survivorUuid.equals(id)
                && index==e.b && e.bInRange()) {
            // Fresh A rejoined; original B remains the true owner until
            // vanilla B actually leaves, without reusing any stale index.
        } else if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraRejoinRelease")
                && phase==4 && originalUuid.equals(id)
                && index==e.a && e.b==-1 && e.aOnlySelected()) {
            // A is still genuine CREATIVE while the actual B-disconnected
            // 15-callback UUID lock proof runs. This callback never certifies
            // positive damage, and cannot skip the full phase gate.
        } else if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraRejoinRelease")
                && phase>=5 && originalUuid.equals(id)
                && index==e.a && e.b==-1 && e.aInRange()) {
            if (!reacquiredClicked) {
                reacquiredClicked=true;
                phase=6;
                System.out.println(
                        "CUSTOMMC_189_REJOIN_FRESH_A_NATIVE_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else {
            throw new IllegalStateException("rejoin stale or wrong UUID attack phase="
                    +phase+" actual="+id);
        }
    }

    private static final class Evidence {
        final Minecraft189WorldEntityUuidState.Snapshot ids;
        final Minecraft189WorldEntityKindState.Snapshot kinds;
        final Minecraft189WorldEntityCombatState.Snapshot combat;
        final Minecraft189NearestPlayerTargetState.Snapshot nearest;
        final Minecraft189TargetRotationState.Snapshot rotation;
        final int a,b;
        final double da,db;
        Evidence(Minecraft189WorldEntityUuidState.Snapshot ids,
                Minecraft189WorldEntityKindState.Snapshot kinds,
                Minecraft189WorldEntityCombatState.Snapshot combat,
                Minecraft189NearestPlayerTargetState.Snapshot nearest,
                Minecraft189TargetRotationState.Snapshot rotation,
                int a,int b,double da,double db) {
            this.ids=ids;this.kinds=kinds;this.combat=combat;
            this.nearest=nearest;this.rotation=rotation;
            this.a=a;this.b=b;this.da=da;this.db=db;
        }
        boolean selected(int i) {
            return i>=0 && nearest.found() && nearest.entityIndex()==i
                    && rotation.available() && rotation.entityIndex()==i;
        }
        boolean bInRange() { return b>=0 && db>=1.0D && db<=3.5D; }
        boolean aInRange() { return a>=0 && da>=1.0D && da<=3.5D; }
        boolean aOnlySelected() { return b<0 && aInRange() && selected(a); }
        boolean aPreferred() {
            return a>=0 && da>=1.0D && da<=3.25D && bInRange()
                    && da+0.12D<db && selected(a);
        }
        boolean bOnlySelected() { return a<0 && bInRange() && selected(b); }
        boolean aCloserButBSelected() {
            return a>=0 && da>=1.0D && da<=3.25D && bInRange()
                    && da+0.12D<db && selected(b);
        }
    }
}
