<#
Script para criar milestones/labels/issues e adicionar as issues ao Project Kanban (Projects v2).
Requisitos:
- Defina a variável de ambiente GITHUB_TOKEN com um token que tenha permissão `repo`.
- O Project alvo é um Project v2 de usuário/organização; informe o login do dono e o número do projeto.

Uso:
PS> $env:GITHUB_TOKEN = 'ghp_...'
PS> .\create_and_add_to_project.ps1 -ProjectOwner 'marcelobezerra-dotcom' -ProjectNumber 9

Notas:
- O script cria milestones, labels e issues (REST), recupera o node_id das issues e usa a API GraphQL para adicionar itens ao Project v2.
- Se algum item já existir no Project, a API pode retornar erro; o script tentará prosseguir.
#>
param(
    [string]$ProjectOwner = 'marcelobezerra-dotcom',
    [int]$ProjectNumber = 9
)

$repoFull = "marcelobezerra-dotcom/Projeto-Requisitos-GAC"
$token = $env:GITHUB_TOKEN
if (-not $token -or $token -eq '') {
    Write-Error "Defina a variável de ambiente GITHUB_TOKEN com um token válido e execute novamente."
    exit 1
}

$headersRest = @{ Authorization = "token $token"; Accept = 'application/vnd.github+json'; 'User-Agent' = 'Projeto-Requisitos-Script' }
$headersGraph = @{ Authorization = "bearer $token"; Accept = 'application/vnd.github+json'; 'User-Agent' = 'Projeto-Requisitos-Script' }
$base = 'https://api.github.com'
$graphql = 'https://api.github.com/graphql'
$parts = $repoFull -split '/' 
$owner = $parts[0]
$repo = $parts[1]

function Ensure-Milestone($title, $due_on, $description) {
    $url = "$base/repos/$owner/$repo/milestones"
    $existing = Invoke-RestMethod -Method Get -Uri $url -Headers $headersRest -ErrorAction SilentlyContinue
    $found = $null
    if ($existing) { $found = $existing | Where-Object { $_.title -eq $title } }
    if ($found) { Write-Host "Milestone '$title' já existe (#$($found.number))."; return $found.number }

    $body = @{ title = $title; due_on = $due_on; description = $description } | ConvertTo-Json
    $resp = Invoke-RestMethod -Method Post -Uri $url -Headers $headersRest -Body $body -ContentType 'application/json'
    Write-Host "Criado milestone '$title' (#$($resp.number))."
    return $resp.number
}

function Ensure-Label($name, $color, $description) {
    $url = "$base/repos/$owner/$repo/labels/$name"
    try { $exists = Invoke-RestMethod -Method Get -Uri $url -Headers $headersRest -ErrorAction SilentlyContinue }
    catch { $exists = $null }
    if ($exists) { Write-Host "Label '$name' já existe."; return }
    $urlPost = "$base/repos/$owner/$repo/labels"
    $body = @{ name = $name; color = $color; description = $description } | ConvertTo-Json
    $resp = Invoke-RestMethod -Method Post -Uri $urlPost -Headers $headersRest -Body $body -ContentType 'application/json'
    Write-Host "Criada label '$name'."
}

function Create-IssueReturnNode($title, $bodyText, $labels, $milestoneNumber) {
    $url = "$base/repos/$owner/$repo/issues"
    $payload = @{ title = $title; body = $bodyText }
    if ($labels -and $labels.Count -gt 0) { $payload.labels = $labels }
    if ($milestoneNumber) { $payload.milestone = $milestoneNumber }
    $json = $payload | ConvertTo-Json -Depth 6
    $resp = Invoke-RestMethod -Method Post -Uri $url -Headers $headersRest -Body $json -ContentType 'application/json'
    Write-Host "Issue criada: $($resp.html_url) (#$($resp.number))."
    return @{ number = $resp.number; node_id = $resp.node_id; url = $resp.html_url }
}

function Get-ProjectV2Id($login, $number) {
    $query = @"
query($login:String!, $number:Int!) {
  user(login:$login) {
    projectV2(number:$number) { id }
  }
}
"@
    $body = @{ query = $query; variables = @{ login = $login; number = $number } } | ConvertTo-Json
    $resp = Invoke-RestMethod -Method Post -Uri $graphql -Headers $headersGraph -Body $body -ContentType 'application/json'
    return $resp.data.user.projectV2.id
}

function Add-Item-To-Project($projectId, $contentId) {
    $mutation = @"
mutation($projectId:ID!, $contentId:ID!) {
  addProjectV2ItemById(input:{projectId:$projectId, contentId:$contentId}) {
    item { id }
  }
}
"@
    $body = @{ query = $mutation; variables = @{ projectId = $projectId; contentId = $contentId } } | ConvertTo-Json
    try {
        $resp = Invoke-RestMethod -Method Post -Uri $graphql -Headers $headersGraph -Body $body -ContentType 'application/json'
        return $resp.data.addProjectV2ItemById.item.id
    } catch {
        Write-Warning "Falha ao adicionar item ao project: $($_.Exception.Message)"
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
        if ($cfg.projectOwner) { $ProjectOwner = $cfg.projectOwner }
        if ($cfg.projectNumber) { $ProjectNumber = [int]$cfg.projectNumber }
        if ($cfg.milestones) { $milestones = $cfg.milestones }
        if ($cfg.labels) { $labels = $cfg.labels }
        if ($cfg.issues) { $issues = $cfg.issues }
    } catch {
        Write-Warning "Falha ao ler config.json: $($_.Exception.Message). Usando valores padrão embutidos."
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

# criar milestones
$milestoneMap = @{}
foreach ($m in $milestones) { $num = Ensure-Milestone -title $m.title -due_on $m.due_on -description $m.description; if ($num) { $milestoneMap[$m.title] = $num } }

# criar labels
foreach ($l in $labels) { Ensure-Label -name $l.name -color $l.color -description $l.description }

# obter project id
Write-Host "Obtendo Project v2 id para $ProjectOwner / #$ProjectNumber ..."
$projectId = Get-ProjectV2Id -login $ProjectOwner -number $ProjectNumber
if (-not $projectId) { Write-Warning "Não foi possível obter o Project v2 id. Verifique o owner/number e permissões." }
else { Write-Host "Project v2 id: $projectId" }

# criar issues e adicionar ao project
$createdUrls = @()
foreach ($it in $issues) {
    $mNum = $null
    if ($it.milestone -and $milestoneMap.ContainsKey($it.milestone)) { $mNum = $milestoneMap[$it.milestone] }
    $res = Create-IssueReturnNode -title $it.title -bodyText $it.body -labels $it.labels -milestoneNumber $mNum
    if ($res -and $res.node_id) {
        if ($projectId) {
            Write-Host "Adicionando issue (#$($res.number)) ao project..."
            $itemId = Add-Item-To-Project -projectId $projectId -contentId $res.node_id
            if ($itemId) { Write-Host "Item adicionado ao project (item id: $itemId)" }
            else { Write-Warning "Falha ao adicionar issue ao project." }
        }
        $createdUrls += $res.url
    }
}

Write-Host "\nConcluído. Issues criadas: $($createdUrls.Count)"
$createdUrls | ForEach-Object { Write-Host $_ }

Write-Host "\nFim do script."