# API

## MultiCrafter

The `MultiCrafter` works like a
normal [`GenericCrafter`](https://mindustrygame.github.io/wiki/modding/5-types/#genericcrafter),
so `MultiCrafter` has most of `GenericCrafter`'s API.

|          Field          |   Type    |              Default              |                                                      Note                                                       |
|:-----------------------:|:---------:|:---------------------------------:|:---------------------------------------------------------------------------------------------------------------:|
| itemCapacityMultiplier  |   float   |                1f                 |                                                                                                                 | 
| fluidCapacityMultiplier |   float   |                1f                 |                                                                                                                 |   
| powerCapacityMultiplier |   float   |                1f                 |                                                        ️                                                        |   
|         recipes         |  Object   |               null                |                                                        ️                                                        |   
|          menu           |  String   |             transform             |                                                        ️                                                        |   
|       craftEffect       |  Effect   |               none                |                                                        ️                                                        |   
|      updateEffect       |  Effect   |               none                |                                                        ️                                                        |   
|   changeRecipeEffect    |  Effect   |            upgradeCore            |                 when recipe is changed.                                                       ️                 |   
|  fluidOutputDirections  |   int[]   |               {-1}                | substitute for vanilla `liquidOutputDirections`                                                               ️ |   
|   updateEffectChance    |   float   |               0.04f               |                                                        ️                                                        |   
|       warmupSpeed       |   float   |              0.019f               |                                                        ️                                                        |   
|      powerCapacity      |   float   |                0f                 |                                                        ️                                                        |   
|     dumpExtraFluid      |  boolean  |               true                |                                                        ️                                                        |   
|        heatColor        |   Color   | new Color(1f, 0.22f, 0.22f, 0.8f) |         What color of heat for recipe selector.                                                       ️         |   
|         drawer          | DrawBlock |         new DrawDefault()         |                                                        ️                                                        |   

## Drawer

|      Drawer       |        Replacement        |
|:-----------------:|:-------------------------:|
|  DrawHeatRegion   | multicraft.DrawHeatRegion |
| DrawLiquidOutputs |      No replacement       |