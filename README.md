# Remote Bot Fetch

[![Latest](https://img.shields.io/github/v/release/SGU-Server/Remote-Bot-Fetch?label=Latest)](https://github.com/SGU-Server/Remote-Bot-Fetch/releases/latest)
[![License](https://img.shields.io/github/license/SGU-Server/Remote-Bot-Fetch.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![Issues](https://img.shields.io/github/issues/SGU-Server/Remote-Bot-Fetch.svg)](https://github.com/SGU-Server/Remote-Bot-Fetch/issues)
[![Minecraft Versions](<https://cf.way2muchnoise.eu/versions/For%20MC_1807783_all(555-E04E14-fff-010101).svg>)](https://www.curseforge.com/minecraft/mc-mods/remote-bot-fetch)<br>
[![GitHub Downloads](https://img.shields.io/github/downloads/SGU-Server/Remote-Bot-Fetch/total?label=GitHub%20Downloads)](https://github.com/SGU-Server/Remote-Bot-Fetch/releases)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1807783?label=CurseForge%20Downloads)](https://www.curseforge.com/minecraft/mc-mods/remote-bot-fetch)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/WTPbXrOy?label=Modrinth%20Downloads)](https://modrinth.com/mod/remote-bot-fetch)

[English](#english) | [简体中文](#简体中文)

## English

Remote Bot Fetch (RBF) is a Fabric mod for item lookup and retrieval with Carpet fake players and automated storage. It also integrates these features with the client inventory and Litematica Material List.

### Features

- Request items from server-side fake players through an inventory fetch panel.
- Query stored item counts from the Litematica Material List and assist with fetching materials.
- Use the server-side `/getItem` and `/getStorageData` commands, storage data API, and optional built-in storage website.
- Configure client keybinds, fake-player interactions, and server storage features.

See the [official documentation](https://rbf.sgu-server.com) for configuration and command details.

### Downloads

- [GitHub Releases](https://github.com/SGU-Server/Remote-Bot-Fetch/releases)
- [Modrinth](https://modrinth.com/mod/remote-bot-fetch/versions)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/remote-bot-fetch/files)

Choose a file for your Minecraft version. Check the release pages for the currently supported versions.

### Installation

Fabric only. Install the matching [Fabric Loader](https://fabricmc.net/use/) version, and make sure the client and server use compatible mod versions.

Dependencies:

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Fabric Carpet](https://modrinth.com/mod/carpet)
- [MaLiLib](https://modrinth.com/mod/malilib)

Install [Litematica](https://modrinth.com/mod/litematica) as well to use the client integration with its Material List.

For server-side features only, consider [Remote Bot Fetch Server](https://github.com/SGU-Server/Remote-Bot-Fetch-Server), a lightweight alternative with fewer dependencies that only depends on Fabric Carpet.

### Documentation

- English documentation: https://rbf.sgu-server.com
- [Downloads and dependencies](https://rbf.sgu-server.com/docs/download)
- [Configuration](https://rbf.sgu-server.com/docs/config)

### License and acknowledgements

This project is licensed under the [GNU General Public License v3.0](http://www.gnu.org/licenses/gpl-3.0.html). Some features were separated from [Antideath-survival-assistant](https://github.com/jfglzs/Antideath-survival-assistant) and [Carpet-LMS-Addition](https://github.com/Citrus-Union/Carpet-LMS-Addition); see [`third-party/`](third-party/) for their licenses. The project also references [Carpet-TIS-Addition](https://github.com/TISUnion/Carpet-TIS-Addition) ([license](third-party/Carpet-TIS-Addition/LICENSE)).

## 简体中文

Remote Bot Fetch（RBF）是一个面向 Fabric 的 Minecraft 模组，为 Carpet 假人和自动化仓储提供物品查询与取回功能，并将这些功能接入客户端物品栏和 Litematica 材料列表。

### 功能

- 在客户端物品栏中使用取物面板，向服务器假人请求物品。
- 配合 Litematica Material List 查询仓储物品数量，并辅助取回材料。
- 服务端提供 `/getItem`、`/getStorageData` 命令，以及仓储数据 API 和可选的内置仓储网页。
- 可配置客户端快捷键、假人交互行为和服务端仓储数据功能。

具体配置项和命令参数请见[官方文档](https://rbf.sgu-server.com/zh-Hans)。

### 下载

- [GitHub Releases](https://github.com/SGU-Server/Remote-Bot-Fetch/releases)
- [Modrinth](https://modrinth.com/mod/remote-bot-fetch/versions)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/remote-bot-fetch/files)

选择与你的 Minecraft 版本匹配的文件。支持版本以各平台发布页面为准。

### 安装

本模组仅支持 Fabric。先安装对应版本的 [Fabric Loader](https://fabricmc.net/use/)，并确保客户端和服务器使用兼容的模组版本。

依赖项：

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Fabric Carpet](https://modrinth.com/mod/carpet)
- [MaLiLib](https://modrinth.com/mod/malilib)

客户端使用 Litematica 材料列表集成功能时，还需安装 [Litematica](https://modrinth.com/mod/litematica)。

如果只需要服务端功能，可以使用轻量版 [Remote Bot Fetch Server](https://github.com/SGU-Server/Remote-Bot-Fetch-Server)：它依赖更少，仅依赖 Fabric Carpet。

### 文档

- 官网（中文）：https://rbf.sgu-server.com/zh-Hans
- [下载与依赖](https://rbf.sgu-server.com/zh-Hans/docs/download)
- [配置说明](https://rbf.sgu-server.com/zh-Hans/docs/config)

### 许可证与致谢

本项目以 [GNU General Public License v3.0](http://www.gnu.org/licenses/gpl-3.0.html) 发布。项目的部分功能从 [Antideath-survival-assistant](https://github.com/jfglzs/Antideath-survival-assistant) 和 [Carpet-LMS-Addition](https://github.com/Citrus-Union/Carpet-LMS-Addition) 分离而来，相关许可证见 [`third-party/`](third-party/)。项目也参考了 [Carpet-TIS-Addition](https://github.com/TISUnion/Carpet-TIS-Addition)（[许可证](third-party/Carpet-TIS-Addition/LICENSE)）。

<a href="https://www.star-history.com/?repos=sgu-server%2Fremote-bot-fetch&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=sgu-server/remote-bot-fetch&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=sgu-server/remote-bot-fetch&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=sgu-server/remote-bot-fetch&type=date&legend=top-left" />
 </picture>
</a>
