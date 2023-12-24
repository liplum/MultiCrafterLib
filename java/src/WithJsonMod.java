import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.*;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import multicraft.*;

public class WithJsonMod extends Mod {
  public WithJsonMod() {
    Log.info("WithJson Mod <init>");
  }

  @Override
  public void init() {
    Log.info("WithJson Mod init()");
  }

  @Override
  public void loadContent() {
    Log.info("WithJson Mod loadContent()");
    new MultiCrafter("java-crafter") {{
      requirements(Category.crafting, ItemStack.with(
        Items.copper, 10
      ));
      switchStyle = RecipeSwitchStyle.detailed;
      resolvedRecipes = Seq.with(
        new Recipe() {{
          input = new IOEntry() {{
            items = ItemStack.with(Items.copper, 1);
          }};
          output = new IOEntry() {{
            items = ItemStack.with(Items.copper, 1);
            power = 0.5f;
          }};
          craftTime = 160f;
          icon = () -> Items.copper.uiIcon;
          craftEffect = Fx.unitCapKill;
          iconColor = Pal.accent;
        }},
        new Recipe() {{
          input = new IOEntry() {{
            fluids = LiquidStack.with(Liquids.oil, 0.15f);
          }};
          output = new IOEntry() {{
            items = ItemStack.with(Items.coal, 2);
            power = 2.5f;
          }};
          craftTime = 60f;
          craftEffect = Fx.smoke;
        }}
      );
    }};
  }
}
