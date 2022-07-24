<div align="center">

# [MultiCrafter Lib](https://liplum.github.io/MultiCrafterLib/) [![Discord](GFX/Discord.png)](https://discord.gg/PDwyxM3waw)

[![Discord](https://img.shields.io/discord/937228972041842718?color=%23529b69&label=Discord&logo=Discord&style=for-the-badge)](https://discord.gg/PDwyxM3waw)
[![TotalDownloads](https://img.shields.io/github/downloads/liplum/MultiCrafterLib/total?color=674ea7&label=Download&logo=docusign&logoColor=white&style=for-the-badge)](https://github.com/liplum/MultiCrafterLib/releases)
[![](https://jitpack.io/v/liplum/MultiCrafterLib.svg)](https://jitpack.io/#liplum/MultiCrafterLib)

A Mindustry MultiCrafter lib-mod for Json and JavaScript mods.
Please check the [instruction](https://liplum.github.io/MultiCrafterLib/).
___
</div>

## Showcase
<img alt="Statistics" src="GFX/Statistics.gif" width="460pt" height="640pt"/>

## How to Use

Please check the [instruction](https://liplum.github.io/MultiCrafterLib/) to learn MultiCrafter.

### Class Files Injection

<details>
<summary>Click to Expand</summary>

You should download a zip filled with `.class` files [here](https://github.com/liplum/MultiCrafterLib/releases/latest).
As a convention, it should be named as `MultiCrafter-<version>.zip`.

You need to unzip this and get its content ...

- If you don't want to publish your mod on GitHub,
  you need put the content into the root directory of your mod's zip file.
- If you've published your mod on GitHub,
  you need upload the content, use `git add` and `git push` or something else,
  into the root directory of your GitHub repository.

In this way, you have to write JavaScript to create your block.

How you create a block is basically the same as
<a href="#as-a-mod-dependency">the way to add a mod dependency</a>
in JavaScript but without a declaration of mod dependency.

E.g.:

```javascript
const multi = require("multi-crafter/lib")
const mineCrafter = multi.MultiCrafter("mine-crafter")
```

</details>

### Jitpack Dependency

<details>
<summary>Click to Expand</summary>

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

</details>

## Awesome

Some mods using *MultiCrafter Lib* are listed here, you may learn from them.

-[Java] [Omaloon](https://github.com/xStaBUx/Omaloon-mod-public) by `xStaBUx`

-[Json] [Z.P.G.M._Mod](https://github.com/r-omnom/Z.P.G.M._Mod) by `r-omnom`

## Licence

GNU General Public License v3.0
