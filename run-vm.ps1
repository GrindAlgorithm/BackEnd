# 개발계(Oracle VM) DB + judge0 를 쓰는 백엔드를 로컬에서 띄운다.
#
#   PS> .\run-vm.ps1
#
# VM 의 MariaDB(3306)와 judge0(2358)은 VM 내부 127.0.0.1 에만 바인딩되어 있어
# 외부에서 직접 붙을 수 없다. 이 스크립트가 SSH 터널을 먼저 열고 백엔드를 기동한다.
# 접속 정보는 gitignore 된 src/main/resources/application-vm.yml 에만 있다.

param(
    [string]$VmHost   = '129.225.146.170',   # Reserved public IP — VM 재시작에도 유지된다
    [string]$VmUser   = 'ubuntu',
    [string]$KeyPath  = "$HOME\.ssh\id_ed25519",
    [int]$Port        = 9090,   # 8080 은 Hyper-V 예약 포트 범위(8000~8199)와 겹쳐 바인딩이 실패한다
    [int]$DbPort      = 13307,  # → VM 127.0.0.1:3306 (MariaDB)
    [int]$JudgePort   = 12358   # → VM 127.0.0.1:2358 (judge0)
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

function Test-Port([int]$p) {
    [bool](Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue)
}

# ── 1. 사전 점검 ────────────────────────────────────────────────
if (-not (Test-Path 'src\main\resources\application-vm.yml')) {
    Write-Host "application-vm.yml 이 없습니다." -ForegroundColor Red
    Write-Host "src\main\resources\application-vm.yml.example 을 복사한 뒤 DB 비밀번호와"
    Write-Host "judge0 auth-token 을 채워 넣으세요. (이 파일은 커밋되지 않습니다)"
    exit 1
}
if (-not (Test-Path $KeyPath)) {
    Write-Host "SSH 키를 찾을 수 없습니다: $KeyPath" -ForegroundColor Red
    exit 1
}

if (Test-Port $Port) {
    $owner = (Get-NetTCPConnection -LocalPort $Port -State Listen)[0].OwningProcess
    Write-Host "포트 $Port 를 PID $owner 가 이미 쓰고 있습니다." -ForegroundColor Yellow
    Write-Host "이전에 띄운 백엔드라면 정리하세요:  Stop-Process -Id $owner -Force"
    exit 1
}

# ── 2. SSH 터널 ─────────────────────────────────────────────────
if ((Test-Port $DbPort) -and (Test-Port $JudgePort)) {
    Write-Host "SSH 터널이 이미 열려 있습니다 ($DbPort, $JudgePort). 재사용합니다." -ForegroundColor DarkGray
}
else {
    Write-Host "SSH 터널을 엽니다 → $VmUser@$VmHost" -ForegroundColor Cyan
    $sshArgs = @(
        '-i', $KeyPath,
        '-o', 'StrictHostKeyChecking=no',
        '-o', 'ExitOnForwardFailure=yes',
        '-o', 'ServerAliveInterval=30',
        '-N',
        '-L', "${DbPort}:127.0.0.1:3306",
        '-L', "${JudgePort}:127.0.0.1:2358",
        "$VmUser@$VmHost"
    )
    $ssh = Start-Process -FilePath 'ssh' -ArgumentList $sshArgs -PassThru -WindowStyle Hidden

    $ready = $false
    foreach ($i in 1..20) {
        Start-Sleep -Milliseconds 500
        if ((Test-Port $DbPort) -and (Test-Port $JudgePort)) { $ready = $true; break }
        if ($ssh.HasExited) { break }
    }
    if (-not $ready) {
        Write-Host "터널을 열지 못했습니다. VM 이 꺼져 있거나 공인 IP 가 바뀌었을 수 있습니다." -ForegroundColor Red
        Write-Host "(Ephemeral IP 라 VM 을 재시작하면 주소가 달라집니다 — -VmHost 로 지정하세요)"
        exit 1
    }
    Write-Host "터널 준비 완료 (ssh PID $($ssh.Id))" -ForegroundColor Green
}

# ── 3. 백엔드 기동 ──────────────────────────────────────────────
Write-Host "백엔드를 기동합니다 — profile=vm, port=$Port" -ForegroundColor Cyan
Write-Host "  API   http://localhost:$Port/api/v1"
Write-Host "  중지  Ctrl+C" -ForegroundColor DarkGray
Write-Host ""

& .\gradlew.bat bootRun --args="--spring.profiles.active=vm --server.port=$Port"
