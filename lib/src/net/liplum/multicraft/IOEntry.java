package net.liplum.multicraft;

import arc.struct.Seq;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;

public class IOEntry {
    public Seq<ItemStack> items = new Seq<>();
    public Seq<LiquidStack> fluids = new Seq<>();
    public float power = 0f;

    public boolean isEmpty() {
        return items.isEmpty() && fluids.isEmpty() && power <= 0f;
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

    @Override
    public String toString() {
        return "IOEntry{" +
                "items=" + items +
                "fluids=" + fluids +
                "power=" + power +
                "}";
    }
}
