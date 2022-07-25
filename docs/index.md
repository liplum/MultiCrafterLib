# Overview

## Install

You can get the latest release on [here](https://github.com/liplum/MultiCrafterLib/releases/latest)
or search it on the Mod Browser with its name, `MultiCrafter Lib`.

## Supports

|   Method   | Json | JavaScript |  Java   |              Note              |
|:----------:|:----:|:----------:|:-------:|:------------------------------:|
| Dependency |  ✔️  |     ✔️     |   ✔️    | Players need download this mod |
| Injection  |  ✔️  |     ✔️     | No Need |    Keep your mod standalone    |
|  Jitpack   |      |            |   ✔️    |   Full sources code support    |

=== "Dependency"

    **For a Json, JavaScript or Java mod.**

    If you want your mod to depend on MultiCrafter and only focus on your contents, it's for your use case.
    
    You should add MultiCrafter as a dependency in the `mod.[h]json` file:
    
    === "mod.json"
    
        ```json
        "dependencies": ["multi-crafter"]
        ```
    
    === "mod.hjson"
    
        ```hjson
        dependencies: ["multi-crafter"]
        ```

=== "Injection"

    **For a Json or JavaScript mod.**

    You should download a zip, named `MultiCrafter-injection.zip`, in [here](https://github.com/liplum/MultiCrafterLib/releases/latest).

    Then unzip it and copy the its contents into the root directory of your mod.

    <details>
    <summary>Unzip will add essential files into the root directory.</summary>
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
    ├─ multicrafter
    ├─ scripts/
    |  ├─ multi-crafter/
    |  |  ├─ lib.js
    ├─ content/
    |  ├─ crafter.hjson
    ├─ mod.hjson
    ├─ icon.png
    ├─ classes.dex
    ```
    </details>
    
    Then add this line in your `mod.[h]json`:
    

    === "mod.json"
    
        ```json
        "main": "MultiCrafterAdapter"
        ```
    
    === "mod.hjson"
    
        ```hjson
        main: MultiCrafterAdapter
        ```
    
    Then you can create your own multicrafter after checking this instrution.    
    
    - Root Directory: A folder which always has `icon.png` and `mod.[h]json`.

    <details open>
    <summary>You may face a warning about overwriting.</summary>

    Your device may warn you that would overwrite something.
    It's always safe, but you'd better to back-up your mod workspace before copy.
    </details>

    ### Upgrade MultiCrafter Lib
    With Injection, you have to upgrade `MultiCrafter Lib` manually.

    It's easy that you just need repeat the step above and handle with overwritten.

=== "Jitpack"

    **For a Java mod.**

    You can click here [![](https://jitpack.io/v/liplum/MultiCrafterLib.svg)](https://jitpack.io/#liplum/MultiCrafterLib)
    to fetch the latest version of MultiCrafter Lib.
    
    === "Groovy"
    
        1. Add the JitPack repository to your build.gradle
        
            ```groovy
            repositories { maven { url 'https://jitpack.io' } }
            ``` 
        2. Add the dependency
        
            ```groovy
             dependencies {
                implementation 'com.github.liplum:MultiCrafterLib:<version>'
            }
            ```
    === "Kotlin"

        1. Add the JitPack repository to your build.gradle.kts
        
            ```kotlin
            repositories {
                maven { url = uri("https://www.jitpack.io") }
            }
            ``` 
        2. Add the dependency
        
            ```kotlin
             dependencies {
                implementation("com.github.liplum:MultiCrafterLib:<version>")
            }
            ```
## More Info

You can access the [repository](https://github.com/liplum/MultiCrafterLib) on GitHub to obtain more information.

If you face any issue with MultiCrafter, please contact us
on [Issue Report](https://github.com/liplum/MultiCrafterLib/issues) page.

Join our [Discord server](https://discord.gg/PDwyxM3waw) to send us feedback or get help immediately.

Welcome to contribute MultiCrafter!
