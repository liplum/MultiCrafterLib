package net.liplum.multicraftmod;

import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;

public class TestBlocks {
    public static void load() {
        GenericCrafter test = new GenericCrafter("test-crafter") {{
            consumePower(1f);
            consumeItem(Items.coal, 1);
            outputItem = new ItemStack(Items.surgeAlloy, 1);
        }};
    }
}
