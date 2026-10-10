package dev.trexzo.custommc.platform.v1_8_9;
import dev.trexzo.custommc.core.module.*;
import dev.trexzo.custommc.core.setting.*;
import java.util.ArrayList;
import java.util.List;
final class Minecraft189AttackRangeFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AttackRangeModule module=new Minecraft189AttackRangeModule();
    private final List<AutoCloseable> owned=new ArrayList<AutoCloseable>();
    private boolean closed;
    private Minecraft189AttackRangeFeature(ModuleController c){controller=c;}
    static Minecraft189AttackRangeFeature install(ModuleRegistry modules,ModuleController controller,
            ModulePresentationRegistry presentations,ModuleSettingRegistry binds,
            SettingRegistry settings,SettingPresentationRegistry labels){
        Minecraft189AttackRangeFeature f=new Minecraft189AttackRangeFeature(controller);
        try{
            f.owned.add(modules.register(f.module));
            f.owned.add(presentations.register(new ModuleDescriptor(
                Minecraft189AttackRangeModule.ID,"Attack Range Gate",
                "Optional shared maximum distance for synthetic player attacks.",
                Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID,115)));
            f.owned.add(settings.register(f.module.maxRangeSetting()));
            f.owned.add(labels.register(new SettingDescriptor(
                Minecraft189AttackRangeModule.MAX_RANGE,"Maximum Attack Distance",
                SettingValueKind.DOUBLE,0,new SettingNumericSpec(1,6,0.1))));
            f.owned.add(binds.register(new ModuleSettingBinding(
                Minecraft189AttackRangeModule.ID,Minecraft189AttackRangeModule.MAX_RANGE,0)));
            return f;
        }catch(RuntimeException e){try{f.close();}catch(RuntimeException cleanup){e.addSuppressed(cleanup);}throw e;}
    }
    Minecraft189AttackRangeModule module(){if(closed)throw new IllegalStateException("closed");return module;}
    @Override public synchronized void close(){
        if(closed)return;closed=true;
        RuntimeException error=null;
        try{if(controller.stateOf(module.id())!=ModuleState.DISABLED)controller.disable(module.id());}
        catch(RuntimeException e){error=e;}
        for(int i=owned.size()-1;i>=0;i--)try{owned.get(i).close();}
        catch(Exception e){RuntimeException next=new IllegalStateException("range close",e);
            if(error==null)error=next;else error.addSuppressed(next);}
        owned.clear();if(error!=null)throw error;
    }
}
