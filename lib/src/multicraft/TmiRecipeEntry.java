package multicraft;

import arc.util.Log;
import tmi.RecipeEntry;

public class TmiRecipeEntry extends RecipeEntry {

  @Override
  public void init() {
    Log.info("multicraft.TmiRecipeEntry initialize");
  }

  @Override
  public void afterInit() {
    Log.info("multicraft.TmiRecipeEntry after initialize");
  }

}
