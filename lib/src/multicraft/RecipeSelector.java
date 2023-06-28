package multicraft;

import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Scaling;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import multicraft.MultiCrafter.MultiCrafterBuild;

import java.util.HashMap;

public abstract class RecipeSelector {
    public static HashMap<String, RecipeSelector> all = new HashMap<>();

    public static RecipeSelector get(@Nullable String name) {
        if (name == null) return Transform;
        RecipeSelector inMap = all.get(name.toLowerCase());
        if (inMap == null) return Transform;
        else return inMap;
    }

    public RecipeSelector(String name) {
        all.put(name.toLowerCase(), this);
    }

    public abstract void build(MultiCrafter b, MultiCrafterBuild c, Table table);

    public static Image getDefaultIcon(MultiCrafter b, MultiCrafterBuild c, IOEntry entry) {
        if (entry.icon != null) {
            Image img = new Image(entry.icon.get());
            if (entry.iconColor != null)
                img.setColor(entry.iconColor);
            return img;
        }
        Seq<ItemStack> items = entry.items;
        Seq<LiquidStack> fluids = entry.fluids;
        boolean outputPower = entry.power > 0f;
        boolean outputHeat = entry.heat > 0f;
        if (items.size > 0) {
            return new Image(items.get(0).item.uiIcon);
        } else if (fluids.size > 0) {
            return new Image(fluids.get(0).liquid.uiIcon);
        } else if (outputPower) {
            Image img = new Image(Icon.power.getRegion());
            img.setColor(Pal.power);
            return img;
        } else if (outputHeat) {
            Image img = new Image(Icon.waves.getRegion());
            img.setColor(b.heatColor);
            return img;
        }
        return new Image(Icon.cancel.getRegion());
    }

    public static RecipeSelector Simple = new RecipeSelector("simple") {

        @Override
        public void build(MultiCrafter b, MultiCrafterBuild c, Table table) {
            Table t = new Table();
            t.background(Tex.whiteui);
            t.setColor(Pal.darkerGray);
            for (int i = 0; i < b.resolvedRecipes.size; i++) {
                Recipe recipe = b.resolvedRecipes.get(i);
                int finalI = i;
                ImageButton button = new ImageButton(Styles.clearTogglei);
                Image img;
                if (recipe.icon != null) {
                    img = new Image(recipe.icon.get());
                    if (recipe.iconColor != null)
                        img.setColor(recipe.iconColor);
                } else {
                    img = getDefaultIcon(b, c, recipe.output);
                }
                button.replaceImage(img);
                button.getImageCell().scaling(Scaling.fit).size(Vars.iconLarge);
                button.changed(() -> c.configure(finalI));
                button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                t.add(button).grow().margin(10f);
                if (i != 0 && i % 3 == 0) {
                    t.row();
                }
            }
            table.add(t).grow();
        }
    };

    public static RecipeSelector Number = new RecipeSelector("number") {
        @Override
        public void build(MultiCrafter b, MultiCrafterBuild c, Table table) {
            Table t = new Table();
            for (int i = 0; i < b.resolvedRecipes.size; i++) {
                Recipe recipe = b.resolvedRecipes.get(i);
                int finalI = i;
                TextButton button = Elem.newButton("" + i, Styles.togglet,
                    () -> c.configure(finalI));
                if (recipe.iconColor != null)
                    button.setColor(recipe.iconColor);
                button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                t.add(button).size(50f);
                if (i != 0 && i % 3 == 0) {
                    t.row();
                }
            }
            table.add(t).grow();
        }
    };
    public static RecipeSelector Transform = new RecipeSelector("transform") {
        @Override
        public void build(MultiCrafter b, MultiCrafterBuild c, Table table) {
            Table t = new Table();
            for (int i = 0; i < b.resolvedRecipes.size; i++) {
                if (i != 0 && i % 2 == 0) {
                    t.row();
                }
                Recipe recipe = b.resolvedRecipes.get(i);
                int finalI = i;
                ImageButton button = new ImageButton(Styles.clearTogglei);
                Table bt = new Table();
                Image in = getDefaultIcon(b, c, recipe.input);
                bt.add(in).pad(6f);
                bt.image(Icon.right).pad(6f);
                Image out = getDefaultIcon(b, c, recipe.output);
                bt.add(out).pad(6f);
                button.replaceImage(bt);
                button.changed(() -> c.configure(finalI));
                button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                t.add(button).grow().pad(8f).margin(10f);
            }
            table.add(t).grow();
        }
    };

    public static RecipeSelector Detailed = new RecipeSelector("detailed") {

        @Override
        public void build(MultiCrafter b, MultiCrafterBuild c, Table table) {
            for (int i = 0; i < b.resolvedRecipes.size; i++) {
                Recipe recipe = b.resolvedRecipes.get(i);
                Table t = new Table();
                t.background(Tex.whiteui);
                t.setColor(Pal.darkestGray);
                b.buildIOEntry(t, recipe, true);
                t.image(Icon.right);
                b.buildIOEntry(t, recipe, false);
                int finalI = i;
                ImageButton button = new ImageButton(Styles.clearTogglei);
                button.changed(() -> c.configure(finalI));
                button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                button.replaceImage(t);
                table.add(button).pad(5f).margin(10f).grow();
                table.row();
            }
        }
    };
}
