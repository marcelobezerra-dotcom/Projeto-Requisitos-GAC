# Plano de Estudo e Implementação — GAC (metodologia Scrum)

Data da apresentação obrigatória: **13/11/2026**

Resumo: plano para o grupo de estudo (3 alunos + coordenador) visando implementar o sistema GAC. As atividades combinam pesquisa sobre biometria digital, RFID, QR code/código de barras, estudo da metodologia SDD e desenvolvimento de protótipos. Gestão por Scrum com quadro Kanban no GitHub.

**Objetivos**
- Entregar resultado demonstrável em 13/11/2026 (slides + demo + resumo SDD).
- Estudar e documentar: RFID; QR code + Código de Barras; Impressão Digital.
- Aplicar SDD para definir arquitetura e requisitos do sistema GAC.
- Produzir protótipos integráveis e demonstração reproducível.

**Papéis (sugerido)**
- **Product Owner (PO)**: Coordenador (você) — priorização do backlog, aceitação.
- **Scrum Master (SM)**: rotativo entre os alunos ou designar um aluno.
- **Equipe de Desenvolvimento**: 3 alunos (cada um responsável por uma tecnologia/PoC).

**Sprints (2 semanas) — calendário sugerido**
- Sprint 1: 06/09/2026 — 19/09/2026 — Kickoff e pesquisas iniciais
- Sprint 2: 20/09/2026 — 03/10/2026 — PoC individuais (leitura/geração)
- Sprint 3: 04/10/2026 — 17/10/2026 — Revisão técnica; iniciar SDD
- Sprint 4: 18/10/2026 — 31/10/2026 — Design detalhado (SDD) e integração inicial
- Sprint 5: 01/11/2026 — 13/11/2026 — Finalização, testes, ensaios e apresentação

Observação: ajustar datas conforme disponibilidade do grupo; Sprint 5 é de 13 dias para alinhar com a data da apresentação.

**Backlog inicial (épicos / itens)**
- Pesquisa e documentação: RFID (epic)
- Pesquisa e documentação: QR code + Código de Barras (epic)
- Pesquisa e documentação: Impressão Digital (epic)
- Estudo e aplicação: SDD (visão, requisitos, arquitetura)
- Protótipos: PoC RFID, PoC QR/Barcode, PoC Fingerprint
- Integração: APIs e fluxo de empréstimo/devolução
- Documentação: Documento SDD resumido + relatório curto
- Material de apresentação: Slides + roteiro de demo

**Divisão de atividades por pessoa**
- **Aluno A (RFID)**: levantamento de hardware/soft, PoC leitura RFID, documento resumo, slides e demo.
- **Aluno B (QR + Código de Barras)**: gerar/ler QR e barcode, integração com processo, PoC, resumo e slides.
- **Aluno C (Impressão Digital)**: estudo de sensores e matching (pode usar simulação), PoC, resumo e slides.
- **Coordenador (PO)**: revisar entregas, validar SDD, preparar introdução e seção de conclusão nos slides.

**Cerimônias Scrum (sugestão)**
- Planejamento de Sprint (sprint planning) — 60 min no início de cada sprint.
- Daily stand-up — 15 min (padrão) ou 2x/semana se estiver difícil diariamente.
- Revisão de Sprint (sprint review) — demo ao final do sprint.
- Retrospectiva — 30–45 min após a review.

**Definição de Done (DoD)**
- Código com README e instruções de execução / imagens quando hardware ausente.
- Testes básicos manuais documentados (passos e resultados).
- Issue associada com checklist concluído e PR ou tag de release.
- Documento SDD parcial ou artefato exigido para a entrega da sprint.

**Kanban no GitHub (estrutura recomendada)**
- Crie um Project Board (Kanban) com colunas: `Backlog`, `To do`, `In progress`, `In review`, `Blocked`, `Done`.
- Use Issues para cada item do backlog; vincule PRs e commits às issues.
- Labels sugeridos: `rfid`, `qrcode`, `fingerprint`, `sdd`, `docs`, `blocker`, `high-priority`, `demo`.

**Templates e artefatos a criar (sugestão)**
- Template de Issue: título, descrição, critério de aceite, estimativa (horas), responsável.
- Template de PR: link para issue, checklist DoD.
- Pasta de trabalho no repositório: `Requisitos/Work/` com templates:
  - `Documento-Visao.md` (template)
  - `SDD-Resumo.md` (template)
  - `Slides-Template.pptx` (opcional)
  - `POC-README-template.md`

**Exemplo de entregáveis por Sprint**
- Sprint 1: resumos de pesquisa (1 pág cada), backlog inicial, reunião kickoff.
- Sprint 2: PoC individuais com README e screenshots.
- Sprint 3: Documento Visão + início do SDD.
- Sprint 4: SDD (arquitetura + APIs) e integração inicial.
- Sprint 5: Demo final, slides, relatório curto e ensaios.

**Ritmo de comunicação**
- Reunião semanal fixa (45–60 min).
- Canal assíncrono: GitHub Issues + WhatsApp/Telegram/Slack para avisos rápidos.

**Critérios de aceitação para apresentação (13/11)**
- Slides prontos e revistos.
- Demo funcionando (ou documentação detalhada + vídeos/screenshots se hardware faltar).
- Documento SDD resumido entregue.

---
### Ações imediatas (esta semana)
1. Agendar reunião kickoff e confirmar papel do SM.
2. Criar Project Board no GitHub e labels.
3. Criar issues iniciais para pesquisas e atribuir aos alunos.
4. Cada aluno entregar 1-página de resumo até o fim da Sprint 1.

---
Se desejar, eu posso:
- Gerar automaticamente as issues iniciais no repositório e o Project Board no formato Kanban; ou
- Criar os templates de documento `Documento-Visao.md`, `SDD-Resumo.md`, `POC-README-template.md` dentro de `Requisitos/Work/`.