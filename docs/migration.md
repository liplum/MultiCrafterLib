# Migration Guide

This is a guide to help users migrate from the `v1.y.z` to `v2.y.z`.
???+ info
    The `v2.y.z` is not yet officially out and is still in the devlopment phase!

## Library rename

The biggest change of all, the rename of the library from `MultiCrater Lib` to `omnicrafter`.

=== "Dependency"
    You will have to update your `mod.json` and change the name of the dependency.

    ```diff
    - "dependencies": ["multi-crafter"]
    + "dependencies": ["omnicrafter"]
    ```

=== "Injection"
    ???+ info inline end
        This repo doesn't exist because the rename didn't occured yet!
    ##### Step 1
    You will have to download a zip, named `omnicrafter-injection.zip`, in [here](https://github.com/liplum/omnicrafter/releases/latest).

    ##### Step 2
    Unzip the downloaded zip and copy its contents into the root directory of your mod.

    <details>
    <summary>
    Unzip will add essential files into the root directory.
    <br>
    Please pay attention to your structure and avoid secondary directory in your mod zip.
    </summary>
    Suppose your have this structure:

    - Before unzip:
    ```
    your-mod/
    ├─ content/
    |  ├─ crafter.hjson
    ├─ mod.hjson
    ├─ icon.png
    ```

    - After unzip:
    ```
    your-mod/
    ├─ multicrafter/
    ├─ scripts/
    |  ├─ multi-crafter/
    |  |  ├─ lib.js
    ├─ content/
    |  ├─ crafter.hjson
    ├─ mod.hjson
    ├─ icon.png
    ├─ classes.dex
    ```

    MultiCrafter injection doesn't work when you zip your mod folder in a wrong way 
    where have created a secondary directory.

    Suppose you had a mod zip, named `your-mod.zip`.
    
    - This will work.
    In your-mod.zip, there are...
    ```
    multicrafter/
    scripts/
    content/
    mod.hjson
    icon.png
    ```

    - But this will not.
    In your-mod.zip, there are...
    ```
    your-mod/
    ├─ multicrafter/
    ├─ scripts/
    ├─ content/
    ├─ mod.hjson
    ├─ icon.png
    ```
    
    If you're using `ZArchiver` app, you could multi-select the each file inside your mod folder 
    and zip them all into one.
    </details>

    ##### Step 3
    Then modify this line in your `mod.[h]json`:

    ```diff
    - "main": "MultiCrafterAdapter"
    + "main": "OmnicrafterAdapter"
    ```

=== "Jitpack (Java)"
    You will have to update your `build.gradle(.kts)` and change the name of the library.

    === "Groovy"
        ```diff
          dependencies {
        -     implementation 'com.github.liplum:MultiCrafterLib:<version>'
        +     implementation 'com.github.liplum:omnicrafter:<version>'
          }
        ```
    === "Kotlin"
        ```diff
          dependencies {
        -     implementation("com.github.liplum:MultiCrafterLib:<version>")
        +     implementation("com.github.liplum:omnicrafter:<version>")
          }
        ```

## Nullary Constructor (Java)

The class `Recipe` and `IOEntry` now use nullary constructor.

???+ example "Example: Before"
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
???+ example "Example: After"
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

???+ example "Example: Before"
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
???+ example "Example: After"
    ```java
    input = new IOEntry() {{
        items = ItemStack.with(Items.copper, 1);
    }};
    output = new IOEntry() {{
        liquids = LiquidStack.with(Liquids.water, 1f);
    }};
    ```
