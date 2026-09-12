# Links Úteis — Pesquisa QR Code / Código de Barras

Lista de referências rápidas usadas na pesquisa de formatos e bibliotecas de leitura (Issue #3). Fichas de leitura completas de cada uma das 3 referências principais estão em `Pesquisa/Biblioteca/Fichas/`.

## Normas e padrões

- **QR Code — ISO/IEC 18004:2024**
  https://www.iso.org/standard/83389.html
  Norma oficial do QR Code (versões 1–40, modos de codificação, correção de erro Reed-Solomon).

- **QR Code — explicação didática completa (versões, capacidade, máscaras)**
  https://qrcodefyi.com/guide/qr-code-standards/

- **EAN-13 — especificação GS1 (estrutura, dígito verificador)**
  https://support.gs1nz.org/hc/en-us/articles/360039814891-EAN-13-Barcode-Specifications

- **EAN-13 — explicação detalhada com exemplos de cálculo**
  https://www.morovia.com/kb/EAN13-Specification-10633.html

- **EAN-8 (variante compacta do EAN-13)**
  https://en.wikipedia.org/wiki/EAN-8

- **Code 128 — FAQ técnico completo (ISO/IEC 15417)**
  https://www.precisionid.com/code-128-faq/

- **Code 128 — Sets A, B, C explicados com exemplos**
  https://www.barcodesinc.com/articles/code128.htm

## Bibliotecas de leitura — Python

- **pyzbar** (wrapper do ZBar) — mais simples de usar, boa opção padrão para QR + 1D
  https://note.nkmk.me/en/python-pyzbar-barcode-qrcode/

- **OpenCV + pyzbar** — tutorial prático de leitura com webcam
  https://learnopencv.com/barcode-and-qr-code-scanner-using-zbar-and-opencv/

- **zxing-cpp (binding Python)** — mais rápido, suporta mais formatos (QR, DataMatrix, Aztec, PDF417, EAN, Code128, Codabar, ITF)
  https://packages.debian.org/ru/forky/s390x/python3-zxing-cpp

- **Benchmark comparativo: ZXing-cpp vs PyZBar vs Dynamsoft (velocidade e taxa de acerto)**
  https://www.dynamsoft.com/codepool/python-zxing-zbar-barcode.html

- **Benchmark QR: 7 bibliotecas testadas em 536 imagens reais**
  https://www.dynamsoft.com/codepool/qr-code-reading-benchmark-and-comparison.html

## Bibliotecas de leitura — JavaScript

- **jsQR** — biblioteca pura JS, leve, só QR Code (não lê 1D)
  https://github.com/cozmo/jsQR (ver comparativo abaixo)

- **@zxing/library (ZXing-js)** — suporta QR + vários formatos 1D, bundle maior
  https://npm-compare.com/@zxing/library,html5-qrcode,jsqr,qr-scanner,qrcode-reader

- **Quagga2** — especializada em códigos de barras 1D (EAN, Code128 etc.), não lê QR
  https://scanbot.io/blog/popular-open-source-javascript-barcode-scanners/

- **html5-qrcode** — usa ZXing-js por baixo, suporta 1D+2D com UI pronta, mas pouco mantida atualmente
  https://scanbot.io/blog/quagga2-vs-html5-qrcode-scanner/

## Notas de uso

- Para QR *e* código de barras 1D no mesmo protótipo: melhor usar **zxing-cpp** (Python) ou **@zxing/library** (JS), pois cobrem ambos os formatos.
- Se o protótipo for **só QR**, jsQR (JS) ou pyzbar (Python) já bastam e são mais leves.
- Se for **só código de barras 1D** (EAN/Code128) em JS, Quagga2 é a opção mais estável.
