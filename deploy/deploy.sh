#!/usr/bin/env bash
# 로컬에서 빌드해 앱 서버(VM #2)로 올린다.
#
#   ./deploy.sh            # 백엔드 + 프론트 모두
#   ./deploy.sh backend    # 백엔드만
#   ./deploy.sh frontend   # 프론트만
#
# 사전 준비: VM #2 에 ssh 키로 접속 가능해야 하고, docs/DEPLOY.md 의 초기 세팅이 끝나 있어야 한다.

set -euo pipefail

APP_HOST="${APP_HOST:-}"                       # 앱 서버 공인 IP 또는 도메인
APP_USER="${APP_USER:-ubuntu}"
KEY_PATH="${KEY_PATH:-$HOME/.ssh/id_ed25519}"

TARGET="${1:-all}"
BACKEND_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="$(cd "$BACKEND_DIR/../FrontEnd" && pwd)"

if [ -z "$APP_HOST" ]; then
  echo "APP_HOST 를 지정하세요.  예)  APP_HOST=1.2.3.4 ./deploy.sh" >&2
  exit 1
fi

SSH=(ssh -i "$KEY_PATH" -o StrictHostKeyChecking=accept-new "$APP_USER@$APP_HOST")
SCP_OPTS=(-i "$KEY_PATH" -o StrictHostKeyChecking=accept-new)

deploy_backend() {
  echo "▸ 백엔드 빌드"
  (cd "$BACKEND_DIR" && ./gradlew bootJar -q)

  local jar
  jar="$(ls -t "$BACKEND_DIR"/build/libs/*.jar | grep -v -- '-plain\.jar$' | head -1)"
  echo "  산출물: $(basename "$jar") ($(du -h "$jar" | cut -f1))"

  echo "▸ 업로드"
  scp "${SCP_OPTS[@]}" "$jar" "$APP_USER@$APP_HOST:/tmp/backend.jar"

  echo "▸ 교체 후 재기동"
  # 실패 시 되돌릴 수 있도록 직전 jar 를 남긴다.
  "${SSH[@]}" 'sudo install -o grindalgo -g grindalgo -m 640 /tmp/backend.jar /opt/grindalgo/backend.new.jar \
    && sudo mv -f /opt/grindalgo/backend.jar /opt/grindalgo/backend.prev.jar 2>/dev/null || true; \
    sudo mv -f /opt/grindalgo/backend.new.jar /opt/grindalgo/backend.jar \
    && rm -f /tmp/backend.jar \
    && sudo systemctl restart grindalgo-backend'

  echo "▸ 기동 대기"
  for _ in $(seq 1 30); do
    if "${SSH[@]}" 'curl -sf -o /dev/null http://127.0.0.1:8080/api/v1/languages' 2>/dev/null; then
      echo "  백엔드 정상"
      return 0
    fi
    sleep 4
  done
  echo "  기동 확인 실패 — 로그를 확인하세요:" >&2
  echo "    ssh $APP_USER@$APP_HOST 'sudo journalctl -u grindalgo-backend -n 80 --no-pager'" >&2
  return 1
}

deploy_frontend() {
  echo "▸ 프론트 빌드"
  (cd "$FRONTEND_DIR" && npm run build)

  echo "▸ 업로드"
  # 정적 파일은 통째로 갈아끼운다. 배포 중 반쯤 섞인 상태가 보이지 않도록 새 디렉터리에 풀고 교체한다.
  tar -C "$FRONTEND_DIR/dist" -czf /tmp/grindalgo-dist.tgz .
  scp "${SCP_OPTS[@]}" /tmp/grindalgo-dist.tgz "$APP_USER@$APP_HOST:/tmp/"
  rm -f /tmp/grindalgo-dist.tgz

  "${SSH[@]}" 'set -e; \
    rm -rf /tmp/grindalgo-new && mkdir -p /tmp/grindalgo-new && \
    tar -C /tmp/grindalgo-new -xzf /tmp/grindalgo-dist.tgz && \
    sudo rm -rf /var/www/grindalgo.old && \
    sudo mv /var/www/grindalgo /var/www/grindalgo.old 2>/dev/null || true; \
    sudo mv /tmp/grindalgo-new /var/www/grindalgo && \
    sudo chown -R caddy:caddy /var/www/grindalgo && \
    rm -f /tmp/grindalgo-dist.tgz'
  echo "  프론트 배포 완료"
}

case "$TARGET" in
  backend)  deploy_backend ;;
  frontend) deploy_frontend ;;
  all)      deploy_backend; deploy_frontend ;;
  *) echo "사용법: ./deploy.sh [all|backend|frontend]" >&2; exit 1 ;;
esac

echo
echo "완료 → https://$APP_HOST/"
