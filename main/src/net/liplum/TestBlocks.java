package net.liplum;

import arc.struct.Seq;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.meta.BuildVisibility;
import multicraft.IOEntry;
import multicraft.MultiCrafter;
import multicraft.Recipe;

public class TestBlocks {
    public static void load() {
        @SuppressWarnings("unused")
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
                        Seq.with(),
                        0f,
                        0f,
                        Seq.with(PayloadStack.with(
                            Blocks.thoriumWall, 2
                        ))
                    ),
                    new IOEntry(
                        Seq.with(ItemStack.with(
                            Items.surgeAlloy, 1,
                            Items.thorium, 1
                        ))
                    ),
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
