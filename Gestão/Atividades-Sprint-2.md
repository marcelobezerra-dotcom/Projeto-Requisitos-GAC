# Atividades Individuais - Sprint 2

**Periodo:** 20/09/2026 a 03/10/2026
**Objetivo da sprint:** implementar e documentar tres provas de conceito Android que apoiem a identificacao de professores e projetores.

## Diretriz tecnica comum

Todas as PoCs desta sprint devem ser desenvolvidas como aplicacoes Android, preferencialmente em Java. Cada entrega deve informar a versao minima do Android, o dispositivo ou emulador utilizado e como executar o aplicativo.

O aplicativo Android deve expor a funcionalidade principal por um servico REST. O Google Apps Script deve consumir esse servico por HTTPS e receber uma resposta JSON. Para a demonstracao, a URL HTTPS do aparelho deve ser alcancavel pelo Apps Script; quando o dispositivo estiver em rede local, pode ser usado um tunel HTTPS temporario. O README deve registrar a URL de teste, o endpoint, o metodo HTTP, o corpo da requisicao e a resposta esperada.

## Aluno Arthur - NFC

**Issue no GitHub:** `PoC: NFC (leitura basica)`

### Atividades de NFC

- [ ]  Criar a aplicacao Android em Java e definir a versao minima do Android.
- [ ]  Definir o ambiente da PoC e a estrategia de teste: celular Android com NFC ou simulacao no aplicativo.
- [ ]  Implementar a leitura da tag NFC.
- [ ]  Exibir e registrar o UID ou identificador lido.
- [ ]  Documentar como o identificador representara o projetor no fluxo futuro.
- [ ]  Expor um endpoint REST HTTPS no aplicativo para solicitar a leitura NFC e retornar o identificador em JSON.
- [ ]  Criar uma chamada de teste no Google Apps Script para consumir o endpoint HTTPS e registrar a resposta.
- [ ]  Registrar evidencias da execucao e as limitacoes encontradas.
- [ ]  Criar ou atualizar o README com instrucoes de execucao.

### Entregaveis e aceite de NFC

- Codigo-fonte da aplicacao Android, README, evidencias da leitura e registro de limitacoes.
- Uma tag NFC deve ser lida e seu identificador exibido de modo verificavel.
- O Google Apps Script deve chamar o servico REST HTTPS do aplicativo e receber o identificador NFC em JSON.
- Outra pessoa deve conseguir repetir o teste seguindo o README.

## Aluno Vinicius - QR Code e Codigo de Barras

**Issue no GitHub:** `PoC: QR/Barcode (geracao e leitura)`

### Atividades de QR Code e Codigo de Barras

- [ ]  Criar a aplicacao Android em Java e definir a versao minima do Android.
- [ ]  Escolher e registrar as bibliotecas Android de geracao e leitura.
- [ ]  Definir o formato do identificador de patrimonio do projetor.
- [ ]  Implementar a geracao de QR Code para o identificador.
- [ ]  Implementar a leitura a partir de imagem ou camera.
- [ ]  Testar e documentar codigo de barras quando suportado pela biblioteca.
- [ ]  Expor um endpoint REST HTTPS no aplicativo para solicitar a leitura e retornar o patrimonio em JSON.
- [ ]  Criar uma chamada de teste no Google Apps Script para consumir o endpoint HTTPS e registrar a resposta.
- [ ]  Registrar exemplos, evidencias e limitacoes.
- [ ]  Criar ou atualizar o README com instrucoes de execucao.

### Entregaveis e aceite de QR Code e Codigo de Barras

- Codigo-fonte da aplicacao Android, exemplos de codigos gerados, README, evidencias de leitura e relacao das bibliotecas utilizadas.
- A PoC deve gerar ao menos um QR Code com o codigo de patrimonio e recuperar o mesmo valor na leitura.
- O Google Apps Script deve chamar o servico REST HTTPS do aplicativo e receber o patrimonio lido em JSON.
- Os passos devem ser reproduziveis pelo README.

## Aluno Nicolas - Impressao Digital

**Issue no GitHub:** `PoC: Impressao Digital (simulacao ou integracao)`

### Atividades de Impressao Digital

- [ ]  Criar a aplicacao Android em Java e definir a versao minima do Android.
- [ ]  Definir a estrategia da PoC: simulacao no aplicativo Android ou integracao com leitor disponivel.
- [ ]  Criar dataset sintetico ou mecanismo de simulacao sem dados biometricos reais.
- [ ]  Implementar o fluxo de captura ou selecao de amostra.
- [ ]  Implementar a comparacao e o retorno do identificador do professor.
- [ ]  Implementar e demonstrar o caso de amostra nao reconhecida.
- [ ]  Expor um endpoint REST HTTPS no aplicativo para solicitar a identificacao e retornar o resultado em JSON.
- [ ]  Criar uma chamada de teste no Google Apps Script para consumir o endpoint HTTPS e registrar a resposta.
- [ ]  Registrar evidencias, limitacoes e cuidados de privacidade.
- [ ]  Criar ou atualizar o README com instrucoes de execucao.

### Entregaveis e aceite de Impressao Digital

- Codigo-fonte da aplicacao Android, dataset sintetico ou simulador, README, evidencias do matching e registro de limitacoes e cuidados de privacidade.
- A PoC deve retornar um identificador de professor para uma amostra reconhecida e tratar uma amostra nao reconhecida.
- O Google Apps Script deve chamar o servico REST HTTPS do aplicativo e receber o resultado da identificacao em JSON, sem dados biometricos.
- A documentacao deve registrar que a solucao final mantera os templates biometricos localmente no Android.

## Acompanhamento

Cada pessoa deve manter sua issue atualizada, marcar as tarefas concluidas e anexar as evidencias de execucao. A entrega da Sprint 2 estara pronta quando as tres PoCs tiverem README e evidencias reproduziveis.
