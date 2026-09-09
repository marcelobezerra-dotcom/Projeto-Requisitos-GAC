# SDD (Resumo) — Projeto GAC

## 1. Visão Geral da Arquitetura
Breve descrição da arquitetura proposta (camadas, responsabilidades, visão geral).

## 2. Componentes Principais
- Frontend: tecnologia e responsabilidades
- Backend: serviços, APIs, autenticação
- Integrações: NFC, QR/Barcode, Impressão Digital, serviço de IA
- Persistência: banco de dados/formatos

## 3. Diagramas (referência)
- Incluir diagrama de componentes e de sequência (link ou arquivo em `Requisitos/Work/diagrams/`).

## 4. Interfaces e APIs
- Endpoint: `POST /emprestimo` — propósito, parâmetros, resposta
- Endpoint: `POST /devolucao` — propósito, parâmetros, resposta
- Especificar contratos de dados (JSON schemas) resumidos

## 5. Modelo de Dados (resumo)
- Entidade `Projetor`: id, etiqueta, estado
- Entidade `Emprestimo`: id, userId, projetorId, dataEmprestimo, dataDevolucaoPrevista

## 6. Requisitos Não Funcionais
- Performance: tempos aceitáveis
- Segurança: autenticação, criptografia, privacidade biométrica
- Escalabilidade e disponibilidade

## 7. Integração com IA
- Papéis da IA (p.ex. reconhecimento, classificação, auxílio na tomada de decisão)
- Interfaces para módulos de IA (input/output esperados)

## 8. Estratégia de Testes
- Testes unitários, integração, testes manuais de hardware
- Checklist de aceitação para cada serviço

## 9. Deployment (resumo)
- Ambientes: dev / staging / prod (opcional)
- Requisitos de infraestrutura mínimos

## 10. Questões em aberto
- Lista de decisões arquiteturais pendentes

> Atualize este resumo para formar o SDD completo quando necessário.
