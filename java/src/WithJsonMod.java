import arc.util.Log;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.graphics.CacheLayer;
import mindustry.mod.Mod;
import multicraft.*;
public class WithJsonMod extends Mod {
    public WithJsonMod(){
        Log.info("WithJson Mod <init>");
    }
    @Override
    public void init() {
        Log.info("WithJson Mod init()");
    }
    @Override
    public void loadContent() {
        Log.info("WithJson Mod loadContent()");
         new MultiCrafter("java-crafter"){{

        }};
    }
}
