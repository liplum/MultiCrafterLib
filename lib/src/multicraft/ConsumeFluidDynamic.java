package multicraft;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.type.LiquidStack;
import mindustry.ui.ReqImage;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;
import mindustry.world.modules.LiquidModule;

import multicraft.ui.*;

public class ConsumeFluidDynamic extends Consume {
    public final Func<Building, LiquidStack[]> fluids;

    @SuppressWarnings("unchecked")
    public <T extends Building> ConsumeFluidDynamic(Func<T, LiquidStack[]> fluids) {
        this.fluids = (Func<Building, LiquidStack[]>) fluids;
    }

    @Override
    public void apply(Block block) {
        block.hasLiquids = true;
    }

    @Override
    public void update(Building build) {
        LiquidStack[] fluids = this.fluids.get(build);
        remove(build.liquids, fluids, build.edelta());
    }

    @Override
    public void build(Building build, Table table) {
        final LiquidStack[][] current = {fluids.get(build)};

        table.table(cont -> {
            table.update(() -> {
                LiquidStack[] newFluids = fluids.get(build);
                if (current[0] != newFluids) {
                    rebuild(build, cont);
                    current[0] = newFluids;
                }
            });

            rebuild(build, cont);
        });
    }

    private void rebuild(Building tile, Table table) {
        table.clear();
        int i = 0;

        LiquidStack[] fluids = this.fluids.get(tile);
        for (LiquidStack stack : fluids) {
            table.add(new ReqImage(new FluidImage(stack.liquid.uiIcon),
                () -> tile.liquids != null && tile.liquids.get(stack.liquid) >= stack.amount)).padRight(8).left();
            if (++i % 4 == 0) table.row();
        }
    }

    @Override
    public float efficiency(Building build) {
        LiquidStack[] fluids = this.fluids.get(build);
        return build.consumeTriggerValid() || has(build.liquids, fluids) ? 1f : 0f;
    }
    public static boolean has(LiquidModule fluids, LiquidStack[] reqs) {
        for (LiquidStack req : reqs) {
            if (fluids.get(req.liquid) < req.amount)
                return false;
        }
        return true;
    }

    public static void remove(LiquidModule fluids, LiquidStack[] reqs, float multiplier) {
        for (LiquidStack req : reqs) {
            fluids.remove(req.liquid, req.amount * multiplier);
        }
    }
}
