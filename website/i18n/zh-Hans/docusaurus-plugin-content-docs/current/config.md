---
title: 配置
sidebar_position: 2
description: "`world/` 下的运行时 JSON 文件"
---

## checkStorageConfig

- 路径：`world/remotebotfetch/checkStorageConfig.json`
- 用途：被仓储数据 API 与内置网站功能读取。包含端口、是否自动启动网站、是否需要密码、令牌过期天数，以及要处理的仓储列表文件名。
- 若文件不存在，会在世界加载时自动生成。
- 示例：

```json
{
  "storageList": ["example.json"]
}
```

## checkStorageList

- 路径：`world/remotebotfetch/checkStorageList/*.json`
- 用途：仓储数据 API 读取的坐标列表。具体读取哪些文件由 `checkStorageConfig.json` 中的 `storageList` 控制。
- 示例文件：`world/remotebotfetch/checkStorageList/example.json`
- 如果 `storageList` 包含 `example.json`，且该文件不存在，模组会在世界加载时自动生成这个示例文件。
- 示例：

```json
{
  "overworld": [[0, 0, 0]],
  "end": [],
  "nether": []
}
```
