# Ficha de Leitura

**Título:** Code 128 Bar Code Symbology Specification (ISO/IEC 15417:2007)
**Fonte:** PrecisionID / Barcodes Inc. / Barcode Guide (Seagull Scientific) — resumos técnicos da norma ISO/IEC 15417
**Links:**
- https://www.precisionid.com/code-128-faq/
- https://www.barcodesinc.com/articles/code128.htm
- https://barcodeguide.seagullscientific.com/content/Symbologies/Code_128.htm
**Tipo de fonte:** Documentação técnica derivada de norma internacional
**Data da leitura:** 10/09/2026
**Responsável:** Vinicios-Gabriel27

## Resumo

O Code 128 é uma simbologia de código de barras linear de alta densidade, capaz de representar todos os 128 caracteres ASCII. Foi introduzido em 1981 e é formalizado pela norma ISO/IEC 15417:2007. Diferente do EAN/UPC (só números), o Code 128 aceita letras, números e caracteres de controle, sendo muito usado em logística (a variante GS1-128 é padrão em etiquetas de transporte/paletes).

## Pontos-chave

- **Três conjuntos de caracteres (Code Sets):**
  - **Set A:** números, letras maiúsculas, pontuação e caracteres de controle ASCII (0–95);
  - **Set B:** números, letras maiúsculas e minúsculas e caracteres especiais (ASCII 32–127);
  - **Set C:** pares de dígitos (00 a 99), permitindo codificar números com o dobro de densidade (2 dígitos por caractere de símbolo).
- **Estrutura do símbolo:** cada caractere é formado por 3 barras e 3 espaços (o caractere de parada tem 4 barras e 3 espaços), com larguras variando entre 1 e 4 módulos.
- **Caractere de início (Start):** define qual conjunto (A, B ou C) será usado inicialmente; é possível alternar de conjunto no meio do código usando caracteres de troca (Code A/B/C) ou "Shift" (troca só o próximo caractere).
- **Dígito verificador:** calculado em Módulo 103, combinando o valor do caractere de início com os valores dos caracteres de dados ponderados por posição.
- **Variante GS1-128:** versão do Code 128 usada pela GS1 para dados logísticos (SSCC, datas de validade, lotes), limitada a 48 caracteres de dados.
- **Zona de silêncio:** exige espaço em branco de pelo menos 10× a largura da barra mais estreita, em ambos os lados.

## Aplicação no projeto

O Code 128 é relevante para o PoC porque muitos cenários de "código de barras" fora do varejo (crachás, etiquetas internas, logística) não usam EAN, e sim Code 128 — então vale testar se as bibliotecas escolhidas (pyzbar/zxing-cpp no Python, ZXing-js/jsQR no JS) decodificam corretamente os três Code Sets (A, B, C), incluindo casos com troca de conjunto no meio do dado.

## Referência completa (ABNT/citação)

ISO/IEC. **ISO/IEC 15417:2007** — Information technology — Automatic identification and data capture techniques — Code 128 bar code symbology specification. Geneva: International Organization for Standardization, 2007.
