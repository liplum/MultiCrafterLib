# Overview

### Install

Repo: [liplum/MultiCrafterLib](https://github.com/liplum/MultiCrafterLib).

You can get the latest release on [here](https://github.com/liplum/MultiCrafterLib/releases/latest)
or search it on the Mod Browser with its name, `MultiCrafter Lib`.

### Supports

|   Method   | Json | JavaScript | Java |              Note              |
|:----------:|:----:|:----------:|:----:|:------------------------------:|
| Dependency |  ✔️  |     ✔️     |  ✔️  | Players need download this mod |
| Injection  |      |     ✔️     |      |    Keep your mod standalone    |
|  Jitpack   |      |            |  ✔️  |   Full sources code support    |

### Support-Dependency
If you want your mod to depend on MultiCrafter and only focus on your contents, it's for your use case.  
#### Add dependency

You should add MultiCrafter as a dependency in the `mod.[h]json` file:

=== "mod.json"

    ```json
    "dependencies": ["multi-crafter"]
    ```

=== "mod.hjson"

    ```hjson
    dependencies: ["multi-crafter"]
    ```
