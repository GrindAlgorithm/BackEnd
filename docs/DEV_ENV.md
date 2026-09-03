# 개발계 환경 접속 가이드

개발계는 Oracle Cloud VM 한 대에 **MariaDB**와 **judge0 채점 서버**가 올라가 있는 구성입니다.
백엔드 애플리케이션은 VM 이 아니라 **각자 로컬에서** 띄우고, SSH 터널로 VM 의 DB·채점 서버에 붙습니다.

```
내 PC                                  Oracle VM (ap-osaka-1)
┌────────────────────────┐             ┌──────────────────────────┐
│ Spring Boot :9090      │             │ MariaDB      127.0.0.1:3306
│   ↓ localhost:13307 ───┼── SSH 터널 ─┼→                          │
│   ↓ localhost:12358 ───┼── SSH 터널 ─┼→ judge0     127.0.0.1:2358│
└────────────────────────┘             └──────────────────────────┘
```

DB(3306)와 judge0(2358)는 **VM 내부에만 열려 있습니다**. 인터넷에서 직접 접속할 수 없고,
SSH 터널을 통해서만 붙습니다. 방화벽을 여는 대신 이 방식을 쓰는 이유는, judge0 가
임의의 코드를 실행하는 서비스라 외부 노출 시 그대로 남의 연산 자원이 되기 때문입니다.

---

## 1. 팀원이 준비할 것

**Java 17** 과 **SSH 클라이언트**(Windows 10 이상·macOS·Linux 기본 포함)가 필요합니다.

SSH 키가 없다면 만들고, **공개키**(`.pub`)를 관리자에게 전달하세요.
개인키(`id_ed25519`)는 절대 공유하지 않습니다.

```bash
ssh-keygen -t ed25519 -C "이름@회사"     # 이미 있으면 건너뜁니다
cat ~/.ssh/id_ed25519.pub                # 이 한 줄을 관리자에게 전달
```

관리자에게서 `application-vm.yml` 파일을 받아 아래 경로에 두세요.
DB 비밀번호와 judge0 토큰이 들어 있어 저장소에 커밋되지 않는 파일입니다.

```
BackEnd/src/main/resources/application-vm.yml
```

## 2. 관리자가 해줄 것

팀원 공개키를 VM 에 등록합니다. 받은 공개키 한 줄을 그대로 붙여 넣습니다.

```bash
ssh -i ~/.ssh/id_ed25519 ubuntu@129.225.146.170 \
  "echo 'ssh-ed25519 AAAA... 이름@회사' >> ~/.ssh/authorized_keys"
```

등록된 키 확인 · 회수(퇴사·키 분실 시)는 이렇게 합니다.

```bash
ssh ubuntu@129.225.146.170 "awk '{print \$NF}' ~/.ssh/authorized_keys"          # 등록 목록
ssh ubuntu@129.225.146.170 "sed -i '/이름@회사/d' ~/.ssh/authorized_keys"        # 해당 키 삭제
```

`application-vm.yml` 은 **평문 메신저로 보내지 마세요.** 비밀번호 관리 도구(1Password,
Bitwarden 등)의 공유 항목이나 사내 비밀 저장소를 쓰고, 그게 없다면 최소한 만료되는
링크를 쓰세요. 유출되면 DB 전체와 채점 서버가 함께 열립니다.

## 3. 실행

```powershell
# Windows
cd BackEnd
.\run-vm.ps1
```

```bash
# macOS / Linux / Git Bash
cd BackEnd
./run-vm.sh
```

스크립트가 SSH 터널을 열고(이미 열려 있으면 재사용) 백엔드를 `vm` 프로필로 띄웁니다.
기동되면 `http://localhost:9090/api/v1` 에서 개발계 데이터로 응답합니다.

옵션을 바꾸려면 이렇게 넘깁니다.

```powershell
.\run-vm.ps1 -Port 8081 -VmHost 1.2.3.4        # Windows
```
```bash
PORT=8081 VM_HOST=1.2.3.4 ./run-vm.sh          # macOS / Linux
```

프론트엔드에서 붙을 때는 `BACKEND_ORIGIN` 을 같은 포트로 맞춰 주세요.

---

## 4. 함께 쓸 때 알아둘 것

**DB 는 한 벌을 같이 씁니다.** 스키마도 데이터도 공유하므로, 팀원이 만든 제출·랭킹·토론이
내 화면에도 보입니다. 각자 자기 계정으로 로그인하면 대부분 문제가 없지만,
**스키마를 바꾸는 작업**(테이블 추가·컬럼 변경)은 반드시 미리 공유하세요.
`hibernate.ddl-auto: none` 이라 스키마는 자동 생성되지 않고 `src/main/resources/db/*.sql`
과 실제 DB 를 손으로 맞춰야 합니다. 한쪽만 바꾸면 다른 팀원의 앱이 뜨지 않습니다.

**채점은 한 번에 하나씩 처리됩니다.** VM 이 메모리 1GB 라 judge0 워커를 1개만 돌리고
있습니다. 여러 명이 동시에 제출하면 순서대로 처리되며 대기가 생깁니다.
부하 테스트나 반복 채점은 시간을 나눠서 하세요.

**공인 IP 는 `129.225.146.170` 으로 고정되어 있습니다.** Reserved IP 라 VM 을 정지했다 켜도
주소가 유지됩니다. 스크립트 기본값도 이 주소이므로 따로 지정할 필요가 없습니다.

**포트 9090 을 쓰는 이유**는 일부 Windows 환경에서 Hyper-V 가 8000~8199 를 예약해
8080 바인딩이 실패하기 때문입니다. 8080 이 잘 열리는 환경이라면 `-Port 8080` 으로 바꿔도 됩니다.

---

## 5. 자주 겪는 문제

**`Unable to determine Dialect without JDBC metadata`**
프로필을 지정하지 않고 띄웠을 때 가장 흔합니다. 프로필 기본값이 `local` 이라
로컬 도커 MariaDB(`localhost:13306`)를 찾는데 그게 없으면 이 오류가 납니다.
`run-vm.ps1` / `run-vm.sh` 로 띄우면 `vm` 프로필이 지정됩니다.
스크립트로 띄웠는데도 같은 오류라면 SSH 터널이 끊긴 것이니 스크립트를 다시 실행하세요.

**`Permission denied (publickey)`**
공개키가 아직 VM 에 등록되지 않았거나, 다른 이름의 키를 쓰고 있습니다.
`KEY_PATH` / `-KeyPath` 로 실제 키 경로를 지정하세요.

**터널은 열렸는데 DB 접속만 실패**
VM 메모리가 부족해 MariaDB 가 내려갔을 수 있습니다.
`ssh ubuntu@129.225.146.170 "systemctl status mariadb"` 로 확인하세요.

**포트가 이미 사용 중**
이전에 띄운 백엔드가 남아 있습니다. 스크립트가 알려주는 PID 를 종료하세요.
