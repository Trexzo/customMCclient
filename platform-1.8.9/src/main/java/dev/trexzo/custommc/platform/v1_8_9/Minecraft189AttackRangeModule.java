package dev.trexzo.custommc.platform.v1_8_9;
import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
public final class Minecraft189AttackRangeModule implements Module {
    public static final String ID="combat.attackRange";
    public static final String MAX_RANGE=ID+".maxRange";
    public static final String USE_NATIVE_HITBOX=ID+".useNativeHitbox";
    private final Setting<Boolean> useNativeHitbox = new Setting<Boolean>(
            USE_NATIVE_HITBOX, Boolean.FALSE, x -> x != null, SettingCodecs.BOOLEAN);
    private final Setting<Double> distance=new Setting<Double>(MAX_RANGE,3.0D,
            x->x!=null&&Double.isFinite(x)&&x>=1.0D&&x<=6.0D,SettingCodecs.DOUBLE);
    private boolean enabled;
    @Override public String id(){return ID;}
    @Override public synchronized void onEnable(){enabled=true;}
    @Override public synchronized void onDisable(){enabled=false;}
    public Setting<Double> maxRangeSetting(){return distance;}
    public Setting<Boolean> useNativeHitboxSetting(){return useNativeHitbox;}
    synchronized boolean needsNativeHitbox(){return enabled && useNativeHitbox.get();}
    synchronized boolean permits(int index,Minecraft189PlayerPositionState.Snapshot local,
            Minecraft189WorldEntityPositionState.Snapshot world,
            Minecraft189WorldEntityKindState.Snapshot kinds){
        return permits(index, local, world, kinds, null);
    }
    synchronized boolean permits(int index,Minecraft189PlayerPositionState.Snapshot local,
            Minecraft189WorldEntityPositionState.Snapshot world,
            Minecraft189WorldEntityKindState.Snapshot kinds,
            double[] nativeHitbox){
        if(!enabled)return true;
        if(index<0||local==null||world==null||kinds==null
                ||!local.available()||!world.available()||!kinds.available()
                ||world.entityCount()!=kinds.entityCount()||index>=world.entityCount()
                ||!kinds.player(index)||kinds.localPlayer(index))return false;
        double dx=world.x(index)-local.x();
        double dy=world.y(index)-local.y();
        double dz=world.z(index)-local.z();
        double square=dx*dx+dy*dy+dz*dz;
        if (useNativeHitbox.get()) {
            // Only exact mapped vanilla bounds; no synthetic expansion.
            if (nativeHitbox == null || nativeHitbox.length != 6) return false;
            for (double value : nativeHitbox) if (!Double.isFinite(value)) return false;
            if (nativeHitbox[0] > nativeHitbox[3]
                    || nativeHitbox[1] > nativeHitbox[4]
                    || nativeHitbox[2] > nativeHitbox[5]) return false;
            final double x = local.x();
            final double y = local.y();
            final double z = local.z();
            // Conservative distance from local position to native box.
            final double bx = Math.max(nativeHitbox[0] - x,
                    Math.max(0.0D, x - nativeHitbox[3]));
            final double by = Math.max(nativeHitbox[1] - y,
                    Math.max(0.0D, y - nativeHitbox[4]));
            final double bz = Math.max(nativeHitbox[2] - z,
                    Math.max(0.0D, z - nativeHitbox[5]));
            square = bx*bx + by*by + bz*bz;
        }
        return Double.isFinite(square)&&square<=distance.get()*distance.get();
    }
}
