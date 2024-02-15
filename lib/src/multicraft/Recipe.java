package multicraft;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;

public class Recipe {
    public IOEntry input;
    public IOEntry output;
    public float craftTime = 0f;
    @Nullable
    public Prov<TextureRegion> icon;
    @Nullable
    public Color iconColor;

    public Effect craftEffect = Fx.none;

    public Recipe() {}

    public void cacheUnique() {
        input.cacheUnique();
        output.cacheUnique();
    }

    public boolean isConsumeItem() {
        return input.items.length > 0;
    }

    public boolean isOutputItem() {
        return output.items.length > 0;
    }

    public boolean isConsumeFluid() {
        return input.fluids.length > 0;
    }

    public boolean isOutputFluid() {
        return output.fluids.length > 0;
    }

    public boolean isConsumePower() {
        return input.power > 0f;
    }

    public boolean isOutputPower() {
        return output.power > 0f;
    }

    public boolean isConsumeHeat() {
        return input.heat > 0f;
    }

    public boolean isOutputHeat() {
        return output.heat > 0f;
    }

    public boolean isConsumePayload() {
        return input.payloads.length > 0;
    }

    public boolean isOutputPayload() {
        return output.payloads.length > 0;
    }

    public boolean hasItems() {
        return isConsumeItem() || isOutputItem();
    }

    public boolean hasFluids() {
        return isConsumeFluid() || isOutputFluid();
    }

    public boolean hasPower() {
        return isConsumePower() || isOutputPower();
    }

    public boolean hasHeat() {
        return isConsumeHeat() || isOutputHeat();
    }

    public boolean hasPayloads() {
        return isConsumePayload() || isOutputPayload();
    }

    public int maxItemAmount() {
        return Math.max(input.maxItemAmount(), output.maxItemAmount());
    }

    public float maxFluidAmount() {
        return Math.max(input.maxFluidAmount(), output.maxFluidAmount());
    }

    public float maxPower() {
        return Math.max(input.power, output.power);
    }

    public float maxHeat() {
        return Math.max(input.heat, output.heat);
    }

    public int maxPayloadAmount() {
        return Math.max(input.maxPayloadAmount(), output.maxPayloadAmount());
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "input=" + input +
                "output=" + output +
                "craftTime" + craftTime +
                "}";
    }
}
