# 무료 배포 가이드

Oracle Cloud **Always Free** 범위 안에서 백엔드와 프론트엔드를 배포합니다. 비용은 0원입니다.

```
                      인터넷
                        │  http(s)
                        ▼
        ┌───────────────────────────────┐                    ┌──────────────────────────┐
        │ VM #2 앱 서버 (64.110.117.207) │   SSH 터널(autossh) │ VM #1 DB (129.225.146.170)│
        │  Caddy :80/:443               │═══════════════════▶│  MariaDB  127.0.0.1:3306 │
        │   ├ /api/*  → :8080           │  10.0.0.46          │  judge0   127.0.0.1:2358 │
        │   └ 그 외   → 정적 프론트      │      → 10.0.0.186   │                          │
        │  Spring Boot 127.0.0.1:8080   │                    └──────────────────────────┘
        └───────────────────────────────┘
```

프론트와 API 가 **같은 오리진**을 쓰는 것이 이 구성의 핵심입니다. 세션 쿠키가 그대로 흐르고
CORS 설정이 필요 없으며, 브라우저의 서드파티 쿠키 제한도 피해 갑니다.
프론트는 이미 `VITE_API_BASE_URL` 이 없으면 상대경로 `/api/v1` 을 쓰므로 코드 수정이 필요 없습니다.

---

## 1. VM #2 만들기 (Oracle 콘솔)

Always Free 는 AMD 마이크로 인스턴스를 **2대까지** 주고 지금 1대만 쓰고 있으므로 한 대를 더 만들 수 있습니다.

Compute → Instances → **Create instance**

| 항목 | 값 |
|---|---|
| Image | Canonical Ubuntu 22.04 |
| Shape | **VM.Standard.E2.1.Micro** (Always Free 표시 확인) |
| VCN / Subnet | **VM #1 과 같은 것** — 서브넷 `10.0.0.0/24` |
| Public IP | 할당함 |
| SSH key | 기존 `~/.ssh/id_ed25519.pub` |

**같은 서브넷을 고르는 것이 중요합니다.** 두 VM 이 사설망으로 통하므로 DB 를 인터넷에 노출하지
않아도 되고, 아래 SSH 터널도 사설 IP 로 연결됩니다.

만든 뒤 공인 IP 를 확인해 두세요. 아래에서 `<APP_IP>` 로 씁니다.

## 2. 앱 서버 → DB VM 연결 (상시 SSH 터널)

VCN 보안 목록을 건드리지 않고 **앱 서버에서 DB VM 으로 상시 SSH 터널**을 유지합니다.
`autossh` 가 끊기면 자동으로 다시 붙고, systemd 가 부팅 시 함께 띄웁니다.

터널 전용 키를 앱 서버에서 만들고, DB VM 에는 **포워딩만 가능한 제한 키**로 등록합니다.

```bash
# 앱 서버에서 — 통과 문구 없이 생성해야 무인 재연결이 된다
ssh-keygen -t ed25519 -N '' -f ~/.ssh/tunnel_ed25519 -C 'vm2-tunnel@grindalgo'
cat ~/.ssh/tunnel_ed25519.pub
```

```bash
# DB VM 의 ~/.ssh/authorized_keys 에 아래 형태로 추가 (앞의 옵션이 핵심)
restrict,command="/bin/false",permitopen="127.0.0.1:3306",permitopen="127.0.0.1:2358",port-forwarding ssh-ed25519 AAAA... vm2-tunnel@grindalgo
```

`restrict` 는 pty 만 막고 `ssh host <명령>` 은 통과시키므로 `command="/bin/false"` 를 반드시 함께
넣습니다. 이 키로는 두 포트 포워딩 외에 아무것도 하지 못합니다.

터널 서비스를 올립니다.

```bash
sudo apt install -y autossh
# deploy/grindalgo-tunnel.service 를 /etc/systemd/system/ 에 두고
sudo systemctl daemon-reload && sudo systemctl enable --now grindalgo-tunnel
ss -tln | grep -E ':3306|:2358'    # 127.0.0.1 에 두 포트가 떠 있으면 성공
```

DB VM 쪽은 MariaDB 와 judge0 를 `127.0.0.1` 에 그대로 두면 되고, 인터넷에도 계속 닫혀 있습니다.
백엔드는 `127.0.0.1:3306` · `127.0.0.1:2358` 로 붙습니다(`application-prod.yml` 참고).

> 로컬 개발(`run-vm.ps1`)은 이 구성과 무관하게 그대로 동작합니다.

## 3. VM #2 초기 세팅

```bash
ssh ubuntu@<APP_IP>

sudo apt update && sudo apt install -y openjdk-17-jre-headless

# Caddy (자동 HTTPS)
sudo apt install -y debian-keyring debian-archive-keyring apt-transport-https curl
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' \
  | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' \
  | sudo tee /etc/apt/sources.list.d/caddy-stable.list
sudo apt update && sudo apt install -y caddy

# 앱 실행 계정과 디렉터리
sudo useradd -r -s /usr/sbin/nologin grindalgo || true
sudo mkdir -p /opt/grindalgo /var/log/grindalgo /var/www/grindalgo
sudo chown grindalgo:grindalgo /opt/grindalgo /var/log/grindalgo
sudo chown caddy:caddy /var/www/grindalgo

# 1GB 라 스왑을 준다 (VM #1 과 동일한 구성)
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 웹 포트 개방
sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save
```

> **콘솔 작업이 반드시 필요합니다.** VM 안의 iptables 를 열어도 Oracle 의 VCN 보안 목록이
> 막고 있으면 외부에서 접속되지 않습니다. Networking → Virtual Cloud Networks → 해당 VCN →
> Security Lists → Default Security List → **Add Ingress Rules** 에서 두 줄을 추가하세요.
> Source CIDR `0.0.0.0/0`, IP Protocol `TCP`, Destination Port `80` 과 `443` 입니다.
> 웹 서비스를 공개하려면 우회할 방법이 없는 유일한 콘솔 작업입니다.

## 4. 설정 파일 올리기

로컬에서 `application-prod.yml.example` 을 복사해 실제 값을 채운 뒤 올립니다.
DB 비밀번호와 judge0 토큰은 VM #1 의 `application-vm.yml` 과 같은 값입니다.

```bash
cp src/main/resources/application-prod.yml.example /tmp/application-prod.yml
# 편집기로 비밀번호·토큰·OAuth 값을 채운다

scp /tmp/application-prod.yml ubuntu@<APP_IP>:/tmp/
scp deploy/grindalgo-backend.service ubuntu@<APP_IP>:/tmp/
scp deploy/Caddyfile ubuntu@<APP_IP>:/tmp/
```

```bash
ssh ubuntu@<APP_IP>

sudo install -o grindalgo -g grindalgo -m 640 /tmp/application-prod.yml /opt/grindalgo/application-prod.yml
sudo mv /tmp/grindalgo-backend.service /etc/systemd/system/
sudo mv /tmp/Caddyfile /etc/caddy/Caddyfile
sudo systemctl daemon-reload
sudo systemctl enable grindalgo-backend
```

`application-prod.yml` 이 `/opt/grindalgo` 에 있으면 Spring Boot 가 작업 디렉터리에서 자동으로 읽습니다.

## 5. 배포 전용 계정

배포는 `deployer` 계정으로 합니다. 이 계정이 sudo 로 할 수 있는 일은 **배포 스크립트 실행 두 가지뿐**입니다.
CI 에서 자동 배포를 붙일 때 `ubuntu` 계정 키를 GitHub Secrets 에 넣으면 저장소에 쓰기 권한이 있는
사람이 곧 서버 관리자가 되므로, 권한을 좁힌 계정을 따로 둡니다.

```bash
sudo useradd -m -s /bin/bash deployer
sudo mkdir -p /home/deployer/.ssh && sudo chmod 700 /home/deployer/.ssh
sudo chown -R deployer:deployer /home/deployer/.ssh

# 배포 스크립트 — root 소유라 deployer 가 고칠 수 없다
sudo install -o root -g root -m 0755 deploy/grindalgo-deploy /usr/local/bin/grindalgo-deploy

# sudo 허용 목록 — 인자까지 고정된 두 줄이 전부
sudo install -o root -g root -m 0440 deploy/sudoers-deployer /etc/sudoers.d/grindalgo-deployer
sudo visudo -c        # 문법 검사, 반드시 확인
```

배포용 SSH 키는 따로 만들어 공개키만 등록합니다. `restrict` 를 붙이면 포트 포워딩·에이전트·X11 이
막히고 파일 전송과 명령 실행만 남습니다.

```bash
ssh-keygen -t ed25519 -N '' -f deploy_key -C 'github-actions@grindalgo'
# 서버의 /home/deployer/.ssh/authorized_keys 에 아래 형태로 (앞의 restrict 가 핵심)
#   restrict ssh-ed25519 AAAA... github-actions@grindalgo
```

**개인키는 저장소에 넣지 마세요.** CI 에 쓸 때는 GitHub Secrets 에 보관합니다.

이렇게 해두면 키가 유출돼도 공격자가 할 수 있는 일은 "이 서버에 코드를 배포하는 것"뿐입니다.
DB 비밀번호 파일(`application-prod.yml`)이나 터널 개인키를 읽을 수 없고, 다른 서비스를 만지거나
배포 스크립트 자체를 고칠 수도 없습니다.

## 6. 배포

로컬에서 한 줄이면 빌드·업로드·재기동까지 됩니다.

```bash
cd BackEnd
APP_HOST=<APP_IP> KEY_PATH=~/.ssh/deploy_key ./deploy/deploy.sh            # 백엔드 + 프론트
APP_HOST=<APP_IP> KEY_PATH=~/.ssh/deploy_key ./deploy/deploy.sh backend    # 백엔드만
APP_HOST=<APP_IP> KEY_PATH=~/.ssh/deploy_key ./deploy/deploy.sh frontend   # 프론트만
```

교체·재기동·기동 확인은 서버의 `grindalgo-deploy` 가 처리합니다.
새 jar 가 뜨지 않으면 **직전 버전으로 자동 롤백**하므로, 배포 실패로 서비스가 멈춰 있지 않습니다.

### GitHub Actions 자동 배포

`staging` 브랜치가 배포 기준선입니다. `dev` 에서 개발하고, 내보낼 준비가 되면 `staging` 에 올립니다.

```bash
git checkout staging && git merge dev && git push origin staging
```

push 하면 `.github/workflows/deploy.yml` 이 CI 와 같은 조건으로 테스트한 뒤, 통과했을 때만
서버에 올립니다. 두 저장소가 각자 배포하므로 **API 가 바뀌는 변경은 백엔드를 먼저** 올리세요.

저장소마다 Secrets 두 개가 필요합니다 — Settings → Secrets and variables → Actions.

| 이름 | 값 |
|---|---|
| `DEPLOY_SSH_KEY` | 배포 계정 개인키 전체 (`-----BEGIN` 부터 `-----END` 줄까지) |
| `APP_HOST` | 앱 서버 주소 |

개인키는 저장소에 절대 넣지 마세요. Secrets 는 로그에 찍혀도 자동으로 가려집니다.

## 7. 도메인 연결

도메인을 사시면 A 레코드를 `<APP_IP>` 로 지정하고, `/etc/caddy/Caddyfile` 첫 줄의
`grindalgo.example.com` 을 실제 도메인으로 바꾼 뒤 `sudo systemctl reload caddy` 하면
인증서가 자동 발급됩니다.

**도메인 전에 IP 로 먼저 띄우려면** Caddyfile 첫 줄을 `:80 {` 으로 바꾸세요.
HTTP 로 동작하며, 이때는 `application-prod.yml` 의 `cookie.secure` 를 잠시 `false` 로 두어야
로그인이 됩니다. 도메인을 붙일 때 둘 다 되돌리세요.

소셜 로그인을 쓰신다면 GitHub·Google 개발자 콘솔의 콜백 주소도 도메인 기준으로 등록해야 합니다.

```
https://<도메인>/login/oauth2/code/github
https://<도메인>/login/oauth2/code/google
```

---

## 배포 전에 정리하면 좋은 것

공개 배포는 개발계와 달리 되돌리기 어렵습니다. 점검에서 나온 것 중 아래 두 가지는
배포 전에 손보시길 권합니다. 자세한 내용은 점검 리포트를 참고하세요.

- **제출·문제 열람 경로가 인증 없이 열려 있습니다.** 세션이 없어도 제출이 익명으로 성공해
  점수가 유실됩니다. 공개 서비스에서는 그대로 두기 어렵습니다.
- **`/error` 가 인증 대상이라** 모든 오류가 401 로 나갑니다. 사용자가 잘못된 입력을 할 때마다
  로그아웃된 것처럼 보입니다.

## 문제가 생기면

```bash
sudo journalctl -u grindalgo-backend -n 100 --no-pager   # 백엔드 로그
sudo journalctl -u caddy -n 50 --no-pager                # Caddy 로그
curl -sf http://127.0.0.1:8080/api/v1/languages          # 백엔드 단독 확인
free -m                                                  # 메모리 (1GB 라 스왑 확인)
```

`backend.prev.jar` 로 직전 버전이 남아 있어 되돌릴 수 있습니다.

```bash
sudo mv /opt/grindalgo/backend.prev.jar /opt/grindalgo/backend.jar
sudo systemctl restart grindalgo-backend
```
