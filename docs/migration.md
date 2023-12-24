# Migration Guide

This is a guide to help users migrate from the `v1.y.z` to `v2.y.z`.
???+ info
    The `v2.y.z` is not yet officially out and is still in the devlopment phase!

## Nullary Constructor (Java)

The class `Recipe` and `IOEntry` now use nullary constructor.

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

## Using `Stack[]` (Java)

Instead of using `Seq<Stack[]>` we now simply use `Stack[]`. It only impacts the variables in the `IOEntry` class (`items`, `fluids` and `payloads`).

??? example "Example: Before"
    ```java
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
    ```
??? example "Example: After"
    ```java
    input = new IOEntry() {{
        items = ItemStack.with(Items.copper, 1);
    }};
    output = new IOEntry() {{
        liquids = LiquidStack.with(Liquids.water, 1f);
    }};
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