#!/usr/bin/env bash
# 개발계(Oracle VM) DB + judge0 를 쓰는 백엔드를 로컬에서 띄운다. (macOS / Linux / Git Bash)
#
#   ./run-vm.sh
#
# VM 의 MariaDB(3306)와 judge0(2358)은 VM 내부 127.0.0.1 에만 바인딩되어 있어
# 외부에서 직접 붙을 수 없다. 이 스크립트가 SSH 터널을 먼저 열고 백엔드를 기동한다.
# 접속 정보는 gitignore 된 src/main/resources/application-vm.yml 에만 있다.

set -euo pipefail

VM_HOST="${VM_HOST:-129.225.146.170}"   # Reserved public IP — VM 재시작에도 유지된다
VM_USER="${VM_USER:-ubuntu}"
KEY_PATH="${KEY_PATH:-$HOME/.ssh/id_ed25519}"
PORT="${PORT:-9090}"        # 8080 은 Hyper-V 예약 포트와 겹치는 환경이 있어 기본값을 9090 으로 둔다
DB_PORT="${DB_PORT:-13307}"    # → VM 127.0.0.1:3306 (MariaDB)
JUDGE_PORT="${JUDGE_PORT:-12358}" # → VM 127.0.0.1:2358 (judge0)

cd "$(dirname "$0")"

port_open() {
  if command -v nc >/dev/null 2>&1; then
    nc -z 127.0.0.1 "$1" >/dev/null 2>&1
  else
    (exec 3<>"/dev/tcp/127.0.0.1/$1") >/dev/null 2>&1
  fi
}

# ── 1. 사전 점검 ────────────────────────────────────────────────
if [ ! -f src/main/resources/application-vm.yml ]; then
  echo "application-vm.yml 이 없습니다." >&2
  echo "src/main/resources/application-vm.yml.example 을 복사한 뒤 DB 비밀번호와" >&2
  echo "judge0 auth-token 을 채워 넣으세요. (이 파일은 커밋되지 않습니다)" >&2
  exit 1
fi
if [ ! -f "$KEY_PATH" ]; then
  echo "SSH 키를 찾을 수 없습니다: $KEY_PATH" >&2
  echo "KEY_PATH=~/.ssh/다른키 ./run-vm.sh 처럼 지정할 수 있습니다." >&2
  exit 1
fi
if port_open "$PORT"; then
  echo "포트 $PORT 를 이미 쓰고 있습니다. 이전에 띄운 백엔드를 종료하세요." >&2
  exit 1
fi

# ── 2. SSH 터널 ─────────────────────────────────────────────────
if port_open "$DB_PORT" && port_open "$JUDGE_PORT"; then
  echo "SSH 터널이 이미 열려 있습니다 ($DB_PORT, $JUDGE_PORT). 재사용합니다."
else
  echo "SSH 터널을 엽니다 → $VM_USER@$VM_HOST"
  ssh -i "$KEY_PATH" \
      -o StrictHostKeyChecking=accept-new \
      -o ExitOnForwardFailure=yes \
      -o ServerAliveInterval=30 \
      -f -N \
      -L "${DB_PORT}:127.0.0.1:3306" \
      -L "${JUDGE_PORT}:127.0.0.1:2358" \
      "$VM_USER@$VM_HOST"

  for _ in $(seq 1 20); do
    if port_open "$DB_PORT" && port_open "$JUDGE_PORT"; then break; fi
    sleep 0.5
  done
  if ! port_open "$DB_PORT"; then
    echo "터널을 열지 못했습니다. VM 이 꺼져 있거나 공인 IP 가 바뀌었을 수 있습니다." >&2
    echo "(Ephemeral IP 라면 VM 재시작 시 주소가 달라집니다 — VM_HOST=<새 주소> 로 지정하세요)" >&2
    exit 1
  fi
  echo "터널 준비 완료"
fi

# ── 3. 백엔드 기동 ──────────────────────────────────────────────
echo "백엔드를 기동합니다 — profile=vm, port=$PORT"
echo "  API   http://localhost:$PORT/api/v1"
echo "  중지  Ctrl+C"
echo

exec ./gradlew bootRun --args="--spring.profiles.active=vm --server.port=$PORT"
