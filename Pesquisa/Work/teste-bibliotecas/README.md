# Teste de Bibliotecas — QR Code e Código de Barras

Pasta de testes práticos da Issue #3 (QR Code / Código de Barras).

## Como rodar

### 1. Instalar dependências

```bash
# Criar ambiente virtual (recomendado)
python -m venv venv
venv\Scripts\activate        # Windows
# source venv/bin/activate   # Linux/Mac

# Instalar pacotes
pip install -r requirements.txt
```

**Observação sobre zxing-cpp:** no Windows, pode ser necessário instalar as
dependências de compilação (CMake + C++ compiler). Se `pip install zxing-cpp`
falhar, alternativas:
- Usar apenas pyzbar (funciona sem compilação)
- Ou instalar via conda: `conda install -c conda-forge zxing-cpp`

### 2. Gerar imagens de teste

```bash
python gerar_imagens.py
```

Cria a pasta `imagens_teste/` com 3 arquivos PNG:
- `qrcode_exemplo.png` — QR Code com URL do repositório
- `ean13_exemplo.png` — EAN-13 com dígito verificador válido
- `code128_exemplo.png` — Code 128 com dados em Set A + Set B

### 3. Rodar os testes de leitura

```bash
python ler_imagens.py
```

Lê cada imagem com **pyzbar** e **zxing-cpp**, imprime o tipo detectado,
conteúdo decodificado e tempo de leitura. Salva os resultados em
`resultado-teste.csv`.

## Arquivos

| Arquivo | Descrição |
|---|---|
| `gerar_imagens.py` | Gera as 3 imagens de teste |
| `ler_imagens.py` | Lê e compara pyzbar vs zxing-cpp |
| `requirements.txt` | Dependências Python |
| `imagens_teste/` | Imagens geradas (PNG) |
| `resultado-teste.csv` | Resultados dos testes (gerado automaticamente) |

## Limitações conhecidas

### pyzbar
- **Qualidade de imagem:** exige imagens com boa resolução e contraste.
  Imagens borradas ou com baixo contraste falham frequentemente.
- **Suporte a formatos 1D:** lê EAN-13 e Code 128, mas pode não detectar
  códigos em ângulos muito inclinados (>30°).
- **QR Code:** funciona bem para QR Codes estáticos (foto de tela, impressão).
  Não lê QR Codes de vídeo/câmera em tempo real (usar OpenCV para isso).

### zxing-cpp
- **Instalação no Windows:** pode exigir CMake e compilador C++ (MSVC).
  Alternativa: usar `pip install zxing-cpp` no Linux ou WSL.
- **Múltiplos códigos:** se a imagem conter vários códigos simultaneamente,
  retorna apenas o primeiro encontrado.
- **Micro QR Code / DataMatrix:** suporta, mas não testado neste PoC.

### python-barcode (geração)
- **EAN-13:** a biblioteca calcula o dígito verificador automaticamente
  (Módulo 10). Não é possível gerar EAN-13 com check digit inválido diretamente.
- **Code 128:** a alternância entre Code Sets A/B/C é automática; não é possível
  forçar um Set específico manualmente.

## Resultado esperado

| Imagem | pyzbar | zxing-cpp | Conteúdo esperado |
|---|---|---|---|
| qrcode_exemplo.png | QRCODE | QRCode | `https://github.com/marcelobezerra-dotcom/Projeto-Requisitos-GAC` |
| ean13_exemplo.png | EAN13 | EAN13 | `5901234123457` (com check digit) |
| code128_exemplo.png | CODE128 | CODE128 | `GAC-2026/projeto-requisitos` |

## Para documentar no resumo

Após rodar os testes, copie os dados do `resultado-teste.csv` para a seção
"Testar bibliotecas de leitura" do checklist da Issue #3, incluindo:
- Data/hora do teste
- Versões das bibliotecas (`pip show pyzbar zxing-cpp`)
- Resultado de cada teste (sucesso/falha, tempo)
- Observações sobre dificuldades encontradas
