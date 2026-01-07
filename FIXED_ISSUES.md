# 已修复的问题

## 1. 图标引用错误
- 修复了 `HomeScreen.kt` 中的图标引用问题
- 将 `ArrowForwardIos` 替换为 `ArrowForward`
- 将 `MergeType` 替换为 `Build`
- 将 `NetworkCheck` 替换为 `Search`
- 添加了 Material Icons Extended 依赖

## 2. 主题设置问题
- 创建了自定义主题 `Theme.ToolsApp`
- 在 `AndroidManifest.xml` 中正确引用主题
- 添加了 AppCompat 依赖

## 3. 文件工具类扩展
- 添加了 `saveToDownloads` 方法
- 添加了 `createTempFile` 方法

## 4. MainActivity 类型修改
- 将 `ComponentActivity` 改为 `AppCompatActivity`

## 5. 包名修复
- 所有文件的包名已统一为 `com.ccdyz.tools`

## 6. 数据库简化
- 移除了 Room 相关依赖
- 简化了 ScanHistory 实体类
- 简化了 ScanHistoryDao 接口

## 7. UI 组件重建
- 重新创建了 PortScannerScreen
- 重新创建了 FileMergerScreen

## 8. 权限配置
- 添加了网络权限
- 添加了存储权限