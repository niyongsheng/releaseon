# CLAUDE.md

Mobile app distribution platform — upload .ipa/.apk, generate download links, QR codes, and DingTalk notifications.

**Tech Stack**: Java 21, Spring Boot 3.3, Shiro 3.0, Thymeleaf, MySQL 8, Gradle 8.8

## Build & Run

```bash
./gradlew clean build                                              # Build
java -jar build/libs/releaseon.jar                                 # Run locally (requires MySQL)
SERVER=SERVER_IP PASS=PASSWORD ./deploy.sh                         # Deploy to server
```

On first start, the admin account `admin` / `admin123456` is created automatically.

## Core Architecture

### Upload Flow
1. `POST /app/upload` → `PackageController.upload()`
2. `StorageUtil.checkAndTransfer()` — validate ZIP, save temp file
3. `ParserClient.parse()` — reflectively load `APKParser` / `IPAParser` to parse metadata
4. `AppService.addPackage()` — store source file + icon, create/update App record
5. `WebHookClient.sendMessage()` — DingTalk notification

### Storage (Strategy Pattern)
`IStorage` interface, `StorageUtil` facade, `storage.active` selects the backend:

| Value | Implementation |
|---|---|
| `local` | Local filesystem |
| `aliyun` | Alibaba Cloud OSS |
| `qiniu` | Qiniu Cloud |
| `tencent` | Tencent Cloud COS |

### Permissions (Apache Shiro)
- `UserAuthorizingRealm` — BCrypt password auth + role/permission authorization
- `ShiroConfig` configures filter chains; `@RequiresPermissions` marks method-level permissions
- Default permissions: `/apps`, `/apps/get`, `/app/delete`, `/packageList/get`

### Database (JPA Entities)
`tb_app` / `tb_package` / `tb_storage` / `tb_provision` / `tb_user` / `tb_role` / `tb_permission` / `tb_web_hook`

### WebHook (Reflection Plugin)
`{Type}WebHook` naming convention in `org.releaseon.utils.webhook` package. Currently only `DingDingWebHook`.

### iOS OTA Distribution
- `/m/{id}` → `manifest.plist` (FreeMarker template)
- `/p/{id}` → download .ipa file
- Install link: `itms-services://?action=download-manifest&url=https://...`

## Configuration (application.properties)

- **Database**: `spring.datasource.*`
- **Port**: `server.port=8081`
- **Storage**: `storage.active` + corresponding backend credentials
- **Upload Limit**: 300MB
- **Debug Mode**: `config.debug=debug` (loads uploaded files from classpath)
