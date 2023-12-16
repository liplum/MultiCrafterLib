package multicraft;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;

import mindustry.ctype.*;
import mindustry.type.*;

public class IOEntry {
    public Seq<ItemStack> items = new Seq<>(ItemStack.class);
    public Seq<LiquidStack> fluids = new Seq<>(LiquidStack.class);
    public float power = 0f;
    public float heat = 0f;
    public Seq<PayloadStack> payloads = new Seq<>(PayloadStack.class);

    public ObjectSet<Item> itemsUnique = new ObjectSet<>();
    public ObjectSet<Liquid> fluidsUnique = new ObjectSet<>();
    public ObjectSet<UnlockableContent> payloadUnique = new ObjectSet<>();
    @Nullable
    public Prov<TextureRegion> icon;
    @Nullable
    public Color iconColor;

    public IOEntry(Seq<ItemStack> items) {
        this(items, new Seq<>(), 0f, 0f, new Seq<>());
    }

    public IOEntry(Seq<ItemStack> items, Seq<LiquidStack> fluids) {
        this(items, fluids, 0f, 0f, new Seq<>());
    }

    public IOEntry(Seq<ItemStack> items, Seq<LiquidStack> fluids, float power) {
        this(items, fluids, power, 0f, new Seq<>());
    }

    public IOEntry(Seq<ItemStack> items, Seq<LiquidStack> fluids, float power, float heat) {
        this(items, fluids, power, heat, new Seq<>());
    }

    public IOEntry(Seq<ItemStack> items, Seq<LiquidStack> fluids, float power, float heat, Seq<PayloadStack> payloads) {
        this.items = items;
        this.fluids = fluids;
        this.power = power;
        this.heat = heat;
        this.payloads = payloads;
    }

    public IOEntry() {
    }

    public void cacheUnique() {
        for (ItemStack item : items) {
            itemsUnique.add(item.item);
        }
        for (LiquidStack fluid : fluids) {
            fluidsUnique.add(fluid.liquid);
        }
        for (PayloadStack payload : payloads) {
            // "item" can be any UnlockableContent (items, liquids, blocks, units)
            payloadUnique.add(payload.item);
        }
    }

    public void shrinkSize() {
        items.shrink();
        fluids.shrink();
        payloads.shrink();
    }

    public boolean isEmpty() {
        return items.isEmpty() && fluids.isEmpty() && power <= 0f && heat <= 0f && payloads.isEmpty();
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

    public float maxPayloadAmount() {
        float max = 0;
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
