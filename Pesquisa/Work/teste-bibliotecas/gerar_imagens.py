"""
Gerador de imagens de teste para QR Code, EAN-13 e Code 128.
Gera 3 imagens PNG na pasta 'imagens_teste/'.

Uso:
    python gerar_imagens.py
"""

import os
import qrcode
import barcode
from barcode.writer import ImageWriter
from PIL import Image, ImageDraw, ImageFont

# ── Configuração ──────────────────────────────────────────────
PASTA = "imagens_teste"
os.makedirs(PASTA, exist_ok=True)

# ── 1. QR Code ────────────────────────────────────────────────
def gerar_qr():
    """Gera um QR Code com uma URL de exemplo."""
    qr = qrcode.QRCode(
        version=1,
        error_correction=qrcode.constants.ERROR_CORRECT_M,
        box_size=10,
        border=4,
    )
    qr.add_data("https://github.com/marcelobezerra-dotcom/Projeto-Requisitos-GAC")
    qr.make(fit=True)
    img = qr.make_image(fill_color="black", back_color="white")
    caminho = os.path.join(PASTA, "qrcode_exemplo.png")
    img.save(caminho)
    print(f"[OK] QR Code salvo em: {caminho}")
    return caminho


# ── 2. EAN-13 ─────────────────────────────────────────────────
def gerar_ean13():
    """
    Gera um EAN-13 válido.
    Código: 590123412345 (fabricante fictício 590 + produto 123412345)
    A biblioteca python-barcode calcula o dígito verificador automaticamente.
    """
    codigo = "590123412345"  # 12 dígitos (o 13º = check digit, calculado)
    ean_class = barcode.get_barcode_class("ean13")
    ean = ean_class(codigo, writer=ImageWriter())
    caminho_base = os.path.join(PASTA, "ean13_exemplo")
    ean.save(caminho_base)  # salva como ean13_exemplo.png
    caminho = caminho_base + ".png"
    print(f"[OK] EAN-13 salvo em: {caminho}")
    return caminho


# ── 3. Code 128 ───────────────────────────────────────────────
def gerar_code128():
    """
    Gera um Code 128 com dados que forçam os 3 Code Sets:
    - 'GAC-2026/' → usa Set A (maiúscula + hífen + dígitos + '/')
    - 'projeto-requisitos' → usa Set B (minúsculas + hífen)
    A biblioteca alterna automaticamente entre os sets.
    """
    dados = "GAC-2026/projeto-requisitos"
    code128_class = barcode.get_barcode_class("code128")
    code128 = code128_class(dados, writer=ImageWriter())
    caminho_base = os.path.join(PASTA, "code128_exemplo")
    code128.save(caminho_base)
    caminho = caminho_base + ".png"
    print(f"[OK] Code 128 salvo em: {caminho}")
    return caminho


# ── Execução ──────────────────────────────────────────────────
if __name__ == "__main__":
    print("=== Gerando imagens de teste ===\n")
    gerar_qr()
    gerar_ean13()
    gerar_code128()
    print(f"\nImagens salvas na pasta: {PASTA}/")
