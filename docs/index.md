# Overview

## Install

You can get the latest release on [here](https://github.com/liplum/MultiCrafterLib/releases/latest)
or search it on the Mod Browser with its name, `MultiCrafter Lib`.

## Supports

|   Method   | Json | JavaScript |  Java   |              Note              |
|:----------:|:----:|:----------:|:-------:|:------------------------------:|
| Dependency |  ✔️  |     ✔️     |   ✔️    | Players need download this mod |
| Injection  |      |     ✔️     | No Need |    Keep your mod standalone    |
|  Jitpack   |      |            |   ✔️    |   Full sources code support    |

=== "Dependency"

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

    You should download a zip filled with `.class` files [here](https://github.com/liplum/MultiCrafterLib/releases/latest).
    As a convention, it should be named as `MultiCrafter-<version>-injection.zip`.
    
    You need to unzip this and get its content ...

    - If you don't want to publish your mod on GitHub,
      you need put the content into the root directory of your mod's zip file.
    - If you've published your mod on GitHub,
      you need upload the content, use `git add` and `git push` or something else,
      into the root directory of your GitHub repository.

    In this way, you have to write JavaScript to create your block.

    How you create a block is basically the same as the what you did 
    in JavaScript but without a declaration of mod dependency.

=== "Jitpack"

    You can click here [![](https://jitpack.io/v/liplum/MultiCrafterLib.svg)](https://jitpack.io/#liplum/MultiCrafterLib)
    to fetch the latest version of MultiCrafter Lib.
    
    1. Add the JitPack repository to your build file
    
    ```groovy
    allprojects {
        repositories { maven { url 'https://jitpack.io' } }
    }
    ``` 
    
    2. Add the dependency
    
    ```groovy
     dependencies {
        implementation 'com.github.liplum:MultiCrafterLib:<version>'
    }
    ```
    
## More Info

You can access the [repository](https://github.com/liplum/MultiCrafterLib) on GitHub to obtain more information.

If you face any issue with MultiCrafter, please contact us
on [Issue Report](https://github.com/liplum/MultiCrafterLib/issues) page.

Join our [Discord server](https://discord.gg/PDwyxM3waw) to send us feedback or get help immediately.

Welcome to contribute MultiCrafter!
