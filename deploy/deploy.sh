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
APP_USER="${APP_USER:-deployer}"               # 배포 전용 계정 — sudo 로는 grindalgo-deploy 만 실행 가능
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

  # 교체·재기동·기동확인·실패 시 롤백은 서버 쪽 grindalgo-deploy 가 전부 처리한다.
  # 배포 계정이 sudo 로 실행할 수 있는 유일한 명령이라, 여기서 임의 명령을 보낼 수 없다.
  echo "▸ 서버에서 교체·재기동"
  "${SSH[@]}" 'sudo /usr/local/bin/grindalgo-deploy backend'
}

deploy_frontend() {
  echo "▸ 프론트 빌드"
  (cd "$FRONTEND_DIR" && npm run build)

  echo "▸ 업로드"
  # 정적 파일은 통째로 갈아끼운다. 배포 중 반쯤 섞인 상태가 보이지 않도록
  # 서버 쪽에서 임시 디렉터리에 풀고 한 번에 교체한다(grindalgo-deploy frontend).
  tar -C "$FRONTEND_DIR/dist" -czf /tmp/grindalgo-dist.tgz .
  scp "${SCP_OPTS[@]}" /tmp/grindalgo-dist.tgz "$APP_USER@$APP_HOST:/tmp/"
  rm -f /tmp/grindalgo-dist.tgz

  echo "▸ 서버에서 교체"
  "${SSH[@]}" 'sudo /usr/local/bin/grindalgo-deploy frontend'
}

case "$TARGET" in
  backend)  deploy_backend ;;
  frontend) deploy_frontend ;;
  all)      deploy_backend; deploy_frontend ;;
  *) echo "사용법: ./deploy.sh [all|backend|frontend]" >&2; exit 1 ;;
esac

echo
echo "완료 → https://$APP_HOST/"
