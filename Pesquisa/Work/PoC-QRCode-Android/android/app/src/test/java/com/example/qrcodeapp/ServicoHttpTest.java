package com.example.qrcodeapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Sobe o servidor HTTP de verdade (em loopback, na porta 0 = efêmera) e fala
 * com ele por HTTP, o mesmo caminho que o {@code test-api.ps1} usa do PC.
 *
 * <p>Estes testes existem porque o contrato com o professor é justamente o que
 * o app responde: um JSON com nome de campo errado passa no build e só falha
 * quando alguém roda o script.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class ServicoHttpTest {

    private ServicoHttp servidor;
    private String token;
    private String base;

    @Before
    public void setUp() throws IOException {
        Context contexto = ApplicationProvider.getApplicationContext();
        token = ServicoConfig.getToken(contexto);
        // Porta 0 deixa o SO escolher uma livre, evitando conflito entre testes
        // e com a 8787 que o professor usa.
        servidor = new ServicoHttp(contexto, 0);
        servidor.start(2000, false);
        base = "http://127.0.0.1:" + servidor.getListeningPort();
    }

    @After
    public void tearDown() {
        if (servidor != null) {
            servidor.stop();
        }
    }

    @Test
    public void healthComTokenValidoRetorna200() throws Exception {
        Resposta r = requisitar("GET", "/health", token);
        assertEquals(200, r.status);
        JSONObject corpo = new JSONObject(r.corpo);
        assertEquals("ok", corpo.getString("status"));
        assertTrue("versao deve vir preenchida", corpo.getString("version").length() > 0);
        assertNotNull("device deve existir para o professor identificar o aparelho",
                corpo.getJSONObject("device"));
    }

    @Test
    public void healthSemTokenRetorna401() throws Exception {
        Resposta r = requisitar("GET", "/health", null);
        assertEquals(401, r.status);
    }

    @Test
    public void healthComTokenErradoRetorna403() throws Exception {
        // 403 e não 401: token presente e errado é problema de arquivo trocado,
        // e a distinção ajuda a diagnosticar o appnfc-test-token.txt.
        Resposta r = requisitar("GET", "/health", token + "x");
        assertEquals(403, r.status);
    }

    @Test
    public void authorizationSemPrefixoBearerRetorna401() throws Exception {
        Resposta r = requisitar("GET", "/health", null, "Basic " + token);
        assertEquals(401, r.status);
    }

    @Test
    public void rotaDesconhecidaRetorna404() throws Exception {
        Resposta r = requisitar("GET", "/nao/existe", token);
        assertEquals(404, r.status);
    }

    @Test
    public void metodoErradoNaRotaRetorna404() throws Exception {
        // POST em /health: o cliente tem a URL certa e o verbo errado, e um 404
        // ainda identifica a falha melhor que um 500.
        Resposta r = requisitar("POST", "/health", token);
        assertEquals(404, r.status);
    }

    @Test
    public void nfcReadRetorna501E() throws Exception {
        // O script chama esse endpoint; responder 501 explica o motivo, enquanto
        // 404 pareceria que a rota não existe.
        Resposta r = requisitar("POST", "/v1/nfc/read", token);
        assertEquals(501, r.status);
        assertEquals("not_implemented", new JSONObject(r.corpo).getString("status"));
    }

    @Test
    public void qrReadSemCameraRetornaTimeoutE() throws Exception {
        // Nenhuma Activity na frente, então ninguém atende o pedido: o servidor
        // precisa devolver 408 em vez de segurar a conexão até o cliente desistir.
        servidor.stop();
        ServicoHttp semTela = new ServicoHttp(
                ApplicationProvider.getApplicationContext(), 0);
        semTela.start(2000, false);
        try {
            HttpURLConnection conexao = abrir("POST", semTela.getListeningPort(),
                    "/v1/qr/read", token);
            conexao.setReadTimeout(45_000);
            int status = conexao.getResponseCode();
            String corpo = ler(conexao, status);
            assertEquals(408, status);
            assertEquals("timeout", new JSONObject(corpo).getString("status"));
        } finally {
            semTela.stop();
            // o tearDown para o servidor original, recriado abaixo se necessário
            servidor = null;
        }
    }

    @Test
    public void auditLogsAceitaLimitePadrao() throws Exception {
        requisitar("GET", "/health", token);
        Resposta r = requisitar("GET", "/v1/audit/logs?limit=20&offset=0", token);
        assertEquals(200, r.status);
        JSONObject corpo = new JSONObject(r.corpo);
        assertEquals(20, corpo.getInt("limit"));
        assertEquals(0, corpo.getInt("offset"));
        assertTrue("o GET /health anterior deve aparecer no log", corpo.getInt("total") >= 1);
        assertNotNull(corpo.getJSONArray("items"));
    }

    @Test
    public void auditLogsRejeitaLimiteForaDaFaixa() throws Exception {
        // O script valida 1..50 antes de chamar; o servidor precisa concordar,
        // senão os dois discordam sobre o que é válido.
        assertEquals(400, requisitar("GET", "/v1/audit/logs?limit=0", token).status);
        assertEquals(400, requisitar("GET", "/v1/audit/logs?limit=51", token).status);
        assertEquals(400, requisitar("GET", "/v1/audit/logs?limit=abc", token).status);
        assertEquals(400, requisitar("GET", "/v1/audit/logs?offset=-1", token).status);
    }

    @Test
    public void auditLogsGuardaCorrelationId() throws Exception {
        HttpURLConnection conexao = abrir("GET", servidor.getListeningPort(),
                "/health", token);
        conexao.setRequestProperty("X-Correlation-Id", "teste-123");
        conexao.getResponseCode();
        // O professor casa requisição e resposta pelo id; se não for devolvido,
        // o log dele fica sem forma de ligar um resultado ao pedido.
        assertEquals("teste-123", conexao.getHeaderField("X-Correlation-Id"));
    }

    private Resposta requisitar(String metodo, String caminho, String token) throws IOException {
        return requisitar(metodo, caminho, token, token == null ? null : "Bearer " + token);
    }

    private Resposta requisitar(String metodo, String caminho, String token,
                                String cabecalhoAuthorization) throws IOException {
        HttpURLConnection conexao = abrir(metodo, servidor.getListeningPort(), caminho,
                token, cabecalhoAuthorization);
        int status = conexao.getResponseCode();
        return new Resposta(status, ler(conexao, status));
    }

    private HttpURLConnection abrir(String metodo, int porta, String caminho, String token)
            throws IOException {
        return abrir(metodo, porta, caminho, token, "Bearer " + token);
    }

    private HttpURLConnection abrir(String metodo, int porta, String caminho, String token,
                                    String cabecalhoAuthorization) throws IOException {
        URL url = new URL("http://127.0.0.1:" + porta + caminho);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod(metodo);
        conexao.setConnectTimeout(5_000);
        conexao.setReadTimeout(45_000);
        if (cabecalhoAuthorization != null) {
            conexao.setRequestProperty("Authorization", cabecalhoAuthorization);
        }
        conexao.setRequestProperty("X-Correlation-Id", "teste-123");
        if ("POST".equals(metodo)) {
            conexao.setDoOutput(true);
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.getOutputStream().write("{}".getBytes(Charset.forName("UTF-8")));
        }
        return conexao;
    }

    private static String ler(HttpURLConnection conexao, int status) throws IOException {
        InputStream entrada = status >= 400 ? conexao.getErrorStream() : conexao.getInputStream();
        if (entrada == null) {
            return "";
        }
        StringBuilder texto = new StringBuilder();
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(entrada))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                texto.append(linha);
            }
        }
        return texto.toString();
    }

    private static final class Resposta {
        final int status;
        final String corpo;

        Resposta(int status, String corpo) {
            this.status = status;
            this.corpo = corpo;
        }
    }
}
