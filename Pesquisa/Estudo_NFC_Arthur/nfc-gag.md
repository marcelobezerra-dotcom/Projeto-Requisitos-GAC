# Pesquisa NFC — Projeto GAC (Locação de Projetores)

Resumo técnico da pesquisa e das decisões relacionadas à tecnologia **NFC** no projeto GAC (Unifor), sob responsabilidade de Arthur. O documento cobre os itens do checklist da issue: tipos de tags, hardware, protocolos, alternativas de simulação, bibliotecas, custos e recomendações para a prova de conceito (PoC).

## 1. Objetivo e aplicação no projeto

O objetivo é utilizar NFC para identificar o professor responsável pela retirada e devolução de projetores no balcão da instituição.

O sistema deverá permitir que o leitor identifique uma tag aproximada, envie os dados ao backend e registre a operação de locação. A aplicação será responsável por validar o vínculo entre o identificador e o professor, além de registrar os horários e o estado do equipamento.

**Fluxo proposto:**

1. O professor aproxima o crachá ou cartão NFC do leitor.
2. O leitor obtém o identificador da tag.
3. O gateway envia a leitura para a API do GAC.
4. O backend valida o cadastro e as regras de locação.
5. O sistema registra a retirada ou devolução e apresenta o resultado.

A leitura NFC identifica uma tag, mas não deve, isoladamente, autorizar uma operação sensível. A validação das permissões deve ocorrer no backend.

---

## 2. Hardware utilizado / avaliado

| Componente | Função no projeto | Observações |
|---|---|---|
| **PN532** | Leitor/gravador NFC no balcão | Módulo com interfaces I2C, SPI ou UART, conforme a placa. Suporta tecnologias NFC específicas, incluindo ISO/IEC 14443 Type A e comunicação NFC compatível com os modos implementados pelo dispositivo. |
| **ESP32** | Gateway de comunicação | Lê a tag por meio do PN532 e envia os dados à API usando Wi-Fi. É a alternativa recomendada para a PoC. |
| **Raspberry Pi** | Gateway alternativo | Permite utilizar Linux e bibliotecas como `libnfc` ou `nfcpy`, conforme o leitor e o sistema operacional. |
| **ACR1552U** | Leitor NFC USB para computador | Alternativa para um balcão com PC. Verificar a versão, o suporte a PC/SC, os drivers e os recursos disponíveis antes da compra. |
| **NTAG213 / NTAG215 / NTAG216** | Tags de identificação | Tags passivas de 13,56 MHz, adequadas para experimentos com registros NDEF, identificadores e URLs. |
| **MIFARE DESFire EV3** | Alternativa para autenticação mais robusta | Indicada para cenários que exigem recursos criptográficos e controle de acesso mais avançados, com implementação compatível no leitor e no backend. |
| **MIFARE Classic 1K** | Alternativa para testes e sistemas legados | Possui vulnerabilidades de segurança conhecidas. Não é a primeira escolha para um projeto novo que exige proteção contra clonagem. |

### 2.1. Kit mínimo recomendado

Para desenvolver e validar a PoC, os componentes prioritários são:

- 1 módulo PN532 com interface documentada.
- 1 placa de desenvolvimento ESP32 com Wi-Fi.
- 5 a 10 tags NTAG213 para testes.
- Cabos jumper e cabo USB para programação.
- Protoboard, caso seja necessária uma montagem experimental.

Um display OLED é opcional e pode ser usado para apresentar mensagens como “Tag reconhecida”, “Professor não cadastrado” ou “Operação registrada”.

**Atenção:** o alcance de leitura depende da antena, do módulo, da tag e das condições de instalação. O alcance de alguns centímetros é esperado em muitas montagens NFC, mas não deve ser tratado como garantia fixa.

---

## 3. Tipos de tags NFC

A família NTAG21x é indicada para a primeira versão do projeto por oferecer tags passivas, compatibilidade com registros NDEF e suporte amplo em dispositivos NFC compatíveis.

| Modelo | Memória de usuário | Aplicação sugerida | Recomendação |
|---|---:|---|---|
| **NTAG213** | 144 bytes | Identificador curto, URL ou registro NDEF simples | Primeira escolha para a PoC |
| **NTAG215** | 504 bytes | Registros NDEF maiores e experimentos com mais dados | Alternativa com mais espaço |
| **NTAG216** | 888 bytes | Conteúdo maior armazenado diretamente na tag | Útil quando a memória adicional é necessária |
| **MIFARE Classic 1K** | 1 KB nominal | Experimentos e sistemas legados | Evitar como nova credencial de segurança |
| **MIFARE DESFire EV3** | Depende da variante | Credenciais com autenticação criptográfica | Considerar se a proteção contra clonagem for requisito |

Os valores de memória útil dos NTAG213, NTAG215 e NTAG216 são especificações da família de chips. A memória disponível para cada aplicação pode ser menor que a memória total do componente.

### 3.1. NTAG213 — recomendação principal

O NTAG213 é suficiente para o fluxo inicial do GAC. A tag pode armazenar um identificador ou uma URL curta, enquanto os dados do professor e as regras de locação permanecem no backend.

**Vantagens:**

- Custo normalmente baixo.
- Formatos físicos variados, incluindo adesivos, cartões e chaveiros.
- Compatibilidade com celulares que suportam NFC e a tecnologia correspondente.
- Suporte a NDEF para armazenar registros padronizados.
- Adequado para experimentos de leitura e gravação.

**Limitações:**

- Memória reduzida em comparação com modelos superiores.
- UID não deve ser tratado como segredo ou prova de autenticidade.
- Proteção por senha não equivale a autenticação criptográfica forte.
- A compatibilidade depende do leitor, do celular e do chip presente na tag adquirida.

### 3.2. NTAG215 e NTAG216

Esses modelos oferecem mais memória de usuário, mas isso não significa, por si só, maior segurança.

O NTAG215 pode ser útil quando os experimentos exigirem registros NDEF maiores. O NTAG216 pode ser escolhido quando houver necessidade concreta de armazenar mais conteúdo diretamente na tag.

Para o GAC, não é necessário armazenar os dados completos do professor, os detalhes da locação ou o histórico do equipamento na tag. Essas informações devem permanecer no banco de dados.

### 3.3. Formatos físicos

| Formato | Aplicação | Observações |
|---|---|---|
| Adesivo NFC | Prototipagem e identificação de objetos | Fácil aplicação, mas pode ser inadequado para superfícies metálicas sem uma versão específica. |
| Cartão PVC NFC | Crachá do professor | Formato prático para identificação individual. |
| Chaveiro NFC | Identificação portátil | Conveniente para uso frequente. |
| Tag anti-metal | Identificação em superfícies metálicas | Utiliza uma estrutura apropriada para reduzir a interferência do metal. |

O formato físico não determina o protocolo ou a segurança. Antes da compra, é necessário confirmar o chip, a frequência, o tipo de tag e a compatibilidade com o leitor.

---

## 4. Meio de comunicação: padrões e protocolos

### 4.1. Frequência

A tecnologia NFC opera em **13,56 MHz**, faixa classificada como HF (*High Frequency*) dentro do espectro de RFID.

### 4.2. Comparação das faixas

| Faixa | Frequência | Características | Aplicação típica |
|---|---|---|---|
| LF | Aproximadamente 125–134 kHz | Tags de identificação simples e sistemas específicos | Controle de acesso legado |
| HF / NFC | 13,56 MHz | Comunicação de proximidade; diferentes padrões e tecnologias de tags | Identificação, cartões e interação com celulares |
| UHF | Aproximadamente 860–960 MHz | Maior alcance, dependendo da implementação | Inventário e logística |

**Faixa escolhida para o GAC: HF/NFC a 13,56 MHz**, pela disponibilidade de tags, leitores e celulares compatíveis.

### 4.3. Padrões relevantes

- **ISO/IEC 14443 Type A e Type B:** padrões utilizados por cartões e dispositivos de proximidade.
- **ISO/IEC 15693:** tecnologia de cartões e tags de vizinhança, com características distintas das tags NFC de proximidade mais comuns.
- **ISO/IEC 18092 / NFCIP-1:** especificação relacionada à comunicação NFC entre dispositivos.
- **NFC Forum Type 2 Tag:** categoria relevante para tags como NTAG213, NTAG215 e NTAG216.
- **NDEF:** formato padronizado para organizar registros de dados, como texto e URLs, em dispositivos compatíveis.

Esses padrões não são intercambiáveis. Um módulo leitor pode suportar determinadas tecnologias sem suportar todas as categorias de tags e modos de operação.

### 4.4. Modos de operação NFC

- **Reader/Writer — escolhido:** o leitor ativo lê uma tag passiva, como o cartão do professor.
- **Card Emulation — alternativa futura:** um dispositivo compatível emula um cartão NFC. No Android, isso pode ser implementado por HCE em cenários suportados.
- **Peer-to-Peer — descartado:** dois dispositivos ativos trocam dados diretamente. Não é necessário para o fluxo de leitura de crachá proposto.

### 4.5. Interfaces físicas

- **PN532:** pode oferecer I2C, SPI ou UART/HSU, conforme o módulo.
- **ACR1552U:** conexão USB, com recursos e compatibilidade dependentes da versão.
- **ESP32:** comunicação com o PN532 por uma interface suportada pela placa, além de Wi-Fi para comunicação com o backend.

---

## 5. Arquitetura proposta para o GAC

### 5.1. Fluxo de hardware

1. A tag NFC é aproximada do PN532.
2. O leitor identifica a tag e obtém os dados disponíveis.
3. O ESP32 recebe o resultado pelo barramento configurado.
4. O ESP32 envia um evento à API REST do GAC.
5. O backend valida a identificação, as permissões e o estado do projetor.
6. O backend registra a retirada ou devolução e retorna o resultado.

### 5.2. Exemplo de evento enviado à API

O formato abaixo é ilustrativo e deve ser adaptado ao contrato real da API:

```json
{
  "tagId": "identificador-de-teste",
  "equipmentId": "PROJETOR-001",
  "operation": "retirada",
  "source": "pn532-esp32",
  "mode": "hardware"
}
```

O horário oficial da operação deve ser registrado pelo backend. O identificador do equipamento e o tipo de operação devem ser validados de acordo com as regras do sistema.

O exemplo não inclui credenciais, dados pessoais ou informações secretas.

### 5.3. Responsabilidades por camada

| Camada | Responsabilidade |
|---|---|
| Tag NFC | Fornecer o identificador ou os registros gravados |
| PN532 | Executar a comunicação NFC |
| ESP32 | Capturar a leitura e enviá-la à API |
| Backend | Validar permissões, regras de negócio e estado do equipamento |
| Banco de dados | Armazenar professores, equipamentos e histórico de locações |
| Interface web | Apresentar o resultado da operação |

---

## 6. Segurança e limitações

Para a PoC, o nível de segurança proposto é intermediário: o projeto deve impedir que uma leitura desconhecida seja automaticamente aceita como uma retirada ou devolução válida.

### 6.1. Cuidados necessários

- Não considerar a UID da tag uma credencial secreta.
- Não autorizar operações exclusivamente com base na UID.
- Validar o cadastro do professor e as permissões no backend.
- Validar se o projetor está disponível antes de registrar uma retirada.
- Impedir devoluções incompatíveis com o estado atual da locação.
- Usar HTTPS entre o gateway e a API em ambientes conectados.
- Proteger as credenciais utilizadas pelo gateway.
- Registrar eventos relevantes para auditoria.
- Bloquear a gravação da tag somente após concluir os testes de escrita, se isso for adequado ao modelo utilizado.

### 6.2. Limitações de segurança das tags

As tags NTAG213, NTAG215 e NTAG216 oferecem recursos como bloqueio de páginas e proteção por senha, mas não fornecem, por si só, autenticação criptográfica forte por operação.

Uma tag pode ser copiada ou simulada em determinadas condições. Portanto, a UID não comprova que o cartão físico original foi apresentado.

Se a evolução do GAC exigir maior resistência à clonagem, uma alternativa a avaliar é a família **NTAG 424 DNA**, que oferece recursos criptográficos adicionais. A implementação exige verificar a compatibilidade do leitor, as bibliotecas disponíveis e a validação criptográfica no backend.

---

## 7. Módulos possíveis de implementação

### 7.1. PN532 + ESP32 + API REST

**Opção recomendada para a PoC.**

O ESP32 lê a tag pelo PN532 e envia um evento por Wi-Fi ao backend.

Vantagens:

- Baixo custo relativo.
- Integração com uma API existente.
- Possibilidade de instalar o leitor no balcão.
- Separação entre hardware e regras de negócio.

### 7.2. Leitor USB + computador

Um leitor USB, como o ACR1552U, pode ser utilizado em um computador fixo no balcão.

A integração depende dos recursos da versão adquirida, incluindo suporte a PC/SC ou emulação de teclado, quando disponível. A emulação de teclado pode simplificar a captura da leitura em uma página web, mas deve ser validada com o equipamento real.

### 7.3. Modo simulado

Se o hardware não estiver disponível a tempo, um script poderá gerar identificadores fictícios para testar a API e as regras de negócio.

O modo simulado deve ser identificado explicitamente e não deve ser confundido com uma leitura NFC real.

Requisitos mínimos:

- Gerar identificadores de teste.
- Simular tags cadastradas e desconhecidas.
- Testar retiradas e devoluções válidas e inválidas.
- Validar respostas de erro da API.
- Manter o modo simulado separado do modo de hardware.

---

## 8. Bibliotecas e APIs

| Biblioteca/API | Linguagem / ambiente | Utilização e limitações |
|---|---|---|
| **Adafruit PN532** | Arduino/C++ | Biblioteca para integração do PN532 com placas compatíveis. Conferir a interface utilizada e o suporte à combinação específica de placa e módulo. |
| **libnfc** | C / Linux | Biblioteca de baixo nível para leitores e dispositivos suportados. A compatibilidade depende do hardware e da configuração. |
| **nfcpy** | Python | Útil para prototipagem e integração com aplicações Python. Verificar os leitores e protocolos suportados. |
| **NFC Tools** | Android / iOS / desktop, conforme a edição | Aplicativo ou ferramenta auxiliar para examinar e gravar registros NDEF em dispositivos compatíveis. |
| **Web NFC API** | JavaScript / navegadores compatíveis | Suporte restrito; a implementação mais conhecida é no Chrome para Android. Não deve ser a solução principal para um balcão que precisa funcionar em diferentes plataformas. |
| **NfcAdapter** | Android / Kotlin ou Java | Permite implementar a leitura NFC em um aplicativo Android compatível. |
| **Core NFC** | iOS / Swift | Permite determinadas operações NFC em aplicativos iOS, sujeitas às APIs, permissões e limitações da plataforma. |

A seleção da biblioteca deverá ser confirmada por meio de um teste de leitura real com o módulo adquirido.

---

## 9. O celular como leitor

Um smartphone compatível pode funcionar como leitor NFC, sendo uma alternativa para testes e para uma eventual estação fixa.

| Papel | Android | iPhone |
|---|---|---|
| Ler uma tag física | Possível por aplicativo compatível ou recursos do sistema | Possível por aplicativo compatível e APIs disponíveis |
| Gravar registros NDEF | Possível em aparelhos e aplicativos compatíveis | Possível em cenários suportados pelo Core NFC |
| Funcionar como estação sempre ativa | Exige avaliar permissões, execução em segundo plano e comportamento do aparelho | Exige avaliar as restrições de execução do iOS |
| Emular o cartão do professor | HCE disponível em cenários e dispositivos compatíveis | Depende dos recursos, das APIs e das condições de acesso disponibilizadas pela Apple na região |

A compatibilidade com NFC não garante que qualquer celular possa executar todos os papéis descritos. A implementação deverá ser testada no aparelho e na versão do sistema operacional escolhidos.

### 9.1. Alternativa com Android como estação

Um aparelho Android compatível pode ser usado para validar a leitura sem adquirir imediatamente um leitor dedicado.

Para professores com iPhone, o uso de uma tag física continua sendo uma alternativa simples dentro do escopo do projeto. Um QR code também pode ser avaliado como fluxo complementar, desde que a aplicação valide a operação no backend.

---

## 10. Ferramentas de apoio

- **NFC Tools:** leitura e gravação de registros em tags compatíveis.
- **Arduino IDE:** desenvolvimento e envio do firmware ao ESP32.
- **PC/SC e pcsc-lite:** infraestrutura de comunicação com leitores compatíveis em sistemas suportados.
- **Postman ou Insomnia:** testes manuais dos endpoints REST.
- **Scripts de simulação:** geração de identificadores fictícios para validar o backend antes da chegada do hardware.
- **Ficha de leitura:** `Pesquisa/Biblioteca/ficha-de-leitura-template.md`, utilizada para catalogar as fontes consultadas conforme solicitado na issue.

---

## 11. Comparativo de custo

Valores de referência preliminares para planejamento, sujeitos a variação conforme fornecedor, frete, impostos e versão do produto.

| Solução | Estimativa de custo | Observações |
|---|---:|---|
| Tags NTAG213 | Baixo custo por unidade | Comprar um pequeno lote para os testes |
| Tags NTAG215 / NTAG216 | Varia conforme o modelo e o lote | Escolher somente se a memória adicional for necessária |
| PN532 + ESP32 | Aproximadamente R$ 40–150 para um kit básico | Faixa preliminar; verificar preço e disponibilidade atuais |
| ACR1552U | Alternativa de custo superior | Consultar preço, disponibilidade nacional, frete e impostos |
| Smartphone NFC existente | Sem custo adicional de hardware, se disponível | Exige desenvolvimento e testes no aparelho |

As estimativas são indicativas e não representam cotações confirmadas. Antes da compra, comparar fornecedores, especificações e prazo de entrega.

---

## 12. Recomendação final

Considerando o escopo acadêmico, o orçamento limitado e a necessidade de demonstrar o fluxo de locação, a recomendação é:

- **Hardware:** PN532 + ESP32.
- **Tag inicial:** NTAG213 em cartão PVC ou adesivo compatível com a montagem.
- **Tag alternativa:** NTAG215, caso haja necessidade de armazenar registros NDEF maiores.
- **Modo NFC:** Reader/Writer.
- **Biblioteca embarcada:** Adafruit PN532, após validar a compatibilidade do módulo.
- **Comunicação:** endpoint REST para enviar a leitura ao backend.
- **Segurança:** validação de permissões e regras de locação no servidor.
- **Fallback:** modo simulado para testar a API antes da chegada do hardware.
- **Alternativa futura:** avaliar autenticação criptográfica caso o projeto passe a exigir resistência à clonagem.

O ACR1552U permanece documentado como uma alternativa de leitor USB para um PC fixo no balcão, desde que a compatibilidade e o custo sejam confirmados.

### Critérios mínimos de validação da PoC

- [ ] Ler uma tag NTAG213 real pelo PN532.
- [ ] Reconhecer tags cadastradas e desconhecidas.
- [ ] Enviar a leitura do ESP32 para a API.
- [ ] Registrar uma retirada válida.
- [ ] Registrar uma devolução válida.
- [ ] Rejeitar operações incompatíveis com o estado do equipamento.
- [ ] Testar a API sem hardware, utilizando o modo simulado.
- [ ] Documentar as limitações de segurança e os resultados dos testes.

---

## 13. Fontes técnicas para consulta

Priorizar a documentação oficial dos fabricantes e das plataformas:

- **NXP Semiconductors:** documentação das famílias NTAG21x, NTAG 424 DNA e MIFARE DESFire EV3.
- **NXP Semiconductors:** documentação técnica do PN532.
- **Adafruit:** documentação e exemplos da biblioteca Adafruit PN532.
- **Android Developers:** documentação de NFC, `NfcAdapter` e Host-based Card Emulation.
- **Apple Developer:** documentação do Core NFC e das APIs de interação NFC.
- **NFC Forum:** especificações e documentação dos padrões NFC e NDEF.
- **ACS:** ficha técnica e documentação oficial do ACR1552U.

As versões exatas dos componentes, as limitações dos leitores e os preços devem ser confirmados nas respectivas fontes antes da implementação e da compra.
