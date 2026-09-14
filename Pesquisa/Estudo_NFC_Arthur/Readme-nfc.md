# Pesquisa NFC — Projeto GAC (Locação de Projetores)

Resumo técnico da parte de **NFC** do projeto GAC (Unifor), responsabilidade de Arthur. Cobre os itens do checklist da issue: tipos de tag, hardware, alternativas de simulação e APIs/bibliotecas.

---

## 1. Hardware utilizado / avaliado

| Componente | Função no projeto | Observações |
| --- | --- | --- |
| **PN532** (módulo leitor/gravador NFC) | Leitor a ser embarcado no balcão de retirada | Interfaces I2C, SPI ou UART (HSU); alcance ~5–10 cm; suporta ISO 14443A/B, FeliCa e ISO 18092 (NFC P2P) |
| **ESP32** (ou Raspberry Pi) | Gateway: lê a UID via PN532 e envia para a API via Wi-Fi | Alternativa mais barata e a mais recomendada para o PoC |
| **ACR1552U** (leitor USB de mesa) | Alternativa plug-and-play para um PC fixo no balcão | Compatível PC/SC, sem driver; suporta modo emulação de teclado. Não é vendido em loja nacional — item de importação |
| **NTAG213 / NTAG215 / NTAG216** (tags) | Cartão/chaveiro de identificação do professor | Baratas, com NDEF, lidas nativamente por qualquer celular Android/iPhone |
| **Mifare Classic** | Alternativa de tag mais antiga | Já tem vulnerabilidades de segurança conhecidas; NTAG é preferível para um projeto novo |

**Simulação sem hardware físico:** caso o kit não chegue a tempo, é possível simular a leitura com um script que gera UIDs falsas para testar a lógica de API/negócio, documentando como "modo simulado" no PoC (aceito pela Definição de Done do plano Scrum).

---

## 2. Meio de comunicação (padrões e protocolos)

- **Frequência:** NFC opera em **13,56 MHz**, que é a faixa **HF** dentro do espectro de RFID.
- **Comparação de faixas (contexto para o resumo de pesquisa):**
  - **LF (125 kHz):** tags simples (EM4100), sem criptografia, só leitura de UID.
  - **HF / NFC (13,56 MHz):** ISO 14443A/B, ISO 15693, FeliCa — suporta NDEF e é lido nativamente por celulares. **Faixa escolhida para este projeto.**
  - **UHF (860–960 MHz):** alcance de metros, usado em logística; hardware mais caro, não necessário aqui.
- **Padrões relevantes:** ISO/IEC 14443 Tipo A e B, ISO/IEC 15693, ISO/IEC 18092 (NFC), FeliCa.
- **Modos de operação NFC:**
  - ✅ **Reader/Writer** — modo escolhido. O leitor (fixo, ativo) lê uma tag passiva (o crachá do professor). Simples, barato e é o padrão usado em crachás de controle de acesso corporativo.
  - ⚠️ **Card Emulation** — alternativa possível se, no futuro, quiserem que o professor use o próprio celular como identificação (exige HCE no app). Não implementado no escopo atual.
  - ❌ **Peer-to-Peer (P2P)** — descartado. Exige dois dispositivos ativos trocando dados (como o antigo Android Beam); o crachá do professor é passivo, então P2P não se aplica ao fluxo do projeto.
- **Interfaces físicas:**
  - PN532 → I2C, SPI ou UART (HSU) até o gateway (ESP32/RaspberryPi).
  - ACR1552U → USB 2.0, compatível PC/SC (driverless).

---

## 3. Módulos possíveis de implementar com o hardware

1. **Leitura via PN532 + ESP32 → API REST**
   Gateway lê a UID da tag e faz um `POST` (Wi-Fi) para o backend, que registra retirada/devolução.
2. **Leitor USB + modo emulação de teclado (ACR1552U)**
   O leitor "digita" a UID como se fosse um teclado — um campo de input numa página web comum já captura a leitura, sem precisar de driver nem de código custom.
3. **Modo simulado (fallback)**
   Script que gera UIDs de teste para validar a lógica de API antes do hardware chegar.

---

## 4. Bibliotecas e APIs

| Biblioteca/API | Linguagem/Ambiente | Uso |
| --- | --- | --- |
| **libnfc** | C (Linux) | Biblioteca de baixo nível para PN532 em Raspberry Pi |
| **nfcpy** | Python | Prototipagem rápida, fácil integração com Flask/FastAPI |
| **Adafruit PN532 library** | Arduino/ESP32 (C++) | Biblioteca direta para o caminho embarcado, suporta I2C/SPI/UART |
| **Web NFC API** | JavaScript (navegador) | Só funciona em Chrome/Chromium no **Android** (não há suporte em iOS, Safari, Firefox ou navegadores desktop) — por isso não é viável como solução principal do balcão físico |

---

## 5. Ferramentas de apoio

- **NFC Tools Desktop** — para ler/gravar tags via ACR1552U sem precisar programar.
- **PC/SC (pcsc-lite/libccid)** — driver padrão usado por leitores USB tipo ACR1552U em Windows/Mac/Linux.
- **Ficha de leitura** (`Pesquisa/Biblioteca/ficha-de-leitura-template.md` no repositório) — usar para catalogar toda fonte/artigo consultado, conforme pedido na issue.

---

## 6. Comparativo de custo (valores em Real, pesquisados em set/2026)

| Solução | Custo aproximado | Observação |
| --- | --- | --- |
| PN532 (kit) + ESP32 | ~R$ 40 a R$ 150 | Vendido por lojas nacionais (Usinainfo, WJ Componentes, PiscaLED) |
| ACR1552U (leitor USB) | ~R$ 650 a R$ 900 (importado) | Não há revenda nacional; preço já considera frete + impostos de importação |

---

## 7. Recomendação

Para o escopo e prazo do projeto (apresentação em 13/11/2026, grupo de estudo com orçamento limitado):

- **Hardware:** PN532 + ESP32 como gateway.
- **Tags:** NTAG213/215.
- **Modo NFC:** Reader/Writer.
- **Biblioteca:** Adafruit PN532 (Arduino/ESP32) para o firmware; endpoint REST simples no backend para registrar UID + horário de retirada/devolução.

O ACR1552U fica como alternativa documentada (útil citar no resumo como "opção plug-and-play para PC fixo"), mas não é a rota recomendada por custo e prazo de importação
