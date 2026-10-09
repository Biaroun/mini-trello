# Démarre Docker Desktop (en contournant le bug des sockets corrompus) puis Postgres.
# Usage : depuis la racine du projet ->  .\scripts\start-db.ps1

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path $PSScriptRoot -Parent

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    # Docker Desktop laisse des fichiers de socket qu'il ne peut plus supprimer au démarrage suivant.
    # On met les dossiers qui les contiennent de côté ; Docker les recrée proprement.
    $stamp = Get-Date -Format "yyyyMMddHHmmss"
    foreach ($dir in "$env:LOCALAPPDATA\Docker\run", "$env:LOCALAPPDATA\docker-secrets-engine") {
        if (Test-Path $dir) {
            Rename-Item $dir "$(Split-Path $dir -Leaf).old-$stamp"
        }
    }

    Write-Host "Démarrage de Docker Desktop..."
    Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    for ($i = 0; $i -lt 60; $i++) {
        docker info *> $null
        if ($LASTEXITCODE -eq 0) { break }
        Start-Sleep 3
    }
    if ($LASTEXITCODE -ne 0) { throw "Docker Desktop n'a pas démarré." }
}

Write-Host "Démarrage de Postgres..."
docker compose -f "$projectRoot\docker-compose.yml" up -d --wait
Write-Host "Postgres est prêt sur localhost:15432"
