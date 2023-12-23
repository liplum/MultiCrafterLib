# Migration Guide

This is a guide to help users migrate from the `v1.y.z` to `v2.y.z`.

???+ note
    The `v2.y.z` is not yet officially out and is still in the devlopment phase!

## Nullary Constructor

The class `Recipe` and `IOEntry` are now using nullary constructor. Here's a before/after.
??? example "Example: Before"
    ```java
    new Recipe(
        new IOEntry(
            Seq.with(
                ItemStack.with(Items.copper, 1)
            )
        ),
        new IOEntry(
            Seq.with(),
            Seq.with(
                LiquidStack.with(Liquids.water, 1f)
            )
        ),
        120f
    )
    ```
??? example "Example: After"
    ```java
    new Recipe() {{
        input = new IOEntry() {{
            items = Seq.with(
                ItemStack.with(Items.copper, 1)
            );
        }};
        output = new IOEntry() {{
            liquids = Seq.with(
                LiquidStack.with(Liquids.water, 1f)
            );
        }};
        craftTime = 120f;
    }}
    ```


<!-- === "Dependency"
    === "JSON"
        yes

    === "JavaScript"
        h

    === "Java"
        i

=== "Injection"
    === "JSON"
        yes

    === "JavaScript"
        h

    === "Java"
        i

=== "Jitpack"
    ???+ info
        This is Java only! -->