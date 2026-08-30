# Especificação de Requisitos Funcionais

## Caso de Uso (CDU) - Sistema de Empréstimo de Projetores — CCT/Unifor

## Histórico de Versões

<!-- markdownlint-disable MD060 -->
| Data       | Versão | Descrição                                      | Autor             |
| ---------- | ------- | ---------------------------------------------- | ----------------- |
| 20/05/2026 | 1.0     | Criação inicial do CDU-02                      | Equipe do Projeto |
|            |         |                                                |                   |
|            |         |                                                |                   |
<!-- markdownlint-enable MD060 -->

## 1. Nome do Caso de Uso

Registrar Devolução de Projetor

---

## 2. Objetivo

Permitir que o funcionário do CCT registre a devolução de um projetor previamente emprestado a um professor, atualizando o status do equipamento para "disponível" e encerrando o registro de empréstimo correspondente.

---

## 3. Tipo de Caso de Uso

Concreto.

---

## 4. Atores

### 4.1 Primário

**Funcionário do CCT** — responsável por receber o projetor devolvido pelo professor e registrar a devolução no sistema.

### 4.2 Secundário

Não se aplica.

---

## 5. Precondições

- O funcionário está autenticado no sistema.
- Existe ao menos um empréstimo ativo registrado no sistema (projetor com status "emprestado").
- O professor retorna fisicamente com o projetor ao setor de empréstimos do CCT.

---

## 6. Fluxo Principal

### P1. Professor devolve o projetor ao funcionário

O professor se dirige ao setor de empréstimos do CCT e entrega fisicamente o projetor ao funcionário.

### P2. Funcionário acessa a tela de devolução

O funcionário seleciona a opção "Registrar Devolução" na tela inicial do sistema.

### P3. Funcionário busca o empréstimo ativo

O funcionário informa o nome do professor ou o número de identificação (ou código de barras) do projetor no campo de busca.

### P4. Sistema localiza o empréstimo ativo correspondente

O sistema busca no banco de dados o empréstimo ativo vinculado ao professor ou ao projetor informado e exibe os detalhes: nome do professor, matrícula, disciplina, número de identificação e código de barras do projetor, data e hora do empréstimo.

### P5. Funcionário confirma os dados da devolução

O funcionário verifica os dados exibidos e confirma que o projetor físico recebido corresponde ao registrado no sistema.

### P6. Sistema registra a devolução

O sistema encerra o registro de empréstimo, armazena a data e hora da devolução e atualiza o status do projetor para "disponível".

### P7. Sistema exibe confirmação da devolução

O sistema exibe uma mensagem de confirmação com os dados da devolução registrada (professor, projetor, data e hora da devolução, duração total do empréstimo).

---

## 7. Fluxos Alternativos

### A1. Busca retorna mais de um empréstimo ativo para o mesmo professor

#### A1.1. No passo P4, o sistema localiza mais de um empréstimo ativo vinculado ao professor informado (professor com múltiplos projetores emprestados).

#### A1.2. O sistema exibe a lista de empréstimos ativos do professor, com os dados de cada projetor (número de identificação, código de barras e data do empréstimo).

#### A1.3. O funcionário seleciona o empréstimo correspondente ao projetor que está sendo devolvido fisicamente.

#### A1.4. O caso de uso retorna ao passo P5.

---

## 8. Fluxos de Exceção

### E1. Empréstimo ativo não encontrado

#### E1.1. No passo P4, o sistema não localiza nenhum empréstimo ativo para o professor ou projetor informado.

#### E1.2. O sistema exibe a mensagem: *"Nenhum empréstimo ativo encontrado para os dados informados. Verifique o nome do professor ou o número do projetor."*

#### E1.3. O funcionário solicita ao professor que confirme seu nome ou verifica o número de identificação e o código de barras no projetor físico e realiza nova busca.

#### E1.4. Se o empréstimo for encontrado, o caso de uso retorna ao passo P4. Se não for encontrado, o funcionário deve registrar a ocorrência manualmente para investigação posterior, e o caso de uso é encerrado sem alteração no banco de dados.

---

### E2. Falha ao salvar o registro de devolução

#### E2.1. No passo P6, ocorre erro no banco de dados durante a tentativa de registrar a devolução.

#### E2.2. O sistema exibe a mensagem: *"Erro ao registrar a devolução. O status do projetor não foi alterado. Tente novamente."*

#### E2.3. O sistema garante que o status do projetor e o registro de empréstimo permanecem inalterados (operação atômica).

#### E2.4. O funcionário tenta registrar a devolução novamente. Se o erro persistir, o caso de uso é encerrado sem registro e a ocorrência deve ser reportada ao suporte técnico.

---

## 9. Pós-condições

- O registro de empréstimo está encerrado no banco de dados, com data e hora da devolução registradas.
- O projetor devolvido está marcado como "disponível" no sistema, podendo ser emprestado novamente.
- Os dados completos do empréstimo (incluindo duração) estão disponíveis para consulta nos relatórios gerenciais.

---

## 10. Requisitos Não Funcionais

- O sistema deve localizar o empréstimo ativo em no máximo **2 segundos** após a busca (RNF 1.1.1).
- A operação de registro da devolução deve ser atômica — ou é concluída integralmente ou nenhum dado é alterado (RNF 1.9.3).

---

## 11. Ponto de Extensão

Não se aplica.

---

## 12. Frequência de Utilização

**Alta.** A devolução ocorre em correspondência com cada empréstimo registrado, sendo acionada múltiplas vezes ao dia, com pico nos horários de encerramento das aulas.

---

## 13. Interface Visual

### IV1. Tela de Registrar Devolução

A tela deve apresentar:
- Campo de busca por nome do professor, número de identificação ou código de barras do projetor (com botão "Buscar").
- Área de exibição dos dados do empréstimo ativo: nome do professor, matrícula, disciplina, projetor e data/hora do empréstimo.
- Botão "Confirmar Devolução".
- Área de confirmação final com resumo da devolução e duração total do empréstimo.

Navegabilidade: tela acessível a partir do menu principal ("Registrar Devolução"). Após a confirmação, o sistema retorna automaticamente à tela inicial.

---

## 14. Observações

- O projetor deve ser inspecionado fisicamente pelo funcionário antes da confirmação da devolução. Eventuais danos ao equipamento devem ser registrados por canal separado (fora do escopo deste CDU).
- Futuramente, pode ser avaliado o envio de notificação automática ao professor quando um projetor emprestado estiver há mais de X horas sem devolução registrada.

---

## 15. Referências

- Especificação de Requisitos Não Funcionais — Sistema de Empréstimo de Projetores CCT/Unifor (v1.0).
- CDU-01 — Registrar Empréstimo de Projetor.
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
