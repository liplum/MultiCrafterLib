package net.liplum.multicraft;

public class Recipe {
    public IOEntry input;
    public IOEntry output;
    public float craftTime = 0f;
    public float powerOutputDuration = 0f;

    public boolean isEmpty() {
        if (input == null || output == null) return true;
        return input.isEmpty() || output.isEmpty();
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

    @Override
    public String toString() {
        return "Recipe{" +
                "input=" + input +
                "output=" + output +
                "craftTime" + craftTime +
                ((output != null && output.power > 0f) ? "powerOutputDuration=" + powerOutputDuration : "") +
                "}";
    }
}
