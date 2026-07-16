![logo](./logo.svg)

# Releaseon

移动应用分发平台。上传 `.ipa` / `.apk`，生成下载链接、二维码和钉钉通知。

**技术栈**: Java 21, Spring Boot 3.3, Shiro 3.0, Thymeleaf, MySQL 8, Gradle 8.8

## 快速开始

```bash
# 构建
./gradlew clean build

# 启动（需本地 MySQL）
java -jar build/libs/releaseon.jar
```

访问 `http://127.0.0.1:8081/account/signin`  
默认管理员 `admin` / `admin123456`  

## Docker

```bash
./gradlew clean build
docker build -t releaseon-app .
docker run -d -p 8081:8081 --name releaseon releaseon-app
```

## 部署

```bash
SERVER=你的服务器IP PASS=密码 ./deploy.sh
```

详情见 [部署指南](.claude/skills/releaseon-deploy.md)。
