# Mindustry Mod Template
A template for Mindustry modding.

English | [中文](README_CN.md)

![ICON](icon.png)

## Requirements
- **Java:** Java 8 SDK

## Functions

### Auto-release
Every commit started with "[AR]" will automatically generate a new release after succeeding in Build Task
by GitHub Actions, for [Anuken/MindustryMods](https://github.com/Anuken/MindustryMods) checking.
The latest auto-release will replace the old one.

### Build Test
To support Continuous Integration(CI), every commit will be checked for validity by building the project.

### Configuration
Modify settings in gradle.properties such as game version.

### Easy Debug
- Run gradle task "runMod" for desktop and "runModSever" for server as Debug mod.
- Enjoy the hot-reload by **build project/module** in Build Menu while the game is running, supported by Intellij IDEA.
- Recommend you to turn off the in-game setting, *Game>>Disable Mods On Startup Crash*, to gain more convenience.
### Android Deployment Fix
It was fixed that the task "deploy" cannot find d8 in your environment variable PATH on some platforms.
Note: You still have to download Android SDK and add it into PATH.

### Kotlin Support
If you want to use Kotlin in your project, set the "UseKotlin=true" in gradle.properties.
And you can also select which Kotlin version you want to use in it.
Build scripts will deal with the problem of duplicate files for you.

## Licence
GNU General Public License v3.0