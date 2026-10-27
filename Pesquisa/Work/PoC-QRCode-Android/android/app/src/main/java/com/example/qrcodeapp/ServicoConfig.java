package com.example.qrcodeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.SecureRandom;

/**
 * Configuração do serviço HTTP embarcado: porta, token e credencial de rede.
 *
 * <p>Tudo editável em runtime pela {@link ServicoActivity}, nada fixo no código.
 * A porta tem que ser configurável porque o script do professor usa 8787, mas o
 * número pode mudar se o serviço for copiado para outro projeto. O token é o
 * mesmo que vai no {@code appnfc-test-token.txt} do PowerShell.
 */
public final class ServicoConfig {

    private static final String PREFS = "servico_prefs";
    private static final String KEY_PORTA = "porta";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_SERIAL = "serial";

    /** Mesma porta usada pelo test-api.ps1. */
    public static final int PORTA_PADRAO = 8787;

    /**
     * Gerador do token quando ainda não houve nenhum. 32 bytes aleatórios, o
     * mesmo formato do arquivo que o professor distribui.
     */
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private ServicoConfig() {
    }

    public static int getPorta(Context context) {
        return prefs(context).getInt(KEY_PORTA, PORTA_PADRAO);
    }

    public static void setPorta(Context context, int porta) {
        prefs(context).edit().putInt(KEY_PORTA, porta).apply();
    }

    /**
     * Token exigido no cabeçalho {@code Authorization}. Nunca devolvido vazio:
     * se o usuário apagar, um token novo é gerado, porque um serviço sem
     * autenticação responderia a qualquer coisa que chegue na porta.
     */
    public static String getToken(Context context) {
        SharedPreferences p = prefs(context);
        String token = p.getString(KEY_TOKEN, null);
        if (token == null || token.trim().isEmpty()) {
            token = gerarToken();
            p.edit().putString(KEY_TOKEN, token).apply();
        }
        return token;
    }

    public static void setToken(Context context, String token) {
        prefs(context).edit().putString(KEY_TOKEN, token == null ? "" : token.trim()).apply();
    }

    /** Identificador do aparelho, para o professor distinguir um teste de outro. */
    public static String getSerial(Context context) {
        SharedPreferences p = prefs(context);
        String serial = p.getString(KEY_SERIAL, null);
        if (serial == null || serial.trim().isEmpty()) {
            serial = "apk-" + Math.abs(ALEATORIO.nextInt()) + "-"
                    + Long.toHexString(System.currentTimeMillis());
            p.edit().putString(KEY_SERIAL, serial).apply();
        }
        return serial;
    }

    private static String gerarToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
