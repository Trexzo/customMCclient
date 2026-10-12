package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;

/**
 * Opt-in test-only witness for real vanilla Minecraft 1.8.9 disconnect
 * and fresh same-username rejoin. Never supplies production target choice.
 */
final class Minecraft189RejoinTargetAcceptance {
    private static UUID originalUuid, survivorUuid;
    private static int phase, disabledTicks, armedTicks, aloneTicks, rejoinedTicks;
    private static boolean survivorClicked;
    private static int firstOriginalIndex = -1;

    private Minecraft189RejoinTargetAcceptance() {}

    static void reset() {
        originalUuid = survivorUuid = null;
        phase = disabledTicks = armedTicks = aloneTicks = rejoinedTicks = 0;
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
        if (other!=0 || b<0) return null;
        double da=Double.POSITIVE_INFINITY,db;
        if (a>=0) {
            double dx=positions.x(a)-local.x(),dy=positions.y(a)-local.y(),
                    dz=positions.z(a)-local.z();
            da=Math.sqrt(dx*dx+dy*dy+dz*dz);
        }
        double bx=positions.x(b)-local.x(),by=positions.y(b)-local.y(),
                bz=positions.z(b)-local.z();
        db=Math.sqrt(bx*bx+by*by+bz*bz);
        if (!Double.isFinite(db) || (a>=0 && !Double.isFinite(da))) return null;
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
        } else if (phase>=3) {
            // The fresh A entity can reuse any index. Its actual offline UUID
            // must be recovered from the CURRENT native list every tick.
            if (e!=null && e.aCloserButBSelected()
                    && survivorUuid.equals(aura.lockedTargetUuid())) {
                if (++rejoinedTicks>=20 && phase==3) {
                    phase=4;
                    System.out.println("CUSTOMMC_189_REJOIN_ORIGINAL_UUID_REVALIDATED_PASS=YES");
                    System.out.println("CUSTOMMC_189_REJOIN_STICKY_B_RETAINED_PASS=20");
                    System.out.println("CUSTOMMC_189_REJOIN_OLD_A_INDEX="
                            + firstOriginalIndex + " NEW_A_INDEX=" + e.a);
                    System.out.flush();
                }
            } else rejoinedTicks=0;
        }
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
            if (!survivorClicked) {
                survivorClicked=true;
                System.out.println("CUSTOMMC_189_REJOIN_B_NATIVE_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else if (phase>=3 && survivorUuid.equals(id)
                && index==e.b && e.bInRange()) {
            // After A rejoins, B remains true locked UUID even if A is closer.
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
        boolean bInRange() { return db>=1.0D && db<=3.5D; }
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
