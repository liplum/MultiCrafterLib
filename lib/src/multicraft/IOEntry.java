package multicraft;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;

import mindustry.ctype.*;
import mindustry.type.*;

public class IOEntry {
    public ItemStack[] items = ItemStack.empty;
    public LiquidStack[] fluids = LiquidStack.empty;
    public float power = 0f;
    public float heat = 0f;
    public PayloadStack[] payloads = {}; // Equivalent of empty

    public ObjectSet<Item> itemsUnique = new ObjectSet<>();
    public ObjectSet<Liquid> fluidsUnique = new ObjectSet<>();
    public ObjectSet<UnlockableContent> payloadsUnique = new ObjectSet<>();
    @Nullable
    public Prov<TextureRegion> icon;
    @Nullable
    public Color iconColor;

    public IOEntry() {}

    public void cacheUnique() {
        for (ItemStack item : items) {
            itemsUnique.add(item.item);
        }
        for (LiquidStack fluid : fluids) {
            fluidsUnique.add(fluid.liquid);
        }
        for (PayloadStack payload : payloads) {
            // "item" can be any UnlockableContent
            payloadsUnique.add(payload.item);
        }
    }

    public boolean isEmpty() {
        return items.length == 0 && fluids.length == 0
            && power <= 0f && heat <= 0f && payloads.length == 0;
    }

    public int maxItemAmount() {
        int max = 0;
        for (ItemStack item : items) {
            max = Math.max(item.amount, max);
        }
        return max;
    }

    public float maxFluidAmount() {
        float max = 0;
        for (LiquidStack fluid : fluids) {
            max = Math.max(fluid.amount, max);
        }
        return max;
    }

    public int maxPayloadAmount() {
        int max = 0;
        for (PayloadStack payload : payloads) {
            max = Math.max(payload.amount, max);
        }
        return max;
    }

    @Override
    public String toString() {
        return "IOEntry{" +
            "items=" + items +
            "fluids=" + fluids +
            "power=" + power +
            "heat=" + heat +
            "payloads=" + payloads +
            "}";
    }
}
