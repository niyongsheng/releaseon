<p align="center">
  <img src="./logo.svg" width="160" alt="Logo">
</p>

# Releaseon

`iOS` | `Android` mobile app distribution platform. Upload `.ipa` / `.apk` packages, generate download links, QR codes, and DingTalk notifications.

**Tech Stack**: Java 21, Spring Boot 3.3, Shiro 3.0, Thymeleaf, MySQL 8, Gradle 8.8

## Quick Start

```bash
# Build
./gradlew clean build

# Run (requires a local MySQL)
java -jar build/libs/releaseon.jar
```

Visit `http://127.0.0.1:8081/account/signin`  
Admin account: `admin` / `admin123456`

## Docker

```bash
./gradlew clean build
docker build -t releaseon-app .
docker run -d -p 8081:8081 --name releaseon releaseon-app
```

## Deploy

See [Deployment Guide](.claude/skills/releaseon-deploy.md) for details.
