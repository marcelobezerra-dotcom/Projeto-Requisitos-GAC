# Ficha de Leitura — Template

- **Título:** Fingerprint Verification Competition (FVC)
- **Autor(es):** Wikipedia (baseado nos trabalhos originais de D. Maio, D. Maltoni, R. Cappelli, J.L. Wayman e A.K. Jain)
- **Ano:** artigo sem data fixa de publicação; competições descritas ocorreram entre 2000 e 2006, com continuidade via FVC-onGoing
- **Fonte / Link:** https://en.wikipedia.org/wiki/Fingerprint_Verification_Competition
- **Tipo:** artigo de referência (Wikipedia)
- **Resumo (máx. 200 palavras):** A FVC é uma competição internacional de algoritmos de verificação de impressão digital, criada em 2000 por laboratórios da Universidade de Bolonha, da San Jose State University e da Michigan State University. Depois da primeira edição (FVC2000), houve mais três (FVC2002, FVC2004, FVC2006), cada uma reunindo várias dezenas de algoritmos submetidos por grupos acadêmicos e empresas. As competições estabeleceram bases de dados públicas de impressões digitais, coletadas com sensores diferentes (incluindo um banco de imagens sintéticas), servindo até hoje como benchmark padrão para comparar algoritmos de reconhecimento. Após a quarta edição, o interesse da comunidade levou à criação do FVC-onGoing, uma avaliação online contínua (sem prazo fixo) que testa algoritmos submetidos contra datasets reservados e publica métricas de desempenho (como taxa de falsa aceitação e falsa rejeição).
- **Principais ideias / insights:** existir um benchmark público e padronizado é o que permite comparar de forma justa diferentes algoritmos/bibliotecas de matching (como o Bozorth3/NBIS e o SourceAFIS) usando os mesmos dados.
- **Métodos / tecnologias usados:** bases de dados coletadas com sensores ópticos e capacitivos reais, mais um banco de imagens sintéticas; métricas de FAR (falsa aceitação) e FRR (falsa rejeição).
- **Pontos relevantes para o projeto GAC:** os datasets FVC (2000/2002/2004) são a fonte pública mais indicada para testar o PoC em modo "real" (com imagens de dedos de verdade), evitando o uso de dados de pessoas reais do grupo.
- **Questões geradas / hipóteses:** qual dataset FVC específico (DB1, DB2, DB3 ou o de imagens sintéticas DB4) seria mais fácil de obter e mais adequado ao PoC, considerando o tipo de sensor usado na coleta de cada um?
- **Citação (formato APA):** Wikipedia contributors. (s.d.). *Fingerprint Verification Competition*. Wikipedia. https://en.wikipedia.org/wiki/Fingerprint_Verification_Competition
