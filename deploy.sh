#!/bin/bash
set -e

# ================================================
# ReleaseOn — Docker Deployment Script
# ================================================
# Usage:
#   SERVER=114.55.104.207 PASS=yourpass ./deploy.sh
#   or set SERVER / PASS env vars, or edit below
# ================================================

SERVER_IP="${SERVER:-114.55.104.207}"
SERVER_USER="${USER:-root}"
SERVER_PASS="${PASS:-DP3YXZOREGO6IQ,./}"
DEPLOY_DIR="/home/releaseon"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "================================================"
echo " ReleaseOn Deployment"
echo " Target: $SERVER_USER@$SERVER_IP:$DEPLOY_DIR"
echo "================================================"

# --------------------------------------------------
# 0. Check prerequisites
# --------------------------------------------------
check_prereqs() {
    echo "[0/6] Checking prerequisites..."
    if ! command -v sshpass &>/dev/null; then
        echo "  ✗ sshpass not found. Install with: brew install sshpass"
        exit 1
    fi
    if [ ! -f "$SCRIPT_DIR/build/libs/releaseon.jar" ]; then
        echo "  ✗ JAR not found. Run ./gradlew clean build -x test first."
        exit 1
    fi
    echo "  ✓ All prerequisites met"
}

# --------------------------------------------------
# 1. Setup directories on server
# --------------------------------------------------
setup_dirs() {
    echo "[1/6] Setting up directories..."
    sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_IP" \
        "mkdir -p $DEPLOY_DIR/{mysql/data,storage}"
    echo "  ✓ Directories created"
}

# --------------------------------------------------
# 2. Sync deployment files to server
# --------------------------------------------------
sync_files() {
    echo "[2/6] Syncing files..."
    cd "$SCRIPT_DIR"

    local tmp_dir
    tmp_dir=$(mktemp -d)
    mkdir -p "$tmp_dir/releaseon/build/libs"

    cp docker-compose.yml "$tmp_dir/releaseon/"
    cp Dockerfile "$tmp_dir/releaseon/"
    cp -r mysql "$tmp_dir/releaseon/"
    cp build/libs/releaseon.jar "$tmp_dir/releaseon/build/libs/"

    # Clean remote build artifacts (keep mysql data & storage)
    sshpass -p "$SERVER_PASS" ssh "$SERVER_USER@$SERVER_IP" \
        "rm -rf $DEPLOY_DIR/docker-compose.yml $DEPLOY_DIR/Dockerfile $DEPLOY_DIR/mysql $DEPLOY_DIR/build"

    sshpass -p "$SERVER_PASS" scp -r "$tmp_dir/releaseon/"* "$SERVER_USER@$SERVER_IP:$DEPLOY_DIR/"
    rm -rf "$tmp_dir"
    echo "  ✓ Files synced"
}

# --------------------------------------------------
# 4. Install Docker if needed
# --------------------------------------------------
install_docker() {
    echo "[3/6] Checking Docker installation..."
    sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_IP" << 'SSHEOF'
        if ! command -v docker &>/dev/null; then
            echo "  Installing Docker..."
            curl -fsSL https://get.docker.com | sh
            systemctl enable docker
            systemctl start docker
        fi
        echo "  ✓ Docker: $(docker --version 2>/dev/null)"

        if ! docker compose version &>/dev/null 2>&1; then
            echo "  Installing Docker Compose..."
            curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
            chmod +x /usr/local/bin/docker-compose
        fi
        echo "  ✓ Compose: $(docker compose version 2>/dev/null)"
SSHEOF
}

# --------------------------------------------------
# 5. Stop & cleanup old containers
# --------------------------------------------------
cleanup() {
    echo "[4/6] Cleaning up..."
    sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_IP" \
        "cd $DEPLOY_DIR && docker compose down 2>/dev/null; docker rm -f releaseon-app 2>/dev/null; true"
    echo "  ✓ Cleaned"
}

# --------------------------------------------------
# 6. Build & start containers
# --------------------------------------------------
deploy_app() {
    echo "[5/6] Deploying containers..."
    sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_IP" << SSHEOF
        cd $DEPLOY_DIR
        export SERVER_DOMAIN=$SERVER_IP
        docker compose pull mysql 2>/dev/null || true
        docker compose up -d --build 2>&1
SSHEOF
    echo "  ✓ Containers started"
}

# --------------------------------------------------
# Verify deployment
# --------------------------------------------------
verify() {
    echo ""
    echo "Verifying deployment..."
    sleep 15
    local http_code
    http_code=$(sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_IP" \
        "curl -s -o /dev/null -w '%{http_code}' http://localhost:8081/account/signin 2>/dev/null" 2>/dev/null || echo "failed")
    if [ "$http_code" = "200" ]; then
        echo "  ✓ App is running (HTTP $http_code)"
    else
        echo "  ⚠ App returned HTTP $http_code — check logs: docker logs releaseon-app"
    fi
}

# --------------------------------------------------
main() {
    echo ""
    check_prereqs
    setup_dirs
    sync_files
    install_docker
    cleanup
    deploy_app
    verify

    echo ""
    echo "================================================"
    echo "  ✅ Deployment complete!"
    echo ""
    echo "  URL:      https://$SERVER_IP"
    echo "  HTTP:     http://$SERVER_IP:8081 (→ HTTPS)"
    echo "  Account:  admin / admin123456"
    echo ""
    echo "  Server:   $SERVER_USER@$SERVER_IP"
    echo "  Data:     $DEPLOY_DIR"
    echo "    ├── mysql/data/   — MySQL data"
    echo "    ├── storage/      — Uploaded files"
    echo "    └── docker-compose.yml"
    echo ""
    echo "  Commands:"
    echo "    docker compose logs -f    — Tail logs"
    echo "    docker compose restart    — Restart"
    echo "    docker compose down       — Stop all"
    echo "================================================"
}

main
