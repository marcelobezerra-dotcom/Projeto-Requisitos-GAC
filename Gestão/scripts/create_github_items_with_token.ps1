<#
Script PowerShell para criar milestones, labels e issues usando o GitHub REST API com um token.
Uso:
- Exporte a variável de ambiente `GITHUB_TOKEN` contendo um token com escopo `repo`.
  No PowerShell:
    $env:GITHUB_TOKEN = 'ghp_...'
- Execute o script na pasta `Gestão/scripts`:
    .\create_github_items_with_token.ps1

Observações:
- Não modifica o Project Board (Projects v2) automaticamente — isso requer API GraphQL ou IDs do projeto.
- O token é lido preferencialmente da variável de ambiente `GITHUB_TOKEN`; se não houver, o script solicita de forma segura.
#>

param()

$repoFull = "marcelobezerra-dotcom/Projeto-Requisitos-GAC"
$projectUrl = "https://github.com/users/marcelobezerra-dotcom/projects/9" # referência

# obter token
$token = $env:GITHUB_TOKEN
if (-not $token -or $token -eq '') {
    Write-Host "Variável GITHUB_TOKEN não encontrada; solicitando token (entrada invisível):"
    $secure = Read-Host -AsSecureString "Token GitHub"
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    $token = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
}

if (-not $token -or $token -eq '') {
    Write-Error "Token não fornecido. Abortando."
    exit 1
}

$headers = @{
    Authorization = "token $token"
    Accept = "application/vnd.github+json"
    'User-Agent' = 'Projeto-Requisitos-Script'
}

$base = 'https://api.github.com'
$parts = $repoFull -split '/' 
$owner = $parts[0]
$repo = $parts[1]

function Ensure-Milestone($title, $due_on, $description) {
    $url = "$base/repos/$owner/$repo/milestones"
    # verificar existente
    $existing = Invoke-RestMethod -Method Get -Uri $url -Headers $headers -ErrorAction SilentlyContinue
    $found = $null
    if ($existing) { $found = $existing | Where-Object { $_.title -eq $title } }
    if ($found) { Write-Host "Milestone '$title' já existe (#$($found.number))."; return $found.number }

    $body = @{ title = $title; due_on = $due_on; description = $description } | ConvertTo-Json
    try {
        $resp = Invoke-RestMethod -Method Post -Uri $url -Headers $headers -Body $body -ContentType 'application/json'
        Write-Host "Criado milestone '$title' (#$($resp.number))."
        return $resp.number
    } catch {
        Write-Warning "Falha ao criar milestone '$title': $($_.Exception.Message)"
        return $null
    }
}

function Ensure-Label($name, $color, $description) {
    $url = "$base/repos/$owner/$repo/labels/$name"
    try {
        $exists = Invoke-RestMethod -Method Get -Uri $url -Headers $headers -ErrorAction SilentlyContinue
        if ($exists) { Write-Host "Label '$name' já existe."; return }
    } catch {}

    $urlPost = "$base/repos/$owner/$repo/labels"
    $body = @{ name = $name; color = $color; description = $description } | ConvertTo-Json
    try {
        $resp = Invoke-RestMethod -Method Post -Uri $urlPost -Headers $headers -Body $body -ContentType 'application/json'
        Write-Host "Criada label '$name'."
    } catch {
        Write-Warning "Falha ao criar label '$name': $($_.Exception.Message)"
    }
}

function Create-Issue($title, $bodyText, $labels, $milestoneNumber) {
    $url = "$base/repos/$owner/$repo/issues"
    $payload = @{ title = $title; body = $bodyText }
    if ($labels -and $labels.Count -gt 0) { $payload.labels = $labels }
    if ($milestoneNumber) { $payload.milestone = $milestoneNumber }
    $json = $payload | ConvertTo-Json -Depth 6
    try {
        $resp = Invoke-RestMethod -Method Post -Uri $url -Headers $headers -Body $json -ContentType 'application/json'
        Write-Host "Issue criada: $($resp.html_url)"
        return $resp.html_url
    } catch {
        Write-Warning "Falha ao criar issue '$title': $($_.Exception.Message)"
        return $null
    }
}

# carregar configuração externa (config.json) se disponível
$configPath = Join-Path $PSScriptRoot 'config.json'
if (Test-Path $configPath) {
    try {
        $cfg = Get-Content $configPath -Raw | ConvertFrom-Json
        if ($cfg.repoFull) {
            $repoFull = $cfg.repoFull
            $parts = $repoFull -split '/' 
            $owner = $parts[0]
            $repo = $parts[1]
        }
        if ($cfg.projectUrl) { $projectUrl = $cfg.projectUrl }
        if ($cfg.milestones) { $milestones = $cfg.milestones }
        if ($cfg.labels) { $labels = $cfg.labels }
        if ($cfg.issues) { $issues = $cfg.issues }
    } catch {
        Write-Warning "Falha ao ler config.json: $($_.Exception.Message). Usando valores embutidos."
    }
} else {
    Write-Host "config.json não encontrado em $PSScriptRoot — usando valores padrão embutidos."
}

# fallback: definir valores padrão se ainda não existirem
if (-not $milestones) {
    $milestones = @(
        @{ title = 'Sprint 1'; due_on = '2026-09-19T23:59:00Z'; description = 'Kickoff e pesquisas iniciais' },
        @{ title = 'Sprint 2'; due_on = '2026-10-03T23:59:00Z'; description = 'PoC individuais' },
        @{ title = 'Sprint 3'; due_on = '2026-10-17T23:59:00Z'; description = 'Revisão técnica e início do SDD' },
        @{ title = 'Sprint 4'; due_on = '2026-10-31T23:59:00Z'; description = 'Design detalhado (SDD) e integração' },
        @{ title = 'Sprint 5'; due_on = '2026-11-13T23:59:00Z'; description = 'Finalização e apresentação' }
    )
}

if (-not $labels) {
    $labels = @(
        @{ name='rfid'; color='0e8a16'; description='Tarefas relacionadas a RFID' },
        @{ name='qrcode'; color='1d76db'; description='QR code e código de barras' },
        @{ name='fingerprint'; color='5319e7'; description='Impressão digital' },
        @{ name='sdd'; color='0052cc'; description='Arquitetura e SDD' },
        @{ name='docs'; color='b60205'; description='Documentação' },
        @{ name='demo'; color='d4c5f9'; description='Itens para demo/apresentação' },
        @{ name='high-priority'; color='b60205'; description='Alta prioridade' },
        @{ name='blocker'; color='000000'; description='Bloqueador' }
    )
}

if (-not $issues) {
    $issues = @(
        @{ title='Pesquisa: RFID'; body='Resumo (1 pág): conceitos, padrões, hardware, APIs e 3 referências.'; labels=@('rfid','docs'); milestone='Sprint 1' },
        @{ title='PoC: RFID (leitura básica)'; body='Implementar PoC de leitura RFID. Incluir README e screenshots.'; labels=@('rfid','demo'); milestone='Sprint 2' },
        @{ title='Pesquisa: QR code e Código de Barras'; body='Resumo (1 pág): formatos, bibliotecas, geração e leitura. 3 referências.'; labels=@('qrcode','docs'); milestone='Sprint 1' },
        @{ title='PoC: QR/Barcode (geração e leitura)'; body='PoC que gera e lê códigos; incluir README.'; labels=@('qrcode','demo'); milestone='Sprint 2' },
        @{ title='Pesquisa: Impressão Digital'; body='Resumo (1 pág): sensores, algoritmos de matching, privacidade e 3 referências.'; labels=@('fingerprint','docs'); milestone='Sprint 1' },
        @{ title='PoC: Impressão Digital (simulação ou integração)'; body='PoC de matching ou simulação com dados de exemplo.'; labels=@('fingerprint','demo'); milestone='Sprint 2' },
        @{ title='Estudo SDD e Documento de Visão'; body='Sessões conjuntas para aplicar SDD; produzir Documento de Visão (rascunho).'; labels=@('sdd','docs'); milestone='Sprint 3' },
        @{ title='Desenho de Arquitetura (SDD)'; body='Criar diagrama de componentes e especificar APIs.'; labels=@('sdd'); milestone='Sprint 4' },
        @{ title='Integração dos PoC e testes'; body='Integrar módulos disponíveis; documentar testes manuais.'; labels=@('demo','sdd'); milestone='Sprint 4' },
        @{ title='Preparar apresentação (slides + roteiro)'; body='Montar slides e roteiro de demo para 13/11.'; labels=@('demo','docs'); milestone='Sprint 5' }
    )
}

# criar milestones (a partir de $milestones, seja do config ou do fallback)
$milestoneMap = @{}
foreach ($m in $milestones) {
    $num = Ensure-Milestone -title $m.title -due_on $m.due_on -description $m.description
    if ($num) { $milestoneMap[$m.title] = $num }
}

# criar labels (a partir de $labels)
foreach ($l in $labels) { Ensure-Label -name $l.name -color $l.color -description $l.description }

# criar issues (a partir de $issues)
$created = @()
foreach ($it in $issues) {
    $mNum = $null
    if ($it.milestone -and $milestoneMap.ContainsKey($it.milestone)) { $mNum = $milestoneMap[$it.milestone] }
    $url = Create-Issue -title $it.title -bodyText $it.body -labels $it.labels -milestoneNumber $mNum
    if ($url) { $created += $url }
}

Write-Host "`nIssues criadas: $($created.Count)`n"
$created | ForEach-Object { Write-Host $_ }

Write-Host "`nObservação: para adicionar automaticamente itens ao Project Kanban (Projects v2), é necessário usar a API GraphQL dos Projects v2 e o ID do projeto. Se quiser, posso gerar um script PowerShell extra que tenta adicionar itens ao projeto quando você fornecer o Project ID e conceder permissões apropriadas."

Write-Host "Script finalizado."