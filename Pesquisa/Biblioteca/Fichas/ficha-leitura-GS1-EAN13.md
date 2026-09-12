# Ficha de Leitura

**Título:** EAN-13 Barcode Specification (GTIN-13) — GS1 General Specifications
**Fonte:** GS1 / GS1 New Zealand Support Centre / Morovia KB
**Links:**
- https://support.gs1nz.org/hc/en-us/articles/360039814891-EAN-13-Barcode-Specifications
- https://www.morovia.com/kb/EAN13-Specification-10633.html
**Tipo de fonte:** Documentação técnica oficial de organização padronizadora (GS1)
**Data da leitura:** 10/09/2026
**Responsável:** Vinicios-Gabriel27

## Resumo

O EAN-13 (European Article Number, hoje parte da família GTIN — Global Trade Item Number) é o código de barras linear padrão usado no varejo mundial para identificar produtos em pontos de venda (PDV). É mantido pela GS1, organização que assumiu a gestão do padrão EAN em 2005. Cada EAN-13 codifica 13 dígitos numéricos em barras e espaços de largura variável.

## Pontos-chave

- **Estrutura dos 13 dígitos:**
  1. Prefixo GS1 (2–3 dígitos) — identifica a organização membro da GS1 que emitiu o código (não indica necessariamente o país de fabricação);
  2. Código da empresa/fabricante (tamanho variável, atribuído pela GS1);
  3. Código do produto (definido pela própria empresa);
  4. Dígito verificador (check digit) — calculado a partir dos 12 dígitos anteriores pelo método Módulo 10.
- **Particularidade técnica:** o primeiro dígito não é representado por barras próprias; ele é codificado por meio do padrão de paridade (conjuntos de caracteres A e B) da metade esquerda do código — o leitor reconstrói esse dígito a partir dessa paridade.
- **Tamanho do símbolo:** 113 módulos de largura; exige uma "zona de silêncio" (quiet zone) equivalente a pelo menos 10 barras estreitas em cada lado.
- **Variantes relacionadas:** EAN-8 (versão reduzida para embalagens pequenas, como cigarros ou chicletes) e UPC-A (padrão americano, compatível: um EAN-13 iniciado em "0" é, na prática, um UPC-A com zero à esquerda).
- **Uso em livros:** o "Bookland EAN" usa EAN-13 iniciado em 978 ou 979 para representar o ISBN-13 de livros.
- **Cálculo do dígito verificador (Módulo 10):** soma-se os dígitos em posições ímpares e multiplica-se por 3; soma-se os dígitos em posições pares; soma-se os dois resultados; o dígito verificador é o valor que completa esse total até o próximo múltiplo de 10.

## Aplicação no projeto

Essa referência é essencial para o PoC de leitura de código de barras: qualquer implementação de geração/validação de EAN-13 no protótipo deve implementar corretamente o cálculo do dígito verificador (Módulo 10), e o teste das bibliotecas (pyzbar, ZXing etc.) deve incluir casos de EAN-13 válidos e inválidos para verificar se a biblioteca rejeita dígitos verificadores incorretos.

## Referência completa (ABNT/citação)

GS1. **EAN-13 Barcode Specifications.** GS1 New Zealand Support Centre, [s.d.]. Disponível em: https://support.gs1nz.org/hc/en-us/articles/360039814891-EAN-13-Barcode-Specifications. Acesso em: 10 set. 2026.
