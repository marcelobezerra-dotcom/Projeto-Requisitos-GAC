# Documento de Visão — Sistema de Empréstimo de Projetores (GAC)

## Histórico de Versões

| Data | Versão | Descrição | Autor |
| ---- | ------ | --------- | ----- |
| 22/05/2026 | 1.0 | Documento inicial de visão preenchido a partir dos artefatos do repositório | Equipe do Projeto |

## 1. Objetivo

Definir a proposta de valor, as necessidades principais e o escopo do Sistema de Empréstimo de Projetores do CCT/Unifor, orientando as decisões de projeto, priorização de requisitos e critérios de aceitação para os PoCs e entregas de sprint.

## 2. Visão Geral

O sistema substitui o processo manual atual por um fluxo digital que permite identificar professores por biometria, registrar empréstimos e devoluções, atualizar automaticamente o estado dos projetores e gerar relatórios gerenciais. O objetivo é aumentar a agilidade do atendimento, garantir rastreabilidade dos equipamentos e fornecer métricas para a gestão do acervo.

Público-alvo: funcionários do CCT (operadores), professores (beneficiários) e o coordenador do CCT (usuário gerencial).

## 3. Partes Interessadas (Stakeholders)

- CCT / Unifor — Cliente / Gestor do acervo (Coordenador do CCT)
- Professor — Beneficiário que solicita o equipamento
- Funcionário do CCT — Operador que registra empréstimos/devoluções
- Equipe de TI / Desenvolvedores — Implementação e manutenção do sistema
- Sistema Acadêmico da Unifor — Sistema externo para consulta de dados institucionais

## 4. Personas

1. Funcionário do CCT
- Responsabilidade: atender solicitações presenciais, registrar empréstimos e devoluções.
- Necessidade: operar de forma rápida, reduzir erros manuais e consultar rapidamente projetores disponíveis.

2. Professor
- Necessidade: obter projetor de forma ágil, receber confirmação do empréstimo e ter histórico acessível.

3. Coordenador do CCT
- Necessidade: acessar métricas e relatórios para planejamento do acervo e alocação de recursos.

## 5. Necessidades e Recursos Principais

1) Identificação por biometria
- F1.1 Leitura biométrica do professor: identificação por digital no balcão.
- F1.2 Preenchimento automático do formulário: preenchimento dos dados do professor consultando o Sistema Acadêmico.

2) Gestão de empréstimo
- F2.1 Registro de empréstimo: associar projetor (identificado por etiqueta/código de barras) ao registro do professor.
- F2.2 Seleção de projetor disponível: exibir projetores com status "disponível".
- F2.3 Alerta de empréstimos em aberto: informar se o professor já tem empréstimos ativos.

3) Gestão de devolução
- F3.1 Registro de devolução: encerrar o empréstimo ativo e registrar carimbo de data/hora.
- F3.2 Atualização de status: marcar projetor como "disponível" imediatamente.

4) Notificações
- F4.1 Envio de email de confirmação ao professor após registro do empréstimo.

5) Relatórios gerenciais
- F5.1 Ranking de professores por uso
- F5.2 Relatório de disciplinas com maior demanda
- F5.3 Horários de pico de utilização
- F5.4 Histórico de empréstimos com exportação

6) Segurança e conformidade
- F6.1 Autenticação por perfil de acesso
- F6.2 Registro auditável de operações
- F6.3 Alertas de estoque baixo (<=20%)
- F6.4 Conformidade com LGPD para tratamento de dados pessoais

## 6. Escopo

Inclui: captura biométrica no balcão, registro de empréstimos e devoluções, integração com Sistema Acadêmico para preenchimento de dados, geração de notificações por email, painel gerencial com relatórios e exportação, registro de histórico e alertas de estoque.

Fora do escopo inicial: integração com sistemas de reserva online externos, faturamento, manutenção de inventário físico avançado (contagens fora do fluxo de atendimento) — poderão ser considerados em sprints posteriores.

## 7. Requisitos de Alto Nível

- RF-01: Identificar professor via leitura biométrica e associar ao registro do empréstimo.
- RF-02: Registrar empréstimo com referência ao projetor (id e código de barras) e timestamp.
- RF-03: Atualizar status do projetor ao registrar devolução.
- RNF-01: Tempo de registro (end-to-end) menor que 30 segundos em condições normais.
- RNF-02: Autenticação segura e controle de acessos por perfis.

## 8. Critérios de Sucesso

- Redução do tempo médio de atendimento no balcão em >= 30% em relação ao processo manual.
- 100% dos empréstimos registrados digitalmente durante o período de avaliação.
- Disponibilidade do serviço no horário de atendimento do CCT ≥ 99% (para ambiente de produção futuro).

## 9. Restrições e Premissas

- Presume-se disponibilidade da leitora biométrica no balcão e drivers compatíveis.
- Integração com o Sistema Acadêmico depende de API institucional e credenciais apropriadas.
- Dados biométricos serão tratados de acordo com LGPD; preferir armazenamento de templates e não imagens brutas, com criptografia.

## 10. Riscos e Mitigações

- Risco: indisponibilidade do leitor biométrico → Mitigação: modo manual de busca por matrícula/nome.
- Risco: falha de integração com Sistema Acadêmico → Mitigação: cache temporário de dados e fallback manual.
- Risco: vazamento/uso indevido de dados biométricos → Mitigação: criptografia, políticas de retenção e minimização de dados.

## 11. Arquitetura (resumo)

Componentes principais: Interface web (navegador), Leitora biométrica (hardware local), API REST com módulos de Autenticação, Empréstimos, Devoluções, Notificações e Relatórios, Banco de dados relacional, Integração com Sistema Acadêmico e servidor de email institucional.

Diagramas detalhados e propostas estão disponíveis em `Requisitos/` e `Prototipos/` do repositório (use os arquivos existentes para referência e para enriquecer o SDD).

## 12. Cronograma Resumido

- Sprint 1: pesquisas e PoCs iniciais (biometria, leitura de código de barras, NFC) — entrega: resumos e PoC mínimo.
- Sprint 2: PoCs funcionais e README dos PoCs — entrega: repositório com execução mínima e screenshots.
- Sprint 3: Início do SDD e Documento de Visão finalizado.
- Sprint 4: Design detalhado e integração inicial.
- Sprint 5: Finalização, testes e apresentação (13/11).

## 13. Checklist de Validação

- [ ] Objetivo e proposta de valor claros e alinhados com a demanda do CCT
- [ ] Stakeholders e personas identificados
- [ ] Funcionalidades principais descritas e relacionadas a atores
- [ ] Critérios de sucesso definidos
- [ ] Restrições, riscos e mitigações registradas
- [ ] Arquitetura resumida e apontamentos para diagramas

---

> Fonte e referência: conteúdo extraído e consolidado a partir de `Requisitos/Visão/visaoDemanda-ProjetoresUnifor.md` e artefatos do repositório (templates em `Pesquisa/Work/`, protótipos e requisitos). Atualize campos de data, versão e autores conforme apropriado.
