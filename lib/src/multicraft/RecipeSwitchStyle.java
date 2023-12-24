package multicraft;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import multicraft.MultiCrafter.*;

import java.util.*;

public abstract class RecipeSwitchStyle {
    public static HashMap<String, RecipeSwitchStyle> all = new HashMap<>();

    public static RecipeSwitchStyle get(@Nullable String name) {
        if (name == null) return transform;
        RecipeSwitchStyle inMap = all.get(name.toLowerCase());
        if (inMap == null) return transform;
        else return inMap;
    }

    public RecipeSwitchStyle(String name) {
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
        ItemStack[] items = entry.items;
        LiquidStack[] fluids = entry.fluids;
        boolean outputPower = entry.power > 0f;
        boolean outputHeat = entry.heat > 0f;
        PayloadStack[] paylods = entry.payloads;
        if (items.length > 0) {
            return new Image(items[0].item.uiIcon);
        } else if (fluids.length > 0) {
            return new Image(fluids[0].liquid.uiIcon);
        } else if (outputPower) {
            Image img = new Image(Icon.power.getRegion());
            img.setColor(Pal.power);
            return img;
        } else if (outputHeat) {
            Image img = new Image(Icon.waves.getRegion());
            img.setColor(b.heatColor);
            return img;
        } else if (paylods.length > 0) {
            return new Image(paylods[0].item.uiIcon);
        }
        return new Image(Icon.cancel.getRegion());
    }

    public static RecipeSwitchStyle simple = new RecipeSwitchStyle("simple") {

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

    public static RecipeSwitchStyle number = new RecipeSwitchStyle("number") {
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
    public static RecipeSwitchStyle transform = new RecipeSwitchStyle("transform") {
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

    public static RecipeSwitchStyle detailed = new RecipeSwitchStyle("detailed") {

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
