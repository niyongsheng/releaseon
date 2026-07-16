---
name: releaseon-deploy
description: 部署 ReleaseOn OTA 分发平台到服务器（Docker 方式），支持 Cloudflare Tunnel、对象存储配置
metadata:
  type: project
  tags: [releaseon, deploy, docker, ios-ota, cloudflare]
---

# ReleaseOn 部署指南

## 前置条件

- 服务器已安装 Docker + Docker Compose v2
- Java 21 JDK（仅构建时需要）
- Gradle 8.8（包装器已包含）

## 快速部署

```bash
# 1. 构建 JAR
JAVA_HOME=/path/to/jdk-21 ./gradlew clean build -x test

# 2. 部署到服务器（需 sshpass）
SERVER=114.55.104.207 PASS=your_password ./deploy.sh

# 或手动复制后执行：
# scp -r docker-compose.yml Dockerfile mysql/ build/libs/*.jar root@SERVER:/home/releaseon/
# ssh root@SERVER "cd /home/releaseon && docker compose up -d --build"
```

## 配置说明

### 核心环境变量（docker-compose.yml）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SERVER_DOMAIN` | `releaseon.nicolab.top` | 域名，用于生成存储地址 |
| `STORAGE_LOCAL_ADDRESS` | `https://releaseon.nicolab.top/fetch/` | 文件存储访问地址 |

### 对象存储

`storage.active` 支持 `local` / `aliyun` / `tencent` / `qiniu`，在 `application.properties` 或环境变量中配置。

## 架构

```
Cloudflare (HTTPS) ── Tunnel ──> Docker Host:8081 ──> Container:8081 (HTTP)
                                    ├── releaseon-app    (Spring Boot 3.3 + Java 21)
                                    └── releaseon-mysql  (MySQL 8.0)
```

## 服务器目录结构

```
/home/releaseon/
├── docker-compose.yml    # 容器编排
├── Dockerfile            # 应用镜像
├── mysql/
│   ├── init.sql          # 数据库初始化
│   └── data/             # MySQL 持久化数据
├── storage/              # 上传文件存储
└── build/libs/
    └── releaseon.jar
```

## iOS OTA 安装要求

1. **HTTPS 是必须的** — Cloudflare Tunnel 有效证书满足此要求
2. **Provisioning Profile**：
   - Ad-Hoc：设备 UDID 需在描述文件中注册
   - 企业证书：设备需信任企业证书（设置→通用→VPN与设备管理）
   - 上传后应用详情页显示包类型和设备列表
3. **manifest.plist** 由应用自动生成（FreeMarker 模板），URL 使用 HTTPS
4. 安装链接格式：`itms-services://?action=download-manifest&url=https://domain/m/{id}`

## 管理命令

```bash
# 查看日志
docker compose logs -f app

# 重启应用
docker compose restart app

# 重新构建并启动（代码变更后）
docker compose build --no-cache app && docker compose up -d app

# 完全停止
docker compose down

# 进入容器排查
docker exec -it releaseon-app sh
```

## 首次访问

管理地址：`https://你的域名/`
默认账号：`admin` / `admin123456`
