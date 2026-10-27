package com.example.qrcodeapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;
import androidx.test.core.app.ApplicationProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.GraphicsMode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Testa a leitura de QR Code a partir de imagem (Issue #4, item 5) e a
 * geração do QR Code (item 4), usando o mesmo {@link GeradorQrCode} e o mesmo
 * {@link LeitorQrImagem} do app.
 *
 * <p>Usa {@code GraphicsMode.NATIVE} de propósito: no modo legado o shadow de
 * {@code Bitmap} devolve uma imagem vazia para {@code createBitmap} com matriz,
 * que é justamente a chamada usada para girar a foto conforme o EXIF. No modo
 * nativo a rotação é executada pelo Skia de verdade, então o teste cobre o
 * código real em vez de um contorno.
 */
@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class QrCodeImagemTest {

    @Rule
    public TemporaryFolder pasta = new TemporaryFolder();

    private Context contexto;

    @Before
    public void setUp() {
        contexto = ApplicationProvider.getApplicationContext();
    }

    // --- item 4: geração ---

    @Test
    public void conteudoQrCodeUsaPrefixoENumeroNormalizado() {
        assertEquals("projetor:4", new Projetor("4").conteudoQrCode());
        // O prefixo é fixo e o número já normalizado: " 04 " não pode gerar
        // "projetor: 04 ", que a leitura rejeitaria.
        assertEquals("projetor:4", new Projetor(" 04 ").conteudoQrCode());
        assertEquals("projetor:P-04", new Projetor("p-04").conteudoQrCode());
    }

    @Test
    public void qrGeradoEhLidoDeVoltaPeloProprioApp() throws Exception {
        String conteudo = new Projetor("4").conteudoQrCode();
        Bitmap bitmap = GeradorQrCode.gerar(conteudo, GeradorQrCode.TAMANHO_PADRAO);

        Uri uri = gravarPng(bitmap, "ida-e-volta.png");

        assertEquals(conteudo, LeitorQrImagem.ler(contexto, uri));
    }

    // --- item 5: leitura por imagem ---

    @Test
    public void leQrCodeDeImagemPng() throws Exception {
        Bitmap bitmap = GeradorQrCode.gerar("projetor:12", 600);
        Uri uri = gravarPng(bitmap, "projetor-12.png");

        assertEquals("projetor:12", LeitorQrImagem.ler(contexto, uri));
    }

    @Test
    public void leQrCodeDeFotoJpegComQualidadeReduzida() throws Exception {
        // Foto de celular real vem em JPEG com compressão; um PNG perfeito
        // não provaria que a leitura aguenta ruído de fotografia.
        Bitmap bitmap = GeradorQrCode.gerar("projetor:7", 600);
        Uri uri = gravarJpeg(bitmap, 55, "foto-projetor-7.jpg");

        assertEquals("projetor:7", LeitorQrImagem.ler(contexto, uri));
    }

    @Test
    public void leQrCodeDeFotoGiradaUsandoMetadadoExif() throws Exception {
        // É o caso real: a câmera grava os pixels deitados e anota a rotação
        // no EXIF. Sem aplicar essa rotação, a leitura falha mesmo com o QR
        // enquadrado.
        Bitmap original = GeradorQrCode.gerar("projetor:21", 600);
        Bitmap girada = girar(original, 90);

        File arquivo = arquivo("foto-girada.jpg");
        escrever(girada, arquivo, Bitmap.CompressFormat.JPEG, 70);

        ExifInterface exif = new ExifInterface(arquivo.getAbsolutePath());
        exif.setAttribute(ExifInterface.TAG_ORIENTATION,
                String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
        exif.saveAttributes();

        assertEquals("projetor:21", LeitorQrImagem.ler(contexto, Uri.fromFile(arquivo)));
    }

    @Test
    public void devolveNuloQuandoNaoHaQrCodeNaImagem() throws Exception {
        Bitmap imagemQualquer = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        imagemQualquer.eraseColor(Color.WHITE);
        Uri uri = gravarPng(imagemQualquer, "sem-qr.png");

        assertNull(LeitorQrImagem.ler(contexto, uri));
    }

    @Test
    public void lidaConteudoDeQrDeOutroFormatoParaRejeitarDepoisNaTela() throws Exception {
        // Um QR Code qualquer (aqui, uma URL) tem de ser lido com sucesso aqui
        // para a validação de formato poder recusá-lo com a mensagem certa.
        Uri uri = gravarPng(qrComOutroConteudo("https://exemplo.com"), "url.png");

        assertEquals("https://exemplo.com", LeitorQrImagem.ler(contexto, uri));
    }

    @Test
    public void imagemGrandeEReduzidaAntesDeSerDecodificada() {
        // Sem redução, uma foto de 12 MP em ARGB_8888 ocupa ~48 MB e derruba
        // aparelhos de entrada. A amostra é sempre uma potência de 2 porque é
        // o único valor que o BitmapFactory decodifica rápido.
        assertEquals(1, LeitorQrImagem.calcularAmostra(1600, 1200));
        assertEquals(2, LeitorQrImagem.calcularAmostra(3200, 2400));
        assertEquals(4, LeitorQrImagem.calcularAmostra(6400, 4800));
        assertEquals(8, LeitorQrImagem.calcularAmostra(12800, 9600));

        // Sempre dentro do limite, qualquer que seja a proporção.
        for (int[] lado : new int[][]{{20000, 100}, {100, 20000}, {20000, 20000}, {1, 1}}) {
            int amostra = LeitorQrImagem.calcularAmostra(lado[0], lado[1]);
            assertTrue("amostra deve reduzir o lado maior", lado[0] / amostra <= 1600);
            assertTrue("menor lado não pode virar zero", lado[1] / amostra >= 1);
        }
    }

    // --- utilidades ---

    private static Bitmap qrComOutroConteudo(String conteudo) throws WriterException {
        Map<com.google.zxing.EncodeHintType, Object> hints = new HashMap<>();
        hints.put(com.google.zxing.EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        BitMatrix matriz = new com.google.zxing.MultiFormatWriter()
                .encode(conteudo, BarcodeFormat.QR_CODE, 500, 500, hints);
        int[] pixels = new int[500 * 500];
        for (int y = 0; y < 500; y++) {
            for (int x = 0; x < 500; x++) {
                pixels[y * 500 + x] = matriz.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        return Bitmap.createBitmap(pixels, 500, 500, Bitmap.Config.ARGB_8888);
    }

    private static Bitmap girar(Bitmap origem, int graus) {
        // A rotação é feita pixel a pixel em vez de Bitmap.createBitmap(..., matriz):
        // o shadow de Bitmap do Robolectric (modo legado) devolve uma imagem
        // vazia para a sobrecarga com matriz, e o teste falharia por causa do
        // shadow, não do código. O app usa a API de matriz normalmente.
        if (graus != 90) {
            throw new IllegalArgumentException("só 90° é usado nos testes");
        }
        int largura = origem.getWidth();
        int altura = origem.getHeight();
        int[] entrada = new int[largura * altura];
        origem.getPixels(entrada, 0, largura, 0, 0, largura, altura);

        int[] saida = new int[largura * altura];
        for (int sy = 0; sy < altura; sy++) {
            for (int sx = 0; sx < largura; sx++) {
                // sentido horário: destino tem largura = altura da origem
                int dx = altura - 1 - sy;
                int dy = sx;
                saida[dy * altura + dx] = entrada[sy * largura + sx];
            }
        }
        return Bitmap.createBitmap(saida, altura, largura, Bitmap.Config.ARGB_8888);
    }

    private Uri gravarPng(Bitmap bitmap, String nome) throws Exception {
        return gravar(bitmap, nome, Bitmap.CompressFormat.PNG, 100);
    }

    private Uri gravarJpeg(Bitmap bitmap, int qualidade, String nome) throws Exception {
        return gravar(bitmap, nome, Bitmap.CompressFormat.JPEG, qualidade);
    }

    private Uri gravar(Bitmap bitmap, String nome, Bitmap.CompressFormat formato,
                      int qualidade) throws Exception {
        File arquivo = arquivo(nome);
        assertNotNull(escrever(bitmap, arquivo, formato, qualidade));
        return Uri.fromFile(arquivo);
    }

    private static File escrever(Bitmap bitmap, File arquivo, Bitmap.CompressFormat formato,
                                int qualidade) throws Exception {
        try (FileOutputStream saida = new FileOutputStream(arquivo)) {
            bitmap.compress(formato, qualidade, saida);
        }
        return arquivo;
    }

    private File arquivo(String nome) throws Exception {
        return new File(pasta.getRoot(), nome);
    }
}
