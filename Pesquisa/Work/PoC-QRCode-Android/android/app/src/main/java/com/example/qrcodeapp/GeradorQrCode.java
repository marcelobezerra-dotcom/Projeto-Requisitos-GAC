package com.example.qrcodeapp;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * Geração do QR Code do projetor (Issue #4, item 4).
 *
 * <p>Isolado da Activity para que os testes e a geração das evidências usem
 * exatamente o mesmo código do app — se a evidência do repositório fosse
 * produzida por outro caminho, ela não provaria nada sobre o app.
 */
final class GeradorQrCode {

    /**
     * Resolução do QR gerado. Maior que a ImageView para não borrar ao ampliar.
     */
    static final int TAMANHO_PADRAO = 600;

    private GeradorQrCode() {
    }

    /**
     * @param conteudo texto a codificar, no formato definido por
     *                 {@link Projetor#conteudoQrCode()}.
     * @param tamanho  lado do bitmap resultante, em pixels.
     * @throws WriterException se o conteúdo não couber num QR Code.
     */
    static Bitmap gerar(String conteudo, int tamanho) throws WriterException {
        if (tamanho <= 0) {
            throw new IllegalArgumentException("tamanho inválido: " + tamanho);
        }

        Map<EncodeHintType, Object> hints = new HashMap<>();
        // H (recupera ~30%): etiqueta de projetor vive em ambiente com poeira,
        // marca de caneta e reflexo, onde um QR sem redundância para de ler.
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // Margem 1: o padrão do ZXing é 4, que só é necessário em leitores sem
        // borda. A etiqueta impressa já tem margem própria.
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                conteudo, BarcodeFormat.QR_CODE, tamanho, tamanho, hints);

        // Converte o BitMatrix direto num array de pixels: um setPixel por
        // módulo custaria centenas de milhares de chamadas na main thread.
        int[] pixels = new int[tamanho * tamanho];
        for (int y = 0; y < tamanho; y++) {
            int linha = y * tamanho;
            for (int x = 0; x < tamanho; x++) {
                pixels[linha + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        return Bitmap.createBitmap(pixels, tamanho, tamanho, Bitmap.Config.ARGB_8888);
    }

    static Bitmap gerar(String conteudo) throws WriterException {
        return gerar(conteudo, TAMANHO_PADRAO);
    }
}
