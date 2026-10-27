<#
    test-api.ps1 - Cliente de teste do Servico de QR Code (App "Aluguel de Projetores").

    O CELULAR e o servidor (app Android, porta 8787). ESTE script roda no PC e
    faz os pedidos HTTP. PC e celular precisam estar na MESMA rede.

    Como usar (exemplos):
        .\test-api.ps1 -Ip 192.168.0.104 -TokenPath .\appnfc-test-token.txt
        .\test-api.ps1 -Ip 192.168.0.104 -Token "COLE_O_TOKEN_AQUI" -ReadQr
        .\test-api.ps1 -Ip 192.168.0.104 -TokenPath .\appnfc-test-token.txt -Logs

    Sem nenhuma acao o script so faz o /health (teste de conexao).
#>
[CmdletBinding()]
param(
    # IP do celular (aparece na tela "Servico da API" do app).
    [Parameter(Mandatory = $true)]
    [string]$Ip,

    # Porta configurada no app. O padrao do projeto e 8787.
    [int]$Port = 8787,

    # Caminho do arquivo com o token (o mesmo appnfc-test-token.txt).
    [string]$TokenPath,

    # Token escrito direto na linha de comando (alternativa ao -TokenPath).
    [string]$Token,

    # Pede uma leitura de QR Code: abre a camera no celular e espera o resultado.
    [switch]$ReadQr,

    # Mostra o log de auditoria do servico.
    [switch]$Logs,

    # Quantidade de registros do log (1 a 50).
    [int]$Limit = 20
)

$ErrorActionPreference = 'Stop'
$base = "http://$Ip`:$Port"

function Escrever-Titulo($texto) {
    Write-Host ""
    Write-Host "== $texto ==" -ForegroundColor Cyan
}

function Obter-Token {
    if ($Token) { return $Token.Trim() }
    if ($TokenPath -and (Test-Path -LiteralPath $TokenPath)) {
        # -Raw para nao perder nada e .Trim() para tirar quebra de linha no fim.
        return (Get-Content -LiteralPath $TokenPath -Raw).Trim()
    }
    if ($TokenPath) {
        Write-Host "Arquivo de token nao encontrado: $TokenPath" -ForegroundColor Yellow
    }
    # Nao deu para achar o token: pergunta na tela (o texto fica escondido).
    Write-Host "Cole o token do app (botao 'Copiar token') e aperte Enter:" -ForegroundColor Yellow
    $lido = Read-Host -AsSecureString
    return ([System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
            [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($lido))).Trim()
}

function Invoke-Api {
    param(
        [string]$Metodo,
        [string]$Caminho,
        [hashtable]$Cabecalhos
    )
    $url = "$base$Caminho"
    Write-Host "> $Metodo $url" -ForegroundColor DarkGray
    # Timeout de 60s: o servidor responde no maximo em ~35s (leitura do QR).
    return Invoke-RestMethod -Uri $url -Method $Metodo -Headers $Cabecalhos -TimeoutSec 60
}

# --- Fluxo principal ---------------------------------------------------------
$token = Obter-Token
$cabecalhos = @{ Authorization = "Bearer $token" }

try {
    Escrever-Titulo "1) /health  (conexao PC -> celular)"
    $health = Invoke-Api -Metodo 'GET' -Caminho '/health' -Cabecalhos $cabecalhos
    Write-Host "Tudo certo! Servico: $($health.service) v$($health.version)" -ForegroundColor Green
    Write-Host "Aparelho : $($health.device.manufacturer) $($health.device.model) (Android $($health.device.android))"
    Write-Host "Endpoints: $($health.endpoints -join ', ')"

    if ($ReadQr) {
        Escrever-Titulo "2) POST /v1/qr/read  (aponte a camera para o QR)"
        Write-Host "Aguardando leitura no celular (ate ~35s)..." -ForegroundColor Yellow
        $leitura = Invoke-Api -Metodo 'POST' -Caminho '/v1/qr/read' -Cabecalhos $cabecalhos
        Write-Host "status: $($leitura.status)" -ForegroundColor Green
        Write-Host "texto : $($leitura.text)"
        Write-Host "formato: $($leitura.format)  ($($leitura.durationMs) ms)"
    }

    if ($Logs) {
        Escrever-Titulo "3) GET /v1/audit/logs"
        $log = Invoke-Api -Metodo 'GET' -Caminho "/v1/audit/logs?limit=$Limit&offset=0" -Cabecalhos $cabecalhos
        Write-Host "total: $($log.total)  (mostrando $($log.items.Count))"
        $log.items | Format-Table timestamp, method, path, status -AutoSize
    }

    Write-Host ""
    Write-Host "Concluido sem erros." -ForegroundColor Green
}
catch {
    Write-Host ""
    Write-Host "FALHOU: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Checklist rapido:" -ForegroundColor Yellow
    Write-Host " - O servico esta 'Ativo' na tela do app? (botao Iniciar servico)"
    Write-Host " - PC e celular na MESMA rede? (Wi-Fi do celular x cabo do PC no mesmo roteador)"
    Write-Host " - IP correto? (veja 'Endereco: http://...' na tela do app)"
    Write-Host " - Token igual ao do app? (botao Copiar token)"
    Write-Host " - 401 = faltou token | 403 = token errado | 404 = rota inexistente"
    exit 1
}
