import arc.util.Log;
import mindustry.mod.Mod;

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
    }
}
