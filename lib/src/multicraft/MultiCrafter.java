package multicraft;

import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.struct.EnumSet;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.ui.ItemImage;
import mindustry.world.Block;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.heat.HeatConductor.*;
import mindustry.world.blocks.power.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.*;

import multicraft.ui.*;

import java.util.*;

import static mindustry.Vars.tilesize;

public class MultiCrafter extends Block {
    public float itemCapacityMultiplier = 1f;
    public float fluidCapacityMultiplier = 1f;
    public float powerCapacityMultiplier = 1f;
    /*
    [ ==> Seq
      { ==> ObjectMap
        // String ==> Any --- Value may be a Seq, Map or String
        input:{
          // String ==> Any --- Value may be a Seq<String>, Seq<Map>, String or Map
          items:["mod-id-item/1","mod-id-item2/1"],
          fluids:["mod-id-liquid/10.5","mod-id-gas/10"]
          power: 3 pre tick
        },
        output:{
          items:["mod-id-item/1","mod-id-item2/1"],
          fluids:["mod-id-liquid/10.5","mod-id-gas/10"]
          heat: 10
        },
        craftTime: 120
      }
    ]
     */
    /**
     * For Json and Javascript to configure
     */
    public Object recipes;
    /**
     * The resolved recipes.
     */
    @Nullable
    public Seq<Recipe> resolvedRecipes = null;
    public String menu = "transform";
    @Nullable
    public RecipeSelector selector = null;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public Effect changeRecipeEffect = Fx.rotateBlock;
    public int[] fluidOutputDirections = {-1};
    public float updateEffectChance = 0.04f;
    public float warmupSpeed = 0.019f;
    /**
     * Whether stop production when the fluid is full.
     * Turn off this to ignore fluid output, for instance, the fluid is only by-product.
     */
    public boolean ignoreLiquidFullness = false;
    /**
     * If true, the crafter with multiple fluid outputs will dump excess,
     * when there's still space for at least one fluid type.
     */
    public boolean dumpExtraFluid = true;
    public DrawBlock drawer = new DrawDefault();

    protected boolean isOutputItem = false;
    protected boolean isConsumeItem = false;
    protected boolean isOutputFluid = false;
    protected boolean isConsumeFluid = false;
    protected boolean isOutputPower = false;
    protected boolean isConsumePower = false;
    protected boolean isOutputHeat = false;
    protected boolean isConsumeHeat = false;
    /**
     * What color of heat for recipe selector.
     */
    public Color heatColor = new Color(1f, 0.22f, 0.22f, 0.8f);
    public float powerCapacity = 0f;

    /**
     * For {@linkplain HeatConsumer},
     * it's used to display something of block or initialize the recipe index.
     */
    public int defaultRecipeIndex = 0;
    /**
     * For {@linkplain HeatConsumer},
     * after heat meets this requirement, excess heat will be scaled by this number.
     */
    public float overheatScale = 1f;
    /**
     * For {@linkplain HeatConsumer},
     * maximum possible efficiency after overheat.
     */
    public float maxEfficiency = 1f;
    /**
     * For {@linkplain HeatBlock}
     */
    public float warmupRate = 0.15f;
    /**
     * Whether to show name tooltip in {@link MultiCrafterBuild#buildStats(Table)}
     */
    protected boolean showNameTooltip = false;

    public MultiCrafter(String name) {
        super(name);
        update = true;
        solid = true;
        sync = true;
        flags = EnumSet.of(BlockFlag.factory);
        ambientSound = Sounds.machine;
        configurable = true;
        saveConfig = true;
        ambientSoundVolume = 0.03f;
        config(Integer.class, MultiCrafterBuild::setCurRecipeIndexFromRemote);
        Log.info("MultiCrafter[" + this.name + "] loaded.");
    }

    @Override
    public void init() {
        hasItems = false;
        hasPower = false;
        hasLiquids = false;
        outputsPower = false;
        // if the recipe is already set in another way, don't analyze it again.
        if (resolvedRecipes == null && recipes != null) resolvedRecipes = MultiCrafterAnalyzer.analyze(this, recipes);
        if (resolvedRecipes == null || resolvedRecipes.isEmpty())
            throw new ArcRuntimeException(MultiCrafterAnalyzer.genName(this) + " has no recipe! It's perhaps because all recipes didn't find items or fluids they need. Check your `last_log.txt` to obtain more information.");
        if (selector == null) selector = RecipeSelector.get(menu);
        decorateRecipes();
        setupBlockByRecipes();
        defaultRecipeIndex = Mathf.clamp(defaultRecipeIndex, 0, resolvedRecipes.size - 1);
        recipes = null; // free the recipe Seq, it's useless now.
        setupConsumers();
        super.init();
    }

    @Nullable
    protected static Table hoveredInfo;

    public class MultiCrafterBuild extends Building implements HeatBlock, HeatConsumer {
        /**
         * For {@linkplain HeatConsumer}, only enabled when the multicrafter requires heat input
         */
        public float[] sideHeat = new float[4];
        /**
         * For {@linkplain HeatConsumer} and {@linkplain HeatBlock},
         * only enabled when the multicrafter requires heat as input or can output heat.
         * Serialized
         */
        public float heat = 0f;
        /**
         * Serialized
         */
        public float craftingTime;
        /**
         * Serialized
         */
        public float warmup;
        /**
         * Serialized
         */
        public int curRecipeIndex = defaultRecipeIndex;

        public void setCurRecipeIndexFromRemote(int index) {
            int newIndex = Mathf.clamp(index, 0, resolvedRecipes.size - 1);
            if (newIndex != curRecipeIndex) {
                curRecipeIndex = newIndex;
                createEffect(changeRecipeEffect);
                craftingTime = 0f;
                if (!Vars.headless) rebuildHoveredInfo();
            }
        }

        public Recipe getCurRecipe() {
            // Prevent out of bound
            curRecipeIndex = Mathf.clamp(curRecipeIndex, 0, resolvedRecipes.size - 1);
            return resolvedRecipes.get(curRecipeIndex);
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return hasItems &&
                getCurRecipe().input.itemsUnique.contains(item) &&
                items.get(item) < getMaximumAccepted(item);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return hasLiquids &&
                getCurRecipe().input.fluidsUnique.contains(liquid) &&
                liquids.get(liquid) < liquidCapacity;
        }
        
        @Override
        public float edelta() {
            Recipe cur = getCurRecipe();
            if (cur.input.power > 0f) return this.efficiency *
                Mathf.clamp(getCurPowerStore() / cur.input.power) *
                this.delta();
            else return this.efficiency * this.delta();
        }

        @Override
        public void updateTile() {
            Recipe cur = getCurRecipe();
            float craftTimeNeed = cur.craftTime;
            // As HeatConsumer
            if (cur.isConsumeHeat()) heat = calculateHeat(sideHeat);
            if (cur.isOutputHeat()) {
                float heatOutput = cur.output.heat;
                heat = Mathf.approachDelta(heat, heatOutput * efficiency, warmupRate * edelta());
            }
            // cool down
            if (efficiency > 0 && (!hasPower || getCurPowerStore() >= cur.input.power)) {
                // if <= 0, instantly produced
                if (craftTimeNeed > 0f) craftingTime += edelta();
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);
                if (hasPower) {
                    float powerChange = (cur.output.power - cur.input.power) * delta();
                    if (!Mathf.zero(powerChange))
                        setCurPowerStore((getCurPowerStore() + powerChange));
                }

                //continuously output fluid based on efficiency
                if (cur.isOutputFluid()) {
                    float increment = getProgressIncrease(1f);
                    for (LiquidStack output : cur.output.fluids) {
                        Liquid fluid = output.liquid;
                        handleLiquid(this, fluid, Math.min(output.amount * increment, liquidCapacity - liquids.get(fluid)));
                    }
                }
                // particle fx
                if (wasVisible && Mathf.chanceDelta(updateEffectChance))
                    updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
            } else warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);

            if (craftTimeNeed <= 0f) {
                if (efficiency > 0f)
                    craft();
            } else if (craftingTime >= craftTimeNeed)
                craft();

            updateBars();
            dumpOutputs();
        }

        public void updateBars() {
            barMap.clear();
            setBars();
        }

        @Override
        public boolean shouldConsume() {
            Recipe cur = getCurRecipe();
            if (hasItems) for (ItemStack output : cur.output.items) if (items.get(output.item) + output.amount > itemCapacity) return false;

            if (hasLiquids) if (cur.isOutputFluid() && !ignoreLiquidFullness) {
                boolean allFull = true;
                for (LiquidStack output : cur.output.fluids)
                    if (liquids.get(output.liquid) >= liquidCapacity - 0.001f) {
                        if (!dumpExtraFluid) return false;
                    } else
                        allFull = false; //if there's still space left, it's not full for all fluids

                //if there is no space left for any fluid, it can't reproduce
                if (allFull) return false;
            }
            return enabled;
        }

        public void craft() {
            consume();
            Recipe cur = getCurRecipe();
            if (cur.isOutputItem()) for (ItemStack output : cur.output.items) for (int i = 0; i < output.amount; i++) offload(output.item);

            if (wasVisible) createCraftEffect();
            if (cur.craftTime > 0f)
                craftingTime %= cur.craftTime;
            else
                craftingTime = 0f;
        }

        public void createCraftEffect() {
            Recipe cur = getCurRecipe();
            Effect curFx = cur.craftEffect;
            Effect fx = curFx != Fx.none ? curFx : craftEffect;
            createEffect(fx);
        }

        public void dumpOutputs() {
            Recipe cur = getCurRecipe();
            if (cur.isOutputItem() && timer(timerDump, dumpTime / timeScale)) for (ItemStack output : cur.output.items) dump(output.item);

            if (cur.isOutputFluid()) {
                Seq<LiquidStack> fluids = cur.output.fluids;
                for (int i = 0; i < fluids.size; i++) {
                    int dir = fluidOutputDirections.length > i ? fluidOutputDirections[i] : -1;
                    dumpLiquid(fluids.get(i).liquid, 2f, dir);
                }
            }
        }

        /**
         * As {@linkplain HeatBlock}
         */
        @Override
        public float heat() {
            return heat;
        }

        /**
         * As {@linkplain HeatBlock}
         */
        @Override
        public float heatFrac() {
            Recipe cur = getCurRecipe();
            if (isOutputHeat && cur.isOutputHeat()) return heat / cur.output.heat;
            else if (isConsumeHeat && cur.isConsumeHeat()) return heat / cur.input.heat;
            return 0f;
        }

        /**
         * As {@linkplain HeatConsumer}
         * Only for visual effects
         */
        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        /**
         * As {@linkplain HeatConsumer}
         * Only for visual effects
         */
        @Override
        public float heatRequirement() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) return cur.input.heat;
            return 0f;
        }

        @Override
        public float calculateHeat(float[] sideHeat) {
            Point2[] edges = this.block.getEdges();
            int length = edges.length;
            for (int i=0; i < length; ++i) {
                Point2 edge = edges[i];
                Building build = this.nearby(edge.x, edge.y);
                if (build != null && build.team == this.team && build instanceof HeatBlock) {
                    HeatBlock heater = (HeatBlock)build;
                    // Only calculate heat if the block is a heater or a multicrafter heat output
                    if (heater instanceof MultiCrafterBuild) {
                        MultiCrafterBuild multi = (MultiCrafterBuild)heater;
                        if (multi.getCurRecipe().isOutputHeat())
                            return this.calculateHeat(sideHeat, (IntSet)null);
                    } else return this.calculateHeat(sideHeat, (IntSet)null);
                }
            }

            return 0.0f;
        }

        @Override
        public float getPowerProduction() {
            Recipe cur = getCurRecipe();

            if (isOutputPower && cur.isOutputPower()) return cur.output.power * efficiency;
            else return 0f;
        }

        @Override
        public void buildConfiguration(Table table) {
            selector.build(MultiCrafter.this, this, table);
        }

        public float getCurPowerStore() {
            if (power == null) return 0f;
            return power.status * powerCapacity;
        }

        public void setCurPowerStore(float powerStore) {
            if (power == null) return;
            power.status = Mathf.clamp(powerStore / powerCapacity);
        }

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public Object config() {
            return curRecipeIndex;
        }

        @Override
        public boolean shouldAmbientSound() {
            return efficiency > 0;
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress) return progress();
            if (sensor == LAccess.heat) return warmup();
            //attempt to prevent wild total fluid fluctuation, at least for crafter
            //if(sensor == LAccess.totalLiquids && outputLiquid != null) return liquids.get(outputLiquid.liquid);
            return super.sense(sensor);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(craftingTime);
            write.f(warmup);
            write.i(curRecipeIndex);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            craftingTime = read.f();
            warmup = read.f();
            curRecipeIndex = Mathf.clamp(read.i(), 0, resolvedRecipes.size - 1);
            heat = read.f();
        }

        public float warmupTarget() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) return Mathf.clamp(heat / cur.input.heat);
            else return 1f;
        }

        @Override
        public void updateEfficiencyMultiplier() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) {
                efficiency *= efficiencyScale();
                potentialEfficiency *= efficiencyScale();
            }
        }

        public float efficiencyScale() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) {
                float heatRequirement = cur.input.heat;
                float over = Math.max(heat - heatRequirement, 0f);
                return Math.min(Mathf.clamp(heat / heatRequirement) + over / heatRequirement * overheatScale, maxEfficiency);
            } else return 1f;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float progress() {
            Recipe cur = getCurRecipe();
            return Mathf.clamp(cur.craftTime > 0f ? craftingTime / cur.craftTime : 1f);
        }

        @Override
        public void display(Table table) {
            super.display(table);
            hoveredInfo = table;
        }

        public void rebuildHoveredInfo() {
            try {
                Table info = hoveredInfo;
                if (info != null) {
                    info.clear();
                    display(info);
                }
            } catch (Exception ignored) {
                // Maybe null pointer or cast exception
            }
        }

        public void createEffect(Effect effect) {
            if (effect == Fx.none) return;
            if (effect == Fx.placeBlock) effect.at(x, y, block.size);
            else if (effect == Fx.coreBuildBlock) effect.at(x, y, 0f, block);
            else if (effect == Fx.upgradeCore) effect.at(x, y, 0f, block);
            else if (effect == Fx.upgradeCoreBloom) effect.at(x, y, block.size);
            else if (effect == Fx.rotateBlock) effect.at(x, y, block.size);
            else effect.at(x, y, 0, this);
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.output, t -> {
            showNameTooltip = true;
            buildStats(t);
            showNameTooltip = false;
        });
    }

    public void buildStats(Table stat) {
        stat.row();
        for (Recipe recipe : resolvedRecipes) {
            Table t = new Table();
            t.background(Tex.whiteui);
            t.setColor(Pal.darkestGray);
            // Input
            buildIOEntry(t, recipe, true);
            // Time
            Table time = new Table();
            final float[] duration = {0f};
            float visualCraftTime = recipe.craftTime;
            time.update(() -> {
                duration[0] += Time.delta;
                if (duration[0] > visualCraftTime) duration[0] = 0f;
            });
            String craftTime = recipe.craftTime == 0 ? "0" : String.format("%.2f", recipe.craftTime / 60f);
            Cell<Bar> barCell = time.add(new Bar(() -> craftTime,
                    () -> Pal.accent,
                    () -> Interp.smooth.apply(duration[0] / visualCraftTime)))
                .height(45f);
            barCell.width(Vars.mobile ? 220f : 250f);
            Cell<Table> timeCell = t.add(time).pad(12f);
            if (showNameTooltip) timeCell.tooltip(Stat.productionTime.localized() + ": " + craftTime + " " + StatUnit.seconds.localized());
            // Output
            buildIOEntry(t, recipe, false);
            stat.add(t).pad(10f).grow();
            stat.row();
        }
        stat.row();
        stat.defaults().grow();
    }

    protected void buildIOEntry(Table table, Recipe recipe, boolean isInput) {
        Table t = new Table();
        if (isInput) t.left();
        else t.right();
        Table mat = new Table();
        IOEntry entry = isInput ? recipe.input : recipe.output;
        int i = 0;
        for (ItemStack stack : entry.items) {
            Cell<ItemImage> iconCell = mat.add(new ItemImage(stack.item.uiIcon, stack.amount))
                .pad(2f);
            if (showNameTooltip)
                iconCell.tooltip(stack.item.localizedName);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (i != 0 && i % 2 == 0) mat.row();
            i++;
        }
        for (LiquidStack stack : entry.fluids) {
            Cell<FluidImage> iconCell = mat.add(new FluidImage(stack.liquid.uiIcon, stack.amount * 60f))
                .pad(2f);
            if (showNameTooltip)
                iconCell.tooltip(stack.liquid.localizedName);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (i != 0 && i % 2 == 0) mat.row();
            i++;
        }
        // No redundant ui
        // Power
        if (entry.power > 0f) {
            Cell<PowerImage> iconCell = mat.add(new PowerImage(entry.power * 60f))
                .pad(2f);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (showNameTooltip)
                iconCell.tooltip(entry.power + " " + StatUnit.powerSecond.localized());
            i++;
            if (i != 0 && i % 2 == 0) mat.row();
        }
        //Heat
        if (entry.heat > 0f) {
            Cell<HeatImage> iconCell = mat.add(new HeatImage(entry.heat))
                .pad(2f);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (showNameTooltip)
                iconCell.tooltip(entry.heat + " " + StatUnit.heatUnits.localized());
            i++;
            if (i != 0 && i % 2 == 0) mat.row();
        }
        Cell<Table> matCell = t.add(mat);
        if (isInput) matCell.left();
        else matCell.right();
        Cell<Table> tCell = table.add(t).pad(12f).fill();
        tCell.width(Vars.mobile ? 90f : 120f);
    }

    @Override
    public void setBars() {
        super.setBars();

        if (hasPower) 
            addBar("power", (MultiCrafterBuild b) -> new Bar(
                b.getCurRecipe().isOutputPower() ? Core.bundle.format("bar.poweroutput", Strings.fixed(b.getPowerProduction() * 60f * b.timeScale(), 1)) : "bar.power",
                Pal.powerBar,
                () -> b.efficiency
            ));
        if (isConsumeHeat || isOutputHeat) 
            addBar("heat", (MultiCrafterBuild b) -> new Bar(
                b.getCurRecipe().isConsumeHeat() ? Core.bundle.format("bar.heatpercent", (int) (b.heat + 0.01f), (int) (b.efficiencyScale() * 100 + 0.01f)) : "bar.heat",
                Pal.lightOrange,
                b::heatFrac
            ));
        addBar("progress", (MultiCrafterBuild b) -> new Bar(
            "bar.loadprogress",
            Pal.accent,
            b::progress
        ));
    }

    @Override
    public boolean rotatedOutput(int x, int y) {
        return false;
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
    }

    @Override
    public boolean outputsItems() {
        return isOutputItem;
    }

    @Override
    public void drawOverlay(float x, float y, int rotation) {
        Recipe firstRecipe = resolvedRecipes.get(defaultRecipeIndex);
        Seq<LiquidStack> fluids = firstRecipe.output.fluids;
        for (int i = 0; i < fluids.size; i++) {
            int dir = fluidOutputDirections.length > i ? fluidOutputDirections[i] : -1;

            if (dir != -1) Draw.rect(
                fluids.get(i).liquid.fullIcon,
                x + Geometry.d4x(dir + rotation) * (size * tilesize / 2f + 4),
                y + Geometry.d4y(dir + rotation) * (size * tilesize / 2f + 4),
                8f, 8f
            );
        }
    }

    protected void decorateRecipes() {
        resolvedRecipes.shrink();
        for (Recipe recipe : resolvedRecipes) {
            recipe.shrinkSize();
            recipe.cacheUnique();
        }
    }

    protected void setupBlockByRecipes() {
        int maxItemAmount = 0;
        float maxFluidAmount = 0f;
        float maxPower = 0f;
        float maxHeat = 0f;
        for (Recipe recipe : resolvedRecipes) {
            maxItemAmount = Math.max(recipe.maxItemAmount(), maxItemAmount);
            maxFluidAmount = Math.max(recipe.maxFluidAmount(), maxFluidAmount);
            maxPower = Math.max(recipe.maxPower(), maxPower);
            maxHeat = Math.max(recipe.maxHeat(), maxHeat);
            hasItems |= recipe.hasItem();
            hasLiquids |= recipe.hasFluid();
            hasPower |= recipe.hasPower();
            isOutputItem |= recipe.isOutputItem();
            isConsumeItem |= recipe.isConsumeItem();
            isOutputFluid |= recipe.isOutputFluid();
            isConsumeFluid |= recipe.isConsumeFluid();
            isOutputPower |= recipe.isOutputPower();
            isConsumePower |= recipe.isConsumePower();
            isOutputHeat |= recipe.isOutputHeat();
            isConsumeHeat |= recipe.isConsumeHeat();
        }
        outputsPower = isOutputPower;
        consumesPower = isConsumePower;
        itemCapacity = Math.max((int) (maxItemAmount * itemCapacityMultiplier), itemCapacity);
        liquidCapacity = Math.max((int) (maxFluidAmount * 60f * fluidCapacityMultiplier), liquidCapacity);
        powerCapacity = Math.max(maxPower * 60f * powerCapacityMultiplier, powerCapacity);
        if (isOutputHeat) {
            rotate = true;
            rotateDraw = false;
            canOverdrive = false;
            drawArrow = true;
        }
    }

    protected void setupConsumers() {
        if (isConsumeItem) consume(new ConsumeItemDynamic(
            // items seq is already shrunk, it's safe to access
            (MultiCrafterBuild b) -> b.getCurRecipe().input.items.items
        ));
        if (isConsumeFluid) consume(new ConsumeFluidDynamic(
            // fluids seq is already shrunk, it's safe to access
            (MultiCrafterBuild b) -> b.getCurRecipe().input.fluids.items
        ));
        if (isConsumePower) consume(new ConsumePowerDynamic(b ->
            ((MultiCrafterBuild)b).getCurRecipe().input.power
        ));
    }
}
