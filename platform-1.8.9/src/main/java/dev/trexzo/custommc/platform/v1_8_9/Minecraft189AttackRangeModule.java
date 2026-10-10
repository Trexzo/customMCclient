package dev.trexzo.custommc.platform.v1_8_9;
import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
public final class Minecraft189AttackRangeModule implements Module {
    public static final String ID="combat.attackRange";
    public static final String MAX_RANGE=ID+".maxRange";
    private final Setting<Double> distance=new Setting<Double>(MAX_RANGE,3.0D,
            x->x!=null&&Double.isFinite(x)&&x>=1.0D&&x<=6.0D,SettingCodecs.DOUBLE);
    private boolean enabled;
    @Override public String id(){return ID;}
    @Override public synchronized void onEnable(){enabled=true;}
    @Override public synchronized void onDisable(){enabled=false;}
    public Setting<Double> maxRangeSetting(){return distance;}
    synchronized boolean permits(int index,Minecraft189PlayerPositionState.Snapshot local,
            Minecraft189WorldEntityPositionState.Snapshot world,
            Minecraft189WorldEntityKindState.Snapshot kinds){
        if(!enabled)return true;
        if(index<0||local==null||world==null||kinds==null
                ||!local.available()||!world.available()||!kinds.available()
                ||world.entityCount()!=kinds.entityCount()||index>=world.entityCount()
                ||!kinds.player(index)||kinds.localPlayer(index))return false;
        double dx=world.x(index)-local.x();
        double dy=world.y(index)-local.y();
        double dz=world.z(index)-local.z();
        double square=dx*dx+dy*dy+dz*dz;
        return Double.isFinite(square)&&square<=distance.get()*distance.get();
    }
}
