package example;

import arc.util.Log;
import mindustry.mod.*;

public class TemplateMod extends Mod{

    public TemplateMod(){
        Log.info("Loaded TemplateMod constructor.");
    }

    @Override
    public void loadContent(){
        Log.info("Loading some template content.");
    }

}
