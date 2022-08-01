package multicraft;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.serialization.JsonValue;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.Liquids;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Icon;
import mindustry.type.*;
import mindustry.world.Block;

import java.lang.reflect.Field;
import java.util.*;

public class MultiCrafterAnalyzer {
    private static final String[] inputAlias = {
        "input", "in", "i"
    };
    private static final String[] outputAlias = {
        "output", "out", "o"
    };

    /**
     * Only work on single threading.
     */
    private static String curBlock = "";
    /**
     * Only work on single threading.
     */
    private static int index = 0;

    public static Object preProcessArc(Object seq) {
        try {
            return processFunc(seq);
        } catch (Exception e) {
            error("Can't convert Seq in preprocess " + seq, e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object processFunc(Object o) {
        if (o instanceof Seq) {
            Seq seq = (Seq) o;
            ArrayList list = new ArrayList(seq.size);
            for (Object e : new Seq.SeqIterable<>(seq)) {
                list.add(processFunc(e));
            }
            return list;
        } else if (o instanceof ObjectMap) {
            ObjectMap objMap = (ObjectMap) o;
            HashMap map = new HashMap();
            for (ObjectMap.Entry<Object, Object> entry : new ObjectMap.Entries<Object, Object>(objMap)) {
                map.put(entry.key, processFunc(entry.value));
            }
            return map;
        } else if (o instanceof JsonValue) {
            return convert((JsonValue) o);
        }
        return o;
    }

    @SuppressWarnings({"rawtypes"})
    public static Seq<Recipe> analyze(Block meta, Object o) {
        curBlock = genName(meta);
        o = preProcessArc(o);
        Seq<Recipe> recipes = new Seq<>(Recipe.class);
        index = 0;
        if (o instanceof List) { // A list of recipe
            List all = (List) o;
            for (Object recipeMapObj : all) {
                Map recipeMap = (Map) recipeMapObj;
                analyzeRecipe(recipeMap, recipes);
                index++;
            }
        } else if (o instanceof Map) { // Only one recipe
            Map recipeMap = (Map) o;
            analyzeRecipe(recipeMap, recipes);
        } else {
            throw new RecipeAnalyzerException("Unsupported recipe list from <" + o + ">");
        }
        return recipes;
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeRecipe(Map recipeMap, Seq<Recipe> to) {
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
            recipe.input = analyzeIOEntry("input", inputsRaw);
            recipe.output = analyzeIOEntry("output", outputsRaw);
            Object craftTimeObj = recipeMap.get("craftTime");
            recipe.craftTime = analyzeFloat(craftTimeObj);
            Object iconObj = recipeMap.get("icon");
            if (iconObj instanceof String)
                recipe.icon = findIcon((String) iconObj);
            Object iconColorObj = recipeMap.get("iconColor");
            if (iconColorObj instanceof String)
                recipe.iconColor = Color.valueOf((String) iconColorObj);
            Object fxObj = recipeMap.get("craftEffect");
            Effect fx = analyzeFx(fxObj);
            if (fx != null)
                recipe.craftEffect = fx;
            // Check empty
            if (!recipe.isAnyEmpty()) to.add(recipe);
            else Log.warn("Recipe is empty, so skip it", recipe);
        } catch (Exception e) {
            error("Can't load a recipe", e);
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

    @SuppressWarnings({"rawtypes"})
    public static IOEntry analyzeIOEntry(String meta, Object ioEntry) {
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
                    analyzeItems((List) items, res.items);
                } else if (items instanceof String) {
                    analyzeItemPair((String) items, res.items);
                } else if (items instanceof Map) {
                    analyzeItemMap((Map) items, res.items);
                } else throw new RecipeAnalyzerException("Unsupported type of items at " + meta + " from <" + items + ">");
            }
            // Fluids
            Object fluids = ioRawMap.get("fluids");
            if (fluids != null) {
                if (fluids instanceof List) { // ["mod-id-item/1","mod-id-item2"]
                    analyzeFluids((List) fluids, res.fluids);
                } else if (fluids instanceof String) {
                    analyzeFluidPair((String) fluids, res.fluids);
                } else if (fluids instanceof Map) {
                    analyzeFluidMap((Map) fluids, res.fluids);
                } else throw new RecipeAnalyzerException("Unsupported type of fluids at " + meta + " from <" + fluids + ">");
            }
            // power
            Object powerObj = ioRawMap.get("power");
            res.power = analyzeFloat(powerObj);
            Object heatObj = ioRawMap.get("heat");
            res.heat = analyzeFloat(heatObj);
            Object iconObj = ioRawMap.get("icon");
            if (iconObj instanceof String)
                res.icon = findIcon((String) iconObj);
            Object iconColorObj = ioRawMap.get("iconColor");
            if (iconColorObj instanceof String)
                res.iconColor = Color.valueOf((String) iconColorObj);
        } else if (ioEntry instanceof List) {
            /*
              input/output: []
             */
            for (Object content : (List) ioEntry) {
                if (content instanceof String) {
                    analyzeAnyPair((String) content, res.items, res.fluids);
                } else if (content instanceof Map) {
                    analyzeAnyMap((Map) content, res.items, res.fluids);
                } else throw new RecipeAnalyzerException("Unsupported type of content at " + meta + " from <" + content + ">");
            }
        } else if (ioEntry instanceof String) {
            /*
                input/output : "item/1"
             */
            analyzeAnyPair((String) ioEntry, res.items, res.fluids);
        } else throw new RecipeAnalyzerException("Unsupported type of " + meta + " <" + ioEntry + ">");
        return res;
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeItems(List items, Seq<ItemStack> to) {
        for (Object entryRaw : items) {
            if (entryRaw instanceof String) { // if the input is String as "mod-id-item/1"
                analyzeItemPair((String) entryRaw, to);
            } else if (entryRaw instanceof Map) {
                // if the input is Map as { item : "copper", amount : 1 }
                analyzeItemMap((Map) entryRaw, to);
            } else {
                error("Unsupported type of items <" + entryRaw + ">, so skip them");
            }
        }
    }

    public static void analyzeItemPair(String pair, Seq<ItemStack> to) {
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
            error("Can't analyze an item from <" + pair + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeFluids(List fluids, Seq<LiquidStack> to) {
        for (Object entryRaw : fluids) {
            if (entryRaw instanceof String) { // if the input is String as "mod-id-item/1"
                analyzeFluidPair((String) entryRaw, to);
            } else if (entryRaw instanceof Map) {
                // if the input is Map as { item : "water", amount : 1.2 }
                analyzeFluidMap((Map) entryRaw, to);
            } else {
                error("Unsupported type of fluids <" + entryRaw + ">, so skip them");
            }
        }
    }

    public static void analyzeFluidPair(String pair, Seq<LiquidStack> to) {
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
            error("Can't analyze a fluid from <" + pair + ">, so skip it", e);
        }
    }

    /**
     * @param pair "mod-id-item/1" or "mod-id-gas"
     */
    public static void analyzeAnyPair(String pair, Seq<ItemStack> items, Seq<LiquidStack> fluids) {
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
            error("Can't analyze this uncertain <" + pair + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeAnyMap(Map map, Seq<ItemStack> items, Seq<LiquidStack> fluids) {
        try {

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
            error("Can't find the corresponding item or fluid from <" + map + ">, so skip it");
        } catch (Exception e) {
            error("Can't analyze this uncertain <" + map + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeItemMap(Map map, Seq<ItemStack> to) {
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
            int amount = analyzeInt(map.get("amount"));
            entry.amount = amount;
            if (amount <= 0) {
                error("Item amount is +" + amount + " <=0, so reset as 1");
                entry.amount = 1;
            }
            to.add(entry);
        } catch (Exception e) {
            error("Can't analyze an item <" + map + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void analyzeFluidMap(Map map, Seq<LiquidStack> to) {
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
            float amount = analyzeFloat(map.get("amount"));
            entry.amount = amount;
            if (amount <= 0f) {
                error("Fluids amount is +" + amount + " <=0, so reset as 1.0f");
                entry.amount = 1f;
            }
            to.add(entry);
        } catch (Exception e) {
            error("Can't analyze <" + map + ">, so skip it", e);
        }
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

    /**
     * Only work on single threading.
     */
    public static void error(String content) {
        Log.err("[" + curBlock + "](at recipe " + index + ")\n" + content);
    }

    /**
     * Only work on single threading.
     */
    public static void error(String content, Throwable e) {
        Log.err("[" + curBlock + "](at recipe " + index + ")\n" + content, e);
    }

    public static String genName(Block meta) {
        if (meta.localizedName.equals(meta.name)) return meta.name;
        else return meta.localizedName + "(" + meta.name + ")";
    }

    @Nullable
    public static Item findItem(String id) {
        for (Item item : Vars.content.items())
            if (id.equals(item.name)) return item;// prevent null pointer
        return null;
    }

    @Nullable
    public static Liquid findFluid(String id) {
        for (Liquid fluid : Vars.content.liquids())
            if (id.equals(fluid.name)) return fluid;// prevent null pointer
        return null;
    }

    @Nullable
    public static Block findBlock(String id) {
        for (Block block : Vars.content.blocks())
            if (id.equals(block.name)) return block; // prevent null pointer
        return null;
    }

    @Nullable
    public static UnitType findUnit(String id) {
        for (UnitType unit : Vars.content.units())
            if (id.equals(unit.name)) return unit; // prevent null pointer
        return null;
    }

    @Nullable
    public static UnlockableContent findPayload(String id) {
        UnitType unit = findUnit(id);
        if (unit != null) return unit;
        return findBlock(id);
    }

    private static final Prov<TextureRegion> NotFound = () -> Icon.cancel.getRegion();

    /**
     * Supported name pattern:
     * <ul>
     *     <li> "Icon.xxx" from {@link Icon}
     *     <li> "copper", "water", "router" or "mono"
     * </ul>
     */
    @Nullable
    public static Prov<TextureRegion> findIcon(String name) {
        if (name.startsWith("Icon.") && name.length() > 5) {
            try {
                String fieldName = name.substring(5);
                Field field = Icon.class.getField(fieldName.contains("-") ? kebab2camel(fieldName) : fieldName);
                Object icon = field.get(null);
                TextureRegion tr = ((TextureRegionDrawable) icon).getRegion();
                return () -> tr;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                error("Icon <" + name + "> not found, so use a cross instead.", e);
                return NotFound;
            }
        } else {
            Item item = findItem(name);
            if (item != null) return () -> item.uiIcon;
            Liquid fluid = findFluid(name);
            if (fluid != null) return () -> fluid.uiIcon;
            UnlockableContent payload = findPayload(name);
            if (payload != null) return () -> payload.uiIcon;
            TextureRegion tr = Core.atlas.find(name);
            if (tr.found()) return () -> tr;
        }
        error("Texture <" + name + "> not found, so use a cross instead.");
        return NotFound;
    }

    public static String kebab2camel(String kebab) {
        StringBuilder sb = new StringBuilder();
        boolean hyphen = false;
        for (int i = 0; i < kebab.length(); i++) {
            char c = kebab.charAt(i);
            if (c == '-') {
                hyphen = true;
            } else {
                if (hyphen) {
                    sb.append(Character.toUpperCase(c));
                    hyphen = false;
                } else {
                    if (i == 0) sb.append(Character.toLowerCase(c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Effect analyzeFx(Object obj) {
        if (obj instanceof String)
            return findFx((String) obj);
        else if (obj instanceof List) {
            return composeMultiFx((List<String>) obj);
        } else return null;
    }

    @Nullable
    public static Effect findFx(String name) {
        Object effect = field(Fx.class, name);
        if (effect instanceof Effect) return (Effect) effect;
        else return null;
    }

    private static final Effect[] EffectType = new Effect[0];

    public static Effect composeMultiFx(List<String> names) {
        ArrayList<Effect> all = new ArrayList<>();
        for (String name : names) {
            Effect fx = findFx(name);
            if (fx != null) all.add(fx);
        }
        return new MultiEffect(all.toArray(EffectType));
    }

    public static Object field(Class<?> type, JsonValue value) {
        return field(type, value.asString());
    }

    /**
     * Gets a field from a static class by name, throwing a descriptive exception if not found.
     */
    public static Object field(Class<?> type, String name) {
        try {
            Object b = type.getField(name).get(null);
            if (b == null) throw new IllegalArgumentException(type.getSimpleName() + ": not found: '" + name + "'");
            return b;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    private static Object convert(JsonValue j) {
        JsonValue.ValueType type = j.type();
        switch (type) {
            case object: {
                HashMap map = new HashMap();
                for (JsonValue cur = j.child; cur != null; cur = cur.next) {
                    map.put(cur.name, convert(cur));
                }
                return map;
            }
            case array: {
                ArrayList list = new ArrayList();
                for (JsonValue cur = j.child; cur != null; cur = cur.next) {
                    list.add(convert(cur));
                }
                return list;
            }
            case stringValue:
                return j.asString();
            case doubleValue:
                return j.asDouble();
            case longValue:
                return j.asLong();
            case booleanValue:
                return j.asBoolean();
            case nullValue:
                return null;
        }
        return Collections.emptyMap();
    }
}
