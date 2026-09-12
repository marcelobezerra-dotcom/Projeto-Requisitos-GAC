# Ficha de Leitura

**Título:** ISO/IEC 18004 — QR Code Bar Code Symbology Specification
**Edição consultada:** 4ª edição (2024), com referência à 3ª edição (2015)
**Fonte:** ISO — International Organization for Standardization
**Link:** https://www.iso.org/standard/83389.html
**Tipo de fonte:** Norma técnica internacional
**Data da leitura:** 10/09/2026
**Responsável:** Vinicios-Gabriel27

## Resumo

A ISO/IEC 18004 é a norma internacional que define oficialmente o símbolo QR Code. Ela especifica a estrutura do símbolo, os modos de codificação de caracteres, os níveis de correção de erro, o algoritmo de referência para decodificação e os parâmetros de qualidade de impressão. A norma nasceu do padrão japonês JIS X 0510 (1999) e teve sua primeira edição ISO em 2000, sendo revisada em 2006, 2015 e mais recentemente em 2024.

## Pontos-chave

- **Estrutura do símbolo:** o QR Code é uma simbologia matricial (2D), composta por módulos quadrados organizados em um padrão quadrado maior. Possui três padrões de localização ("finder patterns") em três dos quatro cantos, usados para detectar posição, tamanho e inclinação do símbolo.
- **Versões:** existem 40 versões, da Versão 1 (matriz 21×21, capacidade de até 72 bits com correção alta) até a Versão 40 (matriz 177×177, até ~23 mil bits com correção baixa). Quanto maior a versão, maior a capacidade de dados e maior a complexidade de leitura.
- **Modos de codificação de dados:** Numérico, Alfanumérico, Byte, Kanji e ECI (Extended Channel Interpretation), além de suporte a "Structured Append" (dividir dados em múltiplos símbolos).
- **Correção de erro:** utiliza códigos Reed-Solomon em 4 níveis — L (~7%), M (~15%), Q (~25%) e H (~30%) de tolerância a dados corrompidos/ilegíveis.
- **Micro QR Code:** variante compacta da mesma norma, com menos módulos de overhead, indicada para marcação direta em peças pequenas.
- **Máscaras (mask patterns):** padrões aplicados sobre os dados para evitar áreas com muito contraste ou padrões que confundam o leitor; a norma define um sistema de pontuação para escolher a melhor máscara.

## Aplicação no projeto

Essa norma é a base teórica para justificar tecnicamente por que escolhemos (ou não) gerar QR Codes com determinada versão/nível de correção de erro no PoC. Serve também para explicar, no resumo de 1 página, a diferença entre QR Code "puro" (norma ISO) e implementações proprietárias.

## Referência completa (ABNT/citação)

ISO/IEC. **ISO/IEC 18004:2024** — Information technology — Automatic identification and data capture techniques — QR code bar code symbology specification. 4. ed. Geneva: International Organization for Standardization, 2024.
