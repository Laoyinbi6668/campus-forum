param(
    [string]$ApiBaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendDir = Join-Path $projectRoot "frontend"
$envFile = Join-Path $frontendDir ".env.production.local"
$distDir = Join-Path $frontendDir "dist"
$deployDir = Join-Path $frontendDir "deploy-dist"
$webConfigPath = Join-Path $deployDir "web.config"

function Require-Command {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "未找到命令: $Name"
    }
}

Write-Host "开始执行前端部署脚本..." -ForegroundColor Cyan
Require-Command -Name "node"
Require-Command -Name "npm"

Set-Content -Path $envFile -Encoding utf8 -Value "VITE_API_BASE_URL=$ApiBaseUrl"
Write-Host "已写入前端生产环境变量: $envFile" -ForegroundColor Green

Push-Location $frontendDir
try {
    Write-Host "执行前端构建..." -ForegroundColor Cyan
    npm run build
    if ($LASTEXITCODE -ne 0) {
        throw "前端构建失败"
    }
}
finally {
    Pop-Location
}

if (Test-Path $deployDir) {
    Remove-Item -Recurse -Force $deployDir
}

Copy-Item -Recurse $distDir $deployDir

$webConfig = @'
<?xml version="1.0" encoding="utf-8"?>
<configuration>
  <system.webServer>
    <rewrite>
      <rules>
        <rule name="VueRouterHistory" stopProcessing="true">
          <match url=".*" />
          <conditions logicalGrouping="MatchAll">
            <add input="{REQUEST_FILENAME}" matchType="IsFile" negate="true" />
            <add input="{REQUEST_FILENAME}" matchType="IsDirectory" negate="true" />
          </conditions>
          <action type="Rewrite" url="/" />
        </rule>
      </rules>
    </rewrite>
  </system.webServer>
</configuration>
'@

Set-Content -Path $webConfigPath -Encoding utf8 -Value $webConfig

Write-Host "前端构建完成，部署目录已生成: $deployDir" -ForegroundColor Green
