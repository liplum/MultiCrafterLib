package multicraft;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.draw.DrawBlock;

/**
 * {@link mindustry.world.draw.DrawHeatRegion}
 */
public class DrawHeatRegion extends DrawBlock {
    public Color color = new Color(1f, 0.22f, 0.22f, 0.8f);
    public float pulse = 0.3f, pulseScl = 10f;

    public TextureRegion heat;
    public String suffix = "-glow";

    public DrawHeatRegion(String suffix) {
        this.suffix = suffix;
    }

    public DrawHeatRegion() {
    }

    @Override
    public void draw(Building build) {
        Draw.z(Layer.blockAdditive);
        if (build instanceof HeatBlock && build instanceof HeatConsumer) {
            HeatBlock hb = (HeatBlock) build;
            HeatConsumer hc = (HeatConsumer) build;
            if (hb.heat() > 0) {
                Draw.blend(Blending.additive);
                Draw.color(color, Mathf.clamp(hb.heat() / hc.heatRequirement()) * (color.a * (1f - pulse + Mathf.absin(pulseScl, pulse))));
                Draw.rect(heat, build.x, build.y);
                Draw.blend();
                Draw.color();
            }
        }
        Draw.z(Layer.block);
    }

    @Override
    public void load(Block block) {
        heat = Core.atlas.find(block.name + suffix);
    }
}
