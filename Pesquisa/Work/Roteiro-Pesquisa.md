# Roteiro prático para pesquisas tecnológicas — Grupo GAC

Objetivo: orientar os alunos na pesquisa técnica (RFID, QR/Barcode, Impressão Digital) e produção de um PoC e resumo de 1 página. Siga os passos abaixo e preencha o `POC-README-template.md` e o `Documento-Visao.md` conforme solicitado.

1) Preparação (2–4 horas)
- Leia o objetivo do projeto no `Pesquisa/Work/Documento-Visao.md`.
- Crie uma pasta local para o seu trabalho: `Pesquisa/Work/PoC-<tecnologia>-<nome>`.
- Abra uma issue no repositório (se já não existir) com o título `Pesquisa: <tecnologia> - <seu nome>` e vincule-a à milestone `Sprint 1`.

2) Estrutura da pesquisa (entregáveis)
- Resumo de 1 página (máx. 1 A4): problema, hipótese, ferramentas, 3 referências.
- `POC-README.md` preenchido a partir do `POC-README-template.md`.
- Código/ferramenta mínima para demonstrar a leitura/geração/matching (PoC).
- 3 screenshots ou um pequeno vídeo (opcional) que demonstrem o PoC.

3) Como organizar o trabalho (passo a passo)
- Passo A — Levantamento (4–8h):
  - Conceitos fundamentais da tecnologia (como funciona, limites, padrões existentes).
  - Principais bibliotecas e ferramentas (listar links e versões).
  - Hardware necessário e alternativas (simulação se hardware não disponível).

- Passo B — Experimentos rápidos (4–12h):
  - Instalar bibliotecas e rodar exemplos oficiais.
  - Implementar um mini-experimento que demonstre a função essencial (ex.: leitura de uma tag RFID; geração e leitura de um QR; matching básico de impressão digital usando um dataset aberto).

- Passo C — Documentação e entrega (2–4h):
  - Preencher `POC-README.md` com passos de instalação e execução.
  - Escrever o resumo de 1 página e anexar referências.
  - Subir alterações no repositório (branch `poC/<tecnologia>-<nome>`) e abrir PR apontando para a issue.

4) Critérios técnicos e dicas por tecnologia
- RFID
  - Foque em: tipos de tags (LF/HF/UHF), padrões (ISO 14443, EPC), alcance, APIs (e.g., libnfc, pymfrc522), leitura anti-colisão.
  - PoC sugerido: ler uma tag usando um leitor USB (ou simular leitura com arquivo JSON se não houver hardware).
  - Atenção a: formatos de UID, permissões de driver e segurança física.

- QR code & Código de Barras
  - Foque em: formatos (QR Model 2, EAN, Code128), bibliotecas (zxing, pyzbar, qrcode, bwip-js), geração x leitura, densidade e níveis de correção.
  - PoC sugerido: gerar QR/código de barras a partir de dados de empréstimo; ler via webcam ou imagem.

- Impressão Digital
  - Foque em: sensores comuns (optical, capacitive), pipeline (captura → pré-processamento → extração de características → matching), privacidade e armazenamento de templates.
  - PoC sugerido: usar dataset público (ex.: FVC, or NFingerprint datasets) e uma library de matching (source afim) ou simular matching comparando templates criptografados.
  - Atenção a: privacidade — não use dados reais sem consentimento; prefira datasets públicos ou simulações.

5) Conectar pesquisa com SDD
- Para cada experimento, registre ao menos 3 requisitos (user stories) que surgem a partir do PoC — ex.: "Como operador, quero ler a tag RFID para identificar o projetor".
- Anote dependências de integração (APIs, formatos de dados, latência, falhas esperadas).

6) Formato do resumo (modelo rápido)
- Título
- 3–4 linhas descrevendo o que foi estudado e por quê
- Ferramentas / bibliotecas / hardware usadas
- Principais resultados (o que funciona, limitações)
- 3 referências (links)

7) Checklist de aceitação (entrega mínima para Sprint 1/2)
- [ ] Issue criada e atribuída
- [ ] Pasta do PoC no repositório com `POC-README.md`
- [ ] Código mínimo para reproduzir o PoC ou documentação de simulação
- [ ] Resumo de 1 página
- [ ] 3 screenshots ou vídeo curto (opcional)

8) Avaliação e feedback
- O coordenador/PO fará revisão técnica do PoC e do resumo; cada aluno deverá receber feedback escrito em até 5 dias.

9) Recursos úteis (links e bibliotecas)
- RFID: libnfc (C), pymfrc522 (Python), Adafruit PN532 libraries
- QR/Barcode: zxing, pyzbar, qrcode (Python), bwip-js
- Impressão Digital: Neurotechnology (comercial), SourceAFIS (open-source), datasets FVC

10) Tempo recomendado
- Pesquisa inicial + PoC básico: 2 semanas (Sprint 1–2)
- Refinamento e integração: 2–3 semanas (Sprint 3–4)

11) Suporte
- Se tiver dúvida técnica, abra uma issue `help` com a tag `blocker` ou `sdd` e marque o coordenador.

---
Use este roteiro como checklist e adapte conforme a disponibilidade de hardware. Se quiser, eu transformo cada passo em issues automáticas no GitHub (posso gerar as issues por aluno). 
