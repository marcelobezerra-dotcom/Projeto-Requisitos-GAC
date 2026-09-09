# Ficha de Leitura — Template

- **Título:** SourceAFIS for Java (documentação do projeto)
- **Autor(es):** Robert Važan
- **Ano:** projeto ativo e mantido; sem ano único de publicação
- **Fonte / Link:** https://github.com/robertvazan/sourceafis-java
- **Tipo:** documentação de software / repositório open-source
- **Resumo (máx. 200 palavras):** SourceAFIS é uma engine open-source (licença Apache 2.0) de reconhecimento de impressão digital, com portes em Java, .NET, Go e Rust. Ela recebe duas imagens de impressão digital como entrada e devolve um escore de similaridade entre elas, podendo fazer tanto comparação 1:1 (duas digitais específicas) quanto busca 1:N (uma digital contra uma base inteira). Internamente, extrai um "template" de características (minúcias) de cada imagem e compara os dois templates, com um limiar de escore configurável para decidir se é a mesma pessoa. A API é simples: criar um `FingerprintTemplate` a partir da imagem (informando a resolução em DPI) e depois usar um `FingerprintMatcher` para obter o escore. O projeto é ativamente mantido e tem uma "página" (livro) explicando o funcionamento interno do algoritmo para quem quiser detalhes de implementação.
- **Principais ideias / insights:** existem alternativas prontas e maduras ao NBIS (a biblioteca de referência do governo americano) para reconhecimento de impressão digital, com API mais simples de usar; a escolha entre elas depende da linguagem do projeto e da necessidade (ou não) de busca 1:N eficiente.
- **Métodos / tecnologias usados:** extração de minúcias + matching por template, com suporte a indexação para buscas rápidas em bases grandes.
- **Pontos relevantes para o projeto GAC:** é a alternativa natural ao NBIS/`afis` (usado no PoC em Python) caso o grupo GAC decida migrar a implementação para Java ou .NET, ou precise de busca 1:N em vez de apenas comparação 1:1.
- **Questões geradas / hipóteses:** o escore de similaridade do SourceAFIS é diretamente comparável ao escore do Bozorth3 (NBIS), ou cada algoritmo usa uma escala própria que exigiria recalibrar o limiar de decisão?
- **Citação (formato APA):** Važan, R. (s.d.). *SourceAFIS for Java* [Software]. GitHub. https://github.com/robertvazan/sourceafis-java
