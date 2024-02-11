package multicraft;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.effect.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

import java.util.*;

import static multicraft.ContentResolver.*;
import static multicraft.ParserUtils.*;

public class MultiCrafterParser {
    private static final String[] inputAlias = {"input", "in", "i"};
    private static final String[] outputAlias = {"output", "out", "o"};

    private String curBlock = "";
    private int recipeIndex = 0;

    private final Seq<String> errors = new Seq<>();
    private final Seq<String> warnings = new Seq<>();

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
            // parse input
            Object inputsRaw = findValueByAlias(recipeMap, inputAlias);
            if (inputsRaw == null) {
                warn("Recipe has no input, please ensure it's expected.");
            }
            recipe.input = parseIOEntry("input", inputsRaw);
            // parse output
            Object outputsRaw = findValueByAlias(recipeMap, outputAlias);
            if (outputsRaw == null) {
                warn("Recipe has no output, please ensure it's expected");
            }
            recipe.output = parseIOEntry("output", outputsRaw);
            // parse craft time
            Object craftTimeObj = recipeMap.get("craftTime");
            recipe.craftTime = parseFloat(craftTimeObj);
            // parse icon
            Object iconObj = recipeMap.get("icon");
            if (iconObj instanceof String) {
                recipe.icon = findIcon((String) iconObj);
            }
            Object iconColorObj = recipeMap.get("iconColor");
            if (iconColorObj instanceof String) {
                recipe.iconColor = Color.valueOf((String) iconColorObj);
            }
            // parse fx
            Object fxObj = recipeMap.get("craftEffect");
            Effect fx = parseFx(fxObj);
            if (fx != null) {
                recipe.craftEffect = fx;
            }
            // Check empty
            if (recipe.input.isEmpty() && recipe.output.isEmpty()) {
                warn("Recipe is completely empty.");
            }
            to.add(recipe);
        } catch (Exception e) {
            error("Can't load a recipe", e);
        }
    }

    @SuppressWarnings({"rawtypes"})
    private IOEntry parseIOEntry(String meta, @Nullable Object ioEntry) {
        IOEntry res = new IOEntry();
        if (ioEntry == null) {
            return res;
        } else if (ioEntry instanceof Map) {
            /*
                input/output:{
                  items:[],
                  fluids:[],
                  power:0,
                  heat:0,
                  payloads:[],
                  icon: Icon.power,
                  iconColor: "#FFFFFF"
                }
             */
            Map ioRawMap = (Map) ioEntry;
            // Items
            Object items = ioRawMap.get("items");
            if (items != null) {
                if (items instanceof List) { // ["mod-id-item/1","mod-id-item2"]
                    parseItems((List) items, res);
                } else if (items instanceof String) {
                    parseItemPair((String) items, res);
                } else if (items instanceof Map) {
                    parseItemMap((Map) items, res);
                } else
                    throw new RecipeParserException("Unsupported type of items at " + meta + " from <" + items + ">");
            }
            // Fluids
            Object fluids = ioRawMap.get("fluids");
            if (fluids != null) {
                if (fluids instanceof List) { // ["mod-id-fluid/1","mod-id-fluid2"]
                    parseFluids((List) fluids, res);
                } else if (fluids instanceof String) {
                    parseFluidPair((String) fluids, res);
                } else if (fluids instanceof Map) {
                    parseFluidMap((Map) fluids, res);
                } else
                    throw new RecipeParserException("Unsupported type of fluids at " + meta + " from <" + fluids + ">");
            }
            // Power
            Object powerObj = ioRawMap.get("power");
            res.power = parseFloat(powerObj);
            // Heat
            Object heatObj = ioRawMap.get("heat");
            res.heat = parseFloat(heatObj);
            // Payloads
            Object payloads = ioRawMap.get("payloads");
            if (payloads != null) {
                if (payloads instanceof List) { // ["mod-id-payload/1","mod-id-payload2"]
                    parsePayloads((List) payloads, res);
                } else if (payloads instanceof String) {
                    parsePayloadPair((String) payloads, res);
                } else if (payloads instanceof Map) {
                    parsePayloadMap((Map) payloads, res);
                } else
                    throw new RecipeParserException("Unsupported type of payloads at " + meta + " from <" + payloads + ">");
            }

            // Icon
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
                    parseAnyPair((String) content, res);
                } else if (content instanceof Map) {
                    parseAnyMap((Map) content, res);
                } else {
                    throw new RecipeParserException("Unsupported type of content at " + meta + " from <" + content + ">");
                }
            }
        } else if (ioEntry instanceof String) {
            /*
                input/output : "item/1"
             */
            parseAnyPair((String) ioEntry, res);
        } else {
            throw new RecipeParserException("Unsupported type of " + meta + " <" + ioEntry + ">");
        }
        return res;
    }

    @SuppressWarnings("rawtypes")
    private void parseItems(List items, IOEntry res) {
        for (Object entryRaw : items) {
            if (entryRaw instanceof String) { // if the input is String as "mod-id-item/1"
                parseItemPair((String) entryRaw, res);
            } else if (entryRaw instanceof Map) {
                // if the input is Map as { item : "copper", amount : 1 }
                parseItemMap((Map) entryRaw, res);
            } else {
                error("Unsupported type of items <" + entryRaw + ">, so skip them");
            }
        }
    }

    private void parseItemPair(String pair, IOEntry res) {
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
            res.items = addItemStack(res.items, entry);
        } catch (Exception e) {
            error("Can't parse an item from <" + pair + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseFluids(List fluids, IOEntry res) {
        for (Object entryRaw : fluids) {
            if (entryRaw instanceof String) { // if the input is String as "mod-id-fluid/1"
                parseFluidPair((String) entryRaw, res);
            } else if (entryRaw instanceof Map) {
                // if the input is Map as { fluid : "water", amount : 1.2 }
                parseFluidMap((Map) entryRaw, res);
            } else {
                error("Unsupported type of fluids <" + entryRaw + ">, so skip them");
            }
        }
    }

    private void parseFluidPair(String pair, IOEntry res) {
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
            res.fluids = addLiquidStack(res.fluids, entry);
        } catch (Exception e) {
            error("Can't parse a fluid from <" + pair + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parsePayloads(List payloads, IOEntry res) {
        for (Object entryRaw : payloads) {
            if (entryRaw instanceof String) { // if the input is String as "mod-id-payload/1"
                parsePayloadPair((String) entryRaw, res);
            } else if (entryRaw instanceof Map) {
                // if the input is Map as { payload : "router", amount : 1 }
                parsePayloadMap((Map) entryRaw, res);
            } else {
                error("Unsupported type of items <" + entryRaw + ">, so skip them");
            }
        }
    }

    private void parsePayloadPair(String pair, IOEntry res) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }
            String payloadID = id2Amount[0];
            UnlockableContent payload = findPayload(payloadID);
            if (payload == null) {
                error("<" + payloadID + "> doesn't exist in all payloads, so skip this");
                return;
            }
            PayloadStack entry = new PayloadStack();
            entry.item = payload;
            if (id2Amount.length == 2) {
                String amountStr = id2Amount[1];
                entry.amount = Integer.parseInt(amountStr);// throw NumberFormatException
            } else {
                entry.amount = 1;
            }
            res.payloads = addPayloadStack(res.payloads, entry);
        } catch (Exception e) {
            error("Can't parse an item from <" + pair + ">, so skip it", e);
        }
    }

    /**
     * @param pair "mod-id-item/1" or "mod-id-gas"
     */
    private void parseAnyPair(String pair, IOEntry res) {
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
                ItemStack entry = new ItemStack(Items.copper, 1);
                entry.item = item;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Integer.parseInt(amountStr);// throw NumberFormatException
                }
                res.items = addItemStack(res.items, entry);
                return;
            }
            // Find in fluid
            Liquid fluid = findFluid(id);
            if (fluid != null) {
                LiquidStack entry = new LiquidStack(Liquids.water, 1f);
                entry.liquid = fluid;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Float.parseFloat(amountStr);// throw NumberFormatException
                }
                res.fluids = addLiquidStack(res.fluids, entry);
                return;
            }
            // Find in payload
            UnlockableContent payload = findPayload(id);
            if (payload != null) {
                PayloadStack entry = new PayloadStack(Blocks.router, 1);
                entry.item = payload;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Integer.parseInt(amountStr);// throw NumberFormatException
                }
                res.payloads = addPayloadStack(res.payloads, entry);
                return;
            }
            error("Can't find the corresponding item, fluid or payload from this <" + pair + ">, so skip it");
        } catch (Exception e) {
            error("Can't parse this uncertain <" + pair + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseAnyMap(Map map, IOEntry res) {
        try {
            Object itemRaw = map.get("item");
            if (itemRaw instanceof String) {
                Item item = findItem((String) itemRaw);
                if (item != null) {
                    ItemStack entry = new ItemStack();
                    entry.item = item;
                    Object amountRaw = map.get("amount");
                    entry.amount = parseInt(amountRaw);
                    res.items = addItemStack(res.items, entry);
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
                    res.fluids = addLiquidStack(res.fluids, entry);
                    return;
                }
            }
            Object payloadRaw = map.get("payload");
            if (payloadRaw instanceof String) {
                UnlockableContent payload = findPayload((String) payloadRaw);
                if (payload != null) {
                    PayloadStack entry = new PayloadStack();
                    entry.item = payload;
                    Object amountRaw = map.get("amount");
                    entry.amount = parseInt(amountRaw);
                    res.payloads = addPayloadStack(res.payloads, entry);
                    return;
                }
            }
            error("Can't find the corresponding item, fluid or payload from <" + map + ">, so skip it");
        } catch (Exception e) {
            error("Can't parse this uncertain <" + map + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseItemMap(Map map, IOEntry res) {
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
                error("Item amount is +" + amount + " <= 0, so reset as 1");
                entry.amount = 1;
            }
            res.items = addItemStack(res.items, entry);
        } catch (Exception e) {
            error("Can't parse an item <" + map + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseFluidMap(Map map, IOEntry res) {
        try {
            LiquidStack entry = new LiquidStack(Liquids.water, 0f);
            Object fluidID = map.get("fluid");
            if (fluidID instanceof String) {
                Liquid fluid = findFluid((String) fluidID);
                if (fluid == null) {
                    error(fluidID + " doesn't exist in all fluids, so skip this");
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
                error("Fluids amount is +" + amount + " <= 0, so reset as 1.0f");
                entry.amount = 1f;
            }
            res.fluids = addLiquidStack(res.fluids, entry);
        } catch (Exception e) {
            error("Can't parse <" + map + ">, so skip it", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parsePayloadMap(Map map, IOEntry res) {
        try {
            PayloadStack entry = new PayloadStack();
            Object payloadID = map.get("payload");
            if (payloadID instanceof String) {
                UnlockableContent payload = findPayload((String) payloadID);
                if (payload == null) {
                    error("<" + payloadID + "> doesn't exist in all payloads, so skip this");
                    return;
                }
                entry.item = payload;
            } else {
                error("Can't recognize a fluid from <" + map + ">");
                return;
            }
            int amount = parseInt(map.get("amount"));
            entry.amount = amount;
            if (amount <= 0) {
                error("Payload amount is +" + amount + " <= 0, so reset as 1");
                entry.amount = 1;
            }
            res.payloads = addPayloadStack(res.payloads, entry);
        } catch (Exception e) {
            error("Can't parse an item <" + map + ">, so skip it", e);
        }
    }

    
    private void error(String content) {
        error(content, null);
    }

    private void error(String content, @Nullable Throwable e) {
        String message = buildRecipeIndexInfo() + content;
        errors.add(message);
        if (e == null) {
            Log.err(message);
        } else {
            Log.err(message, e);
        }
    }

    private void warn(String content) {
        String message = buildRecipeIndexInfo() + content;
        warnings.add(message);
        Log.warn(message);
    }

    private String buildRecipeIndexInfo() {
        return "[" + curBlock + "](at recipe " + recipeIndex + ")\n";
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
        if (obj instanceof String) return findFx((String) obj);
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
