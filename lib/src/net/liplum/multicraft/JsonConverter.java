package net.liplum.multicraft;

import arc.util.Nullable;
import arc.util.serialization.JsonValue;
import arc.util.serialization.JsonValue.ValueType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class JsonConverter {
    public static Object convert(JsonValue j) {
        return convertFunc(j);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    private static Object convertFunc(JsonValue j) {
        ValueType type = j.type();
        switch (type) {
            case object: {
                HashMap map = new HashMap();
                for (JsonValue cur = j.child; cur != null; cur = cur.next) {
                    map.put(cur.name, convertFunc(cur));
                }
                return map;
            }
            case array: {
                ArrayList list = new ArrayList();
                for (JsonValue cur = j.child; cur != null; cur = cur.next) {
                    list.add(convertFunc(cur));
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
