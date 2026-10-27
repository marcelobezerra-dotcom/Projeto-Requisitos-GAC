# Aluguel de Projetores (Java / Android)

App Android nativo, em **Java**, para controlar o aluguel de projetores usando
**QR Code** — tudo salvo localmente no aparelho, sem internet.

> Documentação completa do PoC (formato do identificador, evidências, limitações
> e divergências com os requisitos): `../POC-README.md`

## O que o app faz

- **Tela inicial**: lista dos projetores cadastrados (na primeira abertura são
  cadastrados 1, 2 e 3 como exemplo), com status **Disponível** ou **Alugado**.
- **Botão "+"**: cadastra um novo projetor pelo número/identificador.
- **Tocar num projetor**: abre a tela dele, com o **QR Code exclusivo** daquele
  projetor (gerado com ZXing) e, conforme o status, o formulário de aluguel
  (nome do locatário, telefone, data prevista de devolução) ou os dados de quem
  alugou + botão de devolução.
- **📷 Ler QR Code de um projetor**: abre a câmera e leva direto à tela do
  projetor lido, sem procurar na lista.
- **🖼️ Ler QR Code de uma imagem**: escolhe uma foto da galeria e decodifica o
  QR Code pela mesma validação da câmera. Fotos de celular são reduzidas e
  rotacionadas conforme o metadado EXIF antes da leitura.

## Formato do QR Code

```
projetor:<número>
```

Exemplos: `projetor:1`, `projetor:12`, `projetor:P-04`. O prefixo existe para o
app distinguir o QR de um projetor de qualquer outro QR Code lido pela câmera
(uma URL, um contato, um Pix). A justificativa completa, os casos rejeitados e a
normalização do número estão no `POC-README.md`.

## Bibliotecas

| Biblioteca                            | Para quê                                        |
| ------------------------------------- | ----------------------------------------------- |
| `com.google.zxing:core`               | Geração do QR Code e leitura de imagem estática |
| `com.journeyapps:zxing-android-embedded` | Tela de câmera para leitura em tempo real    |
| `androidx.exifinterface`              | Orientação da foto (EXIF) antes de ler o QR     |
| `androidx.appcompat`, `material`, `recyclerview` | Interface                                |
| JUnit + Robolectric                   | Testes unitários                                |

Persistência em `SharedPreferences` com JSON, via `ProjetorStorage`.

## Como compilar

Requer **JDK 21** e **Android SDK 34** (`JAVA_HOME` e `ANDROID_HOME`
configurados). O projeto não usa Gradle Wrapper; com o Gradle 8.9 instalado:

```bash
gradle testDebugUnitTest    # 38 testes
gradle lintDebug            # análise estática
gradle assembleDebug        # APK de teste
gradle assembleRelease      # APK assinado (requer keystore.properties)
```

No Android Studio: `File > Open...` e aponte para esta pasta.

## Testes

38 testes unitários, todos passando:

| Classe                     | Testes | Cobre                                        |
| -------------------------- | ------ | -------------------------------------------- |
| `QrCodeImagemTest`         | 8      | Geração, leitura por imagem, JPEG, EXIF       |
| `ProjetorNormalizacaoTest` | 10     | Normalização e validação do identificador    |
| `ProjetorOrdenacaoTest`    | 8      | Ordenação da lista                            |
| `ProjetorStorageTest`      | 12     | Persistência, duplicidade, cadastro fantasma |

## Estrutura

```
android/
├── app/
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/qrcodeapp/
│       │   │   ├── MainActivity.java             (lista, cadastro, leitura QR)
│       │   │   ├── DetalheProjetorActivity.java  (detalhe, aluguel e devolução)
│       │   │   ├── GeradorQrCode.java            (geração do QR Code)
│       │   │   ├── LeitorQrImagem.java           (decodificação de imagem)
│       │   │   ├── Projetor.java                 (modelo e normalização)
│       │   │   ├── ProjetorStorage.java          (persistência local)
│       │   │   └── ProjetorAdapter.java          (lista visual)
│       │   └── res/
│       │       ├── layout/                       (activity_main, activity_detalhe_projetor, item_projetor)
│       │       ├── values/strings.xml
│       │       └── xml/data_extraction_rules.xml
│       └── test/java/com/example/qrcodeapp/      (4 classes de teste)
└── build.gradle, settings.gradle, gradle.properties
```

## Limitações conhecidas (resumo)

- Só **QR Code**; código de barras 1D não é suportado (divergência com a RN8,
  ver `POC-README.md`).
- O QR **não tem assinatura**: o número não é conferido contra cadastro. Ler um
  QR impostor abre a tela de um projetor que talvez não exista — mas **não**
  cadastra equipamento novo sem confirmação na tela.
- **Backup desligado**: desinstalar o app apaga o inventário. Não há exportação.
- A devolução **apaga** o histórico do aluguel, o que impede gerar os relatórios
  exigidos pela RN7.
- Os dados ficam só neste aparelho; não há sincronização nem autenticação.

## Próximos passos

- Exportar/importar o inventário em CSV.
- Guardar histórico de empréstimos em vez de apagar na devolução.
- Conferir o número do QR contra o cadastro antes de alugar.
- Suporte a EAN-13 e Code 128, se a RN8 for mantida como está.
- Etiquetas em PDF para imprimir e colar nos aparelhos.
