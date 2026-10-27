package com.example.qrcodeapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Lê um QR Code a partir de uma imagem da galeria (Issue #4, item 5).
 *
 * <p>Usa o mesmo {@code com.google.zxing:core} que gera os QR Codes do app, sem
 * depender da biblioteca de câmera: a câmera do {@code zxing-android-embedded}
 * decodifica em tempo real e não serve para imagem estática.
 *
 * <p>Foto de celular costuma vir gravada deitada (metadado EXIF) e com resolução
 * muito acima do necessário. Por isso a imagem é reduzida antes de decodificar e
 * rotacionada conforme o EXIF — sem isso, metade das fotos de QR Code falharia
 * só por causa da orientação.
 */
final class LeitorQrImagem {

    /**
     * Lado maior máximo, em pixels, usado na decodificação. Acima disso a
     * leitura não ganha nada em acerto e o custo de memória dispara: um bitmap
     * de 4000x3000 em ARGB_8888 sozinho ocupa 48 MB e derruba aparelhos de
     * entrada. 1600 px ainda deixa o QR Code com folga para ser lido.
     */
    private static final int DIMENSAO_MAXIMA = 1600;

    private LeitorQrImagem() {
    }

    /**
     * Tenta decodificar um QR Code na imagem escolhida.
     *
     * @return o texto contido no QR Code, ou {@code null} se a imagem for
     *         legível mas não tiver QR Code.
     * @throws IOException se a imagem não puder ser aberta ou não for suportada.
     */
    static String ler(Context contexto, Uri uri) throws IOException {
        int rotacaoGraus = lerRotacaoExif(contexto, uri);
        Bitmap bitmap = carregarBitmapReduzido(contexto, uri);

        try {
            Bitmap orientado = rotacionar(bitmap, rotacaoGraus);
            try {
                return decodificar(orientado);
            } finally {
                if (orientado != bitmap && !orientado.isRecycled()) {
                    orientado.recycle();
                }
            }
        } finally {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    /**
     * Lê a orientação dos metadados. Falha de leitura de EXIF não é erro: a
     * imagem pode não ter metadado nenhum, e nesse caso assume-se 0°.
     */
    private static int lerRotacaoExif(Context contexto, Uri uri) {
        try (InputStream entrada = contexto.getContentResolver().openInputStream(uri)) {
            if (entrada == null) {
                return 0;
            }
            int orientacao = new ExifInterface(entrada)
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
            switch (orientacao) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException | RuntimeException e) {
            return 0;
        }
    }

    /**
     * Abre a imagem em duas passadas: a primeira só para descobrir as dimensões
     * (e escolher o {@code inSampleSize}), a segunda para decodificar de fato.
     * Decodificar direto, em tamanho cheio, é a forma mais rápida de estourar a
     * memória do aparelho.
     */
    private static Bitmap carregarBitmapReduzido(Context contexto, Uri uri) throws IOException {
        int largura;
        int altura;

        BitmapFactory.Options medir = new BitmapFactory.Options();
        medir.inJustDecodeBounds = true;
        try (InputStream entrada = contexto.getContentResolver().openInputStream(uri)) {
            if (entrada == null) {
                throw new IOException("Não foi possível abrir a imagem");
            }
            BitmapFactory.decodeStream(entrada, null, medir);
        }

        largura = medir.outWidth;
        altura = medir.outHeight;
        if (largura <= 0 || altura <= 0) {
            throw new IOException("Formato de imagem não suportado");
        }

        BitmapFactory.Options carregar = new BitmapFactory.Options();
        carregar.inSampleSize = calcularAmostra(largura, altura);
        carregar.inPreferredConfig = Bitmap.Config.ARGB_8888;

        try (InputStream entrada = contexto.getContentResolver().openInputStream(uri)) {
            if (entrada == null) {
                throw new IOException("Não foi possível abrir a imagem");
            }
            Bitmap bitmap = BitmapFactory.decodeStream(entrada, null, carregar);
            if (bitmap == null) {
                throw new IOException("Não foi possível decodificar a imagem");
            }
            return bitmap;
        }
    }

    /** Maior potência de 2 que mantém a imagem dentro de {@link #DIMENSAO_MAXIMA}. */
    static int calcularAmostra(int largura, int altura) {
        int maiorLado = Math.max(largura, altura);
        int amostra = 1;
        while (maiorLado / amostra > DIMENSAO_MAXIMA) {
            amostra *= 2;
        }
        return amostra;
    }

    private static Bitmap rotacionar(Bitmap origem, int graus) {
        if (graus == 0) {
            return origem;
        }
        Matrix matriz = new Matrix();
        matriz.postRotate(graus);
        return Bitmap.createBitmap(origem, 0, 0, origem.getWidth(), origem.getHeight(),
                matriz, true);
    }

    /**
     * @return o texto do QR Code, ou {@code null} se não houver nenhum.
     */
    private static String decodificar(Bitmap bitmap) {
        Map<DecodeHintType, Object> dicas = new EnumMap<>(DecodeHintType.class);
        // Só QR Code: o app não implementa os formatos 1D da RN8 (ver README).
        dicas.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        dicas.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        MultiFormatReader leitor = new MultiFormatReader();
        leitor.setHints(dicas);
        try {
            Result resultado = leitor.decodeWithState(
                    new BinaryBitmap(new HybridBinarizer(new FonteLuminanciaBitmap(bitmap))));
            return resultado.getText();
        } catch (NotFoundException e) {
            return null;
        } finally {
            leitor.reset();
        }
    }

    /**
     * Adapta um {@link Bitmap} para o {@link LuminanceSource} do ZXing.
     *
     * <p>Não existe adaptador pronto para bitmap em {@code zxing-core} nem no
     * {@code zxing-android-embedded}, então a conversão para tons de cinza é
     * feita aqui, com a luminância BT.601: {@code Y = 33R + 34G + 33B} dividido
     * por 256. É a mesma conta que o ZXing usa nos pontos de amostragem dos
     * formatos 1D, então não há diferença de acerto em relação a ele.
     */
    private static final class FonteLuminanciaBitmap extends LuminanceSource {

        private final byte[] luminancia;

        FonteLuminanciaBitmap(Bitmap bitmap) {
            super(bitmap.getWidth(), bitmap.getHeight());
            int largura = bitmap.getWidth();
            int altura = bitmap.getHeight();
            int[] pixels = new int[largura * altura];
            bitmap.getPixels(pixels, 0, largura, 0, 0, largura, altura);
            luminancia = new byte[largura * altura];
            for (int i = 0; i < pixels.length; i++) {
                int cor = pixels[i];
                int vermelho = (cor >> 16) & 0xFF;
                int verde = (cor >> 8) & 0xFF;
                int azul = cor & 0xFF;
                luminancia[i] = (byte) ((vermelho * 33 + verde * 34 + azul * 33) >> 8);
            }
        }

        @Override
        public byte[] getRow(int y, byte[] linha) {
            int largura = getWidth();
            if (linha == null || linha.length < largura) {
                linha = new byte[largura];
            }
            System.arraycopy(luminancia, y * largura, linha, 0, largura);
            return linha;
        }

        @Override
        public byte[] getMatrix() {
            return luminancia;
        }
    }
}
