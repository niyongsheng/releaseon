# CLAUDE.md

移动应用分发平台 — 上传 .ipa/.apk，生成下载链接、二维码和钉钉通知。

**技术栈**: Java 21, Spring Boot 3.3, Shiro 3.0, Thymeleaf, MySQL 8, Gradle 8.8

## 构建 & 运行

```bash
./gradlew clean build                                              # 构建
java -jar build/libs/releaseon.jar                                 # 本地启动（需 MySQL）
SERVER=服务器IP PASS=密码 ./deploy.sh                              # 部署到服务器
```

首次启动自动创建管理员 `admin` / `admin123456`。

## 核心架构

### 上传流程
1. `POST /app/upload` → `PackageController.upload()`
2. `StorageUtil.checkAndTransfer()` — 校验 ZIP，保存临时文件
3. `ParserClient.parse()` — 反射加载 `APKParser` / `IPAParser`，解析元数据
4. `AppService.addPackage()` — 存储源文件+图标，创建/更新 App 记录
5. `WebHookClient.sendMessage()` — 钉钉通知

### 存储（策略模式）
`IStorage` 接口，`StorageUtil` 门面，`storage.active` 选择后端：

| 值 | 实现 |
|---|---|
| `local` | 本地文件系统 |
| `aliyun` | 阿里云 OSS |
| `qiniu` | 七牛云 |
| `tencent` | 腾讯云 COS |

### 权限（Apache Shiro）
- `UserAuthorizingRealm` — BCrypt 密码认证 + 角色/权限鉴权
- `ShiroConfig` 配置过滤链，`@RequiresPermissions` 标记方法权限
- 默认权限：`/apps`, `/apps/get`, `/app/delete`, `/packageList/get`

### 数据库（JPA 实体）
`tb_app` / `tb_package` / `tb_storage` / `tb_provision` / `tb_user` / `tb_role` / `tb_permission` / `tb_web_hook`

### WebHook（反射插件）
`{Type}WebHook` 命名规范，在 `org.releaseon.utils.webhook` 包。当前仅 `DingDingWebHook`。

### iOS OTA 分发
- `/m/{id}` → `manifest.plist`（FreeMarker 模板）
- `/p/{id}` → 下载 .ipa 文件
- 安装链接：`itms-services://?action=download-manifest&url=https://...`

## 配置（application.properties）

- **数据库**: `spring.datasource.*`
- **端口**: `server.port=8081`
- **存储**: `storage.active` + 对应后端密钥
- **上传限制**: 300MB
- **调试模式**: `config.debug=debug`（从 classpath 加载上传文件）
