# Mindustry Mod 开发模板
用于 Mindustry Mod 开发的模板

[English](README.md) | 中文

![ICON](icon.png)

This page is outdated and needs to be translated. 

## 要求
- **Java:** Java 8 SDK

## 功能

- **自动编译**：每一个以 "[AR]" 开头的 commit 都会自动在 Github Action 编译任务成功之后自动生成新版本，以用于 [Anuken/MindustryMods](https://github.com/Anuken/MindustryMods) 的检查。最新的自动编译版将会自动覆盖旧版。
- **编译测试**：为支持 持续集成（CI），每一个 commit 将会通过构建的方式来测试可用性。
- **易于调试**：运行 gradle 自定义任务 "debugJar" 之后，生成模组的 jar 文件将拷贝到 Mindustry 的模组文件夹，并且自动打开游戏，所以建议关闭“游戏>> 游戏启动崩溃后禁用模组”（Game>>Disable Mods On Startup Crash）方便纠错，还可以在 gradle.properties 文件里配置你想开启的游戏的路径。
- **修复安卓部署**：修复了 "deploy" 任务在某些平台的环境变量PATH中找不到d8的错误，注意，你还是需要下载 Android SDK 然后添加到环境变量PATH中。
- **Kotlin支持**：如果你想在项目中使用Kotlin，你只需要在 build.gradle 中把 "useKotlin" 设为 true 即可。编译脚本会自动处理重复文件问题。
