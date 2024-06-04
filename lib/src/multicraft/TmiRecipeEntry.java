package multicraft;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import org.jetbrains.annotations.NotNull;
import tmi.RecipeEntry;
import tmi.TooManyItems;
import tmi.recipe.RecipeParser;
import tmi.recipe.RecipeType;
import tmi.recipe.types.HeatMark;
import tmi.recipe.types.PowerMark;

public class TmiRecipeEntry extends RecipeEntry {

  @Override
  public void init() {
    Log.info("multicraft.TmiRecipeEntry initialize");
    TooManyItems.recipesManager.registerParser(new MultiCrafterParser());
  }

  @Override
  public void afterInit() {
    Log.info("multicraft.TmiRecipeEntry after initialize");
  }

}

class MultiCrafterParser extends RecipeParser<MultiCrafter> {
  @Override
  public boolean isTarget(@NotNull Block content) {
    return content instanceof MultiCrafter;
  }

  @NotNull
  @Override
  public Seq<tmi.recipe.Recipe> parse(@NotNull MultiCrafter crafter) {
    Seq<tmi.recipe.Recipe> result = new Seq<>();
    for (Recipe recipe : crafter.resolvedRecipes) {
      // input
      tmi.recipe.Recipe tmiRecipe = new tmi.recipe.Recipe(RecipeType.factory)
        .setBlock(getWrap(crafter))
        .setTime(recipe.craftTime);
      for (ItemStack stack : recipe.input.items) {
        tmiRecipe.addMaterial(getWrap(stack.item), stack.amount);
      }
      for (LiquidStack stack : recipe.input.fluids) {
        tmiRecipe.addMaterial(getWrap(stack.liquid), stack.amount);
      }
      tmiRecipe.addMaterial(HeatMark.INSTANCE, recipe.input.heat);
      tmiRecipe.addMaterial(PowerMark.INSTANCE, recipe.input.power);

      // output
      for (ItemStack stack : recipe.output.items) {
        tmiRecipe.addProduction(getWrap(stack.item), stack.amount);
      }
      for (LiquidStack stack : recipe.output.fluids) {
        tmiRecipe.addProduction(getWrap(stack.liquid), stack.amount);
      }

      tmiRecipe.addProduction(HeatMark.INSTANCE, recipe.output.heat);
      tmiRecipe.addProduction(PowerMark.INSTANCE, recipe.output.power);
      result.add(tmiRecipe);
    }

    return result;
  }
}