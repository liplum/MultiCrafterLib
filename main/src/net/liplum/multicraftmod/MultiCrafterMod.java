package net.liplum.multicraftmod;

import mindustry.Vars;
import mindustry.mod.Mod;
import mindustry.mod.Mods;

public class MultiCrafterMod extends Mod {
    public static final boolean DebugMode = true;
    public static final String MultiCrafterClzName = "net.liplum.multicraft.MultiCrafter";
    public MultiCrafterMod() {
    }

    @Override
    public void loadContent() {
        if (DebugMode) {
            TestBlocks.load();
        }
    }
}
