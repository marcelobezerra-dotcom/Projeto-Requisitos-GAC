# Resumo de Pesquisa — QR Code e Código de Barras (Issue #3)

**Responsáveis:** Vinicios-Gabriel27 e [nome do parceiro]
**Data:** 10/09/2026

## 1. Formatos e padrões pesquisados

| Formato | Norma | Tipo de dado | Capacidade | Uso típico |
|---|---|---|---|---|
| **QR Code** | ISO/IEC 18004:2024 | Numérico, alfanumérico, byte, kanji | até ~4.300 caracteres (v40, nível L) | URLs, pagamentos, ingressos |
| **EAN-13** | Padrão GS1 (GTIN-13) | Numérico (13 dígitos) | fixo, 13 dígitos | varejo / PDV |
| **Code 128** | ISO/IEC 15417:2007 | Todo o ASCII (128 caracteres) | variável (até 48 na variante GS1-128) | logística, crachás, etiquetas |

**Principais diferenças:** o QR Code é 2D (matricial) e tolera dano parcial graças à correção de erro Reed-Solomon (níveis L/M/Q/H). EAN-13 e Code 128 são lineares (1D); o EAN-13 é fixo em 13 dígitos numéricos com um dígito verificador Módulo 10, enquanto o Code 128 é mais flexível (aceita letras e símbolos) e usa três conjuntos de caracteres (A/B/C) que podem ser alternados dentro do mesmo código, com verificação Módulo 103.

## 2. Bibliotecas de leitura testadas

**Python:**
- `pyzbar` (wrapper do ZBar) — simples, boa opção padrão. Benchmarks apontam ~71–77% de acerto em imagens reais.
- `zxing-cpp` — mais rápido (~40ms/imagem) e com mais formatos suportados (QR, EAN, Code128, DataMatrix, PDF417 etc.), porém cai bastante em imagens com múltiplos códigos.
- `opencv` (nativo, `cv2.QRCodeDetector`) — funciona sem dependências extras, mas com acurácia mais baixa que as opções dedicadas.

**JavaScript:**
- `jsQR` — leve, só QR Code, ideal se o protótipo não precisar de código de barras 1D.
- `@zxing/library` (ZXing-js) — cobre QR + formatos 1D (EAN, Code128 etc.), bundle maior (~9 MB fonte, mas minificado é menor).
- `Quagga2` — especializada em 1D (EAN/Code128), não lê QR.
- `html5-qrcode` — usa ZXing-js por baixo, já vem com UI de câmera pronta, mas está com manutenção reduzida.

**Recomendação para o PoC:** como o grupo precisa ler tanto QR quanto código de barras, a combinação mais prática é **zxing-cpp** no back-end/testes em Python e **@zxing/library** no front-end em JS — ambas cobrem os três formatos pesquisados (QR, EAN-13, Code128) sem precisar combinar múltiplas bibliotecas.

## 3. Exemplo de uso — Python (pyzbar)

```python
from pyzbar.pyzbar import decode
from PIL import Image

resultado = decode(Image.open("exemplo.png"))
for codigo in resultado:
    print(codigo.type, codigo.data.decode("utf-8"))
```

## 4. Exemplo de uso — JavaScript (jsQR)

```javascript
import jsQR from "jsqr";

const code = jsQR(imageData.data, imageData.width, imageData.height);
if (code) {
  console.log("Conteúdo:", code.data);
}
```

## 5. Referências (fichas completas em `Pesquisa/Biblioteca/Fichas/`)

1. ISO/IEC 18004:2024 — QR code bar code symbology specification.
2. GS1 — EAN-13 Barcode Specifications (GTIN-13).
3. ISO/IEC 15417:2007 — Code 128 bar code symbology specification.

Links complementares e benchmarks de bibliotecas em `Pesquisa/Biblioteca/links.md`.
