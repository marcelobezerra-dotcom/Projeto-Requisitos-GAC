# PoC — Impressão Digital

> Responsável: Nikolas · Issue #5 · Milestone Sprint 1 (18/09/2026)

## O que este PoC demonstra

O pipeline conceitual de um sistema de reconhecimento por impressão digital:

`captura → pré-processamento → extração de características (minúcias) → matching (escore de similaridade)`

O script tem dois modos:

- **`simulate`** (padrão, sem dependência de hardware ou dataset): gera dois
  "templates" de minúcias sintéticos representando duas capturas do mesmo
  dedo (com leve ruído e deslocamento, como aconteceria ao reposicionar o
  dedo no leitor) e uma captura de um dedo diferente. Faz o matching entre
  eles e mostra o escore.
- **`real`**: usa imagens reais de impressão digital (ex.: amostras do
  dataset público FVC) e a biblioteca open-source `afis`, que reimplementa
  em Python o pipeline do **NBIS** (NIST Biometric Image Software):
  extrator de minúcias **MINDTCT** + matcher **Bozorth3**.

## Por que dois modos

Não temos leitor biométrico físico disponível para a pesquisa, e datasets
reais de impressão digital (como o FVC) exigem cadastro/uso restrito a fins
de pesquisa. O modo `simulate` cobre a recomendação do roteiro de
"simular leitura/matching se hardware ou dados não disponíveis" e já
demonstra a lógica central do algoritmo. O modo `real` fica pronto para
quando tivermos acesso a imagens de um dataset público.

## Instalação

```bash
python -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

## Execução

Modo simulado (não precisa de nenhum arquivo de imagem):

```bash
python fingerprint_poc.py --mode simulate
```

Saída esperada:

```
== Mesmo dedo, duas capturas ==
{'n_minutiae_a': 30, 'n_minutiae_b': 30, 'matched_pairs': 30, 'score': 100.0, 'decision': True}
Resultado: MATCH

== Dedos diferentes ==
{'n_minutiae_a': 30, 'n_minutiae_b': 30, 'matched_pairs': 0, 'score': 0.0, 'decision': False}
Resultado: NO MATCH
```

Modo real (com duas imagens de impressão digital, ex. do dataset FVC):

```bash
python fingerprint_poc.py --mode real --img-a caminho/dedo_a_captura1.png --img-b caminho/dedo_a_captura2.png
```

## Limitações conhecidas

- O modo `simulate` usa um algoritmo de matching simplificado (alinhamento
  por centroide + contagem de minúcias próximas), só para fins didáticos —
  não é comparável em robustez a um matcher real como o Bozorth3.
- O modo `real` depende de imagens com qualidade mínima de captura; imagens
  sintéticas simples (sem cristas/minúcias reais) não geram minúcias
  detectáveis pelo MINDTCT — testado durante o desenvolvimento deste PoC.
- Nenhuma imagem de impressão digital real de uma pessoa foi usada ou
  armazenada neste PoC (ver observações de privacidade no resumo).

## Referências

Ver o resumo de 1 página (`Resumo-Impressao-Digital.md`) na pasta.
