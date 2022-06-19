package net.liplum.multicraftmod;

import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.meta.BuildVisibility;
import net.liplum.multicraft.type.IOEntry;
import net.liplum.multicraft.type.MultiCrafter;
import net.liplum.multicraft.type.Recipe;

public class TestBlocks {
    public static void load() {
        MultiCrafter test = new MultiCrafter("multi-crafter") {{
            buildVisibility = BuildVisibility.shown;
            category = Category.crafting;
            size = 5;
            resolvedRecipes = Seq.with(
                new Recipe(
                    new IOEntry(
                        Seq.with(ItemStack.with(
                            Items.copper, 1,
                            Items.lead, 1
                        )),
                        Seq.with(LiquidStack.with(
                            Liquids.water, 0.5f
                        )),
                        5f),
                    new IOEntry(
                        Seq.with(ItemStack.with(
                            Items.surgeAlloy, 1,
                            Items.thorium, 1
                        )),
                        Seq.with(LiquidStack.with(
                            Liquids.neoplasm, 0.2f
                        ))),
                    120f
                ),
                new Recipe(
                    new IOEntry(
                        Seq.with(ItemStack.with(
                            Items.plastanium, 1,
                            Items.pyratite, 1
                        )),
                        Seq.with(LiquidStack.with(
                            Liquids.slag, 0.5f
                        ))),
                    new IOEntry(
                        Seq.with(ItemStack.with(
                            Items.coal, 1,
                            Items.sand, 1
                        )),
                        Seq.with(LiquidStack.with(
                            Liquids.oil, 0.2f
                        )),
                        5f),
                    150f
                )
            );
        }};
    }
}
