package com.example.qrcodeapp;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.iki.elonen.NanoHTTPD;

/**
 * Servidor HTTP embarcado no app, com os mesmos endpoints que o
 * {@code test-api.ps1} do professor consome.
 *
 * <p>Por que o app é o servidor: o script de teste se conecta <em>ao celular</em>
 * ("Não foi possível conectar ao celular em 192.168.0.104:8787"). O app abre a
 * porta, e o PC é o cliente. Assim o mesmo aparelho que lê o QR Code também
 * expõe essa leitura para as ferramentas de teste.
 *
 * <p>Contrato implementado — os corpos de resposta são propostas deste PoC. O
 * script só faz {@code ConvertTo-Json} do retorno, então qualquer objeto serve
 * para ele; estas chaves é que qualquer outro consumidor vai precisar:
 * <pre>
 * GET  /health                     -&gt; { status, service, version, device, endpoints, uptimeMs }
 * POST /v1/qr/read                 -&gt; { status, text, format, durationMs }
 * GET  /v1/audit/logs?limit&amp;offset -&gt; { total, limit, offset, items: [...] }
 * POST /v1/nfc/read                -&gt; 501 { status: "not_implemented", ... }
 * </pre>
 *
 * <p>Todas exigem {@code Authorization: Bearer <token>}. O
 * {@code X-Correlation-Id} recebido é ecoado de volta, para casar requisição e
 * resposta no log do professor.
 *
 * <p>A biblioteca declara o pacote {@code fi.iki.elonen} sob o groupId
 * {@code org.nanohttpd} — é uma peculiaridade histórica dela, não um erro.
 */
public class ServicoHttp extends NanoHTTPD {

    private static final String TAG = "ServicoHttp";
    private static final String SERVICE_NAME = "aluguel-projetores-qr";
    private static final String JSON_UTF8 = "application/json; charset=utf-8";

    private final Context contexto;
    private final long iniciadoEm = System.currentTimeMillis();
    private final List<RegistroAuditoria> auditoria =
            Collections.synchronizedList(new ArrayList<RegistroAuditoria>());

    /** Teto do log em memória. O script só pede 50 por vez, então sobra folga. */
    private static final int MAX_REGISTROS = 200;

    public ServicoHttp(Context contexto, int porta) {
        super("0.0.0.0", porta);
        this.contexto = contexto.getApplicationContext();
    }

    /** Loopback do próprio aparelho, para diagnóstico sem depender da rede. */
    public String urlLocal() {
        return "http://127.0.0.1:" + getListeningPort() + "/";
    }

    @Override
    public Response serve(IHTTPSession session) {
        String metodo = session.getMethod().name();
        String uri = session.getUri();
        String correlationId = cabecalho(session.getHeaders(), "X-Correlation-Id");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        long inicio = System.currentTimeMillis();

        Response resposta;
        try {
            resposta = encaminhar(session, metodo, uri);
        } catch (Exception e) {
            Log.e(TAG, "Falha ao tratar " + metodo + " " + uri, e);
            resposta = json(Response.Status.INTERNAL_ERROR,
                    erro("internal_error", e.getMessage() == null
                            ? e.getClass().getSimpleName() : e.getMessage()));
        }

        resposta.addHeader("X-Correlation-Id", correlationId);
        resposta.addHeader("X-Service", SERVICE_NAME);
        int status = resposta.getStatus().getRequestStatus();
        registrar(metodo, uri, status, correlationId,
                System.currentTimeMillis() - inicio, session.getParms());
        return resposta;
    }

    private Response encaminhar(IHTTPSession session, String metodo, String uri) throws Exception {
        // Autenticação antes de qualquer rota: um endpoint descoberto não pode
        // responder a quem não tem o token.
        Response semToken = validarToken(session.getHeaders());
        if (semToken != null) {
            return semToken;
        }

        if ("GET".equals(metodo) && "/health".equals(uri)) {
            return health();
        }
        if ("POST".equals(metodo) && "/v1/qr/read".equals(uri)) {
            return lerQr();
        }
        if ("POST".equals(metodo) && "/v1/nfc/read".equals(uri)) {
            // NfcAdapter exige hardware NFC e leitura com o app em primeiro plano;
            // não faz parte deste PoC. 501 explica melhor do que 404.
            return json(Response.Status.NOT_IMPLEMENTED, erro("not_implemented",
                    "Leitura NFC nao implementada neste PoC. O endpoint existe "
                            + "apenas para manter o contrato do test-api.ps1."));
        }
        if ("GET".equals(metodo) && "/v1/audit/logs".equals(uri)) {
            return logs(session.getParms());
        }
        return json(Response.Status.NOT_FOUND,
                erro("not_found", "Rota nao encontrada: " + metodo + " " + uri));
    }

    /**
     * Busca de cabeçalho sem depender de maiúsculas. O NanoHTTPD grava as
     * chaves em minúsculas, então {@code get("Authorization")} devolveria
     * {@code null} e todo REQUEST cairia no 401 — o erro é silencioso porque
     * o servidor responde normalmente, só que "sem token".
     */
    private static String cabecalho(Map<String, String> cabecalhos, String nome) {
        String direto = cabecalhos.get(nome);
        if (direto != null) {
            return direto;
        }
        for (Map.Entry<String, String> e : cabecalhos.entrySet()) {
            if (nome.equalsIgnoreCase(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * Confere {@code Authorization: Bearer <token>}. 401 sem o token, 403 com
     * token errado — a distinção ajuda a descobrir se o
     * {@code appnfc-test-token.txt} está com o conteúdo trocado.
     */
    private Response validarToken(Map<String, String> cabecalhos) {
        String authorization = cabecalho(cabecalhos, "Authorization");

        if (authorization == null || authorization.isEmpty()) {
            return json(Response.Status.UNAUTHORIZED, erro("unauthorized",
                    "Cabecalho Authorization ausente. Use: Authorization: Bearer <token>"));
        }
        if (!authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return json(Response.Status.UNAUTHORIZED, erro("unauthorized",
                    "Formato invalido. Use: Authorization: Bearer <token>"));
        }
        String recebido = authorization.substring(7).trim();
        if (!recebido.equals(ServicoConfig.getToken(contexto))) {
            return json(Response.Status.FORBIDDEN, erro("forbidden",
                    "Token invalido. Confira o conteudo do appnfc-test-token.txt."));
        }
        return null;
    }

    private Response health() {
        JSONObject corpo = new JSONObject();
        try {
            JSONObject device = new JSONObject();
            device.put("model", Build.MODEL);
            device.put("manufacturer", Build.MANUFACTURER);
            device.put("android", Build.VERSION.RELEASE);
            device.put("sdk", Build.VERSION.SDK_INT);
            device.put("serial", ServicoConfig.getSerial(contexto));

            corpo.put("status", "ok");
            corpo.put("service", SERVICE_NAME);
            corpo.put("version", "1.2");
            corpo.put("device", device);
            corpo.put("endpoints", new JSONArray()
                    .put("/health")
                    .put("/v1/qr/read")
                    .put("/v1/audit/logs"));
            corpo.put("uptimeMs", System.currentTimeMillis() - iniciadoEm);
        } catch (JSONException e) {
            return json(Response.Status.INTERNAL_ERROR,
                    erro("internal_error", "falha ao montar /health"));
        }
        return json(Response.Status.OK, corpo);
    }

    /**
     * Entrega a leitura à thread principal e espera o resultado. Se ninguém
     * atender o pedido — app em segundo plano e o Android bloqueando a
     * abertura da câmera — devolve timeout em vez de segurar a conexão.
     */
    private Response lerQr() {
        long inicio = System.currentTimeMillis();
        try {
            String conteudo = FilaLeituraQr.solicitarLeitura(FilaLeituraQr.TIMEOUT_MS);
            JSONObject corpo = new JSONObject();
            if (conteudo == null) {
                corpo.put("status", "timeout");
                corpo.put("message", "Nenhum QR Code lido em "
                        + (FilaLeituraQr.TIMEOUT_MS / 1000) + "s. "
                        + "Abra o app e toque em 'Ler QR Code'.");
                corpo.put("text", JSONObject.NULL);
                return json(Response.Status.REQUEST_TIMEOUT, corpo);
            }
            corpo.put("status", "ok");
            corpo.put("text", conteudo);
            corpo.put("format", "QR_CODE");
            corpo.put("durationMs", System.currentTimeMillis() - inicio);
            return json(Response.Status.OK, corpo);
        } catch (IllegalStateException e) {
            return json(Response.Status.INTERNAL_ERROR, erro("read_failed",
                    e.getMessage() == null ? "falha na leitura" : e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return json(Response.Status.INTERNAL_ERROR,
                    erro("interrupted", "leitura interrompida"));
        } catch (JSONException e) {
            return json(Response.Status.INTERNAL_ERROR,
                    erro("internal_error", "falha ao montar resposta de leitura"));
        }
    }

    /** Aplica as mesmas restrições do script: limit entre 1 e 50, offset >= 0. */
    private Response logs(Map<String, String> parametros) {
        int limit;
        int offset;
        try {
            limit = Integer.parseInt(
                    parametros.containsKey("limit") ? parametros.get("limit") : "20");
            offset = Integer.parseInt(
                    parametros.containsKey("offset") ? parametros.get("offset") : "0");
        } catch (NumberFormatException e) {
            return json(Response.Status.BAD_REQUEST,
                    erro("bad_request", "limit e offset devem ser numeros inteiros"));
        }
        if (limit < 1 || limit > 50) {
            return json(Response.Status.BAD_REQUEST, erro("bad_request", "use limit entre 1 e 50"));
        }
        if (offset < 0 || offset > 10000) {
            return json(Response.Status.BAD_REQUEST,
                    erro("bad_request", "use offset entre 0 e 10000"));
        }

        List<RegistroAuditoria> copia;
        synchronized (auditoria) {
            copia = new ArrayList<>(auditoria);
        }

        JSONArray items = new JSONArray();
        for (int i = offset; i < copia.size() && items.length() < limit; i++) {
            try {
                items.put(copia.get(i).paraJson());
            } catch (JSONException e) {
                Log.w(TAG, "Registro de auditoria ignorado: " + e.getMessage());
            }
        }

        JSONObject corpo = new JSONObject();
        try {
            corpo.put("total", copia.size());
            corpo.put("limit", limit);
            corpo.put("offset", offset);
            corpo.put("items", items);
        } catch (JSONException e) {
            return json(Response.Status.INTERNAL_ERROR,
                    erro("internal_error", "falha ao montar logs"));
        }
        return json(Response.Status.OK, corpo);
    }

    private void registrar(String metodo, String uri, int status, String correlationId,
                           long duracaoMs, Map<String, String> parametros) {
        auditoria.add(new RegistroAuditoria(metodo, uri, status, correlationId, duracaoMs, parametros));
        while (auditoria.size() > MAX_REGISTROS) {
            auditoria.remove(0);
        }
    }

    private static JSONObject erro(String codigo, String mensagem) {
        JSONObject corpo = new JSONObject();
        try {
            corpo.put("status", codigo);
            corpo.put("message", mensagem);
        } catch (JSONException ignorada) {
            // Nenhuma das duas chaves deveria falhar.
        }
        return corpo;
    }

    /**
     * O construtor de {@code Response} é protected e inacessível de fora do
     * pacote {@code fi.iki.elonen}; os helpers estáticos da própria NanoHTTPD
     * são o caminho suportado.
     */
    private static Response json(Response.Status status, JSONObject corpo) {
        Response resposta = newFixedLengthResponse(status, JSON_UTF8, corpo.toString());
        resposta.addHeader("Cache-Control", "no-store");
        return resposta;
    }

    /** Um registro do log de auditoria, no formato entregue em /v1/audit/logs. */
    private static final class RegistroAuditoria {

        private final long timestamp = System.currentTimeMillis();
        private final String metodo;
        private final String caminho;
        private final int status;
        private final String correlationId;
        private final long duracaoMs;
        private final List<String[]> parametros = new ArrayList<>();

        RegistroAuditoria(String metodo, String caminho, int status, String correlationId,
                          long duracaoMs, Map<String, String> query) {
            this.metodo = metodo;
            this.caminho = caminho;
            this.status = status;
            this.correlationId = correlationId;
            this.duracaoMs = duracaoMs;
            for (Map.Entry<String, String> e : query.entrySet()) {
                if (parametros.size() < 20) {
                    parametros.add(new String[]{e.getKey(), e.getValue()});
                }
            }
        }

        JSONObject paraJson() throws JSONException {
            JSONObject o = new JSONObject();
            o.put("timestamp", timestamp);
            o.put("method", metodo);
            o.put("path", caminho);
            o.put("status", status);
            o.put("correlationId", correlationId);
            o.put("durationMs", duracaoMs);
            if (!parametros.isEmpty()) {
                JSONObject query = new JSONObject();
                for (String[] par : parametros) {
                    query.put(par[0], par[1]);
                }
                o.put("query", query);
            }
            return o;
        }
    }
}
