param(
    [ValidateSet("dev", "prod")]
    [string]$Environment = "prod",
    [switch]$RunJar
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$projectRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $projectRoot "backend"
$deployDir = Join-Path $backendDir "deploy"
$configTemplate = Join-Path $projectRoot "deploy\backend\application-prod.yml.example"
$configTarget = Join-Path $deployDir "application-$Environment.yml"
$jarPath = Join-Path $backendDir "target\forum-1.0.0.jar"

function Require-Command {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "未找到命令: $Name"
    }
}

Write-Host "开始执行后端部署脚本..." -ForegroundColor Cyan
Require-Command -Name "java"
Require-Command -Name "mvn"

if (-not (Test-Path $deployDir)) {
    New-Item -ItemType Directory -Path $deployDir | Out-Null
}

Push-Location $backendDir
try {
    Write-Host "执行 Maven 打包..." -ForegroundColor Cyan
    mvn clean package
    if ($LASTEXITCODE -ne 0) {
        throw "Maven 打包失败"
    }
}
finally {
    Pop-Location
}

if (-not (Test-Path $configTarget) -and (Test-Path $configTemplate)) {
    Copy-Item $configTemplate $configTarget
    Write-Host "已生成配置模板: $configTarget" -ForegroundColor Yellow
    Write-Host "请先修改数据库、JWT 和百度 NLP 配置后再用于正式环境。" -ForegroundColor Yellow
}

if (-not (Test-Path $jarPath)) {
    throw "未找到 Jar 文件: $jarPath"
}

Write-Host "后端打包完成: $jarPath" -ForegroundColor Green

if ($RunJar) {
    Write-Host "启动后端应用..." -ForegroundColor Cyan
    Push-Location $backendDir
    try {
        java "-Dspring.profiles.active=$Environment" "-Dspring.config.additional-location=$configTarget" -jar $jarPath
    }
    finally {
        Pop-Location
    }
}
