package multicraft;

import arc.func.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

/** Copy of {@linkplain ConsumePayloadDynamic} that takes a PayloadStack[] */
public class CustomConsumePayloadDynamic extends Consume {
    public final Func<Building, PayloadStack[]> payloads;

    @SuppressWarnings("unchecked")
    public <T extends Building> CustomConsumePayloadDynamic(Func<T, PayloadStack[]> payloads) {
        this.payloads = (Func<Building, PayloadStack[]>)payloads;
    }
    
    @Override
    public float efficiency(Building build){
        float mult = multiplier.get(build);
        for(PayloadStack stack : payloads.get(build)){
            if(!build.getPayloads().contains(stack.item, Math.round(stack.amount * mult))){
                return 0f;
            }
        }
        return 1f;
    }

    @Override
    public void trigger(Building build){
        float mult = multiplier.get(build);
        for(PayloadStack stack : payloads.get(build)){
            build.getPayloads().remove(stack.item, Math.round(stack.amount * mult));
        }
    }

    @Override
    public void display(Stats stats){
        //needs to be implemented by the block itself, not enough info to display here
    }

    @Override
    public void build(Building build, Table table){
        PayloadStack[][] current = {payloads.get(build)};

        table.table(cont -> {
            table.update(() -> {
                if(current[0] != payloads.get(build)){
                    rebuild(build, cont);
                    current[0] = payloads.get(build);
                }
            });

            rebuild(build, cont);
        });
    }

    private void rebuild(Building build, Table table){
        PayloadSeq inv = build.getPayloads();
        PayloadStack[] pay = payloads.get(build);

        table.table(c -> {
            int i = 0;
            for(PayloadStack stack : pay){
                c.add(new ReqImage(new ItemImage(stack.item.uiIcon, Math.round(stack.amount * multiplier.get(build))),
                () -> inv.contains(stack.item, Math.round(stack.amount * multiplier.get(build))))).padRight(8);
                if(++i % 4 == 0) c.row();
            }
        }).left();
    }
}
