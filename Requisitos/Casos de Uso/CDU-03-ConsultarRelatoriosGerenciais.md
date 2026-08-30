# Especificação de Requisitos Funcionais

## Caso de Uso (CDU) - Sistema de Empréstimo de Projetores — CCT/Unifor

## Histórico de Versões

<!-- markdownlint-disable MD060 -->
| Data       | Versão | Descrição                                      | Autor             |
| ---------- | ------- | ---------------------------------------------- | ----------------- |
| 22/05/2026 | 1.0     | Criação inicial do CDU-03                      | Equipe do Projeto |
|            |         |                                                |                   |
|            |         |                                                |                   |
<!-- markdownlint-enable MD060 -->

## 1. Nome do Caso de Uso

Consultar Relatórios Gerenciais

---

## 2. Objetivo

Permitir que o coordenador do CCT consulte as métricas de uso dos projetores por meio do sistema, obtendo informações gerenciais como os professores com maior frequência de uso, as disciplinas com maior demanda, os horários de pico de utilização e o tempo médio de empréstimo por equipamento, subsidiando decisões de gestão do acervo.

---

## 3. Tipo de Caso de Uso

Concreto.

---

## 4. Atores

### 4.1 Primário

**Coordenador do CCT** — responsável pela gestão do departamento, que acessa o sistema para visualizar métricas e relatórios sobre o uso dos projetores.

### 4.2 Secundário

Não se aplica.

---

## 5. Precondições

- O coordenador está autenticado no sistema com perfil de acesso gerencial.
- Existem registros de empréstimos no banco de dados para a geração dos relatórios.

---

## 6. Fluxo Principal

### P1. Coordenador acessa o sistema

O coordenador faz login no sistema de empréstimos com suas credenciais institucionais, que concedem acesso ao módulo gerencial.

### P2. Coordenador acessa o módulo de relatórios

O coordenador seleciona a opção "Relatórios Gerenciais" no menu principal.

### P3. Coordenador seleciona o tipo de relatório

O sistema exibe as opções de relatório disponíveis:

- Professores com maior frequência de uso de projetores.
- Disciplinas com maior demanda de projetores.
- Horários de aula com maior demanda de projetores.
- Histórico de empréstimos por período.
- Tempo médio de empréstimo por equipamento.

O coordenador seleciona o tipo de relatório desejado.

### P4. Coordenador define os filtros do relatório

O sistema exibe os parâmetros de filtragem disponíveis para o tipo de relatório selecionado: período (data inicial e data final) e semestre letivo. O coordenador preenche os filtros desejados e aciona a opção "Gerar Relatório".

### P5. Sistema processa e exibe o relatório

O sistema consulta o banco de dados com os filtros aplicados e exibe o relatório em formato de tabela e gráfico, com os dados correspondentes ao período e tipo selecionados.

### P6. Coordenador analisa os dados

O coordenador visualiza as informações exibidas e pode navegar entre as seções do relatório.

### P7. Coordenador exporta o relatório

O coordenador aciona a opção "Exportar" e seleciona o formato desejado (PDF ou planilha). O sistema gera o arquivo e o disponibiliza para download.

---

## 7. Fluxos Alternativos

### A1. Nenhum dado disponível para os filtros selecionados

#### A1.1. No passo P5, o sistema não encontra registros de empréstimos para os filtros aplicados.

#### A1.2. O sistema exibe a mensagem: *"Nenhum dado encontrado para o período ou filtros selecionados. Ajuste os parâmetros e tente novamente."*

#### A1.3. O coordenador ajusta os filtros e aciona novamente a opção "Gerar Relatório".

#### A1.4. O caso de uso retorna ao passo P4.

---

### A2. Coordenador opta por não exportar o relatório

#### A2.1. No passo P7, o coordenador decide não exportar o relatório.

#### A2.2. O coordenador permanece na tela de visualização e pode selecionar um novo tipo de relatório ou encerrar a sessão.

#### A2.3. Se optar por novo relatório, o caso de uso retorna ao passo P3.

---

## 8. Fluxos de Exceção

### E1. Falha ao carregar os dados do relatório

#### E1.1. No passo P5, o sistema encontra erro ao consultar o banco de dados.

#### E1.2. O sistema exibe a mensagem: *"Não foi possível carregar os dados do relatório. Tente novamente em instantes."*

#### E1.3. O coordenador aguarda e tenta novamente. Se o erro persistir, o caso de uso é encerrado e a ocorrência deve ser reportada ao suporte técnico.

---

### E2. Falha ao exportar o relatório

#### E2.1. No passo P7, o sistema encontra erro ao gerar o arquivo de exportação.

#### E2.2. O sistema exibe a mensagem: *"Erro ao exportar o relatório. Tente novamente."*

#### E2.3. O coordenador tenta novamente. Se o erro persistir, pode visualizar os dados apenas na tela e o caso de uso é encerrado sem exportação.

---

### E3. Coordenador sem perfil de acesso gerencial

#### E3.1. No passo P2, o sistema identifica que o usuário autenticado não possui perfil de coordenador.

#### E3.2. O sistema oculta ou desabilita a opção "Relatórios Gerenciais" no menu principal e exibe a mensagem: *"Acesso restrito. Esta funcionalidade está disponível apenas para o coordenador do CCT."*

#### E3.3. O caso de uso é encerrado sem exibição de dados.

---

## 9. Pós-condições

- O coordenador visualizou os dados gerenciais solicitados.
- Os dados foram apresentados de forma clara, em formato de tabela e gráfico.
- O relatório exportado (quando solicitado) está disponível para download pelo coordenador.

---

## 10. Requisitos Não Funcionais

- O sistema deve gerar e exibir o relatório em no máximo **5 segundos** após o acionamento de "Gerar Relatório" (RNF 1.1.1).
- O acesso ao módulo de relatórios deve ser restrito a usuários com perfil de coordenador ou administrador do sistema (RNF 1.4.1).
- Os dados exibidos nos relatórios devem refletir o estado atual do banco de dados no momento da consulta, sem defasagem (RNF 1.10.2).
- A exportação do relatório deve ser concluída em no máximo **10 segundos** para períodos de até 1 ano de dados (RNF 1.1.1).

---

## 11. Ponto de Extensão

Não se aplica.

---

## 12. Frequência de Utilização

**Baixa a moderada.** O módulo é acessado periodicamente pelo coordenador para análise de desempenho e tomada de decisão gerencial. A frequência esperada é de consultas semanais ou mensais, sem obrigatoriedade de acesso diário.

---

## 13. Interface Visual

### IV1. Tela de Relatórios Gerenciais

A tela deve apresentar:

- Menu de seleção de tipo de relatório (lista com as cinco categorias disponíveis).
- Filtros de período: campos de data inicial e data final.
- Filtro de semestre letivo (seleção em lista).
- Botão "Gerar Relatório".
- Área de exibição do relatório em formato de tabela e gráfico interativo.
- Botão "Exportar" com opções de formato: PDF e planilha (CSV/XLSX).
- Botão "Novo Relatório" para retornar à seleção de tipo sem sair do módulo.

Navegabilidade: tela acessível apenas a usuários com perfil de coordenador ou administrador, a partir da opção "Relatórios Gerenciais" no menu principal. Após a visualização, o coordenador pode gerar um novo relatório ou retornar ao menu principal.

---

## 14. Observações

- O acesso ao módulo de relatórios deve ser concedido exclusivamente ao coordenador do CCT e ao administrador do sistema, preservando a privacidade dos dados (RN10).
- Os relatórios não devem ser utilizados para monitoramento ou avaliação de desempenho individual dos professores, conforme orientação ética do sistema (RNF 2.1).
- Futuramente, pode ser avaliada a exportação automática periódica de relatórios por email ao coordenador, com frequência configurável.

---

## 15. Referências

- Especificação de Requisitos Não Funcionais — Sistema de Empréstimo de Projetores CCT/Unifor (v1.0).
- CDU-01 — Registrar Empréstimo de Projetor.
- CDU-02 — Registrar Devolução de Projetor.
- RN7 — Todo empréstimo deve registrar dados para geração de relatórios gerenciais.
- RN10 — Acesso ao módulo gerencial é restrito ao coordenador do CCT.

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
