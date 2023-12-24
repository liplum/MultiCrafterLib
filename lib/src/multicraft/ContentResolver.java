package multicraft;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

import java.lang.reflect.*;

import static multicraft.ParserUtils.*;

public class ContentResolver {

    @Nullable
    public static Effect findFx(String name) {
        Object effect = field(Fx.class, name);
        if (effect instanceof Effect) return (Effect) effect;
        else return null;
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
                return null;
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
        return null;
    }
}
