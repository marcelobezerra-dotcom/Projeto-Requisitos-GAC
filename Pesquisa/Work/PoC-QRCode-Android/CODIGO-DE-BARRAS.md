# Código de Barras na PoC de QR Code — o que foi testado, o que foi documentado

Este documento fecha o item da **Issue #4**:

> "Testar e documentar código de barras quando suportado pela biblioteca."

Ele explica a relação entre a **RN8** e o que a PoC entrega, o que o ZXing
suporta, o que está implementado hoje e o que seria necessário para acrescentar
código de barras **linear (1D)**.

---

## 1. O que a RN8 pede

A regra de negócio do projeto diz:

> **RN8 — Cada projetor deve ser identificado por número único e código de barras.**
>
> "Cada projetor cadastrado no sistema deve possuir um número de identificação
> único e um código de barras para rastreamento. A seleção do projetor durante o
> empréstimo e a sua identificação na devolução devem ser realizadas com base
> nesses identificadores, garantindo precisão no controle do equipamento e
> eliminando ambiguidades no registro de movimentações."
>
> — `Requisitos/Casos de Uso/regrasDeNegocio-ProjetoresUnifor.md`

A regra não diz **qual** simbologia usar. Ela pede um identificador único e um
código legível por máquina. É aí que entram as opções abaixo.

## 2. QR Code também é código de barras

Essa distinção confunde, então vale deixar registrado:

- **Código de barras 1D (linear):** barras verticais de larguras diferentes.
  Exemplos: **EAN-13**, **Code 128**, Code 39, UPC-A.
- **Código de barras 2D (matricial):** quadrados organizados em grade.
  Exemplos: **QR Code**, Data Matrix, PDF417.

O QR Code é normatizado pela **ISO/IEC 18004** e é, tecnicamente, um código de
barras bidimensional. Portanto, a PoC **já entrega um código de barras** no
sentido amplo da RN8: o QR Code carrega o número único do projetor.

O que **não** está implementado é a simbologia **linear 1D**. Na prática, para o
uso do projeto (etiqueta impressa e leitura pela câmera do celular), o QR Code é
a opção mais adequada — os motivos estão na seção 3.

## 3. Formatos avaliados

| Formato | Norma | Tipo | Dados aceitos | Capacidade | Leitura pela câmera do celular |
| --- | --- | --- | --- | --- | --- |
| **QR Code** | ISO/IEC 18004:2024 | 2D | numérico, alfanumérico, bytes, kanji | até ~4.300 caracteres (versão 40, nível L) | Excelente, inclusive de longe e com QR danificado |
| **EAN-13** | GS1 / GTIN-13 | 1D | **somente 13 dígitos** | fixo | Boa, mas exige enquadramento próximo e nítido |
| **Code 128** | ISO/IEC 15417:2007 | 1D | ASCII completo (letras, números, símbolos) | variável | Boa, mesmo enquadramento do EAN-13 |

**Diferenças que importam para o projeto:**

- **QR Code** tem correção de erro Reed-Solomon nos níveis L/M/Q/H. No app é
  usado o nível **H** (~30% de área danificada tolerada). Aceita o prefixo
  `projetor:` e números alfanuméricos como `P-04`. Pode ser lido de uma foto da
  galeria, de um cartaz ou da tela de outro celular.
- **EAN-13** é rígido: exatamente 13 dígitos numéricos, sendo o último um dígito
  verificador calculado por **módulo 10**. Não aceita `projetor:P-04`. Serviria
  se o patrimônio fosse um GTIN de 13 dígitos.
- **Code 128** é o 1D mais flexível: aceita o identificador atual
  (`projetor:P-04`) e usa três conjuntos de caracteres (A/B/C), com verificação
  **módulo 103**. É a escolha natural se o grupo decidir manter o formato de
  texto e migrar para 1D.

## 4. O que o ZXing suporta

O ZXing é a biblioteca usada no app e cobre **geração e leitura** dos três
formatos. A API é a mesma; o que muda é a classe do *writer* e a lista de
formatos permitidos no *reader*.

| Operação | QR Code | EAN-13 | Code 128 |
| --- | --- | --- | --- |
| Gerar | `QRCodeWriter` | `EAN13Writer` | `Code128Writer` |
| Ler (imagem) | `MultiFormatReader` com `POSSIBLE_FORMATS` | idem | idem |
| Ler (câmera) | `ScanOptions.QR_CODE` | `ScanOptions.ONE_D_CODE_TYPES` | idem |

Ou seja: **a limitação de só ler QR não é da biblioteca, é uma escolha do app.**
Hoje os dois leitores estão restritos de propósito:

- `MainActivity` → `options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)`;
- `LeitorQrImagem` → `DecodeHintType.POSSIBLE_FORMATS` com apenas
  `BarcodeFormat.QR_CODE`.

Essa restrição evita que o app aceite um código de barras de produto por engano
como se fosse um projetor. Nada impede ampliá-la depois.

## 5. O que a PoC implementa hoje

| Item | Situação |
| --- | --- |
| Gerar QR Code do patrimônio | ✅ implementado (`GeradorQrCode`) |
| Ler QR por câmera | ✅ implementado (`MainActivity` + zxing-android-embedded) |
| Ler QR por imagem | ✅ implementado (`LeitorQrImagem`) |
| Recuperar o mesmo valor gerado | ✅ comprovado nas 7 evidências (ver `POC-README.md`) |
| Gerar EAN-13 | ❌ não implementado |
| Gerar Code 128 | ❌ não implementado |
| Ler EAN-13 / Code 128 | ❌ não implementado (formatos restritos a QR) |
| Documentar código de barras | ✅ este documento |

**Por que só QR:** o escopo combinado com o professor foi QR Code. A issue pede
para **testar e documentar** código de barras **quando suportado**; como a
biblioteca suporta, a documentação está aqui, mas a implementação 1D ficou como
próximo passo para não alterar o app já validado.

## 6. Como seria implementar 1D (se necessário)

As mudanças são pequenas e ficam concentradas em três pontos.

### 6.1 Gerar EAN-13

```java
// EAN-13 precisa de 12 ou 13 dígitos; com 12, o ZXing calcula o dígito
// verificador (módulo 10). Ex.: "789123456789" -> "7891234567898".
BitMatrix matriz = new EAN13Writer().encode(
        "789123456789", BarcodeFormat.EAN_13, 600, 200, dicas);
```

> `EAN13Writer` lança exceção para conteúdo não numérico ou com tamanho
> diferente de 12/13. O identificador `projetor:1` **não serve** para EAN-13.

### 6.2 Gerar Code 128

```java
// Code 128 aceita o formato atual de texto.
BitMatrix matriz = new Code128Writer().encode(
        "projetor:P-04", BarcodeFormat.CODE_128, 800, 200, dicas);
```

### 6.3 Ler os três formatos

```java
// LeitorQrImagem: acrescentar os formatos desejados.
dicas.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
        BarcodeFormat.QR_CODE,
        BarcodeFormat.EAN_13,
        BarcodeFormat.CODE_128));
dicas.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
```

```java
// MainActivity: liberar a câmera para QR + códigos lineares.
ScanOptions options = new ScanOptions();
options.setDesiredBarcodeFormats(
        ScanOptions.QR_CODE,
        ScanOptions.ONE_D_CODE_TYPES);
```

Depois disso é preciso **separar os tipos** no resultado: um EAN-13 lido é um
número de 13 dígitos, não um `projetor:<n>`. O app teria que decidir o que fazer
com cada formato (rejeitar, converter para número ou abrir a tela do projetor).

### 6.4 Decisão pendente com o professor

- Se o patrimônio for **13 dígitos numéricos** → EAN-13 faz sentido.
- Se continuar como **texto** (`projetor:P-04`) → Code 128 é o único 1D viável.
- Se o objetivo for **etiqueta + celular** → QR Code continua sendo o melhor
  custo-benefício e já está pronto.

## 7. Recomendação

1. **Manter o QR Code** como identificador principal: cabe texto, tolera dano,
   é lido de longe e funciona bem com a câmera do celular.
2. **Se a RN8 for cobrada ao pé da letra**, acrescentar **Code 128** na etiqueta
   impressa, usando o mesmo texto do QR (`projetor:<número>`), e liberar a
   leitura 1D no app. Isso mantém **um único identificador** para as duas
   simbologias.
3. **Não usar EAN-13** a menos que o número de patrimônio seja realmente um
   GTIN de 13 dígitos; o formato atual não cabe.

## 8. Limitações desta documentação

- Os exemplos de EAN-13 e Code 128 acima são **trechos de referência**, não
  código compilado nesta entrega. A PoC executável é a de QR Code.
- Não foram geradas evidências de leitura 1D, porque o app não as produz hoje.
- A escolha final da simbologia depende da definição do formato de patrimônio
  (o comentário da issue fala em 15 caracteres; o app usa `projetor:<número>`).

## 9. Referências

- ISO/IEC 18004:2024 — QR Code.
- ISO/IEC 15417:2007 — Code 128.
- GS1 — EAN-13 / GTIN-13.
- Resumo de pesquisa do grupo:
  [`resumo-pesquisa-QR-Barcode.md`](../resumo-pesquisa-QR-Barcode.md)
- Regra RN8:
  [`../../../Requisitos/Casos de Uso/regrasDeNegocio-ProjetoresUnifor.md`](../../../Requisitos/Casos%20de%20Uso/regrasDeNegocio-ProjetoresUnifor.md)
- ZXing (formatos e *writers*): <https://github.com/zxing/zxing>
