package net.liplum.multicraft;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Liquids;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import net.liplum.multicraft.type.IOEntry;
import net.liplum.multicraft.type.Recipe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MultiCrafterAnalyzer {
    private static final String[] inputAlias = {
        "input", "in", "i"
    };
    private static final String[] outputAlias = {
        "output", "out", "o"
    };

    @SuppressWarnings({"rawtypes"})
    public static Seq<Recipe> analyze(Block meta, Object o) {
        Seq<Recipe> recipes = new Seq<>();
        int index = 0;
        if (o instanceof List) { // A list of recipe
            List all = (List) o;
            for (Object recipeMapObj : all) {
                Map recipeMap = (Map) recipeMapObj;
                analyzeRecipe(recipeAt(meta, index), recipeMap, recipes);
                index++;
            }
        } else if (o instanceof Map) { // Only one recipe
            Map recipeMap = (Map) o;
            analyzeRecipe(recipeAt(meta, index), recipeMap, recipes);
        }
        return recipes;
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeRecipe(String recipeIndex, Map recipeMap, Seq<Recipe> to) {
        try {
            Recipe recipe = new Recipe();
            Object inputsRaw = findValueByAlias(recipeMap, inputAlias);
            if (inputsRaw == null) {
                Log.warn("Recipe of " + recipeIndex + " doesn't have any input, so skip it.");
                return;
            }
            Object outputsRaw = findValueByAlias(recipeMap, outputAlias);
            if (outputsRaw == null) {
                Log.warn("Recipe of " + recipeIndex + " doesn't have any output, so skip it.");
                return;
            }
            recipe.input = analyzeIOEntry("input", inputsRaw);
            recipe.output = analyzeIOEntry("output", outputsRaw);
            Object craftTimeObj = recipeMap.get("craftTime");
            recipe.craftTime = analyzeFloat(craftTimeObj);
            if (!recipe.isAnyEmpty()) to.add(recipe);
            else Log.warn("Recipe of " + recipeIndex + " is empty, so skip it.", recipe);
        } catch (Exception e) {
            Log.err("Can't load a recipe of " + recipeIndex + " because " + e, e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public static Object findValueByAlias(Map map, String... aliases) {
        for (String alias : aliases) {
            Object tried = map.get(alias);
            if (tried != null) return tried;
        }
        return null;
    }

    public static String recipeAt(Block meta, int index) {
        return genName(meta) + " at " + index;
    }

    @SuppressWarnings({"rawtypes"})
    public static IOEntry analyzeIOEntry(String meta, Object ioEntry) {
        IOEntry res = new IOEntry();
        // Inputs
        if (ioEntry instanceof Map) {
            /*
                input/output:{
                  items:[],
                  fluids:[],
                  power:0
                }
             */
            Map ioRawMap = (Map) ioEntry;
            // Items
            Object items = ioRawMap.get("items");
            if (items != null) {
                if (items instanceof List) { // ["mod-id-item/1","mod-id-item2"]
                    analyzeItems((List) items, res.items);
                } else if (items instanceof String) {
                    ItemStack entry = analyzeItemPair((String) items);
                    res.items.add(entry);
                } else if (items instanceof Map) {
                    ItemStack entry = analyzeItemMap((Map) items);
                    res.items.add(entry);
                }  else throw new MulticrafterRecipeAnalyzerException("Unsupported type of item " + items);
            }
            // Fluids
            Object fluids = ioRawMap.get("fluids");
            if (fluids != null) {
                if (fluids instanceof List) { // ["mod-id-item/1","mod-id-item2"]
                    analyzeFluids((List) fluids, res.fluids);
                } else if (fluids instanceof String) {
                    LiquidStack entry = analyzeFluidPair((String) fluids);
                    res.fluids.add(entry);
                } else if (fluids instanceof Map) {
                    LiquidStack entry = analyzeFluidMap((Map) fluids);
                    res.fluids.add(entry);
                } else throw new MulticrafterRecipeAnalyzerException("Unsupported type of fluid " + fluids);
            }
            // power
            Object powerObj = ioRawMap.get("power");
            res.power = analyzeFloat(powerObj);
        } else if (ioEntry instanceof List) {
            /*
              input/output: []
             */
            for (Object content : (List) ioEntry) {
                if (content instanceof String) {
                    analyzeAnyPair((String) content, res.items, res.fluids);
                } else if (content instanceof Map) {
                    analyzeAnyMap((Map) content, res.items, res.fluids);
                } else throw new MulticrafterRecipeAnalyzerException("Unsupported type of " + meta + "" + content);
            }
        } else if (ioEntry instanceof String) {
            /*
                input/output : "item/1"
             */
            analyzeAnyPair((String) ioEntry, res.items, res.fluids);
        } else throw new MulticrafterRecipeAnalyzerException("Unsupported type of " + meta + " " + ioEntry);
        return res;
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeItems(List items, Seq<ItemStack> to) {
        for (Object entryRaw : items) {
            if (entryRaw instanceof String) { // if the input is String as "mod-id-item/1"
                ItemStack entry = analyzeItemPair((String) entryRaw);
                to.add(entry);
            } else if (entryRaw instanceof Map) {
                // if the input is Map as { item : "copper", amount : 1 }
                ItemStack entry = analyzeItemMap((Map) entryRaw);
                to.add(entry);
            } else throw new MulticrafterRecipeAnalyzerException("Unsupported type of ItemStack " + entryRaw);
        }
    }

    public static ItemStack analyzeItemPair(String pair) throws NumberFormatException, MulticrafterRecipeAnalyzerException {
        String[] id2Amount = pair.split("/");
        if (id2Amount.length != 1 && id2Amount.length != 2)
            throw new MulticrafterRecipeAnalyzerException(Arrays.toString(id2Amount) + "doesn't contains 1 or 2 entries.");
        String itemID = id2Amount[0];
        Item item = findItem(itemID);
        if (item == null) throw new MulticrafterRecipeAnalyzerException(itemID + " doesn't exist in items.");
        ItemStack entry = new ItemStack();
        entry.item = item;
        if (id2Amount.length == 2) {
            String amountStr = id2Amount[1];
            entry.amount = Integer.parseInt(amountStr);// throw NumberFormatException
        } else {
            entry.amount = 1;
        }
        return entry;
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeFluids(List fluids, Seq<LiquidStack> to) {
        for (Object entryRaw : fluids) {
            if (entryRaw instanceof String) { // if the input is String as "mod-id-item/1"
                LiquidStack entry = analyzeFluidPair((String) entryRaw);
                to.add(entry);
            } else if (entryRaw instanceof Map) {
                // if the input is Map as { item : "water", amount : 1.2 }
                LiquidStack entry = analyzeFluidMap((Map) entryRaw);
                to.add(entry);
            } else throw new MulticrafterRecipeAnalyzerException("Unsupported type of LiquidStack " + entryRaw);
        }
    }

    public static LiquidStack analyzeFluidPair(String pair) {
        String[] id2Amount = pair.split("/");
        if (id2Amount.length != 1 && id2Amount.length != 2)
            throw new MulticrafterRecipeAnalyzerException(Arrays.toString(id2Amount) + "doesn't contains 1 or 2 entries.");
        String fluidID = id2Amount[0];
        Liquid fluid = findFluid(fluidID);
        if (fluid == null) throw new MulticrafterRecipeAnalyzerException(fluidID + " doesn't exist in fluids.");
        LiquidStack entry = new LiquidStack(Liquids.water, 0f);
        entry.liquid = fluid;
        if (id2Amount.length == 2) {
            String amountStr = id2Amount[1];
            entry.amount = Float.parseFloat(amountStr);// throw NumberFormatException
        } else {
            entry.amount = 1f;
        }
        return entry;
    }

    /**
     * @param pair "mod-id-item/1" or "mod-id-gas"
     */
    public static void analyzeAnyPair(String pair, Seq<ItemStack> items, Seq<LiquidStack> fluids) {
        String[] id2Amount = pair.split("/");
        if (id2Amount.length != 1 && id2Amount.length != 2)
            throw new MulticrafterRecipeAnalyzerException(Arrays.toString(id2Amount) + "doesn't contains 1 or 2 entries.");
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
        throw new MulticrafterRecipeAnalyzerException(pair + "isn't an item or a fluid.");
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeAnyMap(Map map, Seq<ItemStack> items, Seq<LiquidStack> fluids) {
        Object itemRaw = map.get("item");
        if (itemRaw instanceof String) {
            Item item = findItem((String) itemRaw);
            if (item != null) {
                ItemStack entry = new ItemStack();
                entry.item = item;
                Object amountRaw = map.get("amount");
                entry.amount = analyzeInt(amountRaw);
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
                entry.amount = analyzeFloat(amountRaw);
                fluids.add(entry);
                return;
            }
        }

        throw new MulticrafterRecipeAnalyzerException("Unsupported amp of input/output " + map);
    }

    @SuppressWarnings("rawtypes")
    public static ItemStack analyzeItemMap(Map map) {
        ItemStack entry = new ItemStack();
        Object itemID = map.get("item");
        if (itemID instanceof String) {
            Item item = findItem((String) itemID);
            if (item == null) throw new MulticrafterRecipeAnalyzerException(itemID + " doesn't exist in items.");
            entry.item = item;
        } else throw new MulticrafterRecipeAnalyzerException("Can't recognize the item from an ItemStack" + map);
        int amount = analyzeInt(map.get("amount"));
        if (amount <= 0) throw new MulticrafterRecipeAnalyzerException("Item amount should > 0 but " + amount);
        entry.amount = amount;
        return entry;
    }

    @SuppressWarnings("rawtypes")
    public static LiquidStack analyzeFluidMap(Map map) {
        LiquidStack entry = new LiquidStack(Liquids.water, 0f);
        Object itemID = map.get("fluid");
        if (itemID instanceof String) {
            Liquid fluid = findFluid((String) itemID);
            if (fluid == null) throw new MulticrafterRecipeAnalyzerException(itemID + " doesn't exist in fluids.");
            entry.liquid = fluid;
        } else throw new MulticrafterRecipeAnalyzerException("Can't recognize the item from an LiquidStack" + map);
        float amount = analyzeFloat(map.get("amount"));
        if (amount <= 0f) throw new MulticrafterRecipeAnalyzerException("Fluid amount should > 0.0f but " + amount);
        entry.amount = amount;
        return entry;
    }

    public static float analyzeFloat(@Nullable Object floatObj) {
        if (floatObj == null) return 0f;
        if (floatObj instanceof Number) {
            return ((Number) floatObj).floatValue();
        }
        try {
            return Float.parseFloat((String) floatObj);
        } catch (Exception e) {
            return 0f;
        }
    }

    public static int analyzeInt(@Nullable Object intObj) {
        if (intObj == null) return 0;
        if (intObj instanceof Number) {
            return ((Number) intObj).intValue();
        }
        try {
            return Integer.parseInt((String) intObj);
        } catch (Exception e) {
            return 0;
        }
    }


    public static String genName(Block meta) {
        return meta.localizedName + "[" + meta.name + "]";
    }

    @Nullable
    public static Item findItem(String id) {
        Seq<Item> items = Vars.content.items();
        for (Item item : items) {
            if (id.equals(item.name)) { // prevent null pointer
                return item;
            }
        }
        return null;
    }

    @Nullable
    public static Liquid findFluid(String id) {
        Seq<Liquid> fluids = Vars.content.liquids();
        for (Liquid fluid : fluids) {
            if (id.equals(fluid.name)) { // prevent null pointer
                return fluid;
            }
        }
        return null;
    }
}
