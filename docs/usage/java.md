# Java

In your Java file containing all your blocks (create one if you don't already have one) imports the MultiCrater library.

```java
import multicraft.*
```

Then create a new block with the type `MultiCrafter`

```java
Block mine-crafter = new MultiCrafter("mine-crafter") {{

}};
```

You can add recipes like this:

```java
resolvedRecipes = Seq.with(
    new Recipe() {{
        input = new IOEntry() {{
            items = ItemStack.with(
                Items.copper, 1,
                Items.lead, 1
            );
        }};
        output = new IOEntry() {{
            items = ItemStack.with(
                Items.surgeAlloy, 1,
                Items.thorium, 1
            );
        }};
        craftTime = 120f;
    }},
    new Recipe() {{
        input = new IOEntry() {{
            items = ItemStack.with(
                Items.copper, 1
            );
        }};
        output = new IOEntry() {{
            items = ItemStack.with(
                Items.copper, 1,
                Items.beryllium, 1
            );
        }};
        craftTime = 160f;
    }}
);
```

### Recipe

A recipe has several fields:

| Field       | Type                               | Note                                        |
|-------------|------------------------------------|---------------------------------------------|
| input       | IOEntry                            |                                             |
| output      | IOEntry                            |                                             |
| crafterTime | Float                              | how long to do a synthesis, can be 0.       |
| icon        | Prov<TextureRegion\>                | such as `Icon.lock-open`. See [Icon](#icon) |
| iconColor   | Color (RGB, RGBA, rgba8888 or Hex) | a color for icon                            |



### Input and Output

The `input` or `output` are `IOEntry`.
With this style, its power is unlimited.

| Key         | Type                               | Note                                            |
|-------------|------------------------------------|-------------------------------------------------|
| items       | ItemStack[]                        | how much item for input/output, default: empty  |
| fluids      | LiquidStack[]                      | how much fluid for input/output, default: empty |
| power       | Float &#124  unit: power/tick      | how much power for input/output, default: 0f    |
| heat        | Float                              | how much heat for input/output, default: 0f     |
| icon        | Icon                               | such as `Icon.lock-open`. See [Icon](#icon)     |
| iconColor   | Color (RGB, RGBA, rgba8888 or Hex) | a color for icon                                |
| craftEffect | Effect                             | an independent craft effect for each recipe     |

### Icon

You can customize which icon is used for your recipe selector menu.

If you don't set a dedicated icon, it will find the first one from the recipe.

For example:

=== "alphaaaa"

    ![Alphaaaa](../assets/customizedIcon-alphaaaa.png){ loading=lazy }

    <details>
    <summary>
    icon = Icon.alphaaaa
    <br>
    iconColor: F30000
    </summary>
    ```
    switchStyle = RecipeSwitchStyle.simple;
    resolvedRecipes = Seq.with(
        new Recipe() {{
            input = new IOEntry(){{
                fluids = Seq.with(
                    Liquids.ozone, 1.5f
                );
            }};
            output = new IOEntry() {{
                items = Seq.with(
                    Items.coal, 1
                )
                power = 2f;
                icon: alphaaaa
                iconColor = Color.valueOf("#F30000");
            }};
            craftTime = 250f;
        }},
        new Recipe() {{
            input = new IOEntry(){{
                items = Seq.with(
                    Items.copper, 1
                );
            }};
            output = new IOEntry() {{
                items = Seq.with(
                    Items.coal, 1
                )
                icon = () -> Icon.lock.uiIcon;
            }};
            craftTime = 120f;
        }}
    );
    ```
    </details>

=== "mono"

    ![Mono](../assets/customizedIcon-mono.png){ loading=lazy width="250" }

    <details>
    <summary>
    icon: mono
    </summary>
    ```java
    switchStyle = RecipeSwitchStyle.simple;
    resolvedRecipes = Seq.with(
        new Recipe() {{
            input = new IOEntry(){{
                items = Seq.with(
                    Items.copper, 1
                );
            }};
            output = new IOEntry() {{
                items = Seq.with(
                    Items.coal, 1
                )
            }};
            craftTime = 60f;
            icon = () -> UnitTypes.mono.uiIcon;
        }},
        new Recipe() {{
            input = new IOEntry(){{
                items = Seq.with(
                    Items.copper, 1
                );
            }};
            output = new IOEntry() {{
                fluids = Seq.with(
                    Liquid.ozone, 1f
                )
            }};
            craftTime = 60f;
        }}
    );
    ```
    </details>

- The `icon` variable as to be always defined by `icon = () -> ...;`
- For a built-in icon, it should start with `Icon.`, such as `Icon.lock-open` or `Icon.trash`.
- For an icon from item, fluid, unit or block, it should be the content `uiIcon`, such as `Units.mono.uiIcon`,`phase-heat.uiIcon`.
- For any texture, it should be its name, such as `your-mod-icon` or `alphaaaa`.

