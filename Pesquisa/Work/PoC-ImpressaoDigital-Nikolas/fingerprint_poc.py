"""
PoC - Reconhecimento por Impressão Digital
Pesquisa GAC 2026.02 - Nikolas

Objetivo: demonstrar o pipeline conceitual de reconhecimento por impressão
digital (captura -> pré-processamento -> extração de características ->
matching) de duas formas:

  --mode simulate (padrão): gera "templates" de minúcias sintéticos e faz o
      matching entre eles. Não depende de hardware nem de dataset real -
      útil para a apresentação da PoC quando não há leitor biométrico
      disponível, conforme sugerido no roteiro de pesquisa.

  --mode real: usa imagens de impressão digital reais (ex.: amostras do
      dataset público FVC) e a biblioteca open-source `afis`
      (extrator MINDTCT + matcher Bozorth3, o mesmo algoritmo usado no NBIS
      do NIST) para extrair minúcias e calcular o escore de similaridade.

Uso:
    python fingerprint_poc.py --mode simulate
    python fingerprint_poc.py --mode real --img-a caminho/a.png --img-b caminho/b.png
"""

import argparse
import math
import random
from dataclasses import dataclass


# ---------------------------------------------------------------------------
# Modo SIMULADO: gera minúcias sintéticas e faz o matching
# ---------------------------------------------------------------------------

@dataclass
class Minutia:
    x: float
    y: float
    angle: float  # orientação da crista, em radianos


def generate_template(seed: int, n_minutiae: int = 30, size: int = 300) -> list[Minutia]:
    """Simula a etapa de 'extração de características': gera um conjunto de
    minúcias (terminações/bifurcações de cristas) com posição e ângulo."""
    rng = random.Random(seed)
    return [
        Minutia(
            x=rng.uniform(0, size),
            y=rng.uniform(0, size),
            angle=rng.uniform(0, 2 * math.pi),
        )
        for _ in range(n_minutiae)
    ]


def capture_variation(template: list[Minutia], rng: random.Random,
                       jitter: float = 4.0, angle_jitter: float = 0.08,
                       dx: float = 6.0, dy: float = -4.0) -> list[Minutia]:
    """Simula uma segunda captura do MESMO dedo: leve ruído de sensor +
    pequeno deslocamento/rotação por causa do reposicionamento do dedo."""
    return [
        Minutia(
            x=m.x + dx + rng.uniform(-jitter, jitter),
            y=m.y + dy + rng.uniform(-jitter, jitter),
            angle=m.angle + rng.uniform(-angle_jitter, angle_jitter),
        )
        for m in template
    ]


def match_templates(a: list[Minutia], b: list[Minutia],
                     dist_threshold: float = 8.0,
                     angle_threshold: float = 0.25) -> dict:
    """Simula a etapa de 'matching': alinha os templates pelo centroide
    (equivalente ao alinhamento global feito por algoritmos como o
    Bozorth3) e conta quantas minúcias coincidem dentro de uma tolerância
    de distância e ângulo."""

    def centroid(pts):
        return (sum(p.x for p in pts) / len(pts), sum(p.y for p in pts) / len(pts))

    cx_a, cy_a = centroid(a)
    cx_b, cy_b = centroid(b)
    shift_x, shift_y = cx_a - cx_b, cy_a - cy_b

    matched_pairs = 0
    used_b = set()
    for ma in a:
        for j, mb in enumerate(b):
            if j in used_b:
                continue
            mb_aligned_x = mb.x + shift_x
            mb_aligned_y = mb.y + shift_y
            dist = math.hypot(ma.x - mb_aligned_x, ma.y - mb_aligned_y)
            angle_diff = abs((ma.angle - mb.angle + math.pi) % (2 * math.pi) - math.pi)
            if dist <= dist_threshold and angle_diff <= angle_threshold:
                matched_pairs += 1
                used_b.add(j)
                break

    score = matched_pairs / max(len(a), len(b)) * 100
    decision = score >= 40.0  # limiar simples só para fins didáticos
    return {
        "n_minutiae_a": len(a),
        "n_minutiae_b": len(b),
        "matched_pairs": matched_pairs,
        "score": round(score, 1),
        "decision": decision,
    }


def run_simulation():
    rng = random.Random(42)

    finger_1_capture_1 = generate_template(seed=1)
    finger_1_capture_2 = capture_variation(finger_1_capture_1, rng)
    finger_2_capture_1 = generate_template(seed=2)

    print("== Mesmo dedo, duas capturas ==")
    result_same = match_templates(finger_1_capture_1, finger_1_capture_2)
    print(result_same)
    print("Resultado:", "MATCH" if result_same["decision"] else "NO MATCH")

    print("\n== Dedos diferentes ==")
    result_diff = match_templates(finger_1_capture_1, finger_2_capture_1)
    print(result_diff)
    print("Resultado:", "MATCH" if result_diff["decision"] else "NO MATCH")


# ---------------------------------------------------------------------------
# Modo REAL: usa imagens de fato + biblioteca `afis` (NBIS: MINDTCT + Bozorth3)
# ---------------------------------------------------------------------------

def run_real(img_a_path: str, img_b_path: str):
    import numpy as np
    from PIL import Image
    import afis

    extractor = afis.MindtctExtractor()

    def load(path):
        return np.array(Image.open(path).convert("L"))

    template_a = extractor.extract_minutiae(load(img_a_path))
    template_b = extractor.extract_minutiae(load(img_b_path))

    result = extractor.match(template_a, template_b)
    print(result)
    print("Resultado:", "MATCH" if result.decision else "NO MATCH")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="PoC de reconhecimento por impressão digital")
    parser.add_argument("--mode", choices=["simulate", "real"], default="simulate")
    parser.add_argument("--img-a", help="caminho da imagem A (modo real)")
    parser.add_argument("--img-b", help="caminho da imagem B (modo real)")
    args = parser.parse_args()

    if args.mode == "simulate":
        run_simulation()
    else:
        if not args.img_a or not args.img_b:
            parser.error("--mode real exige --img-a e --img-b")
        run_real(args.img_a, args.img_b)
