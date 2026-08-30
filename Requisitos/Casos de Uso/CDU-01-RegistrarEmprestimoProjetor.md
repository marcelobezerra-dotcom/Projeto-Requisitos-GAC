# Especificação de Requisitos Funcionais

## Caso de Uso (CDU) - Sistema de Empréstimo de Projetores — CCT/Unifor

## Histórico de Versões

<!-- markdownlint-disable MD060 -->
| Data       | Versão | Descrição                                      | Autor             |
| ---------- | ------- | ---------------------------------------------- | ----------------- |
| 20/05/2026 | 1.0     | Criação inicial do CDU-01                      | Equipe do Projeto |
|            |         |                                                |                   |
|            |         |                                                |                   |
<!-- markdownlint-enable MD060 -->

## 1. Nome do Caso de Uso

Registrar Empréstimo de Projetor

---

## 2. Objetivo

Permitir que o funcionário do CCT registre o empréstimo de um projetor a um professor, realizando a identificação do professor por meio de leitura biométrica (digital) e o preenchimento automático do formulário virtual de empréstimo, com envio de notificação por email ao professor após a confirmação do registro.

---

## 3. Tipo de Caso de Uso

Concreto.

---

## 4. Atores

### 4.1 Primário

**Funcionário do CCT** — responsável por operar o sistema e registrar o empréstimo no momento em que o professor solicita o equipamento presencialmente.

### 4.2 Secundário

**Leitora Biométrica** — equipamento responsável por capturar a digital do professor e transmitir os dados ao sistema para identificação.

**Sistema Acadêmico da Unifor** — fornece automaticamente os dados cadastrais do professor (nome completo, matrícula, disciplinas lecionadas no semestre vigente) a partir do identificador biométrico obtido.

---

## 5. Precondições

- O funcionário está autenticado no sistema.
- Existe ao menos um projetor disponível (não emprestado) no estoque do CCT.
- O professor solicitante está cadastrado na base acadêmica da Unifor.

---

## 6. Fluxo Principal

### P1. Professor solicita um projetor e realiza a leitura biométrica

O professor se dirige ao setor de empréstimos do CCT e coloca sua digital na leitora biométrica disponível no balcão de atendimento.

### P2. Leitora biométrica captura e transmite os dados

A leitora biométrica captura a digital do professor e a transmite ao sistema de empréstimos.

### P3. Funcionário acessa a tela de novo empréstimo

O funcionário seleciona a opção "Novo Empréstimo" na tela inicial do sistema.

### P4. Sistema identifica o professor e exibe seus dados

O sistema consulta o Sistema Acadêmico da Unifor com base no identificador biométrico e retorna os dados do professor: nome completo, matrícula e disciplinas lecionadas no semestre vigente.

### P5. Sistema preenche automaticamente o formulário virtual

O sistema exibe o formulário de empréstimo com os campos já preenchidos: nome completo, matrícula, disciplina(s) e data/hora da solicitação (gerada automaticamente pelo sistema).

### P6. Funcionário confirma os dados e seleciona o projetor

O funcionário verifica os dados exibidos com o professor, seleciona um projetor disponível na lista apresentada pelo sistema — identificado por número e código de barras — e confirma o empréstimo.

### P7. Sistema registra o empréstimo

O sistema salva o registro do empréstimo no banco de dados, associando o projetor selecionado ao professor, e marca o equipamento como "emprestado".

### P8. Sistema envia email de confirmação ao professor

O sistema envia automaticamente um email ao endereço institucional do professor informando que um projetor foi emprestado em seu nome, especificando o número de identificação do projetor, a data e o horário do empréstimo.

### P9. Sistema exibe confirmação do empréstimo

O sistema exibe uma mensagem de confirmação com os dados do empréstimo registrado (professor, projetor, data e hora).

### P10. Funcionário entrega o projetor ao professor e confirma no sistema

O funcionário busca o projetor correspondente, o entrega ao professor e confirma a entrega no sistema, concluindo o atendimento.

---

## 7. Fluxos Alternativos

### A1. Professor possui empréstimo em aberto

#### A1.1. No passo P4, após identificar o professor, o sistema verifica que ele possui um ou mais projetores emprestados e ainda não devolvidos.

#### A1.2. O sistema exibe um alerta informando os detalhes do(s) empréstimo(s) em aberto (número do projetor, data do empréstimo).

#### A1.3. O funcionário decide, conforme critério institucional, se prossegue ou cancela o novo empréstimo.

#### A1.4. Se o funcionário decidir prosseguir, o caso de uso retorna ao passo P5. Se cancelar, o caso de uso é encerrado sem registro.

---

### A2. Estoque com apenas um projetor disponível

#### A2.1. No passo P6, o sistema exibe apenas um projetor disponível na lista.

#### A2.2. O funcionário confirma o empréstimo sem necessidade de seleção adicional.

#### A2.3. O caso de uso segue para o passo P7.

---

## 8. Fluxos de Exceção

### E1. Digital não reconhecida pelo sistema biométrico

#### E1.1. No passo P2, a leitora biométrica não consegue identificar a digital do professor.

#### E1.2. O sistema exibe a mensagem: *"Digital não reconhecida. Solicite ao professor que reposicione o dedo na leitora."*

#### E1.3. O funcionário orienta o professor a tentar novamente. Se a releitura for bem-sucedida, o caso de uso retorna ao passo P3.

#### E1.4. Após três tentativas sem sucesso, o empréstimo não pode ser registrado e o funcionário encerra o atendimento sem registro.

---

### E2. Nenhum projetor disponível no estoque

#### E2.1. No passo P6, o sistema não apresenta nenhum projetor disponível na lista.

#### E2.2. O sistema exibe a mensagem: *"Não há projetores disponíveis no momento."*

#### E2.3. O funcionário informa ao professor sobre a indisponibilidade e o caso de uso é encerrado sem registro.

---

### E3. Falha na integração com o Sistema Acadêmico

#### E3.1. No passo P4, o sistema não consegue se comunicar com o Sistema Acadêmico da Unifor para recuperar os dados do professor identificado biometricamente.

#### E3.2. O sistema exibe a mensagem: *"Não foi possível consultar a base acadêmica. Tente novamente em instantes."*

#### E3.3. O funcionário aguarda e tenta novamente. Se a falha persistir, o atendimento é suspenso até a normalização da integração.

---

### E4. Falha ao salvar o registro de empréstimo

#### E4.1. No passo P7, ocorre erro no banco de dados durante a tentativa de salvar o empréstimo.

#### E4.2. O sistema exibe a mensagem: *"Erro ao registrar o empréstimo. O projetor não foi marcado como emprestado. Tente novamente."*

#### E4.3. O sistema garante que o estado do projetor no banco de dados não é alterado (operação atômica).

#### E4.4. O funcionário tenta registrar o empréstimo novamente. Se o erro persistir, o caso de uso é encerrado sem registro.

---

### E5. Falha no envio do email de confirmação

#### E5.1. No passo P8, o sistema não consegue enviar o email de confirmação ao professor.

#### E5.2. O sistema registra a falha internamente e exibe um aviso ao funcionário: *"Não foi possível enviar o email de confirmação ao professor."*

#### E5.3. O empréstimo permanece registrado normalmente. O funcionário informa verbalmente ao professor sobre a impossibilidade do envio de email e o caso de uso segue para o passo P9.

---

## 9. Pós-condições

- O empréstimo do projetor está registrado no banco de dados com todos os dados associados (professor, matrícula, disciplina, projetor, data e hora).
- O projetor emprestado está marcado como "indisponível" no sistema.
- Os dados do empréstimo estão disponíveis para consulta nos relatórios gerenciais.
- Um email de confirmação foi enviado ao professor com os dados do empréstimo (número do projetor, data e hora).

---

## 10. Requisitos Não Funcionais

- O sistema deve identificar o professor a partir da leitura biométrica e exibir seus dados em no máximo **3 segundos** após a captura da digital (RNF 1.1.1).
- O formulário deve ser preenchido automaticamente em menos de **1 segundo** após a identificação biométrica do professor (RNF 1.1.1).
- O fluxo completo de registro de empréstimo deve ser concluído em no máximo **4 interações** na interface, após a leitura biométrica (RNF 1.6.3).
- A operação de salvamento do empréstimo deve ser atômica — ou é salva integralmente ou não altera o banco de dados (RNF 1.9.3).

---

## 11. Ponto de Extensão

### PE1. Registrar Devolução de Projetor

Caso o funcionário identifique, durante o atendimento (passo A1), que o professor ainda possui um projetor em aberto, o sistema pode direcionar para o caso de uso **CDU-02 — Registrar Devolução de Projetor**, permitindo encerrar o empréstimo anterior antes de registrar o novo.

---

## 12. Frequência de Utilização

**Alta.** Este é o caso de uso central do sistema, acionado sempre que um professor solicitar um projetor. A frequência esperada é de múltiplos registros diários, com pico nos horários de início de aula (manhã: 07h–08h, tarde: 13h–14h, noite: 19h–20h).

---

## 13. Interface Visual

### IV1. Tela de Novo Empréstimo

A tela deve apresentar:
- Indicador de status da leitora biométrica (aguardando leitura / leitura concluída / erro).
- Área de exibição dos dados identificados via biometria: nome completo, matrícula e disciplina(s).
- Botão de confirmação de dados ("Confirmar Professor").
- Lista de projetores disponíveis para seleção, exibindo número de identificação e código de barras de cada equipamento.
- Botão "Registrar Empréstimo".
- Área de confirmação final com resumo do empréstimo registrado e indicação de envio do email ao professor.

Navegabilidade: tela acessível a partir do menu principal ("Novo Empréstimo"). Após a confirmação, o sistema retorna automaticamente à tela inicial.

---

## 14. Observações

- O professor **não interage diretamente** com o sistema; toda operação é mediada pelo funcionário. A única interação direta do professor com o equipamento é a leitura da digital na leitora biométrica.
- A leitora biométrica deve estar cadastrada e integrada ao sistema antes da operação. Falhas no equipamento devem ser reportadas ao suporte técnico do CCT.
- O campo "Disciplina" deve refletir apenas as disciplinas do semestre vigente, conforme retornado pelo Sistema Acadêmico.
- O email de confirmação deve ser enviado ao endereço institucional do professor registrado na base acadêmica da Unifor.

---

## 15. Referências

- Especificação de Requisitos Não Funcionais — Sistema de Empréstimo de Projetores CCT/Unifor (v1.0).
- CDU-02 — Registrar Devolução de Projetor.
- CDU-03 — Consultar Relatórios Gerenciais.

---

## 16. Checklist de Validação do Artefato (CDU)

### 16.1 Estrutura mínima

* [ ] Nome do caso de uso iniciado com verbo no infinitivo.
* [ ] Objetivo claro, direto e com foco em um objetivo principal.
* [ ] Tipo do caso de uso informado (concreto/abstrato), quando aplicável.
* [ ] Atores primário e secundários identificados corretamente.
* [ ] Precondições registradas (ou seção marcada como "Não se aplica").
* [ ] Fluxo principal completo e coerente com o objetivo.
* [ ] Fluxos alternativos e de exceção definidos quando necessários.
* [ ] Pós-condições registradas (ou seção marcada como "Não se aplica").
* [ ] Requisitos não funcionais específicos do CDU registrados, quando existirem.
* [ ] Pontos de extensão identificados corretamente, quando existirem.
* [ ] Frequência de utilização estimada.

### 16.2 Qualidade da especificação

* [ ] Passos escritos com linguagem simples e objetiva.
* [ ] Ações descritas com verbos no presente do indicativo (3ª pessoa).
* [ ] Alternância entre ação do ator e ação da solução está clara.
* [ ] Não há ambiguidade (termos vagos sem detalhe técnico).
* [ ] Regras de negócio e mensagens estão referenciadas quando necessário.

### 16.3 Consistência e rastreabilidade

* [ ] Pontos de entrada e saída dos fluxos alternativos estão explícitos.
* [ ] Fluxos de exceção estão vinculados aos passos corretos da solução.
* [ ] Referências internas entre passos (retorna/segue para) estão corretas.
* [ ] Interface visual (IV1 etc.) está coerente com o fluxo descrito.
* [ ] Referências para visão da demanda, glossário e RNF estão atualizadas.

### 16.4 Revisão final

* [ ] Não há contradições entre seções do artefato.
* [ ] Links internos e externos foram validados.
* [ ] Documento revisado por pares.
* [ ] Artefato pronto para uso em desenvolvimento e testes.
