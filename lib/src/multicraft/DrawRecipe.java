package multicraft;

import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import multicraft.MultiCrafter.*;

public class DrawRecipe extends DrawBlock {
    public int defaultDrawer = 0;
    public DrawBlock[] drawers = {};

    public DrawRecipe() {
        super();
    }

    @Override
    public void getRegionsToOutline(Block block, Seq<TextureRegion> out) {
        if (0 <= defaultDrawer && defaultDrawer < drawers.length)
            drawers[defaultDrawer].getRegionsToOutline(block, out);
    }

    @Override
    public void draw(Building build) {
        if (build instanceof MultiCrafterBuild) {
            MultiCrafterBuild crafter = (MultiCrafterBuild) build;
            int i = crafter.curRecipeIndex;
            if (0 <= i && i < drawers.length)
                drawers[i].draw(build);
        } else {
            Draw.rect(build.block.region, build.x, build.y, build.drawrot());
        }
    }

    @Override
    public void drawLight(Building build) {
        if (build instanceof MultiCrafterBuild) {
            MultiCrafterBuild crafter = (MultiCrafterBuild) build;
            int i = crafter.curRecipeIndex;
            if (0 < i && i < drawers.length)
                drawers[i].drawLight(build);
        }
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        if (0 <= defaultDrawer && defaultDrawer < drawers.length)
            drawers[defaultDrawer].drawPlan(block, plan, list);
        else
            block.drawDefaultPlanRegion(plan, list);
    }

    @Override
    public void load(Block block) {
        for (DrawBlock drawer : drawers) {
            drawer.load(block);
        }
    }

    @Override
    public TextureRegion[] icons(Block block) {
        if (0 <= defaultDrawer && defaultDrawer < drawers.length)
            return drawers[defaultDrawer].icons(block);
        return new TextureRegion[]{block.region};
    }
}
