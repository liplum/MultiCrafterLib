package net.liplum.multicraft;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;

public class MultiCrafter extends Block {
    public float itemCapacityMultiplier = 1f;
    public float liquidCapacityMultiplier = 1f;
    /*
    [ ==> Seq
      { ==> ObjectMap
        String ==> Any // Value may be a Seq, Float or Integer
        input:{
          items:["mod-id-item/1","mod-id-item2/1"],
          fluids:["mod-id-liquid/10.5","mod-id-gas/10"]
          power:10
        },
        output:{
          items:["mod-id-item/1","mod-id-item2/1"],
          fluids:["mod-id-liquid/10.5","mod-id-gas/10"]
        },
        craftTime:120
      }
    ]
     */
    public Seq<ObjectMap<String, Object>> recipes = new Seq<>();
    /**
     * The analyzed recipes.
     */
    public Seq<Recipe> _recipes;

    public MultiCrafter(String name) {
        super(name);
        update = true;
        solid = true;
    }

    @Override
    public void init() {
        _recipes = MultiCrafterAnalyzer.analyze(this, this.recipes);
        setupBlockByRecipes();
        super.init();
    }

    public class MultiCrafterBuild extends Building {

    }

    public void setupBlockByRecipes() {
        int maxItemAmount = 0;
        float maxFluidAmount = 0f;
        float maxPower = 0f;
        for (Recipe recipe : _recipes) {
            Seq<ItemStack> items = recipe.input.items;
            Seq<LiquidStack> fluids = recipe.input.fluids;
            maxItemAmount = Math.max(recipe.maxItemAmount(), maxItemAmount);
            maxFluidAmount = Math.max(recipe.maxFluidAmount(), maxFluidAmount);
            hasItems |= !items.isEmpty();
            hasLiquids |= !fluids.isEmpty();
            maxPower = Math.max(recipe.maxPower(), maxPower);
            hasPower |= maxPower > 0f;
        }
        itemCapacity = Math.max((int) (maxItemAmount * itemCapacityMultiplier), itemCapacity);
        liquidCapacity = Math.max((int) (maxFluidAmount * liquidCapacityMultiplier), liquidCapacity);
    }
}
