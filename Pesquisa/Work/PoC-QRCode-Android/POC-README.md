# POC README — QR Code para identificação de projetores

## Objetivo

Demonstrar, em um app Android nativo escrito em **Java**, o ciclo completo de
identificação por QR Code de um projetor de uso coletivo:

1. **gerar** um QR Code com o identificador de patrimônio do projetor;
2. **ler** esse QR Code pela câmera do celular ou por uma imagem da galeria;
3. **recuperar exatamente o mesmo valor** que foi gravado;
4. **expor a leitura por um serviço HTTP** embarcado no próprio celular, para
   que um script no PC consiga consumir o patrimônio lido em JSON.

O PoC também serve de base para a **RN8** ("Cada projetor deve ser identificado
por número único e código de barras"), documentada em
[`CODIGO-DE-BARRAS.md`](CODIGO-DE-BARRAS.md).

## Status dos itens da Issue #4

Legenda: `[x]` feito · `[~]` fora do escopo, com justificativa · `[ ]` pendente.

- [x] Criar a aplicação Android em Java e definir a versão mínima do Android.
- [x] Escolher e registrar as bibliotecas Android de geração e leitura.
- [x] Definir o formato do identificador de patrimônio do projetor.
- [x] Implementar a geração de QR Code para o identificador.
- [x] Implementar a leitura a partir de imagem ou câmera.
- [x] Testar e documentar código de barras quando suportado pela biblioteca.
- [x] Expor um endpoint REST no aplicativo para solicitar a leitura e retornar
      o patrimônio em JSON.
- [x] Criar uma chamada de teste para consumir o endpoint e registrar a resposta.
- [x] Registrar exemplos, evidências e limitações.
- [x] Criar ou atualizar o README com instruções de execução.

**Entregáveis e aceite:**

- [x] Código-fonte Android, exemplos de códigos gerados, README, evidências de
      leitura e relação das bibliotecas.
- [x] A PoC gera QR Codes de patrimônio e recupera o mesmo valor na leitura
      (7 de 7 imagens decodificadas — ver [Evidências](#evidências)).
- [~] Google Apps Script chamando o serviço REST.
      **Fora do escopo por orientação do professor.** O serviço REST está
      implementado e foi validado com o `test-api.ps1`; qualquer cliente HTTP
      (Apps Script inclusive) pode consumi-lo.
- [x] Passos reproduzíveis pelo README.

### Divergências registradas

| Item | O que a issue pede | O que a PoC entrega | Situação |
| --- | --- | --- | --- |
| Protocolo do serviço | "endpoint REST **HTTPS**" | HTTP na rede local (porta 8787) | Divergência assumida: o script de teste usa `http://` e um certificado autoassinado quebraria a chamada no PowerShell. Servir TLS continua como próximo passo. |
| Identificador | Comentário: "O ID do equipamento será um texto de 15 caracteres" | `projetor:<número>`, com número livre (ex.: `1`, `12`, `P-04`) | Pendente de confirmação. O prefixo `projetor:` já ocupa 9 caracteres; um ID fixo de 15 exigiria mudar o formato e o cadastro. |
| Código de barras 1D | "Testar e documentar código de barras quando suportado" | Apenas QR Code implementado; 1D **documentado**, não implementado | Fora do escopo combinado. Ver [`CODIGO-DE-BARRAS.md`](CODIGO-DE-BARRAS.md). |

## Tecnologias

- **Linguagem/plataforma:** Java 8 (source/target), Android `minSdk 21`,
  `targetSdk 34`, `compileSdk 34`.
- **Bibliotecas:**
  - `com.google.zxing:core:3.5.3` — geração do QR Code e leitura de imagem estática;
  - `com.journeyapps:zxing-android-embedded:4.3.0` — tela de câmera pronta;
  - `androidx.exifinterface:exifinterface:1.3.7` — rotação da foto antes da leitura;
  - `org.nanohttpd:nanohttpd:2.3.1` — servidor HTTP embarcado;
  - `androidx.appcompat`, `material`, `recyclerview`, `constraintlayout` — interface;
  - `junit`, `robolectric`, `androidx.test:core`, `org.json` — testes unitários.
- **Hardware:** celular Android com câmera (testado em Redmi Note 13, Android 13 /
  API 33) e um PC na **mesma rede Wi-Fi** para consumir o serviço.

## Pré-requisitos

- **JDK 21** (`JAVA_HOME` configurado).
- **Android SDK 34** com `platforms;android-34` e `build-tools;34.0.0`
  (`ANDROID_HOME` configurado).
- **Gradle 8.9** instalado (o projeto não versiona o Gradle Wrapper).
- Celular com **Depuração USB** ativada (para instalar via `adb`) **ou** o APK
  copiado e instalado manualmente.
- PC e celular na mesma rede, com o Wi-Fi do celular permitindo conexões de
  entrada (redes de visitante e alguns roteadores isolam os dispositivos).
- No PC: **PowerShell 5.1+** para rodar o `test-api.ps1`.

## Instalação

```bash
# 1. Compilar, testar e gerar o APK de release
gradle testDebugUnitTest
gradle lintDebug
gradle assembleRelease      # gera app/build/outputs/apk/release/app-release.apk

# 2. Instalar no celular conectado por USB
adb install -r app/build/outputs/apk/release/app-release.apk
```

A assinatura de release é opcional para testar: sem o `keystore.properties` o
build cai para a assinatura de debug. Para assinar de verdade, copie
`keystore.properties.sample` para `keystore.properties` e preencha os valores.
**Nem o `keystore.properties` nem o `.jks` vão para o Git** (estão no
`.gitignore`).

## Configuração

Tudo é configurável **em tempo de execução**, na tela **"Serviço da API"** do
app — nada de IP, porta ou token fixo no código.

| Item | Onde fica | Padrão |
| --- | --- | --- |
| Porta | campo "Porta" da tela do serviço | `8787` |
| Token | campo "Token (Bearer)" + botão "Copiar token" | gerado no primeiro uso: 32 bytes aleatórios em Base64 |
| Identificador do aparelho | gerado no primeiro uso (`apk-<n>-<hex>`) e mostrado em `/health` | automático |

O token **nunca** é versionado. No PC, salve o token copiado do app em um arquivo
`appnfc-test-token.txt` (uma linha, sem espaços) e passe esse caminho ao script.

> **Atenção:** o token do app e o conteúdo do `appnfc-test-token.txt` precisam ser
> **idênticos**. Token diferente devolve `403 forbidden`; token ausente devolve
> `401 unauthorized`.

## Como executar

1. Abra o app **Aluguel de Projetores** no celular.
2. Toque em **"Serviço da API"**.
3. Confira a porta (`8787`) e o token; toque em **"Iniciar serviço"**.
   - O Android mostra uma notificação permanente. Ela é necessária para o
     sistema não fechar a porta quando o app vai para segundo plano.
   - Se aparecer "Não subiu: ...", leia a mensagem: porta ocupada, permissão de
     notificação negada ou outro app usando a mesma porta.
4. Anote o **IP** mostrado na tela (ex.: `192.168.3.89`) e o comando de teste
   que o app já monta.
5. No PC, na pasta do projeto:

   ```powershell
   .\test-api.ps1 -Ip 192.168.3.89 -Port 8787 -TokenPath .\appnfc-test-token.txt
   ```

6. Para pedir uma leitura de QR Code:

   ```powershell
   .\test-api.ps1 -Ip 192.168.3.89 -Port 8787 -TokenPath .\appnfc-test-token.txt -ReadQr
   ```

   O celular abre a câmera (ou a tela do app pede que você toque em
   **"Ler QR Code agora"**, caso o Android bloqueie a abertura em segundo plano).
   Aponte para o QR Code do projetor.

7. Para ver o log de auditoria do serviço:

   ```powershell
   .\test-api.ps1 -Ip 192.168.3.89 -Port 8787 -TokenPath .\appnfc-test-token.txt -Logs -Limit 20
   ```

## Endpoints do serviço

Todos exigem `Authorization: Bearer <token>`. O cabeçalho `X-Correlation-Id`
recebido é ecoado na resposta (e aparece no log) para casar pedido e resposta.
As respostas são sempre JSON UTF-8, com `Cache-Control: no-store`.

| Método | Rota | Resposta |
| --- | --- | --- |
| `GET` | `/health` | `{ status, service, version, device{model,manufacturer,android,sdk,serial}, endpoints, uptimeMs }` |
| `POST` | `/v1/qr/read` | `{ status:"ok", text, format:"QR_CODE", durationMs }` ou `408` com `{ status:"timeout", message, text:null }` |
| `GET` | `/v1/audit/logs?limit=1..50&offset=0..10000` | `{ total, limit, offset, items:[{timestamp,method,path,status,correlationId,durationMs,query?}] }` |
| `POST` | `/v1/nfc/read` | `501` `{ status:"not_implemented", ... }` — mantido só para o contrato do script |

Códigos de erro: `401` sem `Authorization` ou formato inválido; `403` token
errado; `400` parâmetros de `limit`/`offset` inválidos; `404` rota inexistente;
`500` falha interna.

Exemplo de `curl` (Windows/PowerShell usa `curl.exe`):

```bash
curl.exe -H "Authorization: Bearer SEU_TOKEN" http://192.168.3.89:8787/health
```

## Formato do identificador

```
projetor:<número>
```

Regras de normalização (`Projetor.normalizarNumero`):

- espaços nas pontas são removidos; **espaço no meio é rejeitado** (`"4 5"` não
  vira `"45"`);
- só são aceitos `0-9`, `A-Z`, `a-z`, `-`, `_` e `.`;
- se for só dígitos, zeros à esquerda somem (`"04"` → `"4"`, `"000"` → `"0"`);
- letras viram maiúsculas (`"p-04"` → `"P-04"`).

Por que o prefixo: o app lê **qualquer** QR Code pela câmera. Sem `projetor:`,
uma URL ou um Pix lido por engano seria tratado como número de projetor. Com o
prefixo, o app sabe na hora se o código é dele.

## Testes e validação

### Testes automatizados

```bash
gradle testDebugUnitTest
```

**49 testes, 0 falhas**, distribuídos em:

| Classe | Testes | Cobre |
| --- | --- | --- |
| `QrCodeImagemTest` | 8 | Geração, leitura por imagem, JPEG, EXIF |
| `ProjetorNormalizacaoTest` | 10 | Normalização e validação do identificador |
| `ProjetorOrdenacaoTest` | 8 | Ordenação da lista |
| `ProjetorStorageTest` | 12 | Persistência, duplicidade, cadastro fantasma |
| `ServicoHttpTest` | 11 | Rotas, autenticação, limites de `limit`/`offset`, timeout |

`gradle lintDebug` termina sem erros (restam apenas avisos de estilo).

### Validação manual do QR

1. Cadastre um projetor (ex.: `4`) e abra a tela dele.
2. Confira o QR Code exibido.
3. Toque em **"Ler QR Code de uma imagem"** e escolha uma foto desse QR (ou use a
   câmera). A tela do projetor `4` deve abrir.
4. Teste um QR que **não** é de projetor (uma URL). O app deve recusar com uma
   mensagem clara, sem cadastrar nada.

### Validação do serviço

```powershell
# conexão + versão + aparelho
.\test-api.ps1 -Ip <IP_DO_CELULAR> -TokenPath .\appnfc-test-token.txt

# leitura pela câmera
.\test-api.ps1 -Ip <IP_DO_CELULAR> -TokenPath .\appnfc-test-token.txt -ReadQr

# auditoria
.\test-api.ps1 -Ip <IP_DO_CELULAR> -TokenPath .\appnfc-test-token.txt -Logs
```

Teste real realizado: PC em `192.168.3.90` → celular em `192.168.3.89:8787`.
Com token **diferente** do configurado no app, o serviço respondeu
`403 {"status":"forbidden"}`, provando que a porta está aberta, o JSON sai
correto e a autenticação está barrando quem não tem o token. Com os tokens
alinhados, o `/health` responde `200`.

## Evidências

As imagens em [`evidencias/`](evidencias/) foram geradas com **os mesmos
parâmetros do app** (600 × 600 px, correção de erro **H**, margem 1) e depois
**lidas de volta** com o ZXing, provando o critério de aceite "gerar e recuperar
o mesmo valor".

| Arquivo | Conteúdo gravado | Leitura de volta |
| --- | --- | --- |
| `qr-projetor-1.png` | `projetor:1` | ✅ `projetor:1` (`QR_CODE`) |
| `qr-projetor-2.png` | `projetor:2` | ✅ `projetor:2` (`QR_CODE`) |
| `qr-projetor-3.png` | `projetor:3` | ✅ `projetor:3` (`QR_CODE`) |
| `qr-projetor-12.png` | `projetor:12` | ✅ `projetor:12` (`QR_CODE`) |
| `qr-projetor-P-04.png` | `projetor:P-04` | ✅ `projetor:P-04` (`QR_CODE`) |
| `qr-https-exemplo-com-nao-e-projetor.png` | `https://exemplo.com/nao-e-projetor` | ✅ mesmo texto — **rejeitado pelo app** (sem prefixo) |
| `qr-BEM-VINDO-UNIFOR.png` | `BEM-VINDO-UNIFOR` | ✅ mesmo texto — **rejeitado pelo app** (sem prefixo) |

Resultado da verificação: **7 de 7 imagens decodificadas**. As duas últimas
existem justamente para provar que o filtro do prefixo funciona: o QR é lido,
mas o app não o trata como projetor.

## Resultados esperados

- O app cadastra projetores e mostra um QR Code único por projetor.
- A leitura (câmera ou imagem) devolve **o mesmo identificador** que foi gerado.
- QR Codes que não começam com `projetor:` são recusados com mensagem, sem
  cadastro acidental.
- O serviço abre a porta configurada, exige Bearer token e devolve JSON.
- `POST /v1/qr/read` devolve `200` com o patrimônio lido, ou `408` se ninguém
  atender a câmera em aproximadamente 30 s.
- A leitura de hostilidade básica funciona: sem token → `401`; token errado →
  `403`; rota inexistente → `404`.

## Limitações conhecidas

- **HTTP, não HTTPS.** O tráfego na rede local não é criptografado. Em rede
  compartilhada, o token pode ser observado por quem estiver no mesmo Wi-Fi.
  Mitigação atual: token longo, rotável e nunca versionado; uso em rede
  confiável. TLS com certificado próprio é o próximo passo.
- **Serviço em primeiro plano.** A porta só fica aberta com o app em execução e
  a notificação permanente ativa. O Android pode bloquear a abertura da câmera
  em segundo plano; nesse caso o pedido fica pendente até o usuário tocar em
  "Ler QR Code agora" ou expira em `408`.
- **Um leitor por vez.** `FilaLeituraQr` atende uma solicitação por vez; dois PCs
  pedindo leitura ao mesmo tempo esperam na fila.
- **Log em memória.** `/v1/audit/logs` guarda no máximo 200 registros e perde
  tudo quando o serviço é reiniciado.
- **Sem assinatura no QR.** O conteúdo é texto puro; um QR forjado com
  `projetor:999` é lido como o projetor 999. O app não confere contra o cadastro
  antes de abrir a tela.
- **Sem TLS, sem usuários e sem sincronização.** Os dados ficam só no aparelho
  (`SharedPreferences`); desinstalar apaga o inventário.
- **Código de barras 1D não implementado.** Só QR Code. Ver
  [`CODIGO-DE-BARRAS.md`](CODIGO-DE-BARRAS.md).
- **Formato do ID diferente do comentário da issue.** O comentário pede 15
  caracteres; a PoC usa `projetor:<número>`.
- **NFC não implementado.** `POST /v1/nfc/read` responde `501` de propósito.

## Próximos passos

- Servir o endpoint com **HTTPS** (certificado próprio instalado no aparelho ou
  TLS terminado por um proxy).
- Assinar o conteúdo do QR (HMAC com chave do servidor) para impedir QR forjado.
- Conferir o número lido contra o inventário antes de abrir o aluguel.
- Implementar **EAN-13** ou **Code 128** para atender a RN8 ao pé da letra
  (avaliar qual formato cabe no etiquetamento físico dos projetores).
- Gerar etiquetas em PDF para impressão e colagem nos equipamentos.
- Persistir o log de auditoria em arquivo/banco em vez de memória.
- Alinhar o formato do identificador com o professor (15 caracteres fixos?).

## Referências

- Issue #4 — *PoC: QR/Barcode (geração e leitura)*:
  <https://github.com/marcelobezerra-dotcom/Projeto-Requisitos-GAC/issues/4>
- ISO/IEC 18004:2024 — especificação do QR Code.
- ISO/IEC 15417:2007 — especificação do Code 128.
- GS1 — especificação do EAN-13 / GTIN-13.
- Resumo de pesquisa do grupo:
  [`../resumo-pesquisa-QR-Barcode.md`](../resumo-pesquisa-QR-Barcode.md)
- Regra de negócio RN8:
  [`../../../Requisitos/Casos de Uso/regrasDeNegocio-ProjetoresUnifor.md`](../../../Requisitos/Casos%20de%20Uso/regrasDeNegocio-ProjetoresUnifor.md)
- ZXing — <https://github.com/zxing/zxing>
- NanoHTTPD — <https://github.com/NanoHttpd/nanohttpd>
