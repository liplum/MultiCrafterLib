package net.liplum.multicraft.mod;

import mindustry.mod.Mod;

public class MultiCrafterMod extends Mod {
    public static final boolean DebugMode = true;
    public MultiCrafterMod() {
    }

    @Override
    public void loadContent() {
        if(DebugMode){
            TestBlocks.load();
        }
    }
}
