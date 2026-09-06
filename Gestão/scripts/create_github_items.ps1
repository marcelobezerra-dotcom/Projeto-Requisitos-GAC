<#
Script para automatizar criação de milestones, labels e issues no repositório
Requisitos:
- GitHub CLI `gh` instalado e autenticado (gh auth login)
- Permissões de escrita no repositório `marcelobezerra-dotcom/Projeto-Requisitos-GAC`

Uso:
PS> .\create_github_items.ps1
#>
param()

$repoFull = "marcelobezerra-dotcom/Projeto-Requisitos-GAC"
$projectUrl = "https://github.com/users/marcelobezerra-dotcom/projects/9" # usado só para referência

# checa `gh`
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Write-Error "GitHub CLI 'gh' não encontrado. Instale e autentique com 'gh auth login'."
    exit 1
}

$parts = $repoFull -split '/' 
$owner = $parts[0]
$repo = $parts[1]

# Milestones (sprints)
$milestones = @(
    @{ title = 'Sprint 1'; due_on = '2026-09-19T23:59:00Z'; description = 'Kickoff e pesquisas iniciais' },
    @{ title = 'Sprint 2'; due_on = '2026-10-03T23:59:00Z'; description = 'PoC individuais' },
    @{ title = 'Sprint 3'; due_on = '2026-10-17T23:59:00Z'; description = 'Revisão técnica e início do SDD' },
    @{ title = 'Sprint 4'; due_on = '2026-10-31T23:59:00Z'; description = 'Design detalhado (SDD) e integração' },
    @{ title = 'Sprint 5'; due_on = '2026-11-13T23:59:00Z'; description = 'Finalização e apresentação' }
)

$milestoneMap = @{}
foreach ($m in $milestones) {
    Write-Host "Criando milestone: $($m.title) ..."
    $respRaw = gh api -X POST /repos/$owner/$repo/milestones -f title="$($m.title)" -f description="$($m.description)" -f due_on="$($m.due_on)" 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Falha ao criar milestone '$($m.title)'. Pode já existir. Tentando obter existente..."
        $existing = gh api /repos/$owner/$repo/milestones --paginate | ConvertFrom-Json | Where-Object { $_.title -eq $m.title }
        if ($existing) { $milestoneMap[$m.title] = $existing.number; continue }
        else { Write-Warning "Não foi possível criar ou localizar milestone '$($m.title)'"; continue }
    }
    $resp = $respRaw | ConvertFrom-Json
    $milestoneMap[$m.title] = $resp.number
}

# Labels
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

foreach ($l in $labels) {
    Write-Host "Criando label: $($l.name) ..."
    # verifica se existe
    $exists = gh api /repos/$owner/$repo/labels/$($l.name) 2>$null
    if ($LASTEXITCODE -eq 0) { Write-Host "Label '$($l.name)' já existe, pulando."; continue }
    gh api -X POST /repos/$owner/$repo/labels -f name="$($l.name)" -f color="$($l.color)" -f description="$($l.description)"
}

# Issues iniciais (backlog)
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

$createdIssues = @()
foreach ($it in $issues) {
    Write-Host "Criando issue: $($it.title) ..."
    $mNumber = $null
    if ($it.milestone -and $milestoneMap.ContainsKey($it.milestone)) { $mNumber = $milestoneMap[$it.milestone] }
    $payload = @{ title = $it.title; body = $it.body }
    if ($it.labels) { $payload.labels = $it.labels }
    if ($mNumber) { $payload.milestone = $mNumber }
    $json = $payload | ConvertTo-Json -Depth 4
    $respRaw = gh api -X POST /repos/$owner/$repo/issues -f title="$($it.title)" -f body="$($it.body)" -F labels=@($($it.labels -join ','))  -f milestone=$mNumber 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Falha ao criar issue '$($it.title)'. Verifique permissões ou se já existe."; continue
    }
    $resp = $respRaw | ConvertFrom-Json
    $createdIssues += $resp.html_url
    Write-Host "Criado: $($resp.html_url)"
}

Write-Host "\nResumo: issues criadas ($($createdIssues.Count)):\n"
$createdIssues | ForEach-Object { Write-Host $_ }

Write-Host "\nATENÇÃO: o script cria milestones, labels e issues no repositório. Para adicionar as issues automaticamente ao Project Kanban (user project), pode ser necessário o uso de API GraphQL ou comandos específicos do 'gh' com IDs do projeto."
Write-Host "Sugestão: após execução, abra o Project em: $projectUrl e adicione os items criados. Se desejar, posso gerar comandos adicionais para tentar adicionar itens ao project via 'gh' (precisa confirmar acesso e tipo do projeto)."

# fim do script
