package net.liplum.multicraft;

import arc.struct.ObjectMap;
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

import java.util.Arrays;

public class MultiCrafterAnalyzer {
    public static Seq<Recipe> analyze(Block meta, Seq<ObjectMap<String, Object>> r) {
        Seq<Recipe> recipes = new Seq<>();
        int index = 0;
        for (ObjectMap<String, Object> recipeMap : r) {
            try {
                Recipe recipe = new Recipe();
                Object inputsRaw = recipeMap.get("input");
                if (inputsRaw == null) {
                    Log.warn("Recipe of " + recipeAt(meta, index) + " doesn't have any input, so skip it.");
                    continue;
                }
                Object outputsRaw = recipeMap.get("output");
                if (outputsRaw == null) {
                    Log.warn("Recipe of " + recipeAt(meta, index) + " doesn't have any output, so skip it.");
                    continue;
                }
                recipe.input = analyzeIOEntry("input", inputsRaw);
                recipe.output = analyzeIOEntry("output", inputsRaw);
                Object craftTimeObj = recipeMap.get("craftTime");
                recipe.craftTime = analyzeFloat(craftTimeObj);
                Object powerOutputDurationObj = recipeMap.get("powerOutputDuration");
                recipe.powerOutputDuration = analyzeFloat(powerOutputDurationObj);
                if (!recipe.isEmpty())
                    recipes.add(recipe);
                else
                    Log.warn("Recipe of " + recipeAt(meta, index) + " is empty, so skip it.", recipe);
            } catch (Exception e) {
                Log.err("Can't load a recipe of " + recipeAt(meta, index) + " because " + e, e);
            } finally {
                index++;
            }
        }
        return recipes;
    }

    public static String recipeAt(Block meta, int index) {
        return genName(meta) + " at " + index;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static IOEntry analyzeIOEntry(String meta, Object map) {
        IOEntry res = new IOEntry();
        // Inputs
        if (map instanceof ObjectMap<?, ?>) {
            /*
                input/output:{
                  items:[],
                  fluids:[],
                  power:0
                }
             */
            ObjectMap<String, Object> ioRawMap = (ObjectMap<String, Object>) map;
            // Items
            Object items = ioRawMap.get("items");
            if (items instanceof Seq) { // ["mod-id-item/1","mod-id-item2"]
                analyzeItems((Seq) items, res.items);
            } else if (items instanceof String) {
                ItemStack input = analyzeItemPair((String) items);
                res.items.add(input);
            } else throw new MulticrafterRecipeAnalyzerException("Unsupported type of item " + items);
            // Fluids
            Object fluids = ioRawMap.get("fluids");
            if (fluids instanceof Seq) { // ["mod-id-item/1","mod-id-item2"]
                analyzeFluids((Seq) fluids, res.fluids);
            } else if (items instanceof String) {
                LiquidStack input = analyzeFluidPair((String) fluids);
                res.fluids.add(input);
            } else throw new MulticrafterRecipeAnalyzerException("Unsupported type of fluid " + items);
            // power
            Object powerObj = ioRawMap.get("power");
            res.power = analyzeFloat(powerObj);
        } else throw new MulticrafterRecipeAnalyzerException("Unsupported type of " + meta + " " + map);
        return res;
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeItems(Seq items, Seq<ItemStack> to) {
        for (Object inputEntry : items) {
            if (inputEntry instanceof String) { // if the input is String as "mod-id-item/1"
                ItemStack entry = analyzeItemPair((String) inputEntry);
                to.add(entry);
            }// TODO: Add map support
        }
    }

    public static ItemStack analyzeItemPair(String pair) throws NumberFormatException, MulticrafterRecipeAnalyzerException {
        String[] id2Amount = pair.split("/");
        if (id2Amount.length != 1 && id2Amount.length != 2)
            throw new MulticrafterRecipeAnalyzerException(Arrays.toString(id2Amount) + "doesn't contains 1 or 2 entries.");
        String itemID = id2Amount[0];
        Item item = findItem(itemID);
        if (item == null) throw new MulticrafterRecipeAnalyzerException(itemID + " doesn't exist.");
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
    public static void analyzeFluids(Seq fluids, Seq<LiquidStack> to) {
        for (Object inputEntry : fluids) {
            if (inputEntry instanceof String) { // if the input is String as "mod-id-item/1"
                LiquidStack entry = analyzeFluidPair((String) inputEntry);
                to.add(entry);
            }// TODO: Add map support
        }
    }

    public static LiquidStack analyzeFluidPair(String pair) throws NumberFormatException, MulticrafterRecipeAnalyzerException {
        String[] id2Amount = pair.split("/");
        if (id2Amount.length != 1 && id2Amount.length != 2)
            throw new MulticrafterRecipeAnalyzerException(Arrays.toString(id2Amount) + "doesn't contains 1 or 2 entries.");
        String fluidID = id2Amount[0];
        Liquid fluid = findFluid(fluidID);
        if (fluid == null) throw new MulticrafterRecipeAnalyzerException(fluidID + " doesn't exist.");
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
     * @param pair "mod-id-item/1" or "mod-id-item"
     * @return {@linkplain ItemStack} or {@linkplain LiquidStack}
     */
    public static Object analyzeAnyPair(String pair) throws NumberFormatException, MulticrafterRecipeAnalyzerException {
        // TODO: Complete this
        ItemStack entry = new ItemStack();
        return entry;
    }

    @SuppressWarnings("rawtypes")
    public static ItemStack analyzeItemMap(ObjectMap map) throws NumberFormatException, MulticrafterRecipeAnalyzerException {
        // TODO: Complete this
        ItemStack entry = new ItemStack();
        return entry;
    }

    @SuppressWarnings("rawtypes")
    public static LiquidStack analyzeFluidMap(ObjectMap map) throws NumberFormatException, MulticrafterRecipeAnalyzerException {
        // TODO: Complete this
        LiquidStack entry = new LiquidStack(Liquids.water, 0f);
        return entry;
    }

    public static float analyzeFloat(@Nullable Object floatObj) {
        if (floatObj == null) return 0f;
        if (floatObj instanceof Float) {
            return (Float) floatObj;
        }
        try {
            return Float.parseFloat((String) floatObj);
        } catch (Exception e) {
            return 0f;
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
