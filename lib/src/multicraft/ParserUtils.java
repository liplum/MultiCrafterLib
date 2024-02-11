package multicraft;

import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import mindustry.type.*;

import java.util.*;

public class ParserUtils {

    public static float parseFloat(@Nullable Object floatObj) {
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

    public static int parseInt(@Nullable Object intObj) {
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object parseJsonToObject(Object o) {
        if (o instanceof Seq) {
            Seq seq = (Seq) o;
            ArrayList list = new ArrayList(seq.size);
            for (Object e : new Seq.SeqIterable<>(seq)) {
                list.add(parseJsonToObject(e));
            }
            return list;
        } else if (o instanceof ObjectMap) {
            ObjectMap objMap = (ObjectMap) o;
            HashMap map = new HashMap();
            for (ObjectMap.Entry<Object, Object> entry : new ObjectMap.Entries<Object, Object>(objMap)) {
                map.put(entry.key, parseJsonToObject(entry.value));
            }
            return map;
        } else if (o instanceof JsonValue) {
            return convert((JsonValue) o);
        }
        return o;
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

    public static ItemStack[] addItemStack(ItemStack[] stackArray, ItemStack stack) {
        ArrayList<ItemStack> newItemStack = new ArrayList<ItemStack>(Arrays.asList(stackArray));
        newItemStack.add(stack);
        return newItemStack.toArray(stackArray);
    }

    public static LiquidStack[] addLiquidStack(LiquidStack[] stackArray, LiquidStack stack) {
        ArrayList<LiquidStack> newLiquidStack = new ArrayList<LiquidStack>(Arrays.asList(stackArray));
        newLiquidStack.add(stack);
        return newLiquidStack.toArray(stackArray);
    }

    public static PayloadStack[] addPayloadStack(PayloadStack[] stackArray, PayloadStack stack) {
        ArrayList<PayloadStack> newPayloadStack = new ArrayList<PayloadStack>(Arrays.asList(stackArray));
        newPayloadStack.add(stack);
        return newPayloadStack.toArray(stackArray);
    }
}
