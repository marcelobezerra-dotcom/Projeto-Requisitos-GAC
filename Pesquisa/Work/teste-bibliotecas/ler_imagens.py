"""
Leitor de imagens de teste — compara zxing-cpp e OpenCV.

Lê as 3 imagens geradas por gerar_imagens.py e imprime:
  - Tipo de símbolo detectado
  - Conteúdo decodificado
  - Tempo de leitura (milissegundos)

Uso:
    python ler_imagens.py
"""

import os
import time
import csv
from pathlib import Path
from PIL import Image

# ── zxing-cpp ─────────────────────────────────────────────────
import zxingcpp

# ── OpenCV (QR Code nativo) ──────────────────────────────────
import cv2

# ── Configuração ──────────────────────────────────────────────
PASTA_IMAGENS = "imagens_teste"
ARQUIVO_RESULTADO = "resultado-teste.csv"

IMAGENS_ESPERADAS = {
    "qrcode_exemplo.png": "QR_CODE",
    "ean13_exemplo.png": "EAN_13",
    "code128_exemplo.png": "CODE_128",
}


# ── Leitura com zxing-cpp ─────────────────────────────────────
def ler_zxing(caminho: str) -> dict:
    """Lê a imagem com zxing-cpp."""
    img = Image.open(caminho)
    inicio = time.perf_counter()
    resultado = zxingcpp.read_barcode(img)
    fim = time.perf_counter()
    tempo_ms = (fim - inicio) * 1000

    if resultado is not None:
        return {
            "tipo": str(resultado.format),
            "conteudo": resultado.text,
            "tempo_ms": round(tempo_ms, 2),
        }
    return {"tipo": "N/A", "conteudo": "(não decodificado)", "tempo_ms": round(tempo_ms, 2)}


# ── Leitura com OpenCV ────────────────────────────────────────
def ler_opencv(caminho: str) -> dict:
    """Lê a imagem com OpenCV (detecta QR Code nativamente)."""
    img = cv2.imread(caminho)
    if img is None:
        return {"tipo": "N/A", "conteudo": "(imagem não lida)", "tempo_ms": 0}

    detector = cv2.QRCodeDetector()
    inicio = time.perf_counter()
    dados, _, _ = detector.detectAndDecode(img)
    fim = time.perf_counter()
    tempo_ms = (fim - inicio) * 1000

    if dados:
        return {"tipo": "QR_CODE", "conteudo": dados, "tempo_ms": round(tempo_ms, 2)}
    return {"tipo": "N/A", "conteudo": "(não decodificado)", "tempo_ms": round(tempo_ms, 2)}


# ── Main ──────────────────────────────────────────────────────
def main():
    print("=" * 70)
    print("  TESTE DE BIBLIOTECAS — LEITURA DE QR CODE E CÓDIGO DE BARRAS")
    print("=" * 70)

    linhas_csv = []

    for nome_img, tipo_esperado in IMAGENS_ESPERADAS.items():
        caminho = os.path.join(PASTA_IMAGENS, nome_img)
        if not os.path.exists(caminho):
            print(f"\n[!] Imagem não encontrada: {caminho}")
            print("    Execute primeiro: python gerar_imagens.py")
            continue

        print(f"\n{'-' * 70}")
        print(f"  Imagem: {nome_img}  |  Tipo esperado: {tipo_esperado}")
        print(f"{'-' * 70}")

        # zxing-cpp
        res_zxing = ler_zxing(caminho)
        # Normalizar nomes do zxing para comparar com o esperado
        tipo_zxing = res_zxing["tipo"].replace(" ", "_").replace("-", "_").upper()
        ok_zxing = tipo_zxing == tipo_esperado
        print(f"\n  zxing-cpp:")
        print(f"    Tipo detectado: {res_zxing['tipo']} {'OK' if ok_zxing else 'ERRO (esperado: ' + tipo_esperado + ')'}")
        print(f"    Conteudo:       {res_zxing['conteudo']}")
        print(f"    Tempo:          {res_zxing['tempo_ms']} ms")

        # OpenCV
        res_opencv = ler_opencv(caminho)
        ok_opencv = res_opencv["tipo"] == tipo_esperado
        print(f"\n  OpenCV:")
        print(f"    Tipo detectado: {res_opencv['tipo']} {'OK' if ok_opencv else 'ERRO (esperado: ' + tipo_esperado + ')'}")
        print(f"    Conteúdo:       {res_opencv['conteudo']}")
        print(f"    Tempo:          {res_opencv['tempo_ms']} ms")

        # Registrar para CSV
        linhas_csv.append({
            "imagem": nome_img,
            "tipo_esperado": tipo_esperado,
            "biblioteca": "zxing-cpp",
            "tipo_detectado": res_zxing["tipo"],
            "conteudo": res_zxing["conteudo"],
            "tempo_ms": res_zxing["tempo_ms"],
            "sucesso": ok_zxing,
        })
        linhas_csv.append({
            "imagem": nome_img,
            "tipo_esperado": tipo_esperado,
            "biblioteca": "opencv",
            "tipo_detectado": res_opencv["tipo"],
            "conteudo": res_opencv["conteudo"],
            "tempo_ms": res_opencv["tempo_ms"],
            "sucesso": ok_opencv,
        })

    # Salvar CSV
    if linhas_csv:
        with open(ARQUIVO_RESULTADO, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=linhas_csv[0].keys())
            writer.writeheader()
            writer.writerows(linhas_csv)
        print(f"\n{'=' * 70}")
        print(f"  Resultados salvos em: {ARQUIVO_RESULTADO}")
        print(f"{'=' * 70}")

    # Resumo
    print(f"\n{'=' * 70}")
    print("  RESUMO")
    print(f"{'=' * 70}")
    total = len(linhas_csv)
    sucessos = sum(1 for l in linhas_csv if l["sucesso"])
    print(f"  Total de testes: {total}")
    print(f"  Sucesso: {sucessos}/{total}")
    print(f"  Taxa de acerto: {sucessos/total*100:.1f}%" if total > 0 else "  Nenhum teste executado")


if __name__ == "__main__":
    main()
