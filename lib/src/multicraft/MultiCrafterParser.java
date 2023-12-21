package multicraft;

import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.Block;

import java.util.*;

import static multicraft.ContentResolver.*;
import static multicraft.ParserUtils.*;

public class MultiCrafterParser {
    private static final String[] inputAlias = {
            "input", "in", "i"
    };
    private static final String[] outputAlias = {
            "output", "out", "o"
    };

    private String curBlock = "";
    private int recipeIndex = 0;

    @SuppressWarnings({"rawtypes"})
    public Seq<Recipe> parse(Block meta, Object o) {
        curBlock = genName(meta);
        try {
            o = parseJsonToObject(o);
        } catch (Exception e) {
            error("Can't convert Seq in preprocess " + o, e);
            o = Collections.emptyList();
        }
        Seq<Recipe> recipes = new Seq<>(Recipe.class);
        recipeIndex = 0;
        if (o instanceof List) { // A list of recipe
            List all = (List) o;
            for (Object recipeMapObj : all) {
                Map recipeMap = (Map) recipeMapObj;
                parseRecipe(recipeMap, recipes);
                recipeIndex++;
            }
        } else if (o instanceof Map) { // Only one recipe
            Map recipeMap = (Map) o;
            parseRecipe(recipeMap, recipes);
        } else {
            throw new RecipeParserException("Unsupported recipe list from <" + o + ">");
        }
        return recipes;
    }

    private static final Prov<TextureRegion> NotFound = () -> Icon.cancel.getRegion();

    @SuppressWarnings("rawtypes")
    private void parseRecipe(Map recipeMap, Seq<Recipe> to) {
        try {
            Recipe recipe = new Recipe();
            Object inputsRaw = findValueByAlias(recipeMap, inputAlias);
            if (inputsRaw == null) {
                Log.warn("Recipe doesn't have any input, so skip it");
                return;
            }
            Object outputsRaw = findValueByAlias(recipeMap, outputAlias);
            if (outputsRaw == null) {
                Log.warn("Recipe doesn't have any output, so skip it");
                return;
            }
            recipe.input = parseIOEntry("input", inputsRaw);
            recipe.output = parseIOEntry("output", outputsRaw);
            Object craftTimeObj = recipeMap.get("craftTime");
            recipe.craftTime = parseFloat(craftTimeObj);
            Object iconObj = recipeMap.get("icon");
            if (iconObj instanceof String) {
                recipe.icon = findIcon((String) iconObj);
            }
            Object iconColorObj = recipeMap.get("iconColor");
            if (iconColorObj instanceof String) {
                recipe.iconColor = Color.valueOf((String) iconColorObj);
            }
            Object fxObj = recipeMap.get("craftEffect");
            Effect fx = parseFx(fxObj);
            if (fx != null) {
                recipe.craftEffect = fx;
            }
            // Check empty
            if (!recipe.isAnyEmpty()) {
                to.add(recipe);
            } else Log.warn("Recipe is empty, so skip it", recipe);
        } catch (Exception e) {
            error("Can't load a recipe", e);
        }
    }

    @SuppressWarnings({"rawtypes"})
    private IOEntry parseIOEntry(String meta, Object ioEntry) {
        IOEntry res = new IOEntry();
        // Inputs
        if (ioEntry instanceof Map) {
            /*
                input/output:{
                  items:[],
                  fluids:[],
                  power:0,
                  heat:0,
                  icon: Icon.power,
                  iconColor: "#FFFFFF"
                }
             */
            Map ioRawMap = (Map) ioEntry;
            // Items
            Object items = ioRawMap.get("items");
            if (items != null) {
                if (items instanceof List) { // ["mod-id-item/1","mod-id-item2"]
                    parseItems((List) items, res.items);
                } else if (items instanceof String) {
                    parseItemPair((String) items, res.items);
                } else if (items instanceof Map) {
                    parseItemMap((Map) items, res.items);
                } else
                    throw new RecipeParserException("Unsupported type of items at " + meta + " from <" + items + ">");
            }
            // Fluids
            Object fluids = ioRawMap.get("fluids");
            if (fluids != null) {
                if (fluids instanceof List) { // ["mod-id-item/1","mod-id-item2"]
                    parseFluids((List) fluids, res.fluids);
                } else if (fluids instanceof String) {
                    parseFluidPair((String) fluids, res.fluids);
                } else if (fluids instanceof Map) {
                    parseFluidMap((Map) fluids, res.fluids);
                } else
                    throw new RecipeParserException("Unsupported type of fluids at " + meta + " from <" + fluids + ">");
            }
            // power
            Object powerObj = ioRawMap.get("power");
            res.power = parseFloat(powerObj);
            Object heatObj = ioRawMap.get("heat");
            res.heat = parseFloat(heatObj);
            Object iconObj = ioRawMap.get("icon");
            if (iconObj instanceof String) {
                res.icon = findIcon((String) iconObj);
            }
            Object iconColorObj = ioRawMap.get("iconColor");
            if (iconColorObj instanceof String) {
                res.iconColor = Color.valueOf((String) iconColorObj);
            }
        } else if (ioEntry instanceof List) {
            /*
              input/output: []
             */
            for (Object content : (List) ioEntry) {
                if (content instanceof String) {
                    parseAnyPair((String) content, res.items, res.fluids);
                } else if (content instanceof Map) {
                    parseAnyMap((Map) content, res.items, res.fluids);
                } else {
                    throw new RecipeParserException("Unsupported type of content at " + meta + " from <" + content + ">");
                }
            }
        } else if (ioEntry instanceof String) {
            /*
                input/output : "item/1"
             */
            parseAnyPair((String) ioEntry, res.items, res.fluids);
        } else {
            throw new RecipeParserException("Unsupported type of " + meta + " <" + ioEntry + ">");
        }
        return res;
    }

    @SuppressWarnings("rawtypes")
    private void parseItems(List items, Seq<ItemStack> to) {
        for (Object entryRaw : items) {
            if (entryRaw instanceof String) { // if the input is String as "mod-id-item/1"
                parseItemPair((String) entryRaw, to);
            } else if (entryRaw instanceof Map) {
                // if the input is Map as { item : "copper", amount : 1 }
                parseItemMap((Map) entryRaw, to);
            } else {
                error("Unsupported type of items <" + entryRaw + ">, so skip them");
            }
        }
    }

    private void parseItemPair(String pair, Seq<ItemStack> to) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }
            String itemID = id2Amount[0];
            Item item = findItem(itemID);
            if (item == null) {
                error("<" + itemID + "> doesn't exist in all items, so skip this");
                return;
            }
            ItemStack entry = new ItemStack();
            entry.item = item;
            if (id2Amount.length == 2) {
                String amountStr = id2Amount[1];
                entry.amount = Integer.parseInt(amountStr);// throw NumberFormatException
            } else {
                entry.amount = 1;
            }
            to.add(entry);
        } catch (Exception e) {
            error("Can't parse an item from <" + pair + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseFluids(List fluids, Seq<LiquidStack> to) {
        for (Object entryRaw : fluids) {
            if (entryRaw instanceof String) { // if the input is String as "mod-id-item/1"
                parseFluidPair((String) entryRaw, to);
            } else if (entryRaw instanceof Map) {
                // if the input is Map as { item : "water", amount : 1.2 }
                parseFluidMap((Map) entryRaw, to);
            } else {
                error("Unsupported type of fluids <" + entryRaw + ">, so skip them");
            }
        }
    }

    private void parseFluidPair(String pair, Seq<LiquidStack> to) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }
            String fluidID = id2Amount[0];
            Liquid fluid = findFluid(fluidID);
            if (fluid == null) {
                error("<" + fluidID + "> doesn't exist in all fluids, so skip this");
                return;
            }
            LiquidStack entry = new LiquidStack(Liquids.water, 0f);
            entry.liquid = fluid;
            if (id2Amount.length == 2) {
                String amountStr = id2Amount[1];
                entry.amount = Float.parseFloat(amountStr);// throw NumberFormatException
            } else {
                entry.amount = 1f;
            }
            to.add(entry);
        } catch (Exception e) {
            error("Can't parse a fluid from <" + pair + ">, so skip it", e);
        }
    }

    /**
     * @param pair "mod-id-item/1" or "mod-id-gas"
     */
    private void parseAnyPair(String pair, Seq<ItemStack> items, Seq<LiquidStack> fluids) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }
            String id = id2Amount[0];
            // Find in item
            Item item = findItem(id);
            if (item != null) {
                ItemStack entry = new ItemStack();
                entry.item = item;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Integer.parseInt(amountStr);// throw NumberFormatException
                } else {
                    entry.amount = 1;
                }
                items.add(entry);
                return;
            }
            Liquid fluid = findFluid(id);
            if (fluid != null) {
                LiquidStack entry = new LiquidStack(Liquids.water, 0f);
                entry.liquid = fluid;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Float.parseFloat(amountStr);// throw NumberFormatException
                } else {
                    entry.amount = 1f;
                }
                fluids.add(entry);
                return;
            }
            error("Can't find the corresponding item or fluid from this <" + pair + ">, so skip it");
        } catch (Exception e) {
            error("Can't parse this uncertain <" + pair + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseAnyMap(Map map, Seq<ItemStack> items, Seq<LiquidStack> fluids) {
        try {
            Object itemRaw = map.get("item");
            if (itemRaw instanceof String) {
                Item item = findItem((String) itemRaw);
                if (item != null) {
                    ItemStack entry = new ItemStack();
                    entry.item = item;
                    Object amountRaw = map.get("amount");
                    entry.amount = parseInt(amountRaw);
                    items.add(entry);
                    return;
                }
            }
            Object fluidRaw = map.get("fluid");
            if (fluidRaw instanceof String) {
                Liquid fluid = findFluid((String) fluidRaw);
                if (fluid != null) {
                    LiquidStack entry = new LiquidStack(Liquids.water, 0f);
                    entry.liquid = fluid;
                    Object amountRaw = map.get("amount");
                    entry.amount = parseFloat(amountRaw);
                    fluids.add(entry);
                    return;
                }
            }
            error("Can't find the corresponding item or fluid from <" + map + ">, so skip it");
        } catch (Exception e) {
            error("Can't parse this uncertain <" + map + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseItemMap(Map map, Seq<ItemStack> to) {
        try {
            ItemStack entry = new ItemStack();
            Object itemID = map.get("item");
            if (itemID instanceof String) {
                Item item = findItem((String) itemID);
                if (item == null) {
                    error("<" + itemID + "> doesn't exist in all items, so skip this");
                    return;
                }
                entry.item = item;
            } else {
                error("Can't recognize a fluid from <" + map + ">");
                return;
            }
            int amount = parseInt(map.get("amount"));
            entry.amount = amount;
            if (amount <= 0) {
                error("Item amount is +" + amount + " <=0, so reset as 1");
                entry.amount = 1;
            }
            to.add(entry);
        } catch (Exception e) {
            error("Can't parse an item <" + map + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseFluidMap(Map map, Seq<LiquidStack> to) {
        try {
            LiquidStack entry = new LiquidStack(Liquids.water, 0f);
            Object itemID = map.get("fluid");
            if (itemID instanceof String) {
                Liquid fluid = findFluid((String) itemID);
                if (fluid == null) {
                    error(itemID + " doesn't exist in all fluids, so skip this");
                    return;
                }
                entry.liquid = fluid;
            } else {
                error("Can't recognize an item from <" + map + ">");
                return;
            }
            float amount = parseFloat(map.get("amount"));
            entry.amount = amount;
            if (amount <= 0f) {
                error("Fluids amount is +" + amount + " <=0, so reset as 1.0f");
                entry.amount = 1f;
            }
            to.add(entry);
        } catch (Exception e) {
            error("Can't parse <" + map + ">, so skip it", e);
        }
    }


    /**
     * Only work on single threading.
     */
    private void error(String content) {
        Log.err("[" + curBlock + "](at recipe " + recipeIndex + ")\n" + content);
    }

    /**
     * Only work on single threading.
     */
    private void error(String content, Throwable e) {
        Log.err("[" + curBlock + "](at recipe " + recipeIndex + ")\n" + content, e);
    }

    public static String genName(Block meta) {
        if (meta.localizedName.equals(meta.name)) {
            return meta.name;
        } else {
            return meta.localizedName + "(" + meta.name + ")";
        }
    }

    private Prov<TextureRegion> findIcon(String name) {
        Prov<TextureRegion> icon = ContentResolver.findIcon(name);
        if (icon == null) {
            error("Icon <" + name + "> not found, so use a cross instead.");
            icon = NotFound;
        }
        return icon;
    }

    private static final Effect[] EffectType = new Effect[0];

    private static Effect composeMultiFx(List<String> names) {
        ArrayList<Effect> all = new ArrayList<>();
        for (String name : names) {
            Effect fx = findFx(name);
            if (fx != null) all.add(fx);
        }
        return new MultiEffect(all.toArray(EffectType));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static Effect parseFx(Object obj) {
        if (obj instanceof String)
            return findFx((String) obj);
        else if (obj instanceof List) {
            return composeMultiFx((List<String>) obj);
        } else {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    private static Object findValueByAlias(Map map, String... aliases) {
        for (String alias : aliases) {
            Object tried = map.get(alias);
            if (tried != null) {
                return tried;
            }
        }
        return null;
    }
}
