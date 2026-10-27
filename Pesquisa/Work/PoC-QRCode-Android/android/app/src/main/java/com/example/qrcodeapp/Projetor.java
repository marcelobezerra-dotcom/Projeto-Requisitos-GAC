package com.example.qrcodeapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Representa um projetor cadastrado: número, status de aluguel e dados de quem alugou.
 */
public class Projetor {

    public static final String STATUS_DISPONIVEL = "disponivel";
    public static final String STATUS_ALUGADO = "alugado";

    public String numero;
    public String status = STATUS_DISPONIVEL;
    public String locatario = "";
    public String telefone = "";
    public String dataInicio = "";
    public String dataDevolucao = "";

    public Projetor() {
    }

    public Projetor(String numero) {
        this.numero = normalizarNumero(numero);
    }

    public boolean isAlugado() {
        return STATUS_ALUGADO.equals(status);
    }

    /**
     * Padroniza o identificador do projetor para que "4", " 04 " e "p-04" sejam
     * sempre comparados da mesma forma.
     *
     * <p>Sem isso, "4" e "04" viravam dois projetores diferentes na lista e a
     * ordenação ficava inconsistente.
     *
     * @return o número normalizado, ou string vazia se a entrada for inválida.
     *         Espaços internos são rejeitados em vez de sumirem: "4 5" não pode
     *         virar o projetor "45" sem ninguém perceber.
     */
    public static String normalizarNumero(String bruto) {
        if (bruto == null) {
            return "";
        }
        String s = bruto.trim();
        if (s.isEmpty()) {
            return "";
        }
        for (int i = 0; i < s.length(); i++) {
            if (!isCaracterePermitido(s.charAt(i))) {
                return "";
            }
        }
        if (isSomenteDigitos(s)) {
            // Remove zeros à esquerda, mas mantém um dígito no mínimo ("000" -> "0").
            int inicio = 0;
            while (inicio < s.length() - 1 && s.charAt(inicio) == '0') {
                inicio++;
            }
            s = s.substring(inicio);
        }
        return s.toUpperCase(Locale.ROOT);
    }

    public static boolean isNumeroValido(String normalizado) {
        return !normalizado.isEmpty();
    }

    public static boolean isSomenteDigitos(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    private static boolean isCaracterePermitido(char c) {
        return (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == '-' || c == '_' || c == '.';
    }

    /**
     * Conteúdo que vai dentro do QR Code deste projetor.
     */
    public String conteudoQrCode() {
        return "projetor:" + numero;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("numero", numero);
        o.put("status", status);
        o.put("locatario", locatario);
        o.put("telefone", telefone);
        o.put("dataInicio", dataInicio);
        o.put("dataDevolucao", dataDevolucao);
        return o;
    }

    public static Projetor fromJson(JSONObject o) throws JSONException {
        Projetor p = new Projetor(o.optString("numero"));
        p.status = o.optString("status", STATUS_DISPONIVEL);
        p.locatario = o.optString("locatario", "");
        p.telefone = o.optString("telefone", "");
        p.dataInicio = o.optString("dataInicio", "");
        p.dataDevolucao = o.optString("dataDevolucao", "");
        return p;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Projetor)) {
            return false;
        }
        Projetor p = (Projetor) outro;
        return numero != null ? numero.equals(p.numero) : p.numero == null;
    }

    @Override
    public int hashCode() {
        return numero == null ? 0 : numero.hashCode();
    }
}
