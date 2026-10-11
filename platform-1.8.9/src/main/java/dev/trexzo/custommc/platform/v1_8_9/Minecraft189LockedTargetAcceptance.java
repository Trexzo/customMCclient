package dev.trexzo.custommc.platform.v1_8_9;
import java.util.UUID;

/** Real Mojang localhost CI only; observes production sticky UUID arbitration. */
final class Minecraft189LockedTargetAcceptance {
    private static UUID aUuid, bUuid;
    private static int phase, initialTicks, heldTicks;
    private static boolean aHit, bHit;
    private Minecraft189LockedTargetAcceptance() {}
    static void reset() {
        aUuid = bUuid = null;
        phase = initialTicks = heldTicks = 0;
        aHit = bHit = false;
    }

    private static Evidence read(Minecraft189HostRuntime host) {
        if (aUuid == null) {
            aUuid = UUID.fromString(System.getProperty("custommc.acceptance.targetAUuid"));
            bUuid = UUID.fromString(System.getProperty("custommc.acceptance.targetBUuid"));
            if (aUuid.equals(bUuid)) throw new IllegalStateException("duplicate UUIDs");
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
        Minecraft189NearestPlayerTargetState.Snapshot near =
                host.nearestPlayerTargetState().snapshot();
        Minecraft189TargetRotationState.Snapshot aim =
                host.targetRotationState().snapshot();
        if (!ids.available() || !kinds.available() || !combat.available()
                || !positions.available() || !local.available()
                || ids.entityCount() != kinds.entityCount()
                || ids.entityCount() != combat.entityCount()
                || ids.entityCount() != positions.entityCount()) return null;
        int a=-1, b=-1, remotes=0;
        for (int i=0; i<kinds.entityCount(); i++) {
            if (!kinds.player(i) || kinds.localPlayer(i)) continue;
            remotes++;
            if (!kinds.living(i) || !combat.alive(i)) continue;
            if (aUuid.equals(ids.at(i))) a=i;
            if (bUuid.equals(ids.at(i))) b=i;
        }
        if (remotes != 2 || a<0 || b<0 || a==b) return null;
        double adx=positions.x(a)-local.x(), ady=positions.y(a)-local.y(),
                adz=positions.z(a)-local.z();
        double bdx=positions.x(b)-local.x(), bdy=positions.y(b)-local.y(),
                bdz=positions.z(b)-local.z();
        double da=Math.sqrt(adx*adx+ady*ady+adz*adz);
        double db=Math.sqrt(bdx*bdx+bdy*bdy+bdz*bdz);
        if (!Double.isFinite(da) || !Double.isFinite(db)) return null;
        return new Evidence(ids,kinds,combat,near,aim,a,b,da,db);
    }

    static void observe(Minecraft189HostRuntime host) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraLockedTarget")) return;
        Evidence e=read(host);
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        if (phase==0) {
            if (aura.active()) throw new IllegalStateException("Aura pre-enabled");
            initialTicks=e!=null && e.initialA() ? initialTicks+1 : 0;
            if (initialTicks>=20) {
                host.featureCatalog().enableKillAuraForOfficialLockedTargetAcceptance();
                phase=1;
                System.out.println("CUSTOMMC_189_LOCK_TWO_ELIGIBLE_INITIAL_A_PASS=20");
                System.out.flush();
            }
            return;
        }
        if (!aura.active() || !aura.lockTargetSetting().get())
            throw new IllegalStateException("sticky module disabled");
        if (phase==2) {
            if (e!=null && e.bCloserButASelected()
                    && aUuid.equals(aura.lockedTargetUuid())) {
                if (++heldTicks>=15) {
                    phase=3;
                    System.out.println("CUSTOMMC_189_LOCK_A_RETAINED_WITH_CLOSER_B_PASS=15");
                    System.out.flush();
                }
            } else heldTicks=0;
        }
        if (phase==3 && e!=null && e.aFarBSelected()
                && bUuid.equals(aura.lockedTargetUuid())) {
            phase=4;
            System.out.println("CUSTOMMC_189_LOCK_A_INELIGIBLE_RELEASES_TO_B_PASS=YES");
            System.out.flush();
        }
    }

    static void verifyOwnedClick(Minecraft189HostRuntime host,
            boolean playerHit, int index) {
        Evidence e=read(host);
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        UUID uuid=e==null?null:e.ids.at(index);
        if (!aura.active() || !aura.lockTargetSetting().get()
                || !playerHit || index<0 || e==null
                || index>=e.ids.entityCount()
                || !e.kinds.player(index) || e.kinds.localPlayer(index)
                || !e.kinds.living(index) || !e.combat.alive(index)
                || !e.selected(index)
                || !Double.isFinite(e.aim.distance()) || e.aim.distance()>3.5D
                || !uuid.equals(aura.lockedTargetUuid()))
            throw new IllegalStateException("no live locked owner: phase="+phase
                    +" index="+index+" uuid="+uuid+" locked="+aura.lockedTargetUuid());
        if (phase==1 && aUuid.equals(uuid)) {
            aHit=true; phase=2;
            System.out.println("CUSTOMMC_189_LOCK_A_NATIVE_UUID_OWNED_ATTACK_PASS=YES");
            System.out.flush();
        } else if ((phase==2 || phase==3) && aUuid.equals(uuid)) {
            if (!(e.da<=3.5D && e.da>=1.0D))
                throw new IllegalStateException("stale A attack after range expiry");
        } else if (phase>=3 && bUuid.equals(uuid) && aHit && e.aFarBSelected()) {
            if (phase==3) {
                phase=4;
                System.out.println("CUSTOMMC_189_LOCK_A_INELIGIBLE_RELEASES_TO_B_PASS=YES");
            }
            if (!bHit) {
                bHit=true; phase=5;
                System.out.println("CUSTOMMC_189_LOCK_B_NATIVE_UUID_OWNED_ATTACK_PASS=YES");
            }
            System.out.flush();
        } else if (phase==5 && bUuid.equals(uuid)) {
            // Subsequent B attacks remain gated by real current UUID owner.
        } else throw new IllegalStateException("wrong locked target UUID phase="
                +phase+" uuid="+uuid);
    }

    private static final class Evidence {
        final Minecraft189WorldEntityUuidState.Snapshot ids;
        final Minecraft189WorldEntityKindState.Snapshot kinds;
        final Minecraft189WorldEntityCombatState.Snapshot combat;
        final Minecraft189NearestPlayerTargetState.Snapshot near;
        final Minecraft189TargetRotationState.Snapshot aim;
        final int a,b;
        final double da,db;
        Evidence(Minecraft189WorldEntityUuidState.Snapshot ids,
                Minecraft189WorldEntityKindState.Snapshot kinds,
                Minecraft189WorldEntityCombatState.Snapshot combat,
                Minecraft189NearestPlayerTargetState.Snapshot near,
                Minecraft189TargetRotationState.Snapshot aim,
                int a,int b,double da,double db) {
            this.ids=ids;this.kinds=kinds;this.combat=combat;
            this.near=near;this.aim=aim;this.a=a;this.b=b;
            this.da=da;this.db=db;
        }
        boolean selected(int i) {
            return near.found() && near.entityIndex()==i && aim.available()
                    && aim.entityIndex()==i;
        }
        boolean initialA() {
            return da>=1 && da<=3.25 && db>=1 && db<=3.25
                    && da+0.12<db && selected(a);
        }
        boolean bCloserButASelected() {
            return da>=1 && da<=3.5 && db>=1 && db<=3.5
                    && db+0.12<da && selected(a);
        }
        boolean aFarBSelected() {
            return da>3.5 && db>=1 && db<=3.5 && selected(b);
        }
    }
}
