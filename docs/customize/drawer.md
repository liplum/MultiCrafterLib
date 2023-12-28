# DrawRecipe

`DrawRecipe` drawer let you draw different images for each recipe.

**Type:** multicraft.DrawRecipe

It looks like a `DrawMulti`, but the drawer will be changed once another recipe is selected.

|     Field     |    Type     | Default |                           Note                            |
|:-------------:|:-----------:|:-------:|:---------------------------------------------------------:|
|    drawers    | DrawBlock[] |   {}    |              ordered drawers for each recipe              | 
| defaultDrawer |     int     |    0    | the default drawer index in `drawers` for icon generation |

Suppose you have those sprites with a MultiCrafter, named `mine-crafter`.

```
sprites/
├─ blocks/
│  ├─ mine-crafter-1.png
│  ├─ mine-crafter-2.png
│  ├─ mine-crafter-3.png
```

|                             Sprite                              |     File Name      |              
|:---------------------------------------------------------------:|:------------------:|
| ![mine-crafter-1.png](../assets/test-drawer/mine-crafter-1.png) | mine-crafter-1.png |
| ![mine-crafter-2.png](../assets/test-drawer/mine-crafter-2.png) | mine-crafter-2.png |
| ![mine-crafter-3.png](../assets/test-drawer/mine-crafter-3.png) | mine-crafter-3.png |

![DrawRecipe example](../assets/draw-recipes.gif){ loading=lazy width="280" }

=== "HJSON"

    ```hjson
    drawer: {
      type: multicraft.DrawRecipe
      defaultDrawer: 0 // an index used for generating the icon of this crafter. 
      drawers: [
        // for recipe 0
        { 
        type: DrawMulti
        drawers: [
          {
              type: DrawRegion
              suffix: -1
          }
          {
              type: DrawArcSmelt
          }
        ]
        }
        // for recipe 1
        { 
          type: DrawRegion
          suffix: -2
        }
        // for recipe 2
        { 
          type: DrawRegion
          suffix: -3
        }
      ]
    }
    ```

=== "JSON"

    ```json
    "drawer": {
      "type": "multicraft.DrawRecipe",
      "drawers": [
        {
        "type": "DrawMulti",
        "drawers": [
          {
              "type": "DrawRegion",
              "suffix": "-1"
          },
          {
              "type": "DrawArcSmelt"
          },
        ]
        },
        {
          "type": "DrawRegion",
          "suffix":"-2"
        },
        {
          "type": "DrawRegion",
          "suffix": "-3"
        }
      ]
    }
    ```