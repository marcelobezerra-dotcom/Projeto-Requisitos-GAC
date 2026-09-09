# Pesquisa: Reconhecimento por Impressão Digital

**Autor:** Nikolas · **Projeto:** Grupo de Estudo GAC 2026.02 · **Issue:** #5

## O que foi estudado e por quê

A pesquisa cobre como sistemas de reconhecimento por impressão digital
funcionam de ponta a ponta: os tipos de sensor usados para capturar a
imagem, o pipeline de extração de características (minúcias) e matching, e
os cuidados de privacidade no tratamento desses dados. O objetivo é embasar
um requisito de identificação biométrica dentro do projeto SDD do grupo
(ex.: "como operador, quero identificar um usuário pela digital").

## Sensores

Os dois tipos mais usados comercialmente são o **óptico** e o
**capacitivo**. O sensor óptico captura uma imagem da digital por meio de
luz e uma câmera/CMOS, é mais barato e mais resistente fisicamente, mas é
mais fácil de enganar com uma boa foto da digital e é mais volumoso. O
sensor capacitivo mede diferenças de carga elétrica entre as cristas e
vales da pele, é mais compacto (por isso domina em celulares), tem melhor
precisão e resistência a spoofing, porém custa mais para fabricar. Existe
ainda o sensor **ultrassônico**, mais caro, com melhor desempenho em dedos
molhados ou sujos, usado principalmente em smartphones topo de linha.
Para um PoC de baixo custo (ex.: controle de acesso simples), sensores
ópticos USB são a opção mais viável; para produtos com maior exigência de
segurança, capacitivo é o padrão de mercado.

## Algoritmos de matching e bibliotecas

O fluxo padrão é: **captura → pré-processamento (realce de imagem) →
extração de minúcias (terminações e bifurcações de crista) → matching**
(comparação de dois conjuntos de minúcias, com alinhamento por rotação e
translação, gerando um escore de similaridade).

Duas bibliotecas open-source relevantes:

- **NBIS** (NIST Biometric Image Software) — conjunto de ferramentas de
  referência do governo americano, com o extrator **MINDTCT** e o matcher
  **Bozorth3**. Hoje existe até um pacote Python (`afis`) que embute essa
  mesma lógica, o que usamos no PoC deste repositório.
- **SourceAFIS** — engine de reconhecimento open-source (Apache 2.0),
  disponível em Java, .NET, Go e Rust, que também extrai minúcias e retorna
  um escore de similaridade entre duas digitais, com suporte a busca 1:N em
  bases maiores.

## Privacidade e tratamento de dados

Boas práticas identificadas: nunca armazenar a imagem bruta da digital,
apenas o template de minúcias (dado derivado, não reversível para a imagem
original); tratar esse template como dado sensível (idealmente
criptografado em repouso); e, para fins de pesquisa/PoC, usar apenas
datasets públicos de avaliação (como o FVC) ou dados sintéticos — nunca
digitais reais de pessoas sem consentimento explícito.

## Principais resultados

- O pipeline captura → minúcias → matching foi validado na prática com um
  PoC funcional (modo simulado, sem hardware) que diferencia corretamente
  "mesmo dedo" (escore alto) de "dedos diferentes" (escore zero).
- Testar o pipeline real (biblioteca `afis`/MINDTCT) com imagens sintéticas
  simples não funciona: o extrator não encontra minúcias em padrões sem
  cristas realistas — é necessário usar imagens de dataset real (ex. FVC)
  ou capturas de um sensor de verdade.
- Sensor óptico USB + biblioteca open-source (NBIS/afis ou SourceAFIS) é a
  combinação mais viável para um próximo PoC com hardware real, dado o
  custo e a disponibilidade.

## Limitações

O escore do modo simulado usa um matcher simplificado feito só para esta
PoC (não é o Bozorth3 real) — serve para ilustrar o conceito, não para
medir taxa de acerto real do sistema.

## Referências

1. Miaxis. *Optical vs Capacitive Fingerprint Scanners: What You Need to Know* (nov. 2025). https://miaxis.net/company-news/optical-vs-capacitive-fingerprint-scanners-what-you-need-to-know.html
2. Wikipedia. *Fingerprint Verification Competition (FVC)*. https://en.wikipedia.org/wiki/Fingerprint_Verification_Competition
3. Važan, R. *SourceAFIS* — engine de reconhecimento de impressão digital open-source. https://github.com/robertvazan/sourceafis-java
